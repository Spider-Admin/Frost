package org.shiftone.cache.policy.zero;



import org.shiftone.cache.Cache;


/**
 * Class ZeroCache
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class ZeroCache implements Cache
{

    public final void addObject(Object userKey, Object cacheObject) {}


    public final Object getObject(Object key)
    {
        return null;
    }


    public final int size()
    {
        return 0;
    }


    public final void remove(Object key) {}


    public final void clear() {}


    public final String toString()
    {
        return "ZeroCache";
    }
}
