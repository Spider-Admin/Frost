package freenet.client;

import java.io.IOException;

import freenet.support.*;

/*
  This code is distributed under the GNU Public Licence (GPL)
  version 2.  See http://www.gnu.org/ for further details of the GPL.
*/

/**
 * Interface for plugging Forward Error Correction (FEC) decoder
 * implementaions into Fred.
 * <p>
 * See the requirements listed in the javadoc for FECEncoder.
 * <p>
 * REDFLAG: look up how to make a javadoc link!
 *
 * @author giannij
 **/
public interface FECDecoder {
    /**
     * The name of the algorithm. It is legal to call this before
     * init() is called.
     * <p>
     * A FECEncoder and the FECDecoder implementation that decodes
     * it *must* return the same value for getName().
     *
     **/
    String getName();

    /**
     * Create a FECDecoder capable of decoding a
     * segmented file of size length encoded with
     * the sister FECEncoder for the same algorithm.
     * <p>
     * init() leaves the decoder defaulted to segment 0.
     * If there are additional segments the client should
     * call setSegement() to reset the decoder for each one.
     * <p>
     * @param length the length of the file.
     * @param factory a BucketFactory used to make temporary
     *                Buckets.
     *
     * @return true on success, false if a decoder can't be
     *         allocated.
     **/
    boolean init(long length, BucketFactory factory);


    /**
     * FEC decode missing data blocks.
     * <p>
     * This is a blocking call that may take
     * a long time. Polite implementations
     * should abort as quickly as possible and
     * throw InterruptedIOException if the thread
     * is interrupted.
     * <p>
     * This call releases any data buckets submitted
     * via putBucket() for the current segment.
     *
     * @return true on success, false otherwise.
     **/
    public boolean decode(int [] requestedBlocks,
                          Bucket[] decodedBlocks) throws IOException;

    /**
     * Releases all resources (memory, file handles, temp files, etc.)
     * used by the encoder instance.
     * <p>
     * Implementations must support calling this function
     * irregardless of the state of the decoder instance. i.e.
     * it isn't an error to call release on a decoder that
     * has already been released or has never been initialized.
     **/
    void release();

    /**
     * @return the block size for data blocks in the current segment.
     **/
    int getBlockSize();

    /**
     * @return the block size check blocks in the current segment.
     **/
    int getCheckBlockSize();

    /**
     * @return the number of data and check blocks in the
     *         current segment.
     **/
    int getN();

    /**
     * @return the number of data blocks in the
     *         current segment.
     **/
    int getK();

    /**
     * @return the number of the current segment.
     **/
    int getCurrentSegment();

    /**
     * Sets the current segement.
     * <p>
     * @return true on success, false otherwise.
     **/
    boolean setSegment(int segment);

    /**
     * @return the number of segments used to encode the file.
     **/
    int getSegmentCount();

    /**
     * Called by clients to submit blocks to the decoder.
     * <p>
     * The implementation owns all buckets submitted
     * via this function and is responsible for
     * deleting them.
     * <p>
     * Block numbers are per segment. Data blocks are numbered from
     * 0 to getK() - 1. Check blocks are numbered from
     * getK() to getN() - 1.  It is an error for client
     * code to submit a block from a segment other than the
     * current segment as denoted by getCurrentSegment().
     * <p>
     * @param bucket the block.
     * @param number the block number.
     **/
    void putBucket(Bucket bucket, int number) throws IOException;

    /**
     * @return true as soon as the decoder has enough blocks
     * to fully decode the the current segment.
     **/
    public boolean isDecodable();
}










