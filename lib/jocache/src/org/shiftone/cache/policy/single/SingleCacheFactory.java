package org.shiftone.cache.policy.single;



import org.shiftone.cache.Cache;
import org.shiftone.cache.CacheFactory;
import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * Creates a simple cache that holds only one key/value.
 * This cache type can be useful for storing a fairly static but still database driven list,
 * such as a list of countries.
 * Obviously, this cache doesn't require a very complex implementation.  If a cache
 * will only ever have one value, then this will be the most efficient implementation.
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class SingleCacheFactory implements CacheFactory
{

    //  CacheReaper reaper = CacheReaper.getReaper();

    /**
     * Method newInstance
     */
    public Cache newInstance(String cacheName, long timeoutMilliSeconds, int maxSize)
    {

        ReapableCache cache = new SingleCache(timeoutMilliSeconds);

        //todo reaper.register(cache);
        return cache;
    }


    public String toString()
    {
        return "SingleCacheFactory";
    }
}
