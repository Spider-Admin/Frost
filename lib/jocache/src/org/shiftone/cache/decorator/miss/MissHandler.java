package org.shiftone.cache.decorator.miss;



/**
 * Interface MissHandler.
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public interface MissHandler
{

    /**
     * In the event of a cache miss, a user implemented instance of this class will be
     * asked to do whatever processing is needed to obtain the cache element
     *
     * @param key
     * @return the object that was fetched
     */
    Object fetchObject(Object key) throws Exception;
}
