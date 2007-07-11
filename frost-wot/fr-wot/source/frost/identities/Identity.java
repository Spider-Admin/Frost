/*
  Identity.java / Frost
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

import java.sql.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.util.*;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity implements XMLizable {

    private static final int GOOD    = 1;
    private static final int CHECK   = 2;
    private static final int OBSERVE = 3;
    private static final int BAD     = 4;
    
    private static final String GOOD_STRING    = "GOOD";
    private static final String CHECK_STRING   = "CHECK";
    private static final String OBSERVE_STRING = "OBSERVE";
    private static final String BAD_STRING     = "BAD";

    private String uniqueName;
    protected String key;
    private long lastSeenTimestamp = -1;

    int state = 2;
    String stateString = CHECK_STRING; 

    private static final Logger logger = Logger.getLogger(Identity.class.getName());

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

        Element el = doc.createElement("Identity");

        Element element = doc.createElement("name");
        CDATASection cdata = doc.createCDATASection(getUniqueName());
        element.appendChild( cdata );
        el.appendChild( element );

        element = doc.createElement("key");
        cdata = doc.createCDATASection(getKey());
        element.appendChild( cdata );
        el.appendChild( element );

        return el;
    }

    public Element getXMLElement_old(Document doc)  {

        Element el = doc.createElement("MyIdentity");

        Element element = doc.createElement("name");
        CDATASection cdata = doc.createCDATASection(getUniqueName());
        element.appendChild( cdata );
        el.appendChild( element );

        element = doc.createElement("key");
        cdata = doc.createCDATASection(getKey());
        element.appendChild( cdata );
        el.appendChild( element );

        return el;
    }

    public Element getExportXMLElement(Document doc)  {
        Element el = getXMLElement(doc);

        if( lastSeenTimestamp > -1 ) {
            Element element = doc.createElement("lastSeen");
            Text txt = doc.createTextNode(Long.toString(lastSeenTimestamp));
            element.appendChild( txt );
            el.appendChild( element );
        }
        return el;
    }

    public void loadXMLElement(Element e) throws SAXException {
        uniqueName = XMLTools.getChildElementsCDATAValue(e, "name");
        key =  XMLTools.getChildElementsCDATAValue(e, "key");

        String _lastSeenStr = XMLTools.getChildElementsTextValue(e,"lastSeen");
        if( _lastSeenStr != null && ((_lastSeenStr=_lastSeenStr.trim())).length() > 0 ) {
            lastSeenTimestamp = Long.parseLong(_lastSeenStr);
        } else {
            // not yet set, init with current timestamp
            lastSeenTimestamp = System.currentTimeMillis();
        }
        
        uniqueName = Mixed.makeFilename(uniqueName);
    }

    /**
     * we use this constructor whenever we have all the info
     */
    public Identity(String name, String key) {
        this.key = key;
        if( name.indexOf("@") != -1 ) {
            this.uniqueName = name;
        } else {
            this.uniqueName = name + "@" + Core.getCrypto().digest(getKey());
        }
        
        uniqueName = Mixed.makeFilename(uniqueName);
    }
    

    public Identity(String uname, String pubkey, long lseen, int s) {
        uniqueName = uname;
        key = pubkey;
        lastSeenTimestamp = lseen;
        state = s;
        updateStateString();
        
        uniqueName = Mixed.makeFilename(uniqueName);
    }
    
    /**
     * @return  the public key of this Identity
     */
    public String getKey() {
        return key;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    // dont't store BoardAttachement with pubKey=SSK@...
    public static boolean isForbiddenBoardAttachment(BoardAttachment ba) {
        if( ba != null &&
            ba.getBoardObj().getPublicKey() != null &&
            ba.getBoardObj().getPublicKey().startsWith("SSK@") )
        {
            return true; // let delete SKK pubKey board
        } else {
            return false;
        }
    }

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void updateLastSeenTimestamp(long v) {
        lastSeenTimestamp = v;
        updateIdentitiesDatabaseTable();
    }

    public int getState() {
        return state;
    }

    public String toString() {
        return getUniqueName();
    }
    
    public boolean isGOOD() {
        return state==GOOD;
    }
    public boolean isCHECK() {
        return state==CHECK;
    }
    public boolean isOBSERVE() {
        return state==OBSERVE;
    }
    public boolean isBAD() {
        return state==BAD;
    }
    
    public void setGOOD() {
        state=GOOD;
        updateStateString();
        updateIdentitiesDatabaseTable();
    }
    public void setCHECK() {
        state=CHECK;
        updateStateString();
        updateIdentitiesDatabaseTable();
    }
    public void setOBSERVE() {
        state=OBSERVE;
        updateStateString();
        updateIdentitiesDatabaseTable();
    }
    public void setBAD() {
        state=BAD;
        updateStateString();
        updateIdentitiesDatabaseTable();
    }

    public void setGOODWithoutUpdate() {
        state=GOOD;
        updateStateString();
    }
    public void setCHECKWithoutUpdate() {
        state=CHECK;
        updateStateString();
    }
    public void setOBSERVEWithoutUpdate() {
        state=OBSERVE;
        updateStateString();
    }
    public void setBADWithoutUpdate() {
        state=BAD;
        updateStateString();
    }

    private void updateStateString() {
        if( isCHECK() ) {
            stateString = CHECK_STRING;
        } else if( isOBSERVE() ) {
            stateString = OBSERVE_STRING;
        } else if( isGOOD() ) {
            stateString = GOOD_STRING;
        } else if( isBAD() ) {
            stateString = BAD_STRING;
        } else {
            stateString = "*ERR*";
        }
    }
    
    public String getStateString() {
        return stateString;
    }
    
    protected boolean updateIdentitiesDatabaseTable() {
        if( FrostIdentities.isDatabaseUpdatesAllowed() == false ) {
            return false;
        }
        if( this instanceof LocalIdentity ) {
            return false; // nothing to save here
        }
        try {
            AppLayerDatabase.getIdentitiesDatabaseTable().updateIdentity(this);
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error updating an identity", ex);
        }
        return false;
    }
}
