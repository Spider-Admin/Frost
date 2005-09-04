/*
  SignMetaData.java / Frost
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
package frost.crypt;

import java.io.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.identities.*;

/**
 * @author zlatinb
 *
 * This file represents MetaData that's of a file in Freenet.
 * It has the following format:
 * <FrostMetaData>
 *  <MyIdentity>
 * 	 <name> unique name of person</name>
 * 	 <key> public key of person </key>
 *  </MyIdentity>
 *  <sig> signature of file </sig>
 * </FrostMetaData>
 */
public class SignMetaData extends MetaData {

	private static Logger logger = Logger.getLogger(SignMetaData.class.getName());
	
	byte [] plaintext;
	String sig;
//	boolean signed; // true = signed, false = encrypted
	
	public SignMetaData() {
		person = null;
		plaintext = null;
		sig = null;
	}
	
	/**
	 * Represents a metadata of something about to be sent.
	 * @param plaintext
	 */
	public SignMetaData(byte[] plaintext, LocalIdentity myId) {
		this.person = myId;
		this.plaintext = plaintext;
		sig = Core.getCrypto().detachedSign(plaintext, myId.getPrivKey());
	}
    
    
	/**
     * Metadata of something that was received. 
	 */
	public SignMetaData(byte [] plaintext, byte [] metadata) throws Throwable {
        
		File tmp = new File("metadataTemp"+ System.currentTimeMillis());
		FileAccess.writeFile(metadata,tmp);
		Document d = XMLTools.parseXmlFile(tmp,false);
		Element el = d.getDocumentElement();
        if( el.getTagName().equals("FrostMetaData") == false ) {
            tmp.delete();
            throw new Exception("This is not FrostMetaData XML file.");
        }
		this.plaintext = plaintext;
		try {
			loadXMLElement(el);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
            tmp.delete();
			plaintext = null;
            throw e;
		}
		tmp.delete();
	}
	
	/**
	 * represents something that was received and needs to be verified
	 * @param plaintext the plaintext to be verified
	 * @param el the xml element to populate from
	 */
	public SignMetaData(byte [] plaintext, Element el) throws SAXException {
		this.plaintext = plaintext;
		try {
			loadXMLElement(el);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
			plaintext = null;
            throw e;
		}
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		Element el = super.getXMLElement(container);
		
		Element _sig = container.createElement("sig");
		CDATASection cdata = container.createCDATASection(sig);
		_sig.appendChild(cdata);
		
		el.appendChild(_sig);
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _person = (Element) XMLTools.getChildElementsByTagName(e,"MyIdentity").iterator().next();
		person = new Identity(_person);
		sig = XMLTools.getChildElementsCDATAValue(e,"sig");
		
		assert person!=null && sig!=null;
	}


	/**
	 * @return
	 */
	public String getSig() {
		return sig;
	}

    /**
     * Creates the metadata xml file and returns the byte[] containing the utf-16 text.
     * @param targetFilename
     * @return
     */   
     
    /*public byte[] getRawXmlContent()
    {
        // create a XML file
        Document doc = XMLTools.createDomDocument();
        if( doc == null )
        {
            Core.getOut().println("Factory did not create a XML document.");
            return null; 
        }
        Element rootElement = doc.createElement("FrostMetaData");
        Element childs = getXMLElement(doc);
        rootElement.appendChild( childs );
        // write to a dummy file
        File tempFile = null;
        try
        {
            tempFile = File.createTempFile("metadataraw_", ".tmp", new File(frame1.frostSettings.getValue("temp.dir")));
        }
        catch( IOException ex )
        {
            tempFile = new File("metadataxmltmp-"+System.currentTimeMillis());
        }
        
        boolean writeOK;
        try {
            writeOK = XMLTools.writeXmlFile(doc, tempFile.getPath());
        } catch(Throwable ex) { writeOK = false; }
            
        if( writeOK )
        {
            byte[] result = FileAccess.readByteArray(tempFile);
            tempFile.delete();
            return result;
        }
        tempFile.delete();
        return null;
    }
*/
	/* (non-Javadoc)
	 * @see frost.crypt.MetaData#getType()
	 */
	public int getType() {
		return MetaData.SIGN;
	}
}
