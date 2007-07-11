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
import frost.util.baysian.*;

public class BayesianWordsStorage implements Savable {

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
    
    private Storage getStorage() {
        return storage;
    }
    
    public boolean initStorage() {
        String databaseFilePath = "store/bayesianWords.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // remove() during iteration (for cleanup)
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (BayesianWordsStorageRoot)storage.getRoot();
        if (storageRoot == null) { 
            // Storage was not initialized yet
            storageRoot = new BayesianWordsStorageRoot();
            
            storageRoot.bayesianWords = storage.createScalableList();

            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public synchronized void commitStore() {
        getStorage().commit();
    }

    public void save() throws StorageException {
        saveBayesianWords(FrostBayesianFilter.inst().getWords());
        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: BayesianWordsStorage closed.");
    }
    
    /**
     * Removes all items from the given List and deallocates each item from Storage.
     * @param plst  IPersistentList of persistent items
     */
    private void removeAllFromStorage(IPersistentList<? extends Persistent> plst) {
        for(Iterator<? extends Persistent> i=plst.iterator(); i.hasNext(); ) {
            Persistent pi = (Persistent)i.next();
            i.remove(); // remove from List
            pi.deallocate(); // remove from Storage
        }
    }

    public void saveBayesianWords(Collection<WordProbability> bayesianWords) {

        removeAllFromStorage(storageRoot.bayesianWords);
        int count = 0;
        for(Iterator<WordProbability> i=bayesianWords.iterator(); i.hasNext(); ) {
            
            WordProbability wp = i.next();

            PerstBayesianWordProbability pbwp = new PerstBayesianWordProbability();
            pbwp.word = wp.getWord();
            pbwp.matchingCount = wp.getMatchingCount();
            pbwp.nonMatchingCount = wp.getNonMatchingCount();
            
            pbwp.makePersistent(storage);
            pbwp.modify(); // for already persistent items
            
            storageRoot.bayesianWords.add(pbwp);
            
            count++;
        }
        
        storageRoot.bayesianWords.modify();
        
        storage.commit();
        
        System.out.println("Bayesian filter: Saved "+count+" words.");
    }
    
    public void loadBayesianWords(SimpleWordsDataSource swds) {

        int count = 0;
        for(Iterator<PerstBayesianWordProbability> i=storageRoot.bayesianWords.iterator(); i.hasNext(); ) {

            PerstBayesianWordProbability pi = i.next();
            
            WordProbability wp = new WordProbability(pi.word, pi.matchingCount, pi.nonMatchingCount);
            swds.setWordProbability(wp);
            count++;
        }
        System.out.println("Bayesian filter: Loaded "+count+" words.");
    }
}
