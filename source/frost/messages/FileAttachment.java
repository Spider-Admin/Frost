/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import java.io.File;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.XMLTools;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileAttachment extends Attachment {

	private SharedFileObject fileObj;

	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		return Attachment.FILE;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		Element el = container.createElement("Attachment");
		el.setAttribute("type", "file");
		el.appendChild(fileObj.getXMLElement(container));

		//if file is ofline and it has a File obj associated with it 
		//(i.e. if it is a file we're sharing),
		//add a <path> element to this element

		if (!fileObj.isOnline() && fileObj.getFile() != null) {
			CDATASection cdata = container.createCDATASection(fileObj.getFile().getPath());
			Element path = container.createElement("path");
			path.appendChild(cdata);
			el.appendChild(path);
		}
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _file = (Element) XMLTools.getChildElementsByTagName(e, "File").iterator().next();
		fileObj = SharedFileObject.getInstance(_file);

		assert fileObj != null;

		if (XMLTools.getChildElementsByTagName(e, "path").size() > 0)
			fileObj.setFile(new File(XMLTools.getChildElementsCDATAValue(e, "path")));

	}

	/**
	 * @param e
	 * @throws SAXException
	 */
	public FileAttachment(Element e) throws SAXException {
		loadXMLElement(e);
	}

	/**
	 * @param o
	 */
	public FileAttachment(SharedFileObject o) {
		fileObj = o;
	}
	/**
	 * @return the SharedFileObject this class wraps
	 */
	public SharedFileObject getFileObj() {
		return fileObj;
	}

	/* 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		String myName = fileObj.getFilename();
		String otherName = ((FileAttachment) o).getFileObj().getFilename();
		return myName.compareTo(otherName);
	}

}
