package org.shiftone.cache.decorator.miss;



import org.shiftone.cache.util.Log;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class TestMissHandler implements MissHandler
{

    private static final Log LOG                = new Log(TestMissHandler.class);
    private int              inFetchObjectCount = 0;

    public TestMissHandler()
    {
        LOG.info("new");
    }


    public Object fetchObject(Object key)
    {

        synchronized (this)
        {
            LOG.info("begin fetchObject");

            inFetchObjectCount++;
        }

        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e) {}

        synchronized (this)
        {
            LOG.info("end fetchObject");

            inFetchObjectCount--;
        }

        return "value for : " + key;
    }
}
