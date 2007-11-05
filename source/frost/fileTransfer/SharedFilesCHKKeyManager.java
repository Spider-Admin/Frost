/*
 SharedFilesCHKKeyManager.java / Frost
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
package frost.fileTransfer;

import java.util.*;
import java.util.logging.*;

import frost.storage.perst.*;
import frost.threads.*;
import frost.util.Logging;

public class SharedFilesCHKKeyManager {

    private static final Logger logger = Logger.getLogger(SharedFilesCHKKeyManager.class.getName());

    // TODO: download bis zu _1 mal hintereinander, wenn fail dann noch bis _2 mal taeglich. dann ende.
    private static final int MAX_DOWNLOAD_RETRIES_1 = 7;
//    private static final int MAX_DOWNLOAD_RETRIES_2 = 7 + 3;

    private static final int MAX_KEYS_TO_SEND = 300;

    /**
     * @return List with SharedFileCHKKey object that should be send inside a KSK pointer file
     */
    public static List<SharedFilesCHKKey> getCHKKeysToSend() {
        // get a number of CHK keys from database that must be send
        // include only 1 of our new CHK keys into this list, don't send CHK keys of different identities
        // together, this compromises anonymity!
        try {
            // rules what chks are choosed are in the following method
            return SharedFilesCHKKeyStorage.inst().getSharedFilesCHKKeysToSend(MAX_KEYS_TO_SEND);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in SharedFilesCHKKeysDatabaseTable().getSharedFilesCHKKeysToSend", t);
        }
        return null;
    }

    /**
     * @param chkKeys a List of SharedFileCHKKey objects that were successfully sent within a KSK pointer file
     */
    public static void updateCHKKeysWereSuccessfullySent(final List<SharedFilesCHKKey> chkKeys) {

        final long now = System.currentTimeMillis();

        if( !SharedFilesCHKKeyStorage.inst().beginExclusiveThreadTransaction() ) {
            return;
        }

        try {
            for( final SharedFilesCHKKey key : chkKeys ) {
                key.incrementSentCount();
                key.setLastSent(now);

                key.modify();
            }
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during database update", t);
        }
        SharedFilesCHKKeyStorage.inst().endThreadTransaction();
    }

    /**
     * Process the List of newly received chk keys.
     * Update existing keys or insert new keys.
     */
    public static void processReceivedCHKKeys(final FilePointerFileContent content) {

        if( content == null || content.getChkKeyStrings() == null || content.getChkKeyStrings().size() == 0 ) {
            return;
        }

        if( !SharedFilesCHKKeyStorage.inst().beginExclusiveThreadTransaction() ) {
            return;
        }

        try {
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("processReceivedCHKKeys: processing "+content.getChkKeyStrings().size()+" keys");
            }
            int newKeys = 0;
            int seenKeys = 0;
            int newOwnKeys = 0;

            for( final String chkStr : content.getChkKeyStrings() ) {
                try {
                    SharedFilesCHKKey ck = SharedFilesCHKKeyStorage.inst().retrieveSharedFilesCHKKey(chkStr);
                    if( ck == null ) {
                        // new key
//                        System.out.println("processReceivedCHKKeys: enqueueing new key");
                        newKeys++;
                        // add to database
                        ck = new SharedFilesCHKKey(chkStr, content.getTimestamp());
                        SharedFilesCHKKeyStorage.inst().storeItem(ck);

                        // new key, directly enqueue for download
                        FileListDownloadThread.getInstance().enqueueNewKey(chkStr);

                    } else {

                        final boolean isOurOwnKey = (ck.getSeenCount() == 0); // its in database, but we never saw it, its ours

                        ck.incrementSeenCount();

                        if( ck.getLastSeen() < content.getTimestamp() ) {
                            ck.setLastSeen(content.getTimestamp());
                        }
                        if( ck.getFirstSeen() > content.getTimestamp() ) {
                            ck.setFirstSeen(content.getTimestamp());
                        }
                        ck.modify();

                        // enqueue key immediately if it is one of our keys and was never received
                        if( isOurOwnKey && !ck.isDownloaded() ) {
                            // enqueue for download
                            FileListDownloadThread.getInstance().enqueueNewKey(chkStr);
//                            System.out.println("processReceivedCHKKeys: new own key enqueued");
                            newOwnKeys++;
                        } else {
//                            System.out.println("processReceivedCHKKeys: key seen again");
                            seenKeys++;
                        }
                    }
                } catch(final Throwable t) {
                    logger.log(Level.SEVERE, "Exception in processReceivedCHKKeys", t);
                }
            }
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("processReceivedCHKKeys: finished processing keys, new="+newKeys+", seen="+seenKeys+", newOwn="+newOwnKeys);
            }
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during chk key processing", t);
        }
        SharedFilesCHKKeyStorage.inst().endThreadTransaction();
    }

    public static List<String> getCHKKeyStringsToDownload() {
        // retrieve all CHK keys that must be downloaded
        try {
            // rules what chks are choosed are in the following method
            final List<String> chkKeys = SharedFilesCHKKeyStorage.inst().retrieveSharedFilesCHKKeysToDownload(MAX_DOWNLOAD_RETRIES_1);
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("getCHKKeyStringsToDownload: returning keys: "+(chkKeys==null?"(none)":Integer.toString(chkKeys.size())));
            }
            return chkKeys;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in retrieveSharedFilesCHKKeysToDownload", t);
        }
        return null;
    }

    /**
     * @return  true if update was successful
     */
    public static boolean updateCHKKeyDownloadSuccessful(final String chkKey, final boolean isValid) {
        // this chk was successfully downloaded, update database
        try {
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("updateCHKKeyDownloadSuccessful: key="+chkKey+", isValid="+isValid);
            }
            return SharedFilesCHKKeyStorage.inst().updateSharedFilesCHKKeyAfterDownloadSuccessful(chkKey, isValid);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in updateSharedFilesCHKKeyAfterDownloadSuccessful", t);
        }
        return false;
    }

    /**
     * @return  true if we should retry this key
     */
    public static boolean updateCHKKeyDownloadFailed(final String chkKey) {
        try {
            final boolean doRetry = SharedFilesCHKKeyStorage.inst().updateSharedFilesCHKKeyAfterDownloadFailed(chkKey, MAX_DOWNLOAD_RETRIES_1);
            return doRetry;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in updateCHKKeyDownloadFailed", t);
        }
        return false;
    }

    public static boolean addNewCHKKeyToSend(final SharedFilesCHKKey key) {
        try {
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("addNewCHKKeyToSend: "+key);
            }
            if( !SharedFilesCHKKeyStorage.inst().beginExclusiveThreadTransaction() ) {
                return false;
            }
            SharedFilesCHKKeyStorage.inst().storeItem(key);
            SharedFilesCHKKeyStorage.inst().endThreadTransaction();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in addNewCHKKeyToSend", t);
        }
        return false;
    }
}
