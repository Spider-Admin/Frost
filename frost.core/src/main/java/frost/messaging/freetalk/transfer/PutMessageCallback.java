/*
  PutMessageCallback.java / Frost
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

import java.util.logging.Logger;

import javax.swing.JOptionPane;

import frost.MainFrame;
import frost.fcp.fcp07.NodeMessage;
import frost.fcp.fcp07.freetalk.FcpFreetalkConnection.FreetalkNodeMessageCallback;
import frost.messaging.freetalk.FreetalkManager;

/**
 * Callback for PutMessage command.
 *
 * Evaluates Freetalks response and displays error dialog when an error occured.
 */
public class PutMessageCallback implements FreetalkNodeMessageCallback {

    private static final Logger logger = Logger.getLogger(PutMessageCallback.class.getName());

    private final MainFrame mainFrame;

    public PutMessageCallback(final MainFrame mf) {
        mainFrame = mf;
    }

    public void handleNodeMessage(final String id, final NodeMessage nodeMsg) {

        FreetalkManager.getInstance().getConnection().unregisterCallback(id);

        if (!nodeMsg.isMessageName("FCPPluginReply")) {
            logger.severe("Unexpected NodeMessage received: "+nodeMsg.getMessageName());
            return;
        }

        if (!"PutMessageReply".equals(nodeMsg.getStringValue("Replies.Message"))) {
            logger.severe("Unexpected NodeMessage received: "+nodeMsg.getStringValue("Replies.Message"));
            return;
        }

        final boolean msgEnqueued = nodeMsg.getBoolValue("Replies.MessageEnqueued");
        final String errorDesc = nodeMsg.getStringValue("Replies.ErrorDescription");

        if (!msgEnqueued) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Error Description: "+errorDesc,
                    "Error sending message to Freetalk",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
