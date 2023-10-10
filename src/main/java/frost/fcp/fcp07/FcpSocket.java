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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import frost.Core;
import frost.fcp.NodeAddress;

public class FcpSocket {

    private static final Logger logger = Logger.getLogger(FcpSocket.class.getName());

    // This is the timeout set in Socket.setSoTimeout().
    private final static int TIMEOUT = 90 * 60 * 1000;

    private final NodeAddress nodeAddress;

    private Socket fcpSock;
    private BufferedInputStream fcpIn;
    private PrintStream fcpOut;
    private final BufferedOutputStream fcpRawOut;

    private final Set<String> checkedDirectories = Collections.synchronizedSet(new HashSet<String>());
    
    private static long fcpidentifierPart1 = Core.getCrypto().getSecureRandom().nextLong();
    private static long fcpidentifierPart2 = 0L;

    public enum DDAModes {
        WANT_DOWNLOAD,
        WANT_UPLOAD,
        WANT_DOWNLOAD_AND_UPLOAD
    };

    public static synchronized String getNextFcpId() {
        return new StringBuilder()
            .append("FcpSocket-")
            .append(fcpidentifierPart1)
            .append("-")
            .append(fcpidentifierPart2++)
            .toString();
    }

    /**
     * Create a connection to a host using FCP
     *
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    public FcpSocket(final NodeAddress na) throws UnknownHostException, IOException {
        this(na, false);
    }

    /**
     * Create a connection to a host using FCP
     *
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    public FcpSocket(final NodeAddress na, final boolean infiniteTimeout) throws UnknownHostException, IOException {
        nodeAddress = na;
        fcpSock = new Socket(nodeAddress.getHost(), nodeAddress.getPort());
        if (!infiniteTimeout) {
            fcpSock.setSoTimeout(TIMEOUT);
        }
        fcpSock.setKeepAlive(true);

        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpRawOut = new BufferedOutputStream(fcpSock.getOutputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream(), false, "UTF-8");

        doHandshake();
    }

    /**
     * Factory method to get a socket without to catch an Exception.
     */
    public static FcpSocket create(final NodeAddress na) {
        try {
            final FcpSocket newSocket = new FcpSocket(na);
            return newSocket;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception catched", t);
            return null;
        }
    }

    public Set<String> getCheckedDirectories() {
        return checkedDirectories;
    }

    public NodeAddress getNodeAddress() {
        return nodeAddress;
    }

    public BufferedInputStream getFcpIn() {
        return fcpIn;
    }

    public PrintStream getFcpOut() {
        return fcpOut;
    }

    public BufferedOutputStream getFcpRawOut() {
        return fcpRawOut;
    }

    public Socket getFcpSock() {
        return fcpSock;
    }

    public void close() {
        if( fcpIn != null ) {
            try {
                fcpIn.close();
            } catch (final Throwable e) {
            }
            fcpIn = null;
        }
        if( fcpOut != null ) {
            try {
                fcpOut.close();
            } catch (final Throwable e) {
            }
            fcpOut = null;
        }
        if( fcpSock != null ) {
            try {
                fcpSock.close();
            } catch (final Throwable e) {
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
            final NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
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
