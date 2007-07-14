/*
  BaysianWordsStorage.java / Frost
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
package frost.storage.perst;

import java.util.*;
import java.util.logging.*;

import net.sf.classifier4J.bayesian.*;

import org.garret.perst.*;

import frost.storage.*;
import frost.util.*;

public class BayesianWordsStorage implements Savable {

    public final static int MAX_TEACHEDMSGIDS_SIZE = 5000;

    private static final Logger logger = Logger.getLogger(BayesianWordsStorage.class.getName());

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB
    
    private Storage storage = null;
    private BayesianWordsStorageRoot storageRoot = null;
    
    private static BayesianWordsStorage instance = new BayesianWordsStorage();

    protected BayesianWordsStorage() {
    }
    
    public static BayesianWordsStorage inst() {
        return instance;
    }
    
    public Storage getStorage() {
        return storage;
    }
    
    public void commitStore() {
        if( getStorage() == null ) {
            return;
        }
        getStorage().commit();
    }
    
    public boolean initStorage() {
        String databaseFilePath = "store/bayesianFilter.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // remove() during iteration (for cleanup)
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (BayesianWordsStorageRoot)storage.getRoot();
        if (storageRoot == null) { 
            // Storage was not initialized yet
            storageRoot = new BayesianWordsStorageRoot();
            
            storageRoot.bayesianWords = storage.createIndex(String.class, true);
            storageRoot.teachedMsgIds = storage.createScalableSet();

            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public void save() throws StorageException {
        getStorage().commit();
        System.out.println("BayesianWordsStorage: bayesianWords in store: "+storageRoot.bayesianWords.size());
        System.out.println("BayesianWordsStorage: teachedMsgIds in store: "+storageRoot.teachedMsgIds.size());
        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: BayesianWordsStorage closed.");
    }
    
    public Index<WordProbability> getBayesianWordsIndex() {
        return storageRoot.bayesianWords;
    }
    
    public IPersistentSet<PerstBayesianTeachedMsgId> getTeachedMsgIdsSet() {
        return storageRoot.teachedMsgIds;
    }
    
    // FIXME: ensure a maximum of X teached ids in store, remove eldest ids
    public void cleanupTeachedMsgIds() {
        int msgIdsToDelete = getTeachedMsgIdsSet().size() - MAX_TEACHEDMSGIDS_SIZE;
        if( msgIdsToDelete > 0 ) {
            
            // sort items by teachDate
            List<PerstBayesianTeachedMsgId> allMsgIds = new ArrayList<PerstBayesianTeachedMsgId>(getTeachedMsgIdsSet());
            Collections.sort(allMsgIds, new TeachDateComparator()); // ascending, oldest first

            for(int i=0; i < msgIdsToDelete; i++) {
                PerstBayesianTeachedMsgId mi = allMsgIds.get(i);
                getTeachedMsgIdsSet().remove(mi);
                mi.deallocate();
            }
            getTeachedMsgIdsSet().modify();
            commitStore();
        }
    }
    
    protected class TeachDateComparator implements Comparator<PerstBayesianTeachedMsgId> {
        public int compare(PerstBayesianTeachedMsgId o1, PerstBayesianTeachedMsgId o2) {
            return Mixed.compareLong(o1.firstTeachDate, o2.firstTeachDate);
        }
    }
}
