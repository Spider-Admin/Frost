/*
  UploadManager.java / Frost
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
import frost.storage.*;
import frost.util.*;

public class UploadManager {

    private static Logger logger = Logger.getLogger(UploadManager.class.getName());

    private UploadModel model;
    private UploadPanel panel;
    private UploadTicker ticker;
    private UploadStatusPanel statusPanel;

    public UploadManager() {
        super();
    }

    public void initialize(List sharedFiles) throws StorageException {
        getPanel();
        getStatusPanel();
        getModel().initialize(sharedFiles);
    }
    
    public void startTicker() {
        if (Core.isFreenetOnline()) {
            getTicker().start();
        }
    }
    
    public void save() throws StorageException {
        getPanel().getTableFormat().saveTableLayout();
        getModel().save();
    }
    
    public void addPanelToMainFrame(MainFrame mainFrame) {
        mainFrame.addPanel("MainFrame.tabbedPane.uploads", getPanel());
        mainFrame.addStatusPanel(getStatusPanel(), 0);
    }

    public UploadPanel getPanel() {
        if (panel == null) {
            panel = new UploadPanel();
            panel.setModel(getModel());
            panel.initialize();
        }
        return panel;
    }

    private UploadStatusPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new UploadStatusPanel(getTicker());
        }
        return statusPanel;
    }

    private UploadTicker getTicker() {
        if (ticker == null) {
            ticker = new UploadTicker(getModel(), getPanel());
        }
        return ticker;
    }

    public UploadModel getModel() {
        if (model == null) {
            model = new UploadModel();
        }
        return model;
    }

    /**
     * Handle a finished file upload, either successful or failed.
     */
    public void notifyUploadFinished(FrostUploadItem uploadItem, FcpResultPut result) {

        if (result != null && (result.isSuccess() || result.isKeyCollision()) ) {
            
            logger.info("Upload of " + uploadItem.getFile().getName() + " was successful.");

            // upload successful
            uploadItem.setKey(result.getChkKey());
            if( uploadItem.isSharedFile() ) {
                uploadItem.getSharedFileItem().notifySuccessfulUpload(result.getChkKey());
            }

            uploadItem.setEnabled(Boolean.FALSE);
            uploadItem.setState(FrostUploadItem.STATE_DONE);
            
            // notify model that shared upload file can be removed
            if( uploadItem.isSharedFile() ) {
                getModel().notifySharedFileUploadWasSuccessful(uploadItem);
            } else {
                // maybe log successful manual upload to file localdata/uploads.txt
                if( Core.frostSettings.getBoolValue(SettingsClass.LOG_UPLOADS_ENABLED) ) {
                    String line = uploadItem.getKey() + "/" + uploadItem.getFile().getName();
                    String fileName = Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA) + "Frost-Uploads.log";
                    File targetFile = new File(fileName);
                    FileAccess.appendLineToTextfile(targetFile, line);
                }
            }

            // maybe remove finished upload immediately
            if( Core.frostSettings.getBoolValue(SettingsClass.UPLOAD_REMOVE_FINISHED) ) {
                getModel().removeFinishedUploads();
            }

        } else {
            // upload failed
            logger.warning("Upload of " + uploadItem.getFile().getName() + " was NOT successful.");

            if( result != null && result.isFatal() ) {
                uploadItem.setEnabled(Boolean.FALSE);
                uploadItem.setState(FrostUploadItem.STATE_FAILED);
            } else {
                uploadItem.setRetries(uploadItem.getRetries() + 1);
                
                if (uploadItem.getRetries() > Core.frostSettings.getIntValue(SettingsClass.UPLOAD_MAX_RETRIES)) {
                    uploadItem.setEnabled(Boolean.FALSE);
                    uploadItem.setState(FrostUploadItem.STATE_FAILED);
                } else {
                    // retry
                    uploadItem.setState(FrostUploadItem.STATE_WAITING);
                }
            }
            if( result != null ) {
                uploadItem.setErrorCodeDescription(result.getCodeDescription());
            }
        }
        uploadItem.setLastUploadStopTimeMillis(System.currentTimeMillis());
    }
    
    /**
     * Chooses next upload item to start from upload table.
     * @return the next upload item to start uploading or null if a suitable one was not found.
     */
    public FrostUploadItem selectNextUploadItem() {

        ArrayList<FrostUploadItem> waitingItems = new ArrayList<FrostUploadItem>();

        for (int i = 0; i < model.getItemCount(); i++) {
            FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);
            boolean itemIsEnabled = (ulItem.isEnabled()==null?true:ulItem.isEnabled().booleanValue());
            if( !itemIsEnabled ) {
                continue;
            }
            if( ulItem.isExternal() ) {
                continue;
            }
            
            // we choose items that are waiting, enabled and for 0.5 the encoding must be done before
            if (ulItem.getState() == FrostUploadItem.STATE_WAITING
                && (ulItem.getKey() != null || FcpHandler.isFreenet07() ) )
            {
                // check if waittime has expired
                long waittimeMillis = (long)Core.frostSettings.getIntValue(SettingsClass.UPLOAD_WAITTIME) * 60L * 1000L;
                if ((System.currentTimeMillis() - ulItem.getLastUploadStopTimeMillis()) > waittimeMillis) {
                    waitingItems.add(ulItem);
                }
            }
        }

        if (waitingItems.size() == 0) {
            return null;
        }

        if (waitingItems.size() > 1) {
            Collections.sort(waitingItems, uploadDlStopMillisCmp);
        }
        return (FrostUploadItem) waitingItems.get(0);
    }

    /**
     * Used to sort FrostUploadItems by lastUploadStopTimeMillis ascending.
     */
    private static final Comparator<FrostUploadItem> uploadDlStopMillisCmp = new Comparator<FrostUploadItem>() {
        public int compare(FrostUploadItem value1, FrostUploadItem value2) {
            if (value1.getLastUploadStopTimeMillis() > value2.getLastUploadStopTimeMillis())
                return 1;
            else if (value1.getLastUploadStopTimeMillis() < value2.getLastUploadStopTimeMillis())
                return -1;
            else
                return 0;
        }
    };
}
