/*
  FileTransferInformation.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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

/**
 * A short live object that holds the count of running/pending uploads and downloads.
 * Created and filled by the FileTransferManager, used from with ticker in MainFrame
 * to update status bar and panels. 
 */
public class FileTransferInformation {
    
    private int uploadsRunning = 0;
    private int uploadsWaiting = 0;

    private int downloadsRunning = 0;
    private int downloadsWaiting = 0;
    
    private int fileListDownloadQueueSize = 0;
    
    public int getUploadsRunning() {
        return uploadsRunning;
    }
    public void setUploadsRunning(int uploadsRunning) {
        this.uploadsRunning = uploadsRunning;
    }
    public int getUploadsWaiting() {
        return uploadsWaiting;
    }
    public void setUploadsWaiting(int uploadsWaiting) {
        this.uploadsWaiting = uploadsWaiting;
    }
    public int getDownloadsRunning() {
        return downloadsRunning;
    }
    public void setDownloadsRunning(int downloadsRunning) {
        this.downloadsRunning = downloadsRunning;
    }
    public int getDownloadsWaiting() {
        return downloadsWaiting;
    }
    public void setDownloadsWaiting(int downloadsWaiting) {
        this.downloadsWaiting = downloadsWaiting;
    }
    public int getFileListDownloadQueueSize() {
        return fileListDownloadQueueSize;
    }
    public void setFileListDownloadQueueSize(int fileListDownloadQueueSize) {
        this.fileListDownloadQueueSize = fileListDownloadQueueSize;
    }
}
