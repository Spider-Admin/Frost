package org.shiftone.cache;



import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class CacheException extends Exception
{

    private Throwable rootCause;

    public CacheException(String message)
    {
        super(message);
    }


    public CacheException(String message, Throwable rootCause)
    {
        super(message);
    }


    public CacheException(Throwable rootCause)
    {

        super(rootCause.getMessage());

        this.rootCause = rootCause;
    }


    public Throwable getRootCause()
    {
        return rootCause;
    }


    public void printStackTrace()
    {
        printStackTrace(System.out);
    }


    public void printStackTrace(PrintStream s)
    {
        printStackTrace(new PrintWriter(s));
    }


    public void printStackTrace(PrintWriter s)
    {

        super.printStackTrace(s);

        if (rootCause != null)
        {
            s.println("*** Root cause is :");
            rootCause.printStackTrace(s);
        }

        s.flush();
    }
}
