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

import frost.fcp.*;
import frost.fileTransfer.*;
import frost.transferlayer.*;
import frost.util.*;
import frost.util.Logging;

/**
 * Thread that downloads the CHK file lists.
 * The Thread monitors a queue with CHKs to download and downloads them.
 *
 * @pattern: Singleton
 */
public class FileListDownloadThread extends Thread {

    private static final Logger logger = Logger.getLogger(FileListDownloadThread.class.getName());

    private static final int wait1minute = 1 * 60 * 1000;

    private final CHKKeyQueue keyQueue = new CHKKeyQueue();

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

    @Override
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
                final String chkKey = keyQueue.getKeyFromQueue();

                if( chkKey == null ) {
                    // paranoia
                    if( Logging.inst().doLogFilebaseMessages() ) {
                        System.out.println("FileListDownloadThread: waiting 1 minute, chkKey=null");
                    }
                    Mixed.wait(wait1minute);
                    continue;
                } else if( previousKey != null && previousKey.equals(chkKey) ) {
                    // same key as before, so no more keys else in queue. wait some time longer...
                    if( Logging.inst().doLogFilebaseMessages() ) {
                        System.out.println("FileListDownloadThread: waiting 1 minute, same key as before");
                    }
                    Mixed.wait(wait1minute);
                } else {
                    // short wait to not to hurt node
                    Mixed.waitRandom(2000);
                    previousKey = chkKey; // different key as before, remember
                }
                if( Logging.inst().doLogFilebaseMessages() ) {
                    System.out.println("FileListDownloadThread: starting download of key: "+chkKey);
                }

                final GlobalFileDownloaderResult result = GlobalFileDownloader.downloadFile(chkKey, FcpHandler.MAX_FILELIST_SIZE_07, -1);

                if( result == null || result.getResultFile() == null ) {
                    // download failed
                    final boolean retryDownload = SharedFilesCHKKeyManager.updateCHKKeyDownloadFailed(chkKey);
                    if( Logging.inst().doLogFilebaseMessages() ) {
                        System.out.println("FileListDownloadThread: download failed, key="+chkKey+"; retry="+retryDownload);
                    }
                    if( retryDownload ) {
                        keyQueue.appendKeyToQueue(chkKey);
                    }
                    continue;
                }

                if( Logging.inst().doLogFilebaseMessages() ) {
                    System.out.println("FileListDownloadThread: download successful, key="+chkKey);
                }

                // download successful, read file and validate
                final File downloadedFile = result.getResultFile();

                FileListFileContent content = null;
                try {
                    content = FileListFile.readFileListFile(downloadedFile);
                } catch (final Exception e) {
                    logger.log(Level.WARNING, "Invalid XML content: "+e.getMessage());
                }
                // content==null -> isValid=false
                final boolean isValid = FileListManager.processReceivedFileList(content);

                if( Logging.inst().doLogFilebaseMessages() ) {
                    System.out.println("FileListDownloadThread: processed results, isValid="+isValid);
                }

                final long timestamp;
                if( content == null ) {
                    // invalid content, use current timestamp, chk will never be spreaded
                    timestamp = System.currentTimeMillis();
                } else {
                    timestamp = content.getTimestamp();
                }

                downloadedFile.delete();
                SharedFilesCHKKeyManager.updateCHKKeyDownloadSuccessful(chkKey, timestamp, isValid);

            } catch(final Throwable t) {
                logger.log(Level.SEVERE, "Exception catched",t);
                occuredExceptions++;
            }

            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FileListDownloadThread because of too much exceptions");
                break;
            }
        }
    }

    private void initializeQueue() {
        // get all waiting keys from database
        final List<String> keys = SharedFilesCHKKeyManager.getCHKKeyStringsToDownload();
        if( keys == null ) {
            return;
        }
        for(final String chk : keys ) {
            keyQueue.initialAppendKeyToQueue(chk);
        }
    }

    public void enqueueNewKey(final String key) {
        // key was already added to database!
        keyQueue.appendKeyToQueue(key);
    }

    public int getCHKKeyQueueSize() {
        return keyQueue.getQueueSize();
    }

    private class CHKKeyQueue {

        private final LinkedList<String> queue = new LinkedList<String>();
        // FIXME: first return all keys not older than 3 days, then all older keys ???
        public synchronized String getKeyFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    if( Logging.inst().doLogFilebaseMessages() ) {
                        System.out.println("CHKKeyQueue: Waiting for work, queue length="+getQueueSize());
                    }
                    wait();
                }
            } catch (final InterruptedException e) {
                if( Logging.inst().doLogFilebaseMessages() ) {
                    System.out.println("CHKKeyQueue: NO key returned(1), queue length="+getQueueSize());
                }
                return null; // waiting abandoned
            }

            if( queue.isEmpty() == false ) {
                final String key = queue.removeFirst();
                if( Logging.inst().doLogFilebaseMessages() ) {
                    System.out.println("CHKKeyQueue: Key returned, new queue length="+getQueueSize());
                }
                return key;
            }
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("CHKKeyQueue: NO key returned(2), queue length="+getQueueSize());
            }
            return null;
        }

        public synchronized void initialAppendKeyToQueue(final String key) {
            queue.addLast(key);
            notifyAll(); // notify all waiters (if any) of new record
        }

        public synchronized void appendKeyToQueue(final String key) {
            queue.addLast(key);
            if( Logging.inst().doLogFilebaseMessages() ) {
                System.out.println("CHKKeyQueue: Key appended, new queue length="+getQueueSize());
            }
            notifyAll(); // notify all waiters (if any) of new record
        }

        public synchronized int getQueueSize() {
            return queue.size();
        }
    }
}
