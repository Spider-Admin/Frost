package org.shiftone.cache.adaptor;



import org.shiftone.cache.Cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Makes a shiftone cache look like a map.
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class CacheMap implements Map
{

    private final Cache cache;

    public CacheMap(Cache cache)
    {
        this.cache = cache;
    }


    /**
     * The size of this cache at this instance in time
     */
    public int size()
    {
        return cache.size();
    }


    /**
     * Is the cache empty at this instance in time
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }


    /**
     * <b>Warning</b> - just because this method returns true, you can NOT
     * expect to be able to get the value of the object that the cache contains.
     * This is because some time will pass between when you test for the elements
     * existance in the cache, and when you call get().  During that window of time,
     * the cache element may be evicted.
     */
    public boolean containsKey(Object key)
    {
        return (cache.getObject(key) != null);
    }


    /**
     * Gets an object from the cache based on it's key
     */
    public Object get(Object key)
    {
        return cache.getObject(key);
    }


    /**
     * Puts an object into the cache and returns the previous
     * value that was associated with that key.
     */
    public Object put(Object key, Object value)
    {

        Object oldValue = cache.getObject(key);

        cache.addObject(key, value);

        return oldValue;
    }


    public Object remove(Object key)
    {

        cache.remove(key);

        return null;
    }


    /**
     * Copies all of the mappings from the specified map to this cache.
     */
    public void putAll(Map map)
    {

        Object   key, value;
        Set      keys = map.keySet();
        Iterator i    = keys.iterator();

        while (i.hasNext())
        {
            key   = i.next();
            value = map.get(key);

            if (value != null)
            {
                cache.addObject(key, value);
            }
        }
    }


    /**
     * Clears the cache
     */
    public void clear()
    {
        cache.clear();
    }


    /**
     * <b>not implemented</b>
     */
    public boolean containsValue(Object value)
    {
        throw new UnsupportedOperationException("containsValue");
    }


    /**
     * <b>not implemented</b>
     */
    public Set keySet()
    {
        throw new UnsupportedOperationException("keySet");
    }


    /**
     * <b>not implemented</b>
     */
    public Collection values()
    {
        throw new UnsupportedOperationException("values");
    }


    /**
     * <b>not implemented</b>
     */
    public Set entrySet()
    {
        throw new UnsupportedOperationException("entrySet");
    }
}
