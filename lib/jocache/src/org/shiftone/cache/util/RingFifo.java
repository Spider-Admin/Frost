package org.shiftone.cache.util;



/**
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class RingFifo
{

    private Object[] array;
    private int      maxSize;
    private int      head;
    private int      size;

    public RingFifo(int maxSize)
    {

        this.maxSize = maxSize;

        clear();
    }


    public void clear()
    {

        array = new Object[maxSize];
        head  = 0;
        size  = 0;
    }


    public void enqueue(Object obj)
    {

        array[head] = obj;
        head        = (head + 1) % maxSize;

        if (size < maxSize)
        {
            size++;
        }
    }


    private final int peekIndex()
    {
        return (head + maxSize - size) % maxSize;
    }


    public Object peek()
    {
        return array[peekIndex()];
    }


    public Object dequeue()
    {

        Object obj = null;

        if (size > 0)
        {
            int index = peekIndex();

            obj          = array[index];
            array[index] = null;

            size--;
        }

        return obj;
    }


    public int getMaxSize()
    {
        return maxSize;
    }


    public int size()
    {
        return size;
    }


    public String dump()
    {

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < maxSize; i++)
        {
            if (i != 0)
            {
                sb.append(",");
            }

            if (array[i] != null)
            {
                sb.append(array[i]);
            }
        }

        System.out.println(sb);

        return sb.toString();
    }
}
