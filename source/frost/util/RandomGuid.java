/*
 * Created on 24-dic-2004
 * 
 */
package frost.util;

import java.security.*;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class is used to generate random GUIDs of the type
 * F45C47D0-FF4E-11D8-9669-0800200C9A66 (38 characters long, 
 * including dashes).
 * 
 * @author $Author$
 * @version $Revision$
 */
public class RandomGuid {

	private static Random random = new Random();
	private String guid;
	
	/**
	 * This creates a new instance of RandomGuid. 
	 * @throws NoSuchAlgorithmException if the instance could not be created because
	 * 					the "MD5" algorithm is not available
	 */
	public RandomGuid() throws NoSuchAlgorithmException {
		generateGuid();
	}
	
	/**
	 * This method generates the random guid
	 */
	private void generateGuid() throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		StringBuffer stringToDigest = new StringBuffer();
		
		long time = System.currentTimeMillis();
		long rand = random.nextLong();
		
		stringToDigest.append(time);
		stringToDigest.append("-");
		stringToDigest.append(rand);
		
		md5.update(stringToDigest.toString().getBytes());
		byte[] digestBytes = md5.digest();	//This is always 128 bits long.
		StringBuffer digest = new StringBuffer();
		for (int i = 0; i < digestBytes.length; ++i) {
			int b = digestBytes[i] & 0xFF;
			if (b < 0x10) {
				digest.append('0');
			}
			digest.append(Integer.toHexString(b));
		}
		guid = digest.toString();
	}
	
	/** 
	 * This method returns a String representation of the guid in
	 * the standard format for GUIDs, like F45C47D0-FF4E-11D8-9669-0800200C9A66
	 * @see java.lang.Object#toString()
	 */	
	public String toString() {
		String guidUpperCase = guid.toUpperCase();
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append(guidUpperCase.substring(0, 8));
		stringBuffer.append("-");
		stringBuffer.append(guidUpperCase.substring(8, 12));
		stringBuffer.append("-");
		stringBuffer.append(guidUpperCase.substring(12, 16));
		stringBuffer.append("-");
		stringBuffer.append(guidUpperCase.substring(16, 20));
		stringBuffer.append("-");
		stringBuffer.append(guidUpperCase.substring(20));

		return stringBuffer.toString();
	}
	
}
