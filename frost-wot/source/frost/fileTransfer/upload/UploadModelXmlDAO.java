/*
 * Created on 06-dic-2004
 * 
 */
package frost.fileTransfer.upload;

import java.io.*;

import frost.SettingsClass;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class UploadModelXmlDAO implements UploadModelDAO {
	
	private static final String XML_FILENAME = "uploads.xml";
		
	private String directory;
	
	/**
	 * @param settings
	 */
	public UploadModelXmlDAO(SettingsClass settings) {
		directory = settings.getValue("config.dir");
	}

	/* (non-Javadoc)
	 * @see frost.fileTransfer.upload.UploadModelDAO#exists()
	 */
	public boolean exists() {
		File xmlFile = new File(directory + XML_FILENAME);
		if (xmlFile.length() == 0) {
			xmlFile.delete();
		}
		if (xmlFile.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see frost.fileTransfer.upload.UploadModelDAO#load(frost.fileTransfer.upload.UploadModel)
	 */
	public void load(UploadModel uploadModel) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see frost.fileTransfer.upload.UploadModelDAO#save(frost.fileTransfer.upload.UploadModel)
	 */
	public void save(UploadModel uploadModel) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see frost.fileTransfer.upload.UploadModelDAO#create()
	 */
	public void create() throws StorageException {
		File xmlFile = new File(directory + XML_FILENAME);
		try {
			boolean success = xmlFile.createNewFile();
			if (!success) {
				throw new StorageException("There was a problem while creating the storage.");
			}
		} catch (IOException ioe) {
			throw new StorageException("There was a problem while creating the storage.", ioe);
		}		
	}

}
