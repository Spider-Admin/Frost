/*
 MessageHashes.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging;

import java.util.*;

import frost.storage.*;

/**
 * This class contains the hashes of all the messages. It is used to check
 * if a message is a duplicate of those we already have a local copy of.
 * Class maintains a maximum of MAX_HASHES digests, eldest entries are
 * removed if a new entry is added.
 */
public class MessageHashes implements Savable {

//  private static final Logger logger = Logger.getLogger(MessageHashes.class.getName());

    private final static int MAX_HASHES = 3000; // 30 new files for 100 boards per day, is this enough?

    private final OwnLinkedHashMap hashesMap = new OwnLinkedHashMap(); // uses insertion order, load/save care about this

    private class OwnLinkedHashMap extends LinkedHashMap {
        public OwnLinkedHashMap() {
            super(MAX_HASHES); // initialCapacity
        }
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_HASHES; // let remove eldest if list is full
        }
    }

    /**
     * This method initializes the instance of MessageHashes and reads its contents
     * from disk.
     * @throws StorageException if there was any error while initializing the instance.
     */
    public void initialize() throws StorageException {
        MessageHashesDAO hashesDAO = DAOFactory.getFactory(DAOFactory.XML).getMessageHashesDAO();
        if (!hashesDAO.exists()) {
            // The storage doesn't exist yet. We create it.
            hashesDAO.create();
        } else {
            // Storage exists. Load from it.
            hashesDAO.load(this);
        }
    }

    /**
     * This method saves to disk the contents of the instance of MessageHashes.
     * Its implementation is thread safe.
     * @throws StorageException if there was any error while saving the contents.
     */
    public void save() throws StorageException {
        MessageHashesDAO hashesDAO = DAOFactory.getFactory(DAOFactory.XML).getMessageHashesDAO();
        synchronized (hashesMap) {
            hashesDAO.save(this);
        }
    }

    /**
     * This method adds the given digest to the set of message hashes. Its
     * implementation is thread safe.
     * @param digest the new digest to add to the set of message hashes.
     */
    public synchronized void add(String digest) {
        hashesMap.put(digest, digest);
    }

    /**
     * This method returns true if the set of message hashes contains the
     * digest given as a paremeter. Its implementation is thread safe.
     * @param digest digest whose presence in this set is to be tested
     * @return true if this set contains the specified digest.
     */
    public synchronized boolean contains(String digest) {
        return hashesMap.containsKey(digest);
    }
    /**
     * This method returns an Iterator with all of the message
     * hashes. Not thread-safe.
     * @return an Iterator with all of the message hashes.
     */
    protected Iterator getHashes() {
        return hashesMap.keySet().iterator();
    }
}
