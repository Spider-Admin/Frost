package frost.threads;

import java.io.*;
import java.net.*;
import java.util.*;

import frost.*;
import frost.FcpTools.*;

public class putKeyThread extends Thread {

    private String uri;
    private File uploadMe;
    private int htl;
    private String[][] results;
    private int index;
    private boolean mode;
    private static String[] keywords = {"Success",
                    "RouteNotFound",
                    "KeyCollision",
                    "SizeError",
                    "DataNotFound"};


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

    public void run() {

    int tries = 0;
    String[] result = {"Error", "Error"};
    while (!result[0].equals("Success") &&
           !result[0].equals("KeyCollision") &&
           tries < 8) {
        tries++;
        System.out.println("Splitfile upload: " + tries);
        String output = new String();
        try {
        FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
        try {
            output = connection.putKeyFromFile(uri, uploadMe.getPath(), htl, mode);
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        }
        catch (FcpToolsException e) {
        System.out.println("FcpToolsException " + e);
        frame1.displayWarning(e.toString());
        }
        catch (UnknownHostException e) {
        System.out.println("UnknownHostException");
        frame1.displayWarning(e.toString());
        }
        catch (IOException e) {
        System.out.println("IOException");
        frame1.displayWarning(e.toString());
        }

        result = result(output);
        mixed.wait(3000);
    }
    results[index][0] = result[0];
    results[index][1] = result[1];
    uploadMe.delete();
    System.out.println("*****" + result[0] + " " + result[1] + " " + index);
    }

    /**
     * Constructor
     * @param p Process to get the Input Stream from
     */
    public putKeyThread (String uri, File uploadMe, int htl, String[][] results, int index, boolean mode) {
    this.uri = uri;
    this.uploadMe = uploadMe;
    this.htl = htl;
    this.results = results;
    this.index = index;
    this.mode = mode;
    }

}
