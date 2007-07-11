/*
  SignMetaData.java / Frost
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

import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.identities.*;
import frost.util.*;

/**
 * @author zlatinb
 *
 * This file represents MetaData that's of a file in Freenet.
 * It has the following format:
 * <FrostMetaData>
 *  <MyIdentity>
 *   <name> unique name of person</name>
 *   <key> public key of person </key>
 *  </MyIdentity>
 *  <sig> signature of file </sig>
 * </FrostMetaData>
 */
public class SignMetaData extends MetaData {

    private static final Logger logger = Logger.getLogger(SignMetaData.class.getName());

    String sig;

    public SignMetaData() {
        person = null;
        sig = null;
    }

    /**
     * Represents a metadata of something about to be sent.
     * Computes signature of plaintext.
     */
    public SignMetaData(byte[] plaintext, LocalIdentity myId) {
        this.person = myId;
        sig = Core.getCrypto().detachedSign(plaintext, myId.getPrivKey());
    }

    /**
     * Metadata of something that was received.
     */
    public SignMetaData(byte [] metadata) throws Throwable {

        Document d = XMLTools.parseXmlContent(metadata, false);
        Element el = d.getDocumentElement();
        if( el.getTagName().equals("FrostMetaData") == false ) {
            throw new Exception("This is not FrostMetaData XML file.");
        }
        try {
            loadXMLElement(el);
        } catch (SAXException e) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
            throw e;
        }
    }

    /**
     * Represents something that was received and needs to be verified.
     * Parses XML and sets person and sig.
     * @param plaintext the plaintext to be verified
     * @param el the xml element to populate from
     */
    public SignMetaData(Element el) throws SAXException {
        try {
            loadXMLElement(el);
        } catch (SAXException e) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
     */
    public Element getXMLElement(Document container) {

        Element el = super.getXMLElement(container);
        
        Element _sig = container.createElement("sig");
        CDATASection cdata = container.createCDATASection(sig);
        _sig.appendChild(cdata);

        el.appendChild(_sig);
        return el;
    }

    /* (non-Javadoc)
     * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
     */
    public void loadXMLElement(Element e) throws SAXException {

        // New Frosts send "Identity" and "MyIdentity", old Frosts only "MyIdentity"
        List tags = XMLTools.getChildElementsByTagName(e, "Identity");
        if( tags.size() == 0 ) {
            // fallback to old format
            tags = XMLTools.getChildElementsByTagName(e, "MyIdentity");
        }
        
        Element _person = (Element) tags.iterator().next();
        person = new Identity(_person);
        sig = XMLTools.getChildElementsCDATAValue(e, "sig");

        assert person!=null && sig!=null;
    }

    /**
     * @return
     */
    public String getSig() {
        return sig;
    }

    /* (non-Javadoc)
     * @see frost.crypt.MetaData#getType()
     */
    public int getType() {
        return MetaData.SIGN;
    }
}
