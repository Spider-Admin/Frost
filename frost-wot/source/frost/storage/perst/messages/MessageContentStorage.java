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

import org.garret.perst.*;

import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;

public class MessageContentStorage implements Savable {

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB
    
    private Storage storage = null;
    private MessageContentStorageRoot storageRoot = null;
    
    private static MessageContentStorage instance = new MessageContentStorage();
    
    protected MessageContentStorage() {}
    
    public static MessageContentStorage inst() {
        return instance;
    }
    
    private Storage getStorage() {
        return storage;
    }
    
    public boolean initStorage() {
        String databaseFilePath = "store/messagesContents.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.string.encoding", "UTF-8");
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (MessageContentStorageRoot)storage.getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new MessageContentStorageRoot(storage);
            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public synchronized void commitStore() {
        if( getStorage() == null ) {
            return;
        }
        getStorage().commit();
    }

    public void save() throws StorageException {
        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: MessagesContentStorage closed.");
    }
    
    public boolean addContentForOid(int oid, String content) {
        PerstString ps = new PerstString(content);
        return storageRoot.getContentByMsgOid().put(oid, ps);
    }

    public boolean addPublickeyForOid(int oid, String content) {
        PerstString ps = new PerstString(content);
        return storageRoot.getPublickeyByMsgOid().put(oid, ps);
    }

    public boolean addSignatureForOid(int oid, String content) {
        PerstString ps = new PerstString(content);
        return storageRoot.getSignatureByMsgOid().put(oid, ps);
    }

    public boolean addAttachmentsForOid(int oid, AttachmentList boards, AttachmentList files) {
        PerstAttachments pa = new PerstAttachments(getStorage(), boards, files);
        return storageRoot.getAttachmentsByMsgOid().put(oid, pa);
    }

    public String getContentForOid(int oid) {
        PerstString ps = storageRoot.getContentByMsgOid().get(oid);
        if( ps != null ) {
            return ps.getValue();
        }
        return null;
    }
    
    public String getPublickeyForOid(int oid) {
        PerstString ps = storageRoot.getPublickeyByMsgOid().get(oid);
        if( ps != null ) {
            return ps.getValue();
        }
        return null;
    }

    public String getSignatureForOid(int oid) {
        PerstString ps = storageRoot.getSignatureByMsgOid().get(oid);
        if( ps != null ) {
            return ps.getValue();
        }
        return null;
    }

    public PerstAttachments getAttachmentsForOid(int oid) {
        PerstAttachments pa = storageRoot.getAttachmentsByMsgOid().get(oid);
        return pa;
    }

    public void deallocateForOid(int oid) {
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
        
        PerstAttachments pa = storageRoot.getAttachmentsByMsgOid().removeKey(oid);
        if( pa != null ) {
            pa.deallocate();
        }
    }
}
