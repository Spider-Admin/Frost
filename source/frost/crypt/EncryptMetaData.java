/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.crypt;

import frost.*;
import frost.identities.Identity;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * MetaData for encrypted messages
 * Format:
 * <FrostMetaData>
 * <Identity>
 * <name>...</name>
 * <pubKey>...</pubKey>
 * </Identity>
 * </FrostMetaData>
 */
public class EncryptMetaData extends MetaData {

	Identity sender;
	/* (non-Javadoc)
	 * @see frost.crypt.MetaData#getType()
	 */
	public int getType() {
		return MetaData.ENCRYPT;
	}

	
	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _person = (Element) XMLTools.getChildElementsByTagName(e,"Identity").iterator().next();
		person = new Identity(_person);
		
		//signing optional now, but we may make it mandatory
		if (XMLTools.getChildElementsByTagName(e,"MyIdentity").size()>0){
			Element _sender = (Element) XMLTools.getChildElementsByTagName(e,"MyIdentity").get(0);
			sender = new Identity(_sender);
		}
		
		assert person!=null;
	}
	
	public EncryptMetaData(Element e) throws SAXException{
		loadXMLElement(e);
	}
	
	


	/**
	 * @return Returns the sender.
	 */
	public Identity getSender() {
		return sender;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {

		Element el = super.getXMLElement(container);
		if (sender!=null) {
			Element _sender = sender.getSafeXMLElement(container);
			el.appendChild(_sender)
		}
		return el;
	}
	
	/**
	 * Creates metadata to be attached to encrypted message
	 * @param recipient the recepient
	 * @param sign whether to sign the message
	 */
	public EncryptMetaData(Identity recipient, boolean sign){
		person = recipient;
		if (sign)
			sender = Core.getMyId();
	}

}
