/*
  UploadThread.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
import frost.fileTransfer.*;

class UploadThread extends Thread {

    private static final Logger logger = Logger.getLogger(UploadThread.class.getName());

    private final UploadTicker ticker;
    private final FrostUploadItem uploadItem;
    private final boolean doMime;

    protected UploadThread(final UploadTicker newTicker, final FrostUploadItem ulItem, final boolean doMim) {
        ticker = newTicker;
        uploadItem = ulItem;
        doMime = doMim;
    }

    @Override
    public void run() {
        ticker.uploadingThreadStarted();
        try {
            upload();
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Exception thrown in run()", e);
        }
        ticker.uploadThreadFinished();
    }

    private void upload() { // real upload

        logger.info("Upload of " + uploadItem.getFile().getName() + " started.");

        FcpResultPut result = null;
        try {
            result = FcpHandler.inst().putFile(
                    FcpHandler.TYPE_FILE,
                    "CHK@",
                    uploadItem.getFile(),
                    doMime,
                    uploadItem); // provide the uploadItem to indicate that this upload is contained in table
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception thrown in putFile()", t);
        }

        FileTransferManager.inst().getUploadManager().notifyUploadFinished(uploadItem, result);
    }
}
