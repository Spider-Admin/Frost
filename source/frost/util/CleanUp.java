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

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.messages.*;
import frost.storage.database.applayer.*;

/**
 * Expire messages and cleans several database tables.
 * Is only called during startup of frost, never during runtime.
 */
public class CleanUp {

    private static Logger logger = Logger.getLogger(CleanUp.class.getName());

    public static final int DELETE_MESSAGES  = 1;
    public static final int ARCHIVE_MESSAGES = 2;
    public static final int KEEP_MESSAGES    = 3;
    
    // we hold indices, chk keys, ... for at least the following count of days:
    private final static int MINIMUM_DAYS_OLD = 28;

    /**
     * Expire messages during startup of Frost.
     * Clean indexslot tables and file tables.
     * Gets the mode to use from settings.
     */
    public static void runExpirationTasks(List boardList) {
        int mode;
        String strMode = Core.frostSettings.getValue("messageExpirationMode");
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
        
        // ALWAYS cleanup following
        cleanupIndexSlotTables();
        cleanupSharedChkKeyTable();
    }

    private static void processExpiredMessages(List boardList, int mode) {

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
        int defaultDaysOld = Core.frostSettings.getIntValue("messageExpireDays") + 1;

        if( defaultDaysOld < Core.frostSettings.getIntValue("maxMessageDisplay") ) {
            defaultDaysOld = Core.frostSettings.getIntValue("maxMessageDisplay") + 1;
        }
        if( defaultDaysOld < Core.frostSettings.getIntValue("maxMessageDownload") ) {
            defaultDaysOld = Core.frostSettings.getIntValue("maxMessageDownload") + 1;
        }

        for(Iterator i=boardList.iterator(); i.hasNext(); ) {

            int currentDaysOld = defaultDaysOld;
            Board board = (Board)i.next();
            if( board.isConfigured() && board.getMaxMessageDisplay() > currentDaysOld ) {
                currentDaysOld = board.getMaxMessageDisplay();
            }
            
            if( mode == ARCHIVE_MESSAGES ) {
                MessageTableCallback mtCallback = new MessageTableCallback();
                try {
                    AppLayerDatabase.getMessageTable().retrieveMessagesForArchive(board, currentDaysOld, mtCallback);
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception during retrieveMessagesForArchive", t);
                    continue;
                }
                if( mtCallback.errorOccurred() ) {
                    continue; // don't delete messages
                }
            }
            
            // if insert to archive database table was ok OR if mode is DELETE then
            //   delete all msgs for this board from msg table
            int deletedCount = 0;
            try {
                deletedCount = AppLayerDatabase.getMessageTable().deleteExpiredMessages(board, currentDaysOld);
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception during deleteExpiredMessages", t);
                continue;
            }
            logger.info("Processed "+deletedCount+" expired messages for board "+board.getName());
        }
        logger.info("Finished to process expired messages.");
    }
    
    /**
     * Callback that gets each expired message and tries to insert it into MessageArchive.
     */
    private static class MessageTableCallback implements MessageDatabaseTableCallback {
        boolean errorOccurred = false;
        public boolean messageRetrieved(FrostMessageObject mo) {
            if( errorOccurred ) {
                return errorOccurred; // stop
            }
            
            try {
                if( AppLayerDatabase.getMessageArchiveTable().insertMessage(mo) == false ) {
                    errorOccurred = true;
                }
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception during cleanup of GlobalIndexSlots", t);
            }
            
            return errorOccurred; // maybe stop
        }
        public boolean errorOccurred() {
            return errorOccurred;
        }
    }
    
    /**
     * Cleanup old indexslot table entries.
     * Keep indices files for maxMessageDownload*2 days, but at least MINIMUM_DAYS_OLD days.
     */
    private static void cleanupIndexSlotTables() {
        
        int maxDaysOld = Core.frostSettings.getIntValue("maxMessageDownload") * 2;
        if( maxDaysOld < MINIMUM_DAYS_OLD ) {
            maxDaysOld = MINIMUM_DAYS_OLD;
        }
        
        int deletedCount = 0;

        try {
            GlobalIndexSlotsDatabaseTable gixSlots = new GlobalIndexSlotsDatabaseTable();
            deletedCount += gixSlots.cleanupTable(maxDaysOld);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of GlobalIndexSlots", t);
        }

        try {
            IndexSlotsDatabaseTable ixSlots = new IndexSlotsDatabaseTable();
            deletedCount += ixSlots.cleanupTable(maxDaysOld);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of IndexSlots", t);
        }

        logger.info("Finished to delete expired index slots, deleted "+deletedCount+" rows.");
    }

    /**
     * Cleanup old CHK keys from pointer files.
     * All keys we did'nt see for MINIMUM_DAYS_OLD days will be deleted.
     */
    private static void cleanupSharedChkKeyTable() {
        
        int deletedCount = 0;

        try {
            deletedCount += AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().cleanupTable(deletedCount);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception during cleanup of SharedFilesCHKKeys", t);
        }

        logger.info("Finished to delete expired SharedFilesCHKKeys, deleted "+deletedCount+" rows.");
    }
}
