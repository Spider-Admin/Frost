package frost.threads;

import java.io.*;
// import java.net.*;

// import frost.*;
import frost.FcpTools.*;

/**
 * Reads a key from Freenet
 * @author Jan-Thomas Czornack
 * @version 010711
 */
public class getKeyThread extends Thread
{

    private boolean DEBUG = true;
    private String key;
    private File file;
    private int htl;
    private boolean[] results;
    private int index;
    private int checkSize;

    // remove later
    private static String[] keywords = {"Success",
        "RouteNotFound",
        "KeyCollision",
        "SizeError",
        "DataNotFound"};

    // remove later
    private static String[] result(String text)
    {
        String[] result = new String[2];
        result[0] = "Error";
        result[1] = "Error";

        for( int i = 0; i < keywords.length; i++ )
        {
            if( text.indexOf(keywords[i]) != -1 )
                result[0] = keywords[i];
        }
        if( text.indexOf("CHK@") != -1 )
        {
            result[1] = text.substring(text.lastIndexOf("CHK@"),
                                       text.lastIndexOf("EndMessage"));
            result[1] = result[1].trim();
        }
        else
        {
            result[1] = "Error";
        }
        return result;
    }
/*
    private boolean checkKey(String key, File file, int checkSize)
    {
        boolean isOk = false;
        // File exists?
        if( (file.length() == checkSize) || ((file.length() > 0) && (checkSize == -1)) )
        {
            // Check content of file
            if( !frame1.frostSettings.getBoolValue("reducedBlockCheck") )
            {
                try
                {    // Check content
                    FcpConnection connection = FcpFactory.getFcpConnectionInstance();
                    if( connection != null )
                    {
                        // That's not yet clean. Original frost code requires to start the insert funktion
                        // to generate the key, and here we process the results. Direct key generation
                        // should replace that, then we can also remove the result method
                        String contentKey = null;
                        int tries = 0;
                        int maxTries = 3; // try 3 times if connect errors occurs (node overloaded)
                        while( contentKey == null && tries < maxTries )
                        {
                            try
                            {
                                contentKey = result(connection.putKeyFromFile(key, file.getPath(), 0, false))[1];
                            }
                            catch( ConnectException e )
                            {
                                System.out.println("Exception in checkKey(): "+e.getMessage());
                                tries++;
                                mixed.wait(1750);
                            }
                        }
                        if( contentKey == null )
                        {
                            System.out.println("Error in checkKey(): FAILED to check key because of connection errors to node!");
                            return false;
                        }

                        String prefix = "freenet:";
                        if( contentKey.startsWith(prefix) ) contentKey = contentKey.substring(prefix.length());
                        if( key.startsWith(prefix) ) key = key.substring(prefix.length());
                        if( contentKey.compareTo(key) == 0 )
                        {
                            isOk = true;
                        }
                        else
                        {
                            // We have the file, but the key does not match the content
                            System.out.println("ERROR: We have file " + file.getName() + ", but the content does not match the key");
                        }
                    }
                }
                catch( UnknownHostException e )
                {
                    System.out.println(e.toString());
                    frame1.displayWarning(e.toString());
                }
                catch( IOException e )
                {
                    System.out.println(e.toString());
                    frame1.displayWarning(e.toString());
                }
            }
            else
            {  // Use without check of content
                isOk = true;
            }
        }
        return isOk;
    }
*/
    public void run()
    {
        boolean success = false;

// REMOVED because CHK generation is'nt longer supported in FcpConnection
// if REALLY needed convert to FcpTools.generateCHK ...

        // just for the case ...
/*        if( results[index] = checkKey(key, file, checkSize) )
        {
            if( DEBUG ) System.out.println("Chunk exists (skip request): " + file);
            return;
        }
*/
        if( DEBUG ) System.out.println("Requesting " + file.getName() + " with HTL " + htl + ". Size is " + checkSize + " bytes.");

        boolean exception = false;
        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if( connection != null )
        {
            try
            {
                connection.getKeyToFile(key, file.getPath(), htl);
            }
            catch( FcpToolsException e )
            {
                exception = true;
            }
            catch( IOException e )
            {
                exception = true;
            }
        }

        if( !exception && file.length() > 0 )
        {
//          REMOVED because CHK generation is'nt longer supported in FcpConnection
//          if REALLY needed convert to FcpTools.generateCHK ...
/*            if( results[index] = success = checkKey(key, file, checkSize) )
            {
                return;
            }*/
            return; // we hope chunk download was OK ;)
        }

        // if we come here, something failed, delete file
        results[index] = false;
        file.delete();
    }

    /**
     * Constructor
     * @param p Process to get the Input Stream from
     */
    public getKeyThread (String key, File file, int htl, boolean[] results, int index, int checkSize)
    {
        this.key = key;
        this.file = file;
        this.htl = htl;
        this.results = results;
        this.index = index;
        this.checkSize = checkSize;
    }

}
