package freenet.client;

import freenet.*;
import freenet.support.Bucket;
import freenet.support.io.DataNotValidIOException;
import freenet.crypt.UnsupportedCipherException;
import java.io.*;

/**
 * This defines an interface for Client key objects, used to generate Freenet
 * Keys for inserts, or to decode them after requests.
 *
 * @author oskar
 */
public interface ClientKey {

    /** @return  the TLA for the key type (CHK, SVK, SSK, KSK, etc.)
      */
    String keyType();
    
    /** @return  the String name of the symmetric block cipher
      */
    String getCipher();

    /** Tells this ClientKey what symmetric block cipher to use for
      * encryption/decryption of the document.
      * 
      * May only be called BEFORE the first use of getCryptoKey().
      *
      * @throws UnsupportedCipherException  if the cipher name is unsupported
      */
    void setCipher(String cipherName) throws UnsupportedCipherException;

    /** Returns the encryption key to use for encrypting this document. How
      * this is generated depends on the keytype, for some keys it may be
      * random, others dependant on some property set at construction.
      * It may even depend on one of the encode() methods having been called,
      * depending on which constructor was used.
      *
      * Should only be called AFTER setCipher(), if setCipher() is used at all.
      */
    byte[] getCryptoKey() throws KeyException;

    /**
     * This produces an Inputstream of ciphertext from the document according
     * to this Key's protocol. In doing this the ClientKey implementation 
     * should also gather any information it may need about the data (such
     * as digest value).
     *
     * @param plain       the plaintext source
     * @param metaLength  length of the initial metadata (may be 0)
     * @param ctBucket    a bucket in which to store the ciphertext
     *                    as it is being processed.
     */
    InputStream encode(Bucket plain, long metaLength, Bucket ctBucket)
                throws KeyException, IOException;

    InputStream encode(Bucket plainMeta, Bucket plainData, Bucket ctBucket)
                throws KeyException, IOException;

    Document decode(Storables sto, long transLength)
                throws KeyException, DataNotValidIOException;

    /**
     * Returns the Key value of this object. Depending on the constructor 
     * this may rely on encode having been called and may return null 
     * if it hasn't.
     * @return     The Key for this object if it is known, otherwise null.
     */
    Key getKey();

    /**
     * Relies on one of the encode()/decode() methods having been called.
     * @return the Document if known, otherwise null.
     */
    Document getDocument();

    /**
     * Relies on one of the encode()/decode() methods having been called.
     * @return the Storables if known, otherwise null.
     */
    Storables getStorables();
    
    /**
     * The length of the data minus all control characters.
     * @return The length of the plain data if it is known, 0 otherwise.
     */
    long getPlainLength();

    /** @return  the length of the plain data, padded to a power of 2 size
      */
    long getPaddedLength();

    long getPaddedLength(long plainLength);

    /**
     * The total data length, after padding and interleaving with control bytes.
     * @return The length of the total data if it is known, 0 otherwise.
     */
    long getTotalLength();

    long getTotalLength(long plainLength);

    long getTotalLength(long paddedLength, long partSize);

    /**
     * The length of a single part minus control characters.
     * @return The PartSize if it is known, 0 otherwise.
     */
    long getPartSize();

    long getPartSize(long paddedLength);

    /**
     * The length of the sequence of control characters at the end of each
     * part.
     * @return  The length of the sequence of control characters at the end of
     *          each part.
     */
    int getControlLength();

    /**
     * The String URI of this Key
     * @return The URI of this Key if it is known, null otherwise.
     **/
    FreenetURI getURI();
}





