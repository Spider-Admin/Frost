package frost;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

import frost.gui.model.*;
import frost.gui.objects.*;

import frost.FcpTools.*;
import frost.threads.*;
import fillament.util.*;

/**
 * Requests a key from freenet
 * @author <a href=mailto:jantho666@hotmail.com>Jan-Thomas Czornack</a>
 */

// TODO: mark checked blocks marked and dont recheck them on every download
// check nextDownload behaviour!
public class FcpRequest
{
    final static boolean DEBUG = true;
    final static int chunkSize = 262144;

    private static ThreadLocal healingQueue = new ThreadLocal();
    private static ThreadLocal healer = new ThreadLocal();
    //private static ThreadLocal toheal = new ThreadLocal();

    private static class HealerThread extends Thread
    {
        private WorkQueue wq;
        private String Name;
        public HealerThread(WorkQueue wq, String Name)
        {
            this.Name = Name;
            this.wq = wq;
        }
        public void run()
        {
            System.out.println("Healer starting for " + Name);
            String [][]results = new String[1][2];

            while( wq.hasMore() )
            {
                File block = (File)wq.next();
                System.out.println("\nTrying to heal " + Name + " with " + block.getPath());
                Thread inserter = new putKeyThread("CHK@",block,5,results,0,true);
                inserter.run();
                block.delete();
            }
            System.out.println("Healer ending for " + Name);
        }
    }

    private static int getActiveThreads(Thread[] threads)
    {
        int count = 0;
        for( int i = 0; i < threads.length; i++ )
        {
            if( threads[i] != null )
            {
                if( threads[i].isAlive() )
                    count++;
            }
        }
        return count;
    }

    private static boolean getFECSplitFile(String key, File target, int htl, FrostDownloadItemObject dlItem)
    {

        Vector toHeal = new Vector();
        //toheal.set(toHeal);
        //make the healing queue and thread
        healingQueue.set(new WorkQueue());
        HealerThread hl = new HealerThread((WorkQueue)(healingQueue.get()),target.getPath());
        healer.set(hl);

        Vector segmentHeaders = null;
        FcpFECUtils fecutils = new FcpFECUtils(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getIntValue("nodePort"));
        boolean success = true;

        int maxThreads = 3;
        maxThreads = frame1.frostSettings.getIntValue("splitfileDownloadThreads");

        long splitFileSize = -1;
        try
        {
            splitFileSize = Long.parseLong(SettingsFun.getValue(target.getPath(), "SplitFile.Size"), 16);
        }
        catch( NumberFormatException e )
        {}

        {
            synchronized (fecutils.getClass())
            {
                // Does an exception prevent release of the lock, better catch them
                try
                {
                    segmentHeaders = fecutils.FECSegmentFile("OnionFEC_a_1_2", splitFileSize);
                }
                catch( Exception e )
                {
                    System.out.println("exeption in getfecsplit " +e.toString());
                }
            }
        }

        int totalRequiredBlocks = 0;
        int totalAvailableBlocks = 0;
        for( int segmentCnt = 0; segmentCnt < segmentHeaders.size(); segmentCnt++ )
        {
            FcpFECUtilsSegmentHeader currentSegment = (FcpFECUtilsSegmentHeader)segmentHeaders.get(segmentCnt);
            totalRequiredBlocks += currentSegment.BlocksRequired;
            totalAvailableBlocks += currentSegment.BlockCount + currentSegment.CheckBlockCount;
        }
        //System.out.println("FILESIZE="+splitFileSize);
        //System.out.println("BLOCKS_REQUIRED ="+totalRequiredBlocks);
        //System.out.println("BLOCKS_AVAILABLE="+totalAvailableBlocks);
        int totalSuccessfulBlocks = 0;

        if( dlItem != null )
        {
            // set filesize as given in root split file
            dlItem.setFileSize( splitFileSize );

            // update gui table
            dlItem.setBlockProgress( 0,
                                     totalRequiredBlocks,
                                     totalAvailableBlocks);
            dlItem.setState( dlItem.STATE_PROGRESS );

            ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
        }

        // step through all segments and try to get required count of blocks for each
        for( int segmentCnt = 0; segmentCnt < segmentHeaders.size(); segmentCnt++ )
        {
            FcpFECUtilsSegmentHeader currentSegment = (FcpFECUtilsSegmentHeader)segmentHeaders.get(segmentCnt);
            int blockCount = (int)currentSegment.BlockCount;
            int blockSize = (int)currentSegment.BlockSize;
            int checkBlockSize = (int)currentSegment.CheckBlockSize;
            int checkBlockCount = (int)currentSegment.CheckBlockCount;
            int requiredBlocks = (int)currentSegment.BlocksRequired;
            int chunkBase = (int)currentSegment.DataBlockOffset;
            int checkBase = (int)currentSegment.CheckBlockOffset;
            int totalBlocks = blockCount + checkBlockCount;
            int blockNo = 0;    // This counts splitfile chunks within the segment

            // Put ascending numbers into array
            int[] blockNumbers = new int[totalBlocks];
            for( int i = 0; i < totalBlocks; i++ )
                blockNumbers[i] = i;

            // CofE's Chunkmixer
            Random rand = new Random(System.currentTimeMillis());
            for( int i = 0; i < totalBlocks; i++ )
            {
                int tmp = blockNumbers[i];
                int randomNumber = Math.abs(rand.nextInt()) % totalBlocks;
                blockNumbers[i] = blockNumbers[randomNumber];
                blockNumbers[randomNumber] = tmp;
            }

            boolean[] results = new boolean[totalBlocks];
            Thread[] threads = new Thread[totalBlocks];
            int successfullBlocks = 0;

            // Iterate over chunks and checks already on disc
            // and mark them succeeded. They are  not requested again

            int availableChunks = 0;
            for( int i = 0; i < totalBlocks; i ++ )
            {
                int j = blockNumbers[i];
                results[j] = false;
                if( j < blockCount )
                {
                    File chunkFile = new File( new StringBuffer()
                                               .append(frame1.keypool)
                                               .append(target.getName())
                                               .append("-chunk-")
                                               .append((j + chunkBase + 1)).toString() );
                    boolean chunkOK = getKeyThread.checkKey(
                                             SettingsFun.getValue(target.getPath(),
                                                "SplitFile.Block." + Integer.toHexString(j+chunkBase+1)),
                                             chunkFile,
                                             blockSize );
                    if( chunkOK )
                    {
                        results[j] = true;
                        availableChunks++;
                        successfullBlocks++;
                    }
                }
                else
                {
                    File checkFile = new File( new StringBuffer()
                                               .append(frame1.keypool)
                                               .append(target.getName())
                                               .append("-check-")
                                               .append((j + checkBase + 1)).toString() );
                    boolean chunkOK = getKeyThread.checkKey(
                                             SettingsFun.getValue(target.getPath(),
                                                "SplitFile.Block." + Integer.toHexString(j+checkBase+1)),
                                             checkFile,
                                             checkBlockSize );
                    if( chunkOK )
                    {
                        results[j] = true;
                        successfullBlocks++;
                    }
                }
            }

            // update gui table
            if( dlItem != null )
            {
                dlItem.setBlockProgress( (totalSuccessfulBlocks + successfullBlocks),
                                         totalRequiredBlocks, totalAvailableBlocks);
                ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
            }

            System.out.println(new StringBuffer().append("Found ").append(availableChunks)
                               .append(" chunks on disc for segment ").append(segmentCnt).toString());

            // Already have all data for the segment?
            if( availableChunks == requiredBlocks )
            {
                System.out.println("Already have all chunks on disc for segment " + segmentCnt);
                // And now remove check blocks, we don't need them any more
                for( int i = 0; i < totalBlocks; i ++ )
                {
                    int j = blockNumbers[i];
                    if( j >= blockCount )
                    {
                        File chunk = new File(new StringBuffer().append(frame1.keypool)
                                              .append(target.getName()).append("-check-")
                                              .append((j + checkBase + 1 )).toString()
                                             );
                        if( chunk.exists() )
                            chunk.delete();
                    }
                }
                continue;
            }

            int i = 0;
            while( successfullBlocks < requiredBlocks )
            {
                int j;
                String chk;

                // Skip asking for blocks already there
                if( i < totalBlocks && results[blockNumbers[i]] == true )
                {
                    i++;
                    continue;
                }

                if( i < totalBlocks )
                {
                    j = blockNumbers[i];

                    // Decide whether we ask for a chunk block or a check block
                    if( j < blockCount )
                        chk = SettingsFun.getValue(target.getPath(), "SplitFile.Block." + Integer.toHexString(j+chunkBase+1));
                    else
                        chk = SettingsFun.getValue(target.getPath(), "SplitFile.CheckBlock." + Integer.toHexString(j + checkBase - blockCount+1));

                    // Do not exceed maxThreads limit
                    while( getActiveThreads(threads) >= maxThreads )
                    {
                        mixed.wait(5000);

                        // Calculate number of successfull blocks
                        successfullBlocks = 0;
                        for( int k = 0; k < totalBlocks; k++ )
                        {
                            if( results[k] )
                            {
                                successfullBlocks++;
                            }
                        }

                        // update gui table
                        if( dlItem != null )
                        {
                            dlItem.setBlockProgress( (totalSuccessfulBlocks + successfullBlocks),
                                                     totalRequiredBlocks, totalAvailableBlocks);
                            ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
                        }
                    }

                    // Calculate number of successfull blocks
                    successfullBlocks = 0;
                    for( int k = 0; k < totalBlocks; k++ )
                    {
                        if( results[k] )
                        {
                            successfullBlocks++;
                        }
                    }

                    // update gui table
                    if( dlItem != null )
                    {
                        dlItem.setBlockProgress( (totalSuccessfulBlocks + successfullBlocks),
                                                 totalRequiredBlocks, totalAvailableBlocks);
                        ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
                    }

                    // If the sum of successfull blocks and outstanding threads is not
                    // enough to reconstruct the segment, start a new thread
                    if( getActiveThreads(threads) + successfullBlocks < requiredBlocks )
                    {
                        if( j < blockCount )
                            threads[j] = new getKeyThread(chk,
                                                          new File(new StringBuffer().append(frame1.keypool).append(target.getName())
                                                                   .append("-chunk-").append((j + chunkBase + 1)).toString()
                                                                  ),
                                                          htl,
                                                          results,
                                                          j,
                                                          blockSize);
                        else
                            threads[j] = new getKeyThread(chk,
                                                          new File(new StringBuffer().append(frame1.keypool).append(target.getName())
                                                                   .append("-check-").append((j + checkBase + 1)).toString()
                                                                  ),
                                                          htl,
                                                          results,
                                                          j,
                                                          checkBlockSize);
                        threads[j].start();
                        i++;
                    }
                    else
                    {
                        mixed.wait(5000);
                    }
                }
                else
                {
//                    System.out.println("Reached totalBlocks, still outstanding: " + getActiveThreads(threads) );
                    if( getActiveThreads(threads) == 0 )
                    {
                        // leave with no success, if we have searched for all the blocks
                        // and could not find enough
                        success = false;
                        break;
                    }
                    mixed.wait(5000);
                }
            }

            // wait until all threads are done
            // should however never see that
            while( getActiveThreads(threads) > 0 )
            {
                if( DEBUG ) System.out.println("Should not occur: Active Splitfile request remaining (htl " + htl + "): " + getActiveThreads(threads));
                mixed.wait(5000);
            }

            // Each request thread stores it's result in results[]
            // We need to verify that all threads finished successfully
            if( !success )
            {
                if( DEBUG ) System.out.println("NO SUCCESS Segment " + (int)currentSegment.SegmentNum);
            }
            else
            {
                // we have the required count of blocks received
                totalSuccessfulBlocks += requiredBlocks;

                if( DEBUG ) System.out.println("SUCCESS Segment " + (int)currentSegment.SegmentNum);
                // Calculate missing blocks here
                String blockList = new String();
                String checkList = new String();
                String requestList = new String();
                int suppliedBlocks = 0;
                int suppliedChecks = 0;
                for( i = 0; i < blockCount; i++ )
                {
                    if( results[i] )
                    {
                        if( blockList.length() > 0 ) blockList += ",";
                        blockList += Integer.toHexString(i);
                        suppliedBlocks++;
                    }
                    else
                    {
                        if( requestList.length() > 0 ) requestList += ",";
                        requestList += Integer.toHexString(i);
                    }
                }
                for( i = 0; i < checkBlockCount; i++ )
                {
                    if( results[i + blockCount] )
                    {
                        if( checkList.length() > 0 ) checkList += ",";
                        checkList += Integer.toHexString(i + blockCount);
                        suppliedChecks++;
                    }
                    else
                    { //also request all the checks.
                        if( requestList.length() > 0 ) requestList += ",";
                        requestList += Integer.toHexString(i + blockCount);
                    }
                }
                Socket fcpSock;
                BufferedInputStream fcpIn;
                PrintStream fcpOut;
                {
                    synchronized (fecutils.getClass())
                    {
                        // Does an exception prevent release of the lock, better catch them
                        try
                        {
                            fcpSock = new Socket(InetAddress.getByName(frame1.frostSettings.getValue("nodeAddress")), frame1.frostSettings.getIntValue("nodePort"));
                            fcpSock.setSoTimeout(1800000);
                            fcpOut = new PrintStream(fcpSock.getOutputStream());
                            fcpIn = new BufferedInputStream(fcpSock.getInputStream());
                            String headerString = new StringBuffer().append("SegmentHeader\n")
                                                  .append(currentSegment.reconstruct()).append("EndMessage\n").toString();
                            String dataHeaderString = new StringBuffer().append("\0\0\0\2FECDecodeSegment\n")
                                                      .append("BlockList=").append(blockList).append("\n")
                                                      .append("CheckList=").append(checkList).append("\n")
                                                      .append("RequestedList=").append(requestList).append("\n")
                                                      .append("MetadataLength=").append(Long.toHexString(headerString.length())).append("\n")
                                                      .append("DataLength=")
                                                      .append(Long.toHexString(headerString.length() + suppliedBlocks * currentSegment.BlockSize + suppliedChecks * currentSegment.CheckBlockSize))
                                                      .append("\n")
                                                      .append("Data\n").append(headerString).toString();
//                            System.out.print(dataHeaderString);
                            fcpOut.print(dataHeaderString);
                            String[] Elements = blockList.split(",");
                            byte[] buffer;
                            for( i = 0; i < Elements.length; i++ )
                            {
                                int bytesRead;
                                System.out.println("Transferring data for chunk " + Elements[i]);
                                File chunkFile = new File(frame1.keypool + target.getName() + "-chunk-" + (Integer.parseInt(Elements[i],16) + chunkBase + 1));
                                chunkFile.deleteOnExit();
                                FileInputStream inFile = new FileInputStream(chunkFile);
                                buffer = new byte[(int)currentSegment.BlockSize];
                                bytesRead = inFile.read(buffer);
                                if( bytesRead < buffer.length )
                                {
                                    System.out.println("Not enough input data for chunk " + (Integer.parseInt(Elements[i],16) + chunkBase + 1) + " - filling");
                                    for( int j = bytesRead; j < buffer.length; j++ ) buffer[j] = 0;
                                }
                                inFile.close();
                                fcpOut.write(buffer);
                            }
                            Elements = checkList.split(",");
                            for( i = 0; i < Elements.length; i++ )
                            {
                                int bytesRead;
                                System.out.println("Transferring data for check " + Elements[i]);
                                File checkFile = new File(frame1.keypool + target.getName() + "-check-" + (Integer.parseInt(Elements[i],16)  + checkBase + 1));
                                checkFile.deleteOnExit();
                                FileInputStream inFile = new FileInputStream(checkFile);
                                buffer = new byte[(int)currentSegment.CheckBlockSize];
                                bytesRead = inFile.read(buffer);
                                if( bytesRead < buffer.length )
                                {
                                    System.out.println("Not enough input data for check " + (Integer.parseInt(Elements[i],16) + chunkBase + 1) + " - filling");
                                    for( int j = bytesRead; j < buffer.length; j++ ) buffer[j] = 0;
                                }
                                inFile.close();
                                fcpOut.write(buffer);
                            }
                            // Fetch decoded blocks
                            Elements = requestList.split(",");
                            int chunkNo = 0;
                            File uploadMe = null;
                            FileOutputStream outFile = null;
                            {
                                String currentLine;
                                long BlockSize = currentSegment.BlockSize;
                                long CheckSize = currentSegment.CheckBlockSize;
                                int chunkPtr = 0;
                                int length = 0;
                                if( DEBUG ) System.out.println("Expecting chunk " + Elements[chunkNo] + "\n");

                                do
                                {
                                    int index = Integer.parseInt(Elements[chunkNo],16);
                                    currentLine = fecutils.getLine(fcpIn).trim();
                                    if( DEBUG ) System.out.println(currentLine);

                                    if( currentLine.startsWith("Length=") )
                                    {
                                        length = Integer.parseInt((currentLine.split("="))[1],16);
                                    }
                                    if( currentLine.equals("Data") )
                                    {
                                        int currentRead;
                                        buffer = new byte[(int)length];
                                        if( uploadMe == null )
                                        {
                                            if( index >= blockCount )
                                            {
                                                uploadMe = new File(new StringBuffer().append(frame1.keypool).append(target.getName())
                                                                    .append("-check-").append((index + checkBase + 1)).toString()
                                                                   );
                                            }
                                            else
                                            {
                                                uploadMe = new File(new StringBuffer().append(frame1.keypool).append(target.getName())
                                                                    .append("-chunk-").append((index + chunkBase + 1)).toString()
                                                                   );
                                            }
                                            outFile = new FileOutputStream(uploadMe);
                                            System.out.println("Recovering " + uploadMe.getName());
                                        }

                                        currentRead = fcpIn.read(buffer);
                                        while( currentRead < length )
                                        {
                                            currentRead += fcpIn.read(buffer,currentRead,length - currentRead);
                                        }
                                        outFile.write(buffer);
                                        chunkPtr += currentRead;
                                        if( chunkPtr > BlockSize )
                                            System.out.println("!!!!! Unsupported length");
                                        if( (chunkPtr == BlockSize) || (chunkPtr == CheckSize) )
                                        {
                                            // We received a complete check block
                                            outFile.close();
                                            //add the checks to the workqueue
                                            //if (index > blockCount) {

                                            System.out.println("added to heal queue " + uploadMe.getPath());
                                            toHeal.add(uploadMe.getPath());
                                            ((WorkQueue)healingQueue.get()).add(uploadMe);
                                            //}
                                            uploadMe = null;
                                            chunkNo++;
                                            chunkPtr = 0;
                                        }
                                    }
                                } while( currentLine.length() > 0  && chunkNo < Elements.length );
                            }

                            fcpOut.close();
                            fcpIn.close();
                            fcpSock.close();
                        }
                        catch( ArrayIndexOutOfBoundsException e )
                        {
                            e.printStackTrace();
                        }
                        catch( Exception e )
                        {
                            System.out.println("\nexception in getfecsplit 2" +e.toString());
                        }
                        // We might remove check blocks here
                    }
                }
            }
        }

        // If the chunks have been downloaded successfully (all segements)
        // we can connect them to one file

        if( success )
        {
            ((Thread)healer.get()).start();
            // Generate output
            FileOutputStream fileOut;

            try
            {
                fileOut = new FileOutputStream(target);
                if( DEBUG ) System.out.println("Connecting chunks");

                long bytesWritten = 0;
                int blockCount = 0;
                long remaining;
                long thisRead;
                while( bytesWritten < splitFileSize )
                {

                    if( DEBUG ) System.out.println("Adding chunk " + blockCount + " to " + target.getName());
                    File toRead = new File(new StringBuffer().append(frame1.keypool).append(target.getName())
                                           .append("-chunk-").append((blockCount+1)).toString()
                                          );
                    FileInputStream fileIn = new FileInputStream(toRead);
                    remaining = splitFileSize - bytesWritten;
                    thisRead = toRead.length();
                    if( remaining < thisRead ) thisRead = remaining;
                    byte[] buffer = new byte[(int)thisRead];
                    fileIn.read(buffer);
                    fileOut.write(buffer);
                    bytesWritten += thisRead;
                    if( !toHeal.contains(toRead.getPath()) )
                    {
                        toRead.deleteOnExit();
                        toRead.delete();
                    }
                    //((WorkQueue)healingQueue.get()).add(toRead);
                    blockCount++;
                }

                fileOut.close();
            }
            catch( IOException e )
            {
                if( DEBUG ) System.out.println("Write Error: " + target.getPath());
            }
        }
        else
        {
            // target.delete(); done in getFile
            if( DEBUG ) System.out.println("!!!!!! Download of " + target.getName() + " failed.");
        }
        return success;
    }

    /**
     * getFile retrieves a file from Freenet. It does detect if this file is a redirect, a splitfile or
     * just a simple file. It checks the size for the file and returns false if sizes do not match.
     * Size is ignored if it is NULL
     *
     * @param key The key to retrieve. All to Freenet known key formats are allowed (passed to node via FCP).
     * @param size Size of the file in bytes. Is ignored if not an integer value or -1 (splitfiles do not need this setting).
     * @param target Target path
     * @param htl request htl
     * @param doRedirect If true, getFile redirects if possible and downloads the file it was redirected to.
     * @return True if download was successful, else false.
     */
    public static boolean getFile(String key, Long size, File target, int htl, boolean doRedirect)
    {
        // use temp file by default, only filedownload needs the target file to monitor download progress
        return getFile(key,size,target,htl,doRedirect,    true, null);
    }

    public static boolean getFile(String key, Long size, File target, int htl, boolean doRedirect,
                                  boolean createTempFile, FrostDownloadItemObject dlItem)
    {
        File tempFile = null;
        if( createTempFile )
        {
            try
            {
                tempFile = File.createTempFile("getFile_", ".tmp", new File(frame1.frostSettings.getValue("temp.dir")));
            }
            catch( IOException ex )
            {
                ex.printStackTrace();
                return false;
            }
        }
        else
        {
            tempFile = new File( target.getPath() + ".tmp" );
        }

        // First we just download the file, not knowing what lies ahead
        FcpResults results = null;
        if( (results = getKey(key, tempFile, htl)) != null &&
            tempFile.length() > 0 )
        {
            if( results.getMetadata() != null &&
                tempFile.length() <= 65536 &&
                doRedirect )
            { // File may be a redirect

                // Check if this file is a redirect and if there is a key to the file in the metadata
                String redirectCHK = getRedirectCHK( results.getMetadata(), key );

                if( redirectCHK != null )
                { // File is a redirect
                    if( DEBUG ) System.out.println("Redirecting to " + redirectCHK);

                    results = null;
                    results = getKey(redirectCHK, tempFile, htl);
                    if( results == null || tempFile.length() == 0 )
                    {
                        // remove temporary file if download failed
                        tempFile.delete();
                        return false;
                    }
                }
            }

            // Check if file is a splitfile.
            boolean isSplitfile = false;
            if( tempFile.length() < 65536 ) // TODO: is redirect file of this max size?
            {
                String content[] = FileAccess.readFile(tempFile).split("\n");
                for( int i = 0; i < content.length; i++ )
                    if( content[i].startsWith("SplitFile.Size") ) isSplitfile = true;
                if( isSplitfile )
                { // File is a splitfile
                    boolean success;
    // FIXED: decide by algo if this is a supported FEC splitfile, not by format
                    String algo = SettingsFun.getValue(tempFile.getPath(), "SplitFile.AlgoName");
                    if( algo.equals("OnionFEC_a_1_2") )
                        success = getFECSplitFile(key, tempFile, htl, dlItem);
                    else
                        success = getSplitFile(key, tempFile, htl, dlItem);
    /*
                    String format = SettingsFun.getValue(tempFile.getPath(), "Info.Format");
                    if( format.equals("Frost/FEC") )
    */
                    if( success )
                    {
                        // If the target file exists, we remove it
                        if( target.isFile() )
                            target.delete();
                        tempFile.renameTo(target);
                    }
                    else
                    {
                        // remove temporary file (e.g. redirect file) if download failed
                        tempFile.delete();
                    }
                    return success;
                }
            }

            // download should be successful now
            if( size == null || size.longValue() == tempFile.length() )
            {
                // If the target file exists, we remove it
                if( target.isFile() )
                    target.delete();
                boolean wasOK = tempFile.renameTo(target);
                if( wasOK == false )
                {
                    System.out.println("ERROR: Could not move file '"+tempFile.getPath()+"' to '"+target.getPath()+"'.");
                    System.out.println("Maybe the locations are on different filesystems where a move is not allowed.");
                    System.out.println("Please try change the location of 'temp.dir' in the frost.ini file.");
                }
                return true;
            }
        }

        // if we reach here, the download was NOT successful in any way
        tempFile.delete();
        return false;
    }

    private static String getRedirectCHK(String[] metadata, String key)
    {
/*
SAMPLE URL:
------------

SSK@CKesZYUJWn2GMvoif1R4SDbujIgPAgM/fuqid/9//FUQID-1.2.zip

METAFILE FORMAT:
-----------------
Version
Revision=1
EndPart
Document
Redirect.Target=freenet:CHK@OvGKjXgv3CpQ50AhHumTxQ1TQdkOAwI,eMG88L0X0H82rQjM4h1y4g
Name=index.html
Info.Format=text/html
EndPart
Document
Redirect.Target=freenet:CHK@~ZzKVquUvXfnbaI5bR12wvu99-4LAwI,~QYjCzYNT6E~kVIbxF7DoA
Name=activelink.png
Info.Format=image/png
EndPart
Document
Redirect.Target=freenet:CHK@9rz6vjVwOBPn6GhxmSsl5ZUf9SgUAwI,09Tt5bS-bsGWZiNSzLD38A
Name=FUQID-1.2.zip
Info.Format=application/zip
End
Document
*/
        String searchedFilename = null;
        int pos1 = key.lastIndexOf("/");
        if( pos1 > -1 )
        {
            searchedFilename = key.substring(pos1+1).trim();
            if( searchedFilename.length() == 0 )
                searchedFilename = null;

        }
        if( searchedFilename == null )
            return null; // no filename found in key

        // scan through lines and find the Redirect.Target=(CHK) for Name=(our searchedFilename)
        // and get the CHK of the file
        final String keywordName = "Name=";
        final String keywordRedirTarget = "Redirect.Target=";
        String actualFilename = null;
        String actualCHK = null;
        String resultCHK = null;
        for( int lineno = 0; lineno < metadata.length; lineno++ )
        {
            String line = metadata[lineno].trim();
            if( line.length() == 0 )
                continue;

            if( line.equals("Document") )
            {
                // new file section begins
                actualFilename = null;
                actualCHK = null;
            }
            else if( line.equals("End") || line.equals("EndPart") )
            {
                // we should have actualFilename and actualCHK now, look if this is our searched file
                if( actualCHK != null && actualFilename != null )
                {
                    if( actualFilename.equals( searchedFilename ) )
                    {
                        resultCHK = actualCHK;
                        return resultCHK;
                    }
                }
            }
            else if( line.startsWith(keywordName) )
            {
                actualFilename = line.substring( keywordName.length() ).trim();
            }
            else if( line.startsWith(keywordRedirTarget) )
            {
                actualCHK = line.substring( keywordRedirTarget.length() ).trim();
            }
        }
        return null;
    }

    // used by getFile
    private static FcpResults getKey(String key, File target, int htl)
    {
        if( key == null || key.length() == 0 || key.startsWith("null") )
            return null;

        FcpResults results = null;
        try
        {
            FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"),
                                                         frame1.frostSettings.getValue("nodePort"));
            try
            {
                results = connection.getKeyToFile(key, target.getPath(), htl);
            }
            catch( DataNotFoundException ex ) // frost.FcpTools.DataNotFoundException
            {
                // do nothing, data not found is usual ...
                if( DEBUG ) System.out.println("FcpRequest.getKey: DataNotFoundException (usual if not found)");
            }
            catch( FcpToolsException e )
            {
                if( DEBUG ) System.out.println("FcpRequest.getKey: FcpToolsException " + e);
            }
            catch( IOException e )
            {
                if( DEBUG ) System.out.println("FcpRequest.getKey: IOException " + e);
            }
        }
        catch( FcpToolsException e )
        {
            if( DEBUG ) System.out.println("FcpRequest.getKey: FcpToolsException " + e);
            frame1.displayWarning(e.toString());
        }
        catch( UnknownHostException e )
        {
            if( DEBUG ) System.out.println("FcpRequest.getKey: UnknownHostException " + e);
            frame1.displayWarning(e.toString());
        }
        catch( IOException e )
        {
            if( DEBUG ) System.out.println("FcpRequest.getKey: IOException " + e);
            frame1.displayWarning(e.toString());
        }

        String printableKey = null;
        if( DEBUG )
        {
            String keyPrefix = "";
            if( key.indexOf("@") > -1 )  keyPrefix = key.substring(0, key.indexOf("@")+1);
            String keyUrl = "";
            if( key.indexOf("/") > -1 )  keyUrl = key.substring(key.indexOf("/"));
            printableKey = new StringBuffer().append(keyPrefix)
                                             .append("...")
                                             .append(keyUrl).toString();
        }

        if( results != null && target.length() > 0 )
        {
            if( DEBUG ) System.out.println("getKey - Success: " + printableKey );
            return results;
        }
        target.delete();
        if( DEBUG ) System.out.println("getKey - Failed: " + printableKey );
        return null;
    }


///////////////////////////////////////////
// OLD splitfile support (non-FEC)
///////////////////////////////////////////
    private static boolean getSplitFile(String key, File target, int htl, FrostDownloadItemObject dlItem)
    {
        String blockCount = SettingsFun.getValue(target.getPath(), "SplitFile.BlockCount");
        String splitFileSize = SettingsFun.getValue(target.getPath(), "SplitFile.Size");
        String splitFileBlocksize = SettingsFun.getValue(target.getPath(), "SplitFile.Blocksize");

        int maxThreads = 3;
        maxThreads = frame1.frostSettings.getIntValue("splitfileDownloadThreads");

        int intBlockCount = 0;
        try {
            intBlockCount = Integer.parseInt(blockCount, 16);
        }
        catch( NumberFormatException e ) {}

        long intSplitFileSize = -1;
        try {
            intSplitFileSize = Long.parseLong(splitFileSize, 16);
        }
        catch( NumberFormatException e ) {}

        int intSplitFileBlocksize = -1;
        try {
            intSplitFileBlocksize = Integer.parseInt(splitFileBlocksize, 16);
        }
        catch( NumberFormatException e ) {}

        // Put ascending numbers into array
        int[] blockNumbers = new int[intBlockCount];
        for( int i = 0; i < intBlockCount; i++ )
            blockNumbers[i] = i + 1;

        // CofE's Chunkmixer
        Random rand = new Random(System.currentTimeMillis());
        for( int i = 0; i < intBlockCount; i++ )
        {
            int tmp = blockNumbers[i];
            int randomNumber = Math.abs(rand.nextInt()) % intBlockCount;
            blockNumbers[i] = blockNumbers[randomNumber];
            blockNumbers[randomNumber] = tmp;
        }

        if( dlItem != null )
        {

            if( dlItem.getFileSize() == null )
            {
                dlItem.setFileSize( intSplitFileSize );
            }
            else // paranoia
            {
                if( dlItem.getFileSize().longValue() != intSplitFileSize )
                {
                    System.out.println("WARNING: size of fec splitfile differs from size given from download table. MUST not happen!");
                }
            }
            // update gui table
            dlItem.setBlockProgress( 0,
                                     intBlockCount,
                                     intBlockCount);
            dlItem.setState( dlItem.STATE_PROGRESS );
            ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
        }

        boolean success = true;
        boolean[] results = new boolean[intBlockCount];
        Thread[] threads = new Thread[intBlockCount];
        for( int i = 0; i < intBlockCount; i++ )
        {
            int j = blockNumbers[i];
            String chk = SettingsFun.getValue(target.getPath(), "SplitFile.Block." + Integer.toHexString(j));

            // Do not exceed maxThreads limit
            while( getActiveThreads(threads) >= maxThreads )
            {
                mixed.wait(5000);
                // update gui
                if( dlItem != null )
                {
                    int doneBlocks = 0;
                    for( int z = 0; z < intBlockCount; z++ )
                    {
                        if( results[z] == true )
                        {
                            doneBlocks++;
                        }
                    }
                    dlItem.setBlockProgress( doneBlocks,
                                             intBlockCount,
                                             intBlockCount);
                    ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
                }
            }

            if( DEBUG ) System.out.println("Requesting: SplitFile.Block." + Integer.toHexString(j) + "=" + chk);

            // checkSize is the size (in bytes) of one chunk.
            // Because the last chunk is probably smaller, we
            // calculate the last chunks size here.
            int checkSize = intSplitFileBlocksize;
            if( blockNumbers[i] == intBlockCount && intSplitFileBlocksize != -1 )
                checkSize = (int)(intSplitFileSize - (intSplitFileBlocksize * (intBlockCount - 1)));

            threads[i] = new getKeyThread(chk,
                                          new File(frame1.keypool + target.getName() + "-chunk-" + j),
                                          htl,
                                          results,
                                          i,
                                          checkSize);
            threads[i].start();

            // update gui
            if( dlItem != null )
            {
                int doneBlocks = 0;
                for( int z = 0; z < intBlockCount; z++ )
                {
                    if( results[z] == true )
                    {
                        doneBlocks++;
                    }
                }
                dlItem.setBlockProgress( doneBlocks,
                                         intBlockCount,
                                         intBlockCount);
                ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
            }
        }

        // wait until all threads are done
        while( getActiveThreads(threads) > 0 )
        {
//            if( DEBUG ) System.out.println("Active Splitfile request remaining (htl " + htl + "): " + getActiveThreads(threads));
            mixed.wait(5000);
            // update gui
            if( dlItem != null )
            {
                int doneBlocks = 0;
                for( int z = 0; z < intBlockCount; z++ )
                {
                    if( results[z] == true )
                    {
                        doneBlocks++;
                    }
                }
                dlItem.setBlockProgress( doneBlocks,
                                         intBlockCount,
                                         intBlockCount);
                ((DownloadTableModel)frame1.getInstance().getDownloadTable().getModel()).updateRow( dlItem );
            }
        }

        // Each request thread stores it's result in results[]
        // We need to verify that all threads finished successfully
        for( int i = 0; i < intBlockCount; i++ )
        {
            if( !results[i] )
            {
                success = false;
                if( DEBUG ) System.out.println("NO SUCCESS");
            }
            else
            {
                if( DEBUG ) System.out.println("SUCCESS");
            }
        }

        // If the chunks have been downloaded successfully
        // we can connect them to one file
        if( success )
        {
            FileOutputStream fileOut;

            try
            {
                fileOut = new FileOutputStream(target);
                if( DEBUG ) System.out.println("Connecting chunks");

                for( int i = 1; i <= intBlockCount; i++ )
                {

                    if( DEBUG ) System.out.println("Adding chunk " + i + " to " + target.getName());
                    File toRead = new File(frame1.keypool + target.getName() + "-chunk-" + i);
                    fileOut.write(FileAccess.readByteArray(toRead));
                    toRead.deleteOnExit();
                    toRead.delete();
                }

                fileOut.close();
            }
            catch( IOException e )
            {
                if( DEBUG ) System.out.println("Write Error: " + target.getPath());
            }
        }
        else
        {
            // remove redirect and chunks if download was incomplete
            target.delete();
            if( DEBUG ) System.out.println("!!!!!! Download of " + target.getName() + " failed.");
        }

        return success;
    }

}
