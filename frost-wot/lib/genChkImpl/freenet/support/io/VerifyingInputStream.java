package freenet.support.io;
import java.io.*;

/**
 * This class limits the length of the data on a stream to a specified length.
 * In other words, it makes a section of data on a stream with a specified length
 * look like an entire stream.  (Could it be named better?)
 */
public class VerifyingInputStream extends DiscontinueInputStream {

    protected long dataLength, bytesRead;

    /** Whether the stream has finished */
    protected boolean finished = false; 

    /** Whether implementations should strip control bytes from output */
    protected boolean stripControls = false;

    /** This will become true after the second to last byte is read.
      * This is to notify implementations that only the final control
      * byte is left, so they can read, validate, and throw it out.
      */
    protected boolean allRead = false;

    public VerifyingInputStream(InputStream in, long dataLength) {
        super(in);
        this.dataLength = dataLength;
    }

    public void stripControls(boolean stripControls) {
        this.stripControls = stripControls;
    }

    public int read() throws IOException, DataNotValidIOException {

        if (finished) {
            //System.err.println("hit finished boolean");
            return -1;
        }
        
        int ret = in.read();
        if (ret != -1) {
            ++bytesRead;
            if (bytesRead == dataLength)
                finished = true;
            else if (bytesRead == dataLength - 1)
                allRead = true;
        }

        return ret;
    }

    public int read(byte[] b, int off, int len)
               throws IOException, DataNotValidIOException {

        if (finished) {
            //System.err.println("hit finished boolean");
            return -1;
        }

        // If this is the last byte, then just read a single byte.
        if (allRead) {
            int i = read();
            if (i == -1) return -1;
            b[off] = (byte) i;
            return 1;
        }

        // If the number of bytes requested is greater than what's
        // supposed to be left (limited by dataLength), then only read up to
        // that point.  In actual fact it reads only up to dataLength-1, not
        // dataLength.  The case above handles that last byte.

        if (len > dataLength - bytesRead - 1)
            len = (int) (dataLength - bytesRead - 1);

        //System.err.println("LALA VIS CALLING IN.READ() - IN: " + in.toString());
        int ret = in.read(b, off, len);
        if (ret != -1) {
            bytesRead += ret;
            if (bytesRead == dataLength - 1)
                allRead = true;
        }
        
        return ret;
    }

    public int available() throws IOException {

        // Lie about bytes available to ensure we don't overrun dataLength.

        int n = super.available();
        return allRead ? (n == 0 ? 0 : 1)
                       : (int) Math.min(n, stripControls ? dataLength - bytesRead - 1
                                                         : dataLength - bytesRead);
    }

    // a bit of a hack, but it's hard to see the Right Way (tm)
    public void discontinue() throws IOException {
        if (in instanceof DiscontinueInputStream)
            ((DiscontinueInputStream) in).discontinue();
        else
            close();
    }
}








