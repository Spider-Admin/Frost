package org.shiftone.cache.decorator.stat;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.Log;

import java.util.Date;


/**
 * Class StatCache
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class StatCache implements Cache
{

    private static final Log LOG = new Log(StatCache.class);
    private final Cache      cache;
    private final String     name;
    private final Date       createDate  = new Date();
    private Sequence         getCount    = new Sequence();
    private Sequence         addCount    = new Sequence();
    private Sequence         removeCount = new Sequence();
    private Sequence         missCount   = new Sequence();
    private Sequence         hitCount    = new Sequence();

    public StatCache(String name, Cache cache)
    {
        this.name  = name;
        this.cache = cache;
    }


    /**
     * Method addObject
     */
    public void addObject(Object userKey, Object cacheObject)
    {
        addCount.increment();
        cache.addObject(userKey, cacheObject);
    }


    /**
     * Method getObject
     */
    public Object getObject(Object key)
    {

        Object object = cache.getObject(key);

        getCount.increment();

        if (object == null)
        {
            missCount.increment();
        }
        else
        {
            hitCount.increment();
        }

        return object;
    }


    /**
     * Method remove
     */
    public void remove(Object key)
    {
        removeCount.increment();
        cache.remove(key);
    }


    /**
     * Method size
     */
    public int size()
    {
        return cache.size();
    }


    /**
     * Method clear
     */
    public void clear()
    {
        cache.clear();
    }


    /**
     * Method getHitRatio is the ratio of hits to tries.
     * hitCount / (hitCount + missCount)
     */
    public double getHitRatio()
    {

        long tryCount = hitCount.getValue() + missCount.getValue();

        return (tryCount == 0)
               ? 0
               : (hitCount.getValue() / tryCount);
    }


    /**
     * Method toString
     */
    public String toString()
    {
        return "StatCache->" + cache;
    }


    public void printStats()
    {

        LOG.info("Stats : " + name + " - " + cache + " - " + createDate);
        LOG.info("\t:\tgetCount    = " + getCount);
        LOG.info("\t:\taddCount    = " + addCount);
        LOG.info("\t:\tremoveCount = " + removeCount);
        LOG.info("\t:\tmissCount   = " + missCount);
        LOG.info("\t:\tgetCount    = " + getCount);
        LOG.info("\t:\thitCount    = " + hitCount);
    }
}
