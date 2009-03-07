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
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.storage.*;
import frost.storage.perst.filelist.*;
import frost.util.*;
import frost.util.model.*;

public class DownloadManager implements ExitSavable {

    private static final Logger logger = Logger.getLogger(DownloadManager.class.getName());

	private DownloadModel model;
	private DownloadPanel panel;
	private DownloadTicker ticker;

	public DownloadManager() {
		super();
	}

	public void initialize() throws StorageException {
        getPanel();
		getModel().initialize();

        // on 0.5, load progress of all files
        if( FcpHandler.isFreenet05() ) {
            for(int x=0; x < getModel().getItemCount(); x++) {
                final FrostDownloadItem item = (FrostDownloadItem) getModel().getItemAt(x);
                frost.fcp.fcp05.FcpRequest.updateProgress(item);
            }
        }
	}

	/**
	 * Start download now (manually).
	 */
	public boolean startDownload(final FrostDownloadItem dlItem) {
	    if( FileTransferManager.inst().getPersistenceManager() != null ) {
	        return FileTransferManager.inst().getPersistenceManager().startDownload(dlItem);
	    } else {
	        return ticker.startDownload(dlItem);
	    }
	}

	public void startTicker() {
        if (Core.isFreenetOnline()) {
            getTicker().start();
        }
    }

    public void exitSave() throws StorageException {
        getPanel().getTableFormat().saveTableLayout();
        getModel().exitSave();
    }

    public void addPanelToMainFrame(final MainFrame mainFrame) {
        mainFrame.addPanel("MainFrame.tabbedPane.downloads", getPanel());
    }

    /**
     * Count running items in model.
     */
    public void updateFileTransferInformation(final FileTransferInformation infos) {
        int waitingItems = 0;
        int runningItems = 0;
        for (int x = 0; x < model.getItemCount(); x++) {
            final FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(x);
            if( dlItem == null ) {
                continue;
            }
            if (dlItem.getState() != FrostDownloadItem.STATE_DONE
                    && dlItem.getState() != FrostDownloadItem.STATE_FAILED)
            {
                waitingItems++;
            }
            if (dlItem.getState() == FrostDownloadItem.STATE_PROGRESS) {
                runningItems++;
            }
        }
        infos.setDownloadsRunning(runningItems);
        infos.setDownloadsWaiting(waitingItems);
    }

    /**
     * Checks if a file with this name is already in model, and returns
     * a new name if needed.
     */
    public String ensureUniqueFilename(final String filename) {

        String newFilename = filename;
        int count = 2;

        while(true) {
            boolean loopAgain = false;
            for(int x=0; x < getModel().getItemCount(); x++) {
                final FrostDownloadItem dlItem = (FrostDownloadItem) getModel().getItemAt(x);
                if( dlItem.getFilename().equalsIgnoreCase(newFilename) ) {
                    loopAgain = true;
                    // we have a duplicate filename
                    // build new filename like "filename_2.ext"
                    final int pos = filename.lastIndexOf('.');
                    if( pos > 0 ) {
                        final String beforeDot = filename.substring(0, pos);
                        final String afterDot = filename.substring(pos);
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
			ticker = new DownloadTicker(getPanel());
		}
		return ticker;
	}

	/**
	 * Add a new download item to the download table model.
	 * @param key        complete key of download item
	 * @param fileName   file name
	 */
	public void addNewDownload(final String key, final String fileName) {
	    // TODO: enhancement: search for key in shared files, maybe add as shared file
        final FrostDownloadItem dlItem = new FrostDownloadItem(fileName, key);
        model.addDownloadItem(dlItem); // false if file is already in table
	}

    /**
     * @return  true if request should be retried
     */
    public boolean notifyDownloadFinished(final FrostDownloadItem downloadItem, final FcpResultGet result, final File targetFile) {

        final String filename = downloadItem.getFilename();
        final String key = downloadItem.getKey();

        boolean retryImmediately = false;

        if (result == null || result.isSuccess() == false) {
            // download failed

            if( result != null ) {
                downloadItem.setErrorCodeDescription(result.getCodeDescription());
            }

            if( result != null
                    && FcpHandler.isFreenet07()
                    && result.getReturnCode() == 5
                    && key.startsWith("CHK@")
                    && key.indexOf("/") > 0 )
            {
                // 5 - Archive failure
                // node tries to access the file as a .zip file, try download again without any path
                final String newKey = key.substring(0, key.indexOf("/"));
                downloadItem.setKey(newKey);
                downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                downloadItem.setLastDownloadStopTime(0);
                downloadItem.setInternalRemoveExpected(true);
                retryImmediately = true;

                logger.warning("Removed all path levels from key: "+key+" ; "+newKey);

            } else if( result != null
                    && FcpHandler.isFreenet07()
                    && result.getReturnCode() == 11
                    && key.startsWith("CHK@")
                    && key.indexOf("/") > 0 )
            {
                // 11 - The URI has more metastrings and I can't deal with them
                // remove one path level from CHK
                final String newKey = key.substring(0, key.lastIndexOf("/"));
                downloadItem.setKey(newKey);
                downloadItem.setState(FrostDownloadItem.STATE_WAITING);
                downloadItem.setLastDownloadStopTime(0);
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
//                downloadItem.setEnabled(Boolean.FALSE);  // keep enabled to allow sending of requests for shared files
                downloadItem.setState(FrostDownloadItem.STATE_FAILED);
                logger.warning("FILEDN: Download of " + filename + " failed FATALLY.");
            } else {
                downloadItem.setRetries(downloadItem.getRetries() + 1);

                logger.warning("FILEDN: Download of " + filename + " failed.");
                // set new state -> failed or waiting for another try
                if (downloadItem.getRetries() > Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_RETRIES)) {
//                    downloadItem.setEnabled(Boolean.valueOf(false)); // keep enabled to allow sending of requests for shared files
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

            downloadItem.setDownloadFinishedTime(System.currentTimeMillis());

            // update lastDownloaded time in filelist
            if( downloadItem.isSharedFile() ) {
                FileListStorage.inst().updateFrostFileListFileObjectAfterDownload(
                        downloadItem.getFileListFileObject().getSha(),
                        System.currentTimeMillis() );
            }

            // maybe log successful download to file localdata/downloads.txt
            if( Core.frostSettings.getBoolValue(SettingsClass.LOG_DOWNLOADS_ENABLED) && !downloadItem.isLoggedToFile() ) {
                final String line = downloadItem.getKey() + "/" + downloadItem.getFilename();
                final String fileName = Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA) + "Frost-Downloads.log";
                final File targetLogFile = new File(fileName);
                FileAccess.appendLineToTextfile(targetLogFile, line);
                downloadItem.setLoggedToFile(true);
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

    @SuppressWarnings("unchecked")
    public List<FrostDownloadItem> getDownloadItemList() {
        return getModel().getItems();
    }

    /**
     * Chooses next download item to start from download table.
     * @return the next download item to start downloading or null if a suitable
     *          one was not found.
     */
    public FrostDownloadItem selectNextDownloadItem() {

        // get the item with state "Waiting"
        final ArrayList<FrostDownloadItem> waitingItems = new ArrayList<FrostDownloadItem>();
        for (int i = 0; i < model.getItemCount(); i++) {
            final FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(i);
            final boolean itemIsEnabled = (dlItem.isEnabled()==null?true:dlItem.isEnabled().booleanValue());
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

            if( dlItem.getState() != FrostDownloadItem.STATE_WAITING ) {
                continue;
            }

            // check if waittime is expired
            final long waittimeMillis = Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_WAITTIME) * 60L * 1000L;
            // min->millisec
            if (dlItem.getLastDownloadStopTime() == 0 // never started
                || (System.currentTimeMillis() - dlItem.getLastDownloadStopTime()) > waittimeMillis)
            {
                waitingItems.add(dlItem);
            }
        }

        if (waitingItems.size() == 0) {
            return null;
        }

        if (waitingItems.size() > 1) { // performance issues
            Collections.sort(waitingItems, nextItemCmp);
        }
        return waitingItems.get(0);
    }

    public void notifyDownloadItemEnabledStateChanged(final FrostDownloadItem dlItem) {
        // for persistent items, set priority to 6 (pause) when disabled; and to configured default if enabled
        if( FileTransferManager.inst().getPersistenceManager() == null ) {
            return;
        }
        if( dlItem.isExternal() ) {
            return;
        }
        if( dlItem.getState() != FrostDownloadItem.STATE_PROGRESS ) {
            // not running, not in queue
            return;
        }
        final boolean itemIsEnabled = (dlItem.isEnabled()==null?true:dlItem.isEnabled().booleanValue());
        if( itemIsEnabled ) {
            // item is now enabled
            final int prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD);
            FileTransferManager.inst().getPersistenceManager().changeItemPriorites(new ModelItem[] {dlItem}, prio);
        } else {
            // item is now disabled
            FileTransferManager.inst().getPersistenceManager().changeItemPriorites(new ModelItem[] {dlItem}, 6);
        }
    }

    /**
     * Used to sort FrostDownloadItems by lastUpdateStartTimeMillis ascending.
     */
    private static final Comparator<FrostDownloadItem> nextItemCmp = new Comparator<FrostDownloadItem>() {
        public int compare(final FrostDownloadItem value1, final FrostDownloadItem value2) {

            // choose item that with lowest addedTime
            final int cmp1 = Mixed.compareLong(value1.getDownloadAddedMillis(), value2.getDownloadAddedMillis());
            if( cmp1 != 0 ) {
                return cmp1;
            }

            // equal addedTimes, choose by blocksRemaining
            int blocksTodo1;
            int blocksTodo2;

            // compute remaining blocks
            if( value1.getRequiredBlocks() > 0 && value1.getDoneBlocks() > 0 ) {
                blocksTodo1 = value1.getRequiredBlocks() - value1.getDoneBlocks();
            } else {
                blocksTodo1 = Integer.MAX_VALUE; // never started
            }
            if( value2.getRequiredBlocks() > 0 && value2.getDoneBlocks() > 0 ) {
                blocksTodo2 = value2.getRequiredBlocks() - value2.getDoneBlocks();
            } else {
                blocksTodo2 = Integer.MAX_VALUE; // never started
            }

            final int cmp2 = Mixed.compareInt(blocksTodo1, blocksTodo2);
            if( cmp2 != 0 ) {
                return cmp2;
            }

            // equal remainingBlocks, choose smaller file (filesize can be -1)
            return Mixed.compareLong(value1.getFileSize(), value2.getFileSize());
        }
    };
}
