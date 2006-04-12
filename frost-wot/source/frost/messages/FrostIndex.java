/*
  FrostIndex.java / Frost
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
import org.xml.sax.SAXException;

import frost.*;
import frost.identities.Identity;

/**
 * Represents an index file in Freenet.
 */
public class FrostIndex implements XMLizable {

    Identity sharer;
    TreeMap filesMap;
    String signature = null;

    private static Logger logger = Logger.getLogger(FrostIndex.class.getName());

    public FrostIndex(Element e) {
        filesMap = new TreeMap();
        
        try {
            loadXMLElement(e);
        } catch (SAXException ex){
            logger.log(Level.SEVERE, "Exception thrown in constructor", ex);
        }
    }

    public FrostIndex(Map files) {
        filesMap = new TreeMap(files);

        if (Core.frostSettings.getBoolValue("signUploads")) {
            sharer = Core.getInstance().getIdentities().getMyId();
        } else {
            sharer = null;
        }
    }
    
    public String getSignature() {
        return signature;
    }

    public void setSignature(String sig) {
        if( sig != null && sig.length() == 0 ) {
            signature = null;
        } else {
            signature = sig;
        }
    }

    public void signFiles() {
        String signContent = getSignableContent();
        String sig = Core.getCrypto().detachedSign(signContent, Core.getIdentities().getMyId().getPrivKey());
        setSignature(sig);
    }
    
    public boolean verifySignature(Identity owner) {
        String signContent = getSignableContent();
        boolean sigIsValid = Core.getCrypto().detachedVerify(signContent, owner.getKey(), getSignature());
        return sigIsValid;
    }
    
    protected String getSignableContent() {
        StringBuffer signContent = new StringBuffer();
        for(Iterator i = getFilesIterator(); i.hasNext(); ) {
            SharedFileObject sfo = (SharedFileObject)i.next();
            signContent.append( sfo.getSHA1() );
            signContent.append( sfo.getFilename() );
            signContent.append( sfo.getSize().toString() );
            if( sfo.getKey() != null ) {
                signContent.append( sfo.getKey() );
            }
        }
        return signContent.toString();
    }

    /* (non-Javadoc)
     * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
     */
    public Element getXMLElement(Document container) {

        Element el = container.createElement("FrostIndex");

        boolean signUploads = Core.frostSettings.getBoolValue("signUploads");

        //if user signs uploads, remove the sensitive fields and append element
        if (signUploads && sharer != null) {
            Element _sharer = sharer.getSafeXMLElement(container);
            el.appendChild(_sharer);

            if( getSignature() != null ) {
                Element element = container.createElement("Signature");
                CDATASection cdata = container.createCDATASection(getSignature());
                element.appendChild(cdata);
                el.appendChild(element);
            }
        }

        // iterate through set of files and add them all
        for(Iterator i = getFilesIterator(); i.hasNext(); ) {
            SharedFileObject current = (SharedFileObject)i.next();
            Element currentElement = current.getXMLElement(container);

            //remove sensitive information
            List sensitive = XMLTools.getChildElementsByTagName(currentElement,"dateShared");

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
            _sharer = XMLTools.getChildElementsByTagName(e,"Identity"); // other format
            if (_sharer.size() > 0) {
                sharer = new Identity((Element)_sharer.get(0));
            } else {
                sharer = null;
            }
        }
        
        setSignature( XMLTools.getChildElementsCDATAValue(e, "Signature") );

        List _files = XMLTools.getChildElementsByTagName(e,"File");

        Iterator it = _files.iterator();
        while (it.hasNext()) {
            Element el = (Element)it.next();
            SharedFileObject file = SharedFileObject.getInstance(el);
            if (file.getSHA1() != null) {
                filesMap.put(file.getSHA1(), file);
            }
        }
    }

    /**
     * @return the person sharing the index
     */
    public Identity getSharer() {
        return sharer;
    }
    
    public SharedFileObject getFileBySHA1(String sha1) {
        return (SharedFileObject)filesMap.get(sha1);
    }

    public void removeFileBySHA1(String sha1) {
        filesMap.remove(sha1);
    }

    public void addFile(SharedFileObject file) {
        filesMap.put(file.getSHA1(), file);
    }
    
    public Iterator getFilesIterator() {
        return filesMap.values().iterator();
    }

    public Map getFilesMap() {
        return filesMap;
    }
}
