/*
  FcpMultiRequestConnection.java / Frost
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
package frost.fcp.fcp07;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;

import javax.swing.event.*;

import frost.fcp.*;
import frost.util.*;

public class FcpMultiRequestConnection {

    private static final Logger logger = Logger.getLogger(FcpMultiRequestConnection.class.getName());

    private final NodeAddress nodeAddress;
    
    private FcpSocket fcpSocket = null;

    private final ReentrantLock writeSocketLock;
    
    private ReceiveThread receiveThread;
    
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Create a connection to a host using FCP.
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    protected FcpMultiRequestConnection(NodeAddress na) throws UnknownHostException, IOException {

        nodeAddress = na;
        
        fcpSocket = new FcpSocket(nodeAddress);

        notifyConnected();

        writeSocketLock = new ReentrantLock(true);
        
        receiveThread = new ReceiveThread(fcpSocket.getFcpIn());
        receiveThread.start();
    }
    
    public static FcpMultiRequestConnection createInstance(NodeAddress na) throws UnknownHostException, IOException {
        return new FcpMultiRequestConnection(na);
    }
    
    public FcpSocket getFcpSocket() {
        return fcpSocket;
    }
    
    protected void reconnect() {
        // we are disconnected
        notifyDisconnected();

        int count = 0;
        while(true) {
            logger.severe("reconnect try no. "+count);
            try {
                fcpSocket = new FcpSocket(nodeAddress);
                break;
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "reconnect failed, exception catched: "+t.getMessage());
            }
            logger.severe("waiting 30 seconds before next reconnect try");
            Mixed.wait(30000);
            count++;
        }
        logger.severe("reconnect was successful, restarting ReceiveThread now");

        notifyConnected();

        receiveThread = new ReceiveThread(fcpSocket.getFcpIn());
        receiveThread.start();
    }
    
    public boolean isDDA() {
        return fcpSocket.isDDA();
    }

    public NodeAddress getNodeAddress() {
        return fcpSocket.getNodeAddress();
    }
    
    public void addNodeMessageListener(NodeMessageListener l) {
        listenerList.add(NodeMessageListener.class, l);
    }

    public void NodeMessageListener(NodeMessageListener  l) {
        listenerList.remove(NodeMessageListener.class, l);
    }
    
    protected void handleNodeMessage(NodeMessage nodeMsg) {
        String id = nodeMsg.getStringValue("Identifier");
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == NodeMessageListener.class) {
                if( id != null ) {
                    ((NodeMessageListener)listeners[i+1]).handleNodeMessage(id, nodeMsg);
                } else {
                    ((NodeMessageListener)listeners[i+1]).handleNodeMessage(nodeMsg);
                }
            }
        }
    }

    protected void notifyConnected() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == NodeMessageListener.class) {
                ((NodeMessageListener)listeners[i+1]).connected();
            }
        }
    }

    protected void notifyDisconnected() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == NodeMessageListener.class) {
                ((NodeMessageListener)listeners[i+1]).disconnected();
            }
        }
    }

    /**
     * Writes a message to the socket. Ensures that only 1 thread writes at any time (writeSocketLock).
     * @param message     the message to send
     * @param sendEndMsg  if true EndMessage should be appended
     */
    public boolean sendMessage(List<String> message, boolean sendEndMsg) {

        writeSocketLock.lock();
        try {
            if(Logging.inst().doLogFcp2Messages()) { 
                System.out.println("### SEND >>>>>>>");
            }
            for(Iterator<String> i=message.iterator(); i.hasNext(); ) {
                String msgLine = i.next();
                fcpSocket.getFcpOut().println(msgLine);
                if(Logging.inst().doLogFcp2Messages()) { 
                    System.out.println(msgLine);
                }
            }
            if( sendEndMsg ) {
                fcpSocket.getFcpOut().println("EndMessage");
                if(Logging.inst().doLogFcp2Messages()) { 
                    System.out.println("*EndMessage*");
                }
            }
            boolean isError = fcpSocket.getFcpOut().checkError();
            if(Logging.inst().doLogFcp2Messages()) { 
                System.out.println("### SEND <<<<<<< isError="+isError);
            }
            return isError;
        } finally {
            writeSocketLock.unlock();
        }
    }

    public boolean sendMessageAndData(List<String> message, boolean sendEndMsg, File sourceFile) {
        writeSocketLock.lock();
        try {
            if(Logging.inst().doLogFcp2Messages()) { 
                System.out.println("### SEND_DATA >>>>>>>");
            }
            for(Iterator<String> i=message.iterator(); i.hasNext(); ) {
                String msgLine = i.next();
                fcpSocket.getFcpOut().println(msgLine);
                if(Logging.inst().doLogFcp2Messages()) { 
                    System.out.println(msgLine);
                }
            }
            if( sendEndMsg ) {
                fcpSocket.getFcpOut().println("Data");
            }
            
            fcpSocket.getFcpOut().flush();
            
            // send file
            final BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));
            while( true ) {
                final int d = fileInput.read();
                if( d < 0 ) {
                    break; // EOF
                }
                fcpSocket.getFcpRawOut().write(d);
            }
            fileInput.close();
            fcpSocket.getFcpRawOut().flush();
            
            if(Logging.inst().doLogFcp2Messages()) { 
                System.out.println("### SEND_DATA <<<<<<<");
            }
            return false; // no error
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Error sending file to socket", t);
            return true; // error
        } finally {
            writeSocketLock.unlock();
        }
    }
        
    public void closeConnection() {
        fcpSocket.close();
    }

    /**
     * Performs a handshake using this FcpConnection
     */
    public void doHandshake() throws IOException, ConnectException {
        fcpSocket.getFcpOut().println("ClientHello");
        fcpSocket.getFcpOut().println("Name=hello-" + FcpSocket.getNextFcpId());
        fcpSocket.getFcpOut().println("ExpectedVersion=2.0");
        fcpSocket.getFcpOut().println("EndMessage");
        fcpSocket.getFcpOut().flush();

        // receive and process node messages
        boolean isSuccess = false;
        while(true) {
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpSocket.getFcpIn());
            if( nodeMsg == null ) {
                break;
            }

            if( nodeMsg.isMessageName("NodeHello") ) {
                isSuccess = true;
                break;
            }
            // any other message means error here
            break;
        }
        
        if( !isSuccess ) {
            throw new ConnectException();
        }
    }
    
    private class ReceiveThread extends Thread {
        
        private final BufferedInputStream fcpInp;
        
        public ReceiveThread(BufferedInputStream newFcpInp) {
            super();
            this.fcpInp = newFcpInp;
        }

        public void run() {
            while(true) {
                NodeMessage nodeMsg = NodeMessage.readMessage(fcpInp);
                if( nodeMsg == null ) {
                    break; // socket closed
                } else {
                    // notify listeners
                    handleNodeMessage(nodeMsg);
                }
            }

            logger.severe("Socket closed, ReceiveThread ended, trying to reconnect");
            System.out.println("ReceiveThread ended, trying to reconnect");
            
            reconnect();
        }
    }
}
