package org.shiftone.cache.decorator.soft;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.AbstractDecoratorCacheFactory;
import org.shiftone.cache.util.reaper.CacheReaper;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class SoftCacheFactory extends AbstractDecoratorCacheFactory
{

    private long period = 1000 * 5;

    protected Cache wrapDelegate(String cacheName, Cache delegateCache)
    {
        return CacheReaper.register(new SoftCache(delegateCache), period);
    }


    public long getPeriod()
    {
        return period;
    }


    public void setPeriod(long period)
    {
        this.period = period;
    }


    public String toString()
    {
        return "SoftCacheFactory->" + getDelegate();
    }
}
