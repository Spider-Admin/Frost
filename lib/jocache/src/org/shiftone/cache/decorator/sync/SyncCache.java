package org.shiftone.cache.decorator.sync;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class SyncCache implements Cache, ReapableCache
{

    private final ReapableCache reapableCache;
    private final Cache         cache;

    public SyncCache(Cache cache)
    {

        this.cache = cache;

        if (cache instanceof ReapableCache)
        {
            reapableCache = (ReapableCache) cache;
        }
        else
        {
            reapableCache = null;
        }
    }


    public void addObject(Object userKey, Object cacheObject)
    {

        synchronized (cache)
        {
            cache.addObject(userKey, cacheObject);
        }
    }


    public Object getObject(Object key)
    {

        synchronized (cache)
        {
            return cache.getObject(key);
        }
    }


    public int size()
    {
        return cache.size();
    }


    public void remove(Object key)
    {

        synchronized (cache)
        {
            cache.remove(key);
        }
    }


    public void clear()
    {

        synchronized (cache)
        {
            cache.clear();
        }
    }


    public void removeExpiredElements()
    {

        if (reapableCache != null)
        {
            synchronized (cache)
            {
                reapableCache.removeExpiredElements();
            }
        }
    }


    public String toString()
    {
        return "SycnCache->" + cache;
    }
}
