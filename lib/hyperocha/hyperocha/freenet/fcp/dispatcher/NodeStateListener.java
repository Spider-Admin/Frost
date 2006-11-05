/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import java.util.EventListener;

/**
 * @author saces
 *
 */
public interface NodeStateListener extends EventListener {
	void stateChanged(NodeStateEvent e);
}
