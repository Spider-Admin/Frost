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
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.job.CHKFileInsertJob;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * @version $Id$
 */
public class FrostCHKFileInsertJob extends CHKFileInsertJob {
    
    // FIXME: overwrite doPrepare(), jobStarted(), jobFinished()

    private static Logger logger = Logger.getLogger(FrostCHKFileInsertJob.class.getName());

	private FrostUploadItem uploadItem = null; 
	
	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileInsertJob(FrostUploadItem uitem) {
		super(Core.getFcpVersion(), uitem.getGqIdentifier(), uitem.getFile());
		uploadItem = uitem;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#onSimpleProgress(boolean, long, long, long, long)
	 */
	public void onSimpleProgress(boolean isFinalized, long totalBlocks,  long requiredBlocks,long doneBlocks, long failedBlocks, long fatallyFailedBlocks) {
		// no DownloadItem set? we are not intrested in progress
		if (uploadItem == null) { return; }

        uploadItem.setFinalized(isFinalized);
        uploadItem.setTotalBlocks((int)totalBlocks);
        uploadItem.setDoneBlocks((int)doneBlocks);
        uploadItem.fireValueChanged();
	}
}
