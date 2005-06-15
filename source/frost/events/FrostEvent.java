/*
 * Created on 16-jun-2005
 * 
 */
package frost.events;

public class FrostEvent {

	public static final int STORAGE_ERROR_EVENT_ID = 0; 
	
	private int id;

	/**
	 * @param id
	 */
	public FrostEvent(int id) {
		this.id = id;
	}
	
	/**
	 * @return
	 */
	public int getId() {
		return id;
	}
	
}
