/*
  FcpPersistentConnection.java / Frost
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
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import frost.fcp.*;

public class FcpPersistentConnection {

//    private static Logger logger = Logger.getLogger(FcpPersistentConnection.class.getName());
    
    private static FcpPersistentConnection instance = null;
    
    private FcpSocket fcpSocket = null;

    private ReentrantLock writeSocketLock = null;
    private Semaphore waitForEndOfReceiveLock = null;
    
    private ReceiveThread receiveThread = null;

    /**
     * Create a connection to a host using FCP.
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    protected FcpPersistentConnection(NodeAddress na) throws UnknownHostException, IOException {
        
        fcpSocket = new FcpSocket(na);
        
        writeSocketLock = new ReentrantLock(true);
        waitForEndOfReceiveLock = new Semaphore(1, true);
        
        receiveThread = new ReceiveThread(fcpSocket.getFcpIn());
        receiveThread.start();
    }
    
    public static FcpPersistentConnection getInstance() {
        return instance;
    }
    
    public static void initialize(NodeAddress na) throws UnknownHostException, IOException {
        instance = new FcpPersistentConnection(na);
    }
    
    public static boolean isInitialized() {
        return instance != null;
    }
    
    public boolean isDDA() {
        return fcpSocket.isDDA();
    }

    public NodeAddress getNodeAddress() {
        return fcpSocket.getNodeAddress();
    }

    /**
     * Writes a message to the socket. Ensures that only 1 thread writes at any time.
     * Returns the output as list of NodeMessages.
     * @param message     the message to send
     * @param endMessage  optional end message name - if null no endMessage is expected, if "" then one, else the exact endMessage
     * @param sendEndMsg  if true EndMessage is appended
     */
    public List<NodeMessage> sendMessage(List<String> message, String endMessage, boolean sendEndMsg) {

        writeSocketLock.lock();
        
        receiveThread.startNewNodeMessageList(endMessage);
        
        boolean acquired = false;
        try {
            for(Iterator<String> i=message.iterator(); i.hasNext(); ) {
                String msgLine = i.next();
                fcpSocket.getFcpOut().println(msgLine);
            }
            if( sendEndMsg ) {
                fcpSocket.getFcpOut().println("EndMessage");
            }
            fcpSocket.getFcpOut().flush();

            if( endMessage != null ) {
                // wait for receivethread to finish
                waitForEndOfReceiveLock.acquireUninterruptibly();
                acquired = true;
            }

            return receiveThread.getNodeMessageList();

        } finally {
            writeSocketLock.unlock();
            if( acquired ) {
                waitForEndOfReceiveLock.release();
            }
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
        
        private BufferedInputStream fcpInp;
        private List<NodeMessage> nodeMessages = null;
        private String expectedEndMessage = null;
        
        public ReceiveThread(BufferedInputStream fcpInp) {
            this.fcpInp = fcpInp;
        }
        
        public void run() {
            while(true) {
                NodeMessage nodeMsg = NodeMessage.readMessage(fcpInp);
                if( nodeMsg == null ) {
                    break; // socket closed
                }
                
                if( nodeMessages == null ) {
                    System.out.println("Unrequested NodeMessage dropped: "+nodeMsg.toString());
                    continue;
                }
                
                nodeMessages.add(nodeMsg);
//                System.out.println("Added: "+nodeMsg.toString());
                
                if( expectedEndMessage != null ) {
                    if( expectedEndMessage.length() == 0 || nodeMsg.isMessageName(expectedEndMessage) ) {
                        waitForEndOfReceiveLock.release();
                    }
                }
            }
            System.out.println("ReceiveThread ended!");
        }
        
        /**
         * If caller waits for an given endmessage, then lock until this message is received.
         * This will block the caller.
         */
        public void startNewNodeMessageList(String endMsg) {
            nodeMessages = new LinkedList<NodeMessage>();
            expectedEndMessage = endMsg;
            if( expectedEndMessage != null ) {
                waitForEndOfReceiveLock.acquireUninterruptibly();
            }
        }
        
        public List<NodeMessage> getNodeMessageList() {
            List<NodeMessage> retList = nodeMessages;
            nodeMessages = null;
            expectedEndMessage = null;
            return retList;
        }
    }
}
