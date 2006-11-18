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
import frost.util.*;

public class DownloadThread extends Thread {

    private DownloadTicker ticker;

    private static Logger logger = Logger.getLogger(DownloadThread.class.getName());

    public static final String KEYCOLL_INDICATOR = "ERROR: key collision";

    private String filename;
    private Long size;
    private String key;

    private FrostDownloadItem downloadItem;
//    private DownloadModel downloadModel;
    
    public DownloadThread(DownloadTicker newTicker, FrostDownloadItem item, DownloadModel model) {
        filename = item.getFileName();
        size = item.getFileSize();
        key = item.getKey();
        ticker = newTicker;
        downloadItem = item;
//        downloadModel = model;
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
            FcpResultGet result = null;

            try {
                result = FcpHandler.inst().getFile(
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

//            // file might be erased from table during download...
//            boolean inTable = false;
//            for (int x = 0; x < downloadModel.getItemCount(); x++) {
//                FrostDownloadItem actItem = (FrostDownloadItem) downloadModel.getItemAt(x);
//                if (actItem.getKey() != null && actItem.getKey().equals(downloadItem.getKey())) {
//                    inTable = true;
//                    break;
//                }
//            }

            if (result == null || result.isSuccess() == false) {
                // download failed
                if( result != null
                        && result.getReturnCode() == 11 
                        && key.startsWith("CHK@")
                        && key.indexOf("/") > 0 ) 
                {
                    // remove filename from CHK, store new key in db
                    String plainKey = key.substring(0, key.indexOf("/"));
                    downloadItem.setKey(plainKey);
                    downloadItem.setLastDownloadStopTime(0);
                    
                    System.out.println("*!*!* Removed filename from key: "+key+" ; "+plainKey);
                    
                } else if( result != null && result.isFatal() ) {
                    // fatal, don't retry
                    downloadItem.setEnableDownload(Boolean.valueOf(false));
                    downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                    logger.warning("FILEDN: Download of " + filename + " failed FATALLY.");
                } else {
                    downloadItem.setRetries(downloadItem.getRetries() + 1);
    
                    logger.warning("FILEDN: Download of " + filename + " failed.");
                    // set new state -> failed or waiting for another try
                    if (downloadItem.getRetries() > Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_RETRIES)) {
                        downloadItem.setEnableDownload(Boolean.valueOf(false));
                        downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                    } else {
                        downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                    }
                }
                if( result != null ) {
                    downloadItem.setErrorCodeDescription(result.getCodeDescription());
                }
            } else {
                // download successful
                downloadItem.setFileSize(new Long(newFile.length()));
                downloadItem.setState(FrostDownloadItem.STATE_DONE);
                downloadItem.setEnableDownload(Boolean.valueOf(false));

                // update lastDownloaded time in filelist
                if( downloadItem.isSharedFile() ) {
                    AppLayerDatabase.getFileListDatabaseTable().updateFrostFileListFileObjectAfterDownload(
                            downloadItem.getFileListFileObject().getSha(),
                            System.currentTimeMillis() );
                }

                // maybe log successful download to file localdata/downloads.txt
                if( Core.frostSettings.getBoolValue(SettingsClass.LOG_DOWNLOADS_ENABLED) ) {
                    String line = downloadItem.getKey() + "/" + downloadItem.getFileName();
                    String fileName = Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA) + "Frost-Downloads.log";
                    File targetFile = new File(fileName);
                    FileAccess.appendLineToTextfile(targetFile, line);
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
