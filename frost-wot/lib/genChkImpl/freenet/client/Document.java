package freenet.client;

import java.io.*;
import java.math.BigInteger;
import java.util.Vector;

import freenet.Key;
import freenet.crypt.*;
import freenet.support.io.DataNotValidIOException;

/*
  This code is part of the Java Adaptive Network Client by Ian Clarke. 
  It is distributed under the GNU Public Licence (GPL) version 2.  See
  http://www.gnu.org/ for further details of the GPL.
*/

/**
  * This class holds all relevant data about a Freenet document to support
  * document-level encryption and verification.
  *
  * In 0.4, Document no longer encapsulates InputStreams for the data and
  * metadata.  For the sake of simplicity and flexibility, it now only
  * encapsulates these primitives:
  *
  *     -- total file length
  *     -- metadata length
  *     -- symmetric cipher and encryption key
  *
  * A Document may be constructed with all these values known,
  * in which case it will calculate the Document-header for later retrieval
  * with getDocumentHeader().
  *
  * A Document may also be constructed only from the cipher, encryption key,
  * and an encrypted Document-header, in which case the validity of the
  * Document-header will be checked and the lengths will be extracted.
  *
  * Users of the first constructor would then normally use
  * encipheringInputStream() or encipheringOutputStream()
  * to write the encrypted data (making sure to zero-pad up
  * to a power of 2 file size).
  *
  * Users of the second constructor would normally use
  * decipheringInputStream() or decipheringOutputStream()
  * to read the decrypted data.
  */
public class Document {

    // for the client-side DataNotValidIOExceptions
    public static final int
        DOC_BAD_HEADER     = 0xC1,
        DOC_BAD_KEY        = 0xC2,
        DOC_UNKNOWN_CIPHER = 0xC3,
        DOC_BAD_LENGTH     = 0xC4,
        DOC_BAD_STORABLES  = 0xC5;

    public static final String getTextForDNV(int code) {
        switch (code) {
            case DOC_BAD_HEADER:      return "DOC_BAD_HEADER";
            case DOC_BAD_KEY:         return "DOC_BAD_KEY";
            case DOC_UNKNOWN_CIPHER:  return "DOC_UNKNOWN_CIPHER";
            case DOC_BAD_LENGTH:      return "DOC_BAD_LENGTH";
            case DOC_BAD_STORABLES:   return "DOC_BAD_STORABLES";
            default:                  return null;
        }
    }

    protected long length, metaLength;
    protected byte[] header;
    protected byte[] cryptoKey;
    protected PCFBMode ctx;

    protected static int fieldSize(long n) {
        int byteLen = (Util.log2(n+1) + 7) >> 3;
        return byteLen == 0 ? 1 : byteLen;
    }

    /** Create a new document from the supplied parameters.
      * Use this to encrypt and insert a file into Freenet.
      *
      * @param cipher      The symmetric cipher to encrypt with.
      * @param cryptoKey   The encryption key to use.
      * @param length      The total length of the file.
      * @param metaLength  The length of the initial metadata.
      * @throws IOException internal screwup with ByteArrayOutputStream
      */
    public Document( BlockCipher cipher, byte[] cryptoKey,
                     long length, long metaLength          ) throws IOException {
        
        if (length < 0 || Util.log2(length) > Key.LOG2_MAXSIZE)
            throw new IllegalArgumentException("length out of bounds");
        if (metaLength < 0 || metaLength > length)
            throw new IllegalArgumentException("metadata length out of bounds");
	if (length + metaLength <= 0)
	    throw new IllegalArgumentException("completely empty insert");
        
        this.cryptoKey  = cryptoKey;
        this.length     = length;
        this.metaLength = metaLength;
    
        cipher.initialize(cryptoKey);
        // leaving the IV initialized to all zeroes for now...
        ctx = new PCFBMode(cipher);
        
        // fill the Document-header bytes
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        fillHeader(bout, Util.hashBytes(new SHA1(), cryptoKey));
        fillHeader(bout, length);
        fillHeader(bout, metaLength);
        bout.write(0);
        bout.write(0);

        // add padding if necessary
        int padLen = (1 << Util.log2(bout.size())) - bout.size();
        if (padLen > 0) {
            SHA1 dig = new SHA1();
	    // Use SHA1 directly for rollingHashPad
	    
            dig.update(bout.toByteArray());
            Util.rollingHashPad(bout, padLen, dig);
        }
        
        // encrypt
        header = bout.toByteArray();
        for (int i=0; i<header.length; ++i)
            header[i] = (byte) ctx.encipher(header[i]);
    }

    protected void fillHeader(ByteArrayOutputStream bout, byte[] b) throws IOException {
        bout.write(0xFF & b.length >> 8);
        bout.write(0xFF & b.length);
        bout.write(b);
    }

    protected void fillHeader(ByteArrayOutputStream bout, long num) throws IOException {
        int byteLen = fieldSize(num);
        bout.write(0xFF & byteLen >> 8);
        bout.write(0xFF & byteLen);
        for (int i = 8*(byteLen-1); i >= 0; i -= 8)
            bout.write((int) (0xFF & num >> i));
    }

    /** Create a Document from an encrypted Document-header Storable.
      * Use this to request and decrypt a file from Freenet.
      * 
      * @param cipher     The BlockCipher created according to the
      *                   Symmetric-cipher given in the Storables.
      * @param cryptoKey  The crypto key expected to decrypt the file.
      * @param header     The encrypted Document-header after hex-decoding.
      */
    public Document(BlockCipher cipher, byte[] cryptoKey, byte[] header)
           throws DataNotValidIOException {
                      
        // check power of 2 requirement
        if (header.length != 1 << Util.log2(header.length))
            throw new DataNotValidIOException(DOC_BAD_HEADER);

        this.header    = header;
        this.cryptoKey = cryptoKey;

        cipher.initialize(cryptoKey);
        // as of now we are leaving the IV initialized to all zeroes
        ctx = new PCFBMode(cipher);
        
        // decode document header
        Vector fields = new Vector();
        try {
            int i = 0;
            while (i < header.length) {
                int len = ((byte) ctx.decipher(header[i++])) << 8 | (byte) ctx.decipher(header[i++]);
                if (len <= 0) break;
                byte[] f = new byte[len];
                for (int j=0; j<len; ++j)
                    f[j] = (byte) ctx.decipher(header[i++]);
                fields.addElement(f);
            }
            // need to run through the encrypted padding too
            while (i < header.length) ctx.decipher(header[i++]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new DataNotValidIOException(DOC_BAD_HEADER);
        }
        
        byte[] tmp;

        // check crypto key
        if (fields.size() < 1)
            throw new DataNotValidIOException(DOC_BAD_KEY);
        tmp = (byte []) fields.elementAt(0);
        if (tmp == null || !Util.byteArrayEqual(tmp, Util.hashBytes(new SHA1(), cryptoKey))) // FIXME: static method to do hash of short stretch?
            throw new DataNotValidIOException(DOC_BAD_KEY);
        
        if (fields.size() < 3)
            throw new DataNotValidIOException(DOC_BAD_HEADER);
        
        // read and check length
        tmp = (byte []) fields.elementAt(1);
        if (tmp == null)
            throw new DataNotValidIOException(DOC_BAD_HEADER);
        length = (new BigInteger(1, tmp)).longValue();
        if (length <= 0 || Util.log2(length) > Key.LOG2_MAXSIZE
                        || fieldSize(length) != tmp.length)
            throw new DataNotValidIOException(DOC_BAD_HEADER);
        
        // read and check metadata-length
        tmp = (byte []) fields.elementAt(2);
        if (tmp == null)
            throw new DataNotValidIOException(DOC_BAD_HEADER);
        metaLength = (new BigInteger(1, tmp)).longValue();
        if (metaLength < 0 || metaLength > length
                           || fieldSize(metaLength) != tmp.length)
            throw new DataNotValidIOException(DOC_BAD_HEADER);
    }

    /** @return the encrypted bytes of the Document-header
      */
    public byte[] getDocumentHeader() {
        return header;
    }
    
    public boolean hasMetadata() {
        return metaLength != 0;
    }

    /** @return the _total_ length, metadata + data
      */
    public long length() {
        return length;
    }

    /** @return the metadata length
      */
    public long metadataLength() {
        return metaLength;
    }

    /** @return the total length less the metadata length
      */
    public long dataLength() {
        return length - metaLength;
    }

    /** Use the Document to decrypt a stream.
      * @param out  the OutputStream to which the decrypted bytes will be written
      * @return     an OutputStream to which the encrypted data may be written
      * @see decipheringInputStream()
      */
    public OutputStream decipheringOutputStream(OutputStream out) throws IOException {
        return new DecipherOutputStream(out, ctx);
    }

    /** Use the Document to encrypt a stream.
      * It is the user's responsibility to write enough padding bytes to
      * reach the first power of 2 size greater than or equal to length().
      * Padding must be done by repeating the data exactly.
      * @param out     an OutputStream to which the encrypted bytes will be written
      * @return        an OutputStream to which the plaintext bytes may be written
      * @see encipheringInputStream()
      */
    public OutputStream encipheringOutputStream(OutputStream out) throws IOException {
        return new CipherOutputStream(ctx, out);
    }

    /** Use the Document to decrypt a stream.
      * @param in  the source containing the encrypted bytes
      * @return    an InputStream from which the decrypted data may be read
      * @see decipheringOutputStream()
      */
    public InputStream decipheringInputStream(InputStream in) throws IOException {
        return new CipherInputStream(ctx, in);
    }

    /** Use the Document to encrypt a stream.
      * It is the user's responsibility to ensure that `in' will provide enough
      * padding bytes to reach the first power of 2 size greater than or equal
      * to length().  Padding must be done by repeating the data exactly.
      * @param in  the source containing the plaintext bytes
      * @return    an InputStream from which the encrypted data may be read
      * @see encipheringOutputStream()
      */
    public InputStream encipheringInputStream(InputStream in) throws IOException {
        return new EncipherInputStream(in, ctx);
    }
    

/*
    public static void main(String[] args) throws Exception {
        SHA1 ctx=new SHA1();
        Twofish c=new Twofish();
        byte[] key=new byte[c.getKeySize()>>3];
        File f=new File(args[1]);
        
        if (args[0].equals("create")) {
            byte[] hash=Util.hashFile(ctx, f);
            System.err.println(Fields.bytesToHex(hash,0,key.length));
            Util.makeKey(hash, key, 0, key.length);
            System.err.println(Fields.bytesToHex(key,0,key.length));

            FileInputStream in=new FileInputStream(f);
            Document d=new Document(in, key);
            InputStream i=d.toInputStream(c);
            byte[] buffer=new byte[65536];
            int rc=0;
            OutputStream out=System.out;

            do {
                rc=i.read(buffer, 0, buffer.length);
                if (rc>0) {
                    out.write(buffer, 0, rc);
                }
            } while (rc!=-1);
            out.flush();            
        } else if (args[0].equals("read")) {
            Fields.hexToBytes(args[2],key,0);
            c.initialize(key);
            Document d=read(new FileInputStream(f), key, c, 0, 0);
            d.writeData(System.out);
        } else if (args[0].equals("encipher")) {
            Util.makeKey(Util.hashFile(ctx, f), key, 0, key.length);
            System.err.println(Fields.bytesToHex(key,0,key.length));

            FileInputStream in=new FileInputStream(f);
            Document d=new Document(in, key);
            InputStream inf=d.toInputStream(c);
            int rc=0;
            while (rc!=-1) {
                rc=inf.read();
                if (rc!=-1) System.out.write(rc);
            }
            System.out.flush();
        }            
    }
*/

}



