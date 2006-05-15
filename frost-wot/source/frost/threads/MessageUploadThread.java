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

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.transferlayer.*;

/**
 * Uploads a message to a certain message board. Uploads all attached files if needed.
 */
public class MessageUploadThread extends BoardUpdateThreadObject implements BoardUpdateThread, MessageUploaderCallback {

    private static Logger logger = Logger.getLogger(MessageUploadThread.class.getName());

    private JFrame parentFrame;
    private Board board;

    private String destinationBase;

    private MessageObject message;

    private Identity encryptForRecipient;

    /**
     * Upload a message.
     * If recipient is not null, the message will be encrypted for the recipient.
     * In this case the sender must be not Anonymous!
     */
    public MessageUploadThread(Board board, MessageObject mo, Identity recipient) {
        super(board);
        this.board = board;
        message = mo;
        encryptForRecipient = recipient;
    }

    /**
     * This method composes the downloading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    public String composeDownloadKey(int index) {
        String key;
        if (board.isWriteAccessBoard()) {
            key = new StringBuffer()
                    .append(board.getPublicKey())
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
                    .append(Core.frostSettings.getValue("messageBase"))
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
    public String composeUploadKey(int index) {
        String key;
        if (board.isWriteAccessBoard()) {
            key = new StringBuffer()
                    .append(board.getPrivateKey())
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
                    .append(Core.frostSettings.getValue("messageBase"))
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
                    .append(Core.frostSettings.getValue("keypool.dir"))
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

    public int getThreadType() {
        return BoardUpdateThread.MSG_UPLOAD;
    }

    public void run() {
        notifyThreadStarted(this);

        logger.info("TOFUP: Preparing upload of message to board '" + board.getName() + "'");

        try {
            // we only set the date&time if they are not already set
            // (in case the uploading was pending from before)
            // _OR_ if the date of the message differs from current date, because
            //      we don't want to insert messages with another date into keyspace of today
            // this also allows to do a date check when we receive a file,
            // see VerifyableMessageObject.verifyDate
            if (message.getDate() == "" || message.getDate().equals(DateFun.getDate()) == false) {
                message.setTime(DateFun.getFullExtendedTime() + "GMT");
                message.setDate(DateFun.getDate());
            }

            // this class always creates a new msg file on hd and deletes the file
            // after upload was successful, or keeps it for next try
            String uploadMe = new StringBuffer()
                    .append(Core.frostSettings.getValue("unsent.dir"))
                    .append("unsent")
                    .append(String.valueOf(System.currentTimeMillis()))
                    .append(".xml")
                    .toString();
            File unsentMessageFile = new File(uploadMe);

            // first save msg to be able to resend on crash
            message.setFile(unsentMessageFile);
            if (!message.save()) {
                logger.severe("This was a HARD error and the file to upload is lost, please report to a dev!");
                return;
            }

            // BBACKFLAG: ask user if uploading of X files is allowed!
            // if one attachment file does not longer exists (on retry), we delete the message in uploadAttachments()!
            if (!uploadAttachments(message)) {
                return;
            }

            uploadMessage();

        } catch (IOException ex) {
            logger.log(Level.SEVERE,"ERROR: Unexpected IOException, terminating thread ...",ex);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Oo. EXCEPTION in MessageUploadThread", t);
        }
        
        logger.info("TOFUP: Upload Thread finished");

        notifyThreadFinished(this);
    }

    /**
     * @param parentFrame
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * This inserts an attached SharedFileObject into freenet
     * @param attachment the SharedFileObject to upload
     * @return true if successful. False otherwise.
     */
    private boolean uploadAttachment(SharedFileObject attachment) {

        assert attachment.getFile() != null : "message.getFile() failed!";

        int uploadHtl = Core.frostSettings.getIntValue("htlUpload");
        logger.info(
            "TOFUP: Uploading attachment "
                + attachment.getFile().getPath()
                + " with HTL "
                + uploadHtl);

        int maxTries = 3;
        int tries = 0;
        while (tries < maxTries) {
            try {
                FcpResultPut result = FcpHandler.inst().putFile(
                        "CHK@",
                        attachment.getFile(),
                        null,
                        uploadHtl,
                        true, // doRedirect
                        true, // removeLocalKey, insert with full HTL even if existing in local store
                        new FrostUploadItem(null, null));

                if (result.isSuccess() || result.isKeyCollision()) {
                    logger.info("TOFUP: Upload of attachment '"+attachment.getFile().getPath()+"' was successful.");
                    attachment.setKey(result.getChkKey());
                    attachment.setFilename(attachment.getFile().getName()); // remove path from filename
                    attachment.setFile(null); // we never want to give out a real pathname, this is paranoia
                    return true;
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "TOFUP: Exception catched, will retry upload of attachment '"+attachment.getFile().getPath()+"'.",ex);
            }
            tries++;
        }
        logger.warning("TOFUP: Upload of attachment '"+attachment.getFile().getPath()+"' was NOT successful.");
        return false;
    }

    /**
     * Uploads all the attachments of a MessageObject and updates its
     * XML representation on disk
     * @param msg the MessageObject whose attachments will be uploaded
     * @param file file whose path will be used to save the MessageObject to disk.
     * @return true if successful. False otherwise.
     */
    private boolean uploadAttachments(MessageObject msg) {
        boolean success = true;
        List fileAttachments = msg.getOfflineFiles();
        
        int remainingAttachmentsToUploadCount = fileAttachments.size();
        
        if( remainingAttachmentsToUploadCount == 0 ) {
            return true; // nothing to upload
        }
        
        setAttachmentsToUploadCount(remainingAttachmentsToUploadCount);
        setAttachmentsToUploadRemainingCount(remainingAttachmentsToUploadCount);
        
        // check if upload files still exist
        for(Iterator i = fileAttachments.iterator(); i.hasNext(); ) {
            SharedFileObject attachment = (SharedFileObject) i.next();
            if( attachment.getFile()== null ||
                attachment.getFile().isFile() == false ||
                attachment.getFile().length() == 0 )
            {
                JOptionPane.showMessageDialog(
                        parentFrame,
                        "The message that is currently send (maybe a send retry on next startup of Frost)\n"+
                        "contains a file attachment that does not longer exist, or it is a 0 byte file!\n\n"+
                        "The send of the message was aborted and the message file was deleted\n"+
                        "to prevent another upload try on next startup of Frost.",
                        "Unrecoverable error",
                        JOptionPane.ERROR_MESSAGE);

                msg.getFile().delete();
                return false;
            }
        }

        SharedFileObject failedAttachment = null;
        
        // upload each attachment
        for(Iterator i = fileAttachments.iterator(); i.hasNext(); ) {
            SharedFileObject attachment = (SharedFileObject) i.next();
            if(uploadAttachment(attachment)) {
                // if the attachment was successfully inserted, we update the message on disk.
                msg.save();
                remainingAttachmentsToUploadCount--;
                setAttachmentsToUploadRemainingCount(remainingAttachmentsToUploadCount);
            } else {
                success = false;
                failedAttachment = attachment;
                setAttachmentsToUploadRemainingCount(0);
                break;
            }
        }

        if (!success) {
            JOptionPane.showMessageDialog(
                parentFrame,
                "Attachment '"+failedAttachment.getFilename()+"' failed to upload.\n"+ 
                    "Will retry to upload attachments and message after next startup.",
                "Attachment upload failed",
                JOptionPane.ERROR_MESSAGE);
        }

        return success;
    }

    /**
     * Composes the complete path + filename of a messagefile in the keypool for the given index.
     */
    public String composeMsgFilePath(int index) {
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
    public int findNextFreeUploadIndex(int startIndex) {

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
                if (encryptForRecipient == null && message.compareTo(testMe)) {
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
    private void uploadMessage() throws IOException {

        int index = MessageUploader.uploadMessage(
                message, 
                encryptForRecipient, 
                this, 
                parentFrame, 
                board.getName());

        if( index < 0 ) {
            // upload failed, unsentMessageFile was handled by MessageUploader (kept or deleted, user choosed)
            return;
        }
        
        // upload was successful, move message file to sent folder
        String finalName = composeMsgFileNameWithoutXml(index);
        File sentTarget = new File( Core.frostSettings.getValue("sent.dir") + finalName + ".xml" );

        int counter = 2;
        while( sentTarget.exists() ) {
            // paranoia, target file already exists, append an increasing number
            sentTarget = new File( Core.frostSettings.getValue("sent.dir") + finalName + "-" + counter + ".xml" );
            counter++;
        }

        File unsentMessageFile = message.getFile();
        boolean wasOk = unsentMessageFile.renameTo(sentTarget);
        if( !wasOk ) {
            logger.severe("Error: rename of '"+unsentMessageFile.getPath()+"' into '"+sentTarget.getPath()+"' failed!");
            unsentMessageFile.delete(); // we must delete the file from unsent folder to prevent another upload
        }
    }
}
