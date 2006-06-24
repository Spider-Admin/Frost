/*
  TOF.java / Frost
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
package frost.boards;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.tree.*;

import frost.*;
import frost.fileTransfer.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.*;

/**
 * @pattern Singleton
 *
 * @author $Author$
 * @version $Revision$
 */
public class TOF {

    private static Logger logger = Logger.getLogger(TOF.class.getName());

    private UpdateTofFilesThread updateThread = null;
    private UpdateTofFilesThread nextUpdateThread = null;

    private TofTreeModel tofTreeModel;

    private static boolean initialized = false;

    /**
     * The unique instance of this class.
     */
    private static TOF instance = null;

    /**
     * Return the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static TOF getInstance() {
        return instance;
    }

    /**
     * Prevent instances of this class from being created from the outside.
     * @param tofTreeModel this is the TofTreeModel this TOF will operate on
     */
    private TOF(TofTreeModel tofTreeModel) {
        super();
        this.tofTreeModel = tofTreeModel;
    }

    /**
     * This method initializes the TOF.
     * If it has already been initialized, this method does nothing.
     * @param tofTreeModel this is the TofTreeModel this TOF will operate on
     */
    public static void initialize(TofTreeModel tofTreeModel) {
        if (!initialized) {
            initialized = true;
            instance = new TOF(tofTreeModel);
        }
    }

    /**
     * Resets the NEW state to READ for all messages shown in board table.
     *
     * @param tableModel  the messages table model
     * @param board  the board to reset
     */
    public void setAllMessagesRead(final Board board) {
        // now takes care if board is changed during mark read of many boards! reloads current table if needed
        
        try {
            GuiDatabase.getMessageTable().setAllMessagesRead(board);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error marking all messages read", e);
            return;
        }
        
        // if this board is currently shown, update messages in table
        if( MainFrame.getInstance().getTofTreeModel().getSelectedNode() == board ) {

            final MessageTableModel tableModel = MainFrame.getInstance().getMessageTableModel();
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    for(int row=0; row < tableModel.getRowCount(); row++ ) {
                        final FrostMessageObject message = (FrostMessageObject)tableModel.getRow(row);
                        if( message.isNew() ) {
                            message.setNew(false);
                            tableModel.updateRow(message);
                        }
                    }
                    board.setNewMessageCount( 0 );
                    MainFrame.getInstance().updateMessageCountLabels(board);
                    MainFrame.getInstance().updateTofTree(board);
            }});
        } else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    MainFrame.getInstance().updateMessageCountLabels(board);
                    MainFrame.getInstance().updateTofTree(board);
            }});
        }
    }

    /**
     * Add new msg to database (as invalid), mark download slot used. 
     */
    public void receivedInvalidMessage(Board b, Calendar calDL, int index, String reason) {
        
        // first add to database, then mark slot used. this way its ok if Frost is shut down after add to db but
        // before mark of the slot.
        FrostMessageObject invalidMsg = new FrostMessageObject(b, calDL, index, reason);
        try {
            GuiDatabase.getMessageTable().insertMessage(invalidMsg);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting invalid message into database", e);
            return;
        }
    }
    
    /**
     * Add new msg to database, mark download slot used. 
     */
    public void receivedValidMessage(MessageObjectFile currentMsg, Board board, int index) {
        FrostMessageObject newMsg = new FrostMessageObject(currentMsg, board, index);
        newMsg.setNew(true);
        try {
            GuiDatabase.getMessageTable().insertMessage(newMsg);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting new message into database", e);
            return;
        }
        // after add to database
        processNewMessage(newMsg, board);
    }

    /**
     * Process incoming message.
     */
    private void processNewMessage(FrostMessageObject currentMsg, Board board) {
        if ( blocked(currentMsg, board) ) {
            board.incBlocked();
            logger.info("TOFDN: Blocked message for board '"+board.getName()+"'.");
        } else {
            // check if msg would be displayed (maxMessageDays)
            Calendar minDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            minDate.add(Calendar.DATE, -1*board.getMaxMessageDisplay());
            
            Calendar msgDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            msgDate.setTimeInMillis( currentMsg.getSqlDate().getTime());
            
            if( !msgDate.before(minDate) ) {
                // add new message or notify of arrival
                addNewMessageToTable(currentMsg, board);
            } // else msg is not displayed due to maxMessageDisplay
            
            // add all files indexed files, but never for BAD users
            if( !currentMsg.isMessageStatusBAD() ) {
                Iterator it = currentMsg.getAttachmentsOfType(Attachment.FILE).iterator();
                while (it.hasNext()) {
                    SharedFileObject current = ((FileAttachment)it.next()).getFileObj();
                    if (current.getOwner() != null) {
                        Index fileindex = Index.getInstance();
                        synchronized(fileindex) {
                            fileindex.add(current, board);
                        }
                    }
                }
            }
            // add all boards to the list of known boards
            if( currentMsg.isMessageStatusOLD() &&
                Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_UNSIGNED) == true )
            {
                logger.info("Boards from unsigned message blocked");
            } else if( currentMsg.isMessageStatusBAD() &&
                       Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_BAD) == true )
            {
                logger.info("Boards from BAD message blocked");
            } else if( currentMsg.isMessageStatusCHECK() &&
                       Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_CHECK) == true )
            {
                logger.info("Boards from CHECK message blocked");
            } else if( currentMsg.isMessageStatusOBSERVE() &&
                       Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_OBSERVE) == true )
            {
                logger.info("Boards from OBSERVE message blocked");
            } else if( currentMsg.isMessageStatusTAMPERED() ) {
                logger.info("Boards from TAMPERED message blocked");
            } else {
                // either GOOD user or not blocked by user
                Core.addNewKnownBoards(currentMsg.getAttachmentsOfType(Attachment.BOARD));
            }
        }
    }

    /**
     * called by non-swing thread
     * @param newMsgFile
     * @param board
     * @param markNew
     */
    private void addNewMessageToTable(final FrostMessageObject message, final Board board) {
        
        final SortedTableModel tableModel = MainFrame.getInstance().getMessageTableModel();

        MainFrame.displayNewMessageIcon(true);
        board.incNewMessageCount();

        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                // check if tof table shows this board
                MainFrame.getInstance().updateTofTree(board);
                Board selectedBoard = tofTreeModel.getSelectedNode();
                if( !selectedBoard.isFolder() && selectedBoard.getName().equals( board.getName() ) )
                {
                    tableModel.addRow(message);
                    MainFrame.getInstance().updateMessageCountLabels(board);
                }
            } });
    }

    /**
     * Clears the tofTable, reads in the messages to be displayed,
     * does check validity for each message and adds the messages to
     * table. Additionaly it returns a Vector with all MessageObjects
     * @param board The selected board.
     * @param keypool Frost keypool directory
     * @param daysToRead Maximum age of the messages to be displayed
     * @param table The tofTable.
     * @return Vector containing all MessageObjects that are displayed in the table.
     */
    public void updateTofTable(Board board, String keypool) {
        int daysToRead = board.getMaxMessageDisplay();
        // changed to not block the swing thread
        MessageTableModel tableModel = MainFrame.getInstance().getMessageTableModel();

        if( updateThread != null ) {
            if( updateThread.toString().equals( board ) ) {
                // already updating
                return;
            } else {
                // stop current thread, then start new
                updateThread.cancel();
            }
        }
        // start new thread, the thread will set itself to updateThread,
        // but first it waits before the actual thread is finished
        nextUpdateThread = new UpdateTofFilesThread(board, keypool, daysToRead, tableModel);
        nextUpdateThread.start();
    }

    private class UpdateTofFilesThread extends Thread {

        Board board;
        String keypool;
        int daysToRead;
        SortedTableModel tableModel;
        boolean isCancelled = false;
        String fileSeparator = System.getProperty("file.separator");

        /**
         * @param board
         * @param keypool
         * @param daysToRead
         * @param table
         */
        public UpdateTofFilesThread(Board board, String keypool, int daysToRead, SortedTableModel tableModel) {
            this.board = board;
            this.keypool = keypool;
            this.daysToRead = daysToRead;
            this.tableModel = tableModel;
        }

        public synchronized void cancel() {
            isCancelled = true;
        }

        public synchronized boolean isCancel() {
            return isCancelled;
        }

        public String toString() {
            return board.getName();
        }

        public void run() {
            while( updateThread != null ) {
                // wait for running thread to finish
                Mixed.wait(250);
                if( nextUpdateThread != this ) {
                    // leave, there is a newer thread than we waiting
                    return;
                }
            }
            // paranoia: are WE the next thread?
            if( nextUpdateThread != this ) {
                return;
            } else {
                updateThread = this;
            }

            // lower thread prio to allow users to select and view messages while this thread runs
            try { setPriority(getPriority() - 1); }
            catch(Throwable t) { }

            // clear tofTable
            final Board innerTargetBoard = board;
            SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        // check if tof table shows this board
                        if( tofTreeModel.getSelectedNode().isFolder() == false &&
                            tofTreeModel.getSelectedNode().getName().equals( innerTargetBoard.getName() ) )
                        {
                            tableModel.clearDataModel();
                            MainFrame.getInstance().updateMessageCountLabels(innerTargetBoard);
                        }
                    }
                });

            boolean showDeletedMessages = Core.frostSettings.getBoolValue("showDeletedMessages");
            
            final List messages;
            try {
                // TODO: maybe receive without content and dynamically load contents if needed
                // TODO: if we do this, blocked can't check the mesagebody!
                messages = GuiDatabase.getMessageTable().retrieveMessages(board, daysToRead, true, true, showDeletedMessages);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error retrieving messages for board "+board.getName(), e);
                return;
            }
            
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    if( tofTreeModel.getSelectedNode().isFolder() == false &&
                        tofTreeModel.getSelectedNode().getName().equals( innerTargetBoard.getName() ) ) {
                        
                        for(Iterator i=messages.iterator(); i.hasNext(); ) {
                            FrostMessageObject msg = (FrostMessageObject)i.next();
                            tableModel.addRow(msg);
                        }
                        
                        MainFrame.getInstance().updateTofTree(innerTargetBoard);
                        MainFrame.getInstance().updateMessageCountLabels(innerTargetBoard);
                    }
                }
            });
            
            updateThread = null;
        }
    }

    /**
     * Returns true if the message should not be displayed
     * @param message The message object to check
     * @return true if message is blocked, else false
     */
    public boolean blocked(FrostMessageObject message, Board board) {

        if (board.getShowSignedOnly()
            && (message.isMessageStatusOLD() || message.isMessageStatusTAMPERED()) )
        {
            return true;
        }
        if (board.getHideBad() && message.isMessageStatusBAD()) {
            return true;
        }
        if (board.getHideCheck() && message.isMessageStatusCHECK()) {
            return true;
        }
        if (board.getHideObserve() && message.isMessageStatusOBSERVE()) {
            return true;
        }
        //If the message is not signed and contains a @ character in the from field, we block it.
        if (message.isMessageStatusOLD() && message.getFromName().indexOf('@') > -1) {
            return true;
        }

        // TODO: maybe allow regexp here?!

        // Block by subject (and rest of the header)
        if (Core.frostSettings.getBoolValue("blockMessageChecked")) {
            String header = message.getSubject().toLowerCase();
            StringTokenizer blockWords = new StringTokenizer(Core.frostSettings.getValue("blockMessage"), ";");
            boolean found = false;
            while (blockWords.hasMoreTokens() && !found) {
                String blockWord = blockWords.nextToken().trim();
                if ((blockWord.length() > 0) && (header.indexOf(blockWord) != -1)) {
                    found = true;
                }
            }
            if (found) {
                return true;
            }
        }
        // Block by body
        if (Core.frostSettings.getBoolValue("blockMessageBodyChecked")) {
            String content = message.getContent().toLowerCase();
            StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue("blockMessageBody"), ";");
            boolean found = false;
            while (blockWords.hasMoreTokens() && !found) {
                String blockWord = blockWords.nextToken().trim();
                if ((blockWord.length() > 0) && (content.indexOf(blockWord) != -1)) {
                    found = true;
                }
            }
            if (found) {
                return true;
            }
        }
        // Block by attached boards
        if (Core.frostSettings.getBoolValue("blockMessageBoardChecked")) {
            List boards = message.getAttachmentsOfType(Attachment.BOARD);
            StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue("blockMessageBoard"), ";");
            boolean found = false;
            while (blockWords.hasMoreTokens() && !found) {
                String blockWord = blockWords.nextToken().trim();
                Iterator boardsIterator = boards.iterator();
                while (boardsIterator.hasNext()) {
                    BoardAttachment boardAttachment = (BoardAttachment) boardsIterator.next();
                    Board boardObject = boardAttachment.getBoardObj();
                    if ((blockWord.length() > 0) && (boardObject.getName().equalsIgnoreCase(blockWord))) {
                        found = true;
                    }
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    public void initialSearchNewMessages() {
        new SearchAllNewMessages().start();
    }

    private class SearchAllNewMessages extends Thread {
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Enumeration e = ((DefaultMutableTreeNode) tofTreeModel.getRoot()).depthFirstEnumeration();
            while( e.hasMoreElements() ) {
                Board board = (Board)e.nextElement();
                searchNewMessages(board);
            }
        }
    }

    /**
     * @param board
     */
    public void initialSearchNewMessages(Board board) {
        new SearchNewMessages( board ).start();
    }

    private class SearchNewMessages extends Thread
    {
        private Board board;
        /**
         * @param b
         */
        public SearchNewMessages(Board b) {
            board = b;
        }
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            searchNewMessages(board);
        }
    }

    /**
     * @param board
     */
    private void searchNewMessages(final Board board) {
        if( board.isFolder() == true ) {
            return;
        }

        int daysToRead = board.getMaxMessageDisplay();

        int beforeMessages = board.getNewMessageCount(); // remember old val to track if new msg. arrived
        
        int newMessages = 0;
        try {
            newMessages = GuiDatabase.getMessageTable().getNewMessageCount(board, daysToRead);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving new message count", e);
        }

        // count new messages arrived while processing
        int arrivedMessages = board.getNewMessageCount() - beforeMessages;
        if( arrivedMessages > 0 )
            newMessages += arrivedMessages;

        board.setNewMessageCount(newMessages);

        // now a board is finished, update the tree
        SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                   MainFrame.getInstance().updateTofTree(board);
               }
           });
    }
}
