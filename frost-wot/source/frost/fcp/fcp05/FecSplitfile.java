/*
  FecSplitfile.java / Frost
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
package frost.fcp.fcp05;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import fecimpl.*;
import freenet.support.*;
import frost.*;
import frost.util.*;

/**
 * This class represents a FEC splitfile.
 * Contains all data / check blocks.
 */
public class FecSplitfile
{
    public static final int MODE_UPLOAD = 1; // intendet mode for this file
    public static final int MODE_DOWNLOAD = 2;
    public static final int MODE_FINISHED = 3; // we are finished, currently only set in setCorrect...

    private static final String FROST_TRANSFER_INDICATOR = "namespace.frost.transferInProgress";
    private static final String FROST_TRANSFER_FINISHED_INDICATOR = "namespace.frost.transferFinished.";

    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FecSplitfile.class.getName());

    protected int transferMode; // the mode for this file: is it to upload or download?

    protected ArrayList<SingleSegmentValues> segmentValues; // holds infos about the segmentation of file
    protected File dataFile; // the target file + ".data"
    protected long dataFileSize;
    protected File checkBlocksFile; // the target file + ".checkblocks"
    protected long checkBlocksFileSize;
    protected File redirectFile; // // the target file + ".redirect"
    protected int fileDataBlockCount; // number of data blocks for whole file
    protected int fileCheckBlockCount; // number of check blocks for whole file
    protected File downloadTargetFile; // the real target filename

    protected OnionFECEncoder encoder = null;
    protected FrostFECEncodeBucketFactory fecEncodeFactory = null;
    protected OnionFECDecoder decoder = null;
    protected FrostFECDecodeBucketFactory fecDecodeFactory = null;

    // the lists holding the data+check blocks
    protected ArrayList<FecBlock> dataBlocks, checkBlocks;

    /**
     * This constructor expects that both files are valid.
     * Will check if files are existing or not and handle this.
     * After construction the object is ready and target files are
     * created or existing target files were scanned.
     * Filenames should be like:
     *   downloadtargetdir/DownloadFile.zip.data
     *   downloadtargetdir/DownloadFile.zip.redirect
     * Scans for existing
     *   downloadtargetdir/DownloadFile.zip.checkblocks
     *
     * @param downloadTargetFile
     * @param redirectFile
     */
    public FecSplitfile(File downloadFile, File redirectFile) throws IllegalStateException, Exception
    {
        transferMode = MODE_DOWNLOAD;
        this.downloadTargetFile = downloadFile;
        this.dataFile = new File(downloadTargetFile.getPath()+".data");
        this.redirectFile = redirectFile;
        // filesize and others is determined by parsing the redirect file

        this.checkBlocksFile = new File( downloadTargetFile.getPath() + ".checkblocks" );

        initFromRedirectFile();
    }

    /**
     * Constructor reads all info about splitfile from encoder and stores it.
     * Used to construct a file to upload.
     */
    public FecSplitfile(File uploadFile)
    {
        transferMode = MODE_UPLOAD;
        dataFile = uploadFile;
        dataFileSize = uploadFile.length();

        this.encoder = new OnionFECEncoder();
        this.fecEncodeFactory = new FrostFECEncodeBucketFactory();
        encoder.init( dataFileSize, this.fecEncodeFactory );

        fillSegmentValues( this.encoder );

        // working files are placed in localdata
        // they get name:
        //  c:\myfiles\datafile.abc  -->  _c_myfiles_datafile.abc
        // /home/user/datafile.abc   -->  _home_user_datafile.abc
        String filename = uploadFile.getPath();
        if( System.getProperty("os.name").startsWith("Windows") ) {
            // first a special windows handling: remove the ':'
            int pos = filename.indexOf(":");
            if( pos > -1 ) {
                String newfilename = filename.substring(0, pos) + filename.substring(pos + 1);
                filename = newfilename;
            }
        }
        // now convert all file.separator (e.g. / or \) to _
        filename = filename.replace(System.getProperty("file.separator").charAt(0), '_');

        // append localdata dir and a _ before filename
        filename = Core.frostSettings.getValue(SettingsClass.DIR_LOCALDATA) + "_" + filename;

        logger.fine("DBG-ULFILENAME="+filename);

        this.checkBlocksFile = new File( filename + ".checkblocks" );
        this.redirectFile = new File( filename + ".redirect" );
    }

    /**
     * This method initializes the splitfile using an existing redirect file.
     * This file tracks the upload/download state of the file.
     * Method expects an existing this.redirectFile and that the data/checkblock
     * files are created.
     * Sometimes it does different actions for upload or download, this is decided using
     * this.transferMode.
     *
     * @throws IllegalStateException
     * @throws Exception
     */
    protected void initFromRedirectFile() throws IllegalStateException, Exception
    {
         List lines = FileAccess.readLines(this.redirectFile);
         if( lines.size() == 0 )
            throw new IllegalStateException("Empty redirect file");

// DEBUG
//for(int s=0;s<lines.size();s++)
//    Core.getOut().println(lines.get(s).toString());

         long fileSize;
         int dataBlockCount;
         int checkBlockCount;

         String v1 = getValue(lines,"SplitFile.BlockCount");
         String v2 = getValue(lines,"SplitFile.CheckBlockCount");
         String v3 = getValue(lines,"SplitFile.Size");
         try {
            dataBlockCount = Integer.parseInt(v1, 16);
            checkBlockCount = Integer.parseInt(v2, 16);
            fileSize = Long.parseLong(v3, 16);
         } catch (Exception e) {
            logger.log(Level.SEVERE, "ERROR: One of the following 3 values is invalid in received redirect file:\n" +
                                     "(SplitFile.BlockCount='" + v1 + "')\n" +
                                     "(SplitFile.CheckBlockCount='" + v2 + "')\n" +
                                     "(SplitFile.Size='"+v3+"')", e);
            throw new IllegalStateException("Could not parse block count from redirect file: "+e.getMessage());
         }
         if( dataBlockCount == 0 || fileSize == 0) {
            throw new IllegalStateException("Invalid data block count of 0");
         }

         if( this.transferMode == MODE_DOWNLOAD ) {
            this.dataFileSize = fileSize;
            this.decoder = new OnionFECDecoder();
            this.fecDecodeFactory = new FrostFECDecodeBucketFactory();
            this.decoder.init(this.dataFileSize, this.fecDecodeFactory);
            fillSegmentValues(this.decoder);
        } else if( this.transferMode == MODE_UPLOAD ) {
            this.encoder = new OnionFECEncoder();
            this.fecEncodeFactory = new FrostFECEncodeBucketFactory();
            this.encoder.init(this.dataFileSize, this.fecEncodeFactory);
            fillSegmentValues(this.encoder);
        } else {
            throw new IllegalStateException("transferMode is invalid");
        }

         // paranoia
         if( this.fileDataBlockCount != dataBlockCount ||
             this.fileCheckBlockCount != checkBlockCount )
         {
            throw new IllegalStateException("Block counts from redirect file and from decoder do not match");
         }

        /*
         * redirect and data file are valid, and the redirect file exists.
         * In redirect file there could be parameter
         *  "namespace.frost.downloadInProgress=true"
         * This means the download is in progress and there are already some
         * blocks downloaded. If this parameter not appears, this is the
         * first time the download runs or there is not yet any block downloaded.
         */

         boolean transferContinues = getValue(lines, FROST_TRANSFER_INDICATOR).toLowerCase().equals("true");

        /* IF downloadContinues == true
         * This file download was in progress before.
         * This means the data and the checkblock file are already created.
         * We now have to step through lines, find the splitfile CHKs and create
         * all FecBlocks for this splitfile.
         * If there is a line
         *   "namespace.frost.transferFinished.SplitFile.Block"
         * this is an indication that this block is already downloaded and stored in file.
         */
         if( transferContinues == false && this.transferMode == MODE_DOWNLOAD ) {
            // create a data file with size of added data block sizes
            // this means the last bytes are padded with 0, after successful
            // download the filesize is set to correct size
            long addedDataBlockSize = 0;
            for( int x = 0; x < getSegmentCount(); x++ ) {
                SingleSegmentValues seginf = (SingleSegmentValues) getValuesForSegment(x);
                addedDataBlockSize += seginf.dataBlockCount * seginf.dataBlockSize;
            }
            logger.info("First time download, create target files");
            /*
             * This seems to be a first time download of this redirect file. The data and checkblock file are maye
             * created, lets check this here. We get filesize from redirect file and check against filesize of existing
             * files.
             */
            if( this.dataFile.isFile() == false || this.dataFileSize != addedDataBlockSize ) {
                // file is invalid, remove it and create new empty file.
                this.dataFile.delete();
                boolean created = createFileOfLength(this.dataFile, addedDataBlockSize);
                if( created == false ) {
                    throw new Exception("Could not create the data file");
                }
            }

            if( this.checkBlocksFile.isFile() == false || this.checkBlocksFileSize != this.checkBlocksFile.length() ) {
                // file is invalid, remove it and create new empty file.
                this.checkBlocksFile.delete();
                boolean created = createFileOfLength(this.checkBlocksFile, this.checkBlocksFileSize);
                if( created == false ) {
                    throw new Exception("Could not create the checkblocks file");
                }
            }
        }

        logger.info("Reading CHK keys from redirect file");
         buildFecBlocks(false);
         // read in all data block CHKs
         for( int x = 0; x < this.fileDataBlockCount; x++ ) {
            String blockParameter = "SplitFile.Block." + Integer.toHexString(x + 1);
            String blockChk = getValue(lines, blockParameter);

            if( blockChk == null ) {
                throw new IllegalStateException("Redirect file contains an invalid CHK for a data block");
            }

            FecBlock b = (FecBlock) this.dataBlocks.get(x);
            if( b == null || b.getIndexInFile() != x ) {
                throw new IllegalStateException("Could not find the data block");
            }

            if( blockChk.length() < 58 ) {
                b.setCurrentState(FecBlock.STATE_TRANSFER_INVALID);
                // could be 'Error' or something completely unknown
                if( blockChk.indexOf("Error") < 0 ) {
                    logger.warning("Warning: Found invalid key in redirect file: " + blockChk);
                }
                blockChk = null;
            } else if( transferContinues ) {
                String isBlockFinished = getValue(lines, FROST_TRANSFER_FINISHED_INDICATOR + blockParameter);
                if( isBlockFinished != null && isBlockFinished.toLowerCase().equals("true") ) {
                    b.setCurrentState(FecBlock.STATE_TRANSFER_FINISHED);
                }
            }
            b.setChkKey(blockChk);
        }

         // read in all check CHKs
         for( int x = 0; x < this.fileCheckBlockCount; x++ ) {
            String blockParameter = "SplitFile.CheckBlock." + Integer.toHexString(x + 1);
            String blockChk = getValue(lines, blockParameter);
            if( blockChk == null ) {
                throw new IllegalStateException("Redirect file contains an invalid CHK for a check block");
            }
            FecBlock b = (FecBlock) this.checkBlocks.get(x);
            if( b == null || b.getIndexInFile() != x )
                throw new IllegalStateException("Could not find the checkBlock");

            if( blockChk.length() < 58 ) {
                b.setCurrentState(FecBlock.STATE_TRANSFER_INVALID);
                // could be 'Error' or something completely unknown
                if( blockChk.indexOf("Error") < 0 ) {
                    logger.warning("Warning: Found invalid key in redirect file: " + blockChk);
                }
                blockChk = null;
            } else if( transferContinues ) {
                String isBlockFinished = getValue(lines, FROST_TRANSFER_FINISHED_INDICATOR + blockParameter);
                if( isBlockFinished != null && isBlockFinished.toLowerCase().equals("true") ) {
                    b.setCurrentState(FecBlock.STATE_TRANSFER_FINISHED);
                }
            }
            b.setChkKey(blockChk);
        }
         logger.info("Download prepared");
    }

    public boolean isDecodeable(int segmentNo) {
        SingleSegmentValues segval = (SingleSegmentValues) this.segmentValues.get(segmentNo);
        int neededBlocks = segval.dataBlockCount;
        int providedBlocks = 0;
        for( int x = 0; x < this.dataBlocks.size(); x++ ) {
            FecBlock b = (FecBlock) this.dataBlocks.get(x);
            if( b.getSegmentNo() == segmentNo && b.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                providedBlocks++;
            }
        }
        for( int x = 0; x < this.checkBlocks.size(); x++ ) {
            FecBlock b = (FecBlock) this.checkBlocks.get(x);
            if( b.getSegmentNo() == segmentNo && b.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                providedBlocks++;
            }
        }
        return (providedBlocks >= neededBlocks);
    }

    public boolean isDecodeNeeded(int segmentNo) {
        SingleSegmentValues segval = (SingleSegmentValues) this.segmentValues.get(segmentNo);
        int neededBlocks = segval.dataBlockCount;
        int providedBlocks = 0;
        for( int x = 0; x < this.dataBlocks.size(); x++ ) {
            FecBlock b = (FecBlock) this.dataBlocks.get(x);
            if( b.getSegmentNo() == segmentNo && b.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                providedBlocks++;
            }
        }
        return (providedBlocks < neededBlocks);
    }

    public void decode(int segmentNo) throws Throwable {
        if( !isDecodeNeeded(segmentNo)) {
            return; // we already have all data blocks
        }
        if( !isDecodeable(segmentNo) ) {
            throw new IllegalStateException("Can't decode the segment, too less blocks provided");
        }

        logger.info("Starting to decode segment " + segmentNo);
        SingleSegmentValues segval = (SingleSegmentValues)this.segmentValues.get(segmentNo);
        int dataBlocksInSegment = segval.dataBlockCount;

        // init decoder for this segment
        this.decoder.setSegment( segmentNo );

        ArrayList<Integer> requestedDataBlocksIxList = new ArrayList<Integer>(); // Integer
        ArrayList<RandomAccessFileBucket> requestedDataBlocksBucketList = new ArrayList<RandomAccessFileBucket>(); // RandomAccessFileBuckets

        // search downloaded data blocks, put existing into decoder,
        // and remember the missing for the list of missing blocks
        for( int x = 0; x < this.dataBlocks.size(); x++ ) {
            FecBlock b = (FecBlock) this.dataBlocks.get(x);
            if( b.getSegmentNo() == segmentNo ) {
                if( b.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                    decoder.putBucket(b.getRandomAccessFileBucket(true), b.getIndexInSegment());
                } else {
                    requestedDataBlocksIxList.add(new Integer(b.getIndexInSegment()));
                    requestedDataBlocksBucketList.add(b.getRandomAccessFileBucket(false));
                }
            }
        }

        // search downloaded check blocks and put them into decoder
        for( int x = 0; x < this.checkBlocks.size(); x++ ) {
            FecBlock b = (FecBlock) this.checkBlocks.get(x);
            if( b.getSegmentNo() == segmentNo && b.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                decoder.putBucket(b.getRandomAccessFileBucket(true), (dataBlocksInSegment + b.getIndexInSegment()));
            }
        }

        // build int[] array with requested data block indicies
        int[] missingDataBlockIx = new int[requestedDataBlocksIxList.size()];
        for( int x = 0; x < missingDataBlockIx.length; x++ ) {
            missingDataBlockIx[x] = ((Integer) requestedDataBlocksIxList.get(x)).intValue();
        }

        // provide the bucketFactory with the buckets for the missing blocks
        this.fecDecodeFactory.init( requestedDataBlocksBucketList );

        // provide an empty array to hold the returned buckets
        // we don't need them here ... our factory hands out the correct buckets on the fly
        Bucket[] targetBuckets = new Bucket[missingDataBlockIx.length];
        this.decoder.decode(missingDataBlockIx, targetBuckets);

        logger.info("Finished decoding segment " + segmentNo);
    }

    /**
     * Checks for valid, existing checkblocks+redirect file and reads them in,
     * or returns false to indicate there are no valid files existing and
     * an encode() is needed.
     *
     * @return boolean  true if splitfile exists and is encoded, false if not
     */
    public boolean uploadInit() {
        boolean splitfileExists = false;
        if( this.redirectFile.isFile() && this.redirectFile.length() > 0 && this.checkBlocksFile.isFile()
                && this.checkBlocksFile.length() > 0 ) {
            // check for sure if filesize in redirect file is same as datafilesize
            List lines = FileAccess.readLines(this.redirectFile);
            String slen = getValue(lines, "SplitFile.Size");
            if( slen.length() > 0 ) {
                long fsize = Long.parseLong(slen, 16);
                if( fsize == this.dataFile.length() ) {
                    splitfileExists = true;
                    logger.info("Splitfile is already encoded.");
                    try {
                        initFromRedirectFile();
                    } catch (IllegalStateException e) {
                        logger.log(Level.SEVERE, "Exception thrown in uploadInit()", e);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception thrown in uploadInit()", e);
                    }
                }
            }
        }
        if( splitfileExists == false ) {
            logger.info("Splitfile needs encoding.");
            this.redirectFile.delete(); // for sure
            this.checkBlocksFile.delete();
        }
        return splitfileExists;
    }

    public void encode() throws Throwable {
        logger.info("Splitfile encode starts ...");
//        Core.getOut().println("Preparing check blocks file, size="+checkBlocksFileSize);
        this.fecEncodeFactory.init(this.checkBlocksFile, getCheckBlocksFileSize());

//        Core.getOut().println("Processing segments");

        // block sizes could differ in each segment
        RandomAccessFile raf = new RandomAccessFile( this.dataFile, "r" );
        int segmentCount = getSegmentCount();
        for(int actSegment = 0; actSegment < segmentCount; actSegment++) {
            int blockCount = getValuesForSegment(actSegment).dataBlockCount;
            int blockSize = getValuesForSegment(actSegment).dataBlockSize;
            long segmentStartOffset = getValuesForSegment(actSegment).segmentStartOffset;
//System.out.println("DBG: blockCount="+blockCount+" , blockSize="+blockSize);

//Core.getOut().println("seg="+actSegment+"  fsize="+filesize+"  blocks="+blockCount+"  bsize="+blockSize);

            Bucket[] actSegmentsDataBlocks = RandomAccessFileBucket2.segment(
                    this.dataFile, blockSize, segmentStartOffset, blockCount, true, raf);

//Core.getOut().println("lastb="+actSegmentsDataBlocks[actSegmentsDataBlocks.length-1].size());

            this.encoder.encode( actSegment, actSegmentsDataBlocks, null ); // null = request all checkblocks
        }

        //////////////////////////////////////////////////////////////////////////////////////
        // after successful encoding, build the FecBlock objects for each block (data+check)
        //////////////////////////////////////////////////////////////////////////////////////
        buildFecBlocks(true);

        /////////////////////////////////////////////////////////////////////////////////////
        // Finally, create and save the redirect file. Once this file is written, the
        // preparing is successfully finished.
        /////////////////////////////////////////////////////////////////////////////////////
        if( createRedirectFile(true) == false ) {
            String emsg = "Error: Could not create the redirect file.";
            logger.severe(emsg);
            throw new Exception(emsg);
        }
        logger.info("Splitfile encode finished.");
    }
    
    public void closeBuckets() {
        if( dataBlocks != null ) {
            for(Iterator i=dataBlocks.iterator(); i.hasNext(); ) {
                FecBlock fb = (FecBlock)i.next();
                fb.close();
            }
        }
        if( checkBlocks != null ) {
            for(Iterator i=checkBlocks.iterator(); i.hasNext(); ) {
                FecBlock fb = (FecBlock)i.next();
                fb.close();
            }
        }
    }

    /**
     * Build the list of data / check blocks.
     * If called with TRUE, expects an existing datafile and
     * creates the CHK keys for each block.
     *
     * @param encodeBlocks true to encode, false to only create list of blocks
     * @throws Exception
     */
    protected void buildFecBlocks(boolean encodeBlocks) throws Exception {
        this.dataBlocks = new ArrayList<FecBlock>();
        this.checkBlocks = new ArrayList<FecBlock>();

        int dataBlockIndexInFile = 0;
        int checkBlockIndexInFile = 0;
        long dataBlockOffset = 0;
        long checkBlockOffset = 0;
        int segmentCount = getSegmentCount();
        for(int actSegment = 0; actSegment < segmentCount; actSegment++) {
            int dataBlockIndexInSegment = 0;
            int checkBlockIndexInSegment = 0;

            int blockCount = getValuesForSegment(actSegment).dataBlockCount;
            int blockSize = getValuesForSegment(actSegment).dataBlockSize;
            int checkBlockCount = getValuesForSegment(actSegment).checkBlockCount;
            int checkBlockSize = getValuesForSegment(actSegment).checkBlockSize;

            // build data blocks and while on it compute CHK@ for each block
            if( encodeBlocks == true ) {
                logger.info("Creating CHK keys for "+blockCount+" blocks...");
            }
            for( int x=0; x<blockCount; x++ ) {

                FecBlock b = new FecBlock( FecBlock.TYPE_DATABLOCK,
                                           this.dataFile,
                                           actSegment,
                                           dataBlockIndexInSegment,
                                           dataBlockIndexInFile,
                                           blockSize,
                                           dataBlockOffset
                                         );

                if( encodeBlocks == true ) {
                    Bucket blockdata = b.getPaddedMemoryBucket();
                    String chkKey = FecTools.generateCHK( blockdata );
                    if( chkKey == null ) {
                        String msg = "ERROR: could NOT generate CHK key of a splitfile data block!!!";
                        logger.severe(msg);
                        throw new Exception(msg);
                    }
                    b.setChkKey( chkKey );
                }

                dataBlockOffset += blockSize;
                dataBlockIndexInFile++;
                dataBlockIndexInSegment++;

                this.dataBlocks.add( b );
            }

            // build check blocks and while on it compute CHK@ for each block
            if( encodeBlocks == true ) {
                logger.fine("Creating CHK keys for "+checkBlockCount+" check blocks...");
            }

            for( int x=0; x<checkBlockCount; x++ ) {

                FecBlock b = new FecBlock( FecBlock.TYPE_CHECKBLOCK,
                                           this.checkBlocksFile,
                                           actSegment,
                                           checkBlockIndexInSegment,
                                           checkBlockIndexInFile,
                                           checkBlockSize,
                                           checkBlockOffset
                                         );
                if( encodeBlocks == true ) {
                    Bucket blockdata = b.getPaddedMemoryBucket();
                    String chkKey = FecTools.generateCHK( blockdata );
                    if( chkKey == null ) {
                        String msg = "ERROR: could NOT generate CHK key of a splitfile check block!!!";
                        logger.severe(msg);
                        throw new Exception(msg);
                    }
                    b.setChkKey( chkKey );
                }

                checkBlockOffset += checkBlockSize;
                checkBlockIndexInFile++;
                checkBlockIndexInSegment++;

                this.checkBlocks.add( b );
            }
        }
    }

    /**
     * Creates a redirect file. Is called also from within concurrent threads,
     * hence this method must be synchronized.
     *
     * @param transferInProgress  if true a redirect file containing the progress is written
     * @return
     */
    public synchronized boolean createRedirectFile(boolean transferInProgress) {

        String s = getRedirectFileContent(transferInProgress);
        FileAccess.writeFile(s, this.redirectFile);

        return true;
    }

    /**
     * Creates a redirect filecontent . Is called also from within concurrent threads,
     * hence this method must be synchronized.
     *
     * @param transferInProgress  if true a redirect file containing the progress is written
     * @return
     */
    public synchronized String getRedirectFileContent(boolean transferInProgress) {
        // TODO: maybe add metafile information and checksum

        StringBuilder redirect = new StringBuilder(512);
        redirect.append("Version\n");
        redirect.append("Revision=1\n");
        redirect.append("EndPart\n");
        redirect.append("Document\n");

        if( transferInProgress ) {
            redirect.append(FROST_TRANSFER_INDICATOR).append("=true\n");
        }

        redirect.append("SplitFile.AlgoName=OnionFEC_a_1_2\n");
        redirect.append("SplitFile.Size=").append(Long.toHexString(this.dataFileSize).toLowerCase()).append("\n");
        // insert all data block references
        redirect.append("SplitFile.BlockCount=").append(Integer.toHexString(this.dataBlocks.size())).append("\n");
        for( int x = 0; x < this.dataBlocks.size(); x++ ) {
            FecBlock fb = (FecBlock) this.dataBlocks.get(x);
            String blockChk = fb.getChkKey();
            if( blockChk == null ) {
                blockChk = "Error";
            }
            redirect.append("SplitFile.Block.").append(Integer.toHexString(x + 1)).append("=").append(blockChk).append("\n");
            if( transferInProgress && fb.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                redirect.append(FROST_TRANSFER_FINISHED_INDICATOR).append("SplitFile.Block.").append(
                        Integer.toHexString(x + 1)).append("=true\n");
            }
        }
        // insert all check block references
        redirect.append("SplitFile.CheckBlockCount=").append(Integer.toHexString(this.checkBlocks.size())).append("\n");
        for( int x = 0; x < this.checkBlocks.size(); x++ ) {
            FecBlock fb = (FecBlock) this.checkBlocks.get(x);
            String blockChk = fb.getChkKey();
            if( blockChk == null ) {
                blockChk = "Error";
            }
            redirect.append("SplitFile.CheckBlock.").append(Integer.toHexString(x + 1)).append("=").append(blockChk)
                    .append("\n");
            if( transferInProgress && fb.getCurrentState() == FecBlock.STATE_TRANSFER_FINISHED ) {
                redirect.append(FROST_TRANSFER_FINISHED_INDICATOR).append("SplitFile.CheckBlock.").append(
                        Integer.toHexString(x + 1)).append("=true\n");
            }
        }

        redirect.append("End\n");

        return redirect.toString();
    }

/*
This is a Redirect file created by freenet itself.
I used this as a template, e.g. the hex file size is lowercase, ...

Version
Revision=1
EndPart
Document
Info.Format=application/x-zip-compressed
// Info.Checksum=03e56e32086000be9962f23b231575e36e44ad13
Info.Description=file
SplitFile.AlgoName=OnionFEC_a_1_2
SplitFile.Block.7=freenet:CHK@Rz0cvf8M6Iz0z0jMBAujy9YU5p8SAwI,aSbpE~pwb6eeUdeZ55EGcg
SplitFile.Block.6=freenet:CHK@6hzoBSwXiKbawiI~SKd7Gz-dUAcSAwI,HSYXERk1FXugXCCpR6H8vg
SplitFile.Block.5=freenet:CHK@mygDm9UFzIv43c1loAlFt6Ofc5MSAwI,HbsW94ya6oJcfqUZay0PVA
SplitFile.Size=1a4449
SplitFile.Block.4=freenet:CHK@BD-wYKO~jtlSr2ZZsirc-aOlL48SAwI,bjsn7stpOqAv24G0Adlz~w
SplitFile.Block.3=freenet:CHK@cAQEhDtXHEDz1IwD0Zyt5VgZ3h0SAwI,b01JDJ289MwTv8vuMgpNtw
SplitFile.Block.2=freenet:CHK@~SFyyEX417QRKBcQVdJ4vKM9PMcSAwI,oxBLhllktYIN1-tz5i~VVg
SplitFile.Block.1=freenet:CHK@EXz7rt1urVeRzOBi~VGQJ1CIBUoSAwI,KvMbpc95nzYRr1YDKIlqWg
SplitFile.BlockCount=7
SplitFile.CheckBlock.3=freenet:CHK@dFkmDEU5m5iiRHhkAcrRi7SkWgQSAwI,XYJ-pkEu67bPoYhTv7YU5A
SplitFile.CheckBlock.2=freenet:CHK@l7Ypv2wpWLSCx7vQYeWcgoRnV40SAwI,-L0E-cZCZYFNNObayvuJ~w
SplitFile.CheckBlock.1=freenet:CHK@PuXnVzzQv4ZzIhy5gO8R2zDKKqoSAwI,CL5mbL85tYkhfviqcm6sUQ
SplitFile.CheckBlockCount=3
End
*/
    protected void fillSegmentValues(OnionFECBase myEncoder) {

        int segmentCount = myEncoder.getSegmentCount();
        this.segmentValues = new ArrayList<SingleSegmentValues>(segmentCount);
        this.checkBlocksFileSize = 0;

        this.fileDataBlockCount = 0;
        this.fileCheckBlockCount = 0;

        long tmpSegmentStartOffset = 0;
        for(int z=0; z < segmentCount; z++) {
            SingleSegmentValues sval = new SingleSegmentValues();
            sval.checkBlockSize = myEncoder.getCheckBlockSize(z);
            sval.checkBlockCount = myEncoder.getN(z) - myEncoder.getK(z);
            sval.dataBlockSize = myEncoder.getBlockSize(z);
            sval.dataBlockCount = myEncoder.getK(z);
            sval.segmentSize = myEncoder.getSegmentSize(z);
            sval.segmentStartOffset = tmpSegmentStartOffset;
            this.segmentValues.add( sval );

            tmpSegmentStartOffset += sval.segmentSize;

            this.checkBlocksFileSize += (sval.checkBlockSize * sval.checkBlockCount);
            this.fileDataBlockCount += sval.dataBlockCount;
            this.fileCheckBlockCount += sval.checkBlockCount;
        }
    }

    public long getCheckBlocksFileSize() {
        return checkBlocksFileSize;
    }

    public SingleSegmentValues getValuesForSegment(int ix) {
        if( ix < 0 || ix > this.segmentValues.size() ) {
            return null;
        }
        return (SingleSegmentValues) this.segmentValues.get(ix);
    }

    public int getSegmentCount() {
        return this.segmentValues.size();
    }

    public List<FecBlock> getCheckBlocks() {
        return checkBlocks;
    }

    public List<FecBlock> getDataBlocks() {
        return dataBlocks;
    }

    /**
     * The BucketFactory used to create the check block buckets. Initially creates a file to hold all check blocks, then
     * gives out an area in this file for each checkblock.
     */
    private class FrostFECEncodeBucketFactory implements BucketFactory
    {
        int checkBlockCounter = 0;
        File factoryCheckBlocksFile;
        long actFileOffset = 0;
        RandomAccessFile raf = null;

        public void init(File cBlocksFile, long myCheckBlocksFileSize) throws IOException {

            factoryCheckBlocksFile = cBlocksFile;
            if( factoryCheckBlocksFile.exists() &&
                factoryCheckBlocksFile.length() == myCheckBlocksFileSize )
            {
                return; // file already existing
            }

            factoryCheckBlocksFile.delete();
            // create file with given size
            boolean created = createFileOfLength( cBlocksFile, myCheckBlocksFileSize );

            // init the used raf
            raf = new RandomAccessFile( factoryCheckBlocksFile, "rw" );
        }

        public Bucket makeBucket(long size) throws IOException {

            // the RandomAccessFileBucket2 reuses the open RandomAccessFile internally (seek)
            // only 1 bucket is written to at a time!
            RandomAccessFileBucket2 b = new RandomAccessFileBucket2( factoryCheckBlocksFile, actFileOffset, size, false, raf);
            actFileOffset += size;
            return b;
        }

        public void freeBucket(Bucket b) throws IOException {
            // called if an error occurs to clean the partially written check block buckets
            // usually this means the whole check block generation failed.

            //Core.getOut().println("FreeBucket");
        }
    }

    private class FrostFECDecodeBucketFactory implements BucketFactory {

        int actualIndexInBucketList;
        List bucketList;

        public void init(List blist) throws IOException {
            bucketList = blist;
            actualIndexInBucketList = 0;
        }

        public Bucket makeBucket(long size) throws IOException {
            // FECUtils.makeBuckets will request this blocks in order...
            Bucket b = (Bucket)bucketList.get(actualIndexInBucketList);
            actualIndexInBucketList++;
            if( b.size() != size ) {
                throw new IOException("Bucket size ("+b.size()+") differs of requested size ("+size+") "+actualIndexInBucketList);
            }
            return b;
        }
        public void freeBucket(Bucket b) throws IOException {
            // called if an error occurs to clean the partially written check block buckets
            // usually this means the whole check block generation failed.

            //Core.getOut().println("FreeBucket");
        }
    }

    public class SingleSegmentValues
    {
        public long segmentSize;
        public long segmentStartOffset; // the offset in input file at which this segment starts

        public int dataBlockSize;
        public int dataBlockCount;
        public int checkBlockSize;
        public int checkBlockCount;
    }

    public void setCorrectDatafileSize() {
        this.transferMode = MODE_FINISHED;
        try {
            RandomAccessFile f = new RandomAccessFile(this.dataFile, "rw");
            if( f.length() > this.dataFileSize ) {
                // truncate file
                f.setLength(this.dataFileSize);
            }
            f.close();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception thrown in setCorrectDatafileSize()", ex);
        }
    }

    /**
     * Rename the tmp. download file to real target name, maybe remove redirect and checkblocks file. Can be called to
     * clean up for cancelled downloads or for finishing a successful download.
     */
    public void finishDownload(boolean removeWorkFiles) {
        // check if size was set after successful download+decode
        if( this.transferMode == MODE_FINISHED ) {
            // download was ok, handle finished download.
            boolean ret = this.dataFile.renameTo(this.downloadTargetFile);
            if( ret == false ) {
                logger.severe("ERROR: Could not move file '" + dataFile.getPath() + "' to '"
                        + downloadTargetFile.getPath() + "'.\n"
                        + "Maybe the locations are on different filesystems where a move is not allowed.\n"
                        + "Please try change the location of 'temp.dir' in the frost.ini file.");
                this.dataFile = null;
            }
        }

        if( removeWorkFiles ) {
            if( this.dataFile != null && // was rename ok?
                    !dataFile.getPath().equals(this.downloadTargetFile) ) // paranoia, dont delete correct file
            {
                dataFile.delete();
            }
            redirectFile.delete();
            checkBlocksFile.delete();
        }
    }

    /**
     * Maybe remove redirect and checkblocks file. Can be called to clean up for cancelled uploads or for finishing a
     * successful upload.
     */
    public void finishUpload(boolean removeWorkFiles) {
        this.transferMode = MODE_FINISHED;

        if( removeWorkFiles ) {
            redirectFile.delete();
            checkBlocksFile.delete();
        }
    }

    public long getDataFileSize() {
        return dataFileSize;
    }

    public File getRedirectFile() {
        return redirectFile;
    }

    /**
     * Creates a new file and sets its length to requested length. First the RandomAccessFile.setLength() is called, but
     * there is a problem in linux vfat driver and an Exception is throw if you call setLength to enlarge a file that
     * resides on a FAT32 partition (truncating works). So these method catches an Excpetion in setLength() and tries to
     * create the file using write. The contents of the file are not defined after creation.
     *
     * @param newfile
     *            the new file to create, must not be existent
     * @param filelength
     *            the requested filelength that the file should have.
     * @return true if ceate was successful, or false if not.
     */
    protected boolean createFileOfLength( File newfile, long filelength)
    {
        try {
            RandomAccessFile raf = new RandomAccessFile( newfile, "rw" );
            // create file with given size
            // ATTN: there is a bug in linux vfat driver: truncating a file works, but enlarging not
            boolean fileLengthSet = false;
            try {
                raf.setLength( filelength );
                fileLengthSet = true;
                raf.close();
            } catch(IOException ex) {

                // * if there is not enough space on disk getMessage() will return:
                //     "There is not enough space on the disk"
                // * if filesystem does not support such large files, getMessage() will return:
                //     "An attempt was made to move the file pointer before the beginning of the file"
                //   - e.g. this msg occurs on FAT16 if you create a 2GB file -> 2GB is -1
                String msgtxt = ex.getMessage();
                try { raf.close(); } catch(Exception ex2) { }
                newfile.delete();
                if( msgtxt.equals("There is not enough space on the disk") ||
                    msgtxt.equals("An attempt was made to move the file pointer before the beginning of the file") )
                {
                    // these errors are not recoverable
                    logger.severe("ERROR: Could not create the work file: " + msgtxt);
                    newfile.delete();
                    return false;
                }

                logger.warning("Warning: Could not create a work file, error=" + msgtxt);

                // * if the linux vfat problem occurs, getMessage() will return:
                //     "Operation not permitted" or "Invalid argument"
                // This error _could_ be recoverable, so try to fill using write (below) ...
                // and currently we also try to recover on all other exception messages
            }

            if( fileLengthSet == false ) {

                logger.info("Trying to use a slower creating method, starting ...");
                // fallback, fill file using write
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newfile));
                long written = 0;
                while( written < filelength ) {
                    out.write(0);
                    written++;
                }
                out.close();
                logger.info("... finished to create the work file using slower creation method.");
            }
            return true;
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "createFileOfLength(File newfile, long filelength)", ex);
        }
        newfile.delete(); // delete file on error
        logger.severe("ERROR: Could not create the work file " + newfile.getPath());
        return false;
    }
    
    /**
     * Returns the requested value in a settings vector
     * 
     * @param lines
     *            Vector containing lines of a settings file
     * @param value
     *            the requested value
     * @return String the requested value as a String
     */
    private static String getValue(List lines, String value) {
        for( int i = 0; i < lines.size(); i++ ) {
            String line = (String) lines.get(i);
            if( line.startsWith(value + "=") || line.startsWith(value + " ") ) {
                if( line.indexOf("=") != -1 ) {
                    return line.substring(line.indexOf("=") + 1, line.length()).trim();
                }
            }
        }
        logger.fine("Setting not found: " + value);
        return "";
    }
}
