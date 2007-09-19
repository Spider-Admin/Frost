/*
  MessageContentStorage.java / Frost
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

import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;

public class MessageContentStorage extends AbstractFrostStorage implements Savable {

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB

    private MessageContentStorageRoot storageRoot = null;

    private static MessageContentStorage instance = new MessageContentStorage();

    protected MessageContentStorage() {
        super();
    }

    public static MessageContentStorage inst() {
        return instance;
    }

    @Override
    public boolean initStorage() {
        final int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes
        return initStorage(pagePoolSize);
    }

    @Override
    public boolean initStorage(final int pagePoolSize) {
        final String databaseFilePath = "store/messagesContents.dbs"; // path to the database file

        open(databaseFilePath, pagePoolSize, true, false, false);

        storageRoot = (MessageContentStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new MessageContentStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commitStore(); // commit transaction
        }
        return true;
    }

    public void save() {
        close();
        storageRoot = null;
        System.out.println("INFO: MessagesContentStorage closed.");
    }

    public boolean addContentForOid(final int oid, final String content) {
        final PerstString ps = new PerstString(content);
        return storageRoot.getContentByMsgOid().put(oid, ps);
    }

    public boolean addPublickeyForOid(final int oid, final String content) {
        final PerstString ps = new PerstString(content);
        return storageRoot.getPublickeyByMsgOid().put(oid, ps);
    }

    public boolean addSignatureForOid(final int oid, final String content) {
        final PerstString ps = new PerstString(content);
        return storageRoot.getSignatureByMsgOid().put(oid, ps);
    }

    public boolean addAttachmentsForOid(final int oid, final AttachmentList boards, final AttachmentList files) {
        final PerstAttachments pa = new PerstAttachments(getStorage(), boards, files);
        return storageRoot.getAttachmentsByMsgOid().put(oid, pa);
    }

    public String getContentForOid(final int oid) {
        final PerstString ps = storageRoot.getContentByMsgOid().get(oid);
        if( ps != null ) {
            return ps.getValue();
        }
        return null;
    }

    public String getPublickeyForOid(final int oid) {
        final PerstString ps = storageRoot.getPublickeyByMsgOid().get(oid);
        if( ps != null ) {
            return ps.getValue();
        }
        return null;
    }

    public String getSignatureForOid(final int oid) {
        final PerstString ps = storageRoot.getSignatureByMsgOid().get(oid);
        if( ps != null ) {
            return ps.getValue();
        }
        return null;
    }

    public PerstAttachments getAttachmentsForOid(final int oid) {
        final PerstAttachments pa = storageRoot.getAttachmentsByMsgOid().get(oid);
        return pa;
    }

    public void deallocateForOid(final int oid) {
        // FIXME: testen ob removeKey auch tut! Nimmt obj statt int...
        PerstString ps = storageRoot.getContentByMsgOid().removeKey(oid);
        if( ps != null ) {
            ps.deallocate();
        }
        ps = storageRoot.getPublickeyByMsgOid().removeKey(oid);
        if( ps != null ) {
            ps.deallocate();
        }
        ps = storageRoot.getSignatureByMsgOid().removeKey(oid);
        if( ps != null ) {
            ps.deallocate();
        }

        final PerstAttachments pa = storageRoot.getAttachmentsByMsgOid().removeKey(oid);
        if( pa != null ) {
            pa.deallocate();
        }
    }
}
