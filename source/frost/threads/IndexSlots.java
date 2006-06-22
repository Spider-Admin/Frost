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
import frost.transferlayer.*;

/**
 * Class provides functionality to track used index slots
 * for upload and download.
 */
public class IndexSlots implements IndexFileUploaderCallback {
    
    // ensure that multiple queries are run in a transaction
    private static Object syncObj = new Object();

    private String indexName;
    private String boardName;
    
    private TransferLayerDatabase db;

    private static final String SQL_DDL = 
        "CREATE TABLE INDEXSLOTS (indexname VARCHAR, boardname VARCHAR, date DATE, index INT, used BOOLEAN)";
    private static final String SQL_INSERT =
        "INSERT INTO INDEXSLOTS (indexname,boardname,date,index,used) VALUES (?,?,?,?,?)";
    private static final String SQL_UPDATE =
        "UPDATE INDEXSLOTS SET used=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
    private static final String SQL_MAX_USED_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? "+
        "AND used=TRUE ORDER BY index DESC";
    private static final String SQL_MAX_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? ORDER BY index DESC";
    private static final String SQL_NEXT_UNUSED_SLOT =
        "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
        "AND used=FALSE ORDER BY index";
    
    private PreparedStatement ps_INSERT = null;
    private PreparedStatement ps_UPDATE = null;
    private PreparedStatement ps_MAX_USED_SLOT = null;
    private PreparedStatement ps_MAX_SLOT = null;
    private PreparedStatement ps_NEXT_UNUSED_SLOT = null;

    public IndexSlots(String indexName, String boardName) {
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

    private PreparedStatement getPsUPDATE() throws SQLException {
        if( ps_UPDATE == null ) {
            ps_UPDATE = db.prepare(SQL_UPDATE);
        }
        return ps_UPDATE;
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

    private PreparedStatement getPsMAX_SLOT() throws SQLException {
        if( ps_MAX_SLOT == null ) {
            ps_MAX_SLOT = db.prepare(SQL_MAX_SLOT);
        }
        return ps_MAX_SLOT;
    }

    public void close() {
        // close created prepared statements
        maybeClose(ps_INSERT); ps_INSERT = null;
        maybeClose(ps_UPDATE); ps_UPDATE = null;
        maybeClose(ps_MAX_USED_SLOT); ps_MAX_USED_SLOT = null;
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
    
    public int findFirstFreeDownloadSlot(java.sql.Date date) throws SQLException {
        return findNextFreeSlot(-1, date);
    }
    
    private ResultSet executePsMAX_USED_SLOT(java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? "+
        // "AND used=TRUE ORDER BY index DESC";
        PreparedStatement ps = getPsMAX_USED_SLOT();
        ps.setString(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        
        ResultSet rs = ps.executeQuery();
        return rs;
    }

    /**
     * First free upload slot is right behind last used slot.
     * @throws SQLException 
     */
    public int findFirstFreeUploadSlot(java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            ResultSet rs = executePsMAX_USED_SLOT(date);
            
            int freeUploadIndex = 0; 
            if( rs.next() ) {
                int maxUsedIndex = rs.getInt(1);
                freeUploadIndex = maxUsedIndex + 1;
            }
            rs.close();
            
            updateOrInsertSlot(freeUploadIndex, false, date);
    
            return freeUploadIndex;
        }
    }
    
    private ResultSet executePsNEXT_UNUSED_SLOT(int beforeIndex, java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
        // "AND used=FALSE ORDER BY index";
        PreparedStatement ps = getPsNEXT_UNUSED_SLOT();
        ps.setString(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        ps.setInt(4, beforeIndex);
        
        ResultSet rs = ps.executeQuery();
        return rs;
    }
    
    private ResultSet executePsMAX_SLOT(java.sql.Date date) throws SQLException {
        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? ORDER BY index DESC"
        PreparedStatement ps = getPsMAX_SLOT();
        ps.setString(1, indexName);
        ps.setString(2, boardName);
        ps.setDate(3, date);
        ResultSet rs = ps.executeQuery();
        return rs;
    }

    public int findNextFreeSlot(int beforeIndex, java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            ResultSet rs = executePsNEXT_UNUSED_SLOT(beforeIndex, date);
            
            int nextFreeSlot = -1; 
            if( rs.next() ) {
                nextFreeSlot = rs.getInt(1);
            }
            rs.close();
            
            if( nextFreeSlot < 0 ) {
                // no free slot in table, get MAX+1
                ResultSet rs2 = executePsMAX_SLOT(date);
                if( rs2.next() ) {
                    nextFreeSlot = rs2.getInt(1);
                    nextFreeSlot++;
                    executePsINSERT(nextFreeSlot, false, date);
                }
                rs2.close();
            }
    
            if( nextFreeSlot < 0 ) {
                // still not set? use first free slot
                nextFreeSlot = 0;
                executePsINSERT(nextFreeSlot, false, date);
            }
    
            return nextFreeSlot;
        }
    }

    public void setSlotUsed(int i, java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            updateOrInsertSlot(i, true, date);
        }
    }

    public void setSlotUnused(int i, java.sql.Date date) throws SQLException {
        synchronized(syncObj) {
            updateOrInsertSlot(i, false, date);
        }
    }

    private void updateOrInsertSlot(int index, boolean used, java.sql.Date date) throws SQLException {
        boolean updateOk = false;
        try {
            if( executePsUPDATE(index, used, date) == 1 ) {
                updateOk = true;
            }
        } catch(SQLException e) {
            // no record to update, try an insert
        }
        
        if( updateOk == false ) {
            if( executePsINSERT(index, used, date) != 1 ) {
                throw new SQLException("update or insert of slot failed!");
            }
        }
    }
    
    private int executePsUPDATE(int index, boolean used, java.sql.Date date) throws SQLException {
        // "UPDATE INDEXSLOTS SET used=? WHERE indexname=? AND boardname=? AND date=? AND index=?"
        PreparedStatement ps = getPsUPDATE();
        ps.setBoolean(1, used);
        ps.setString(2, indexName);
        ps.setString(3, boardName);
        ps.setDate(4, date);
        ps.setInt(5, index);
        return ps.executeUpdate();
    }

    private int executePsINSERT(int index, boolean used, java.sql.Date date) throws SQLException {
        // "INSERT INTO INDEXSLOTS (indexname,boardname,date,index,used) VALUES (?,?,?,?,?)";
        PreparedStatement ps2 = getPsINSERT();
        ps2.setString(1, indexName);
        ps2.setString(2, boardName);
        ps2.setDate(3, date);
        ps2.setInt(4, index);
        ps2.setBoolean(5, used);
        return ps2.executeUpdate();
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
