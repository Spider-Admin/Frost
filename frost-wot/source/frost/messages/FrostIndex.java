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
		}
		
		//iterate through set of files and add them all
		Iterator i = files.iterator();
		while (i.hasNext()){
			SharedFileObject current = (SharedFileObject)i.next();
			el.appendChild(current.getXMLElement(container));
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
