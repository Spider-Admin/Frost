package org.shiftone.cache.policy.lru;



import org.shiftone.cache.util.*;
import org.shiftone.cache.util.reaper.ReapableCache;

import java.util.Map;


/**
 * Class LruCache
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class LruCache extends AbstractPolicyCache implements ReapableCache
{

    private static final Log LOG = new Log(LruCache.class);
    private final Map        map;
    private final LinkedList fifo;
    private final LinkedList lru;

    LruCache(String name, long timeoutMilliSeconds, int maxSize)
    {

        super(name, timeoutMilliSeconds, maxSize);

        map  = MapFactory.createMap(maxSize);
        fifo = new LinkedList();
        lru  = new LinkedList();
    }


    protected CacheNode findNodeByKey(Object key)
    {
        return (LruNode) map.get(key);
    }


    public int size()
    {
        return map.size();
    }


    protected void revalueNode(CacheNode node)
    {

        LruNode n = (LruNode) node;

        lru.moveToFirst(n.lruNode);
    }


    protected void delete(CacheNode node)
    {

        LruNode n = (LruNode) node;

        fifo.remove(n.fifoNode);
        lru.remove(n.lruNode);
        map.remove(n.key);
    }


    protected void removeLeastValuableNode()
    {

        LinkedListNode lln  = null;
        LruNode        node = null;

        lln  = lru.peekLast();
        node = (LruNode) lln.getValue();

        delete(node);
    }


    public void removeExpiredElements()
    {

        LinkedListNode lln  = null;
        LruNode        node = null;

        while ((lln = fifo.peekLast()) != null)
        {
            node = (LruNode) lln.getValue();

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

        LruNode node = new LruNode();

        node.key         = userKey;
        node.value       = cacheObject;
        node.fifoNode    = fifo.addFirst(node);
        node.lruNode     = lru.addFirst(node);
        node.timeoutTime = System.currentTimeMillis() + getTimeoutMilliSeconds();

        map.put(userKey, node);

        return node;
    }


    String dumpLruKeys()
    {

        String         dump    = null;
        StringBuffer   sb      = new StringBuffer();
        LinkedListNode node    = lru.peekFirst();
        LruNode        current = null;

        while (node != null)
        {
            current = (LruNode) node.getValue();

            sb.append(current.key);

            node = node.getNext();
        }

        dump = sb.toString();

        LOG.debug("dumpLruKeys : " + dump);

        return dump;
    }


    String dumpFifoKeys()
    {

        String         dump    = null;
        StringBuffer   sb      = new StringBuffer();
        LinkedListNode node    = fifo.peekFirst();
        LruNode        current = null;

        while (node != null)
        {
            current = (LruNode) node.getValue();

            sb.append(current.key);

            node = node.getNext();
        }

        dump = sb.toString();

        LOG.debug("dumpFifoKeys : " + dump);

        return dump;
    }
}
