package org.shiftone.cache.config;



import org.shiftone.cache.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * This class treats methods case-insensitive.  This can cause a problem if
 * their are two setters with the same name in different case.  Don't do that.
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class BeanWrapper
{

    private static final Log LOG = new Log(BeanWrapper.class);
    private final Object     object;
    private final Class      klass;
    private Method[]         methods;
    private Map              setters = new HashMap();

    public BeanWrapper(Object object)
    {

        this.object  = object;
        this.klass   = object.getClass();
        this.methods = klass.getMethods();

        for (int i = 0; i < methods.length; i++)
        {
            Method method = methods[i];
            String name   = method.getName().toLowerCase();

            if ((name.startsWith("set"))                           //
                    && (method.getParameterTypes().length == 1)    //
                    && (method.getReturnType().equals(Void.TYPE)))
            {
                setters.put(name.substring(3), method);

                // LOG.info(name.substring(3) + " " + method.getReturnType());
            }
        }
    }


    public Class getWrappedObjectClass()
    {
        return klass;
    }


    public Object getWrappedObject()
    {
        return object;
    }


    public Method getSetter(String name) throws NoSuchMethodException
    {

        Method method = (Method) setters.get(name.toLowerCase());

        if (method == null)
        {
            throw new NoSuchMethodException("no setter for : " + name);
        }

        return method;
    }


    public Class getType(String name) throws NoSuchMethodException
    {
        return getSetter(name).getParameterTypes()[0];
    }


    public void setProperty(String name, String value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
    {
        setProperty(name, convert(value, getType(name)));
    }


    public void setProperty(String name, Object objectValue) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        LOG.debug(klass.getName() + " SET " + name + " -> " + objectValue);
        getSetter(name).invoke(object, new Object[]{ objectValue });
    }


    private Object convert(String val, Class type) throws IllegalArgumentException, ClassNotFoundException
    {

        String v = val.trim();

        if (val == null)
        {
            return null;
        }
        else if (Class.class.isAssignableFrom(type))
        {
            return Class.forName(val);
        }
        else if (String.class.isAssignableFrom(type))
        {
            return val;
        }
        else if (Integer.TYPE.isAssignableFrom(type) || Integer.class.isAssignableFrom(type))
        {
            return new Integer(v);
        }
        else if (Long.TYPE.isAssignableFrom(type) || Long.class.isAssignableFrom(type))
        {
            return new Long(v);
        }
        else if (Boolean.TYPE.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type))
        {
            if ("true".equalsIgnoreCase(v))
            {
                return Boolean.TRUE;
            }
            else if ("false".equalsIgnoreCase(v))
            {
                return Boolean.FALSE;
            }
        }

        throw new IllegalArgumentException("unable to convert '" + v + "' to '" + type.getName() + "' on class '" + klass.getName() + "'");
    }
}
