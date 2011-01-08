/*
 Attachment.java / Frost
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
package frost.messaging.frost;

import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.util.*;

@SuppressWarnings("serial")
public abstract class Attachment implements XMLizable, Comparable {
	public static final int FILE = 0;
	public static final int BOARD = 1;
	public static final int PERSON = 2;

	private static final Logger logger = Logger.getLogger(Attachment.class.getName());

	/**
	 * @return the type of this attachment
	 */
	public abstract int getType();

	public static Attachment getInstance(Element e) {

		assert e.getAttribute("type").length() > 0 : "attachment type not specified!";
		try {
			if (e.getAttribute("type").equals("file"))
				return new FileAttachment(e);
			else if (e.getAttribute("type").equals("board"))
				return new BoardAttachment(e);
			else
				return new PersonAttachment(e);
		} catch (SAXException ex) {
			logger.log(Level.SEVERE, "Exception thrown in getInstance(Element e)", ex);
			return null;
		}
	}

}
