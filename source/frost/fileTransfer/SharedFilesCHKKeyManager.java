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

import frost.storage.database.applayer.*;
import frost.threads.*;

public class SharedFilesCHKKeyManager {

    private static Logger logger = Logger.getLogger(SharedFilesCHKKeyManager.class.getName());

    // FIXME: download bis zu _1 mal hintereinander, wenn fail dann noch bis _2 mal täglich. dann ende.
    private static final int MAX_DOWNLOAD_RETRIES_1 = 7;
//    private static final int MAX_DOWNLOAD_RETRIES_2 = 7 + 3;

    private static final int MAX_KEYS_TO_SEND = 300;

    /**
     * @return List with SharedFileCHKKey object that should be send inside a KSK pointer file
     */
    public static List getCHKKeysToSend() {
        // get a number of CHK keys from database that must be send
        // include only 1 of our new CHK keys into this list, don't send CHK keys of different identities
        // together, this compromises anonymity!
        try {
            // rules what chks are choosed are in the following method
            return AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().getSharedFilesCHKKeysToSend(MAX_KEYS_TO_SEND);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in SharedFilesCHKKeysDatabaseTable().getSharedFilesCHKKeysToSend", t);
        }
        return null;
    }
    
    /**
     * @param chkKeys a List of SharedFileCHKKey objects that were successfully sent within a KSK pointer file
     */
    public static void updateCHKKeysWereSuccessfullySent(List chkKeys) {
        
        long now = System.currentTimeMillis();
        
        for( Iterator i = chkKeys.iterator(); i.hasNext(); ) {
            SharedFilesCHKKey key = (SharedFilesCHKKey) i.next();
            
            key.incrementSentCount();
            key.setLastSent(now);
            
            try {
                AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().updateSharedFilesCHKKeyAfterSend(key);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception in SharedFilesCHKKeysDatabaseTable().updateCHKKeysWereSuccessfullySent", t);
            }            
        }
    }
    
    /**
     * Process the List of newly received chk keys.
     * Update existing keys or insert new keys.
     */
    public static void processReceivedCHKKeys(FilePointerFileContent content) {
        
        if( content == null || content.getChkKeyStrings() == null || content.getChkKeyStrings().size() == 0 ) {
            return;
        }
System.out.println("processReceivedCHKKeys: processing keys");
        for( Iterator i = content.getChkKeyStrings().iterator(); i.hasNext(); ) {
            String chkStr = (String) i.next();

            try {
                SharedFilesCHKKey ck = AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().retrieveSharedFilesCHKKey(chkStr);
                if( ck == null ) {
                    // new key
System.out.println("processReceivedCHKKeys: enqueueing new key");
                    // add to database
                    ck = new SharedFilesCHKKey(chkStr, content.getTimestamp());
                    AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().insertSharedFilesCHKKey(ck);
                    
                    // new key, directly enqueue for download
                    FileListDownloadThread.getInstance().enqueueNewKey(chkStr);

                } else {
                    
                    boolean isOurOwnKey = (ck.getSeenCount() == 0); // its in database, but we never saw it, its ours
                    
                    ck.incrementSeenCount();
                    
                    if( ck.getLastSeen() < content.getTimestamp() ) {
                        ck.setLastSeen(content.getTimestamp());
                    }
                    if( ck.getFirstSeen() > content.getTimestamp() ) {
                        ck.setFirstSeen(content.getTimestamp());
                    }
                    AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().updateSharedFilesCHKKeyAfterReceive(ck);

                    // enqueue key immediately if it is one of our keys and was never received
                    if( isOurOwnKey && !ck.isDownloaded() ) {
                        // enqueue for download
                        FileListDownloadThread.getInstance().enqueueNewKey(chkStr);
System.out.println("processReceivedCHKKeys: new key enqueued");
                    }
                    
System.out.println("processReceivedCHKKeys: key seen again");
                }
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception in processReceivedCHKKeys", t);
            }
        }
    }

    public static List getCHKKeyStringsToDownload() {
        // retrieve all CHK keys that must be downloaded
        try {
            // rules what chks are choosed are in the following method
            List chkKeys = AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().retrieveSharedFilesCHKKeysToDownload(7);
System.out.println("getCHKKeyStringsToDownload: returing keys: "+(chkKeys==null?"null":""+chkKeys.size()));            
            return chkKeys;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in retrieveSharedFilesCHKKeysToDownload", t);
        }
        return null;
    }

    /**
     * @return  true if update was successful
     */
    public static boolean updateCHKKeyDownloadSuccessful(String chkKey, boolean isValid) {
        // this chk was successfully downloaded, update database
        try {
System.out.println("updateCHKKeyDownloadSuccessful: "+chkKey+","+isValid);
            return AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().updateSharedFilesCHKKeyAfterDownloadSuccessful(chkKey, isValid);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in updateSharedFilesCHKKeyAfterDownloadSuccessful", t);
        }
        return false;
    }
    
    /**
     * @return  true if we should retry this key
     */
    public static boolean updateCHKKeyDownloadFailed(String chkKey) {
        try {
            SharedFilesCHKKey ck = AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().retrieveSharedFilesCHKKey(chkKey);
            if( ck == null ) {
                return false;
            }
            ck.incDownloadRetries();
            ck.setLastDownloadTryStopTime(System.currentTimeMillis());
            boolean wasOk = AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().updateSharedFilesCHKKeyAfterDownloadFailed(ck);
            if( !wasOk ) {
                // don't retry
                return false;
            }
            if( ck.getDownloadRetries() < MAX_DOWNLOAD_RETRIES_1 ) {
                return true; // retry download
            }
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in updateCHKKeyDownloadFailed", t);
        }
        return false;
    }
    
    public static boolean addNewCHKKeyToSend(SharedFilesCHKKey key) {
        try {
System.out.println("addNewCHKKeyToSend: "+key);            
            return AppLayerDatabase.getSharedFilesCHKKeysDatabaseTable().insertSharedFilesCHKKey(key);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in addNewCHKKeyToSend", t);
        }
        return false;
    }
}
