package freenet.support;

import java.io.*;

import freenet.support.io.NullInputStream;

/**
 * A file Bucket is an implementation of Bucket that writes to a file.
 *
 * @author oskar
 **/
public class FileBucket implements Bucket {

    protected File file;
    protected boolean restart = true;
    protected boolean newFile;	// hack to get around deletes
    protected long length; // JVM caches File.size() and there is no way to flush the cache, so we need to track it ourselves
    protected long fileRestartCounter;

    protected static String tempDir = null;

    /**
     * Creates a new FileBucket.
     * @param file  The File to read and write to.
     **/
    public FileBucket(File file) {
	this.file = file;
	this.newFile = false;
        // Useful for finding temp file leaks.
        // System.err.println("-- FileBucket.ctr(0) -- " + file.getAbsolutePath());
        // (new Exception("get stack")).printStackTrace();
	fileRestartCounter = 0;
	length = file.length();
    }

    /**
     * Creates a new FileBucket in a random temporary file in the temporary directory.
     */

    public FileBucket() {
	file = new File("tempdir/" + System.currentTimeMillis());
        //new File(tempDir, "t" + Fields.longToString(Math.abs(Core.randSource.nextInt())));
        // Useful for finding temp file leaks.
        //System.err.println("-- FileBucket.ctr(1) -- " + file.getAbsolutePath());
        //(new Exception("get stack")).printStackTrace();
	newFile = true;
	length = 0;
        file.deleteOnExit();
        //OnExitCleanUp.deleteOnExit(file);
    }

    public OutputStream getOutputStream() throws IOException {
	synchronized(this) {
	    boolean append = !restart;
	    if(restart) fileRestartCounter++;
	    // we want the length of the file we are currently writing to

	    // FIXME: behaviour depends on UNIX semantics, to totally abstract it out we would have to kill the old write streams here
	    // FIXME: what about existing streams? Will ones on append append to the new truncated file? Do we want them to? What about truncated ones? We should kill old streams here, right?
	    restart = false;
	    return newFileBucketOutputStream(file.getPath(),append,
					     fileRestartCounter);
	}
    }

    protected FileBucketOutputStream
	newFileBucketOutputStream(String s, boolean append,
				  long restartCount) throws IOException {
	return new FileBucketOutputStream(s, append, restartCount);
    }

    protected void resetLength() {
	long oldLen = length;
	length = 0;
    }

    class FileBucketOutputStream extends FileOutputStream {

	long restartCount;
	Exception e;

	protected FileBucketOutputStream(String s, boolean append,
					 long restartCount)
	    throws FileNotFoundException {
	    super(s, append);
	    this.restartCount = restartCount;
	    // if we throw, we're screwed anyway
	    if(!append) {
		resetLength();
	    }
	}

	protected void confirmWriteSynchronized() {
	    if(fileRestartCounter > restartCount)
		throw new IllegalStateException
		    ("writing to file after restart");
	}

	public void write(byte[] b) throws IOException {
	    synchronized(FileBucket.this) {
		confirmWriteSynchronized();
		super.write(b);
		length += b.length;
	    }
	}

	public void write(byte[] b, int off, int len) throws IOException {
	    synchronized(FileBucket.this) {
		confirmWriteSynchronized();
		super.write(b, off, len);
		length += len;
	    }
	}

	public void write(int b) throws IOException {
	    synchronized(FileBucket.this) {
		confirmWriteSynchronized();
		if(fileRestartCounter > restartCount)
		    throw new IllegalStateException
			("writing to file after restart");
		super.write(b);
		length++;
	    }
	}
    }

    class FileBucketInputStream extends FileInputStream {
	Exception e;

	public FileBucketInputStream(File f) throws IOException {
	    super(f);
	}
    }

    public InputStream getInputStream() throws IOException {
        return file.exists() ? (InputStream) new FileBucketInputStream(file)
                             : (InputStream) new NullInputStream();
    }

    /** @return  the name of the file.
      */
    public String getName() {
        return file.getName();
    }

    public void resetWrite() {
	restart = true;
    }

    public long size() {
	return length;
    }


    /**
     * Returns the file object this buckets data is kept in.
     **/
    public File getFile() {
	return file;
    }

    /** Actually delete the underlying file.
     * Called by finalizer, will not be called twice.
     * But length must still be valid when calling it.
     */
    protected void deleteFile() {
	file.delete();
    }

    public void finalize() throws Throwable
    {
	if(newFile && file.exists()) {
	    deleteFile();
	}
    }

    /**
     * Return directory used for temp files.
     **/
    public final synchronized static String getTempDir() {
        return tempDir;
    }

    /**
     * Set temp file directory.
     * <p>
     * The directory must exist.
     **/
    public final synchronized static void setTempDir(String dirName) {
        File dir = new File(dirName);
        if (!(dir.exists() && dir.isDirectory() && dir.canWrite())) {
            throw new IllegalArgumentException("Bad Temp Directory: " + dir.getAbsolutePath());
        }
        tempDir = dirName;
    }

    // determine the temp directory in one of several ways

    static {
	// Try the Java property (1.2 and above)
	tempDir = System.getProperty("java.io.tmpdir");

	// Deprecated calls removed.

	// Try TEMP and TMP
	//	if (tempDir == null) {
	//	    tempDir = System.getenv("TEMP");
	//	}

	//	if (tempDir == null) {
	//	    tempDir = System.getenv("TMP");
	//	}

	// make some semi-educated guesses based on OS.

	if (tempDir == null) {
	    String os = System.getProperty("os.name");
	    if (os != null) {

		String[] candidates = null;

		// XXX: Add more possible OSes here.
		if (os.equalsIgnoreCase("Linux")) {
		    String[] linuxCandidates = { "/tmp", "/var/tmp" };
		    candidates = linuxCandidates;
		} else if (os.equalsIgnoreCase("Windows")) {
		    String[] windowsCandidates = { "C:\\TEMP", "C:\\WINDOWS\\TEMP" };
		    candidates = windowsCandidates;
		}

		if (candidates != null) {
		    for (int i = 0; i < candidates.length; i++) {
			File path = new File(candidates[i]);
			if (path.exists() &&
			    path.isDirectory() &&
			    path.canWrite())
			{
			    tempDir = candidates[i];
			    break;
			}
		    }
		}
	    }
	}

	// last resort -- use current working directory

	if (tempDir == null) {
	    // This can be null -- but that's OK, null => cwd for File constructor, anyways.
	    tempDir = System.getProperty("user.dir");
	}
    }

}
