/*
 * Created on Nov 17, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import java.io.File;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import frost.XMLTools;
import frost.gui.objects.FrostBoardObject;

/**
 * @author zlatinb
 *
 * represents a fileObject holding a FEC redirect
 */
public class FECRedirectFileObject extends RedirectFileObject {

	String redirect;

	/* (non-Javadoc)
	 * @see frost.messages.RedirectFileObject#getRedirectType()
	 */
	public int getRedirectType() {
		return FEC_REDIRECT;
	}

	public Element getXMLElement(Document doc) {
		// TODO Auto-generated method stub
		Element el =  super.getXMLElement(doc);
		Element el2 = doc.createElement("redirect");
		CDATASection cdata = doc.createCDATASection(redirect);
		el2.appendChild(cdata);
		el.appendChild(el2);
		el.setAttribute("redirect","FEC");
		return el;
	}

	public void loadXMLElement(Element current) throws SAXException {
		assert current.getAttribute("redirect").equals("FEC") &&
			XMLTools.getChildElementsByTagName(current,"redirect").size()>0 :
				"fec redirect not present in file";
		super.loadXMLElement(current);		
		redirect = XMLTools.getChildElementsCDATAValue(current,"redirect");
	}

	/**
	 * @return Returns the redirect.
	 */
	public String getRedirect() {
		return redirect;
	}

	/**
	 * @param redirect The redirect to set.
	 */
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	/**
	 * @param file
	 * @param board
	 */
	public FECRedirectFileObject(File file, FrostBoardObject board) {
		super(file, board);
		redirect = null;
	}
	
	

	public FECRedirectFileObject() {
		super();
		redirect = null;
	}

}
