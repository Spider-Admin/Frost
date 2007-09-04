/*
  Migrate1to2.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.migration;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.*;
import frost.gui.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.identities.*;
import frost.storage.perst.messagearchive.*;
import frost.storage.perst.messages.*;
import frost.util.migration.migrate1to2.*;

public class Migrate1to2 {

    private static final Logger logger = Logger.getLogger(Migrate1to2.class.getName());
    
    private boolean openDatabase() {
        // open database
        try {
            AppLayerDatabase.initialize();
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error opening the databases", ex);
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean closeDatabase() {
        try {
            AppLayerDatabase.getInstance().close();
            AppLayerDatabase.destroy();
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error closing the databases", ex);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Must be started before the boards were loaded!
     */
    public boolean runStep1() {
        
        if( !openDatabase() ) {
            return false;
        }
        
        if( !migrateBoards() ) {
            closeDatabase();
            return false;
        }
        if( !migrateIdentities() ) {
            closeDatabase();
            return false;
        }
        if( !migrateKnownBoards() ) {
            closeDatabase();
            return false;
        }
        return true;
    }

    /**
     * convert boards table into perst, TofTreeModel needs this during initialization 
     */
    private boolean migrateBoards() {
        Hashtable<String,Integer> boardPrimaryKeysByName;
        // load all board primary keys and names
        System.out.println("Converting boards...");
        try {
            boardPrimaryKeysByName = BoardDatabaseTable.loadBoards(AppLayerDatabase.getInstance());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve board primary keys", e);
            return false;
        }

        // create perst boards
        MessageStorage.inst().importBoards(boardPrimaryKeysByName);

// FIXME        return dropTable("NEWUPLOADFILES");

        return true;
    }

    private boolean migrateIdentities() {
        System.out.println("Converting identities...");
        try {
            List<LocalIdentity> li = IdentitiesDatabaseTable.getLocalIdentities(AppLayerDatabase.getInstance());
            IdentitiesStorage.inst().importLocalIdentities(li);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve local identities", e);
            return false;
        }
        try {
            List<Identity> li = IdentitiesDatabaseTable.getIdentities(AppLayerDatabase.getInstance());
            IdentitiesStorage.inst().importIdentities(li);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve identities", e);
            return false;
        }
        return true;
    }

    private boolean migrateKnownBoards() {
        System.out.println("Converting known boards...");
        try {
            HashSet<String> hiddenNames = KnownBoardsDatabaseTable.loadHiddenNames(AppLayerDatabase.getInstance());
            FrostFilesStorage.inst().saveHiddenBoardNames(hiddenNames);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve hidden board names", e);
            return false;
        }

        try {
            List<KnownBoard> knownBoards = KnownBoardsDatabaseTable.getKnownBoards(AppLayerDatabase.getInstance());
            FrostFilesStorage.inst().addNewKnownBoards(knownBoards);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve known boards", e);
            return false;
        }

        return true;
    }

    /**
     * Must only be run after TofTreeModel was initialized!
     */
    public boolean runStep2() {
        List<Board> allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        if( !migrateSentMessages(allBoards) ) {
            closeDatabase();
            return false;
        }
        if( !migrateUnsentMessages(allBoards) ) {
            closeDatabase();
            return false;
        }
        if( !migrateKeypool(allBoards) ) {
            closeDatabase();
            return false;
        }
        if( !migrateFileList() ) {
            closeDatabase();
            return false;
        }
        if( !migrateArchive() ) {
            closeDatabase();
            return false;
        }
        closeDatabase();
        return true;
    }

    private boolean migrateSentMessages(List<Board> allBoards) {
        try {
            System.out.println("Converting sent messages...");
            final MessageStorage ms = MessageStorage.inst();

            MessageCallback mc = new MessageCallback() {
                int cnt=0;
                public boolean messageRetrieved(FrostMessageObject mo) {
                    ms.addSentMessage(mo, false);
                    cnt++;
                    if(cnt%100 == 0) {
                        MessageStorage.inst().commitStore();
                        System.out.println("Committed after "+cnt+" sent messages");
                    }
                    return false;
                }
            };
            
            new SentMessageDatabaseTable().retrieveAllMessages(AppLayerDatabase.getInstance(), mc, allBoards);
            MessageStorage.inst().commitStore();
            return true;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }
    
    private boolean migrateUnsentMessages(List<Board> allBoards) {
        try {
            System.out.println("Converting unsent messages...");
            final MessageStorage ms = MessageStorage.inst();

            List<FrostUnsentMessageObject> unsentMsgs = UnsentMessageDatabaseTable.retrieveMessages(AppLayerDatabase.getInstance(), allBoards);
            int cnt=0;
            for( FrostUnsentMessageObject umo : unsentMsgs ) {
                ms.addUnsentMessage(umo, false);
                cnt++;
                if(cnt%100 == 0) {
                    MessageStorage.inst().commitStore();
                    System.out.println("Committed after "+cnt+" unsent messages");
                }
            }
            
            MessageStorage.inst().commitStore();
            return true;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }
    
    private boolean migrateKeypool(List<Board> allBoards) {
        try {
            System.out.println("Converting keypool messages...");
            final MessageStorage ms = MessageStorage.inst();
            
            MessageCallback mc = new MessageCallback() {
                int cnt=0;
                public boolean messageRetrieved(FrostMessageObject mo) {
                    ms.insertMessage(mo, false);
                    cnt++;
                    if(cnt%100 == 0) {
                        MessageStorage.inst().commitStore();
                        System.out.println("Committed after "+cnt+" keypool messages");
                    }
                    return false;
                }
            };
            
            new MessageDatabaseTable().retrieveAllMessages(AppLayerDatabase.getInstance(), mc, allBoards);
            MessageStorage.inst().commitStore();
            return true;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }
    
    private boolean migrateFileList() {
        try {
            System.out.println("Converting file list...");
            final FileListStorage ms = FileListStorage.inst();
            
            FileListCallback mc = new FileListCallback() {
                int cnt=0;
                public boolean fileRetrieved(FrostFileListFileObject fo) {
                    ms.insertOrUpdateFileListFileObject(fo, false);
                    cnt++;
                    if(cnt%100 == 0) {
                        ms.commitStore();
                        System.out.println("Committed after "+cnt+" file list files");
                    }
                    return false;
                }
            };
            new FileListDatabaseTable().retrieveFiles(AppLayerDatabase.getInstance(), mc, null, null, null, null);
            ms.commitStore();
            return true;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }
    
    private boolean migrateArchive() {
        try {
            System.out.println("Converting archive messages...");
            final ArchiveMessageStorage ms = ArchiveMessageStorage.inst();
            
            MessageCallback mc = new MessageCallback() {
                int cnt=0;
                public boolean messageRetrieved(FrostMessageObject mo) {
                    String bname = (String)mo.getUserObject();
                    ms.insertMessage(mo, bname, false);
                    cnt++;
                    if(cnt%100 == 0) {
                        ms.commitStore();
                        System.out.println("Committed after "+cnt+" archive messages");
                    }
                    return false;
                }
            };
            
            MessageArchiveDatabaseTable.retrieveAllMessages(AppLayerDatabase.getInstance(), mc);
            ms.commitStore();
            return true;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }
    
    
    private boolean dropTable(String tableName) {
        try {
            Statement stmt = AppLayerDatabase.getInstance().createStatement();
            String sql = "DROP TABLE "+tableName;
            stmt.executeUpdate(sql);
            AppLayerDatabase.getInstance().commit();
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return true;
    }

}
