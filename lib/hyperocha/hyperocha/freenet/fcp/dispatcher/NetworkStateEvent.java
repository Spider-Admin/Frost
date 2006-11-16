/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import java.util.EventObject;



/**
 * @author saces
 *
 */
public class NetworkStateEvent extends EventObject {
	
	private int newState;
	private String nid;

	/**
	 * 
	 */
	public NetworkStateEvent(Dispatcher d, String id, int newstate) {
		super(d);
		newState = newstate;
		nid = id;
	}

	/**
	 * @return the newState
	 */
	public int getNewState() {
		return newState;
	}
	
	/**
	 * @return the netwirk id
	 */
	public String getNetworkID() {
		return nid;
	}

}
