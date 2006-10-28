/**
 * 
 */
package hyperocha.freenet.fcp;

import java.util.Hashtable;

/**
 * @author saces
 *
 */
public interface IIncomming {
	public void incommingMessage(String id, Hashtable message);
	public void incommingData(String id, Hashtable message, FCPConnection conn);
}
