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

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.identities.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.messages.*;

public class IdentitiesStorage extends AbstractFrostStorage implements ExitSavable {

    private static final Logger logger = Logger.getLogger(IdentitiesStorage.class.getName());

    private static final String STORAGE_FILENAME = "identities.dbs";

    private IdentitiesStorageRoot storageRoot = null;

    private static IdentitiesStorage instance = new IdentitiesStorage();

    protected IdentitiesStorage() {
        super();
    }

    public static IdentitiesStorage inst() {
        return instance;
    }

    @Override
    public String getStorageFilename() {
        return STORAGE_FILENAME;
    }

    @Override
    public boolean initStorage() {
        final String databaseFilePath = buildStoragePath(getStorageFilename()); // path to the database file
        final int pagePoolSize = getPagePoolSize(SettingsClass.PERST_PAGEPOOLSIZE_IDENTITIES);

        open(databaseFilePath, pagePoolSize, true, true, false);

        storageRoot = (IdentitiesStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new IdentitiesStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commit(); // commit transaction
        }
        return true;
    }

    public void exitSave() throws StorageException {
        close();
        storageRoot = null;
        System.out.println("INFO: IdentitiesStorage closed.");
    }

    public void importLocalIdentities(final List<LocalIdentity> lids, final Hashtable<String,Integer> msgCounts) {
        if( !beginExclusiveThreadTransaction() ) {
            return;
        }
        try {
            for( final LocalIdentity li : lids ) {
                final Integer i = msgCounts.get(li.getUniqueName());
                if( i != null ) {
                    li.setReceivedMessageCount(i.intValue());
                }
                li.correctUniqueName();
                storageRoot.getLocalIdentities().add(li);
            }
        } finally {
            endThreadTransaction();
        }
    }

    public void importIdentities(final List<Identity> ids, final Hashtable<String,Integer> msgCounts) {
        if( !beginExclusiveThreadTransaction() ) {
            return;
        }
        try {
            int cnt = 0;
            for( final Identity li : ids ) {
                final Integer i = msgCounts.get(li.getUniqueName());
                if( i != null ) {
                    li.setReceivedMessageCount(i.intValue());
                }
                li.correctUniqueName();
                storageRoot.getIdentities().add(li);
                cnt++;
                if( cnt % 100 == 0 ) {
                    System.out.println("Committing after " + cnt + " identities");
                    endThreadTransaction();
                    beginExclusiveThreadTransaction();
                }
            }
        } finally {
            endThreadTransaction();
        }
    }

    public Hashtable<String,Identity> loadIdentities() {
        final Hashtable<String,Identity> result = new Hashtable<String,Identity>();
        beginCooperativeThreadTransaction();
        try {
            for( final Identity id : storageRoot.getIdentities() ) {
                if( id == null ) {
                    logger.severe("Retrieved a null id !!! Please repair identities.dbs.");
                } else {
                    if( storageRoot.getMigrationLevel() < IdentitiesStorageRoot.MIGRATION_LEVEL_1 ) {
                        // one-time migration, remove all ids that have a '_' instead of an '@'
                        if( !Core.getIdentities().isIdentityValid(id) ) {
                            removeIdentity(id);
                            logger.severe("Dropped an invalid identity: "+id.getUniqueName());
                            continue;
                        }
                    }
                    result.put(id.getUniqueName(), id);
                }
            }
        } finally {
            storageRoot.setMigrationLevel(IdentitiesStorageRoot.MIGRATION_LEVEL_1);
            endThreadTransaction();
        }
        return result;
    }

    public boolean insertIdentity(final Identity id) {
        if( id == null ) {
            logger.severe("Rejecting to insert a null id!");
            return false;
        }
        storageRoot.getIdentities().add( id );
        return true;
    }

    public boolean removeIdentity(final Identity id) {
        if( id.getStorage() == null ) {
            logger.severe("id not in store");
            return false;
        }
        final boolean isRemoved = storageRoot.getIdentities().remove(id);
        if( isRemoved ) {
            id.deallocate();
        }
        return isRemoved;
    }

    public int getIdentityCount() {
        return storageRoot.getIdentities().size();
    }

    public Hashtable<String,LocalIdentity> loadLocalIdentities() {
        final Hashtable<String,LocalIdentity> result = new Hashtable<String,LocalIdentity>();
        beginCooperativeThreadTransaction();
        try {
            for( final LocalIdentity id : storageRoot.getLocalIdentities() ) {
                if( id == null ) {
                    logger.severe("Retrieved a null id !!! Please repair identities.dbs.");
                } else {
                    result.put(id.getUniqueName(), id);
                }
            }
        } finally {
            endThreadTransaction();
        }
        return result;
    }

    public boolean insertLocalIdentity(final LocalIdentity id) {
        if( id == null ) {
            logger.severe("Rejecting to insert a null id!");
            return false;
        }
        storageRoot.getLocalIdentities().add(id);
        return true;
    }

    public boolean removeLocalIdentity(final LocalIdentity lid) {
        if( lid.getStorage() == null ) {
            logger.severe("lid not in store");
            return false;
        }
        final boolean isRemoved = storageRoot.getLocalIdentities().remove(lid);
        if( isRemoved ) {
            lid.deallocate();
        }
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
    public Hashtable<String,IdentityMsgAndFileCount> retrieveMsgAndFileCountPerIdentity() {

        final Hashtable<String,IdentityMsgAndFileCount> data = new Hashtable<String,IdentityMsgAndFileCount>();

//        for(final Iterator<Identity> i = storageRoot.getIdentities().iterator(); i.hasNext(); ) {
//            final Identity id = i.next();
//            if( id == null ) {
//                i.remove();
//                continue;
//            }
        for( final Identity id : Core.getIdentities().getIdentities() ) {
            final int messageCount = MessageStorage.inst().getMessageCount(id.getUniqueName());
            final int fileCount = FileListStorage.inst().getFileCount(id.getUniqueName());
            final IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
            data.put(id.getUniqueName(), s);
        }

//        for(final Iterator<LocalIdentity> i = storageRoot.getLocalIdentities().iterator(); i.hasNext(); ) {
//            final LocalIdentity id = i.next();
//            if( id == null ) {
//                i.remove();
//                continue;
//            }
        for( final LocalIdentity id : Core.getIdentities().getLocalIdentities() ) {
            final int messageCount = MessageStorage.inst().getMessageCount(id.getUniqueName());
            final int fileCount = FileListStorage.inst().getFileCount(id.getUniqueName());
            final IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
            data.put(id.getUniqueName(), s);
        }
        return data;
    }

    public void repairStorage() {

        System.out.println("Repairing identities.dbs (may take some time!)...");

        final String databaseFilePath = buildStoragePath("identities.dbs"); // path to the database file
        final int pagePoolSize = 2*1024*1024;

        open(databaseFilePath, pagePoolSize, true, true, false);

        storageRoot = (IdentitiesStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            System.out.println("No identities.dbs found");
            return;
        }

        int brokenEntries = 0;
        int validEntries = 0;

        final List<Identity> lst = new ArrayList<Identity>();

        final int progressSteps = storageRoot.getIdentities().size() / 75; // all 'progressSteps' entries print one dot
        int progress = progressSteps;

        for( int x=0; x < storageRoot.getIdentities().size(); x++ ) {
            if( x > progress ) {
                System.out.print('.');
                progress += progressSteps;
            }
            Identity sfk;
            try {
                sfk = storageRoot.getIdentities().get(x);
            } catch(final Throwable t) {
                brokenEntries++;
                continue;
            }
            if( sfk == null ) {
                brokenEntries++;
                continue;
            }
            validEntries++;
            lst.add(sfk);
        }

        storageRoot.getIdentities().clear();
        commit();

        for( final Identity sfk : lst ) {
            storageRoot.getIdentities().add(sfk);
        }
        commit();

        close();
        storageRoot = null;

        System.out.println();
        System.out.println("Repair finished, brokenEntries="+brokenEntries+"; validEntries="+validEntries);
    }
}
