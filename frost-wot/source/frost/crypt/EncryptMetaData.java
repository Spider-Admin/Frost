/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.crypt;

import frost.*;
import frost.identities.Identity;


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

	/* (non-Javadoc)
	 * @see frost.crypt.MetaData#getType()
	 */
	public int getType() {
		assert false; //until this is implemented
		return MetaData.ENCRYPT;
	}

	
	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _person = (Element) XMLTools.getChildElementsByTagName(e,"Identity").iterator().next();
		person = new Identity(_person);
		
		assert person!=null;
	}
	
	public EncryptMetaData(Element e) throws SAXException{
		loadXMLElement(e);
	}


}
