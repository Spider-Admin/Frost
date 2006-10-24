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

import frost.*;
import frost.fcp.*;
import frost.storage.database.applayer.*;

public class DownloadThread extends Thread {

    private DownloadTicker ticker;

    private static Logger logger = Logger.getLogger(DownloadThread.class.getName());

    public static final String KEYCOLL_INDICATOR = "ERROR: key collision";

    private String filename;
    private Long size;
    private String key;

    private FrostDownloadItem downloadItem;
    private DownloadModel downloadModel;
    
    public DownloadThread(DownloadTicker newTicker, FrostDownloadItem item, DownloadModel model) {
        filename = item.getFileName();
        size = item.getFileSize();
        key = item.getKey();
        ticker = newTicker;
        downloadItem = item;
        downloadModel = model;
    }

    public void run() {
        ticker.threadStarted();
        try {
            File newFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + filename);

            // if we don't have the CHK, we should not be here
            if (key == null) {
                ticker.threadFinished();
                return;
            }

            // otherwise, proceed as usual
            logger.info("FILEDN: Download of '" + filename + "' started.");

            // Download file
            FcpResultGet success = null;

            try {
                success = FcpHandler.inst().getFile(
                            FcpHandler.TYPE_FILE,
                            key,
                            size,
                            newFile,
                            true, // doRedirect
                            false, // fastDownload
                            false, // createTempFile
                            downloadItem);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception thrown in run()", t);
            }

            // file might be erased from table during download...
            boolean inTable = false;
            for (int x = 0; x < downloadModel.getItemCount(); x++) {
                FrostDownloadItem actItem = (FrostDownloadItem) downloadModel.getItemAt(x);
                if (actItem.getKey() != null && actItem.getKey().equals(downloadItem.getKey())) {
                    inTable = true;
                    break;
                }
            }

            // download failed
            if (success == null) {
                downloadItem.setRetries(downloadItem.getRetries() + 1);

                logger.warning("FILEDN: Download of " + filename + " failed.");
                if (inTable == true) {
                    // set new state -> failed or waiting for another try
                    if (downloadItem.getRetries() > Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_RETRIES)) {
                        downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                    } else {
                        downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                    }
                }
            }
            // download successfull
            else {
                downloadItem.setFileSize(new Long(newFile.length()));
                downloadItem.setState(FrostDownloadItem.STATE_DONE);
                downloadItem.setEnableDownload(Boolean.valueOf(false));

                // update lastDownloaded time in filelist
                if( downloadItem.isSharedFile() ) {
                    AppLayerDatabase.getFileListDatabaseTable().updateFrostFileListFileObjectAfterDownload(
                            downloadItem.getFileListFileObject().getSha(),
                            System.currentTimeMillis() );
                }

                logger.info("FILEDN: Download of " + filename + " was successful.");
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in requestThread.run", t);
        }

        downloadItem.setLastDownloadStopTime(System.currentTimeMillis());
        ticker.threadFinished();
    }
}
