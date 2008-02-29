/*
  MessageContentStorageRoot.java / Frost
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

import org.garret.perst.*;

import frost.storage.perst.*;

public class MessageContentStorageRoot extends Persistent {

    private Index<PerstString> contentByMsgOid;
    private Index<PerstString> publickeyByMsgOid;
    private Index<PerstString> signatureByMsgOid;
    private Index<PerstAttachments> attachmentsByMsgOid;
    
    public MessageContentStorageRoot() {}
    
    public MessageContentStorageRoot(Storage storage) {
        contentByMsgOid = storage.createIndex(int.class, true);
        publickeyByMsgOid = storage.createIndex(int.class, true);
        signatureByMsgOid = storage.createIndex(int.class, true);
        attachmentsByMsgOid = storage.createIndex(int.class, true);
    }

    public Index<PerstString> getContentByMsgOid() {
        return contentByMsgOid;
    }

    public Index<PerstString> getPublickeyByMsgOid() {
        return publickeyByMsgOid;
    }

    public Index<PerstString> getSignatureByMsgOid() {
        return signatureByMsgOid;
    }

    public Index<PerstAttachments> getAttachmentsByMsgOid() {
        return attachmentsByMsgOid;
    }
}
