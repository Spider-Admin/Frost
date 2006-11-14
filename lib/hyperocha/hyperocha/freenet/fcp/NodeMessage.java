/**
 * 
 */
package hyperocha.freenet.fcp;

import java.util.Hashtable;

/**
 * 
 * helper class for ease the access to the node messages
 * caution: this class dosn't contain the data after DATA
 * @author saces
 * @version $Id$
 *
 */
public class NodeMessage {
	
	private String messageName;
	private Hashtable items;
	private String messageEndMarker;

	/**
	 * Creates a new Message
	 */
	public NodeMessage(String name) {
		messageName = name;
		items = new Hashtable();
	}

	/** 
	 * returns the message as string for debug/log output
	 */
	public String toString() {
		return messageName + " " + items + " " + messageEndMarker;
	}
	
	protected void setItem(String name, String value) {
		items.put(name, value);
	}
	
	protected void setEnd(String em) {
		messageEndMarker = em;
	}
	
	protected String getMessageName() {
		return messageName;
	}
	
	public boolean isMessageName(String aName) {
		if (aName == null) {
			return false;
		}
		return aName.equalsIgnoreCase(messageName);
	}
	
	public String getStringValue(String name) {
		return (String)items.get(name);
	}

	public long getLongValue(String name) {
		return Long.parseLong((String)(items.get(name)));
	}
	
	public long getLongValue(String name, int radix) {
		return Long.parseLong((String)(items.get(name)), radix);
	}
	
	public FreenetKey getKeyValue(String name) {
		return FreenetKey.getKeyFromString((String)items.get(name));
	}
	
	public boolean getBoolValue(String name) {
		return "true".equalsIgnoreCase((String)items.get(name));
	}
	
	public void addItem(String key, String value) {
		items.put(key, value); 
	}

	
	
}
