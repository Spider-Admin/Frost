/*
  AbstractGlobalFileUploader.java / Frost
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
package frost.transferlayer;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.fcp.FcpHandler;
import frost.fcp.FcpResultPut;
import frost.storage.perst.IndexSlot;

/**
 * Provides an upload functionality, using the global index slots.
 */
public class GlobalFileUploader {

	private static final Logger logger = LoggerFactory.getLogger(GlobalFileUploader.class);

    public static boolean uploadFile(
            final IndexSlot gis,
            final File uploadFile,
            final String insertKey,
            final String insertKeyExtension,
            final boolean doMime)
    {
        boolean success = false;
        boolean error = false;
        try {
            int tries = 0;
            final int maxTries = 3;
            // get first index and lock it
            int index = gis.findFirstUploadSlot();
            while( !success && !error) {
                logger.info("Trying file upload to index {}", index);

                final FcpResultPut result = FcpHandler.inst().putFile(
                        FcpHandler.TYPE_MESSAGE,
                        insertKey + index + insertKeyExtension,
                        uploadFile,
                        doMime);

                if( result.isSuccess() ) {
                    // my files are already added to totalIdx, we don't need to download this index
                    gis.setUploadSlotUsed(index);
                    gis.modify();
                    logger.info("FILEDN: File successfully uploaded.");
                    success = true;
                } else {
                    if( result.isKeyCollision() ) {
                        // get next index and lock slot
                        index = gis.findNextUploadSlot(index);
                        tries = 0; // reset tries
                        logger.info("FILEDN: File collided, increasing index.");
                        continue;
                    }
                    tries++;
                    if( tries < maxTries ) {
                        logger.error("FILEDN: Upload error (try #{}), retrying index {}", tries, index);
                    } else {
                        logger.error("FILEDN: Upload error (try #{}), giving up on index {}", tries, index);
                        error = true;
                    }
                }
            }
        } catch (final Throwable e) {
            logger.error("Exception in uploadFile", e);
        }
        logger.info("FILEDN: File upload finished, file uploaded state is: {}", success);
        return success;
    }
}
