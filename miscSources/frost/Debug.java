package frost;


// CLASS IS NOT USED YET ... BUT IT WILL!

public class Debug
{
    private static boolean debug = true;

    /**
     * Sets the debug mode.
     */
    public static void setDebug(boolean v)
    {
        debug = v;
    }

    /**
     * Returns true if debug mode is enabled.
     */
    public static boolean debug()
    {
        return debug;
    }

    /**
     * Returns true if debug mode is enabled AND if localDebug is also true.
     * Useable if you have a class variable MYDEBUG and check if debug should be written by:
     *  if(Debug.debug(MYDEBUG)) writeMyDebugOutput()
     */
    public static boolean debug( boolean localDebug )
    {
        return debug && localDebug;
    }
}