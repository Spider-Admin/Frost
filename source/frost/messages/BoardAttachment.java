/*
 BoardAttachment.java / Frost
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
package frost.messages;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.gui.objects.Board;

public class BoardAttachment extends Attachment implements SafeXMLizable {

	private Board boardObj;

	/* (non-Javadoc) 
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		return Attachment.BOARD;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {

		Element el = getSafeXMLElement(container);

		CDATASection cdata;

		Element privkey = container.createElement("privKey");
		cdata = container.createCDATASection(Mixed.makeSafeXML(boardObj.getPrivateKey())); //null is ok
		privkey.appendChild(cdata);
		el.appendChild(privkey);
		
		Element description = container.createElement("description");
		cdata = container.createCDATASection(Mixed.makeSafeXML(boardObj.getDescription()));	//null is ok
		description.appendChild(cdata);
		el.appendChild(description);

		return el;
	}

	/* (non-Javadoc)
	 * @see frost.SafeXMLizable#getSafeXMLElement(org.w3c.dom.Document)
	 */
	public Element getSafeXMLElement(Document container) {

		Element el = container.createElement("Attachment");
		el.setAttribute("type", "board");

		CDATASection cdata;

		Element name = container.createElement("Name");
		cdata = container.createCDATASection(Mixed.makeSafeXML(boardObj.getName()));
		name.appendChild(cdata);
		el.appendChild(name);

		Element pubkey = container.createElement("pubKey");
		cdata = container.createCDATASection(Mixed.makeSafeXML(boardObj.getPublicKey()));
		pubkey.appendChild(cdata);
		el.appendChild(pubkey);

		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		String name, privkey, pubkey, description;
		name = XMLTools.getChildElementsCDATAValue(e, "Name");
		privkey = XMLTools.getChildElementsCDATAValue(e, "privKey");
		pubkey = XMLTools.getChildElementsCDATAValue(e, "pubKey");
		description = XMLTools.getChildElementsCDATAValue(e, "description");

		boardObj = new Board(name, pubkey, privkey, description);
	}

	/**
	 * @param e
	 * @throws SAXException
	 */
	protected BoardAttachment(Element e) throws SAXException {
		loadXMLElement(e);
	}

	/**
	 * @param obj
	 */
	public BoardAttachment(Board obj) {
		boardObj = obj;
	}

	/**
	 * @return a FrostBoardObject
	 */
	public Board getBoardObj() {
		return boardObj;
	}

	/**
	 * A toString to enable the SortedSet to sort by boardname
	 */
	public String toString() {
		if (getBoardObj() != null) {
			return getBoardObj().getName();
		}
		return "*ERR*";
	}

	/* 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		String me = toString();
		String other = ((BoardAttachment) o).toString();
		return me.compareToIgnoreCase(other);
	}

}
