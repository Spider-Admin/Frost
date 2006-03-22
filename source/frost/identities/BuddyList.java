/*
  BuddyList.java / Frost
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

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;

/**
 * contains the people the local user trusts
 */
public class BuddyList implements XMLizable {

    private HashMap hashMap = null;

    /**constructor*/
    public BuddyList() {
        hashMap = new HashMap(100); //that sounds like a reasonable number
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
