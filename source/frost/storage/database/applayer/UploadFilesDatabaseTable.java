/*
 UploadFilesDatabaseTable.java / Frost
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

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.fileTransfer.sharing.*;
import frost.fileTransfer.upload.*;
import frost.storage.database.*;

/**
 * This table contains the currently uploaded files and their state.
 * Onyl manually added files are saved, the uploading shared files have their own state,
 * but both types of files appear in the GUI upload table.
 */
public class UploadFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(UploadFilesDatabaseTable.class.getName());
    
    private final static String SQL_FILES_DDL =
        "CREATE TABLE UPLOADFILES ("+
          "path VARCHAR NOT NULL,"+   // complete path, with name
          "size BIGINT NOT NULL,"+
          "fnkey VARCHAR,"+           // if NULL file was not uploaded by us yet
          
          "enabled BOOLEAN,"+         // is upload enabled?
          "state INT,"+  // uploading, waiting, finished
          "uploadaddedtime BIGINT,"+
          "uploadstartedtime BIGINT,"+
          "uploadfinishedtime BIGINT,"+
          "retries INT,"+             // number of upload tries, set to 0 on any successful upload
          "lastuploadstoptime BIGINT,"+ // millis when upload stopped the last time, needed to schedule uploads
          "gqid VARCHAR,"+             // global queue identifier (name-unique_id)
          
          "sharedfilessha VARCHAR,"+   // if set then this uploadfile is a shared file
        "CONSTRAINT UPLOADFILES_1 UNIQUE(path) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_FILES_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE UPLOADFILES");
        return true;
    }

    public void saveUploadFiles(List uploadFiles) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM UPLOADFILES"); // delete all

        PreparedStatement ps = db.prepare(
                "INSERT INTO UPLOADFILES ("+
                  "path,size,fnkey,enabled,state," +
                  "uploadaddedtime,uploadstartedtime,uploadfinishedtime,retries,lastuploadstoptime,gqid," +
                  "sharedfilessha) "+
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
        
        for(Iterator i=uploadFiles.iterator(); i.hasNext(); ) {

            FrostUploadItem ulItem = (FrostUploadItem)i.next();
            
            int ix=1;
            ps.setString(ix++, ulItem.getFile().getPath());
            ps.setLong(ix++, ulItem.getFileSize());
            ps.setString(ix++, ulItem.getKey());
            ps.setBoolean(ix++, (ulItem.isEnabled()==null?true:ulItem.isEnabled().booleanValue()));
            ps.setInt(ix++, ulItem.getState());
            ps.setLong(ix++, ulItem.getUploadAddedMillis());
            ps.setLong(ix++, ulItem.getUploadStartedMillis());
            ps.setLong(ix++, ulItem.getUploadFinishedMillis());
            ps.setInt(ix++, ulItem.getRetries());
            ps.setLong(ix++, ulItem.getLastUploadStopTimeMillis());
            ps.setString(ix++, ulItem.getGqIdentifier());
            
            ps.setString(ix++, (ulItem.getSharedFileItem()==null?null:ulItem.getSharedFileItem().getSha()));
            
            ps.executeUpdate();
        }
        ps.close();
    }
    
    public List loadUploadFiles(List sharedFiles) throws SQLException {

        LinkedList uploadItems = new LinkedList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT path,size,fnkey,enabled,state," +
                "uploadaddedtime,uploadstartedtime,uploadfinishedtime,retries,lastuploadstoptime,gqid,sharedfilessha "+
                "FROM UPLOADFILES");

        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix = 1;
            String filepath = rs.getString(ix++);
            long filesize = rs.getLong(ix++);
            String key = rs.getString(ix++);
            boolean isEnabled = rs.getBoolean(ix++);
            int state = rs.getInt(ix++);
            long uploadAddedTime = rs.getLong(ix++);
            long uploadStartedTime = rs.getLong(ix++);
            long uploadFinishedTime = rs.getLong(ix++);
            int retries = rs.getInt(ix++);
            long lastUploadStopMillis = rs.getLong(ix++);
            String gqId = rs.getString(ix++);
            
            String sharedFilesSha = rs.getString(ix++);
            
            File file = new File(filepath);
            if( !file.isFile() ) {
                logger.warning("Upload items file does not exist, removed from upload files: "+filepath);
                continue;
            }
            if( file.length() != filesize ) {
                logger.warning("Upload items file size changed, removed from upload files: "+filepath);
                continue;
            }
            
            FrostSharedFileItem sharedFileItem = null;
            if( sharedFilesSha != null && sharedFilesSha.length() > 0 ) {
                for(Iterator i = sharedFiles.iterator(); i.hasNext(); ) {
                    FrostSharedFileItem s = (FrostSharedFileItem)i.next();
                    if( s.getSha().equals(sharedFilesSha) ) {
                        sharedFileItem = s;
                        break;
                    }
                }
                if( sharedFileItem == null ) {
                    logger.warning("Upload items shared file object does not exist, removed from upload files: "+filepath);
                    continue;
                }
            }
            
            FrostUploadItem ulItem = new FrostUploadItem(
                    file,
                    filesize,
                    key,
                    isEnabled,
                    state,
                    uploadAddedTime,
                    uploadStartedTime,
                    uploadFinishedTime,
                    retries,
                    lastUploadStopMillis,
                    gqId);
            
            ulItem.setSharedFileItem(sharedFileItem);

            uploadItems.add(ulItem);
        }
        rs.close();
        ps.close();

        return uploadItems;
    }
}
