/*
 * Created on 06-dic-2004
 * 
 */
package frost.fileTransfer.upload;

import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public interface UploadModelDAO {
	/**
	 * This method checks if underlying storage exists. If it does, it
	 * returns true. If it doesn't (for instance, when the application
	 * is started for the first time) it returns false.
	 * @return true if the underlying storage exists. False otherwise.
	 */
	public boolean exists();
	
	/**
	 * This method loads the information contained in the storage and fills
	 * the given UploadModel object with it.
	 * @param uploadModel UploadModel object to be filled with the information
	 * in the storage
	 * @throws StorageException if there was a problem while loading the information.
	 */
	public void load(UploadModel uploadModel) throws StorageException;
	
	/**
	 * This method saves the information contained in the given UploadModel object
	 * on the storage.
	 * @param uploadModel UploadModel whose information is going to be saved
	 * @throws StorageException if there was a problem while saving the information.
	 */
	public void save(UploadModel uploadModel) throws StorageException;
	
	/**
	 * This method creates the underlying storage.
	 * @throws StorageException if there was a problem while creating the storage.
	 */
	public void create() throws StorageException;
}
