/*
  IdentitiesXmlDAO.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.identities;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.storage.*;

public class IdentitiesXmlDAO implements IdentitiesDAO {
	
	private static Logger logger = Logger.getLogger(IdentitiesXmlDAO.class.getName());
	
//	private static final String OLD_FILENAME = "identities";
	private static final String XML_FILENAME = "identities.xml";
	private static final String TMP_FILENAME = "identities.xml.tmp";
	private static final String BAK_FILENAME = "identities.xml.bak";
	
	/* (non-Javadoc)
	 * @see frost.identities.IdentitiesDAO#exists()
	 */
	public boolean exists() {
//		File oldFile = new File(OLD_FILENAME);
		File xmlFile = new File(XML_FILENAME);
//		if (oldFile.length() == 0) {
//			oldFile.delete();
//		}
		if (xmlFile.length() == 0) {
			xmlFile.delete();
		}
		if (/*oldFile.exists() || */ xmlFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param identities
	 */
	private void loadNewFormat(FrostIdentities identities) throws SAXException, IllegalArgumentException {
		logger.info("Trying to create/load ids");
		Document d = XMLTools.parseXmlFile(XML_FILENAME, false);
		Element rootEl = d.getDocumentElement();
        
		//first myself
		Element myself = (Element) XMLTools.getChildElementsByTagName(rootEl, "MyIdentity").get(0);
        LocalIdentity myId = null;
        if( myself != null ) {
    		myId = new LocalIdentity(myself);
    		identities.setMyId(myId);
        } else {
            identities.setMyId(null); // an imported identities file without myId
        }

		//then friends, enemies and neutrals
		List lists = XMLTools.getChildElementsByTagName(rootEl, "BuddyList");
		Iterator it = lists.iterator();
		BuddyList friends = identities.getFriends();
		BuddyList enemies = identities.getEnemies();
		BuddyList neutrals = identities.getNeutrals();
		while (it.hasNext()) {
			Element current = (Element) it.next();
			if (current.getAttribute("type").equals("friends")) {
				friends.loadXMLElement(current);
			} else if (current.getAttribute("type").equals("enemies")) {
				enemies.loadXMLElement(current);
			} else if (current.getAttribute("type").equals("neutral")) {
				neutrals.loadXMLElement(current);
			}
		}
		logger.info("Loaded " + friends.size() + " friends and " + enemies.size() + " enemies and "
				+ neutrals.size() + " neutrals.");

        // TODO: remove
//		if (myId != null && friends.add(myId)) {
//			logger.info("Added myself to the friends list");
//		}
	}
	
	/**
	 * @param identities
	 */
//	private void loadOldFormat(FrostIdentities identities, File oldFile) throws IOException {
//		BufferedReader fin = new BufferedReader(new FileReader(oldFile));
//		String name = fin.readLine();
//		String address = fin.readLine();
//		String keys[] = new String[2];
//		keys[1] = fin.readLine();
//		keys[0] = fin.readLine();
//		if (address.startsWith("CHK@") == false) {
//			// pubkey chk was not successfully computed
//			byte[] pubkeydata;
//			try {
//				pubkeydata = keys[1].getBytes("UTF-8");
//			} catch (UnsupportedEncodingException ex) {
//				pubkeydata = keys[1].getBytes();
//			}
//
//			String tmp = FecTools.generateCHK(pubkeydata);
//			address = tmp.substring(tmp.indexOf("CHK@"), tmp.indexOf("CHK@") + 58);
//			logger.info("Re-calculated my public key CHK: " + address + "\n");
//
//		}
//		LocalIdentity myId = new LocalIdentity(name, keys);
//		identities.setMyId(myId);
//		logger.info("Loaded myself with name " + myId.getName());
//
//		//take out the ****
//		fin.readLine();
//		
//		//process the friends
//		BuddyList friends = identities.getFriends();
//		boolean stop = false;
//		String key;
//		while (!stop) {
//			name = fin.readLine();
//			if (name == null || name.startsWith("***"))
//				break;
//			address = fin.readLine();
//			key = fin.readLine();
//			friends.add(new Identity(name, key));
//		}
//		logger.info("Loaded " + friends.size() + " friends");
//
//		//just the good ids
//		Hashtable goodIds = identities.getGoodIds();
//		while (!stop) {
//			String id = fin.readLine();
//			if (id == null || id.startsWith("***"))
//				break;
//			goodIds.put(id, id);
//		}
//		logger.info("Loaded " + goodIds.size() + " good ids");
//
//		//and the enemies
//		BuddyList enemies = identities.getEnemies();
//		while (!stop) {
//			name = fin.readLine();
//			if (name == null || name.startsWith("***"))
//				break;
//			address = fin.readLine();
//			key = fin.readLine();
//			enemies.add(new Identity(name, key));
//		}
//		logger.info("Loaded " + enemies.size() + " enemies");
//
//		//and the bad ids
//		Hashtable badIds = identities.getBadIds();
//		while (!stop) {
//			String id = fin.readLine();
//			if (id == null || id.startsWith("***"))
//				break;
//			badIds.put(id, id);
//		}
//		logger.info("Loaded " + badIds.size() + " bad ids");
//		if (friends.add(myId)) {
//			logger.info("Added myself to the friends list");
//		}
//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see frost.identities.IdentitiesDAO#load(frost.identities.FrostIdentities)
	 */
	public void load(FrostIdentities identities) throws StorageException {
//		File oldFile = new File(OLD_FILENAME);
		File xmlFile = new File(XML_FILENAME);
		
		if (xmlFile.exists()) {
			try {
				loadNewFormat(identities);
//				oldFile.delete();		//In case we have an old file hanging around, we delete it.
			} catch (Exception e) {
				throw new StorageException("Exception while loading the new identities format.", e);
			}
		} 
//        else {
//			try {
//				loadOldFormat(identities, oldFile);
//			} catch (Exception ioe) {
//				throw new StorageException("Exception while loading the old identities format.", ioe);
//			}
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see frost.identities.IdentitiesDAO#create()
	 */
	public void create() throws StorageException {
		File xmlIdentities = new File(XML_FILENAME);
		try {
			boolean success = xmlIdentities.createNewFile();
			if (!success) {
				throw new StorageException("There was a problem while creating the storage.");
			}
		} catch (IOException ioe) {
			throw new StorageException("There was a problem while creating the storage.", ioe);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see frost.identities.IdentitiesDAO#save(frost.identities.FrostIdentities)
	 */
	public void save(FrostIdentities identities) throws StorageException {
		logger.info("Saving " + XML_FILENAME);

		//First we copy "identities.xml" to "identities.xml.bak"
		File identitiesFile = new File(XML_FILENAME);
		if (identitiesFile.exists()) {
			File bakFile = new File(BAK_FILENAME);
			bakFile.delete();
			try {
				FileAccess.copyFile(XML_FILENAME, BAK_FILENAME);
			} catch (IOException exception) {
				logger.log(Level.SEVERE, 
							"Error while copying " + XML_FILENAME + " to " + BAK_FILENAME, 
							exception);
			}
		}

		//We delete "identities.xml.tmp"
		File identitiesTmpFile = new File(TMP_FILENAME);
		if (identitiesTmpFile.exists()) {
			identitiesTmpFile.delete();
		}

		Document d = XMLTools.createDomDocument();
		Element rootElement = d.createElement("FrostIdentities");
		//first save myself
		rootElement.appendChild(identities.getMyId().getXMLElement(d));
        
        // myId is in friends list too, dont save it there
        identities.getFriends().remove(identities.getMyId().getUniqueName());
        
		//then friends
		Element friends = identities.getFriends().getXMLElement(d);
		friends.setAttribute("type", "friends");
		rootElement.appendChild(friends);
		//then enemies
		Element enemies = identities.getEnemies().getXMLElement(d);
		enemies.setAttribute("type", "enemies");
		rootElement.appendChild(enemies);
		//then everybody else
		Element neutral = identities.getNeutrals().getXMLElement(d);
		neutral.setAttribute("type", "neutral");
		rootElement.appendChild(neutral);
        
        // finally put myId back to friends
//        identities.getFriends().add(identities.getMyId());
        
		d.appendChild(rootElement);

		//We save identities to "identities.xml.tmp"
		if (XMLTools.writeXmlFile(d, TMP_FILENAME)) {
			//Success
			if (identitiesTmpFile.exists()) {
				//We replace "identities.xml" by "identities.xml.tmp"
				identitiesFile.delete();
				if (!identitiesTmpFile.renameTo(identitiesFile)) {
					//Replacement failed. We try to restore "identities.xml"
					// from "identities.xml.bak"
					try {
						FileAccess.copyFile(BAK_FILENAME, XML_FILENAME);
					} catch (IOException exception) {
						//Uh, oh, we are having a bad, bad day.
						throw new StorageException(
								"Error while restoring " + XML_FILENAME, exception);
					}
				}
			} else {
				//This shouldn't happen, but...
				throw new StorageException("Could not save " + XML_FILENAME);
			}
		} else {
			//Failure
			throw new StorageException("Could not save " + XML_FILENAME);
		}
	}
}
