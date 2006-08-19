/*
 DownloadFilesDatabaseTable.java / Frost
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

import frost.*;
import frost.fileTransfer.download.*;
import frost.gui.objects.*;
import frost.storage.database.*;

public class DownloadFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(DownloadFilesDatabaseTable.class.getName());

    private final static String SQL_DDL =
        "CREATE TABLE DOWNLOADFILES ("+
        "primkey BIGINT DEFAULT UNIQUEKEY('DOWNLOADFILES') NOT NULL,"+
        "name VARCHAR NOT NULL,"+          // filename
        "state INT NOT NULL,"+ 
        "enabled BOOLEAN NOT NULL,"+       // is upload enabled?
        "retries INT NOT NULL,"+           // number of upload tries, set to 0 on any successful upload
        "targetpath VARCHAR,"+    // set by us
        "laststopped TIMESTAMP NOT NULL,"+ // time of last start of download

        "board INT,"+  // only set for board files, not needed for attachments/manually added files. -1 means not set!
        "fromname VARCHAR,"+
        "sha1 VARCHAR,"+          // maybe not set, for attachments/manually added files
        "lastrequested DATE,"+    // date of last sent request for this file
        "requestcount INT NOT NULL,"+      // number of requests sent for this file
        
        // TODO: during upload of index, check if a download file must be requested.
        //   if a file must be requested for the current board, request it.
        //   but if the requestcount is high, request it in other boards if possible
        
        // fnkey: NOT NULL, because here "" means not set. sql select for NULL values does not work!
        "fnkey VARCHAR NOT NULL,"+ // maybe not set for board files -> request key, use infos from FILELIST table (sha1)
        "size BIGINT,"+ // size is not set if the key was added manually
        "CONSTRAINT dlf_pk PRIMARY KEY (primkey),"+
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
                "INSERT INTO DOWNLOADFILES (name,state,enabled,retries,targetpath,laststopped,board,sha1,fromname,lastrequested,"+
                "requestcount,fnkey,size) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        for(Iterator i=downloadFiles.iterator(); i.hasNext(); ) {

            FrostDownloadItem dlItem = (FrostDownloadItem)i.next();

            int ix=1;
            ps.setString(ix++, dlItem.getFileName());
            ps.setInt(ix++, dlItem.getState());
            ps.setBoolean(ix++, (dlItem.getEnableDownload()==null?true:dlItem.getEnableDownload().booleanValue()));
            ps.setInt(ix++, dlItem.getRetries());
            ps.setString(ix++, null); // targetpath
            ps.setTimestamp(ix++, new Timestamp(dlItem.getLastDownloadStopTimeMillis()));
            ps.setInt(ix++, (dlItem.getSourceBoard()==null?-1:dlItem.getSourceBoard().getPrimaryKey().intValue()));
            ps.setString(ix++, dlItem.getSHA1());
            ps.setString(ix++, dlItem.getOwner());
            ps.setDate(ix++, dlItem.getLastRequestedDate());
            ps.setInt(ix++, dlItem.getRequestedCount());
            ps.setString(ix++, (dlItem.getKey()==null?"":dlItem.getKey()));
            ps.setLong(ix++, (dlItem.getFileSize()==null?0:dlItem.getFileSize().longValue()));
            
            ps.executeUpdate();
        }
        ps.close();
    }
    
    public List loadDownloadFiles() throws SQLException {

        LinkedList downloadItems = new LinkedList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "SELECT name,state,enabled,retries,laststopped,board,sha1,fromname,lastrequested,requestcount,fnkey,size "+
                "FROM DOWNLOADFILES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix=1;
            String filename = rs.getString(ix++);
            int state = rs.getInt(ix++);
            boolean enabledownload = rs.getBoolean(ix++);
            int retries = rs.getInt(ix++);
            long lastStopped = rs.getTimestamp(ix++).getTime();
            int boardname = rs.getInt(ix++);
            String sha1 = rs.getString(ix++);
            String from = rs.getString(ix++);
            java.sql.Date lastRequested = rs.getDate(ix++);
            int requestCount = rs.getInt(ix++);
            String key = rs.getString(ix++);
            long size = rs.getLong(ix++);
            
            Board board = null;
            if (boardname >= 0) {
                board = MainFrame.getInstance().getTofTreeModel().getBoardByPrimaryKey(new Integer(boardname));
                if (board == null) {
                    logger.warning("Download item found (" + filename + ") whose source board (" +
                            boardname + ") does not exist.");
                    if( key == null || key.length() == 0 ) {
                        // if we have no key we can't continue to download because we can't request the file
                        continue;
                    }
                }
            }
            FrostDownloadItem dlItem = new FrostDownloadItem(
                    filename,
                    (size==0?null:new Long(size)),
                    (key.length()==0?null:key),
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
