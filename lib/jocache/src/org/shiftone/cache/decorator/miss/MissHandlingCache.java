package org.shiftone.cache.decorator.miss;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.Log;

import java.util.Hashtable;
import java.util.Map;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class MissHandlingCache implements Cache
{

    private static final Log  LOG = new Log(MissHandlingCache.class);
    private final Cache       cache;
    private final Object      fetchLock = new Object();
    private final MissHandler missHandler;
    private Map               keyMap = new Hashtable(11);

    public MissHandlingCache(Cache cache, MissHandler missHandler)
    {

        this.cache       = cache;
        this.missHandler = missHandler;

        LOG.debug("new");
    }


    synchronized Object getLock(Object key)
    {

        Lock lock = (Lock) keyMap.get(key);

        if (lock == null)
        {
            lock       = new Lock();
            lock.count = 0;

            keyMap.put(key, lock);
        }
        else
        {
            lock.count++;
        }

        return lock;
    }


    synchronized void unlockKey(Object key)
    {

        Lock lock = (Lock) keyMap.get(key);

        if (lock != null)
        {
            lock.count--;

            if (lock.count < 0)
            {
                keyMap.remove(key);
            }
        }
        else
        {
            throw new IllegalStateException("unlock error");
        }
    }


    public Object getObject(Object key)
    {

        Object value = null;
        Object lock  = getLock(key);

        synchronized (lock)
        {
            try
            {
                synchronized (cache)
                {
                    value = cache.getObject(key);
                }

                if (value == null)
                {
                    try
                    {
                        value = missHandler.fetchObject(key);
                    }
                    catch (Exception e)
                    {
                        LOG.error("MissHandler error : " + missHandler, e);
                    }
                }

                if (value != null)
                {
                    addObject(key, value);
                }
            }
            finally
            {
                unlockKey(key);
            }

            return value;
        }
    }


    public void addObject(Object userKey, Object cacheObject)
    {

        synchronized (cache)
        {
            cache.addObject(userKey, cacheObject);
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


    public final String toString()
    {
        return "MissHandlingCache->" + cache;
    }


    public static class Lock
    {
        int count = 0;
    }
}
