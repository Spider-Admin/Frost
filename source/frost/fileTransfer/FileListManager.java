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
import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.identities.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.identities.*;

public class FileListManager {

    private static final Logger logger = Logger.getLogger(FileListManager.class.getName());

    public static final int MAX_FILES_PER_FILE = 250; // TODO: count utf-8 size of sharedxmlfiles, not more than 512kb!

    /**
     * Used to sort FrostSharedFileItems by refLastSent ascending.
     */
    private static final Comparator<FrostSharedFileItem> refLastSentComparator = new Comparator<FrostSharedFileItem>() {
        public int compare(final FrostSharedFileItem value1, final FrostSharedFileItem value2) {
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
        //   (this wrap-around ensures that each file will be send over the time)

        // compute minDate, items last shared before this date must be reshared
        final int maxAge = Core.frostSettings.getIntValue(SettingsClass.MIN_DAYS_BEFORE_FILE_RESHARE);
        final long maxDiff = maxAge * 24L * 60L * 60L * 1000L;
        final long now = System.currentTimeMillis();
        final long minDate = now - maxDiff;

        final List<LocalIdentity> localIdentities = Core.getIdentities().getLocalIdentities();
        int identityCount = localIdentities.size();

        // we modify several own identities (id.setLastFilesSharedMillis())
        if( !IdentitiesStorage.inst().beginExclusiveThreadTransaction() ) {
            return null;
        }
        try {
            while(identityCount > 0) {

                LocalIdentity idToUpdate = null;
                long minUpdateMillis = Long.MAX_VALUE;

                // find next identity to update
                for(final LocalIdentity id : localIdentities ) {
                    final long lastShared = id.getLastFilesSharedMillis();
                    if( lastShared < minUpdateMillis ) {
                        minUpdateMillis = lastShared;
                        idToUpdate = id;
                    }
                }

                // mark that we tried this owner
                idToUpdate.setLastFilesSharedMillis(now);

                final LinkedList<SharedFileXmlFile> filesToShare =
                    getUploadItemsToShare(idToUpdate.getUniqueName(), MAX_FILES_PER_FILE, minDate);
                if( filesToShare != null && filesToShare.size() > 0 ) {
                    final FileListManagerFileInfo fif = new FileListManagerFileInfo(filesToShare, idToUpdate);
                    return fif;
                } else {
                    // else try next owner
                    identityCount--;
                }
            }

            // currently there is nothing to share
            return null;

        } finally {
            IdentitiesStorage.inst().endThreadTransaction();
        }
    }

    private static LinkedList<SharedFileXmlFile> getUploadItemsToShare(
            final String owner,
            final int maxItems,
            final long minDate)
    {

        final LinkedList<SharedFileXmlFile> result = new LinkedList<SharedFileXmlFile>();

        final ArrayList<FrostSharedFileItem> sorted = new ArrayList<FrostSharedFileItem>();

        {
            final List<FrostSharedFileItem> sharedFileItems = FileTransferManager.inst().getSharedFilesManager().getSharedFileItemList();

            // first collect all items for this owner and sort them
            for( final FrostSharedFileItem sfo : sharedFileItems ) {
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
            final FrostSharedFileItem sfo = sorted.get(0);
            if( sfo.getRefLastSent() > minDate ) {
                // oldest item is'nt too old, don't share
                return result;
            }
        }

        // finally add up to MAX_FILES items from the sorted list
        for( final FrostSharedFileItem sfo : sorted ) {
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
    public static boolean updateFileListWasSuccessfullySent(final List<SharedFileXmlFile> files) {

        final long now = System.currentTimeMillis();

        final List<FrostSharedFileItem> sharedFileItems = FileTransferManager.inst().getSharedFilesManager().getSharedFileItemList();

        for( final SharedFileXmlFile sfx : files ) {
            // update FrostSharedUploadFileObject
            for( final FrostSharedFileItem sfo : sharedFileItems ) {
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
    public static boolean processReceivedFileList(final FileListFileContent content) {

        if( content == null
            || content.getReceivedOwner() == null
            || content.getFileList() == null
            || content.getFileList().size() == 0 )
        {
            return false;
        }

        Identity localOwner;
        synchronized( Core.getIdentities().getLockObject() ) {
            localOwner = Core.getIdentities().getIdentity(content.getReceivedOwner().getUniqueName());
            if( localOwner == null ) {
                // new identity, add. Validated inside FileListFile.readFileListFile()
                localOwner = content.getReceivedOwner();
                localOwner.setLastSeenTimestampWithoutUpdate(content.getTimestamp());
                if( !Core.getIdentities().addIdentity(content.getReceivedOwner()) ) {
                    logger.severe("Core.getIdentities().addIdentity() returned false for identity: "+content.getReceivedOwner());
                    return false;
                }
            } else {
                if( localOwner.getLastSeenTimestamp() < content.getTimestamp() ) {
                    localOwner.setLastSeenTimestamp(content.getTimestamp());
                }
            }
        }

        if (localOwner.isBAD() && Core.frostSettings.getBoolValue(SettingsClass.SEARCH_HIDE_BAD)) {
            logger.info("Skipped index file from BAD user " + localOwner.getUniqueName());
            return true;
        }

        final List<FrostDownloadItem> downloadItems = FileTransferManager.inst().getDownloadManager().getDownloadItemList();

        // update all filelist files, maybe restart failed downloads
        final List<FrostDownloadItem> downloadsToRestart = new ArrayList<FrostDownloadItem>();
        boolean errorOccured = false;

        if( !FileListStorage.inst().beginExclusiveThreadTransaction() ) {
            logger.severe("Failed to begin an EXCLUSIVE thread transaction, aborting.");
            return false;
        }

        final boolean isFreenet07 = FcpHandler.isFreenet07();

        try {
            for( final SharedFileXmlFile sfx : content.getFileList() ) {

                final FrostFileListFileObject sfo = new FrostFileListFileObject(sfx, localOwner, content.getTimestamp());

                if( isFreenet07 && FreenetKeys.isOld07ChkKey( sfo.getKey() )) {
                    // ignore old chk keys
                    continue;
                }

                // before updating the file list object (this overwrites the current lastUploaded time),
                // check if there is a failed download item for this shared file. If yes, and the lastUpload
                // time is later than the current one, restart the download automatically.
                for( final FrostDownloadItem dlItem : downloadItems ) {
                    if( !dlItem.isSharedFile() || dlItem.getState() != FrostDownloadItem.STATE_FAILED ) {
                        continue;
                    }
                    final FrostFileListFileObject dlSfo = dlItem.getFileListFileObject();
                    if( dlSfo.getSha().equals( sfx.getSha() ) ) {
                        if( dlSfo.getLastUploaded() < sfo.getLastUploaded() ) {
                            // restart failed download, file was uploaded again
                            downloadsToRestart.add(dlItem); // restart later if no error occured
                        }
                    }
                }

                // update filelist storage
                final boolean wasOk = FileListStorage.inst().insertOrUpdateFileListFileObject(sfo);
                if( wasOk == false ) {
                    errorOccured = true;
                    break;
                }
            }
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception during insertOrUpdateFrostSharedFileObject", t);
            errorOccured = true;
        }

        if( errorOccured ) {
            FileListStorage.inst().rollbackTransaction();
            return false;
        } else {
            FileListStorage.inst().endThreadTransaction();
        }
// FIXME: change key for running, stalled downloads
        // after updating the db, check if we have to update download items with the new informations
        for( final SharedFileXmlFile sfx : content.getFileList() ) {

            // if a FrostDownloadItem references this file (by sha), retrieve the updated file from db and set it
            for( final FrostDownloadItem dlItem : downloadItems ) {
                if( !dlItem.isSharedFile() ) {
                    continue;
                }
                final FrostFileListFileObject dlSfo = dlItem.getFileListFileObject();
                if( dlSfo.getSha().equals( sfx.getSha() ) ) {
                    // this download item references the updated file
                    // update the shared file object from database (owner, sources, ... may have changed)

                    // NOTE: if no key was set before, this sets the chkKey and the ticker will start to download this file!

                    FrostFileListFileObject updatedSfo = null;
                    if( !FileListStorage.inst().beginCooperativeThreadTransaction() ) {
                        logger.severe("Failed to begin an COOPERATIVE thread transaction.");
                    } else {
                        updatedSfo = FileListStorage.inst().getFileBySha(sfx.getSha());
                        FileListStorage.inst().endThreadTransaction();
                    }
                    if( updatedSfo != null ) {
                        dlItem.setFileListFileObject(updatedSfo);
                    } else {
                        System.out.println("no file for sha!");
                    }
                    break; // there is only one file in download table with same SHA
                }
            }
        }

        // restart failed downloads, as collected above
        for( final FrostDownloadItem dlItem : downloadsToRestart ) {
            dlItem.setState(FrostDownloadItem.STATE_WAITING);
            dlItem.setRetries(0);
            dlItem.setLastDownloadStopTime(0);
            dlItem.setEnabled(Boolean.valueOf(true)); // enable download on restart
        }

        return true;
    }
}
