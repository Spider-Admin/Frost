/**
 * 
 */
package hyperocha.freenet.fcp;

import java.util.Hashtable;

/**
 * @author saces
 */
public interface IIncoming {
	public void incomingMessage(String id, Hashtable message);
	public void incomingData(String id, Hashtable message, FCPConnection conn);
}
