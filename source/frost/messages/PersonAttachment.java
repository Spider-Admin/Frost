/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.XMLTools;
import frost.identities.Identity;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PersonAttachment extends Attachment {

	private Identity identity;

	/**
	 * @param e
	 * @throws SAXException
	 */
	public PersonAttachment(Element e) throws SAXException {
		loadXMLElement(e);
	}

	/**
	 * @param newIdentity
	 */
	public PersonAttachment(Identity newIdentity) {
		identity = newIdentity;
	}

	/* 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		String myKey = identity.getKey();
		String otherKey = ((PersonAttachment) o).getIdentity().getKey();
		return myKey.compareTo(otherKey);
	}

	/**
	 * @return
	 */
	public Identity getIdentity() {
		return identity;
	}
	
	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		return Attachment.PERSON;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		Element el = container.createElement("Attachment");
		el.setAttribute("type", "person");
		el.appendChild(identity.getSafeXMLElement(container));
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _person =
			(Element) XMLTools.getChildElementsByTagName(e, "Identity").iterator().next();
		identity = new Identity(_person);
	}

}
