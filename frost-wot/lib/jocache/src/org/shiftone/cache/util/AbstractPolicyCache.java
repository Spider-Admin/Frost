package org.shiftone.cache.util;



import org.shiftone.cache.util.reaper.ReapableCache;


/**
 * Class AbstractPolicyCache
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public abstract class AbstractPolicyCache implements ReapableCache
{

    private static final double MIN_PCT_FREE = 1.0;
    private static final int    SHRINK_MULT  = 2;
    public static final int     SHRINK_DIV   = 3;
    private final String        name;
    private final int           maxSize;
    private final long          timeoutMilliSeconds;

    protected AbstractPolicyCache(String name, long timeoutMilliSeconds, int maxSize)
    {

        this.name                = name;
        this.timeoutMilliSeconds = timeoutMilliSeconds;
        this.maxSize             = maxSize;
    }


    public final void addObject(Object userKey, Object cacheObject)
    {

        CacheNode node;

        /*
                if (userKey == null)
                {
                    throw new NullPointerException("key");
                }

                 if (cacheObject == null)
                {
                    throw new NullPointerException("value");
                }
                */
        node = findNodeByKey(userKey);

        if (node != null)
        {

            // if the node exists, then set it's value, and revalue it.
            // this is better than deleting it, because it doesn't require
            // more memory to be allocated
            node.setValue(cacheObject);
            revalueNode(node);
        }
        else
        {
            shrinkToSize(getMaxSize() - 1);
            createNode(userKey, cacheObject);
        }

        removeExpiredElements();

        //checkFreeMemory();
    }


    public final Object getObject(Object key)
    {

        Object    value = null;
        CacheNode node;

        removeExpiredElements();

        node = findNodeByKey(key);

        if (node == null)
        {
            ;    // cache miss
        }
        else if (node.isExpired())
        {
            delete(node);
        }
        else if (node != null)
        {
            revalueNode(node);

            value = node.getValue();
        }

        return value;
    }


    public final void remove(Object key)
    {

        CacheNode node = findNodeByKey(key);

        if (node != null)
        {
            delete(node);
        }

        removeExpiredElements();
    }


    protected String getName()
    {
        return name;
    }


    protected final int getMaxSize()
    {
        return this.maxSize;
    }


    protected final long getTimeoutMilliSeconds()
    {
        return this.timeoutMilliSeconds;
    }


    /**
     * Method removes the least valuable nodes, one by one until the cache's
     * size is less than or equal to the desired size.
     */
    private final void shrinkToSize(int desiredSize)
    {

        while (size() > desiredSize)
        {

            //Log.info(getClass(), "desiredSize = " + desiredSize + " size=" + size());
            removeLeastValuableNode();
        }
    }


    /**
     * This method calls shrinkToSize(0) which will loop through
     * each element, removing them one by one (in order of lease valued
     * to most valued).  Derived classes may wish to implement this in a
     * more efficient way (by just reinitalizing itself).
     */
    public void clear()
    {
        shrinkToSize(0);
    }


    /**
     * Method checkFreeMemory
     */
    private final void checkFreeMemory()
    {

        /*
      double        percentFree = MemUtil.freeMemoryPct();
      long          start       = System.currentTimeMillis();
      DecimalFormat format      = null;

      if (percentFree < MIN_PCT_FREE)
      {

          //log.warn("xxx", new Exception("XXX"));
          format = new DecimalFormat("##.##");

          shrinkToSize((size() * SHRINK_MULT) / SHRINK_DIV);
          Runtime.getRuntime().gc();
          Runtime.getRuntime().runFinalization();
          Thread.yield();
          log.warn(format.format(percentFree) + "% memory free - cleanup took "
                   + (System.currentTimeMillis() - start) + "ms - size is now " + size());
      }
     */
    }


    /**
     * Method findNodeByKey
     */
    abstract protected CacheNode findNodeByKey(Object key);


    /**
     * Update the node's value.  This is done immediately after
     * the node is retrieved.
     */
    abstract protected void revalueNode(CacheNode node);


    /**
     * Remove a node from the cache.
     */
    abstract protected void delete(CacheNode node);


    /**
     * This method will execute the cache's eviction strategy.
     * If this method is called, there will be at least one
     * element in the cache to remove.  The method itself does
     * not need to check for the existance of a element.
     * <p>
     * This method is only called by shrinkToSize();
     */
    abstract protected void removeLeastValuableNode();


    /**
     * Purge the cache of expired elements.
     */
    abstract public void removeExpiredElements();


    /**
     * Create a new node.
     */
    abstract protected CacheNode createNode(Object userKey, Object cacheObject);


    private static String shortName(Class klass)
    {

        String className = klass.getName();
        int    lastDot   = className.lastIndexOf('.');

        if (lastDot != -1)
        {
            className = className.substring(lastDot + 1);
        }

        return className;
    }


    public String toString()
    {
        return shortName(getClass()) + "(" + getName() + "," + getTimeoutMilliSeconds() + "," + getMaxSize() + ")";
    }
}
