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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.fcp.fcp07.*;
import frost.util.Logging;

public class MessageTransferHandler implements NodeMessageListener {
    
    private static final Logger logger = Logger.getLogger(MessageTransferHandler.class.getName());

    private final FcpMultiRequestConnectionTools fcpTools;
    
    private final HashMap<String,MessageTransferTask> taskMap = new HashMap<String,MessageTransferTask>(); 
    
    private boolean isConnected = true; // guaranteed to connect during construction
    
    public MessageTransferHandler() throws Throwable {
        
        if( FcpHandler.inst().getNodes().isEmpty() ) {
            throw new Exception("No freenet nodes defined");
        }
        NodeAddress na = FcpHandler.inst().getNodes().get(0);
        this.fcpTools = new FcpMultiRequestConnectionTools(FcpMultiRequestConnection.createInstance(na));
    }
    
    public void start() {
        fcpTools.getFcpPersistentConnection().addNodeMessageListener(this);
    }
    // key umwandeln in | !!!
    public synchronized void enqueueTask(MessageTransferTask task) {
        
        if( !isConnected ) {
            logger.severe("Rejecting new task, not connected!");
            task.setFailed();
            task.setFinished();
            return;
        }
        
        // send task to socket
        if( task.isModeDownload() ) {
            fcpTools.startDirectGet(task.getIdentifier(), task.getKey(), task.getPriority(), task.getMaxSize());
        } else {
            fcpTools.startDirectPut(task.getIdentifier(), task.getKey(), task.getPriority(), task.getFile());
        }
        
        taskMap.put(task.getIdentifier(), task);
    }
    
    protected synchronized void setTaskFinished(MessageTransferTask task) {
        taskMap.remove(task.getIdentifier());
        task.setFinished();
    }

////////////////////////////////////////////////////////////////////////////////////////////////    
//  NodeMessageListener interface //////////////////////////////////////////////////////////////    
////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void connected() {
        // allow new tasks
        isConnected = true;
    }

    public synchronized void disconnected() {

        isConnected = false;
        
        synchronized(taskMap) {
            // notify all pending tasks that transfer failed
            for( MessageTransferTask task : taskMap.values() ) {
                task.setFailed();
                task.setFinished();
            }
            taskMap.clear();
        }
    }

    public void handleNodeMessage(NodeMessage nm) {
        // handle a NodeMessage without identifier
    }
// FIXME: restart tasks in queue after reconnect! accept new tasks during disconnect (??????)
    public void handleNodeMessage(String id, NodeMessage nm) {
        if(Logging.inst().doLogFcp2Messages()) { 
            System.out.println(">>>RCV>>>>");
            System.out.println("MSG="+nm);
            System.out.println("<<<<<<<<<<");
        }
        
        MessageTransferTask task = taskMap.get(id);
        if( task == null ) {
            logger.severe("No task in list for identifier: "+id);
            return;
        }
        
        if( nm.isMessageName("AllData") ) {
            onAllData(task, nm); // get successful
        } else if( nm.isMessageName("GetFailed") ) {
            onGetFailed(task, nm);
        } else if( nm.isMessageName("DataFound") ) {
            // ignore

        } else if( nm.isMessageName("PutSuccessful") ) {
            onPutSuccessful(task, nm);
        } else if( nm.isMessageName("PutFailed") ) {
            onPutFailed(task, nm);
        } else if( nm.isMessageName("URIGenerated") ) {
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
            System.out.println("### INFO - Unhandled msg: "+nm);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////
//  handleNodeMessage methods //////////////////////////////////////////////////////////////////    
////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onAllData(MessageTransferTask task, NodeMessage nm) {
        if( nm.getMessageEnd() == null || !nm.getMessageEnd().equals("Data") ) {
            logger.severe("NodeMessage has invalid end marker: "+nm.getMessageEnd());
            return;
        }
        // data follow, first get datalength
        long dataLength = nm.getLongValue("DataLength");
        long bytesWritten = 0;

        try {
            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(task.getFile()));
            byte[] b = new byte[4096];
            long bytesLeft = dataLength;
            int count;
            BufferedInputStream fcpIn = fcpTools.getFcpPersistentConnection().getFcpSocket().getFcpIn(); 
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
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Catched exception", e);
        }
        
        if(Logging.inst().doLogFcp2Messages()) { 
            System.out.println("*GET** Wrote "+bytesWritten+" of "+dataLength+" bytes to file.");
        }
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
    protected void onGetFailed(MessageTransferTask task, NodeMessage nm) {
        final int returnCode = nm.getIntValue("Code");
        final String codeDescription = nm.getStringValue("CodeDescription");
        final boolean isFatal = nm.getBoolValue("Fatal");
        final String redirectURI = nm.getStringValue("RedirectURI");
        final FcpResultGet result = new FcpResultGet(false, returnCode, codeDescription, isFatal, redirectURI);
        task.setFcpResultGet(result);
        setTaskFinished(task);
    }
    
    protected void onPutSuccessful(MessageTransferTask task, NodeMessage nm) {
        
        String chkKey = nm.getStringValue("URI");
        // check if the returned text contains the computed CHK key
        int pos = chkKey.indexOf("CHK@"); 
        if( pos > -1 ) {
            chkKey = chkKey.substring(pos).trim();
        }
        task.setFcpResultPut(new FcpResultPut(FcpResultPut.Success, chkKey));
        setTaskFinished(task);
    }
    protected void onPutFailed(MessageTransferTask task, NodeMessage nm) {
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
    
    protected void handleError(MessageTransferTask task, NodeMessage nm) {

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
