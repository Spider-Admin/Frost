package org.shiftone.cache.policy.fifo;



import org.shiftone.cache.util.*;
import org.shiftone.cache.util.reaper.ReapableCache;

import java.util.Map;


/**
 * Class FifoCache
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class FifoCache extends AbstractPolicyCache implements ReapableCache
{

    private static final Log LOG = new Log(FifoCache.class);
    private final Map        map;
    private final LinkedList fifo;

    FifoCache(String name, long timeoutMilliSeconds, int maxSize)
    {

        super(name, timeoutMilliSeconds, maxSize);

        this.map  = MapFactory.createMap(maxSize);
        this.fifo = new LinkedList();
    }


    protected CacheNode findNodeByKey(Object key)
    {
        return (FifoNode) map.get(key);
    }


    public int size()
    {
        return map.size();
    }


    /**
     * <b>DO NOTHING</b>
     */
    protected final void revalueNode(CacheNode cacheNode) {}


    protected void delete(CacheNode node)
    {

        FifoNode n = (FifoNode) node;

        fifo.remove(n.fifoNode);
        map.remove(n.key);
    }


    protected void removeLeastValuableNode()
    {

        LinkedListNode lln  = null;
        FifoNode       node = null;

        lln  = fifo.peekLast();
        node = (FifoNode) lln.getValue();

        delete(node);
    }


    public void removeExpiredElements()
    {

        LinkedListNode lln = null;
        CacheNode      node;

        while ((lln = fifo.peekLast()) != null)
        {
            node = (CacheNode) lln.getValue();

            if (node.isExpired())
            {
                delete(node);
            }
            else
            {

                // not expired.. can stop now
                break;
            }
        }
    }


    protected CacheNode createNode(Object userKey, Object cacheObject)
    {

        FifoNode node = null;

        node             = new FifoNode();
        node.key         = userKey;
        node.value       = cacheObject;
        node.fifoNode    = fifo.addFirst(node);    // expensive
        node.timeoutTime = System.currentTimeMillis() + getTimeoutMilliSeconds();

        map.put(userKey, node);

        return node;
    }


    String dumpFifoKeys()
    {

        String         dump    = null;
        StringBuffer   sb      = new StringBuffer();
        LinkedListNode node    = fifo.peekFirst();
        FifoNode       current = null;

        while (node != null)
        {
            current = (FifoNode) node.getValue();

            sb.append(current.key);

            node = node.getNext();
        }

        dump = sb.toString();

        return dump;
    }
}
