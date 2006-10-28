/*
  UploadThread.java / Frost
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
package frost.fileTransfer.upload;

import java.io.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.util.*;

class UploadThread extends Thread {
    
    private UploadTicker ticker;

    private static Logger logger = Logger.getLogger(UploadThread.class.getName());

    public static final int MODE_GENERATE_CHK  = 2;
    public static final int MODE_UPLOAD        = 3;

    FrostUploadItem uploadItem = null; // for upload and generate CHK

    protected UploadThread(UploadTicker newTicker, FrostUploadItem ulItem) {

        ticker = newTicker;
        uploadItem = ulItem;
    }

    public void run() {
        ticker.uploadingThreadStarted();
        try {
            upload();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception thrown in run()", e);
        }
        ticker.uploadingThreadFinished();
    }

    private void upload() { // real upload

        logger.info("Upload of " + uploadItem.getFile().getName() + " started.");

        FcpResultPut result = FcpHandler.inst().putFile(
                FcpHandler.TYPE_FILE,
                "CHK@",
                uploadItem.getFile(),
                null, // metadata
                true, // doRedirect
                true, // removeLocalKey, insert with full HTL even if existing in local store
                uploadItem); // provide the uploadItem to indicate that this upload is contained in table

        if (result.isSuccess() || result.isKeyCollision() ) {
            // upload successful
            uploadItem.setKey(result.getChkKey());
            if( uploadItem.isSharedFile() ) {
                uploadItem.getSharedFileItem().notifySuccessfulUpload(result.getChkKey());
            }
            logger.info("Upload of " + uploadItem.getFile().getName() + " was successful.");

            uploadItem.setEnabled(Boolean.FALSE);
            uploadItem.setState(FrostUploadItem.STATE_DONE);
            
            // notify model that shared upload file can be removed
            if( uploadItem.isSharedFile() ) {
                FileTransferManager.getInstance().getUploadManager().getModel().notifySharedFileUploadWasSuccessful(uploadItem);
            } else {
                // maybe log successful manual upload to file localdata/uploads.txt
                if( Core.frostSettings.getBoolValue(SettingsClass.LOG_UPLOADS_ENABLED) ) {
                    String line = uploadItem.getKey() + "/" + uploadItem.getFile().getName();
                    String fileName = Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA) + "Frost-Uploads.log";
                    File targetFile = new File(fileName);
                    FileAccess.appendLineToTextfile(targetFile, line);
                }
            }
            
        } else {
            // upload failed
            logger.warning("Upload of " + uploadItem.getFile().getName() + " was NOT successful.");

            uploadItem.setRetries(uploadItem.getRetries() + 1);
            
            if (uploadItem.getRetries() > Core.frostSettings.getIntValue(SettingsClass.UPLOAD_MAX_RETRIES)) {
                uploadItem.setEnabled(Boolean.FALSE);
                uploadItem.setState(FrostUploadItem.STATE_FAILED);
            } else {
                // retry
                uploadItem.setState(FrostUploadItem.STATE_WAITING);
            }
        }
        uploadItem.setLastUploadStopTimeMillis(System.currentTimeMillis());
    }
}
