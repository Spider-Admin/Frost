package org.shiftone.cache.policy.lru;



import org.shiftone.cache.util.AbstractPolicyCacheFactory;
import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * Creates a least-recently-used cache.
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class LruCacheFactory extends AbstractPolicyCacheFactory
{

    public ReapableCache newReapableCache(String cacheName, long timeoutMilliSeconds, int maxSize)
    {
        return new LruCache(cacheName, timeoutMilliSeconds, maxSize);
    }


    public String toString()
    {
        return "LruCacheFactory";
    }
}
