/*
  DownloadThread.java / Frost
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
package frost.fileTransfer.download;

import java.io.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.fileTransfer.*;

public class DownloadThread extends Thread {

    private static final Logger logger = Logger.getLogger(DownloadThread.class.getName());

    private final DownloadTicker ticker;
    private final String filename;
    private final Long size;
    private final String key;
    private final File targetFile;

    private final FrostDownloadItem downloadItem;

    public DownloadThread(final DownloadTicker newTicker, final FrostDownloadItem item, final File target) {
        filename = item.getFilename();
        size = item.getFileSize();
        key = item.getKey();
        ticker = newTicker;
        downloadItem = item;
        targetFile = target;
    }

    @Override
    public void run() {
        ticker.threadStarted();

        // if we don't have the CHK, we should not be here
        if (key == null) {
            ticker.threadFinished();
            return;
        }

        try {
            // otherwise, proceed as usual
            logger.info("FILEDN: Download of '" + filename + "' started.");

            // Download file
            FcpResultGet result = null;

            try {
                result = FcpHandler.inst().getFile(
                            FcpHandler.TYPE_FILE,
                            key,
                            size,
                            targetFile,
                            true,  // doRedirect
                            false, // fastDownload
                            -1,    // maxSize
                            -1,    // maxRetries, use default
                            false, // createTempFile
                            downloadItem);
            } catch (final Throwable t) {
                logger.log(Level.SEVERE, "Exception thrown in getFile()", t);
            }

            final boolean retryNow =
                FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(downloadItem, result, targetFile);

            if( retryNow ) {
                FileTransferManager.inst().getDownloadManager().startDownload(downloadItem);
            }

        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in requestThread.run", t);
        }

        ticker.threadFinished();
    }
}
