package frost.crypt;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.StringTokenizer;
import java.nio.channels.*;
import java.nio.*;
import java.io.*;

import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.util.encoders.Base64;
//import org.bouncycastle.crypto.paddings.X923Padding;




/**
 * implementation of the crypto layer
 */


 public final class FrostCrypt implements crypt {

 public static Base64 texter; //so people can use it outside the class

 private static RSAKeyPairGenerator keygen;
 private static AsymmetricCipherKeyPair keys;
 private static RSAEngine rsaEngine = new RSAEngine();
 private static SHA1Digest sha1Digest = new SHA1Digest();
 private static final PSSSigner signer = new PSSSigner(rsaEngine, sha1Digest, 16);
 private static TwofishEngine twofish = new TwofishEngine();
 private static BufferedAsymmetricBlockCipher d_encryptor = new BufferedAsymmetricBlockCipher(rsaEngine);
 //private static BufferedBlockCipher sd_encryptor;

 public FrostCrypt() {
 	keygen = new RSAKeyPairGenerator();
	//System.out.println("creating signer " + signer.toString());
	twofish = new TwofishEngine();
	texter=new Base64();
 }


 public synchronized String[] generateKeys() {
 	keygen.init(new RSAKeyGenerationParameters(new
BigInteger("3490529510847650949147849619903898133417764638493387843990820577"),new SecureRandom(), 1024, 80));
	//this big integer is the winner of some competition as far as I remember

	keys = keygen.generateKeyPair();


	//extract the keys
	RSAKeyParameters PubKey = (RSAKeyParameters) keys.getPublic();
        RSAPrivateCrtKeyParameters PrivKey = (RSAPrivateCrtKeyParameters) keys.getPrivate();

	//the return value
	String[] result = new String[2];
	StringBuffer temp = new StringBuffer("");

	//create the keys
	temp.append(new String(Base64.encode(PubKey.getExponent().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PubKey.getModulus().toByteArray())));
	result[1] = temp.toString();

	//rince and repeat, this time exactly the way its done in the constructor
	temp = new StringBuffer("");
	temp.append(new String(Base64.encode(PrivKey.getModulus().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getPublicExponent().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getExponent().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getP().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getQ().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getDP().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getDQ().toByteArray())));
	temp.append(":");
	temp.append(new String(Base64.encode(PrivKey.getQInv().toByteArray())));
	result[0] = temp.toString();

	//that's it
	return result;
 }


 public synchronized String sign(String message, String key) {



 	//extract the key
	StringBuffer signedMessage = new StringBuffer(new String("===Frost signed message===\n"));
	signedMessage.append(message);
	StringTokenizer keycutter = new StringTokenizer(key,":");
	RSAPrivateCrtKeyParameters privKey =new RSAPrivateCrtKeyParameters(
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())));

	//initialize the signer

	signer.init(true,privKey);
	signer.update(message.getBytes(),0,message.length());

	//and sign
	try {
            byte[] signature = signer.generateSignature();
            signedMessage.append("\n=== Frost message signature: ===\n");
            signedMessage.append(new String(Base64.encode(signature)));
            signedMessage.append("\n=== End of Signature. ===");
        } catch (CryptoException e) {
            System.err.println("CryptoException in Verifier.sign: " + e.getMessage());
        }

	//reset the signer
	signer.reset();

        return signedMessage.toString();
 }


 public synchronized boolean verify(String message, String key) {
 	//reset the signer

 	//process the message first
	StringBuffer msg = new StringBuffer(message);

	//check for header, footer, etc.
	int a = msg.indexOf("===Frost signed message===\n");
	if (a == -1) return false;
	int b = msg.lastIndexOf("\n=== Frost message signature: ===\n");
	if ((b == -1) || (b < a)) return false;
	int c = msg.indexOf("\n=== End of Signature. ===",b);
	if ((c== -1) || (c < b) || (c < a)) return false;

	//now extract the message and sig
	String plaintext = msg.substring(a+MSG_HEADER_SIZE,b);
	//System.out.println("plaintext is " + plaintext);
	String signature = msg.substring(b+SIG_HEADER_SIZE,c);
	//System.out.println("signature is " + signature);

	//extract the key
	StringTokenizer keycutter = new StringTokenizer(key,":");
	BigInteger Exponent = new BigInteger(Base64.decode(keycutter.nextToken()));
        BigInteger Modulus = new BigInteger(Base64.decode(keycutter.nextToken()));
	signer.init(false,new RSAKeyParameters(true,Modulus,Exponent));

	//and verify!
	signer.update(plaintext.getBytes(),0,plaintext.length());

	boolean result= signer.verifySignature(Base64.decode(signature.getBytes()));
	signer.reset();
	return result;

 }

 public synchronized String digest(String message) {
        SHA1Digest stomach = new SHA1Digest();
	stomach.reset();
        byte[] food = message.getBytes();
        stomach.update(food, 0, food.length);
        byte[] poop = new byte[64];
        stomach.doFinal(poop,0);
        return (new String(Base64.encode(poop))).substring(0,27);
    }
    
 public synchronized String digest(File file) {
 	SHA1Digest stomach = new SHA1Digest();
	byte[] poop = new byte[64];
	FileChannel chan = null;
	try {
		chan = (new FileInputStream(file)).getChannel();
	}catch(IOException e) {
		e.printStackTrace();
	}
	byte []temp = new byte[1024*1024];
	ByteBuffer _temp = ByteBuffer.wrap(temp);
	long x = 0;
	long y = 0;
	try {
		while(true) {
			//if (y >= file.length()) break;
			//if (y > file.length()) y = file.length();
			int pos = _temp.position();
			int read = chan.read(_temp);
			if (read==-1) break;
			stomach.update(temp,pos,read);
			if (_temp.remaining() == 0) _temp.position(0);
		}
		chan.close();
	}catch (IOException e) {
		e.printStackTrace();
	}
	stomach.doFinal(poop,0);
        return (new String(Base64.encode(poop))).substring(0,27);
 }

 public synchronized String simEncrypt(String what, String key){/*
 //initialize
 //X923Padding pad = new X923Padding();
 //pad.init(new SecureRandom());
 sd_encryptor = new BufferedBlockCipher(twofish);
 sd_encryptor.init(true, new KeyParameter((new String("asdfasdfasdfasdf")).getBytes(),0,16));

 byte []result;
 if (what.length() % sd_encryptor.getBlockSize() == 0) result = new byte[what.length()];
 else
 result = new byte[(what.length() / sd_encryptor.getBlockSize() + 1) * sd_encryptor.getBlockSize()];

 //do the god damn padding myself
 byte []source = new byte[result.length];
 (new Random()).nextBytes(source);
 System.arraycopy(what.getBytes(),0,source,0,what.length());



 System.out.println("encrypting " + what + " to a buffer size " + result.length + " but block size is " + sd_encryptor.getBlockSize());
 try{
 if (sd_encryptor.processBytes(source,0,what.length(),result,0) == 0) {
 	System.out.println("doing final");
 	sd_encryptor.doFinal(result,0);
	}
 }catch (InvalidCipherTextException e){System.out.println("problems");};
 /*
 boolean overflow =false;



 //find the number of necessary blocks.

 if (what.length() % twofish.getBlockSize() != 0) overflow = true;


 //create the result buffer
 byte [] result = new byte[blocks * twofish.getBlockSize()];

 for (int i = 0; i< what.length() /twofish.getBlockSize();i++)
 	twofish.processBlock(what.getBytes(), i*twofish.getBlockSize(),result,i*twofish.getBlockSize());

 /*if (overflow)
 	twofish.processBlock
 //and process the string

 //reset and return
 sd_encryptor.reset();


 return new String(texter.encode(result));*/ return null;
 }

 public synchronized String simDecrypt(String what, String key) {

 //twofish.init(false, //find out about twofish parameters - key length etc.
 return null;
 }

 public synchronized String encryptSign(String what, String myKey, String otherKey){

 	//initialize d_encryptor
 	StringTokenizer keycutter = new StringTokenizer(otherKey,":");
	BigInteger Exponent = new BigInteger(Base64.decode(keycutter.nextToken()));
        BigInteger Modulus = new BigInteger(Base64.decode(keycutter.nextToken()));
	RSAEngine rsa = new RSAEngine();
	rsa.init(true, new RSAKeyParameters(false,Modulus,Exponent));
 	//d_encryptor.init(true, new RSAKeyParameters(false,Modulus,Exponent));
	int size = rsa.getInputBlockSize();
	//System.out.println("input block size "+size);
	int outSize = rsa.getOutputBlockSize();
	//System.out.println("output block size " + outSize);



	//sign the message
	what = sign(what,myKey);
	/*System.out.println(what);/*
	System.out.println("encoded plaintext looks like \n " + new String(texter.encode(what.getBytes())));
	System.out.println("this will need " + (what.length()/size +1) + " blocks");*/

	//put the message in the encryptor

	int noRuns = what.length()/size;
	if (what.length() % size !=0) noRuns++;
	byte []tmp = new byte[noRuns*128];
	byte []str = new byte[noRuns*size];
	System.arraycopy(what.getBytes(),0,str,0,what.length());

	//determine how many blocks we need
	//int noRuns =what.length() / size;
	String result =new String();
	//insert them in the cipher, block at a time

	for (int i=0;i<noRuns;i++) {
	System.arraycopy(rsa.processBlock(str,i*size,size),0,tmp,i*outSize,outSize);
	}
	result = new String(Base64.encode(tmp));
	//d_encryptor.processBytes(what.getBytes(),(what.length()-(what.length() % size)),what.length() % size);
	//result = result + (new String(texter.encode(d_encryptor.doFinal())));


	//pad the string with header and footer
	result =new String("==== Frost Signed+Encrypted Message ====" +
					 result +
					 "==== End Of Frost SE Message ====");

	//rsa.reset();
	return result;


 //return null;
 }

 public synchronized String decrypt(String what, String otherKey){

 	what = what.substring(crypt.ENC_HEADER_SIZE,what.indexOf("==== End Of Frost SE Message ===="));
	//System.out.println("stripped what: " +what);
	//if (what.length() % 172 == 0) System.out.println("good size");

	RSAEngine rsa = new RSAEngine();

	StringTokenizer keycutter = new StringTokenizer(otherKey,":");
	RSAPrivateCrtKeyParameters privKey =new RSAPrivateCrtKeyParameters(
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())),
						new BigInteger(Base64.decode(keycutter.nextToken())));
 	rsa.init(false,privKey);

	int size = rsa.getInputBlockSize();
	int outSize = rsa.getOutputBlockSize();

	//decode the text
	byte [] cipherText = Base64.decode(what.getBytes());
	byte [] plainText = new byte[cipherText.length*128/size];


	//String result = new String();

	for (int i=0;i<cipherText.length / size;i++) {
	System.arraycopy(rsa.processBlock(cipherText,i*size,size),0,plainText,i*outSize,outSize);
	}

	String result = new String(plainText);


	return result;

 }

 }
