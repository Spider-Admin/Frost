/*
 CleanUp.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

package frost.util;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.fcp.*;
import frost.fcp.fcp05.*;
import frost.fileTransfer.upload.*;
import frost.gui.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.messagearchive.*;
import frost.storage.perst.messages.*;

/**
 * Expire messages and cleans several database tables.
 * Is only called during startup of frost, never during runtime.
 */
public class CleanUp {

    private static final Logger logger = Logger.getLogger(CleanUp.class.getName());

    public static final int DELETE_MESSAGES  = 1;
    public static final int ARCHIVE_MESSAGES = 2;
    public static final int KEEP_MESSAGES    = 3;

    // we hold indices, chk keys, ... for at least the following count of days:
    private final static int MINIMUM_DAYS_OLD = 28;

    private static Splashscreen splashScreen;

    /**
     * Expire messages during startup of Frost.
     * Clean indexslot tables and file tables.
     * Gets the mode to use from settings.
     */
    public static void runExpirationTasks(final Splashscreen sp, final List<Board> boardList) {

        splashScreen = sp;

        // each time run cleanup for perst storages
        cleanPerstStorages(boardList);

        // on 0.5, delete old upload file chunks
        if( Core.frostSettings.getBoolValue(SettingsClass.UPLOAD_REMOVE_CHUNKS)
                && FcpHandler.isFreenet05() )
        {
            splashScreen.setText("Removing old upload chunks");
            cleanup05UploadChunks();
        }

        // cleanup/archive McKoi tables all X days
        final int cleanupDatabaseInterval = Core.frostSettings.getIntValue(SettingsClass.DB_CLEANUP_INTERVAL);
        final long lastCleanupTime = Core.frostSettings.getLongValue(SettingsClass.DB_CLEANUP_LASTRUN);
        final long now = System.currentTimeMillis();
        final long intervalMillis = (cleanupDatabaseInterval) * 24L * 60L * 60L * 1000L; // interval days into millis

        // when last cleanup was before the chosen interval days then run cleanup and archiving
        if( lastCleanupTime < (now - intervalMillis) ) {
            cleanStorages(boardList);
            Core.frostSettings.setValue(SettingsClass.DB_CLEANUP_LASTRUN, now);
        }
    }

    private static void cleanPerstStorages(final List<Board> boardList) {
        splashScreen.setText("Cleaning index tables");
        cleanupIndexSlotsStorage(boardList);

        splashScreen.setText("Cleaning CHK filelist tables");
        cleanupSharedCHKKeyStorage();
    }

    private static void cleanStorages(final List<Board> boardList) {
        int mode;

        final String strMode = Core.frostSettings.getValue(SettingsClass.MESSAGE_EXPIRATION_MODE);
        if( strMode.toUpperCase().equals("KEEP") ) {
            mode = KEEP_MESSAGES;
        } else if( strMode.toUpperCase().equals("ARCHIVE") ) {
            mode = ARCHIVE_MESSAGES;
        } else if( strMode.toUpperCase().equals("DELETE") ) {
            mode = DELETE_MESSAGES;
        } else {
            mode = KEEP_MESSAGES;
        }

        processExpiredMessages(boardList, mode);

        splashScreen.setText("Cleaning filelist tables");
        cleanupFileListFileOwners();
        cleanupFileListFiles();
    }

    private static void processExpiredMessages(final List<Board> boardList, final int mode) {

        if( mode == ARCHIVE_MESSAGES ) {
            logger.info("Expiration mode is ARCHIVE_MESSAGES.");
        } else if( mode == DELETE_MESSAGES ) {
            logger.info("Expiration mode is DELETE_MESSAGES.");
        } else if( mode == KEEP_MESSAGES ) {
            logger.info("Expiration mode is KEEP_MESSAGES.");
        } else {
            logger.severe("ERROR: invalid MODE specified: "+mode);
            return;
        }

        if( mode == KEEP_MESSAGES ) {
            // nothing to do here
            return;
        }

        // take maximum
        int defaultDaysOld = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_EXPIRE_DAYS) + 1;

        if( defaultDaysOld < Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DISPLAY) ) {
            defaultDaysOld = Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DISPLAY) + 1;
        }
        if( defaultDaysOld < Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DOWNLOAD) ) {
            defaultDaysOld = Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DOWNLOAD) + 1;
        }

        for( final Board board : boardList ) {

            int currentDaysOld = defaultDaysOld;
            if( board.isConfigured() ) {
                currentDaysOld = Math.max(board.getMaxMessageDisplay(), currentDaysOld);
                currentDaysOld = Math.max(board.getMaxMessageDownload(), currentDaysOld);
            }

            final boolean archiveKeepUnread = Core.frostSettings.getBoolValue(SettingsClass.ARCHIVE_KEEP_UNREAD);
            final boolean archiveKeepFlaggedAndStarred = Core.frostSettings.getBoolValue(SettingsClass.ARCHIVE_KEEP_FLAGGED_AND_STARRED);

            splashScreen.setText("Archiving messages in board: "+board.getName());

            final MessageTableCallback mtCallback = new MessageTableCallback(mode);
            try {
                MessageStorage.inst().retrieveMessagesForArchive(
                        board,
                        currentDaysOld,
                        archiveKeepUnread,
                        archiveKeepFlaggedAndStarred,
                        mtCallback);
            } catch(final Throwable t) {
                logger.log(Level.SEVERE, "Exception during retrieveMessagesForArchive", t);
                continue;
            }
            if( mtCallback.getCount() > 0 ) {
                logger.warning("INFO: Processed "+mtCallback.getCount()+" expired messages for board "+board.getName());
            }

            MessageStorage.inst().commit();
            ArchiveMessageStorage.inst().commit();
        }
        MessageStorage.inst().commit();
        ArchiveMessageStorage.inst().commit();

        logger.info("Finished to process expired messages.");
    }

    /**
     * Callback that gets each expired message and tries to insert it into MessageArchive.
     */
    private static class MessageTableCallback implements MessageArchivingCallback {
        int mode;
        int count = 0;
        public MessageTableCallback(final int m) { mode = m; }
        public int messageRetrieved(final FrostMessageObject mo) {
            // mode is either ARCHIVE or DELETE
            if( count%100 == 0 ) {
                MessageStorage.inst().commit();
                ArchiveMessageStorage.inst().commit();
            }
            if( mode == ARCHIVE_MESSAGES ) {
                // maybe insert into archive
                final int rc = ArchiveMessageStorage.inst().insertMessage(mo);
                if( rc == ArchiveMessageStorage.INSERT_ERROR ) {
                    return MessageArchivingCallback.KEEP_MESSAGE;
                }
            }
            // for ARCHIVE or DELETE delete the message from keypool
            count++;
            return MessageArchivingCallback.DELETE_MESSAGE;
        }
        public int getCount() { return count; }
    }

    /**
     * Cleanup old indexslot table entries.
     * Keep indices files for maxMessageDownload*2 days, but at least MINIMUM_DAYS_OLD days.
     */
    private static void cleanupIndexSlotsStorage(final List<Board> boardList) {

        int maxDaysOld = Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DOWNLOAD) * 2;

        // max from any board
        for( final Board board : boardList ) {
            if( board.isConfigured() ) {
                maxDaysOld = Math.max(board.getMaxMessageDownload(), maxDaysOld);
            }
        }
        if( maxDaysOld < MINIMUM_DAYS_OLD ) {
            maxDaysOld = MINIMUM_DAYS_OLD;
        }

        int deletedCount = 0;

        try {
            deletedCount += IndexSlotsStorage.inst().cleanup(maxDaysOld);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of IndexSlots", t);
        }
        if( deletedCount > 0 ) {
            logger.warning("INFO: Finished to delete expired index slots, deleted "+deletedCount+" rows.");
        }
    }

    /**
     * Cleanup old CHK keys from pointer files.
     * All keys we did'nt see for MINIMUM_DAYS_OLD days will be deleted.
     */
    private static void cleanupSharedCHKKeyStorage() {
        int deletedCount = 0;
        try {
            deletedCount = SharedFilesCHKKeyStorage.inst().cleanupTable(MINIMUM_DAYS_OLD);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of SharedFilesCHKKeys", t);
        }
        if( deletedCount > 0 ) {
            logger.warning("INFO: Finished to delete expired SharedFilesCHKKeys, deleted "+deletedCount+" rows.");
        }
    }

    /**
     * Remove owners that were not seen for more than MINIMUM_DAYS_OLD days and have no CHK key set.
     */
    private static void cleanupFileListFileOwners() {
        int deletedCount = 0;
        try {
            deletedCount = FileListStorage.inst().cleanupFileListFileOwners(MINIMUM_DAYS_OLD);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of FileListFileOwners", t);
        }
        if( deletedCount > 0 ) {
            logger.warning("INFO: Finished to delete expired FileListFileOwners, deleted "+deletedCount+" rows.");
        }
    }

    /**
     * Remove files that have no owner and no CHK key.
     */
    private static void cleanupFileListFiles() {
        int deletedCount = 0;
        try {
            deletedCount = FileListStorage.inst().cleanupFileListFiles();
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of FileListFiles", t);
        }
        if( deletedCount > 0 ) {
            logger.warning("INFO: Finished to delete expired FileListFiles, deleted "+deletedCount+" rows.");
        }
    }

    /**
     * Scans all 0.5 upload file chunks in LOCALDATA folder (.redirect, .checkblocks).
     * Scans files currently in upload table.
     * Deletes all chunks of files which are not in upload table.
     */
    private static void cleanup05UploadChunks() {

        // upload items are already loaded into model
        final List<FrostUploadItem> ulItems = Core.getInstance().getFileTransferManager().getUploadManager().getModel().getItems();

        // prepare a Set of all known valid absolute filenames
        final HashSet<String> ulItemFilePaths = new HashSet<String>();
        for( final FrostUploadItem ulItem : ulItems ) {
            final File uploadFile = ulItem.getFile();
            final String ulFilename = FecSplitfile.convertUploadFilename(uploadFile);
            final File checkBlocksFile = new File( ulFilename + FecSplitfile.FILE_CHECKBLOCKS_EXTENSION );
            final File redirectFile = new File( ulFilename + FecSplitfile.FILE_REDIRECT_EXTENSION );

            ulItemFilePaths.add(checkBlocksFile.getAbsolutePath());
            ulItemFilePaths.add(redirectFile.getAbsolutePath());
        }

        // get a List of all files in LOCALDATA directory that have extension .redirect or .checkblocks
        final File localdataDir = new File(Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA));

        final FilenameFilter ffilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if( name.endsWith(FecSplitfile.FILE_CHECKBLOCKS_EXTENSION)
                        || name.endsWith(FecSplitfile.FILE_REDIRECT_EXTENSION) )
                {
                    return true;
                }
                return false;
            }
        };

        int deletedCount = 0;
        long deletedSize = 0L;
        final File[] allUploadChunks = localdataDir.listFiles(ffilter);
        for( final File aFile : allUploadChunks ) {
            if( aFile.isFile() && !ulItemFilePaths.contains(aFile.getAbsolutePath()) ) {
                // file is not known, delete
                deletedSize += aFile.length();
                aFile.delete();
                deletedCount++;
            }
        }
        logger.warning("INFO: Finished to delete old upload file chunks, deleted "+deletedCount+" files, overall size "+deletedSize);
    }
}
