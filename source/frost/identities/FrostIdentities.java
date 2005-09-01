package frost.identities;

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import frost.SettingsClass;
import frost.storage.*;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.Language;

/**
 * A class that maintains identity stuff.
 * @author $Author$
 * @version $Revision$
 */
public class FrostIdentities implements Savable {
	
	private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());
	
//	private Hashtable badIds = new Hashtable();
//	private Hashtable goodIds = new Hashtable();
	private BuddyList enemies = new BuddyList();
	private BuddyList friends = new BuddyList();	
	private BuddyList neutrals = new BuddyList();
	
	private LocalIdentity mySelf = null;
	
	private SettingsClass settings;

	/**
	 * @param settings
	 */
	public FrostIdentities(SettingsClass settings) {
		super();
		this.settings = settings;
	}
	
	/**
	 * @param freenetIsOnline
	 */
	public void initialize(boolean freenetIsOnline) throws StorageException {
		IdentitiesDAO identitiesDAO = DAOFactory.getFactory(DAOFactory.XML).getIdentitiesDAO();
		Language language = Language.getInstance();
		if (!identitiesDAO.exists()) {
			//The storage doesn't exist yet. We create it.
			identitiesDAO.create();
			if (freenetIsOnline == false) {
				MiscToolkit.getInstance().showMessage(
						language.getString("Core.loadIdentities.ConnectionNotEstablishedBody"),
						JOptionPane.ERROR_MESSAGE,
						language.getString("Core.loadIdentities.ConnectionNotEstablishedTitle"));
				System.exit(2);
			}
			//create new identities
			try {
				String nick = null;
				do {
					nick = MiscToolkit.getInstance().showInputDialog(
							language.getString("Core.loadIdentities.ChooseName"));
					if (!(nick == null || nick.length() == 0)) {
						// check for a '@' in nick, this is strongly forbidden
						if (nick.indexOf("@") > -1) {
							MiscToolkit.getInstance().showMessage(
									language.getString("Core.loadIdentities.InvalidNameBody"),
									JOptionPane.ERROR_MESSAGE,
									language.getString("Core.loadIdentities.InvalidNameTitle"));
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
			settings.setValue("userName", mySelf.getUniqueName());
		} else {
			//Storage exists. Load from it.
			identitiesDAO.load(this);
			logger.info("ME = '" + mySelf.getUniqueName() + "'");
		}
	}
	
	/* (non-Javadoc)
	 * @see frost.storage.Savable#save()
	 */
	public void save() throws StorageException {
		IdentitiesDAO identitiesDAO = DAOFactory.getFactory(DAOFactory.XML).getIdentitiesDAO();
		identitiesDAO.save(this);
	}

//	/**
//	 * @return
//	 */
//	public Hashtable getBadIds() {
//        System.out.println("GET_BAD_IDS:"+badIds.size());
//		return badIds;
//	}

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

//	/**
//	 * @return
//	 */
//	public Hashtable getGoodIds() {
//        System.out.println("GET_GOOD_IDS:"+goodIds.size());
//		return goodIds;
//	}

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
