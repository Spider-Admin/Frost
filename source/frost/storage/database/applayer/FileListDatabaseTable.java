package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.database.*;

public class FileListDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(FileListDatabaseTable.class.getName());

    private final static String SQL_FILES_DDL =
        "CREATE TABLE FILELIST ("+
        "primkey BIGINT NOT NULL IDENTITY PRIMARY KEY,"+
        "sha1 VARCHAR NOT NULL,"+
        "size BIGINT NOT NULL,"+
        "key VARCHAR NOT NULL,"+         // if "" then file is not yet inserted
        "lastdownloaded DATE,"+ // last time we successfully downloaded this file
        "lastreceived DATE NOT NULL,"+ // GLOBAL last time we received this file in a fileindex. kept if all refs were removed
        "CONSTRAINT FILELIST_1 UNIQUE (sha1) )";
    
    private final static String SQL_OWNER_BOARD_DDL =
        "CREATE TABLE FILEOWNERBOARDLIST ("+
        "refkey BIGINT NOT NULL,"+
        "board VARCHAR NOT NULL,"+
        "owner VARCHAR NOT NULL,"+     // if "" then owner is anonymous
        "name VARCHAR NOT NULL,"+
        "lastreceived DATE,"+ // last time we received this file in a fileindex
        "lastuploaded DATE,"+ // last time this owner uploaded the file
//        "lastShared DATE,"+ // TODO: in case we send out files of friends
        "FOREIGN KEY (refkey) REFERENCES FILELIST(primkey) ON DELETE CASCADE,"+
        "CONSTRAINT FILEOWNERBOARDLIST_1 UNIQUE (refkey,owner,board) )";

    // FIXME: daily check: remove refs older than 3 month(?), keep files with keys, but remember last seen if last ref!
    
    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_FILES_DDL);
        lst.add(SQL_OWNER_BOARD_DDL);
        return lst;
    }

    /**
     * Insert/updates a new NewFrostSharedFileObject. 
     */
    public boolean insertOrUpdateFrostSharedFileObject(FrostSharedFileObject newSfo) throws SQLException {
        long identity;
        synchronized(getSyncObj()) {

            FrostSharedFileObject oldSfo = getFrostSharedFileObject(newSfo.getSha1());
            if( oldSfo != null ) {
                // file is already in FILELIST table, maybe add new FILEOWNERBOARD and update fields
                identity = oldSfo.getPrimkey().longValue();
                // maybe update oldSfo
                boolean doUpdate = false;
                if( oldSfo.getKey() == null && newSfo.getKey() != null ) {
                    oldSfo.setKey(newSfo.getKey()); doUpdate = true;
                }
                if( oldSfo.getLastReceived().getTime() < newSfo.getLastReceived().getTime() ) {
                    oldSfo.setLastReceived(newSfo.getLastReceived()); doUpdate = true;
                }
                if( oldSfo.getLastDownloaded() == null && newSfo.getLastDownloaded() != null ) {
                    oldSfo.setLastDownloaded(newSfo.getLastDownloaded());
                } else if( oldSfo.getLastDownloaded() != null && 
                           newSfo.getLastDownloaded() != null &&
                           oldSfo.getLastDownloaded().getTime() < newSfo.getLastDownloaded().getTime() )
                {
                    oldSfo.setLastDownloaded(newSfo.getLastDownloaded()); doUpdate = true;
                }
                if( doUpdate ) {
                    updateFrostSharedFileObjectInFILELIST(oldSfo);
                }
            } else {
                // file is not yet in FILELIST table
                Long longIdentity = insertFrostSharedFileObjectIntoFILELIST(newSfo);
                if( longIdentity == null ) {
                    return false;
                }
                identity = longIdentity.longValue();
            }
            
            // UNIQUE: refkey,owner,board
            for(Iterator i=newSfo.getFrostSharedFileObjectOwnerBoardList().iterator(); i.hasNext(); ) {
                    
                FrostSharedFileObjectOwnerBoard ob = (FrostSharedFileObjectOwnerBoard)i.next();
                ob.setRefkey(identity);
                
                updateOrInsertFrostSharedFileObjectOwnerBoard(ob);
            }            
        }        
        return true;
    }

    private FrostSharedFileObject getFrostSharedFileObject(String sha1) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "SELECT primkey,size,key,lastdownloaded,lastreceived FROM FILELIST WHERE sha1=?");
        
        ps.setString(1, sha1);
        
        FrostSharedFileObject fo = null;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            long primkey = rs.getLong(1);
            long size = rs.getLong(2);
            String key = rs.getString(3);
            java.sql.Date lastDownloaded = rs.getDate(4);
            java.sql.Date lastReceived = rs.getDate(5);

            if( key.length() == 0 ) {
                key = null;
            }

            fo = new FrostSharedFileObject(primkey, sha1, size, key, lastDownloaded, lastReceived);
        }
        rs.close();
        ps.close();
        
        return fo;
    }

    private FrostSharedFileObject getFrostSharedFileObject(long primkey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "SELECT sha1,size,key,lastdownloaded,lastreceived FROM FILELIST WHERE primkey=?");
        
        ps.setLong(1, primkey);
        
        FrostSharedFileObject fo = null;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            String sha1 = rs.getString(1);
            long size = rs.getLong(2);
            String key = rs.getString(3);
            java.sql.Date lastDownloaded = rs.getDate(4);
            java.sql.Date lastReceived = rs.getDate(5);
            
            if( key.length() == 0 ) {
                key = null;
            }
            
            fo = new FrostSharedFileObject(primkey, sha1, size, key, lastDownloaded, lastReceived);
        }
        rs.close();
        ps.close();
        
        return fo;
    }

    private Long insertFrostSharedFileObjectIntoFILELIST(FrostSharedFileObject sfo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
            "INSERT INTO FILELIST (sha1,size,key,lastdownloaded,lastreceived) VALUES (?,?,?,?,?)");
        
        ps.setString(1, sfo.getSha1());
        ps.setLong(2, sfo.getSize());
        ps.setString(3, (sfo.getKey()==null?"":sfo.getKey()));
        ps.setDate(4, sfo.getLastDownloaded());
        ps.setDate(5, sfo.getLastReceived());
        
        boolean wasOk = (ps.executeUpdate()==1);
        ps.close();
        
        if( !wasOk ) {
            logger.log(Level.SEVERE,"Error inserting new item into filelist");
            return null;
        }

        // get generated identity
        Long identity = null;
        Statement s = db.createStatement();
        ResultSet rs = s.executeQuery("CALL IDENTITY();");
        if( rs.next() ) {
            identity = new Long(rs.getLong(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve the generated identity after insert!");
        }
        rs.close();
        s.close();

        return identity;
    }

    /**
     * Update item with SHA1, set key,lastreceived,lastdownloaded
     */
    private boolean updateFrostSharedFileObjectInFILELIST(FrostSharedFileObject sfo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
            "UPDATE FILELIST SET key=?,lastdownloaded=?,lastreceived=? WHERE sha1=?");
        
        ps.setString(1, (sfo.getKey()==null?"":sfo.getKey()));
        ps.setDate(2, sfo.getLastDownloaded());
        ps.setDate(3, sfo.getLastReceived());
        ps.setString(4, sfo.getSha1());
        
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
    private boolean updateOrInsertFrostSharedFileObjectOwnerBoard(FrostSharedFileObjectOwnerBoard obNew) throws SQLException {
        
        FrostSharedFileObjectOwnerBoard obOld = getFrostSharedFileObjectOwnerBoard(
                obNew.getRefkey(),
                obNew.getBoard().getName(),
                obNew.getOwner());
        
        if( obOld == null ) {
            // insert new
            return insertFrostSharedFileObjectOwnerBoard(obNew);
        } else {
            // update existing
            if( obOld.getLastReceived().getTime() < obNew.getLastReceived().getTime() ) {

                obOld.setLastReceived(obNew.getLastReceived());
                obOld.setName(obNew.getName());
                obOld.setLastUploaded(obNew.getLastUploaded());

                return updateFrostSharedFileObjectOwnerBoard(obOld);
            }
            return true; // no need to update, lastReceived of new was earlier
        }
    }

    /** 
     * update name,lastreceived,lastuploaded
     */
    private boolean updateFrostSharedFileObjectOwnerBoard(FrostSharedFileObjectOwnerBoard ob) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "UPDATE FILEOWNERBOARDLIST SET name=?,lastreceived=?,lastuploaded=? WHERE refkey=? AND BOARD=? AND OWNER=?");

        // insert board/owner, identity is set
        ps.setString(1, ob.getName());
        ps.setDate(2, ob.getLastReceived());
        ps.setDate(3, ob.getLastUploaded());
        
        ps.setLong(4, ob.getRefkey());
        ps.setString(5, ob.getBoard().getName());
        ps.setString(6, (ob.getOwner()==null?"":ob.getOwner()));
        
        boolean result = false;
        try {
            ps.executeUpdate();
            result = true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE,"Error updating file owner board ref", ex);
        }
        ps.close();
        
        return result;
    }

    /** 
     * update name,lastreceived,lastuploaded
     */
    private boolean insertFrostSharedFileObjectOwnerBoard(FrostSharedFileObjectOwnerBoard ob) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "INSERT INTO FILEOWNERBOARDLIST (refkey,name,board,owner,lastreceived,lastuploaded) VALUES (?,?,?,?,?,?)");

        // insert board/owner, identity is set
        ps.setLong(1, ob.getRefkey());
        ps.setString(2, ob.getName());
        ps.setString(3, ob.getBoard().getName());
        ps.setString(4, (ob.getOwner()==null?"":ob.getOwner()));
        ps.setDate(5, ob.getLastReceived());
        ps.setDate(6, ob.getLastUploaded());

        boolean result = false;
        try {
            ps.executeUpdate();
            result = true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE,"Error inserting file owner board ref", ex);
        }
        ps.close();
        
        return result;
    }

    private FrostSharedFileObjectOwnerBoard getFrostSharedFileObjectOwnerBoard(long refkey, String boardname, String owner) 
    throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT name,lastreceived,lastuploaded FROM FILEOWNERBOARDLIST WHERE refkey=? AND board=? and owner=?");
        
        ps.setLong(1, refkey);
        ps.setString(2, boardname);
        ps.setString(3, (owner==null?"":owner));

        FrostSharedFileObjectOwnerBoard ob = null;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            String name = rs.getString(1);
            java.sql.Date lastreceived = rs.getDate(2);
            java.sql.Date lastuploaded = rs.getDate(3);
            
            Board board = null;
            board = MainFrame.getInstance().getTofTreeModel().getBoardByName(boardname);
            if (board == null) {
                logger.warning("Upload item found (" + name + ") whose target board (" +
                        boardname + ") does not exist.");
                ob = null;
            } else {
                ob = new FrostSharedFileObjectOwnerBoard(refkey, name, board, owner, lastreceived, lastuploaded);
            }
        }
        rs.close();
        ps.close();
        
        return ob;
    }

    private List getFrostSharedFileObjectOwnerBoardList(long refkey) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT board,owner,name,lastreceived,lastuploaded FROM FILEOWNERBOARDLIST WHERE refkey=?");
        
        ps.setLong(1, refkey);

        LinkedList frostSharedFileObjectOwnerBoardList = new LinkedList(); 
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            String boardname = rs.getString(1);
            String owner = rs.getString(2);
            if(owner != null && owner.length()==0) {
                owner = null; // anonymous
            }
            String name = rs.getString(3);
            java.sql.Date lastreceived = rs.getDate(4);
            java.sql.Date lastuploaded = rs.getDate(5);
            
            Board board = null;
            board = MainFrame.getInstance().getTofTreeModel().getBoardByName(boardname);
            if (board == null) {
                logger.warning("Upload item found (" + name + ") whose target board (" +
                        boardname + ") does not exist.");
            } else {
                FrostSharedFileObjectOwnerBoard ob = null;
                ob = new FrostSharedFileObjectOwnerBoard(refkey, name, board, owner, lastreceived, lastuploaded);
                frostSharedFileObjectOwnerBoardList.add(ob);
            }
        }
        rs.close();
        ps.close();
        
        return frostSharedFileObjectOwnerBoardList;
    }
    
    /**
     * Return filecount for specified board.
     */
    public int getFileCount(Board board) throws SQLException {
        // alle sha1 die mindestens eine ref zu ownerboard haben wo das board matcht
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
            "SELECT COUNT(primkey) FROM FILELIST WHERE primkey=(SELECT refkey FROM FILEOWNERBOARDLIST WHERE board=? GROUP BY refkey)");
        ps.setString(1, board.getName());
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
    public void retrieveFilesByBoards(List boardsToSearch, FileListDatabaseTableCallback callback) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        if( boardsToSearch.size() == 0 ) {
            return;
        }
        
        String sql = "SELECT refkey FROM FILEOWNERBOARDLIST WHERE board=?";

        boolean firstLoop = true;
        for(int x=boardsToSearch.size(); x >= 0; x--) {
            if( firstLoop ) {
                firstLoop=false;
            } else {
                sql += " OR board=?";
            }
        }
        sql += " GROUP BY refkey";

        PreparedStatement ps = db.prepare(sql);
        int ix=1;
        for(Iterator i=boardsToSearch.iterator(); i.hasNext(); ) {
            Board b = (Board)i.next();
            ps.setString(ix++, b.getName()); // TODO: check case!
        }
        
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            long refkey = rs.getLong(1);
            
            FrostSharedFileObject fo = getFrostSharedFileObject(refkey);
            List obs = getFrostSharedFileObjectOwnerBoardList(refkey);
            fo.getFrostSharedFileObjectOwnerBoardList().addAll(obs);
            
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
    public void retrieveFilesAllBoards(FileListDatabaseTableCallback callback) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
            "SELECT primkey,sha1,size,key,lastdownloaded,lastreceived FROM FILELIST");
    
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            int ix=1;
            long primkey = rs.getLong(ix++);
            String sha1 = rs.getString(ix++);
            long size = rs.getLong(ix++);
            String key = rs.getString(ix++);
            java.sql.Date lastDownloaded = rs.getDate(ix++);
            java.sql.Date lastReceived = rs.getDate(ix++);

            if( key.length() == 0 ) {
                key = null;
            }

            FrostSharedFileObject fo = new FrostSharedFileObject(primkey, sha1, size, key, lastDownloaded, lastReceived);
            List obs = getFrostSharedFileObjectOwnerBoardList(primkey);
            fo.getFrostSharedFileObjectOwnerBoardList().addAll(obs);
 
            boolean shouldStop = callback.fileRetrieved(fo); // pass to callback
            if( shouldStop ) {
                break;
            }
        }
        rs.close();
        ps.close();
    }
}
