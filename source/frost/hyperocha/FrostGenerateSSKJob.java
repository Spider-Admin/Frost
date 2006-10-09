/**
 * 
 */
package frost.hyperocha;

import frost.Core;
import hyperocha.freenet.fcp.dispatcher.job.GenerateSSKJob;

/**
 * @author saces
 *
 */
public class FrostGenerateSSKJob extends GenerateSSKJob {

	/**
	 * @param requirednetworktype
	 */
	public FrostGenerateSSKJob() {
		super(Core.getFcpVersion(), FHUtil.getNextJobID());
	}

}
