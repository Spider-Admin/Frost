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
	
	private boolean newState;
	private String nid;

	/**
	 * 
	 */
	public NetworkStateEvent(Dispatcher d, String id, boolean newstate) {
		super(d);
		newState = newstate;
		nid = id;
	}

	/**
	 * @return the newState
	 */
	public boolean getNewState() {
		return newState;
	}
	
	/**
	 * @return the netwirk id
	 */
	public String getNetworkID() {
		return nid;
	}

}
