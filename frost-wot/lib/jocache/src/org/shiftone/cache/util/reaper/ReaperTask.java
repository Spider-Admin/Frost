package org.shiftone.cache.util.reaper;



import org.shiftone.cache.util.Log;

import java.lang.ref.SoftReference;
import java.util.TimerTask;


/**
 * Class ReaperTask
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class ReaperTask extends TimerTask
{

    private static final Log  LOG             = new Log(ReaperTask.class);
    private static long       instanceCounter = 0;
    private static final long instanceNumber  = (instanceCounter++);
    private SoftReference     reference       = null;
    private String            toString;

    public ReaperTask(ReapableCache cache)
    {
        this.toString  = cache.toString();
        this.reference = new SoftReference(cache);
    }


    public void run()
    {

        ReapableCache reapableCache = (ReapableCache) reference.get();
        String        threadName    = Thread.currentThread().getName();

        Thread.currentThread().setName("REAPER for " + instanceNumber);
        LOG.debug("run");

        if (reapableCache == null)
        {
            LOG.info("cache reaper quitting for : " + toString + " #" + instanceNumber);
            cancel();
        }
        else
        {
            reapableCache.removeExpiredElements();
        }

        Thread.currentThread().setName(threadName);
    }
}
