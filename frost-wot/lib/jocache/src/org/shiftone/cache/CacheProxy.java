package org.shiftone.cache;



import org.shiftone.cache.util.CacheInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


/**
 * Static class CacheProxy can be used to create cache proxy objects of
 * instances of objects that implement an interface.
 * <pre>
 * Thing thing = new ThingImpl();
 * Thing cachedThing =
 *         (Thing)CacheProxy.newProxyInstance(thing, Thing.class, cache);
 *
 * cachedThing.doThing();
 * </pre>
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class CacheProxy
{

    private static final ClassLoader DEFAULT_CLASS_LOADER = CacheProxy.class.getClassLoader();

    public static Object newProxyInstance(ClassLoader loader, Object target, Class iface, Cache cache) throws IllegalArgumentException
    {

        InvocationHandler handler = null;
        Class[]           ifaces  = new Class[]{ iface };

        handler = new CacheInvocationHandler(target, cache);

        return Proxy.newProxyInstance(loader, ifaces, handler);
    }


    public static Object newProxyInstance(Object target, Class iface, Cache cache) throws IllegalArgumentException
    {
        return newProxyInstance(DEFAULT_CLASS_LOADER, target, iface, cache);
    }
}
