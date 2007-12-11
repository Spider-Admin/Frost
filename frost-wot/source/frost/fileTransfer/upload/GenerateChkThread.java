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

import java.util.logging.*;

import frost.fcp.*;

public class GenerateChkThread extends Thread {

    private UploadTicker ticker;

    private static final Logger logger = Logger.getLogger(GenerateChkThread.class.getName());

    FrostUploadItem uploadItem = null; // for upload and generate CHK

    protected GenerateChkThread(UploadTicker newTicker, FrostUploadItem ulItem) {
        ticker = newTicker;
        uploadItem = ulItem;
    }

    public void run() {
        ticker.generatingThreadStarted();
        try {
            generateCHK();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception thrown in run()", e);
        }
        ticker.generatingThreadFinished();
    }

    private void generateCHK() {
        logger.info("CHK generation started for file: " + uploadItem.getFile().getName());
        String chkkey = null;
        
        // yes, this destroys any upload progress, but we come only here if
        // chkKey == null, so the file should'nt be uploaded until now
        try {
            chkkey = FcpHandler.inst().generateCHK(uploadItem.getFile());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Encoding failed", t);
            uploadItem.setState(FrostUploadItem.STATE_WAITING);
            return;
        }

        if (chkkey != null) {
            String prefix = new String("freenet:");
            if (chkkey.startsWith(prefix)) {
                chkkey = chkkey.substring(prefix.length());
            }
        } else {
            logger.warning("Could not generate CHK key for file.");
            logger.log(Level.SEVERE, "Encoding failed");
            uploadItem.setState(FrostUploadItem.STATE_WAITING);
            return;
        }

        uploadItem.setKey(chkkey);

        // after key generation set to state waiting for upload 
        uploadItem.setState(FrostUploadItem.STATE_WAITING);
    }
}
