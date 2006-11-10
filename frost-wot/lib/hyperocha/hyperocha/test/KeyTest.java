/**
 * 
 */
package hyperocha.test;

import hyperocha.freenet.fcp.FreenetKey;

/**
 * @author saces
 *
 */
public class KeyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Key1:");
		FreenetKey key1 = FreenetKey.getKeyFromString(null);
		testKey(key1);
		
		System.out.println("Key2:");
		
		FreenetKey key2 = FreenetKey.getKeyFromString("");
		
		testKey(key2);
		
		System.out.println("Key3:");
		
		FreenetKey key3 = FreenetKey.KSKfromString(null);
		
		testKey(key3);
		
		System.out.println("Key4:");
		
		FreenetKey key4 = FreenetKey.KSKfromString("");

		testKey(key4);
		
		System.out.println("Key5:");
		
		FreenetKey key5 = FreenetKey.CHKfromString(null);
		
		testKey(key5);
		
		System.out.println("Key6:");
		
		FreenetKey key6 = FreenetKey.CHKfromString("");
		
		testKey(key6);
		
		testKeyString(null);
		testKeyString("");
		testKeyString("USK@S3OdhajGOBfGUDHjRI9DfRpDU6qvufF0ecms2OEVqQw,5ASb27oiCK7lsdr9Orf8v7JT5FBHvmdvL5B9ikgpolw,AQABAAE/Volodya/-9/");
		testKeyString("USK@XeMBryjuEaxqazEuxwnn~G7wCUOXFOZlVWbscdCOUFs,209eycYVidlZvhgL5V2a3INFxrofxzQctEZvyJaFL7I,AQABAAE/frost/-2/");
		testKeyString("CHK@sxUPFJhNsiCuAJ4TOSZ9eJjSt7AgPo5RmKBDOES5epE,~E-srZbUGarDheOhWLDkLrvfD~m-i1X~E5qk0XGTLm0,AAEC--8/frost-07-Nov-2006.zip");
		testKeyString("CHK@sxUPFJhNsiCuAJ4TOSZ9eJjSt7AgPo5RmKBDOES5epE,~E-srZbUGarDheOhWLDkLrvfD~m-i1X~E5qk0XGTLm0,AAEC--8/");
		testKeyString("CHK@sxUPFJhNsiCuAJ4TOSZ9eJjSt7AgPo5RmKBDOES5epE,~E-srZbUGarDheOhWLDkLrvfD~m-i1X~E5qk0XGTLm0,AAEC--8");
		testKeyString("SSK@ZmT-sXV4LpCGeMjo-b7KAs5xc3kPAgM,cYya4cyJ0w5GPYs0vtRDzQ/bOOm/2//");
		testKeyString("CHK@fD8Y1CnN4BSXrvd5MeWfFNXFvgIMAwI,UH9ZL-d7d9VAc0fn7LI7uQ/frost-20-Jun-2006-java14.zip");
		testKeyString("CHK@fD8Y1CnN4BSXrvd5MeWfFNXFvgIMAwI,UH9ZL-d7d9VAc0fn7LI7uQ/");
		testKeyString("CHK@fD8Y1CnN4BSXrvd5MeWfFNXFvgIMAwI,UH9ZL-d7d9VAc0fn7LI7uQ");
		
		testKeyString("KSK@gpl.txt");
		testKeyString("1");

	}
	
	public static void testKeyString(String aString) {
		if ( aString == null ) {
			System.out.println("Teste String: null");
		} else {
			System.out.println("Teste String: '" + aString + "'");
		}
		FreenetKey k1 = FreenetKey.getKeyFromString(aString);
		testKey(k1);
		
	}

	public static void testKey(FreenetKey aKey) {
		if (aKey == null) {
			System.out.println("Key ist null.");
			return;
		}
		aKey.decompose();
		System.out.println("Key: " + aKey.toString());
		System.out.println("-----------------");
	}
}
