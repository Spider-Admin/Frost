/*
  FilePointersThread.java / Frost
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
import frost.storage.perst.*;
import frost.transferlayer.*;
import frost.util.*;

/**
 * This thread downloads the KSK pointer files for file sharing from the public indices.
 * Received KSK files contain CHK keys of the filelists, this CHK keys are inserted into
 * the database (another thread downloads them).
 * When an update is finished, the thread checks if there are pending CHK keys that must be
 * send, and sends them.
 * Finally the thread sleeps for some time and restarts to retrieve the pointer files.
 */
public class FilePointersThread extends Thread {
    
    private static final Logger logger = Logger.getLogger(FilePointersThread.class.getName());
    
    private static final int baseSleepTime = 15 * 60 * 1000;

    private String keyPrefix;

    // one and only instance
    private static FilePointersThread instance = new FilePointersThread();
    
    private FilePointersThread() {
        String fileBase = Core.frostSettings.getValue(SettingsClass.FILE_BASE);
        keyPrefix = "KSK@frost/filepointers/" + fileBase + "-";
    }

    public static FilePointersThread getInstance() {
        return instance;
    }
    
    public boolean cancelThread() {
        return false;
    }

    /**
     * Returns true if no error occured.
     */
    private boolean uploadIndexFile(String dateStr, IndexSlot gis) throws Throwable {

        // get a list of CHK keys to send
        List<SharedFilesCHKKey> sharedFileCHKkeys = SharedFilesCHKKeyManager.getCHKKeysToSend();
        if( sharedFileCHKkeys == null || sharedFileCHKkeys.size() == 0 ) {
            logger.info("FILEDN: No CHK keys to send.");
            return true;
        }

        // write a pointerfile to a tempfile
        List<String> tmpChkStringKeys = new ArrayList<String>(sharedFileCHKkeys.size());
        for( Iterator i = sharedFileCHKkeys.iterator(); i.hasNext(); ) {
            SharedFilesCHKKey ck = (SharedFilesCHKKey) i.next();
            tmpChkStringKeys.add( ck.getChkKey() );
        }
        
        FilePointerFileContent content = new FilePointerFileContent(System.currentTimeMillis(), tmpChkStringKeys);

        File tmpPointerFile = FileAccess.createTempFile("kskptr_", ".xml");
        tmpPointerFile.deleteOnExit();
        if( !FilePointerFile.writePointerFile(content, tmpPointerFile) ) {
            logger.severe("FILEDN: Error writing the KSK pointer file.");
            return false;
        }

        tmpChkStringKeys.clear();
        tmpChkStringKeys = null;

        // Wait some random time to not to flood the node
        Mixed.waitRandom(3000);

        logger.info("FILEDN: Starting upload of pointer file containing "+sharedFileCHKkeys.size()+" CHK keys");
        
        String insertKey = keyPrefix + dateStr + "-";
System.out.println("uploadIndexFile: Starting upload of pointer file containing "+sharedFileCHKkeys.size()+" CHK keys to "+insertKey+"...");
        boolean wasOk = GlobalFileUploader.uploadFile(gis, tmpPointerFile, insertKey, ".xml", true);
System.out.println("uploadIndexFile: upload finished, wasOk="+wasOk);
        tmpPointerFile.delete();
        if( wasOk ) {
            SharedFilesCHKKeyManager.updateCHKKeysWereSuccessfullySent(sharedFileCHKkeys);
        }
        return wasOk;
    }
    
    private void downloadDate(String dateStr, IndexSlot gis, boolean isForToday) throws Throwable {
        
        // "KSK@frost/filelistpointer/2006.11.1-<index>.xml"
        String requestKey = keyPrefix + dateStr + "-"; 

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
            Mixed.waitRandom(3000);

            logger.info("FILEDN: Requesting index " + index + " for date " + dateStr);

            String downKey = requestKey + index + ".xml";
System.out.println("FilePointersThread.downloadDate: requesting: "+downKey);       

            GlobalFileDownloaderResult result = GlobalFileDownloader.downloadFile(downKey, FcpHandler.MAX_MESSAGE_SIZE_07);
            
            if(  result == null ) {
System.out.println("FilePointersThread.downloadDate: failure");
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
                System.out.println("FilePointersThread.downloadDate: Skipping index "+index+" for now, will try again later.");
                // next loop we try next index
                index = gis.findNextDownloadSlot(index);
                continue;
            }

            // downloaded something, mark it
            gis.setDownloadSlotUsed(index);
            gis.modify();
            // next loop we try next index
            index = gis.findNextDownloadSlot(index);
            
            if( result.getErrorCode() == GlobalFileDownloaderResult.ERROR_FILE_TOO_BIG ) {
                System.out.println("FilePointersThread.downloadDate: Dropping index "+index+", FILE_TOO_BIG.");
                continue;
            }

System.out.println("FilePointersThread.downloadDate: success");

            File downloadedFile = result.getResultFile(); 
            
            FilePointerFileContent content = FilePointerFile.readPointerFile(downloadedFile);
System.out.println("readPointerFile: result: "+content);
            downloadedFile.delete();
            SharedFilesCHKKeyManager.processReceivedCHKKeys(content);
        }
        System.out.println("FilePointersThread.downloadDate: finished");        
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
                    
                    IndexSlot gis = IndexSlotsStorage.inst().getSlotForDate(
                            IndexSlotsStorage.FILELISTS, date);
                    
System.out.println("FilePointersThread: download for "+dateStr);                    
                    // download file pointer files for this date
                    if( !isInterrupted() ) {
                        downloadDate(dateStr, gis, isForToday);
                    }
                    
                    // for today, maybe upload a file pointer file
                    if( !isInterrupted() && isForToday ) {
                        try {
System.out.println("FilePointersThread: upload for "+dateStr);                    
                            uploadIndexFile(dateStr, gis);
                        } catch(Throwable t) {
                            logger.log(Level.SEVERE, "Exception during uploadIndexFile()", t);
                        }
                    }
                    
                    IndexSlotsStorage.inst().storeSlot(gis);
                    
                    if( isInterrupted() ) {
                        break;
                    }
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception catched", e);
                occuredExceptions++;
            }
            
            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FilePointersThread because of too much exceptions");
                break;
            }
            if( isInterrupted() ) {
                break;
            }
            
            IndexSlotsStorage.inst().commitStore(); // commit changes for this run
            
            // random sleeptime to anonymize our uploaded pointer files
            Mixed.waitRandom(baseSleepTime); 
        }
    }
}
