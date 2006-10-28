/**
 * 
 */
package frost.hyperocha;

import frost.*;
import frost.fileTransfer.download.*;
import hyperocha.freenet.fcp.*;
import hyperocha.freenet.fcp.dispatcher.job.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

/**
 * @author saces
 *
 */
public class FrostCHKFileDownloadJob extends CHKFileDownoadJob {
	
    private static Logger logger = Logger.getLogger(FrostCHKFileDownloadJob.class.getName());

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
	public void incommingMessage(String id, Hashtable message) {
		//System.out.println(" -> " + this + " -> " + message);
        
        // Sample message:
//        SimpleProgress
//        Total=12288 // 12,288 blocks we can fetch
//        Required=8192 // we only need 8,192 of them (because of splitfile redundancy)
//        Failed=452 // 452 of them have failed due to running out of retries
//        FatallyFailed=0 // none of them have encountered fatal errors
//        Succeeded=1027 // we have successfully fetched 1,027 blocks
//        FinalizedTotal=true // the Total will not increase any further (if this is false, it may increase; it will never decrease)
//        Identifier=Request Number One
//        EndMessage
        
        // first let super do its work (we hope it throws nothing to us)
        super.incommingMessage(id, message);

        // we don't want to die for any reason here...
        try {
            if ("SimpleProgress".equals(message.get(FCPConnection.MESSAGENAME))) {
                // no DownloadItem set? we are not intrested in progress
                if (dlItem == null) { return; }
                
                // the doc says this is right:
                // don't belive this value before FinalizedTotal=true
                String bolMsg = (String)message.get("FinalizedTotal");
                boolean isFinalized;
                if( bolMsg != null && bolMsg.trim().equalsIgnoreCase("true") ) {
                    isFinalized = true;
                } else {
                    isFinalized = false;
                }
                dlItem.setFinalized(isFinalized);
                
                int totalBlocks = Integer.parseInt((String)message.get("Total"));
                dlItem.setTotalBlocks(totalBlocks);
                
                int requiredBlocks = Integer.parseInt((String)message.get("Required"));
                dlItem.setRequiredBlocks(requiredBlocks);          

                int doneBlocks = Integer.parseInt((String)message.get("Succeeded"));
                dlItem.setDoneBlocks(doneBlocks);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dlItem.fireValueChanged();
                    }
                });
            }
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception catched", t);
        }
	}
}
