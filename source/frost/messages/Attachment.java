/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import frost.XMLizable;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface Attachment extends XMLizable {
	public static final int FILE=0;
	public static final int BOARD=1;
	public static final int PERSON=2;
	
	
	/**
	 * 
	 * @return the type of this attachment
	 */
	public int getType();
	
	/**
	 * 
	 * @return the message this attachment came from
	 */
	public MessageObject getMessage();

}
