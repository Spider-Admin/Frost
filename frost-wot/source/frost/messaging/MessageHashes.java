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
 * @author $Author$
 * @version $Revision$
 */
public class MessageHashes implements Savable {

	private static Logger logger = Logger.getLogger(MessageHashes.class.getName());

	private Set hashesSet = new HashSet(); // set of message digests

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
		synchronized (hashesSet) {
			hashesDAO.save(this);
		}
	}
	
	/**
	 * This method adds the given digest to the set of message hashes. Its
	 * implementation is thread safe.
	 * @param digest the new digest to add to the set of message hashes.
	 * @return true if this set did not already contain the specified digest.
	 */
	public boolean add(String digest) {
		boolean result;
		synchronized (hashesSet) {
			result = hashesSet.add(digest);
		}
		return result;
	}
	
	/**
	 * This method returns true if the set of message hashes contains the 
	 * digest given as a paremeter. Its implementation is thread safe.
	 * @param digest digest whose presence in this set is to be tested
	 * @return true if this set contains the specified digest.
	 */
	public boolean contains(String digest) {
		boolean result;
		synchronized (hashesSet) {
			result = hashesSet.contains(digest);
		}
		return result;
	}
	/**
	 * This method returns an Iterator with all of the message
	 * hashes. Not thread-safe.
	 * @return an Interator with all of the message hashes.
	 */
	protected Iterator getHashes() {
		return hashesSet.iterator();
	}
}
