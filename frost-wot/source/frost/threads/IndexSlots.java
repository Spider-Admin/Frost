/*
  IndexSlots.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.threads;

import java.sql.*;
import java.util.*;

import frost.storage.*;

/**
 * Class provides functionality to track used index slots
 * for upload and download.
 */
public class IndexSlots {

    public static final int FILELISTS = 1;
    public static final int MESSAGES  = 2;
    
/*
 * - find first download slot (unused,unlocked)
 * - find next download slot (unused,unlocked)
 * - set download slot used (set used)
 * 
 * - find first upload slot (unused,unlocked; locks!)
 * - find next upload slot (unused,unlocked; locks!)
 * - set upload slot used (set used, unlock!)
 * - set upload slot unlocked (set unlocked)
 */    
    
    // ensure that multiple queries are run in a transaction
    private static Object syncObj = new Object();

    private int indexName;
    private String boardName;
    
    private TransferLayerDatabase db;

    private static final String SQL_DDL = 
        "CREATE TABLE INDEXSLOTS (indexname INT, boardname VARCHAR, date DATE, index INT, used BOOLEAN, locked BOOLEAN)";
    private static final String SQL_INSERT =
        "INSERT INTO INDEXSLOTS (indexname,boardname,date,index,used,locked) VALUES (?,?,?,?,?,?)";
    
    private static final String SQL_UPDATE_BOTH =
        "UPDATE INDEXSLOTS SET used=?,locked=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
    private static final String SQL_UPDATE_USED =
        "UPDATE INDEXSLOTS SET used=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
    private static final String SQL_UPDATE_LOCKED =
        "UPDATE INDEXSLOTS SET locked=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
    
    private static final String SQL_MAX_USED_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? "+
        "AND ( used=TRUE OR locked=TRUE ) ORDER BY index DESC";
    private static final String SQL_NEXT_MAX_USED_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
        "AND ( used=TRUE OR locked=TRUE ) ORDER BY index DESC";
    private static final String SQL_MAX_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? ORDER BY index DESC";
    private static final String SQL_NEXT_UNUSED_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
        "AND used=FALSE AND locked=FALSE ORDER BY index";
    
    private PreparedStatement ps_INSERT = null;
    private PreparedStatement ps_UPDATE_BOTH = null;
    private PreparedStatement ps_UPDATE_USED = null;
    private PreparedStatement ps_UPDATE_LOCKED = null;
    private PreparedStatement ps_MAX_USED_SLOT = null;
    private PreparedStatement ps_NEXT_MAX_USED_SLOT = null;
    private PreparedStatement ps_MAX_SLOT = null;
    private PreparedStatement ps_NEXT_UNUSED_SLOT = null;

    public IndexSlots(int indexName, String boardName) {
        this.boardName = boardName;
        this.indexName = indexName;
        
        db = TransferLayerDatabase.getInstance();
    }
    
    public static List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_DDL);
        return lst;
    }
    
    private PreparedStatement getPsINSERT() throws SQLException {
        if( ps_INSERT == null ) {
            ps_INSERT = db.prepare(SQL_INSERT);
        }
        return ps_INSERT;
    }

    private PreparedStatement getPsUPDATE_BOTH() throws SQLException {
        if( ps_UPDATE_BOTH == null ) {
            ps_UPDATE_BOTH = db.prepare(SQL_UPDATE_BOTH);
        }
        return ps_UPDATE_BOTH;
    }

    private PreparedStatement getPsUPDATE_USED() throws SQLException {
        if( ps_UPDATE_USED == null ) {
            ps_UPDATE_USED = db.prepare(SQL_UPDATE_USED);
        }
        return ps_UPDATE_USED;
    }

    private PreparedStatement getPsUPDATE_LOCKED() throws SQLException {
        if( ps_UPDATE_LOCKED == null ) {
            ps_UPDATE_LOCKED = db.prepare(SQL_UPDATE_LOCKED);
        }
        return ps_UPDATE_LOCKED;
    }

    private PreparedStatement getPsNEXT_UNUSED_SLOT() throws SQLException {
        if( ps_NEXT_UNUSED_SLOT == null ) {
            ps_NEXT_UNUSED_SLOT = db.prepare(SQL_NEXT_UNUSED_SLOT);
        }
        return ps_NEXT_UNUSED_SLOT;
    }

    private PreparedStatement getPsMAX_USED_SLOT() throws SQLException {
        if( ps_MAX_USED_SLOT == null ) {
            ps_MAX_USED_SLOT = db.prepare(SQL_MAX_USED_SLOT);
        }
        return ps_MAX_USED_SLOT;
    }

    private PreparedStatement getPsNEXT_MAX_USED_SLOT() throws SQLException {
        if( ps_NEXT_MAX_USED_SLOT == null ) {
            ps_NEXT_MAX_USED_SLOT = db.prepare(SQL_NEXT_MAX_USED_SLOT);
        }
        return ps_NEXT_MAX_USED_SLOT;
    }

    private PreparedStatement getPsMAX_SLOT() throws SQLException {
        if( ps_MAX_SLOT == null ) {
            ps_MAX_SLOT = db.prepare(SQL_MAX_SLOT);
        }
        return ps_MAX_SLOT;
    }

    public void close() {
        // close created prepared statements
        maybeClose(ps_INSERT); ps_INSERT = null;
        maybeClose(ps_UPDATE_BOTH); ps_UPDATE_BOTH = null;
        maybeClose(ps_UPDATE_USED); ps_UPDATE_USED = null;
        maybeClose(ps_UPDATE_LOCKED); ps_UPDATE_LOCKED = null;
        maybeClose(ps_MAX_USED_SLOT); ps_MAX_USED_SLOT = null;
        maybeClose(ps_NEXT_MAX_USED_SLOT); ps_NEXT_MAX_USED_SLOT = null;
        maybeClose(ps_MAX_SLOT); ps_MAX_SLOT = null;
        maybeClose(ps_NEXT_UNUSED_SLOT); ps_NEXT_UNUSED_SLOT = null;
    }

    private void maybeClose(PreparedStatement ps) {
        if( ps != null ) {
            try {
                ps.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // find first unused, unlocked
    public int findFirstDownloadSlot(java.sql.Date date) throws SQLException {
        return findNextDownloadSlot(-1, date);
    }

    // find next unused, unlocked
    public int findNextDownloadSlot(int beforeIndex, java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            ResultSet rs = executePsNEXT_UNUSED_SLOT(beforeIndex, date);
            
            int nextFreeSlot = -1; 
            if( rs.next() ) {
                nextFreeSlot = rs.getInt(1);
            }
            rs.close();
            
            if( nextFreeSlot < 0 ) {
                // no free slot in table, get MAX+1 (max is a used/locked slot)
                ResultSet rs2 = executePsMAX_SLOT(date);
                if( rs2.next() ) {
                    nextFreeSlot = rs2.getInt(1);
                    nextFreeSlot++;
                    executePsINSERT(nextFreeSlot, false, false, date);
                }
                rs2.close();
            }

            if( nextFreeSlot < 0 ) {
                // still not set? no single slot is in database for today, use first free slot
                nextFreeSlot = 0;
                executePsINSERT(nextFreeSlot, false, false, date);
            }
    
            return nextFreeSlot;
        }
    }

    // set used
    public void setDownloadSlotUsed(int index, java.sql.Date date) throws SQLException {
        updateOrInsertSlotUsed(index, true, date);
    }

    // find first unused, unlocked, and lock!
    public int findFirstUploadSlot(java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            ResultSet rs = executePsMAX_USED_SLOT(date);
            
            int freeUploadIndex = 0; 
            if( rs.next() ) {
                int maxUsedIndex = rs.getInt(1);
                freeUploadIndex = maxUsedIndex + 1;
            } // else we use index=0, it is either not there or not used
            rs.close();

            // lock slot
            updateOrInsertSlotLocked(freeUploadIndex, true, date);
    
            return freeUploadIndex;
        }
    }

    // find next unused, unlocked, and lock!
    public int findNextUploadSlot(int beforeIndex, java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            ResultSet rs = executePsNEXT_MAX_USED_SLOT(beforeIndex, date);
            
            int freeUploadIndex = beforeIndex+1; 
            if( rs.next() ) {
                int maxUsedIndex = rs.getInt(1);
                freeUploadIndex = maxUsedIndex + 1;
            } // else we use before index + 1 
            rs.close();

            // lock slot
            updateOrInsertSlotLocked(freeUploadIndex, true, date);
    
            return freeUploadIndex;
        }
    }

    // set used and unlock!
    public void setUploadSlotUsed(int index, java.sql.Date date) throws SQLException {
        updateOrInsertSlotBoth(index, true, false, date);
    }

    // set unlocked
    public void setUploadSlotUnlocked(int index, java.sql.Date date) throws SQLException {
        updateOrInsertSlotLocked(index, false, date);
    }

    private void updateOrInsertSlotUsed(int index, boolean used, java.sql.Date date) throws SQLException {
        boolean updateOk = false;
        try {
            if( executePsUPDATE_USED(index, used, date) == 1 ) {
                updateOk = true;
            }
        } catch(SQLException e) {
            // no record to update, try an insert
        }
        
        if( updateOk == false ) {
            if( executePsINSERT(index, used, false, date) != 1 ) {
                throw new SQLException("update or insert of slot failed!");
            }
        }
    }

    private void updateOrInsertSlotLocked(int index, boolean locked, java.sql.Date date) throws SQLException {
        boolean updateOk = false;
        try {
            if( executePsUPDATE_LOCKED(index, locked, date) == 1 ) {
                updateOk = true;
            }
        } catch(SQLException e) {
            // no record to update, try an insert
        }
        
        if( updateOk == false ) {
            if( executePsINSERT(index, false, locked, date) != 1 ) {
                throw new SQLException("update or insert of slot failed!");
            }
        }
    }

    private void updateOrInsertSlotBoth(int index, boolean used, boolean locked, java.sql.Date date) throws SQLException {
        boolean updateOk = false;
        try {
            if( executePsUPDATE_BOTH(index, used, locked, date) == 1 ) {
                updateOk = true;
            }
        } catch(SQLException e) {
            // no record to update, try an insert
        }
        
        if( updateOk == false ) {
            if( executePsINSERT(index, used, locked, date) != 1 ) {
                throw new SQLException("update or insert of slot failed!");
            }
        }
    }

    private int executePsUPDATE_BOTH(int index, boolean used, boolean locked, java.sql.Date date) throws SQLException {
        // "UPDATE INDEXSLOTS SET used=?,locked=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
        PreparedStatement ps = getPsUPDATE_BOTH();
        ps.setBoolean(1, used);
        ps.setBoolean(2, locked);
        ps.setInt(3, indexName);
        ps.setString(4, boardName);
        ps.setDate(5, date);
        ps.setInt(6, index);
        return ps.executeUpdate();
    }

    private int executePsUPDATE_USED(int index, boolean used, java.sql.Date date) throws SQLException {
        // "UPDATE INDEXSLOTS SET used=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
        PreparedStatement ps = getPsUPDATE_USED();
        ps.setBoolean(1, used);
        ps.setInt(2, indexName);
        ps.setString(3, boardName);
        ps.setDate(4, date);
        ps.setInt(5, index);
        return ps.executeUpdate();
    }

    private int executePsUPDATE_LOCKED(int index, boolean locked, java.sql.Date date) throws SQLException {
        // "UPDATE INDEXSLOTS SET locked=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
        PreparedStatement ps = getPsUPDATE_LOCKED();
        ps.setBoolean(1, locked);
        ps.setInt(2, indexName);
        ps.setString(3, boardName);
        ps.setDate(4, date);
        ps.setInt(5, index);
        return ps.executeUpdate();
    }

    private int executePsINSERT(int index, boolean used, boolean locked, java.sql.Date date) throws SQLException {
        // "INSERT INTO INDEXSLOTS (indexname,boardname,date,index,used,locked) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps2 = getPsINSERT();
        ps2.setInt(1, indexName);
        ps2.setString(2, boardName);
        ps2.setDate(3, date);
        ps2.setInt(4, index);
        ps2.setBoolean(5, used);
        ps2.setBoolean(6, locked);
        return ps2.executeUpdate();
    }

    private ResultSet executePsMAX_USED_SLOT(java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? "+
        // "AND ( used=TRUE OR locked=TRUE ) ORDER BY index DESC";
        PreparedStatement ps = getPsMAX_USED_SLOT();
        ps.setInt(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        
        ResultSet rs = ps.executeQuery();
        return rs;
    }

    private ResultSet executePsNEXT_UNUSED_SLOT(int beforeIndex, java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
        // "AND used=FALSE AND locked=FALSE ORDER BY index";
        PreparedStatement ps = getPsNEXT_UNUSED_SLOT();
        ps.setInt(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        ps.setInt(4, beforeIndex);
        
        ResultSet rs = ps.executeQuery();
        return rs;
    }
    
    private ResultSet executePsMAX_SLOT(java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? ORDER BY index DESC"
        PreparedStatement ps = getPsMAX_SLOT();
        ps.setInt(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        ResultSet rs = ps.executeQuery();
        return rs;
    }
    
    private ResultSet executePsNEXT_MAX_USED_SLOT(int beforeIndex, java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
        // "AND ( used=TRUE OR locked=TRUE ) ORDER BY index DESC";
        PreparedStatement ps = getPsNEXT_MAX_USED_SLOT();
        ps.setInt(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        ps.setInt(4, beforeIndex);
        
        ResultSet rs = ps.executeQuery();
        return rs;
    }

//    public static void main(String[] args) throws Throwable {
//        DatabaseTransferLayer.initialize();
//        IndexSlots is = new IndexSlots("indexName", "boardName", new java.sql.Date(System.currentTimeMillis()));
//        int i=0;
//
//        System.out.println("a="+is.findFirstFreeDownloadSlot());
//        System.out.println("b="+is.findFirstFreeUploadSlot());
//        
//        ResultSet rs = DatabaseTransferLayer.getInstance().createStatement().executeQuery("SELECT * FROM INDEXSLOTS");
//        Database.dump(rs);
//        
//        is.setSlotUsed(i);
//        i=is.findNextFreeSlot(i);
//        i=is.findNextFreeSlot(i);
//        i=is.findNextFreeSlot(i);
//        i=is.findNextFreeSlot(i);
//        is.setSlotUsed(i);
//        
//        rs = DatabaseTransferLayer.getInstance().createStatement().executeQuery("SELECT * FROM INDEXSLOTS");
//        Database.dump(rs);
//        
//        System.out.println("a="+is.findFirstFreeDownloadSlot());
//        System.out.println("b="+is.findFirstFreeUploadSlot());
//    }
    
}
