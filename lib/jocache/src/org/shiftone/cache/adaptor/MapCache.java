package org.shiftone.cache.adaptor;



import org.shiftone.cache.Cache;

import java.util.HashMap;
import java.util.Map;


/**
 * Makes a map look like a shiftone cache.
 *
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class MapCache implements Cache
{

    private final Map map;

    public MapCache()
    {
        this(new HashMap());
    }


    public MapCache(Map map)
    {
        this.map = map;
    }


    public void addObject(Object userKey, Object cacheObject)
    {
        map.put(userKey, cacheObject);
    }


    public Object getObject(Object key)
    {
        return map.get(key);
    }


    public int size()
    {
        return map.size();
    }


    public void remove(Object key)
    {
        map.remove(key);
    }


    public void clear()
    {
        map.clear();
    }


    public String toString()
    {
        return "MapCache[" + map.getClass().getName() + "]";
    }
}
