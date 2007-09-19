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

import java.util.logging.*;

import org.garret.perst.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.messages.*;
import frost.storage.perst.identities.*;
import frost.util.*;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity extends Persistent implements XMLizable {
    
    private transient String publicKey;
    
    private PerstIdentityPublicKey pPublicKey;
    
    @Override
    public boolean recursiveLoading() {
        return false;
    }
    
    @Override
    public void deallocate() {
        if( pPublicKey != null ) {
            pPublicKey.deallocate();
            pPublicKey = null;
        }
        super.deallocate();
    }

    class PerstIdentityPublicKey extends Persistent {
        private String perstPublicKey;
        public PerstIdentityPublicKey() {}
        public PerstIdentityPublicKey(String pk) {
            perstPublicKey = pk;
        }
        public String getPublicKey() {
            return perstPublicKey;
        }
        public void onLoad() {
            System.out.println("load pubkey");
        }
        @Override
        public boolean recursiveLoading() {
            return false; // load publicKey on demand
        }
    }
    
    public void onStore() {
        if( pPublicKey == null && publicKey != null ) {
            // link public key
            pPublicKey = new PerstIdentityPublicKey(publicKey);
        }
    }
    
    private static transient final int GOOD    = 1;
    private static transient final int CHECK   = 2;
    private static transient final int OBSERVE = 3;
    private static transient final int BAD     = 4;
    
    private static transient final String GOOD_STRING    = "GOOD";
    private static transient final String CHECK_STRING   = "CHECK";
    private static transient final String OBSERVE_STRING = "OBSERVE";
    private static transient final String BAD_STRING     = "BAD";

    private String uniqueName;
    private long lastSeenTimestamp = -1;

    private int state = 2;
    private transient String stateString = CHECK_STRING; 

    private static transient final Logger logger = Logger.getLogger(Identity.class.getName());
    
    public Identity() {}

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
        cdata = doc.createCDATASection(getPublicKey());
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
        cdata = doc.createCDATASection(getPublicKey());
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
        publicKey =  XMLTools.getChildElementsCDATAValue(e, "key");

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
        this.publicKey = key;
        if( name.indexOf("@") != -1 ) {
            this.uniqueName = name;
        } else {
            this.uniqueName = name + "@" + Core.getCrypto().digest(getPublicKey());
        }
        
        uniqueName = Mixed.makeFilename(uniqueName);
    }
    

    public Identity(String uname, String pubkey, long lseen, int s) {
        uniqueName = uname;
        publicKey = pubkey;
        lastSeenTimestamp = lseen;
        state = s;
        updateStateString();
        
        uniqueName = Mixed.makeFilename(uniqueName);
    }
    
    /**
     * @return  the public key of this Identity
     */
    public String getPublicKey() {
        if( publicKey == null && pPublicKey != null ) {
            pPublicKey.load();
            return pPublicKey.getPublicKey();
        }
        return publicKey;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    // dont't store BoardAttachment with pubKey=SSK@...
    public static boolean isForbiddenBoardAttachment(BoardAttachment ba) {
        if( ba != null &&
            ba.getBoardObj().getPublicKey() != null &&
            ba.getBoardObj().getPublicKey().startsWith("SSK@") )
        {
            return true; // let delete SSK pubKey board
        } else {
            return false;
        }
    }

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void updateLastSeenTimestamp(long v) {
        lastSeenTimestamp = v;
        updateIdentitiesStorage();
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
        updateIdentitiesStorage();
    }
    public void setCHECK() {
        state=CHECK;
        updateStateString();
        updateIdentitiesStorage();
    }
    public void setOBSERVE() {
        state=OBSERVE;
        updateStateString();
        updateIdentitiesStorage();
    }
    public void setBAD() {
        state=BAD;
        updateStateString();
        updateIdentitiesStorage();
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
    
    protected boolean updateIdentitiesStorage() {
        if( FrostIdentities.isDatabaseUpdatesAllowed() == false ) {
            return false;
        }
        if( getStorage() == null ) {
            return false;
        }
        modify();
        IdentitiesStorage.inst().commit();
        return true;
    }
}
