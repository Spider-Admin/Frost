/*
  FrostCHKFileInsertJob.java / Frost
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
import frost.fileTransfer.upload.FrostUploadItem;
import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.dispatcher.job.CHKFileInsertJob;

import java.io.File;
import java.util.Hashtable;
import java.util.logging.*;

import javax.swing.*;

/**
 * @author saces
 */
public class FrostCHKFileInsertJob extends CHKFileInsertJob {

    private static Logger logger = Logger.getLogger(FrostCHKFileInsertJob.class.getName());

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
	public void incomingMessage(String id, Hashtable message) {
        
        // Sample message:
//      SimpleProgress
//      Total=12288 // 12,288 blocks we can fetch
//      Required=8192 // we only need 8,192 of them (because of splitfile redundancy)
//      Failed=452 // 452 of them have failed due to running out of retries
//      FatallyFailed=0 // none of them have encountered fatal errors
//      Succeeded=1027 // we have successfully fetched 1,027 blocks
//      FinalizedTotal=true // the Total will not increase any further (if this is false, it may increase; it will never decrease)
//      Identifier=Request Number One
//      EndMessage

        try {
    		if ("SimpleProgress".equals(message.get(FCPConnection.MESSAGENAME))) {
    			// no DownloadItem set? we are not intrested in progress
    			if (uploadItem == null) { return; }

                String bolMsg = (String)message.get("FinalizedTotal");
                boolean isFinalized0;
                if( bolMsg != null && bolMsg.trim().equalsIgnoreCase("true") ) {
                    isFinalized0 = true;
                } else {
                    isFinalized0 = false;
                }

                final boolean isFinalized = isFinalized0;
                final int totalBlocks = Integer.parseInt((String)message.get("Total"));
                final int doneBlocks = Integer.parseInt((String)message.get("Succeeded"));
                

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        uploadItem.setFinalized(isFinalized);
                        uploadItem.setTotalBlocks(totalBlocks);
                        uploadItem.setDoneBlocks(doneBlocks);

                        uploadItem.fireValueChanged();
                    }
                });
    			//System.out.println("SP" + this + message);
            }
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception catched", t);
        }
        // maybe we could start to spread the key before complete upload?
//		if ("PutFetchable".equals(message.get(FCPConnection.MESSAGENAME))) {
//			//System.out.println("PutFetchable" + this + message);
//			if (uploadItem == null) { return; }
//			uploadItem.setKey((String)message.get("URI"));
//			// dont return, super the putfetchable;
//		}

		super.incomingMessage(id, message);
	}
}
