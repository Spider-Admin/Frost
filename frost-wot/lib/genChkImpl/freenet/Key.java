package freenet;

import java.io.InputStream;
import java.lang.reflect.*;
import java.util.Hashtable;

import freenet.crypt.*;
import freenet.support.*;
import freenet.support.io.*;

/**
 * A base implementation of all keys. Used as superclass for other types,
 * and class for unknown keytypes.
 *
 * @author <A HREF="mailto:I.Clarke@strs.co.uk">Ian Clarke</A>
 * @author oskar (total rewrite)
 * @author tavin (added more test code + cleaned up comparison methods)
 */
public class Key implements Measurable { //Cloneable {

    public static final Key NIL = new Key(new byte[0]);

    /**
     * The largest acceptable size for the data of a key,
     * after padding but prior to the addition of control bytes.
     * Currently, 2^62 because that is the largest power of 2
     * that can be stored in a long.
     */
    public static final int LOG2_MAXSIZE = 62,
                            LOG2_MINSIZE = 10;

    private static final Hashtable keytypes = new Hashtable();

    /**
     * Registers a key by its type number.
     */
    public static void addKeyType(int typen, Class c) throws KeyException {
        if (!Key.class.isAssignableFrom(c)) {
            throw new KeyException("Not a subclass of Key");
        }

        Constructor con;

        try {
            con = c.getConstructor(new Class[] {byte[].class});
            keytypes.put(new Integer(typen), con);
        }
        catch (NoSuchMethodException e) {
            throw new KeyException("Not constructable from a byte array");
        }
    }

    /**
     * Returns an instance of a key given as a string in the format
     * &lt;type&gt;/&lt;value&gt; by looking for a class with the type
     * name in Freenet.keys
     */
    public static Key readKey(String s) throws KeyException {
        byte[] keyval;
        try {
            keyval = Fields.hexToBytes(s);
        }
        catch (RuntimeException e) {
            throw new KeyException("Key string is not legal hexadecimal");
        }
        return readKey(keyval);
    }

    /**
     * Returns an instance of a key given as a raw array of bytes
     * by looking for a class with the type name in Freenet.keys
     */
    public static Key readKey(byte[] keyval) throws KeyException {

        Integer type = new Integer(
            keyval[keyval.length-2] << 8 | keyval[keyval.length - 1] );

        Constructor con = (Constructor) keytypes.get(type);
        if (con != null) {
            try {
                return (Key) con.newInstance(new Object[] {keyval});
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException() instanceof KeyException
                      ? (KeyException) e.getTargetException()
                      : new KeyException(e.getTargetException().toString());
            }
            catch (Exception e) {
                throw new KeyException(e.toString());
            }
        }
        else {
            if (type.intValue() != 0) {
/*                Core.logger.log(Key.class, "Got unknown key type : " +
                                Integer.toHexString(type.intValue()), Logger.MINOR);*/
            }
            return new Key(keyval);
        }
    }

    /**
     * @return the number of control bytes following each part, when
     *         the key is streamed in the trailing field of a message
     */
    public static final int getControlLength() {
        return 21;
    }

    /**
     * @return the total length of the trailing field, with control bytes,
     *         given the length of the data (which should be a power of 2)
     */
    public static final long getTransmissionLength(long dataLength, long partSize) {
        if (dataLength < 0 || partSize <= 0)
            return 0;
        int parts     = (int) ((dataLength-1) / partSize);
        long lastpart = dataLength - parts * partSize;
        return parts * (partSize + getControlLength()) + lastpart + 1;
    }

    /**
     * @return the length of the data, without control bytes, given the
     *         total length of the trailing field
     */
    public static final long getDataLength(long transLength, long partSize) {
        if (transLength < 0 || partSize < 0)
            return 0;
        int parts     = (int) (transLength / (partSize + getControlLength()));
        long lastpart = transLength - parts * (partSize + getControlLength());
        return parts * partSize + lastpart - 1;
    }

    /**
     * @return the correct Part-size to use for data
     *         of the given (padded) length
     */
    public static final long getPartSize(long dataLength) {
        return Math.max( dataLength >> 7, Math.min(dataLength, 16384) );
    }


    /**
     * @return  the expected trailing-field length based on
     *          the size byte
     */
    public final long getExpectedTransmissionLength() {
        long dlen = getExpectedDataLength();
        return dlen == -1 ? -1 : getTransmissionLength(dlen, getPartSize(dlen));
    }

    /**
     * @return  the expected data length (after padding, no control bytes)
     *          based on the size byte
     */
    public final long getExpectedDataLength() {
        return val.length-3 >= 0 ? 1 << val[val.length-3] : -1;
    }




    protected byte[] val;

    /**
     * General constructor - just uses the byte array as given.
     */
    public Key(byte[] val) {
        this.val = val;
    }

    /**
     * Makes a copy of the byte array and adds the end bytes
     * for size and key number.
     */
    public Key(byte[] val, int log2size, int keyNumber) {
        this(val.length, log2size, keyNumber);
        System.arraycopy(val, 0, this.val, 0, val.length);
    }

    /**
     * Leaves the value uninitialized but fills in the last 3 bytes.
     * @param valueLen   the key length without the 3 end bytes
     * @param log2size   the 1st end byte
     * @param keyNumber  the last 2 end bytes
     */
    protected Key(int valueLen, int log2size, int keyNumber) {
        val = new byte[valueLen + 3];
        val[val.length-3] = (byte) (0xFF & log2size);
        val[val.length-2] = (byte) (0xFF & keyNumber >> 8);
        val[val.length-1] = (byte) (0xFF & keyNumber);
    }

    /**
     * Reads s as the string in hex format.
     */
    public Key(String s) throws NumberFormatException {
        this(Fields.hexToBytes(s));
    }




    /**
     * Wraps a verifying stream around the data stream for a key, so the
     * data is verified to belong to the key
     *
     * Subclasses should override this, and make other checks before
     * calling this routine, such as checking the validity of the routing
     * key itself, and the validity of the storables.
     *
     * @param data        The data stream.
     * @param storables   Node visable metadata fields.
     * @param transLength The length of data that should be verified.
     */
    public VerifyingInputStream verifyStream(InputStream data,
                                             Storables storables,
                                             long transLength)
                                   throws DataNotValidIOException {

        return new ProgressiveHashInputStream( data, storables.getPartSize(),
                                               transLength, SHA1.getInstance(),
                                               storables.getInitialDigest() );
    }




    /**
     * Utility method used to implement Comparable / Measurable
     */
    private static final int at(byte[] b, int i) {
        return (i < b.length ? b[i] : 0) & 0xff;
    }



    /**
     * Implements the Comparable interface.
     * Returns -1 if this is less than o, 1 if this is greater than o,
     * and 0 if they are equal.
     *
     * @param o the key Object to compare to
     * @return integer <0, >0, or ==0
     * @exception ClassCastException thrown if !(o instanceof Key)
     */
    public final int compareTo(Object o) {
        return compareTo((Key) o);
    }

    public final int compareTo(Key k) {
        int len = Math.max(val.length, k.val.length);
        for (int i=0; i<len; ++i) {
            if (at(val, i) < at(k.val, i))
                return -1;
            if (at(val, i) > at(k.val, i))
                return 1;
        }
        return 0;
    }




    /**
     * Implements the Measurable interface.
     */
    public final int compareTo(Object A, Object B) {
        return compareTo((Key) A, (Key) B);
    }

    public final int compareTo(Key A, Key B) {
        int ABcmp = A.compareTo(B);
        if (ABcmp < 0) {
            if (compareTo(B) > 0) return 1;
            if (compareTo(A) > 0) return compareToSorted(A, B);
            else                  return -1;
        }
        else if (ABcmp > 0) {
            if (compareTo(A) > 0) return -1;
            if (compareTo(B) > 0) return -1 * compareToSorted(B, A);
            else                  return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * Implements the Measurable interface.
     */
    public final int compareToSorted(Object A, Object B) {
        return compareToSorted((Key) A, (Key) B);
    }

    /**
     * Given that this Key is between Key A and Key B, determines
     * whether this key is closer to A, B, or equidistant.
     * @return   0 if equidistant
     *          >0 if the distance to A is greater
     *          <0 if the distance to B is greater
     */
    public final int compareToSorted(Key A, Key B) {
        int len = Math.max(val.length, Math.max(A.val.length, B.val.length));
        int diff = 0;
        for (int i=0; i<len; ++i) {
            diff += (at(val,i) - at(A.val,i)) - (at(B.val,i) - at(val,i));
            if (diff < -1) return -1;
            if (diff > 1)  return 1;
            diff *= 0x100;
        }
        return diff;
    }







    public final boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

    /**
     * For the hashcode, I'll just return the last four numbers
     * no, I guess I won't, Scott complained.
     */
    public final int hashCode() {
        return Fields.hashCode(val);
    }

    /**
     * Returns the Key as a hex string.
     */
    public final String toString() {
        return Fields.bytesToHex(val);
    }

    /**
     * Returns the byte value of the key
     */
    public final byte[] getVal() {
        return val;
    }

    /**
     * Returns the byte length of the key
     */
    public final int length() {
        return val.length;
    }




    /** Test code */
    public static void main(String[] args) {
        System.out.println("Until we figure out a standardized testing system,");
        System.out.println("edit the source and uncomment whatever you want to test.");
/*
        Key A = new Key(Fields.hexToBytes("1000000000000000000000000000000000000000000001",0));
        Key B = new Key(Fields.hexToBytes("2000000000000000000000000000000000000000000001",0));
        Key C = new Key(Fields.hexToBytes("3000000000000000000000000000000000000000000000",0));

        System.out.println("A is            == " + A.toString());
        System.out.println("B is            == " + B.toString());
        System.out.println("C is            == " + C.toString());

        System.out.println("B - A           == " + B.absoluteDistance(A));
        System.out.println("B - C           == " + B.absoluteDistance(C));
        System.out.println("B.compare(A, C) == " + B.compare(A, C));

        System.out.println("");

        Key a = new Key(Fields.hexToBytes("0657fbed1bf63058ed2cc2cab29a748a295285f7100201",0));
        Key self = new Key(Fields.hexToBytes("06bacd71abb51d0040714394876204669aac607c0f0301",0));
        Key b = new Key(Fields.hexToBytes("0658fa50f6863ed5b9a7c032a645642d676829c5090301",0));
        Key c = new Key(Fields.hexToBytes("065941a761ef6a12278cac93a16af770700df9d9100201",0));

        System.out.println("self is:              " + self.toString());
        System.out.println("   a is:              " + a.toString());
        System.out.println("   b is:              " + b.toString());
        System.out.println("   c is:              " + c.toString());

        System.out.println("self.compare(c, b) == " + self.compare(c, b));
        System.out.println("self - c           == " + self.absoluteDistance(c));
        System.out.println("self - b           == " + self.absoluteDistance(b));

        System.out.println("self.compare(b, a): " + self.compare(b, a));
        System.out.println("self - b           == " + self.absoluteDistance(b));
        System.out.println("self - a           == " + self.absoluteDistance(a));

        System.out.println("self.compare(a, c): " + self.compare(a, c));
        System.out.println("self - a           == " + self.absoluteDistance(a));
        System.out.println("self - c           == " + self.absoluteDistance(c));

        System.out.println("self.compare(a, b): " + self.compare(a, b));
        System.out.println("self - a           == " + self.absoluteDistance(a));
        System.out.println("self - b           == " + self.absoluteDistance(b));

        System.out.println("self.compare(a, a): " + self.compare(a, a));
        System.out.println("self - a           == " + self.absoluteDistance(a));
*/
    }
}




