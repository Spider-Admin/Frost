package org.shiftone.cache.util;



/**
 * I'm not trying to reinvent Log4j here, I just want most of this to get
 * compiled out.
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public final class Log
{

    public static final boolean  DEBUG         = false;
    public static final boolean  INFO          = true;
    public static final boolean  MESSAGE       = true;
    public static final boolean  WARNING       = true;
    public static final int      DEBUG_LEVEL   = 0;
    public static final int      INFO_LEVEL    = 1;
    public static final int      MESSAGE_LEVEL = 2;
    public static final int      WARNING_LEVEL = 3;
    public static final int      ERROR_LEVEL   = 4;
    public static final String[] LEVELS        = { "DEBUG", "INFO", "MESSAGE", "WARN", "ERROR" };
    public static final long     START         = System.currentTimeMillis();
    private final Class          klass;

    public Log(Class klass)
    {
        this.klass = klass;
    }


    public final void debug(Object object)
    {

        if (DEBUG)
        {
            log(DEBUG_LEVEL, object);
        }
    }


    public final void info(Object object)
    {

        if (INFO)
        {
            log(INFO_LEVEL, object);
        }
    }


    public final void warn(Object object)
    {

        if (WARNING)
        {
            log(WARNING_LEVEL, object);
        }
    }


    public final void message(Object object)
    {

        if (MESSAGE)
        {
            log(MESSAGE_LEVEL, object);
        }
    }


    public final void error(Object object, Throwable throwable)
    {
        log(ERROR_LEVEL, object);
        throwable.printStackTrace(System.out);
    }


    private void log(int level, Object object)
    {

        StringBuffer sb = new StringBuffer(30);

        sb.append(LEVELS[level]);
        sb.append(" ");
        sb.append(System.currentTimeMillis() - START);
        sb.append(" ");
        sb.append(klass.getName());
        sb.append(" - ");
        sb.append(object);
        System.out.println(sb.toString());
    }
}
