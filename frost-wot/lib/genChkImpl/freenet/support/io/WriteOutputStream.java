package freenet.support.io;
//import freenet.Core;
//import freenet.support.Logger;
import java.io.*;
/**
 * An OutputStream for writing to Freenet streams. This is like a simpler,
 * platform constant version of PrintStream (instead of the platforms encoding
 * and newlines, this always uses UTF8 and /n).
 *
 * @author oskar
 **/

public class WriteOutputStream extends FilterOutputStream {

    /**
     * Creates a new WriteOutputStream
     */
    public WriteOutputStream(OutputStream out) {
	super(out);
    }

    /**
     * Writes the UTF bytes of the string to the output.
     * @param s  The String to write.
     */
    public void writeUTF(String s) throws IOException {
	super.write(s.getBytes("UTF8"));
    }

    /**
     * Writes the UTF bytes of the string to the output.
     * @param s     The String to write.
     * @param term  The (line) terminator to write.
     */
    public void writeUTF(String s, char term) throws IOException {
	writeUTF(s);
	super.write(encodeUTF(term));
    }

    /**
     * Writes the UTF bytes of the string to the output.
     * @param s     The String to write.
     * @param pre   The character to write before the terminator.
     * @param term  The (line) terminator to write.
     */
    public void writeUTF(String s, char pre, char term) throws IOException {
	writeUTF(s);
	super.write(encodeUTF(pre));
	super.write(encodeUTF(term));
    }


    /**
     * The same as writeUTF(s) but this swallows the IOException. I'm leaving
     * this since a lot of the code was written for printstream originally.
     */
    public void print(String s) {
	try {
	    writeUTF(s);
	} catch (IOException e) {
//	    Core.logger.log(this, "IOException when printing : " + e,
//			    Logger.DEBUGGING);
	}
    }

    /**
     * The same as writeUTF(s,'\n') but this swallows the IOException. I'm
     * leaving this since a lot of the code was written for printstream
     * originally.
     */
    public void println(String s) {
    	try {
	    writeUTF(s,'\n');
	} catch (IOException e) {
//	    Core.logger.log(this, "IOException when printing : " + e,
//			    Logger.DEBUGGING);
	}
    }

    /*
     * Like for the decoding, I wish I didn't have to do this myself, but
     * I can't find anywhere where java lets one access a UTF encoder flexibly
     * (ie without the overhead of making chars into strings).
     *
     */
    private byte[] encodeUTF(char c) {
	return ((c <= 0x007F) ?
		new byte[] { (byte)(c & 0xFF) } :
		((c <= 0x07FF) ?
		 new byte[] { (byte)(0xC0 | (c >> 6)) ,
			      (byte)(0x80 | (c & 0x3F)) } :
		 new byte[] { (byte)(0xE0 | (c >> 12)) ,
			      (byte)(0xC0 | (c >> 6)) ,
			      (byte)(0x80 | (c & 0x3F)) } ));
    }
}
