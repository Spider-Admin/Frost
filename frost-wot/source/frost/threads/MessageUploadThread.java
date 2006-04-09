/*
  MessageUploadThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import org.w3c.dom.*;

import frost.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.fileTransfer.upload.*;
import frost.gui.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;

/**
 * Uploads a message to a certain message board
 */
public class MessageUploadThread extends BoardUpdateThreadObject implements BoardUpdateThread {

    private static Logger logger = Logger.getLogger(MessageUploadThread.class.getName());

    private SettingsClass frostSettings;
    private JFrame parentFrame;
    private Board board;

    private String destinationBase;

    private String keypool;
    private MessageObject message;

    private File unsentMessageFile;
    private int messageUploadHtl;
    private String privateKey;
    private String publicKey;
    private boolean secure;

    private byte[] signMetadata;
    private File zipFile;

    private Identity encryptForRecipient;

    public MessageUploadThread(
            Board board,
            MessageObject mo,
            FrostIdentities newIdentities,
            SettingsClass frostSettings) {

        this(board, mo, newIdentities, frostSettings, null);
    }
    /**
     * Upload a message.
     * If recipient is not null, the message will be encrypted for the recipient.
     * In this case the sender must be not Anonymous!
     */
    public MessageUploadThread(
        Board board,
        MessageObject mo,
        FrostIdentities newIdentities,
        SettingsClass frostSettings,
        Identity recipient) {

        super(board, newIdentities);
        this.board = board;
        this.message = mo;
        this.frostSettings = frostSettings;
        this.encryptForRecipient = recipient;

        // we only set the date&time if they are not already set
        // (in case the uploading was pending from before)
        // _OR_ if the date of the message differs from current date, because
        //      we don't want to insert messages with another date into keyspace of today
        // this also allows to do a date check when we receive a file,
        // see VerifyableMessageObject.verifyDate
        if (mo.getDate() == "" || mo.getDate().equals(DateFun.getDate()) == false) {
            mo.setTime(DateFun.getFullExtendedTime() + "GMT");
            mo.setDate(DateFun.getDate());
        }

        messageUploadHtl = frostSettings.getIntValue("tofUploadHtl");
        keypool = frostSettings.getValue("keypool.dir");

        // this class always creates a new msg file on hd and deletes the file
        // after upload was successful, or keeps it for next try
        String uploadMe = new StringBuffer()
                .append(frostSettings.getValue("unsent.dir"))
                .append("unsent")
                .append(String.valueOf(System.currentTimeMillis()))
                .append(".xml")
                .toString();
        unsentMessageFile = new File(uploadMe);
    }

    /**
     * This method compares the message that is to be uploaded with
     * a local message to see if they are equal
     * @param localFile the local message to compare the message to
     *         be uploaded with.
     * @return true if they are equal. False otherwise.
     */
    private boolean checkLocalMessage(File localFile) {
        try {
            MessageObject localMessage = new MessageObject(localFile);
            // We compare the messages by content (body), subject, from and attachments
            if (!localMessage.getContent().equals(message.getContent())) {
                return false;
            }
            if (!localMessage.getSubject().equals(message.getSubject())) {
                return false;
            }
            if (!localMessage.getFrom().equals(message.getFrom())) {
                return false;
            }
            AttachmentList attachments1 = message.getAllAttachments();
            AttachmentList attachments2 = localMessage.getAllAttachments();
            if (attachments1.size() != attachments2.size()) {
                return false;
            }
            Iterator iterator1 = attachments1.iterator();
            Iterator iterator2 = attachments2.iterator();
            while (iterator1.hasNext()) {
                Attachment attachment1 = (Attachment) iterator1.next();
                Attachment attachment2 = (Attachment) iterator2.next();
                if (attachment1.compareTo(attachment2) != 0) {
                    return false;
                }
            }
            return true;
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Handled Exception in checkLocalMessage", exception);
            return false; // We assume that the local message is different (it may be corrupted)
        }
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
    private boolean checkRemoteFile(int index) {
        try {
            File remoteFile = new File(unsentMessageFile.getPath() + ".coll");
            remoteFile.delete(); // just in case it already exists
            remoteFile.deleteOnExit(); // so that it is deleted when Frost exits

            if( !downloadMessage(index, remoteFile) ) {
                remoteFile.delete();
                return false; // We could not retrieve the remote file. We assume they are different.
            }

            if( encryptForRecipient != null ) {
                // we compare the local encrypted zipFile with remoteFile
                boolean isEqual = FileAccess.compareFiles(zipFile, remoteFile);
                remoteFile.delete();
                return isEqual;
            } else {
                // compare contents
                byte[] unzippedXml = FileAccess.readZipFileBinary(remoteFile);
                if(unzippedXml == null) {
                    return false;
                }
                FileAccess.writeFile(unzippedXml, remoteFile);
                boolean isEqual = checkLocalMessage(remoteFile);
                remoteFile.delete();
                return isEqual;
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Handled exception in checkRemoteFile", e);
            return false;
        }
    }

    private boolean downloadMessage(int index, File targetFile) {
        try {
            String downKey = composeDownKey(index);
            FcpResults res = FcpHandler.inst().getFile(downKey, null, targetFile, messageUploadHtl, false, false);
            if( res != null && targetFile.length() > 0 ) {
                return true;
            }
        } catch(Throwable t) {
            logger.log(Level.WARNING, "Handled exception in downloadMessage", t);
        }
        return false;
    }

    /**
     * This method composes the downloading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    private String composeDownKey(int index) {
        String key;
        if (secure) {
            key = new StringBuffer()
                    .append(publicKey)
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(message.getDate())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            key = new StringBuffer()
                    .append("KSK@frost/message/")
                    .append(frostSettings.getValue("messageBase"))
                    .append("/")
                    .append(message.getDate())
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return key;
    }

    /**
     * This method composes the uploading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    private String composeUpKey(int index) {
        String key;
        if (secure) {
            key = new StringBuffer()
                    .append(privateKey)
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(message.getDate())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            key = new StringBuffer()
                    .append("KSK@frost/message/")
                    .append(frostSettings.getValue("messageBase"))
                    .append("/")
                    .append(message.getDate())
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return key;
    }

    /**
     * This method returns the base path from which we look for
     * existing files while looking for the next available index to use.
     * That directory is also created if it doesn't exist.
     * @return the base path to use when looking for existing files while
     *          looking for the next index.
     */
    private String getDestinationBase() {
        if (destinationBase == null) {
            String fileSeparator = System.getProperty("file.separator");
            destinationBase = new StringBuffer()
                    .append(keypool)
                    .append(board.getBoardFilename())
                    .append(fileSeparator)
                    .append(DateFun.getDate())
                    .append(fileSeparator)
                    .toString();
            File makedir = new File(destinationBase);
            if (!makedir.exists()) {
                makedir.mkdirs();
            }
        }
        return destinationBase;
    }

    /* (non-Javadoc)
     * @see frost.threads.BoardUpdateThread#getThreadType()
     */
    public int getThreadType() {
        return BoardUpdateThread.MSG_UPLOAD;
    }

    /**
     * This method performs several tasks before uploading the message.
     * @return true if the initialization was successful. False otherwise.
     */
    private boolean initialize() {

        // switch public / secure board
        if (board.isWriteAccessBoard()) {
            privateKey = board.getPrivateKey();
            publicKey = board.getPublicKey();
            secure = true;
        } else {
            secure = false;
        }

        logger.info("TOFUP: Uploading message to board '" + board.getName() + "' with HTL " + messageUploadHtl);

        // first save msg to be able to resend on crash
        if (!saveMessage(message, unsentMessageFile)) {
            logger.severe("This was a HARD error and the file to upload is lost, please report to a dev!");
            return false;
        }

        // BBACKFLAG: ask user if uploading of X files is allowed!
        // if one attachment file does not longer exists (on retry), we delete the message in uploadAttachments()!
        if (!uploadAttachments(message, unsentMessageFile)) {
            return false;
        }

        // zip the xml file to a temp file
        zipFile = new File(unsentMessageFile.getPath() + ".upltmp");
        zipFile.delete(); // just in case it already exists
        zipFile.deleteOnExit(); // so that it is deleted when Frost exits
        FileAccess.writeZipFile(FileAccess.readByteArray(unsentMessageFile), "entry", zipFile);

        if( !zipFile.isFile() || zipFile.length() == 0 ) {
            logger.severe("Error: zip of message xml file failed, result file not existing or empty. Please report to a dev!");
            return false;
        }

        // encrypt and sign or just sign the zipped file if necessary
        String sender = message.getFrom();
        String myId = identities.getMyId().getUniqueName();
        if (sender.equals(myId) // nick same as my identity
            || sender.equals(Mixed.makeFilename(myId))) // serialization may have changed it
        {
            byte[] zipped = FileAccess.readByteArray(zipFile);

            if( encryptForRecipient != null ) {
                // encrypt + sign
                // first encrypt, then sign

                byte[] encData = Core.getCrypto().encrypt(zipped, encryptForRecipient.getKey());
                if( encData == null ) {
                    logger.severe("Error: could not encrypt the message, please report to a dev!");
                    return false;
                }
                zipFile.delete();
                FileAccess.writeFile(encData, zipFile); // write encrypted zip file

                EncryptMetaData ed = new EncryptMetaData(encData, identities.getMyId(), encryptForRecipient.getUniqueName());
                signMetadata = XMLTools.getRawXMLDocument(ed);

            } else {
                // sign only
                SignMetaData md = new SignMetaData(zipped, identities.getMyId());
                signMetadata = XMLTools.getRawXMLDocument(md);
            }
        } else if( encryptForRecipient != null ) {
            logger.log(Level.SEVERE, "TOFUP: ALERT - can't encrypt message if sender is Anonymous! Will not send message!");
            return false; // unable to encrypt
        }

        long allLength = zipFile.length();
        if( signMetadata != null ) {
            allLength += signMetadata.length;
        }
        if( allLength > 32767 ) { // limit in FcpInsert.putFile()
            String txt = "<html>The data you want to upload is too large ("+allLength+"), "+32767+" is allowed.<br>"+
                         "This should never happen, please report this to a Frost developer!</html>";
            JOptionPane.showMessageDialog(parentFrame, txt, "Error: message too large", JOptionPane.ERROR_MESSAGE);
            // TODO: the msg will be NEVER sent, we need an unsent folder in gui
            // but no too large message should reach us, see MessageFrame
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        notifyThreadStarted(this);

        boolean retry = true;
        try {
            if (initialize()) {
                while (retry) {
                    retry = uploadMessage();
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE,"ERROR: Unexpected IOException, terminating thread ...",ex);
//      } catch (MessageAlreadyUploadedException exception) {
//          logger.info("The message had already been uploaded. Therefore it will not be uploaded again.");
//          messageFile.delete();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in MessageUploadThread", t);
        }

        notifyThreadFinished(this);
    }

    /**
     * @param parentFrame
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * This method saves a message to disk in XML format (using a new name)
     * @param msg the MessageObject to save
     * @param file the file whose path will be used to save the message
     * @return true if successful. False otherwise.
     */
    private boolean saveMessage(MessageObject msg, File file) {
        File tmpFile = new File(file.getPath() + ".tmp");
        boolean success = false;
        try {
            Document doc = XMLTools.createDomDocument();
            doc.appendChild(msg.getXMLElement(doc));
            success = XMLTools.writeXmlFile(doc, tmpFile.getPath());
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Exception thrown in saveMessage()", ex);
        }
        if (success && tmpFile.length() > 0) {
            unsentMessageFile.delete();
            tmpFile.renameTo(unsentMessageFile);
            return true;
        } else {
            tmpFile.delete();
            return false;
        }
    }

    /**
     * This inserts an attached SharedFileObject into freenet
     * @param attachment the SharedFileObject to upload
     * @return true if successful. False otherwise.
     */
    private boolean uploadAttachment(SharedFileObject attachment) {

        assert attachment.getFile() != null : "message.getFile() failed!";

        String[] result = { "", "" };
        int uploadHtl = frostSettings.getIntValue("htlUpload");
        logger.info(
            "TOFUP: Uploading attachment "
                + attachment.getFile().getPath()
                + " with HTL "
                + uploadHtl);

        int maxTries = 3;
        int tries = 0;
        while (tries < maxTries
            && !result[0].equals("KeyCollision")
            && !result[0].equals("Success")) {
            try {
                result = FcpHandler.inst().putFile(
                        "CHK@",
                        attachment.getFile(),
                        null,
                        uploadHtl,
                        true, // doRedirect
                        true, // removeLocalKey, insert with full HTL even if existing in local store
                        new FrostUploadItem(null, null));
            } catch (Exception ex) {
                result = new String[1];
                result[0] = "Error";
            }
            tries++;
        }
        if (result[0].equals("KeyCollision") || result[0].equals("Success")) {
            logger.info(
                "TOFUP: Upload of attachment '"
                    + attachment.getFile().getPath()
                    + "' was successful.");
            String chk = result[1];
            attachment.setKey(chk);
            attachment.setFilename(attachment.getFile().getName()); // remove path from filename

            attachment.setFile(null); // we never want to give out a real pathname, this is paranoia
            return true;
        } else {
            logger.warning(
                "TOFUP: Upload of attachment '"
                    + attachment.getFile().getPath()
                    + "' was NOT successful.");
            return false;
        }
    }

    /**
     * Uploads all the attachments of a MessageObject and updates its
     * XML representation on disk
     * @param msg the MessageObject whose attachments will be uploaded
     * @param file file whose path will be used to save the MessageObject to disk.
     * @return true if successful. False otherwise.
     */
    private boolean uploadAttachments(MessageObject msg, File file) {
        boolean success = true;
        List fileAttachments = msg.getOfflineFiles();
        Iterator i = fileAttachments.iterator();
        // check if upload files still exist
        while (i.hasNext()) {
            SharedFileObject attachment = (SharedFileObject) i.next();
            if( attachment.getFile()== null ||
                attachment.getFile().isFile() == false ||
                attachment.getFile().length() == 0 )
            {
                JOptionPane.showMessageDialog(
                        parentFrame,
                        "The message that is currently send (maybe a send retry on next startup of Frost)\n"+
                        "contains a file attachment that does not longer exist!\n\n"+
                        "The send of the message was aborted and the message file was deleted\n"+
                        "to prevent another upload try on next startup of Frost.",
                        "Unrecoverable error",
                        JOptionPane.ERROR_MESSAGE);

                unsentMessageFile.delete();
                return false;
            }
        }

        // upload each attachment
        i = fileAttachments.iterator();
        while (i.hasNext()) {
            SharedFileObject attachment = (SharedFileObject) i.next();
            if(uploadAttachment(attachment)) {
                //If the attachment was successfully inserted, we update the message on disk.
                saveMessage(msg, file);
            } else {
                success = false;
            }
        }

        if (!success) {
            JOptionPane.showMessageDialog(
                parentFrame,
                "One or more attachments failed to upload.\n"
                    + "Will retry to upload attachments and message on next startup.",
                "Attachment upload failed",
                JOptionPane.ERROR_MESSAGE);
        }

        return success;
    }

    /**
     * Composes the complete path + filename of a messagefile in the keypool for the given index.
     */
    private String composeMsgFilePath(int index) {
        return new StringBuffer().append(getDestinationBase()).append(message.getDate())
        .append("-").append(board.getBoardFilename()).append("-").append(index).append(".xml")
        .toString();
    }

    /**
     * Composes only the filename of a messagefile in the keypool for the given index.
     */
    private String composeMsgFileNameWithoutXml(int index) {
        return new StringBuffer().append(message.getDate()).append("-")
        .append(board.getBoardFilename()).append("-").append(index).toString();
    }

    /**
     * Finds the next free index slot, starting at startIndex.
     * If a free slot is found (no xml message exists for this index in keypool)
     * the method reads some indicies ahead to check if it found a gap only.
     * The final firstEmptyIndex is returned.
     *
     * @param startIndex
     * @return index higher -1 ; or -1 if message was already uploaded
     * @throws MessageAlreadyUploadedException
     */
    private int findNextFreeIndex(int startIndex) {

        final int maxGap = 3;
        int tryIndex = startIndex;
        int firstEmptyIndex = -1;

        logger.fine("TOFUP: Searching free index in board "+
                    board.getBoardFilename()+", starting at index " + startIndex);

        while(true) {

            String testFilename = composeMsgFilePath(tryIndex);

            File testMe = new File(testFilename);
            if (testMe.exists() && testMe.length() > 0) {
                // check each existing message in board if this is the msg we want to send
                if (encryptForRecipient == null && checkLocalMessage(testMe)) {
                    return -1;
                } else {
                    tryIndex++;
                    firstEmptyIndex = -1;
                }
            } else {
                // a message file with this index does not exist
                // check if there is a gap between the next existing index
                if( firstEmptyIndex >= 0 ) {
                    if( (tryIndex - firstEmptyIndex) > maxGap ) {
                        break;
                    }
                } else {
                    firstEmptyIndex = tryIndex;
                }
                tryIndex++;
            }
        }
        logger.fine("TOFUP: Found free index in board "+board.getBoardFilename()+" at " + firstEmptyIndex);
        return firstEmptyIndex;
    }

    /**
     * @return
     * @throws IOException
     * @throws MessageAlreadyUploadedException
     */
    private boolean uploadMessage() throws IOException {
        boolean success = false;
        int index = 0;
        int tries = 0;
        int maxTries = 10;
        boolean error = false;
        boolean tryAgain;

        boolean retrySameIndex = false;
        File lockRequestIndex = null;

        String logInfo = null;

        while (!success) {

            if( retrySameIndex == false ) {
                // find next free index slot
                index = findNextFreeIndex(index);
                if( index < 0 ) {
                    // same message was already uploaded today
                    logger.info("TOFUP: Message seems to be already uploaded (1)");
                    success = true;
                    continue;
                }

                // probably empty slot, check if other threads currently try to insert to this index
                lockRequestIndex = new File(composeMsgFilePath(index) + ".lock");
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
                String upKey = composeUpKey(index);
                logInfo = " board="+board.getName()+", key="+upKey;
                // signMetadata is null for unsigned upload. Do not do redirect.
                result = FcpHandler.inst().putFile(
                        upKey,
                        zipFile,
                        signMetadata,
                        messageUploadHtl,
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
                File tmpFile = new File(unsentMessageFile.getPath() + ".down");

                int dlTries = 0;
                // we use same maxTries as for upload
                while(dlTries < maxTries) {
                    Mixed.wait(waitTime);
                    tmpFile.delete(); // just in case it already exists
                    if( downloadMessage(index, tmpFile) ) {
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
                    if (checkRemoteFile(index)) {
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

            tryAgain = false;

            // move message file to sent folder

            String finalName = composeMsgFileNameWithoutXml(index);

            File sentTarget = new File( frostSettings.getValue("sent.dir") + finalName + ".xml" );

            int counter = 2;
            while( sentTarget.exists() ) {
                // paranoia, target file already exists, append an increasing number
                sentTarget = new File( frostSettings.getValue("sent.dir") + finalName + "-" + counter + ".xml" );
                counter++;
            }

            boolean wasOk = unsentMessageFile.renameTo(sentTarget);
            if( !wasOk ) {
                logger.severe("Error: rename of '"+unsentMessageFile.getPath()+"' into '"+sentTarget.getPath()+"' failed!");
                unsentMessageFile.delete(); // we must delete the file from unsent folder to prevent another upload
            }
            zipFile.delete();

        } else {
            logger.warning("TOFUP: Error while uploading message.");

            boolean retrySilently = frostSettings.getBoolValue(SettingsClass.SILENTLY_RETRY_MESSAGES);
            if (!retrySilently) {
                // Uploading of that message failed. Ask the user if Frost
                // should try to upload the message another time.
                MessageUploadFailedDialog faildialog = new MessageUploadFailedDialog(parentFrame);
                int answer = faildialog.startDialog();
                if (answer == MessageUploadFailedDialog.RETRY_VALUE) {
                    logger.info("TOFUP: Will try to upload again.");
                    tryAgain = true;
                } else if (answer == MessageUploadFailedDialog.RETRY_NEXT_STARTUP_VALUE) {
                    zipFile.delete();
                    logger.info("TOFUP: Will try to upload again on next startup.");
                    tryAgain = false;
                } else if (answer == MessageUploadFailedDialog.DISCARD_VALUE) {
                    zipFile.delete();
                    unsentMessageFile.delete();
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
        logger.info("TOFUP: Upload Thread finished");
        return tryAgain;
    }
}
