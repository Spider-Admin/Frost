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

import org.garret.perst.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import frost.Core;
import frost.messaging.frost.BoardAttachment;
import frost.storage.perst.identities.IdentitiesStorage;
import frost.util.Mixed;
import frost.util.XMLTools;
import frost.util.XMLizable;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity extends Persistent implements XMLizable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(Identity.class);

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
    private int receivedMessageCount = 0;
    private String comment = "";

    private int state = CHECK;
    private transient String stateString = CHECK_STRING;

    private transient String publicKey;

    private PerstIdentityPublicKey pPublicKey;

	public Identity() {
	}

	protected Identity(final Element el) throws Exception {
        try {
            loadXMLElement(el);
        } catch (final SAXException e) {
            logger.error("Exception thrown in constructor", e);
        }
    }

    /**
     * we use this constructor whenever we have all the info
     *
     * @param name
     * @param key
     */
    private Identity(final String name, final String key) {
        this.publicKey = key;
        this.uniqueName = name;
    }

    /**
     * If a LocalIdentity is deleted, we ceate a GOOD Identity for the deleted LocalIdentity
     *
     * @param li
     */
    public Identity(final LocalIdentity li) {
        uniqueName = li.getUniqueName();
        publicKey = li.getPublicKey();
        lastSeenTimestamp = li.getLastSeenTimestamp();
        receivedMessageCount = li.getReceivedMessageCount();
        comment = li.getComment();
        updateStateString();
    }

    /**
     * Create a new Identity from the specified uniqueName and publicKey.
     * If uniqueName does not contain an '@', this method creates a new digest
     * for the publicKey and appens it to the uniqueName.
     * Finally Mixed.makeFilename() is called for the uniqueName.
     *
     * @param name
     * @param key
     * @param createNew
     */
    protected Identity(String name, final String key, final boolean createNew) {
        if( name.indexOf("@") < 0 ) {
            name = name + "@" + Core.getCrypto().digest(key);
        }
        name = Mixed.makeFilename(name);

        this.publicKey = key;
        this.uniqueName = name;
    }

    ////////////////////////////////////////////////////////////////////
    // FACTORY METHODS /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * Create a new Identity from the specified uniqueName and publicKey.
     * Does not convert the specified uniqueName using Mixed.makeFilename() !
     *
     * @return null if the Identity cannot be created
     */
    public static Identity createIdentityFromExactStrings(final String name, final String key) {
        return new Identity(name, key);
    }

    /**
     * Create a new Identity, read from the specified XML element.
     * Calls Mixed.makeFilename() on read uniqueName.
     *
     * @param el  the XML element containing the Identity information
     * @return    the new Identity, or null if Identity cannot be created (invalid input)
     */
    public static Identity createIdentityFromXmlElement(final Element el) {
        try {
            return new Identity(el);
        } catch (final Exception e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////

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

		private static final long serialVersionUID = 1L;

        private String perstPublicKey;
        public PerstIdentityPublicKey() {}
        public PerstIdentityPublicKey(final String pk) {
            perstPublicKey = pk;
        }
        public String getPublicKey() {
            return perstPublicKey;
        }

		public void onLoad() {
			logger.debug("load pubkey");
		}

        @Override
        public boolean recursiveLoading() {
            return false; // load publicKey on demand
        }
    }

    @Override
    public void onStore() {
        if( pPublicKey == null && publicKey != null ) {
            // link public key
            pPublicKey = new PerstIdentityPublicKey(publicKey);
        }
    }

    @Override
    public void onLoad() {
        updateStateString();
    }

    public Element getXMLElement(final Document doc)  {

        final Element el = doc.createElement("Identity");

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

    @Deprecated
    public Element getXMLElement_old(final Document doc)  {

        final Element el = doc.createElement("MyIdentity");

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

    public Element getExportXMLElement(final Document doc)  {
        final Element el = getXMLElement(doc);

        if( getLastSeenTimestamp() > -1 ) {
            final Element element = doc.createElement("lastSeen");
            final Text txt = doc.createTextNode(Long.toString(getLastSeenTimestamp()));
            element.appendChild( txt );
            el.appendChild( element );
        }
        if( getReceivedMessageCount() > -1 ) {
            final Element element = doc.createElement("messageCount");
            final Text txt = doc.createTextNode(Long.toString(getReceivedMessageCount()));
            element.appendChild( txt );
            el.appendChild( element );
        }
        return el;
    }

    public void loadXMLElement(final Element e) throws SAXException {
        uniqueName = XMLTools.getChildElementsCDATAValue(e, "name");
        publicKey =  XMLTools.getChildElementsCDATAValue(e, "key");

        String lastSeenStr = XMLTools.getChildElementsTextValue(e,"lastSeen");
        if( lastSeenStr != null && ((lastSeenStr=lastSeenStr.trim())).length() > 0 ) {
            lastSeenTimestamp = Long.parseLong(lastSeenStr);
        } else {
            // not yet set, init with current timestamp
            lastSeenTimestamp = System.currentTimeMillis();
        }

        String msgCount = XMLTools.getChildElementsTextValue(e,"messageCount");
        if( msgCount != null && ((msgCount=msgCount.trim())).length() > 0 ) {
            receivedMessageCount = Integer.parseInt(msgCount);
        } else {
            receivedMessageCount = 0;
        }
//        uniqueName = Mixed.makeFilename(uniqueName);
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

    public void correctUniqueName() {
        uniqueName = Mixed.makeFilename(uniqueName);
    }

    // dont't store BoardAttachment with pubKey=SSK@...
    public static boolean isForbiddenBoardAttachment(final BoardAttachment ba) {
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

    public void setLastSeenTimestamp(final long v) {
        lastSeenTimestamp = v;
        updateIdentitiesStorage();
    }

    public void setLastSeenTimestampWithoutUpdate(final long v) {
        lastSeenTimestamp = v;
    }

    public int getState() {
        return state;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        updateIdentitiesStorage();
    }

    @Override
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
        if( !IdentitiesStorage.inst().beginExclusiveThreadTransaction() ) {
            return false;
        }
        modify();
        IdentitiesStorage.inst().endThreadTransaction();
        return true;
    }

    public int getReceivedMessageCount() {
        return receivedMessageCount;
    }

    public void setReceivedMessageCount(final int i) {
        receivedMessageCount = i;
    }

    public void incReceivedMessageCount() {
        this.receivedMessageCount++;
        updateIdentitiesStorage();
    }
}
