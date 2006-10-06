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

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.identities.*;
import frost.storage.database.applayer.*;

public class FileListManager {

    private static Logger logger = Logger.getLogger(FileListManager.class.getName());
    
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
        long maxDiff = maxAge * 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        long minDate = now - maxDiff;
        
        // FIXME: if we share files, reshare also files that don't MUST be shared. otherwise we have to reshare them sooner
        
        List localIdentities = Core.getIdentities().getLocalIdentities();
        int identityCount = localIdentities.size(); 
        int maxKeys = 250; // FIXME: count utf-8 size of sharedxmlfiles, not more than 512kb!
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

            LinkedList filesToShare = getUploadItemsToShare(idToUpdate.getUniqueName(), maxKeys, minDate);
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

    private static LinkedList getUploadItemsToShare(String owner, int maxItems, long minDate) {

        LinkedList result = new LinkedList();
        
        List sharedFileItems = FileTransferManager.getInstance().getSharedFilesManager().getModel().getItems();
        
        for( Iterator i = sharedFileItems.iterator(); i.hasNext(); ) {
            FrostSharedFileItem sfo = (FrostSharedFileItem) i.next();
            
            if( !sfo.getOwner().equals(owner) ) {
                continue;
            }
            
            if( sfo.getRefLastSent() < minDate ) {
                
                // we must share this file now, either its new or updated
                result.add( sfo.getSharedFileXmlFileInstance() );
            }
    
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

        List sharedFileItems = FileTransferManager.getInstance().getSharedFilesManager().getModel().getItems();

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
            // new identity, add
            Core.getIdentities().addIdentity(content.getReceivedOwner());
            localOwner = content.getReceivedOwner();
        }
        localOwner.updateLastSeenTimestamp(content.getTimestamp());
        
        if (localOwner.isBAD() && Core.frostSettings.getBoolValue("hideBadFiles")) {
            logger.info("Skipped index file from BAD user " + localOwner.getUniqueName());
            return true;
        }

        List downloadItems = FileTransferManager.getInstance().getDownloadManager().getModel().getItems();

        for( Iterator i = content.getFileList().iterator(); i.hasNext(); ) {
            SharedFileXmlFile sfx = (SharedFileXmlFile) i.next();
            
            FrostFileListFileObject sfo = new FrostFileListFileObject(sfx, localOwner, content.getTimestamp());
            
            // update filelist database table
            try {
                AppLayerDatabase.getFileListDatabaseTable().insertOrUpdateFrostFileListFileObject(sfo);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception in insertOrUpdateFrostSharedFileObject", t);
            }
            
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
                    break; // there is only one file in download table with same sha
                }
            }
        }
        return true;
    }
}
