/*
 ImportIdentities.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.identities.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.*;

public class ImportIdentities {
    
    private static final Logger logger = Logger.getLogger(ImportIdentities.class.getName());
    
    private BuddyList friends = new BuddyList();
    private BuddyList neutrals = new BuddyList();
    private BuddyList observed = new BuddyList();
    private BuddyList enemies = new BuddyList();

    public void importIdentities(File identitiesXmlFile) {
        FrostIdentities fi = new FrostIdentities();
        FrostIdentities.setDatabaseUpdatesAllowed(false);
        try {
            load(fi, identitiesXmlFile);
            convertOldListsToNew(fi);
        } catch (StorageException e) {
            logger.log(Level.SEVERE, "error importing old identities", e);
            return;
        }
        // save to database
        IdentitiesDatabaseTable dbt = AppLayerDatabase.getIdentitiesDatabaseTable();

        try {
            AppLayerDatabase.getInstance().setAutoCommitOff();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error set autocommit off", e);
        }

        List identities = fi.getIdentities();
        for(Iterator i=identities.iterator(); i.hasNext(); ) {
            Identity id = (Identity)i.next();
            try {
                dbt.insertIdentity(id);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error inserting identity", e);
            }
        }
        List localIdentities = fi.getLocalIdentities();
        for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
            LocalIdentity id = (LocalIdentity)i.next();
            try {
                dbt.insertLocalIdentity(id);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error inserting local identity", e);
            }
        }

        try {
            AppLayerDatabase.getInstance().setAutoCommitOn();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error set autocommit on", e);
        }

        FrostIdentities.setDatabaseUpdatesAllowed(true);
    }

    public static LocalIdentity importLocalIdentityFromIdentityXml(File identitiesXmlFile) {
        Document d = XMLTools.parseXmlFile(identitiesXmlFile, false);
        Element rootEl = d.getDocumentElement();

        Element myself = (Element) XMLTools.getChildElementsByTagName(rootEl, "MyIdentity").get(0);
        LocalIdentity myId = null;
        if( myself != null ) {
            myId = new LocalIdentity(myself);
        }
        return myId;
    }

    private void load(FrostIdentities identities, File xmlFile) throws StorageException {
        if (xmlFile.exists()) {
            try {
                loadNewFormat(identities, xmlFile);
            } catch (Exception e) {
                throw new StorageException("Exception while loading the new identities format.", e);
            }
        }
    }
    
    private void loadNewFormat(FrostIdentities identities, File xmlFile) throws SAXException, IllegalArgumentException {
        logger.info("Trying to create/load ids");
        Document d = XMLTools.parseXmlFile(xmlFile.getPath(), false);
        Element rootEl = d.getDocumentElement();

        //first myself
        Element myself = (Element) XMLTools.getChildElementsByTagName(rootEl, "MyIdentity").get(0);
        LocalIdentity myId = null;
        if( myself != null ) {
            myId = new LocalIdentity(myself);
            identities.addLocalIdentity(myId);
        }

        //then friends, enemies and neutrals
        List lists = XMLTools.getChildElementsByTagName(rootEl, "BuddyList");
        Iterator it = lists.iterator();

        while (it.hasNext()) {
            Element current = (Element) it.next();
            if (current.getAttribute("type").equals("friends")) {
                friends.loadXMLElement(current);
            } else if (current.getAttribute("type").equals("enemies")) {
                enemies.loadXMLElement(current);
            } else if (current.getAttribute("type").equals("neutral")) {
                neutrals.loadXMLElement(current);
            } else if (current.getAttribute("type").equals("observed")) {
                observed.loadXMLElement(current);
            }
        }
        logger.info("Loaded " + friends.size() + " friends and " + enemies.size() + " enemies, "
                + neutrals.size() + " neutrals and "+observed.size()+" observed.");
    }

    private void convertOldListsToNew(FrostIdentities identities) {

        for(Iterator i = getFriends().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getFriends().get(key);
            id.setGOOD();
            identities.addIdentity(id);
        }
        getFriends().clearAll();

        for(Iterator i = getObserved().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getObserved().get(key);
            id.setOBSERVE();
            identities.addIdentity(id);
        }
        getObserved().clearAll();

        for(Iterator i = getNeutrals().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getNeutrals().get(key);
            id.setCHECK();
            identities.addIdentity(id);
        }
        getNeutrals().clearAll();

        for(Iterator i = getEnemies().getAllKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Identity id = (Identity)getEnemies().get(key);
            id.setBAD();
            identities.addIdentity(id);
        }
        getEnemies().clearAll();
    }
    
    private BuddyList getEnemies() {
        return enemies;
    }
    private BuddyList getFriends() {
        return friends;
    }
    private BuddyList getObserved() {
        return observed;
    }
    private BuddyList getNeutrals() {
        return neutrals;
    }
    
    public class BuddyList implements XMLizable {

        private HashMap<String,Identity> hashMap = null;

        /**constructor*/
        public BuddyList() {
            hashMap = new HashMap<String,Identity>(100); //that sounds like a reasonable number
        }

        void clearAll() {
            hashMap.clear();
        }

        /**
         * adds a user to the list
         * returns false if the user exists
         */
        public synchronized boolean add(Identity user) {
            String str = Mixed.makeFilename(user.getUniqueName());
            if (containsKey(str)) {
                return false;
            }
            hashMap.put(str, user);

            return true;
        }

        /**
         * @param key
         * @return
         */
        public boolean containsKey(String key) {
            return hashMap.containsKey(Mixed.makeFilename((String) key));
        }

        /**
         * returns the user in the list, null if not in
         */
        public synchronized Identity get(String name) {
            String str = Mixed.makeFilename(name);
            if (containsKey(str)) {
                return (Identity) hashMap.get(str);
            } else {
                return null;
            }
        }

        // note - I decided to keep the same structure.  Its probably better from
        //XML point of view to have each identity's trust status marked as attribute,
        //but this way is easier..
        public synchronized Element getXMLElement(Document doc) {
            Element main = doc.createElement("BuddyList");
            Iterator it = hashMap.values().iterator(); //We iterate: therefore synchronized.
            while (it.hasNext()) {
                Identity id = (Identity) it.next();
                Element el = id.getXMLElement(doc);
                main.appendChild(el);
            }
            return main;
        }

        public void loadXMLElement(Element el) throws SAXException {
            if (el == null) {
                return;
            }
            List l = XMLTools.getChildElementsByTagName(el, "Identity");
            Iterator it = l.iterator();
            while (it.hasNext()) {
                add(new Identity((Element) it.next()));
            }
        }

        /**
         * @param key
         * @return
         */
        protected Object remove(String key) {
            return hashMap.remove(Mixed.makeFilename(key));
        }

        /**
         * @return
         */
        public int size() {
            return hashMap.size();
        }

        public Set getAllKeys() {
            return hashMap.keySet();
        }
    }
}
