package org.shiftone.cache.util;



import org.shiftone.cache.Cache;
import org.shiftone.cache.CacheException;
import org.shiftone.cache.CacheFactory;
import org.shiftone.cache.policy.zero.ZeroCacheFactory;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public abstract class AbstractDecoratorCacheFactory implements CacheFactory
{

    private static final Log LOG = new Log(AbstractDecoratorCacheFactory.class);
    private CacheFactory     delegate;

    public Cache newInstance(String cacheName, long timeoutMs, int maxSize)
    {

        CacheFactory factory = getDelegate();
        Cache        cache   = null;

        try
        {
            cache = factory.newInstance(cacheName, timeoutMs, maxSize);

            if (factory != null)
            {
                cache = wrapDelegate(cacheName, cache);
            }
        }
        catch (Exception e)
        {
            LOG.error("unable to create cache decorator : " + cacheName, e);
        }

        if (cache == null)
        {
            LOG.info("returning null cache for : " + cacheName);

            cache = ZeroCacheFactory.NULL_CACHE;
        }

        return cache;
    }


    protected abstract Cache wrapDelegate(String cacheName, Cache delegateCache) throws CacheException;


    public CacheFactory getDelegate()
    {
        return delegate;
    }


    public void setDelegate(CacheFactory delegate)
    {
        this.delegate = delegate;
    }


    public String toString()
    {
        return getClass().getName() + "[" + delegate + "]";
    }
}
