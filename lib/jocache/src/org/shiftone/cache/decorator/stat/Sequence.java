package org.shiftone.cache.decorator.stat;



/**
 * @version $Revision$
 * @author $Author$
 */
public class Sequence
{

    private long value = 0;

    public synchronized void increment()
    {
        value++;
    }


    public synchronized long getValue()
    {
        return value;
    }


    public String toString()
    {
        return String.valueOf(value);
    }
}
