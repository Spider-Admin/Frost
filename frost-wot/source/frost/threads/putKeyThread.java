package frost.threads;

import java.io.*;

import frost.*;
import frost.FcpTools.*;
import frost.gui.model.UploadTableModel;
import frost.gui.objects.FrostUploadItemObject;

public class putKeyThread extends Thread {

    private String uri;
    private File uploadMe;
    private int htl;
    private String[][] results;
    private int index;
    private boolean mode;
    private FrostUploadItemObject uploadItem;
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
           tries < 8)
    {
        if( tries > 0 )
            mixed.wait(30000); // wait some time between 2 tries

        tries++;
        System.out.println("putKeyThread: Splitfile upload: " + tries);
        String output = new String();

        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if( connection != null )
        {
            try {
                output = connection.putKeyFromFile(uri, uploadMe.getPath(), htl, mode);
            }
            catch (IOException e) {
                System.out.println("putKeyThread: IOException"+e);
            }
        }

        result = result(output);
    }

    // maybe update upload table
    if( uploadItem != null &&
        ( result[0].equals("KeyCollision") || result[0].equals("Success") )
      )
    {
        // block successfully uploaded, update progress in uploadtable
        uploadItem.incUploadProgressDoneBlocks(); // 1 block more successful

        UploadTableModel model = (UploadTableModel)frame1.getInstance().getUploadTable().getModel();
        model.updateRow( uploadItem );
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
    public putKeyThread (String uri, File uploadMe, int htl, String[][] results, int index, boolean mode)
    {
        this.uri = uri;
        this.uploadMe = uploadMe;
        this.htl = htl;
        this.results = results;
        this.index = index;
        this.mode = mode;
        this.uploadItem = null;
    }

    public putKeyThread (String uri, File uploadMe, int htl, String[][] results, int index, boolean mode,
                         FrostUploadItemObject ulItem)
    {
        this(uri,uploadMe,htl,results,index,mode);
        this.uploadItem = ulItem;
    }


}
