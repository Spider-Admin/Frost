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

import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.boards.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * A class that maintains identity stuff.
 */
public class FrostIdentities implements Savable {
	
	private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());
    
    public static final int FRIEND  = 1; 
    public static final int NEUTRAL = 2; 
    public static final int OBSERVE = 3; 
    public static final int ENEMY   = 4; 
	
	private BuddyList friends = new BuddyList();	
	private BuddyList neutrals = new BuddyList();
    private BuddyList observed = new BuddyList();
    private BuddyList enemies = new BuddyList();
	
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
			//create new identitiy
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
            // TODO: remove
//			if (friends.add(mySelf)) {
//				logger.info("added myself to list");
//			}
			settings.setValue("userName", mySelf.getUniqueName());
		} else {
			//Storage exists. Load from it.
			identitiesDAO.load(this);
			logger.info("ME = '" + mySelf.getUniqueName() + "'");
            // if not already generated...
            if( mySelf.getBoard() == null && freenetIsOnline ) {
                mySelf.generateOwnBoard();
            }
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
        if( (found = getObserved().get(uniqueName)) != null ) {
            return found;
        }
        if( (found = getEnemies().get(uniqueName)) != null ) {
            return found;
        }
        return null;
    }

    public void removeIdentityFromAnyList(String ident) {
        getFriends().remove(ident);
        getEnemies().remove(ident);
        getNeutrals().remove(ident);
        getObserved().remove(ident);
    }

    // run repair one time!
    public void repairIdentities() {
        // what is repaired? Due to a problem in BuddyList.remove the identities
        // are not completely correct.
        // test and fix: 
        //  - if someone is ( (GOOD or BAD) AND NEUTRAL) set it to GOOD or BAD
        //  - if someone is (GOOD AND BAD AND NEUTRAL) put it to GOOD
        HashSet allFroms = new HashSet();
        Set s = getFriends().getAllKeys();
        allFroms.addAll(s);
        s = getNeutrals().getAllKeys();
        allFroms.addAll(s);
        s = getEnemies().getAllKeys();
        allFroms.addAll(s);
        Iterator i = allFroms.iterator();
        while(i.hasNext()) {
            String from = (String)i.next();
            Identity ident = getIdentityFromAnyList(from);

            boolean friend  = getFriends().containsKey(from);
            boolean neutral = getNeutrals().containsKey(from);
            boolean enemy   = getEnemies().containsKey(from);
            
            if( neutral ) {
                if( enemy && friend ) {
                    removeIdentityFromAnyList(from);
                    getFriends().add(ident);
                } else if(enemy) {
                    removeIdentityFromAnyList(from);
                    getEnemies().add(ident);
                } else if(friend) {
                    removeIdentityFromAnyList(from);
                    getFriends().add(ident);
                }
            } else if( enemy && friend ) {
                removeIdentityFromAnyList(from);
                getFriends().add(ident);
            }
        }
    }
    
    public void changeTrust(String from, int newState) {
        
        from = Mixed.makeFilename(from);
        
        Identity newIdentity;
        // TODO: dont reload table after change!
        // problem: if we changed someone to bad we should reload all new messages for all folders!
        String newStateStr;
        if( newState == FrostIdentities.FRIEND ) {
            newStateStr = "GOOD";
        } else if( newState == FrostIdentities.NEUTRAL ) {
            newStateStr = "CHECK";
        } else if( newState == FrostIdentities.OBSERVE ) {
            newStateStr = "OBSERVE";
        } else if( newState == FrostIdentities.ENEMY ) {
            newStateStr = "BAD";
        } else {
            logger.log(Level.SEVERE, "Invalid new state: "+newState);
            return;
        }
        logger.info("Setting '" + from + "' to state '" + newStateStr + "'.");

        // don't change GOOD state for mySelf!
        if( isMySelf(from) && newState != FrostIdentities.FRIEND ) {
            logger.info("Ignored call to change my own ID to state '" + newStateStr + "'.");
            return;
        }

        newIdentity = getIdentityFromAnyList(from);

        if( newIdentity == null ) {
            logger.log(Level.SEVERE, "FROM not found in any list: "+from);
            return;
        }
        
        removeIdentityFromAnyList(from);
        
        if( newState == FrostIdentities.FRIEND ) { 
            // set to good
            getFriends().add( newIdentity );
        } else if( newState == FrostIdentities.ENEMY ) {
            // set to bad
            getEnemies().add( newIdentity );
        } else if( newState == FrostIdentities.NEUTRAL ) {
            // set to good
            getNeutrals().add( newIdentity );
        } else if( newState == FrostIdentities.OBSERVE ) {
            // set to observed
            getObserved().add( newIdentity );
        }

        // finally step through all board files, count new messages and show only wanted messages
        TOF.getInstance().initialSearchNewMessages(); // starts a separate thread

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame.getInstance().tofTree_actionPerformed(null);
            } });
    }
    
    public boolean isMySelf(String uniqueName) {
        return getMyId().getUniqueName().equals(uniqueName);
    }

	public BuddyList getEnemies() {
		return enemies;
	}

	public BuddyList getFriends() {
		return friends;
	}

    public BuddyList getObserved() {
        return observed;
    }

    public BuddyList getNeutrals() {
        return neutrals;
    }

    public LocalIdentity getMyId() {
		return mySelf;
	}
	
	void setMyId(LocalIdentity myId) {
		mySelf = myId;
	}
}
