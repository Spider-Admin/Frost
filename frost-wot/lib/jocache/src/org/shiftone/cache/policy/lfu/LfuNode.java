package org.shiftone.cache.policy.lfu;



import org.shiftone.cache.util.CacheNode;
import org.shiftone.cache.util.LinkedListNode;


/**
 * Class LfuNode
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class LfuNode implements CacheNode
{

    /// private static final Logger LOG         = Logger.getLogger(LfuNode.class);
    Object         key         = null;
    Object         value       = null;
    LinkedListNode fifoNode    = null;
    LinkedListNode lfuNode     = null;
    long           timeoutTime = 0;
    int            numUsages   = 0;

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
        return "(lfu-" + String.valueOf(key) + ":u=" + numUsages + ")";
    }
}
