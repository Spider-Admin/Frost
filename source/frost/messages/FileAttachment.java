/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import frost.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileAttachment implements Attachment {

	SharedFileObject fileObj;
	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		return Attachment.FILE;
	}

	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getMessage()
	 */
	public MessageObject getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		return fileObj.getXMLElement(container);
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		fileObj = new SharedFileObject();
		fileObj.loadXMLElement(e);

	}
	
	public FileAttachment(Element e) throws SAXException{
		loadXMLElement(e);
	}
	
	public FileAttachment(SharedFileObject o) {
		fileObj =o;
	}
	/**
	 * @return the SharedFileObject this class wraps
	 */
	public SharedFileObject getFileObj() {
		return fileObj;
	}

}
