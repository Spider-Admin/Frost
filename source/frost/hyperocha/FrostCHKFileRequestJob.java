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
import java.util.logging.Logger;

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
//	public FrostCHKFileRequestJob(String key, File target ) {
//		this(key, target, null);
//	}
		
	public FrostCHKFileRequestJob(String key, File target, FrostDownloadItem dli) {	
		super(Core.getFcpVersion(), makeID(dli), FreenetKey.CHKfromString(key), target);
		dlItem = dli;
	}

	/* on dda wo dont know the filesize until the node says DataFound
	 * we watch for DataFound and pass it to super.
	 * @see hyperocha.freenet.fcp.dispatcher.job.CHKFileRequestJob#incomingMessage(java.lang.String, hyperocha.freenet.fcp.NodeMessage)
	 */
	public void incomingMessage(String id, NodeMessage msg) {
		if (msg.isMessageName("DataFound")) {
			dlItem.setFileSize(new Long(msg.getLongValue("DataLength")));
			dlItem.fireValueChanged();
		}
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
