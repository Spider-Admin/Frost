package org.shiftone.cache;



/**
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public interface CacheFactory
{
    Cache newInstance(String cacheName, long timeoutMilliSeconds, int maxSize);
}
