/*
 * Created on Nov 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.identities;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.XMLTools;
import frost.fcp.FecTools;
import frost.storage.StorageException;

/**
 * @author $author$
 * @version $revision$
 */
public class IdentitiesXmlDAO implements IdentitiesDAO {
	
	private static Logger logger = Logger.getLogger(IdentitiesXmlDAO.class.getName());
	
	private static final String OLD_FILENAME = "identities";
	private static final String XML_FILENAME = "identities.xml";
	
	/* (non-Javadoc)
	 * @see frost.identities.IdentitiesDAO#exists()
	 */
	public boolean exists() {
		File oldFile = new File(OLD_FILENAME);
		File xmlFile = new File(XML_FILENAME);
		if (oldFile.length() == 0) {
			oldFile.delete();
		}
		if (xmlFile.length() == 0) {
			xmlFile.delete();
		}
		if (oldFile.exists() || xmlFile.exists()) {
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
		LocalIdentity myId = new LocalIdentity(myself);
		identities.setMyId(myId);

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
			} else {
				neutrals.loadXMLElement(current);
			}
		}
		logger.info("Loaded " + friends.size() + " friends and " + enemies.size() + " enemies and "
				+ neutrals.size() + " neutrals.");
		if (friends.add(myId)) {
			logger.info("Added myself to the friends list");
		}
	}
	
	/**
	 * @param identities
	 */
	private void loadOldFormat(FrostIdentities identities, File oldFile) throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(oldFile));
		String name = fin.readLine();
		String address = fin.readLine();
		String keys[] = new String[2];
		keys[1] = fin.readLine();
		keys[0] = fin.readLine();
		if (address.startsWith("CHK@") == false) {
			// pubkey chk was not successfully computed
			byte[] pubkeydata;
			try {
				pubkeydata = keys[1].getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				pubkeydata = keys[1].getBytes();
			}

			String tmp = FecTools.generateCHK(pubkeydata);
			address = tmp.substring(tmp.indexOf("CHK@"), tmp.indexOf("CHK@") + 58);
			logger.info("Re-calculated my public key CHK: " + address + "\n");

		}
		LocalIdentity myId = new LocalIdentity(name, keys);
		identities.setMyId(myId);
		logger.info("Loaded myself with name " + myId.getName());

		//take out the ****
		fin.readLine();
		
		//process the friends
		BuddyList friends = identities.getFriends();
		boolean stop = false;
		String key;
		while (!stop) {
			name = fin.readLine();
			if (name == null || name.startsWith("***"))
				break;
			address = fin.readLine();
			key = fin.readLine();
			friends.add(new Identity(name, key));
		}
		logger.info("Loaded " + friends.size() + " friends");

		//just the good ids
		Hashtable goodIds = identities.getGoodIds();
		while (!stop) {
			String id = fin.readLine();
			if (id == null || id.startsWith("***"))
				break;
			goodIds.put(id, id);
		}
		logger.info("Loaded " + goodIds.size() + " good ids");

		//and the enemies
		BuddyList enemies = identities.getEnemies();
		while (!stop) {
			name = fin.readLine();
			if (name == null || name.startsWith("***"))
				break;
			address = fin.readLine();
			key = fin.readLine();
			enemies.add(new Identity(name, key));
		}
		logger.info("Loaded " + enemies.size() + " enemies");

		//and the bad ids
		Hashtable badIds = identities.getBadIds();
		while (!stop) {
			String id = fin.readLine();
			if (id == null || id.startsWith("***"))
				break;
			badIds.put(id, id);
		}
		logger.info("Loaded " + badIds.size() + " bad ids");
		if (friends.add(myId)) {
			logger.info("Added myself to the friends list");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see frost.identities.IdentitiesDAO#load(frost.identities.FrostIdentities)
	 */
	public void load(FrostIdentities identities) throws StorageException {
		File oldFile = new File(OLD_FILENAME);
		File xmlFile = new File(XML_FILENAME);
		
		if (xmlFile.exists()) {
			try {
				loadNewFormat(identities);
			} catch (Exception e) {
				throw new StorageException("Exception while loading the new identities format.", e);
			}
		} else {
			try {
				loadOldFormat(identities, oldFile);
			} catch (Exception ioe) {
				throw new StorageException("Exception while loading the old identities format.", ioe);
			}
		}
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
}
