/**
 * 
 */
package frost.hyperocha;

import frost.Core;
import hyperocha.freenet.fcp.dispatcher.job.KSKMessageUploadJob;

/**
 * @author saces
 *
 */
public class FrostMessageInsertJob extends KSKMessageUploadJob {

	/**
	 * @param requirednetworktype
	 */
	public FrostMessageInsertJob() {
		super(Core.getFcpVersion(), FHUtil.getNextJobID());
	}



}
