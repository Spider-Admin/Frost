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
import frost.gui.objects.*;

public class DownloadThread extends Thread {

    private SettingsClass settings;

    private DownloadTicker ticker;

    private static Logger logger = Logger.getLogger(DownloadThread.class.getName());

    public static final String KEYCOLL_INDICATOR = "ERROR: key collision";

    private String filename;
    private Long size;
    private String key;
    private Board board;

    private FrostDownloadItem downloadItem;
    private DownloadModel downloadModel;

    public void run() {
        ticker.threadStarted();
        try {
            File newFile = new File(settings.getValue("downloadDirectory") + filename);

            // if we don't have the CHK, means the key was not inserted. request it by SHA1.
            System.out.println("key="+key);
            if (key == null) {
                maybeDoRequest();
                downloadItem.setLastDownloadStopTimeMillis(System.currentTimeMillis());
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
                    // Upload request to request stack
                    if (settings.getBoolValue("downloadEnableRequesting")
                        && downloadItem.getRetries() >= settings.getIntValue("downloadRequestAfterTries")
                        && board != null
                        && board.isFolder() == false)
                    {
                        logger.info("FILEDN: Download failed, requesting file " + filename);

                        downloadItem.setState(FrostDownloadItem.STATE_REQUESTING);
                    } else {
                        logger.info("FILEDN: Download failed (file is NOT requested).");
                    }

                    // set new state -> failed or waiting for another try
                    if (downloadItem.getRetries()
                        > settings.getIntValue("downloadMaxRetries")) {
                        if (settings.getBoolValue("downloadRestartFailedDownloads")) {
                            downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                            downloadItem.setRetries(0);
                        } else {
                            downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                        }
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

                logger.info("FILEDN: Download of " + filename + " was successful.");
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in requestThread.run", t);
        }

        downloadItem.setLastDownloadStopTimeMillis(System.currentTimeMillis());
        ticker.threadFinished();
    }
    
    private void maybeDoRequest() {
        // check if we already requested this file today
        if( downloadItem.getLastRequestedDate() != null ) {
            if( !downloadItem.getLastRequestedDate().before(DateFun.getCurrentSqlDateGMT()) ) {
                // already requested today
                return;
            } 
        }
        logger.info("FILEDN: Requesting " + filename);
        downloadItem.setState(FrostDownloadItem.STATE_REQUESTING);
    }

    /**Constructor*/
    public DownloadThread(
        DownloadTicker newTicker,
        FrostDownloadItem item,
        DownloadModel model,
        SettingsClass frostSettings) {

        settings = frostSettings;
        filename = item.getFileName();
        size = item.getFileSize();
        key = item.getKey();
        board = item.getSourceBoard();
        ticker = newTicker;
        downloadItem = item;
        downloadModel = model;
    }
}
