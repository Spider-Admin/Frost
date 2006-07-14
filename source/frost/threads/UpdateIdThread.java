/*
  UpdateIdThread.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.threads;

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.database.transferlayer.*;
import frost.transferlayer.*;

public class UpdateIdThread extends Thread // extends BoardUpdateThreadObject implements BoardUpdateThread
{
    private static Logger logger = Logger.getLogger(UpdateIdThread.class.getName());

    private java.sql.Date date;
    private Board board;
    private String publicKey;
    private String privateKey;
    private String requestKey;
    private String insertKey;

    private boolean isForToday = false;

    private IndexSlotsDatabaseTable indexSlots;

//    public int getThreadType() {
//        return BoardUpdateThread.BOARD_FILE_DNLOAD;
//    }

    /**
     * Returns true if no error occured.
     */
    private boolean uploadIndexFile() throws Throwable {

        logger.info("FILEDN: UpdateIdThread - makeIndexFile for " + board.getName());

        // Calculate the keys to be uploaded, the list contains FrostUploadItemOwnerBoard objects
        List filesToShare = Index.getInstance().getUploadKeys(board);

        if(filesToShare == null || filesToShare.size() == 0 ) {
            logger.info("FILEDN: No keys to upload for board " + board.getName());
            return true;
        }

        logger.info("FILEDN: Starting upload of index file to board " + board.getName()+"; files="+filesToShare.size());
        
        // the sharer of the files is the same for each item in this list, get it from 1st item
        FrostUploadItemOwnerBoard ob = (FrostUploadItemOwnerBoard)filesToShare.get(0);
        String ownSharerStr = ob.getOwner();
        LocalIdentity ownSharer = null; // anonymous
        if( ownSharerStr != null ) {
            ownSharer = Core.getIdentities().getLocalIdentity(ownSharerStr);
        }
//System.out.println("sharer="+ownSharerStr+" / "+ownSharer);
        FrostIndex frostIndex = new FrostIndex(filesToShare, ownSharer);

        boolean wasOk = IndexFileUploader.uploadIndexFile(frostIndex, board, insertKey, indexSlots, date);

        // if ok then update lastshared for sent files
        if( wasOk ) {
            java.sql.Date now = DateFun.getCurrentSqlDateGMT();
            for(Iterator i=filesToShare.iterator(); i.hasNext(); ) {
                FrostUploadItemOwnerBoard uob = (FrostUploadItemOwnerBoard)i.next();
                uob.setLastSharedDate(now);
            }
        }
        
        return wasOk;
    }

    /**
     * Returns true if no error occured.
     */
    private boolean uploadRequestFile() throws Throwable {

        logger.info("FILEDN: UpdateIdThread - makeRequestFile for " + board.getName());

        // Calculate the keys to be uploaded
        List requestKeys = Index.getInstance().getRequestKeys(board);

        if(requestKeys == null || requestKeys.size() == 0 ) {
            logger.info("FILEDN: No requests to upload for board " + board.getName());
            return true;
        }

        logger.info("FILEDN: Starting upload of request file to board " + board.getName()+"; requests="+requestKeys.size());
        
        List sha1ToRequest = new LinkedList();
        for(Iterator i=requestKeys.iterator(); i.hasNext(); ) {
            FrostDownloadItem dlItem = (FrostDownloadItem)i.next();
            sha1ToRequest.add( dlItem.getSHA1() );
        }
        
        boolean wasOk = IndexFileUploader.uploadRequestFile(sha1ToRequest, board, insertKey, indexSlots, date);

        // if ok then update requested status for download files
        if( wasOk ) {
            java.sql.Date now = DateFun.getCurrentSqlDateGMT();
            for(Iterator i=requestKeys.iterator(); i.hasNext(); ) {
                FrostDownloadItem dlItem = (FrostDownloadItem)i.next();
                dlItem.setLastRequestedDate(now);
                dlItem.setRequestedCount(dlItem.getRequestedCount()+1);
                if( dlItem.getState() == FrostDownloadItem.STATE_REQUESTING ) {
                    dlItem.setState(FrostDownloadItem.STATE_REQUESTED);
                }
            }
        }
        
        return wasOk;
    }

    public void run() {
//      notifyThreadStarted(this);

        try {
            // Wait some random time to speed up the update of the TOF table
            // ... and to not to flood the node
            int waitTime = (int) (Math.random() * 2000);
            // wait a max. of 2 seconds between start of threads
            Mixed.wait(waitTime);

            int maxFailures;
            if (isForToday) {
                maxFailures = 3; // skip a maximum of 2 empty slots for today
            } else {
                maxFailures = 2; // skip a maximum of 1 empty slot for backload
            }
            int index = indexSlots.findFirstDownloadSlot(date);
            int failures = 0;
            while (failures < maxFailures && index >= 0 ) {

                logger.info("FILEDN: Requesting index " + index + " for board " + board.getName() + " for date " + date);

                String downKey = requestKey + index + ".idx.sha3.zip";
                IndexFileDownloaderResult idfResult = IndexFileDownloader.downloadIndexFile(downKey, board);
                
                if( idfResult == null ) {
                    // download failed. 
                    failures++;
                    // next loop we try next index
                    index = indexSlots.findNextDownloadSlot(index, date);
                    continue;
                }
                
                // download was successful, mark it
                indexSlots.setDownloadSlotUsed(index, date);
                // next loop we try next index
                index = indexSlots.findNextDownloadSlot(index, date);
                failures = 0;
                
                // we do not look at the idfResult, it does not matter if it was not, 
                // files were added on success 
            }

            // FIXED: I assume its enough to do this on current day, not for all days the same
            //   this thread is started up to maxDays times per board update!

            // Ok, we're done with downloading the keyfiles
            // Now calculate whitch keys we want to upload.
            // We only upload own keyfiles if:
            // 1. We've got more than minKeyCount keys to upload
            // 2. We don't upload any more files
            if( !isInterrupted() && isForToday ) {
                try {
                    uploadIndexFile();
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception during uploadIndexFile()", t);
                }
            }
            // check if we have requests to upload for this board, and upload them
            if( !isInterrupted() && isForToday ) {
                try {
                    uploadRequestFile();
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception during uploadRequestFile()", t);
                }
            }

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in UpdateIdThread", t);
        }
        indexSlots.close();
//      notifyThreadFinished(this);
    }

    /**Constructor*/
    public UpdateIdThread(Board board, String dateStr, java.sql.Date date, boolean isForToday) {

        this.board = board;
        this.date = date;
        this.isForToday = isForToday;

        // first load the index with the date we wish to download
        indexSlots = new IndexSlotsDatabaseTable(IndexSlotsDatabaseTable.FILELISTS, board.getName());

        publicKey = board.getPublicKey();
        privateKey = board.getPrivateKey();

        if (board.isPublicBoard() == false && publicKey != null) {
            requestKey = new StringBuffer()
                    .append(publicKey)
                    .append("/")
                    .append(dateStr)
                    .append("/")
                    .toString();
        } else {
            requestKey = new StringBuffer()
                    .append("KSK@frost/index/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(dateStr)
                    .append("/")
                    .toString();
        }

        // we make all inserts today (isForCurrentDate)
        if (board.isPublicBoard() == false && privateKey != null) {
            insertKey = new StringBuffer()
                    .append(privateKey)
                    .append("/")
                    .append(dateStr)
                    .append("/")
                    .toString();
        } else {
            insertKey = new StringBuffer()
                    .append("KSK@frost/index/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(dateStr)
                    .append("/")
                    .toString();
        }
    }
}
