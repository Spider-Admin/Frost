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

import java.util.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.search.*;
import frost.fileTransfer.upload.*;
import frost.identities.*;
import frost.storage.*;
import frost.util.model.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class FileTransferManager implements Savable {

    private SettingsClass settings;

    private MainFrame mainFrame;

    private TofTreeModel tofTreeModel;

    private boolean isOnline;

    private FrostIdentities identities;

    private String keypool;

    private DownloadManager downloadManager;
    private SearchManager searchManager;
    private UploadManager uploadManager;
    private NewUploadFilesManager newUploadFilesManager;
    
    /**
     * @param settings
     */
    public FileTransferManager(SettingsClass settings) {
        super();
        this.settings = settings;
    }

    /**
     * @throws StorageException
     */
    public void initialize() throws StorageException {
        getDownloadManager().initialize();
        getSearchManager().initialize();
        getUploadManager().initialize();
        getNewUploadFilesManager().initialize();
        Index.initialize(getDownloadManager().getModel(), getUploadManager().getModel());

        //Until the downloads and uploads are fully separated from frame1:
        mainFrame.getMessagePanel().getMessageTextPane().setDownloadModel(getDownloadManager().getModel());
        mainFrame.setUploadPanel(getUploadManager().getPanel());
    }

    /**
     * @param mainFrame
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * @param tofTreeModel
     */
    public void setTofTreeModel(TofTreeModel tofTreeModel) {
        this.tofTreeModel = tofTreeModel;
    }

    /**
     * @param isOnline
     */
    public void setFreenetIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    /**
     * @param identities
     */
    public void setIdentities(FrostIdentities identities) {
        this.identities = identities;
    }

    /**
     * @param keypool
     */
    public void setKeypool(String keypool) {
        this.keypool = keypool;
    }

    private DownloadManager getDownloadManager() {
        if (downloadManager == null) {
            downloadManager = new DownloadManager(settings);
            downloadManager.setMainFrame(mainFrame);
            downloadManager.setFreenetIsOnline(isOnline);
        }
        return downloadManager;
    }
    
    public void setDownloadItemsAfterImport(List dlItems) {
        for(Iterator i=dlItems.iterator(); i.hasNext(); ) {
            FrostDownloadItem di = (FrostDownloadItem)i.next();
            getDownloadManager().getModel().addDownloadItem(di);
        }
    }

    public void setUploadItemsAfterImport(List ulItems) {
        for(Iterator i=ulItems.iterator(); i.hasNext(); ) {
            FrostUploadItem di = (FrostUploadItem)i.next();
            getUploadManager().getModel().addConsistentUploadItem(di);
        }
    }
    
    public int countFilesSharedByLocalIdentity(LocalIdentity li) {
        int count = 0;
        for (int x = 0; x < getUploadManager().getModel().getItemCount(); x++) {
            FrostUploadItem item = (FrostUploadItem) getUploadManager().getModel().getItemAt(x);
            for(int y = 0; y < item.getFrostUploadItemOwnerBoardList().size(); y++ ) {
                FrostUploadItemOwnerBoard ob = (FrostUploadItemOwnerBoard)item.getFrostUploadItemOwnerBoardList().get(y);
                if( ob.getOwner() != null && ob.getOwner().equals(li.getUniqueName()) ) {
                    count++;
                }
            }
        }
        return count;
    }

    public void removeFilesSharedByLocalIdentity(LocalIdentity li) {

        for (int x = getUploadManager().getModel().getItemCount()-1; x >= 0; x--) {
            FrostUploadItem item = (FrostUploadItem) getUploadManager().getModel().getItemAt(x);
            for(int y = item.getFrostUploadItemOwnerBoardList().size()-1; y >= 0; y-- ) {
                FrostUploadItemOwnerBoard ob = (FrostUploadItemOwnerBoard)item.getFrostUploadItemOwnerBoardList().get(y);
                if( ob.getOwner() != null && ob.getOwner().equals(li.getUniqueName()) ) {
                    item.getFrostUploadItemOwnerBoardList().remove(y);
                }
            }
            if( item.getFrostUploadItemOwnerBoardList().size() == 0 ) {
                // remove file, no more refs
                getUploadManager().getModel().removeItems(new ModelItem[] { item });
            }
        }
    }

    private SearchManager getSearchManager() {
        if (searchManager == null) {
            searchManager = new SearchManager(settings);
            searchManager.setMainFrame(mainFrame);
            searchManager.setDownloadModel(getDownloadManager().getModel());
            searchManager.setUploadModel(getUploadManager().getModel());
            searchManager.setTofTreeModel(tofTreeModel);
            searchManager.setKeypool(keypool);
            searchManager.setIdentities(identities);
        }
        return searchManager;
    }

    private UploadManager getUploadManager() {
        if (uploadManager == null) {
            uploadManager = new UploadManager(settings);
            uploadManager.setMainFrame(mainFrame);
            uploadManager.setTofTreeModel(tofTreeModel);
            uploadManager.setFreenetIsOnline(isOnline);
        }
        return uploadManager;
    }
    
    public NewUploadFilesManager getNewUploadFilesManager() {
        if( newUploadFilesManager == null ) {
            newUploadFilesManager = new NewUploadFilesManager();
        }
        return newUploadFilesManager;
    }

    /* (non-Javadoc)
     * @see frost.storage.Savable#save()
     */
    public void save() throws StorageException {
        getDownloadManager().getModel().save();
        getUploadManager().getModel().save();
        getNewUploadFilesManager().save();
    }
}
