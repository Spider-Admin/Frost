/*
  MetaData.java / Frost
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

import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.identities.*;
import frost.util.*;

public abstract class MetaData implements XMLizable {

    private static final Logger logger = Logger.getLogger(MetaData.class.getName());

    public static final int SIGN    = 0;
    public static final int ENCRYPT = 1;

    public abstract int getType();

    Identity person; // sender

    /**
     * @return the person (sender) of the message
     */
    public Identity getPerson(){
        return person;
    }

    public Element getXMLElement(Document container){
        Element el = container.createElement("FrostMetaData");
        
        // for backward compatability we add "Identity" and "MyIdentity" to the metadata.
        // "MyIdentity" is for older Frosts...
        // SignMetaData class is able to read both types of identity tags
        
        // with tag "Identity"
        Element _person = person.getXMLElement(container);
        el.appendChild(_person);
        
        // with tag "MyIdentity"
        Element _person_old = person.getXMLElement_old(container);
        el.appendChild(_person_old);
        
        return el;
    }

    public static MetaData getInstance(Element e) {

        if( e == null ) {
            logger.log(Level.SEVERE, "MetaData.getInstance(): The provided XML element is null.");
            return null;
        }
        if( e.getNodeName().equals("FrostMetaData") == false ) {
            logger.log(Level.SEVERE, "MetaData.getInstance(): This is no FrostMetaData XML element.");
            return null;
        }

        try {
            if (XMLTools.getChildElementsByTagName(e,"Recipient").size() == 0) {
                return new SignMetaData(e);
            } else {
                return new EncryptMetaData(e);
            }
        } catch(SAXException ex) {
            logger.log(Level.SEVERE, "Exception thrown in getInstance(byte [] body, Element e)", ex);
        }
        return null;
    }
}
