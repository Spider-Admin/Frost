// REDFLAG: test and javadoc
package freenet.support;

import java.io.IOException;

/**
 * Class to present a stripe accross an array of
 * Buckets as an array of Buckets each the size
 * of the stripe width.
 *
 * Note: This class manipulates the underlying File's
 *       directly.  It doesn't copy the buckets.
 */
public class StripedBucketArray {
    // Shouldn't have any open streams.
    // Must be either FileBuckets or RandomAccessFileBuckets
    // Must all be the same size.
    public Bucket[] allocate(Bucket[] src) throws IOException {
        if (srcData != null) {
            throw new IllegalStateException("Release the previously allocated buckets!");
        }

        long size = src[0].size();

        // Do paranoid checks.
/*        for (int i = 0; i < src.length; i++) {
            if (src[i].size() != size) {
                throw new IllegalArgumentException("All buckets must be the same size.");
            }
            if (!((src[i] instanceof FileBucket) ||
                  (src[i] instanceof RandomAccessFileBucket))) {
                // This is rude. But making deep copies of Buckets is ruder...
                throw new IllegalArgumentException("Buckets must be of type FileBucket or " +
                                                   "RandomAccessFileBucket.");
            }
        }*/

        Bucket[] ret = new Bucket[src.length];

        try {
            srcData = new BucketData[src.length];
            for (int i = 0; i <src.length; i++) {
                srcData[i] = makeBucketData(src[i]);
                ret[i] = srcData[i].rafb;
            }
        }
        catch (IOException ioe) {
            srcData = null;
            throw ioe;
        }

        return ret;
    }

    public void release() {
	if (srcData == null) {
	    return;
	}

        for (int i = 0; i < srcData.length; i ++) {
            try {
                srcData[i].release();
            }
            catch (Exception e) {
                // NOP. This should not happen.
            }
        }
        srcData = null;
    }

    public void setRange(long offset, long len) throws IOException {
        for (int i = 0; i < srcData.length; i ++) {
            srcData[i].rafb.setRange(srcData[i].offset + offset, len);
        }
    }

    ////////////////////////////////////////////////////////////

    // Stuff we need to remember to release.
    private static BucketData makeBucketData(Bucket b) throws IOException {
        if (b instanceof RandomAccessFileBucket) {
            return new BucketData((RandomAccessFileBucket)b);
        }
        else if (b instanceof FileBucket) {
            return new BucketData((FileBucket)b);
        }
        throw new IllegalArgumentException("Bucket must be of type FileBucket or " +
                                           "RandomAccessFileBucket.");
    }

    private static class BucketData {
        BucketData(RandomAccessFileBucket rafb) {
            RandomAccessFileBucket.Range r = rafb.getRange();
            offset = r.offset;
            len = r.len;
            fromRAFB = true;
            this.rafb = rafb;
        }

        BucketData(FileBucket fb) throws IOException {
            offset = 0;
            len = fb.size();
            fromRAFB = false;
            this.rafb = new RandomAccessFileBucket(fb.getFile(), 0, fb.size(), false);
        }

        void release() {
            if (fromRAFB) {
                try {
                    rafb.setRange(offset, len);
                }
                catch (Exception e) {
                    // hmmm this shouldn't happen.
                }
            }
            else {
                rafb.release();
            }
            rafb = null;
        }

        long offset = 0;
        long len = 0; // hmmm. not really needed.
        boolean fromRAFB = false;
        RandomAccessFileBucket rafb = null;
    }

    private RandomAccessFileBucket[] Buckets = null;

    private BucketData[] srcData = null;
}
