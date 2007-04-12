/*
  FcpSocket.java / Frost
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
import java.util.logging.*;

import frost.fcp.*;

public class FcpSocket {
    
    private static final Logger logger = Logger.getLogger(FcpSocket.class.getName());

    // This is the timeout set in Socket.setSoTimeout().
    // The value was 900000 (15 minutes), but I often saw INSERT errors caused by a timeout in the read socket part;
    //   this sometimes leaded to double inserted messages.
    // Using infinite (0) is'nt a good idea, because due to freenet bugs it happened in the past that
    //   the socket blocked forever.
    // We now use with 60 minutes to be sure. mxbee (fuqid developer) told that he would maybe use 90 minutes!
    private final static int TIMEOUT = 60 * 60 * 1000;

    private NodeAddress nodeAddress;
    
    private Socket fcpSock;
    private BufferedInputStream fcpIn;
    private PrintStream fcpOut;
    
    private boolean useDDA;

    private static long staticFcpConnectionId = 0;
    
    public static synchronized String getNextFcpId() {
        StringBuilder sb = new StringBuilder().append("fcps-").append(System.currentTimeMillis()).append(staticFcpConnectionId++);
        return sb.toString();
    }

    /**
     * Create a connection to a host using FCP
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    public FcpSocket(NodeAddress na) throws UnknownHostException, IOException {
        nodeAddress = na;
        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpSock.setKeepAlive(true);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream(), false, "UTF-8");

        if( na.isDirectDiskAccessTested ) {
            useDDA = na.isDirectDiskAccessPossible;
        } else {
            useDDA = false;
        }

        doHandshake();
    }

    /**
     * Factory method to get a socket without to catch an Exception.
     */
    public static FcpSocket create(NodeAddress na) {
        try {
            FcpSocket newSocket = new FcpSocket(na);
            return newSocket;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception catched", t);
            return null;
        }
    }
    
    public NodeAddress getNodeAddress() {
        return nodeAddress;
    }
    
    public boolean isDDA() {
        return useDDA; 
    }
    
    public BufferedInputStream getFcpIn() {
        return fcpIn;
    }
    
    public PrintStream getFcpOut() {
        return fcpOut;
    }
    
    public Socket getFcpSock() {
        return fcpSock;
    }
    
    public void close() {
        if( fcpIn != null ) {
            try {
                fcpIn.close();
            } catch (Throwable e) {
            }
            fcpIn = null;
        }
        if( fcpOut != null ) {
            try {
                fcpOut.close();
            } catch (Throwable e) {
            }
            fcpOut = null;
        }
        if( fcpSock != null ) {
            try {
                fcpSock.close();
            } catch (Throwable e) {
            }
            fcpSock = null;
        }
    }

    /**
     * Performs a handshake using this FcpConnection
     */
    public void doHandshake() throws IOException, ConnectException {
        fcpOut.println("ClientHello");
        fcpOut.println("Name=hello-" + getNextFcpId());
        fcpOut.println("ExpectedVersion=2.0");
        fcpOut.println("EndMessage");
        fcpOut.flush();

        // receive and process node messages
        boolean isSuccess = false;
        while(true) {
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
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
}
