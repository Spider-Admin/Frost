package freenet.support.io;
import java.io.*;

/**
 * This reader wraps around a stream and provides the methods necessary
 * to efficiently read a Freenet message. Unlike a BufferedReader this does 
 * not  buffer the stream at all, so the trailing field is left in tact.
 *
 * This is written for Freenet and as such assumes the characters are UTF8
 * encoded.
 * @author oskar
 **/

public class ReadInputStream extends FilterInputStream {

    public static void main(String[] args) throws Throwable {
	InputStream is = new FileInputStream(args[0]);
	ReadInputStream mr = new ReadInputStream(is);
	System.out.print((char)mr.read());
	System.out.print((char)is.read());

	for (int i = 0; i < 9 ; i++ )
	    System.out.println(mr.readTo('\n', '\r'));
	for (int i = 0; i < 20 ; i++ )	
	    System.out.print((char)is.read());
    }

    /** The maximum number of chars to allow in a readTo**/
    public static int MAX_LENGTH = 4096;

    public ReadInputStream(InputStream i) {
	super(i);
    }

    public String readln() throws IOException, EOFException {
        return readTo('\n', '\r');
    }

    /**
     * Reads until a certain character is found, and returns a string
     * containing the characters up to, but not including, that character,
     * though it is also consumed.
     *
     * This method assumes the stream is UTF8 encoded.
     * @param ends The character to read up until.
     * @return The string read, decoded with UTF8
     * @exception EOFException if the end of the stream is reached, with the
     *                      string as read so far as the comment.
     * @exception IOException  if the end has not been found within MAX_LENGTH
     *                      characters, or if a bad UTF8 encoding is read, or
     *                      some other error condition occurs.
     */
    public String readTo(char ends) throws IOException, EOFException {
	StringBuffer tmp = new StringBuffer();
	char r = ' ';
	int read = 0;
	while (true) {
	    try {
		r = readUTF();
		read++;
	    } catch (EOFException e) {
		throw new EOFException(tmp.toString());
	    }
	    if (r == -1)
		throw new EOFException(tmp.toString());
	    if (r == ends)
		break;
	    if (read > MAX_LENGTH)
		throw new IOException(tmp.toString());
	    tmp.append(r);
	}
	return tmp.toString();
    }

    /**
     * Reads until a certain character is found, and returns a string 
     * containing the characters up to, but not including, that character,
     * though it is also consumed.
     *
     * This method assumes the stream is UTF8 encoded.
     * @param ends      The character to read up until.
     * @param ignore    A character that should be removed if it directly
     *                 precedes the terminating character. (This is for the
     *                 very particular situation of removing \r if it comes
     *                 right before \n in Freenet messages. Not sexy
     *                 but necessary.)
     * @return The string read, decoded with UTF8
     * @throws EOFException if the end of the stream is reached, with the
     *                      string as read so far as the comment.
     **/
    public String readTo(char ends, char ignore) throws IOException, EOFException {
	String s = readTo(ends);
	return s.length() > 0 && s.charAt(s.length() - 1) == ignore ?
	    s.substring(0,s.length() - 1) :
	    s;
    }

    /**
     * Reads until a certain character is found or EOF is encountered
     * or MAX_LENGTH characters are read, and returns a string
     * containing the characters up to, but not including, that
     * character, though it is also consumed.  So if a read is cut
     * short by EOF, the truncated string is returned this time, but
     * EOFException will be thrown on the subsequent call.
     * 
     * This method assumes the stream is UTF8 encoded.
     * @param ends The character to read up until.
     * @return The string read, decoded with UTF8
     * @exception EOFException if the stream is already positioned at EOF
     *                         (i.e. no characters at all are available)
     * @exception IOException  if a bad UTF8 encoding is read, or
     *                         some other error condition occurs.
     **/
    public String readToEOF(char ends) throws IOException, EOFException {
        // 25 seems to be a good value, to reduce memory allocations
	StringBuffer tmp = new StringBuffer(26);
	char r = ' ';
	int read = 0;
	while (true) {
	    try {
		r = readUTF();
		read++;
	    } catch (EOFException e) {
		if (tmp.length() > 0)
		    return tmp.toString();
		else
		    throw e;
	    }
	    if (r == -1) {
		if (tmp.length() > 0)
		    return tmp.toString();
		else
		    throw new EOFException();
	    }
	    if (r == ends) {
		break;
	    }
	    if (read > MAX_LENGTH) {
		if (tmp.length() > 0)
		    return tmp.toString();
		else
		    throw new EOFException();
	    }
	    tmp.append(r);
	}
	return tmp.toString();
    }

    /**
     * Reads until a certain character is found or EOF is encountered
     * or MAX_LENGTH characters are read, and returns a string
     * containing the characters up to, but not including, that
     * character, though it is also consumed.  So if a read is cut
     * short by EOF, the truncated string is returned this time, but
     * EOFException will be thrown on the subsequent call.
     *
     * This method assumes the stream is UTF8 encoded.
     * @param ends      The character to read up until.
     * @param ignore    A character that should be removed if it directly
     *                 precedes the terminating character. (This is for the
     *                 very particular situation of removing \r if it comes
     *                 right before \n in Freenet messages. Not sexy
     *                 but necessary.)
     * @return The string read, decoded with UTF8
     * @throws EOFException if the end of the stream is reached, with the
     *                      string as read so far as the comment.
     **/
    public String readToEOF(char ends, char ignore) throws IOException, EOFException {
	String s = readToEOF(ends);
	return s.length() > 0 && s.charAt(s.length() - 1) == ignore ?
	    s.substring(0,s.length() - 1) :
	    s;
    }

    /**
     * Reads a UTF8 encoded Unicode character off the stream.
     **/

    /* Yes, it would be better to use java's built in encoding
     * to accomplish this rather then hardcoding UTF8, but none
     * of java's readers will work for us as they have a nasty
     * habit of swallowing the entire stream. Another option would
     * be to read the bytes into an array and use the string 
     * constructor that lets you specify an encoding, that is 
     * probably slower, but should provide a fallback
     * if this fucks with you.
     *
     * This code is "inspired" by the UTF8 decoder in the GNU classpath
     * libraries which is (c) Free Software Foundation
     *
     * RCB <autophile@dol.net> sez:
     *
     * There's always DataInputStream, but its docs say that it
     * reads a "slight modification" of UTF-8. Specifically, that
     * the null byte (\u0000) is encoded as two bytes (C0 80) rather
     * than one (00), and that only the one-, two-, and three-byte
     * formats are used (which is all this readUTF method supports
     * anyway).
     *
     * So the questions are: Do you care about the null byte, and does
     * DataInputStream snarf up more bytes than it needs when you
     * decode a single UTF character?
     *
     * oskar sz:
     *
     * DataInputStream.readUTF() expects two bytes denoting the the length
     * and then the actual string. That is not what we want.
     * DataInputStream.readChar() doesn't do UTF at all but 
     * just (char)((a << 8) | (b & 0xff))
     * So no luck there.
     */
    public char readUTF() throws EOFException, IOException {
	int val;
	int b = in.read();
	if (b == -1)
	    throw new EOFException();
	
	if ((b & 0xE0) == 0xE0) { // three bytes
	    val = (b & 0x0F) << 12;

	    if ((b = in.read()) == -1)
		throw new EOFException();
	    if ((b & 0x80) != 0x80)
		throw new UTFDataFormatException("Bad encoding");
	    val |= (b & 0x3F) << 6;

	    if ((b = in.read()) == -1)
		throw new EOFException();
	    if ((b & 0x80) != 0x80)
		throw new UTFDataFormatException("Bad encoding"); 
	    val |= (b & 0x3F);
	} else if ((b & 0xC0) == 0xC0) { // two bytes
	    val = (b & 0x1F) << 6;

	    if ((b = in.read()) == -1)
		throw new EOFException();
	    if ((b & 0x80) != 0x80)
		throw new UTFDataFormatException("Bad encoding");
	    val |= (b & 0x3F);
	} else if (b < 0x80) {// one byte
	    val = b;
	} else {
	    throw new UTFDataFormatException("Bad encoding");
	}

	return (char) val;
    }
}











