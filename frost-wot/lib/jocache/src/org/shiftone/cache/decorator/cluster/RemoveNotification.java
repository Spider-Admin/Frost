package org.shiftone.cache.decorator.cluster;



import org.shiftone.cache.Cache;

import java.io.Serializable;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class RemoveNotification implements Notification
{

    private final long         senderInstanceId;
    private final Serializable key;
    private final String       cacheName;

    public RemoveNotification(long senderInstanceId, String cacheName, Serializable key)
    {

        this.senderInstanceId = senderInstanceId;
        this.cacheName        = cacheName;
        this.key              = key;
    }


    public RemoveNotification(long senderInstanceId, String cacheName, Object key)
    {

        this.senderInstanceId = senderInstanceId;
        this.cacheName        = cacheName;

        if (key instanceof Serializable)
        {
            this.key = (Serializable) key;
        }
        else
        {
            throw new ClassCastException("unable to cast " + key.getClass() + " to Serializable");
        }
    }


    public void execute(Cache cache)
    {
        cache.remove(key);
    }


    public long getSenderInstanceId()
    {
        return senderInstanceId;
    }


    public String getCacheName()
    {
        return cacheName;
    }


    public String toString()
    {
        return "remove(" + key + ")";
    }
}
