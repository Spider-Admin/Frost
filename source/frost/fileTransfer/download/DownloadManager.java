/*
  DownloadManager.java / Frost

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
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.*;

public class DownloadManager {

    private static Logger logger = Logger.getLogger(DownloadManager.class.getName());

	private DownloadModel model;
	private DownloadPanel panel;
	private DownloadTicker ticker;
	private DownloadStatusPanel statusPanel;

	public DownloadManager() {
		super();
	}
	
	public void initialize() throws StorageException {
        getPanel();
        getStatusPanel();
		getModel().initialize();
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
        mainFrame.addPanel("MainFrame.tabbedPane.downloads", getPanel());
        mainFrame.addStatusPanel(getStatusPanel(), 0);
    }
	
	private DownloadStatusPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new DownloadStatusPanel();
		}
		return statusPanel;
	}
    
    /**
     * Checks if a file with this name is already in model, and returns
     * a new name if needed.
     */
    public String ensureUniqueFilename(String filename) {
        
        String newFilename = filename;
        int count = 2;
        
        while(true) {
            boolean loopAgain = false;
            for(int x=0; x < getModel().getItemCount(); x++) {
                FrostDownloadItem dlItem = (FrostDownloadItem) getModel().getItemAt(x);
                if( dlItem.getFilename().equalsIgnoreCase(newFilename) ) {
                    loopAgain = true;
                    // we have a duplicate filename
                    // build new filename like "filename_2.ext"
                    int pos = filename.lastIndexOf('.'); 
                    if( pos > 0 ) {
                        String beforeDot = filename.substring(0, pos);
                        String afterDot = filename.substring(pos);
                        newFilename = beforeDot + "_" + (count++) + afterDot;
                    } else {
                        // no '.' in filename
                        newFilename = filename + "_" + (count++);
                    }
                }
            }
            if( !loopAgain ) {
                break;
            }
        }
        return newFilename;
    }

	public DownloadPanel getPanel() {
		if (panel == null) {
			panel = new DownloadPanel();
			panel.setModel(getModel());
			panel.initialize();
		}
		return panel;
	}
	
	public DownloadModel getModel() {
		if (model == null) {
			model = new DownloadModel(new DownloadTableFormat());	
		}
		return model;
	}
	
	private DownloadTicker getTicker() {
		if (ticker == null) {
			ticker = new DownloadTicker(getModel(), getPanel(), getStatusPanel());
		}
		return ticker;
	}

    /**
     * @return  true if request should be retried
     */
    public boolean notifyDownloadFinished(FrostDownloadItem downloadItem, FcpResultGet result, File targetFile) {

        String filename = downloadItem.getFilename();
        String key = downloadItem.getKey();

        boolean retryImmediately = false;

        if (result == null || result.isSuccess() == false) {
            // download failed

            if( result != null ) {
                downloadItem.setErrorCodeDescription(result.getCodeDescription());
            }

            if( result != null
                    && FcpHandler.isFreenet07()
                    && result.getReturnCode() == 11 
                    && key.startsWith("CHK@")
                    && key.indexOf("/") > 0 ) 
            {
                // remove one path level from CHK
                String newKey = key.substring(0, key.lastIndexOf("/"));
                downloadItem.setKey(newKey);
                downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                downloadItem.setInternalRemoveExpected(true);
                retryImmediately = true;
                
                logger.warning("Removed one path level from key: "+key+" ; "+newKey);

            } else if( result != null
                        && FcpHandler.isFreenet07()
                        && result.getReturnCode() == 27
                        && result.getRedirectURI() != null)
            {
                // permanent redirect, use new uri
                downloadItem.setKey(result.getRedirectURI());
                downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                downloadItem.setInternalRemoveExpected(true);
                retryImmediately = true;
                
                logger.warning("Redirected to URI: "+result.getRedirectURI());

            } else if( result != null && result.isFatal() ) {
                // fatal, don't retry
                downloadItem.setEnabled(Boolean.valueOf(false));
                downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                logger.warning("FILEDN: Download of " + filename + " failed FATALLY.");
            } else {
                downloadItem.setRetries(downloadItem.getRetries() + 1);

                logger.warning("FILEDN: Download of " + filename + " failed.");
                // set new state -> failed or waiting for another try
                if (downloadItem.getRetries() > Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_RETRIES)) {
                    downloadItem.setEnabled(Boolean.valueOf(false));
                    downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                } else {
                    downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                }
            }
        } else {
            
            logger.info("FILEDN: Download of " + filename + " was successful.");

            // download successful
            downloadItem.setFileSize(new Long(targetFile.length()));
            downloadItem.setState(FrostDownloadItem.STATE_DONE);
            downloadItem.setEnabled(Boolean.valueOf(false));

            // update lastDownloaded time in filelist
            if( downloadItem.isSharedFile() ) {
                try {
                    AppLayerDatabase.getFileListDatabaseTable().updateFrostFileListFileObjectAfterDownload(
                            downloadItem.getFileListFileObject().getSha(),
                            System.currentTimeMillis() );
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Exception in updateFrostFileListFileObjectAfterDownload()", e);
                }
            }

            // maybe log successful download to file localdata/downloads.txt
            if( Core.frostSettings.getBoolValue(SettingsClass.LOG_DOWNLOADS_ENABLED) ) {
                String line = downloadItem.getKey() + "/" + downloadItem.getFilename();
                String fileName = Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA) + "Frost-Downloads.log";
                File targetLogFile = new File(fileName);
                FileAccess.appendLineToTextfile(targetLogFile, line);
            }

            // maybe remove finished download immediately
            if( Core.frostSettings.getBoolValue(SettingsClass.DOWNLOAD_REMOVE_FINISHED) ) {
                FileTransferManager.inst().getDownloadManager().getModel().removeFinishedDownloads();
            }
        }

        if( retryImmediately ) {
            downloadItem.setLastDownloadStopTime(-1);
        } else {
            downloadItem.setLastDownloadStopTime(System.currentTimeMillis());
        }
        
        return retryImmediately;
    }
    
    /**
     * Chooses next download item to start from download table.
     * @return the next download item to start downloading or null if a suitable
     *          one was not found.
     */
    public FrostDownloadItem selectNextDownloadItem() {

        // get the item with state "Waiting"
        ArrayList<FrostDownloadItem> waitingItems = new ArrayList<FrostDownloadItem>();
        for (int i = 0; i < model.getItemCount(); i++) {
            FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(i);
            boolean itemIsEnabled = (dlItem.isEnabled()==null?true:dlItem.isEnabled().booleanValue());
            if( !itemIsEnabled ) {
                continue;
            }
            if( dlItem.isExternal() ) {
                continue;
            }
            if( dlItem.getKey() == null ) {
                // still no key, wait
                continue;
            }
            
            if( dlItem.getState() == FrostDownloadItem.STATE_WAITING ) {
                // check if waittime is expired
                long waittimeMillis = (long)Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_WAITTIME) * 60L * 1000L;
                // min->millisec
                if (dlItem.getLastDownloadStopTime() == 0 // never started
                    || (System.currentTimeMillis() - dlItem.getLastDownloadStopTime()) > waittimeMillis) 
                {
                    waitingItems.add(dlItem);
                }
            }
        }

        if (waitingItems.size() == 0) {
            return null;
        }

        if (waitingItems.size() > 1) { // performance issues
            Collections.sort(waitingItems, downloadDlStopMillisCmp);
        }
        return (FrostDownloadItem) waitingItems.get(0);
    }
    
    /**
     * Used to sort FrostDownloadItems by lastUpdateStartTimeMillis ascending.
     */
    private static final Comparator<FrostDownloadItem> downloadDlStopMillisCmp = new Comparator<FrostDownloadItem>() {
        public int compare(FrostDownloadItem value1, FrostDownloadItem value2) {
            return Mixed.compareLong(value1.getLastDownloadStopTime(), value2.getLastDownloadStopTime());
        }
    };
}
