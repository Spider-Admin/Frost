/*
  IdentitiesStorage.java / Frost
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
package frost.storage.perst.identities;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.garret.perst.*;

import frost.*;
import frost.identities.*;
import frost.storage.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.messages.*;

public class IdentitiesStorage implements Savable {

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB
    
    private Storage storage = null;
    private IdentitiesStorageRoot storageRoot = null;
    
    private static IdentitiesStorage instance = new IdentitiesStorage();

    protected IdentitiesStorage() {}
    
    public static IdentitiesStorage inst() {
        return instance;
    }
    
    private Storage getStorage() {
        return storage;
    }
    
    public boolean initStorage() {
        String databaseFilePath = "store/identities.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.string.encoding", "UTF-8");
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (IdentitiesStorageRoot)storage.getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new IdentitiesStorageRoot(storage);
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
        System.out.println("INFO: IdentitiesStorage closed.");
    }
    
    public void importLocalIdentities(List<LocalIdentity> lids) {
        for(LocalIdentity li : lids) {
            storageRoot.getLocalIdentities().add( li );
        }
        commitStore();
    }

    public void importIdentities(List<Identity> ids) {
        int cnt = 0;
        for(Identity li : ids) {
            storageRoot.getIdentities().add( li );
            cnt++;
            if( cnt%100 == 0 ) {
                System.out.println("Committing after "+cnt+" identities");
                commitStore();
            }
        }
        commitStore();
    }
    
    public Hashtable<String,Identity> loadIdentities() {
        Hashtable<String,Identity> result = new Hashtable<String,Identity>();
        for(Identity id : storageRoot.getIdentities()) {
            result.put(id.getUniqueName(), id);
        }
        return result;
    }
    
    public void insertIdentity(Identity id) {
        storageRoot.getIdentities().add( id );
        commitStore();
    }

    public boolean removeIdentity(Identity id, boolean doCommit) {
        if( id.getStorage() == null ) {
            System.out.println("id not in store");
            return false;
        }
        boolean isRemoved = storageRoot.getIdentities().remove(id);
        id.deallocate();
        if( doCommit ) {
            commitStore();
        }
        return isRemoved;
    }
    
    public int getIdentityCount() {
        return storageRoot.getIdentities().size();
    }

    public Hashtable<String,LocalIdentity> loadLocalIdentities() {
        Hashtable<String,LocalIdentity> result = new Hashtable<String,LocalIdentity>();
        for(LocalIdentity id : storageRoot.getLocalIdentities()) {
            result.put(id.getUniqueName(), id);
        }
        return result;
    }

    public void insertLocalIdentity(LocalIdentity id) {
        storageRoot.getLocalIdentities().add( id );
        commitStore();
    }

    public boolean removeLocalIdentity(LocalIdentity lid) {
        if( lid.getStorage() == null ) {
            System.out.println("lid not in store");
            return false;
        }
        boolean isRemoved = storageRoot.getLocalIdentities().remove(lid);
        lid.deallocate();
        commitStore();
        return isRemoved;
    }
    
    public static class IdentityMsgAndFileCount {
        final int fileCount;
        final int messageCount;
        public IdentityMsgAndFileCount(int mc, int fc) {
            messageCount = mc;
            fileCount = fc;
        }
        public int getFileCount() {
            return fileCount;
        }
        public int getMessageCount() {
            return messageCount;
        }
    }
    
    /**
     * Retrieve msgCount and fileCount for each identity.
     */
    public Hashtable<String,IdentityMsgAndFileCount> retrieveMsgAndFileCountPerIdentity() throws SQLException {
        
        Hashtable<String,IdentityMsgAndFileCount> data = new Hashtable<String,IdentityMsgAndFileCount>();

        for(Identity id : Core.getIdentities().getIdentities()) {
            int messageCount = MessageStorage.inst().getMessageCount(id.getUniqueName());
            int fileCount = FileListStorage.inst().getFileCount(id.getUniqueName());
            IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
            data.put(id.getUniqueName(), s);
        }

        for(LocalIdentity id : Core.getIdentities().getLocalIdentities()) {
            int messageCount = MessageStorage.inst().getMessageCount(id.getUniqueName());
            int fileCount = FileListStorage.inst().getFileCount(id.getUniqueName());
            IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
            data.put(id.getUniqueName(), s);
        }
        return data;
    }
}
