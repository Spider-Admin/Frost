package frost.identities;

import java.io.*;
import java.util.Hashtable;
import java.util.logging.*;

import javax.swing.JOptionPane;

import org.w3c.dom.*;

import frost.*;
import frost.storage.*;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * A class that maintains identity stuff.
 */
public class FrostIdentities implements Savable {
	
	private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());
	
	private Hashtable badIds = new Hashtable();
	private Hashtable goodIds = new Hashtable();
	private BuddyList enemies = new BuddyList();
	private BuddyList friends = new BuddyList();	
	private BuddyList neutrals = new BuddyList();
	
	private LocalIdentity mySelf = null;
	
	private UpdatingLanguageResource languageResource = null;

	public FrostIdentities(UpdatingLanguageResource newLanguageResource) {
		super();
		languageResource = newLanguageResource;
	}
	
	/**
	 * @param freenetIsOnline
	 */
	public void initialize(boolean freenetIsOnline) throws StorageException {
		IdentitiesDAO identitiesDAO = DAOFactory.getFactory(DAOFactory.XML).getIdentitiesDAO();
		if (!identitiesDAO.exists()) {
			//The storage doesn't exist yet. We create it.
			identitiesDAO.create();
			if (freenetIsOnline == false) {
				MiscToolkit.getInstance().showMessage(
						languageResource.getString("Core.loadIdentities.ConnectionNotEstablishedBody"),
						JOptionPane.ERROR_MESSAGE,
						languageResource.getString("Core.loadIdentities.ConnectionNotEstablishedTitle"));
				System.exit(2);
			}
			//create new identities
			try {
				String nick = null;
				do {
					nick = MiscToolkit.getInstance().showInputDialog(
							languageResource.getString("Core.loadIdentities.ChooseName"));
					if (!(nick == null || nick.length() == 0)) {
						// check for a '@' in nick, this is strongly forbidden
						if (nick.indexOf("@") > -1) {
							MiscToolkit.getInstance().showMessage(
									languageResource.getString("Core.loadIdentities.InvalidNameBody"),
									JOptionPane.ERROR_MESSAGE,
									languageResource.getString("Core.loadIdentities.InvalidNameTitle"));
							nick = "";
						}
					}
				} while (nick != null && nick.length() == 0);
				if (nick == null) {
					logger.severe("Frost can't run without an identity.");
					System.exit(1);
				}
				do { //make sure there's no // in the name.
					mySelf = new LocalIdentity(nick);
				} while (mySelf.getUniqueName().indexOf("//") != -1);
				
			} catch (Exception e) {
				logger.severe("couldn't create new identitiy" + e.toString());
			}
			if (friends.add(mySelf)) {
				logger.info("added myself to list");
			}
		} else {
			//Storage exists. Load from it.
			identitiesDAO.load(this);
			logger.info("ME = '" + mySelf.getUniqueName() + "'");
		}
	}
	
	public boolean save() {
		logger.info("saving identities.xml");

		String identitiesName = "identities.xml";
		String identitiesTmpName = "identities.xml.tmp";
		String identitiesBakName = "identities.xml.bak";

		//First we copy "identities.xml" to "identities.xml.bak"
		File identitiesFile = new File(identitiesName);
		if (identitiesFile.exists()) {
			File bakFile = new File(identitiesBakName);
			bakFile.delete();
			try {
				FileAccess.copyFile(identitiesName, identitiesBakName);
			} catch (IOException exception) {
				logger.log(Level.SEVERE, "Error while saving identities.xml", exception);
			}
		}

		//We delete "identities.xml.tmp"
		File identitiesTmpFile = new File(identitiesTmpName);
		if (identitiesTmpFile.exists()) {
			identitiesTmpFile.delete();
		}

		Document d = XMLTools.createDomDocument();
		Element rootElement = d.createElement("FrostIdentities");
		//first save myself
		rootElement.appendChild(mySelf.getXMLElement(d));
		//then friends
		Element friends = getFriends().getXMLElement(d);
		friends.setAttribute("type", "friends");
		rootElement.appendChild(friends);
		//then enemies 
		Element enemies = getEnemies().getXMLElement(d);
		enemies.setAttribute("type", "enemies");
		rootElement.appendChild(enemies);
		//then everybody else
		Element neutral = getNeutrals().getXMLElement(d);
		neutral.setAttribute("type", "neutral");
		rootElement.appendChild(neutral);
		d.appendChild(rootElement);

		//We save identities to "identities.xml.tmp"
		if (XMLTools.writeXmlFile(d, identitiesTmpName)) {
			//Success	
			if (identitiesTmpFile.exists()) {
				//We replace "identities.xml" by "identities.xml.tmp"
				identitiesFile.delete();
				if (!identitiesTmpFile.renameTo(identitiesFile)) {
					//Replacement failed. We try to restore "identities.xml" from "identities.xml.bak"
					try {
						FileAccess.copyFile(identitiesBakName, identitiesName);
					} catch (IOException exception) {
						//Uh, oh, we are having a bad, bad day.
						logger.log(Level.SEVERE, "Error while restoring identities.xml", exception);
					}
				} else {
					return true;
				}
			} else {
				//This shouldn't happen, but...
				logger.severe("Could not save identities.xml");
			}
		} else {
			//Failure
			logger.severe("Could not save identities.xml");
		}
		return false;
	}

	/**
	 * @return
	 */
	public Hashtable getBadIds() {
		return badIds;
	}

	/**
	 * @return
	 */
	public BuddyList getEnemies() {
		return enemies;
	}

	/**
	 * @return
	 */
	public BuddyList getFriends() {
		return friends;
	}

	/**
	 * @return
	 */
	public Hashtable getGoodIds() {
		return goodIds;
	}

	/**
	 * @return
	 */
	public LocalIdentity getMyId() {
		return mySelf;
	}
	
	/**
	 * @return
	 */
	void setMyId(LocalIdentity myId) {
		mySelf = myId;
	}

	/**
	 * @return
	 */
	public BuddyList getNeutrals() {
		return neutrals;
	}

}
