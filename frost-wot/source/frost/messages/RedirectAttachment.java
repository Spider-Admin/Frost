/*
 * Created on Nov 16, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import frost.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * An attachment which contains the entire FEC redirect of a given file 
 * as a CDATA section.  It is meant to be used as an internal attachment to the
 * FileAttachment object.
 */
public class RedirectAttachment extends FileAttachment {

	//the entire redirect will be contained here
	String entireRedirect;
	
	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		
		return FEC_REDIRECT;
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
		Element el = super.getXMLElement(container);
		el.setAttribute("FEC","true");
		Element el2 = container.createElement("Redirect");
		CDATASection cdata = container.createCDATASection(entireRedirect);
		el2.appendChild(cdata);
		el.appendChild(el2);
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		if (!e.getNodeName().equals("Attachment")) 
			throw new SAXException("missing \"Attachment\" tag");
		
		super.loadXMLElement(e);
		entireRedirect = XMLTools.getChildElementsCDATAValue(e,"Redirect");

	}

	/**
	 * @return Returns the entireRedirect.
	 */
	public String getRedirect() {
		return entireRedirect;
	}

	RedirectAttachment(Element el) throws SAXException{
		super(el);
	}
	
	RedirectAttachment(SharedFileObject sfo, String redirect){
		super(sfo);
		entireRedirect = redirect;
	}
}
