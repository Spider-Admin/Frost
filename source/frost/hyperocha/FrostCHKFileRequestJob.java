/*
  FrostCHKFileRequestJob.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.hyperocha;

import frost.Core;
import frost.fileTransfer.download.FrostDownloadItem;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.job.CHKFileRequestJob;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * @version $Id$
 */
public class FrostCHKFileRequestJob extends CHKFileRequestJob {
	
    // FIXME: overwrite doPrepare(), jobStarted(), jobFinished()

    private static Logger logger = Logger.getLogger(FrostCHKFileRequestJob.class.getName());

	private FrostDownloadItem dlItem = null;

	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileRequestJob(String key, File target ) {
		this(key, target, null);
	}
		
	public FrostCHKFileRequestJob(String key, File target, FrostDownloadItem dli) {	
		super(Core.getFcpVersion(), makeID(dli), FreenetKey.CHKfromString(key), target);
		dlItem = dli;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.CHKFileDownoadJob#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingMessage(String id, NodeMessage msg) {
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
        
        // we don't want to die for any reason here...
//        try {
//            if (msg.isMessageName("SimpleProgress")) {
//                // no DownloadItem set? we are not intrested in progress
//                if (dlItem == null) { return; }
//                
//                // the doc says this is right:
//                // don't belive this value before FinalizedTotal=true
//                final boolean isFinalized = msg.getBoolValue("FinalizedTotal");
//                final int totalBlocks = (int)msg.getLongValue("Total");
//                final int requiredBlocks = (int)msg.getLongValue("Required");
//                final int doneBlocks = (int)msg.getLongValue("Succeeded");
//                
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        dlItem.setFinalized(isFinalized);
//                        dlItem.setTotalBlocks(totalBlocks);
//                        dlItem.setRequiredBlocks(requiredBlocks);          
//                        dlItem.setDoneBlocks(doneBlocks);
//
//                        dlItem.fireValueChanged();
//                    }
//                });
//            }
//        } catch(Throwable t) {
//            logger.log(Level.SEVERE, "Exception catched", t);
//        }
        
        super.incomingMessage(id, msg);
	}
	
	private static String makeID(FrostDownloadItem dlItem) {
		if (dlItem == null) {
			return  FHUtil.getNextJobID();
		}
		return dlItem.getGqIdentifier();
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#onSimpleProgress(boolean, long, long, long, long)
	 */
	public void onSimpleProgress(boolean isFinalized, long totalBlocks, long requiredBlocks, long doneBlocks, long failedBlocks, long fatallyFailedBlocks) {
		 dlItem.setFinalized(isFinalized);
         dlItem.setTotalBlocks((int)totalBlocks);
         dlItem.setRequiredBlocks((int)requiredBlocks);          
         dlItem.setDoneBlocks((int)doneBlocks);

         dlItem.fireValueChanged();
	}
}
