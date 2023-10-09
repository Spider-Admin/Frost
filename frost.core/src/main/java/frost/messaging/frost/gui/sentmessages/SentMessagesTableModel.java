/*
  SendMessagesTableModel.java / Frost
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import frost.messaging.frost.FrostMessageObject;
import frost.messaging.frost.SentMessagesManager;
import frost.util.model.SortedModel;
import frost.util.model.SortedTableFormat;

public class SentMessagesTableModel extends SortedModel<SentMessagesTableItem> {

    public SentMessagesTableModel(SortedTableFormat<SentMessagesTableItem> f) {
        super(f);
    }
    
    public void loadTableModel() {
        List<FrostMessageObject> msgs = SentMessagesManager.retrieveSentMessages();
        for( Iterator<FrostMessageObject> i = msgs.iterator(); i.hasNext(); ) {
            FrostMessageObject mo = i.next();
            SentMessagesTableItem item = new SentMessagesTableItem(mo);
            addSendMessageItem(item);
        }
    }
    
    public void addSendMessageItem(SentMessagesTableItem item) {
        addItem(item);
    }
    
    public void addFrostMessageObject( FrostMessageObject mo ) {
        SentMessagesTableItem item = new SentMessagesTableItem(mo);
        addSendMessageItem(item);
    }
    
    @Override
    public boolean removeItems(List<SentMessagesTableItem> selectedItems) {
        LinkedList<FrostMessageObject> itemsToDelete = new LinkedList<FrostMessageObject>();
        for( int x = selectedItems.size() - 1; x >= 0; x-- ) {
            itemsToDelete.add(selectedItems.get(x).getFrostMessageObject());
        }
        if( SentMessagesManager.deleteSentMessages(itemsToDelete) == 0 ) {
            return false;
        }
        boolean retval = super.removeItems(selectedItems);
        return retval;
    }
}
