package org.shiftone.cache;



/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class ConfigurationException extends CacheException
{

    public ConfigurationException(String message)
    {
        super(message);
    }


    public ConfigurationException(String message, Throwable rootCause)
    {
        super(message, rootCause);
    }


    public ConfigurationException(Throwable cause)
    {
        super(cause);
    }
}
