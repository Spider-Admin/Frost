package freenet.client;

import java.io.IOException;

import freenet.support.*;

/*
  This code is distributed under the GNU Public Licence (GPL)
  version 2.  See http://www.gnu.org/ for further details of the GPL.
*/

/**
 * Interface for plugging Forward Error Correction (FEC) encoder
 * implementaions into the Fred.
 * <p>
 * Requirements:<br>
 * <ul>
 * <li> For a given segment with k data blocks and n - k check
 *      blocks, it must be possible to decode all k data blocks
 *      from any k of n data or check blocks.
 * <li> Encoder and Decoder implementations must be completly
 *      specified by an implementation name and a file length.
 *      No other parameters can be required to instantiate the
 *      encoder or decoder.
 * <li> Implementations are not guaranteed to be
 *      thread safe. The client code is responsible
 *      for serializing access.
 * <li> Implementations must have a no args constructor in order
 *      to be loadable by FECFactory.  All initialization should
 *      be done in init().
 * <li> Within a segment all data blocks must be the same size and
 *      all check blocks must be the same size.  The check block
 *      and data block sizes are not required to be the same however.
 *      Smaller trailing blocks must be zero padded to the required
 *      length.
 * <li> The encoder may ask for extra trailing data blocks.
 *      i.e. getN() * getBlockSize() * getSegments()  can
 *      be greater than the blockSize padded length. These extra
 *      blocks must contain zeros.
 * <li> Implementations should avoid segmenting if possible. <p>
 *      You get better robustness if all check blocks contain
 *      information about all data blocks.
 *      However, some FEC codecs just can't
 *      handle really huge files at reasonable
 *      block sizes.  In this case, segmenting
 *      is the only way out.
 *
 * </ul>
 * <p>
 * @author giannij
 **/
public interface FECEncoder {

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
     * Initializes the encoder to encode a file of
     * length len.
     * @return true on success, false otherwise.
     **/
    boolean init(long len, BucketFactory factory);

    /**
     * Releases all resources (memory, file handles, temp files, etc.)
     * used by the encoder instance.
     * <p>
     * Implementations must support calling this function
     * irregardless of the state of the encoder instance. e.g.
     * it isn't an error to call release on an encoder that
     * has already been released or has never been initialized.
     **/
    void release();

    /**
     * Polite implementations should use block sizes which are
     * integer powers of 2  <= 1M.
     * @return The block size for data blocks.
     **/
    int getBlockSize(int segment);

    /**
     * Polite implementations should use block sizes which are
     * integer powers of 2  <= 1M.
     * @return The block size for checks blocks.
     **/
    int getCheckBlockSize(int segment);

    /**
     * @return the total number of data and check blocks per segment.
     */
    int getN(int segment);

    /**
     * @return the number of data blocks per segment.
     **/
    int getK(int segment);

    /**
     * @return the number of segments.
     **/
    int getSegmentCount();

    /**
     * @return the segment size.
     **/
    int getSegmentSize(int segment);

    /**
     * Make check blocks for a single segment. This may take
     * a very long time.  Polite implementations should
     * abort as quickly as possible and throw an
     * InterruptedIOException if the thread is interrupted.
     * <p>
     * REQUIRES: blocks.length == getK(segmentNumber)
     * <p>
     * @param segmentNumber the segment number
     * @param blocks all the data blocks for the segment, in order
     * @param requestedCheckBlocks the indices of the requested check blocks.
     *        This can be null.  If it is null all n - k check blocks are returned.
     * @return the check blocks in order.
     **/
    Bucket[] encode(int segmentNumber, Bucket[] blocks, int[] requestedCheckBlocks) throws IOException;
}





