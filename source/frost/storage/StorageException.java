/*
 * Created on 23-nov-2004
 * 
 */
package frost.storage;

/**
 * @author $author$
 * @version $revision$
 */
public class StorageException extends Exception {

	/**
	 * 
	 */
	public StorageException() {
		super();
	}
	/**
	 * @param message
	 */
	public StorageException(String message) {
		super(message);
	}
	/**
	 * @param message
	 * @param cause
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param cause
	 */
	public StorageException(Throwable cause) {
		super(cause);
	}
}
