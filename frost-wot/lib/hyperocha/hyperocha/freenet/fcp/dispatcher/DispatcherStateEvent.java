/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import java.util.EventObject;



/**
 * @author saces
 *
 */
public class DispatcherStateEvent extends EventObject {
	
	private int newState;

	/**
	 * 
	 */
	public DispatcherStateEvent(Dispatcher d, int newstate) {
		super(d);
		newState = newstate;
	}

	/**
	 * @return the newState
	 */
	public int getNewState() {
		return newState;
	}

}
