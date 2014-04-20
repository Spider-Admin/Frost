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
import java.util.logging.*;

import frost.*;
import frost.fcp.fcp07.*;
import frost.fcp.fcp07.messagetransfer.*;
import frost.fileTransfer.FreenetPriority;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.util.Logging;

public class FcpHandler07 extends FcpHandler {

    private static final Logger logger = Logger.getLogger(FcpHandler07.class.getName());

    private MessageTransferHandler msgTransferConnection = null;

    @Override
    public void initialize(final String node) throws Exception {
        FcpFactory.init(node); // init the factory with configured nodes
    }

    /**
     * Invoked when the node is online.
     */
    @Override
    public void goneOnline() {
        if( Core.frostSettings.getBoolValue(SettingsClass.FCP2_USE_ONE_CONNECTION_FOR_MESSAGES) ) {
            try {
                msgTransferConnection = new MessageTransferHandler();
                msgTransferConnection.start();
            } catch (final Throwable e) {
                logger.log(Level.SEVERE, "Initialization of MessageTransferConnection failed", e);
            }
        }
    }

    @Override
    public NodeAddress getFreenetNode() {
        return FcpFactory.getFreenetNode();
    }

    @Override
    public FcpResultGet getFile(
            final int type,
            String key,
            final Long size,
            final File targetFile,
            final int maxSize,
            final int maxRetries,
            final boolean createTempFile,
            final FrostDownloadItem dlItem)
    {
        // unused by 07: htl, doRedirect, fastDownload,
        key = FcpConnection.stripSlashes(key);
        final boolean doLogging = Logging.inst().doLogFcp2Messages();
        final int cnt = count++;
        final long l = System.currentTimeMillis();
        final FcpResultGet result;
        if( type == FcpHandler.TYPE_MESSAGE && msgTransferConnection != null ) {
            // use the shared socket
            if (doLogging) {
                System.out.println("GET_START(S)("+cnt+"):"+key);
            }
            final String id = "get-" + FcpSocket.getNextFcpId();
            final FreenetPriority prio = FreenetPriority.getPriority(Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE_DOWNLOAD));
            final MessageTransferTask task = new MessageTransferTask(id, key, targetFile, prio, maxSize, maxRetries);

            // enqueue task
            msgTransferConnection.enqueueTask(task);
            // wait for task to finish
            task.waitForFinished();

            result = task.getFcpResultGet();

            if (doLogging) {
                System.out.println("GET_END(S)("+cnt+"):"+key+", duration="+(System.currentTimeMillis()-l));
            }
        } else {
            // use a new socket
            if (doLogging) {
                System.out.println("GET_START(N)("+cnt+"):"+key);
            }
            result = FcpRequest.getFile(type, key, size, targetFile, maxSize, maxRetries, createTempFile, dlItem);
            if (doLogging) {
                System.out.println("GET_END(N)("+cnt+"):"+key+", duration="+(System.currentTimeMillis()-l));
            }
        }
        return result;
    }

    int count = 0;

    @Override
    public FcpResultPut putFile(
            final int type,
            String key,
            final File sourceFile,
            final boolean doMime,
            final FrostUploadItem ulItem)
    {
        key = FcpConnection.stripSlashes(key);
        final boolean doLogging = Logging.inst().doLogFcp2Messages();
        final int cnt = count++;
        final long l = System.currentTimeMillis();
        final FcpResultPut result;
        if( type == FcpHandler.TYPE_MESSAGE && msgTransferConnection != null ) {
            // use the shared socket
            if (doLogging) {
                System.out.println("PUT_START(S)("+cnt+"):"+key);
            }
            final String id = "get-" + FcpSocket.getNextFcpId();
            final FreenetPriority prio = FreenetPriority.getPriority(Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE_UPLOAD));
            final MessageTransferTask task = new MessageTransferTask(id, key, sourceFile, prio);

            // enqueue task
            msgTransferConnection.enqueueTask(task);
            // wait for task to finish
            task.waitForFinished();

            result = task.getFcpResultPut();

            if (doLogging) {
                System.out.println("PUT_END(S)("+cnt+"):"+key+", duration="+(System.currentTimeMillis()-l));
            }
        } else {
            if (doLogging) {
                System.out.println("PUT_START(N)("+cnt+"):"+key);
            }
            result = FcpInsert.putFile(type, key, sourceFile, doMime, ulItem);
            if (doLogging) {
                System.out.println("PUT_END(N)("+cnt+"):"+key+", duration="+(System.currentTimeMillis()-l));
            }
        }

        if( result == null ) {
            return FcpResultPut.ERROR_RESULT;
        } else {
            return result;
        }
    }

    @Override
    public String generateCHK(final File file) throws IOException, ConnectException {

        final FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }
        final String chkkey = connection.generateCHK(file);
        connection.close();
        return chkkey;
    }

    @Override
    public BoardKeyPair generateBoardKeyPair() throws IOException, ConnectException {

        final FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if (connection == null) {
            return null;
        }

        final String[] keyPair = connection.getKeyPair();
        connection.close();
        if( keyPair == null ) {
            return null;
        }
        final String privKey = keyPair[0];
        final String pubKey = keyPair[1];
        return new BoardKeyPair(pubKey, privKey);
    }
}
