/*
  ListMessagesCallback.java / Frost
  Copyright (C) 2009  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.freetalk.transfer;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.fcp07.*;
import frost.fcp.fcp07.freetalk.FcpFreetalkConnection.*;
import frost.messaging.freetalk.*;
import frost.messaging.freetalk.boards.*;

public class ListMessagesCallback implements FreetalkNodeMessageCallback {

    private static final Logger logger = Logger.getLogger(ListMessagesCallback.class.getName());

    private final MainFrame mainFrame;
    private final FreetalkBoard board;
    private final boolean isThreaded;

    private final List<FreetalkMessage> newMsgList = new ArrayList<FreetalkMessage>();

    public ListMessagesCallback(final MainFrame mf, final FreetalkBoard b, final boolean isThreaded) {
        mainFrame = mf;
        board = b;
        this.isThreaded = isThreaded;
    }

    public void handleNodeMessage(final String id, final NodeMessage nodeMsg) {

        if (!nodeMsg.isMessageName("FCPPluginReply")) {
            logger.severe("Unexpected NodeMessage received: "+nodeMsg.getMessageName());
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            mainFrame.deactivateGlassPane();
            return;
        }

        if ("EndListMessages".equals(nodeMsg.getStringValue("Replies.Message"))) {
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);

            // add to message panel
            if (isThreaded) {
                addMessageThreaded(newMsgList);
            } else {
                addMessageFlat(newMsgList);
            }

            mainFrame.getFreetalkMessageTab().getMessagePanel().getMessageTable().updateUI();
            mainFrame.deactivateGlassPane();
            return;
        }

        if (!"Message".equals(nodeMsg.getStringValue("Replies.Message"))) {
            logger.severe("Unexpected NodeMessage received: "+nodeMsg.getStringValue("Replies.Message"));
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            mainFrame.deactivateGlassPane();
            return;
        }

        final String msgId = nodeMsg.getStringValue("Replies.ID");
        final int msgIndex = new Integer(nodeMsg.getStringValue("Replies.MessageIndex")).intValue();
        final String title = nodeMsg.getStringValue("Replies.Title");
        final String author = nodeMsg.getStringValue("Replies.Author");
        final long dateMillis = new Long(nodeMsg.getStringValue("Replies.Date")).longValue();
        final long fetchDateMillis = new Long(nodeMsg.getStringValue("Replies.FetchDate")).longValue();
        final String parentMsgID = nodeMsg.getStringValue("Replies.ParentID");
        final String threadRootMsgID = nodeMsg.getStringValue("Replies.ThreadID");
        int attachmentCount = 0;
        if (nodeMsg.getStringValue("Replies.FileAttachmentCount") != null) {
            attachmentCount = Integer.parseInt(nodeMsg.getStringValue("Replies.FileAttachmentCount"));
        }
        List<FreetalkFileAttachment> fileAttachments = null;
        if (attachmentCount > 0) {
            fileAttachments = new ArrayList<FreetalkFileAttachment>();
            for(int x=0; x<attachmentCount; x++) {
                final String uriString = nodeMsg.getStringValue("FileAttachmentURI."+x);
                final String sizeString = nodeMsg.getStringValue("FileAttachmentSize."+x);

                final FreetalkFileAttachment att = new FreetalkFileAttachment(uriString, new Long(sizeString).longValue());
                fileAttachments.add(att);
            }
        }

        // maybe receive data
        String messageText = null;
        if (nodeMsg.isValueSet("DataLength")) {

            if (!"Data".equals(nodeMsg.getMessageEnd())) {
                logger.severe("Endmarker is not Data: "+nodeMsg.getStringValue("Replies.Message"));
            } else {
                final long i = Long.parseLong(nodeMsg.getStringValue("DataLength"));

                // receive TextLength bytes
                try {
                    final byte[] dataBytes = nodeMsg.receiveMessageData(i);
                    messageText = new String(dataBytes, "UTF-8");
                } catch(final IOException ex) {
                    logger.log(Level.SEVERE, "Error receiving message data", ex);
                }
            }
        }

        // create message object
        final FreetalkMessage ftMsg = new FreetalkMessage(
                board,
                msgId,
                msgIndex,
                title,
                author,
                dateMillis,
                fetchDateMillis,
                parentMsgID,
                threadRootMsgID,
                fileAttachments);

        ftMsg.setContent(messageText);

        newMsgList.add(ftMsg);
    }

    private void addMessageFlat(final List<FreetalkMessage> msgList) {

        for (final FreetalkMessage newMsg : msgList) {
            final FreetalkMessage rootNode = mainFrame.getFreetalkMessageTab().getMessagePanel().getMessageTable().getRootNode();
            rootNode.add(newMsg, false);
        }
    }

    private void addMessageThreaded(final List<FreetalkMessage> msgList) {

        final FreetalkMessage rootNode = mainFrame.getFreetalkMessageTab().getMessagePanel().getMessageTable().getRootNode();

        // add messages without parent to root
        for (final Iterator<FreetalkMessage> i = msgList.iterator(); i.hasNext(); ) {

            final FreetalkMessage newMsg = i.next();
            if (newMsg.getParentMsgID() == null) {
                i.remove();
                rootNode.add(newMsg, false);
            }
        }

        // add to parents
        boolean foundParent = false;
        do {
            foundParent = false;
            final Iterator<FreetalkMessage> freetalkMessageIterator = msgList.iterator();
            while( freetalkMessageIterator.hasNext()) {

                final FreetalkMessage newMsg = freetalkMessageIterator.next();
                final String parentMsgIdString = newMsg.getParentMsgID();
                // find parent
                final Enumeration<FreetalkMessage> e = rootNode.breadthFirstEnumeration();
                while (e.hasMoreElements()) {
                	
                    final FreetalkMessage freetalkMessage =  e.nextElement();
                    if (parentMsgIdString.equals(freetalkMessage.getMsgId())) {
                        freetalkMessageIterator.remove();
                        freetalkMessage.add(newMsg, false);
                        foundParent = true;
                        break;
                    }
                }
            }
        } while (foundParent);

        // add unmatched to root
        for (final FreetalkMessage newMsg : msgList) {
            rootNode.add(newMsg, false);
        }
    }
}
