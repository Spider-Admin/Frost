/**
 * 
 */
package frost.hyperocha;

import frost.Core;
import frost.fileTransfer.download.FrostDownloadItem;
import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.dispatcher.job.CHKFileDownoadJob;

import java.io.File;
import java.util.Hashtable;

/**
 * @author saces
 *
 */
public class FrostCHKFileDownloadJob extends CHKFileDownoadJob {
	
	private FrostDownloadItem dlItem = null;

	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileDownloadJob(String key, File target ) {
		this(key, target, null);
	}
		
	public FrostCHKFileDownloadJob(String key, File target, FrostDownloadItem dli) {	
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), FreenetKey.CHKfromString(key), target);
		dlItem = dli;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.CHKFileDownoadJob#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incommingMessage(FCPConnection conn, Hashtable message) {
		if ("SimpleProgress".equals(message.get(FCPConnection.MESSAGENAME))) {
			// no DownloadItem set? we are not intrested in progress
			if (dlItem == null) { return; }
			
			// the doc says this is right:
			// don't belive this value before FinalizedTotal=true
			if ("true".equals(message.get("FinalizedTotal"))) {
				dlItem.setTotalBlocks(Integer.parseInt((String)message.get("Total")));
			}
			dlItem.setDoneBlocks(Integer.parseInt((String)message.get("Succeeded")));
			// add as neccessary or wanted ;)
			
			// invoke later? 
			dlItem.fireValueChanged();
			return;
        }
		// not a simple progress, leave default is the best one atm <g>
		super.incommingMessage(conn, message);
	}
}
