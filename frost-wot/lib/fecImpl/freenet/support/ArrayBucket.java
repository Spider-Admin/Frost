package freenet.support;
import java.io.*;
import java.util.*;

/**
 * A bucket that stores data in the memory.
 *
 * @author oskar
 */

public class ArrayBucket implements Bucket {

    private Vector data;
    private boolean reset;
    private String name;

    public ArrayBucket() {
        this("ArrayBucket");
    }

    public ArrayBucket(byte[] initdata) {
        this("ArrayBucket");
        data.addElement(initdata);
    }

    public ArrayBucket(String name) {
        data = new Vector();
        this.name = name;
    }

    public OutputStream getOutputStream() {
        return new ArrayBucketOutputStream(reset);
    }

    public InputStream getInputStream() {
        return new ArrayBucketInputStream();
    }

    public String toString() {
	StringBuffer s = new StringBuffer();
	for(Enumeration e = data.elements() ; e.hasMoreElements() ;) {
            byte[] b  = (byte[]) e.nextElement();
	    s.append(new String(b));
	}
	return new String(s);
    }

    public void read(InputStream in) throws IOException {
        OutputStream out = new ArrayBucketOutputStream(reset);
        int i; byte[] b = new byte[0xffff];
        while ((i = in.read(b)) != -1) {
            out.write(b, 0, i);
        }
        out.close();
    }

    public long size() {
        long size = 0;
        for (Enumeration e = data.elements() ; e.hasMoreElements() ;) {
            byte[] b  = (byte[]) e.nextElement();
            size += b.length;
        }
        return size;
    }

    public String getName() {
        return name;
    }

    public void resetWrite() {
        reset = true;
    }



    private class ArrayBucketOutputStream extends ByteArrayOutputStream {
        boolean reset;
        boolean done = false;
        public ArrayBucketOutputStream(boolean reset) {
            super();
            this.reset = reset;
        }

        public void close() {
            done = true;
            if (reset) {
                data.removeAllElements();
		data.trimToSize();
	    }
            reset = false;
            data.addElement(toByteArray());
        }
    }

    private class ArrayBucketInputStream extends InputStream {
        Enumeration e;
        ByteArrayInputStream in;

        public ArrayBucketInputStream() {
            e = data.elements();
        }

        public int read() {
            return priv_read();
        }

        private int priv_read() {
            if (in == null) {
                if (e.hasMoreElements()) {
                    in = new ByteArrayInputStream((byte[]) e.nextElement());
                } else {
                    return -1;
                }
            }
            int i = in.read();
            if (i == -1) {
                in = null;
                return priv_read();
            } else {
                return i;
            }
        }

        public int read(byte[] b) {
            return priv_read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) {
            return priv_read(b, off, len);
        }

        private int priv_read(byte[] b, int off, int len) {
            if (in == null) {
                if (e.hasMoreElements()) {
                    in = new ByteArrayInputStream((byte[]) e.nextElement());
                } else {
                    return -1;
                }
            }
            int i = in.read(b, off, len);
            if (i == -1) {
                in = null;
                return priv_read(b, off, len);
            } else {
                return i;
            }
        }

        public int available() {
            if (in == null) {
                if (e.hasMoreElements()) {
                    in = new ByteArrayInputStream((byte[]) e.nextElement());
                } else {
                    return 0;
                }
            }
            return in.available();
        }

        
    }
}
