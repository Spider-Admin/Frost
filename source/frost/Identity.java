package frost;

import java.lang.*;
import java.io.*;
import frost.FcpTools.*;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity implements Serializable
{
    private final String name;
    protected String key, keyaddress;
    protected transient FcpConnection con;
    public static final String NA = "NA";
    private static ThreadLocal tmpfile;

    /**
     * we use this constructor whenever we have all the info
     */
    public Identity(String name, String keyaddress, String key)
    {
        this.name = name;
        this.keyaddress = keyaddress;
        this.key = key;
    }

    /**
     * this constructor fetches the key from a SSK,
     * it blocks so it should be done from the TOFDownload thread (I think)
     */
    public Identity(String name, String keyaddress) throws IllegalArgumentException
    {
        this.name=name;
        this.keyaddress = keyaddress;
        try {
            con = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
        }
        catch( FcpToolsException e ) {
            System.out.println("fcptools exception");this.key=NA;
        }
        catch( IOException e ) {
            this.key=NA;
        }

        if( !keyaddress.startsWith("CHK@") )
        {
            this.key = NA;
            throw (new IllegalArgumentException("not a CHK"));
        }

        System.out.println("\n\n starting to request CHK for " + name +"with htl 25 \n\n");

        if( FcpRequest.getFile(keyaddress,"unknown",name + ".key","25",false) )
        {
            key = FileAccess.read(name +".key");
            System.out.println("\n\n CHK recieved for " +name + "\n\n");
        }
        else
        {
            key=NA;
            System.out.println("\n\n failed to get CHK for " +name +"\n\n");
        }
    }

    //obvious stuff
    public String getName()
    {
        return name;
    }
    public String getKeyAddress()
    {
        return keyaddress;
    }
    public String getKey()
    {
        return key;
    }
    public String getStrippedName()
    {
        return new String(name.substring(0,name.indexOf("@")));
    }
}
