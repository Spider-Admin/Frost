package org.bouncycastle.crypto.paddings;

import java.security.SecureRandom;

import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 * Block cipher padders are expected to conform to this interface
 */
public interface BlockCipherPadding
{
    /**
     * Initialise the padder.
     *
     * @param param parameters, if any required.
     */
    public void init(SecureRandom random)
        throws IllegalArgumentException;

    /**
     * Return the name of the algorithm the cipher implements.
     *
     * @return the name of the algorithm the cipher implements.
     */
    public String getPaddingName();

    /**
     * add the pad bytes to the passed in block, returning the
     * number of bytes added.
     */
    public int addPadding(byte[] in, int inOff);

    /**
     * return the number of pad bytes present in the block.
     * @exception InvalidCipherTextException if the padding is badly formed
     * or invalid.
     */
    public int padCount(byte[] in)
        throws InvalidCipherTextException;
}
