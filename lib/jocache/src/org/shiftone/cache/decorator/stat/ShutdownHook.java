package org.shiftone.cache.decorator.stat;



import org.shiftone.cache.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * @version $Revision$
 * @author $Author$
 */
public class ShutdownHook extends Thread
{

    private static final Log     LOG       = new Log(ShutdownHook.class);
    private static final Runtime RUNTIME   = Runtime.getRuntime();
    private final List           cacheList = new ArrayList();
    private int                  maxCaches;
    private String               title = "[unnamed]";

    public ShutdownHook()
    {
        RUNTIME.addShutdownHook(this);
    }


    public void run()
    {

        LOG.info("shutdown : " + title);

        for (int i = 0; i < getCacheCount(); i++)
        {
            getStatCache(i).printStats();
        }
    }


    public void setTitle(String title)
    {
        this.title = title;
    }


    public void setMaxCaches(int maxCaches)
    {
        this.maxCaches = maxCaches;
    }


    public void addStatCache(StatCache statCache)
    {

        if (getCacheCount() <= maxCaches)
        {
            cacheList.add(statCache);
        }
    }


    public int getCacheCount()
    {
        return cacheList.size();
    }


    public StatCache getStatCache(int index)
    {
        return (StatCache) cacheList.get(index);
    }
}
