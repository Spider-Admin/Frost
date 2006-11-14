/**
 * 
 */
package frost.hyperocha;

import hyperocha.freenet.fcp.dispatcher.job.GlobalQueueJob;

/**
 * @author 
 *
 */
public class FrostGlobalQueueJob extends GlobalQueueJob {

	/**
	 * @param nodeid
	 */
	public FrostGlobalQueueJob(String nodeid) {
		super(nodeid);
	}
	
	public boolean doPrepare() {
		// TODO check the nodeid is in config?
		return true;
	}

}
