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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * A class that maintains identity stuff.
 */
public class FrostIdentities implements Savable {

    // FIXME: use separate signatures for each identity
    
    private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());

    private Hashtable identities = new Hashtable();
    private Hashtable localIdentities = new Hashtable();
    
    Language language = Language.getInstance();
    
    private static boolean databaseUpdatesAllowed = true; // forbidden during first import

    /**
     * @param freenetIsOnline
     */
    public void initialize(boolean freenetIsOnline) throws StorageException {

        try {
            List localIdentitiesList = AppLayerDatabase.getIdentitiesDatabaseTable().getLocalIdentities();
            
            // check if there is at least one identity in database, otherwise create one
            if ( localIdentitiesList.size() == 0 ) {
                // first startup, no identity available
                if (freenetIsOnline == false) {
                    MiscToolkit.getInstance().showMessage(
                            language.getString("Core.loadIdentities.ConnectionNotEstablishedBody"),
                            JOptionPane.ERROR_MESSAGE,
                            language.getString("Core.loadIdentities.ConnectionNotEstablishedTitle"));
                    System.exit(2);
                }
                
                LocalIdentity mySelf = createIdentity(true);
                if(mySelf == null) {
                    logger.severe("Frost can't run without an identity.");
                    System.exit(1);
                }
                addLocalIdentity(mySelf); // add and save
            } else {
                for(Iterator i=localIdentitiesList.iterator(); i.hasNext(); ) {
                    LocalIdentity li = (LocalIdentity)i.next();
                    localIdentities.put(li.getUniqueName(), li);
                }
            }
            localIdentitiesList = null;
            
            // all identities
            List identitiesList = AppLayerDatabase.getIdentitiesDatabaseTable().getIdentities();
            for(Iterator i=identitiesList.iterator(); i.hasNext(); ) {
                Identity li = (Identity)i.next();
                identities.put(li.getUniqueName(), li);
            }
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "error loading from database", ex);
            throw new StorageException("error loading from database");
        }
    }
    
    /**
     * Creates new local identity, and adds it to database.
     */
    public LocalIdentity createIdentity() {
        LocalIdentity li = createIdentity(false);
        if( li != null ) {
            if( addLocalIdentity(li) == false ) {
                // double
                return null;
            }
        }
        return li;
    }
    
    /**
     * Creates new local identity, and adds it to database.
     */
    private LocalIdentity createIdentity(boolean firstIdentity) {

        LocalIdentity newIdentity = null;
        
        // create new identitiy
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
                return null;
            }
            do { //make sure there's no // in the name.
                newIdentity = new LocalIdentity(nick);
            } while (newIdentity.getUniqueName().indexOf("//") != -1);

        } catch (Exception e) {
            logger.severe("couldn't create new identitiy" + e.toString());
        }
        if( newIdentity != null && firstIdentity ) {
            Core.frostSettings.setValue("userName", newIdentity.getUniqueName());
        }
        
        return newIdentity;
    }

    public Identity getIdentity(String uniqueName) {
        if( uniqueName == null ) {
            return null;
        }
        Identity identity = null;
        identity = getLocalIdentity(uniqueName);
        if( identity == null ) {
            identity = (Identity)identities.get(uniqueName);
        }
        return identity;
    }

    public boolean addIdentity(Identity id) {
        String key = id.getUniqueName();
        if (identities.containsKey(key)) {
            return false;
        }
        if( isDatabaseUpdatesAllowed() ) {
            try {
                AppLayerDatabase.getIdentitiesDatabaseTable().insertIdentity(id);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error inserting new identity", e);
                return false;
            }
        }
        identities.put(key, id);
        return true;
    }
    
    public boolean addLocalIdentity(LocalIdentity li) {
        if (localIdentities.containsKey(li.getUniqueName())) {
            return false;
        }
        if( isDatabaseUpdatesAllowed() ) {
            try {
                AppLayerDatabase.getIdentitiesDatabaseTable().insertLocalIdentity(li);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error inserting new local identity", e);
                return false;
            }
        }
        localIdentities.put(li.getUniqueName(), li);
        return true;
    }
    
    public boolean deleteLocalIdentity(LocalIdentity li) {
        if (!localIdentities.containsKey(li.getUniqueName())) {
            return false;
        }
        
        localIdentities.remove(li.getUniqueName());
        
        try {
            return AppLayerDatabase.getIdentitiesDatabaseTable().removeLocalIdentity(li);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error removing local identity", e);
            return false;
        }
    }

    public boolean deleteIdentity(Identity li) {
        if (!identities.containsKey(li.getUniqueName())) {
            return false;
        }
        
        identities.remove(li.getUniqueName());
        
        try {
            return AppLayerDatabase.getIdentitiesDatabaseTable().removeIdentity(li);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error removing identity", e);
            return false;
        }
    }

    public boolean isMySelf(String uniqueName) {
        if( getLocalIdentity(uniqueName) != null ) {
            return true;
        }
        return false;
    }
    
    public LocalIdentity getLocalIdentity(String uniqueName) {
        LocalIdentity li = null;
        li = (LocalIdentity) localIdentities.get(uniqueName);
//        if( li == null ) {
//            li = (LocalIdentity) localIdentities.get(Mixed.makeFilename(uniqueName));
//        }
        return li;
    }

    public List getAllGOODIdentities() {
        LinkedList list = new LinkedList();
        for( Iterator i = identities.values().iterator(); i.hasNext(); ) {
            Identity id = (Identity)i.next();
            if( id.isGOOD() ) {
                list.add(id);
            }
        }
        return list;
    }

    public List getLocalIdentities() {
        return new ArrayList(localIdentities.values());
    }

    public List getIdentities() {
        return new ArrayList(identities.values());
    }

    public static boolean isDatabaseUpdatesAllowed() {
        return databaseUpdatesAllowed;
    }

    public static void setDatabaseUpdatesAllowed(boolean dbUpdatesAllowed) {
        databaseUpdatesAllowed = dbUpdatesAllowed;
    }
    
    /**
     * Identities are saved on demand, but the information about the 
     * last files shared per board must be saved during exit.
     */
    public void save() throws StorageException {
        try {
            // TODO: don't save infos for deleted boards
            AppLayerDatabase.getIdentitiesDatabaseTable().saveAllLastFilesSharedPerBoard(getLocalIdentities());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error saving last files shared information", e);
        }
    }
}
