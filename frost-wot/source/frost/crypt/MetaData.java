/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.crypt;

import frost.identities.Identity;
import frost.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class MetaData implements XMLizable {
	
	public static final int SIGN=0;
	public static final int ENCRYPT=1;
	
	public abstract int getType();
	
	Identity person;
	/**
	 * @return the person (sender or recepient) of the message
	 */
	public Identity getPerson(){
		return person;
	}
	
	public Element getXMLElement(Document container){
		Element el= container.createElement("FrostMetaData");
//		make sure we don't add sensitive fields in the metadata
			  // FIXME: maybe its better to remove all but the only wanted?
			  // otherwise new fields could come into the xml file
			  Element _person = person.getSafeXMLElement(container);

			  el.appendChild(_person);
			  return el;
	}
	
	public static MetaData getInstance(byte [] body, Element e){
		assert e.getNodeName().equals("FrostMetaData") : "root tag \"FrostMetaData\" missing!";
		
		try {
			if (XMLTools.getChildElementsByTagName(e,"sig").size() > 0)
				return new SignMetaData(body, e);
			else
				return new EncryptMetaData(e);
		}catch(SAXException ex) {
			ex.printStackTrace(Core.getOut());
		}
		return null;
	}
}