/*
  UnsendMessagesTableModel.java / Frost
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
package frost.gui.unsendmessages;

import java.util.*;

import frost.messages.*;
import frost.util.model.*;

public class UnsendMessagesTableModel extends OrderedModel {

    public UnsendMessagesTableModel() {
        super();
    }
    
    public void loadTableModel() {
        List msgs = UnsendMessagesManager.getUnsendMessages();
        for( Iterator i = msgs.iterator(); i.hasNext(); ) {
            FrostUnsendMessageObject mo = (FrostUnsendMessageObject) i.next();
            UnsendMessagesTableItem item = new UnsendMessagesTableItem(mo);
            addSendMessageItem(item);
        }
    }
    
    public void addSendMessageItem(UnsendMessagesTableItem item) {
        addItem(item);
    }
    
    public void addFrostUnsendMessageObject( FrostUnsendMessageObject mo ) {
        UnsendMessagesTableItem item = new UnsendMessagesTableItem(mo);
        addSendMessageItem(item);
    }

    /**
     * @param mo  the item to remove from the unsend messages table
     */
    public void removeFrostUnsendMessageObject( FrostUnsendMessageObject mo ) {
        for(int x=0; x < getItemCount(); x++) {
            UnsendMessagesTableItem i = (UnsendMessagesTableItem) getItemAt(x);
            if( i.getFrostUnsendMessageObject().getMessageId().equals(mo.getMessageId()) ) {
                removeItems(new ModelItem[] { i } );
                return;
            }
        }
    }

    public void updateFrostUnsendMessageObject( FrostUnsendMessageObject mo ) {
        for(int x=0; x < getItemCount(); x++) {
            UnsendMessagesTableItem i = (UnsendMessagesTableItem) getItemAt(x);
            if( i.getFrostUnsendMessageObject().getMessageId().equals(mo.getMessageId()) ) {
                i.fireChange();
                return;
            }
        }
    }

    /**
     * Returns null if ok, or the item that failed deletion.
     */
    public FrostUnsendMessageObject deleteItems(ModelItem[] selectedItems) {
        for(int x=0; x < selectedItems.length; x++) {
            UnsendMessagesTableItem item = (UnsendMessagesTableItem) selectedItems[x];
            if( !UnsendMessagesManager.deleteMessage(item.getFrostUnsendMessageObject()) ) {
                return item.getFrostUnsendMessageObject();
            }
            super.removeItems( new ModelItem[] { item } );
        }
        return null;
    }
}
