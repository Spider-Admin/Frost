package fillament.meltdown;

import FcpTools.*;
import fillament.util.*;
import java.io.*;
import java.util.*;

public class InsertThread extends WorkThread {
    
    Hashtable queues;
    WorkQueue info;
    FcpConnection fcp;
    static String pSep = System.getProperty("file.separator");
    String workingFile = "meltdown.ins";
    String meltdownPath = "fillament"+pSep+"meltdown";
    
    public InsertThread(Hashtable Queues) {
        this(Queues, 1000);
    }
    
    public InsertThread(Hashtable Queues, int SleepTime) {
        super((WorkQueue) Queues.get("Insert"),"InsertThread", SleepTime);
        info = (WorkQueue) Queues.get("Info");
        try {
            fcp = new FcpConnection("127.0.0.1",8481,15);
        } catch (java.net.UnknownHostException e) {
            System.err.println("UnknownHostException in InsertThread: " + e.getMessage());
            System.exit(-1);
        } catch (FcpToolsException e) {
            System.err.println("FcpToolsException in InsertThread: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("IOException in InsertThread: " + e.getMessage());
            System.exit(-1);
        }
    }
    
    public void doWork(Object o) {
        File outMsg = (File) o;
        String board = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(outMsg)));
            if (br.ready()) {
                do {
                    String monkey = br.readLine();
                    if (monkey.startsWith("board=")) {
                        board = monkey.substring(monkey.indexOf("=") + 1);
                    }
                } while (br.ready() && board.equals(""));
            }
            br.close();
            br = null;
        } catch (IOException e) {
            System.err.println("IOException getting boardname: " + e.getMessage());
        }
        String inKeySpace = "KSK@sftmeage/news/" + board + "/";
        String outKeySpace = "KSK@sftmeage/news/" + board + "/";

        String keyFolderString = pSep + "keys" + pSep;
        File keyFile = new File(meltdownPath + keyFolderString + board.toLowerCase() + ".key");
        if (keyFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(keyFile)));
                while (br.ready()) {
                    String line = br.readLine();
                    if (line.startsWith("privateKey=")) {
                        outKeySpace = line.substring(line.indexOf("=") + 1) + "/" + board + "/";
                    } else if (line.startsWith("publicKey=")) {
                        inKeySpace = line.substring(line.indexOf("=") + 1) + "/" + board + "/";
                    }
                }
            } catch (IOException e) {
                System.err.println("IOException getting keys: " + e.getMessage());
            }
        }
        String thisDate = Message.getFrostDate(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
        int index = 0;
        boolean gotThatShit = false;
        while (!gotThatShit) {
            String getKey = inKeySpace + thisDate + "-" + index + ".txt";
            try {
                System.out.println("Checking for " + getKey);
                fcp.getKeyToFile(getKey,workingFile);
            } catch (FcpToolsException e) {
                System.err.println("FcpToolsException getting " + getKey + ": " + e.getMessage());
            } catch (IOException e) {
                System.err.println("IOException getting " + getKey + ": " + e.getMessage());
            }
            File tmpFile = new File(workingFile);
            if (tmpFile.exists()) {
                index++;
                tmpFile.delete();
            } else {
                gotThatShit = true;
            }
            tmpFile = null;
        }
        String putKey = outKeySpace + thisDate + "-" + index + ".txt";
        try {
            System.out.println("Inserting " + outMsg.getAbsolutePath() + " " + putKey);
            fcp.putKeyFromFile(putKey,outMsg.getAbsolutePath(),true);
            outMsg.delete();
            (new File(outMsg.getAbsoluteFile() + ".lck")).delete();
            //} catch (FcpToolsException e) {
            //        System.err.println("FcpToolsException getting " + inKey + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException putting " + putKey + ": " + e.getMessage());
        }
    }
}
