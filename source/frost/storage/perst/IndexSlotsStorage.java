/*
  GlobalIndexSlotsStorage.java / Frost
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
import org.joda.time.*;

import frost.storage.*;

/**
 * Storage with an compound index of indexName and msgDate (int/long)
 */
public class IndexSlotsStorage implements Savable {

//    private static final Logger logger = Logger.getLogger(IndexSlotsStorage.class.getName());

    // boards have positive indexNames (their primkey)
    public static final int FILELISTS = -1;
    public static final int REQUESTS  = -2;

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB

    private Storage storage = null;
    private IndexSlotsStorageRoot storageRoot = null;

    private static IndexSlotsStorage instance = new IndexSlotsStorage();

    protected IndexSlotsStorage() {
    }

    public static IndexSlotsStorage inst() {
        return instance;
    }

    private Storage getStorage() {
        return storage;
    }

    private boolean addToIndices(final IndexSlot gis) {
        if( getStorage() == null ) {
            return false;
        }
        final boolean wasOk = storageRoot.slotsIndexIL.put(new Key(gis.getIndexName(), gis.getMsgDate()), gis);
        storageRoot.slotsIndexLI.put(new Key(gis.getMsgDate(), gis.getIndexName()), gis);
        return wasOk;
    }

    public boolean initStorage() {
        final String databaseFilePath = "store/gixSlots.dbs"; // path to the database file
        final int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.serialize.transient.objects", Boolean.TRUE); // serialize BitSets
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // remove() during iteration (for cleanup)
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (IndexSlotsStorageRoot)storage.getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new IndexSlotsStorageRoot();
            // unique compound index of indexName and msgDate
            storageRoot.slotsIndexIL = storage.createIndex(new Class[] { Integer.class, Long.class }, true);
            // index for cleanup
            storageRoot.slotsIndexLI = storage.createIndex(new Class[] { Long.class, Integer.class }, true);
            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    /**
     * Deletes any items with a date < maxDaysOld
     */
    public int cleanup(final int maxDaysOld) {

        // millis before maxDaysOld days
        final long date = new LocalDate().minusDays(maxDaysOld + 1).toDateTimeAtMidnight(DateTimeZone.UTC).getMillis();

        // delete all items with msgDate < maxDaysOld
        int deletedCount = 0;

        final Iterator<IndexSlot> i = storageRoot.slotsIndexLI.iterator(
                new Key(Long.MIN_VALUE, Integer.MIN_VALUE, true),
                new Key(date, Integer.MAX_VALUE, true),
                Index.ASCENT_ORDER);

        while(i.hasNext()) {
            final IndexSlot gis = i.next();
            storageRoot.slotsIndexIL.remove(gis); // also remove from IL index
            i.remove(); // remove from iterated LI index
            gis.deallocate(); // remove from Storage
            deletedCount++;
        }

        commitStore();

        return deletedCount;
    }

    public synchronized IndexSlot getSlotForDate(final int indexName, final long date) {
        final Key dateKey = new Key(indexName, date);
        IndexSlot gis = storageRoot.slotsIndexIL.get(dateKey);
//        String s = "";
//        s += "getSlotForDate: indexName="+indexName+", date="+date+"\n";
        if( gis == null ) {
            // not yet in storage
            gis = new IndexSlot(indexName, date);
//            s += "getSlotForDate: NEW SLOT CREATED!\n";
        }
//        logger.warning(s);
        return gis;
    }

    public synchronized void storeSlot(final IndexSlot gis) {
        if( getStorage() == null ) {
            return;
        }
        if( gis.getStorage() == null ) {
            gis.makePersistent(getStorage());
            addToIndices(gis);
        } else {
            gis.modify();
        }
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
        System.out.println("INFO: GlobalIndexSlotsStorage closed.");
    }

    // tests
//    public static void main(String[] args) {
//        IndexSlotsStorage s = IndexSlotsStorage.inst();
//
//        s.initStorage();
//
//        IndexSlotsStorageRoot root = (IndexSlotsStorageRoot)s.getStorage().getRoot();
//
//        for( Iterator<IndexSlot> i = root.slotsIndexIL.iterator(); i.hasNext(); ) {
//            IndexSlot gi = i.next();
//            System.out.println("----GI-------");
//            System.out.println(gi);
//        }
//
//        s.getStorage().close();
//    }
}
