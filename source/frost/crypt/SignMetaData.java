/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.crypt;

import java.io.File;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.identities.Identity;



/**
 * @author zlatinb
 *
 * This file represents MetaData that's of a file in Freenet.
 * It has the followint format:
 * <FrostMetaData>
 * <MyIdentity>
 * 	<name> unique name of person</name>
 * 	<key> public key of person </key>
 * </MyIdentity>
 * <sig> signature of file </sig>
 * </FrostMetaData>
 */
public class SignMetaData extends MetaData {

	private static Logger logger = Logger.getLogger(SignMetaData.class.getName());
	
	byte [] plaintext;
	String sig;
	boolean signed; //true = signed, false = encrypted
	
	public SignMetaData(){
			person =null;
			plaintext=null;
			sig=null;
	}
	
	/**
	 * represents a metadata of something about to be sent
	 * @param plaintext
	 */
	public SignMetaData(byte[] plaintext) {
		this.person = Core.getMyId();
		this.plaintext = plaintext;
		sig = Core.getCrypto().detachedSign(plaintext, Core.getMyId().getPrivKey());
	}
	
	public SignMetaData(byte [] plaintext, byte [] metadata) throws Throwable
    {
		File tmp = new File("metadataTemp"+ System.currentTimeMillis());
		FileAccess.writeByteArray(metadata,tmp);
		Document d = XMLTools.parseXmlFile(tmp,false);
		Element el = d.getDocumentElement();
        if( el.getTagName().equals("FrostMetaData") == false )
        {
            tmp.delete();
            throw new Exception("This is not FrostMetaData XML file.");
        }
		this.plaintext = plaintext;
		try {
			loadXMLElement(el);
		}catch (SAXException e){
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
	public SignMetaData(byte [] plaintext, Element el) throws SAXException
    {
		this.plaintext = plaintext;
		try{
			loadXMLElement(el);
		}catch (SAXException e){
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
