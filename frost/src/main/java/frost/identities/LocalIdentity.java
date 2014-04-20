/*
  LocalIdentity.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.identities;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.util.*;

/**
 * Represents the main user's identity
 */
public class LocalIdentity extends Identity {

    private String privateKey;
    private String signature;

    private long lastFilesSharedMillis = 0;

    public LocalIdentity() {}

    @Override
    public Element getXMLElement(final Document doc) {
        // external element, "Identity"
        return super.getXMLElement(doc);
    }

    /**
     * Appends the private key!
     */
    @Override
    public Element getExportXMLElement(final Document doc) {
        // have to copy all children, no Element.rename()unfortunately
        final Element el = super.getXMLElement(doc);
        final Element el2 = doc.createElement("MyIdentity");
        final NodeList list = el.getChildNodes();
        while(list.getLength() > 0) {
            el2.appendChild(list.item(0)); // removes Node from el
        }
        {
            final Element element = doc.createElement("privKey");
            final CDATASection cdata = doc.createCDATASection(getPrivateKey());
            element.appendChild( cdata );
            el2.appendChild( element );
        }
        if( getSignature() != null ) {
            final Element element = doc.createElement("signature");
            final CDATASection cdata = doc.createCDATASection(getSignature());
            element.appendChild( cdata );
            el2.appendChild( element );
        }

        return el2;
    }

    @Override
    public void loadXMLElement(final Element el) throws SAXException {
        super.loadXMLElement(el);
        privateKey =  XMLTools.getChildElementsCDATAValue(el, "privKey");
        signature =  XMLTools.getChildElementsCDATAValue(el, "signature");
    }

    /**
     * Creates a new Identity, adds digest to name.
     */
    protected LocalIdentity(final String name, final String key) {
        super(name, key, true);
    }

    protected LocalIdentity(final String name, final String[] keys) {
        this(name,  keys[1]);
        this.privateKey = keys[0];
    }

    /**
     * Only used for migration.
     */
    public LocalIdentity(final String uname, final String pubKey, final String prvKey, final String sign) {
        this(uname, pubKey);
        privateKey = prvKey;
        signature = sign;
    }

    protected LocalIdentity(final Element el) throws Exception {
        // finally calls loadXMLElement of this class!
        super(el);
    }

    /**
     * Create a new Identity, read from the specified XML element.
     * Calls Mixed.makeFilename() on read uniqueName.
     *
     * @param el  the XML element containing the Identity information
     * @return    the new Identity, or null if Identity cannot be created (invalid input)
     */
    public static LocalIdentity createLocalIdentityFromXmlElement(final Element el) {
        try {
            return new LocalIdentity(el);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * constructor that creates an RSA Keypair
     */
    public LocalIdentity(final String name) {
        this(name, Core.getCrypto().generateKeys());
        // generateOwnBoard();
        // TODO: generate other than SSK
    }

//    void generateOwnBoard() {
//        if( board == null ) {
//            FcpConnection connection = FcpFactory.getFcpConnectionInstance();
//            if (connection == null) {
//                return;
//            }
//            // generate own board keys
//            try {
//                // TODO: change!
//                String[] svk = connection.getKeyPair();
//                board = new BoardAttachment(new Board(getUniqueName(), svk[1], svk[0], null));
//
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, "Exception thrown in constructor", ex);
//                board = null;
//            }
//        }
//    }

    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * @return  the signature the user entered for this identity.
     */
    public String getSignature() {
        return signature;
    }
    public void setSignature(final String s) {
        signature = s;
        updateIdentitiesStorage();
    }

    @Override
    public boolean isGOOD() {
        return true;
    }
    @Override
    public boolean isCHECK() {
        return false;
    }
    @Override
    public boolean isOBSERVE() {
        return false;
    }
    @Override
    public boolean isBAD() {
        return false;
    }

    public long getLastFilesSharedMillis() {
        return lastFilesSharedMillis;
    }
    public void setLastFilesSharedMillis(final long l) {
        lastFilesSharedMillis = l;
        modify();
    }
}
