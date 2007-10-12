/*
 AttachmentList.java / Frost
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

import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.util.*;

public class AttachmentList extends LinkedList implements XMLizable {

	private static final Logger logger = Logger.getLogger(AttachmentList.class.getName());

	/**
	 * traverses the list for attachment of given type
	 * @param type the type - BOARD, FILE, PERSON...
	 * @return another list which contains only attachments of the specified type
	 */
	public AttachmentList getAllOfType(final int type) {
		assert type == Attachment.FILE ||
					type == Attachment.BOARD ||
					type == Attachment.PERSON :
					"list of unknown type of attachments requested";

		final Iterator<Attachment> i = iterator();
		final AttachmentList result = new AttachmentList();
		while (i.hasNext()) {
			final Attachment current = i.next();
			if (current.getType() == type) {
				result.add(current);
            }
		}

		return result;
	}

	synchronized public Element getXMLElement(final Document d){
        if( size() == 0 ) {
            return null;
        }
		final Element el = d.createElement("AttachmentList");
		final Iterator<Attachment> i = iterator();
		while (i.hasNext()) {
			final Attachment current = i.next();
			el.appendChild(current.getXMLElement(d));
		}
		return el;
	}

	public void loadXMLElement(final Element el) throws SAXException {
        if( el == null ) {
            return;
        }
		final Iterator<Element> i = XMLTools.getChildElementsByTagName(el,"Attachment").iterator();
		while (i.hasNext()){
			final Element current = i.next();
			final Attachment attachment = Attachment.getInstance(current);
			add(attachment);
		}
		if (size()==0) {
			logger.info("empty attachment list upon creation");
        }
	}
}
