package freenet.support.io;
import java.io.*;

/**
 * A FilterInputStream that adds a second close() method, to indicate a more
 * graceful termination of the data read.
 *
 * @author oskar
 */
public abstract class DiscontinueInputStream extends FilterInputStream {

    public DiscontinueInputStream(InputStream in) {
        super(in);
    }

    public abstract void discontinue() throws IOException;

}
