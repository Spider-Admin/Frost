/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import frost.gui.objects.*;
import frost.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BoardAttachment implements Attachment {

	FrostBoardObject boardObj;
	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		return Attachment.BOARD;
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
		
		Element el = container.createElement("Attachment");
		el.setAttribute("type","board");
		
		Element name = container.createElement("Name");
		CDATASection cdata = container.createCDATASection(
					mixed.makeSafeXML(boardObj.getBoardName()));
		name.appendChild(cdata);
		el.appendChild(name);
		
		Element pubkey = container.createElement("pubKey");
		cdata = container.createCDATASection(
			mixed.makeSafeXML(boardObj.getPublicKey()));
		pubkey.appendChild(cdata);
		el.appendChild(pubkey);
		
		Element privkey = container.createElement("privKey");
		cdata = container.createCDATASection(boardObj.getPrivateKey()); //null is ok
		privkey.appendChild(cdata);
		el.appendChild(privkey);
		
		return el;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		String name,privkey,pubkey;
		name = XMLTools.getChildElementsCDATAValue(e,"Name");
		privkey = XMLTools.getChildElementsCDATAValue(e,"privKey");
		pubkey = XMLTools.getChildElementsCDATAValue(e,"pubKey");
		
		boardObj = new FrostBoardObject(name,pubkey,privkey);

	}
	
	public BoardAttachment(Element e) throws SAXException{
		loadXMLElement(e);
	}
	
	public BoardAttachment(FrostBoardObject obj) {
		boardObj=obj;
	}


	/**
	 * @return a FrostBoardObject
	 */
	public FrostBoardObject getBoardObj() {
		return boardObj;
	}

}
