import com.onionnetworks.fec.*;
import com.onionnetworks.util.*;

import freenet.client.*;
import freenet.support.*;
import freenet.support.io.*;

import java.io.*;

public class test
{
    public static void main(String argv[]) throws Throwable
    {
        new test().run();
    }

    public void run() throws Throwable
    {
        System.out.println("Starting");
        File inputFile = new File("biginputfile.dat");
        long filesize = inputFile.length();

        OnionFECEncoder encoder = new OnionFECEncoder();
        FrostFECEncodeBucketFactory fecFactory = new FrostFECEncodeBucketFactory();
        encoder.init( filesize, fecFactory );

        int segmentCount = encoder.getSegmentCount();

        long checkBlocksFileSize = 0;
        for(int z=0; z<segmentCount; z++)
        {
            int checkBlockSize = encoder.getCheckBlockSize(z);
            int checkBlockCount = encoder.getN(z) - encoder.getK(z);
System.out.println("segment="+z+"  checkblocks="+checkBlockCount+"  checkblocksize="+checkBlockSize);
            checkBlocksFileSize += checkBlockSize * checkBlockCount;
        }

        System.out.println("Preparing check blocks file, size="+checkBlocksFileSize);
        fecFactory.init("biginputfile.checks", checkBlocksFileSize);
        System.out.println("Processing segments");

        // block sizes could differ in each segment
        long segmentStartOffset = 0;
        for(int actSegment = 0; actSegment < segmentCount; actSegment++)
        {
            int blockCount = encoder.getK( actSegment );
            int blockSize = encoder.getBlockSize( actSegment );

System.out.println("seg="+actSegment+"  fsize="+filesize+"  blocks="+blockCount+"  bsize="+blockSize);
            Bucket[] actSegmentsDataBlocks = RandomAccessFileBucket.segment(
                    inputFile, blockSize, segmentStartOffset, blockCount, true);
System.out.println("lastb="+actSegmentsDataBlocks[actSegmentsDataBlocks.length-1].size());
            encoder.encode( actSegment, actSegmentsDataBlocks, null ); // null = request all checkblocks

            segmentStartOffset += encoder.getSegmentSize(actSegment);
        }

    }

    class FrostFECEncodeBucketFactory implements BucketFactory
    {
        int checkBlockCounter = 0;
        File checkBlocksFile;
        long actFileOffset = 0;

        public void init(String checkBlocksFileName, long checkBlocksFileSize) throws IOException
        {
            checkBlocksFile = new File(checkBlocksFileName);
            RandomAccessFile raf = new RandomAccessFile( checkBlocksFile, "rw" );
            raf.setLength( checkBlocksFileSize );
            raf.close();
        }

        public Bucket makeBucket(long size) throws IOException
        {
            RandomAccessFileBucket b = new RandomAccessFileBucket( checkBlocksFile, actFileOffset, size, false );
            actFileOffset += size;
            return b;
        }
        public void freeBucket(Bucket b) throws IOException
        {
            System.out.println("FreeBucket");
        }
    }
}