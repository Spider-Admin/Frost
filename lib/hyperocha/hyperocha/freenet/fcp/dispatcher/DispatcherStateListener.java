/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import java.util.EventListener;

/**
 * @author saces
 *
 */
public interface DispatcherStateListener extends EventListener {
	void stateChanged(DispatcherStateEvent e);
}
