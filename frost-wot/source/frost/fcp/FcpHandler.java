/*
  FcpHandler.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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

import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

public abstract class FcpHandler {

    private static Logger logger = Logger.getLogger(FcpHandler.class.getName());
    
    private static FcpHandler instance = null;
    
    public static int FREENET_05 = 5;
    public static int FREENET_07 = 7;
    
    private static int initializedVersion = -1;
    
    public static FcpHandler inst() {
        return instance;
    }
    
    public static int getInitializedVersion() {
        return initializedVersion;
    }

    public static void initializeFcp(List nodes, int freenetVersion) throws UnsupportedOperationException {
        
        if( freenetVersion == FREENET_05 ) {
            instance = new FcpHandler05();
            instance.initialize(nodes);
            initializedVersion = freenetVersion;
        } else if( freenetVersion == FREENET_07 ) {
            instance = new FcpHandler07();
            instance.initialize(nodes);
            initializedVersion = freenetVersion;
        } else {
            logger.severe("Unsupported freenet version: "+freenetVersion);
            throw new UnsupportedOperationException("This Freenet version is not supported, must be 5 or 7: "+freenetVersion);
        }
    }
    
    public abstract void initialize(List nodes);
    
    public abstract List getNodes();
    
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
     * @return null on error, or FcpResults
     */
    public FcpResults getFile(String key,
                                  Long size,
                                  File target,
                                  int htl,
                                  boolean doRedirect)
    {
        // use temp file by default, only filedownload needs the target file to monitor download progress
        return getFile(key,size,target,htl,doRedirect, false, true, null);
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
     * @param fastDownload  If true request stop if node reports a timeout. If false try until node indicates end.
     * @return null on error, or FcpResults
     */
    public FcpResults getFile(String key,
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
     * getFile retrieves a file from Freenet. It does detect if this file is a redirect, a splitfile or
     * just a simple file. It checks the size for the file and returns false if sizes do not match.
     * Size is ignored if it is NULL
     *
     * @param key The key to retrieve. All to Freenet known key formats are allowed (passed to node via FCP).
     * @param size Size of the file in bytes. Is ignored if not an integer value or -1 (splitfiles do not need this setting).
     * @param target Target path
     * @param htl request htl
     * @param doRedirect If true, getFile redirects if possible and downloads the file it was redirected to.
     * @param fastDownload  If true request stop if node reports a timeout. If false try until node indicates end.
     * @param createTempFile  true to download to a temp file and rename to target file after success.  
     * @param dlItem   The DownloadItem for this download for progress updates, or null if there is none.
     * @return null on error, or FcpResults
     */
    public abstract FcpResults getFile(String key,
                                  Long size,
                                  File target,
                                  int htl,
                                  boolean doRedirect,
                                  boolean fastDownload,
                                  boolean createTempFile,
                                  FrostDownloadItem dlItem);
    
    /**
     * Inserts a file into freenet.
     * The boardfilename is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * This method wraps the calls without the uploadItem.
     */
    public String[] putFile(
            String uri,
            File file,
            byte[] metadata,
            int htl,
            boolean doRedirect,
            boolean removeLocalKey)
    {
        return putFile(uri, file, metadata, htl, doRedirect, removeLocalKey, null);
    }

    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public abstract String[] putFile(
            String uri,
            File file,
            byte[] metadata,
            int htl,
            boolean doRedirect,
            boolean removeLocalKey,
            FrostUploadItem ulItem);
    
    public abstract String generateCHK(File file) throws Throwable;
    
    public abstract List getNodeInfo() throws IOException, ConnectException;
    
    public abstract BoardKeyPair generateBoardKeyPair() throws IOException, ConnectException;
}
