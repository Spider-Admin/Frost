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

import java.io.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.storage.database.applayer.*;

/**
 * Provides an upload functionality, using the global index slots.
 */
public class GlobalFileUploader {

    private static final Logger logger = Logger.getLogger(GlobalFileUploader.class.getName());

    public static boolean uploadFile(
            GlobalIndexSlotsDatabaseTable indexSlots, 
            long date, 
            File uploadFile,
            String insertKey,
            String insertKeyExtension,
            boolean doMime) 
    {
        boolean success = false;
        boolean error = false;
        try {
            int tries = 0;
            final int maxTries = 3;
            // get first index and lock it
            int index = indexSlots.findFirstUploadSlot(date);
            while( !success && !error) {
                logger.info("Trying file upload to index "+index);

                FcpResultPut result = FcpHandler.inst().putFile(
                        FcpHandler.TYPE_MESSAGE,
                        insertKey + index + insertKeyExtension,
                        uploadFile,
                        null,
                        false, // doRedirect
                        true,  // removeLocalKey, insert with full HTL even if existing in local store
                        doMime); 

                if( result.isSuccess() ) {
                    // my files are already added to totalIdx, we don't need to download this index
                    indexSlots.setUploadSlotUsed(index, date);
                    logger.info("FILEDN: File successfully uploaded.");
                    success = true;
                } else {
                    if( result.isKeyCollision() ) {
                        // get next index and lock slot
                        index = indexSlots.findNextUploadSlot(index, date);
                        tries = 0; // reset tries
                        logger.info("FILEDN: File collided, increasing index.");
                        continue;
                    }
                    tries++;
                    if( tries < maxTries ) {
                        logger.info("FILEDN: Upload error (try #" + tries + "), retrying index "+index);
                    } else {
                        logger.info("FILEDN: Upload error (try #" + tries + "), giving up on index "+index);
                        error = true;
                    }
                }
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception in uploadFile", e);
        }
        logger.info("FILEDN: File upload finished, file uploaded state is: "+success);
        return success;
    }
}
