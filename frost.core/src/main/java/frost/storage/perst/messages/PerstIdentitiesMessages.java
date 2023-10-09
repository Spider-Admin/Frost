/*
  PerstIdentitiesMessages.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.messages;

import org.garret.perst.IPersistentList;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

public class PerstIdentitiesMessages extends Persistent {
    private String uniqueName;
    private IPersistentList<PerstFrostMessageObject> messagesFromIdentity;
    
    public PerstIdentitiesMessages() {}
    public PerstIdentitiesMessages(String un, Storage storage) {
        uniqueName = un;
        messagesFromIdentity = storage.createScalableList();
    }
    public String getUniqueName() {
        return uniqueName;
    }
    public IPersistentList<PerstFrostMessageObject> getMessagesFromIdentity() {
        return messagesFromIdentity;
    }
    public void addMessageToIdentity(PerstFrostMessageObject pmo) {
        messagesFromIdentity.add(pmo);
    }
    public void removeMessageFromIdentity(PerstFrostMessageObject pmo) {
        messagesFromIdentity.remove(pmo);
    }
    
    @Override
    public void deallocate() {
        if( messagesFromIdentity != null ) {
            messagesFromIdentity.deallocate();
            messagesFromIdentity = null;
        }
        super.deallocate();
    }
}