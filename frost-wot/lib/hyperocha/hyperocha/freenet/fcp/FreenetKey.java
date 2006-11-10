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
 * Keys: sss@jdj,aab,xxx/bla/bla
 * <pre>
 *   sss@ - type
 *   jdj,aab,xxx - the key itselfs
 *   /bla/bla - filename (or intepreted as metadata) 
 * </pre>
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
	private String extra; 
	private String docName;
	private int revision = -1;  // the latest one.

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
		extra = suffix;  
	}
	
	public boolean isFreenetKeyType(FreenetKeyType keytype) {
		return keyType.equals(keytype);
	}
	
	public String getReadFreenetKey() {
		if (keyType.equals(FreenetKeyType.KSK)) {
			return "KSK@" + docName;
		}
		String s = keyType + "@" + pubKey + "," + cryptoKey + "," + extra + "/";
		if (docName != null) {
			s = s + docName;
		}
		//return "" + keyType + pubKey + "," + cryptoKey + "," + suffiX + "/";
		return s;
	}
	
	public String getWriteFreenetKey() {
		return "" + keyType + "@" + privKey + "," + cryptoKey + "/";
	}

	/** 
	 * returns a string representation of the (hyperocha) Key
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[INSERT]freenet:" + getWriteFreenetKey() + "[REQUEST]freenet:" + getReadFreenetKey();
	}
	
	/**
	 * - trim<br> 
	 * - remove 'freenet:'<br>
	 * - check min length (5)
	 * @param aString the string who needs wellnes
	 * @return null if param is null or length < 5, otherwise the niced string
	 */
	public static String freenetKeyStringNiceness(String aString) {
		if (aString == null) { return null; }
		String s = aString.trim();
		String tmpS;
		if (s.startsWith("freenet:")) {
			tmpS = s.substring(8);
		} else {
			tmpS = s;
		}
		if (tmpS.length() < 5) { //at least KSK@x
			return null;
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
		return extra;
	}
	
	/**
	 * @deprecated
	 * @param k
	 * @return
	 */
	public static FreenetKey KSKfromString(String k) {
		String tmpS = freenetKeyStringNiceness(k);
		if ( tmpS == null ) { return null; }
		
		FreenetKey newKey = new FreenetKey();
		newKey.keyType = FreenetKeyType.KSK;
		newKey.docName = tmpS.substring(4);
		return newKey;
	}
	
	/**
	 * @deprecated
	 * @param k
	 * @return
	 */
	public static FreenetKey CHKfromString(String k) {
		String tmpS = freenetKeyStringNiceness(k);
		if ( tmpS == null ) { return null; }
		
//		System.out.println(s);
//		System.out.println(s.substring(5,47));
//		System.out.println(s.substring(48,91));
//		System.out.println(s.substring(92,99));
		FreenetKey newKey = new FreenetKey(Network.FCP2, FreenetKeyType.CHK, tmpS.substring(4,47), null, tmpS.substring(48,91), tmpS.substring(92,99));

		System.out.println("Keytest" + tmpS);
		
		if (tmpS.length() > 100) {
			tmpS = tmpS.substring(100);
			newKey.docName = tmpS;
		}
		System.out.println("Keytest" + tmpS);
//		FreenetKey newKey = new FreenetKey();
//		newKey.keyType = FreenetKeyType.KSK;
//		newKey.pubKey = s.substring(4);
		return newKey;
	}
	
	public static FreenetKey getKeyFromString(String aKey) {
		String tmpS = freenetKeyStringNiceness(aKey);
		if ( tmpS == null ) { return null; }
		
		FreenetKey newKey = new FreenetKey();
		
		// vorlage aus knotensource geklaut:
//		 decode keyType
        int atchar = tmpS.indexOf('@');
        if (atchar == -1) { // kein '@' drinne, buh!
        	return null;
        } else {
        	newKey.keyType = FreenetKeyType.getTypeFromString(tmpS.substring(0, atchar).toUpperCase().trim());
        	tmpS = tmpS.substring(atchar+1);
        }
        
        if ( newKey.keyType == null ) { // buh! kein gueltiger freenet key type
        	return null;
        }
        
        
        if ( newKey.keyType.equals(FreenetKeyType.KSK) ) {
        	newKey.docName = tmpS;
        	
        	// KSK kann .5 und .7 sein, mal sehen was der bei 1.0 alles kann ;)
        	return newKey;        	
        	
        }

        // meta string abschnippeln
        int slash2 = tmpS.indexOf('/');
        if (slash2 != -1) { // ein '/' drinne, alles danach kommt nach docName
        	newKey.docName = tmpS.substring(slash2 + "/".length());
        	tmpS = tmpS.substring(0 ,slash2);
        }

		//if (true) throw new Error("Hier key parse hineinbasteln");

		// wenn nicht ksk ist jetz der zahlenmus uebrig
        
        int comma = tmpS.indexOf(',');
        //System.err.println("comma: " + comma);
        if ( comma == 43) { // 0.7 key
        	newKey.setNetworkType(Network.FCP2);
//        	System.err.println("mus: " + tmpS);
        	newKey.pubKey = tmpS.substring(0,43);
        	newKey.cryptoKey = tmpS.substring(44,87);
        	newKey.extra = tmpS.substring(88,95);
        
        } else { // .5 krempel
    //    	newKey.setNetworkType(Network.FCP1);
	//         	newKey.pubKey = tmpS.substring(0,43);
	//        	newKey.cryptoKey = tmpS.substring(44,87);
	//        	newKey.extra = tmpS.substring(88,95);
        	// not suppoted yet
        	return null;
        }
		return newKey;
	}

	/**
	 * @param String
	 * @return true if param entspricht irgendein valid freenet key schema
	 */
	public static boolean isValidKey(String aKey) {
		
		return ( getKeyFromString(aKey) != null );
		
	}
	
	/**
	 * @param networkType the networkType to set
	 */
	private void setNetworkType(int networktype) {
		this.networkType = networktype;
	}
	
	/**
	 * debug und test: print detailed key 
	 */
	public void decompose() {
        String r = pubKey == null ? "none" : pubKey;
        String w = privKey == null ? "none" : privKey;
        String k = cryptoKey == null ? "none" : cryptoKey;
        String e = extra == null ? "none" : extra;
        System.out.println("FreenetKey: " + this);
        
        System.out.println("Network type : " + Network.getNetworkTypeName(networkType));
        System.out.println("Key type   : " + keyType);
        System.out.println("Read key: " + r);
        System.out.println("Write key: " + r);
        System.out.println("Crypto key : " + k);
        System.out.println("Extra      : " + e);
        System.out.println("File name   : " + (docName == null ? "none" : docName));
    }
}
