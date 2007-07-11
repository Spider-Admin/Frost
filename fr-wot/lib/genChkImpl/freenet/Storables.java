package freenet;

import java.math.BigInteger;
import java.util.Random;

import freenet.crypt.*;
import freenet.support.Fields;

/** A simple wrapper around a FieldSet for dealing with the Storable.*
  * fields in a Freenet message.
  */
public class Storables extends FieldSet {

    public static final String PREFIX = "Storable";

    public Storables() {
        super();
    }

    private Storables(FieldSet sto) {
        super(sto);
    }

    public static Storables readFrom(FieldSet fs) {
        FieldSet sto = fs.getSet(PREFIX);
        return sto == null ? null : new Storables(sto);
    }

    public void addTo(FieldSet fs) {
        fs.put(PREFIX, this);
    }
    
    /** Checks whether the Storables are legal for a CHK.
      * The following fields, and no others, must be present:
      *     Part-size
      *     Initial-digest
      *     Symmetric-cipher
      *     Document-header
      * Part-size must be greater than zero.
      */
    public boolean isLegalForCHK() {
        return size()               == 4
            && getPartSize()         > 0
            && getInitialDigest()   != null
            && getSymmetricCipher() != null
            && getDocumentHeader()  != null;
    }

    /** Checks whether the Storables are legal for a SVK or derivative.
      * The following fields, and no others, must be present:
      *     Part-size
      *     Initial-digest
      *     Symmetric-cipher
      *     Document-header
      *     Public-key
      *     Document-name (optional)
      *     Signature
      * Part-size must be greater than zero.
      */
    public boolean isLegalForSVK() {
        if (size() == 6)
            {}
        else if (size() != 7 || getDocumentName() == null)
            return false;
        return getPartSize()         > 0
            && getInitialDigest()   != null
            && getSymmetricCipher() != null
            && getDocumentHeader()  != null
            && getPublicKey()       != null
            && getSignature()       != null;
    }
    
    /** Calculates and stores the Public-key and Signature
      * fields in the Storables.
      */
    public void sign(Random r, DSAPrivateKey sk, DSAGroup g) {
        setPublicKey(new DSAPublicKey(g, sk));
        Digest ctx = SHA1.getInstance();
        hashUpdate(ctx, new String[] {"Signature"});
        BigInteger k = Util.generateLargeRandom(80, 160, r);
        BigInteger m = Util.byteArrayToMPI(ctx.digest());
        setSignature(DSA.sign(g, sk, k, m));
    }

    /** Checks the Signature using the Public-key.
      * This may throw NPEs if you haven't checked isLegalSVK() first.
      */
    public boolean verifies() {
        Digest ctx = SHA1.getInstance();
        hashUpdate(ctx, new String[] {"Signature"});
        BigInteger m = Util.byteArrayToMPI(ctx.digest());
        return DSA.verify(getPublicKey(), getSignature(), m);
    }
    
    
    //=======================================================================//
    // the remainder of these methods are tailored get()/set()               //
    // pairs for the various fields                                          //
    //=======================================================================//

    /** @return the part size, or zero
      */
    public long getPartSize() {
        try {
            return Math.max(0, Long.parseLong(get("Part-size"), 16));
        }
        catch (NumberFormatException e) {}
        return 0;
    }

    /** Store the part size, or zero.
      */
    public void setPartSize(long partSize) {
        put("Part-size", Long.toHexString(Math.max(0, partSize)));
    }
    
    /** @return the digest of the first part as a byte array
      */
    public byte[] getInitialDigest() {
        String s = get("Initial-digest");
        return s == null ? null : Fields.hexToBytes(s);
    }

    /** Store the digest of the first part.
      */
    public void setInitialDigest(byte[] digest) {
        put("Initial-digest", Fields.bytesToHex(digest));
    }

    /** @return the string name of the symmetric cipher used for the document
      */
    public String getSymmetricCipher() {
        return get("Symmetric-cipher");
    }

    /** Store the string name of the symmetric cipher used for the document.
      */
    public void setSymmetricCipher(String cipher) {
        put("Symmetric-cipher", cipher);
    }

    /** @return the encrypted header data as a byte array
      */
    public byte[] getDocumentHeader() {
        String s = get("Document-header");
        return s == null ? null : Fields.hexToBytes(s);
    }

    /** Store the encrypted document header.
      */
    public void setDocumentHeader(byte[] header) {
        put("Document-header", Fields.bytesToHex(header));
    }

    private static final String[] ypqgS = {"y","p","q","g"};
    /** @return the signing public key
      */
    public DSAPublicKey getPublicKey() {
        FieldSet fs = getSet("Public-key");
        if (fs == null) return null;
        BigInteger[] ypqg = new BigInteger[4];
        for (int i = 0 ; i < 4 ; i++) {
            String n = fs.get(ypqgS[i]);
            if (n == null)
                return null;
            ypqg[i] = new BigInteger(n, 16);
        }

        return new DSAPublicKey(new DSAGroup(ypqg[1], ypqg[2],
                                             ypqg[3]), ypqg[0]);
    }

    /** Store the signing public key.
      */
    public void setPublicKey(DSAPublicKey pk) {
        FieldSet fs = new FieldSet();
        fs.put("y", pk.getY().toString(16));
        fs.put("p", pk.getP().toString(16));
        fs.put("q", pk.getQ().toString(16));
        fs.put("g", pk.getG().toString(16));
        put("Public-key", fs);
    }

    /** @return the hash of the document name as a byte array.
      */
    public byte[] getDocumentName() {
        String s = get("Document-name");
        return s == null ? null : Fields.hexToBytes(s);
    }

    /** Store the hash of the document's name.
      */
    public void setDocumentName(byte[] hash) {
        put("Document-name", Fields.bytesToHex(hash));
    }

    /** @return the DSA signature
      */
    public DSASignature getSignature() {
        String s = get("Signature");
        return s == null ? null : new DSASignature(s);
    }

    /** Store the DSA signature.
      */
    public void setSignature(DSASignature sig) {
        put("Signature", sig.toString());
    }

}



