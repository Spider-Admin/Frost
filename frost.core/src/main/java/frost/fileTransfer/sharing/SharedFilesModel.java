/*
  SharedFilesModel.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.sharing;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.filelist.FileListUploadThread;
import frost.storage.ExitSavable;
import frost.storage.StorageException;
import frost.storage.perst.FrostFilesStorage;
import frost.util.model.SortedModel;
import frost.util.model.SortedTableFormat;

/**
 * This is the model that stores all FrostUploadItems.
 *
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class SharedFilesModel extends SortedModel<FrostSharedFileItem> implements ExitSavable {

    // TODO: for shared directories: add new files to another table, waiting for owner assignment

    private static final Logger logger = Logger.getLogger(SharedFilesModel.class.getName());

    Timer timer;

    public SharedFilesModel(final SortedTableFormat<FrostSharedFileItem> f) {
        super(f);
    }

    /**
     * Will add this item to the model if not already in the model.
     * The new item must only have 1 FrostUploadItemOwnerBoard in its list.
     */
    public synchronized boolean addNewSharedFile(final FrostSharedFileItem itemToAdd, final boolean replacePathIfFileExists) {
        for (int x = 0; x < getItemCount(); x++) {
            final FrostSharedFileItem item = (FrostSharedFileItem) getItemAt(x);
            // add if file is not shared already
            if( itemToAdd.getSha().equals(item.getSha()) ) {
                // is already in list
                if( replacePathIfFileExists == false ) {
                    // ignore new file
                    return false;
                } else {
                    // renew file path
                    final File file = itemToAdd.getFile();
                    item.setLastModified(file.lastModified());
                    item.setFile(file);
                    item.setValid(true);
                    return true;
                }
            }
        }
        // not in model, add
        addItem(itemToAdd);

        // notify list upload thread that user changed something
        FileListUploadThread.getInstance().userActionOccured();

        return true;
    }

    /**
     * Will add this item to the model, no check for dups.
     */
    private synchronized void addConsistentSharedFileItem(final FrostSharedFileItem itemToAdd) {
        addItem(itemToAdd);
    }

    /**
     * Returns true if the model contains an item with the given key.
     */
    public synchronized boolean containsItemWithSha(final String sha) {
        for (int x = 0; x < getItemCount(); x++) {
            final FrostSharedFileItem sfItem = (FrostSharedFileItem) getItemAt(x);
            if (sfItem.getSha() != null && sfItem.getSha().equals(sha)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method tells all items to start uploading (if their current state allows it)
     */
    public synchronized void requestAllItems() {
        final Iterator<FrostSharedFileItem> iterator = data.iterator();
        while (iterator.hasNext()) {
            final FrostSharedFileItem sfItem =  iterator.next();
            if( !sfItem.isCurrentlyUploading() ) {
                FileTransferManager.inst().getUploadManager().getModel().addNewUploadItemFromSharedFile(sfItem);
            }
        }
    }

    /**
     * This method tells items passed as a parameter to start uploading (if their current state allows it)
     */
    public void requestItems(final List<FrostSharedFileItem> items) {
        for( final FrostSharedFileItem sfItem : items ) {
            if( !sfItem.isCurrentlyUploading() ) {
                FileTransferManager.inst().getUploadManager().getModel().addNewUploadItemFromSharedFile(sfItem);
            }
        }
    }

    /**
     * Initializes the model
     */
    public void initialize() throws StorageException {
        List<FrostSharedFileItem> uploadItems;
        try {
            uploadItems = FrostFilesStorage.inst().loadSharedFiles();
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Error loading shared file items", e);
            throw new StorageException("Error loading shared file items");
        }
        for( final FrostSharedFileItem di : uploadItems ) {
            addConsistentSharedFileItem(di); // no check for dups
        }
    }

    /**
     * Saves the upload model to database.
     */
    public void exitSave() throws StorageException {
        final List<FrostSharedFileItem> itemList = getItems();
        try {
            FrostFilesStorage.inst().saveSharedFiles(itemList);
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Error saving shared file items", e);
            throw new StorageException("Error saving shared file items");
        }
    }
}
