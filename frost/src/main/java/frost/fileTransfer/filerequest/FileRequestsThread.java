/*
  FileRequestsThread.java / Frost
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
package frost.fileTransfer.filerequest;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.joda.time.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.filelist.*;
import frost.storage.perst.*;
import frost.transferlayer.*;
import frost.util.*;
import frost.util.Logging;

/**
 * Thread receives and sends file requests periodically.
 * Runs forever and sleeps between loops.
 */
public class FileRequestsThread extends Thread {

    private static final Logger logger = Logger.getLogger(FilePointersThread.class.getName());

    // sleeptime between request loops
    private static final int sleepTime = 10 * 60 * 1000;

    private final String keyPrefix;

    // one and only instance
    private static FileRequestsThread instance = new FileRequestsThread();

    private FileRequestsThread() {
        final String fileBase = Core.frostSettings.getValue(SettingsClass.FILE_BASE);
        keyPrefix = "KSK@frost/filerequests/" + fileBase + "-";
    }

    public static FileRequestsThread getInstance() {
        return instance;
    }

    public boolean cancelThread() {
        return false;
    }

    /**
     * Returns true if no error occured.
     */
    private boolean uploadRequestFile(final String dateStr, final IndexSlot gis) throws Throwable {

        // get a list of CHK keys to send
        final List<String> fileRequests = FileRequestsManager.getRequestsToSend();
        if( fileRequests == null || fileRequests.size() == 0 ) {
            logger.info("No requests to send.");
            return true;
        }
        if( Logging.inst().doLogFilebaseMessages() ) {
            System.out.println("uploadRequestFile: fileRequests to send: "+fileRequests.size());
        }

        final FileRequestFileContent content = new FileRequestFileContent(System.currentTimeMillis(), fileRequests);

        // write a file with requests to a tempfile
        final File tmpRequestFile = FileAccess.createTempFile("filereq_", ".xml");
        if( !FileRequestFile.writeRequestFile(content, tmpRequestFile) ) {
            logger.severe("Error writing the file requests file.");
            return false;
        }

        // Wait some random time to not to flood the node
        Mixed.waitRandom(2000);

        logger.info("Starting upload of request file containing "+fileRequests.size()+" SHAs");
        if( Logging.inst().doLogFilebaseMessages() ) {
            System.out.println("uploadRequestFile: Starting upload of request file containing "+fileRequests.size()+" SHAs");
        }

        final String insertKey = keyPrefix + dateStr + "-";
        final boolean wasOk = GlobalFileUploader.uploadFile(gis, tmpRequestFile, insertKey, ".xml", true);
        tmpRequestFile.delete();
        if( Logging.inst().doLogFilebaseMessages() ) {
            System.out.println("uploadRequestFile: upload finished, wasOk="+wasOk);
        }
        if( wasOk ) {
            FileRequestsManager.updateRequestsWereSuccessfullySent(fileRequests);
        }

        IndexSlotsStorage.inst().storeSlot(gis);

        return wasOk;
    }

    private void downloadDate(final String dateStr, final IndexSlot gis, final boolean isForToday) throws Throwable {

        // "KSK@frost/filerequests/2006.11.1-<index>.xml"
        final String requestKey = keyPrefix + dateStr + "-";

        int maxFailures;
        if (isForToday) {
            maxFailures = 3; // skip a maximum of 2 empty slots for today
        } else {
            maxFailures = 2; // skip a maximum of 1 empty slot for backload
        }
        int index = gis.findFirstDownloadSlot();
        int failures = 0;

        while (failures < maxFailures && index >= 0 ) {

            // Wait some random time to not to flood the node
            Mixed.waitRandom(2500);

            logger.info("Requesting index " + index + " for date " + dateStr);

            final boolean quicklyFailOnAdnf;
            final int maxRetries;
            if( Core.frostSettings.getBoolValue(SettingsClass.FCP2_QUICKLY_FAIL_ON_ADNF) ) {
                quicklyFailOnAdnf = true;
                maxRetries = 2;
            } else {
                // default
                quicklyFailOnAdnf = false;
                maxRetries = -1;
            }

            final String downKey = requestKey + index + ".xml";
            final GlobalFileDownloaderResult result = GlobalFileDownloader.downloadFile(downKey, FcpHandler.MAX_MESSAGE_SIZE_07, maxRetries);

            if( result == null ) {
                // download failed.
                if( gis.isDownloadIndexBehindLastSetIndex(index) ) {
                    // we stop if we tried maxFailures indices behind the last known index
                    failures++;
                }
                // next loop we try next index
                index = gis.findNextDownloadSlot(index);
                continue;
            }

            failures = 0;

            if( result.getErrorCode() == GlobalFileDownloaderResult.ERROR_EMPTY_REDIRECT ) {
                if( Logging.inst().doLogFilebaseMessages() ) {
                    if( quicklyFailOnAdnf ) {
                        System.out.println("FileRequestsThread.downloadDate: Index "+index+" got ADNF, will never try index again.");
                    } else {
                        System.out.println("FileRequestsThread.downloadDate: Skipping index "+index+" for now, will try again later.");
                    }
                }
                if( quicklyFailOnAdnf ) {
                    // don't try again
                    gis.setDownloadSlotUsed(index);
                    IndexSlotsStorage.inst().storeSlot(gis); // remember each progress
                }
                // next loop we try next index
                index = gis.findNextDownloadSlot(index);
                continue;
            }

            // downloaded something, mark it
            gis.setDownloadSlotUsed(index);
            // next loop we try next index
            index = gis.findNextDownloadSlot(index);

            if( result.getErrorCode() == GlobalFileDownloaderResult.ERROR_FILE_TOO_BIG ) {
                logger.severe("FileRequestsThread.downloadDate: Dropping index "+index+", FILE_TOO_BIG.");
            } else {
                // process results
                final File downloadedFile = result.getResultFile();

                final FileRequestFileContent content = FileRequestFile.readRequestFile(downloadedFile);
                downloadedFile.delete();
                FileRequestsManager.processReceivedRequests(content);
            }

            // downloaded file was processed, store slot
            IndexSlotsStorage.inst().storeSlot(gis);
        }
    }

    @Override
    public void run() {

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        // 2 times after startup we download full backload, then only 1 day backward
        int downloadFullBackloadCount = 2;

        while( true ) {

            // +1 for today
            int downloadBack;
            if( downloadFullBackloadCount > 0 ) {
                downloadBack = 1 + Core.frostSettings.getIntValue(SettingsClass.MAX_FILELIST_DOWNLOAD_DAYS);
                downloadFullBackloadCount--;
            } else {
                downloadBack = 2; // today and yesterday only
            }

            try {
                final LocalDate nowDate = new LocalDate(DateTimeZone.UTC);
                for (int i=0; i < downloadBack; i++) {
                    boolean isForToday;
                    if( i == 0 ) {
                        isForToday = true; // upload own keys today only
                    } else {
                        isForToday = false;
                    }

                    final LocalDate localDate = nowDate.minusDays(i);
                    final String dateStr = DateFun.FORMAT_DATE.print(localDate);
                    final long date = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();

                    final IndexSlot gis = IndexSlotsStorage.inst().getSlotForDate(
                            IndexSlotsStorage.REQUESTS, date);

                    if( Logging.inst().doLogFilebaseMessages() ) {
                        System.out.println("FileRequestsThread: starting download for "+dateStr);
                    }
                    // download file pointer files for this date
                    if( !isInterrupted() ) {
                        downloadDate(dateStr, gis, isForToday);
                    }

                    // for today, maybe upload a file pointer file
                    if( !isInterrupted() && isForToday ) {
                        try {
                            if( Logging.inst().doLogFilebaseMessages() ) {
                                System.out.println("FileRequestsThread: starting upload for "+dateStr);
                            }
                            uploadRequestFile(dateStr, gis);
                        } catch(final Throwable t) {
                            logger.log(Level.SEVERE, "Exception catched during uploadRequestFile()", t);
                        }
                    }

                    if( isInterrupted() ) {
                        break;
                    }
                }
            } catch (final Throwable e) {
                logger.log(Level.SEVERE, "Exception catched", e);
                occuredExceptions++;
            }

            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FileRequestsThread because of too much exceptions");
                break;
            }
            if( isInterrupted() ) {
                break;
            }

            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("FileRequestsThread: sleeping 10 minutes");
            }
            Mixed.wait(sleepTime); // sleep 10 minutes
        }
    }
}
