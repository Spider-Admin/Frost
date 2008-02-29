package freenet.support.io;
import java.io.*;

public class NullInputStream extends InputStream {
    public NullInputStream() {}
    public int read() throws IOException { return -1; }
}

