/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.upload;

import java.io.File;
import java.util.*;

import frost.SettingsClass;
import frost.gui.TableXmlIO;
import frost.storage.Savable;
import frost.util.model.*;

/**
 * This is the model that stores all FrostUploadItems. 
 * 
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class UploadModel extends OrderedModel implements Savable {

	private SettingsClass settings;

	/**
	 * 
	 */
	public UploadModel(SettingsClass frostSettings) {
		super();
		settings = frostSettings;
	}

	/**
	 * Will add this item to the model if not already in the model.
	 */
	public synchronized boolean addUploadItem(FrostUploadItem itemToAdd) {
		for (int x = 0; x < getItemCount(); x++) {
			FrostUploadItem item = (FrostUploadItem) getItemAt(x);
			if (item.getFilePath().equals(itemToAdd.getFilePath())) {
				// already in model (compared by path)
				return false;
			}
		}
		// not in model, add
		addItem(itemToAdd);
		return true;
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
	 * Saves the upload model to disk.
	 */
	public boolean save() {
		String filename = settings.getValue("config.dir") + "uploads.xml";
		File check = new File(filename);
		if (check.exists()) {
			// rename old file to .bak, overwrite older .bak
			String bakFilename = settings.getValue("config.dir") + "uploads.xml.bak";
			File bakFile = new File(bakFilename);
			if (bakFile.exists()) {
				bakFile.delete();
			}
			check.renameTo(bakFile);
		}
		return TableXmlIO.saveUploadModel(this, filename);
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
	 * Loads the upload model from disk
	 */
	public boolean load() {
		String filename = settings.getValue("config.dir") + "uploads.xml";
		// the call changes the tablemodel and loads nodes into it
		File iniFile = new File(filename);
		if (iniFile.exists() == false) {
			return true; // nothing loaded, but no error
		}
		return TableXmlIO.loadUploadModel(this, filename);
	}

}
