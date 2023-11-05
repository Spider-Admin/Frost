/*
  UploadModel.java / Frost
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.MainFrame;
import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.sharing.FrostSharedFileItem;
import frost.storage.ExitSavable;
import frost.storage.StorageException;
import frost.storage.perst.FrostFilesStorage;
import frost.util.gui.translation.Language;
import frost.util.model.SortedModel;
import frost.util.model.SortedTableFormat;

/**
 * This is the model that stores all FrostUploadItems.
 *
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class UploadModel extends SortedModel<FrostUploadItem> implements ExitSavable {

	private static final Logger logger = LoggerFactory.getLogger(UploadModel.class);

    public UploadModel(final SortedTableFormat<FrostUploadItem> f) {
        super(f);
    }

    public boolean addNewUploadItemFromSharedFile(final FrostSharedFileItem sfi) {
        final FrostUploadItem newUlItem = new FrostUploadItem(sfi.getFile(), true);
        newUlItem.setSharedFileItem(sfi);
        return addNewUploadItem(newUlItem);
    }

    public void addExternalItem(final FrostUploadItem i) {
        addItem(i);
    }

    /**
     * Will add this item to the model if not already in the model.
     * The new item must only have 1 FrostUploadItemOwnerBoard in its list.
     */
    public synchronized boolean addNewUploadItem(final FrostUploadItem itemToAdd) {

        final String pathToAdd = itemToAdd.getFile().getPath();

        for (int x = 0; x < getItemCount(); x++) {
            final FrostUploadItem item = getItemAt(x);
            // add if file is not already in list (path)
            // if we add a shared file and the same file is already in list (manually added), we connect them
            if( pathToAdd.equals(item.getFile().getPath()) ) {
                // file with same path is already in list
                if( itemToAdd.isSharedFile() && !item.isSharedFile() ) {
                    // to shared file to manually added upload item
                    item.setSharedFileItem( itemToAdd.getSharedFileItem() );
                    return true;
                } else {
                    return false; // don't add 2 files with same path
                }
            }
        }
        // not in model, add
        addItem(itemToAdd);
        return true;
    }
    
    
    public void addUploadItemList(List<FrostUploadItem> frostUploadItemList){
    	for(FrostUploadItem frostUploadItem : frostUploadItemList) {
    		addNewUploadItem(frostUploadItem);
    	}
    }

    /**
     * Will add this item to the model, no check for dups.
     */
    private synchronized void addConsistentUploadItem(final FrostUploadItem itemToAdd) {
        addItem(itemToAdd);
    }

    /**
     * if upload was successful, remove item from uploadtable
     */
    public void notifySharedFileUploadWasSuccessful(final FrostUploadItem frostUploadItemToRemove) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            
        	final FrostUploadItem ulItem =  getItemAt(i);

            if( ulItem == frostUploadItemToRemove ) {
            	// remove this item
            	final List<FrostUploadItem> frostUploadiItems = new ArrayList<FrostUploadItem>();
            	frostUploadiItems.add(frostUploadItemToRemove);
                removeItems(frostUploadiItems );
                break;
            }
        }
    }

    /**
     * This method removes from the model the items whose associated files
     * no longer exist on hard disk. Using this method may be very expensive
     * if the model has a lot of items.
     */
    public synchronized void removeNotExistingFiles() {
        final ArrayList<FrostUploadItem> items = new ArrayList<FrostUploadItem>();
        
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final FrostUploadItem ulItem = getItemAt(i);
            
            if( ulItem.isExternal() ) {
                continue;
            }
            if (!ulItem.getFile().exists()) {
                items.add(ulItem);
                logger.warn("Upload items file does not exist, removed from upload files: {}", ulItem.getFile().getPath());
            
            } else if( ulItem.getFileSize() != ulItem.getFile().length() ){
                items.add(ulItem);
                logger.warn("Upload items file size changed, removed from upload files: {}", ulItem.getFile().getPath());
            }
        }
        if (items.size() > 0) {
            removeItems(items);

            // don't notify user that files were removed
            final Language language = Language.getInstance();
            final String title = language.getString("UploadPane.invalidUploadFilesRemoved.title");
            final String text = language.getString("UploadPane.invalidUploadFilesRemoved.text");
            JOptionPane.showMessageDialog(
                    MainFrame.getInstance(),
                    text,
                    title,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method tells items passed as a parameter to start uploading
     * (if their current state allows it)
     */
    public void uploadItems(final List<FrostUploadItem> items) {
        for( final FrostUploadItem ulItem : items ) {
            if (ulItem.getState() == FrostUploadItem.STATE_FAILED
                || ulItem.getState() == FrostUploadItem.STATE_DONE)
            {
                ulItem.setRetries(0);
                ulItem.setLastUploadStopTimeMillis(0);
                ulItem.setEnabled(Boolean.valueOf(true));
                ulItem.setState(FrostUploadItem.STATE_WAITING);
            }
        }
    }

    /**
     * This method tells items passed as a parameter to generate their chks
     * (if their current state allows it).
     * For shared files no CHK will be generated until we uploaded the file.
     */
    public void generateChkItems(final List<FrostUploadItem> items) {
        for( final FrostUploadItem ulItem : items ) {
            // Since it is difficult to identify the states where we are allowed to
            // start an upload we decide based on the states in which we are not allowed
            // start gen chk only if IDLE
            if ( (ulItem.getState() == FrostUploadItem.STATE_WAITING
                    || ulItem.getState() == FrostUploadItem.STATE_FAILED)
                 && ulItem.getKey() == null
                 && !ulItem.isSharedFile() )
            {
                ulItem.setState(FrostUploadItem.STATE_ENCODING_REQUESTED);
            }
        }
    }

    /**
     * Removes finished uploads from the model.
     */
    public synchronized void removeFinishedUploads() {
        final ArrayList<FrostUploadItem> items = new ArrayList<FrostUploadItem>();
        
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final FrostUploadItem ulItem = getItemAt(i);
            if (ulItem.getState() == FrostUploadItem.STATE_DONE) {
                items.add(ulItem);
            }
        }
        if (items.size() > 0) {
            removeItems(items);
        }
    }

    /**
     * Removes external uploads from the model.
     */
    public synchronized void removeExternalUploads() {
        final ArrayList<FrostUploadItem> items = new ArrayList<FrostUploadItem>();
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final FrostUploadItem ulItem = getItemAt(i);
            if (ulItem.isExternal()) {
                items.add(ulItem);
            }
        }
        if (items.size() > 0) {
            removeItems(items);
        }
    }

    /**
     * Initializes and loads the model
     */
    public void initialize(final List<FrostSharedFileItem> sharedFiles) throws StorageException {
        final List<FrostUploadItem> uploadItems;
        try {
            uploadItems = FrostFilesStorage.inst().loadUploadFiles(sharedFiles);
        } catch (final Throwable e) {
            logger.error("Error loading upload items", e);
            throw new StorageException("Error loading upload items");
        }
        for( final FrostUploadItem di : uploadItems ) {
            addConsistentUploadItem(di); // no check for dups
        }
    }

    /**
     * Saves the upload model to database.
     */
    public void exitSave() throws StorageException {
        final List<FrostUploadItem> itemList = getItems();
        try {
            FrostFilesStorage.inst().saveUploadFiles(itemList);
        } catch (final Throwable e) {
            logger.error("Error saving upload items", e);
            throw new StorageException("Error saving upload items");
        }
    }

    /**
     * This method enables / disables download items in the model. If the
     * enabled parameter is null, the current state of the item is inverted.
     * @param enabled new state of the items. If null, the current state
     *        is inverted
     */
    public synchronized void setAllItemsEnabled(final Boolean enabled) {
        for (int x = 0; x < getItemCount(); x++) {
            final FrostUploadItem ulItem = getItemAt(x);
            if (ulItem.getState() != FrostUploadItem.STATE_DONE) {
                ulItem.setEnabled(enabled);
                FileTransferManager.inst().getUploadManager().notifyUploadItemEnabledStateChanged(ulItem);
            }
        }
    }

    /**
     * This method enables / disables download items in the model. If the
     * enabled parameter is null, the current state of the item is inverted.
     * @param enabled new state of the items. If null, the current state
     *        is inverted
     * @param items items to modify
     */
    public void setItemsEnabled(final Boolean enabled, final List<FrostUploadItem> items) {
        for( final FrostUploadItem item : items ) {
            if (item.getState() != FrostUploadItem.STATE_DONE) {
                item.setEnabled(enabled);
                FileTransferManager.inst().getUploadManager().notifyUploadItemEnabledStateChanged(item);
            }
        }
    }
}
