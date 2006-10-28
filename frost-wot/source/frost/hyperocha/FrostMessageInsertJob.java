/**
 * 
 */
package frost.hyperocha;

import java.io.File;

import frost.Core;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.job.KSKMessageUploadJob;

/**
 * @author saces
 *
 */
public class FrostMessageInsertJob extends KSKMessageUploadJob {

	/**
	 * @param requirednetworktype
	 */
	public FrostMessageInsertJob(String targetKey, File insertFile) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), insertFile, string2key(targetKey));
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
