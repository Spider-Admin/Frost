package org.shiftone.cache.decorator.cluster;



import org.shiftone.cache.Cache;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class ClearNotification implements Notification
{

    private final long   senderInstanceId;
    private final String cacheName;

    public ClearNotification(long senderInstanceId, String cacheName)
    {
        this.senderInstanceId = senderInstanceId;
        this.cacheName        = cacheName;
    }


    public void execute(Cache cache)
    {
        cache.clear();
    }


    public String getCacheName()
    {
        return cacheName;
    }


    public long getSenderInstanceId()
    {
        return senderInstanceId;
    }


    public String toString()
    {
        return "clear()";
    }
}
