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
package frost.transferlayer;

import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.gui.*;
import frost.identities.*;
import frost.messages.*;

/**
 * This class uploads a message file to freenet. The preparation of the
 * file that is uploaded is done differently for freenet 0.5 and freenet 0.7.
 * To accomplish this the abstract method prepareMessage() is called, which is
 * implemented by MessageUploader05 and MessageUploader07.
 */
public class MessageUploader {
    
    private static Logger logger = Logger.getLogger(MessageUploader.class.getName());

    /**
     * The work area for MessageUploader.
     */
    static class MessageUploaderWorkArea {
        
        MessageObject message;
        File uploadFile;
        File unsentMessageFile;
        MessageUploaderCallback callback;
        byte[] signMetadata;
        Identity encryptForRecipient;
        JFrame parentFrame;
        
        String logBoardName;
    }
    
    /**
     * Create a file to upload from the message. 
     * Sets the MessageUploaderWorkArea.uploadFile value.
     * @return  true if successful, false otherwise 
     */
    protected static boolean prepareMessage(MessageUploaderWorkArea wa) {
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
            return prepareMessage05(wa);
        } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            return prepareMessage07(wa);
        } else {
            logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
            return false;
        }
    }

    /**
     * Prepares and uploads the message.
     * Returns -1 if upload failed (unsentMessageFile should stay in unsent msgs folder in this case)
     * or returns a value >= 0 containing the final index where the message was uploaded to. 
     */
    public static int uploadMessage(
            MessageObject message, 
            Identity encryptForRecipient,
            MessageUploaderCallback callback,
            JFrame parentFrame,
            String logBoardName) {
        
        MessageUploaderWorkArea wa = new MessageUploaderWorkArea();
        
        wa.message = message;
        wa.unsentMessageFile = message.getFile();
        wa.parentFrame = parentFrame;
        wa.callback = callback;
        wa.encryptForRecipient = encryptForRecipient;
        wa.logBoardName = logBoardName;
        
        if( prepareMessage(wa) == false ) {
            return -1;
        }
        
        try {
            return uploadMessage(wa);
        } catch (IOException ex) {
            logger.log(Level.SEVERE,"ERROR: Unexpected IOException, upload stopped.",ex);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in MessageUploadThread", t);
        }
        return -1;
    }
    
    /**
     * Upload the message file.
     */
    protected static int uploadMessage(MessageUploaderWorkArea wa) throws IOException {

        logger.info("TOFUP: Uploading message to board '" + wa.logBoardName + "' with HTL " + Core.frostSettings.getIntValue("tofUploadHtl"));

        boolean tryAgain;
        do {
            boolean success = false;
            int index = 0;
            int tries = 0;
            int maxTries = 10;
            boolean error = false;
    
            boolean retrySameIndex = false;
            File lockRequestIndex = null;
            
            String logInfo = null;
    
            while (!success) {
    
                if( retrySameIndex == false ) {
                    // find next free index slot
                    index = wa.callback.findNextFreeUploadIndex(index);
                    if( index < 0 ) {
                        // same message was already uploaded today
                        logger.info("TOFUP: Message seems to be already uploaded (1)");
                        success = true;
                        continue;
                    }
    
                    // probably empty slot, check if other threads currently try to insert to this index
                    lockRequestIndex = new File(wa.callback.composeMsgFilePath(index) + ".lock");
                    if (lockRequestIndex.createNewFile() == false) {
                        // another thread tries to insert using this index, try next
                        index++;
                        logger.fine("TOFUP: Other thread tries this index, increasing index to " + index);
                        continue; // while
                    } else {
                        // we try this index
                        lockRequestIndex.deleteOnExit();
                    }
                } else {
                    // reset flag
                    retrySameIndex = false;
                    // lockfile already created
                }
    
                // try to insert message
                FcpResultPut result = null;
    
                try {
                    String upKey = wa.callback.composeUploadKey(index);
                    logInfo = " board="+wa.logBoardName+", key="+upKey;
                    // signMetadata is null for unsigned upload. Do not do redirect.
                    result = FcpHandler.inst().putFile(
                            upKey,
                            wa.uploadFile,
                            wa.signMetadata,
                            Core.frostSettings.getIntValue("tofUploadHtl"),
                            false,  // doRedirect
                            false); // removeLocalKey, we want a KeyCollision if key does already exist in local store!
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "TOFUP: Error in FcpInsert.putFile."+logInfo, t);
                }
    
                final int waitTime = 15000;

                if (result.isRetry()) {
                    logger.severe("TOFUP: Message upload failed (RouteNotFound)!\n"+logInfo+
                            "\n(try no. " + tries + " of " + maxTries + "), retrying index " + index);
                    tries++;
                    retrySameIndex = true;
                    
                } else if (result.isSuccess()) {
                    // msg is probabilistic cached in freenet node, retrieve it to ensure it is in our store
                    File tmpFile = new File(wa.unsentMessageFile.getPath() + ".down");
    
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
                    if (checkRemoteFile(index, wa)) {
                        logger.warning("TOFUP: Message seems to be already uploaded (2)."+logInfo);
                        success = true;
                    } else {
                        index++;
                        logger.warning("TOFUP: Upload collided, increasing index to " + index+"."+logInfo);
                        Mixed.wait(waitTime);
                    }
                } else {
                    // other error
                    if (tries > maxTries) {
                        success = true;
                        error = true;
                    } else {
                        logger.warning("TOFUP: Upload failed, "+logInfo+"\n(try no. " + tries + " of " + maxTries
                                + "), retrying index " + index);
                        tries++;
                        retrySameIndex = true;
                        Mixed.wait(waitTime);
                    }
                }
                // finally delete the index lock file, if we retry this index we keep it
                if (retrySameIndex == false) {
                    lockRequestIndex.delete();
                }
            }
    
            if (!error) {
                logger.info("*********************************************************************\n"
                        + "Message successfully uploaded."+logInfo+"\n"
                        + "*********************************************************************");
    
                wa.uploadFile.delete();
                
                return index;
    
            } else {
                logger.warning("TOFUP: Error while uploading message.");
    
                boolean retrySilently = Core.frostSettings.getBoolValue(SettingsClass.SILENTLY_RETRY_MESSAGES);
                if (!retrySilently) {
                    // Uploading of that message failed. Ask the user if Frost
                    // should try to upload the message another time.
                    MessageUploadFailedDialog faildialog = new MessageUploadFailedDialog(wa.parentFrame);
                    int answer = faildialog.startDialog();
                    if (answer == MessageUploadFailedDialog.RETRY_VALUE) {
                        logger.info("TOFUP: Will try to upload again.");
                        tryAgain = true;
                    } else if (answer == MessageUploadFailedDialog.RETRY_NEXT_STARTUP_VALUE) {
                        wa.uploadFile.delete();
                        logger.info("TOFUP: Will try to upload again on next startup.");
                        tryAgain = false;
                    } else if (answer == MessageUploadFailedDialog.DISCARD_VALUE) {
                        wa.uploadFile.delete();
                        wa.unsentMessageFile.delete();
                        logger.warning("TOFUP: Will NOT try to upload message again.");
                        tryAgain = false;
                    } else { // paranoia
                        logger.warning("TOFUP: Paranoia - will try to upload message again.");
                        tryAgain = true;
                    }
                } else {
                    // Retry silently
                    tryAgain = true;
                }
            }
        }
        while(tryAgain);
        
        return -1; // upload failed
    }

    /**
     * Download the specified index, used to check if file was correctly uploaded.
     */
    private static boolean downloadMessage(int index, File targetFile, MessageUploaderWorkArea wa) {
        try {
            String downKey = wa.callback.composeDownloadKey(index);
            FcpResultGet res = FcpHandler.inst().getFile(
                    downKey, 
                    null, 
                    targetFile, 
                    Core.frostSettings.getIntValue("tofUploadHtl"), 
                    false, 
                    false);
            if( res != null && targetFile.length() > 0 ) {
                return true;
            }
        } catch(Throwable t) {
            logger.log(Level.WARNING, "Handled exception in downloadMessage", t);
        }
        return false;
    }

    /**
     * This method is called when there has been a key collision. It checks
     * if the remote message with that key is the same as the message that is
     * being uploaded
     * @param upKey the key of the remote message to compare with the message
     *         that is being uploaded.
     * @return true if the remote message with the given key equals the
     *          message that is being uploaded. False otherwise.
     */
    private static boolean checkRemoteFile(int index, MessageUploaderWorkArea wa) {
        try {
            File remoteFile = new File(wa.unsentMessageFile.getPath() + ".coll");
            remoteFile.delete(); // just in case it already exists
            remoteFile.deleteOnExit(); // so that it is deleted when Frost exits

            if( !downloadMessage(index, remoteFile, wa) ) {
                remoteFile.delete();
                return false; // We could not retrieve the remote file. We assume they are different.
            }

            if( wa.encryptForRecipient != null ) {
                // we compare the local encrypted zipFile with remoteFile
                boolean isEqual = FileAccess.compareFiles(wa.uploadFile, remoteFile);
                remoteFile.delete();
                return isEqual;
            } else {
                // compare contents
                byte[] unzippedXml = FileAccess.readZipFileBinary(remoteFile);
                if(unzippedXml == null) {
                    return false;
                }
                FileAccess.writeFile(unzippedXml, remoteFile);
                boolean isEqual = wa.message.compareTo(remoteFile);
                remoteFile.delete();
                return isEqual;
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Handled exception in checkRemoteFile", e);
            return false;
        }
    }
    
    /**
     * Encrypt, sign and zip the message into a file that is uploaded afterwards.
     */
    protected static boolean prepareMessage05(MessageUploaderWorkArea wa) {
        // zip the xml file to a temp file
        wa.uploadFile = new File(wa.unsentMessageFile.getPath() + ".upltmp");
        wa.uploadFile.delete(); // just in case it already exists
        wa.uploadFile.deleteOnExit(); // so that it is deleted when Frost exits

        boolean doSign = false;
        
        String sender = wa.message.getFrom();
        String myId = Core.getIdentities().getMyId().getUniqueName();
        if (sender.equals(myId) // nick same as my identity
            || sender.equals(Mixed.makeFilename(myId))) // serialization may have changed it
        {
            doSign = true;
            
            // we put the signature into the message too, but it is not used for verification currently
            // to keep compatability to previous frosts for 0.5
            wa.message.signMessage(Core.getIdentities().getMyId().getPrivKey());
            
            if( !wa.message.save() ) {
                logger.severe("Save of signed msg failed. This was a HARD error, please report to a dev!");
                return false;
            }
        }
        
        FileAccess.writeZipFile(FileAccess.readByteArray(wa.unsentMessageFile), "entry", wa.uploadFile);

        if( !wa.uploadFile.isFile() || wa.uploadFile.length() == 0 ) {
            logger.severe("Error: zip of message xml file failed, result file not existing or empty. Please report to a dev!");
            return false;
        }

        // encrypt and sign or just sign the zipped file if necessary
        if (doSign) {
            byte[] zipped = FileAccess.readByteArray(wa.uploadFile);
            
            if( wa.encryptForRecipient != null ) {
                // encrypt + sign
                // first encrypt, then sign

                byte[] encData = Core.getCrypto().encrypt(zipped, wa.encryptForRecipient.getKey());
                if( encData == null ) {
                    logger.severe("Error: could not encrypt the message, please report to a dev!");
                    return false;
                }
                wa.uploadFile.delete();
                FileAccess.writeFile(encData, wa.uploadFile); // write encrypted zip file

                EncryptMetaData ed = new EncryptMetaData(encData, Core.getIdentities().getMyId(), wa.encryptForRecipient.getUniqueName());
                wa.signMetadata = XMLTools.getRawXMLDocument(ed);

            } else {
                // sign only
                SignMetaData md = new SignMetaData(zipped, Core.getIdentities().getMyId());
                wa.signMetadata = XMLTools.getRawXMLDocument(md);
            }
        } else if( wa.encryptForRecipient != null ) {
            logger.log(Level.SEVERE, "TOFUP: ALERT - can't encrypt message if sender is Anonymous! Will not send message!");
            return false; // unable to encrypt
        }

        long allLength = wa.uploadFile.length();
        if( wa.signMetadata != null ) {
            allLength += wa.signMetadata.length;
        }
        if( allLength > 32767 ) { // limit in FcpInsert.putFile()
            String txt = "<html>The data you want to upload is too large ("+allLength+"), "+32767+" is allowed.<br>"+
                         "This should never happen, please report this to a Frost developer!</html>";
            JOptionPane.showMessageDialog(wa.parentFrame, txt, "Error: message too large", JOptionPane.ERROR_MESSAGE);
            // TODO: the msg will be NEVER sent, we need an unsent folder in gui
            // but no too large message should reach us, see MessageFrame
            return false;
        }
        return true;
    }

    /**
     * Encrypt and sign the message into a file that is uploaded afterwards.
     */
    protected static boolean prepareMessage07(MessageUploaderWorkArea wa) {
        
        // copy the xml file to a temp file that is uploaded
        wa.uploadFile = new File(wa.unsentMessageFile.getPath() + ".upltmp");
        wa.uploadFile.delete(); // just in case it already exists
        wa.uploadFile.deleteOnExit(); // so that it is deleted when Frost exits
        
        // sign the message content if necessary
        String sender = wa.message.getFrom();
        String myId = Core.getIdentities().getMyId().getUniqueName();

        if (sender.equals(myId) // nick same as my identity
            || sender.equals(Mixed.makeFilename(myId))) // serialization may have changed it
        {
            // sign msg
            wa.message.signMessage(Core.getIdentities().getMyId().getPrivKey());
        }

        // save msg to uploadFile   
        if (!wa.message.saveToFile(wa.uploadFile)) {
            logger.severe("Save to file '"+wa.uploadFile.getPath()+"' failed. This was a HARD error, file was NOT uploaded, please report to a dev!");
            return false;
        }

        if( wa.message.getSignature() != null && 
            wa.message.getSignature().length() > 0 && // we signed, so encrypt is possible
            wa.encryptForRecipient != null )
        {
            // encrypt file to temp. upload file
            if(!MessageObject.encryptForRecipientAndSaveCopy(wa.uploadFile, wa.encryptForRecipient, wa.uploadFile)) {
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
