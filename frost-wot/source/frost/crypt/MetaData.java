/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.crypt;

import frost.*;
import frost.identities.*;
import frost.XMLizable;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;



/**
 * @author zlatinb
 *
 * This file represents MetaData that's of a file in Freenet.
 * It has the followint format:
 * <FrostMetaData>
 * <MyIdentity>
 * 	<name> unique name of sharer</name>
 * 	<key> public key of sharer </key>
 * </MyIdentity>
 * <sig> signature of file </sig>
 * </FrostMetaData>
 */
public class MetaData implements XMLizable {

	Identity sharer;
	byte [] plaintext,sig;
	boolean signed; //true = signed, false = encrypted
	
	public MetaData(){
			sharer =null;
			plaintext=null;
			sig=null;
	}
	
	/**
	 * represents a metadata of something about to be sent
	 * @param plaintext
	 */
	public MetaData(byte[] plaintext) {
		this.sharer = Core.getMyId();
		this.plaintext = plaintext;
		sig = Core.getCrypto().detachedSign(plaintext, Core.getMyId().getPrivKey());
	}
	
	public MetaData(byte [] plaintext, byte [] metadata) throws Throwable
    {
		File tmp = new File("metadataTemp"+ System.currentTimeMillis());
		FileAccess.writeByteArray(metadata,tmp);
		Document d = XMLTools.parseXmlFile(tmp,false);
		Element el = d.getDocumentElement();
		this.plaintext = plaintext;
		try {
			loadXMLElement(el);
		}catch (SAXException e){
			e.printStackTrace(Core.getOut());
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
	public MetaData(byte [] plaintext, Element el) throws Throwable
    {
		this.plaintext = plaintext;
		try {
			loadXMLElement(el);
		}catch (SAXException e){
			e.printStackTrace(Core.getOut());
			plaintext = null;
            throw e;
		}
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		Element el = container.createElement("FrostMetaData");
		
		//make sure we don't add sensitive fields in the metadata
		Element _sharer = sharer.getXMLElement(container);
		List privElements = XMLTools.getChildElementsByTagName(_sharer,"privKey");
		privElements.addAll(XMLTools.getChildElementsByTagName(_sharer,"files"));
		privElements.addAll(XMLTools.getChildElementsByTagName(_sharer,"messages"));
		privElements.addAll(XMLTools.getChildElementsByTagName(_sharer,"CHK"));
		privElements.addAll(XMLTools.getChildElementsByTagName(_sharer,"trustedIds"));
		
		Iterator it = privElements.iterator();
		while (it.hasNext())
			_sharer.removeChild((Element)it.next());
		
		el.appendChild(_sharer);
		
		Element _sig = container.createElement("sig");
		CDATASection cdata = container.createCDATASection(new String(sig));
		_sig.appendChild(cdata);
		
		el.appendChild(_sig);
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _sharer = (Element) XMLTools.getChildElementsByTagName(e,"MyIdentity").iterator().next();
		sharer = new Identity(_sharer);
		sig = XMLTools.getChildElementsCDATAValue(e,"sig").getBytes();
	}

	/**
	 * @return
	 */
	public Identity getSharer() {
		return sharer;
	}

	/**
	 * @return
	 */
	public byte[] getSig() {
		return sig;
	}

}
