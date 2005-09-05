/*
  FrostIdentities.java / Frost
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

import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * A class that maintains identity stuff.
 */
public class FrostIdentities implements Savable {
	
	private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());
	
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

    /**
     * Returns the Identity for uniqueName if found on any list.
     */
    public Identity getIdentityFromAnyList(String uniqueName) {
        Identity found = null;
        if( (found = getNeutrals().get(uniqueName)) != null ) {
            return found;
        }
        if( (found = getFriends().get(uniqueName)) != null ) {
            return found;
        }
        if( (found = getEnemies().get(uniqueName)) != null ) {
            return found;
        }
        return null;
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
