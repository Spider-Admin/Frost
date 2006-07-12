/*
 DAOFactory.java / Frost
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

import frost.*;
import frost.messaging.*;

/**
 * This class implements the Abstract Factory pattern. It is used by the business objects
 * to get the concrete factory for the specific storage method they use.
 * 
 * @pattern Abstract Factory
 * 
 * @author $author$
 * @version $revision$
 */
public abstract class DAOFactory {

	//List of DAO types supported by this factory
	public static final int XML = 1;
	
	private static XmlDAOFactory xmlDAOFactory = null;
	
	private static SettingsClass settings;
	
	/**
	 * This method returns the concrete DAOFactory implementation
	 * for the storage method whose id is passed as a parameter.
	 * 
	 * @param storageMethod id of the storage method of the wanted factory
	 * @return the concrete factory for the given storage method
	 */
	public static DAOFactory getFactory(int storageMethod) {
		switch (storageMethod) {
			case XML:
				return getXmlDAOFactory();
			default:
				return null;
		}
	}
	
	/**
	 * This method returns the XmlDAOFactory, creating it if necessary.
	 * @return the XmlDAOFactory
	 */
	private static XmlDAOFactory getXmlDAOFactory() {
		if (xmlDAOFactory == null) {
			xmlDAOFactory = new XmlDAOFactory(settings);
		}	
		return xmlDAOFactory;
	}
	
	/**
	 * This method returns a data access object for the MessageHashes
	 * business object.
	 * @return a MessageHashesDAO for the MessageHashes business object.
	 */
	public abstract MessageHashesDAO getMessageHashesDAO();

	/**
	 * This method initializes the DAOFactory with the given SettingsClass.
	 * @param frostSettings
	 */
	public static void initialize(SettingsClass frostSettings) {
		settings = frostSettings;		
	}
}
