/*
 * Created on Nov 4, 2004
 *
 */
package frost.storage;

import frost.SettingsClass;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.identities.*;

/**
 * @author $author$
 * @version $revision$
 */
public class XmlDAOFactory extends DAOFactory {
	
	private SettingsClass settings;
	
	/**
	 * @param settings
	 */
	protected XmlDAOFactory(SettingsClass settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see frost.storage.DAOFactory#getIdentitiesDAO()
	 */
	public IdentitiesDAO getIdentitiesDAO() {
		return new IdentitiesXmlDAO();
	}

	/* (non-Javadoc)
	 * @see frost.storage.DAOFactory#getUploadModelDAO()
	 */
	public UploadModelDAO getUploadModelDAO() {
		return new UploadModelXmlDAO(settings);
	}

	/* (non-Javadoc)
	 * @see frost.storage.DAOFactory#getDownloadModelDAO()
	 */
	public DownloadModelDAO getDownloadModelDAO() {
		return new DownloadModelXmlDAO(settings);
	}
}
