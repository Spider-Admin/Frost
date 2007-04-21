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
package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.joda.time.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.storage.database.applayer.*;
import frost.transferlayer.*;
import frost.util.*;

/**
 * Thread receives and sends file requests periodically.
 * Runs forever and sleeps between loops.
 */
public class FileRequestsThread extends Thread {

    private static final Logger logger = Logger.getLogger(FilePointersThread.class.getName());
    
    // sleeptime between request loops
    private static final int sleepTime = 10 * 60 * 1000;

    private GlobalIndexSlotsDatabaseTable indexSlots;
    private String keyPrefix;
    
    // one and only instance
    private static FileRequestsThread instance = new FileRequestsThread();
    
    private FileRequestsThread() {
        indexSlots = new GlobalIndexSlotsDatabaseTable(GlobalIndexSlotsDatabaseTable.REQUESTS);
        
        String fileBase = Core.frostSettings.getValue(SettingsClass.FILE_BASE);
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
    private boolean uploadRequestFile(String dateStr, long sqlDate) throws Throwable {

        // get a list of CHK keys to send
        List<String> fileRequests = FileRequestsManager.getRequestsToSend();
System.out.println("uploadRequestFile: fileRequests to send: "+fileRequests.size());
        if( fileRequests == null || fileRequests.size() == 0 ) {
            logger.info("No requests to send.");
            return true;
        }
        
        FileRequestFileContent content = new FileRequestFileContent(System.currentTimeMillis(), fileRequests); 

        // write a file with requests to a tempfile
        File tmpRequestFile = FileAccess.createTempFile("filereq_", ".xml");
        if( !FileRequestFile.writeRequestFile(content, tmpRequestFile) ) {
            logger.severe("Error writing the file requests file.");
            return false;
        }

        // Wait some random time to not to flood the node
        Mixed.waitRandom(3000);

        logger.info("Starting upload of request file containing "+fileRequests.size()+" SHAs");
System.out.println("uploadRequestFile: Starting upload of request file containing "+fileRequests.size()+" SHAs");
        
        String insertKey = keyPrefix + dateStr + "-";
        boolean wasOk = GlobalFileUploader.uploadFile(indexSlots, sqlDate, tmpRequestFile, insertKey, ".xml", true);
        tmpRequestFile.delete();
System.out.println("uploadRequestFile: upload finished, wasOk="+wasOk);
        if( wasOk ) {
            FileRequestsManager.updateRequestsWereSuccessfullySent(fileRequests);
        }
        return wasOk;
    }
    
    private void downloadDate(String dateStr, long sqlDate, boolean isForToday) throws Throwable {
        
        // "KSK@frost/filerequests/2006.11.1-<index>.xml"
        String requestKey = keyPrefix + dateStr + "-"; 

        int maxFailures;
        if (isForToday) {
            maxFailures = 3; // skip a maximum of 2 empty slots for today
        } else {
            maxFailures = 2; // skip a maximum of 1 empty slot for backload
        }
        int index = indexSlots.findFirstDownloadSlot(sqlDate);
        int failures = 0;

        while (failures < maxFailures && index >= 0 ) {

            // Wait some random time to not to flood the node
            Mixed.waitRandom(3000);
            
            logger.info("Requesting index " + index + " for date " + dateStr);

            String downKey = requestKey + index + ".xml";
            GlobalFileDownloaderResult result = GlobalFileDownloader.downloadFile(downKey, FcpHandler.MAX_MESSAGE_SIZE_07);

            if( result == null ) {
                // download failed. 
                failures++;
                // next loop we try next index
                index = indexSlots.findNextDownloadSlot(index, sqlDate);
                continue;
            }
            
            failures = 0;

            if( result.getErrorCode() == GlobalFileDownloaderResult.ERROR_EMPTY_REDIRECT ) {
                // try index again later
                System.out.println("FileRequestsThread.downloadDate: Skipping index "+index+" for now, will try again later.");
                // next loop we try next index
                index = indexSlots.findNextDownloadSlot(index, sqlDate);
                continue;
            }

            // downloaded something, mark it
            indexSlots.setDownloadSlotUsed(index, sqlDate);
            // next loop we try next index
            index = indexSlots.findNextDownloadSlot(index, sqlDate);

            if( result.getErrorCode() == GlobalFileDownloaderResult.ERROR_FILE_TOO_BIG ) {
                System.out.println("FileRequestsThread.downloadDate: Dropping index "+index+", FILE_TOO_BIG.");
                continue;
            }

            File downloadedFile = result.getResultFile(); 

            FileRequestFileContent content = FileRequestFile.readRequestFile(downloadedFile);
            downloadedFile.delete();
            FileRequestsManager.processReceivedRequests(content);
        }
    }

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
                LocalDate nowDate = new LocalDate(DateTimeZone.UTC);
                for (int i=0; i < downloadBack; i++) {
                    boolean isForToday;
                    if( i == 0 ) {
                        isForToday = true; // upload own keys today only
                    } else {
                        isForToday = false;
                    }
                    
                    LocalDate localDate = nowDate.minusDays(i);
                    String dateStr = DateFun.FORMAT_DATE.print(localDate);
                    long date = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();
                    
//                    String dateStr = DateFun.getDate(i);
//                    java.sql.Date sqlDate = DateFun.getSqlDateGMTDaysAgo(i);
                    
System.out.println("FileRequestsThread: starting download for "+dateStr);
                    // download file pointer files for this date
                    if( !isInterrupted() ) {
                        downloadDate(dateStr, date, isForToday);
                    }
                    
                    // for today, maybe upload a file pointer file
                    if( !isInterrupted() && isForToday ) {
                        try {
System.out.println("FileRequestsThread: starting upload for "+dateStr);
                            uploadRequestFile(dateStr, date);
                        } catch(Throwable t) {
                            logger.log(Level.SEVERE, "Exception catched during uploadRequestFile()", t);
                        }
                    }
                    
                    if( isInterrupted() ) {
                        break;
                    }
                }
            } catch (Throwable e) {
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
System.out.println("FileRequestsThread: sleeping 10 minutes");            
            Mixed.wait(sleepTime); // sleep 10 minutes
        }
            
        indexSlots.close();
    }
}
