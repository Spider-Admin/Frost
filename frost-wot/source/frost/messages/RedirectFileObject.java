/*
 * Created on Nov 17, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * @author zlatinb
 *
 * represents a fileObject that also contains a redirect in it.
 */
public abstract class RedirectFileObject extends SharedFileObject {

	public static final int FEC_REDIRECT = 1;
	
	public abstract int getRedirectType();

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element current) throws SAXException {
		if (current.getAttribute("redirect").length()>0)
			super.loadXMLElement(current);
		else
			throw new SAXException("redirect attribute not found");
		
	}

}
