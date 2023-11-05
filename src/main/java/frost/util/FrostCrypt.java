/*
  FrostCrypt.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Implementation of the crypto layer.
 */
public final class FrostCrypt {

	private static final Logger logger = LoggerFactory.getLogger(FrostCrypt.class);

    private PSSSigner signer;
    private SecureRandom secureRandom = null;

    private KeyGenerator keyGeneratorAES = null;

    public FrostCrypt() {
        Security.addProvider(new BouncyCastleProvider());

        signer = new PSSSigner(new RSAEngine(), new SHA1Digest(), 16);
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            secureRandom = new SecureRandom();
        }
    }

    /**
     * Generate a new RSA 1024 bit key pair.
     * @returns String[0] is private key; String[1] is public key
     */
    public synchronized String[] generateKeys() {

        RSAKeyPairGenerator keygen = new RSAKeyPairGenerator();
        keygen.init(
            new RSAKeyGenerationParameters(
                new BigInteger("3490529510847650949147849619903898133417764638493387843990820577"),
                getSecureRandom(),
                1024,
                80));
        //this big integer is the winner of some competition as far as I remember

        AsymmetricCipherKeyPair keys = keygen.generateKeyPair();

        //extract the keys
        RSAKeyParameters pubKey = (RSAKeyParameters) keys.getPublic();
        RSAPrivateCrtKeyParameters privKey = (RSAPrivateCrtKeyParameters) keys.getPrivate();

        //the return value
        String[] result = new String[2];
        StringBuilder temp = new StringBuilder();

        //create the keys
        temp.append( new String(Base64.encode(pubKey.getExponent().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(pubKey.getModulus().toByteArray())));
        result[1] = temp.toString(); // public key

        //rince and repeat, this time exactly the way its done in the constructor
        temp = new StringBuilder();
        temp.append(new String(Base64.encode(privKey.getModulus().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getPublicExponent().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getExponent().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getP().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getQ().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getDP().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getDQ().toByteArray())));
        temp.append(":");
        temp.append(new String(Base64.encode(privKey.getQInv().toByteArray())));
        result[0] = temp.toString(); // private key

        return result;
    }

    /**
     * Computes the SHA-1 checksum of given message.
     */
    public synchronized String digest(String message) {
        try {
            SHA1Digest stomach = new SHA1Digest();
            stomach.reset();
            byte[] food = message.getBytes("UTF-8");
            stomach.update(food, 0, food.length);
            byte[] poop = new byte[64];
            stomach.doFinal(poop, 0);
            return (new String(Base64.encode(poop))).substring(0, 27);
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding is not supported.", ex);
        }
        return null;
    }

    /**
     * Computes the SHA-1 checksum of given file.
     */
    public synchronized String digest(File file) {
        SHA1Digest stomach = new SHA1Digest();
        byte[] poop = new byte[64];
        FileChannel chan = null;
        try {
            chan = (new FileInputStream(file)).getChannel();
        } catch (IOException e) {
            logger.error("Exception thrown in digest(File file)", e);
        }
        byte[] temp = new byte[4 * 1024];
        ByteBuffer _temp = ByteBuffer.wrap(temp);
        try {
            while (true) {
                //if (y >= file.length()) break;
                //if (y > file.length()) y = file.length();
                int pos = _temp.position();
                int read = chan.read(_temp);
                if (read == -1)
                    break;
                stomach.update(temp, pos, read);
                if (_temp.remaining() == 0)
                    _temp.position(0);
            }
            chan.close();
        } catch (IOException e) {
            logger.error("Exception thrown in digest(File file)", e);
        }
        stomach.doFinal(poop, 0);
        return (new String(Base64.encode(poop))).substring(0, 27);
    }

    public synchronized String encrypt(String what, String publicKey) {
        try {
            byte[] whatBytes = what.getBytes("UTF-8");
            byte[] encryptedBytes = encrypt(whatBytes, publicKey);
            String result = new String(Base64.encode(encryptedBytes), "ISO-8859-1");
            return result;
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding is not supported.", ex);
        }
        return null;
    }

    /**
     * Encryption of a byte array.
     *
     * We generate a new 128bit AES key and encrypt the content with this key.
     * Then we RSA encrypt the key with publicKey. RSA ecpects 117 bytes of input
     * and generates 128byte of output. So we prepare a byte array of length 117 with
     * random data and copy the AES key into the front of it. Then we RSA encrypt this
     * array and put the result array of 128bytes length into the front of the AES encrypted
     * data.
     *
     *  @returns null if anything failed.
     */
    public synchronized byte[] encrypt(byte[] what, String publicKey) {

        byte[] aesKeyBytes = null;
        Cipher cipherAES = null;
        Cipher cipherRSA = null;

        int cipherRSAinSize = 0;
        int cipherRSAoutSize = 0;

        // prepare AES, we need cipherAES and keyBytes
        aesKeyBytes = generateAESSessionKey(); // 16 bytes
        if( aesKeyBytes == null ) {
            return null;
        }
        cipherAES = buildCipherAES(Cipher.ENCRYPT_MODE, aesKeyBytes);;
        if( cipherAES == null ) {
            return null;
        }

        // prepare RSA, we only need chiperRSA
        try {
            StringTokenizer keycutter = new StringTokenizer(publicKey, ":");
            BigInteger Exponent = new BigInteger(Base64.decode(keycutter.nextToken()));
            BigInteger Modulus = new BigInteger(Base64.decode(keycutter.nextToken()));
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(Modulus, Exponent);

            KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
            PublicKey pubKey = fact.generatePublic(pubKeySpec);
            cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipherRSA.init(Cipher.ENCRYPT_MODE, pubKey);
            cipherRSAinSize = cipherRSA.getBlockSize();
            cipherRSAoutSize = cipherRSA.getOutputSize(cipherRSAinSize);
            if( cipherRSAinSize != 117 || cipherRSAoutSize != 128 ) {
                throw new Exception("block size invalid, inSize="+cipherRSAinSize+"; outSize="+cipherRSAoutSize);
            }
        } catch(Throwable t) {
            logger.error("Error in encrypt, RSA preparation", t);
            return null;
        }

        // encrypt keybytes with RSA
        byte rsaEncData[] = null;
        try {
            byte[] rsaInpData = new byte[cipherRSAinSize]; // input for RSA encryption
            // build 128 byte, first 16 byte the AES session key, remaining bytes are random data
            byte[] randomBytes = new byte[cipherRSAinSize - aesKeyBytes.length];
            getSecureRandom().nextBytes(randomBytes);

            System.arraycopy(aesKeyBytes, 0, rsaInpData, 0, aesKeyBytes.length);
            System.arraycopy(randomBytes, 0, rsaInpData, aesKeyBytes.length, randomBytes.length);

            rsaEncData = cipherRSA.doFinal(rsaInpData, 0, rsaInpData.length);
            if( rsaEncData.length != cipherRSAoutSize ) {
                throw new Exception("RSA out block size invalid: "+rsaEncData.length);
            }
        } catch (Throwable t) {
            logger.error("Error in encrypt, RSA encryption", t);
            return null;
        }

        // encrypt content using AES

        ByteArrayOutputStream plainOut = new ByteArrayOutputStream(what.length + (what.length/10) +rsaEncData.length);
        try {
            // write RSA encrypted data
            plainOut.write(rsaEncData);

            // encrypt
            CipherOutputStream cOut = new CipherOutputStream(plainOut, cipherAES);
            cOut.write(what);

            cOut.close();
        } catch (IOException e) {
            logger.error("Error in encrypt, AES encryption", e);
            return null;
        }
        return plainOut.toByteArray();
    }

    public synchronized String decrypt(String what, String privateKey) {
        try {
            byte[] encBytes = Base64.decode(what.getBytes("ISO-8859-1"));
            byte[] decBytes = decrypt(encBytes, privateKey);
            return new String(decBytes, "UTF-8");
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding is not supported.", ex);
        }
        return null;
    }

    /**
     * Decrypts a byte array.
     *
     * The first 128 byte in array must be the RSA encrypted AES key,
     * remaining data is the AES data. See encrypt().
     */
    public synchronized byte[] decrypt(byte[] what, String privateKey) {

        Cipher cipherAES = null;
        Cipher cipherRSA = null;

        int cipherRSAinSize = 0;
        int cipherRSAoutSize = 0;

        // prepare RSA, we need chiperRSA
        try {
            StringTokenizer keycutter = new StringTokenizer(privateKey, ":");
            RSAPrivateCrtKeySpec privKeySpec = new RSAPrivateCrtKeySpec(
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())),
                    new BigInteger(Base64.decode(keycutter.nextToken())));

            KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
            PrivateKey privKey = fact.generatePrivate(privKeySpec);
            cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipherRSA.init(Cipher.DECRYPT_MODE, privKey);
            cipherRSAinSize = cipherRSA.getBlockSize();
            cipherRSAoutSize = cipherRSA.getOutputSize(cipherRSAinSize);
            if( cipherRSAinSize != 128 || cipherRSAoutSize != 117 ) {
                throw new Exception("RSA decryption block size invalid, inSize="+cipherRSAinSize+"; outSize="+cipherRSAoutSize);
            }
        } catch(Throwable e) {
            logger.error("Error in decrypt, RSA preparation", e);
            return null;
        }

        // decrypt rsa and get aes key
        byte[] aesKeyBytes = null;
        try {
            byte[] sessionKeyBytes = cipherRSA.doFinal(what, 0, cipherRSAinSize);
            if( sessionKeyBytes == null ) {
                throw new Exception("RSA decryption failed, sessionKeyBytes = null");
            }
            if( sessionKeyBytes.length != cipherRSAoutSize ) {
                throw new Exception("RSA decryption failed, sessionKeyBytes.length = "+sessionKeyBytes.length+
                        ", must be "+cipherRSAoutSize);
            }
            // we need the first 16 byte
            aesKeyBytes = new byte[16];
            System.arraycopy(sessionKeyBytes, 0, aesKeyBytes, 0, aesKeyBytes.length);
        } catch (Throwable e) {
            logger.error("Error in decrypt, RSA decryption", e);
            return null;
        }

        // prepare AES, we need cipherAES
        cipherAES = buildCipherAES(Cipher.DECRYPT_MODE, aesKeyBytes);;
        if( cipherAES == null ) {
            return null;
        }

        // decrypt aes
        ByteArrayOutputStream plainOut = new ByteArrayOutputStream(what.length - cipherRSAinSize);

        ByteArrayInputStream bIn = new ByteArrayInputStream(what, cipherRSAinSize, what.length-cipherRSAinSize);
        CipherInputStream cIn = new CipherInputStream(bIn, cipherAES);

        try {
            byte[] buf = new byte[1024];
            while(true) {
                int bLen = cIn.read(buf);
                if( bLen < 0 ) {
                    break; // eof
                }
                plainOut.write(buf, 0, bLen);
            }
            cIn.close();
        } catch (Throwable e) {
            logger.error("Error in decrypt, AES decryption", e);
            return null;
        }
        return plainOut.toByteArray();
    }

    public synchronized String detachedSign(String message, String key){
        try {
            byte[] msgBytes = message.getBytes("UTF-8");
            return detachedSign(msgBytes, key);
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding is not supported.", ex);
        }
        return null;
    }
    public synchronized String detachedSign(byte[] message, String key) {

        StringTokenizer keycutter = new StringTokenizer(key, ":");
        RSAPrivateCrtKeyParameters privKey =
            new RSAPrivateCrtKeyParameters(
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())),
                new BigInteger(Base64.decode(keycutter.nextToken())));

        signer.init(true, privKey);
        signer.update(message, 0, message.length);

        byte[] signature = null;
        try {
            signature = signer.generateSignature();
        } catch (CryptoException e) {
            logger.error("Exception thrown in detachedSign(String message, String key)", e);
        }
        signer.reset();
        try {
            String result = new String(Base64.encode(signature), "ISO-8859-1");
            return result;
        } catch(UnsupportedEncodingException ex) {
            logger.error("ISO-8859-1 encoding is not supported.", ex);
        }
        return null;
    }

    public synchronized boolean detachedVerify(String message, String key, String sig){
        try {
            byte[] msgBytes = message.getBytes("UTF-8");
            return detachedVerify(msgBytes, key, sig);
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding is not supported.", ex);
        }
        return false;
    }
    public synchronized boolean detachedVerify(byte[] message, String key, String _sig) {
        try {
            byte[] sig = Base64.decode(_sig.getBytes("ISO-8859-1"));

            StringTokenizer keycutter = new StringTokenizer(key, ":");
            BigInteger Exponent = new BigInteger(Base64.decode(keycutter.nextToken()));
            BigInteger Modulus = new BigInteger(Base64.decode(keycutter.nextToken()));

            signer.init(false, new RSAKeyParameters(true, Modulus, Exponent));

            signer.update(message, 0, message.length);
            boolean result = signer.verifySignature(sig);
            signer.reset();
            return result;
        } catch(UnsupportedEncodingException ex) {
            logger.error("ISO-8859-1 encoding is not supported.", ex);
        }
        return false;
    }

    public synchronized SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public String decode64(String what) {
        try {
            byte[] whatBytes = what.getBytes("ISO-8859-1");
            return new String(Base64.decode(whatBytes), "UTF-8");
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 or ISO-8859-1 encoding is not supported.", ex);
        }
        return null;
    }

    public String encode64(String what) {
        try {
            byte[] whatBytes = what.getBytes("UTF-8");
            return new String(Base64.encode(whatBytes), "ISO-8859-1");
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 or ISO-8859-1 encoding is not supported.", ex);
        }
        return null;
    }

    /**
     * Called by encrypt() to generate a new random session key for AES.
     * Must be called synchronized (we use a global object)!
     * Currently only called by synchronized encrypt().
     *
     * @return the new session key or null.
     */
    private byte[] generateAESSessionKey() {
        if( keyGeneratorAES == null ) {
            try {
                keyGeneratorAES = KeyGenerator.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                logger.error("Could not get a KeyGenerator for AES.", e);
                return null;
            }
            keyGeneratorAES.init(128); // 192 and 256 bits may not be available!
        }
        SecretKey skey = keyGeneratorAES.generateKey();
        byte[] keyBytes = skey.getEncoded(); // 16 bytes
        return keyBytes;
    }

    /**
     * Builds and returns a new Cipher for AES.
     */
    private Cipher buildCipherAES(int mode, byte[] aesKey) {
        Cipher cipherAES = null;
        try {
            if( aesKey == null ) {
                return null;
            }
            SecretKeySpec sessionKey = new SecretKeySpec(aesKey, "AES");
            cipherAES = Cipher.getInstance("AES", "BC");
            cipherAES.init(mode, sessionKey);

        } catch(Throwable t) {
            logger.error("Error in AES preparation", t);
            return null;
        }
        return cipherAES;
    }

    /**
     * Computes the SHA256 checksum of utf-8 string.
     */
    public String computeChecksumSHA256(String message) {
        try {
            byte[] food = message.getBytes("UTF-8");
            return computeChecksumSHA256(food);
        } catch(UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding is not supported.", ex);
        }
        return null;
    }
    
    /**
     * Computes the SHA256 checksum of bytes.
     */
    public String computeChecksumSHA256(byte[] message) {
        try {
            byte[] food = message;
            
            MessageDigest sha256 = MessageDigest.getInstance("SHA256", "BC");
            sha256.update(food);
            byte[] poop = sha256.digest();
            
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < poop.length; i++) {
                sb.append(Integer.toString( ( poop[i] & 0xff ) + 0x100 , 16).substring(1));
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Algorithm SHA256 not supported.", ex);
        } catch (NoSuchProviderException ex) {
            logger.error("Provider BC not supported.", ex);
        }
        return null;
    }
    
    /**
     * Computes the SHA256 checksum of a file.
     */
    public String computeChecksumSHA256(File file) {
        try {
            FileChannel chan = null;
            try {
                chan = (new FileInputStream(file)).getChannel();
            } catch (Throwable e) {
                logger.error("Exception thrown 1", e);
                return null;
            }
            
            MessageDigest sha256 = MessageDigest.getInstance("SHA256", "BC");
            
            byte[] temp = new byte[4 * 1024];
            ByteBuffer _temp = ByteBuffer.wrap(temp);
            try {
                while (true) {
                    //if (y >= file.length()) break;
                    //if (y > file.length()) y = file.length();
                    int pos = _temp.position();
                    int read = chan.read(_temp);
                    if (read == -1)
                        break;
                    sha256.update(temp, pos, read);
                    if (_temp.remaining() == 0)
                        _temp.position(0);
                }
                chan.close();
            } catch (Throwable e) {
                logger.error("Exception thrown 2", e);
            }
            
            byte[] poop = sha256.digest();
            
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < poop.length; i++) {
                sb.append(Integer.toString( ( poop[i] & 0xff ) + 0x100 , 16).substring(1));
            }
            return sb.toString().toUpperCase();
            
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Algorithm SHA256 not supported.", ex);
        } catch (NoSuchProviderException ex) {
            logger.error("Provider BC not supported.", ex);
        }
        return null;
    }
}
