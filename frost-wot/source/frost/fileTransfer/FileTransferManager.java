/*
  FileTransferManager.java / Frost
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
package frost.fileTransfer;

import frost.Core;
import frost.MainFrame;
import frost.fileTransfer.download.DownloadManager;
import frost.fileTransfer.search.SearchManager;
import frost.fileTransfer.sharing.FrostSharedFileItem;
import frost.fileTransfer.sharing.SharedFilesManager;
import frost.fileTransfer.upload.FrostUploadItem;
import frost.fileTransfer.upload.UploadManager;
import frost.identities.LocalIdentity;
import frost.storage.AutoSavable;
import frost.storage.ExitSavable;
import frost.storage.StorageException;

public class FileTransferManager implements ExitSavable, AutoSavable {

    private DownloadManager downloadManager;
    private SearchManager searchManager;
    private UploadManager uploadManager;
    private SharedFilesManager sharedFilesManager;
    private NewUploadFilesManager newUploadFilesManager;

    private PersistenceManager persistenceManager = null;

    private static FileTransferManager instance = null;

    private FileTransferManager() {
        super();
    }

    public static FileTransferManager inst() {
        if( instance == null ) {
            instance = new FileTransferManager();
        }
        return instance;
    }

    public void initialize() throws StorageException {
        getDownloadManager().initialize();
        getSearchManager().initialize();
        getSharedFilesManager().initialize();
        getUploadManager().initialize( getSharedFilesManager().getSharedFileItemList() );
        getNewUploadFilesManager().initialize();

        if( PersistenceManager.isPersistenceEnabled() && Core.isFreenetOnline() ) {
            try {
                persistenceManager = new PersistenceManager(getUploadManager().getModel(), getDownloadManager().getModel());
            } catch(final Throwable t) {
                System.out.println("FAILED TO ESTABLISH THE PERSISTENT CONNECTION!");
                t.printStackTrace();
            }
        }

        // call order is order of panels in gui
        final MainFrame mainFrame = MainFrame.getInstance();
        getDownloadManager().addPanelToMainFrame(mainFrame);
        getUploadManager().addPanelToMainFrame(mainFrame);
        getSearchManager().addPanelToMainFrame(mainFrame);
        getSharedFilesManager().addPanelToMainFrame(mainFrame);
    }

    /**
     * @return  null if not using persistence
     */
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void startTickers() {
        getDownloadManager().startTicker();
        getUploadManager().startTicker();
        getNewUploadFilesManager().start();

        // maybe start persistence threads
        if( getPersistenceManager() != null ) {
            getPersistenceManager().startThreads();
        }
    }

    public DownloadManager getDownloadManager() {
        if (downloadManager == null) {
            downloadManager = new DownloadManager();
        }
        return downloadManager;
    }

    public FileTransferInformation getFileTransferInformation() {
        final FileTransferInformation infos = new FileTransferInformation();
        getDownloadManager().updateFileTransferInformation(infos);
        getUploadManager().updateFileTransferInformation(infos);
        infos.setFileListDownloadQueueSize(FileSharingManager.getFileListDownloadQueueSize());
        return infos;
    }

    /**
     * Updates the count of waiting uploads/downloads in the toolbar of upload/download tab.
     */
    public void updateWaitingCountInPanels(final FileTransferInformation infos) {
        if( infos == null ) {
            return;
        }
        getDownloadManager().getPanel().setDownloadItemCount(infos.getDownloadsWaiting());
        getUploadManager().getPanel().setUploadItemCount(infos.getUploadsWaiting());
    }

    public int countFilesSharedByLocalIdentity(final LocalIdentity li) {
        int count = 0;
        for (int x = 0; x < getSharedFilesManager().getModel().getItemCount(); x++) {
            final FrostSharedFileItem item = (FrostSharedFileItem) getSharedFilesManager().getModel().getItemAt(x);
            if( item.getOwner().equals( li.getUniqueName()) ) {
                count++;
            }
        }
        return count;
    }

    public void removeFilesSharedByLocalIdentity(final LocalIdentity li) {

        // remove from uploadtable
        for (int x = getUploadManager().getModel().getItemCount()-1; x >= 0; x--) {
            final Object obj = getUploadManager().getModel().getItemAt(x);
            if( !(obj instanceof FrostSharedFileItem) ) {
                continue;
            }
            final FrostSharedFileItem suf = (FrostSharedFileItem)obj;
            if( suf.getOwner().equals(li.getUniqueName()) ) {
            	// FIXME: why do we have a FrostSharedFileItem and try to remove a FrostUploadItem?
            	FrostUploadItem frostSharedFileItem = (FrostUploadItem) (Object) suf;
                getUploadManager().getModel().removeItem(frostSharedFileItem);
            }
        }

        // remove from sharedfiles table
        for (int x = 0; x < getSharedFilesManager().getModel().getItemCount(); x++) {
            final FrostSharedFileItem item = (FrostSharedFileItem) getSharedFilesManager().getModel().getItemAt(x);
            if( item.getOwner().equals( li.getUniqueName()) ) {
                getSharedFilesManager().getModel().removeItem(item);
            }
        }
    }

    public SearchManager getSearchManager() {
        if (searchManager == null) {
            searchManager = new SearchManager();
        }
        return searchManager;
    }

    public UploadManager getUploadManager() {
        if (uploadManager == null) {
            uploadManager = new UploadManager();
        }
        return uploadManager;
    }

    public SharedFilesManager getSharedFilesManager() {
        if (sharedFilesManager == null) {
            sharedFilesManager = new SharedFilesManager();
        }
        return sharedFilesManager;
    }

    public NewUploadFilesManager getNewUploadFilesManager() {
        if( newUploadFilesManager == null ) {
            newUploadFilesManager = new NewUploadFilesManager();
        }
        return newUploadFilesManager;
    }

    public void exitSave() throws StorageException {
        save();
    }
    public void autoSave() throws StorageException {
        save();
    }

    public void save() throws StorageException {
        getDownloadManager().exitSave();
        getUploadManager().exitSave();
        getSharedFilesManager().exitSave();
        getNewUploadFilesManager().exitSave();
    }
}
