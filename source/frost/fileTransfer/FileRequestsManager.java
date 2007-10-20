/*
 FileRequestsManager.java / Frost
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

import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.storage.perst.filelist.*;

/**
 * Collects the files to request from other users.
 * Processes incoming requests.
 */
public class FileRequestsManager {

    private static final Logger logger = Logger.getLogger(SharedFilesCHKKeyManager.class.getName());

    private static final int MAX_SHA_PER_REQUESTFILE = 350;

    private static final long MIN_LAST_UPLOADED = 10; // start upload if last upload is X days back

    /**
     * @return List with SHA strings that should be requested
     */
    public static List<String> getRequestsToSend() {

        // get files from downloadtable that are shared and check if we should send a request for them
        // sha256 = 64 bytes, send a maximum of 350 requests per file (<32kb)

        // Rules for send of a request:
        // - don't send a request if the file to request was not seen in a file index for more than 3 days
        //   -> maybe file was already uploaded!
        // - must be not requested since 23h ( by us or others )
        // - we DON'T have the chk OR
        // - we HAVE the chk, but download FAILED, and last try was not longer then 3 days before (maybe successful now)

        // FIXME: maybe only request FAILED if failed reason was Data_Not_Found!
        //        But this requires that the dlitem remembers the failed reason!
        // FIXME: maybe only send requests when CHK queue is empty

        final long now = System.currentTimeMillis();
        final long before23hours = now -  1L * 23L * 60L * 60L * 1000L;
        final long before3days =   now -  3L * 24L * 60L * 60L * 1000L;

        final List<String> mustSendRequests = new LinkedList<String>();

        final List<FrostDownloadItem> downloadModelItems = FileTransferManager.inst().getDownloadManager().getDownloadItemList();
        for( final FrostDownloadItem dlItem : downloadModelItems ) {

            if( !dlItem.isSharedFile() ) {
                continue;
            }

            if( dlItem.isEnabled().booleanValue() == false ) {
                continue;
            }

            final FrostFileListFileObject sfo = dlItem.getFileListFileObject();

            if( sfo.getLastReceived() < before3days ) {
                // sha not received for 3 days, is it still shared, or maybe already uploaded? don't request it.
                continue;
            }

            if( sfo.getRequestLastSent() > before23hours || sfo.getRequestLastReceived() > before23hours ) {
                // request received or send in last 24 hours
                continue;
            }

            if( dlItem.getKey() != null && dlItem.getKey().length() > 0 ) {
                // FIXME: test fix
                // || dlItem.getLastDownloadStopTime() < before3days
                if( dlItem.getState() != FrostDownloadItem.STATE_FAILED  ) {
                    // download failed OR last download try was not in last 2 days (retry!)
                    continue;
                }
            }

            // we MUST request this file
            mustSendRequests.add( sfo.getSha() );

            if( mustSendRequests.size() == MAX_SHA_PER_REQUESTFILE ) {
                break;
            }
        }

        return mustSendRequests;
    }

    /**
     * @param requests a List of String objects with SHAs that were successfully sent within a request file
     */
    public static void updateRequestsWereSuccessfullySent(final List<String> requests) {

        final long now = System.currentTimeMillis();

        final List<FrostDownloadItem> downloadModelItems = FileTransferManager.inst().getDownloadManager().getDownloadItemList();

        // first update filelistfiles in memory
        for( final String sha : requests ) {
            // filelist files in download table
            for( final FrostDownloadItem dlItem : downloadModelItems ) {
                if( !dlItem.isSharedFile() ) {
                    continue;
                }
                final FrostFileListFileObject sfo = dlItem.getFileListFileObject();
                if( !sfo.getSha().equals(sha) ) {
                    continue;
                }
                sfo.setRequestsSentCount(sfo.getRequestsSentCount() + 1);
                sfo.setRequestLastSent(now);
            }
        }

        // then update same filelistfiles in database
        for( final String sha : requests ) {
            FileListStorage.inst().updateFrostFileListFileObjectAfterRequestSent(sha, now);
        }
        FileListStorage.inst().commit();
    }

    /**
     * Process the List of newly received requests (sha keys)
     */
    public static void processReceivedRequests(final FileRequestFileContent content) {

        if( content == null || content.getShaStrings() == null || content.getShaStrings().size() == 0 ) {
            return;
        }

        final long now = System.currentTimeMillis();
        final long minDiff = MIN_LAST_UPLOADED * 24L * 60L * 60L * 1000L; // MIN_LAST_UPLOADED days in milliseconds
        final long minLastUploaded = now - minDiff; // starts items whose lastupload was before this time

        final List<FrostDownloadItem> downloadModelItems = FileTransferManager.inst().getDownloadManager().getDownloadItemList();
        final List<FrostSharedFileItem> sharedFilesModelItems = FileTransferManager.inst().getSharedFilesManager().getSharedFileItemList();

        // first update the download and shared files in memory
        for( final String sha : content.getShaStrings() ) {
            // filelist files in download table
            for( final FrostDownloadItem dlItem : downloadModelItems ) {
                if( !dlItem.isSharedFile() ) {
                    continue;
                }
                final FrostFileListFileObject sfo = dlItem.getFileListFileObject();
                if( !sfo.getSha().equals(sha) ) {
                    continue;
                }
                sfo.setRequestsReceivedCount(sfo.getRequestsReceivedCount() + 1);
                if( sfo.getRequestLastReceived() < content.getTimestamp() ) {
                    sfo.setRequestLastReceived(content.getTimestamp());
                }
            }

            // our own shared files in shared files table
            for( final FrostSharedFileItem sfo : sharedFilesModelItems ) {
                if( !sfo.getSha().equals(sha) ) {
                    continue;
                }

                if( sfo.getRequestLastReceived() < content.getTimestamp() ) {
                    sfo.setRequestLastReceived(content.getTimestamp());
                }
                sfo.setRequestsReceived(sfo.getRequestsReceived() + 1);

                // Maybe start an upload
                // Rules:
                // - only if upload is not running already
                // - only if last upload was'nt earlier than 3 days
                // - only if our lastuploaded is NOT after the request timestamp

                // search upload table, check if we currently upload this file
                if( !sfo.isCurrentlyUploading() && sfo.isValid() ) {
                    // is not uploading currently
                    if( sfo.getLastUploaded() < minLastUploaded
                            && sfo.getLastUploaded() < content.getTimestamp() )
                    {
                        // last upload earlier than X days before, start upload
                        // add to upload files
                        FileTransferManager.inst().getUploadManager().getModel().addNewUploadItemFromSharedFile(sfo);

                        logger.log(Level.SEVERE, "INFO: Shared file upload started, file='"+sfo.getFilename()+
                                "', timeNow="+now+
                                ", minLastUploaded="+minLastUploaded+
                                ", requestTimestamp="+content.getTimestamp()+
                                ", lastUploaded="+sfo.getLastUploaded());
                    }
                }
            }
        }

        // now update the filelistfiles in database
        for( final String sha : content.getShaStrings() ) {
            FileListStorage.inst().updateFrostFileListFileObjectAfterRequestReceived(sha, content.getTimestamp());
        }
        FileListStorage.inst().commit();
    }
}
