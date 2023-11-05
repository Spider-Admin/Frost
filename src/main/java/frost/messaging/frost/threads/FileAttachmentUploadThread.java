/*
  FileAttachmentUploadManager.java / Frost
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
package frost.messaging.frost.threads;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.Core;
import frost.MainFrame;
import frost.fcp.FcpHandler;
import frost.fcp.FcpResultPut;
import frost.fileTransfer.upload.FrostUploadItem;
import frost.messaging.frost.FileAttachment;
import frost.messaging.frost.FrostUnsentMessageObject;
import frost.messaging.frost.UnsentMessagesManager;
import frost.util.Mixed;
import frost.util.gui.translation.Language;

/**
 * Uploads file attachments from unsend messages and updates db table after successful uploads.
 */
public class FileAttachmentUploadThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(FileAttachmentUploadThread.class);

    private static final int wait1minute = 1 * 60 * 1000;

    private final MessageQueue msgQueue = new MessageQueue();

    // one and only instance
    private static FileAttachmentUploadThread instance = new FileAttachmentUploadThread();

    private FileAttachmentUploadThread() {
    }

    public static FileAttachmentUploadThread getInstance() {
        return instance;
    }

    public boolean cancelThread() {
        return false;
    }

    @Override
    public void run() {

        // monitor and process file attachment uploads
        // we expect an appr. chk file size of 512kb, max. 768kb

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        while(true) {
            try {
                while( Core.isFreenetOnline() == false ) {
                    Mixed.wait(1*60*1000); // wait 1 minute
                }
                // if there is no work in queue this call waits for a new queue item
                final MessageFileAttachment msgFileAttachment = msgQueue.getMessageFromQueue();

                if( msgFileAttachment == null ) {
                    // paranoia
                    Mixed.wait(wait1minute);
                    continue;
                } else {
                    // short wait to not to hurt node
                    Mixed.waitRandom(3000);
                }

                final FileAttachment fa = msgFileAttachment.getFileAttachment();

                if( fa.getInternalFile()== null ||
                    fa.getInternalFile().isFile() == false ||
                    fa.getInternalFile().length() == 0 )
                {
                    final Language language = Language.getInstance();
                    final String title = language.getString("FileAttachmentUploadThread.fileNotFoundError.title");
                    final String txt = language.formatMessage("FileAttachmentUploadThread.fileNotFoundError.text", fa.getFileName());
                    JOptionPane.showMessageDialog(
                            MainFrame.getInstance(),
                            txt,
                            title,
                            JOptionPane.ERROR_MESSAGE);

                    logger.warn("FileAttachmentUploadThread: unsent file attachment disappeared: {}; {}", fa.getInternalFile(), fa.getFileName());

                    UnsentMessagesManager.deleteMessage(msgFileAttachment.getMessageObject());

                    continue;
                }

                if( msgFileAttachment.isDeleted() ) {
                    continue; // drop
                }

                logger.info("Starting upload of file: {}", fa.getInternalFile().getPath());

                String chkKey = null;
                try {
                    final FcpResultPut result = FcpHandler.inst().putFile(
                            FcpHandler.TYPE_FILE,
                            "CHK@",
                            fa.getInternalFile(),
                            true,
                            new FrostUploadItem());

                    if (result.isSuccess() || result.isKeyCollision()) {
                        chkKey = result.getChkKey();
                    }
                } catch (final Exception ex) {
                    logger.error("Exception catched", ex);
                }

                logger.info("Finished upload of {}, key: {}", fa.getInternalFile(), chkKey);

                // if the assiciated msg was deleted by user, forget all updates
                if( !msgFileAttachment.isDeleted() ) {
                    if( chkKey != null ) {
                        // upload successful, update message
                        fa.setKey(chkKey);
                        UnsentMessagesManager.updateMessageFileAttachmentKey(
                                msgFileAttachment.getMessageObject(),
                                msgFileAttachment.getFileAttachment());
                    } else {
                        // upload failed, retry
                        msgQueue.appendToQueue(msgFileAttachment);
                    }
                }

            } catch(final Throwable t) {
                logger.error("Exception catched", t);
                occuredExceptions++;
            }

            if( occuredExceptions > maxAllowedExceptions ) {
                logger.error("Stopping because of too much exceptions");
                break;
            }
        }
    }

    public void messageWasDeleted(final String messageId) {
        // message was deleted, remove all items of this message
        msgQueue.deleteAllItemsOfMessage(messageId);
    }

    public void checkAndEnqueueNewMessage(final FrostUnsentMessageObject msg) {
        final LinkedList<FileAttachment> unsend = msg.getUnsentFileAttachments();
        if( unsend != null && unsend.size() > 0 ) {
            for( final FileAttachment fa : unsend ) {
                final MessageFileAttachment mfa = new MessageFileAttachment(msg, fa);
                msgQueue.appendToQueue(mfa);
            }
        }
    }

    public int getQueueSize() {
        return msgQueue.getQueueSize();
    }

    private class MessageQueue {

        private final LinkedList<MessageFileAttachment> queue = new LinkedList<MessageFileAttachment>();

        public synchronized MessageFileAttachment getMessageFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    wait();
                }
            } catch (final InterruptedException e) {
                return null; // waiting abandoned
            }

            if( queue.isEmpty() == false ) {
                final MessageFileAttachment msg = queue.removeFirst();
                return msg;
            }
            return null;
        }

        public synchronized void appendToQueue(final MessageFileAttachment msg) {
            queue.addLast(msg);
            notifyAll(); // notify all waiters (if any) of new record
        }
        /**
         * Delete all items that reference message mo.
         */
        public synchronized void deleteAllItemsOfMessage(final String messageId) {
            for( final Iterator<MessageFileAttachment> i=queue.iterator(); i.hasNext(); ) {
                final MessageFileAttachment mfa = i.next();
                if( mfa.getMessageObject().getMessageId().equals(messageId) ) {
                    mfa.setDeleted(true);
                    i.remove();
                }
            }
        }

        public synchronized int getQueueSize() {
            return queue.size();
        }
    }

    private class MessageFileAttachment {

        private final FrostUnsentMessageObject messageObject;
        private final FileAttachment fileAttachment;

        private boolean isDeleted = false;

        public MessageFileAttachment(final FrostUnsentMessageObject mo, final FileAttachment fa) {
            messageObject = mo;
            fileAttachment = fa;
        }

        public FileAttachment getFileAttachment() {
            return fileAttachment;
        }

        public FrostUnsentMessageObject getMessageObject() {
            return messageObject;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public void setDeleted(final boolean isDeleted) {
            this.isDeleted = isDeleted;
        }
    }
}
