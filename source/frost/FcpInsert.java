package frost;
import java.io.*;
import java.net.*;
import java.util.*;

import frost.FcpTools.*;
import frost.threads.*;

/**
 * Requests a CHK key from freenet
 * @author <a href=mailto:jantho@users.sourceforge.net>Jan-Thomas Czornack</a>
 */
public class FcpInsert {
    final static boolean DEBUG = true;
    final static int smallestChunk = 262144;

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

    public static String[] putFile(String uri, String filename, String htl, boolean doRedirect, boolean mode) {
    return putFile(uri, new File(filename), Integer.parseInt(htl), doRedirect, mode);
    }

    public static String[] putFile(String uri, File file, String htl, boolean doRedirect, boolean mode) {
    return putFile(uri, file, Integer.parseInt(htl), doRedirect, mode);
    }

    public static String[] putFile(String uri, File file, int htl, boolean doRedirect, boolean mode) {
    if (file.length() > 32000 && uri.startsWith("KSK@")) {
        if (doRedirect)
        return putFECSplitFile(uri, file, htl, mode);
        else {
        String errorString[] = {"Error", "Error"};
        return errorString;
        }
    }
    else {
        if (file.length() <= smallestChunk) {
        try {
            FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
            String output = connection.putKeyFromFile(uri, file.getPath(), htl, mode);
            return result(output);
        }
        catch (FcpToolsException e) {
            if (DEBUG) System.out.println("FcpToolsException " + e);
            frame1.displayWarning(e.toString());
        }
        catch (UnknownHostException e) {
            if (DEBUG) System.out.println("UnknownHostException");
            frame1.displayWarning(e.toString());
        }
        catch (IOException e) {
            if (DEBUG) System.out.println("IOException");
            frame1.displayWarning(e.toString());
        }
        return result("");
        }
        else {
        return putFECSplitFile(uri, file, htl, mode);
        }
    }
    }

    private static void updateUploadTable(File file, int progress, boolean mode) {
    if (mode) {
        // Need to synchronize table accesses
        synchronized (frame1.getInstance().getUploadTable()){
        // Does an exception prevent release of the lock, better catch them
        try{
            int rows = frame1.getInstance().getUploadTable().getModel().getRowCount();
            progress = progress/1024;
            String text = progress + "Kb";
            for (int i = 0; i < rows; i++) {
            if (file.getPath().equals(frame1.getInstance().getUploadTable().getModel().getValueAt(i, 3)))
                frame1.getInstance().getUploadTable().getModel().setValueAt(text, i, 2);
            }
        }
        catch (Exception e){}
        }
    }
    }

    private static String getBoard(File file) {
    String result = null;
    // Need to synchronize table accesses
    synchronized (frame1.getInstance().getUploadTable()){
        // Does an exception prevent release of the lock, better catch them
        try{
        int rows = frame1.getInstance().getUploadTable().getModel().getRowCount();
        for (int i = 0; i < rows; i++) {
            if (file.getPath().equals(frame1.getInstance().getUploadTable().getModel().getValueAt(i, 3)))
            result = (String)frame1.getInstance().getUploadTable().getModel().getValueAt(i, 4);
        }
        }
        catch (Exception e){}
    }
    return result;
    }

    public static String[] putSplitFile(String uri, File file, int htl, boolean mode) {
    int fileLength = (int)file.length();
    int chunkSize = smallestChunk;

    if (fileLength > 67108864)
        chunkSize = Math.abs(fileLength / 256);

    if (DEBUG) System.out.println("ChunkSize: " + chunkSize);

    int c;
    int chunk = 0;
    String output = new String();
    Thread[] threads = new Thread[fileLength / chunkSize + 1];
    int threadCount = 0;
    int maxThreads = frame1.frostSettings.getIntValue("splitfileUploadThreads");
    String[][] results = new String[threads.length][2];

    try {
        FileInputStream fileIn = new FileInputStream(file);

        // get all full size chunks
        if (DEBUG) System.out.println("File size: " + fileLength);
        for (int i = chunkSize; i <= fileLength; i += chunkSize) {
        if (DEBUG) System.out.println("Full Size Chunk: " + i);
        // read chunkSize bytes
        int bytesRead = 0;
        int count = 0;
        byte[] data = new byte[chunkSize];
        while (bytesRead < chunkSize) {
            count = fileIn.read(data, bytesRead, chunkSize - bytesRead);
            if (count < 0) {
            break;
            }
            else {
            bytesRead += count;
            }
        }

        File uploadMe = new File(frame1.keypool + String.valueOf(System.currentTimeMillis()) + ".tmp");
        uploadMe.deleteOnExit();
        FileAccess.writeByteArray(data, uploadMe);

        data = null;

        while (getActiveThreads(threads) >= maxThreads)
            mixed.wait(5000);

        threads[threadCount] = new putKeyThread("CHK@",
                            uploadMe,
                            htl,
                            results,
                            threadCount,
                            mode);
        threads[threadCount].start();
        threadCount++;
        updateUploadTable(file, i, mode);
        mixed.wait(1000);
        }

        // read chunkSize bytes
        int bytesRead = 0;
        int count = 0;
        int lastChunk = (fileLength - threadCount * chunkSize);

        if (lastChunk > 0) {
        if (DEBUG) System.out.println("lastChunk:" + lastChunk);
        byte[] lastPart = new byte[lastChunk];
        while (bytesRead < lastChunk) {
            count = fileIn.read(lastPart, bytesRead, lastChunk - bytesRead);
            if (count < 0) {
            break;
            }
            else {
            bytesRead += count;
            }
        }

        File uploadMe = new File(frame1.keypool + String.valueOf(System.currentTimeMillis()) + ".tmp");
        uploadMe.deleteOnExit();
        FileAccess.writeByteArray(lastPart, uploadMe);

        lastPart = null;

        if (uploadMe.length() > 0) {
            threads[threadCount] = new putKeyThread("CHK@",
                                uploadMe,
                                htl,
                                results,
                                threadCount,
                                mode);
            threads[threadCount].start();

            threadCount++;
        }

        lastPart = null;
        }

        // wait until all threads are done
        while (getActiveThreads(threads) > 0) {
        if (DEBUG) System.out.println("Active Splitfile inserts remaining: " + getActiveThreads(threads));
        mixed.wait(3000);
        }

        fileIn.close();
        threads = null;

    }
    catch (IOException e) {}

    // Generate redirect
    String redirect = new String();
    redirect = "Version\nRevision=1\nEndPart\nDocument\n";
    redirect += "SplitFile.Size=" + Integer.toHexString(fileLength) + "\n";
    redirect += "SplitFile.Blocksize=" + Integer.toHexString(chunkSize) + "\n";
    redirect += "SplitFile.BlockCount=" + Integer.toHexString(threadCount) + "\n";

    for (int i = 0; i < threadCount; i++) {
        String message = results[i][0];
        String chk = results[i][1];

        if (chk == null) { // Thread aborted without obvious reason (shit happens)
        String[] returnError = {"Error", "Error"};
        if (DEBUG) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (DEBUG) System.out.println("Chunk " + i + " thread returned no value!");
        if (DEBUG) System.out.println("Aborting upload of " + file.getName());
        if (DEBUG) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return returnError;
        }
        else {
        if (chk.indexOf("Error") != -1) {
            String[] returnError = {"Error", "Error"};
            if (DEBUG) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (DEBUG) System.out.println("Chunk " + i + " upload failed!");
            if (DEBUG) System.out.println("Aborting upload of " + file.getName());
            if (DEBUG) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return returnError;
        }
        }
        redirect += "SplitFile.Block." + Integer.toHexString(i + 1) + "=freenet:" + chk + "\n";
    }

    redirect += "Info.Format=Frost\n";
    redirect += "End\n";

    if (DEBUG) System.out.println(redirect);

    // Upload redirect file
    // Try 8 times
    // Only stop after Success or KeyCollision
    int tries = 0;
    String[] result = {"Error", "Error"};
    while (!result[0].equals("Success") &&
           !result[0].equals("KeyCollision") &&
           tries < 8) {
        tries++;
        try {
        FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
        output = connection.putKeyFromFile(uri, null, redirect.getBytes(), htl, mode);
        }
        catch (FcpToolsException e) {
        if (DEBUG) System.out.println("FcpToolsException " + e);
        frame1.displayWarning(e.toString());
        }
        catch (UnknownHostException e) {
        if (DEBUG) System.out.println("UnknownHostException");
        frame1.displayWarning(e.toString());
        }
        catch (IOException e) {
        if (DEBUG) System.out.println("IOException");
        frame1.displayWarning(e.toString());
        }

        result = result(output);
        mixed.wait(3000);
        if (DEBUG) System.out.println("*****" + result[0] + " " + result[1] + " ");
    }

    return result(output);
    }

    public static String[] putFECSplitFile(String uri, File file, int htl, boolean mode) {
        FcpFECUtils fecutils = null;
    Vector segmentHeaders = null;
    Vector segmentFileMaps = new Vector();
    Vector checkFileMaps = new Vector();
    Vector segmentKeyMaps = new Vector();
    Vector checkKeyMaps = new Vector();
    int fileLength = (int)file.length();

    String output = new String();
    int maxThreads = frame1.frostSettings.getIntValue("splitfileUploadThreads");
    Thread[] chunkThreads = null;
    String[][] chunkResults = null;
    Thread[] checkThreads = null;
    String[][] checkResults = null;
    int threadCount = 0;
    String board = getBoard(file);

    {
        fecutils = new FcpFECUtils(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getIntValue("nodePort"));
        synchronized (fecutils.getClass()) {
        // Does an exception prevent release of the lock, better catch them
        try{
            segmentHeaders = fecutils.FECSegmentFile("OnionFEC_a_1_2", fileLength);
        }
        catch (Exception e){}
        }
        int chunkCnt = 0;   // This counts splitfile chunks over the complete file
        int checkCnt = 0;
        // Make FEC check file
        synchronized (fecutils.getClass()){
        // Does an exception prevent release of the lock, better catch them
        try{
            Socket fcpSock;
            BufferedInputStream fcpIn;
            PrintStream fcpOut;
            for (int i = 0; i < segmentHeaders.size(); i++){
            // Send data to FEC encoder
            int blockCount = (int)((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockCount;
            int blockNo = 0;    // This counts splitfile chunks within the segment
            fcpSock = new Socket(InetAddress.getByName(frame1.frostSettings.getValue("nodeAddress")), frame1.frostSettings.getIntValue("nodePort"));
            fcpSock.setSoTimeout(1800000);
            fcpOut = new PrintStream(fcpSock.getOutputStream());
            fcpIn = new BufferedInputStream(fcpSock.getInputStream());
            FileInputStream fileIn = new FileInputStream(file);
            File[] chunkFiles = new File[blockCount];
            {
                System.out.println("Processing segment " + i);
                fileIn.skip(((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).Offset);
                long segLength = ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockCount * ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockSize;
                System.out.println("segLength = " + Long.toHexString(segLength));
                String headerString = "SegmentHeader\n" + ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).reconstruct() + "EndMessage\n";
                String dataHeaderString = "\0\0\0\2FECEncodeSegment\nMetadataLength=" + Long.toHexString(headerString.length()) + "\nDataLength=" + Long.toHexString(headerString.length() + segLength) + "\nData\n" + headerString;
                System.out.print(dataHeaderString);
                fcpOut.print(dataHeaderString);
                long count = 0;
                while (count < segLength){
                byte[] buffer = new byte[(int)((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockSize];
                System.out.println(Long.toHexString(((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).Offset+count));
                int inbytes = fileIn.read(buffer);
                if (inbytes < 0){
                    System.out.println("End of input file - no data");
                    for (int j = 0; j < buffer.length; j++) buffer[j] = 0;
                    inbytes = buffer.length;
                }
                if (inbytes < buffer.length){
                    System.out.println("End of input file - not enough data");
                    for (int j = inbytes; j < buffer.length; j++) buffer[j] = 0;
                    inbytes = buffer.length;
                }
                if (inbytes > segLength - count)
                    inbytes = (int)(segLength - count);
                fcpOut.write(buffer);
                File uploadMe = new File(frame1.keypool + String.valueOf(System.currentTimeMillis()) + "-" + chunkCnt + ".tmp");
                chunkFiles[blockNo] = uploadMe;
                uploadMe.deleteOnExit();
                FileOutputStream fileOut = new FileOutputStream(uploadMe);
                fileOut.write(buffer,0,(int)inbytes);
                fileOut.close();
                count += inbytes;
                chunkCnt++;;
                blockNo++;
                if (blockNo >= blockCount) break;
                }
                segmentFileMaps.add(chunkFiles);
                fcpOut.flush();
                fileIn.close();
            }
            // Fetch encoded blocks
            int checkNo = 0;
            int checkBlockCount = (int)((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).CheckBlockCount;
            File[] checkFiles = new File[checkBlockCount];
            File uploadMe = null;
            FileOutputStream outFile = null;
            {
                String currentLine;
                long checkBlockSize = ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).CheckBlockSize;
                int checkPtr = 0;
                int length = 0;

                do {
                boolean started = false;
                currentLine = fecutils.getLine(fcpIn).trim();
                if (currentLine.equals("DataChunk")){
                    started = true;
                }
                if (currentLine.startsWith("Length=")){
                    length = Integer.parseInt((currentLine.split("="))[1],16);
                }
                if (currentLine.equals("Data")){
                    int currentRead;
                    byte[] buffer = new byte[(int)length];
                    if (uploadMe == null){
                    uploadMe = new File(frame1.keypool + String.valueOf(System.currentTimeMillis()) + "-chk-" + checkCnt + ".tmp");
                    uploadMe.deleteOnExit();
                    outFile = new FileOutputStream(uploadMe);
                    }

                    currentRead = fcpIn.read(buffer);
                    while (currentRead < length){
                    currentRead += fcpIn.read(buffer,currentRead,length - currentRead);
                    }
                    outFile.write(buffer);
                    checkPtr += currentRead;
                    if (checkPtr == checkBlockSize){
                    // We received a complete check block
                    outFile.close();
                    checkFiles[checkNo] = uploadMe;
                    uploadMe = null;
                    checkNo++;
                    checkCnt++;
                    checkPtr = 0;
                    }
                }
                } while (currentLine.length() > 0);
                checkFileMaps.add(checkFiles);
            }

            fcpOut.close();
            fcpIn.close();
            fcpSock.close();
            }
        }
        catch (Exception e){System.out.println("putFECSplitFile NOT GOOD "+e.toString());}
        }

        // upload all chunk blocks

        int chunkNo = 0;
        int uploadedBytes = 0;
        for (int i = 0; i < segmentFileMaps.size(); i++) {
            File[] currentFileMap = (File[])segmentFileMaps.get(i);
        chunkThreads = new Thread[currentFileMap.length];   // We have as many results as we have files
        chunkResults = new String[currentFileMap.length][2];
        threadCount = 0;
        for (int j = 0; j < currentFileMap.length; j++){
            if (DEBUG) System.out.println("Chunk: " + chunkNo);

            while (getActiveThreads(chunkThreads) >= maxThreads)
            mixed.wait(5000);

            chunkThreads[threadCount] = new putKeyThread("CHK@",
                                currentFileMap[j],
                                htl,
                                chunkResults,
                                threadCount,
                                mode);
            chunkThreads[threadCount].start();
            threadCount++;
            uploadedBytes += currentFileMap[j].length();
            updateUploadTable(file, uploadedBytes, mode);
            mixed.wait(1000);
            chunkNo++;
        }
        // wait until all chunk upload threads are done
        while (getActiveThreads(chunkThreads) > 0) {
            if (DEBUG) System.out.println("Active Splitfile inserts remaining: " + getActiveThreads(chunkThreads));
            mixed.wait(3000);
        }
        segmentKeyMaps.add(chunkResults);
        }


        // upload all check blocks
        int checkNo = 0;
        for (int i = 0; i < checkFileMaps.size(); i++) {
            File[] currentFileMap = (File[])checkFileMaps.get(i);
        checkThreads = new Thread[currentFileMap.length];
        checkResults = new String[currentFileMap.length][2];
        threadCount = 0;
        for (int j = 0; j < currentFileMap.length; j++){
            if (DEBUG) System.out.println("Check: " + checkNo);

            while (getActiveThreads(checkThreads) >= maxThreads)
            mixed.wait(5000);

            checkThreads[threadCount] = new putKeyThread("CHK@",
                                currentFileMap[j],
                                htl,
                                checkResults,
                                threadCount,
                                mode);
            checkThreads[threadCount].start();
            threadCount++;
            uploadedBytes += currentFileMap[j].length();
            updateUploadTable(file, uploadedBytes, mode);
            mixed.wait(1000);
            checkNo++;
        }
        // wait until all threads are done
        while (getActiveThreads(checkThreads) > 0) {
            if (DEBUG) System.out.println("Active Checkblock inserts remaining: " + getActiveThreads(checkThreads));
            mixed.wait(3000);
        }
        checkKeyMaps.add(checkResults);
        }


        checkThreads = null;

    }

    // Generate redirect
    String redirect = null;
    {
        synchronized (fecutils.getClass()){
        // Does an exception prevent release of the lock, better catch them
        try{
            redirect = fecutils.FECMakeMetadata(segmentHeaders, segmentKeyMaps, checkKeyMaps, "Frost");
        }
        catch (Exception e){System.out.println("putFECSplitFile NOT GOOD "+e.toString());}
        }
        String[] sortedRedirect = redirect.split("\n");
        for (int z = 0; z < sortedRedirect.length; z++)
            System.out.println(sortedRedirect[z]);
        int sortStart = -1;
        int sortEnd = -1;
        for (int line = 0; line < sortedRedirect.length; line++){
            if (sortedRedirect[line].equals("Document")){
            sortStart = line + 1;
            break;
        }
        }
        for (int line = sortStart; line < sortedRedirect.length; line++){
            if (sortedRedirect[line].equals("End")){
            sortEnd = line;
            break;
        }
        }
        System.out.println("sortStart " + sortStart + " sortEnd " + sortEnd);
        if (sortStart < sortEnd)
            Arrays.sort(sortedRedirect, sortStart, sortEnd);
        redirect = new String();
        for (int line = 0; line < sortedRedirect.length; line++)
            redirect += sortedRedirect[line] + "\n";
        System.out.println(redirect);
    }

    // Upload redirect file
    // Try 8 times
    // Only stop after Success or KeyCollision
    int tries = 0;
    String[] result = {"Error", "Error"};
    while (!result[0].equals("Success") &&
           !result[0].equals("KeyCollision") &&
           tries < 8) {
        tries++;
        try {
        FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
        output = connection.putKeyFromFile(uri, null, redirect.getBytes(), htl, mode);
        }
        catch (FcpToolsException e) {
        if (DEBUG) System.out.println("FcpToolsException " + e);
        frame1.displayWarning(e.toString());
        }
        catch (UnknownHostException e) {
        if (DEBUG) System.out.println("UnknownHostException");
        frame1.displayWarning(e.toString());
        }
        catch (IOException e) {
        if (DEBUG) System.out.println("IOException");
        frame1.displayWarning(e.toString());
        }

        result = result(output);
        mixed.wait(3000);
        if (DEBUG) System.out.println("*****" + result[0] + " " + result[1] + " ");
    }


    if ((result[0].equals("Success") || result[0].equals("KeyCollision")) && mode){
        try{
        GregorianCalendar cal= new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        String dirdate = cal.get(Calendar.YEAR) + ".";
        dirdate += cal.get(Calendar.MONTH) + 1 + ".";
        dirdate += cal.get(Calendar.DATE);

        String fileSeparator = System.getProperty("file.separator");
        String destination = frame1.keypool + board + fileSeparator + dirdate + fileSeparator;
        FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
        // That's not yet clean. Original frost code requires to start the insert funktion
        // to generate the key, and here we process the results. Direct key generation
        // should replace that, then we can also remove the result method
        String contentKey = result(connection.putKeyFromFile(uri, null, redirect.getBytes(), htl, false))[1];
        String prefix = new String("freenet:");
        if (contentKey.startsWith(prefix)) contentKey = contentKey.substring(prefix.length());

        FileAccess.writeFile("Already uploaded today", destination + contentKey + ".lck");
        }
        catch (Exception e) {}
    }

    return result;
    }

    private static int getActiveThreads(Thread[] threads) {
    int count = 0;
    for (int i = 0; i < threads.length; i++) {
        if (threads[i] != null) {
        if (threads[i].isAlive())
            count++;
        }
    }
    return count;
    }

}
