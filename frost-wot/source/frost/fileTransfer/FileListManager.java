/*
  FileListManager.java / Frost
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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.FcpHandler;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.identities.*;
import frost.storage.database.applayer.*;

public class FileListManager {

    private static final Logger logger = Logger.getLogger(FileListManager.class.getName());
    
    public static final int MAX_FILES_PER_FILE = 250; // TODO: count utf-8 size of sharedxmlfiles, not more than 512kb!
    
    /**
     * Used to sort FrostSharedFileItems by refLastSent ascending.
     */
    private static final Comparator<FrostSharedFileItem> refLastSentComparator = new Comparator<FrostSharedFileItem>() {
        public int compare(FrostSharedFileItem value1, FrostSharedFileItem value2) {
            if (value1.getRefLastSent() > value2.getRefLastSent()) {
                return 1;
            } else if (value1.getRefLastSent() < value2.getRefLastSent()) {
                return -1;
            } else {
                return 0;
            }
        }
    };
    
    /**
     * @return  an info class that is guaranteed to contain an owner and files
     */
    public static FileListManagerFileInfo getFilesToSend() {

        // get files to share from UPLOADFILES
        // - max. 250 keys per fileindex
        // - get keys of only 1 owner/anonymous, next time get keys from different owner
        // this wrap-arounding ensures that each file will be send over the time

        // compute minDate, items last shared before this date must be reshared
        int maxAge = Core.frostSettings.getIntValue(SettingsClass.MIN_DAYS_BEFORE_FILE_RESHARE);
        long maxDiff = (long)maxAge * 24L * 60L * 60L * 1000L;
        long now = System.currentTimeMillis();
        long minDate = now - maxDiff;
        
        List localIdentities = Core.getIdentities().getLocalIdentities();
        int identityCount = localIdentities.size(); 
        while(identityCount > 0) {
            
            LocalIdentity idToUpdate = null;
            long minUpdateMillis = Long.MAX_VALUE;
            
            // find next identity to update
            for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
                LocalIdentity id = (LocalIdentity)i.next();
                long lastShared = id.getLastFilesSharedMillis();
                if( lastShared < minUpdateMillis ) {
                    minUpdateMillis = lastShared;
                    idToUpdate = id;
                }
            }

            // mark that we tried this owner
            idToUpdate.updateLastFilesSharedMillis();

            int maxfiles;
            if (FcpHandler.isFreenet07()) {
            	maxfiles = 35; // should ideally fit in a single chunk on .7
            } else {
            	maxfiles = MAX_FILES_PER_FILE;
            }
            LinkedList<SharedFileXmlFile> filesToShare = getUploadItemsToShare(idToUpdate.getUniqueName(), maxfiles, minDate);
            if( filesToShare != null && filesToShare.size() > 0 ) {
                FileListManagerFileInfo fif = new FileListManagerFileInfo(filesToShare, idToUpdate); 
                return fif;
            }
            // else try next owner
            identityCount--;
        }
        
        // nothing to share now
        return null;
    }

    private static LinkedList<SharedFileXmlFile> getUploadItemsToShare(String owner, int maxItems, long minDate) {

        LinkedList<SharedFileXmlFile> result = new LinkedList<SharedFileXmlFile>();
        
        ArrayList<FrostSharedFileItem> sorted = new ArrayList<FrostSharedFileItem>();

        {        
            List sharedFileItems = FileTransferManager.inst().getSharedFilesManager().getModel().getItems();
            
            // first collect all items for this owner and sort them
            for( Iterator i = sharedFileItems.iterator(); i.hasNext(); ) {
                FrostSharedFileItem sfo = (FrostSharedFileItem) i.next();
                if( !sfo.isValid() ) {
                    continue;
                }
                if( !sfo.getOwner().equals(owner) ) {
                    continue;
                }
                sorted.add(sfo);
            }
        }

        if( sorted.isEmpty() ) {
            // no shared files for this owner
            return result;
        }

        // sort ascending, oldest items at the beginning
        Collections.sort(sorted, refLastSentComparator);

        {
            // check if oldest item must be shared (maybe its new or updated)
            FrostSharedFileItem sfo = sorted.get(0);
            if( sfo.getRefLastSent() > minDate ) {
                // oldest item is'nt too old, don't share
                return result;
            }
        }

        // finally add up to MAX_FILES items from the sorted list
        for( Iterator i = sorted.iterator(); i.hasNext(); ) {
            FrostSharedFileItem sfo = (FrostSharedFileItem) i.next();
            result.add( sfo.getSharedFileXmlFileInstance() );
            if( result.size() >= maxItems ) {
                return result;
            }
        }
        
        return result;
    }

    /**
     * Update sent files.
     * @param files  List of SharedFileXmlFile objects that were successfully sent inside a CHK file
     */
    public static boolean updateFileListWasSuccessfullySent(List files) {
        
        long now = System.currentTimeMillis();

        List sharedFileItems = FileTransferManager.inst().getSharedFilesManager().getModel().getItems();

        for( Iterator i = files.iterator(); i.hasNext(); ) {
            SharedFileXmlFile sfx = (SharedFileXmlFile) i.next();
            
            // update FrostSharedUploadFileObject
            for( Iterator j = sharedFileItems.iterator(); j.hasNext(); ) {
                FrostSharedFileItem sfo = (FrostSharedFileItem) j.next();
                
                if( sfo.getSha().equals(sfx.getSha()) ) {
                    sfo.setRefLastSent(now);
                }
            }
        }
        return true;
    }
    
    /**
     * Add or update received files from owner
     */
    public static boolean processReceivedFileList(FileListFileContent content) {
        
        if( content == null 
            || content.getReceivedOwner() == null 
            || content.getFileList() == null 
            || content.getFileList().size() == 0 )
        {
            return false;
        }
        
        Identity localOwner = Core.getIdentities().getIdentity(content.getReceivedOwner().getUniqueName());
        if( localOwner == null ) {
            // new identity, maybe add
            if( !Core.getIdentities().isNewIdentityValid(content.getReceivedOwner()) ) {
                // hash of public key does not match the unique name
                return false;
            }
            Core.getIdentities().addIdentity(content.getReceivedOwner());
            localOwner = content.getReceivedOwner();
        }
        localOwner.updateLastSeenTimestamp(content.getTimestamp());
        
        if (localOwner.isBAD() && Core.frostSettings.getBoolValue(SettingsClass.SEARCH_HIDE_BAD)) {
            logger.info("Skipped index file from BAD user " + localOwner.getUniqueName());
            return true;
        }

        // first, update all filelist files
        
        // get a connection for updates
        // works well if no duplicate SHA is in the received list, otherwise we have false updates
        Connection conn = AppLayerDatabase.getInstance().getPooledConnection();

        boolean errorOccured = false;
        try {
            conn.setAutoCommit(false);
        
            for( Iterator i = content.getFileList().iterator(); i.hasNext(); ) {
                SharedFileXmlFile sfx = (SharedFileXmlFile) i.next();
                
                FrostFileListFileObject sfo = new FrostFileListFileObject(sfx, localOwner, content.getTimestamp());
                
                // update filelist database table
                boolean wasOk = AppLayerDatabase.getFileListDatabaseTable().insertOrUpdateFrostFileListFileObject(sfo, conn);
                if( wasOk == false ) {
                    errorOccured = true;
                    break;
                }
            }
            if( errorOccured == false ) {
                conn.commit();
            } else {
                // if there is one error we skip the whole processing
                conn.rollback();
            }
            conn.setAutoCommit(true);
        } catch(Throwable t) {
            if( t.getMessage() != null && t.getMessage().indexOf("Select from table that has committed changes") > 0 ) {
                // only a warning!
                logger.log(Level.WARNING, "INFO: Select from table that has committed changes");
                try { conn.commit(); } catch(Throwable t1) { logger.log(Level.SEVERE, "Exception during commit", t1); }
            } else {
                logger.log(Level.SEVERE, "Exception during insertOrUpdateFrostSharedFileObject", t);
                try { conn.rollback(); } catch(Throwable t1) { logger.log(Level.SEVERE, "Exception during rollback", t1); }
            }
            try { conn.setAutoCommit(true); } catch(Throwable t1) { }
        } finally {
            AppLayerDatabase.getInstance().givePooledConnection(conn);
        }
        
        if( errorOccured ) {
            return false;
        }

        // after updating the db, check if we have to update download items with the new informations
        List downloadItems = FileTransferManager.inst().getDownloadManager().getModel().getItems();

        for( Iterator i = content.getFileList().iterator(); i.hasNext(); ) {
            SharedFileXmlFile sfx = (SharedFileXmlFile) i.next();

            // if a FrostDownloadItem references this file (by sha), retrieve the updated file from db and set it
            for( Iterator j = downloadItems.iterator(); j.hasNext(); ) {
                FrostDownloadItem dlItem = (FrostDownloadItem) j.next();
                if( !dlItem.isSharedFile() ) {
                    continue;
                }
                FrostFileListFileObject dlSfo = dlItem.getFileListFileObject();
                if( dlSfo.getSha().equals( sfx.getSha() ) ) {
                    // this download item references the updated file
                    // update the shared file object from database (owner, sources, ... may have changed)
                    FrostFileListFileObject updatedSfo = null;
                    try {
                        updatedSfo = AppLayerDatabase.getFileListDatabaseTable().retrieveFileBySha(sfx.getSha());
                    } catch (Throwable t) {
                        logger.log(Level.SEVERE, "Exception in retrieveFileBySha", t);
                    }
                    if( updatedSfo != null ) {
                        dlItem.setFileListFileObject(updatedSfo);
                    }
                    break; // there is only one file in download table with same SHA
                }
            }
        }

        return true;
    }
}
