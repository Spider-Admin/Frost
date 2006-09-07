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

import frost.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.*;
import frost.storage.database.*;

public class UploadFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(UploadFilesDatabaseTable.class.getName());
    
    // FIXME: this table should track only the currently uploaded files, either manually added or from SharedFiles!

    private final static String SQL_FILES_DDL =
        "CREATE TABLE UPLOADFILES ("+
        "primkey BIGINT NOT NULL,"+
        "sha1 VARCHAR NOT NULL,"+
        "name VARCHAR NOT NULL,"+
        "path VARCHAR NOT NULL,"+   // complete path, with name
        "size BIGINT NOT NULL,"+
        "fnkey VARCHAR,"+           // if NULL file was not uploaded by us yet
        "lastuploaded DATE,"+       // date of last successful upload
        "uploadcount INT,"+         // number of uploads for this file so far
        "lastrequested DATE,"+      // date of last request (from any board)
        "requestcount INT,"+        // number of requests received for this file so far
        "state INT,"+
        "enabled BOOLEAN,"+         // is upload enabled?
        "laststopped TIMESTAMP NOT NULL,"+   // time of last start of upload
        "retries INT,"+             // number of upload tries, set to 0 on any successful upload
        "CONSTRAINT ulfiles_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT UPLOADFILES_1 UNIQUE(sha1) )";

    // TODO: wie NEWUPLOADFILES darstellen? erstmal als grau in die table, oder einfach einen todo-count ueber die table?
    
    private final static String SQL_OWNER_BOARD_DDL =
        "CREATE TABLE UPLOADFILESOWNERBOARD ("+
        "refkey BIGINT NOT NULL,"+
        "board INT NOT NULL,"+   // targetboard
        "fromname VARCHAR,"+         // if NULL we upload this file as anonymous
        "lastshared DATE,"+          // date when we sent this file in our index for this board
        // UNIQUE(refkey,board)!!!
        "CONSTRAINT UFOB_FK FOREIGN KEY (refkey) REFERENCES UPLOADFILES(primkey) ON DELETE CASCADE,"+
        "CONSTRAINT UFOB_FK2 FOREIGN KEY (board) REFERENCES BOARDS(primkey) ON DELETE CASCADE )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_FILES_DDL);
        lst.add(SQL_OWNER_BOARD_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE UPLOADFILES");
        stmt.executeUpdate("COMPACT TABLE UPLOADFILESOWNERBOARD");
        return true;
    }

    public void saveUploadFiles(List uploadFiles) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM UPLOADFILES"); // delete all
        s.executeUpdate("DELETE FROM UPLOADFILESOWNERBOARD"); // delete all

        PreparedStatement ps = db.prepare(
                "INSERT INTO UPLOADFILES (primkey,sha1,name,path,size,fnkey,lastuploaded,uploadcount,lastrequested,requestcount,"+
                "state,enabled,laststopped,retries) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        PreparedStatement ps2 = db.prepare("INSERT INTO UPLOADFILESOWNERBOARD (refkey,board,fromname,lastshared) VALUES (?,?,?,?)");
        
        for(Iterator i=uploadFiles.iterator(); i.hasNext(); ) {

            FrostUploadItem ulItem = (FrostUploadItem)i.next();
            
            Long identity = null;
            Statement stmt = AppLayerDatabase.getInstance().createStatement();
            ResultSet rs = stmt.executeQuery("select UNIQUEKEY('UPLOADFILES')");
            if( rs.next() ) {
                identity = new Long(rs.getLong(1));
            } else {
                logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
            }
            rs.close();
            stmt.close();

            int ix=1;
            ps.setLong(ix++, identity.longValue());
            ps.setString(ix++, ulItem.getSHA1());
            ps.setString(ix++, ulItem.getFileName());
            ps.setString(ix++, ulItem.getFilePath());
            ps.setLong(ix++, ulItem.getFileSize());
            ps.setString(ix++, ulItem.getKey());
            ps.setDate(ix++, ulItem.getLastUploadDate());
            ps.setInt(ix++, ulItem.getUploadCount());
            ps.setDate(ix++, ulItem.getLastRequestedDate());
            ps.setInt(ix++, ulItem.getRequestedCount());
            ps.setInt(ix++, ulItem.getState());
            ps.setBoolean(ix++, (ulItem.isEnabled()==null?true:ulItem.isEnabled().booleanValue()));
            ps.setTimestamp(ix++, new java.sql.Timestamp(ulItem.getLastUploadStopTimeMillis()));
            ps.setInt(ix++, ulItem.getRetries());
            
            ps.executeUpdate();
            
            // insert all owner/board refs
            for(Iterator j=ulItem.getFrostUploadItemOwnerBoardList().iterator(); j.hasNext(); ) {
                FrostUploadItemOwnerBoard fuiob = (FrostUploadItemOwnerBoard)j.next();
                ix=1;
                ps2.setLong(ix++, identity.longValue());
                ps2.setInt(ix++, fuiob.getTargetBoard().getPrimaryKey().intValue());
                ps2.setString(ix++, fuiob.getOwner());
                ps2.setDate(ix++, fuiob.getLastSharedDate());
                ps2.executeUpdate();
            }
        }
        ps.close();
        ps2.close();
    }
    
    public List loadUploadFiles() throws SQLException {

        LinkedList uploadItems = new LinkedList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT primkey,sha1,name,path,size,fnkey,lastuploaded,uploadcount,lastrequested,requestcount,"+
                "state,enabled,laststopped,retries FROM UPLOADFILES");

        PreparedStatement ps2 = db.prepare("SELECT board,fromname,lastshared FROM UPLOADFILESOWNERBOARD "+
                "WHERE refkey=?");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix=1;
            long primkey = rs.getLong(ix++);
            
            String sha1 = rs.getString(ix++);
            String filename = rs.getString(ix++);
            String filepath = rs.getString(ix++);
            long filesize = rs.getLong(ix++);
            String key = rs.getString(ix++);
            java.sql.Date lastUploadDate = rs.getDate(ix++);
            int uploadCount = rs.getInt(ix++);
            java.sql.Date lastRequestedDate = rs.getDate(ix++);
            int requestedCount = rs.getInt(ix++);
            int state = rs.getInt(ix++);
            boolean isEnabled = rs.getBoolean(ix++);
            long lastUploadStopMillis = rs.getTimestamp(ix++).getTime();
            int retries = rs.getInt(ix++);
            
            File file = new File(filepath);
            if( !file.isFile() ) {
                logger.warning("Upload items file does not exist, removed from upload files: "+filepath);
                continue;
            }
            if( file.length() != filesize ) {
                logger.warning("Upload items file size changed, removed from upload files: "+filepath);
                continue;
            }
            
            FrostUploadItem ulItem = new FrostUploadItem(
                    sha1,
                    filename,
                    filepath,
                    filesize,
                    key,
                    lastUploadDate,
                    uploadCount,
                    lastRequestedDate,
                    requestedCount,
                    state,
                    isEnabled,
                    lastUploadStopMillis,
                    retries);

            ps2.setLong(1, primkey);
            ResultSet rs2 = ps2.executeQuery();
            while(rs2.next()) {
                int boardIx = rs2.getInt(1);
                String fromname = rs2.getString(2);
                java.sql.Date lastshared = rs2.getDate(3);

                Board board = null;
                board = MainFrame.getInstance().getTofTreeModel().getBoardByPrimaryKey(new Integer(boardIx));
                if (board == null) {
                    logger.warning("Upload item found (" + filename + ") whose target board (" +
                            boardIx + ") does not exist. Board reference removed.");
                    continue;
                }
                
                if( fromname != null && Core.getIdentities().isMySelf(fromname) == false ) {
                    logger.warning("Own identity does not longer exist, owner reference removed: "+fromname);
                    continue;
                }

                FrostUploadItemOwnerBoard ob = new FrostUploadItemOwnerBoard(ulItem, board, fromname, lastshared);
                ulItem.addFrostUploadItemOwnerBoard(ob);
            }
            rs2.close();
            
            if( ulItem.getFrostUploadItemOwnerBoardList().size() == 0 ) {
                // drop item, no board
                logger.warning("Upload item removed, no single board/owner ref found: "+filepath);
                continue;
            }
            
            uploadItems.add(ulItem);
        }
        rs.close();
        ps.close();
        ps2.close();

        return uploadItems;
    }
}
