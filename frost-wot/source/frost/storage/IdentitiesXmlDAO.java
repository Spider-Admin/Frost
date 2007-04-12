/*
  IdentitiesXmlDAO.java / Frost
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
package frost.storage;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.identities.*;
import frost.util.*;

public class IdentitiesXmlDAO {

    private static final Logger logger = Logger.getLogger(IdentitiesXmlDAO.class.getName());

    /**
     * Loads good, observe and bad identities from xml file.
     */
    public static List<Identity> loadIdentities(File file) {
        
        LinkedList<Identity> identities = new LinkedList<Identity>();

        Document d = XMLTools.parseXmlFile(file, false);
        Element rootEl = d.getDocumentElement();

        List lists = XMLTools.getChildElementsByTagName(rootEl, "BuddyList");
        Iterator it = lists.iterator();

        while (it.hasNext()) {
            Element current = (Element) it.next();
            if (current.getAttribute("type").equals("friends")) {
                BuddyList buddyList = new BuddyList();
                try {
                    buddyList.loadXMLElement(current);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "Error loading good identities", e);
                }
                for(Iterator i = buddyList.getAllValues().iterator(); i.hasNext(); ) {
                    Identity id = (Identity)i.next();
                    id.setGOODWithoutUpdate();
                    identities.add(id);
                }
            } else if (current.getAttribute("type").equals("enemies")) {
                BuddyList buddyList = new BuddyList();
                try {
                    buddyList.loadXMLElement(current);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "Error loading bad identities", e);
                }
                for(Iterator i = buddyList.getAllValues().iterator(); i.hasNext(); ) {
                    Identity id = (Identity)i.next();
                    id.setBADWithoutUpdate();
                    identities.add(id);
                }
            } else if (current.getAttribute("type").equals("neutral")) {
                BuddyList buddyList = new BuddyList();
                try {
                    buddyList.loadXMLElement(current);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "Error loading check identities", e);
                }
                for(Iterator i = buddyList.getAllValues().iterator(); i.hasNext(); ) {
                    Identity id = (Identity)i.next();
                    id.setCHECKWithoutUpdate();
                    identities.add(id);
                }
            } else if (current.getAttribute("type").equals("observed")) {
                BuddyList buddyList = new BuddyList();
                try {
                    buddyList.loadXMLElement(current);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "Error loading observe identities", e);
                }
                for(Iterator i = buddyList.getAllValues().iterator(); i.hasNext(); ) {
                    Identity id = (Identity)i.next();
                    id.setOBSERVEWithoutUpdate();
                    identities.add(id);
                }
            }
        }
        
        return identities;
    }

    /**
     * Returns -1 on error, 0 if no identity is to export and no file was created,
     * or >0 for exported identity count.
     */
    public static int saveIdentities(File file, List<Identity> identities) {
        
        BuddyList friends = new BuddyList();
        BuddyList observed = new BuddyList();
        BuddyList enemies = new BuddyList();
        
        int count = 0;
        
        for( Iterator i = identities.iterator(); i.hasNext(); ) {
            Identity id = (Identity) i.next();
            if( id.isGOOD() ) {
                friends.add(id);
                count++;
            } else if( id.isOBSERVE() ) {
                observed.add(id);
                count++;
            } else if( id.isBAD() ) {
                enemies.add(id);
                count++;
            }
            // we ignore CHECK ids here! this is the default
        }
        
        if( count == 0 ) {
            // dont create an empty file
            return count;
        }
        
        Document d = XMLTools.createDomDocument();
        Element rootElement = d.createElement("FrostIdentities");

        // then friends
        Element friendsElement = friends.getXMLElement(d);
        friendsElement.setAttribute("type", "friends");
        rootElement.appendChild(friendsElement);
        // then enemies
        Element enemiesElement = enemies.getXMLElement(d);
        enemiesElement.setAttribute("type", "enemies");
        rootElement.appendChild(enemiesElement);
        // then observed
        Element observedElement = observed.getXMLElement(d);
        observedElement.setAttribute("type", "observed");
        rootElement.appendChild(observedElement);

        d.appendChild(rootElement);

        if( XMLTools.writeXmlFile(d, file) ) {
            return count;
        } else {
            return -1;
        }
    }

    private static class BuddyList implements XMLizable {

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
            String str = user.getUniqueName();
            if (containsKey(str)) {
                return false;
            }
            hashMap.put(str, user);

            return true;
        }

        public boolean containsKey(String key) {
            return hashMap.containsKey(Mixed.makeFilename((String) key));
        }

        public synchronized Element getXMLElement(Document doc) {
            Element main = doc.createElement("BuddyList");
            Iterator it = hashMap.values().iterator();
            while (it.hasNext()) {
                Identity id = (Identity) it.next();
                Element el = id.getExportXMLElement(doc);
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
        protected Object remove(String key) {
            return hashMap.remove(Mixed.makeFilename(key));
        }
        public int size() {
            return hashMap.size();
        }
        public Collection getAllValues() {
            return hashMap.values();
        }
    }
}
