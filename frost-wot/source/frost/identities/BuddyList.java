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

	/**
	 * adds a user to the list
	 * returns false if the user exists
	 */
	public synchronized boolean add(Identity user) {
		if (containsKey(Mixed.makeFilename(user.getUniqueName()))) {
			return false;
		} else {
			hashMap.put(Mixed.makeFilename(user.getUniqueName()), user);
			return true;
		}
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
		if (containsKey(Mixed.makeFilename(name))) {
			return (Identity) hashMap.get(Mixed.makeFilename(name));
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
		if (el == null)
			return;
		List l = XMLTools.getChildElementsByTagName(el, "Identity");
		Iterator it = l.iterator();
		while (it.hasNext())
			add(new Identity((Element) it.next()));

	}

	/**
	 * @param key
	 * @return
	 */
	public Object remove(String key) {
		return hashMap.remove(Mixed.makeFilename(key));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public Object remove(Identity key) {
		return hashMap.remove(key);
	}

	/**
	 * @return
	 */
	public int size() {
		return hashMap.size();
	}

}
