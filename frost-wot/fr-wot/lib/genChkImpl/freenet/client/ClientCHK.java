package freenet.client;
import java.io.*;
import java.util.Random;

import freenet.*;
import freenet.crypt.*;
import freenet.keys.CHK;
import freenet.support.Bucket;

/**
 * ClientKey implementation for CHKs.
 *
 * @author oskar
 **/

public class ClientCHK extends AbstractClientKey {

    public static ClientKey createFromInsertURI(Random r, FreenetURI uri) throws KeyException {
        return new ClientCHK();
    }

    public static ClientKey createFromRequestURI(FreenetURI uri) throws KeyException {
        if (uri.getRoutingKey() == null)
            throw new KeyException("Unspecified routing key");
        if (uri.getCryptoKey() == null)
            throw new KeyException("Unspecified encryption key");
        return new ClientCHK(uri.getRoutingKey(), uri.getCryptoKey());
    }

    private byte[] cryptoKey;
    private CHK chk;

    /**
     * Create a new ClientCHK.  Use encode() to calculate the CHK value.
     */
    public ClientCHK() {
        super();
    }

    public ClientCHK(byte[] val, byte[] cryptoKey) {
        this(new CHK(val), cryptoKey);
    }

    /**
     * Create a ClientCHK object from a given CHK and encryption key.
     * @param chk        The key value.
     * @param cryptoKey  The key used to decrypt/encrypt the document.
     */
    public ClientCHK(CHK chk, byte[] cryptoKey) {
        super();
        this.chk       = chk;
        this.cryptoKey = cryptoKey;
    }

    /** @return "CHK"
      */
    public String keyType() {
        return "CHK";
    }

    /** If the no-args constructor was used, this will return null
      * until encode() has been used.
      * @return the Key if it is known, or null
      */
    public Key getKey() {
        return chk;
    }

    /** If the no-args constructor was used, or if the URI did not contain
      * the encryption key, this will return null until encode() has been used.
      * @return the cryptoKey if it is known, or null
      */
    public byte[] getCryptoKey() {
        return cryptoKey;
    }

    /** Calculates make_key(hash(data)).
      */
    private void makeCryptoKey(InputStream in, long plainLength)
                 throws IOException {
        byte[] buf = new byte[Core.blockSize];
        Digest ctx = SHA1.getInstance();
        while (plainLength > 0) {
            int i = in.read(buf, 0, plainLength < buf.length ? (int) plainLength : buf.length);
            if (i > 0) {
                ctx.update(buf, 0, i);
                plainLength -= i;
            }
            else if (i == -1) throw new EOFException();
        }
        byte[] entropy = ctx.digest();
        cryptoKey = new byte[cipher.getKeySize() >> 3];
        Util.makeKey(entropy, cryptoKey, 0, cryptoKey.length);
    }
/*
    private static Digest ctx=SHA1.getInstance();

    public static void makeKey(byte[] entropy, byte[] key, int offset, int len) {
        synchronized(ctx) {
            ctx.digest(); // reinitialize

            int ic=0;
            while (len>0) {
                ic++;
                for (int i=0; i<ic; i++)
                    ctx.update((byte)0);
                ctx.update(entropy, 0, entropy.length);
                int bc;
                if (len>20) {
                    ctx.digest(true, key, offset);
                    bc=20;
                } else {
                    byte[] hash=ctx.digest();
                    bc=Math.min(len, hash.length);
                    System.arraycopy(hash, 0, key, offset, bc);
                }
                offset+=bc;
                len-=bc;
            }
        }
        wipe(entropy);
    }
*/

    /** Encodes the data in the bucket and re-generates
      * the encryption key and the CHK.
      * @return  an InputStream of encrypted data interleaved w/ control bytes
      */
    public InputStream encode(Bucket data, long metaLength, Bucket ctBucket)
                       throws KeyException, IOException {

        InputStream in1 = data.getInputStream(),
                    in2 = data.getInputStream();
        try {
            makeCryptoKey(in1, data.size());
            InputStream ret = super.encode(in2, data.size(),
                                           metaLength, ctBucket);
            chk = new CHK(storables, Util.log2(getPaddedLength()));
            return ret;
        }
        finally {
            try { in1.close(); } catch (IOException e) {}
            try { in2.close(); } catch (IOException e) {}
        }
    }

    /** Encodes the data in the buckets and re-generates
      * the encryption key and the CHK.
      * @return  an InputStream of encrypted data interleaved w/ control bytes
      */
    public InputStream encode(Bucket meta, Bucket data, Bucket ctBucket)
                       throws KeyException, IOException {

        InputStream ret, metaIn = null, dataIn = null;
        try {
            metaIn = meta.getInputStream();
            dataIn = data.getInputStream();
            makeCryptoKey(new SequenceInputStream(metaIn, dataIn),
                          meta.size() + data.size() );
        }
        finally {
            try { metaIn.close(); } catch (IOException e) {}
            try { dataIn.close(); } catch (IOException e) {}
        }
        try {
            metaIn = meta.getInputStream();
            dataIn = data.getInputStream();
            ret = super.encode(new SequenceInputStream(metaIn, dataIn),
                               meta.size() + data.size(),
                               meta.size(), ctBucket );
        }
        finally {
            try { metaIn.close(); } catch (IOException e) {}
            try { dataIn.close(); } catch (IOException e) {}
        }
        chk = new CHK(storables, Util.log2(getPaddedLength()));
        return ret;
    }

}



