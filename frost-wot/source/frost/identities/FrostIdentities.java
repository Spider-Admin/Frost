package frost.identities;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import swingwtx.swing.JOptionPane;

import org.w3c.dom.*;

import frost.*;
import frost.fcp.FecTools;
import frost.threads.maintenance.Savable;
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
	
	public void load(boolean freenetIsOnline) {
		File identities = new File("identities");
		File identitiesxml = new File("identities.xml");
		try {

			if (identities.length() == 0)
				identities.delete();
			if (identitiesxml.length() == 0)
				identitiesxml.delete();
			if (identities.createNewFile() && identitiesxml.createNewFile()) {
				if (freenetIsOnline == false) {
					JOptionPane.showMessageDialog(
						MainFrame.getInstance(),
						languageResource.getString(
							"Core.loadIdentities.ConnectionNotEstablishedBody"),
						languageResource.getString(
							"Core.loadIdentities.ConnectionNotEstablishedTitle"),
						JOptionPane.ERROR_MESSAGE);
					System.exit(2);
				}
				//create new identities
				try {
					String nick = null;
					do {
						nick =
							JOptionPane.showInputDialog(null,null,
								languageResource.getString("Core.loadIdentities.ChooseName"),0);
						if (!(nick == null || nick.length() == 0)) {
							// check for a '@' in nick, this is strongly forbidden
							if (nick.indexOf("@") > -1) {
								JOptionPane.showMessageDialog(
									MainFrame.getInstance(),
									languageResource.getString(
										"Core.loadIdentities.InvalidNameBody"),
									languageResource.getString(
										"Core.loadIdentities.InvalidNameTitle"),
									JOptionPane.ERROR_MESSAGE);
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

					//JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
				} catch (Exception e) {
					logger.severe("couldn't create new identitiy" + e.toString());
				}
				//friends = new BuddyList();

				if (friends.add(mySelf)) {
					logger.info("added myself to list");
				}
				//enemies = new BuddyList();
			} else
				//first try with the new format
				if (identitiesxml.exists()) {
					//friends = new BuddyList();
					//enemies = new BuddyList();
					try {
						logger.info("trying to create/load ids");
						Document d = XMLTools.parseXmlFile("identities.xml", false);
						Element rootEl = d.getDocumentElement();
						//first myself
						Element myself =
							(Element) XMLTools.getChildElementsByTagName(rootEl, "MyIdentity").get(
								0);
						mySelf = new LocalIdentity(myself);

						//then friends
						List lists = XMLTools.getChildElementsByTagName(rootEl, "BuddyList");
						Iterator it = lists.iterator();
						while (it.hasNext()) {
							Element current = (Element) it.next();
							if (current.getAttribute("type").equals("friends"))
								friends.loadXMLElement(current);
							else if (current.getAttribute("type").equals("enemies"))
								enemies.loadXMLElement(current);
							else
								neutrals.loadXMLElement(current);
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Exception thrown in loadIdentities()", e);
					}
					logger.info(
						"loaded "
							+ friends.size()
							+ " friends and "
							+ enemies.size()
							+ " enemies and "
							+ neutrals.size()
							+ " neutrals.");
					if (friends.add(mySelf))
						logger.info("added myself to list");

				} else {
					try {

						BufferedReader fin = new BufferedReader(new FileReader(identities));
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
						mySelf = new LocalIdentity(name, keys);
						logger.info("loaded myself with name " + mySelf.getName());
						//out.println("and public key" + mySelf.getKey());

						//take out the ****
						fin.readLine();

						//process the friends
						logger.info("loading friends");
						friends = new BuddyList();
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
						logger.info("loaded " + friends.size() + " friends");

						//just the good ids
						while (!stop) {
							String id = fin.readLine();
							if (id == null || id.startsWith("***"))
								break;
							goodIds.put(id, id);
						}
						logger.info("loaded " + goodIds.size() + " good ids");

						//and the enemies
						enemies = new BuddyList();
						logger.info("loading enemies");
						while (!stop) {
							name = fin.readLine();
							if (name == null || name.startsWith("***"))
								break;
							address = fin.readLine();
							key = fin.readLine();
							enemies.add(new Identity(name, key));
						}
						logger.info("loaded " + enemies.size() + " enemies");

						//and the bad ids
						while (!stop) {
							String id = fin.readLine();
							if (id == null || id.startsWith("***"))
								break;
							badIds.put(id, id);
						}
						logger.info("loaded " + badIds.size() + " bad ids");

					} catch (IOException e) {
						logger.severe("IOException :" + e.toString());
						friends = new BuddyList();
						enemies = new BuddyList();
						friends.add(mySelf);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Exception thrown in loadIdentities()", e);
					}
				}

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception thrown in loadIdentities()", e);
		}
		logger.info("ME = '" + mySelf.getUniqueName() + "'");
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
						return true;
					} catch (IOException exception) {
						//Uh, oh, we are having a bad, bad day.
						logger.log(Level.SEVERE, "Error while restoring identities.xml", exception);
					}
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
	public BuddyList getNeutrals() {
		return neutrals;
	}

}
