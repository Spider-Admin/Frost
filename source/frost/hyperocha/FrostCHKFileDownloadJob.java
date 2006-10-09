/**
 * 
 */
package frost.hyperocha;

import frost.Core;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.dispatcher.job.CHKFileDownoadJob;

import java.io.File;

/**
 * @author saces
 *
 */
public class FrostCHKFileDownloadJob extends CHKFileDownoadJob {

	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileDownloadJob(String key, File target) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), FreenetKey.CHKfromString(key), target);
		// TODO Auto-generated constructor stub
	}
}
