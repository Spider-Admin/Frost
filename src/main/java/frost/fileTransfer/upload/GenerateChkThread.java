/*
  GenerateChkThread.java / Frost
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
package frost.fileTransfer.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.fcp.FcpHandler;

public class GenerateChkThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(GenerateChkThread.class);

    private UploadTicker ticker;

    FrostUploadItem uploadItem = null; // for upload and generate CHK

    protected GenerateChkThread(UploadTicker newTicker, FrostUploadItem ulItem) {
        ticker = newTicker;
        uploadItem = ulItem;
    }

    @Override
    public void run() {
        ticker.generatingThreadStarted();
        try {
            generateCHK();
        } catch (Throwable e) {
            logger.error("Exception thrown in run()", e);
        }
        ticker.generatingThreadFinished();
    }

    private void generateCHK() {
        logger.info("CHK generation started for file: {}", uploadItem.getFileName());
        String chkkey = null;
        
        // yes, this destroys any upload progress, but we come only here if
        // chkKey == null, so the file should'nt be uploaded until now
        try {
            chkkey = FcpHandler.inst().generateCHK(uploadItem.getFile());
        } catch (Throwable t) {
            logger.error("Encoding failed", t);
            uploadItem.setState(FrostUploadItem.STATE_WAITING);
            return;
        }

        if (chkkey != null) {
            String prefix = new String("freenet:");
            if (chkkey.startsWith(prefix)) {
                chkkey = chkkey.substring(prefix.length());
            }
        } else {
            logger.error("Could not generate CHK key for file.");
            uploadItem.setState(FrostUploadItem.STATE_WAITING);
            return;
        }

        uploadItem.setKey(chkkey);

        // after key generation set to state waiting for upload 
        uploadItem.setState(FrostUploadItem.STATE_WAITING);
    }
}
