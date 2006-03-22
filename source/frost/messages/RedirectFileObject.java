/*
 RedirectFileObject.java / Frost
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

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import frost.gui.objects.Board;
import frost.*;

/**
 * @author zlatinb
 *
 * represents a fileObject that also contains a redirect in it.
 */
public abstract class RedirectFileObject extends SharedFileObject {

	public static final int FEC_REDIRECT = 1;
	
	public abstract int getRedirectType();

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element current) throws SAXException {

		assert current.getAttribute("redirect").length()>0 : 
			"redirect attribute not found!";

		super.loadXMLElement(current);		
	}

	/**
	 * @param file
	 * @param board
	 */
	public RedirectFileObject(File file, Board board) {
		super(file, board);
	}

	/**
	 * 
	 */
	public RedirectFileObject() {
		super();
	}

	
	public abstract String getRedirect();
	
	public static SharedFileObject getRedirectInstance(Element e) throws SAXException{
		assert e.getAttribute("redirect").length() > 0 &&
		XMLTools.getChildElementsByTagName(e,"redirect").size() > 0: "element does not contain redirect";
		
		SharedFileObject result = null;
		if (e.getAttribute("redirect").equals("FEC")) {
			result = new FECRedirectFileObject();
			result.loadXMLElement(e);
			
		}
		return result;
	}
}
