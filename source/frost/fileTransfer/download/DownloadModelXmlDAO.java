/*
 * Created on 08-dic-2004
 * 
 */
package frost.fileTransfer.download;

import java.io.*;

import frost.SettingsClass;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class DownloadModelXmlDAO implements DownloadModelDAO {

	private static final String XML_FILENAME = "downloads.xml";
	private static final String TMP_FILENAME = "downloads.xml.tmp";
	private static final String BAK_FILENAME = "downloads.xml.bak";
	
	private String directory;

	/**
	 * @param settings
	 */
	public DownloadModelXmlDAO(SettingsClass settings) {
		directory = settings.getValue("config.dir");
	}
	
	/* (non-Javadoc)
	 * @see frost.fileTransfer.download.DownloadModelDAO#create()
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
	
	/* (non-Javadoc)
	 * @see frost.fileTransfer.download.DownloadModelDAO#exists()
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
	 * @see frost.fileTransfer.download.DownloadModelDAO#load(frost.fileTransfer.download.DownloadModel)
	 */
	public void load(DownloadModel DownloadModel) throws StorageException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see frost.fileTransfer.download.DownloadModelDAO#save(frost.fileTransfer.download.DownloadModel)
	 */
	public void save(DownloadModel downloadModel) throws StorageException {
		// TODO Auto-generated method stub

	}

}
