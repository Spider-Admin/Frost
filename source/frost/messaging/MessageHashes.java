/*
 * Created on 26-ene-2005
 * 
 */
package frost.messaging;

import java.io.*;
import java.util.*;
import java.util.HashSet;
import java.util.logging.*;
import java.util.logging.Level;

import frost.storage.*;
import frost.storage.Savable;

/**
 * This class contains the hashes of all the messages. It is used to check
 * if a message is a duplicate of those we already have a local copy of.
 * @author $Author$
 * @version $Revision$
 */
public class MessageHashes implements Savable {

	private static Logger logger = Logger.getLogger(MessageHashes.class.getName());

	private static Set hashesSet = new HashSet(); // set of message digests

	/**
	 * This method initializes the instance of MessageHashes and reads its contents
	 * from disk.
	 * @throws StorageException if there was any error while initializing the instance.
	 */
	public void initialize() throws StorageException {
		File hashes = new File("hashes");
		if (hashes.exists())
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(hashes));
				hashesSet = (HashSet) ois.readObject();
				logger.info("loaded " + hashesSet.size() + " message hashes");
				ois.close();
			} catch (Throwable t) {
				throw new StorageException("Error while loading the Message Hashes:\n" + t.getMessage());
			}
	}

	/**
	 * This method saves to disk the contents of the instance of MessageHashes
	 * @throws StorageException if there was any error while saving the contents.
	 */
	public void save() throws StorageException {
		try {
			File hashes = new File("hashes");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(hashes));
			synchronized (hashesSet) {
				oos.writeObject(hashesSet);
			}
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Exception thrown in saveHashes()", t);
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
}
