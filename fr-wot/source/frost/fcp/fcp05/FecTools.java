/*
  FecTools.java / Frost
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

import java.io.File;
import java.util.*;
import java.util.logging.*;

import freenet.client.ClientCHK;
import freenet.support.*;

/**
 * This class contains methods to:
 * - compute check blocks out of the upload file
 * - compute the CHK@ keys of blocks / files
 *
 * Class uses the implementation of freenet to do computations.
 */
public class FecTools {
    
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FecTools.class.getName());

    /****************************************
     * Methods for CHK@ key generation
     ****************************************/

    /**
     * Generate the CHK@ key of data in a given file.
     *
     * @return String with generated CHK@ key _or_ null on error
     */
    public static String generateCHK(File inputfile) {
        return generateCHK(inputfile, 0);
    }

    /**
     * Generate the CHK@ key of META data in a given file.
     * 
     * @return String with generated CHK@ key _or_ null on error
     */
    public static String generateCHK(File inputfile, long metalength) {
        if( inputfile == null || inputfile.exists() == false || inputfile.length() == 0 ) {
            return null;
        }

        Bucket data = new FileBucket(inputfile);
        return generateCHK(data, metalength);
    }

    /**
     * Generate CHK@ key of data in a given byte array.
     * 
     * @return String with generated CHK@ key _or_ null on error
     */
    public static String generateCHK(byte[] inputdata) {
        return generateCHK(inputdata, 0);
    }

    public static String generateCHK(byte[] inputdata, long metalength) {
        if( inputdata == null || inputdata.length == 0 ) {
            return null;
        }

        Bucket data = new ArrayBucket(inputdata);
        return generateCHK(data, metalength);
    }

    public static String generateCHK(Bucket inputbucket) {
        return generateCHK(inputbucket, 0);
    }

    /**
     * Generate CHK@ key of data in a given Bucket.
     * 
     * @return String with generated CHK@ key _or_ null on error
     */
    public static String generateCHK(Bucket inputbucket, long metalength) {
        if( inputbucket == null || inputbucket.size() <= 0 ) {
            return null;
        }

        try {
            long size = inputbucket.size();
            ClientCHK chk = new ClientCHK();
            // chk.setCipher("Twofish"); // this is the default!

            // provide some temp storage for computation
            byte[] tmpStorage = new byte[(int) chk.getTotalLength(size)];
            Bucket ctBucket = new ArrayBucket(tmpStorage);

            String chkKey = null;

            // call freenet methods to do the work ...
            chk.encode(inputbucket, metalength, ctBucket).close();
            chkKey = chk.getURI().toString();
            // System.out.println("chk="+chkKey);
            return chkKey;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception in FecTools.generateCHK()", t);
        }
        return null;
    }

    /*******************************************************************************************************************
     * Methods for FEC check block encoding
     ******************************************************************************************************************/

    /**
     * Prepares an file for upload.
     * Builds a complete FecSplitfile object containing the
     * check blocks, the CHK@ keys of all blocks and the redirect file.
     */
    public static FecSplitfile prepareFECSplitfile(File inputFile) throws Throwable {
        if( inputFile == null || inputFile.isFile() == false || inputFile.length() == 0 ) {
            return null;
        }

        FecSplitfile splitfile = new FecSplitfile( inputFile );
        try {
            splitfile.encode();
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Error while encoding FEC splitfile", t);
            return null;
        }

        List<FecBlock> datab = splitfile.getDataBlocks();
        List<FecBlock> checkb = splitfile.getCheckBlocks();

        int cnt = 0;
        for(Iterator<FecBlock> i = datab.iterator(); i.hasNext(); ) {
            FecBlock b = i.next();
            logger.finer("data_" + cnt + ": '" + b.getChkKey() + "'");
            cnt++;
        }

        cnt = 0;
        for(Iterator<FecBlock> i = checkb.iterator(); i.hasNext(); ) {
            FecBlock b = i.next();
            logger.finer("check_" + cnt + ": '" + b.getChkKey() + "'");
            cnt++;
        }

        return splitfile;
    }
}
