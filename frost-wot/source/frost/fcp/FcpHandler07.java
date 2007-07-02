/*
  FcpHandler07.java / Frost
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

import frost.fcp.fcp07.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

public class FcpHandler07 extends FcpHandler {

    public void initialize(List<String> nodes) {
        FcpFactory.init(nodes); // init the factory with configured nodes
    }

    public List<NodeAddress> getNodes() {
        return FcpFactory.getNodes();
    }
    
    public boolean initializePersistence() {
        if( getNodes().isEmpty() ) {
            return false;
        }
        NodeAddress na = getNodes().get(0);
        try {
            FcpPersistentConnection.initialize(na);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
 
    public FcpResultGet getFile(
            int type,
            String key,
            Long size,
            File target,
            boolean doRedirect,
            boolean fastDownload,
            int maxSize,
            boolean createTempFile,
            FrostDownloadItem dlItem)
    {
        // unused by 07: htl, doRedirect, fastDownload,
//        return FcpRequest.getFile(type, key, size, target, createTempFile, dlItem);
        int cnt = count++;
        long l = System.currentTimeMillis();
        System.out.println("GET_START("+cnt+"):"+key);
        FcpResultGet r = FcpRequest.getFile(type, key, size, target, maxSize, createTempFile, dlItem);
        System.out.println("GET_END("+cnt+"):"+key+", duration="+(System.currentTimeMillis()-l));
        return r;
    }

    int count = 0;

    public FcpResultPut putFile(
            int type,
            String uri,
            File file,
            byte[] metadata,
            boolean doRedirect,
            boolean removeLocalKey,
            boolean doMime,
            FrostUploadItem ulItem)
    {
        // unused by 07:  metadata, htl, doRedirect, removeLocalKey,
        int cnt = count++;
        long l = System.currentTimeMillis();
        System.out.println("PUT_START("+cnt+"):"+uri);
        FcpResultPut result = FcpInsert.putFile(type, uri, file, doMime, ulItem);
        System.out.println("PUT_END("+cnt+"):"+uri+", duration="+(System.currentTimeMillis()-l));
        
        if( result == null ) {
            return FcpResultPut.ERROR_RESULT;
        } else {
            return result;
        }
    }
    
    public String generateCHK(File file) throws IOException, ConnectException {

        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }
        String chkkey = connection.generateCHK(file);
        return chkkey;
    }

    public List<String> getNodeInfo() throws IOException, ConnectException {

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
        if( keyPair == null ) {
            return null;
        }
        String privKey = keyPair[0];
        String pubKey = keyPair[1];
        return new BoardKeyPair(pubKey, privKey);
    }
}
