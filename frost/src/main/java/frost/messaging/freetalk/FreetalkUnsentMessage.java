/*
  FreetalkUnsentMessage.java / Frost
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
package frost.messaging.freetalk;

import java.util.*;

import frost.messaging.freetalk.boards.*;
import frost.messaging.freetalk.identities.*;

/**
 * Holds fields of an unsent Freetalk message.
 */
public class FreetalkUnsentMessage {

    final String parentId;
    final FreetalkOwnIdentity ownIdentity;
    final FreetalkBoard replyToBoard;
    final List<FreetalkBoard> targetBoards;
    final String title;
    final String content;
    final List<FreetalkFileAttachment> fileAttachments;

    public FreetalkUnsentMessage(
            final String parentId,
            final FreetalkOwnIdentity ownIdentity,
            final FreetalkBoard replyToBoard,
            final List<FreetalkBoard> targetBoards,
            final String title,
            final String content,
            final List<FreetalkFileAttachment> fileAttachments)
    {
        this.parentId = parentId;
        this.ownIdentity = ownIdentity;
        this.replyToBoard = replyToBoard;
        this.targetBoards = targetBoards;
        this.title = title;
        this.content = content;
        this.fileAttachments = fileAttachments;
    }

    public String getParentId() {
        return parentId;
    }

    public FreetalkOwnIdentity getOwnIdentity() {
        return ownIdentity;
    }

    public FreetalkBoard getReplyToBoard() {
        return replyToBoard;
    }

    public List<FreetalkBoard> getTargetBoards() {
        return targetBoards;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<FreetalkFileAttachment> getFileAttachments() {
        return fileAttachments;
    }
}
