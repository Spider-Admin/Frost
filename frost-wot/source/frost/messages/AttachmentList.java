/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import java.util.*;
import java.util.logging.Logger;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AttachmentList extends LinkedList implements XMLizable {
	
	private static Logger logger = Logger.getLogger(AttachmentList.class.getName());
	
	/**
	 * traverses the list for attachment of given type
	 * @param type the type - BOARD, FILE, PERSON...
	 * @return another list which contains only attachments of the specified type
	 */
	public AttachmentList getAllOfType(int type) {
		assert type == Attachment.FILE || 
					type == Attachment.BOARD ||
					type == Attachment.PERSON :
					"list of unknown type of attachments requested";
		
		
		Iterator i = iterator();
		AttachmentList result = new AttachmentList();
		while (i.hasNext()) {
			Attachment current = (Attachment)i.next();
			if (current.getType() == type)
				result.add(current);
		}
		
		return result;
	}
    
	synchronized public Element getXMLElement(Document d){
        if( size() == 0 )
        {
            return null;
        }
		Element el = d.createElement("AttachmentList");
		Iterator i = iterator();
		while (i.hasNext()) {
			Attachment current = (Attachment)i.next();
			el.appendChild(current.getXMLElement(d));
		}
		return el;
	}
	
	public void loadXMLElement(Element el) throws SAXException {
        if( el == null )
            return;
		Iterator i = XMLTools.getChildElementsByTagName(el,"Attachment").iterator();
		while (i.hasNext()){
			Element current = (Element)i.next();
			Attachment attachment = Attachment.getInstance(current);
			add(attachment);
		}
		
		if (size()==0)
			logger.info("empty attachment list upon creation");
	}
}
