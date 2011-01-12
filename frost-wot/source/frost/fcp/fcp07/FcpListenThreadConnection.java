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
import java.util.logging.*;

import javax.swing.event.*;

import frost.fcp.*;
import frost.util.*;

public class FcpListenThreadConnection extends AbstractBasicConnection {

    private static final Logger logger = Logger.getLogger(FcpListenThreadConnection.class.getName());

    private ReceiveThread receiveThread;

    private final EventListenerList listenerList = new EventListenerList();

    private final Set<String> checkedDirectories = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * Create a connection to a host using FCP.
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    protected FcpListenThreadConnection(final NodeAddress na) throws UnknownHostException, IOException {
        super(na);

        notifyConnected();

        receiveThread = new ReceiveThread(fcpSocket.getFcpIn());
        receiveThread.start();
    }

    public static FcpListenThreadConnection createInstance(final NodeAddress na) throws UnknownHostException, IOException {
        return new FcpListenThreadConnection(na);
    }

    public BufferedInputStream getFcpSocketIn() {
        return fcpSocket.getFcpIn();
    }
    
    public Set<String> getCheckedDirectories() {
        return checkedDirectories;
    }

    protected void reconnect() {
        // we are disconnected
        notifyDisconnected();

        int count = 0;
        while(true) {
            logger.severe("reconnect try no. "+count);
            try {
                fcpSocket = new FcpSocket(nodeAddress, true);
                break;
            } catch(final Throwable t) {
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

    public NodeAddress getNodeAddress() {
        return fcpSocket.getNodeAddress();
    }

    public void addNodeMessageListener(final NodeMessageListener l) {
        listenerList.add(NodeMessageListener.class, l);
    }

    public void removeNodeMessageListener(final NodeMessageListener  l) {
        listenerList.remove(NodeMessageListener.class, l);
    }

    protected void handleNodeMessage(final NodeMessage nodeMsg) {
        final String id = nodeMsg.getStringValue("Identifier");
        // Guaranteed to return a non-null array
        final Object[] listeners = listenerList.getListenerList();
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
        final Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == NodeMessageListener.class) {
                ((NodeMessageListener)listeners[i+1]).connected();
            }
        }
    }

    protected void notifyDisconnected() {
        // Guaranteed to return a non-null array
        final Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == NodeMessageListener.class) {
                ((NodeMessageListener)listeners[i+1]).disconnected();
            }
        }
    }

    private class ReceiveThread extends Thread {

        private final BufferedInputStream fcpInp;

        public ReceiveThread(final BufferedInputStream newFcpInp) {
            super();
            this.fcpInp = newFcpInp;
        }

        @Override
        public void run() {
            while(true) {
                final NodeMessage nodeMsg = NodeMessage.readMessage(fcpInp);
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
