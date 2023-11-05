/*
  MessageTransferHandler.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp07.messagetransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.fcp.FcpHandler;
import frost.fcp.FcpResultGet;
import frost.fcp.FcpResultPut;
import frost.fcp.NodeAddress;
import frost.fcp.fcp07.FcpListenThreadConnection;
import frost.fcp.fcp07.FcpMultiRequestConnectionFileTransferTools;
import frost.fcp.fcp07.NodeMessage;
import frost.fcp.fcp07.NodeMessageListener;

public class MessageTransferHandler implements NodeMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(MessageTransferHandler.class);

    private final FcpMultiRequestConnectionFileTransferTools fcpTools;

    private final HashMap<String,MessageTransferTask> taskMap = new HashMap<String,MessageTransferTask>();

    private boolean isConnected = true; // guaranteed to connect during construction

    public MessageTransferHandler() throws Throwable {

        if (FcpHandler.inst().getFreenetNode() == null) {
            throw new Exception("No freenet node defined");
        }
        final NodeAddress na = FcpHandler.inst().getFreenetNode();
        this.fcpTools = new FcpMultiRequestConnectionFileTransferTools(FcpListenThreadConnection.createInstance(na));
    }

    public void start() {
        fcpTools.getFcpPersistentConnection().addNodeMessageListener(this);
    }

    public synchronized void enqueueTask(final MessageTransferTask task) {

        if( !isConnected ) {
            logger.error("Rejecting new task, not connected!");
            task.setFailed();
            task.setFinished();
            return;
        }

        taskMap.put(task.getIdentifier(), task);

        // send task to socket
        if( task.isModeDownload() ) {
            fcpTools.startDirectGet(
                    task.getIdentifier(),
                    task.getKey(),
                    task.getPriority(),
                    task.getMaxSize(),
                    task.getMaxRetries());
        } else {
            fcpTools.startDirectPut(
                    task.getIdentifier(),
                    task.getKey(),
                    task.getPriority(),
                    task.getFile());
        }
    }

    protected synchronized void setTaskFinished(final MessageTransferTask task) {
        taskMap.remove(task.getIdentifier());
        task.setFinished();
    }

////////////////////////////////////////////////////////////////////////////////////////////////
//  NodeMessageListener interface //////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void connected() {
        // allow new tasks
        isConnected = true;
        logger.info("now connected");
    }

    public synchronized void disconnected() {

        isConnected = false;
        int taskCount = 0;
        synchronized(taskMap) {
            // notify all pending tasks that transfer failed
            for( final MessageTransferTask task : taskMap.values() ) {
                task.setFailed();
                task.setFinished();
                taskCount++;
            }
            taskMap.clear();
        }
        logger.info("disconnected, set {} tasks failed", taskCount);
    }

    public void handleNodeMessage(final NodeMessage nm) {
        // handle a NodeMessage without identifier
    }

    public void handleNodeMessage(final String id, final NodeMessage nm) {
    	logger.debug(">>>RCV>>>>");
    	logger.debug("MSG = {}", nm);
    	logger.debug("<<<<<<<<<<");

        final MessageTransferTask task = taskMap.get(id);
        if( task == null ) {
            logger.error("No task in list for identifier: {}", id);
            return;
        }

        if( nm.isMessageName("AllData") ) {
            onAllData(task, nm); // get successful
        } else if( nm.isMessageName("GetFailed") ) {
            onGetFailed(task, nm);
        } else if( nm.isMessageName("DataFound") ) {
            // ignore
        } else if( nm.isMessageName("ExpectedMIME") ) {
            // ignore

        } else if( nm.isMessageName("PutSuccessful") ) {
            onPutSuccessful(task, nm);
        } else if( nm.isMessageName("PutFailed") ) {
            onPutFailed(task, nm);
        } else if( nm.isMessageName("URIGenerated") ) {
            // ignore
        } else if( nm.isMessageName("ExpectedHashes") ) {
            // ignore

        } else if( nm.isMessageName("ProtocolError") ) {
            handleError(task, nm);
        } else if( nm.isMessageName("IdentifierCollision") ) {
            handleError(task, nm);
        } else if( nm.isMessageName("UnknownNodeIdentifier") ) {
            handleError(task, nm);
        } else if( nm.isMessageName("UnknownPeerNoteType") ) {
            handleError(task, nm);
        } else {
            // unhandled msg
            logger.warn("Unhandled msg: {}", nm);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////
//  handleNodeMessage methods //////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onAllData(final MessageTransferTask task, final NodeMessage nm) {
        if( nm.getMessageEnd() == null || !nm.getMessageEnd().equals("Data") ) {
            logger.error("NodeMessage has invalid end marker: {}", nm.getMessageEnd());
            return;
        }
        // data follow, first get datalength
        final long dataLength = nm.getLongValue("DataLength");
        long bytesWritten = 0;

        try {
            final BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(task.getFile()));
            final byte[] b = new byte[4096];
            long bytesLeft = dataLength;
            int count;
            final BufferedInputStream fcpIn = fcpTools.getFcpPersistentConnection().getFcpSocketIn();
            while( bytesLeft > 0 ) {
                count = fcpIn.read(b, 0, ((bytesLeft > b.length)?b.length:(int)bytesLeft));
                if( count < 0 ) {
                    break;
                } else {
                    bytesLeft -= count;
                }
                fileOut.write(b, 0, count);
                bytesWritten += count;
            }
            fileOut.close();
        } catch (final Throwable e) {
            logger.error("Catched exception", e);
        }

        logger.debug("*GET** Wrote {} of {} bytes to file.", bytesWritten, dataLength);
        final FcpResultGet result;
        if( bytesWritten == dataLength ) {
            // success
            result = new FcpResultGet(true);
        } else {
            result = new FcpResultGet(false);
        }
        task.setFcpResultGet(result);
        setTaskFinished(task);
    }

    protected void onGetFailed(final MessageTransferTask task, final NodeMessage nm) {
        final int returnCode = nm.getIntValue("Code");
        final String codeDescription = nm.getStringValue("CodeDescription");
        final boolean isFatal = nm.getBoolValue("Fatal");
        final String redirectURI = nm.getStringValue("RedirectURI");
        final FcpResultGet result = new FcpResultGet(false, returnCode, codeDescription, isFatal, redirectURI);
        task.setFcpResultGet(result);
        setTaskFinished(task);
    }

    protected void onPutSuccessful(final MessageTransferTask task, final NodeMessage nm) {

        String chkKey = nm.getStringValue("URI");
        // check if the returned text contains the computed CHK key
        final int pos = chkKey.indexOf("CHK@");
        if( pos > -1 ) {
            chkKey = chkKey.substring(pos).trim();
        }
        task.setFcpResultPut(new FcpResultPut(FcpResultPut.Success, chkKey));
        setTaskFinished(task);
    }

    protected void onPutFailed(final MessageTransferTask task, final NodeMessage nm) {
        final int returnCode = nm.getIntValue("Code");
        final String codeDescription = nm.getStringValue("CodeDescription");
        final boolean isFatal = nm.getBoolValue("Fatal");
        final FcpResultPut result;
        if( returnCode == 9 ) {
            result = new FcpResultPut(FcpResultPut.KeyCollision, returnCode, codeDescription, isFatal);
        } else if( returnCode == 5 ) {
            result = new FcpResultPut(FcpResultPut.Retry, returnCode, codeDescription, isFatal);
        } else {
            result = new FcpResultPut(FcpResultPut.Error, returnCode, codeDescription, isFatal);
        }
        task.setFcpResultPut(result);
        setTaskFinished(task);
    }

    protected void handleError(final MessageTransferTask task, final NodeMessage nm) {

        final int returnCode = nm.getIntValue("Code");
        final String codeDescription = nm.getStringValue("CodeDescription");
        final boolean isFatal = nm.getBoolValue("Fatal");
        if( task.isModeDownload() ) {
            final FcpResultGet result = new FcpResultGet(false, returnCode, codeDescription, isFatal, null);
            task.setFcpResultGet(result);
        } else {
            final FcpResultPut result = new FcpResultPut(FcpResultPut.Error, returnCode, codeDescription, isFatal);
            task.setFcpResultPut(result);
        }
        setTaskFinished(task);
    }
}
