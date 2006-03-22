/*
 FECRedirectFileObject.java / Frost
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

import java.io.File;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import frost.XMLTools;
import frost.gui.objects.Board;

/**
 * @author zlatinb
 *
 * represents a fileObject holding a FEC redirect
 */
public class FECRedirectFileObject extends RedirectFileObject {

	String redirect;

	/* (non-Javadoc)
	 * @see frost.messages.RedirectFileObject#getRedirectType()
	 */
	public int getRedirectType() {
		return FEC_REDIRECT;
	}

	public Element getXMLElement(Document doc) {
		// TODO Auto-generated method stub
		Element el =  super.getXMLElement(doc);
		Element el2 = doc.createElement("redirect");
		CDATASection cdata = doc.createCDATASection(redirect);
		el2.appendChild(cdata);
		el.appendChild(el2);
		el.setAttribute("redirect","FEC");
		return el;
	}

	public void loadXMLElement(Element current) throws SAXException {
		assert current.getAttribute("redirect").equals("FEC") &&
			XMLTools.getChildElementsByTagName(current,"redirect").size()>0 :
				"fec redirect not present in file";
		super.loadXMLElement(current);		
		redirect = XMLTools.getChildElementsCDATAValue(current,"redirect");
	}

	/**
	 * @return Returns the redirect.
	 */
	public String getRedirect() {
		return redirect;
	}

	/**
	 * @param redirect The redirect to set.
	 */
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	/**
	 * @param file
	 * @param board
	 */
	public FECRedirectFileObject(File file, Board board) {
		super(file, board);
		redirect = null;
	}
	
	

	public FECRedirectFileObject() {
		super();
		redirect = null;
	}

}
