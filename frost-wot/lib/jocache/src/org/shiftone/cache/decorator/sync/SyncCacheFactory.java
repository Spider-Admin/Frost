package org.shiftone.cache.decorator.sync;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.AbstractDecoratorCacheFactory;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class SyncCacheFactory extends AbstractDecoratorCacheFactory
{

    protected Cache wrapDelegate(String cacheName, Cache delegateCache)
    {
        return new SyncCache(delegateCache);
    }


    public String toString()
    {
        return "SyncCacheFactory->" + getDelegate();
    }
}
