package org.shiftone.cache.policy.fifo;



import org.shiftone.cache.util.AbstractPolicyCacheFactory;
import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * Creates a first-in-first-out cache.
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class FifoCacheFactory extends AbstractPolicyCacheFactory
{

    public ReapableCache newReapableCache(String cacheName, long timeoutMilliSeconds, int maxSize)
    {
        return new FifoCache(cacheName, timeoutMilliSeconds, maxSize);
    }


    public String toString()
    {
        return "FifoCacheFactory";
    }
}
