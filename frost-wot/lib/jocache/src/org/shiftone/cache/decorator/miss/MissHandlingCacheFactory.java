package org.shiftone.cache.decorator.miss;



import org.shiftone.cache.Cache;
import org.shiftone.cache.CacheException;
import org.shiftone.cache.util.AbstractDecoratorCacheFactory;
import org.shiftone.cache.util.Log;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class MissHandlingCacheFactory extends AbstractDecoratorCacheFactory
{

    private static final Log LOG = new Log(MissHandlingCacheFactory.class);
    private Class            missHandlerClass;

    protected Cache wrapDelegate(String cacheName, Cache delegateCache) throws CacheException
    {
        return new MissHandlingCache(delegateCache, createMissHandler());
    }


    public Class getMissHandlerClass()
    {
        return missHandlerClass;
    }


    public void setMissHandlerClass(Class missHandlerClass)
    {
        this.missHandlerClass = missHandlerClass;
    }


    public MissHandler createMissHandler() throws CacheException
    {

        MissHandler missHandler;
        Class       klass = getMissHandlerClass();

        try
        {
            missHandler = (MissHandler) klass.newInstance();
        }
        catch (Exception e)
        {
            throw new CacheException("unable to create new MissHandler instance", e);
        }

        return missHandler;
    }


    public String toString()
    {
        return "MissHandlingCacheFactory[" + missHandlerClass.getName() + "]->" + getDelegate();
    }
}
