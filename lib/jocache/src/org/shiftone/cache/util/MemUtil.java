package org.shiftone.cache.util;



/**
 * Class MemUtil
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class MemUtil
{

    /**
     * returns percent (100 based) of memory that is "free"
     */
    public static double freeMemoryPct()
    {

        Runtime runtime     = Runtime.getRuntime();
        double  freeMemory  = (double) runtime.freeMemory();
        double  totalMemory = (double) runtime.totalMemory();
        double  percentFree = freeMemory / totalMemory * 100.0;

        return percentFree;
    }
}
