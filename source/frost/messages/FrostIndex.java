/*
  FrostIndex.java / Frost
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
package frost.messages;

import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.identities.Identity;
/**
 * @author zlatinb
 *
 * represents an index file in Freenet
 */
public class FrostIndex implements XMLizable {

	Identity sharer;
	Map filesMap;
	
	private static Logger logger = Logger.getLogger(FrostIndex.class.getName());

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {

		Element el = container.createElement("FrostIndex");
		
		//if user signs uploads, remove the sensitive fields and append element
		if (Core.frostSettings.getBoolValue("signUploads") && sharer!=null) {
			Element _sharer = sharer.getSafeXMLElement(container);
			el.appendChild(_sharer);
		}
		
		boolean signUploads = Core.frostSettings.getBoolValue("signUploads");
		// iterate through set of files and add them all
		for(Iterator i = getFilesMap().values().iterator(); i.hasNext(); ) {
			SharedFileObject current = (SharedFileObject)i.next();
			Element currentElement = current.getXMLElement(container);
			
			//remove sensitive information
			List sensitive = XMLTools.getChildElementsByTagName(currentElement,"lastSharedDate");
			
			//strip the owner field if file is not signed
			if (!signUploads) {
				sensitive.addAll(XMLTools.getChildElementsByTagName(currentElement,"owner"));
            }
			
			for(Iterator i2 = sensitive.iterator(); i2.hasNext(); ) {
				currentElement.removeChild((Element)i2.next());
            }
			el.appendChild(currentElement);
		}
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		List _sharer = XMLTools.getChildElementsByTagName(e,"MyIdentity");
		if (_sharer.size() > 0) {
			sharer = new Identity((Element)_sharer.get(0));
        } else { 
			sharer = null;
        }
			
		List _files = XMLTools.getChildElementsByTagName(e,"File");
		
		filesMap = new HashMap(); //TODO: maybe put a TreeSet and keep all sorted
		Iterator it = _files.iterator();
		while (it.hasNext()) {
			Element el = (Element)it.next();
			SharedFileObject file = SharedFileObject.getInstance(el);
			if (file.getSHA1()!=null) {
				filesMap.put(file.getSHA1(),file);
            }
		}
	}

	public FrostIndex(Element e) {
		try {
			loadXMLElement(e);
		} catch (SAXException ex){
			logger.log(Level.SEVERE, "Exception thrown in constructor", ex);
		}
	}
	
	public FrostIndex(Map filesMap) {
		this.filesMap = filesMap;
		
		if (Core.frostSettings.getBoolValue("signUploads")) {
			sharer = Core.getInstance().getIdentities().getMyId();
        } else { 
			sharer = null;
        }
	}

	/**
	 * @return the person sharing the index
	 */
	public Identity getSharer() {
		return sharer;
	}

	/**
	 * @return
	 */
	public Map getFilesMap() {
		return filesMap;
	}
}
