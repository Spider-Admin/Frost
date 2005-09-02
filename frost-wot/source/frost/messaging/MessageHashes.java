/*
 * Created on 26-ene-2005
 * 
 */
package frost.messaging;

import java.util.*;
import java.util.logging.Logger;

import frost.storage.*;

/**
 * This class contains the hashes of all the messages. It is used to check
 * if a message is a duplicate of those we already have a local copy of.
 * Each digest has a timestamp, when digests are saved we don't save expired digests.
 * @author $Author$
 * @version $Revision$
 */
public class MessageHashes implements Savable {

	private static Logger logger = Logger.getLogger(MessageHashes.class.getName());

    // key is digest, value is a Long with timestamp of this digest (when it was added)
    private Hashtable hashesTable = new Hashtable();

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
		synchronized (hashesTable) {
			hashesDAO.save(this);
		}
	}
	
	/**
	 * This method adds the given digest to the set of message hashes. Its
	 * implementation is thread safe.
	 * @param digest the new digest to add to the set of message hashes.
     * @param timestamp the timestamp of the digest.
	 * @return true if this set did not already contain the specified digest.
	 */
	public boolean add(String digest, long timeStamp) {

        boolean wasNotAlreadyContained;
        if( hashesTable.put(digest, new Long(timeStamp)) == null ) {
            wasNotAlreadyContained = true;
        } else {
            wasNotAlreadyContained = false;
        }
		return wasNotAlreadyContained;
	}

    /**
     * This method adds the given digest to the set of message hashes. Its
     * implementation is thread safe.
     * The timestamp of the new digest is set to current time.
     * @param digest the new digest to add to the set of message hashes.
     * @return true if this set did not already contain the specified digest.
     */
    public boolean add(String digest) {
        return add(digest, System.currentTimeMillis());
    }

	/**
	 * This method returns true if the set of message hashes contains the 
	 * digest given as a paremeter. Its implementation is thread safe.
	 * @param digest digest whose presence in this set is to be tested
	 * @return true if this set contains the specified digest.
	 */
	public boolean contains(String digest) {
		boolean result;
        if( hashesTable.get(digest) != null ) {
            result = true;
        } else {
            result = false;
        }
		return result;
	}
	/**
	 * This method returns an Iterator with all of the message
	 * hashes. Not thread-safe.
	 * @return an Iterator with all of the message hashes.
	 */
	protected Iterator getHashes() {
		return hashesTable.keySet().iterator();
	}
	/**
     * Returns the timestamp for the provided digest. 
     * @param digest  the digest to find the timestamp for
     * @return  the timestamp of this digest or 0
	 */
    protected long getTimestampForDigest(String digest) {
        Long value = (Long)hashesTable.get(digest);
        if( value == null ) {
            return 0; 
        } else {
            return value.longValue();
        }
    }
}
