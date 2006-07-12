package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.gui.objects.*;
import frost.storage.database.*;

public class DownloadFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(DownloadFilesDatabaseTable.class.getName());

    private final static String SQL_DDL =
        "CREATE TABLE DOWNLOADFILES ("+
        "primkey BIGINT NOT NULL IDENTITY PRIMARY KEY,"+
        "name VARCHAR NOT NULL,"+          // filename
        "state INT NOT NULL,"+ 
        "enabled BOOLEAN NOT NULL,"+       // is upload enabled?
        "retries INT NOT NULL,"+           // number of upload tries, set to 0 on any successful upload
        "targetpath VARCHAR,"+    // set by us
        "laststopped TIMESTAMP NOT NULL,"+ // time of last start of download

        "board VARCHAR,"+         // only set for board files, not needed for attachments/manually added files
        "fromname VARCHAR,"+
        "sha1 VARCHAR,"+          // maybe not set, for attachments/manually added files
        "lastrequested DATE,"+    // date of last sent request for this file
        "requestcount INT NOT NULL,"+      // number of requests sent for this file
        
        // TODO: during upload of index, check if a download file must be requested.
        //   if a file must be requested for the current board, request it.
        //   but if the requestcount is high, request it in other boards if possible
        
        // key: NOT NULL, because here "" means not set. sql select for NULL values does not work!
        "key VARCHAR NOT NULL,"+ // maybe not set for board files -> request key, use infos from FILELIST table (sha1)
        "size BIGINT,"+ // size is not set if the key was added manually
        
        "CONSTRAINT DOWNLOADFILES_2 UNIQUE (name) )";  // check before adding a new file!
    
    // TODO: update FILELIST table with lastdownloaded date after successful download
    
    public List getTableDDL() {
        ArrayList lst = new ArrayList(3);
        lst.add(SQL_DDL);
        return lst;
    }
    
    public void saveDownloadFiles(List downloadFiles) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM DOWNLOADFILES"); // delete all
        s.close();

        PreparedStatement ps = db.prepare(
                "INSERT INTO DOWNLOADFILES (name,state,enabled,retries,laststopped,board,sha1,fromname,lastrequested,"+
                "requestcount,key,size) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
        
        for(Iterator i=downloadFiles.iterator(); i.hasNext(); ) {

            FrostDownloadItem dlItem = (FrostDownloadItem)i.next();

            ps.setString(1, dlItem.getFileName());
            ps.setInt(2, dlItem.getState());
            ps.setBoolean(3, (dlItem.getEnableDownload()==null?true:dlItem.getEnableDownload().booleanValue()));
            ps.setInt(4, dlItem.getRetries());
            ps.setTimestamp(5, new Timestamp(dlItem.getLastDownloadStopTimeMillis()));
            ps.setString(6, (dlItem.getSourceBoard()==null?null:dlItem.getSourceBoard().getName()));
            ps.setString(7, dlItem.getSHA1());
            ps.setString(8, dlItem.getOwner());
            ps.setDate(9, dlItem.getLastRequestedDate());
            ps.setInt(10, dlItem.getRequestedCount());
            ps.setString(11, dlItem.getKey());
            ps.setLong(12, (dlItem.getFileSize()==null?0:dlItem.getFileSize().longValue()));
            
            ps.executeUpdate();
        }
        ps.close();
    }
    
    public List loadDownloadFiles() throws SQLException {

        LinkedList downloadItems = new LinkedList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "SELECT name,state,enabled,retries,laststopped,board,sha1,fromname,lastrequested,requestcount,key,size "+
                "FROM DOWNLOADFILES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix=1;
            String filename = rs.getString(ix++);
            int state = rs.getInt(ix++);
            boolean enabledownload = rs.getBoolean(ix++);
            int retries = rs.getInt(ix++);
            long lastStopped = rs.getTimestamp(ix++).getTime();
            String boardname = rs.getString(ix++);
            String sha1 = rs.getString(ix++);
            String from = rs.getString(ix++);
            java.sql.Date lastRequested = rs.getDate(ix++);
            int requestCount = rs.getInt(ix++);
            String key = rs.getString(ix++);
            long size = rs.getLong(ix++);
            
            Board board = null;
            if (boardname != null) {
                board = MainFrame.getInstance().getTofTreeModel().getBoardByName(boardname);
                if (board == null) {
                    logger.warning("Download item found (" + filename + ") whose source board (" +
                            boardname + ") does not exist. Board reference removed.");
                }
            }
            FrostDownloadItem dlItem = new FrostDownloadItem(
                    filename,
                    (size==0?null:new Long(size)),
                    key,
                    retries,
                    from,
                    sha1,
                    state,
                    enabledownload,
                    board,
                    requestCount,
                    lastRequested,
                    lastStopped);

            downloadItems.add(dlItem);
        }
        rs.close();
        ps.close();

        return downloadItems;
    }
}
