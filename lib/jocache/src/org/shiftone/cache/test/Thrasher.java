package org.shiftone.cache.test;



import org.shiftone.cache.Cache;
import org.shiftone.cache.CacheFactory;
import org.shiftone.cache.decorator.sync.SyncCache;
import org.shiftone.cache.policy.fifo.FifoCacheFactory;
import org.shiftone.cache.util.Log;

import java.util.Random;


/**
 * Concurrent cache tester.
 *
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class Thrasher
{

    private static final Log LOG = new Log(Thrasher.class);
    private Thread[]         threads;    // running worker threads
    private Cache            cache;
    private int              cycles = 100000;

    public Thrasher(Cache cache, int threadCount)
    {

        LOG.message("new Thrasher for " + cache.getClass().getName() + " with " + threadCount + " threads");

        this.cache = cache;

        // this.cache   = new StatCache(this.cache);
        this.cache   = new SyncCache(this.cache);
        this.threads = new Thread[threadCount];
    }


    public void thrash()
    {

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new ThrasherRunnable());

            LOG.message("starting thread #" + i);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++)
        {
            try
            {
                LOG.message("waiting for thread #" + i + " to complete");
                threads[i].join();
            }
            catch (Exception e)
            {
                LOG.error("join failed for thread #" + i, e);
            }
        }
    }


    class ThrasherRunnable implements Runnable
    {

        private Random random = new Random();

        public void run()
        {

            try
            {
                for (int i = 0; i < cycles; i++)
                {
                    cache.addObject(new Integer(random.nextInt() % 100), "obj");
                    cache.getObject(new Integer(random.nextInt() % 100));

                    if (i % 5000 == 0)
                    {

                        /// LOG.info(i + " cycles complete " + cache);
                    }
                }
            }
            catch (Exception e)
            {

                /// LOG.error("run failed",e);
            }
        }
    }

    public static void main(String[] args) throws Exception
    {

        Cache        cache;
        CacheFactory cacheFactory;
        Class        factoryClass;
        String       factoryClassName = FifoCacheFactory.class.getName();
        int          size             = 100;
        int          ttl              = 2000;
        int          threads          = 1;      // running worker threads
        int          cycles           = 100;    // cycles per thread
        int          gpc              = 5;      // gets per cycle
        int          ppc              = 5;      // puts per cycle
        Thrasher     thrasher         = null;

        for (int i = 0; i < args.length; i++)
        {
            LOG.message("arg[ " + i + " ] = " + args[i]);
        }

        for (int i = 0; i < args.length; i++)
        {
            if ("-factory".equalsIgnoreCase(args[i]))
            {
                factoryClassName = args[++i];
            }

            if ("-size".equalsIgnoreCase(args[i]))
            {
                size = Integer.parseInt(args[++i]);
            }

            if ("-ttl".equalsIgnoreCase(args[i]))
            {
                ttl = Integer.parseInt(args[++i]);
            }

            if ("-threads".equalsIgnoreCase(args[i]))
            {
                threads = Integer.parseInt(args[++i]);
            }

            if ("-cycles".equalsIgnoreCase(args[i]))
            {
                cycles = Integer.parseInt(args[++i]);
            }

            if ("-gpc".equalsIgnoreCase(args[i]))
            {
                gpc = Integer.parseInt(args[++i]);
            }

            if ("-ppc".equalsIgnoreCase(args[i]))
            {
                ppc = Integer.parseInt(args[++i]);
            }
        }

        factoryClass = Class.forName(factoryClassName);
        cacheFactory = (CacheFactory) factoryClass.newInstance();
        cache        = cacheFactory.newInstance("thrasher", ttl, size);
        thrasher     = new Thrasher(cache, threads);

        thrasher.thrash();
    }
}
