package org.shiftone.cache.util;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class WeakSet
{

    private static final Log LOG     = new Log(WeakSet.class);
    private WeakMap          weakMap = new WeakMap();
    private int              count   = 0;

    public int size()
    {
        return weakMap.size();
    }


    public boolean isEmpty()
    {
        return weakMap.isEmpty();
    }


    public void add(Object obj)
    {
        weakMap.put(new Integer(count++), obj);
    }


    public void clear()
    {
        weakMap.clear();
    }


    public Iterator iterator()
    {

        List     list     = new ArrayList();
        Set      keys     = weakMap.keySet();
        Iterator iterator = keys.iterator();

        while (iterator.hasNext())
        {
            Object key   = iterator.next();
            Object value = weakMap.get(key);

            if (value != null)
            {
                list.add(value);
            }
        }

        return list.iterator();
    }
}
