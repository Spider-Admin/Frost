/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import frost.XMLizable;
import frost.Core;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Attachment implements XMLizable {
	public static final int FILE=0;
	public static final int BOARD=1;
	public static final int PERSON=2;
	
	
	/**
	 * 
	 * @return the type of this attachment
	 */
	public abstract int getType();
	
	/**
	 * 
	 * @return the message this attachment came from
	 */
	public abstract MessageObject getMessage();
	
	public static Attachment getInstance(Element e){
		
		assert e.getAttribute("type").length()>0 :
			"attachment type not specified!";	
		try{
			if (e.getAttribute("type").equals("file")) 		
				return new FileAttachment(e);
			else if (e.getAttribute("type").equals("board"))
				return new BoardAttachment(e);
			else 
				return new PersonAttachment(e);
		}
		catch(SAXException ex){
			ex.printStackTrace(Core.getOut());
			return null;
		}
	}

}
