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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.identities.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.messages.*;

public class IdentitiesStorage extends AbstractFrostStorage implements Savable {

    private static final Logger logger = Logger.getLogger(IdentitiesStorage.class.getName());

    private IdentitiesStorageRoot storageRoot = null;

    private static IdentitiesStorage instance = new IdentitiesStorage();

    protected IdentitiesStorage() {
        super();
    }

    public static IdentitiesStorage inst() {
        return instance;
    }

    @Override
    public boolean initStorage() {
        final String databaseFilePath = getStorageFilename("identities.dbs"); // path to the database file
        final int pagePoolSize = getPagePoolSize(SettingsClass.PERST_PAGEPOOLSIZE_IDENTITIES);

        open(databaseFilePath, pagePoolSize, true, false, false);

        storageRoot = (IdentitiesStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new IdentitiesStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commit(); // commit transaction
        }
        return true;
    }

    public void save() throws StorageException {
        close();
        storageRoot = null;
        System.out.println("INFO: IdentitiesStorage closed.");
    }

    public void importLocalIdentities(final List<LocalIdentity> lids) {
        for(final LocalIdentity li : lids) {
            storageRoot.getLocalIdentities().add( li );
        }
        commit();
    }

    public void importIdentities(final List<Identity> ids) {
        int cnt = 0;
        for(final Identity li : ids) {
            storageRoot.getIdentities().add( li );
            cnt++;
            if( cnt%100 == 0 ) {
                System.out.println("Committing after "+cnt+" identities");
                commit();
            }
        }
        commit();
    }

    public Hashtable<String,Identity> loadIdentities() {
        final Hashtable<String,Identity> result = new Hashtable<String,Identity>();
        for(final Identity id : storageRoot.getIdentities()) {
            result.put(id.getUniqueName(), id);
        }
        return result;
    }

    public void insertIdentity(final Identity id) {
        storageRoot.getIdentities().add( id );
        commit();
    }

    public boolean removeIdentity(final Identity id, final boolean doCommit) {
        if( id.getStorage() == null ) {
            logger.severe("id not in store");
            return false;
        }
        final boolean isRemoved = storageRoot.getIdentities().remove(id);
        id.deallocate();
        if( doCommit ) {
            commit();
        }
        return isRemoved;
    }

    public int getIdentityCount() {
        return storageRoot.getIdentities().size();
    }

    public Hashtable<String,LocalIdentity> loadLocalIdentities() {
        final Hashtable<String,LocalIdentity> result = new Hashtable<String,LocalIdentity>();
        for(final LocalIdentity id : storageRoot.getLocalIdentities()) {
            result.put(id.getUniqueName(), id);
        }
        return result;
    }

    public void insertLocalIdentity(final LocalIdentity id) {
        storageRoot.getLocalIdentities().add( id );
        commit();
    }

    public boolean removeLocalIdentity(final LocalIdentity lid) {
        if( lid.getStorage() == null ) {
            logger.severe("lid not in store");
            return false;
        }
        final boolean isRemoved = storageRoot.getLocalIdentities().remove(lid);
        lid.deallocate();
        commit();
        return isRemoved;
    }

    public static class IdentityMsgAndFileCount {
        final int fileCount;
        final int messageCount;
        public IdentityMsgAndFileCount(final int mc, final int fc) {
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

        final Hashtable<String,IdentityMsgAndFileCount> data = new Hashtable<String,IdentityMsgAndFileCount>();

        for(final Identity id : Core.getIdentities().getIdentities()) {
            final int messageCount = MessageStorage.inst().getMessageCount(id.getUniqueName());
            final int fileCount = FileListStorage.inst().getFileCount(id.getUniqueName());
            final IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
            data.put(id.getUniqueName(), s);
        }

        for(final LocalIdentity id : Core.getIdentities().getLocalIdentities()) {
            final int messageCount = MessageStorage.inst().getMessageCount(id.getUniqueName());
            final int fileCount = FileListStorage.inst().getFileCount(id.getUniqueName());
            final IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
            data.put(id.getUniqueName(), s);
        }
        return data;
    }
}
