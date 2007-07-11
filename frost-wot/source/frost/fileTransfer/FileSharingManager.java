/*
  FileSharingManager.java / Frost
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

import frost.threads.*;

/**
 * This class is the manager for the whole filesharing.
 * Starts and stops the filesharing threads.
 */
public class FileSharingManager {

    private static FileListDownloadThread fileListDownloadThread = null;
    private static FileListUploadThread fileListUploadThread = null;
    private static FileRequestsThread fileRequestsThread = null;
    private static FilePointersThread filePointersThread = null;
    
    public static boolean startFileSharing() {
        // start filesharing threads
        filePointersThread = FilePointersThread.getInstance();
        filePointersThread.start();
        
        fileListDownloadThread = FileListDownloadThread.getInstance();
        fileListDownloadThread.start();

        fileListUploadThread = FileListUploadThread.getInstance();
        fileListUploadThread.start();
        
        fileRequestsThread = FileRequestsThread.getInstance();
        fileRequestsThread.start();
        
        return true;
    }
    
    public static int getFileListDownloadQueueSize() {
        if( fileListDownloadThread == null ) {
            return 0;
        }
        return fileListDownloadThread.getCHKKeyQueueSize();
    }
    
    public static boolean stopFileSharing() {
        // TODO: implement cancelThread() for all threads
        if( filePointersThread != null ) {
            filePointersThread.cancelThread();
            filePointersThread = null;
        }
        if( fileListDownloadThread != null ) {
            fileListDownloadThread.cancelThread();
            fileListDownloadThread = null;
        }
        if( fileListUploadThread != null ) {
            fileListUploadThread.cancelThread();
            fileListUploadThread = null;
        }
        if( fileRequestsThread != null ) {
            fileRequestsThread.cancelThread();
            fileRequestsThread = null;
        }
        
        return true;
    }
}
