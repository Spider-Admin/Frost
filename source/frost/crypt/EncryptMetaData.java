/*
  EncryptMetaData.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.crypt;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.identities.*;

/**
 * @author zlatinb
 * 
 * MetaData for encrypted messages
 * Format:
 * <FrostMetaData>
 *  <MyIdentity>
 *   <name> unique name of sender </name>
 *   <key> public key of sender </key>
 *  </MyIdentity>
 *  
 *  <sig> signature of file </sig>
 *  
 *  <Recipient>
 *   <name> unique name of recipient </name>
 *  </Recipient>
 * </FrostMetaData>
 */
public class EncryptMetaData extends SignMetaData {

	String recipient; // unique name of recipient
    
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

        super.loadXMLElement(e); // get sender and sig
        
        if( XMLTools.getChildElementsByTagName(e,"Recipient").size() > 0 ) {
            recipient = XMLTools.getChildElementsCDATAValue(e, "Recipient");
        }
		assert recipient!=null;
	}
	
	public EncryptMetaData(Element e) throws SAXException{
		loadXMLElement(e);
	}
	
	/**
	 * @return Returns the sender.
	 */
	public String getRecipient() {
		return recipient;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {

		Element el = super.getXMLElement(container); // set sender and sig
        
        if( recipient != null ) {
            Element rec = container.createElement("Recipient");
            CDATASection cdata = container.createCDATASection(recipient);
            rec.appendChild(cdata);
            el.appendChild(rec);
        }
		return el;
	}
	
	/**
	 * Creates metadata to be attached to encrypted message.
     * 
	 * @param recipient the recepient
	 */
	public EncryptMetaData(byte[] encrypted_data, LocalIdentity myId, String recipient) {
        
        super(encrypted_data, myId); // compute sig for encrypted_data
        
        this.recipient = recipient;
	}
    
    /**
     * represents something that was received and needs to be verified
     * @param plaintext the plaintext to be verified
     * @param el the xml element to populate from
     */
    public EncryptMetaData(byte [] plaintext, Element el) throws SAXException {
        this.plaintext = plaintext;
        try {
            loadXMLElement(el);
        } catch (SAXException e) {
            plaintext = null;
            throw e;
        }
    }
}
