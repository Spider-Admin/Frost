package frost;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import frost.FcpTools.*;
import frost.threads.*;

import frost.gui.objects.*;
import frost.gui.model.*;

/**
 * Requests a CHK key from freenet
 * @author <a href=mailto:jantho@users.sourceforge.net>Jan-Thomas Czornack</a>
 */
public class FcpInsert
{
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

    public static String[] putFile(String uri, String filename, String htl, boolean doRedirect, boolean mode,
                                   String boardfilename)
    {
        return putFile(uri, new File(filename), Integer.parseInt(htl), doRedirect, mode, boardfilename);
    }

    public static String[] putFile(String uri, File file, String htl, boolean doRedirect, boolean mode,
                                   String boardfilename)
    {
        return putFile(uri, file, Integer.parseInt(htl), doRedirect, mode, boardfilename);
    }

    /**
     * boardfilename is needed for FEC puts, e.g. for pubkey.txt set it to null.
     */
    public static String[] putFile(String uri, File file, int htl, boolean doRedirect, boolean mode,
                                   String boardfilename)
    {
        if( file.length() > 32000 && uri.startsWith("KSK@") )
        {
            if( doRedirect )
                return putFECSplitFile(boardfilename, uri, file, htl, mode);
            else
            {
                //String errorString[] = {"Error", "Error"};
                return new String[]{"Error", "Error"};//errorString;
            }
        }
        else
        {
            if( file.length() <= smallestChunk )
            {
                try
                {
                    FcpConnection connection = FcpFactory.getFcpConnectionInstance();
                    if( connection == null )
                        return new String[]{"Error", "Error"};

                    String output = connection.putKeyFromFile(uri, file.getPath(), htl, mode);
                    return result(output);
                }
                catch( UnknownHostException e )
                {
                    if( DEBUG ) System.out.println("UnknownHostException");
                    frame1.displayWarning(e.toString());
                }
                catch( IOException e )
                {
                    if( DEBUG ) System.out.println("IOException");
                    frame1.displayWarning(e.toString());
                }
                return result("");
            }
            else
            {
                return putFECSplitFile(boardfilename, uri, file, htl, mode);
            }
        }
    }

    /**
     * Updates the 'state' column for a file that is in table.
     */
    private static FrostUploadItemObject getUploadItemForFile(File file, boolean mode)
    {
        if( mode == false ) // uploading mode?
        {
            // no, generate key mode
            return null;
        }

        // TODO: do not search for item in table, but give the item to this method directly
        final String filePath = file.getPath();
        final UploadTableModel model = (UploadTableModel)frame1.getInstance().getUploadTable().getModel();

        // find the item in table by filepath
        FrostUploadItemObject ulItem = null;
        for( int i = 0; i < model.getRowCount(); i++ )
        {
            FrostUploadItemObject tmpUlItem = ((FrostUploadItemObject)model.getRow(i));
            if( filePath.equals(tmpUlItem.getFilePath()) )
            {
                ulItem = tmpUlItem;
                break;
            }
        }
        return ulItem;
    }

    public static String[] putFECSplitFile(String boardfilename, String uri, File file,
                                           int htl, boolean mode) {
    FcpFECUtils fecutils = null;
    Vector segmentHeaders = null;
    Vector segmentFileMaps = new Vector();
    Vector checkFileMaps = new Vector();
    Vector segmentKeyMaps = new Vector();
    Vector checkKeyMaps = new Vector();
    long fileLength = file.length();

    FrostUploadItemObject ulItem = getUploadItemForFile(file, mode);

    String output = new String();
    int maxThreads = frame1.frostSettings.getIntValue("splitfileUploadThreads");
    Thread[] chunkThreads = null;
    String[][] chunkResults = null;
    Thread[] checkThreads = null;
    String[][] checkResults = null;
    int threadCount = 0;

    {
        fecutils = new FcpFECUtils(frame1.frostSettings.getValue("nodeAddress"),
                                   frame1.frostSettings.getIntValue("nodePort"));
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
            fcpSock = new Socket(InetAddress.getByName(frame1.frostSettings.getValue("nodeAddress")),
                                 frame1.frostSettings.getIntValue("nodePort"));
            fcpSock.setSoTimeout(1800000);
            fcpOut = new PrintStream(fcpSock.getOutputStream());
            fcpIn = new BufferedInputStream(fcpSock.getInputStream());
            FileInputStream fileIn = new FileInputStream(file);
            File[] chunkFiles = new File[blockCount];
            {
                System.out.println("Processing segment " + i);
                fileIn.skip(((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).Offset);
                long segLength = ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockCount *
                                  ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockSize;
                System.out.println("segLength = " + Long.toHexString(segLength));
                String headerString = "SegmentHeader\n" + ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).reconstruct() + "EndMessage\n";
                String dataHeaderString = "\0\0\0\2FECEncodeSegment\nMetadataLength=" + Long.toHexString(headerString.length()) +
                                          "\nDataLength=" + Long.toHexString(headerString.length() + segLength) +
                                          "\nData\n" + headerString;
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
                File uploadMe = new File(frame1.keypool +
                                         String.valueOf(System.currentTimeMillis()) +
                                         "-" + chunkCnt + ".tmp");
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
                    uploadMe = new File(frame1.keypool +
                                        String.valueOf(System.currentTimeMillis()) +
                                        "-chk-" + checkCnt + ".tmp");
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

        // count totalBlocks
        if( ulItem != null )
        {
            int totalBlocks = 0;
            for (int i = 0; i < segmentFileMaps.size(); i++)
            {
                File[] currentFileMap = (File[])segmentFileMaps.get(i);
                totalBlocks += currentFileMap.length;
            }
            for (int i = 0; i < checkFileMaps.size(); i++)
            {
                File[] currentFileMap = (File[])checkFileMaps.get(i);
                totalBlocks += currentFileMap.length;
            }
            ulItem.setUploadProgressTotalBlocks( totalBlocks );
            ulItem.setUploadProgressDoneBlocks( 0 );
            ulItem.setState( ulItem.STATE_PROGRESS );
            UploadTableModel model = (UploadTableModel)frame1.getInstance().getUploadTable().getModel();
            model.updateRow( ulItem );
        }

        // upload all chunk blocks

        int chunkNo = 0;
//        long uploadedBytes = 0;
        for (int i = 0; i < segmentFileMaps.size(); i++) {
            File[] currentFileMap = (File[])segmentFileMaps.get(i);
        chunkThreads = new Thread[currentFileMap.length];   // We have as many results as we have files
        chunkResults = new String[currentFileMap.length][2];
        threadCount = 0;
        for (int j = 0; j < currentFileMap.length; j++){
            if (DEBUG) System.out.println("Chunk: " + chunkNo);

            while (getActiveThreads(chunkThreads) >= maxThreads)
            {
                mixed.wait(5000);
            }

            chunkThreads[threadCount] = new putKeyThread("CHK@",
                                currentFileMap[j],
                                htl,
                                chunkResults,
                                threadCount,
                                mode,
                                ulItem);
            chunkThreads[threadCount].start();
            threadCount++;
//            uploadedBytes += currentFileMap[j].length();
//            updateUploadTable(file, uploadedBytes, mode);
            mixed.wait(3000);
            chunkNo++;
        }
        // wait until all chunk upload threads are done
        while (getActiveThreads(chunkThreads) > 0) {
            if (DEBUG) System.out.println("Active Splitfile inserts remaining: " + getActiveThreads(chunkThreads));
            mixed.wait(8000);
        }
        segmentKeyMaps.add(chunkResults);
        }

// TODO: combine split + check files download (always run X theads)

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
            {
                mixed.wait(5000);
            }

            checkThreads[threadCount] = new putKeyThread("CHK@",
                                currentFileMap[j],
                                htl,
                                checkResults,
                                threadCount,
                                mode,
                                ulItem);
            checkThreads[threadCount].start();
            threadCount++;
//            uploadedBytes += currentFileMap[j].length();
//            updateUploadTable(file, uploadedBytes, mode);
            mixed.wait(3000);
            checkNo++;
        }
        // wait until all threads are done
        while (getActiveThreads(checkThreads) > 0) {
            if (DEBUG) System.out.println("Active Checkblock inserts remaining: " + getActiveThreads(checkThreads));
            mixed.wait(8000);
        }
        checkKeyMaps.add(checkResults);
        }

        checkThreads = null;

    }

    // Generate redirect
    System.out.println("Generating redirect ...");

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
        System.out.println("Uploading redirect, try= "+(tries+1)+" / 8");
        try {
        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if( connection != null )
            output = connection.putKeyFromFile(uri, null, redirect.getBytes(), htl, mode);
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
        System.out.println("Redirect successfully uploaded.");
        try{
        GregorianCalendar cal= new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        String dirdate = cal.get(Calendar.YEAR) + ".";
        dirdate += cal.get(Calendar.MONTH) + 1 + ".";
        dirdate += cal.get(Calendar.DATE);

        String fileSeparator = System.getProperty("file.separator");
        String destination = frame1.keypool + boardfilename + fileSeparator + dirdate + fileSeparator;
        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        // That's not yet clean. Original frost code requires to start the insert funktion
        // to generate the key, and here we process the results. Direct key generation
        // should replace that, then we can also remove the result method
        if( connection != null )
        {
            String contentKey = result(connection.putKeyFromFile(uri, null, redirect.getBytes(), htl, false))[1];
            String prefix = new String("freenet:");
            if (contentKey.startsWith(prefix)) contentKey = contentKey.substring(prefix.length());

            FileAccess.writeFile("Already uploaded today", destination + contentKey + ".lck");
        }
        }
        catch (Exception e) {}
    }
    else
    {
        System.out.println("Could not upload redirect file!");
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
