package org.shiftone.cache.decorator.soft;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.Log;
import org.shiftone.cache.util.reaper.ReapableCache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;


/**
 * Memory sensitive cache.
 * <p>
 * This cache implementation proxies to a cache that was passed to it's constructor.
 * When objects are added to the cache, the object wrapped in a SoftReference,
 * and then the Reference is added to the delegate cache.
 * <p>
 * Once a SoftCache is created for a cache, the SoftCache should always be used to
 * access this cache.  Using the original cache directly is not recommended.
 * <p>
 * Note that there are <n>no guarantees</b> as to how long objects you put in cache will remain
 * there.  This is entirely at the digression of the garbage collector, which tends to
 * be hasty about freeing up memory.
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class SoftCache implements Cache, ReapableCache
{

    private static final Log LOG = new Log(SoftCache.class);

    //private static CacheReaper   reaper         = CacheReaper.getReaper();
    private final ReferenceQueue referenceQueue = new ReferenceQueue();
    private final Cache          cache;

    public SoftCache(Cache cache)
    {

        if (cache instanceof SoftCache)
        {
            throw new UnsupportedOperationException("SoftCache should not delegate to SoftCache");
        }

        this.cache = cache;

        // reaper.register(this);
    }


    public void addObject(Object userKey, Object cacheObject)
    {

        // thanks to JD Evora for the bug report
        cache.addObject(userKey, new KeyReference(userKey, cacheObject, referenceQueue));
    }


    /**
     * Gets a soft reference out of the underlying cache implementation, and then
     * returns the value held by the reference.
     */
    public Object getObject(Object key)
    {

        Object    result = null;
        Reference ref    = null;

        ref = (Reference) cache.getObject(key);

        if (ref != null)
        {
            result = ref.get();

            if (result == null)
            {
                LOG.debug("reference found in cache but GC removed object");
                cache.remove(key);
            }
        }

        return result;
    }


    public final void remove(Object key)
    {
        cache.remove(key);
    }


    public final int size()
    {
        return cache.size();
    }


    public final void clear()
    {
        cache.clear();
    }


    /**
     * Cleans all cache elements out that have had their objects collected by the GC.
     */
    public synchronized void removeExpiredElements()
    {

        Reference ref         = null;
        Object    key         = null;
        int       removeCount = 0;

        while ((ref = referenceQueue.poll()) != null)
        {
            key = ((KeyReference) ref).getKey();

            cache.remove(key);

            removeCount++;
        }

        LOG.debug(this + " removeExpiredElements() removed - " + removeCount + " refs - " + size() + " left");
    }


    public final String toString()
    {
        return "SoftCache->" + cache;
    }


    /**
     * Class KeyReference
     *
     *
     * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
     * @version $Revision$
     */
    class KeyReference extends SoftReference
    {

        private Object key = null;

        /**
         * Constructor KeyReference
         *
         *
         * @param key
         * @param cacheObject
         * @param referenceQueue
         */
        public KeyReference(Object key, Object cacheObject, ReferenceQueue referenceQueue)
        {

            super(cacheObject, referenceQueue);

            this.key = key;
        }


        /**
         * Method getKey
         */
        Object getKey()
        {
            return key;
        }
    }
}
