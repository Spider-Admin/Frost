package frost.identities;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.*;
import frost.*;

/**
 * contains the people the local user trusts
 */

public class BuddyList extends HashMap implements XMLizable
{
	
	// note - I decided to keep the same structure.  Its probably better from
	//XML point of view to have each identity's trust status marked as attribute,
	//but this way is easier..
	public Element getXMLElement(Document doc){
		Element main = doc.createElement("BuddyList");
		Iterator it = values().iterator();
		while (it.hasNext()) {
			Identity id = (Identity)it.next();
			Element el = id.getXMLElement(doc);
			main.appendChild(el);
		}
		return main;
	}
	
	public void loadXMLElement(Element el) throws SAXException {
		if (el == null) return;
		List l = XMLTools.getChildElementsByTagName(el,"Identity");
		Iterator it = l.iterator();
		while (it.hasNext()) 
			Add( new Identity((Element)it.next()));
		
	}
    /**constructor*/
    public BuddyList()
    {
        super(100);  //that sounds like a reasonable number
    }

    /**
     * adds a user to the list
     * returns false if the user exists
     */
    public synchronized boolean Add(Identity user)
    {
        if (containsKey(mixed.makeFilename(user.getUniqueName())))
        {
            return false;
        }
        else
        {
            put(mixed.makeFilename(user.getUniqueName()), user);
            return true;
        }
    }

    /**
     * returns the user in the list, null if not in
     */
    public synchronized Identity Get(String name)
    {
        if (containsKey(mixed.makeFilename(name)))
        {
            return (Identity)get(mixed.makeFilename(name));
        }
        else
        {
            return null;
        }
    }
	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object key) {
		if (key instanceof String)
		return super.remove(mixed.makeFilename((String)key));
		
		return super.remove(key);
	}
	
	public boolean containsKey(Object key){
		if (key instanceof String)
			return super.containsKey(mixed.makeFilename((String)key));
		else return false;
	}

}
