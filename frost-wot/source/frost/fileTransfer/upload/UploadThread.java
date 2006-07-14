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
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.storage.database.applayer.*;

class UploadThread extends Thread
{
    private SettingsClass settings;

    private UploadTicker ticker;

    private static Logger logger = Logger.getLogger(UploadThread.class.getName());

    public static final int MODE_GENERATE_SHA1 = 1;
    public static final int MODE_GENERATE_CHK  = 2;
    public static final int MODE_UPLOAD        = 3;

    private int nextState; // the state to set on uploadItem when finished, or -1 for default (IDLE)
    private int mode;

    FrostUploadItem uploadItem = null; // for upload and generate CHK
    NewUploadFile newUploadFile = null;
    UploadModel uploadModel = null;

    public void run() {
        switch (mode) {
            case MODE_UPLOAD:
                ticker.uploadingThreadStarted();
                break;
            case MODE_GENERATE_SHA1:
                ticker.generatingThreadStarted();
                break;
            case MODE_GENERATE_CHK:
                ticker.generatingThreadStarted();
                break;
        }

        try {
            switch (mode) {
                case MODE_UPLOAD :
                    upload();
                    ticker.uploadingThreadFinished();
                    break;
                case MODE_GENERATE_SHA1 :
                    generateSHA1();
                    ticker.generatingThreadFinished();
                    break;
                case MODE_GENERATE_CHK :
                    generateCHK();
                    ticker.generatingThreadFinished();
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in run()", e);
            switch (mode) {
                case MODE_UPLOAD :
                    ticker.uploadingThreadFinished();
                    break;
                case MODE_GENERATE_SHA1 :
                    ticker.generatingThreadFinished();
                    break;
                case MODE_GENERATE_CHK :
                    ticker.generatingThreadFinished();
                    break;
            }
        }
    }

    private void upload() { // real upload

        logger.info("Upload of " + uploadItem.getFileName() + " started.");

        FcpResultPut result = FcpHandler.inst().putFile(
                FcpHandler.TYPE_FILE,
                "CHK@",
                new File(uploadItem.getFilePath()),
                null, // metadata
                true, // doRedirect
                true, // removeLocalKey, insert with full HTL even if existing in local store
                uploadItem); // provide the uploadItem to indicate that this upload is contained in table

        if (result.isSuccess() || result.isKeyCollision() ) {
            uploadItem.setKey(result.getChkKey());
            // Upload succeeded
            logger.info("Upload of " + uploadItem.getFileName() + " was successful.");

            uploadItem.setState(nextState);
            uploadItem.setLastUploadDate(DateFun.getCurrentSqlDateGMT());
            // mark the obs of this file so the new uploaddate+key is sent
            for(Iterator i=uploadItem.getFrostUploadItemOwnerBoardList().iterator(); i.hasNext(); ) {
                FrostUploadItemOwnerBoard ob = (FrostUploadItemOwnerBoard)i.next();
                ob.setLastSharedDate(null);
            }
        } else {
            // Upload failed
            logger.warning("Upload of " + uploadItem.getFileName() + " was NOT successful.");

            uploadItem.setRetries(uploadItem.getRetries() + 1);
            if (uploadItem.getRetries() > settings.getIntValue(SettingsClass.UPLOAD_MAX_RETRIES)) {
                if (settings.getBoolValue(SettingsClass.RESTART_FAILED_UPLOADS)) {
                    uploadItem.setState(FrostUploadItem.STATE_WAITING);
                    uploadItem.setRetries(0);
                } else {
                    uploadItem.setState(this.nextState);
                }
            } else {
                uploadItem.setState(FrostUploadItem.STATE_WAITING);
            }
        }
        uploadItem.setLastUploadStopTimeMillis(System.currentTimeMillis());
    }

    private void generateSHA1() {

        String SHA1 = Core.getCrypto().digest(newUploadFile.getFile());
        
        FrostUploadItem ulItem = new FrostUploadItem(
                newUploadFile.getFile(),
                newUploadFile.getTargetBoard(), 
                newUploadFile.getFrom(),
                SHA1);
        
        uploadModel.addNewUploadItem(ulItem);

        Core.getInstance().getFileTransferManager().getNewUploadFilesManager().deleteNewUploadFile(newUploadFile);
        
        ulItem.setState(this.nextState);
    }

    private void generateCHK() {
        logger.info("CHK generation started for file: " + uploadItem.getFileName());
        String chkkey = null;
        
        // yes, this destroys any upload progress, but we come only here if
        // chkKey == null, so the file should'nt be uploaded until now
        try {
            chkkey = FcpHandler.inst().generateCHK(new File(uploadItem.getFilePath()));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Encoding failed", t);
            uploadItem.setState(FrostUploadItem.STATE_IDLE);
            return;
        }

        if (chkkey != null) {
            String prefix = new String("freenet:");
            if (chkkey.startsWith(prefix)) {
                chkkey = chkkey.substring(prefix.length());
            }
        } else {
            logger.warning("Could not generate CHK key for redirect file.");
        }

        uploadItem.setKey(chkkey);

        // test if the GetRequestsThread did set us the nextState field...
        if (uploadItem.getNextState() > 0) {
            uploadItem.setState(uploadItem.getNextState());
            uploadItem.setNextState(0); // reset nextState
        } else {
            uploadItem.setState(this.nextState);
        }
    }

    /**Constructor*/
    public UploadThread(UploadTicker newTicker, FrostUploadItem ulItem, SettingsClass settings, int mode) {
        this(newTicker, settings, mode, -1);
        uploadItem = ulItem;
    }

    public UploadThread(UploadTicker newTicker, FrostUploadItem ulItem, SettingsClass settings, int mode, int nextState) {
        this(newTicker, settings, mode, nextState);
        uploadItem = ulItem;
    }

    public UploadThread(UploadTicker newTicker, NewUploadFile uploadFile, UploadModel um, SettingsClass settings, int mode) {
        this(newTicker, settings, mode, -1);
        uploadModel = um;
        newUploadFile = uploadFile;
    }

    protected UploadThread(
        UploadTicker newTicker,
        SettingsClass settings,
        int newMode,
        int newNextState) {

        ticker = newTicker;

        this.settings = settings;
        mode = newMode;
        nextState = newNextState;
        if (nextState < 0) {
            nextState = FrostUploadItem.STATE_IDLE;
        }
    }
}
