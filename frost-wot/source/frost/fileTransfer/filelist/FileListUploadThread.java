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
package frost.fileTransfer.filelist;

import java.io.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.fileTransfer.*;
import frost.fileTransfer.filelist.*;
import frost.storage.perst.*;
import frost.util.*;
import frost.util.Logging;

/**
 * Thread that uploads the CHK file lists.
 * Periodically checks if there are CHKs pending for send, collects and sends them.
 */
public class FileListUploadThread extends Thread {

    private static final Logger logger = Logger.getLogger(FileListUploadThread.class.getName());

    private final int minutes6 = 6 * 60 * 1000;

    private long nextStartTime = 0;

    // one and only instance
    private static FileListUploadThread instance = new FileListUploadThread();

    private FileListUploadThread() {
        nextStartTime = System.currentTimeMillis() + (5L * 60L * 1000L); // wait 5 minutes until first start
    }

    public static FileListUploadThread getInstance() {
        return instance;
    }

    public boolean cancelThread() {
        return false;
    }

    /**
     * User changed data in shared files table, wait 10 minutes starting from now,
     * maybe he does more changes.
     */
    public void userActionOccured() {
        synchronized(instance) {
            nextStartTime = System.currentTimeMillis() + getRandomWaittime();
        }
    }

    private int getRandomWaittime() {
        // at least 6 minutes, or max. 12 minutes
        final int sleepTime = minutes6 + (int)(minutes6 * Math.random());
        return sleepTime;
    }

    @Override
    public void run() {

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        while( true ) {
            try {
                while(true) {
                    // wait until we really reached nextStartTime, nextStartTime may be changed during our wait
                    final int waitTimeDelta = (int)(nextStartTime - System.currentTimeMillis());
                    if( waitTimeDelta > 1000 ) {
                        Mixed.wait( waitTimeDelta );
                    } else {
                        break;
                    }
                }

                // check for sharedfiles to upload for one identity
                final FileListManagerFileInfo fileInfo = FileListManager.getFilesToSend();
                if( fileInfo != null ) {
                    final File targetFile = FileAccess.createTempFile("flFile_", ".xml.tmp");
                    targetFile.deleteOnExit();

                    final FileListFileContent content = new FileListFileContent(
                            System.currentTimeMillis(),
                            fileInfo.getOwner(),
                            fileInfo.getFiles());

                    if( !FileListFile.writeFileListFile(content, targetFile) ) {
                        logger.log(Level.SEVERE, "Could'nt write the filelist xml file");
                    } else {
                        // upload file
                        if( Logging.inst().doLogFilebaseMessages() ) {
                            System.out.println("FileListUploadThread: starting upload of files: "+fileInfo.getFiles().size());
                        }
                        String chkKey = null;
                        try {
                            final FcpResultPut result = FcpHandler.inst().putFile(
                                    FcpHandler.TYPE_FILE,
                                    "CHK@",
                                    targetFile,
                                    false);

                            if (result.isSuccess() || result.isKeyCollision()) {
                                chkKey = result.getChkKey();
                            }
                        } catch (final Exception ex) {
                            logger.log(Level.WARNING, "Exception catched",ex);
                        }
                        if( Logging.inst().doLogFilebaseMessages() ) {
                            System.out.println("FileListUploadThread: upload finished, key: "+chkKey);
                        }
                        if( chkKey != null ) {
                            // add chk to chklist so the PointerThread can find it
                            final SharedFilesCHKKey key = new SharedFilesCHKKey(chkKey);
                            SharedFilesCHKKeyManager.addNewCHKKeyToSend(key);

                            // mark uploaded files in sharedfiles
                            FileListManager.updateFileListWasSuccessfullySent(fileInfo.getFiles());
                        }
                    }

                    // delete tmp file
                    targetFile.delete();
                }

                // randomize, a fix waittime between uploaded CHK timestamps could de-anonymize us!
                final int sleepTime = getRandomWaittime();
                nextStartTime = System.currentTimeMillis() + sleepTime;

            } catch(final Throwable t) {
                logger.log(Level.SEVERE, "Exception catched",t);
                occuredExceptions++;
            }
            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FileListUploadThread because of too much exceptions");
                break;
            }
        }
    }
}
