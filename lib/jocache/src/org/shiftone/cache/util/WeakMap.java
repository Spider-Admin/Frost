package org.shiftone.cache.util;



import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class WeakMap implements Map
{

    private static final Log LOG            = new Log(WeakMap.class);
    private ReferenceQueue   referenceQueue = new ReferenceQueue();
    private Map              referenceMap   = new TreeMap();

    public Object put(Object key, Object value)
    {

        purge();

        return referenceMap.put(key, new KeyedWeakReference(key, value));
    }


    public void putAll(Map map)
    {

        Set      set      = map.entrySet();
        Iterator iterator = set.iterator();

        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();

            put(entry.getKey(), entry.getValue());
        }
    }


    public Object get(Object key)
    {

        purge();

        Reference ref = (Reference) referenceMap.get(key);

        return (ref != null)
               ? ref.get()
               : null;
    }


    public void clear()
    {
        purge();
        referenceMap.clear();
    }


    public Object remove(Object key)
    {

        purge();

        return referenceMap.remove(key);
    }


    public Set keySet()
    {

        purge();

        return referenceMap.keySet();
    }


    public boolean containsKey(Object key)
    {

        purge();

        return referenceMap.containsKey(key);
    }


    public int size()
    {

        purge();

        return referenceMap.size();
    }


    public boolean isEmpty()
    {

        purge();

        return referenceMap.isEmpty();
    }


    public void purge()
    {

        KeyedWeakReference ref;

        while ((ref = (KeyedWeakReference) referenceQueue.poll()) != null)
        {
            LOG.info("referenceQueue purge : " + ref.getKey() + " ; size=" + size());
            referenceMap.remove(ref.getKey());
        }
    }


    public boolean containsValue(Object value)
    {
        throw new UnsupportedOperationException("containsValue");
    }


    public Collection values()
    {
        throw new UnsupportedOperationException("values");
    }


    public Set entrySet()
    {
        throw new UnsupportedOperationException("entrySet");
    }


    class KeyedWeakReference extends WeakReference
    {

        private final Object key;

        public KeyedWeakReference(Object key, Object referent)
        {

            super(referent, referenceQueue);

            this.key = key;
        }


        public Object getKey()
        {
            return key;
        }
    }
}
