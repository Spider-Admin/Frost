/*
 * Meltdown.java
 *
 * Created on February 22, 2003, 11:29 PM
 */

package fillament.meltdown;

/**
 *
 * @author  FillaMent
 */

import java.io.*;
import java.util.Hashtable;

import fillament.util.WorkQueue;

public class Meltdown {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String pSep = System.getProperty("file.separator");
        String outboxPath = "fillament"+pSep+"meltdown" + pSep + "outbox" + pSep;
        boolean keepGoing = true;
        Hashtable qs = new Hashtable();
        WorkQueue isQ = new WorkQueue();
        WorkQueue info = new WorkQueue();
        System.out.println("Queues added.");
        qs.put("Info", info);
        qs.put("Insert", isQ);
        InsertThread it = new InsertThread(qs);
        System.out.println("Starting insertThread");
        it.setName("Meltdown");
        it.start();
        while (keepGoing) {
            System.out.println("Probing...");
            File outBox = new File(outboxPath);
            File[] fileList = outBox.listFiles();
            if (fileList.length > 0) {
                for (int i = 0;i < fileList.length;i++) {
                    System.out.println("Inspecting filename " + fileList[i].getAbsoluteFile());
                    if (!fileList[i].getName().endsWith(".lck") && !fileList[i].getName().endsWith(".") && !(new File(fileList[i].getAbsoluteFile() + ".lck")).exists()) {
                        try {
                            (new File(fileList[i].getAbsoluteFile() + ".lck")).createNewFile();
                        } catch (IOException e) {
                            System.err.println("Could not create file " + fileList[i].getAbsoluteFile() + ".lck: " + e.getMessage());
                            System.exit(-1);
                        }
                        isQ.add(fileList[i]);
                        System.out.println("Added " + fileList[i].getName() + " to insert queue.");
                    }
                }
            }
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
            }
        }
    }
    
}
