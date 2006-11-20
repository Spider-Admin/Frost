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

		testKeyString(null);
		testKeyString("");
		testKeyString("KSK@gpl.txt");
		testKeyString("1234");
		testKeyString("fantasie@hallaballo");
		testKeyString("USK@XeMBryjuEaxqazEuxwnn~G7wCUOXFOZlVWbscdCOUFs,209eycYVidlZvhgL5V2a3INFxrofxzQctEZvyJaFL7I,AQABAAE/frost/-2/");
		testKeyString("SSK@XeMBryjuEaxqazEuxwnn~G7wCUOXFOZlVWbscdCOUFs,209eycYVidlZvhgL5V2a3INFxrofxzQctEZvyJaFL7I,AQABAAE/frost-2/index.html");
		testKeyString("CHK@sxUPFJhNsiCuAJ4TOSZ9eJjSt7AgPo5RmKBDOES5epE,~E-srZbUGarDheOhWLDkLrvfD~m-i1X~E5qk0XGTLm0,AAEC--8/frost-07-Nov-2006.zip");
		testKeyString("CHK@sxUPFJhNsiCuAJ4TOSZ9eJjSt7AgPo5RmKBDOES5epE,~E-srZbUGarDheOhWLDkLrvfD~m-i1X~E5qk0XGTLm0,AAEC--8/");
		testKeyString("CHK@sxUPFJhNsiCuAJ4TOSZ9eJjSt7AgPo5RmKBDOES5epE,~E-srZbUGarDheOhWLDkLrvfD~m-i1X~E5qk0XGTLm0,AAEC--8");
		
		testKeyString("CHK@fD8Y1CnN4BSXrvd5MeWfFNXFvgIMAwI,UH9ZL-d7d9VAc0fn7LI7uQ/frost-20-Jun-2006-java14.zip");
		testKeyString("CHK@fD8Y1CnN4BSXrvd5MeWfFNXFvgIMAwI,UH9ZL-d7d9VAc0fn7LI7uQ/");
		testKeyString("CHK@fD8Y1CnN4BSXrvd5MeWfFNXFvgIMAwI,UH9ZL-d7d9VAc0fn7LI7uQ");
		testKeyString("SSK@rgFrfo~dAesFgV5vylYVNvNGXO0PAgM,wo3T~oLnVbWq-vuD2Kr86Q/frost//");	
		testKeyString("SSK@rgFrfo~dAesFgV5vylYVNvNGXO0PAgM/test/11//index.html");
		testKeyString("SSK@rgFrfo~dAesFgV5vylYVNvNGXO0PAgM,wo3T~oLnVbWq-vuD2Kr86Q/frost/11//index.html");	
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
		//System.out.println("Key: " + aKey.toString());
		aKey.decompose();
		System.out.println("-----------------");
	}
}
