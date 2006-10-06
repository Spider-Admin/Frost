/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.FreenetKey;


/**
 * @author saces
 *
 */
public class GenerateSSKJob extends Job {
	

	/**
	 * 
	 */
	public GenerateSSKJob(int requirednetworktype, String id) {
		super(requirednetworktype, id);
	}
	
	public FreenetKey getKey() {
		return null;
	}

	public boolean doPrepare() {
		return true;
	}
}
