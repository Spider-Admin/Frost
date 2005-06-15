/*
 * Created on 16-jun-2005
 * 
 */
package frost.events;


/**
 * @author $Author$
 * @version $Revision$
 */
public class StorageErrorEvent extends FrostEvent {

	private String message;
	private Exception exception;
	
	/**
	 * @param message
	 */
	public StorageErrorEvent(String message) {
		super(FrostEvent.STORAGE_ERROR_EVENT_ID);
		this.message = message;
	}
	
	/**
	 * @param exception
	 */
	public void setException(Exception exception) {
		this.exception = exception;
		
	}

	/**
	 * @return
	 */
	public Exception getException() {
		return exception;
	}
	
	/**
	 * @return
	 */
	public String getMessage() {
		return message;	
	}

}
