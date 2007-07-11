/*
  IndexSlotsDatabaseTable.java / Frost
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
package frost.util.migration.migrate0to1;

import java.sql.*;

import frost.storage.database.applayer.*;
import frost.storage.perst.*;

/**
 * Class provides functionality to track used index slots
 * for upload and download.
 */
public class IndexSlotsDatabaseTable {

//    public static final int FILELISTS = 1;
    public static final int MESSAGES  = 2;
    
    // ensure that multiple queries are run in a transaction
//    private static Object syncObj = new Object();

//    private int indexName;
//    private int boardIx;
//    
//    private AppLayerDatabase db;
//
//    private static final String SQL_DDL = 
//        "CREATE TABLE IF NOT EXISTS INDEXSLOTS (indexname INT, boardname INT, msgdate BIGINT, msgindex INT,"+
//        " wasdownloaded BOOLEAN, wasuploaded BOOLEAN, locked BOOLEAN,"+
//        " CONSTRAINT board_ref FOREIGN KEY (boardname) REFERENCES BOARDS (primkey) ON DELETE CASCADE,"+
//        " CONSTRAINT UNIQUE_INDICES_ONLY UNIQUE(indexname,boardname,msgdate,msgindex) )";
//    
//    private static final String SQL_INSERT =
//        "INSERT INTO INDEXSLOTS (indexname,boardname,msgdate,msgindex,wasdownloaded,wasuploaded,locked) VALUES (?,?,?,?,?,?,?)";
//
//    private static final String SQL_UPDATE_WASUPLOADED =
//        "UPDATE INDEXSLOTS SET wasuploaded=TRUE,locked=FALSE WHERE indexname=? AND boardname=? AND msgdate=? AND msgindex=?";
//    private static final String SQL_UPDATE_LOCKED =
//        "UPDATE INDEXSLOTS SET locked=? WHERE indexname=? AND boardname=? AND msgdate=? AND msgindex=?";
//    
//    // find highest used msgindex
//    private static final String SQL_NEXT_MAX_USED_SLOT = // TOP 1
//        "SELECT msgindex FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND msgdate=? AND msgindex>? "+
//        "AND ( wasdownloaded=TRUE OR wasuploaded=TRUE OR locked=TRUE ) ORDER BY msgindex DESC";
//    
//    private static final String SQL_MAX_SLOT = // TOP 1
//        "SELECT msgindex FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND msgdate=? ORDER BY msgindex DESC";
//
//    // downloading
//    private static final String SQL_NEXT_DOWNLOAD_SLOT = // TOP 1
//        "SELECT msgindex FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND msgdate=? AND msgindex>? "+
//        "AND wasdownloaded=FALSE AND locked=FALSE ORDER BY msgindex ASC";
//    private static final String SQL_UPDATE_WASDOWNLOADED =
//        "UPDATE INDEXSLOTS SET wasdownloaded=TRUE WHERE indexname=? AND boardname=? AND msgdate=? AND msgindex=?";

    private static final String SQL_MIGRATE_QUERY = 
        "SELECT boardname, msgdate, msgindex, wasdownloaded, wasuploaded "+
        "FROM INDEXSLOTS WHERE indexname=? ORDER BY boardname,msgdate";

//    private PreparedStatement ps_INSERT = null;
//    private PreparedStatement ps_UPDATE_WASUPLOADED = null;
//    private PreparedStatement ps_UPDATE_LOCKED = null;
//    private PreparedStatement ps_NEXT_MAX_USED_SLOT = null;
//    private PreparedStatement ps_MAX_SLOT = null;
//    private PreparedStatement ps_NEXT_UNUSED_SLOT = null;
//    private PreparedStatement ps_UPDATE_WASDOWNLOADED = null;
    
//    private static boolean dumped=false;

    /**
     * Called by Cleanup.
     */
    public IndexSlotsDatabaseTable() {
    }

//    public IndexSlotsDatabaseTable(int indexName, Board board) {
//        
//        this.boardIx = board.getPrimaryKey().intValue(); 
//        this.indexName = indexName;
//
//        db = AppLayerDatabase.getInstance();
//
//        if(!dumped) {
//        try {
//        System.out.println("--------------DUMP START, "+indexName+","+boardIx+"--------------------");
//        Statement s = db.createStatement();
//        dump(s.executeQuery("SELECT * FROM INDEXSLOTS"));
//        System.out.println("---------------------------------------------------------------");
//        }
//        catch(SQLException e) {
//            e.printStackTrace();
//        }
//        dumped=true;
//        }
//    }
    
//    public static List<String> getTableDDL() {
//        ArrayList<String> lst = new ArrayList<String>(1);
//        lst.add(SQL_DDL);
//        return lst;
//    }
//    
//    private PreparedStatement getPsINSERT() throws SQLException {
//        if( ps_INSERT == null ) {
//            ps_INSERT = db.prepareStatement(SQL_INSERT);
//        }
//        return ps_INSERT;
//    }
//
//    private PreparedStatement getPsUPDATE_WASUPLOADED() throws SQLException {
//        if( ps_UPDATE_WASUPLOADED == null ) {
//            ps_UPDATE_WASUPLOADED = db.prepareStatement(SQL_UPDATE_WASUPLOADED);
//        }
//        return ps_UPDATE_WASUPLOADED;
//    }
//
//    private PreparedStatement getPsUPDATE_LOCKED() throws SQLException {
//        if( ps_UPDATE_LOCKED == null ) {
//            ps_UPDATE_LOCKED = db.prepareStatement(SQL_UPDATE_LOCKED);
//        }
//        return ps_UPDATE_LOCKED;
//    }
//
//    private PreparedStatement getPsUPDATE_WASDOWNLOADED() throws SQLException {
//        if( ps_UPDATE_WASDOWNLOADED == null ) {
//            ps_UPDATE_WASDOWNLOADED = db.prepareStatement(SQL_UPDATE_WASDOWNLOADED);
//        }
//        return ps_UPDATE_WASDOWNLOADED;
//    }
//
//    private PreparedStatement getPsNEXT_DOWNLOAD_SLOT() throws SQLException {
//        if( ps_NEXT_UNUSED_SLOT == null ) {
//            ps_NEXT_UNUSED_SLOT = db.prepareStatement(SQL_NEXT_DOWNLOAD_SLOT);
//            ps_NEXT_UNUSED_SLOT.setMaxRows(1);
//        }
//        return ps_NEXT_UNUSED_SLOT;
//    }
//
//    private PreparedStatement getPsNEXT_MAX_USED_SLOT() throws SQLException {
//        if( ps_NEXT_MAX_USED_SLOT == null ) {
//            ps_NEXT_MAX_USED_SLOT = db.prepareStatement(SQL_NEXT_MAX_USED_SLOT);
//            ps_NEXT_MAX_USED_SLOT.setMaxRows(1);
//        }
//        return ps_NEXT_MAX_USED_SLOT;
//    }
//
//    private PreparedStatement getPsMAX_SLOT() throws SQLException {
//        if( ps_MAX_SLOT == null ) {
//            ps_MAX_SLOT = db.prepareStatement(SQL_MAX_SLOT);
//            ps_MAX_SLOT.setMaxRows(1);
//        }
//        return ps_MAX_SLOT;
//    }
//
//    public void close() {
//        // close created prepared statements
//        maybeClose(ps_INSERT); ps_INSERT = null;
//        maybeClose(ps_UPDATE_WASUPLOADED); ps_UPDATE_WASUPLOADED = null;
//        maybeClose(ps_UPDATE_WASDOWNLOADED); ps_UPDATE_WASDOWNLOADED = null;
//        maybeClose(ps_UPDATE_LOCKED); ps_UPDATE_LOCKED = null;
//        maybeClose(ps_NEXT_MAX_USED_SLOT); ps_NEXT_MAX_USED_SLOT = null;
//        maybeClose(ps_MAX_SLOT); ps_MAX_SLOT = null;
//        maybeClose(ps_NEXT_UNUSED_SLOT); ps_NEXT_UNUSED_SLOT = null;
//    }
//
//    private void maybeClose(PreparedStatement ps) {
//        if( ps != null ) {
//            try {
//                ps.close();
//            } catch(SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    
//    // find first not downloaded, unlocked
//    public int findFirstDownloadSlot(long date) throws SQLException {
//        return findNextDownloadSlot(-1, date);
//    }
//
//    // find next not downloaded, unlocked
//    public int findNextDownloadSlot(int beforeIndex, long date) throws SQLException {
//        synchronized(syncObj) {
//            ResultSet rs = executePsNEXT_DOWNLOAD_SLOT(beforeIndex, date);
//            
//            int nextFreeSlot = -1; 
//            if( rs.next() ) {
//                nextFreeSlot = rs.getInt(1);
//            }
//            rs.close();
//            
//            if( nextFreeSlot < 0 ) {
//                // no free slot in table, get MAX+1 (max is a used/locked slot)
//                ResultSet rs2 = executePsMAX_SLOT(date);
//                if( rs2.next() ) {
//                    nextFreeSlot = rs2.getInt(1);
//                    nextFreeSlot++;
//                    executePsINSERT(nextFreeSlot, false, false, false, date);
//                }
//                rs2.close();
//            }
//
//            if( nextFreeSlot < 0 ) {
//                // still not set? no single slot is in database for today, use first free slot
//                nextFreeSlot = 0;
//                executePsINSERT(nextFreeSlot, false, false, false, date);
//            }
//    
//            return nextFreeSlot;
//        }
//    }
//
//    // set used
//    public void setDownloadSlotUsed(int index, long date) throws SQLException {
//        boolean updateOk = false;
//        try {
//            if( executePsUPDATE_WASDOWNLOADED(index, date) == 1 ) {
//                updateOk = true;
//            }
//        } catch(SQLException e) {
//            // no record to update, try an insert
//        }
//        
//        if( updateOk == false ) {
//            if( executePsINSERT(index, true, false, false, date) != 1 ) {
//                throw new SQLException("update or insert of slot failed!");
//            }
//        }
//    }
//
//    // find first unused, unlocked, and lock!
//    public int findFirstUploadSlot(long date) throws SQLException {
//        synchronized(syncObj) {
//            ResultSet rs = executePsNEXT_MAX_USED_SLOT(-1, date);
//            
//            int freeUploadIndex = 0; 
//            if( rs.next() ) {
//                int maxUsedIndex = rs.getInt(1);
//                freeUploadIndex = maxUsedIndex + 1;
//            } // else we use index=0, it is either not there or not used
//            rs.close();
//
//            // lock slot
//            updateOrInsertSlotLocked(freeUploadIndex, true, date);
//    
//            return freeUploadIndex;
//        }
//    }
//
//    // find next unused, unlocked, and lock!
//    public int findNextUploadSlot(int beforeIndex, long date) throws SQLException {
//        synchronized(syncObj) {
//            ResultSet rs = executePsNEXT_MAX_USED_SLOT(beforeIndex, date);
//            
//            int freeUploadIndex = beforeIndex+1; 
//            if( rs.next() ) {
//                int maxUsedIndex = rs.getInt(1);
//                freeUploadIndex = maxUsedIndex + 1;
//            } // else we use before index + 1 
//            rs.close();
//
//            // lock slot
//            updateOrInsertSlotLocked(freeUploadIndex, true, date);
//    
//            return freeUploadIndex;
//        }
//    }
//
//    // set used and unlock!
//    public void setUploadSlotUsed(int index, long date) throws SQLException {
//        executePsUPDATE_WASUPLOADED(index, date);
//    }
//
//    // set unlocked
//    public void setUploadSlotUnlocked(int index, long date) throws SQLException {
//        executePsUPDATE_LOCKED(index, false, date);
//    }
//
//    private void updateOrInsertSlotLocked(int index, boolean locked, long date) throws SQLException {
//        boolean updateOk = false;
//        try {
//            if( executePsUPDATE_LOCKED(index, locked, date) == 1 ) {
//                updateOk = true;
//            }
//        } catch(SQLException e) {
//            // no record to update, try an insert
//        }
//        
//        if( updateOk == false ) {
//            if( executePsINSERT(index, false, false, locked, date) != 1 ) {
//                throw new SQLException("update or insert of slot failed!");
//            }
//        }
//    }
//
//    private int executePsUPDATE_WASUPLOADED(int index, long date) throws SQLException {
//        // "UPDATE INDEXSLOTS SET used=TRUE,locked=FALSE WHERE indexname=? AND boardname=? AND date=? AND index=?";
//        PreparedStatement ps = getPsUPDATE_WASUPLOADED();
//        ps.setInt(1, indexName);
//        ps.setInt(2, boardIx);
//        ps.setLong(3, date);
//        ps.setInt(4, index);
//        return ps.executeUpdate();
//    }
//
//    private int executePsUPDATE_LOCKED(int index, boolean locked, long date) throws SQLException {
//        // "UPDATE INDEXSLOTS SET locked=? WHERE indexname=? AND boardname=? AND date=? AND index=?";
//        PreparedStatement ps = getPsUPDATE_LOCKED();
//        ps.setBoolean(1, locked);
//        ps.setInt(2, indexName);
//        ps.setInt(3, boardIx);
//        ps.setLong(4, date);
//        ps.setInt(5, index);
//        return ps.executeUpdate();
//    }
//    
//    private int executePsUPDATE_WASDOWNLOADED(int index, long date) throws SQLException {
//        // "UPDATE INDEXSLOTS SET wasdownloaded=TRUE WHERE indexname=? AND boardname=? AND date=? AND index=?"
//        PreparedStatement ps = getPsUPDATE_WASDOWNLOADED();
//        ps.setInt(1, indexName);
//        ps.setInt(2, boardIx);
//        ps.setLong(3, date);
//        ps.setInt(4, index);
//        return ps.executeUpdate();
//    }
//
//    private int executePsINSERT(int index, boolean wasdownloaded, boolean wasuploaded, boolean locked, long date) throws SQLException {
//        // "INSERT INTO INDEXSLOTS (indexname,boardname,date,index,wasdownloaded,wasuploaded,locked) VALUES (?,?,?,?,?,?,?)"
//        PreparedStatement ps = getPsINSERT();
//        ps.setInt(1, indexName);
//        ps.setInt(2, boardIx);
//        ps.setLong(3, date);
//        ps.setInt(4, index);
//        ps.setBoolean(5, wasdownloaded);
//        ps.setBoolean(6, wasuploaded);
//        ps.setBoolean(7, locked);
//        return ps.executeUpdate();
//    }
//
//    private ResultSet executePsNEXT_DOWNLOAD_SLOT(int beforeIndex, long date) throws SQLException {
//        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
//        // "AND used=FALSE AND locked=FALSE ORDER BY index";
//        PreparedStatement ps = getPsNEXT_DOWNLOAD_SLOT();
//        ps.setInt(1, indexName);
//        ps.setInt(2, boardIx);
//        ps.setLong(3, date);
//        ps.setInt(4, beforeIndex);
//        
//        ResultSet rs = ps.executeQuery();
//        return rs;
//    }
//    
//    private ResultSet executePsMAX_SLOT(long date) throws SQLException {
//        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? ORDER BY index DESC"
//        PreparedStatement ps = getPsMAX_SLOT();
//        ps.setInt(1, indexName);
//        ps.setInt(2, boardIx);
//        ps.setLong(3, date);
//        ResultSet rs = ps.executeQuery();
//        return rs;
//    }
//    
//    private ResultSet executePsNEXT_MAX_USED_SLOT(int beforeIndex, long date) throws SQLException {
//        // "SELECT TOP 1 index FROM INDEXSLOTS WHERE indexname=? AND boardname=? AND date=? AND index>? "+
//        // "AND ( used=TRUE OR locked=TRUE ) ORDER BY index DESC";
//        PreparedStatement ps = getPsNEXT_MAX_USED_SLOT();
//        ps.setInt(1, indexName);
//        ps.setInt(2, boardIx);
//        ps.setLong(3, date);
//        ps.setInt(4, beforeIndex);
//        
//        ResultSet rs = ps.executeQuery();
//        return rs;
//    }
//
//    /**
//     * Delete all table entries older than maxDaysOld.
//     * @return  count of deleted rows
//     */
//    public int cleanupTable(int maxDaysOld) throws SQLException {
//return 0;
////        AppLayerDatabase localDB = AppLayerDatabase.getInstance();
////
////        // millis before maxDaysOld days
////        long date = new LocalDate().minusDays(maxDaysOld + 1).toDateTimeAtMidnight(DateTimeZone.UTC).getMillis();
////        
////        PreparedStatement ps = localDB.prepareStatement("DELETE FROM INDEXSLOTS WHERE msgdate<?");
////        ps.setLong(1, date);
////        
////        int deletedCount = ps.executeUpdate();
////        
////        ps.close();
////
////        return deletedCount;
//    }
    
    /**
     * Migrate all McKoi data from this table into a perst Storage.
     * @return  count of migrated McKoi table rows
     */
    public int migrateAllData(IndexSlotsStorage perstStorage) throws SQLException {
        AppLayerDatabase localDB = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = localDB.prepareStatement(SQL_MIGRATE_QUERY);
        ps.setInt(1, MESSAGES);
        
        int count = 0;
        
        IndexSlot gis = null;
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {

            // SELECT boardname, msgdate, msgindex, wasdownloaded, wasuploaded
            int boardName = rs.getInt(1);
            long msgDate = rs.getLong(2);
            int msgIndex = rs.getInt(3);
            boolean wasDownloaded = rs.getBoolean(4);
            boolean wasUploaded = rs.getBoolean(5);
            
            if( !wasUploaded && !wasDownloaded ) {
                // ignore empty items
                continue;
            }
            
            count++;
            
            if( gis != null && gis.getMsgDate() != msgDate ) {
                // previous gis is finished, maybe store
                perstStorage.storeSlot(gis);
                
                // start a new gis for the new date
                gis = null;
            }

            if( gis == null ) {
                // gis is null, get from db
                gis = perstStorage.getSlotForDate(boardName, msgDate);
            }

            // update gis
            if( wasDownloaded ) {
                gis.setDownloadSlotUsed(msgIndex);
            }
            if( wasUploaded ) {
                gis.setUploadSlotUsed(msgIndex);
            }
        }
        
        rs.close();
        ps.close();
        
        // maybe store last gis
        if( gis != null ) {
            perstStorage.storeSlot(gis);
        }
        
        perstStorage.commitStore();

        return count;
    }
}
