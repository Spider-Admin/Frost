/*
  Identity.java / Frost
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


import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.messages.*;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity implements SafeXMLizable
{
    private String name;
    private String uniqueName;
    protected String key;
    protected BoardAttachment board = null;
    private long lastSeenTimestamp = -1;
    
	private static Logger logger = Logger.getLogger(Identity.class.getName());
    
    //some trust map methods
    public int noMessages,noFiles;
    protected Set trustees;

	//if this was C++ LocalIdentity wouldn't work
	//fortunately we have virtual construction so loadXMLElement will be called
	//for the inheriting class ;-)
	public Identity(Element el) {
		try {
			loadXMLElement(el);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
		}
	}


	public Element getXMLElement(Document doc)  {
		Element el = getSafeXMLElement(doc);
		
		//# of files
		Element element = doc.createElement("files");
		Text text = doc.createTextNode(""+noFiles);
		element.appendChild(text);
		el.appendChild(element);
		
		//# of messages
		element = doc.createElement("messages");
		text = doc.createTextNode(""+noMessages);
		element.appendChild(text);
		el.appendChild(element);

        // last seen timestamp
        if( getLastSeenTimestamp() > 0 ) {
            element = doc.createElement("lastSeenTimestamp");
            text = doc.createTextNode(""+getLastSeenTimestamp());
            element.appendChild(text);
            el.appendChild(element);
        }

		//if board is present, remove the safe element and add the
		//full one.
		if (board!=null) {
			List list = XMLTools.getChildElementsByTagName(el,"Attachment");
			if (list.size() > 0)
				el.removeChild((Element)(list.get(0)));
			el.appendChild(board.getXMLElement(doc));
		}
		
		//trusted identities
		if (trustees != null) {
			element = doc.createElement("trustedIds");
			Iterator it = trustees.iterator();
			while (it.hasNext()) {
				String id = (String)it.next();
				Element trustee = doc.createElement("trustee");
				CDATASection cdata = doc.createCDATASection(id);
				trustee.appendChild(cdata);
				element.appendChild(trustee);
			}
			el.appendChild(element);
		}
		return el;
	}
	
	//same method used for LocalIdentity
	public Element getSafeXMLElement(Document doc){
		Element el = doc.createElement("Identity");
		
		//name
		Element element = doc.createElement("name");
		CDATASection cdata = doc.createCDATASection(getUniqueName());
		element.appendChild( cdata );
		el.appendChild( element );
		
		//key itself
		element = doc.createElement("key");
		cdata = doc.createCDATASection(getKey());
		element.appendChild( cdata );
		el.appendChild( element );
		
		//board
		if (board!=null) {
			el.appendChild(board.getSafeXMLElement(doc));
        }
		return el;
	}
	
	public void loadXMLElement(Element e) throws SAXException {
		uniqueName = XMLTools.getChildElementsCDATAValue(e, "name");
		name = uniqueName.substring(0,uniqueName.indexOf("@"));
		key =  XMLTools.getChildElementsCDATAValue(e, "key");
		try {
			String _msg = XMLTools.getChildElementsTextValue(e,"messages");
			noMessages = _msg == null ? 0 : Integer.parseInt(_msg);
			String _files = XMLTools.getChildElementsTextValue(e,"files");
			noFiles = _files == null ? 0 : Integer.parseInt(_files);
		} catch (Exception npe) {
			logger.log(Level.SEVERE, "No data about # of messages found for identity " + uniqueName, npe);
		}

        String _lastSeenStr = XMLTools.getChildElementsTextValue(e,"lastSeenTimestamp");
        if( _lastSeenStr != null && ((_lastSeenStr=_lastSeenStr.trim())).length() > 0 ) {
            lastSeenTimestamp = Long.parseLong(_lastSeenStr);
        } else {
            // not yet set, init with current timestamp
            lastSeenTimestamp = System.currentTimeMillis();
        }

		// see if board is attached
		List _board = XMLTools.getChildElementsByTagName(e,"Attachment");
		if (_board.size() > 0) {
			try {
				board = (BoardAttachment)Attachment.getInstance((Element)(_board.get(0)));
			} catch(ClassCastException ex) {
				logger.log(Level.SEVERE, "Exception thrown in loadXMLElement(Element e)", ex);
				board =null;
			}	
        } else {
            board = null;
        }
        
		// check for trustees
		ArrayList _trusteesList = XMLTools.getChildElementsByTagName(e,"trustees");
		Element trusteesList = null;
		if (_trusteesList.size() > 0) {
			trusteesList = (Element) _trusteesList.get(0);
        }
		if (trusteesList != null) {
			if (trustees == null) {
				trustees = new TreeSet();
            }
			List trusteeEntities = XMLTools.getChildElementsByTagName(trusteesList,"trustee");
			Iterator it = trusteeEntities.iterator();
			while (it.hasNext()) {
				Element trustee = (Element)it.next();
				String id = ((CDATASection) trustee.getFirstChild()).getData().trim();
				trustees.add(id);
			}
		}
	}

    /**
     * we use this constructor whenever we have all the info
     */
    public Identity(String name, String key)
    {
        this.key = key;
     	this.name = name;
     	if (name.indexOf("@")!=-1)
     		this.uniqueName = name;
     	else 
     		setName(name);
    }

    private void setName(String nam)
    {
        this.name = nam;
//        if( getKey().equals( NA ) )
//            this.uniqueName = nam;
//        else
        this.uniqueName = nam + "@" + Core.getCrypto().digest( getKey() );
    }

    //obvious stuff
    public String getName()
    {
        return name;
    }
    
    public String getKey()
    {
        return key;
    }
    public String getStrippedName()
    {
        return new String(name.substring(0,name.indexOf("@")));
    }


    public String getUniqueName()
    {
        return Mixed.makeFilename(uniqueName);
    }
	/**
	 * @return list of identities this identity trusts
	 */
	public Set getTrustees() {
		if (trustees== null ) trustees= new TreeSet();
		return trustees;
	}

	/**
	 * @return Returns the board.
	 */
	public BoardAttachment getBoard() {
		return board;
	}
    
    public void setBoard(BoardAttachment b) {
        if( board == null ) {
            board = b;
        }
    }

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }
    
    public void updateLastSeenTimestamp() {
        lastSeenTimestamp = System.currentTimeMillis();
    }
}
