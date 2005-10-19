/*
  FcpInsert.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fileTransfer.upload.*;

/**
 * This class provides methods to insert data into freenet.
 */
public class FcpInsert
{
	private static Logger logger = Logger.getLogger(FcpInsert.class.getName());
	
    //public final static int smallestChunk = 262144;
    // changed to freenets behaviour. also smaller values will produce errors in RandomAccessFile2.segment() 
    //however, Freenet fails very often to transfer large chuncks.
    //--zab
	//public final static int smallestChunk = 256 * 1024; 
    //changed again because files smaller than 768KB in size caused an IllegalArgumentException
    //in RandomAccessFileBucket2.segment(). Kevloral 20-02-2004 
    public final static int smallestChunk = 768 * 1024; 

    private static String[] keywords = {"Success",
                                        "RouteNotFound",
                                        "KeyCollision",
                                        "SizeError",
                                        "DataNotFound"};

    private static String[] result(String text) {
        String[] result = new String[2];
        result[0] = "Error";
        result[1] = "Error";

        logger.info("*** FcpInsert.result: text='"+text+"'");
        // check if the keyword returned by freenet is a known keyword.
        for( int i = 0; i < keywords.length; i++ ) {
            if( text.indexOf(keywords[i]) != -1 ) {
                result[0] = keywords[i];
            }
        }
        // check if the returned text contains the computed CHK key (key generation)
        if( text.indexOf("CHK@") != -1 ) {
            result[1] = text.substring(text.lastIndexOf("CHK@"), text.lastIndexOf("EndMessage"));
            result[1] = result[1].trim();
        } else {
            result[1] = "Error";
        }
        return result;
    }

    /**
     * Inserts a file into freenet.
     * The boardfilename is needed for FEC splitfile puts, 
     * for inserting e.g. the pubkey.txt file set it to null.
     * This method wraps the calls without the uploadItem.
     */
    public static String[] putFile(String uri, File file, int htl, boolean doRedirect) {
        return putFile(uri, file, null, htl, doRedirect, null);
    }
    
    public static String[] putFile(String uri, File file, byte[]metadata, int htl, boolean doRedirect) {
        return putFile(uri, file, metadata, htl, doRedirect, null);
    }
    
    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts, 
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public static String[] putFile(String uri, 
                                   File file,
                                   byte[] metadata, 
                                   int htl, 
                                   boolean doRedirect,
                                   FrostUploadItem ulItem)
    {
        if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't upload empty file: "+file.getPath());
			JOptionPane.showMessageDialog(MainFrame.getInstance(), 
							 "FcpInsert: File "+file.getPath()+" is empty!", // message
							 "Warning", 
							 JOptionPane.WARNING_MESSAGE);
			return new String[]{"Error","Error"};
        }
        // Q: can this 32K be enlarged? to same as for CHK keys?
        // A: no, its hardcoded in freenet, each keytype except CHK is limited to 32kb size (data+metadata).
        long insertLength = file.length();
        if( metadata != null ) {
            insertLength += metadata.length;
        }
        
        if( insertLength > 32767 && (uri.startsWith("KSK@") || uri.startsWith("SSK@")) ) {
            if( doRedirect ) {
                // Q: splitfile do currently NOT get any metadata applied
            	// A: they don't have metadata by convention
                return putFECSplitFile(uri, file, htl, ulItem);

            } else {
                // alternativly we could insert the data as CHK and put a redirect into the metadata of the KSK:
                // (but this would break compatability)
/*
Version
Revision=1
EndPart
Document
Redirect.Target=freenet:CHK@04lpzVes2yukYy-CKLa7X28031oRAwI,mNNpw9nj4IasmujGtQeX0w
Info.Description=file
Info.Format=text/xml
End

----------
RawDataLength=0 
*/                
                logger.log(Level.SEVERE, "Error: Data too large for direct KSK/SSK key, 32767 allowed: "+insertLength);
                JOptionPane.showMessageDialog(MainFrame.getInstance(), 
                         "<html>FcpInsert: Data of file "+file.getPath()+
                         " too large for direct KSK key, 32767 allowed: "+insertLength+
                         "<br>Please report this to a Frost developer!</html>",
                         "Insert Error", 
                         JOptionPane.ERROR_MESSAGE);
                return new String[]{"Error", "Error"};
            }
        } else {

            if( file.length() <= smallestChunk ) {
                // put file directly
                try {
                    FcpConnection connection = FcpFactory.getFcpConnectionInstance();
                    if( connection == null ) {
                        return new String[] { "Error", "Error" };
                    }

					//please remove this workaround when the freenet devs fix the FCP insert 
					//returning empty string bug
					//* * * begin workaround for FCP bug ***
					String output = new String();
					do {
						output = connection.putKeyFromFile(uri, file.getPath(), metadata, htl);
                        if( output.length() < 5 ) { //actually the length is 1, but just in case...
                            logger.warning("Freenet insert failed, maybe a bug. Trying again...");
                            Mixed.wait(333);
                        } else {
                            logger.info("output from fcp insert : "+output);
                        }
					} while (output.length() < 5);
                    	
                    // * * * end workaround for FCP bug ***  	
                    return result(output);

                } catch( UnknownHostException e ) {
					logger.log(Level.SEVERE, "UnknownHostException", e);
                } catch( Throwable e ) {
					logger.log(Level.SEVERE, "Throwable", e);
                }
                return result("");
            } else {
                // file is too big, put as FEC splitfile
                return putFECSplitFile(uri, file, htl, ulItem);
            }
        }
    }

    /**
     * Uploads a FEC splitfile.
     * If uploadItem == null, we upload a file not contained in upload table
     * (e.g. an index file). Then do not update progress, and remove working files
     * after finished.
     * 
     * @param uri  the URI to insert to, e.g. CHK@ or KSK@/mydir/myfile.dat
     * @param file  the file to upload
     * @param htl  the HTL value used to insert file
     * @param ulItem the uploadItem for progress updates or null
     * @return
     */
    private static String[] putFECSplitFile(String uri, File file,
                                           int htl, FrostUploadItem ulItem)
    {
        final String[] ERROR = new String[]{"Error","Error"};
        FecSplitfile splitfile =null;
        
/*        
        if( ulItem != null && ulItem.getKey() == null )
        {
        // FIXED: this is an attachment upload!
            Core.getOut().println("Warning: For some reason putFECSplitFile was called, but file is not prepared or null. This should not happen.");
            Core.getOut().println("         Will encode before uploading ...");
        }
*/        
        splitfile = new FecSplitfile( file );
        
        boolean alreadyEncoded = splitfile.uploadInit();
        if( alreadyEncoded == false ) {
            // should never happen, but for sure we also encode here
            // users could have deleted the .redirect or .checkblocks file
            try {
                splitfile.encode();
            } catch(Throwable t) {
				logger.log(Level.SEVERE, "Encoding failed", t);
                return ERROR;
            }
        }
        
        logger.info("Starting upload of "+file.getName());
                
        int totalAvailableBlocks = splitfile.getDataBlocks().size() + splitfile.getCheckBlocks().size();;
        int totalFinishedBlocks = 0;

        if( ulItem != null && ulItem.getKey() != null ) {
            ulItem.setTotalBlocks( totalAvailableBlocks );
            ulItem.setDoneBlocks( 0 );
            ulItem.setState( FrostUploadItem.STATE_PROGRESS );
        }
        
        LinkedList allBlocksToUpload = new LinkedList();

        for( int segmentNo=0; segmentNo < splitfile.getSegmentCount(); segmentNo++ ) {

            FecSplitfile.SingleSegmentValues seginf = 
                (FecSplitfile.SingleSegmentValues)splitfile.getValuesForSegment(segmentNo);
            
            ArrayList blocksToUpload;
            
            blocksToUpload = getBlocksInSegmentWithState(splitfile.getDataBlocks(), 
                segmentNo, 
                FecBlock.STATE_TRANSFER_WAITING);
            
            blocksToUpload.addAll( getBlocksInSegmentWithState(splitfile.getCheckBlocks(), 
                segmentNo, 
                FecBlock.STATE_TRANSFER_WAITING) );

            int blocksToUploadCount = blocksToUpload.size();
            
            int segmentBlockCount = seginf.dataBlockCount + seginf.checkBlockCount;
            totalFinishedBlocks += segmentBlockCount - blocksToUploadCount;
            if( ulItem != null && ulItem.getKey() != null ) {
                ulItem.setDoneBlocks( totalFinishedBlocks );
            }
            
            if( blocksToUploadCount == 0 ) {
                logger.info("Segment " + segmentNo + " is already inserted");
                continue;
            }

            allBlocksToUpload.addAll( blocksToUpload );
        }        

        // insert all blocks
        
        ArrayList runningThreads = new ArrayList();
        
        while( allBlocksToUpload.size() > 0 || runningThreads.size() > 0 ) {

            // check if threads are finished
            boolean threadsFinished = false;
            for( int y=runningThreads.size()-1; y >= 0; y-- ) {
                
                PutKeyThread pkt = (PutKeyThread)runningThreads.get(y);
                if( pkt.isAlive() == false ) {
                    if( pkt.getSuccess() == true ) {
                        totalFinishedBlocks++;
                        // don't add block back to list
                        if( ulItem != null && ulItem.getKey() != null ) {                                
                            ulItem.setDoneBlocks( totalFinishedBlocks );
                        }
                        // now done in thread
                        // splitfile.createRedirectFile(true);
                    } else {
                        // no success, append block to end of list
                        allBlocksToUpload.addLast(pkt.getBlock());
                    }
                    runningThreads.remove(y);
                    threadsFinished = true; // blocksToUploadCount changed, recheck
                }
            }
            if( threadsFinished == true ) {
                continue;
            }
            
            // start one thread with next block in queue
            int maxThreads = MainFrame.frostSettings.getIntValue("splitfileUploadThreads"); // allows dynamic change
            int maxThreadsNeeded = allBlocksToUpload.size();
            int threadCountAllowedToStart = maxThreads - runningThreads.size();
            if( maxThreadsNeeded > 0 && threadCountAllowedToStart > 0 ) {
                FecBlock block = (FecBlock)allBlocksToUpload.removeFirst(); 
                PutKeyThread thread = new PutKeyThread( splitfile, block, htl, uri );
                runningThreads.add( thread );
                thread.start();
                Mixed.wait(1111); 
                continue;
            }
            // wait before next loop
            // NICE: we could use notify/wait on a synced queue here instead of polling
            Mixed.wait(2500); 
        }

        // ** Upload of blocks must be completely finished here! **
        
        // paranoia: get generated key to check against freenets generated key for redirect file
        String chkKey = null;
        if( ulItem != null ) { 
            chkKey = ulItem.getKey();
        }
        
        // upload redirect file
        boolean success = false;
        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if( connection != null ) {

            // create normal redirect file for uploading
            splitfile.createRedirectFile(false);
            try {
                String resultstr = connection.putKeyFromArray(uri,
                    null,
                    FileAccess.readByteArray(splitfile.getRedirectFile()),
                    htl);
                String[] result = result(resultstr);
                                     
                if( chkKey != null && result[1].indexOf(chkKey) < 0 ) {

                    logger.severe("Error: the CHK keys for redirect file generated by frost and freenet differ:\n" +
                    			   "FreeNet Key ='" + result[1] + "'\n" +
                    			   "Frost Key   ='" + chkKey + "'");
                    
                    chkKey = result[1];
                    
                    // just if its needed sometimes: code to extract chk key out of freenet result msg
                    /*if( text.indexOf("CHK@") != -1 )
                    {
                        result[1] = text.substring(text.lastIndexOf("CHK@"),
                                                   text.lastIndexOf("EndMessage"));
                        result[1] = result[1].trim();
                    }*/
                } else if( chkKey == null ) { // attachment upload
                    // attachment uploaded, get key from freenet insert
                    chkKey = result[1];
                }
                
                if( result[0].equals("Success") ||
                    result[0].equals("KeyCollision") )
                {
                    success = true;
                } else {
					logger.severe("Could not upload redirect file: " + result);
                }
            } catch( Throwable e ) {
                success = false;
				logger.log(Level.SEVERE, "Error uploading redirect file", e);
            }
        }
        
        // if we tried to upload a file not contained in upload table, remove work files
        if( ulItem == null ) {
            splitfile.finishUpload(true);
        } else { // keep workfiles for next insert
            splitfile.finishUpload(false);
        }
        
        if( success == true ) {

            logger.info("Redirect successfully uploaded.");
            // return this to keep old behaviour ...
            return new String[]{"Success",chkKey};
            
// REDFLAG: what was / is this code intented to do? I could'nt find code that check for THIS file! 
//          i think tracking if file is already
//          uploaded today is done by lastUploadDate? Can this be removed ?             
/*            
            try
            {
                GregorianCalendar cal= new GregorianCalendar();
                cal.setTimeZone(TimeZone.getTimeZone("GMT"));

                String dirdate = cal.get(Calendar.YEAR) + ".";
                dirdate += cal.get(Calendar.MONTH) + 1 + ".";
                dirdate += cal.get(Calendar.DATE);

                String fileSeparator = System.getProperty("file.separator");
                String destination = frame1.keypool + boardfilename + fileSeparator + dirdate + fileSeparator;
                //connection = FcpFactory.getFcpConnectionInstance();
                // That's not yet clean. Original frost code requires to start the insert funktion
                // to generate the key, and here we process the results. Direct key generation
                // should replace that, then we can also remove the result method
                if( connection != null )
                {
                    String prefix = new String("freenet:");
                    if( chkKey.startsWith(prefix) ) chkKey = chkKey.substring(prefix.length());

                    FileAccess.writeFile("Already uploaded today", destination + chkKey + ".lck");
                }
            }
            catch( Exception e )
            {
            }*/
        }
        else {
            return ERROR;
        }
    }
    
    private static class PutKeyThread extends Thread {
        
        FecBlock block;
        int htl;
        boolean success;
        String uri;
        FecSplitfile splitfile;
        
        public PutKeyThread(FecSplitfile sf, FecBlock b, int h, String u) {
            block = b;
            htl = h;
            uri = u;
            splitfile = sf;
        }
        
        public void run() {

            block.setCurrentState(FecBlock.STATE_TRANSFER_RUNNING);
            
            this.success = false;
            FcpConnection connection = FcpFactory.getFcpConnectionInstance();
            if( connection != null ) {
                try {
                    String result = connection.putKeyFromArray(uri,//"CHK@", //block.getChkKey(),
                        block.getPaddedMemoryArray(),
                        null, htl);
                    if( result.indexOf("Success") > -1 ||
                        result.indexOf("KeyCollision") > -1 )
                    {
                        success = true;
                    }
                } catch( Throwable e ) {
                    logger.log(Level.WARNING, "Error during insert: ", e);
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
}
