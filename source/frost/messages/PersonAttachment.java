/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;
import frost.*;

import frost.identities.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PersonAttachment implements Attachment {

	Identity person;
	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		
		return Attachment.PERSON;
	}

	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getMessage()
	 */
	public MessageObject getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		Element el = container.createElement("Attachment");
		el.setAttribute("type","person");
		el.appendChild(person.getXMLElement(container));
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _person = (Element)XMLTools.getChildElementsByTagName(e,"Identity").iterator().next();
		person = new Identity(_person);

	}
	
	public PersonAttachment(Identity i) {
		person =i;
	}
	
	public PersonAttachment(Element e) throws SAXException{
		loadXMLElement(e);
	}

}
