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

/**
 * @author  saces
 */
public class FreenetKey {
	/*
	 * CHK
	The first part, SVbD9~HM5nzf3AX4yFCBc-A4dhNUF5DPJZLL5NX5Brs, is the actual hash of the file. 
	 The second part, bA7qLNJR7IXRKn6uS5PAySjIM6azPFvK~18kSi6bbNQ, is the decryption key that unlocks the file (which is stored encrypted). 
	 The third part, AAEA--8, is something to do with settings such as cryptographical algorithms used.

*/
	private FreenetKeyType keyType;
	private String pubKey;  
	private String privKey;
	private String cryptoKey;  
	private String suffiX;    

	 /*
	 *
	 *
	 *
	 *
	 */
	/**
	 * 
	 */
	public FreenetKey() {

	}
	
	public FreenetKey(FreenetKeyType keytype, String pubkey, String privkey, String cryptokey, String suffix) {
		keyType = keytype;
		pubKey = pubkey;  
		privKey = privkey;
		cryptoKey = cryptokey;  
		suffiX = suffix;  
	}
	
	public boolean isFreenetKeyType(FreenetKeyType keytype) {
		return keyType.equals(keytype);
	}
	
	public String getReadKey() {
		return "" + keyType + "@" + pubKey + "," + cryptoKey + "," + suffiX + "/";
	}
	
	public String getWriteKey() {
		return "" + keyType + "@" + privKey + "," + cryptoKey + "/";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[INSERT]freenet:" + getWriteKey() + "[REQUEST]freenet:" + getReadKey();
	}
	
	
	public static FreenetKey getFreenetKeyfromString(String s) {
		return null;
	}
	
	public static String FreenetKeyStringNiceness(String s) {
		return s;
	}

}
