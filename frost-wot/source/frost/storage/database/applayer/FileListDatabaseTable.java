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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.fileTransfer.*;
import frost.identities.*;
import frost.storage.database.*;

/**
 * Contains all shared files from all owners.
 */
public class FileListDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(FileListDatabaseTable.class.getName());

    // TODO: startup check: remove fileowners without uploads and lastseen older than 3 month(?)

    private final static String SQL_FILES_DDL =
        "CREATE TABLE FILELIST ("+
          "primkey BIGINT NOT NULL,"+
          "sha VARCHAR NOT NULL,"+
          "size BIGINT NOT NULL,"+
          "fnkey VARCHAR NOT NULL,"+      // if "" then file is not yet inserted
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
    
    private final static String SQL_OWNER_BOARD_DDL =
        "CREATE TABLE FILEOWNERLIST ("+
          "refkey BIGINT NOT NULL,"+
          "owner VARCHAR NOT NULL,"+ // owner identity name
          "name VARCHAR NOT NULL,"+  // file name provided by this owner
          "comment VARCHAR,"+        // file comment provided by this owner
          "rating INT,"+             // rating provided by this owner
          "keywords VARCHAR,"+       // keywords provided by this owner
          "lastreceived BIGINT,"+      // last time we received this file in a fileindex
          "lastuploaded BIGINT,"+      // last time this owner uploaded the file
        "CONSTRAINT FILEOWNERLIST_FK FOREIGN KEY (refkey) REFERENCES FILELIST(primkey) ON DELETE CASCADE,"+
        "CONSTRAINT FILEOWNERLIST_1 UNIQUE (refkey,owner) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_FILES_DDL);
        lst.add(SQL_OWNER_BOARD_DDL);
        return lst;
    }

    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE FILELIST");
        stmt.executeUpdate("COMPACT TABLE FILEOWNERLIST");
        return true;
    }

    /**
     * Insert/updates a new NewFrostSharedFileObject. 
     */
    public boolean insertOrUpdateFrostFileListFileObject(FrostFileListFileObject newSfo) throws SQLException {
        long identity;
        synchronized(getSyncObj()) {

            FrostFileListFileObject oldSfo = getFrostFileListFileObject(newSfo.getSha());
            if( oldSfo != null ) {
                // file is already in FILELIST table, maybe add new FILEOWNER and update fields
                identity = oldSfo.getPrimkey().longValue();
                // maybe update oldSfo
                boolean doUpdate = false;
                if( oldSfo.getKey() == null && newSfo.getKey() != null ) {
                    oldSfo.setKey(newSfo.getKey()); doUpdate = true;
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
                    updateFrostFileListFileObjectInFILELIST(oldSfo);
                }
            } else {
                // file is not yet in FILELIST table
                Long longIdentity = insertFrostFileListFileObjectIntoFILELIST(newSfo);
                if( longIdentity == null ) {
                    return false;
                }
                identity = longIdentity.longValue();
            }
            
            // UNIQUE: refkey,owner
            for(Iterator i=newSfo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
                    
                FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner)i.next();
                ob.setRefkey(identity);
                
                updateOrInsertFrostFileListFileObjectOwner(ob);
            }            
        }        
        return true;
    }

    private FrostFileListFileObject getFrostFileListFileObject(String sha) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
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

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
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
    }

    private synchronized Long insertFrostFileListFileObjectIntoFILELIST(FrostFileListFileObject sfo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
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
        
        PreparedStatement ps = db.prepare(
            "INSERT INTO FILELIST (primkey,sha,size,fnkey,lastdownloaded,lastuploaded,firstreceived,lastreceived,"+
            "requestlastreceived,requestsreceivedcount,requestlastsent,requestssentcount) "+
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

        int ix = 1;
        ps.setLong(ix++, identity.longValue());
        ps.setString(ix++, sfo.getSha());
        ps.setLong(ix++, sfo.getSize());
        ps.setString(ix++, (sfo.getKey()==null?"":sfo.getKey()));
        ps.setLong(ix++, sfo.getLastDownloaded());
        ps.setLong(ix++, sfo.getLastUploaded());
        ps.setLong(ix++, sfo.getFirstReceived());
        ps.setLong(ix++, sfo.getLastReceived());
        
        ps.setLong(ix++, sfo.getRequestLastReceived());
        ps.setInt(ix++, sfo.getRequestsReceivedCount());
        ps.setLong(ix++, sfo.getRequestLastSent());
        ps.setInt(ix++, sfo.getRequestsSentCount());
        
        boolean wasOk = (ps.executeUpdate()==1);
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error inserting new item into filelist");
            return null;
        }

        rs.close();

        return identity;
    }

    /**
     * Update item with SHA, set key,lastreceived,lastdownloaded and all request infos
     */
    private boolean updateFrostFileListFileObjectInFILELIST(FrostFileListFileObject sfo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
            "UPDATE FILELIST SET fnkey=?,lastdownloaded=?,lastuploaded=?,lastreceived=?,"+
            "requestlastreceived=?,requestsreceivedcount=?,requestlastsent=?,requestssentcount=? "+
            "WHERE sha=?");

        int ix = 1;
        ps.setString(ix++, (sfo.getKey()==null?"":sfo.getKey()));
        ps.setLong(ix++, sfo.getLastDownloaded());
        ps.setLong(ix++, sfo.getLastUploaded());
        ps.setLong(ix++, sfo.getLastReceived());
        
        ps.setLong(ix++, sfo.getRequestLastReceived());
        ps.setInt(ix++, sfo.getRequestsReceivedCount());
        ps.setLong(ix++, sfo.getRequestLastSent());
        ps.setInt(ix++, sfo.getRequestsSentCount());
        
        ps.setString(ix++, sfo.getSha());
        
        boolean wasOk = (ps.executeUpdate()==1);
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        }
        return true;
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount
     */
    public boolean updateFrostFileListFileObjectAfterRequestSend(String sha, long requestLastSent) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        FrostFileListFileObject oldSfo = getFrostFileListFileObject(sha);
        if( oldSfo == null) {
            return false;
        }
        
        PreparedStatement ps = db.prepare("UPDATE FILELIST SET requestlastsent=?,requestssentcount=? WHERE sha=?");

        int ix = 1;
        ps.setLong(ix++, requestLastSent);
        ps.setInt(ix++, oldSfo.getRequestsSentCount() + 1);
        
        ps.setString(ix++, sha);
        
        boolean wasOk = (ps.executeUpdate()==1);
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        }
        return true;
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount
     */
    public boolean updateFrostFileListFileObjectAfterRequestReceived(String sha, long requestLastReceived) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        FrostFileListFileObject oldSfo = getFrostFileListFileObject(sha);
        if( oldSfo == null) {
            return false;
        }
        
        if( oldSfo.getRequestLastReceived() > requestLastReceived ) {
            requestLastReceived = oldSfo.getRequestLastReceived();
        }
        
        PreparedStatement ps = db.prepare("UPDATE FILELIST SET requestlastreceived=?,requestsreceivedcount=? WHERE sha=?");

        int ix = 1;
        ps.setLong(ix++, requestLastReceived);
        ps.setInt(ix++, oldSfo.getRequestsSentCount() + 1);
        
        ps.setString(ix++, sha);
        
        boolean wasOk = (ps.executeUpdate()==1);
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        }
        return true;
    }

    /**
     * Update the item with SHA, set lastdownloaded
     */
    public boolean updateFrostFileListFileObjectAfterDownload(String sha, long lastDownloaded) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("UPDATE FILELIST SET lastdownloaded=? WHERE sha=?");

        int ix = 1;
        ps.setLong(ix++, lastDownloaded);
        
        ps.setString(ix++, sha);
        
        boolean wasOk = (ps.executeUpdate()==1);
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error updating item in filelist");
            return false;
        }
        return true;
    }

    /**
     * Updates or inserts fields in db. 
     * If refkey,boardname,owner is already in db, name,lastreceived and lastupdated will be updated.
     * Oterwise the fields will be inserted
     */
    private boolean updateOrInsertFrostFileListFileObjectOwner(FrostFileListFileObjectOwner obNew) throws SQLException {
        
        FrostFileListFileObjectOwner obOld = getFrostFileListFileObjectOwner(
                obNew.getRefkey(),
                obNew.getOwner());
        
        if( obOld == null ) {
            // insert new
            return insertFrostFileListFileObjectOwner(obNew);
        } else {
            // update existing
            if( obOld.getLastReceived() < obNew.getLastReceived() ) {

                obOld.setLastReceived(obNew.getLastReceived());
                obOld.setName(obNew.getName());
                obOld.setLastUploaded(obNew.getLastUploaded());
                obOld.setComment(obNew.getComment());
                obOld.setKeywords(obNew.getKeywords());
                obOld.setRating(obNew.getRating());

                return updateFrostFileListFileObjectOwner(obOld);
            }
            return true; // no need to update, lastReceived of new was earlier
        }
    }

    /** 
     * update name,lastreceived,lastuploaded,comment,rating,keywords
     */
    private boolean updateFrostFileListFileObjectOwner(FrostFileListFileObjectOwner ob) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "UPDATE FILEOWNERLIST SET name=?,comment=?,rating=?,keywords=?,lastreceived=?,lastuploaded=? "+
            "WHERE refkey=? AND owner=?");

        // insert board/owner, identity is set
        int ix = 1;
        ps.setString(ix++, ob.getName());
        ps.setString(ix++, ob.getComment());
        ps.setInt(ix++, ob.getRating());
        ps.setString(ix++, ob.getKeywords());
        ps.setLong(ix++, ob.getLastReceived());
        ps.setLong(ix++, ob.getLastUploaded());
        
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

    private boolean insertFrostFileListFileObjectOwner(FrostFileListFileObjectOwner ob) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "INSERT INTO FILEOWNERLIST (refkey,name,owner,comment,rating,keywords,lastreceived,lastuploaded) VALUES (?,?,?,?,?,?,?,?)");

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

        PreparedStatement ps = db.prepare(
                "SELECT name,comment,rating,keywords,lastreceived,lastuploaded FROM FILEOWNERLIST WHERE refkey=? AND owner=?");
        
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
            
            ob = new FrostFileListFileObjectOwner(refkey, name, owner, comment, keywords, rating, lastreceived, lastuploaded);
        }
        rs.close();
        ps.close();
        
        return ob;
    }

    private List getFrostFileListFileObjectOwnerList(long refkey) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT owner,name,comment,rating,keywords,lastreceived,lastuploaded FROM FILEOWNERLIST WHERE refkey=?");
        
        ps.setLong(1, refkey);

        LinkedList frostSharedFileObjectOwnerBoardList = new LinkedList(); 
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
            
            FrostFileListFileObjectOwner ob = null;
            ob = new FrostFileListFileObjectOwner(refkey, name, owner, comment, keywords, rating, lastreceived, lastuploaded);
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

        PreparedStatement ps = db.prepare("SELECT COUNT(primkey) FROM FILELIST");
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

        PreparedStatement ps = db.prepare("SELECT COUNT(DISTINCT(owner)) FROM FILEOWNERLIST");
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

        PreparedStatement ps = db.prepare("SELECT SUM(size) FROM FILELIST");
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

        PreparedStatement ps = db.prepare(
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
    public void retrieveFiles(FileListDatabaseTableCallback callback) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        // select only files that have an owner
        String sql = "SELECT DISTINCT refkey FROM FILEOWNERLIST";
        
        PreparedStatement ps = db.prepare(sql);
        
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            long refkey = rs.getLong(1);
            
            FrostFileListFileObject fo = getFrostFileListFileObject(refkey);
            List obs = getFrostFileListFileObjectOwnerList(refkey);
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
        
        PreparedStatement ps = db.prepare(sql);
        ps.setString(1, sha);
        
        FrostFileListFileObject fo = null;
        
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            long primkey = rs.getLong(1);
            
            fo = getFrostFileListFileObject(primkey);
            List obs = getFrostFileListFileObjectOwnerList(primkey);
            fo.getFrostFileListFileObjectOwnerList().addAll(obs);
        }
        rs.close();
        ps.close();
        
        return fo;
    }
}
