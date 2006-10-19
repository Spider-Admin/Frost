/*
  UnsendMessagesManager.java / Frost
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
package frost.messages;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.storage.database.applayer.*;
import frost.threads.*;

/**
 * Holds unsend messages, makes changes persistent.
 */
public class UnsendMessagesManager {

    private static Logger logger = Logger.getLogger(UnsendMessagesManager.class.getName());
    
    private static LinkedList unsendMessages = new LinkedList();
    private static int runningMessageUploads = 0;

    /**
     * Retrieves all unsend messages from database table.
     */
    public static void initialize() {
        List msgs;
        try {
            msgs = AppLayerDatabase.getUnsendMessageTable().retrieveMessages();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving unsend messages", e);
            return;
        }
        if( msgs == null || msgs.size() == 0 ) {
            return;
        }
        
        unsendMessages.addAll(msgs);
        
        // initialize the file attachments to upload
        for(Iterator i = unsendMessages.iterator(); i.hasNext(); ) {
            FrostUnsendMessageObject msg = (FrostUnsendMessageObject) i.next();
            FileAttachmentUploadThread.getInstance().checkAndEnqueueNewMessage(msg);
        }
    }
    
    public static int getUnsendMessageCount() {
        return unsendMessages.size();
    }

    public static List getUnsendMessages() {
        return unsendMessages;
    }
    
    /**
     * Returns a message to upload. The message must have no unsend file attachments.
     * When a message is returned it is dequeued.
     * @param targetBoard  target board for the message
     * @return  a message, or null
     */
    public static FrostUnsendMessageObject getUnsendMessage(Board targetBoard) {
        return getUnsendMessage(targetBoard, null);
    }

    /**
     * Returns a message to upload. The message must have no unsend file attachments.
     * When a message is returned it is dequeued.
     * Takes care that the returned message is from the same userName as specified, 
     * because we don't want to send messages from different userNames together, 
     * this compromises anonymity! 
     * @param targetBoard  target board for the message
     * @return  a message, or null
     */
    public static FrostUnsendMessageObject getUnsendMessage(Board targetBoard, String fromName) {
        for(Iterator i = unsendMessages.iterator(); i.hasNext(); ) {
            FrostUnsendMessageObject mo = (FrostUnsendMessageObject) i.next();
            if( mo.getBoard().getPrimaryKey().intValue() == targetBoard.getPrimaryKey().intValue() ) {
                if( fromName == null || fromName.equals(mo.getFromName()) ) {
                    if( mo.getUnsendFileAttachments().size() == 0 ) {
                        i.remove();
                        return mo;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns a List of all Boards that currently have sendable messages.
     */
    public static List getBoardsWithSendableMessages() {
        Hashtable ht = new Hashtable();
        for(Iterator i = unsendMessages.iterator(); i.hasNext(); ) {
            FrostMessageObject mo = (FrostMessageObject) i.next();
            if( !ht.containsKey(mo.getBoard().getPrimaryKey()) ) {
                ht.put(mo.getBoard().getPrimaryKey(), mo.getBoard());
            }
        }
        List result = new ArrayList(ht.values()); 
        return result;
    }

    public static void addNewUnsendMessage(FrostUnsendMessageObject mo) {
        
        mo.setTimeAdded(System.currentTimeMillis());
        
        try {
            AppLayerDatabase.getUnsendMessageTable().insertMessage(mo);
        } catch (SQLException e1) {
            logger.log(Level.SEVERE, "Error inserting unsend message", e1);
        }

        unsendMessages.add(mo);
        
        // enqueue in file attachment upload thread if needed
        FileAttachmentUploadThread.getInstance().checkAndEnqueueNewMessage(mo);
        
        MainFrame.getInstance().getMessageInfoPanel().addUnsendMessage(mo);
    }

    /**
     * @return  false if message is currently uploading and delete is not possible
     */
    public static boolean deleteMessage(FrostUnsendMessageObject unsendMsg) {
        
        if( unsendMsg.getCurrentUploadThread() != null ) {
            return false; // msg currently uploaded, delete not possible
        }
        
        try {
            AppLayerDatabase.getUnsendMessageTable().deleteMessage(unsendMsg.getMessageId());
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error during delete of unsend message", ex);
        }
        
        for(Iterator i = unsendMessages.iterator(); i.hasNext(); ) {
            FrostUnsendMessageObject mo2 = (FrostUnsendMessageObject) i.next();
            if( unsendMsg.getMessageId().equals(mo2.getMessageId()) ) {
                i.remove();
                break;
            }
        }
        
        FileAttachmentUploadThread.getInstance().messageWasDeleted(unsendMsg.getMessageId());
        
        MainFrame.getInstance().getMessageInfoPanel().removeUnsendMessage(unsendMsg);
        
        return true;
    }
    
    public static void updateMessageFileAttachmentKey(FrostMessageObject mo, FileAttachment fa) throws SQLException {
        try {
            AppLayerDatabase.getUnsendMessageTable().updateMessageFileAttachmentKey(mo, fa);
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error updating table", ex);
        }
    }
    
    public synchronized static int getRunningMessageUploads() {
        return runningMessageUploads;
    }
    public synchronized static void incRunningMessageUploads() {
        runningMessageUploads++;
    }
    public synchronized static void decRunningMessageUploads() {
        runningMessageUploads--;
    }
}
