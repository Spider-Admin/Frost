/*
  EncryptMetaData.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.identities.*;
import frost.util.*;

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
 *  <Recipient> unique name of recipient </Recipient>
 * </FrostMetaData>
 */
public class EncryptMetaData extends SignMetaData {

    private static final Logger logger = Logger.getLogger(EncryptMetaData.class.getName());

    String recipient; // unique name of recipient

    /**
     * Creates MetaData from received metadata.
     * Loads person, sig and receiver from XML.
     */
    public EncryptMetaData(Element el) throws SAXException {
        try {
            loadXMLElement(el);
        } catch (SAXException e) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
            throw e;
        }
    }

    /**
     * Creates metadata to be attached to encrypted message.
     * Computes signature for encrypted_data.
     *
     * @param recipient the recepient
     */
    public EncryptMetaData(byte[] encrypted_data, LocalIdentity myId, String recipient) {

        super(encrypted_data, myId); // compute sig for encrypted_data

        assert recipient != null;

        this.recipient = recipient;
    }

    /**
     * Returns XML with person (sender), sig and recipient set.
     */
    public Element getXMLElement(Document container) {

        Element el = super.getXMLElement(container); // set sender and sig

        if( recipient != null ) {
            Element rec = container.createElement("Recipient");
            CDATASection cdata = container.createCDATASection(recipient);
            rec.appendChild(cdata);
            el.appendChild(rec);
        } else {
            logger.log(Level.SEVERE, "getXMLElement: recipient is null, this is not allowed!");
            return null;
        }
        return el;
    }

    /**
     * Loads person (sender), sig and recipient from XML data.
     */
    public void loadXMLElement(Element e) throws SAXException {

        super.loadXMLElement(e); // get sender and sig

        if( XMLTools.getChildElementsByTagName(e,"Recipient").size() > 0 ) {
            recipient = XMLTools.getChildElementsCDATAValue(e, "Recipient");
        }
        assert recipient != null && recipient.length() > 0;
    }

    /**
     * @return Returns the recipient.
     */
    public String getRecipient() {
        return recipient;
    }

    /* (non-Javadoc)
     * @see frost.crypt.MetaData#getType()
     */
    public int getType() {
        return MetaData.ENCRYPT;
    }
}
