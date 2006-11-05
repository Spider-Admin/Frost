/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import java.util.EventObject;



/**
 * @author saces
 *
 */
public class NodeStateEvent extends EventObject {
	
	private boolean newState;
	private String nid;

	/**
	 * 
	 */
	public NodeStateEvent(Dispatcher d, String id, boolean newstate) {
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
	 * @return the node id
	 */
	public String getNodeID() {
		return nid;
	}

}
