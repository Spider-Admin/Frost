/**
 *   This file is part of JHyperochaFCPLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaFCPLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaFCPLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
	
	public long getIntValue(String name) {
		return Integer.parseInt((String)(items.get(name)));
	}
	
	public long getLongValue(String name, int radix) {
		return Long.parseLong((String)(items.get(name)), radix);
	}
	
	public long getIntValue(String name, int radix) {
		return Integer.parseInt((String)(items.get(name)), radix);
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
