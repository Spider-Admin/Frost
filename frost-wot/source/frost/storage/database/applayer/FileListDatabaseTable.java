/*
 FileListDatabaseTable.java / Frost
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

import java.beans.*;
import java.sql.*;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.identities.*;
import frost.storage.database.*;

/**
 * Contains all shared files from all owners.
 */
public class FileListDatabaseTable extends AbstractDatabaseTable implements PropertyChangeListener {

    private static Logger logger = Logger.getLogger(FileListDatabaseTable.class.getName());
    
    private boolean rememberSharedFileDownloaded;

    private final static String SQL_FILES_DDL =
        "CREATE TABLE IF NOT EXISTS FILELIST ("+
          "primkey BIGINT NOT NULL,"+
          "sha VARCHAR NOT NULL,"+
          "size BIGINT NOT NULL,"+
          "fnkey VARCHAR NOT NULL,"+        // if "" then file is not yet inserted
          "lastdownloaded BIGINT,"+         // last time we successfully downloaded this file
          "lastuploaded BIGINT,"+           // GLOBAL last time someone uploaded this file
          "firstreceived BIGINT NOT NULL,"+ // first time we saw this file
          "lastreceived BIGINT NOT NULL,"+  // GLOBAL last time we received this file in a fileindex. kept if all refs were removed

          "requestlastreceived BIGINT,"+  // time when we received the last request for this sha
          "requestsreceivedcount INT,"+   // received requests count
          
          "requestlastsent BIGINT,"+      // time when we sent the last request for this file
          "requestssentcount INT,"+       // sent requests count
          
        "CONSTRAINT files_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT FILELIST_1 UNIQUE (sha) )";
    
    private final static String SQL_FILELIST_INDEX_PRIMKEY =
        "CREATE UNIQUE INDEX FILELIST_IX_PRIMKEY ON FILELIST ( primkey )";
    private final static String SQL_FILELIST_INDEX_SHA =
        "CREATE UNIQUE INDEX FILELIST_IX_SHA ON FILELIST ( sha )";
    
    private final static String SQL_OWNER_BOARD_DDL =
        "CREATE TABLE IF NOT EXISTS FILEOWNERLIST ("+
          "refkey BIGINT NOT NULL,"+
          "owner VARCHAR NOT NULL,"+ // owner identity name
          "name VARCHAR NOT NULL,"+  // file name provided by this owner
          "comment VARCHAR,"+        // file comment provided by this owner
          "rating INT,"+             // rating provided by this owner
          "keywords VARCHAR,"+       // keywords provided by this owner
          "lastreceived BIGINT,"+    // last time we received this file in a fileindex
          "lastuploaded BIGINT,"+    // last time this owner uploaded the file
          "fnkey VARCHAR,"+          // the CHK key that this owner provided
        "CONSTRAINT FILEOWNERLIST_FK FOREIGN KEY (refkey) REFERENCES FILELIST(primkey) ON DELETE CASCADE,"+
        "CONSTRAINT FILEOWNERLIST_1 UNIQUE (refkey,owner) )";

    private final static String SQL_FILEOWNERLIST_INDEX_REFKEY =
        "CREATE INDEX FILEOWNERLIST_IX_REFKEY ON FILEOWNERLIST ( refkey )";

    public List<String> getTableDDL() {
        ArrayList<String> lst = new ArrayList<String>(5);
        lst.add(SQL_FILES_DDL);
        lst.add(SQL_OWNER_BOARD_DDL);
        lst.add(SQL_FILELIST_INDEX_PRIMKEY);
        lst.add(SQL_FILELIST_INDEX_SHA);
        lst.add(SQL_FILEOWNERLIST_INDEX_REFKEY);
        return lst;
    }

    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE FILELIST");
        stmt.executeUpdate("COMPACT TABLE FILEOWNERLIST");
        return true;
    }
    
    public FileListDatabaseTable() {
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED, this);
    }

    /**
     * Insert/updates a new NewFrostSharedFileObject.
     * If method returns false/exception, the caller rolls back.
     */
    public synchronized boolean insertOrUpdateFrostFileListFileObject(FrostFileListFileObject newSfo, Connection conn) 
    throws SQLException {
        FrostFileListFileObject currentSfo = null;
        FrostFileListFileObject oldSfo = getFrostFileListFileObject(newSfo.getSha());
        if( oldSfo != null ) {
            // file is already in FILELIST table, maybe add new FILEOWNER and update fields
            // maybe update oldSfo
            boolean doUpdate = false;
            if( oldSfo.getKey() == null && newSfo.getKey() != null ) {
                oldSfo.setKey(newSfo.getKey()); doUpdate = true;
            } else if( oldSfo.getKey() != null && newSfo.getKey() != null ) {
                // fix to replace 0.7 keys before 1010 on the fly
                if( FreenetKeys.isOld07ChkKey(oldSfo.getKey()) && !FreenetKeys.isOld07ChkKey(newSfo.getKey()) ) {
                    // replace old chk key with new one
                    oldSfo.setKey(newSfo.getKey()); doUpdate = true;
                }
            }
            if( oldSfo.getFirstReceived() > newSfo.getFirstReceived() ) {
                oldSfo.setFirstReceived(newSfo.getFirstReceived()); doUpdate = true;
            }
            if( oldSfo.getLastReceived() < newSfo.getLastReceived() ) {
                oldSfo.setLastReceived(newSfo.getLastReceived()); doUpdate = true;
            }
            if( oldSfo.getLastUploaded() < newSfo.getLastUploaded() ) {
                oldSfo.setLastUploaded(newSfo.getLastUploaded()); doUpdate = true;
            }
            if( oldSfo.getLastDownloaded() < newSfo.getLastDownloaded() ) {
                oldSfo.setLastDownloaded(newSfo.getLastDownloaded()); doUpdate = true;
            }
            if( oldSfo.getRequestLastReceived() < newSfo.getRequestLastReceived() ) {
                oldSfo.setRequestLastReceived(newSfo.getRequestLastReceived()); doUpdate = true;
            }
            if( oldSfo.getRequestLastSent() < newSfo.getRequestLastSent() ) {
                oldSfo.setRequestLastSent(newSfo.getRequestLastSent()); doUpdate = true;
            }
            if( oldSfo.getRequestsReceivedCount() < newSfo.getRequestsReceivedCount() ) {
                oldSfo.setRequestsReceivedCount(newSfo.getRequestsReceivedCount()); doUpdate = true;
            }
            if( oldSfo.getRequestsSentCount() < newSfo.getRequestsSentCount() ) {
                oldSfo.setRequestsSentCount(newSfo.getRequestsSentCount()); doUpdate = true;
            }
            if( doUpdate ) {
                boolean wasOk = updateFrostFileListFileObjectInFILELIST(oldSfo, conn);
                if( wasOk == false ) {
                    return false;
                }
            }
            currentSfo = oldSfo; // updated sfo
        } else {
            // file is not yet in FILELIST table
            boolean wasOk = insertFrostFileListFileObjectIntoFILELIST(newSfo, conn); // sets new primkey
            if( wasOk == false ) {
                return false;
            }
            currentSfo = newSfo;
        }
        
        long primkey = currentSfo.getPrimkey().longValue();

        /**
         * Updates or inserts fields in db. 
         * If refkey,boardname,owner is already in db, name,lastreceived and lastupdated will be updated.
         * Otherwise the fields will be inserted.
         */
        // UNIQUE: refkey,owner
        for(Iterator<FrostFileListFileObjectOwner> i=newSfo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
                
            FrostFileListFileObjectOwner obNew = i.next();
            obNew.setRefkey(primkey);
            
            FrostFileListFileObjectOwner obOld = getFrostFileListFileObjectOwner(
                    obNew.getRefkey(),
                    obNew.getOwner());
            
            if( obOld == null ) {
                // insert new
                boolean wasOk = insertFrostFileListFileObjectOwner(obNew, conn);
                if( wasOk == false ) {
                    return false;
                }
            } else {
                // update existing
                if( obOld.getLastReceived() < obNew.getLastReceived() ) {

                    obOld.setLastReceived(obNew.getLastReceived());
                    obOld.setName(obNew.getName());
                    obOld.setLastUploaded(obNew.getLastUploaded());
                    obOld.setComment(obNew.getComment());
                    obOld.setKeywords(obNew.getKeywords());
                    obOld.setRating(obNew.getRating());
                    obOld.setKey(obNew.getKey());

                    boolean wasOk = updateFrostFileListFileObjectOwner(obOld, conn);
                    if( wasOk == false ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private FrostFileListFileObject getFrostFileListFileObject(String sha) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement(
            "SELECT primkey,size,fnkey,lastdownloaded,lastuploaded,firstreceived,lastreceived,"+
                   "requestlastreceived,requestsreceivedcount,requestlastsent,requestssentcount "+
            " FROM FILELIST WHERE sha=?");
        
        ps.setString(1, sha);
        
        FrostFileListFileObject fo = null;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            int ix = 1;
            long primkey = rs.getLong(ix++);
            long size = rs.getLong(ix++);
            String key = rs.getString(ix++);
            long lastDownloaded = rs.getLong(ix++);
            long lastUploaded = rs.getLong(ix++);
            long firstReceived = rs.getLong(ix++);
            long lastReceived = rs.getLong(ix++);

            long requestLastReceived = rs.getLong(ix++);  
            int requestsReceivedCount = rs.getInt(ix++);
            long requestLastSent = rs.getLong(ix++);      
            int requestsSentCount = rs.getInt(ix++);

            if( key.length() == 0 ) {
                key = null;
            }

            fo = new FrostFileListFileObject(
                    primkey, 
                    sha, 
                    size, 
                    key, 
                    lastDownloaded,
                    lastUploaded,
                    firstReceived,
                    lastReceived,
                    requestLastReceived,
                    requestsReceivedCount,
                    requestLastSent,
                    requestsSentCount);
        }
        rs.close();
        ps.close();
        
        return fo;
    }

    private FrostFileListFileObject getFrostFileListFileObject(long primkey) throws SQLException {

        Connection conn = AppLayerDatabase.getInstance().getPooledConnection();
        try {

            PreparedStatement ps = conn.prepareStatement(
                "SELECT sha,size,fnkey,lastdownloaded,lastuploaded,firstreceived,lastreceived,"+
                       "requestlastreceived,requestsreceivedcount,requestlastsent,requestssentcount "+
                " FROM FILELIST WHERE primkey=?");
            
            ps.setLong(1, primkey);
            
            FrostFileListFileObject fo = null;
            ResultSet rs = ps.executeQuery();
            if( rs.next() ) {
                int ix = 1;
                String sha = rs.getString(ix++);
                long size = rs.getLong(ix++);
                String key = rs.getString(ix++);
                long lastDownloaded = rs.getLong(ix++);
                long lastUploaded = rs.getLong(ix++);
                long firstReceived = rs.getLong(ix++);
                long lastReceived = rs.getLong(ix++);
                
                long requestLastReceived = rs.getLong(ix++);  
                int requestsReceivedCount = rs.getInt(ix++);
                long requestLastSent = rs.getLong(ix++);      
                int requestsSentCount = rs.getInt(ix++);
                
                if( key.length() == 0 ) {
                    key = null;
                }
                
                fo = new FrostFileListFileObject(
                        primkey, 
                        sha, 
                        size, 
                        key, 
                        lastDownloaded,
                        lastUploaded,
                        firstReceived,
                        lastReceived,
                        requestLastReceived,
                        requestsReceivedCount,
                        requestLastSent,
                        requestsSentCount);
            }
            rs.close();
            ps.close();
            
            return fo;
        } finally {
            AppLayerDatabase.getInstance().givePooledConnection(conn);
        }
    }

    private synchronized boolean insertFrostFileListFileObjectIntoFILELIST(FrostFileListFileObject sfo, Connection conn) 
    throws SQLException {
        
        Long identity = null;
        Statement stmt = AppLayerDatabase.getInstance().createStatement();
        ResultSet rs = stmt.executeQuery("select UNIQUEKEY('FILELIST')");
        if( rs.next() ) {
            identity = new Long(rs.getLong(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
        }
        rs.close();
        stmt.close();
        
        sfo.setPrimkey(identity);
        
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO FILELIST (primkey,sha,size,fnkey,lastdownloaded,lastuploaded,firstreceived,lastreceived,"+
            "requestlastreceived,requestsreceivedcount,requestlastsent,requestssentcount) "+
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

        int ix = 1;
        ps.setLong(ix++, sfo.getPrimkey().longValue());
        ps.setString(ix++, sfo.getSha());
        ps.setLong(ix++, sfo.getSize());
        ps.setString(ix++, (sfo.getKey()==null?"":sfo.getKey()));
        if( rememberSharedFileDownloaded ) {
            ps.setLong(ix++, sfo.getLastDownloaded());
        } else {
            ps.setLong(ix++, 0);
        }
        ps.setLong(ix++, sfo.getLastUploaded());
        ps.setLong(ix++, sfo.getFirstReceived());
        ps.setLong(ix++, sfo.getLastReceived());
        
        ps.setLong(ix++, sfo.getRequestLastReceived());
        ps.setInt(ix++, sfo.getRequestsReceivedCount());
        ps.setLong(ix++, sfo.getRequestLastSent());
        ps.setInt(ix++, sfo.getRequestsSentCount());
        
        boolean wasOk = false; 
        try {
            wasOk = (ps.executeUpdate()==1);
        } catch(SQLException ex) {
            logger.log(Level.SEVERE,"Error inserting new item into filelist", ex);
        }
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error inserting new item into filelist");
            return false;
        }

        rs.close();

        return true;
    }

    /**
     * Update item with SHA, set key,lastreceived,lastdownloaded and all request infos.
     * The provided FrostFileListFileObject must have a valid primkey set.
     */
    private boolean updateFrostFileListFileObjectInFILELIST(FrostFileListFileObject sfo, Connection conn) 
    throws SQLException {
        
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE FILELIST SET fnkey=?,lastdownloaded=?,lastuploaded=?,lastreceived=?,"+
            "requestlastreceived=?,requestsreceivedcount=?,requestlastsent=?,requestssentcount=? "+
            "WHERE primkey=?");

        int ix = 1;
        ps.setString(ix++, (sfo.getKey()==null?"":sfo.getKey()));
        if( rememberSharedFileDownloaded ) {
            ps.setLong(ix++, sfo.getLastDownloaded());
        } else {
            ps.setLong(ix++, 0);
        }
        ps.setLong(ix++, sfo.getLastUploaded());
        ps.setLong(ix++, sfo.getLastReceived());
        
        ps.setLong(ix++, sfo.getRequestLastReceived());
        ps.setInt(ix++, sfo.getRequestsReceivedCount());
        ps.setLong(ix++, sfo.getRequestLastSent());
        ps.setInt(ix++, sfo.getRequestsSentCount());
        
        ps.setLong(ix++, sfo.getPrimkey());
        
        boolean wasOk = false; 
        try {
            wasOk = (ps.executeUpdate()==1);
        } catch(SQLException ex) {
            logger.log(Level.SEVERE,"Error updating item in filelist", ex);
        }
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount
     */
    public boolean updateFrostFileListFileObjectAfterRequestSent(String sha, long requestLastSent, Connection conn) 
    throws SQLException {
        
        FrostFileListFileObject oldSfo = getFrostFileListFileObject(sha);
        if( oldSfo == null) {
            return false;
        }
        // oldSfo has a valid primkey!
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE FILELIST SET requestlastsent=?,requestssentcount=? WHERE primkey=?");

        int ix = 1;
        ps.setLong(ix++, requestLastSent);
        ps.setInt(ix++, oldSfo.getRequestsSentCount() + 1);
        
        ps.setLong(ix++, oldSfo.getPrimkey());
        
        boolean wasOk = false; 
        try {
            wasOk = (ps.executeUpdate()==1);
        } finally {
            ps.close();
        }
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount
     */
    public boolean updateFrostFileListFileObjectAfterRequestReceived(String sha, long requestLastReceived, Connection conn) 
    throws SQLException {
        
        FrostFileListFileObject oldSfo = getFrostFileListFileObject(sha);
        if( oldSfo == null) {
            return false;
        }
        
        if( oldSfo.getRequestLastReceived() > requestLastReceived ) {
            requestLastReceived = oldSfo.getRequestLastReceived();
        }
        // oldSfo has a valid primkey!
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE FILELIST SET requestlastreceived=?,requestsreceivedcount=? WHERE primkey=?");

        int ix = 1;
        ps.setLong(ix++, requestLastReceived);
        ps.setInt(ix++, oldSfo.getRequestsSentCount() + 1);
        
        ps.setLong(ix++, oldSfo.getPrimkey());
        
        boolean wasOk = false; 
        try {
            wasOk = (ps.executeUpdate()==1);
        } finally {
            ps.close();
        }
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Update the item with SHA, set lastdownloaded
     */
    public boolean updateFrostFileListFileObjectAfterDownload(String sha, long lastDownloaded) throws SQLException {

        if( !rememberSharedFileDownloaded ) {
            return true;
        }
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepareStatement("UPDATE FILELIST SET lastdownloaded=? WHERE sha=?");

        int ix = 1;
        ps.setLong(ix++, lastDownloaded);
        
        ps.setString(ix++, sha);
        
        boolean wasOk = false; 
        try {
            wasOk = (ps.executeUpdate()==1);
        } finally {
            ps.close();
        }
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        } else {
            return true;
        }
    }

    /** 
     * update name,lastreceived,lastuploaded,comment,rating,keywords,fnkey
     */
    private boolean updateFrostFileListFileObjectOwner(FrostFileListFileObjectOwner ob, Connection conn) 
    throws SQLException {

        PreparedStatement ps = conn.prepareStatement(
            "UPDATE FILEOWNERLIST SET name=?,comment=?,rating=?,keywords=?,lastreceived=?,lastuploaded=?,fnkey=? "+
            "WHERE refkey=? AND owner=?");

        // insert board/owner, identity is set
        int ix = 1;
        ps.setString(ix++, ob.getName());
        ps.setString(ix++, ob.getComment());
        ps.setInt(ix++, ob.getRating());
        ps.setString(ix++, ob.getKeywords());
        ps.setLong(ix++, ob.getLastReceived());
        ps.setLong(ix++, ob.getLastUploaded());
        ps.setString(ix++, ob.getKey());
        
        ps.setLong(ix++, ob.getRefkey());
        ps.setString(ix++, ob.getOwner());
        
        boolean result = false;
        try {
            ps.executeUpdate();
            result = true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE,"Error updating file owner ref", ex);
        }
        ps.close();
        
        return result;
    }

    private boolean insertFrostFileListFileObjectOwner(FrostFileListFileObjectOwner ob, Connection conn) 
    throws SQLException {

        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO FILEOWNERLIST (refkey,name,owner,comment,rating,keywords,lastreceived,lastuploaded,fnkey) " +
            "VALUES (?,?,?,?,?,?,?,?,?)");

        // insert board/owner, identity is set
        int ix = 1;
        ps.setLong(ix++, ob.getRefkey());
        ps.setString(ix++, ob.getName());
        ps.setString(ix++, ob.getOwner());
        ps.setString(ix++, ob.getComment());
        ps.setInt(ix++, ob.getRating());
        ps.setString(ix++, ob.getKeywords());
        ps.setLong(ix++, ob.getLastReceived());
        ps.setLong(ix++, ob.getLastUploaded());
        ps.setString(ix++, ob.getKey());

        boolean result = false;
        try {
            ps.executeUpdate();
            result = true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE,"Error inserting file owner ref", ex);
        }
        ps.close();
        
        return result;
    }

    private FrostFileListFileObjectOwner getFrostFileListFileObjectOwner(long refkey, String owner) 
    throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement(
                "SELECT name,comment,rating,keywords,lastreceived,lastuploaded,fnkey FROM FILEOWNERLIST " +
                "WHERE refkey=? AND owner=?");
        
        ps.setLong(1, refkey);
        ps.setString(2, owner);

        FrostFileListFileObjectOwner ob = null;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            int ix = 1;
            String name = rs.getString(ix++);
            String comment = rs.getString(ix++);
            int rating = rs.getInt(ix++);
            String keywords = rs.getString(ix++);
            long lastreceived = rs.getLong(ix++);
            long lastuploaded = rs.getLong(ix++);
            String fnKey = rs.getString(ix++);
            
            ob = new FrostFileListFileObjectOwner(refkey, name, owner, comment, keywords, rating, lastreceived, lastuploaded, fnKey);
        }
        rs.close();
        ps.close();
        
        return ob;
    }

    private List<FrostFileListFileObjectOwner> getFrostFileListFileObjectOwnerList(long refkey) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement(
                "SELECT owner,name,comment,rating,keywords,lastreceived,lastuploaded,fnkey FROM FILEOWNERLIST " +
                "WHERE refkey=?");
        
        ps.setLong(1, refkey);

        LinkedList<FrostFileListFileObjectOwner> frostSharedFileObjectOwnerBoardList = new LinkedList<FrostFileListFileObjectOwner>(); 
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            int ix = 1;
            String owner = rs.getString(ix++);
            String name = rs.getString(ix++);
            String comment = rs.getString(ix++);
            int rating = rs.getInt(ix++);
            String keywords = rs.getString(ix++);
            long lastreceived = rs.getLong(ix++);
            long lastuploaded = rs.getLong(ix++);
            String fnKey = rs.getString(ix++);
            
            FrostFileListFileObjectOwner ob = null;
            ob = new FrostFileListFileObjectOwner(refkey, name, owner, comment, keywords, rating, lastreceived, lastuploaded, fnKey);
            frostSharedFileObjectOwnerBoardList.add(ob);
        }
        rs.close();
        ps.close();
        
        return frostSharedFileObjectOwnerBoardList;
    }
    
    /**
     * Return file count.
     */
    public int getFileCount() throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement("SELECT COUNT(primkey) FROM FILELIST");
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }

    /**
     * Return sharer count.
     */
    public int getSharerCount() throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement("SELECT COUNT(DISTINCT(owner)) FROM FILEOWNERLIST");
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }

    /**
     * Return file sizes.
     */
    public long getFileSizes() throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement("SELECT SUM(size) FROM FILELIST");
        long count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getLong(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }

    /**
     * Return filecount for specified identity on all boards.
     */
    public int getFileCountForIdentity(Identity identity) throws SQLException {
        // count of all all SHA that have at least one reference to a OwnerBoard with the given identity
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement(
            "SELECT COUNT(primkey) FROM FILELIST WHERE primkey in (SELECT refkey FROM FILEOWNERLIST WHERE owner=? GROUP BY refkey)");
        ps.setString(1, identity.getUniqueName());
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }

    /**
     * Retrieves a list of FrostSharedFileOjects.
     */
    public void retrieveFiles(
            FileListDatabaseTableCallback callback,
            List<String> names,
            List<String> comments,
            List<String> keywords,
            List<String> owners) 
    throws SQLException 
    {
/*        
SQL='SELECT DISTINCT refkey FROM FILEOWNERLIST WHERE LOWER(name) LIKE ? OR LOWER(name) LIKE ? OR LOWER(comment) LIKE ? OR LOWER(comment) LIKE ? OR LOWER(keywords) LIKE ? OR LOWER(keywords) LIKE ?'        
*/        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        // select only files that have an owner
        String sql = "SELECT DISTINCT refkey FROM FILEOWNERLIST";

        List<String> values = new LinkedList<String>();

        if( (names != null && names.size() > 0) 
         || (comments != null && comments.size() > 0 ) 
         || (keywords != null && keywords.size() > 0)
         || (owners != null && owners.size() > 0) )
        {
            sql += " WHERE";

            if( names != null && names.size() > 0 ) {
                for(String name : names) {
                    sql += " LOWER(name) LIKE ? OR";
                    values.add(name);
                }
            }
            if( comments != null && comments.size() > 0 ) {
                for(String comment : comments) {
                    sql += " LOWER(comment) LIKE ? OR";
                    values.add(comment);
                }
            }
            if( keywords != null && keywords.size() > 0 ) {
                for(String keyword : keywords) {
                    sql += " LOWER(keywords) LIKE ? OR";
                    values.add(keyword);
                }
            }
            if( owners != null && owners.size() > 0 ) {
                for(String owner : owners) {
                    sql += " LOWER(owner) LIKE ? OR";
                    values.add(owner);
                }
            }
            // remove last OR
            sql = sql.substring(0, sql.length() - 3);
        }
        
        PreparedStatement ps = db.prepareStatement(sql);

        int ix = 1;
        for( String value : values ) {
            ps.setString(ix++,"%"+value+"%");
        }
        
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            long refkey = rs.getLong(1);
            
            FrostFileListFileObject fo = getFrostFileListFileObject(refkey);
            if( fo == null ) {
                // db corrupted, no file for this owner refkey, should not be possible due to constraints
                continue;
            }
            List<FrostFileListFileObjectOwner> obs = getFrostFileListFileObjectOwnerList(refkey);
            fo.getFrostFileListFileObjectOwnerList().addAll(obs);
            
            boolean shouldStop = callback.fileRetrieved(fo); // pass to callback
            if( shouldStop ) {
                break;
            }
        }
        rs.close();
        ps.close();
    }

    /**
     * Retrieves a list of FrostSharedFileOjects.
     */
    public FrostFileListFileObject retrieveFileBySha(String sha) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        String sql = "SELECT primkey FROM FILELIST WHERE sha=?";
        
        PreparedStatement ps = db.prepareStatement(sql);
        ps.setString(1, sha);
        
        FrostFileListFileObject fo = null;
        
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            long primkey = rs.getLong(1);
            
            fo = getFrostFileListFileObject(primkey);
            List<FrostFileListFileObjectOwner> obs = getFrostFileListFileObjectOwnerList(primkey);
            fo.getFrostFileListFileObjectOwnerList().addAll(obs);
        }
        rs.close();
        ps.close();
        
        return fo;
    }
    
    /**
     * Remove files that have no owner and no CHK key. 
     */
    public int cleanupFileListFiles() throws SQLException {
        AppLayerDatabase localDB = AppLayerDatabase.getInstance();

        PreparedStatement ps = localDB.prepareStatement(
                "DELETE FROM FILELIST WHERE fnkey='' AND " +
                "primkey NOT IN (SELECT refkey FROM FILEOWNERLIST)");
        
        int deletedCount = ps.executeUpdate();
        
        ps.close();

        return deletedCount;
    }

    /**
     * Remove owners that were not seen for more than MINIMUM_DAYS_OLD days and have no CHK key set.
     */
    public int cleanupFileListFileOwners(int maxDaysOld) throws SQLException {
        AppLayerDatabase localDB = AppLayerDatabase.getInstance();

        long minVal = System.currentTimeMillis() - ((long)maxDaysOld * 24L * 60L * 60L * 1000L);

        PreparedStatement ps = localDB.prepareStatement("DELETE FROM FILEOWNERLIST WHERE lastreceived<? AND fnkey IS NULL");
        ps.setLong(1, minVal);
        
        int deletedCount = ps.executeUpdate();
        
        ps.close();

        return deletedCount;
    }
    
    /**
     * Reset the lastdownloaded column for all file entries.
     */
    public void resetLastDownloaded() throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepareStatement("UPDATE FILELIST SET lastdownloaded=0");
        ps.executeUpdate();
        ps.close();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
    }
}
