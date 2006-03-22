/*
  FcpRequest.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.fcp;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.download.FrostDownloadItem;
import frost.threads.getKeyThread;

/**
 * Requests a key from freenet
 */

// while requesting / inserting, show chunks left to try (incl. trying chunks) -> Warte (9) / 18% (9)

public class FcpRequest
{
    final static boolean DEBUG = true;

    private static Logger logger = Logger.getLogger(FcpRequest.class.getName());

    private static int getActiveThreads(Thread[] threads) {
        int count = 0;
        for( int i = 0; i < threads.length; i++ ) {
            if( threads[i] != null ) {
                if( threads[i].isAlive() )
                    count++;
            }
        }
        return count;
    }

    /**
     * Downloads a FEC splitfile.
     * If downloadItem == null, we download a file not contained in download table
     * (e.g. an index file). Then do not update progress, and remove working files
     * after finished.
     *
     * @param target  File to download to
     * @param redirect  The downloaded redirect file
     * @param htl  HTL to use for download
     * @param dlItem  the download item to update progress. can be null.
     * @return
     */
    private static boolean getFECSplitFile(File target, File redirect, int htl, FrostDownloadItem dlItem)
    {
        // true = try all segments even if one fails
        boolean optionTryAllSegments = MainFrame.frostSettings.getBoolValue("downloadTryAllSegments");
        // true = deocde after download of ALL segments
        boolean optionDecodeAfterDownload = MainFrame.frostSettings.getBoolValue("downloadDecodeAfterEachSegment");

        FecSplitfile splitfile =null;

        // getFECSplitFile expects (e.g. for file 'download.zip')
        //  target like 'downloaddir/download.zip.data
        //  redirect    'downloaddir/download.zip.redirect'

        // check if there is already a redirect file, size > 0
        File t1 = new File(target.getPath() + ".redirect");

        // TODO: check if redirect files have same content (sizes could differ, thats ok!)
        if( t1.exists() == false || t1.length() == 0 ) {
            // move redirect file to working location
            t1.delete();
            redirect.renameTo(t1);
            redirect = new File(target.getPath() + ".redirect");
        } else {
            // we dont need the redirect file any longer, delete it
            // we use existing redirect file
            redirect.delete();
            redirect = t1;
        }

        try {
            splitfile = new FecSplitfile(target, redirect);
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Exception thrown in getFECSplitFile(File, File, int, FrostDownloadItem)", ex);
            return false;
        }
        int displayedRequiredBlocks = splitfile.getDataBlocks().size();
        int displayedAvailableBlocks = splitfile.getDataBlocks().size() + splitfile.getCheckBlocks().size();
        int displayedFinishedBlocks = 0;

        // TODO:
        // - wenn "try to download all segments, even if one fails" AN ist, dann alle laden;
        //   ansonsten ein segment nach dem anderen
        // - immer tracken ob ein segment komplett ist, wegen decode wenns in options an ist!

        if( dlItem != null ) {

            // compute finishedBlocks for ALL segments on start of download
            for( int segmentNo=0; segmentNo < splitfile.getSegmentCount(); segmentNo++ ) {

                FecSplitfile.SingleSegmentValues seginf =
                    (FecSplitfile.SingleSegmentValues)splitfile.getValuesForSegment(segmentNo);
                int neededBlockCount = seginf.dataBlockCount;
//                int availableBlockCount = seginf.dataBlockCount + seginf.checkBlockCount;
                ArrayList missingBlocks = getBlocksInSegmentWithState(splitfile.getDataBlocks(),
                    segmentNo,
                    FecBlock.STATE_TRANSFER_WAITING);
                missingBlocks.addAll( getBlocksInSegmentWithState(splitfile.getCheckBlocks(),
                    segmentNo,
                    FecBlock.STATE_TRANSFER_WAITING) );
                int missingOverallBlockCount = missingBlocks.size();
                ArrayList finishedBlocks = getBlocksInSegmentWithState(splitfile.getDataBlocks(),
                    segmentNo,
                    FecBlock.STATE_TRANSFER_FINISHED);
                finishedBlocks.addAll( getBlocksInSegmentWithState(splitfile.getCheckBlocks(),
                    segmentNo,
                    FecBlock.STATE_TRANSFER_FINISHED) );
                int segmentsFinishedBlocks = finishedBlocks.size();

                // only count needed blocks, do not finish more than needed blocks.
                // this special counting is only needed for progress display
                if( segmentsFinishedBlocks > neededBlockCount ) {
                    displayedFinishedBlocks += neededBlockCount;
                } else {
                    displayedFinishedBlocks += segmentsFinishedBlocks;
                }
            }
            // set filesize as given in root split file
            dlItem.setFileSize(new Long(splitfile.getDataFileSize()));
            // update gui table
            dlItem.setDoneBlocks(displayedFinishedBlocks);
            dlItem.setRequiredBlocks(displayedRequiredBlocks);
            dlItem.setTotalBlocks(displayedAvailableBlocks);
            dlItem.setState( FrostDownloadItem.STATE_PROGRESS );
        }

        boolean[] wasSegmentSuccessful = new boolean[splitfile.getSegmentCount()];
        Arrays.fill(wasSegmentSuccessful, false);

        // try to get the missing blocks per segment.
        // TODO: request multiple segments together!
        for( int segmentNo=0; segmentNo < splitfile.getSegmentCount(); segmentNo++ ) {

            FecSplitfile.SingleSegmentValues seginf =
                (FecSplitfile.SingleSegmentValues)splitfile.getValuesForSegment(segmentNo);
            int neededBlockCount = seginf.dataBlockCount;
//            int availableBlockCount = seginf.dataBlockCount + seginf.checkBlockCount;
            ArrayList missingBlocks = getBlocksInSegmentWithState(splitfile.getDataBlocks(),
                segmentNo,
                FecBlock.STATE_TRANSFER_WAITING);
            missingBlocks.addAll( getBlocksInSegmentWithState(splitfile.getCheckBlocks(),
                segmentNo,
                FecBlock.STATE_TRANSFER_WAITING) );
            int missingOverallBlockCount = missingBlocks.size();
            ArrayList finishedBlocks = getBlocksInSegmentWithState(splitfile.getDataBlocks(),
                segmentNo,
                FecBlock.STATE_TRANSFER_FINISHED);
            finishedBlocks.addAll( getBlocksInSegmentWithState(splitfile.getCheckBlocks(),
                segmentNo,
                FecBlock.STATE_TRANSFER_FINISHED) );
            int segmentsFinishedBlocks = finishedBlocks.size();
            finishedBlocks = null; // not longer needed

            if( missingOverallBlockCount == 0 ) {
                // when a segment is decoded, ALL blocks are set to finished
                logger.info("Segment "+segmentNo+" is already decoded.");
                wasSegmentSuccessful[segmentNo] = true;
                continue;
            }

            int maxThreads = MainFrame.frostSettings.getIntValue("splitfileDownloadThreads");

            if( segmentsFinishedBlocks < neededBlockCount ) {
                // we need to receive some more blocks before we are able to decode
                Collections.shuffle( missingBlocks );
                // start configured amount of splitfile threads
                int actBlockIx = 0;
                Vector runningThreads = new Vector(maxThreads);

                while( segmentsFinishedBlocks < neededBlockCount &&
                       actBlockIx < missingBlocks.size() )
                {
                    // check if threads are finished
                    boolean threadsFinished = false;
                    for( int y=runningThreads.size()-1; y >= 0; y-- ) {
                        GetKeyThread gkt = (GetKeyThread)runningThreads.get(y);
                        if( gkt.isAlive() == false ) {
                            if( gkt.getSuccess() == true ) {
                                // never count higher than datablocks/segment in each segment
                                if( segmentsFinishedBlocks < neededBlockCount ) {
                                    displayedFinishedBlocks++;
                                }
                                segmentsFinishedBlocks++;
                                if( dlItem != null ) {
                                    dlItem.setDoneBlocks(displayedFinishedBlocks);
                                    dlItem.setRequiredBlocks(displayedRequiredBlocks);
                                    dlItem.setTotalBlocks(displayedAvailableBlocks);
                                }
                                // now done in thread
                                // splitfile.createRedirectFile(true);
                            }
                            runningThreads.remove(y);
                            threadsFinished = true;
                        }
                    }
                    if( threadsFinished == true ) {
                        continue;
                    }

                    int maxThreadsNeeded = neededBlockCount - segmentsFinishedBlocks;
                    int threadCountAllowedToStart = maxThreads - runningThreads.size();
                    if( maxThreadsNeeded > 0 && threadCountAllowedToStart > 0 ) {
                        FecBlock block = (FecBlock)missingBlocks.get(actBlockIx);
                        actBlockIx++;
                        GetKeyThread thread = new GetKeyThread( splitfile, block, htl );
                        runningThreads.add( thread );
                        thread.start();
                        Mixed.wait(111); // dont hurt node
                        continue;
                    }
                    // now we are here, no thread allowed to start, so we wait
                    Mixed.wait(1000);
                }
                // wait for all running threads to finish
                while( runningThreads.size() > 0 ) {
                    // check if threads are finished
                    boolean threadsFinished = false;
                    for( int y=runningThreads.size()-1; y >= 0; y-- ) {
                        GetKeyThread gkt = (GetKeyThread)runningThreads.get(y);
                        if( gkt.isAlive() == false ) {
                            if( gkt.getSuccess() == true ) {
                                // never count higher than datablocks/segment in each segment
                                if( segmentsFinishedBlocks < neededBlockCount ) {
                                    displayedFinishedBlocks++;
                                }
                                segmentsFinishedBlocks++;
                                if( dlItem != null ) {
                                    dlItem.setDoneBlocks(displayedFinishedBlocks);
                                    dlItem.setRequiredBlocks(displayedRequiredBlocks);
                                    dlItem.setTotalBlocks(displayedAvailableBlocks);
                                }
                                // now done in thread
                                // splitfile.createRedirectFile(true);
                            }
                            runningThreads.remove(y);
                            threadsFinished = true;
                        }
                    }
                    if( threadsFinished == true ) {
                        continue;
                    }
                    Mixed.wait(1000);
                }
            } // end-of: if( provided < needed )
            // check if we have enough blocks to decode (paranoia)
            if( splitfile.isDecodeable(segmentNo) ) {
                logger.info("Segment "+segmentNo+" is decodeable...");

                if( optionDecodeAfterDownload == false ) {
                    if( dlItem != null ) {
                        dlItem.setState(FrostDownloadItem.STATE_DECODING);
                    }
                    try {
                        splitfile.decode(segmentNo);
                    } catch (Throwable e1) {
                        logger.log(Level.SEVERE, "Exception thrown in getFECSplitFile(File, File, int, FrostDownloadItem)", e1);
                        wasSegmentSuccessful[segmentNo] = false;
                        break;
                    }
                    if( dlItem != null ) {
                        dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                    }

                    // mark all blocks in current segment finished
                    setBlocksInSegmentFinished(splitfile.getDataBlocks(), segmentNo);
                    setBlocksInSegmentFinished(splitfile.getCheckBlocks(), segmentNo);
                    splitfile.createRedirectFile(true);
                }
                // BBACKFLAG: remember unfinished blocks to encode+reinsert them !!!
                wasSegmentSuccessful[segmentNo] = true;
            } else {
                logger.warning("Segment " + segmentNo + " is NOT decodeable...");
                wasSegmentSuccessful[segmentNo] = false;
                if( optionTryAllSegments == false ) {
                    break; // stop downloading now
                }
            }
            // go on with next segment
        }

        boolean success = true;
        // success is true if all segments were
        // - downloaded+decoded (decodeAfterDownload = false -> decode after each segment)
        // - downloaded  (decodeAfterDownload = true)
        for(int x=0; x<wasSegmentSuccessful.length; x++) {
            if( wasSegmentSuccessful[x] == false ) {
                success = false;
            }
        }

        if( optionDecodeAfterDownload == true && success == true ) {
            if( dlItem != null ) {
                dlItem.setState(FrostDownloadItem.STATE_DECODING);
            }
            // decode after all segments have downloaded successfully
            for( int segmentNo=0; segmentNo < splitfile.getSegmentCount(); segmentNo++ ) {
                try {
                    splitfile.decode(segmentNo);
                } catch (Throwable e1) {
                    logger.log(Level.SEVERE, "Exception thrown in getFECSplitFile(File, File, int, FrostDownloadItem)", e1);
                    success = false;
                    wasSegmentSuccessful[segmentNo] = false;
                    break;
                }
            }
            if( dlItem != null ) {
                dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
            }
        }

        if( success == true ) {
            // if we really reach here, all segments are successfully decoded.
            // we need to set the correct size of the downloaded file.
            splitfile.setCorrectDatafileSize();

            // rename target file, maybe delete redirect+checkblocks file
            if( dlItem != null ) {
                splitfile.finishDownload(false); // dont delete working files (TODO: healing!)
            } else {
                // this deletes work file, and if setCorrectDatafileSize() was not called (on error)
                // it also deletes the data file
                splitfile.finishDownload(true);
            }
        } else {// no success
            if( dlItem == null ) {
                // this deletes work file, and if setCorrectDatafileSize() was not called (on error)
                // it also deletes the data file
                splitfile.finishDownload(true);
            }
        }
        return success;
    }

    private static class GetKeyThread extends Thread {

        FecBlock block;
        int htl;
        boolean success;
        FecSplitfile splitfile;

        public GetKeyThread(FecSplitfile sf, FecBlock b, int h) {
            block = b;
            htl = h;
            splitfile = sf;
        }

        public void run() {

            block.setCurrentState(FecBlock.STATE_TRANSFER_RUNNING);

            this.success = false;

            FcpConnection connection = FcpFactory.getFcpConnectionInstance();
            if( connection != null ) {
                try {
                    success = connection.getKeyToBucket(
                            block.getChkKey(),
                            block.getRandomAccessFileBucket(false),
                            htl);
                } catch( FcpToolsException e ) {
                    logger.log(Level.WARNING, "Error during request: ", e);
                    success = false;
                } catch( Throwable e ) {
                    logger.log(Level.WARNING, "Error during request: ", e);
                    success = false;
                }
            }
            if( success == true ) {
                block.setCurrentState(FecBlock.STATE_TRANSFER_FINISHED);
                splitfile.createRedirectFile(true);
            } else {
                block.setCurrentState(FecBlock.STATE_TRANSFER_WAITING);
                // no need to update redirect file
            }
        }

        public boolean getSuccess() {
            return success;
        }

        public synchronized FecBlock getBlock() {
            return block;
        }
    }

    private static ArrayList getBlocksInSegmentWithState(List allBlocks, int segno, int state) {
        ArrayList l = new ArrayList();
        for( int x=0; x<allBlocks.size(); x++ ) {
            FecBlock b = (FecBlock)allBlocks.get(x);
            if( b.getSegmentNo() == segno &&
                b.getCurrentState() == state )
            {
                l.add( b );
            }
        }
        return l;
    }

    private static void setBlocksInSegmentFinished(List allBlocks, int segno) {
        for( int x=0; x<allBlocks.size(); x++ ) {
            FecBlock b = (FecBlock)allBlocks.get(x);
            if( b.getSegmentNo() == segno &&
                b.getCurrentState() != FecBlock.STATE_TRANSFER_FINISHED )
            {
                b.setCurrentState(FecBlock.STATE_TRANSFER_FINISHED);
            }
        }
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
    public static FcpResults getFile(String key,
                                  Long size,
                                  File target,
                                  int htl,
                                  boolean doRedirect)
    {
        // use temp file by default, only filedownload needs the target file to monitor download progress
        return getFile(key,size,target,htl,doRedirect, false, true, null);
    }

    public static FcpResults getFile(String key,
                                  Long size,
                                  File target,
                                  int htl,
                                  boolean doRedirect,
                                  boolean fastDownload)
    {
        // use temp file by default, only filedownload needs the target file to monitor download progress
        return getFile(key,size,target,htl,doRedirect, fastDownload, true, null);
    }

    /**
     * Returns null if nothing was received.
     */
    public static FcpResults getFile(String key,
                                  Long size,
                                  File target,
                                  int htl,
                                  boolean doRedirect,
                                  boolean fastDownload,
                                  boolean createTempFile,
                                  FrostDownloadItem dlItem)
    {
        assert htl >= 0; //some sanity checks

        // prepare a temp file
        File tempFile = null;
        if( createTempFile ) {
            try {
                tempFile = File.createTempFile("getFile_", ".tmp", new File(MainFrame.frostSettings.getValue("temp.dir")));
            } catch( Throwable ex ) {
                logger.log(Level.SEVERE, "Exception thrown in getFile(...)", ex);
                return null;
            }
        } else {
            tempFile = new File( target.getPath() + ".tmp" );
        }

        // First we just download the file, not knowing what lies ahead
        FcpResults results =null;
        String [] metadataLines=null;

        if (dlItem != null && dlItem.getRedirect() != null) {
            results = new FcpResults(dlItem.getRedirect().getBytes(), dlItem.getKey());
            logger.info("starting download of an attached redirect");
        } else {
            results = getKey(key, tempFile, htl, fastDownload);
        }

        if (results != null) {
            metadataLines = results.getMetadataAsLines();
        }

        if( results != null &&
            ( tempFile.length() > 0 || metadataLines != null )
          )
        {
            if( metadataLines != null &&
                // tempFile.length() <= 65536 &&  --bback: redirect file can be greater than 65536!
                doRedirect )
            { // File may be a redirect
                // Check if this file is a redirect and if there is a key to the file in the metadata
                String redirectCHK = getRedirectCHK( metadataLines, key );

                if( redirectCHK != null ) { // File is a redirect
                    logger.info("Redirecting to " + redirectCHK);

                    results = null;
                    results = getKey(redirectCHK, tempFile, htl, fastDownload);
                    // redirect must contain data, not only metadata
                    if( results == null || tempFile.length() == 0 ) {
                        // remove temporary file if download failed
                        tempFile.delete();
                        return null;
                    }
                }
            }

            // Check if file is a splitfile.
            boolean isSplitfile = false;
            if( metadataLines != null ) {
                String content[] = metadataLines;
                String algoName = null;
                for( int i = 0; i < content.length; i++ ) {
                    if( content[i].startsWith("SplitFile.Size") ) {
                        isSplitfile = true;
                    }
                    if( content[i].startsWith("SplitFile.AlgoName") ) {
                        algoName = content[i].substring(content[i].indexOf("=")+1).trim();
                    }
                }

                if( isSplitfile ) {
                    boolean success;
                    if( algoName != null && algoName.equals("OnionFEC_a_1_2") )
                    {
                        // save metadata to temp file
                        FileAccess.writeFile(results.getRawMetadata(), tempFile);

                        success = getFECSplitFile(target, tempFile, htl, dlItem);
                        // this method handles all working files, no more needed here
                        if( success )
                        {
                            return results; // return the metadata
                        }
                        return null;
                    }
                    else
                    {  //FIXME: we could probably remove support for non-fec splitfiles
                        //its been years since they were used
                        success = getSplitFile(key, tempFile, htl, dlItem);
                    }

                    if( success ) {
                        // If the target file exists, we remove it
                        if( target.isFile() )
                            target.delete();
                        tempFile.renameTo(target);
                        return results; // return the metadata
                    } else {
                        // remove temporary file (e.g. redirect file) if download failed
                        tempFile.delete();
                        return null;
                    }
                }
            }

            // download should be successful now
            if( size == null || size.longValue() == tempFile.length() ) {
                // If the target file exists, we remove it
                if( target.isFile() ) {
                    target.delete();
                }
                boolean wasOK = tempFile.renameTo(target);
                if( wasOK == false ) {
                    logger.severe("ERROR: Could not move file '" + tempFile.getPath() + "' to '" + target.getPath() + "'.\n" +
                                  "Maybe the locations are on different filesystems where a move is not allowed.\n" +
                                  "Please try change the location of 'temp.dir' in the frost.ini file,"+
                                  " and copy the file to a save location by yourself.");
                }
                return results;
            }
        }
        // if we reach here, the download was NOT successful in any way
        tempFile.delete();
        return null;
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
    private static FcpResults getKey(String key, File target, int htl, boolean fastDownload) {

        if( key == null || key.length() == 0 || key.startsWith("null") ) {
            return null;
        }

        FcpResults results = null;

        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if( connection != null ) {
            int tries = 0;
            int maxtries = 3;
            while( tries < maxtries || results != null ) {
                try {
                    results = connection.getKeyToFile(key, target.getPath(), htl, fastDownload);
                    break;
                } catch( java.net.ConnectException e ) {
                    tries++;
                    continue;
                } catch( DataNotFoundException ex ) { // frost.FcpTools.DataNotFoundException
                    // do nothing, data not found is usual ...
                    logger.log(Level.INFO, "FcpRequest.getKey(1): DataNotFoundException (usual if not found)", ex);
                    break;
                } catch( FcpToolsException e ) {
                    logger.log(Level.SEVERE, "FcpRequest.getKey(1): FcpToolsException", e);
                    break;
                } catch( IOException e ) {
                    logger.log(Level.SEVERE, "FcpRequest.getKey(1): IOException", e);
                    break;
                }
            }
        }

        String printableKey = null;
        if( DEBUG ) {
            String keyPrefix = "";
            if( key.indexOf("@") > -1 )  keyPrefix = key.substring(0, key.indexOf("@")+1);
            String keyUrl = "";
            if( key.indexOf("/") > -1 )  keyUrl = key.substring(key.indexOf("/"));
            printableKey = new StringBuffer().append(keyPrefix)
                                             .append("...")
                                             .append(keyUrl).toString();
        }

        boolean metadataAvailable = results!=null &&
                                    results.getRawMetadata()!=null &&
                                    results.getRawMetadata().length > 0;
        if( results != null &&
            ( target.length() > 0 || metadataAvailable )
          )
        {
            logger.info("getKey - Success: " + printableKey );
            return results;
        }
        target.delete();
        logger.info("getKey - Failed: " + printableKey );
        return null;
    }


///////////////////////////////////////////
// OLD splitfile support (non-FEC)
///////////////////////////////////////////
    private static boolean getSplitFile(String key, File target, int htl, FrostDownloadItem dlItem)
    {
        logger.warning("ATTENTION: Using old, non-FEC download method!\n" +
                       "           This could run, but is'nt really supported any longer.");

        String blockCount = SettingsFun.getValue(target.getPath(), "SplitFile.BlockCount");
        String splitFileSize = SettingsFun.getValue(target.getPath(), "SplitFile.Size");
        String splitFileBlocksize = SettingsFun.getValue(target.getPath(), "SplitFile.Blocksize");

        int maxThreads = 3;
        maxThreads = MainFrame.frostSettings.getIntValue("splitfileDownloadThreads");

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
                dlItem.setFileSize(new Long(intSplitFileSize));
            }
            else // paranoia
            {
                if( dlItem.getFileSize().longValue() != intSplitFileSize )
                {
                    logger.warning("WARNING: size of fec splitfile differs from size given from download table. MUST not happen!");
                }
            }
            // update gui table
            dlItem.setDoneBlocks(0);
            dlItem.setRequiredBlocks(intBlockCount);
            dlItem.setTotalBlocks(intBlockCount);
            dlItem.setState( FrostDownloadItem.STATE_PROGRESS );
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
                Mixed.wait(5000);
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
                    dlItem.setDoneBlocks(doneBlocks);
                    dlItem.setRequiredBlocks(intBlockCount);
                    dlItem.setTotalBlocks(intBlockCount);
                }
            }

            logger.info("Requesting: SplitFile.Block." + Integer.toHexString(j) + "=" + chk);

            // checkSize is the size (in bytes) of one chunk.
            // Because the last chunk is probably smaller, we
            // calculate the last chunks size here.
            int checkSize = intSplitFileBlocksize;
            if( blockNumbers[i] == intBlockCount && intSplitFileBlocksize != -1 )
                checkSize = (int)(intSplitFileSize - (intSplitFileBlocksize * (intBlockCount - 1)));

            threads[i] = new getKeyThread(chk,
                                          new File(MainFrame.keypool + target.getName() + "-chunk-" + j),
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
                dlItem.setDoneBlocks(doneBlocks);
                dlItem.setRequiredBlocks(intBlockCount);
                dlItem.setTotalBlocks(intBlockCount);
            }
        }

        // wait until all threads are done
        while( getActiveThreads(threads) > 0 )
        {
//            if( DEBUG ) System.out.println("Active Splitfile request remaining (htl " + htl + "): " + getActiveThreads(threads));
            Mixed.wait(5000);
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
                dlItem.setDoneBlocks(doneBlocks);
                dlItem.setRequiredBlocks(intBlockCount);
                dlItem.setTotalBlocks(intBlockCount);
            }
        }

        // Each request thread stores it's result in results[]
        // We need to verify that all threads finished successfully
        for( int i = 0; i < intBlockCount; i++ )
        {
            if( !results[i] )
            {
                success = false;
                logger.info("NO SUCCESS");
            }
            else
            {
                logger.info("SUCCESS");
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
                logger.info("Connecting chunks");

                for( int i = 1; i <= intBlockCount; i++ )
                {

                    logger.fine("Adding chunk " + i + " to " + target.getName());
                    File toRead = new File(MainFrame.keypool + target.getName() + "-chunk-" + i);
                    fileOut.write(FileAccess.readByteArray(toRead));
                    toRead.deleteOnExit();
                    toRead.delete();
                }

                fileOut.close();
            }
            catch( IOException e )
            {
                logger.log(Level.SEVERE, "Write Error: " + target.getPath(), e);
            }
        }
        else
        {
            // remove redirect and chunks if download was incomplete
            target.delete();
            logger.warning("!!!!!! Download of " + target.getName() + " failed.");
        }
        return success;
    }
}
