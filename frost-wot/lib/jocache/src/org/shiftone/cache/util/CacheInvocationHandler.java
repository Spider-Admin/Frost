package org.shiftone.cache.util;



import org.shiftone.cache.Cache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;


/**
 * Class CacheInvocationHandler is used to create cached proxies
 *
 * @see org.shiftone.cache.CacheProxy
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class CacheInvocationHandler implements InvocationHandler
{

    private Cache  cache  = null;
    private Object target = null;

    public CacheInvocationHandler(Object target, Cache cache)
    {
        this.cache  = cache;
        this.target = target;
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {

        Object    result    = null;
        Throwable throwable = null;
        CallKey   callKey   = null;

        callKey = new CallKey(method.getName(), args);

        try
        {
            result = cache.getObject(callKey);

            if (result == null)
            {
                result = method.invoke(target, args);

                cache.addObject(callKey, result);
            }
        }
        catch (UndeclaredThrowableException e)
        {
            throw e.getUndeclaredThrowable();
        }

        return result;
    }
}

class CallKey
{

    private final String   methodName;
    private final Object[] args;

    public CallKey(String methodName, Object[] args)
    {
        this.methodName = methodName;
        this.args       = args;
    }


    public int hashCode()
    {

        int code = methodName.hashCode();

        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                code = (31 * code) + args[i].hashCode();
            }
        }

        return code;
    }


    public boolean equals(Object o)
    {

        if (this == o)
        {
            return true;
        }

        if (!(o instanceof CallKey))
        {
            return false;
        }

        final CallKey callKey = (CallKey) o;

        if (!methodName.equals(callKey.methodName))
        {
            return false;
        }

        if (!Arrays.equals(args, callKey.args))
        {
            return false;
        }

        return true;
    }
}
