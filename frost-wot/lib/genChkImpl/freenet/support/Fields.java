package freenet.support;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * This class contains static methods used for parsing 
 * boolean and unsigned long fields in Freenet messages.
 *
 * Also some general utility methods for dealing with string and numeric data.
 *
 * @author oskar
 */

public abstract class Fields {
	
	private static Logger logger = Logger.getLogger(Fields.class.getName());

    public static void main(String[] args) {
        System.out.println(Long.toHexString(stringToLong(args[0])));
        System.out.println(Long.toHexString(Long.parseLong(args[0], 16)));
    }

    /** @deprecated */
    public static final long stringToLong(String hex) throws NumberFormatException {
        return hexToLong(hex);
    }

    /** @deprecated */
    public static final String longToString(long l) {
        return longToHex(l);
    }

    /** Converts a hex string into a long.
      * 
      * Long.parseLong(hex, 16) assumes the input is nonnegative unless there
      * is a preceding minus sign.  This method reads the input as twos
      * complement instead, so if the input is 8 bytes long, it will
      * correctly restore a negative long produced by Long.toHexString().
      *
      * @param hex  A string in capital or lower case hex,
      *             of no more then 16 characters.
      * @throws NumberFormatException if the string is more than 16 characters
      *                               long, or if any character is not in the set
      *                               [0-9a-fA-f]
      */
    public static final long hexToLong(String hex) throws NumberFormatException {
        int len = hex.length();
        if (len > 16)
            throw new NumberFormatException();
 
        long l = 0;
        for (int i=0; i<len; i++) {
            l<<=4;
            int c = Character.digit(hex.charAt(i), 16);
            if (c < 0)
                throw new NumberFormatException();
            l |= c;
        }
        return l;
    }

    /** Converts a hex string into an int.
      * 
      * Integer.parseInt(hex, 16) assumes the input is nonnegative unless there
      * is a preceding minus sign.  This method reads the input as twos
      * complement instead, so if the input is 8 bytes long, it will
      * correctly restore a negative long produced by Long.toHexString().
      *
      * @param hex  A string in capital or lower case hex,
      *             of no more then 16 characters.
      * @throws NumberFormatException if the string is more than 16 characters
      *                               long, or if any character is not in the set
      *                               [0-9a-fA-f]
      */
    public static final int hexToInt(String hex) throws NumberFormatException {
        int len = hex.length();
        if (len > 16)
            throw new NumberFormatException();
 
        int l = 0;
        for (int i=0; i<len; i++) {
            l<<=4;
            int c = Character.digit(hex.charAt(i), 16);
            if (c < 0)
                throw new NumberFormatException();
            l |= c;
        }
        return l;
    }

    /**
     * Converts a long into a hex String.
     * Equivalent to Long.toHexString(), but faster?
     *
     * @param l the long value to convert.
     * @return A hex String.
     */
    public static final String longToHex(long l) {
        StringBuffer sb = new StringBuffer(17);
	longToHex(l, sb);
        return sb.toString();
    }
    
    /**
     * Converts a long into characters in a StringBuffer.
     * @param l the long value to convert.
     * @param sb the StringBuffer in which to place the output.
     * Note that if there is existing data in the StringBuffer,
     * it will end up reversed and after the output!
     */
    public static final void longToHex(long l, StringBuffer sb) {
        do {
            sb.append(Character.forDigit((int) l & 0xf, 16));
            /* Doing it manually makes this method about 4.8% faster here
               which I don't think justifies it...
            int i = (int) (l & 0xf);
            if (i < 10)
                sb.append((char) (i + '0'));
            else
                sb.append((char) (i + ('a' - 10)));
            */
            l >>>= 4;
            
        } while (l != 0);
	sb.reverse();
    }
    
    /**
     * Converts an int into a hex String.
     * Equivalent to Integer.toHexString(), but faster?
     *
     * @param l the int value to convert.
     * @return A hex String.
     */
    public static final String intToHex(int l) {
        StringBuffer sb = new StringBuffer(9);
        do {
            sb.append(Character.forDigit(l & 0xf, 16));
            /* Doing it manually makes this method about 4.8% faster here
               which I don't think justifies it...
            int i = (int) (l & 0xf);
            if (i < 10)
                sb.append((char) (i + '0'));
            else
                sb.append((char) (i + ('a' - 10)));
            */
            l >>>= 4;
            
        } while (l != 0);
        return sb.reverse().toString();
    }


    /**
     * Finds the boolean value of the field, by doing a caseless match
     * with the strings "true" and "false".
     * @param s   The string
     * @param def The default value if the string can't be parsed. If the default
     *            is true, it checks that the
     *            string is not "false"; if it is false, it checks whether
     *            the string is "true".
     * @return the boolean field value or the default value if the field value
     * couldn't be parsed.
     */
    /* wooo, rocket science! (this is purely abstraction people) */
    public static final boolean stringToBool(String s, boolean def) {
        return (def ? 
                !s.equalsIgnoreCase("false") : 
                s.equalsIgnoreCase("true"));
    }

    /**
     * Converts a boolean to a String of either "true" or "false".
     *
     * @param b the boolean value to convert.
     * @return A "true" or "false" String.
     */
    public static final String boolToString(boolean b) {
        return b ? "true" : "false";
    }

    public static final byte[] hexToBytes(String s) {
        return hexToBytes(s, 0);
    }

    public static final byte[] hexToBytes(String s, int off) {
        byte[] bs = new byte[off + (1 + s.length()) / 2];
        hexToBytes(s, bs, off);
        return bs;
    }

    /**
     * Converts a String of hex characters into an array of bytes.
     * @param s A string of hex characters (upper case or lower) of
     *          even length.
     * @param out  A byte array of length at least s.length()/2 + off
     * @param off  The first byte to write of the array
     */
    public static final void hexToBytes(String s, byte[] out, int off) 
        throws NumberFormatException, IndexOutOfBoundsException {

        if ((s.length() % 2) != 0)
            s = '0' + s;
            
        if (out.length < off + s.length()/2)
            throw new IndexOutOfBoundsException();
            
        byte b;
        for (int i=0; i < s.length(); i++) {
            /* Why were we doing this manually?
            char c = s.charAt(i);
            if (!((c >= 'A' && c <= 'F') ||
                  (c >= 'a' && c <= 'f') ||
                  (c >= '0' && c <='9')))
                throw new NumberFormatException("Unrecognized symbol");
            b = (byte) (c >= 'A' && c <='F' ? 
                        c - 'A' + 10 : 
                        (c >= 'a' && c <= 'f' ? 
                         c - 'a' + 10 :
                         c - '0'));
            */
            b = (byte) Character.digit(s.charAt(i), 16);
            if (b < 0)
                throw new NumberFormatException();
            if (i%2 == 0) {
                out[off + i/2] = (byte) (b << 4);
            } else {
                out[off + i/2] = (byte) (out[off + i/2] | b);
            }
        }
    }
    
    /**
     * Converts a byte array into a string of upper case hex chars.
     * @param bs      A byte array
     * @param offset  The index of the first byte to read
     * @param length  The number of bytes to read.
     * @return the string of hex chars.
     */
    public static final String bytesToHex(byte[] bs, int off, int length) {

        StringBuffer sb = new StringBuffer(length * 2);
	bytesToHexAppend(bs, off, length, sb);
        return sb.toString();
    }
    
    public static final void bytesToHexAppend(byte[] bs, int off, int length,
					      StringBuffer sb) {
	for (int i = off; i < (off + length) && i < bs.length; i++) {
            sb.append(Character.forDigit((bs[i] >>> 4) & 0xf, 16));
            sb.append(Character.forDigit(bs[i] & 0xf, 16));
            /*
	      char c1, c2;
	      
	      c1 = (char) ((bs[i] >>> 4) & 0xf);
	      c2 = (char) (bs[i] & 0xf);
	      c1 = (char) ((c1 > 9) ? 'a' + (c1 - 10) : '0' + c1);
	      c2 = (char) ((c2 > 9) ? 'a' + (c2 - 10) : '0' + c2);
	      sb.append(c1);
	      sb.append(c2);
            */
        }
    }
    
    public static final String bytesToHex(byte[] bs) {
        return bytesToHex(bs, 0, bs.length);
    }


    public static final String[] commaList(String ls) {
        StringTokenizer st = new StringTokenizer(ls, ",");
        String[] r = new String[st.countTokens()];
        for (int i = 0 ; i < r.length ; i++) {
            r[i] = st.nextToken().trim();
        }
        return r;
    }

    public static final String commaList(String[] ls) {
        return textList(ls, ',');
    }

    public static final String textList(String[] ls, char ch) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < ls.length ; i++) {
            sb.append(ls[i]);
            if (i != ls.length - 1)
                sb.append(ch);
        }
        return sb.toString();
    }

    public static final long[] numberList(String ls) throws NumberFormatException {
        StringTokenizer st = new StringTokenizer(ls, ",");
        long[] r = new long[st.countTokens()];
        for (int i = 0 ; i < r.length ; i++) {
            r[i] = stringToLong(st.nextToken());
        }
        return r;
    }

    public static final String numberList(long[] ls) {
        StringBuffer sb = new StringBuffer(ls.length*18);
        for (int i = 0 ; i < ls.length ; i++) {
            sb.append(longToString(ls[i]));
            if (i != ls.length - 1)
                sb.append(',');
        }
        return sb.toString();
    }

    /**
     * Parses a time and date value, using a very strict format.
     * The value has to be of the form YYYYMMDD-HH:MM:SS (where seconds
     * may include a decimal) or YYYYMMDD (in which case 00:00:00 is assumed
     * for time).
     * @returns millis of the epoch of at the time described.
     */
    public static final long dateTime(String date) 
        throws NumberFormatException {

        int dash = date.indexOf('-');


        if (!(dash == -1 && date.length() == 8) &&
            !(dash == 8 && date.length() == 17))
            throw new NumberFormatException("Date time: " + date + " not correct.");
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));

        int hour = dash == -1 ? 0 : Integer.parseInt(date.substring(9, 11));
        int minute = dash == -1 ? 0 : Integer.parseInt(date.substring(12, 14));
        int second = dash == -1 ? 0 : Integer.parseInt(date.substring(15, 17));
        
        // Note that month is zero based in GregorianCalender!
        try {
            return (new GregorianCalendar(year, month - 1, day, hour, 
                                          minute, second)).getTime().getTime();
        } catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in dateTime(String date)", e);
            // The API docs don't say which exception is thrown on bad numbers!
            throw new NumberFormatException("Invalid date " + date + ": " +
                                            e);
        }
            
    }

   
    public static final String secToDateTime(long time) {
        //Calendar c = Calendar.getInstance();
        //c.setTime(new Date(time));
        //gc.setTimeInMillis(time*1000);
       
        DateFormat f = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        //String dateString = f.format(c.getTime());
        String dateString = f.format(new Date(time*1000));
       
        if(dateString.endsWith("-00:00:00"))
	  dateString = dateString.substring(0,8);
       
        return dateString;
    }

    public static final int compareBytes(byte[] b1, byte[] b2) {
        int len = Math.max(b1.length, b2.length);
        for (int i=0; i<len; ++i) {
            if (i == b1.length)
                return i == b2.length ? 0 : -1;
            else if (i == b2.length)
                return 1;
            else if ((0xff & (int) b1[i]) > (0xff & (int) b2[i]))
                return 1;
            else if ((0xff & (int) b1[i]) < (0xff & (int) b2[i]))
                return -1;
        }
        return 0;
    }

    public static final int compareBytes(byte[] a, byte[] b,
                                         int aoff, int boff, int len) {
        for (int i=0; i<len; ++i) {
            if (i+aoff == a.length)
                return i+boff == b.length ? 0 : -1;
            else if (i+boff == b.length)
                return 1;
            else if ((0xff & (int) a[i+aoff]) > (0xff & (int) b[i+boff]))
                return 1;
            else if ((0xff & (int) a[i+aoff]) < (0xff & (int) b[i+boff]))
                return -1;
        }
        return 0;
    }

    public static final boolean byteArrayEqual(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        for (int i=0; i<a.length; ++i)
            if (a[i] != b[i]) return false;
        return true;
    }

    public static final boolean byteArrayEqual(byte[] a, byte[] b,
                                               int aoff, int boff, int len) {
        if (a.length < aoff+len || b.length < boff+len)
            return false;
        for (int i=0; i<len; ++i) 
            if (a[i+aoff] != b[i+boff]) return false;
        return true;
    }


    /**
     * We need this because java 1.1 has no notion
     * that Strings implement Comparable.
     */
    public static final class StringComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            return compare((String) o1, (String) o2);
        }
        public static final int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }    

    /**
     * Orders Strings by length:
     * ascending order is from shortest to longest
     */
    public static final class StringLengthComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            return compare((String) o1, (String) o2);
        }
        public static final int compare(String o1, String o2) {
            return o1.length() == o2.length()
                   ? 0 : (o1.length() > o2.length() ? 1 : -1);
        }
    }
    

    /**
     * Compares byte arrays lexicographically.
     */
    public static final class ByteArrayComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            return compare((byte[]) o1, (byte[]) o2);
        }
        public static final int compare(byte[] o1, byte[] o2) {
            return compareBytes(o1, o2);
        }
    }

    // could add stuff like IntegerComparator, LongComparator etc.
    // if we need it
    
    /**
     * A generic hashcode suited for byte arrays that are
     * more or less random.
     */
    public static final int hashCode(byte[] b) {
        int h = 0;
        for (int i = b.length-1; i >= 0; --i) {
	    int x = ((int)b[i]) & 0xff;
	    h ^= x << ((i & 3) << 3);
	}
        return h;
    }
    
    /**
     * Long version of above
     * Not believed to be secure in any sense of the word :)
     */
    public static final long longHashCode(byte[] b) {
	long h = 0;
	for (int i = b.length-1; i >= 0; --i) {
	    int x = ((int)b[i]) & 0xff;
	    h ^= ((long)x) << ((i & 7) << 3);
	}
	return h;
    }

}


