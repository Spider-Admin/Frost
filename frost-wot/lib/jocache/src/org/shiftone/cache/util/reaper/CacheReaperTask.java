package org.shiftone.cache.util.reaper;



import java.util.TimerTask;


/**
 * Class CacheReaperTask
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
class CacheReaperTask extends TimerTask
{

    private static long       instanceCounter = 0;
    private static final long instanceNumber  = (instanceCounter++);

    /// private static final Logger LOG             = Logger.getLogger(CacheReaperTask.class);
    ReapableCache cache = null;

    /**
     * Constructor CacheReaperTask
     *
     *
     * @param cache
     * @param key
     */
    public CacheReaperTask(ReapableCache cache)
    {

        /// LOG.debug("new CacheReaperTask( " + cache + " )");
        this.cache = cache;
    }


    /**
     * Method run
     */
    public void run()
    {

        String threadName = Thread.currentThread().getName();

        Thread.currentThread().setName("REAPER for " + instanceNumber);

        synchronized (cache)
        {
            cache.removeExpiredElements();
        }

        Thread.currentThread().setName(threadName);
    }
}
