package org.shiftone.cache.policy.lfu;



import org.shiftone.cache.util.*;
import org.shiftone.cache.util.reaper.ReapableCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Class LfuCache
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class LfuCache extends AbstractPolicyCache implements ReapableCache
{

    private static final Log LOG = new Log(LfuCache.class);
    private final Map        map;
    private final LinkedList fifo;
    private final List       lrus;
    private int              maxLruBuckets = 0;

    // when searching for a node to remove, the lowest lru bucked is checked
    // then then next, etc etc.  In some rare cases, we have extra information that
    // would allow a higher bucket to be used to start the search.
    // This is a minor optimizaton.
    private int lowestNonEmptyLru = 0;

    public LfuCache(String name, long timeoutMilliSeconds, int maxSize)
    {

        super(name, timeoutMilliSeconds, maxSize);

        map           = MapFactory.createMap(maxSize);
        fifo          = new LinkedList();
        lrus          = new ArrayList(5);
        maxLruBuckets = maxSize * 3;
    }


    protected final LinkedList lru(int numUsageIndex)
    {

        LinkedList lru      = null;
        int        lruIndex = Math.min(maxLruBuckets, numUsageIndex);

        if (lruIndex >= lrus.size())
        {
            lru = new LinkedList();

            lrus.add(lruIndex, lru);
        }
        else
        {
            lru = (LinkedList) lrus.get(lruIndex);
        }

        return lru;
    }


    public int size()
    {
        return map.size();
    }


    protected CacheNode findNodeByKey(Object key)
    {
        return (LfuNode) map.get(key);
    }


    protected void revalueNode(CacheNode cacheNode)
    {

        LfuNode        node       = (LfuNode) cacheNode;
        LinkedListNode lln        = node.lfuNode;
        LinkedList     currBucket = lru(node.numUsages);
        LinkedList     nextBucket = lru(++node.numUsages);

        currBucket.remove(lln);

        node.lfuNode = nextBucket.addFirst(lln.getValue());
    }


    protected void delete(CacheNode cacheNode)
    {

        LfuNode node = (LfuNode) cacheNode;

        fifo.remove(node.fifoNode);
        lru(node.numUsages).remove(node.lfuNode);
        map.remove(node.key);
    }


    protected LinkedList getLowestNonEmptyLru()
    {

        LinkedList lru = null;

        for (int i = lowestNonEmptyLru; i < lrus.size(); i++)
        {
            lru = lru(i);

            if (lru.size() != 0)
            {
                lowestNonEmptyLru = i;

                return lru;
            }
        }

        return lru;
    }


    protected void removeLeastValuableNode()
    {

        LinkedList     lfu  = getLowestNonEmptyLru();
        LinkedListNode lln  = lfu.peekLast();
        LfuNode        node = (LfuNode) lln.getValue();

        delete(node);
    }


    protected CacheNode createNode(Object userKey, Object cacheObject)
    {

        LfuNode node = null;

        node              = new LfuNode();
        node.key          = userKey;
        node.value        = cacheObject;
        node.fifoNode     = fifo.addFirst(node);
        node.lfuNode      = lru(0).addFirst(node);
        node.timeoutTime  = System.currentTimeMillis() + getTimeoutMilliSeconds();
        lowestNonEmptyLru = 0;

        map.put(userKey, node);

        return node;
    }


    public void removeExpiredElements()
    {

        LinkedListNode lln  = null;
        LfuNode        node = null;

        while ((lln = fifo.peekLast()) != null)
        {
            lln  = fifo.peekLast();
            node = (LfuNode) lln.getValue();

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


    //------------------------------------------------------------------------
    String dumpLfuKeys()
    {

        String         dump    = null;
        StringBuffer   sb      = new StringBuffer();
        LinkedListNode node    = null;    //lfu.peekFirst();
        LfuNode        current = null;

        for (int i = lrus.size() - 1; i >= 0; i--)
        {
            node = lru(i).peekFirst();

            while (node != null)
            {
                current = (LfuNode) node.getValue();

                sb.append(current.key);

                node = node.getNext();
            }
        }

        dump = sb.toString();

        LOG.debug("dumpLfuKeys : " + dump);

        return dump;
    }


    String dumpFifoKeys()
    {

        String         dump    = null;
        StringBuffer   sb      = new StringBuffer();
        LinkedListNode node    = fifo.peekFirst();
        LfuNode        current = null;

        while (node != null)
        {
            current = (LfuNode) node.getValue();

            sb.append(current.key);

            node = node.getNext();
        }

        dump = sb.toString();

        LOG.debug("dumpFifoKeys : " + dump);

        return dump;
    }
}
