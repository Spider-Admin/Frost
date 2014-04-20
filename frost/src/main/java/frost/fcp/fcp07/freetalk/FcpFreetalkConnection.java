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
package frost.fcp.fcp07.freetalk;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import frost.Core;
import frost.fcp.NodeAddress;
import frost.fcp.fcp07.FcpListenThreadConnection;
import frost.fcp.fcp07.NodeMessage;
import frost.fcp.fcp07.NodeMessageListener;
import frost.messaging.freetalk.FreetalkFileAttachment;


public class FcpFreetalkConnection extends FcpListenThreadConnection {

    private static long fcpidentifierPart1 = Core.getCrypto().getSecureRandom().nextLong();
    private static long fcpidentifierPart2 = 0L;

    private static final Logger logger = Logger.getLogger(FcpFreetalkConnection.class.getName());

    private final NodeMessageHandler nodeMessageHandler;

    public static synchronized String getNextFcpidentifier() {
        return new StringBuilder()
            .append("FreetalkConnection-")
            .append(fcpidentifierPart1)
            .append("-")
            .append(fcpidentifierPart2++)
            .toString();
    }

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
        sendMessage(msg);
    }

    public void sendCommandListSubscribedBoards(final String id, final String ownId) throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=ListSubscribedBoards");
        msg.add("Param.OwnIdentityID="+ownId);
        sendMessage(msg);
    }

    public void sendCommandListMessages(
            final String id,
            final String boardname,
            final String ownId,
            final boolean withMessageContent)
    throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=ListMessages");
        msg.add("Param.BoardName="+boardname);
        msg.add("Param.OwnIdentityID="+ownId);
        msg.add("Param.SortByMessageDateAscending=true");
        msg.add("Param.IncludeMessageText="+withMessageContent);
        sendMessage(msg);
    }

    // FIXME: subscribe after create ?!
    public void sendCommandCreateBoard(
            final String id,
            final String boardname)
    throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=CreateBoard");
        msg.add("Param.BoardName="+boardname);
        sendMessage(msg);
    }

    public void sendCommandGetMessage(
            final String id,
            final String boardname,
            final int msgIndex,
            final String ownId,
            final boolean withMessageContent)
    throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=GetMessage");
        msg.add("Param.BoardName="+boardname);
        msg.add("Param.MessageIndex="+msgIndex);
        msg.add("Param.OwnIdentityID="+ownId);
        msg.add("Param.IncludeMessageText="+withMessageContent);
        sendMessage(msg);
    }

    public void sendCommandListOwnIdentities(final String id) throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=ListOwnIdentities");
        sendMessage(msg);
    }

    public void sendCommandPutMessage(
            final String id,
            final String parentId,
            final String ownIdentityUid,
            final String replyToBoard,
            final List<String> targetBoards,
            final String title,
            final String content,
            final List<FreetalkFileAttachment> attachments)
    throws Exception {

        final List<String> msg = new ArrayList<String>();
        msg.add("FCPPluginMessage");
        msg.add("Identifier="+id);
        msg.add("PluginName=plugins.Freetalk.Freetalk");
        msg.add("Param.Message=PutMessage");

        if (parentId != null) {
            msg.add("Param.ParentID="+parentId);
        }
        msg.add("Param.AuthorIdentityID="+ownIdentityUid);
        msg.add("Param.ReplyToBoard="+replyToBoard);

        String targetBoardsStr = "";
        for (final String s : targetBoards) {
            if (targetBoardsStr.length() > 0) {
                targetBoardsStr += ",";
            }
            targetBoardsStr += s;
        }
        msg.add("Param.TargetBoards="+targetBoardsStr);
        msg.add("Param.Title="+title);

        if (attachments != null && attachments.size() > 0) {
            msg.add("Param.FileAttachmentCount="+attachments.size());
            int x = 1;
            for (final FreetalkFileAttachment a : attachments) {
                msg.add("Param.FileAttachmentURI."+x+"="+a.getUri());
                msg.add("Param.FileAttachmentSize."+x+"="+a.getFileSize());
                x++;
            }
        }

        final byte[] contentBytes = content.getBytes("UTF-8");

        sendMessageAndData(msg, contentBytes);
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
            System.out.println("------------ FcpFreetalkConnection: nodeMessage w/o ID \"------------");
            System.out.println(nm.toString());
        }

        public void handleNodeMessage(final String id, final NodeMessage nm) {
            System.out.println("------------ FcpFreetalkConnection: nodeMessage w/ ID \"------------");
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
