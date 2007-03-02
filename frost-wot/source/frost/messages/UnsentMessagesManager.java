/*
  UnsentMessagesManager.java / Frost
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
 * Holds unsent messages, makes changes persistent.
 */
public class UnsentMessagesManager {

    private static Logger logger = Logger.getLogger(UnsentMessagesManager.class.getName());
    
    private static LinkedList<FrostUnsentMessageObject> unsentMessages = new LinkedList<FrostUnsentMessageObject>();
    private static int runningMessageUploads = 0;
    
    private static List<Board> EMPTY_BOARD_LIST = new LinkedList<Board>();

    /**
     * Retrieves all unsend messages from database table.
     */
    public static void initialize() {
        List<FrostUnsentMessageObject> msgs;
        try {
            msgs = AppLayerDatabase.getUnsentMessageTable().retrieveMessages();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving unsend messages", e);
            return;
        }
        if( msgs == null || msgs.size() == 0 ) {
            return;
        }
        
        unsentMessages.addAll(msgs);
        
        // initialize the file attachments to upload
        for(Iterator i = unsentMessages.iterator(); i.hasNext(); ) {
            FrostUnsentMessageObject msg = (FrostUnsentMessageObject) i.next();
            FileAttachmentUploadThread.getInstance().checkAndEnqueueNewMessage(msg);
        }
    }
    
    public static int getUnsentMessageCount() {
        return unsentMessages.size();
    }

    public static List getUnsentMessages() {
        return unsentMessages;
    }
    
    /**
     * Returns a message to upload. The message must have no unsend file attachments.
     * When a message is returned it is dequeued.
     * @param targetBoard  target board for the message
     * @return  a message, or null
     */
    public static FrostUnsentMessageObject getUnsentMessage(Board targetBoard) {
        return getUnsentMessage(targetBoard, null);
    }

    /**
     * Returns a message to upload. The message must have no unsend file attachments.
     * Takes care that the returned message is from the same userName as specified, 
     * because we don't want to send messages from different userNames together, 
     * this compromises anonymity! 
     * @param targetBoard  target board for the message
     * @return  a message, or null
     */
    public static FrostUnsentMessageObject getUnsentMessage(Board targetBoard, String fromName) {
        
        if( Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_UPLOAD_DISABLED) ) {
            return null;
        }

        for(Iterator i = unsentMessages.iterator(); i.hasNext(); ) {
            FrostUnsentMessageObject mo = (FrostUnsentMessageObject) i.next();
            if( mo.getCurrentUploadThread() != null ) {
                continue; // msg is currently uploading
            }
            if( mo.getBoard().getPrimaryKey().intValue() == targetBoard.getPrimaryKey().intValue() ) {
                if( fromName == null || fromName.equals(mo.getFromName()) ) {
                    if( mo.getUnsentFileAttachments().size() == 0 ) {
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
    public static List<Board> getBoardsWithSendableMessages() {
        
        if( Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_UPLOAD_DISABLED) ) {
            return EMPTY_BOARD_LIST;
        }
        
        Hashtable<Integer,Board> ht = new Hashtable<Integer,Board>();
        for(Iterator i = unsentMessages.iterator(); i.hasNext(); ) {
            FrostUnsentMessageObject mo = (FrostUnsentMessageObject) i.next();
            if( mo.getCurrentUploadThread() != null ) {
                continue; // msg is currently uploading
            }
            if( !ht.containsKey(mo.getBoard().getPrimaryKey()) ) {
                ht.put(mo.getBoard().getPrimaryKey(), mo.getBoard());
            }
        }
        List<Board> result = new ArrayList<Board>(ht.values()); 
        return result;
    }

    public static void addNewUnsentMessage(FrostUnsentMessageObject mo) {
        
        mo.setTimeAdded(System.currentTimeMillis());
        
        try {
            AppLayerDatabase.getUnsentMessageTable().insertMessage(mo);
        } catch (SQLException e1) {
            logger.log(Level.SEVERE, "Error inserting unsent message", e1);
        }

        unsentMessages.add(mo);
        
        // enqueue in file attachment upload thread if needed
        FileAttachmentUploadThread.getInstance().checkAndEnqueueNewMessage(mo);
        
        MainFrame.getInstance().getUnsentMessagesPanel().addUnsentMessage(mo);
    }

    /**
     * @return  false if message is currently uploading and delete is not possible
     */
    public static boolean deleteMessage(FrostUnsentMessageObject unsentMsg) {
        
        if( unsentMsg.getCurrentUploadThread() != null ) {
            return false; // msg currently uploaded, delete not possible
        }
        
        try {
            AppLayerDatabase.getUnsentMessageTable().deleteMessage(unsentMsg.getMessageId());
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error during delete of unsend message", ex);
        }
        
        for(Iterator i = unsentMessages.iterator(); i.hasNext(); ) {
            FrostUnsentMessageObject mo2 = (FrostUnsentMessageObject) i.next();
            if( unsentMsg.getMessageId().equals(mo2.getMessageId()) ) {
                i.remove();
                break;
            }
        }
        
        FileAttachmentUploadThread.getInstance().messageWasDeleted(unsentMsg.getMessageId());
        
        MainFrame.getInstance().getUnsentMessagesPanel().removeUnsentMessage(unsentMsg);
        
        return true;
    }

    public static boolean dequeueMessage(FrostUnsentMessageObject unsentMsg) {
        
        for(Iterator i = unsentMessages.iterator(); i.hasNext(); ) {
            FrostUnsentMessageObject mo2 = (FrostUnsentMessageObject) i.next();
            if( unsentMsg.getMessageId().equals(mo2.getMessageId()) ) {
                i.remove();
                break;
            }
        }
        
        MainFrame.getInstance().getUnsentMessagesPanel().removeUnsentMessage(unsentMsg);
        
        return true;
    }

    public static void updateMessageFileAttachmentKey(FrostMessageObject mo, FileAttachment fa) throws SQLException {
        try {
            AppLayerDatabase.getUnsentMessageTable().updateMessageFileAttachmentKey(mo, fa);
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
