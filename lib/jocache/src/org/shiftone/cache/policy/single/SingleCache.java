package org.shiftone.cache.policy.single;



import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * Class SingleCache
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class SingleCache implements ReapableCache
{

    private long   timeoutMilliSeconds = 0;
    private long   expireTime          = 0;
    private Object userKey             = null;
    private Object cacheObject         = null;

    /**
     * Constructor SingleCache
     *
     *
     * @param timeoutMilliSeconds
     */
    public SingleCache(long timeoutMilliSeconds)
    {
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }


    /**
     * Method addObject
     */
    public synchronized void addObject(Object userKey, Object cacheObject)
    {

        this.userKey     = userKey;
        this.cacheObject = cacheObject;
        expireTime       = System.currentTimeMillis() + timeoutMilliSeconds;
    }


    /**
     * Method getObject
     */
    public synchronized Object getObject(Object key)
    {

        Object value = null;

        if ((this.userKey != null) && (key != null) && (key.equals(this.userKey)))
        {
            value = this.cacheObject;
        }

        return value;
    }


    /**
     * Method size
     */
    public int size()
    {

        return (userKey == null)
               ? 0
               : 1;
    }


    /**
     * Method remove
     */
    public synchronized void remove(Object key)
    {

        if ((this.userKey != null) && (key != null) && (key.equals(this.userKey)))
        {
            userKey     = null;
            cacheObject = null;
        }
    }


    /**
     * Method clear
     */
    public synchronized void clear()
    {
        userKey     = null;
        cacheObject = null;
    }


    /**
     * Method removeExpiredElements
     */
    public synchronized void removeExpiredElements()
    {

        /// LOG.info("removeExpiredElements");
        if (System.currentTimeMillis() >= expireTime)
        {
            remove(this.userKey);
        }
    }


    public final String toString()
    {
        return "SingleCache" + size();
    }
}
