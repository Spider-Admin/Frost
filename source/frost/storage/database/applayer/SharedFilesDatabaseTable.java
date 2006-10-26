/*
 SharedFilesDatabaseTable.java / Frost
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

import javax.swing.*;

import frost.*;
import frost.fileTransfer.sharing.*;
import frost.storage.database.*;
import frost.util.gui.translation.*;

/**
 * This table contains all our own shared files.
 * One file can be shared only by one local identity.
 */
public class SharedFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(SharedFilesDatabaseTable.class.getName());

    private final static String SQL_SHAREDFILES_DDL =
        "CREATE TABLE SHAREDFILES ("+

          "path VARCHAR NOT NULL,"+   // complete local path, with name
          "size BIGINT NOT NULL,"+    // file size
          "fnkey VARCHAR,"+           // if NULL file was not uploaded by us yet

          "sha VARCHAR NOT NULL,"+    // checksum of file
          "owner VARCHAR NOT NULL,"+  // our local identity that shares this file
          "comment VARCHAR,"+         // our file comment
          "rating INT,"+              // our file rating
          "keywords VARCHAR,"+        // our file keywords

          "lastuploaded BIGINT,"+     // date of last successful upload
          "uploadcount INT,"+         // number of uploads for this file so far
          
          // some fields like in FileListDatabaseTable, but especially for this file
          "reflastsent BIGINT NOT NULL,"+  // last time we sent this ref inside a CHK file

          "requestlastreceived BIGINT,"+  // time when we received the last request for this sha
          "requestsreceivedcount INT,"+   // received requests count
          
          "lastmodified BIGINT,"+

        "CONSTRAINT SHAREDFILES_1 UNIQUE(sha) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_SHAREDFILES_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE SHAREDFILES");
        return true;
    }

    public void saveSharedFiles(List sfFiles) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM SHAREDFILES"); // delete all

        PreparedStatement ps = db.prepare(
                "INSERT INTO SHAREDFILES ("+
                  "path,size,fnkey,sha,owner,comment,rating,keywords,"+
                  "lastuploaded,uploadcount,reflastsent,requestlastreceived,requestsreceivedcount,lastmodified) "+
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        for(Iterator i=sfFiles.iterator(); i.hasNext(); ) {

            FrostSharedFileItem sfItem = (FrostSharedFileItem)i.next();
            
            int ix=1;
            ps.setString(ix++, sfItem.getFile().getPath());
            ps.setLong(ix++, sfItem.getFileSize());
            ps.setString(ix++, sfItem.getChkKey());
            ps.setString(ix++, sfItem.getSha());
            ps.setString(ix++, sfItem.getOwner());
            ps.setString(ix++, sfItem.getComment());
            ps.setInt(ix++, sfItem.getRating());
            ps.setString(ix++, sfItem.getKeywords());
            ps.setLong(ix++, sfItem.getLastUploaded());
            ps.setInt(ix++, sfItem.getUploadCount());
            ps.setLong(ix++, sfItem.getRefLastSent());
            ps.setLong(ix++, sfItem.getRequestLastReceived());
            ps.setInt(ix++, sfItem.getRequestsReceived());
            ps.setLong(ix++, sfItem.getLastModified());
            
            ps.executeUpdate();
        }
        ps.close();
    }
    
    public List loadSharedFiles() throws SQLException {

        LinkedList sfItems = new LinkedList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT "+
                  "path,size,fnkey,sha,owner,comment,rating,keywords,"+
                  "lastuploaded,uploadcount,reflastsent,requestlastreceived,requestsreceivedcount,lastmodified "+
                "FROM SHAREDFILES");

        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix = 1;
            String filepath = rs.getString(ix++);
            long filesize = rs.getLong(ix++);
            String key = rs.getString(ix++);
            
            String sha = rs.getString(ix++);
            String owner = rs.getString(ix++);
            String comment = rs.getString(ix++);
            int rating = rs.getInt(ix++);
            String keywords = rs.getString(ix++);
            long lastUploaded = rs.getLong(ix++);
            int uploadCount = rs.getInt(ix++);
            long refLastSent = rs.getLong(ix++);
            long requestLastReceived = rs.getLong(ix++);
            int requestsReceivedCount = rs.getInt(ix++);
            long lastModified = rs.getLong(ix++);

            boolean fileIsOk = true;
            File file = new File(filepath);

            Language language = Language.getInstance();
            if( !file.isFile() ) {
                String title = language.getString("StartupMessage.sharedFile.sharedFileNotFound.title");
                String text = language.formatMessage("StartupMessage.sharedFile.sharedFileNotFound.text", filepath);
                MainFrame.enqueueStartupMessage(title, text, JOptionPane.WARNING_MESSAGE);
                logger.severe("Shared file does not exist: "+filepath);
                fileIsOk = false;
            } else if( file.length() != filesize ) {
                String title = language.getString("StartupMessage.sharedFile.sharedFileSizeChanged.title");
                String text = language.formatMessage("StartupMessage.sharedFile.sharedFileSizeChanged.text", filepath);
                MainFrame.enqueueStartupMessage(title, text, JOptionPane.WARNING_MESSAGE);
                logger.severe("Size of shared file changed: "+filepath);
                fileIsOk = false;
            } else if( file.lastModified() != lastModified ) {
                String title = language.getString("StartupMessage.sharedFile.sharedFileLastModifiedChanged.title");
                String text = language.formatMessage("StartupMessage.sharedFile.sharedFileLastModifiedChanged.text", filepath);
                MainFrame.enqueueStartupMessage(title, text, JOptionPane.WARNING_MESSAGE);
                logger.severe("Last modified date of shared file changed: "+filepath);
                fileIsOk = false;
            }
            
            FrostSharedFileItem sfItem = new FrostSharedFileItem(
                    file,
                    filesize,
                    key,
                    sha,
                    owner,
                    comment,
                    rating,
                    keywords,
                    lastUploaded,
                    uploadCount,
                    refLastSent,
                    requestLastReceived,
                    requestsReceivedCount,
                    lastModified,
                    fileIsOk);

            sfItems.add(sfItem);
        }
        rs.close();
        ps.close();

        return sfItems;
    }
}
