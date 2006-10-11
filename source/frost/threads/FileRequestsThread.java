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

import frost.*;
import frost.fileTransfer.*;
import frost.storage.database.applayer.*;
import frost.transferlayer.*;
import frost.util.*;

/**
 * Thread receives and sends file requests periodically.
 * Runs forever and sleeps between loops.
 */
public class FileRequestsThread extends Thread {

    private static Logger logger = Logger.getLogger(FilePointersThread.class.getName());
    
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
    private boolean uploadRequestFile(String dateStr, java.sql.Date sqlDate) throws Throwable {

        // get a list of CHK keys to send
        List fileRequests = FileRequestsManager.getRequestsToSend();
System.out.println("uploadRequestFile: fileRequests to send: "+fileRequests.size());
        if( fileRequests == null || fileRequests.size() == 0 ) {
            logger.info("No requests to send.");
            return true;
        }
        
        FileRequestFileContent content = new FileRequestFileContent(System.currentTimeMillis(), fileRequests); 

        // write a file with requests to a tempfile
        File tmpRequestFile = FileAccess.createTempFile("filereq_", "_xml");
        if( !FileRequestFile.writeRequestFile(content, tmpRequestFile) ) {
            logger.severe("Error writing the file requests file.");
            return false;
        }

        // Wait some random time to not to flood the node
        Mixed.waitRandom(3000);

        logger.info("Starting upload of request file containing "+fileRequests.size()+" SHAs");
System.out.println("uploadRequestFile: Starting upload of request file containing "+fileRequests.size()+" SHAs");
        
        String insertKey = keyPrefix + dateStr + "-";
        boolean wasOk = GlobalFileUploader.uploadFile(indexSlots, sqlDate, tmpRequestFile, insertKey, ".xml");
        tmpRequestFile.delete();
System.out.println("uploadRequestFile: upload finished, wasOk="+wasOk);
        if( wasOk ) {
            FileRequestsManager.updateRequestsWereSuccessfullySent(fileRequests);
        }
        return wasOk;
    }
    
    private void downloadDate(String dateStr, java.sql.Date sqlDate, boolean isForToday) throws Throwable {
        
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
            File downloadedFile = GlobalFileDownloader.downloadFile(downKey); 
            if(  downloadedFile == null ) {
                // download failed. 
                failures++;
                // next loop we try next index
                index = indexSlots.findNextDownloadSlot(index, sqlDate);
                continue;
            }
            
            // download was successful, mark it
            indexSlots.setDownloadSlotUsed(index, sqlDate);
            // next loop we try next index
            index = indexSlots.findNextDownloadSlot(index, sqlDate);
            failures = 0;
            
            FileRequestFileContent content = FileRequestFile.readRequestFile(downloadedFile);
            FileRequestsManager.processReceivedRequests(content);
        }
    }

    public void run() {

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        while( true ) {
            
            // +1 for today
            final int downloadBack = 1 + Core.frostSettings.getIntValue(SettingsClass.MAX_FILELIST_DOWNLOAD_DAYS);
            try {
                for (int i=0; i < downloadBack; i++) {
                    boolean isForToday;
                    if( i == 0 ) {
                        isForToday = true; // upload own keys today only
                    } else {
                        isForToday = false;
                    }
                    
                    String dateStr = DateFun.getDate(i);
                    java.sql.Date sqlDate = DateFun.getSqlDateGMTDaysAgo(i);
System.out.println("FileRequestsThread: starting download for "+dateStr);
                    // download file pointer files for this date
                    if( !isInterrupted() ) {
                        downloadDate(dateStr, sqlDate, isForToday);
                    }
                    
                    // for today, maybe upload a file pointer file
                    if( !isInterrupted() && isForToday ) {
                        try {
System.out.println("FileRequestsThread: starting upload for "+dateStr);
                            uploadRequestFile(dateStr, sqlDate);
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
