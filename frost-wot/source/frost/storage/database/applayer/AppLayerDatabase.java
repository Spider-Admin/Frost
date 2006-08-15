/*
  MessageDatabase.java / Frost
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

import frost.storage.database.*;

public class AppLayerDatabase extends Database {
    
    private static AppLayerDatabase instance = null;
    
    private static MessageDatabaseTable messageTable = null;
    private static SentMessageDatabaseTable sentMessageTable = null;
    
    private static NewUploadFilesDatabaseTable newUploadFilesTable = null;
    private static FileListDatabaseTable fileListDatabaseTable = null;
    private static UploadFilesDatabaseTable uploadFilesDatabaseTable = null;
    private static DownloadFilesDatabaseTable downloadFilesDatabaseTable = null;
    
    private static IdentitiesDatabaseTable identitiesDatabaseTable = null;
    
    private static KnownBoardsDatabaseTable knownBoardsDatabaseTable = null;
    
    protected AppLayerDatabase() throws SQLException {
        super("applayerdb");
    }
    
    public static void initialize() throws SQLException {
        if( instance == null ) {
            instance = new AppLayerDatabase();
            
            messageTable = new MessageDatabaseTable();
            sentMessageTable = new SentMessageDatabaseTable();
            
            newUploadFilesTable = new NewUploadFilesDatabaseTable();
            fileListDatabaseTable = new FileListDatabaseTable();
            uploadFilesDatabaseTable = new UploadFilesDatabaseTable();
            downloadFilesDatabaseTable = new DownloadFilesDatabaseTable();
            
            identitiesDatabaseTable = new IdentitiesDatabaseTable();
            
            knownBoardsDatabaseTable = new KnownBoardsDatabaseTable();
            
            ArrayList lst = new ArrayList();
            lst.add(messageTable);
            lst.add(sentMessageTable);
            lst.add(newUploadFilesTable);
            lst.add(fileListDatabaseTable);
            lst.add(uploadFilesDatabaseTable);
            lst.add(downloadFilesDatabaseTable);
            lst.add(identitiesDatabaseTable);
            lst.add(knownBoardsDatabaseTable);
            instance.ensureTables(lst);
        } else {
            System.out.println("ERROR: AppLayerDatabase already initialized!");
        }
    }

    public static AppLayerDatabase getInstance() {
        return instance;
    }
    
    public static MessageDatabaseTable getMessageTable() {
        return messageTable;
    }
    public static SentMessageDatabaseTable getSentMessageTable() {
        return sentMessageTable;
    }
    
    public static NewUploadFilesDatabaseTable getNewUploadFilesTable() {
        return newUploadFilesTable;
    }
    public static FileListDatabaseTable getFileListDatabaseTable() {
        return fileListDatabaseTable;
    }
    public static UploadFilesDatabaseTable getUploadFilesDatabaseTable() {
        return uploadFilesDatabaseTable;
    }
    public static DownloadFilesDatabaseTable getDownloadFilesDatabaseTable() {
        return downloadFilesDatabaseTable;
    }
    
    public static IdentitiesDatabaseTable getIdentitiesDatabaseTable() {
        return identitiesDatabaseTable;
    }
    
    public static KnownBoardsDatabaseTable getKnownBoardsDatabaseTable() {
        return knownBoardsDatabaseTable;
    }

    private void ensureTables(List lst) {
        for(Iterator i=lst.iterator(); i.hasNext(); ) {
            AbstractDatabaseTable t = (AbstractDatabaseTable)i.next();
            for(Iterator j=t.getTableDDL().iterator(); j.hasNext(); ) {
                String tableDDL = (String)j.next();
                try {
                    update(tableDDL);
                } catch(SQLException ex) {
                    // table/index already exists?
                    if( ! (ex.getSQLState().equals("S0001") || ex.getSQLState().equals("S0011")) ) {
                        ex.printStackTrace(); // another exception, show
                    }
                }
            }
        }
    }
}
