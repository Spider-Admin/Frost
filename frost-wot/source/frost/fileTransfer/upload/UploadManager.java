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
import frost.fileTransfer.*;
import frost.fileTransfer.sharing.*;
import frost.storage.*;
import frost.util.*;

public class UploadManager {

    private static final Logger logger = Logger.getLogger(UploadManager.class.getName());

    private UploadModel model;
    private UploadPanel panel;
    private UploadTicker ticker;
    private UploadStatusPanel statusPanel;

    public UploadManager() {
        super();
    }

    public void initialize(List<FrostSharedFileItem> sharedFiles) throws StorageException {
        getPanel();
        getStatusPanel();
        getModel().initialize(sharedFiles);
        
        // on 0.5, load progress of all files
        if( FcpHandler.isFreenet05() ) {
            for(int x=0; x < getModel().getItemCount(); x++) {
                FrostUploadItem item = (FrostUploadItem) getModel().getItemAt(x);
                frost.fcp.fcp05.FcpInsert.updateProgress(item);
            }
        }
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
            statusPanel = new UploadStatusPanel();
        }
        return statusPanel;
    }

    private UploadTicker getTicker() {
        if (ticker == null) {
            ticker = new UploadTicker(getModel(), getPanel(), getStatusPanel());
        }
        return ticker;
    }

    public UploadModel getModel() {
        if (model == null) {
            model = new UploadModel(new UploadTableFormat());
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
     * Start upload now (manually).
     */
    public boolean startUpload(FrostUploadItem ulItem) {
        if( FileTransferManager.inst().getPersistenceManager() != null ) {
            return FileTransferManager.inst().getPersistenceManager().startUpload(ulItem);
        } else {
            return ticker.startUpload(ulItem);
        }
    }
    
    /**
     * Chooses next upload item to start from upload table.
     * @return the next upload item to start uploading or null if a suitable one was not found.
     */
    public FrostUploadItem selectNextUploadItem() {

        ArrayList<FrostUploadItem> waitingItems = new ArrayList<FrostUploadItem>();
        
        final long currentTime = System.currentTimeMillis();

        for (int i = 0; i < model.getItemCount(); i++) {
            FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);

            // don't start disabled items
            boolean itemIsEnabled = (ulItem.isEnabled()==null?true:ulItem.isEnabled().booleanValue());
            if( !itemIsEnabled ) {
                continue;
            }
            // don't start external 0.7 items
            if( ulItem.isExternal() ) {
                continue;
            }
            // don't start items whose direct transfer to the node is already in progress
            if( FileTransferManager.inst().getPersistenceManager() != null ) {
                if( FileTransferManager.inst().getPersistenceManager().isDirectTransferInProgress(ulItem) ) {
                    continue;
                }
            }
            // only start waiting items
            if (ulItem.getState() != FrostUploadItem.STATE_WAITING) {
                continue;
            }
            // for 0.5 the encoding must be done before so we have a key
            if( FcpHandler.isFreenet05() && ulItem.getKey() == null ) {
                continue;
            }
            // check if items waittime between tries is expired so we could restart it
            long waittimeMillis = (long)Core.frostSettings.getIntValue(SettingsClass.UPLOAD_WAITTIME) * 60L * 1000L;
            if ((currentTime - ulItem.getLastUploadStopTimeMillis()) < waittimeMillis) {
                continue;
            }

            // we could start this item
            waitingItems.add(ulItem);
        }

        if (waitingItems.size() == 0) {
            return null;
        }

        if (waitingItems.size() > 1) {
            Collections.sort(waitingItems, nextItemCmp);
        }

        return (FrostUploadItem) waitingItems.get(0);
    }

    private static final Comparator<FrostUploadItem> nextItemCmp = new Comparator<FrostUploadItem>() {
        public int compare(FrostUploadItem value1, FrostUploadItem value2) {
            
            int blocksTodo1;
            int blocksTodo2;

            // compute remaining blocks
            if( value1.getTotalBlocks() > 0 && value1.getDoneBlocks() > 0 ) {
                blocksTodo1 = value1.getTotalBlocks() - value1.getDoneBlocks(); 
            } else if( FcpHandler.isFreenet05() && value1.getFileSize() <= frost.fcp.fcp05.FcpInsert.smallestChunk ) {
                blocksTodo1 = 1; // 0.5, one block file
            } else {
                blocksTodo1 = Integer.MAX_VALUE; // never started
            }
            if( value2.getTotalBlocks() > 0 && value2.getDoneBlocks() > 0 ) {
                blocksTodo2 = value2.getTotalBlocks() - value2.getDoneBlocks(); 
            } else if( FcpHandler.isFreenet05() && value2.getFileSize() <= frost.fcp.fcp05.FcpInsert.smallestChunk ) {
                blocksTodo2 = 1; // 0.5, one block file
            } else {
                blocksTodo2 = Integer.MAX_VALUE; // never started
            }
            
            int cmp = Mixed.compareInt(blocksTodo1, blocksTodo2);
            if( cmp == 0 ) {
                // equal remainingBlocks, choose smaller file
                return Mixed.compareLong(value1.getFileSize(), value2.getFileSize());
            } else {
                return cmp;
            }
        }
    };
}
