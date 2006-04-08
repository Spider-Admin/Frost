package frost.fcp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import frost.fcp.fcp05.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

public class FcpHandler {

    private static Logger logger = Logger.getLogger(FcpHandler.class.getName());

    public static void initializeFcp(List nodes) {
        FcpFactory.init(nodes); // init the factory with configured nodes
    }
    
    public static List getNodes() {
        return FcpFactory.getNodes();
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
     * @return null on error, or FcpResults
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
    public static FcpResults getFile(String key,
                                  Long size,
                                  File target,
                                  int htl,
                                  boolean doRedirect,
                                  boolean fastDownload,
                                  boolean createTempFile,
                                  FrostDownloadItem dlItem)
    {
        return FcpRequest.getFile(key, size, target, htl, doRedirect, fastDownload, createTempFile, dlItem);
    }
    
    /**
     * Inserts a file into freenet.
     * The boardfilename is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * This method wraps the calls without the uploadItem.
     */
    public static String[] putFile(
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
    public static String[] putFile(
            String uri,
            File file,
            byte[] metadata,
            int htl,
            boolean doRedirect,
            boolean removeLocalKey,
            FrostUploadItem ulItem)
    {
        return FcpInsert.putFile(uri, file, metadata, htl, doRedirect, removeLocalKey, ulItem);
    }
    
    public static String generateCHK(File file) throws Throwable {

        String chkkey;
        if (file.length() <= FcpInsert.smallestChunk) {
            logger.info("File too short, doesn't need encoding.");
            // generate only CHK
            chkkey = FecTools.generateCHK(file);
        } else {
            FecSplitfile splitfile = new FecSplitfile(file);
            boolean alreadyEncoded = splitfile.uploadInit();
            if (!alreadyEncoded) {
                splitfile.encode();
            }
            // yes, this destroys any upload progress, but we come only here if
            // chkKey == null, so the file should'nt be uploaded until now
            splitfile.createRedirectFile(false);
            // gen normal redirect file for CHK generation

            chkkey = FecTools.generateCHK(
                    splitfile.getRedirectFile(),
                    splitfile.getRedirectFile().length());
        }
        return chkkey;
    }
    
    public static String[] getNodeInfo() throws IOException, ConnectException {

        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }
        return connection.getNodeInfo();
    }
    
    public static BoardKeyPair generateBoardKeyPair() throws IOException, ConnectException {
        
        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }

        String[] keyPair = connection.getKeyPair();
        String privKey = keyPair[0];
        String pubKey = keyPair[1];
        return new BoardKeyPair(pubKey, privKey);
    }
}
