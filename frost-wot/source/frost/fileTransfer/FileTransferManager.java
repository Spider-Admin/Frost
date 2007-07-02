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
import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.search.*;
import frost.fileTransfer.sharing.*;
import frost.fileTransfer.upload.*;
import frost.identities.*;
import frost.storage.*;
import frost.util.model.*;

public class FileTransferManager implements Savable {

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
        getUploadManager().initialize( (List<FrostSharedFileItem>)getSharedFilesManager().getModel().getItems() );
        getNewUploadFilesManager().initialize();
        
        if( PersistenceManager.isPersistenceEnabled() && Core.isFreenetOnline() ) {

            boolean wasOk = ((FcpHandler07)FcpHandler.inst()).initializePersistence();
            if( !wasOk ) {
                System.out.println("FAILED TO ESTABLISH THE PERSISTENT CONNECTION!");
            } else {
                persistenceManager = new PersistenceManager(getUploadManager().getModel(), getDownloadManager().getModel());
            }
        }
        
        // call order is order of panels in gui
        MainFrame mainFrame = MainFrame.getInstance();
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
    
    public int countFilesSharedByLocalIdentity(LocalIdentity li) {
        int count = 0;
        for (int x = 0; x < getSharedFilesManager().getModel().getItemCount(); x++) {
            FrostSharedFileItem item = (FrostSharedFileItem) getSharedFilesManager().getModel().getItemAt(x);
            if( item.getOwner().equals( li.getUniqueName()) ) {
                count++;
            }
        }
        return count;
    }

    public void removeFilesSharedByLocalIdentity(LocalIdentity li) {
        
        // remove from uploadtable
        for (int x = getUploadManager().getModel().getItemCount()-1; x >= 0; x--) {
            Object obj = (FrostUploadItem) getUploadManager().getModel().getItemAt(x);
            if( !(obj instanceof FrostSharedFileItem) ) {
                continue;
            }
            FrostSharedFileItem suf = (FrostSharedFileItem)obj;
            if( suf.getOwner().equals(li.getUniqueName()) ) {
                getUploadManager().getModel().removeItems(new ModelItem[] { suf });
            }
        }
        
        // remove from sharedfiles table
        for (int x = 0; x < getSharedFilesManager().getModel().getItemCount(); x++) {
            FrostSharedFileItem item = (FrostSharedFileItem) getSharedFilesManager().getModel().getItemAt(x);
            if( item.getOwner().equals( li.getUniqueName()) ) {
                getSharedFilesManager().getModel().removeItems(new ModelItem[] { item });
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

    public void save() throws StorageException {
        getDownloadManager().save();
        getUploadManager().save();
        getSharedFilesManager().save();
        getNewUploadFilesManager().save();
    }
}
