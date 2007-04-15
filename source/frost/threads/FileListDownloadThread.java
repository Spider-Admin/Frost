/*
  FileListThread.java / Frost
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
import frost.fcp.FcpHandler;
import frost.fileTransfer.*;
import frost.transferlayer.*;
import frost.util.*;

/**
 * Thread that downloads the CHK file lists.
 * The Thread monitors a queue with CHKs to download and downloads them.
 * 
 * @pattern: Singleton
 */
public class FileListDownloadThread extends Thread {
    
    private static final Logger logger = Logger.getLogger(FileListDownloadThread.class.getName());
    
    private static final int wait1minute = 1 * 60 * 1000;
    
    private CHKKeyQueue keyQueue = new CHKKeyQueue();

    // one and only instance
    private static FileListDownloadThread instance = new FileListDownloadThread();
    
    private FileListDownloadThread() {
    }
    
    public static FileListDownloadThread getInstance() {
        return instance;
    }
    
    public boolean cancelThread() {
        return false;
    }

    public void run() {
        
        initializeQueue();
        
        // monitor and process downloads
        // we expect an appr. chk file size of 512kb, max. 768kb (because of 0.5, we want no splitfile there)

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;
        String previousKey = null;

        while(true) {
            try {
                // if there is no work in queue this call waits for a new queue item
                String chkKey = keyQueue.getKeyFromQueue();
                
                if( chkKey == null ) {
                    // paranoia
                    logger.log(Level.WARNING, "FileListDownloadThread: waiting 1 minute, chkKey=null");
                    Mixed.wait(wait1minute);
                    continue;
                } else if( previousKey != null && previousKey.equals(chkKey) ) {
                    // same key as before, so no more keys else in queue. wait some time longer...
                    logger.log(Level.WARNING, "FileListDownloadThread: waiting 1 minute, same key as before");
                    Mixed.wait(wait1minute);
                } else {
                    // short wait to not to hurt node
                    Mixed.waitRandom(1500);
                    previousKey = chkKey; // different key as before, remember
                }
                logger.log(Level.WARNING, "FileListDownloadThread: starting download of key: "+chkKey);

                GlobalFileDownloaderResult result = GlobalFileDownloader.downloadFile(chkKey, FcpHandler.MAX_KSK_SIZE_ON_07);

                if( result == null || result.getResultFile() == null ) {
                    // download failed
                    boolean retryDownload = SharedFilesCHKKeyManager.updateCHKKeyDownloadFailed(chkKey);
                    logger.log(Level.WARNING, "FileListDownloadThread: download failed, key="+chkKey+"; retry="+retryDownload);
                    if( retryDownload ) {
                        keyQueue.appendKeyToQueue(chkKey);
                    }
                    continue;
                }

                logger.log(Level.WARNING, "FileListDownloadThread: download successful, key="+chkKey);

                // download successful, read file and validate
                File downloadedFile = result.getResultFile();
                
                FileListFileContent content = FileListFile.readFileListFile(downloadedFile);
                boolean isValid = FileListManager.processReceivedFileList(content);
                logger.log(Level.WARNING, "FileListDownloadThread: processed results, isValid="+isValid);
                downloadedFile.delete();
                SharedFilesCHKKeyManager.updateCHKKeyDownloadSuccessful(chkKey, isValid);
                
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception catched",t);
                occuredExceptions++;
            }
            
            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FileListUploadThread because of too much exceptions");
                break;
            }
        }
    }
    
    private void initializeQueue() {
        // get all waiting keys from database
        List<String> keys = SharedFilesCHKKeyManager.getCHKKeyStringsToDownload();
        if( keys == null ) {
            return;
        }
        for(String chk : keys ) {
            keyQueue.initialAppendKeyToQueue(chk);
        }
    }
    
    public void enqueueNewKey(String key) {
        // key was already added to database!
        keyQueue.appendKeyToQueue(key);
    }
    
    private class CHKKeyQueue {
        
        private LinkedList<String> queue = new LinkedList<String>();
        // FIXME: first return all keys not older than 3 days, then all older keys ???
        public synchronized String getKeyFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    logger.log(Level.WARNING, "CHKKeyQueue: Waiting for work, queue length="+getQueueSize());
                    wait();
                }
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "CHKKeyQueue: NO key returned(1), queue length="+getQueueSize());
                return null; // waiting abandoned
            }
            
            if( queue.isEmpty() == false ) {
                String key = queue.removeFirst();
                logger.log(Level.WARNING, "CHKKeyQueue: Key returned, new queue length="+getQueueSize());
                return key;
            }
            logger.log(Level.WARNING, "CHKKeyQueue: NO key returned(2), queue length="+getQueueSize());
            return null;
        }

        public synchronized void initialAppendKeyToQueue(String key) {
            queue.addLast(key);
            notifyAll(); // notify all waiters (if any) of new record
        }

        public synchronized void appendKeyToQueue(String key) {
            queue.addLast(key);
            logger.log(Level.WARNING, "CHKKeyQueue: Key appended, new queue length="+getQueueSize());
            notifyAll(); // notify all waiters (if any) of new record
        }
        
        public synchronized int getQueueSize() {
            return queue.size();
        }
    }
}
