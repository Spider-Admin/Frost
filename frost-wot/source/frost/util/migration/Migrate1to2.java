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

import java.io.*;
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
import frost.util.*;
import frost.util.migration.migrate1to2.*;

public class Migrate1to2 {

    private static final Logger logger = Logger.getLogger(Migrate1to2.class.getName());

    private boolean openDatabase() {
        try {
            AppLayerDatabase.initialize();
            return true;
        } catch(final SQLException ex) {
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
        } catch(final SQLException ex) {
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
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve board primary keys", e);
            return false;
        }

        // create perst boards
        MessageStorage.inst().importBoards(boardPrimaryKeysByName);
        return true;
    }

    private boolean migrateIdentities() {
        System.out.println("Converting identities...");
        final Hashtable<String,Integer> messageCounts;
        try {
            messageCounts = IdentitiesDatabaseTable.retrieveMsgCountPerIdentity();
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve identities message count", e);
            return false;
        }
        try {
            final List<LocalIdentity> li = IdentitiesDatabaseTable.getLocalIdentities(AppLayerDatabase.getInstance());
            IdentitiesStorage.inst().importLocalIdentities(li, messageCounts);
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve local identities", e);
            return false;
        }
        try {
            final List<Identity> li = IdentitiesDatabaseTable.getIdentities(AppLayerDatabase.getInstance());
            IdentitiesStorage.inst().importIdentities(li, messageCounts);
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve identities", e);
            return false;
        }
        return true;
    }

    private boolean migrateKnownBoards() {
        System.out.println("Converting known boards...");
        try {
            final HashSet<String> hiddenNames = KnownBoardsDatabaseTable.loadHiddenNames(AppLayerDatabase.getInstance());
            FrostFilesStorage.inst().saveHiddenBoardNames(hiddenNames);
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve hidden board names", e);
            return false;
        }

        try {
            final List<KnownBoard> knownBoards = KnownBoardsDatabaseTable.getKnownBoards(AppLayerDatabase.getInstance());
            FrostFilesStorage.inst().addNewKnownBoards(knownBoards);
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve known boards", e);
            return false;
        }

        return true;
    }

    /**
     * Must only be run after TofTreeModel was initialized!
     */
    public boolean runStep2() {
        final List<Board> allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();

        if( !migrateArchive() ) {
            closeDatabase();
            return false;
        } else {
            // free page pool
            ArchiveMessageStorage.inst().silentClose();
            ArchiveMessageStorage.inst().initStorage();
        }
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
        } else {
            MessageStorage.inst().silentClose();
            MessageStorage.inst().initStorage();
            // re-assign perst boards with boards
            MainFrame.getInstance().getTofTreeModel().initialAssignPerstFrostBoardObjects();
        }

        if( !migrateFileList() ) {
            closeDatabase();
            return false;
        } else {
            // free page pool
            FileListStorage.inst().silentClose();
            FileListStorage.inst().initStorage();
        }

        dropAllTables();

        closeDatabase();

        deleteMcKoiFiles();

        return true;
    }

    private boolean migrateSentMessages(final List<Board> allBoards) {
        try {
            System.out.println("Converting sent messages...");
            final MessageStorage ms = MessageStorage.inst();

            final MessageCallback mc = new MessageCallback() {
                int cnt=0;
                public boolean messageRetrieved(FrostMessageObject mo) {
                    ms.insertSentMessageDirect(mo, false);
                    cnt++;
                    if(cnt%100 == 0) {
                        ms.commit();
                        System.out.println("Committed after "+cnt+" sent messages");
                    }
                    return false;
                }
            };

            new SentMessageDatabaseTable().retrieveAllMessages(AppLayerDatabase.getInstance(), mc, allBoards, false);
            ms.commit();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }

    private boolean migrateUnsentMessages(final List<Board> allBoards) {
        try {
            System.out.println("Converting unsent messages...");
            final MessageStorage ms = MessageStorage.inst();

            final List<FrostUnsentMessageObject> unsentMsgs = UnsentMessageDatabaseTable.retrieveMessages(AppLayerDatabase.getInstance(), allBoards);
            int cnt=0;
            for( final FrostUnsentMessageObject umo : unsentMsgs ) {
                ms.insertUnsentMessageDirect(umo);
                cnt++;
                if(cnt%100 == 0) {
                    ms.commit();
                    System.out.println("Committed after "+cnt+" unsent messages");
                }
            }

            ms.commit();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }

    private boolean migrateKeypool(final List<Board> allBoards) {
        try {
            System.out.println("Converting keypool messages...");
            final MessageStorage ms = MessageStorage.inst();

            final MessageCallback mc = new MessageCallback() {
                int cnt=0;
                public boolean messageRetrieved(FrostMessageObject mo) {
                    if( FrostMessageObject.isSignatureStatusVERIFIED(mo.getSignatureStatus()) ) {
                        mo.setFromName(Mixed.makeFilename(mo.getFromName()));
                    } else {
                        // message is not verified, fromName must not contain an '@'
                        // (fix for bug in older frosts)
                        if( mo.getFromName().indexOf('@')> -1 ) {
                            mo.setFromName(mo.getFromName().replace('@','_'));
                        }
                    }
                    ms.insertMessage(mo, false);
                    cnt++;
                    if(cnt%100 == 0) {
                        ms.commit();
                        // close and reopen storage -> solved heap space problem when migrating archive from McKoi
                        ms.silentClose();
                        ms.initStorage();
                        // re-assign perst boards with boards
                        MainFrame.getInstance().getTofTreeModel().initialAssignPerstFrostBoardObjects();
                        System.out.println("Committed after "+cnt+" keypool messages");
                    }
                    return false;
                }
            };

            // by default we don't migrate invalid messages
            final boolean migrateInvalidMessages = Core.frostSettings.getBoolValue(SettingsClass.STORAGE_STORE_INVALID_MESSAGES);

            new MessageDatabaseTable().retrieveAllMessages(AppLayerDatabase.getInstance(), mc, allBoards, migrateInvalidMessages);
            ms.commit();
            if( migrateInvalidMessages ) {
                System.out.println("INFO: Valid messages = "+ms.getMessageCount()+"; Invalid messages = "+ms.getInvalidMessageCount());
            } else {
                System.out.println("INFO: Valid messages = "+ms.getMessageCount());
            }
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }

    private boolean migrateFileList() {
        try {
            System.out.println("Converting file list...");
            final FileListStorage ms = FileListStorage.inst();

            final FileListCallback mc = new FileListCallback() {
                int cnt=0;
                public boolean fileRetrieved(FrostFileListFileObject fo) {
                    Iterator<FrostFileListFileObjectOwner> i = fo.getFrostFileListFileObjectOwnerIterator();
                    for( ; i.hasNext(); ) {
                        FrostFileListFileObjectOwner ow = i.next();
                        ow.setOwner( Mixed.makeFilename(ow.getOwner()) );
                    }
                    ms.insertOrUpdateFileListFileObject(fo);
                    cnt++;
                    if(cnt%100 == 0) {
                        ms.commit();
                        System.out.println("Committed after "+cnt+" file list files");
                    }
                    return false;
                }
            };
            new FileListDatabaseTable().retrieveFiles(AppLayerDatabase.getInstance(), mc, null, null, null, null);
            ms.commit();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }

    private boolean migrateArchive() {
        try {
            System.out.println("Converting archive messages...");
            final ArchiveMessageStorage ms = ArchiveMessageStorage.inst();

            final MessageCallback mc = new MessageCallback() {
                int cnt=0;
                public boolean messageRetrieved(FrostMessageObject mo) {
                    if( FrostMessageObject.isSignatureStatusVERIFIED(mo.getSignatureStatus()) ) {
                        mo.setFromName(Mixed.makeFilename(mo.getFromName()));
                    } else {
                        // message is not verified, fromName must not contain an '@'
                        // (fix for bug in older frosts)
                        if( mo.getFromName().indexOf('@')> -1 ) {
                            mo.setFromName(mo.getFromName().replace('@','_'));
                        }
                    }
                    ms.insertMessage(mo, (String)mo.getUserObject());
                    cnt++;
                    if(cnt%100 == 0) {
                        ms.commit();
                        // close and reopen storage -> solved heap space problem when migrating archive from McKoi
                        ms.silentClose();
                        ms.initStorage();
                        System.out.println("Committed after "+cnt+" archive messages");
                    }
                    return false;
                }
            };

            MessageArchiveDatabaseTable.retrieveAllMessages(AppLayerDatabase.getInstance(), mc);
            ms.commit();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Migration error!", t);
            return false;
        }
    }


    private boolean dropTable(final String tableName) {
        try {
            final Statement stmt = AppLayerDatabase.getInstance().createStatement();
            final String sql = "DROP TABLE "+tableName;
            stmt.executeUpdate(sql);
            AppLayerDatabase.getInstance().commit();
        } catch(final SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return true;
    }

    private void dropAllTables() {
        System.out.println("Dropping database tables...");
        dropTable("UNSENDBOARDATTACHMENTS");
        dropTable("UNSENDFILEATTACHMENTS");
        dropTable("UNSENDMESSAGES");

        dropTable("SENTMESSAGECONTENTS");
        dropTable("SENTBOARDATTACHMENTS");
        dropTable("SENTFILEATTACHMENTS");
        dropTable("SENTMESSAGES");

        dropTable("MESSAGECONTENTS");
        dropTable("BOARDATTACHMENTS");
        dropTable("FILEATTACHMENTS");
        dropTable("MESSAGES");

        dropTable("MESSAGEARCHIVEFILEATTACHMENTS");
        dropTable("MESSAGEARCHIVEBOARDATTACHMENTS");
        dropTable("MESSAGEARCHIVECONTENTS");
        dropTable("MESSAGEARCHIVE");

        dropTable("HIDDENBOARDNAMES");
        dropTable("KNOWNBOARDS");

        dropTable("IDENTITIES");
        dropTable("OWNIDENTITIES");
        dropTable("OWNIDENTITIESLASTFILESSHARED");

        dropTable("FILEOWNERLIST");
        dropTable("FILELIST");

        dropTable("BOARDS");

        System.out.println("Finished dropping database tables...");
    }

    private void deleteMcKoiFiles() {
        // delete:
        //  store/applayerdb.conf
        //  store/log/*
        //  store/data/*
        try {
            System.out.println("Deleting old McKoi files...");
            FileAccess.deleteDir(new File("store/log"));
            FileAccess.deleteDir(new File("store/data"));
            new File("store/applayerdb.conf").delete();
            new File("repair_db.bat").delete();
            new File("repair_db.sh").delete();
            new File("exec.bat").delete();
        } catch(final Throwable t) {
            t.printStackTrace();
        }
    }
}
