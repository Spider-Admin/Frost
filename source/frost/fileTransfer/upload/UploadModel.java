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

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.objects.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.model.*;

/**
 * This is the model that stores all FrostUploadItems.
 *
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class UploadModel extends OrderedModel implements Savable {

    private static Logger logger = Logger.getLogger(UploadModel.class.getName());

    public UploadModel(SettingsClass frostSettings) {
        super();
    }

    /**
     * Will add this item to the model if not already in the model.
     * The new item must only have 1 FrostUploadItemOwnerBoard in its list.
     */
    public synchronized boolean addNewUploadItem(FrostUploadItem itemToAdd) {
        for (int x = 0; x < getItemCount(); x++) {
            FrostUploadItem item = (FrostUploadItem) getItemAt(x);
            // if file is already in list (sha1), maybe add a new owner/board if not a dup
            if( item.getSHA1().equals(itemToAdd.getSHA1()) ) {
                
                FrostUploadItemOwnerBoard obNew = (FrostUploadItemOwnerBoard)
                        itemToAdd.getFrostUploadItemOwnerBoardList().iterator().next();
                
                // check all old board/owner, only 1 owner/anonymous per board allowed!
                // FIXME: report board match, let user decide to change, but not add
                for(Iterator i=item.getFrostUploadItemOwnerBoardList().iterator(); i.hasNext(); ) {
                    FrostUploadItemOwnerBoard obOld = (FrostUploadItemOwnerBoard)i.next();
                    // compare boards case insensitive!
                    if( obNew.getTargetBoard().getName().equalsIgnoreCase(obOld.getTargetBoard().getName()) ) {
                        return false; // only one owner per board
//                        if( obNew.getOwner() == null && obOld.getOwner() == null ) {
//                            // both anonymous, double
//                            return false;
//                        } else if( obNew.getOwner() == null && obOld.getOwner() != null ) {
//                            // different
//                        } else if( obNew.getOwner() != null && obOld.getOwner() == null ) {
//                            // different
//                        } else if( obNew.getOwner().equals(obOld.getOwner())) {
//                            // same owner, double
//                            return false;
//                        }
                    }
                }
                // we are here because the file is already in table, but we have to add a new board/owner
                item.addFrostUploadItemOwnerBoard(obNew);
                return true; // item added, there no more items with same SHA1 in list
            }
        }
        // not in model, add
        addItem(itemToAdd);
        return true;
    }

    /**
     * Will add this item to the model, no check for dups.
     */
    public synchronized void addConsistentUploadItem(FrostUploadItem itemToAdd) {
        addItem(itemToAdd);
    }

    /**
     * Returns true if the model contains an item with the given key.
     */
    public synchronized boolean containsItemWithKey(String key) {
        for (int x = 0; x < getItemCount(); x++) {
            FrostUploadItem ulItem = (FrostUploadItem) getItemAt(x);
            if (ulItem.getSHA1() != null && ulItem.getSHA1().equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method removes from the model the items whose associated files
     * no longer exist on hard disk. Using this method may be very expensive
     * if the model has a lot of items.
     */
    public synchronized void removeNotExistingFiles() {
        ArrayList items = new ArrayList();
        for (int i = getItemCount() - 1; i >= 0; i--) {
            FrostUploadItem ulItem = (FrostUploadItem) getItemAt(i);
            File checkMe = new File(ulItem.getFilePath());
            if (!checkMe.exists()) {
                items.add(ulItem);
            }
        }
        if (items.size() > 0) {
            FrostUploadItem[] itemsArray = new FrostUploadItem[items.size()];
            for (int i = 0; i < itemsArray.length; i++) {
                itemsArray[i] = (FrostUploadItem) items.get(i);
            }
            removeItems(itemsArray);
        }
    }

    /**
     * This method tells all items to start uploading (if their current state
     * allows it)
     */
    public synchronized void requestAllItems() {
        Iterator iterator = data.iterator();
        while (iterator.hasNext()) {
            FrostUploadItem ulItem = (FrostUploadItem) iterator.next();
            // Since it is difficult to identify the states where we are allowed to
            // start an upload we decide based on the states in which we are not allowed
            if (ulItem.getState() != FrostUploadItem.STATE_UPLOADING
                && ulItem.getState() != FrostUploadItem.STATE_PROGRESS
                && ulItem.getState() != FrostUploadItem.STATE_ENCODING) {
                ulItem.setRetries(0);
                ulItem.setLastUploadStopTimeMillis(0);
                ulItem.setEnabled(Boolean.valueOf(true));
                ulItem.setState(FrostUploadItem.STATE_REQUESTED);
            }
        }
    }

    /**
     * This method tells items passed as a parameter to start uploading
     * (if their current state allows it)
     */
    public void requestItems(ModelItem[] items) {
        for (int i = 0; i < items.length; i++) {
            FrostUploadItem ulItem = (FrostUploadItem) items[i];
            // Since it is difficult to identify the states where we are allowed to
            // start an upload we decide based on the states in which we are not allowed
            if (ulItem.getState() != FrostUploadItem.STATE_UPLOADING
                && ulItem.getState() != FrostUploadItem.STATE_PROGRESS
                && ulItem.getState() != FrostUploadItem.STATE_ENCODING) {
                ulItem.setRetries(0);
                ulItem.setLastUploadStopTimeMillis(0);
                ulItem.setEnabled(Boolean.valueOf(true));
                ulItem.setState(FrostUploadItem.STATE_REQUESTED);
            }
        }
    }

    /**
     * This method tells items passed as a parameter to generate their chks
     * (if their current state allows it)
     */
    public void generateChkItems(ModelItem[] items) {
        for (int i = 0; i < items.length; i++) {
            FrostUploadItem ulItem = (FrostUploadItem) items[i];
            // Since it is difficult to identify the states where we are allowed to
            // start an upload we decide based on the states in which we are not allowed
            // start gen chk only if IDLE
            if (ulItem.getState() == FrostUploadItem.STATE_IDLE && ulItem.getKey() == null) {
                ulItem.setState(FrostUploadItem.STATE_ENCODING_REQUESTED);
            }
        }
    }

    /**
     * Adds a prefix to the filenames of the items passed as a parameter
     */
    public void setPrefixToItems(ModelItem[] items, String prefix) {
        for (int i = 0; i < items.length; i++) {
            FrostUploadItem ulItem = (FrostUploadItem) items[i];
            String newName = prefix + ulItem.getFileName();
            ulItem.setFileName(newName);
        }
    }

    /**
     * Restores the original filenames of the items passed as a parameter
     */
    public void removePrefixFromItems(ModelItem[] items) {
        for (int i = 0; i < items.length; i++) {
            FrostUploadItem ulItem = (FrostUploadItem) items[i];
            File origFile = new File(ulItem.getFilePath());
            if (origFile.isFile()) {
                ulItem.setFileName(origFile.getName());
            }
        }
    }

    /**
     * Restores the original filenames of all items
     */
    public synchronized void removePrefixFromAllItems() {
        Iterator iterator = data.iterator();
        while (iterator.hasNext()) {
            FrostUploadItem ulItem = (FrostUploadItem) iterator.next();
            File origFile = new File(ulItem.getFilePath());
            if (origFile.isFile()) {
                ulItem.setFileName(origFile.getName());
            }
        }
    }

    /**
     * Adds a prefix to the filenames of all items
     */
    public synchronized void setPrefixToAllItems(String prefix) {
        Iterator iterator = data.iterator();
        while (iterator.hasNext()) {
            FrostUploadItem ulItem = (FrostUploadItem) iterator.next();
            String newName = prefix + ulItem.getFileName();
            ulItem.setFileName(newName);
        }
    }

    /**
     * Initializes the model
     */
    public void initialize() throws StorageException {
        List uploadItems; 
        try {
            uploadItems = AppLayerDatabase.getUploadFilesDatabaseTable().loadUploadFiles();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading upload items", e);
            throw new StorageException("Error loading upload items");
        }
        for(Iterator i=uploadItems.iterator(); i.hasNext(); ) {
            FrostUploadItem di = (FrostUploadItem)i.next();
            addConsistentUploadItem(di); // no check for dups
        }
    }
    
    /**
     * Saves the upload model to database.
     */
    public void save() throws StorageException {
        LinkedList itemList = new LinkedList();
        for (int x = 0; x < getItemCount(); x++) {
            FrostUploadItem uploadItem = (FrostUploadItem)getItemAt(x);
            itemList.add(uploadItem);
        }
        
        try {
            AppLayerDatabase.getUploadFilesDatabaseTable().saveUploadFiles(itemList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving upload items", e);
            throw new StorageException("Error saving upload items");
        }
    }
    
    public List getUploadItemsToShare(Board board, String owner, int maxItems, long minDate) {
        LinkedList result = new LinkedList();
//System.out.println("share:"+board.getName()+","+owner);

        for(Iterator i=data.iterator(); i.hasNext(); ) {
            FrostUploadItem ulItem = (FrostUploadItem) i.next();
            for(Iterator j=ulItem.getFrostUploadItemOwnerBoardList().iterator(); j.hasNext(); ) {
                FrostUploadItemOwnerBoard ob = (FrostUploadItemOwnerBoard)j.next();
//System.out.println("subshare:"+ob.getTargetBoard().getName()+","+ob.getOwner());
                if( ob.getTargetBoard().getName().equals(board.getName()) &&
                    ( (ob.getOwner()==null && owner==null) || // anonymous
                      (ob.getOwner()!=null && owner!=null && ob.getOwner().equals(owner)) ) // identity matches      
                  ) 
                {
                    // potential item, check when it was last shared
                    if( ob.getLastSharedDate() == null || ob.getLastSharedDate().getTime() < minDate ) {
                        // never shared, or updated so it must be reshared, or too long not shared
                        result.add(ob);

                        if( result.size() >= maxItems ) {
                            return result;
                        }
                    }
                }
            }
        }
        System.out.println("ret="+result.size());
        return result;
    }
//    FIXME: problem: gross/kleinschreibung von boards!
}
