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
package frost.messaging.frost.gui.unsentmessages;

import java.util.Iterator;
import java.util.List;

import frost.messaging.frost.FrostUnsentMessageObject;
import frost.messaging.frost.UnsentMessagesManager;
import frost.util.model.SortedModel;
import frost.util.model.SortedTableFormat;

public class UnsentMessagesTableModel extends SortedModel<UnsentMessagesTableItem> {

    public UnsentMessagesTableModel(SortedTableFormat<UnsentMessagesTableItem> f) {
        super(f);
    }
    
    public void loadTableModel() {
        List<FrostUnsentMessageObject> msgs = UnsentMessagesManager.getUnsentMessages();
        for( Iterator<FrostUnsentMessageObject> i = msgs.iterator(); i.hasNext(); ) {
            addUnsentMessageItem(new UnsentMessagesTableItem(i.next()));
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
            UnsentMessagesTableItem item = getItemAt(x);
            if( item.getFrostUnsentMessageObject().getMessageId().equals(mo.getMessageId()) ) {
            	removeItem(item);
                return;
            }
        }
    }

    public void updateFrostUnsentMessageObject( FrostUnsentMessageObject mo ) {
        for(int x=0; x < getItemCount(); x++) {
            UnsentMessagesTableItem item = getItemAt(x);
            if( item.getFrostUnsentMessageObject().getMessageId().equals(mo.getMessageId()) ) {
                item.fireChange();
                return;
            }
        }
    }

    /**
     * Returns null if ok, or the item that failed deletion.
     */
    public FrostUnsentMessageObject deleteItems(List<UnsentMessagesTableItem> selectedItems) {
    	final int size = selectedItems.size();
        for(int x=0; x < size; x++) {
            UnsentMessagesTableItem item =  selectedItems.get(x);
            if( !UnsentMessagesManager.deleteMessage(item.getFrostUnsentMessageObject()) ) {
                return item.getFrostUnsentMessageObject();
            }
            super.removeItem( item );
        }
        return null;
    }
}
