/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.util.*;

import frost.*;
import frost.identities.*;
/**
 * @author zlatinb
 *
 * represents an index file in Freenet
 */
public class FrostIndex implements XMLizable {
	Identity sharer;
	Set files;

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document container) {
		Element el = container.createElement("FrostIndex");
		
		//if user signs uploads, remove the sensitive fields and append element
		if (Core.frostSettings.getBoolValue("signUploads")) {
			Element _sharer = sharer.getSafeXMLElement(container);
			
			el.appendChild(_sharer);
		}
		
		boolean signUploads = Core.frostSettings.getBoolValue("signUploads");
		//iterate through set of files and add them all
		Iterator i = files.iterator();
		while (i.hasNext()){
			SharedFileObject current = (SharedFileObject)i.next();
			Element currentElement = current.getXMLElement(container);
			
			//remove sensitive information
			List sensitive = XMLTools.getChildElementsByTagName(currentElement,"lastSharedDate");
			
			//strip the owner field if file is not signed
			if (!signUploads)
				sensitive.addAll(XMLTools.getChildElementsByTagName(currentElement,"owner"));
				
			Iterator i2 = sensitive.iterator();
			while (i2.hasNext())
				currentElement.removeChild((Element)i2.next());
				
			el.appendChild(currentElement);
		}
		
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		List _sharer = XMLTools.getChildElementsByTagName(e,"MyIdentity");
		if (_sharer.size() >0)
			sharer = new Identity((Element)_sharer.get(0));
		else 
			sharer = null;
			
		List _files = XMLTools.getChildElementsByTagName(e,"File");
		
		files = new HashSet(); //TODO: maybe put a TreeSet and keep all sorted
		Iterator it = _files.iterator();
		while (it.hasNext()) {
			Element el = (Element)it.next();
			SharedFileObject file = new SharedFileObject();
			file.loadXMLElement(el);
			files.add(file);
		}

	}

	public FrostIndex(Element e) {
		try {
			loadXMLElement(e);
		} catch (SAXException ex){
			ex.printStackTrace(Core.getOut());
		}
	}

	public FrostIndex(Set files) {
		this.files = files;
		if (Core.frostSettings.getBoolValue("signUploads"))
			sharer = Core.getMyId();
		else 
			sharer = null;
	}

	/**
	 * @return the set of files contained in this index
	 */
	public Set getFiles() {
		return files;
	}

	/**
	 * @return the person sharing the index
	 */
	public Identity getSharer() {
		return sharer;
	}

}
