package frost.crypt;
import java.io.File;
/**
 * facade for verifying/signing messages in frost
 */

 public interface crypt {
 
 	public static final int MSG_HEADER_SIZE = 27;
	public static final int SIG_HEADER_SIZE = 34;
	public static final int ENC_HEADER_SIZE = 40;


 	/**
	 * [0] private, [1] public
	 */
 	public String[] generateKeys(); 
	
	/**
	 * the key is the signing key
	 */
	public String sign(String message, String key);
	
	/**
	 * the key is the verification key
	 */
	public boolean verify(String message, String key);
	

	/**
	 * symmetric encryption of a string.
	 */
	public String simEncrypt(String what, String pass);

	/**
	 * symmetric decryption of a string.
	 */
	public String simDecrypt(String what, String pass);

	/**
	 * checksum of a string
	 */
	public String digest(String what);
	public String digest(File which);
	
	/**
	 * encrypt and sign
	 */
	public String encryptSign(String what, String myKey, String otherKey);

	/**
	 * decrypt and verify, returns null if failed
	 */
	public String decrypt(String what, String myKey);
 }
