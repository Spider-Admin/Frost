/**
 * 
 */
package hyperocha.freenet.fcp;

/**
 * @version $Id$
 * @author saces
 */
public interface IIncoming {
	public void incomingMessage(String id, NodeMessage msg);
	public void incomingData(String id, NodeMessage msg, FCPConnection conn);
}
