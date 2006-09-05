/*
 SharedFilesCHKKeysDatabaseTable.java / Frost
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
package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.database.*;

public class SharedFilesCHKKeysDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(SharedFilesCHKKeysDatabaseTable.class.getName());
    
    // FIXME: implement some expiration for old CHK keys and delete them

    // FIXME: IndexSlots for KSK key! -> problem: IndexSlots uses a board with contraint to the boards table, but we have no board!
    
    // Question: how to ensure own CHK keys, track them once uploaded if we ever see them again! 
    // Answer: Ignore lost keys, we resend them in time!
    //         Handle own received keys like any other keys, we don't even know that this was our key.
    
    private final static String SQL_SHAREDFILESCHK_DDL =
        "CREATE TABLE SHAREDFILESCHK ("+
          "primkey BIGINT NOT NULL,"+
          "chkkey VARCHAR NOT NULL,"+
          "seencount INT NOT NULL,"+
          "firstseen BIGINT NOT NULL,"+ // a time in millis
          "lastseen BIGINT NOT NULL,"+  // a time in millis 
          "isdownloaded BOOLEAN NOT NULL,"+
          "isvalid BOOLEAN NOT NULL,"+   // - if files signature was invalid, don't distribute this file any longer!        
          "downloadretries INT NOT NULL,"+
          "lastdownloadtrystart BIGINT NOT NULL,"+ // a time in millis
        "CONSTRAINT sfiles_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT sfiles_1 UNIQUE (chkkey) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_SHAREDFILESCHK_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE SHAREDFILESCHK");
        return true;
    }

    public SharedFilesCHKKey retrieveSharedFilesCHKKey(String chkKey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT primkey,seencount,firstseen,lastseen,isdownloaded,isvalid,downloadretries,lastdownloadtrystart "+
                "FROM SHAREDFILESCHK WHERE chkkey=?");
        
        ps.setString(1, chkKey);
        
        SharedFilesCHKKey result = null;
        
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            int ix=1;
            long primkey = rs.getLong(ix++);
            int seenCount = rs.getInt(ix++);
            long firstSeen = rs.getLong(ix++);
            long lastSeen = rs.getLong(ix++);
            boolean isDownloaded = rs.getBoolean(ix++);
            boolean isValid = rs.getBoolean(ix++);
            int downloadRetries = rs.getInt(ix++);
            long lastDownloadTryStart = rs.getLong(ix++);
            
            result = new SharedFilesCHKKey(
                    primkey, 
                    chkKey,
                    seenCount,
                    firstSeen,
                    lastSeen,
                    isDownloaded,
                    isValid,
                    downloadRetries,
                    lastDownloadTryStart);
        }
        rs.close();
        ps.close();
        
        return result;
    }

    /** 
     * Retrieves next unretrieved CHK key to download.
     * Takes a List of currently tried CHK keys to not to return duplicates
     */ 
    public SharedFilesCHKKey retrieveNextSharedFilesCHKKeyToDownload(
            List currentlyTriedCHKs, int maxRetries) throws SQLException 
    {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        String sql = "SELECT primkey,chkkey,seencount,firstseen,lastseen,isdownloaded,isvalid,downloadretries,lastdownloadtrystart "+
                     "FROM SHAREDFILESCHK WHERE isdownloaded=FALSE AND downloadretries<?";

        if( currentlyTriedCHKs != null && currentlyTriedCHKs.size() > 0 ) {
            for( int i = 0; i < currentlyTriedCHKs.size(); i++ ) {
                sql += " AND chkkey<>?";
            }
        }
        
        sql += " ORDER BY lastdownloadtrystart DESC";
        
        PreparedStatement ps = db.prepare(sql);
        
        int qix = 1;
        ps.setInt(qix++, maxRetries);

        if( currentlyTriedCHKs != null && currentlyTriedCHKs.size() > 0 ) {
            for( Iterator i = currentlyTriedCHKs.iterator(); i.hasNext(); ) {
                SharedFilesCHKKey element = (SharedFilesCHKKey) i.next();
                ps.setString(qix++, element.getChkKey());
            }
        }

        SharedFilesCHKKey result = null;
        
        ps.setMaxRows(1);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            int ix=1;
            long primkey = rs.getLong(ix++);
            String chkKey = rs.getString(ix++);
            int seenCount = rs.getInt(ix++);
            long firstSeen = rs.getLong(ix++);
            long lastSeen = rs.getLong(ix++);
            boolean isDownloaded = rs.getBoolean(ix++);
            boolean isValid = rs.getBoolean(ix++);
            int downloadRetries = rs.getInt(ix++);
            long lastDownloadTryStart = rs.getLong(ix++);
            
            result = new SharedFilesCHKKey(
                    primkey, 
                    chkKey,
                    seenCount,
                    firstSeen,
                    lastSeen,
                    isDownloaded,
                    isValid,
                    downloadRetries,
                    lastDownloadTryStart);
        }
        rs.close();
        ps.close();
        
        return result;
    }

    public boolean insertSharedFilesCHKKey(SharedFilesCHKKey newkey) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        Long identity = null;
        Statement stmt = AppLayerDatabase.getInstance().createStatement();
        ResultSet rs = stmt.executeQuery("select UNIQUEKEY('SHAREDFILESCHK')");
        if( rs.next() ) {
            identity = new Long(rs.getLong(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
        }
        rs.close();
        stmt.close();

        PreparedStatement ps = db.prepare(
                "INSERT INTO SHAREDFILESCHK (primkey,chkkey,seencount,firstseen,lastseen,"+
                  "isdownloaded,isvalid,downloadretries,lastdownloadtrystart) "+
                "VALUES (?,?,?,?,?,?,?,?,?)");
        
        int ix=1;
        ps.setLong(ix++, identity.longValue());
        ps.setString(ix++, newkey.getChkKey());
        ps.setInt(ix++, newkey.getSeenCount());
        ps.setLong(ix++, newkey.getFirstSeen());
        ps.setLong(ix++, newkey.getLastSeen());
        ps.setBoolean(ix++, newkey.isDownloaded());
        ps.setBoolean(ix++, newkey.isValid());
        ps.setInt(ix++, newkey.getDownloadRetries());
        ps.setLong(ix++, newkey.getLastDownloadTryStartTime());
        
        int insertCount = ps.executeUpdate();
        
        return (insertCount == 1);
    }

    /**
     * Updates newkey in database.
     * primkey, chkkey, firstseen are always fix!
     */
    public boolean updateSharedFilesCHKKey(SharedFilesCHKKey newkey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "UPDATE SHAREDFILESCHK SET seencount=?,lastseen=?,isdownloaded=?,"+
                "isvalid=?,downloadretries=?,lastdownloadtrystart=? "+
                "WHERE primkey=?");

        int ix=1;
        ps.setInt(ix++, newkey.getSeenCount());
        ps.setLong(ix++, newkey.getLastSeen());
        ps.setBoolean(ix++, newkey.isDownloaded());
        ps.setBoolean(ix++, newkey.isValid());
        ps.setInt(ix++, newkey.getDownloadRetries());
        ps.setLong(ix++, newkey.getLastDownloadTryStartTime());
        
        ps.setLong(ix++, newkey.getPrimaryKey());
        
        int updateCount = ps.executeUpdate();
        
        return (updateCount == 1);
    }
}
