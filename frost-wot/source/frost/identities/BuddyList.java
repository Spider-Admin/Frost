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
        if (containsKey(user.getName()))
        {
            return false;
        }
        else
        {
            put(user.getName(), user);
            return true;
        }
    }

    /**
     * returns the user in the list, null if not in
     */
    public synchronized Identity Get(String name)
    {
        if (containsKey(name))
        {
            return (Identity)get(name);
        }
        else
        {
            return null;
        }
    }
}
