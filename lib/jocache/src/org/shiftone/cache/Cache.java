package org.shiftone.cache;



/**
 * Interface Cache
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public interface Cache
{

    /**
     * adds an object to the cache
     */
    void addObject(Object userKey, Object cacheObject);


    /**
     * gets the value stored in the cache by it's key,
     * or null if the key is not found.
     */
    Object getObject(Object key);


    /**
     * The number of key/value pares in the cache
     */
    int size();


    /**
     * remove a specific key/value pair from the cache
     */
    void remove(Object key);


    /**
     * Removes ALL keys and values from the cache.
     * Use with digression.  Using this method too frequently
     * may defeat the purpose of caching.
     */
    void clear();
}
