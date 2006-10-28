/**
 * 
 */
package frost.hyperocha;

import frost.Core;
import frost.fileTransfer.upload.FrostUploadItem;
import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.dispatcher.job.CHKFileInsertJob;

import java.io.File;
import java.util.Hashtable;

/**
 * @author saces
 *
 */
public class FrostCHKFileInsertJob extends CHKFileInsertJob {
	
	private FrostUploadItem uploadItem = null; 
	
	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileInsertJob(File uploadfile) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), uploadfile);
	}

	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileInsertJob(FrostUploadItem uitem) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), uitem.getFile());
		uploadItem = uitem;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.CHKFileInsertJob#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incommingMessage(String id, Hashtable message) {
		if ("SimpleProgress".equals(message.get(FCPConnection.MESSAGENAME))) {
			// no DownloadItem set? we are not intrested in progress
			if (uploadItem == null) { return; }
			
			uploadItem.setTotalBlocks(Integer.parseInt((String)message.get("Total")));
			uploadItem.setDoneBlocks(Integer.parseInt((String)message.get("Succeeded")));
			
			//System.out.println("SP" + this + message);
			return;
        }
		if ("PutFetchable".equals(message.get(FCPConnection.MESSAGENAME))) {
			//System.out.println("PutFetchable" + this + message);
			if (uploadItem == null) { return; }
			uploadItem.setKey((String)message.get("URI"));
			// dont return, super the putfetchable;
		}
		// not a simple progress, leave default is the best one atm <g>
		super.incommingMessage(id, message);
	}



}
