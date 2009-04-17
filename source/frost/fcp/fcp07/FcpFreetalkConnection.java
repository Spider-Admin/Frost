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

    public FcpFreetalkConnection(final NodeAddress na) throws UnknownHostException, IOException {
        super(na);

//        FIXME  add nodemessagelistener!
    }

    public synchronized List<String> listBoards() throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier=moohmooh");
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=ListBoards");

        sendMessage(msg, true);

        final List<String> boardNames = new ArrayList<String>();

        // receive and process node message
        while(true) {
            final NodeMessage nodeMsg = NodeMessage.readMessage(fcpSocket.getFcpIn());
            if (nodeMsg == null) {
                // FIXME: reconnect
                throw new Exception("No NodeMessage received!");
            }

            if (!nodeMsg.isMessageName("FCPPluginReply")) {
                throw new Exception("Unexpected NodeMessage received: "+nodeMsg.getMessageName());
            }

            if ("EndListBoards".equals(nodeMsg.getStringValue("Replies.Message"))) {
                System.out.println("<<<<< End of boards list.");
                break;
            }

            if (!"Board".equals(nodeMsg.getStringValue("Replies.Message"))) {
                throw new Exception("Unexpected NodeMessage received: "+nodeMsg.getStringValue("Replies.Message"));
            }

            System.out.println(">>> Board");
            System.out.println("  Name ............ = "+nodeMsg.getStringValue("Replies.Name"));
            System.out.println("  MessageCount .... = "+nodeMsg.getStringValue("Replies.MessageCount"));
            System.out.println("  FirstSeenDate ... = "+nodeMsg.getStringValue("Replies.FirstSeenDate"));
            System.out.println("  LatestMessageDate = "+nodeMsg.getStringValue("Replies.LatestMessageDate"));

            boardNames.add(nodeMsg.getStringValue("Replies.Name"));
        }
        return boardNames;
    }
}
