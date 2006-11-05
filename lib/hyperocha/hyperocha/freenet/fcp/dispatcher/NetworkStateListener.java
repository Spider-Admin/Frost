/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import java.util.EventListener;

/**
 * @author saces
 *
 */
public interface NetworkStateListener extends EventListener {
	void stateChanged(NetworkStateEvent e);
}
