package org.shiftone.cache.policy.fifo;



import org.shiftone.cache.Cache;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class RingFifoCache implements Cache
{

    private final int      maxSize;
    private final int[]    indexes;
    private final Object[] keys;
    private final Object[] values;
    private int            nextIndexIndex = 0;

    public RingFifoCache(int maxSize)
    {

        this.maxSize = maxSize;
        indexes      = new int[maxSize];
        keys         = new Object[maxSize];
        values       = new Object[maxSize];
    }


    private int getIndex(Object key)
    {
        return (key.hashCode() % maxSize);
    }


    public void addObject(Object userKey, Object cacheObject)
    {

        int index = getIndex(userKey);

        indexes[nextIndexIndex] = index;
        nextIndexIndex          = (nextIndexIndex + 1) % maxSize;
        keys[index]             = userKey;
        values[index]           = cacheObject;
    }


    public Object getObject(Object key)
    {

        int    index = getIndex(key);
        Object k     = keys[index];

        if ((k != null) && (k.equals(key)))
        {
            return values[index];
        }

        return null;
    }


    public int size()
    {
        return 0;
    }


    public void remove(Object key) {}


    public void clear() {}
}
