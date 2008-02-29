/*
  FcpHandler05.java / Frost
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

import frost.*;
import frost.fcp.fcp05.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

public class FcpHandler05 extends FcpHandler {

    @Override
    public void initialize(final List<String> nodes) {
        FcpFactory.init(nodes); // init the factory with configured nodes
    }

    @Override
    public List<NodeAddress> getNodes() {
        return FcpFactory.getNodes();
    }

    public FcpResultGet getFile(
            final int type,
            final String key,
            final Long size,
            final File target,
            final boolean doRedirect,
            final boolean fastDownload,
            final int maxSize,    // not used by 0.5
            final int maxRetries, // not used by 0.5
            final boolean createTempFile,
            final FrostDownloadItem dlItem)
    {
        final int htl = getDownloadHtlForType(type);
        return FcpRequest.getFile(key, size, target, htl, doRedirect, fastDownload, createTempFile, dlItem);
    }

    private int getDownloadHtlForType(final int type) {
        if( type == FcpHandler.TYPE_MESSAGE ) {
            return Core.frostSettings.getIntValue(SettingsClass.MESSAGE_DOWNLOAD_HTL);
        } else if( type == FcpHandler.TYPE_FILE ) {
            return 25;
        } else {
            return 21;
        }
    }

    private int getUploadHtlForType(final int type) {
        if( type == FcpHandler.TYPE_MESSAGE ) {
            return Core.frostSettings.getIntValue(SettingsClass.MESSAGE_UPLOAD_HTL);
        } else if( type == FcpHandler.TYPE_FILE ) {
            return Core.frostSettings.getIntValue(SettingsClass.UPLOAD_FILE_HTL);
        } else {
            return 21;
        }
    }

    @Override
    public FcpResultPut putFile(
            final int type,
            final String uri,
            final File file,
            final byte[] metadata,
            final boolean doRedirect,
            final boolean removeLocalKey,
            final boolean doMime,
            final FrostUploadItem ulItem)
    {
        // doMime is ignored on 0.5
        final int htl = getUploadHtlForType(type);
        final FcpResultPut result = FcpInsert.putFile(uri, file, metadata, htl, doRedirect, removeLocalKey, ulItem);
        if( result == null ) {
            return FcpResultPut.ERROR_RESULT;
        } else {
            return result;
        }
    }

    @Override
    public String generateCHK(final File file) throws Throwable {

        String chkkey;
        if (file.length() <= FcpInsert.smallestChunk) {
            // generate only CHK
            chkkey = FecTools.generateCHK(file);
        } else {
            final FecSplitfile splitfile = new FecSplitfile(file);
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

    @Override
    public List<String> getNodeInfo() throws IOException, ConnectException {

        final FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }
        return connection.getNodeInfo();
    }

    @Override
    public BoardKeyPair generateBoardKeyPair() throws IOException, ConnectException {

        final FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }

        final String[] keyPair = connection.getKeyPair();
        final String privKey = keyPair[0];
        final String pubKey = keyPair[1];
        return new BoardKeyPair(pubKey, privKey);
    }
}
