/*
  FcpRequest.java / Frost
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
package frost.fcp.fcp07;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.util.*;

/**
 * Requests a key from freenet
 */
// while requesting / inserting, show chunks left to try (incl. trying chunks) -> Warte (9) / 18% (9)
public class FcpRequest {
    
	final static boolean DEBUG = true;

	private static Logger logger = Logger.getLogger(FcpRequest.class.getName());
    
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
    public static FcpResultGet getFile(
            int type,
            String key,
            Long size,
            File target,
            int maxSize,
            boolean createTempFile,
            FrostDownloadItem dlItem)
    {
        File tempFile = null;
        if( createTempFile ) {
            tempFile = FileAccess.createTempFile("getFile_", ".tmp");
        } else {
            tempFile = new File( target.getPath() + ".tmp" );
        }

        // First we just download the file, not knowing what lies ahead
        FcpResultGet results = getKey(type, key, tempFile, maxSize, dlItem);

        if( results.isSuccess() ) {

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
               return FcpResultGet.RESULT_FAILED;
            }
        } else {
            // if we reach here, the download was NOT successful in any way
            tempFile.delete();
        }
        return results;
    }

    // used by getFile
    private static FcpResultGet getKey(int type, String key, File target, int maxSize, FrostDownloadItem dlItem) {

        if( key == null || key.length() == 0 || key.startsWith("null") ) {
            System.out.println("getKey: KEY IS NULL!");
            return FcpResultGet.RESULT_FAILED;
        }

        FcpConnection connection;
        try {
            connection = FcpFactory.getFcpConnectionInstance();
        } catch (ConnectException e1) {
            connection = null;
        }
        
        FcpResultGet results = null;

        if( connection != null ) {
            int tries = 0;
            int maxtries = 3;
            while( tries < maxtries ) {
                try {
                    results = connection.getKeyToFile(type, key, target, maxSize, dlItem);
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
            printableKey = new StringBuilder().append(keyPrefix)
                                             .append("...")
                                             .append(keyUrl).toString();
        }
        
        System.out.println("getKey: file='"+target.getPath()+"' ; len="+target.length());
        
        if( results == null ) {
            // paranoia
            results = FcpResultGet.RESULT_FAILED;
            System.out.println("getKey - Failed, result=null");
        } else if( results.isSuccess() && target.length() > 0 ) {
            logger.info("getKey - Success: " + printableKey );
            System.out.println("getKey - Success: " + printableKey);
        } else {
            target.delete();
            logger.info("getKey - Failed: " + printableKey + "; rc="+results.getReturnCode()+"; isFatal="+results.isFatal() );
            System.out.println("getKey - Failed: " + printableKey + "; rc="+results.getReturnCode()+"; isFatal="+results.isFatal());
        }
        return results;
    }
}
