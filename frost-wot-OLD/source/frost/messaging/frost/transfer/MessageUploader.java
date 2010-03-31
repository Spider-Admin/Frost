/*
  MessageUploader.java / Frost
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
package frost.messaging.frost.transfer;

import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.identities.*;
import frost.messaging.frost.*;
import frost.messaging.frost.gui.*;
import frost.storage.perst.*;
import frost.util.*;

/**
 * This class uploads a message file to freenet.
 */
public class MessageUploader {

    private static final Logger logger = Logger.getLogger(MessageUploader.class.getName());

    /**
     * The work area for MessageUploader.
     */
    static class MessageUploaderWorkArea {

        MessageXmlFile message;
        File uploadFile;
        File unsentMessageFile;
        MessageUploaderCallback callback;
        Identity encryptForRecipient;
        LocalIdentity senderId;
        JFrame parentFrame;

        IndexSlot indexSlot;

        String logBoardName;
    }

    /**
     * Create a file to upload from the message.
     * Sets the MessageUploaderWorkArea.uploadFile value.
     * @return  true if successful, false otherwise
     */
    protected static boolean prepareMessage(final MessageUploaderWorkArea wa) {
        return prepareMessage07(wa);
    }

    /**
     * Prepares and uploads the message.
     * Returns -1 if upload failed (unsentMessageFile should stay in unsent msgs folder in this case)
     * or returns a value >= 0 containing the final index where the message was uploaded to.
     *
     * If senderId is provided, the message is signed with this ID.
     * If senderId is null the message is sent anonymously.
     *
     */
    public static MessageUploaderResult uploadMessage(
            final MessageXmlFile message,
            final Identity encryptForRecipient,
            final LocalIdentity senderId,
            final MessageUploaderCallback callback,
            final IndexSlot indexSlot,
            final JFrame parentFrame,
            final String logBoardName) {

        final MessageUploaderWorkArea wa = new MessageUploaderWorkArea();

        wa.message = message;
        wa.unsentMessageFile = message.getFile();
        wa.parentFrame = parentFrame;
        wa.callback = callback;
        wa.indexSlot = indexSlot;
        wa.encryptForRecipient = encryptForRecipient;
        wa.senderId = senderId; // maybe null for anonymous
        wa.logBoardName = logBoardName;

        wa.uploadFile = new File(wa.unsentMessageFile.getPath() + ".upltmp");
        wa.uploadFile.delete(); // just in case it already exists
        wa.uploadFile.deleteOnExit(); // so that it is deleted when Frost exits

        if( prepareMessage(wa) == false ) {
            return new MessageUploaderResult(true); // keep msg
        }

        try {
            return uploadMessage(wa);
        } catch (final IOException ex) {
            logger.log(Level.SEVERE,"ERROR: Unexpected IOException, upload stopped.",ex);
        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in MessageUploadThread", t);
        }
        return new MessageUploaderResult(true); // keep msg
    }

    /**
     * Upload the message file.
     */
    protected static MessageUploaderResult uploadMessage(final MessageUploaderWorkArea wa) throws IOException {

        logger.info("TOFUP: Uploading message to board '" + wa.logBoardName + "'");

        boolean tryAgain;
        do {
            boolean success = false;
            int index = -1;
            int tries = 0;
            final int maxTries = 10;
            boolean error = false;

            boolean retrySameIndex = false;

            String logInfo = null;

            while ( success == false && error == false ) {

                if( retrySameIndex == false ) {
                    // find next free index slot
                    if( index < 0 ) {
                        index = wa.indexSlot.findFirstUploadSlot();
                    } else {
                        index = wa.indexSlot.findNextUploadSlot(index);
                    }
                } else {
                    // we retry the index
                    // reset flag
                    retrySameIndex = false;
                }

                // try to insert message
                FcpResultPut result = null;

                try {
                    final String upKey = wa.callback.composeUploadKey(wa.message, index);
                    logInfo = " board="+wa.logBoardName+", key="+upKey;
                    // signMetadata is null for unsigned upload. Do not do redirect.
                    result = FcpHandler.inst().putFile(
                            FcpHandler.TYPE_MESSAGE,
                            upKey,
                            wa.uploadFile,
                            true);  // doMime
                } catch (final Throwable t) {
                    logger.log(Level.SEVERE, "TOFUP: Error in FcpInsert.putFile."+logInfo, t);
                }

                final int waitTime = 15000;

                if (result.isRetry()) {
                    logger.severe("TOFUP: Message upload failed (RouteNotFound)!\n"+logInfo+
                            "\n(try no. " + tries + " of " + maxTries + "), retrying index " + index);
                    tries++;
                    retrySameIndex = true;
                    Mixed.wait(waitTime);

                } else if (result.isSuccess()) {
                    // msg is probabilistic cached in freenet node, retrieve it to ensure it is in our store
                    final File tmpFile = new File(wa.unsentMessageFile.getPath() + ".down");

                    int dlTries = 0;
                    // we use same maxTries as for upload
                    while(dlTries < maxTries) {
                        Mixed.wait(waitTime);
                        tmpFile.delete(); // just in case it already exists
                        if( downloadMessage(index, tmpFile, wa) ) {
                            break;
                        } else {
                            logger.severe("TOFUP: Uploaded message could NOT be retrieved! "+
                                    "Download try "+dlTries+" of "+maxTries+"\n"+logInfo);
                            dlTries++;
                        }
                    }

                    if( tmpFile.length() > 0 ) {
                        logger.warning("TOFUP: Uploaded message was successfully retrieved."+logInfo);
                        success = true;
                    } else {
                        logger.severe("TOFUP: Uploaded message could NOT be retrieved!\n"+logInfo+
                                "\n(try no. " + tries + " of " + maxTries + "), retrying index " + index);
                        tries++;
                        retrySameIndex = true;
                    }
                    tmpFile.delete();

                } else if (result.isKeyCollision()) {
                    logger.warning("TOFUP: Upload collided, trying next free index."+logInfo);
                    Mixed.wait(waitTime);
                } else if (result.isNoConnection()) {
                    // no connection to node, try after next update
                    logger.severe("TOFUP: Upload failed, no node connection."+logInfo);
                    error = true;
                } else {
                    // other error
                    if (tries > maxTries) {
                        error = true;
                    } else {
                        logger.warning("TOFUP: Upload failed, "+logInfo+"\n(try no. " + tries + " of " + maxTries
                                + "), retrying index " + index);
                        tries++;
                        retrySameIndex = true;
                        Mixed.wait(waitTime);
                    }
                }
            }

            if (success) {
                // mark slot used
                wa.indexSlot.setUploadSlotUsed(index);

                logger.info("Message successfully uploaded."+logInfo+"\n");

                wa.uploadFile.delete();

                return new MessageUploaderResult(index); // success

            } else { // error == true
                logger.warning("TOFUP: Error while uploading message.");

                final boolean retrySilently = Core.frostSettings.getBoolValue(SettingsClass.SILENTLY_RETRY_MESSAGES);
                if (!retrySilently) {
                    // Uploading of that message failed. Ask the user if Frost
                    // should try to upload the message another time.
                    final MessageUploadFailedDialog faildialog = new MessageUploadFailedDialog(wa.parentFrame, wa.message, null);
                    final int answer = faildialog.startDialog();
                    if (answer == MessageUploadFailedDialog.RETRY_VALUE) {
                        logger.info("TOFUP: Will try to upload again immediately.");
                        tryAgain = true;
                    } else if (answer == MessageUploadFailedDialog.RETRY_NEXT_STARTUP_VALUE) {
                        wa.uploadFile.delete();
                        // message is not re-enqueued in UnsentMessagesManager, we read it during next startup
                        logger.info("TOFUP: Will try to upload again on next startup.");
//                        tryAgain = false;
                        return new MessageUploaderResult(true); // keep msg
                    } else if (answer == MessageUploadFailedDialog.DISCARD_VALUE) {
                        wa.uploadFile.delete();
                        logger.warning("TOFUP: Will NOT try to upload message again.");
//                        tryAgain = false;
                        return new MessageUploaderResult(false); // delete msg
                    } else { // paranoia
                        logger.warning("TOFUP: Paranoia - will try to upload message again.");
                        tryAgain = true;
                    }
                } else {
                    // Retry silently
                    tryAgain = true;
                }
            }
        } while(tryAgain);

        return new MessageUploaderResult(true); // upload failed, keep msg
    }

    /**
     * Download the specified index, used to check if file was correctly uploaded.
     */
    private static boolean downloadMessage(final int index, final File targetFile, final MessageUploaderWorkArea wa) {
        try {
            final String downKey = wa.callback.composeDownloadKey(wa.message, index);
            final FcpResultGet res = FcpHandler.inst().getFile(
                    FcpHandler.TYPE_MESSAGE,
                    downKey,
                    null,
                    targetFile,
                    FcpHandler.MAX_MESSAGE_SIZE_07,
                    -1);
            if( res != null && res.isSuccess() && targetFile.length() > 0 ) {
                return true;
            }
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Handled exception in downloadMessage", t);
        }
        return false;
    }

    /**
     * Encrypt and sign the message into a file that is uploaded afterwards.
     */
    protected static boolean prepareMessage07(final MessageUploaderWorkArea wa) {

        // sign the message content if necessary
        if( wa.senderId != null ) {
            // for sure, set fromname
            wa.message.setFromName(wa.senderId.getUniqueName());
            // sign msg
            wa.message.signMessageV1(wa.senderId.getPrivateKey());
            wa.message.signMessageV2(wa.senderId.getPrivateKey());
        }

        // save msg to uploadFile
        if (!wa.message.saveToFile(wa.uploadFile)) {
            logger.severe("Save to file '"+wa.uploadFile.getPath()+"' failed. This was a HARD error, file was NOT uploaded, please report to a dev!");
            return false;
        }

        if( wa.message.getSignatureV2() != null &&
            wa.message.getSignatureV2().length() > 0 && // we signed, so encrypt is possible
            wa.encryptForRecipient != null )
        {
            // encrypt file to temp. upload file
            if(!MessageXmlFile.encryptForRecipientAndSaveCopy(wa.uploadFile, wa.encryptForRecipient, wa.uploadFile)) {
                logger.severe("This was a HARD error, file was NOT uploaded, please report to a dev!");
                return false;
            }

        } else if( wa.encryptForRecipient != null ) {
            logger.log(Level.SEVERE, "TOFUP: ALERT - can't encrypt message if sender is Anonymous! Will not send message!");
            return false; // unable to encrypt
        }
        // else leave msg as is

        return true;
    }
}
