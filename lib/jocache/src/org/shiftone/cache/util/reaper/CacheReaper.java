package org.shiftone.cache.util.reaper;



import org.shiftone.cache.Cache;
import org.shiftone.cache.util.Log;

import java.util.Timer;


/**
 * Class CacheReaper
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class CacheReaper
{

    private static final Log   LOG   = new Log(CacheReaper.class);
    private static final Timer TIMER = new Timer(true);

    public static Cache register(ReapableCache cache, long period)
    {

        LOG.debug("register : " + cache);
        TIMER.scheduleAtFixedRate(new ReaperTask(cache), period, period);

        return cache;
    }
}
