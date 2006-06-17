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
import frost.gui.objects.*;
import frost.messages.*;
import frost.transferlayer.*;

public class UpdateIdThread extends Thread // extends BoardUpdateThreadObject implements BoardUpdateThread
{
    //private static int keyCount = 0;
    //private static int minKeyCount = 50;
    //private static int maxKeysPerFile = 5000;
//  private int maxKeys;

    private static Logger logger = Logger.getLogger(UpdateIdThread.class.getName());

    private String date;
    private Board board;
    private String publicKey;
    private String privateKey;
    private String requestKey;
    private String insertKey;

    private boolean isForToday = false;

    private IndexSlots indexSlots;
    private final static int MAX_SLOTS_PER_DAY = 100;

//    public int getThreadType() {
//        return BoardUpdateThread.BOARD_FILE_DNLOAD;
//    }

    /**
     * Returns true if no error occured.
     */
    private boolean uploadIndexFile() throws Throwable {

        logger.info("FILEDN: UpdateIdThread - makeIndexFile for " + board.getName());

        if( indexSlots.findFirstFreeUploadSlot() < 0 ) {
            // no free upload slot, don't continue now, continue tomorrow
            return true;
        }

        // Calculate the keys to be uploaded
        Map files = null;
        Index index = Index.getInstance();
        synchronized(index) {
            // this method checks the final zip size (<=30000) !!!
            files = index.getUploadKeys(board);
        }

        if(files == null || files.size() == 0 ) {
            logger.info("FILEDN: No keys to upload, stopping UpdateIdThread for " + board.getName());
            return true;
        }

        logger.info("FILEDN: Starting upload of index file to board " + board.getName()+"; files="+files.size());

        FrostIndex frostIndex = new FrostIndex(files);
        files = null;

        return IndexFileUploader.uploadIndexFile(frostIndex, board, insertKey, indexSlots);
    }

    // If we're getting too much files on a board, we lower
    // the maxAge of keys. That way older keys get removed
    // sooner. With the new index system it should be possible
    // to work with large numbers of keys because they are
    // no longer kept in memory, but on disk.
//    private void adjustMaxAge(int count) {/*  //this is not used
//    //if (DEBUG) Core.getOut().println("FILEDN: AdjustMaxAge: old value = " + frame1.frame1.frostSettings.getValue("maxAge"));
//
//    int lowerLimit = 10 * maxKeys / 100;
//    int upperLimit = 90 * maxKeys / 100;
//    int maxAge = frame1.frame1.frame1.frostSettings.getIntValue("maxAge");
//
//    if (count < lowerLimit && maxAge < 21)
//        maxAge++;
//    if (count > upperLimit && maxAge > 1)
//        maxAge--;
//
//    frame1.frame1.frame1.frostSettings.setValue("maxAge", maxAge);
//    //if (DEBUG) Core.getOut().println("FILEDN: AdjustMaxAge: new value = " + maxAge);*/
//    }

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
            int index = indexSlots.findFirstFreeDownloadSlot();
            int failures = 0;
            while (failures < maxFailures && index >= 0 ) {

                logger.info("FILEDN: Requesting index " + index + " for board " + board.getName() + " for date " + date);

                String downKey = requestKey + index + ".idx.sha3.zip";
                IndexFileDownloaderResult idfResult = IndexFileDownloader.downloadIndexFile(downKey, board);
                
                if( idfResult == null ) {
                    // download failed. 
                    failures++;
                    // next loop we try next index
                    index = indexSlots.findNextFreeSlot(index);
                    continue;
                }
                
                // download was successful, mark it
                indexSlots.setSlotUsed(index);
                // next loop we try next index
                index = indexSlots.findNextFreeSlot(index);
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
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in UpdateIdThread", t);
        }

//      notifyThreadFinished(this);
    }

    /**Constructor*/
    public UpdateIdThread(Board board, String date, boolean isForToday) {

        this.board = board;
        this.date = date;
//      maxKeys = MainFrame.frostSettings.getIntValue("maxKeys");
        this.isForToday = isForToday;

        // first load the index with the date we wish to download
        indexSlots = new IndexSlots("indicesV2-", board, date, MAX_SLOTS_PER_DAY);

        publicKey = board.getPublicKey();
        privateKey = board.getPrivateKey();

        if (board.isPublicBoard() == false && publicKey != null) {
            requestKey = new StringBuffer()
                    .append(publicKey)
                    .append("/")
                    .append(date)
                    .append("/")
                    .toString();
        } else {
            requestKey = new StringBuffer()
                    .append("KSK@frost/index/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(date)
                    .append("/")
                    .toString();
        }

        // we make all inserts today (isForCurrentDate)
        if (board.isPublicBoard() == false && privateKey != null) {
            insertKey = new StringBuffer()
                    .append(privateKey)
                    .append("/")
                    .append(date)
                    .append("/")
                    .toString();
        } else {
            insertKey = new StringBuffer()
                    .append("KSK@frost/index/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(date)
                    .append("/")
                    .toString();
        }
    }
}
