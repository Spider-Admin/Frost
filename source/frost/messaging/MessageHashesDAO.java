/*
 * Created on 27-ene-2005
 * 
 */
package frost.messaging;

import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public interface MessageHashesDAO {
	/**
	 * This method checks if underlying storage exists. If it does, it
	 * returns true. If it doesn't (for instance, when the application
	 * is started for the first time) it returns false.
	 * @return true if the underlying storage exists. False otherwise.
	 */
	public boolean exists();
	
	/**
	 * This method loads the information contained in the storage and fills
	 * the given MessageHashes object with it.
	 * @param messageHashes MessageHashes object to be filled with the information
	 * in the storage
	 * @throws StorageException if there was a problem while loading the information.
	 */
	public void load(MessageHashes messageHashes) throws StorageException;
	
	/**
	 * This method saves the information contained in the given MessageHashes object
	 * on the storage.
	 * @param messageHashes MessageHashes whose information is going to be saved
	 * @throws StorageException if there was a problem while saving the information.
	 */
	public void save(MessageHashes messageHashes) throws StorageException;
	
	/**
	 * This method creates the underlying storage.
	 * @throws StorageException if there was a problem while creating the storage.
	 */
	public void create() throws StorageException;
}
