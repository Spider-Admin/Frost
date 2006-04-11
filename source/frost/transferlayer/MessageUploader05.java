/*
  MessageUploader05.java / Frost
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
 * Provides method to upload a message. Does all conversions to build a file for uploading.
 * 
 * ATTN: This class is instanciated only once, so it must behave like a static class.
 *       Use no instance variables!
 */
public class MessageUploader05 extends MessageUploader {
    
    private static Logger logger = Logger.getLogger(MessageUploader05.class.getName());

    // this class is used static, we need a workarea for each 'instance' (you know C? ;) )
    private class WorkArea {
        MessageObject message;
        File zipFile;
        File unsentMessageFile;
        MessageUploaderCallback callback;
        private byte[] signMetadata;
        Identity encryptForRecipient;
        int messageUploadHtl;
        JFrame parentFrame;
        
        String logBoardName;
    }

    /**
     * Prepares and uploads the message.
     * Returns -1 if upload failed (unsentMessageFile should stay in unsent msgs folder in this case)
     * or returns a value >= 0 containing the final index where the message was uploaded to. 
     */
    public int uploadMessage(
            MessageObject message, 
            Identity encryptForRecipient,
            MessageUploaderCallback callback,
            JFrame parentFrame,
            String logBoardName) {

        WorkArea wa = new WorkArea();
        
        wa.message = message;
        wa.unsentMessageFile = message.getFile();
        wa.parentFrame = parentFrame;
        wa.callback = callback;
        wa.messageUploadHtl = Core.frostSettings.getIntValue("tofUploadHtl");
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
     * Encrypt, sign and zip the message into a file that is uploaded afterwards.
     */
    private boolean prepareMessage(WorkArea wa) {
        // zip the xml file to a temp file
        wa.zipFile = new File(wa.unsentMessageFile.getPath() + ".upltmp");
        wa.zipFile.delete(); // just in case it already exists
        wa.zipFile.deleteOnExit(); // so that it is deleted when Frost exits
        FileAccess.writeZipFile(FileAccess.readByteArray(wa.unsentMessageFile), "entry", wa.zipFile);

        if( !wa.zipFile.isFile() || wa.zipFile.length() == 0 ) {
            logger.severe("Error: zip of message xml file failed, result file not existing or empty. Please report to a dev!");
            return false;
        }

        // encrypt and sign or just sign the zipped file if necessary
        String sender = wa.message.getFrom();
        String myId = Core.getIdentities().getMyId().getUniqueName();
        if (sender.equals(myId) // nick same as my identity
            || sender.equals(Mixed.makeFilename(myId))) // serialization may have changed it
        {
            byte[] zipped = FileAccess.readByteArray(wa.zipFile);

            if( wa.encryptForRecipient != null ) {
                // encrypt + sign
                // first encrypt, then sign

                byte[] encData = Core.getCrypto().encrypt(zipped, wa.encryptForRecipient.getKey());
                if( encData == null ) {
                    logger.severe("Error: could not encrypt the message, please report to a dev!");
                    return false;
                }
                wa.zipFile.delete();
                FileAccess.writeFile(encData, wa.zipFile); // write encrypted zip file

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

        long allLength = wa.zipFile.length();
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
     * Upload the message file.
     */
    private int uploadMessage(WorkArea wa) throws IOException {

        logger.info("TOFUP: Uploading message to board '" + wa.logBoardName + "' with HTL " + wa.messageUploadHtl);

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
                String[] result = new String[2];
    
                try {
                    String upKey = wa.callback.composeUploadKey(index);
                    logInfo = " board="+wa.logBoardName+", key="+upKey;
                    // signMetadata is null for unsigned upload. Do not do redirect.
                    result = FcpHandler.inst().putFile(
                            upKey,
                            wa.zipFile,
                            wa.signMetadata,
                            wa.messageUploadHtl,
                            false,  // doRedirect
                            false); // removeLocalKey, we want a KeyCollision if key does already exist in local store!
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "TOFUP: Error in FcpInsert.putFile."+logInfo, t);
                }
    
                if (result == null || result[0] == null || result[1] == null) {
                    result[0] = "Error";
                    result[1] = "Error";
                }
                
                final int waitTime = 15000;
    
                if (result[0].equals("Success")) {
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
                } else {
                    if (result[0].equals("KeyCollision")) {
                        if (checkRemoteFile(index, wa)) {
                            logger.warning("TOFUP: Message seems to be already uploaded (2)."+logInfo);
                            success = true;
                        } else {
                            index++;
                            logger.warning("TOFUP: Upload collided, increasing index to " + index+"."+logInfo);
                            Mixed.wait(waitTime);
                        }
                    } else {
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
    
                wa.zipFile.delete();
                
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
                        wa.zipFile.delete();
                        logger.info("TOFUP: Will try to upload again on next startup.");
                        tryAgain = false;
                    } else if (answer == MessageUploadFailedDialog.DISCARD_VALUE) {
                        wa.zipFile.delete();
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
    private boolean downloadMessage(int index, File targetFile, WorkArea wa) {
        try {
            String downKey = wa.callback.composeDownloadKey(index);
            FcpResults res = FcpHandler.inst().getFile(downKey, null, targetFile, wa.messageUploadHtl, false, false);
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
    private boolean checkRemoteFile(int index, WorkArea wa) {
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
                boolean isEqual = FileAccess.compareFiles(wa.zipFile, remoteFile);
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
}
