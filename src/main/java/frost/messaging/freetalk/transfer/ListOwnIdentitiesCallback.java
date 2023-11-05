/*
  ListOwnIdentitiesCallback.java / Frost
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.fcp.fcp07.NodeMessage;
import frost.fcp.fcp07.freetalk.FcpFreetalkConnection.FreetalkNodeMessageCallback;
import frost.messaging.freetalk.FreetalkManager;
import frost.messaging.freetalk.identities.FreetalkOwnIdentity;

public class ListOwnIdentitiesCallback implements FreetalkNodeMessageCallback {

	private static final Logger logger = LoggerFactory.getLogger(ListOwnIdentitiesCallback.class);

    private final List<FreetalkOwnIdentity> ownIdentityList = new ArrayList<FreetalkOwnIdentity>();

    public ListOwnIdentitiesCallback() {
    }

    public void handleNodeMessage(final String id, final NodeMessage nodeMsg) {

        if (!nodeMsg.isMessageName("FCPPluginReply")) {
            logger.warn("Unexpected NodeMessage received: {}", nodeMsg.getMessageName());
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            return;
        }

        if ("EndListOwnIdentities".equals(nodeMsg.getStringValue("Replies.Message"))) {
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);

            FreetalkManager.getInstance().applyOwnIdentities(ownIdentityList);

            return;
        }

        if (!"OwnIdentity".equals(nodeMsg.getStringValue("Replies.Message"))) {
            logger.warn("Unexpected NodeMessage received: {}", nodeMsg.getStringValue("Replies.Message"));
            FreetalkManager.getInstance().getConnection().unregisterCallback(id);
            return;
        }

        final String uid = nodeMsg.getStringValue("Replies.ID");
        final String nickname = nodeMsg.getStringValue("Replies.Nickname");
        final String ftAddress = nodeMsg.getStringValue("Replies.FreetalkAddress");

        final FreetalkOwnIdentity ownIdentity = new FreetalkOwnIdentity(uid, nickname, ftAddress);
        ownIdentityList.add(ownIdentity);
    }
}
