/*
  FreetalkMessage.java / Frost
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

import javax.swing.tree.*;

import org.joda.time.*;

import frost.messaging.freetalk.boards.*;
import frost.util.*;

/**
 * A Freetalk message.
 */
public class FreetalkMessage extends DefaultMutableTreeNode {

    private FreetalkBoard board = null;
    private String msgId = null;
    private int msgIndex = 0;
    private String title = null;
    private String author = null;
    private long dateMillis = 0;
    private long fetchDateMillis = 0;
    private boolean isThread = false;
    private String parentMsgID = null;
    private List<FreetalkFileAttachment> fileAttachments = null;

    private String content = null;

    private String dateAndTimeString = null;

    /**
     * Constructor for a dummy root node.
     */
    public FreetalkMessage(final boolean isRootnode) {
        board = new FreetalkBoard("(root)");
    }

    /**
     * Constructor used when a new message is received.
     */
    public FreetalkMessage(
            final FreetalkBoard board,
            final String msgId,
            final int msgIndex,
            final String title,
            final String author,
            final long dateMillis,
            final long fetchDateMillis,
            final boolean isThread,
            final String parentMsgID,
            final List<FreetalkFileAttachment> fileAttachments)
    {
        super();
        this.board = board;
        this.msgId = msgId;
        this.msgIndex = msgIndex;
        this.title = title;
        this.author = author;
        this.dateMillis = dateMillis;
        this.fetchDateMillis = fetchDateMillis;
        this.isThread = isThread;
        this.parentMsgID = parentMsgID;
        this.fileAttachments = fileAttachments;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public FreetalkBoard getBoard() {
        return board;
    }

    public String getMsgId() {
        return msgId;
    }

    public int getMsgIndex() {
        return msgIndex;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public long getFetchDateMillis() {
        return fetchDateMillis;
    }

    public boolean isThread() {
        return isThread;
    }

    public String getParentMsgID() {
        return parentMsgID;
    }

    public List<FreetalkFileAttachment> getFileAttachments() {
        return fileAttachments;
    }

    public String getContent() {
        return content;
    }
    public void setContent(final String c) {
        content = c;
    }

    public String getDateAndTimeString() {
        if (dateAndTimeString == null) {
            // Build a String of format yyyy.mm.dd hh:mm:ssGMT
            final DateTime dateTime = new DateTime(getDateMillis(), DateTimeZone.UTC);
            final DateMidnight date = dateTime.toDateMidnight();
            final TimeOfDay time = dateTime.toTimeOfDay();

            final String dateStr = DateFun.FORMAT_DATE_EXT.print(date);
            final String timeStr = DateFun.FORMAT_TIME_EXT.print(time);

            final StringBuilder sb = new StringBuilder(29);
            sb.append(dateStr).append(" ").append(timeStr);

            this.dateAndTimeString = sb.toString();
        }
        return dateAndTimeString;
    }
}
