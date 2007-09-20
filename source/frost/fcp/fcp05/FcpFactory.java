/*
  FcpFactory.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp05;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.util.*;

public class FcpFactory {

    private static final Logger logger = Logger.getLogger(FcpFactory.class.getName());

    private static List<NodeAddress> nodes = new ArrayList<NodeAddress>(); //list of available nodes
    private static Random random = new Random();

    /**
     * This method creates an instance of FcpConnection and handles errors.
     * Returns either the connection, or null on any error.
     */
    public static FcpConnection getFcpConnectionInstance() throws ConnectException {

        FcpConnection connection = null;

        int maxTries;
        if( getNodes().size() == 1 ) {
            // give our single FCP host 3 tries
            maxTries = 3;
        } else {
            // if we have more than one node, try each one at least twice
            maxTries = getNodes().size()*2;
        }

        int tries = 0;
        while (connection == null && tries < maxTries) {
            try {
                connection = getConnection();
            } catch (final UnknownHostException e) {
                logger.severe("FcpConnection.getFcpConnectionInstance: UnknownHostException " + e);
                break;
            } catch (final java.net.ConnectException e) {
                /*  IOException java.net.ConnectException: Connection refused: connect  */
                logger.warning(
                    "FcpConnection.getFcpConnectionInstance: java.net.ConnectException "
                        + e + " , this was try " + (tries + 1) + "/" + maxTries);
            } catch (final IOException e) {
                logger.warning(
                    "FcpConnection.getFcpConnectionInstance: IOException "
                        + e + " , this was try " + (tries + 1) + "/" + maxTries);
            } catch (final Throwable e) {
                logger.severe("FcpConnection.getFcpConnectionInstance: Throwable " + e);
                break;
            }
            tries++;
            Mixed.wait(tries * 1250);
        }
        if (connection == null) {
            logger.warning("ERROR: FcpConnection.getFcpConnectionInstance: Could not connect to node!");
            throw new ConnectException("Could not connect to FCP node.");
        }
        return connection;
    }

    /**
     * @return  Returns a list of available NodeAddress objects.
     */
    public static List<NodeAddress> getNodes() {
        return nodes;
    }

    /**
     * Returns a randomly selected node from the list.
     * @return NodeAddress of selected node.
     */
    protected static NodeAddress selectNode() {
        final int size = nodes.size();
        if(size == 0) {
            throw new Error("All connections to nodes failed. Check your network settings and restart Frost.");
        } else if( size == 1 ) {
            return nodes.get(0);
        } else {
            return nodes.get(random.nextInt(nodes.size()));
        }
    }

//    /**
//     * @param s the node to be removed
//     */
//    protected static void delegateRemove(NodeAddress s) {
//        nodes.remove(s);
//    }
//
    /**
     * Process provided List of string (host:port or host) and create InetAddress objects for each.
     */
    public static void init(final List<String> nodeList) {
        for( final Object element : nodeList ) {
            final String nodeName = (String)element;
            final NodeAddress na;
            if( nodeName.indexOf(":") < 0 ) {
                InetAddress ia = null;
                try {
                    ia = InetAddress.getByName(nodeName);
                    na = new NodeAddress(ia, 8481, ia.getHostName(), ia.getHostAddress());
                } catch(final Throwable t) {
                    logger.log(Level.SEVERE, "Unknown FCP host: "+nodeName, t);
                    continue;
                }
            } else {
                final String[] splitNodeName = nodeName.split(":");
                final InetAddress ia;
                try {
                    ia = InetAddress.getByName(splitNodeName[0]);
                } catch(final Throwable t) {
                    logger.log(Level.SEVERE, "Unknown FCP host: "+nodeName, t);
                    continue;
                }
                final int port;
                try {
                    port = Integer.parseInt(splitNodeName[1]);
                } catch(final Throwable t) {
                    logger.log(Level.SEVERE, "Unknown FCP port: "+nodeName, t);
                    continue;
                }
                na = new NodeAddress(ia, port, ia.getHostName(), ia.getHostAddress());
            }
            nodes.add(na);
        }
        logger.info("Frost will use " + nodes.size() + " Freenet nodes");
    }

    protected static synchronized FcpConnection getConnection()  throws IOException, Error {

        FcpConnection con = null;
        if (getNodes().size()==0) {
            throw new Error("No Freenet nodes available.  You need at least one.");
        }

        final NodeAddress selectedNode = selectNode();

        logger.info("Using node "+selectedNode.getHost().getHostAddress()+" port "+selectedNode.getPort());
        try {
            con = new FcpConnection(selectedNode);
        } catch (final IOException e) {
            // for now, remove on the first failure.
            // TODO: maybe we should give the node few chances?
            // also, should we remove it from the settings (i.e. forever)?
//            delegateRemove(selectedNode);
            throw e;
        }
        return con;
    }
}
