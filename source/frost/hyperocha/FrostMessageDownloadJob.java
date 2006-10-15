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
	public FrostMessageDownloadJob(String key, File dest) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), string2key(key), dest);
	}
	
	private static FreenetKey string2key(String uri) {
		FreenetKey key;
		if (Core.getFcpVersion() == Network.FCP2) {
			key = FreenetKey.KSKfromString(FHUtil.StripSlashes(uri));
		} else {
			key = FreenetKey.KSKfromString(uri);
		}
		return key;
	}
}
