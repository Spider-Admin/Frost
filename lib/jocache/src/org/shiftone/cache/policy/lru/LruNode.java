package org.shiftone.cache.policy.lru;



import org.shiftone.cache.util.CacheNode;
import org.shiftone.cache.util.LinkedListNode;


/**
 * Class LruNode
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class LruNode implements CacheNode
{

    /// private static final Logger LOG         = Logger.getLogger(LruNode.class);
    Object         key         = null;
    Object         value       = null;
    LinkedListNode fifoNode    = null;
    LinkedListNode lruNode     = null;
    long           timeoutTime = 0;

    public final boolean isExpired()
    {

        long timeToGo = timeoutTime - System.currentTimeMillis();

        return (timeToGo <= 0);
    }


    public final Object getValue()
    {
        return this.value;
    }


    public final void setValue(Object value)
    {
        this.value = value;
    }


    public String toString()
    {
        return "LruNode:" + String.valueOf(key);
    }
}
