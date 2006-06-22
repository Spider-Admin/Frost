/*
  FrostIdentities.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * A class that maintains identity stuff.
 *
 * NOTE: the old BuddyLists are not longer used. But because we need to load the
 * lists to repair corrupted lists, the load/save is already done using the BuddyLists!
 * After load and before save we convert the lists to 1 list which is used from within Frost.
 *
 * TODO: This must be removed in a later version, then we can implement load/save directly
 * in FrostIdentities.
 *
 * TODO: neutral list can become very large, find a way to put them to another datastore.
 */
public class FrostIdentities implements Savable {

    private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());

    public static final int FRIEND  = 1;
    public static final int NEUTRAL = 2;
    public static final int OBSERVE = 3;
    public static final int ENEMY   = 4;

    // OLD buddy lists, currently used to load/save the list and to repair it
    private BuddyList friends = new BuddyList();
    private BuddyList neutrals = new BuddyList();
    private BuddyList observed = new BuddyList();
    private BuddyList enemies = new BuddyList();

    private Hashtable identities = new Hashtable();

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
            settings.setValue("userName", mySelf.getUniqueName());
        } else {
            //Storage exists. Load from it.
            identitiesDAO.load(this);
            logger.info("ME = '" + mySelf.getUniqueName() + "'");
            // we loaded all 4 BuddyLists, only needed to repair them one time
            if( settings.getBoolValue("oneTimeUpdate.repairIdentities.didRun") == false ) {
                repairIdentities();
                settings.setValue("oneTimeUpdate.repairIdentities.didRun", true);
            }
            // finally get all identites from all lists into the one and only list
            convertOldListsToNew();
        }
    }

    // ************************************************
    // FOLLOWING METHODS ARE USED NOW:

    public Identity getIdentity(String uniqueName) {
        if( uniqueName == null ) {
            return null;
        }
        Identity identity = null;
        if( uniqueName == null ) {
            if( isMySelf(uniqueName)) {
                identity = getMyId();
            } else {
                identity = (Identity)identities.get(uniqueName);
            }
        }
        return identity;
    }

    public boolean addIdentity(Identity id) {
        String key = Mixed.makeFilename(id.getUniqueName());
        if (identities.containsKey(key)) {
            return false;
        }
        identities.put(key, id);
        return true;
    }

    public List getAllIdentitiesWithState(int state) {
        ArrayList list = new ArrayList();
        for( Iterator i = identities.values().iterator(); i.hasNext(); ) {
            Identity id = (Identity)i.next();
            if( id.getState() == state ) {
                list.add(id);
            }
        }
        return list;
    }

    // ************************************************
    // ************************************************

    private void convertOldListsToNew() {

        for(Iterator i = getFriends().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getFriends().get(key);
            id.setState(FRIEND);
            identities.put(key, id);
        }
        getFriends().clearAll();

        for(Iterator i = getObserved().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getObserved().get(key);
            id.setState(OBSERVE);
            identities.put(key, id);
        }
        getObserved().clearAll();

        for(Iterator i = getNeutrals().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getNeutrals().get(key);
            id.setState(NEUTRAL);
            identities.put(key, id);
        }
        getNeutrals().clearAll();

        for(Iterator i = getEnemies().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getEnemies().get(key);
            id.setState(ENEMY);
            identities.put(key, id);
        }
        getEnemies().clearAll();
    }

    private void convertNewListToOld() {
        getFriends().clearAll();
        getObserved().clearAll();
        getNeutrals().clearAll();
        getEnemies().clearAll();

        for(Iterator i = identities.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)identities.get(key);
            if( id.getState() == FRIEND ) {
                getFriends().add(id);
            }
            if( id.getState() == OBSERVE ) {
                getObserved().add(id);
            }
            if( id.getState() == NEUTRAL ) {
                getNeutrals().add(id);
            }
            if( id.getState() == ENEMY ) {
                getEnemies().add(id);
            }
        }
    }

    /* (non-Javadoc)
     * @see frost.storage.Savable#save()
     */
    public void save() throws StorageException {

        convertNewListToOld();

        IdentitiesDAO identitiesDAO = DAOFactory.getFactory(DAOFactory.XML).getIdentitiesDAO();
        identitiesDAO.save(this);
    }

    public void changeTrust(String from, int newState) {

        from = Mixed.makeFilename(from);

        Identity newIdentity;

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
            logger.info("Ignored request to change my own ID to state '" + newStateStr + "'.");
            return;
        }

        newIdentity = getIdentity(from);

        if( newIdentity == null ) {
            logger.log(Level.SEVERE, "FROM not found in identities list: "+from);
            return;
        }

        newIdentity.setState( newState );

        // walk through shown messages and remove unneeded (e.g. if hideBad)
        // remember selected msg and select next
        Board board = MainFrame.getInstance().getTofTreeModel().getSelectedNode();
        if( board != null || !board.isFolder() ) {
            // a board is selected and shown
            MessageTableModel msgTableModel = MainFrame.getInstance().getMessageTableModel();
            for(int x=msgTableModel.getRowCount() - 1; x >= 0; x--) {
                FrostMessageObject message = (FrostMessageObject)msgTableModel.getRow(x);

                if( TOF.getInstance().blocked(message,board) ) {
                    msgTableModel.deleteRow(message);
                    if( message.isNew() ) {
                        board.decNewMessageCount();
                    }
                } else {
                    msgTableModel.updateRow(message);
                }
            }
            MainFrame.getInstance().updateMessageCountLabels(board);
        }

        // finally step through all board files, count new messages and show only wanted messages
        // starts a separate thread
        TOF.getInstance().initialSearchNewMessages();
    }

    public boolean isMySelf(String uniqueName) {
        return getMyId().getUniqueName().equals(uniqueName);
    }


    public LocalIdentity getMyId() {
        return mySelf;
    }

    void setMyId(LocalIdentity myId) {
        mySelf = myId;
    }

    /************************************************************
     * OLD AND REPAIR STUFF
     ************************************************************/
    // run repair one time!
    private void repairIdentities() {
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

    /**
     * OLD_OLD_OLD
     * Returns the Identity for uniqueName if found on any list.
     */
    private Identity getIdentityFromAnyList(String uniqueName) {
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

    /**
     * OLD_OLD_OLD
     */
    private void removeIdentityFromAnyList(String ident) {
        getFriends().remove(ident);
        getEnemies().remove(ident);
        getNeutrals().remove(ident);
        getObserved().remove(ident);
    }

    /**
     * OLD_OLD_OLD
     */
    BuddyList getEnemies() {
        return enemies;
    }

    /**
     * OLD_OLD_OLD
     */
    BuddyList getFriends() {
        return friends;
    }

    /**
     * OLD_OLD_OLD
     */
    BuddyList getObserved() {
        return observed;
    }

    /**
     * OLD_OLD_OLD
     */
    BuddyList getNeutrals() {
        return neutrals;
    }
}
