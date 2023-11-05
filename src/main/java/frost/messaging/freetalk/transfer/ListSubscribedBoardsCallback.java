/*
  ListBoardsCallback.java / Frost
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.MainFrame;
import frost.fcp.fcp07.NodeMessage;
import frost.fcp.fcp07.freetalk.FcpFreetalkConnection.FreetalkNodeMessageCallback;
import frost.messaging.freetalk.FreetalkManager;
import frost.messaging.freetalk.boards.FreetalkBoard;

public class ListSubscribedBoardsCallback implements FreetalkNodeMessageCallback {

	private static final Logger logger = LoggerFactory.getLogger(ListSubscribedBoardsCallback.class);

    private final MainFrame mainFrame;

    public ListSubscribedBoardsCallback(final MainFrame mf) {
        mainFrame = mf;
    }

    public void handleNodeMessage(final String id, final NodeMessage nodeMsg) {

        if (!nodeMsg.isMessageName("FCPPluginReply")) {
            logger.warn("Unexpected NodeMessage received: {}", nodeMsg.getMessageName());
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            mainFrame.deactivateGlassPane();
            return;
        }

        if ("EndListBoards".equals(nodeMsg.getStringValue("Replies.Message"))) {
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            mainFrame.deactivateGlassPane();
            return;
        }

        if (!"SubscribedBoard".equals(nodeMsg.getStringValue("Replies.Message"))) {
            logger.warn("Unexpected NodeMessage received: {}", nodeMsg.getStringValue("Replies.Message"));
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            mainFrame.deactivateGlassPane();
            return;
        }

        final String name = nodeMsg.getStringValue("Replies.Name");
        final int messageCount = nodeMsg.getIntValue("Replies.MessageCount", 0);
        final long latestMessageDate = nodeMsg.getLongValue("Replies.LatestMessageDate", 0L);
        final long firstSeenDate = nodeMsg.getLongValue("Replies.FirstSeenDate", 0L);

        final FreetalkBoard board = new FreetalkBoard(name, messageCount, firstSeenDate, latestMessageDate);
        // FIXME: add to board list, check dupes and removals

        mainFrame.getFreetalkMessageTab().getBoardTree().addNewBoard(board);
    }
}
