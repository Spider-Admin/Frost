package frost.threads;

import java.io.*;
import java.net.*;

import frost.*;
import frost.FcpTools.*;

/**
 * Reads a key from Freenet
 * @author Jan-Thomas Czornack
 * @version 010711
 */
public class getKeyThread extends Thread {

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
    private static String[] result(String text) {
    String[] result = new String[2];
    result[0] = "Error";
    result[1] = "Error";

    for (int i = 0; i < keywords.length; i++) {
        if (text.indexOf(keywords[i]) != -1)
        result[0] = keywords[i];
    }
    if (text.indexOf("CHK@") != -1) {
        result[1] = text.substring(text.lastIndexOf("CHK@"),
                       text.lastIndexOf("EndMessage"));
        result[1] = result[1].trim();
    }
    else {
        result[1] = "Error";
    }
    return result;
    }

    public static boolean checkKey(String key, File file, int checkSize)
    {
        boolean isOk = false;
    // File exists?
    if ((file.length() == checkSize) || ((file.length() > 0) && (checkSize == -1))) {
        // Check content of file
        if (!frame1.frostSettings.getBoolValue("reducedBlockCheck")){
        try{    // Check content
            FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
            // That's not yet clean. Original frost code requires to start the insert funktion
            // to generate the key, and here we process the results. Direct key generation
            // should replace that, then we can also remove the result method
            String contentKey = result(connection.putKeyFromFile(key, file.getPath(), 0, false))[1];
            String prefix = new String("freenet:");
            if (contentKey.startsWith(prefix)) contentKey = contentKey.substring(prefix.length());
            if (key.startsWith(prefix)) key = key.substring(prefix.length());
            if (contentKey.compareTo(key) == 0){
            isOk = true;
            }
            else {
            // We have the file, but the key does not match the content
            System.out.println("ERROR: We have file " + file.getName() + ", but the content does not match the key");
            }
        }
        catch (UnknownHostException e) {
            System.out.println(e.toString());
            frame1.displayWarning(e.toString());
        }
        catch (FcpToolsException e) {
            System.out.println(e.toString());
            frame1.displayWarning(e.toString());
        }
        catch (IOException e) {
            System.out.println(e.toString());
            frame1.displayWarning(e.toString());
        }
        }
        else {  // Use without check of content
        isOk = true;
        }
    }
    return isOk;
    }

    public void run() {

    frame1.updateDownloads = true;
    int tries = 0;
    boolean success = false;

    if (results[index] = checkKey(key, file, checkSize)){
        if (DEBUG) System.out.println("Chunk exists (skip request): " + file);
        return;
    }

    // Should we really loop here. Freenet tries for us anyhow and if we
    // fail, we'll continue wizh higher htl

    // TODO: FIX: DBG: changed loop for testing purposes

    while (!success && tries < 1) {
        if (DEBUG) System.out.println("Requesting " + file.getName() + " with HTL " + htl + ". Size is " + checkSize + " bytes.");
        if (DEBUG) System.out.println("Splitfile request (tries): " + tries);

        tries++;
        boolean exception = false;
        Exception lastException = new Exception();
        try {
        FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
        try {
            connection.getKeyToFile(key, file.getPath(), htl);
        }
        catch (FcpToolsException e) {
            exception = true;
            lastException = e;
        }
        catch (IOException e) {
            exception = true;
            lastException = e;
        }
        }
        catch (FcpToolsException e) {
        if (DEBUG) System.out.println("getKeyThread: FcpToolsException " + e);
        frame1.displayWarning(e.toString());
        }
        catch (UnknownHostException e) {
        exception = true;
        lastException = e;
        frame1.displayWarning(e.toString());
        }
        catch (IOException e) {
        exception = true;
        lastException = e;
        frame1.displayWarning(e.toString());
        }

        if (!exception && file.length() > 0) {
        if (results[index] = success = checkKey(key, file, checkSize)){
            return;
        }
        }

        if (!success) {
        if (DEBUG) {
            System.out.println("************   getKeyThread   ************************************");
            if (exception) System.out.println("Exception occured: " + lastException);
            System.out.println("Key: " + key);
            System.out.println("Target: " + file.getName());
            System.out.println("HTL: " + htl);
            System.out.println("Tries: " + tries);
            System.out.println("Retrieved: " + file.length() + " bytes.");
            System.out.println("******************************************************************");
        }
        results[index] = false;
        mixed.wait(3000);
        }
    } // end-of: while
    }

    /**
     * Constructor
     * @param p Process to get the Input Stream from
     */
    public getKeyThread (String key, File file, int htl, boolean[] results, int index, int checkSize) {
    this.key = key;
    this.file = file;
    this.htl = htl;
    this.results = results;
    this.index = index;
    this.checkSize = checkSize;
    }

}
