package freenet.client;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import freenet.*;
import freenet.crypt.*;
import freenet.support.Bucket;
import freenet.support.io.DataNotValidIOException;

/** Shared functionality between ClientKey implementations.
  * @author tavin
  */
public abstract class AbstractClientKey implements ClientKey {

    // enough already with these dynamically loaded class problems..
    static {
        ClientCHK.class.toString();
//        ClientSVK.class.toString();
//        ClientSSK.class.toString();
//        ClientKSK.class.toString();
    }

    public static final String DEFAULT_CIPHER = "Twofish";

    public static ClientKey createFromInsertURI(Random r, FreenetURI uri)
                            throws KeyException {
        return createClientKey( uri.getKeyType(),
                                "createFromInsertURI",
                                new Class[]  { Random.class, FreenetURI.class },
                                new Object[] { r, uri } );
    }

    public static ClientKey createFromRequestURI(FreenetURI uri)
                            throws KeyException {
        return createClientKey( uri.getKeyType(),
                                "createFromRequestURI",
                                new Class[]  { FreenetURI.class },
                                new Object[] { uri } );
    }

    private static ClientKey createClientKey( String keyType, String method,
                                              Class[] c, Object[] o )
                             throws KeyException {
        try {
            Class ckClass = Class.forName("freenet.client.Client"+keyType);
            if (!AbstractClientKey.class.isAssignableFrom(ckClass))
                throw new ClassNotFoundException();
            return (ClientKey) ckClass.getDeclaredMethod(method, c).invoke(null, o);
        }
        catch (ClassNotFoundException e) {
            throw new KeyException("Unknown keytype");
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof KeyException)
                throw (KeyException) e.getTargetException();
            else
                throw new KeyException(e.toString());
        }
        catch (NoSuchMethodException e) {
            throw new KeyException("Key not constructable from URI");
        }
        catch (IllegalAccessException e) {
            throw new KeyException("Illegal access");
        }
    }



    protected String cipherName;
    protected BlockCipher cipher;
    protected Document doc;
    protected Storables storables;

    protected AbstractClientKey() throws KeyException {
        try {
            setCipher(DEFAULT_CIPHER);
        }
        catch (UnsupportedCipherException e) {
            throw new KeyException(""+e);
        }
    }

    //protected AbstractClientKey(String cipherName) throws KeyException,
    //                                                      UnsupportedCipherException {
    //    setCipher(cipherName);
    //}

    public void setCipher(String cipherName) throws UnsupportedCipherException {
        if (cipherName == null)
            cipherName = DEFAULT_CIPHER;
        BlockCipher cipher = Util.getCipherByName(cipherName);
        if (cipher == null)
            throw new UnsupportedCipherException("Unsupported cipher " + cipherName);
        this.cipherName = cipherName;
        this.cipher     = cipher;
    }

    public String getCipher() {
        return cipherName;
    }

    /** Subclasses may call this to implement the encodePlainData() methods.
      * The cipher and cryptoKey MUST be set up first.
      * This will prepare the Document and Storables associated with this
      * ClientKey.
      *
      * The fields Part-size, Initial-digest, Symmetric-cipher, and
      * Document-header will be set in the Storables, so subclasses may
      * need to add additional fields.
      *
      * @param plain       the plaintext source
      * @param length      the actual non-padded data length
      * @param metaLength  the length of the initial metadata (may be 0)
      * @param ctBucket    temporary storage for cryptographic operations;
      *                    must be large enough to contain
      *                    getTotalLength(pLen, getPartSize(pLen)) bytes
      *                    where pLen == getPaddedLength(length)
      * @return  an InputStream providing the encrypted bytes interleaved
      *          with control bytes, of length getTotalLength()
      */
    protected InputStream encode(InputStream plain,
                                 long length, long metaLength,
                                 Bucket ctBucket) throws IOException {

        // initialize Document for insert
        doc = new Document(cipher, getCryptoKey(), length, metaLength);
        long partSize   = getPartSize();
        long paddingLen = getPaddedLength() - length;

        ProgressiveHashOutputStream out =
            new ProgressiveHashOutputStream(
                partSize, new SHA1Factory(), ctBucket
            );

        // write the encrypted data through the ProgressiveHashOutputStream
        // and into the ctBucket, while gathering an ordinary SHA1 digest
        // of the plaintext data

	// MUST use SHA1 DIRECTLY, because need to use rollingHashPad()
        SHA1 ctx = new SHA1();
        OutputStream ctStream = doc.encipheringOutputStream(out);

        byte[] buf = new byte[Core.blockSize];
        while (length > 0) {
            int i = plain.read(buf, 0, length < buf.length ? (int) length : buf.length);
            if (i > 0) {
                ctx.update(buf, 0, i);
                ctStream.write(buf, 0, i);
                length -= i;
            }
            else if (i == -1) throw new EOFException();
        }

        // use the digest of the plaintext data to generate the padding,
        // and run the padding through the ProgressiveHashOutputStream
        // and into the ctBucket
        Util.rollingHashPad(ctStream, paddingLen, ctx);

        // finish the ProgressiveHashOutputStream to get the Initial-digest
        // and set all Storables
        ctStream.flush();
        out.close();

        storables = new Storables();
        storables.setSymmetricCipher(cipherName);
        storables.setPartSize(partSize);
        storables.setDocumentHeader(doc.getDocumentHeader());
        storables.setInitialDigest(out.getInitialDigest());

        //byte[] t = out.getInitialDigest();
        //System.err.println("encode(): initial digest: "+t[0]+" "+t[1]+" "+t[2]);

        // return an input stream of the encrypted data with the
        // progressive-hash control bytes
        return out.getInputStream();
    }

    /** Prepares a Document for decoding the data of a Freenet key.
      * Returns the prepared Document for convenience.
      * Also associates the given Storables with this ClientKey.
      * @return  the prepared Document
      */
    public Document decode(Storables sto, long transLength)
                    throws KeyException, DataNotValidIOException {

        String cipherName = sto.getSymmetricCipher();
        byte[] header     = sto.getDocumentHeader();
        if (cipherName == null || header == null)
            throw new DataNotValidIOException(Document.DOC_BAD_STORABLES);
        try {
            setCipher(cipherName);
        }
        catch (UnsupportedCipherException e) {
            throw new DataNotValidIOException(Document.DOC_UNKNOWN_CIPHER);
        }
        doc = new Document(cipher, getCryptoKey(), header);
        if (transLength != getTotalLength()) {
	    DataNotValidIOException e =
		new DataNotValidIOException(Document.DOC_BAD_LENGTH);
/*	    Core.logger.log(this,"Total length is " + getTotalLength() +
			    " but transLength = " + transLength, e,
			    Core.logger.DEBUG);*/
            throw e;
	}
        storables = sto;
        return doc;
    }

    public Document getDocument() {
        return doc;
    }

    public Storables getStorables() {
        return storables;
    }

    public long getPlainLength() {
        return doc == null ? 0 : doc.length();
    }

    public long getPaddedLength() {
        return getPaddedLength(getPlainLength());
    }

    public long getPaddedLength(long plainLength) {
        return Math.max(1 << Key.LOG2_MINSIZE, 1 << Util.log2(plainLength));
    }

    public long getTotalLength() {
        return getTotalLength(getPaddedLength(), getPartSize());
    }

    public long getTotalLength(long plainLength) {
        return getTotalLength( getPaddedLength(plainLength),
                               getPartSize(plainLength) );
    }

    public long getTotalLength(long paddedLength, long partSize) {
        return Key.getTransmissionLength(paddedLength, partSize);
    }

    public long getPartSize() {
        return getPartSize(getPaddedLength());
    }

    public long getPartSize(long paddedLength) {
        return Key.getPartSize(paddedLength);
    }

    /** This should be the same for all keys, as of now :)
      * @return  length of the control bytes between parts
      *          (i.e., the progressive hashes + the one CB)
      */
    public int getControlLength() {
        return Key.getControlLength();
    }

    /** Subclasses such as SSKs should override.
      */
    public String getDocumentName() {
        return null;
    }

    public String toString() {
        return getURI() == null ? "new "+keyType() : getURI().toString();
    }

    public FreenetURI getURI() {
        return getKey() == null || getCryptoKey() == null
            ? null
            : new FreenetURI( keyType(), getDocumentName(),
                              getKey().getVal(), getCryptoKey() );
    }

  public abstract String keyType();
  public abstract byte[] getCryptoKey() throws KeyException;
  public abstract InputStream encode(Bucket plain, long metaLength, Bucket ctBucket)
              throws KeyException, IOException;
  public abstract InputStream encode(Bucket plainMeta, Bucket plainData, Bucket ctBucket)
              throws KeyException, IOException;
  public abstract Key getKey();

}



