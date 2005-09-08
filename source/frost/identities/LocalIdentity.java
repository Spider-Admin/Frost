/*
  LocalIdentity.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.io.IOException;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.fcp.*;
import frost.gui.objects.Board;
import frost.messages.BoardAttachment;

/**
 * Represents the main user's identity
 */
public class LocalIdentity extends Identity
{
    private String privKey;
    
	private static Logger logger = Logger.getLogger(LocalIdentity.class.getName());

	public Element getXMLElement(Document doc) {
	
		//have to copy all children, no Element.rename()unfortunately
		Element el = super.getXMLElement(doc);
		Element el2 = doc.createElement("MyIdentity");
		NodeList list = el.getChildNodes();
		while(list.getLength()>0)
			el2.appendChild(list.item(0));
		
		
		Element element = doc.createElement("privKey");
		CDATASection cdata = doc.createCDATASection(privKey);
		element.appendChild(cdata);
		el2.appendChild(element);
		return el2;
	}
	/**
	 * just renames the element to MyIdentity to maintain
	 * backward compatibility
	 */
	public Element getSafeXMLElement(Document doc){
		Element el = super.getSafeXMLElement(doc);
		Element el2 = doc.createElement("MyIdentity");
		NodeList list = el.getChildNodes();
		while (list.getLength()>0)
			el2.appendChild(list.item(0));
		return el2;	
		
	}
	

	public void loadXMLElement(Element el) throws SAXException {
		super.loadXMLElement(el);
		privKey =  XMLTools.getChildElementsCDATAValue(el, "privKey");
	}

    /**
     * a constructor that assumes that the user has inserted the
     * key in his SSK already
     */
    public LocalIdentity(String name, String[] keys) {
        super(name,  keys[1]);
        privKey=keys[0];
    }

	public LocalIdentity(Element el){
		super(el);
	}
	/**
	 * constructor that creates an RSA Keypair
	 */
	public LocalIdentity(String name) {
		this(name, Core.getCrypto().generateKeys());
        
        generateOwnBoard();

//		FcpConnection connection = FcpFactory.getFcpConnectionInstance();
//		if (connection == null) {
//			this.key = NA;
//			return;
//		}
//        // generate own board keys
//		try {
//			String[] svk = connection.getKeyPair();
//			board = new BoardAttachment(new Board(getUniqueName(), svk[1], svk[0], null));
//
//		} catch (IOException ex) {
//			logger.log(Level.SEVERE, "Exception thrown in constructor", ex);
//			board = null;
//		}
	}
    
    public void generateOwnBoard() {
        if( board == null ) {
            FcpConnection connection = FcpFactory.getFcpConnectionInstance();
            if (connection == null) {
//                this.key = NA;
                return;
            }
            // generate own board keys
            try {
                String[] svk = connection.getKeyPair();
                board = new BoardAttachment(new Board(getUniqueName(), svk[1], svk[0], null));

            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Exception thrown in constructor", ex);
                board = null;
            }
        }
    }

    public String getPrivKey() {
        return privKey;
    }
}

