/*
 * Created on 16-jun-2005
 * 
 */
package frost.events;

/**
 * @author $Author$
 * @version $Revision$
 */
public interface FrostEventDispatcher {

	/**
	 * @param frostEvent
	 */
	void dispatchEvent(FrostEvent frostEvent);

}
