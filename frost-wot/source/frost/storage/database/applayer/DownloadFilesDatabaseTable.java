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

import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.storage.database.*;

/**
 * Stores manually added download files
 */
public class DownloadFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(DownloadFilesDatabaseTable.class.getName());

    private final static String SQL_DDL =
        "CREATE TABLE IF NOT EXISTS DOWNLOADFILES ("+

          "name VARCHAR NOT NULL,"+          // filename
          "targetpath VARCHAR,"+    // set by us
          "size BIGINT,"+                    // size is maybe not set if the key was added manually
          "fnkey VARCHAR,"+
        
          "enabled BOOLEAN,"+       // is upload enabled?
          "state INT,"+ 
          "downloadaddedtime BIGINT,"+
          "downloadstartedtime BIGINT,"+
          "downloadfinishedtime BIGINT,"+
          "retries INT,"+                 // number of upload tries, set to 0 on any successful upload
          "lastdownloadstoptime BIGINT,"+ // time of last start of download
          "gqid VARCHAR,"+                // global queue id
          
          "filelistfilesha VARCHAR,"+

        "CONSTRAINT DOWNLOADFILES_1 UNIQUE (fnkey) )";  // check before adding a new file!
    
    public List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE DOWNLOADFILES");
        return true;
    }
    
    public void saveDownloadFiles(List downloadFiles) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM DOWNLOADFILES"); // delete all
        s.close();

        PreparedStatement ps = db.prepare(
                "INSERT INTO DOWNLOADFILES " +
                "(name,targetpath,size,fnkey,enabled,state,downloadaddedtime,downloadstartedtime,downloadfinishedtime,"+
                "retries,lastdownloadstoptime,gqid,filelistfilesha) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        for(Iterator i=downloadFiles.iterator(); i.hasNext(); ) {

            FrostDownloadItem dlItem = (FrostDownloadItem)i.next();

            int ix=1;
            ps.setString(ix++, dlItem.getFileName());
            ps.setString(ix++, dlItem.getTargetPath());
            ps.setLong(ix++, (dlItem.getFileSize()==null?0:dlItem.getFileSize().longValue()));
            ps.setString(ix++, dlItem.getKey());
            ps.setBoolean(ix++, (dlItem.getEnableDownload()==null?true:dlItem.getEnableDownload().booleanValue()));
            ps.setInt(ix++, dlItem.getState());
            ps.setLong(ix++, dlItem.getDownloadAddedTime());
            ps.setLong(ix++, dlItem.getDownloadStartedTime());
            ps.setLong(ix++, dlItem.getDownloadFinishedTime());
            ps.setInt(ix++, dlItem.getRetries());
            ps.setLong(ix++, dlItem.getLastDownloadStopTime());
            ps.setString(ix++, dlItem.getGqId());
            ps.setString(ix++, dlItem.getFileListFileObject()==null?null:dlItem.getFileListFileObject().getSha());
            
            ps.executeUpdate();
        }
        ps.close();
    }
    
    public List loadDownloadFiles() throws SQLException {

        LinkedList downloadItems = new LinkedList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "SELECT " +
                "name,targetpath,size,fnkey,enabled,state,downloadaddedtime,downloadstartedtime,downloadfinishedtime,"+
                "retries,lastdownloadstoptime,gqid,filelistfilesha FROM DOWNLOADFILES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix=1;
            String filename = rs.getString(ix++);
            String targetPath = rs.getString(ix++);
            long size = rs.getLong(ix++);
            String key = rs.getString(ix++);
            boolean enabledownload = rs.getBoolean(ix++);
            int state = rs.getInt(ix++);
            long downloadAddedTime = rs.getLong(ix++);
            long downloadStartedTime = rs.getLong(ix++);
            long downloadFinishedTime = rs.getLong(ix++);
            int retries = rs.getInt(ix++);
            long lastDownloadStopTime = rs.getLong(ix++);
            String gqId = rs.getString(ix++);
            String sharedFileSha = rs.getString(ix++);
            
            FrostFileListFileObject sharedFileObject = null;
            if( sharedFileSha != null && sharedFileSha.length() > 0 ) {
                sharedFileObject = AppLayerDatabase.getFileListDatabaseTable().retrieveFileBySha(sharedFileSha);
                if( sharedFileObject == null && key == null ) {
                    // no fileobject and no key -> we can't continue to download this file
                    logger.warning("DownloadUpload items file list file object does not exist, and there is no key. " +
                                   "Removed from upload files: "+filename);
                }
            }

            FrostDownloadItem dlItem = new FrostDownloadItem(
                    filename,
                    targetPath,
                    (size==0?null:new Long(size)),
                    key,
                    Boolean.valueOf(enabledownload),
                    state,
                    downloadAddedTime,
                    downloadStartedTime,
                    downloadFinishedTime,
                    retries,
                    lastDownloadStopTime,
                    gqId);
            
            dlItem.setFileListFileObject(sharedFileObject);

            downloadItems.add(dlItem);
        }
        rs.close();
        ps.close();

        return downloadItems;
    }
}
