package freenet.crypt;
import java.io.*;
import java.util.Stack;

import freenet.Presentation;
import freenet.support.Bucket;

/**
 * A progressive hash stream is a stream of data where each part is preceded
 * by a hash of that part AND the hash of the next part. This means
 * that only the hash of the first part (and second hash) needs to be known, 
 * but one can be sure that each part is valid after reading it.
 * 
 * This class provides an OutputStream to which the data must be written,
 * after which it can be streamed out again with the correct hash values
 * and control bytes interleaved. It will require enough temporary 
 * diskspace to write the data.
 *
 * The design of the progressive hash as used in Freenet is taken from:
 * Gennaro, R and Rohatgi, P; "How to Sign Digital Streams", 
 * Advances in Cryptology - CRYPTO '97, 1997.
 *
 * @author oskar
 **/

public class ProgressiveHashOutputStream extends OutputStream {

    private Bucket b;
    private OutputStream out;
    private boolean closed = false;

    private DigestFactory df;
    private Digest ctx;
    private Stack digests;
    private int digSize;
    
    private long partSize;
    private long written = 0;
    private long totalLength = -1;
    
    private byte[] initialDigest;
    private Stack dvals;

    /**
     * Create a SerialHashOutputStream.
     *
     * @param partSize  The amount of data in each part. After each part the 
     *                  digest value and one control byte will be written.
     * @param df        A DigestFactory for creating Digest objects of the
     *                  kind in question.
     * @param b         The bucket to put the data in when it is processed.
     *                  Note that you can't simply take the inputstream from
     *                  this bucket after writing to this class, since it 
     *                  will not contain the interleaved hash values,
     *                  getData() provides that.
     * @return an InputStream of the original data, interleaved after every
     *         partSize bytes with the next digest value and a control char.
     **/
    public ProgressiveHashOutputStream(long partSize, 
                                       DigestFactory df,
                                       Bucket b) throws IOException {
        
        if (partSize <= 0) throw new IllegalArgumentException("partSize must be > 0");

        b.resetWrite();
        
        this.b        = b;
        this.out      = new BufferedOutputStream(b.getOutputStream());
        this.df       = df;
        this.ctx      = df.getInstance();
        this.partSize = partSize;
        this.digests  = new Stack();
        this.digSize  = ctx.digestSize() >> 3;
    }

    public void write(int i)  throws IOException {
        if (closed) throw new IOException("closed");
        if (written == partSize) nextDigest();
        out.write(i);
        ctx.update((byte) i);
        ++written;
    }
    
    public void write(byte[] b, int off, int length) throws IOException {
        if (closed) throw new IOException("closed");
        while (length > 0) {
            int chunk = (int) Math.min(length, partSize - written);
            if (chunk == 0) {
                nextDigest();
                continue;
            }
            out.write(b, off, chunk);
            ctx.update(b, off, chunk);
            written += chunk;
            off     += chunk;
            length  -= chunk;
        }
    }
    
    private void nextDigest() {
        digests.push(ctx);
        ctx = df.getInstance();
        written = 0;
    }

    /**
     * Finalizes the stream, creating the data InputStream, digest value,
     * and total length count.
     */
    public void close() throws IOException {

        if (closed) return;
        out.close();
        closed = true;
        
        // set hash values
        dvals = new Stack();
        while (!digests.empty()) {
            byte[] dval = ctx.digest();
            ctx = (Digest) digests.pop();
            ctx.update(dval);
            dvals.push(dval);
            //System.err.println("pushing dval: "+dval+" "+dval[0]+" "+dval[1]+" "+dval[2]);
        }
        // set initial digest value
        initialDigest = ctx.digest();

        // calculate length
        int parts = (int) ((b.size()-1) / partSize);
        long lastPart = b.size() - parts * partSize;
        totalLength = parts * (partSize + 21) + lastPart + 1;
    }

    /**
     * Returns the initial hash value of the Progressive hash.
     * @return the hash value if finish() has been called, null otherwise.
     **/
    public byte[] getInitialDigest() {
        //return (byte []) initialDigest.clone();
        return initialDigest;
    }

    /**
     * Returns the total length of the new stream, with the interleaved
     * hash values and control bytes counted.
     * @return total length of resulting stream if finish() has been called,
     *         -1 otherwise.
     **/
    public long getLength() {
        return totalLength;
    }
    
    /**
     * Return the an inputstream of the data with interleaved hash values
     * and control bytes.
     * @return The resulting stream if finish() has been called, null
     *         otherwise.
     **/
    public InputStream getInputStream() throws IOException {
        return closed ? new InterleaveInputStream(b.getInputStream(), b.size())
                      : null;
    }

    /**
     * A simple stream that pushes byte arrays (digest values) off the 
     * stack and adds them and a control byte to the stream after every 
     * partSize.
     **/    
    protected class InterleaveInputStream extends FilterInputStream {
        //private long read;        
        private byte[] digestBuf;
        private long pos, length;
        //private boolean ended;
        //private int dSize;
        //private int dig;
        //private byte[] check = getInitialDigest();
        //private Digest ctx = new SHA1();

        public InterleaveInputStream(InputStream in, long length) throws IOException {
            super(in);
            this.length = length;
            pos         = 0;
            digestBuf   = new byte[digSize + 1];
            digestBuf[digSize] = (byte) Presentation.CB_OK;
        }

        public int read() throws IOException {

            if (pos > 0 && (pos == partSize || length == 0)) next();
            
            //if (pos < 0) {
            //    byte ret = digestBuf[digestBuf.length + (int) pos++];
            //    if (pos != 0) {
            //        ctx.update(ret);
            //        if (pos == -1) check();
            //    }
            //    System.err.println("returning 1 control byte");
            //    return (int) ret & 0xff;
            //}
            if (pos < 0)
                return (int) digestBuf[digestBuf.length + (int) pos++] & 0xff;
            if (length == 0)
                return -1;

            int ret = in.read();
            if (ret != -1) {
                ++pos;
                --length;
                //ctx.update((byte) ret);
                //System.err.println("returning 1 data byte");
            }
            return ret;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len <= 0) {
                return 0;
            }
            if (pos > 0 && (pos == partSize || length == 0)) {
                next();
            }
            if (pos < 0) {
                int n = (int) Math.min(len, 0 - (int) pos);
                System.arraycopy(digestBuf, digestBuf.length + (int) pos, b, off, n);
                pos += n;
                //int foo = (pos == 0 ? n-1 : n);
                //if (foo > 0) {
                //    ctx.update(b, off, foo);
                //    if (pos >= -1) check();
                //}
                //System.err.println("returning "+n+" control bytes");
                return n;
            }
            if (length == 0) {
                return -1;
            }
            
            len = (int) Math.min(len, Math.min(partSize - pos, length));

            int n = in.read(b, off, len);
            pos    += n;
            length -= n;

            //ctx.update(b, off, n);
            //System.err.println("returning "+n+" data bytes");
            return n;
        }
/*
        private void check() {
            byte[] tmp = ctx.digest();
            if (!Util.byteArrayEqual(tmp, check))
                System.err.println("failed my own self-check!!");
            System.arraycopy(digestBuf, 0, check, 0, digSize);
        }
*/        
        private void next() {
            if (!dvals.empty()) {
                //byte[] dval = (byte []) dvals.pop();
                //System.err.println("popping dval: "+dval+" "+dval[0]+" "+dval[1]+" "+dval[2]);
                //System.arraycopy(dval, 0, digestBuf, 0, digSize);
                System.arraycopy((byte []) dvals.pop(), 0, digestBuf, 0, digSize);
                pos = -1 - digSize;
            }
            else {
                pos = -1;
            }
        }

        public int available() throws IOException {
            return pos < 0 ? 0 - (int) pos
                           : (int) Math.min(super.available(), partSize - pos);
        }
    }
}



