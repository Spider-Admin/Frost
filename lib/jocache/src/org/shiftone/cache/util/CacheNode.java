package org.shiftone.cache.util;



/**
 * Class CacheNode
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public interface CacheNode
{

    void setValue(Object value);


    Object getValue();


    boolean isExpired();
}
