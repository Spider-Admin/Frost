/*
 * Created on Nov 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.identities;

import frost.storage.StorageException;

/**
 * @author $author$
 * @version $revision$
 */
public interface IdentitiesDAO {
	/**
	 * This method checks if underlying storage exists. If it does, it
	 * returns true. If it doesn't (for instance, when the application
	 * is started for the first time) it returns false.
	 * @return true if the underlying storage exists. False otherwise.
	 */
	public boolean exists();
	
	/**
	 * This method loads the information contained in the storage and fills
	 * the given FrostIdentities object with it.
	 * @param identities FrostIdentities object to be filled with the information
	 * in the storage
	 * @throws StorageException if there was a problem while loading the information.
	 */
	public void load(FrostIdentities identities) throws StorageException;
	/**
	 * This method creates the underlying storage.
	 * @throws StorageException if there was a problem while creating the storage.
	 */
	public void create() throws StorageException;
}
