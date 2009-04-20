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

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.fcp07.*;
import frost.fcp.fcp07.freetalk.FcpFreetalkConnection.*;
import frost.messaging.freetalk.*;
import frost.messaging.freetalk.boards.*;

public class ListMessagesCallback implements FreetalkNodeMessageCallback {

    private static final Logger logger = Logger.getLogger(ListMessagesCallback.class.getName());

    private final FreetalkManager ftManager;
    private final MainFrame mainFrame;
    private final FreetalkBoard board;

    public ListMessagesCallback(final FreetalkManager ftMan, final MainFrame mf, final FreetalkBoard b) {
        ftManager = ftMan;
        mainFrame = mf;
        board = b;
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
        final boolean isThread = Boolean.valueOf(nodeMsg.getStringValue("Replies.IsThread"));
        final String parentMsgID = nodeMsg.getStringValue("Replies.ParentID");
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

//        final FreetalkMessage ftMsg = new FreetalkMessage(
//                board,
//                msgIndex,
//                msgId,
//                title,
//                author,
//                dateMillis,
//                fetchDateMillis,
//                isThread,
//                parentMsgID,
//                fileAttachments);

        // FIXME: add to message panel

//        ftManager.getBoardTree().addNewBoard(board);
    }

}
