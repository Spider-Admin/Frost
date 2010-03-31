/*
  SendMessagesTableItem.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.frost.gui.sentmessages;

import frost.messaging.frost.*;
import frost.util.model.*;

public class SentMessagesTableItem extends ModelItem {

    private final FrostMessageObject messageObject;

    public SentMessagesTableItem(final FrostMessageObject mo) {
        messageObject = mo;
    }

    public String getSubject() {
        return messageObject.getSubject();
    }

    public String getFrom() {
        return messageObject.getFromName();
    }

    public String getTo() {
        if( messageObject.getRecipientName() == null ) {
            return "";
        } else {
            return messageObject.getRecipientName();
        }
    }

    public String getBoardName() {
        if (messageObject.getBoard() == null) {
            return "(*removed*)";
        }
        return messageObject.getBoard().getName();
    }

    public String getDateAndTimeString() {
        return messageObject.getDateAndTimeString();
    }

    public FrostMessageObject getFrostMessageObject() {
        return messageObject;
    }
}
