/*
  DownloadModel.java / Frost

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
import frost.fcp.fcp05.*;
import frost.fileTransfer.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.util.model.*;

/**
 * This is the model that stores all FrostDownloadItems. 
 * 
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class DownloadModel extends SortedModel implements Savable {
	
	private static final Logger logger = Logger.getLogger(DownloadModel.class.getName());

	public DownloadModel(SortedTableFormat f) {
		super(f);
	}
    
	/**
	 * Will add this item to model if not already in model.
	 */
	public synchronized boolean addDownloadItem(FrostDownloadItem itemToAdd) {
        
        FrostFileListFileObject flfToAdd = itemToAdd.getFileListFileObject(); // maybe null of manually added
        
		for (int x = 0; x < getItemCount(); x++) {
			FrostDownloadItem item = (FrostDownloadItem) getItemAt(x);
            FrostFileListFileObject flf = item.getFileListFileObject(); // maybe null of manually added
            
            if( flfToAdd != null && flf != null ) {
                if( flfToAdd.getSha().equals(flf.getSha()) ) {
                    // already in model (compared by SHA)
                    return false;
                }
            }

            // FIXME: 0.7: if we add a new uri chk/name also check if we already download chk!
            // Problem: what if CHK is wrong, then we have to add chk/name. But in the reverse case
            //          we add chk/name and name gets stripped because node reports rc=11, then we have 2 with same
            //          chk! ==> if node reports 11 then check if we have already same plain chk.
            
			if (itemToAdd.getKey() != null
				&& item.getKey() != null
				&& item.getKey().equals(itemToAdd.getKey())) 
            {
				// already in model (compared by key)
				return false;
			}
			
            // FIXME: also check downloaddir for same filename and build new name
            if (item.getFilename().equals(itemToAdd.getFilename())) {
				// same name, but different key. - rename quitely
				int cnt = 2;
				while (true) {
					String nextNewName = itemToAdd.getFilename() + "_" + cnt;
					itemToAdd.setFileName(nextNewName);
					if (addDownloadItem(itemToAdd) == true) {
						// added to model
						return true;
					}
					cnt++;
				}
				// we should never come here
			}
		}
		// not in model, add
		addItem(itemToAdd);
		return true;
	}
    
    public void addExternalItem(FrostDownloadItem i) {
        addItem(i);
    }
	
	/**
	 * Returns true if the model contains an item with the given sha.
	 */
	public synchronized boolean containsItemWithSha(String sha) {
		for (int x = 0; x < getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(x);
            FrostFileListFileObject flf = dlItem.getFileListFileObject();
            if( flf != null ) {
                if( flf.getSha().equals(sha) ) {
                    return true;
                }
            }
		}
		return false;
	}
	
	/**
	 * Removes finished downloads from the download model.
	 */
	public synchronized void removeFinishedDownloads() {
		ArrayList<FrostDownloadItem> items = new ArrayList<FrostDownloadItem>();
		for (int i = getItemCount() - 1; i >= 0; i--) {
			FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(i);
			if (dlItem.getState() == FrostDownloadItem.STATE_DONE) {
				items.add(dlItem);
			}
		}
		if (items.size() > 0) {
			FrostDownloadItem[] itemsArray = new FrostDownloadItem[items.size()];
			for (int i = 0; i < itemsArray.length; i++) {
				itemsArray[i] = (FrostDownloadItem) items.get(i);
			}
			removeItems(itemsArray);
		}
	}

    /**
     * Removes external downloads from the download model.
     */
    public synchronized void removeExternalDownloads() {
        ArrayList<FrostDownloadItem> items = new ArrayList<FrostDownloadItem>();
        for (int i = getItemCount() - 1; i >= 0; i--) {
            FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(i);
            if (dlItem.isExternal()) {
                items.add(dlItem);
            }
        }
        if (items.size() > 0) {
            FrostDownloadItem[] itemsArray = new FrostDownloadItem[items.size()];
            items.toArray(itemsArray);
            removeItems(itemsArray);
        }
    }

	/**
	 * Removes download items from the download model.
	 */
	public boolean removeItems(ModelItem[] items) {
		// First we remove the chunks from disk
		ArrayList<String> oldChunkFilesList = new ArrayList<String>(items.length);
		String dlDir = Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD);
		for (int i = 0; i < items.length; i++) {
			FrostDownloadItem item = (FrostDownloadItem) items[i];
			oldChunkFilesList.add(item.getFilename());
		}
		RemoveChunksThread t = new RemoveChunksThread(oldChunkFilesList, dlDir);
		t.start();
		
		// And now we remove the items from the model
		return super.removeItems(items);
	}
	
	/**
	 * Called to restart the item.
	 */
	public void restartItems(ModelItem[] items) {
		// TODO: stop thread

		for (int x = items.length - 1; x >= 0; x--) {
			FrostDownloadItem dlItem = (FrostDownloadItem) items[x];
			// reset only waiting+failed items
			if (dlItem.getState() == FrostDownloadItem.STATE_FAILED
				|| dlItem.getState() == FrostDownloadItem.STATE_WAITING
				|| dlItem.getState() == FrostDownloadItem.STATE_DONE) 
            {
				dlItem.setState(FrostDownloadItem.STATE_WAITING);
				dlItem.setRetries(0);
				dlItem.setLastDownloadStopTime(0);
				dlItem.setEnabled(Boolean.valueOf(true)); // enable download on restart
			}
		}
	}
	
	/**
	 * This method enables / disables download items in the model. If the
	 * enabled parameter is null, the current state of the item is inverted.
	 * @param enabled new state of the items. If null, the current state 
	 * 		  is inverted
	 */
	public synchronized void setAllItemsEnabled(Boolean enabled) {
		for (int x = 0; x < getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(x);
			if (dlItem.getState() != FrostDownloadItem.STATE_DONE) {
				dlItem.setEnabled(enabled);
			}
		}
	}
	
	/**
	 * This method enables / disables download items in the model. If the
	 * enabled parameter is null, the current state of the item is inverted.
	 * @param enabled new state of the items. If null, the current state 
	 * 		  is inverted
	 * @param items items to modify
	 */
	public void setItemsEnabled(Boolean enabled, ModelItem[] items) {
		for (int i = 0; i < items.length; i++) { 
			FrostDownloadItem item = (FrostDownloadItem) items[i];
			if (item.getState() != FrostDownloadItem.STATE_DONE) {
				item.setEnabled(enabled);
			}
		}
	}
	
//	/**
//	 * Removes all items from the download model.
//	 */
//	public synchronized void removeAllItems() {
//		//First we remove the chunks from disk
//		ArrayList oldChunkFilesList = new ArrayList(getItemCount());
//		String dlDir = Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD);
//		for (int i = 0; i < getItemCount(); i++) {
//			FrostDownloadItem item = (FrostDownloadItem) getItemAt(i);
//			oldChunkFilesList.add(item.getFileName());
//		}
//		RemoveChunksThread t = new RemoveChunksThread(oldChunkFilesList, dlDir);
//		t.start();
//		
//		//And now we remove the items from the model
//		clear();
//	}

	/**
	 * Saves the download model to database.
	 */
	public void save() throws StorageException {
        
        List<FrostDownloadItem> itemList = (List<FrostDownloadItem>)getItems();
        try {
            FrostFilesStorage.inst().saveDownloadFiles(itemList);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error saving download items", e);
            throw new StorageException("Error saving download items");
        }
	}
	
	/**
	 * Initializes the model
	 */
	public void initialize() throws StorageException {
        
        List<FrostDownloadItem> downloadItems; 
        try {
            downloadItems = FrostFilesStorage.inst().loadDownloadFiles();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error loading download items", e);
            throw new StorageException("Error loading download items");
        }
        for(Iterator<FrostDownloadItem> i=downloadItems.iterator(); i.hasNext(); ) {
            FrostDownloadItem di = i.next();
            addDownloadItem(di);
        }
	}
    
    private class RemoveChunksThread extends Thread {
        
        private ArrayList<String> oldChunkFilesList;
        private String dlDir;
        
        public RemoveChunksThread(ArrayList<String> al, String dlDir) {
            this.oldChunkFilesList = al; 
            this.dlDir = dlDir;
        }

        public void run() {
            File[] files = (new File(dlDir)).listFiles();
            for (int i = 0; i < oldChunkFilesList.size(); i++) {
                String filename = (String) oldChunkFilesList.get(i);
                for (int j = 0; j < files.length; j++) { 
                    // remove filename.data , .redirect, .checkblocks
                    if (files[j].getName().equals(filename + FecSplitfile.FILE_DATA_EXTENSION)
                        || files[j].getName().equals(filename + FecSplitfile.FILE_REDIRECT_EXTENSION)
                        || files[j].getName().equals(filename + FecSplitfile.FILE_CHECKBLOCKS_EXTENSION)) {
                        logger.info("Removing " + files[j].getName());
                        files[j].delete();
                    }
                }
            }
        }
    }
}
