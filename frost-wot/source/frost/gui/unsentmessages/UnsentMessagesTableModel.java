/*
  UnsentMessagesTableModel.java / Frost
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

import java.util.*;

import frost.messages.*;
import frost.util.model.*;

public class UnsentMessagesTableModel extends SortedModel {

    public UnsentMessagesTableModel(SortedTableFormat f) {
        super(f);
    }
    
    public void loadTableModel() {
        List msgs = UnsentMessagesManager.getUnsentMessages();
        for( Iterator i = msgs.iterator(); i.hasNext(); ) {
            FrostUnsentMessageObject mo = (FrostUnsentMessageObject) i.next();
            UnsentMessagesTableItem item = new UnsentMessagesTableItem(mo);
            addUnsentMessageItem(item);
        }
    }
    
    public void addUnsentMessageItem(UnsentMessagesTableItem item) {
        addItem(item);
    }
    
    public void addFrostUnsentMessageObject( FrostUnsentMessageObject mo ) {
        UnsentMessagesTableItem item = new UnsentMessagesTableItem(mo);
        addUnsentMessageItem(item);
    }

    /**
     * @param mo  the item to remove from the unsend messages table
     */
    public void removeFrostUnsentMessageObject( FrostUnsentMessageObject mo ) {
        for(int x=0; x < getItemCount(); x++) {
            UnsentMessagesTableItem i = (UnsentMessagesTableItem) getItemAt(x);
            if( i.getFrostUnsentMessageObject().getMessageId().equals(mo.getMessageId()) ) {
                removeItems(new ModelItem[] { i } );
                return;
            }
        }
    }

    public void updateFrostUnsentMessageObject( FrostUnsentMessageObject mo ) {
        for(int x=0; x < getItemCount(); x++) {
            UnsentMessagesTableItem i = (UnsentMessagesTableItem) getItemAt(x);
            if( i.getFrostUnsentMessageObject().getMessageId().equals(mo.getMessageId()) ) {
                i.fireChange();
                return;
            }
        }
    }

    /**
     * Returns null if ok, or the item that failed deletion.
     */
    public FrostUnsentMessageObject deleteItems(ModelItem[] selectedItems) {
        for(int x=0; x < selectedItems.length; x++) {
            UnsentMessagesTableItem item = (UnsentMessagesTableItem) selectedItems[x];
            if( !UnsentMessagesManager.deleteMessage(item.getFrostUnsentMessageObject()) ) {
                return item.getFrostUnsentMessageObject();
            }
            super.removeItems( new ModelItem[] { item } );
        }
        return null;
    }
}
