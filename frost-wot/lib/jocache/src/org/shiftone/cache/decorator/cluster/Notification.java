package org.shiftone.cache.decorator.cluster;



import org.shiftone.cache.Cache;

import java.io.Serializable;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public interface Notification extends Serializable
{

    void execute(Cache cache);


    public long getSenderInstanceId();


    public String getCacheName();
}
