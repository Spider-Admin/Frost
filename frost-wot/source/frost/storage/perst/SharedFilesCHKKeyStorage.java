/*
  SharedFilesCHKKeyStorage.java / Frost
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

import org.garret.perst.*;

import frost.storage.*;
import frost.util.*;

public class SharedFilesCHKKeyStorage implements Savable {

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 2; // page size for the storage in MB

    private Storage storage = null;
    private SharedFilesCHKKeyStorageRoot storageRoot = null;

    private static SharedFilesCHKKeyStorage instance = new SharedFilesCHKKeyStorage();

    protected SharedFilesCHKKeyStorage() {
    }

    public static SharedFilesCHKKeyStorage inst() {
        return instance;
    }
    
    private Storage getStorage() {
        return storage;
    }
    
    private boolean addToIndices(SharedFilesCHKKey gis) {
        return storageRoot.chkKeys.put(gis.getChkKey(), gis);
    }

    public synchronized void storeItem(SharedFilesCHKKey gis) {
        if( gis.getStorage() == null ) {
            gis.makePersistent(getStorage());
            addToIndices(gis);
        } else {
            gis.modify();
        }
    }

    public synchronized void commitStore() {
        getStorage().commit();
    }

    public void save() throws StorageException {
        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: SharedFilesCHKKeyStorage closed.");
    }

    public boolean initStorage() {
        String databaseFilePath = "store/sfChkKeys.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // remove() during iteration (for cleanup)
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (SharedFilesCHKKeyStorageRoot)storage.getRoot();
        if (storageRoot == null) { 
            // Storage was not initialized yet
            storageRoot = new SharedFilesCHKKeyStorageRoot();
            // unique index of chkKeys
            storageRoot.chkKeys = storage.createIndex(String.class, true);
            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }
    
    ////////////////////////////////////////////////////////////
    
    public List<SharedFilesCHKKey> getSharedFilesCHKKeysToSend(int maxKeys) {
        // get a number of CHK keys from database that must be send
        // include (3 to 8) of our new CHK keys into this list, don't send CHK keys of different identities
        // (sending only 1 of our in one list is not needed, because others might pick up more of
        //  our keys also)

        int ownKeysToSend = 3 + (int) (Math.random() * 6); 

        List<SharedFilesCHKKey> keysToSend = new LinkedList<SharedFilesCHKKey>();

        // first search for CHK keys that were created by us, but were never send
        {
            Iterator<SharedFilesCHKKey> i = storageRoot.chkKeys.iterator();
            while(i.hasNext()) {
                SharedFilesCHKKey sfk = i.next();
                if( sfk.getSeenCount() == 0 ) {
                    keysToSend.add(sfk);
                    if( keysToSend.size() >= ownKeysToSend ) {
                        break;
                    }
                }
            }
        }
        
        maxKeys -= keysToSend.size();
        
        // then search for other files to send, but don't include other new files from us
        // - the CHK key must be downloaded already (our new files are not yet downloaded)
        // - key must be valid
        // - keys firstseen must be not earlier than 14 days (don't send old stuff around)
        // - keys lastseen must be more than 24h before (don't send keys we just received)
        // - keys lastsent must be more than 24h before (don't send keys we just sent)
        // - order by seencount asc -> collect keys that are not seen often
        // - collect a maximum of 300 keys
        {
            long now = System.currentTimeMillis();
            long minFirstSeen = now - (14L * 24L * 60L * 60L * 1000L); // now - 14 days
            long maxLastSeen = now - (1L * 24L * 60L * 60L * 1000L); // now - 1 day

            // first collect ALL other keys to send, then sort them and choose maxKeys items
            List<SharedFilesCHKKey> otherKeysToSend = new ArrayList<SharedFilesCHKKey>();
            Iterator<SharedFilesCHKKey> i = storageRoot.chkKeys.iterator();
            while(i.hasNext()) {
                SharedFilesCHKKey sfk = i.next();
                if( sfk.isDownloaded()
                        && sfk.isValid()
                        && sfk.getLastSeen() < maxLastSeen
                        && sfk.getLastSent() < maxLastSeen
                        && sfk.getFirstSeen() > minFirstSeen ) 
                {
                    otherKeysToSend.add(sfk);
                }
            }

            Collections.sort(otherKeysToSend, seenCountComparator);
            
            if( otherKeysToSend.size() > 0 ) {
                keysToSend.addAll( otherKeysToSend.subList(0, Math.min(maxKeys, otherKeysToSend.size())) );
            }
            
            otherKeysToSend.clear();
        }
        return keysToSend;
    }
    
    /**
     * Returns SharedFilesCHKKey for this chkKey or null if not in Storage.
     */
    public SharedFilesCHKKey retrieveSharedFilesCHKKey(String chkKey) {
        return (SharedFilesCHKKey)storageRoot.chkKeys.get(new Key(chkKey));
    }
    
    /** 
     * Retrieves all unretrieved CHK keys to download.
     * Called one time during startup.
     * @return  List of Strings
     */ 
    public List<String> retrieveSharedFilesCHKKeysToDownload(int maxRetries) {

        List<SharedFilesCHKKey> keysToDownload = new ArrayList<SharedFilesCHKKey>();
        
        for(Iterator<SharedFilesCHKKey> i = storageRoot.chkKeys.iterator(); i.hasNext(); ) {
            SharedFilesCHKKey sfk = i.next();
            if( !sfk.isDownloaded()
                    && sfk.getDownloadRetries() < maxRetries)
            {
                keysToDownload.add(sfk);
            }
        }

        Collections.sort(keysToDownload, lastDownloadTryStopTimeComparator);
        
        // now create a list of string only
        List<String> chkKeys = new LinkedList<String>();
        for(Iterator<SharedFilesCHKKey> i=keysToDownload.iterator(); i.hasNext(); ) {
            SharedFilesCHKKey sfk = i.next();
            chkKeys.add( sfk.getChkKey() );
        }
        keysToDownload.clear();

        return chkKeys;
    }

    /**
     * Updates newkey in database.
     */
    public boolean updateSharedFilesCHKKeyAfterDownloadSuccessful(String chkKey, boolean isValid) {
        
        SharedFilesCHKKey key = (SharedFilesCHKKey)storageRoot.chkKeys.get(new Key(chkKey) );
        if( key == null ) {
            return false;
        }
        
        key.setDownloaded(true);
        key.setValid(isValid);
        key.modify();
        
        commitStore();
        return true;
    }
    
    /**
     * Updates newkey in database.
     * @return true if download should be retried
     */
    public boolean updateSharedFilesCHKKeyAfterDownloadFailed(String chkKey, int maxRetries) {

        SharedFilesCHKKey key = (SharedFilesCHKKey)storageRoot.chkKeys.get(new Key(chkKey) );
        if( key == null ) {
            return false;
        }

        key.incDownloadRetries();
        key.setLastDownloadTryStopTime(System.currentTimeMillis());

        key.modify();
        
        commitStore();
        
        if( key.getDownloadRetries() < maxRetries ) {
            return true; // retry download
        } else {
            return false;
        }
    }
    
    /**
     * Delete all table entries that were not seen longer than maxDaysOld.
     * @return  count of deleted rows
     */
    public int cleanupTable(int maxDaysOld) {

        long minVal = System.currentTimeMillis() - ((long)maxDaysOld * 24L * 60L * 60L * 1000L);

        // delete all items with lastSeen < minVal, but lastSeen > 0 
        int deletedCount = 0;
        
        Iterator<SharedFilesCHKKey> i = storageRoot.chkKeys.iterator();
        
        while(i.hasNext()) {
            SharedFilesCHKKey sfk = i.next();
            if( sfk.getLastSeen() > 0 && sfk.getLastSeen() < minVal ) {
                i.remove(); // remove from iterated index
                sfk.deallocate(); // remove from Storage
                deletedCount++;
            }
        }
        
        commitStore();
        
        return deletedCount;
    }
    
//    public static void main(String[] args) {
//        
//        System.out.println("a="+(int) (Math.random() * 5));
//
//        SharedFilesCHKKeyStorage s = new SharedFilesCHKKeyStorage();
//        s.initStorage();
//        
//        // find:
////      String sql = "SELECT chkkey FROM SHAREDFILESCHK WHERE isdownloaded=FALSE AND downloadretries<? " +
////      "ORDER BY lastdownloadtrystop ASC";
//
//        long t1 = System.currentTimeMillis();
//        
//        SharedFilesCHKKeyStorageRoot root = (SharedFilesCHKKeyStorageRoot)s.getStorage().getRoot();
//
//        ArrayList foundItems = new ArrayList();
//        Iterator i = root.chkKeys.iterator();
//        int count = 0;
//        while(i.hasNext()) {
//            SharedFilesCHKKey sfk = (SharedFilesCHKKey)i.next();
//            
//            if(!sfk.isDownloaded() && sfk.getDownloadRetries() < 50) {
//                count++;
////                System.out.println("Found #"+count);
//                foundItems.add(sfk);
//                sfk.modify();
//            }
//        }
//        
//        System.out.println("found: "+count);
//        
////        for(Iterator j=foundItems.iterator(); j.hasNext(); ) {
////            SharedFilesCHKKey k1 = (SharedFilesCHKKey)j.next();
////            System.out.println("->"+k1.getLastDownloadTryStopTime());
////        }
//        System.out.println("sorting!");
//        Collections.sort(foundItems, lastDownloadTryStopTimeComparator);
////        for(Iterator j=foundItems.iterator(); j.hasNext(); ) {
////            SharedFilesCHKKey k1 = (SharedFilesCHKKey)j.next();
////            System.out.println("->"+k1.getLastDownloadTryStopTime());
////        }
//        
//        System.out.println("duration="+(System.currentTimeMillis()-t1));
//        
//        System.out.println("ready");
//        
//        s.getStorage().commit();
//        s.getStorage().close();
//
//    }
    
    protected final static LastDownloadTryStopTimeComparator lastDownloadTryStopTimeComparator = new LastDownloadTryStopTimeComparator();
    protected static class LastDownloadTryStopTimeComparator implements Comparator<SharedFilesCHKKey> {
        public int compare(SharedFilesCHKKey arg0, SharedFilesCHKKey arg1) {
            return Mixed.compareLong(arg0.getLastDownloadTryStopTime(), arg1.getLastDownloadTryStopTime());
        }
    }

    protected final static SeenCountComparator seenCountComparator = new SeenCountComparator();
    protected static class SeenCountComparator implements Comparator<SharedFilesCHKKey> {
        public int compare(SharedFilesCHKKey arg0, SharedFilesCHKKey arg1) {
            return Mixed.compareInt(arg0.getSeenCount(), arg0.getSeenCount());
        }
    }
}
