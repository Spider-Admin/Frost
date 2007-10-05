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

import frost.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;

public class MessageContentStorage extends AbstractFrostStorage implements ExitSavable {

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
        final String databaseFilePath = getStorageFilename("messagesContents.dbs"); // path to the database file
        final int pagePoolSize = getPagePoolSize(SettingsClass.PERST_PAGEPOOLSIZE_MESSAGECONTENTS);

        open(databaseFilePath, pagePoolSize, true, false, false);

        storageRoot = (MessageContentStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new MessageContentStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commit(); // commit transaction
        }
        return true;
    }

    public void exitSave() {
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
        PerstString ps;
        ps = storageRoot.getContentByMsgOid().get(oid);
        if( ps != null ) {
            storageRoot.getContentByMsgOid().removeKey(oid);
            ps.deallocate();
        }
        ps = storageRoot.getPublickeyByMsgOid().get(oid);
        if( ps != null ) {
            storageRoot.getPublickeyByMsgOid().removeKey(oid);
            ps.deallocate();
        }
        ps = storageRoot.getSignatureByMsgOid().get(oid);
        if( ps != null ) {
            storageRoot.getSignatureByMsgOid().removeKey(oid);
            ps.deallocate();
        }

        final PerstAttachments pa = storageRoot.getAttachmentsByMsgOid().get(oid);
        if( pa != null ) {
            storageRoot.getAttachmentsByMsgOid().removeKey(oid);
            pa.deallocate();
        }
    }
}
