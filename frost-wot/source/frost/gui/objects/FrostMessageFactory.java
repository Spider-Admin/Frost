package frost.gui.objects;

import java.io.File;
import java.util.WeakHashMap;
import java.util.logging.*;

import org.shiftone.cache.*;

/**
 * This factory class is used to create new instances of FrostMessageObject. It
 * uses two caches:
 * 
 * - The first of them is implemented using jocache and its configuration is 
 *   established here and in the file called cache.properties. The replacement
 *   algorithm used is fifo, the size is 500 items and there is no timeout. 
 *   It is used to keep those instances of FrostMessageObject that have been
 *   created recently so that we don't have to create them again (which currently 
 *   implies reading a XML file from disk and parsing it).
 * 
 * - The second of them is used to canonicalize the instances of FrostMessageObject. It
 *   uses weak references so that its use doesn't imply an increased memory usage.
 * 
 * There is a single instance of the class <code>FrostMessageFactory</code>,
 * accessed through the static method {@link #getInstance()}.
 *
 * @pattern Singleton
 *
 * @author Administrator
 *
 * @version $Revision$
 */
public class FrostMessageFactory {

	private static final String CACHE_NAME = "fifo";
	private static final int CACHE_SIZE = 500;	//TODO: Add this value to the preferences
	private static final int CACHE_TIMEOUT = Integer.MAX_VALUE;

	private static Logger logger = Logger.getLogger(FrostMessageFactory.class.getName());

	private Cache cache = null;
	
	// This cache is used to canonicalize instances of FrostMessageObject
	private WeakHashMap canonicalCache = new WeakHashMap();

	/**
	 * The unique instance of this class.
	 */
	private static FrostMessageFactory instance = null;

	/**
	 * Prevent instances of this class from being created.
	 */
	private FrostMessageFactory() {
		try {
			cache =
				new CacheConfiguration().createConfiguredCache(
					CACHE_NAME,
					CACHE_TIMEOUT,
					CACHE_SIZE);
		} catch (Exception exception) {
			logger.log(
				Level.SEVERE,
				"Error while creating the object cache. Object caching will not be used.",
				exception);
		}
	}

	/**
	 * Return the unique instance of this class.
	 *
	 * @return the unique instance of this class
	 */
	public static FrostMessageFactory getInstance() {
		if (instance == null) {
			instance = new FrostMessageFactory();
		}
		return instance;
	}

	/**
	 * This method creates a new instance of FrostMessageObject with the information found
	 * in the file that is passed as a parameter.
	 * @param messageFile the file whose information will be used to create the new FrostMessageObject
	 * @return the requested FrostMessageObject
	 * @throws Exception if something went wrong while obtaining the FrostMessageObject
	 */
	public static FrostMessageObject createFrostMessageObject(File messageFile) throws Exception {
		return getInstance().innerCreateFrostMessageObject(messageFile);
	}

	/**
	 * This method checks both caches (the canonical one and the jocache one) and if the requested 
	 * FrostMessageObject is found in one them, that instance is returned. If it wasn't found, a new instance
	 * is created and returned. 
	 * @param messageFile the file whose information will be used to create the new FrostMessageObject
	 * @return the requested FrostMessageObject
	 * @throws Exception if something went wrong while obtaining the FrostMessageObject
	 */
	private FrostMessageObject innerCreateFrostMessageObject(File messageFile) throws Exception {
		if (cache != null) {
			// The cache exists.
			Object messageObject = cache.getObject(messageFile);
			if (messageObject != null) {
				// The message was in the cache.
				return (FrostMessageObject) messageObject;
			} else {
				// The message was not in the cache. We look in the canonical cache.
				messageObject = canonicalCache.get(messageFile);
				if (messageObject != null) {
					// The message was in the canonical cache
					return (FrostMessageObject) messageObject;
				} else {
					// The message was not in the caches. We create a new one.
					messageObject = new FrostMessageObject(messageFile);
					cache.addObject(messageFile, messageObject);
					canonicalCache.put(messageFile, messageObject);
					return (FrostMessageObject) messageObject;
				}
			}
		} else {
			// The cache doesn't exist.
			Object messageObject = canonicalCache.get(messageFile);
			if (messageObject != null) {
				// The message was in the canonical cache.
				return (FrostMessageObject) messageObject;
			} else {
				// The message was not in the canonical cache. We create a new one.
				messageObject = new FrostMessageObject(messageFile);
				canonicalCache.put(messageFile, messageObject);
				return (FrostMessageObject) messageObject;
			}
		}
	}

}
