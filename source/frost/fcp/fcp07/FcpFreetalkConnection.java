/*
  FcpMultiRequestConnectionTools.java / Frost
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

import frost.fcp.*;


public class FcpFreetalkConnection extends FcpListenThreadConnection {

    private static final Logger logger = Logger.getLogger(FcpFreetalkConnection.class.getName());

    private final NodeMessageHandler nodeMessageHandler;

    public FcpFreetalkConnection(final NodeAddress na) throws UnknownHostException, IOException {
        super(na);
        nodeMessageHandler = new NodeMessageHandler();
        addNodeMessageListener(nodeMessageHandler);
    }

    public interface FreetalkNodeMessageCallback {
        public void handleNodeMessage(final String id, final NodeMessage nodeMessage);
    }

    public boolean registerCallback(final String id, final FreetalkNodeMessageCallback cb) {
        return nodeMessageHandler.registerCallback(id, cb);
    }

    public void unregisterCallback(final String id) {
        nodeMessageHandler.unregisterCallback(id);
    }

    public void sendCommandListBoards(final String id) throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=ListBoards");
        sendMessage(msg, true);
    }

    /**
     * Handle and dispatch NodeMessages.
     */
    private class NodeMessageHandler implements NodeMessageListener {

        private final HashMap<String, FreetalkNodeMessageCallback> callbackById = new HashMap<String, FreetalkNodeMessageCallback>();

        public void connected() {
            System.out.println("FcpFreetalkConnection: connected");
        }

        public void disconnected() {
            System.out.println("FcpFreetalkConnection: disconnected");
        }

        public void handleNodeMessage(final NodeMessage nm) {
            System.out.println("FcpFreetalkConnection: nodeMessage w/o ID");
            System.out.println(nm.toString());
        }

        public void handleNodeMessage(final String id, final NodeMessage nm) {
            System.out.println("FcpFreetalkConnection: nodeMessage w/ ID");
            System.out.println(nm.toString());
            final FreetalkNodeMessageCallback cb = callbackById.get(id);
            if (cb == null) {
                logger.severe("No callback for ID registered");
            } else {
                cb.handleNodeMessage(id, nm);
            }
        }

        public synchronized boolean registerCallback(final String id, final FreetalkNodeMessageCallback cb) {
            if (callbackById.containsKey(id)) {
                return false;
            } else {
                callbackById.put(id, cb);
                return true;
            }
        }

        public synchronized void unregisterCallback(final String id) {
            callbackById.remove(id);
        }
    }
}
