/*
  UnsentMessagesTableItem.java / Frost
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
package frost.gui.unsentmessages;

import frost.messages.*;
import frost.util.model.*;

public class UnsentMessagesTableItem extends ModelItem {
    
    public static final int STATE_WAITING = 1;
    public static final int STATE_UPLOADING = 2;

    private FrostUnsentMessageObject messageObject;
    
    public UnsentMessagesTableItem(FrostUnsentMessageObject mo) {
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
        return messageObject.getBoard().getName();
    }

    public String getTimeAddedString() {
        return messageObject.getTimeAddedString();
    }
    
    public FrostUnsentMessageObject getFrostUnsentMessageObject() {
        return messageObject;
    }
    
    public int getState() {
        if( getFrostUnsentMessageObject().getCurrentUploadThread() == null ) {
            return STATE_WAITING;
        } else {
            return STATE_UPLOADING;
        }
    }
    
    @Override
    public void fireChange() {
        super.fireChange();
    }
}
