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
 * 
 * all static functions expect an untainted key-string 
 * @author  saces
 */
public class FreenetKey {
	private int networkType; 
	private FreenetKeyType keyType;
	private String pubKey;  
	private String privKey;
	private String cryptoKey;  
	private String suffiX; 
	private String fileName;
	private long revision;  // the latest one.

	/**
	 * 
	 */
	private FreenetKey() {

	}
	
	public String getPrivatePart() {
		return privKey;
	}
	
	public FreenetKey(int networktype, FreenetKeyType keytype, String pubkey, String privkey, String cryptokey, String suffix) {
		networkType = networktype;
		keyType = keytype;
		pubKey = pubkey;  
		privKey = privkey;
		cryptoKey = cryptokey;  
		suffiX = suffix;  
	}
	
	public boolean isFreenetKeyType(FreenetKeyType keytype) {
		return keyType.equals(keytype);
	}
	
	public String getReadFreenetKey() {
		if (keyType.equals(FreenetKeyType.KSK)) {
			return "" + keyType + fileName;
		}
		String s = keyType + pubKey + "," + cryptoKey + "," + suffiX + "/";
		if (fileName != null) {
			s = s + fileName;
		}
		//return "" + keyType + pubKey + "," + cryptoKey + "," + suffiX + "/";
		return s;
	}
	
	public String getWriteFreenetKey() {
		return "" + keyType + privKey + "," + cryptoKey + "/";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[INSERT]freenet:" + getWriteFreenetKey() + "[REQUEST]freenet:" + getReadFreenetKey();
	}

	
	public static String FreenetKeyStringNiceness(String s) {
		String tmpS;
		if (s.startsWith("freenet:")) {
			tmpS = s.substring(8);
		} else {
			tmpS = s;
		}
		return tmpS;
	}

	public String getPublicPart() {
		return pubKey;
	}

	public String getCryptoPart() {
		return cryptoKey;
	}

	public String getSuffixPart() {
		return suffiX;
	}
	
	public static FreenetKey KSKfromString(String k) {
		String tmpS = FreenetKeyStringNiceness(k);

		FreenetKey newKey = new FreenetKey();
		newKey.keyType = FreenetKeyType.KSK;
		newKey.fileName = tmpS.substring(4);
		return newKey;
	}
	
	public static FreenetKey CHKfromString(String k) {
		String tmpS = FreenetKeyStringNiceness(k);
//		System.out.println(s);
//		System.out.println(s.substring(5,47));
//		System.out.println(s.substring(48,91));
//		System.out.println(s.substring(92,99));
		FreenetKey newKey = new FreenetKey(Network.FCP2, FreenetKeyType.CHK, tmpS.substring(4,47), null, tmpS.substring(48,91), tmpS.substring(92,99));

		System.out.println("Keytest" + tmpS);
		
		if (tmpS.length() > 100) {
			tmpS = tmpS.substring(100);
			newKey.fileName = tmpS;
		}
		System.out.println("Keytest" + tmpS);
//		FreenetKey newKey = new FreenetKey();
//		newKey.keyType = FreenetKeyType.KSK;
//		newKey.pubKey = s.substring(4);
		return newKey;
	}
	
	public FreenetKey getKeyFromString(String aKey) {
		return null;
	}

	public static boolean isValidKey(String aKey) {
		
        if( aKey == null || aKey.length() < 5 ) { // at least KSK@x
            return false;
        }

        // check type
//        int keytype = -1;
        for( int i = 0; i < FreenetKeyType.getFreenetKeyTypes().length; i++ ) {
            if( aKey.startsWith(FreenetKeyType.getFreenetKeyTypes()[i]) ) {
//            	 FIXME!!!
                // keytype found
            	return true;
//                if( i == 0 ) { // FIXME
//                    return true; // KSK key is ok, length must be at least 5
//                }
//                keytype = i;
//                break;
            }
        }
		return false;
	}
	
	public static void main(String[] args) {
		/*
		 * CHK
		The first part, SVbD9~HM5nzf3AX4yFCBc-A4dhNUF5DPJZLL5NX5Brs, is the actual hash of the file. 
		 The second part, bA7qLNJR7IXRKn6uS5PAySjIM6azPFvK~18kSi6bbNQ, is the decryption key that unlocks the file (which is stored encrypted). 
		 The third part, AAEA--8, is something to do with settings such as cryptographical algorithms used.

	*/

		System.out.println("KeyTest start.");
		String testkey1 = "CHK@SVbD9~HM5nzf3AX4yFCBc-A4dhNUF5DPJZLL5NX5Brs,bA7qLNJR7IXRKn6uS5PAySjIM6azPFvK~18kSi6bbNQ,AAEA--8/";
		FreenetKey key1 = CHKfromString(testkey1);
	}

}
