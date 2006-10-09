/**
 * 
 */
package frost.hyperocha;

import java.io.File;

import frost.Core;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.job.KSKMessageDownloadJob;

/**
 * @author saces
 *
 */
public class FrostMessageDownloadJob extends KSKMessageDownloadJob {

	/**
	 * @param requirednetworktype
	 */
	private FrostMessageDownloadJob(FreenetKey key, File dest) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), key, dest);
	}
	
	
	public static FrostMessageDownloadJob makeFrostMessageDownloadJob(String uri, File dest) {
		FreenetKey key;
		if (Core.getFcpVersion() == Network.FCP2) {
			key = FreenetKey.KSKfromString(FHUtil.StripSlashes(uri));
		} else {
			key = FreenetKey.KSKfromString(uri);
		}
		return new FrostMessageDownloadJob(key, dest);
	}
}
