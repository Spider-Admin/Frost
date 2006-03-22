/*
 XmlDAOFactory.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage;

import frost.SettingsClass;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.identities.*;
import frost.messaging.*;

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

	/* (non-Javadoc)
	 * @see frost.storage.DAOFactory#getMessageHashesDAO()
	 */
	public MessageHashesDAO getMessageHashesDAO() {
		return new MessageHashesXmlDAO();
	}
}
