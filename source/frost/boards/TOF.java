/*
  TOF.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.*;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.messages.VerifyableMessageObject;

public class TOF
{
//    private static Hashtable messages = null;
    private static UpdateTofFilesThread updateThread = null;
    private static UpdateTofFilesThread nextUpdateThread = null;
    
	private static Logger logger = Logger.getLogger(TOF.class.getName());

    /**
     * Gets the content of the message selected in the tofTable.
     * @param e This selectionEv ent is needed to determine if the Table is just being edited
     * @param table The tofTable
     * @param messages A Vector containing all MessageObjects that are just displayed by the table
     * @return The content of the message
     */
    public static FrostMessageObject evalSelection(ListSelectionEvent e, JTable table, Board board)
    {
        MessageTableModel tableModel = (MessageTableModel)table.getModel();
        if( !e.getValueIsAdjusting() && !table.isEditing() )
        {
            int row = table.getSelectedRow();
            if( row != -1 && row < tableModel.getRowCount() )
            {
                FrostMessageObject message = (FrostMessageObject)tableModel.getRow(row);

                if( message != null )
                {
                    // Test if lockfile exists, remove it and update the tree display
                    if( message.isMessageNew() == false )
                    {
                        // its a read message, nothing more to do here ...
                        return message;
                    }
                    
                    // this is a new message
                    message.setMessageNew(false); // mark as read
                    tableModel.updateRow(message);

                    board.decNewMessageCount();

                    MainFrame.getInstance().updateMessageCountLabels(board);
                    MainFrame.getInstance().updateTofTree(board);

                    return message;
                }
            }
        }
        return null;
    }

    /**
     * Resets the NEW state to READ for all messages shown in board table.
     * 
     * @param table  the messages table
     * @param board  the board to reset
     */
    public static void setAllMessagesRead(final JTable table, final Board board)
    {
        Runnable resetter = new Runnable() {
            public void run()
            {
                final MessageTableModel tableModel = (MessageTableModel)table.getModel();
                for(int row=0; row < tableModel.getRowCount(); row++ )
                {
                    final FrostMessageObject message = (FrostMessageObject)tableModel.getRow(row);
                    if( message != null )
                    {
                        // Test if lockfile exists, remove it and update the tree display
                        if( message.isMessageNew() == false )
                        {
                            // its a read message, nothing more to do here ...
                            continue;
                        }

                        // this is a new message
                        message.setMessageNew(false); // mark as read
                        
                        board.decNewMessageCount();
                        
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                tableModel.updateRow(message);
                            }
                        });                
                    }
                }
                // all new messages should be gone now ...
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        MainFrame.getInstance().updateMessageCountLabels(board);
                        MainFrame.getInstance().updateTofTree(board);
                    }
                });                
            } };
        new Thread( resetter ).start();
    }
    
    // called by non-swing thread
    public static void addNewMessageToTable(File newMsgFile, final Board board, boolean markNew)
    {
        JTable table = MainFrame.getInstance().getMessageTable();
        final SortedTableModel tableModel = (SortedTableModel)table.getModel();

        if( (newMsgFile.getName()).endsWith(".xml") &&
             newMsgFile.length() > 0 
             //&& newMsgFile.length() < 32000
          )
        {
            final FrostMessageObject message;
            try {
                message = FrostMessageFactory.createFrostMessageObject(newMsgFile);
            }
            catch(Exception ex)
            {
				logger.log(Level.SEVERE, "Error: skipping to load file '" + newMsgFile.getPath()+
                                         "', reason:\n" + ex.getMessage(), ex);
                return;                      
            }
            if( message.isValid() && !blocked(message, board) )
            {
                if(markNew)
                {
                    message.setMessageNew(true);
                    MainFrame.displayNewMessageIcon(true);
                    board.incNewMessageCount();
                }

                SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            // check if tof table shows this board
                            MainFrame.getInstance().updateTofTree(board);
                            if( MainFrame.getInstance().getSelectedNode().getName().equals( board.getName() ) )
                            {
                                tableModel.addRow(message);
                                MainFrame.getInstance().updateMessageCountLabels(board);
                            }
                        } });
            }
        }
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
    public static void updateTofTable(Board board, String keypool)
    {
        int daysToRead = board.getMaxMessageDisplay();
        // changed to not block the swing thread
        JTable table = MainFrame.getInstance().getMessageTable();

        if( updateThread != null )
        {
            if( updateThread.toString().equals( board ) )
            {
                // already updating
                return;
            }
            else
            {
                // stop actual thread, then start new
                updateThread.cancel();
            }
        }
        // start new thread, the thread will set itself to updateThread,
        // but first it waits before the actual thread is finished
        nextUpdateThread = new UpdateTofFilesThread(board,keypool,daysToRead,table);
        nextUpdateThread.start();
    }

    static class UpdateTofFilesThread extends Thread
    {
        Board board;
        String keypool;
        int daysToRead;
        JTable table;
        SortedTableModel tableModel;
        boolean isCancelled = false;
        String fileSeparator = System.getProperty("file.separator");

        public UpdateTofFilesThread(Board board, String keypool, int daysToRead, JTable table)
        {
            this.board = board;
            this.keypool = keypool;
            this.daysToRead = daysToRead;
            this.table = table;
            this.tableModel = (SortedTableModel)table.getModel();
        }

        public synchronized void cancel()
        {
            isCancelled = true;
        }
        public synchronized boolean isCancel()
        {
            return isCancelled;
        }

        public String toString()
        {
            return board.getName();
        }

        public void run()
        {
            while( updateThread != null )
            {
                // wait for running thread to finish
                Mixed.wait(250);
                if( nextUpdateThread != this )
                {
                    // leave, there is a newer thread than we waiting
                    return;
                }
            }
            // paranoia: are WE the next thread
            if( nextUpdateThread != this )
            {
                return;
            }
            else
            {
                updateThread = this;
            }

            //messages = new Hashtable();

            // Clear tofTable
            final Board innerTargetBoard = board;
            SwingUtilities.invokeLater( new Runnable() {
                    public void run()
                    {
                        // check if tof table shows this board
                        if( MainFrame.getInstance().getSelectedNode().getName().equals( innerTargetBoard.getName() ) )
                        {
                            MessageTableModel model = (MessageTableModel)table.getModel();
                            model.clearDataModel();
                            MainFrame.getInstance().updateMessageCountLabels(innerTargetBoard);
                        }
                    }
                });

            // Get actual date
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

            // Read files up to maxMessages days to the past
            GregorianCalendar firstDate = new GregorianCalendar();
            firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            firstDate.set(Calendar.YEAR, 2001);
            firstDate.set(Calendar.MONTH, 5);
            firstDate.set(Calendar.DATE, 11);
            int msgcount=0;
            int counter = 0;
            //int newMsgCount = 0;
            String targetBoard = board.getBoardFilename();
            while( cal.after(firstDate) && counter < daysToRead )
            {
                String date = DateFun.getDateOfCalendar(cal);
                String filename = new StringBuffer().append(keypool).append(targetBoard).append(fileSeparator).append(date).toString();
                File loadDir = new File(filename);

                if( loadDir.isDirectory() )
                {
                    File[] filePointers = loadDir.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                if( name.endsWith(".xml") )
                                    return true;
                                return false;
                            } });
                    if( filePointers != null )
                    {
                        String sdate = new StringBuffer().append(date).append("-").append(targetBoard).append("-").toString();
                        for( int j = 0; j < filePointers.length; j++ )
                        {
                            if( filePointers[j].length() > 0 &&
                                //filePointers[j].length() < 32000 &&
                                filePointers[j].getName().startsWith(sdate)
                              )
                            {
                                FrostMessageObject message;
                                try {
                                    message = FrostMessageFactory.createFrostMessageObject(filePointers[j]);
                                }
                                catch(Exception ex)
                                {
                                    // skip the file silently
                                    message = null;
                                }
                                if( message != null &&
                                	!message.isDeleted() &&
                                    message.isValid() && 
                                    !blocked(message,board) )
                                {
                                    msgcount++;
                                    //messages.put( message.getIndex() + message.getDateAndTime(), message);
                                    // also update labels each 10 messages (or at end, see below)
                                    boolean updateMessagesCountLabels2 = false;
                                    if(msgcount > 9 && msgcount%10==0)
                                    {
                                        updateMessagesCountLabels2 = true;
                                    }
                                    final boolean updateMessagesCountLabels = updateMessagesCountLabels2;
                                    final FrostMessageObject finalMessage = message;
                                    SwingUtilities.invokeLater( new Runnable() {
                                        public void run()
                                        {
                                            // check if tof table shows this board
                                            if( MainFrame.getInstance().getSelectedNode().getName().equals( innerTargetBoard.getName() ) )
                                            {
                                                tableModel.addRow(finalMessage);
                                                if(updateMessagesCountLabels)
                                                {
                                                    MainFrame.getInstance().updateMessageCountLabels(innerTargetBoard);
                                                    MainFrame.getInstance().updateTofTree(innerTargetBoard);
                                                }
                                            }
                                        }
                                        });
                                }
                            }
                            if( isCancel() )
                            {
                                updateThread = null;
                                return;
                            }
                        }
                    }
                }
                if( isCancel() )
                {
                    updateThread = null;
                    return;
                }
                counter++;
                cal.add(Calendar.DATE, -1);
            }

            SwingUtilities.invokeLater( new Runnable() {
                    public void run()
                    {
                        MainFrame.getInstance().updateTofTree(innerTargetBoard);
                        if( MainFrame.getInstance().getSelectedNode().getName().equals( innerTargetBoard.getName() ) )
                        {
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
	public static boolean blocked(VerifyableMessageObject message, Board board) {
		// TODO: remove this later, is already check on incoming message.
		// this is needed as long such messages are in keypool to block these
		//  and of course its needed if you change the setting Hide unsigned 
		if (message.verifyTime() == false) {
			return true;
		}

		if (board.getShowSignedOnly()
			&& (message.getStatus().indexOf("NONE") > -1 || message.getStatus().indexOf("FAKE") > -1))
			return true;
		if (board.getHideBad() && (message.getStatus().indexOf("BAD") > -1))
			return true;
		if (board.getHideCheck() && (message.getStatus().indexOf("CHECK") > -1))
			return true;
		if (board.getHideNA() && (message.getStatus().indexOf("N/A") > -1))
			return true;
		//If the message is not signed and contains a @ character in the from field, we block it.
		if (message.getStatus().indexOf("NONE") != -1 && message.getFrom().indexOf('@') != -1) {
			return true;
		}

		// Block by subject (and rest of the header)
		if (MainFrame.frostSettings.getBoolValue("blockMessageChecked")) {
			String header =
				(message.getSubject() + message.getDate() + message.getTime()).toLowerCase();
			StringTokenizer blockWords =
				new StringTokenizer(MainFrame.frostSettings.getValue("blockMessage"), ";");
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
		if (MainFrame.frostSettings.getBoolValue("blockMessageBodyChecked")) {
			String content = message.getContent().toLowerCase();
			StringTokenizer blockWords =
				new StringTokenizer(MainFrame.frostSettings.getValue("blockMessageBody"), ";");
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
		if (MainFrame.frostSettings.getBoolValue("blockMessageBoardChecked")) {
			Vector boardsVector = message.getBoardAttachments();
			StringTokenizer blockWords =
				new StringTokenizer(MainFrame.frostSettings.getValue("blockMessageBoard"), ";");
			boolean found = false;
			while (blockWords.hasMoreTokens() && !found) {
				String blockWord = blockWords.nextToken().trim();
				Enumeration boardsEnumeration = boardsVector.elements();
				while (boardsEnumeration.hasMoreElements() && !found) {
					Vector row = (Vector) boardsEnumeration.nextElement();
					String attachedBoardName = ((String) row.get(0)).toLowerCase();
					if ((blockWord.length() > 0) && (attachedBoardName.equals(blockWord))) {
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

    public static void initialSearchNewMessages()
    {
        new SearchAllNewMessages().start();
    }

    private static class SearchAllNewMessages extends Thread
    {
        public void run()
        {
            JTree tree = MainFrame.getInstance().getTofTree();
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            Enumeration e = ((DefaultMutableTreeNode)model.getRoot()).depthFirstEnumeration();
            String keypool = MainFrame.keypool;
            while( e.hasMoreElements() )
            {
                Board board = (Board)e.nextElement();
                searchNewMessages(tree, board);
            }
        }
    }

    public static void initialSearchNewMessages(Board board)
    {
        new SearchNewMessages( board ).start();
    }

    private static class SearchNewMessages extends Thread
    {
        Board board;
        public SearchNewMessages(Board b)
        {
            board = b;
        }
        public void run()
        {
            searchNewMessages( MainFrame.getInstance().getTofTree(), board );
        }
    }

    private static void searchNewMessages(JTree tree, final Board board)
    {
        String keypool = MainFrame.keypool;
        int daysToRead = board.getMaxMessageDisplay();

        int beforeMessages = board.getNewMessageCount(); // remember old val to track if new msg. arrived

        if( board.isFolder() == true )
            return;

        final String boardFilename = board.getBoardFilename();
        final String fileSeparator = System.getProperty("file.separator");

        // Get actual date
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Read files up to maxMessages days to the past
        GregorianCalendar firstDate = new GregorianCalendar();
        firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        firstDate.set(Calendar.YEAR, 2001);
        firstDate.set(Calendar.MONTH, 5);
        firstDate.set(Calendar.DATE, 11);
        int dayCounter = 0;
        int newMessages = 0;

        while( cal.after(firstDate) && dayCounter < daysToRead )
        {
            String date = DateFun.getDateOfCalendar(cal);
            File loadDir = new File(new StringBuffer().append(keypool).append(boardFilename).append(fileSeparator)
                                                      .append(date).toString());
            if( loadDir.isDirectory() )
            {
                File[] filePointers = loadDir.listFiles();
                if( filePointers != null )
                {
                    for( int j = 0; j < filePointers.length; j++ )
                    {
                        if( filePointers[j].getName().endsWith(".xml.lck") )
                        {
                            // search for message
                            String lockFilename = filePointers[j].getName();
                            String messagename = lockFilename.substring(0, lockFilename.length()-4);
                            boolean found = false;
                            int k;
                            for( k=0; k<filePointers.length; k++ )
                            {
                                if( filePointers[k].getName().equals( messagename ) &&
                                    filePointers[k].length() > 0 &&
                                    filePointers[k].length() < 32000
                                  )
                                {
                                    // found message file for lock file
                                    found = true;
                                    break;
                                }
                            }
                            if( found == false ) // messagefile for lockfile not found (paranoia)
                            {
                                filePointers[j].delete();
                                continue;  // next .lck file
                            }
                            FrostMessageObject message;
                            try {
                                message = FrostMessageFactory.createFrostMessageObject(filePointers[k]);
                            }
                            catch(Exception ex)
                            {
                                // skip the file quitely
                                message = null;
                            }
                            if( message != null &&
                                message.isValid() && 
                                !blocked(message,board) )
                            {
                                // update the node that contains new messages
                                newMessages++;
                            }
                            else
                            {
                                // message is blocked, delete newmessage indicator file
                                filePointers[j].delete();
                            }
                        }
                    }
                }
            }
            dayCounter++;
            cal.add(Calendar.DATE, -1); // process previous day
        }
        // count new messages arrived while processing
        int arrivedMessages = board.getNewMessageCount() - beforeMessages;
        if( arrivedMessages > 0 )
            newMessages += arrivedMessages;

        board.setNewMessageCount(newMessages);

        // now a board is finished, update the tree
        SwingUtilities.invokeLater( new Runnable() {
               public void run()
               {
                   MainFrame.getInstance().updateTofTree(board);
               }
           });
    }
}
