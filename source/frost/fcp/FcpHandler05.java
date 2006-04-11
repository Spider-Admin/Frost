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

import frost.fcp.fcp05.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

public class FcpHandler05 extends FcpHandler {

    public void initialize(List nodes) {
        FcpFactory.init(nodes); // init the factory with configured nodes
    }

    public List getNodes() {
        return FcpFactory.getNodes();
    }
 
    public FcpResults getFile(String key,
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

    public String[] putFile(
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
    
    public String generateCHK(File file) throws Throwable {

        String chkkey;
        if (file.length() <= FcpInsert.smallestChunk) {
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

    public String[] getNodeInfo() throws IOException, ConnectException {

        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }
        return connection.getNodeInfo();
    }
    
    public BoardKeyPair generateBoardKeyPair() throws IOException, ConnectException {
        
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
