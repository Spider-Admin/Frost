/*
  NewUploadFilesTable.java / Frost
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
import frost.gui.objects.*;
import frost.storage.database.*;

/**
 * Access to a database table that holds the new upload files which
 * do not have a SHA1 yet. A thread gets the entries from this file
 * one by one and computes the SHA1. Then the entries are stored into 
 * the UPLOADFILES database table and are deleted from this table.
 * 
 * This table is needed in case the user shuts down Frost until all
 * SHA1 checksums were deleted.
 */
public class NewUploadFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(NewUploadFilesDatabaseTable.class.getName());

    private final static String SQL_DDL =
        "CREATE TABLE NEWUPLOADFILES ("+
        "filepath VARCHAR NOT NULL,"+
        "targetboard VARCHAR NOT NULL,"+
        "fromname VARCHAR,"+
        "CONSTRAINT NEWUPLOADFILES_1 UNIQUE (filepath,targetboard) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_DDL);
        return lst;
    }
    
    public void saveNewUploadFiles(List newUploadFiles) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM NEWUPLOADFILES");
        s.close();
        
        PreparedStatement ps = db.prepare("INSERT INTO NEWUPLOADFILES (filepath,targetboard,fromname) VALUES (?,?,?)");
        
        for(Iterator i=newUploadFiles.iterator(); i.hasNext(); ) {
            NewUploadFile nuf = (NewUploadFile)i.next(); 
            
            ps.setString(1, nuf.getFile().getPath());
            ps.setString(2, nuf.getTargetBoard().getName());
            ps.setString(3, nuf.getFrom());
            
            ps.executeUpdate();
        }
        ps.close();
    }

    public LinkedList loadNewUploadFiles() throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare("SELECT filepath,targetboard,fromname FROM NEWUPLOADFILES");

        LinkedList newUploadFiles = new LinkedList();
        
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            String filepath = rs.getString(1);
            String boardname = rs.getString(2);
            String from = rs.getString(3);
            
            Board board = null;
            if (boardname != null) {
                board = MainFrame.getInstance().getTofTreeModel().getBoardByName(boardname);
                if (board == null) {
                    logger.warning("Board ("+boardname+" for file ("+filepath+") is missing. File removed.");
                    continue;
                }
            } else {
                continue;
            }
            
            File f = new File(filepath);
            if (!f.isFile()) {
                logger.warning("File ("+filepath+") is missing. File removed.");
                continue;
            }
            
            NewUploadFile nuf = new NewUploadFile(f, board, from);
            newUploadFiles.add(nuf);
        }
        rs.close();
        ps.close();
        
        return newUploadFiles;
    }
}
