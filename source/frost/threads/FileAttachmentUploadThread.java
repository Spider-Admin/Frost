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
package frost.threads;

import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.upload.*;
import frost.messages.*;
import frost.util.*;

/**
 * Uploads file attachments from unsend messages and updates db table after successful uploads.
 */
public class FileAttachmentUploadThread extends Thread {

    private static Logger logger = Logger.getLogger(FileAttachmentUploadThread.class.getName());
    
    private static final int wait1minute = 1 * 60 * 1000;
    
    private MessageQueue msgQueue = new MessageQueue();

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

    public void run() {
        
        // monitor and process file attachment uploads
        // we expect an appr. chk file size of 512kb, max. 768kb (because of 0.5, we want no splitfile there)

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        while(true) {
            try {
                // if there is no work in queue this call waits for a new queue item
                MessageFileAttachment msgFileAttachment = msgQueue.getMessageFromQueue();

                if( msgFileAttachment == null ) {
                    // paranoia
                    Mixed.wait(wait1minute);
                    continue;
                } else {
                    // short wait to not to hurt node
                    Mixed.waitRandom(3000);
                }

                FileAttachment fa = msgFileAttachment.getFileAttachment();
                
                if( fa.getInternalFile()== null ||
                    fa.getInternalFile().isFile() == false ||
                    fa.getInternalFile().length() == 0 )
                {
                    JOptionPane.showMessageDialog(
                            MainFrame.getInstance(),
                            "The message that is currently send (maybe a send retry on next startup of Frost)\n"+
                            "contains a file attachment that does not longer exist, or it is a 0 byte file!\n\n"+
                            "The send of the message was aborted and the message file was deleted\n"+
                            "to prevent another upload try on next startup of Frost.\n"+
                            "File: "+fa.getFilename(),
                            "Unrecoverable error",
                            JOptionPane.ERROR_MESSAGE);

                    UnsendMessagesManager.deleteMessage(msgFileAttachment.getMessageObject().getMessageId());
                    
                    continue;
                }
                
System.out.println("FileAttachmentUploadManager: starting upload of file: "+fa.getInternalFile().getPath());

                String chkKey = null;
                try {
                    FcpResultPut result = FcpHandler.inst().putFile(
                            FcpHandler.TYPE_FILE,
                            "CHK@",
                            fa.getInternalFile(),
                            null,
                            true, // doRedirect
                            true, // removeLocalKey, insert with full HTL even if existing in local store
                            new FrostUploadItem());

                    if (result.isSuccess() || result.isKeyCollision()) {
                        chkKey = result.getChkKey();
                    }
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Exception catched",ex);
                }
                
System.out.println("FileAttachmentUploadManager: upload finished, key: "+chkKey);

                if( chkKey != null ) {
                    // upload successful, update message
                    fa.setKey(chkKey);
                    UnsendMessagesManager.updateMessageFileAttachmentKey(
                            msgFileAttachment.getMessageObject(), 
                            msgFileAttachment.getFileAttachment());
                } else {
                    // upload failed, retry
                    msgQueue.appendToQueue(msgFileAttachment);
                }
                
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception catched",t);
                occuredExceptions++;
            }
            
            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FileAttachmentUploadManager because of too much exceptions");
                break;
            }
        }
    }
    
    public void messageWasDeleted(String messageId) {
        // message was deleted, remove all items of this message
        msgQueue.deleteAllItemsOfMessage(messageId);
    }
    
    public void checkAndEnqueueNewMessage(FrostMessageObject msg) {
        LinkedList unsend = msg.getUnsendFileAttachments();
        if( unsend != null && unsend.size() > 0 ) {
            for(Iterator i=unsend.iterator(); i.hasNext(); ) {
                FileAttachment fa = (FileAttachment) i.next();
                MessageFileAttachment mfa = new MessageFileAttachment(msg, fa);
                msgQueue.appendToQueue(mfa);
            }
        }
    }
    
    public int getQueueSize() {
        return msgQueue.getQueueSize();
    }
    
    private class MessageQueue {
        
        private LinkedList queue = new LinkedList();
        
        public synchronized MessageFileAttachment getMessageFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    wait();
                }
            } catch (InterruptedException e) {
                return null; // waiting abandoned
            }
            
            if( queue.isEmpty() == false ) {
                MessageFileAttachment msg = (MessageFileAttachment) queue.removeFirst();
                return msg;
            }
            return null;
        }

        public synchronized void appendToQueue(MessageFileAttachment msg) {
            queue.addLast(msg);
            notifyAll(); // notify all waiters (if any) of new record
        }
        /**
         * Delete all items that reference message mo.
         */
        public synchronized void deleteAllItemsOfMessage(String messageId) {
            for( Iterator i=queue.iterator(); i.hasNext(); ) {
                MessageFileAttachment mfa = (MessageFileAttachment) i.next();
                if( mfa.getMessageObject().getMessageId().equals(messageId) ) {
                    i.remove();
                }
            }
        }
        
        public synchronized int getQueueSize() {
            return queue.size();
        }
    }
    
    private class MessageFileAttachment {
        
        FrostMessageObject messageObject;
        FileAttachment fileAttachment;
        
        public MessageFileAttachment(FrostMessageObject mo, FileAttachment fa) {
            messageObject = mo;
            fileAttachment = fa;
        }

        public FileAttachment getFileAttachment() {
            return fileAttachment;
        }

        public FrostMessageObject getMessageObject() {
            return messageObject;
        }
    }
}
