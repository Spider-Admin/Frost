package org.shiftone.cache.policy.lfu;



import org.shiftone.cache.util.AbstractPolicyCacheFactory;
import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * Creates a least-frequently-used cache.
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class LfuCacheFactory extends AbstractPolicyCacheFactory
{

    public ReapableCache newReapableCache(String cacheName, long timeoutMilliSeconds, int maxSize)
    {
        return new LfuCache(cacheName, timeoutMilliSeconds, maxSize);
    }


    public String toString()
    {
        return "LfuCacheFactory";
    }
}
