package org.shiftone.cache.util;



import org.shiftone.cache.Cache;
import org.shiftone.cache.CacheFactory;
import org.shiftone.cache.decorator.sync.SyncCache;
import org.shiftone.cache.util.reaper.CacheReaper;
import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * @version $Revision$
 * @author $Author$
 */
public abstract class AbstractPolicyCacheFactory implements CacheFactory
{

    private static final Log LOG    = new Log(AbstractPolicyCacheFactory.class);
    private int              period = 1000;

    public abstract ReapableCache newReapableCache(String cacheName, long timeoutMilliSeconds, int maxSize);


    public Cache newInstance(String cacheName, long timeoutMilliSeconds, int maxSize)
    {

        return CacheReaper.register(                                           //
            new SyncCache(                                                     //
                newReapableCache(cacheName, timeoutMilliSeconds, maxSize)),    //
                period);
    }


    /**
     * time in milliseconds between calls from the reaper.  Every "period"
     * milliseconds this factory's reaper will wake up and call
     * "removeExpiredElements" on the cache.  Note that changing this value will
     * only effect new caches - all existing caches will continue with the same
     * period.
     */
    public int getPeriod()
    {
        return period;
    }


    public void setPeriod(int period)
    {
        this.period = period;
    }
}
