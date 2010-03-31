/*
  FcpFactory.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import frost.util.*;

public class FcpFactory {
    private static final Logger logger = Logger.getLogger(FcpFactory.class.getName());

    private static NodeAddress freenetNode = null;

    /**
     * This method creates an instance of FcpConnection and handles errors.
     * Returns either the connection, or null on any error.
     */
    public static FcpConnection getFcpConnectionInstance() throws ConnectException {

        FcpConnection connection = null;

        final int maxTries = 3;
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
    public static NodeAddress getFreenetNode() {
        return freenetNode;
    }

    /**
     * Process provided List of string (host:port or host) and create InetAddress objects for each.
     */
    public static void init(final String node) throws Exception {
        final String nodeName = node;
        final NodeAddress na;
        if( nodeName.indexOf(":") < 0 ) {
            InetAddress ia = null;
            ia = InetAddress.getByName(nodeName);
            na = new NodeAddress(ia, 9481, ia.getHostName(), ia.getHostAddress());
        } else {
            final String[] splitNodeName = nodeName.split(":");
            final InetAddress ia = InetAddress.getByName(splitNodeName[0]);
            final int port = Integer.parseInt(splitNodeName[1]);
            na = new NodeAddress(ia, port, ia.getHostName(), ia.getHostAddress());
        }
        freenetNode = na;
    }

    protected static synchronized FcpConnection getConnection()  throws IOException, Error {

        FcpConnection con = null;

        final NodeAddress selectedNode = freenetNode;

        logger.info("Using node "+selectedNode.getHost().getHostAddress()+" port "+selectedNode.getPort());
        try {
            con = new FcpConnection(selectedNode);
        } catch (final IOException e) {
            throw e;
        }
        return con;
    }
}
