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

import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

public abstract class FcpHandler {

    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_FILE    = 2;

    public static final int MAX_MESSAGE_SIZE_07 = (64 * 1024) + (16 * 1024); // 64kb + 16kb
    public static final int MAX_FILELIST_SIZE_07 = (512 * 1024) + (16 * 1024); // 512kb + 16kb

    private static FcpHandler instance = null;

    public static FcpHandler inst() {
        return instance;
    }

    public static void initializeFcp(final List<String> nodes) throws UnsupportedOperationException {
        instance = new FcpHandler07();
        instance.initialize(nodes);
        FreenetKeys.initializeFor07();
    }

    public abstract void initialize(List<String> nodes);

    public abstract List<NodeAddress> getNodes();

    /**
     * Invoked when the node is online.
     */
    public void goneOnline() {
    }

    /**
     * getFile retrieves a file from Freenet. It does detect if this file is a redirect, a splitfile or
     * just a simple file. It checks the size for the file and returns false if sizes do not match.
     * Size is ignored if it is NULL
     *
     * @param key The key to retrieve. All to Freenet known key formats are allowed (passed to node via FCP).
     * @param size Size of the file in bytes. Is ignored if not an integer value or -1 (splitfiles do not need this setting).
     * @param target Target path
     * @param doRedirect If true, getFile redirects if possible and downloads the file it was redirected to.
     * @return null on error, or FcpResults
     */
    public FcpResultGet getFile(
            final int type,
            final String key,
            final Long size,
            final File target,
            final boolean doRedirect)
    {
        // use temp file by default, only filedownload needs the target file to monitor download progress
        return getFile(type,key,size,target,-1, -1, true, null);
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
    public FcpResultGet getFile(
            final int type,
            final String key,
            final Long size,
            final File target,
            final int maxSize,
            final int maxRetries)
    {
        // use temp file by default, only filedownload needs the target file to monitor download progress
        return getFile(type, key,size,target,maxSize, maxRetries, true, null);
    }

    /**
     * getFile retrieves a file from Freenet. It does detect if this file is a redirect, a splitfile or
     * just a simple file. It checks the size for the file and returns false if sizes do not match.
     * Size is ignored if it is NULL
     * @param key The key to retrieve. All to Freenet known key formats are allowed (passed to node via FCP).
     * @param size Size of the file in bytes. Is ignored if not an integer value or -1 (splitfiles do not need this setting).
     * @param target Target path
     * @param createTempFile  true to download to a temp file and rename to target file after success.
     * @param dlItem   The DownloadItem for this download for progress updates, or null if there is none.
     *
     * @return null on error, or FcpResults
     */
    public abstract FcpResultGet getFile(
            int type,
            String key,
            Long size,
            File target,
            int maxSize,
            int maxRetries,
            boolean createTempFile,
            FrostDownloadItem dlItem);

    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public FcpResultPut putFile(
            final int type,
            final String uri,
            final File file,
            final boolean doMime)
    {
        return putFile(type,uri,file,doMime,null);
    }

    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public abstract FcpResultPut putFile(
            int type,
            String uri,
            File file,
            boolean doMime,
            FrostUploadItem ulItem);

    public abstract String generateCHK(File file) throws Throwable;

    public abstract List<String> getNodeInfo() throws IOException, ConnectException;

    public abstract BoardKeyPair generateBoardKeyPair() throws IOException, ConnectException;
}
