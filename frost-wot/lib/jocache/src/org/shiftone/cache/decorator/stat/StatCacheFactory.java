package org.shiftone.cache.decorator.stat;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.AbstractDecoratorCacheFactory;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class StatCacheFactory extends AbstractDecoratorCacheFactory
{

    private ShutdownHook shutdownHook = null;

    private synchronized ShutdownHook getShutdownHook()
    {

        if (shutdownHook == null)
        {
            shutdownHook = new ShutdownHook();

            shutdownHook.setTitle(getDelegate().toString());
        }

        return shutdownHook;
    }


    protected Cache wrapDelegate(String cacheName, Cache delegateCache)
    {

        StatCache statCache = new StatCache(cacheName, delegateCache);

        getShutdownHook().addStatCache(statCache);

        return statCache;
    }


    public void setMaxCaches(int maxCaches)
    {
        shutdownHook.setMaxCaches(maxCaches);
    }


    public String toString()
    {
        return "StatCacheFactory->" + getDelegate();
    }
}
