/*
  frame1.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  Some changes by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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

package frost;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;

import frost.boards.*;
import frost.components.BrowserFrame;
import frost.components.translate.TranslateFrame;
import frost.ext.JSysTrayIcon;
import frost.fileTransfer.download.*;
import frost.fileTransfer.search.FrostSearchItem;
import frost.fileTransfer.upload.UploadPanel;
import frost.gui.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.gui.preferences.OptionsFrame;
import frost.identities.*;
import frost.messages.*;
import frost.storage.StorageException;
import frost.threads.maintenance.Truster;
import frost.util.gui.*;
import frost.util.gui.translation.*;

  /**
  * TODO: rework identities stuff + save to xml
  *       - save identities together (not separated friends,enemies)
  *       - each identity have 3 states: GOOD, BAD, NEUTRAL
  *       - filter out enemies on read of messages
  *       - after removing a board, let actual board selected (currently if you 
  *          delete another than selected board the tofTree is updated) 
  * @author $Author$
  * @version $Revision$
  */
public class MainFrame extends JFrame implements ClipboardOwner, SettingsUpdater {
	/**
	 * This listener changes the 'updating' state of a board if a thread starts/finishes.
	 * It also launches popup menus
	 */
	private class Listener extends WindowAdapter {
		
		/* (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
		 */
		public void windowClosing(WindowEvent e) {
			// save size,location and state of window
			Rectangle bounds = getBounds();
			boolean isMaximized = ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);

			frostSettings.setValue("lastFrameMaximized", isMaximized);

			if (!isMaximized) { //Only saves the dimension if it is not maximized
				frostSettings.setValue("lastFrameHeight", bounds.height);
				frostSettings.setValue("lastFrameWidth", bounds.width);
				frostSettings.setValue("lastFramePosX", bounds.x);
				frostSettings.setValue("lastFramePosY", bounds.y);
			}

			fileExitMenuItem_actionPerformed(null);
		}

	} // end of class popuplistener

	/**
	 * 
	 */
	private class MessagePanel extends JPanel {

		/**
		 *  
		 */
		private class Listener
			extends MouseAdapter
			implements
				ActionListener,
				ListSelectionListener,
				PropertyChangeListener,
				TreeSelectionListener,
				TreeModelListener,
				LanguageListener,
				KeyListener
				{
			
			/**
			 * 
			 */
			public Listener() {
				super();
			}

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == updateButton) {
					updateButton_actionPerformed(e);
				}
				if (e.getSource() == newMessageButton) {
					newMessageButton_actionPerformed(e);
				}
				if (e.getSource() == downloadAttachmentsButton) {
					downloadAttachments();
				}
				if (e.getSource() == downloadBoardsButton) {
					downloadBoards();
				}
				if (e.getSource() == replyButton) {
					replyButton_actionPerformed(e);
				}
				if (e.getSource() == saveMessageButton) {
					saveMessageButton_actionPerformed(e);
				}
				if (e.getSource() == trustButton) {
					trustButton_actionPerformed(e);
				}
				if (e.getSource() == notTrustButton) {
					notTrustButton_actionPerformed(e);
				}
				if (e.getSource() == checkTrustButton) {
					checkTrustButton_actionPerformed(e);
				}
			}

			/**
			 * @param e
			 */
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (e.getComponent() == messageTextArea) {
						showTofTextAreaPopupMenu(e);
					}
					if (e.getComponent() == messageTable) {
						showMessageTablePopupMenu(e);
					}
					if (e.getComponent() == boardsTable) {
						showAttachedBoardsPopupMenu(e);
					}
					if (e.getComponent() == filesTable) {
						showAttachedFilesPopupMenu(e);
					}
					//if leftbtn double click on message show this message 
					//in popup window
				}else if(SwingUtilities.isLeftMouseButton(e)){
					if(e.getClickCount() == 2 &&
							e.getComponent() == messageTable )
						showCurrentMessagePopupWindow();
				}
			}

			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			private void maybeDoSomething(KeyEvent e){
				if(e.getSource() == messageTable &&
						e.getKeyChar() == KeyEvent.VK_DELETE)
					deleteSelectedMessage();
			}
			
			public void keyTyped(KeyEvent e){
				//Nothing here
			}
			
			public void keyPressed(KeyEvent e){
				maybeDoSomething(e);
			}
			
			
			public void keyReleased(KeyEvent e){
				//Nothing here
			}
						
			/* (non-Javadoc)
			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
			 */
			public void valueChanged(ListSelectionEvent e) {
				messageTable_itemSelected(e);
			}

			/* (non-Javadoc)
			 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("messageBodyAA")) {
					antialiasing_propertyChanged(evt);
				}
				if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_NAME)) {
					fontChanged();
				}
				if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_SIZE)) {
					fontChanged();
				}
				if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_STYLE)) {
					fontChanged();
				}
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
			 */
			public void valueChanged(TreeSelectionEvent e) {
				boardsTree_actionPerformed(e);
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
			 */
			public void treeNodesChanged(TreeModelEvent e) {
				boardsTreeNode_Changed(e);
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
			 */
			public void treeNodesInserted(TreeModelEvent e) {
				//Nothing here				
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
			 */
			public void treeNodesRemoved(TreeModelEvent e) {
				//Nothing here					
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
			 */
			public void treeStructureChanged(TreeModelEvent e) {
				//Nothing here						
			}

			/* (non-Javadoc)
			 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
			 */
			public void languageChanged(LanguageEvent event) {
				refreshLanguage();				
			}

		}
		/**
		 * 
		 */
		private class PopupMenuAttachmentBoard
			extends JSkinnablePopupMenu
			implements ActionListener, LanguageListener {

			private JMenuItem cancelItem = new JMenuItem();
			private JMenuItem saveBoardItem = new JMenuItem();
			private JMenuItem saveBoardsItem = new JMenuItem();
			
			/**
			 * 
			 */
			public PopupMenuAttachmentBoard() {
				super();
				initialize();
			}
			
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == saveBoardsItem) {
					saveBoards();
				}
				if (e.getSource() == saveBoardItem) {
					saveBoard();
				}
			}
			
			/**
			 * 
			 */
			private void initialize() {
				refreshLanguage();

				saveBoardsItem.addActionListener(this);
				saveBoardItem.addActionListener(this);
			}
			
			/* (non-Javadoc)
			 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
			 */
			public void languageChanged(LanguageEvent event) {
				refreshLanguage();
			}
			
			/**
			 * 
			 */
			private void refreshLanguage() {
				saveBoardsItem.setText(language.getString("Add Board(s)"));
				saveBoardItem.setText(language.getString("Add selected board"));
				cancelItem.setText(language.getString("Cancel"));
			}

			/**
			 * 
			 */
			private void saveBoard() {
				downloadBoards();
			}
			
			/**
			 * 
			 */
			private void saveBoards() {
				downloadBoards();
			}

			/* (non-Javadoc)
			 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
			 */
			public void show(Component invoker, int x, int y) {
				removeAll();

				if (boardsTable.getSelectedRow() == -1) {
					add(saveBoardsItem);
				} else {
					add(saveBoardItem);
				}
				addSeparator();
				add(cancelItem);

				super.show(invoker, x, y);
			}
		}
		
		/**
		 * 
		 */
		private class PopupMenuAttachmentTable
			extends JSkinnablePopupMenu
			implements ActionListener, LanguageListener {

			private JMenuItem cancelItem = new JMenuItem();
			private JMenuItem saveAttachmentItem = new JMenuItem();
			private JMenuItem saveAttachmentsItem = new JMenuItem();
			
			/**
			 * @throws java.awt.HeadlessException
			 */
			public PopupMenuAttachmentTable() throws HeadlessException {
				super();
				initialize();
			}
			
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == saveAttachmentsItem) {
					saveAttachments();
				}
				if (e.getSource() == saveAttachmentItem) {
					saveAttachment();
				}
			}
			
			/**
			 * 
			 */
			private void initialize() {
				refreshLanguage();

				saveAttachmentsItem.addActionListener(this);
				saveAttachmentItem.addActionListener(this);
			}
			
			/* (non-Javadoc)
			 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
			 */
			public void languageChanged(LanguageEvent event) {
				refreshLanguage();
			}
			
			/**
			 * 
			 */
			private void refreshLanguage() {
				saveAttachmentsItem.setText(language.getString("Download attachment(s)"));
				saveAttachmentItem.setText(
						language.getString("Download selected attachment"));
				cancelItem.setText(language.getString("Cancel"));
			}

			/**
			 * 
			 */
			private void saveAttachment() {
				downloadAttachments();
			}
			
			/**
			 * 
			 */
			private void saveAttachments() {
				downloadAttachments();
			}

			/* (non-Javadoc)
			 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
			 */
			public void show(Component invoker, int x, int y) {
				removeAll();

				if (filesTable.getSelectedRow() == -1) {
					add(saveAttachmentsItem);
				} else {
					add(saveAttachmentItem);
				}
				addSeparator();
				add(cancelItem);

				super.show(invoker, x, y);
			}
		}
		
		/**
		 * 
		 */
		private class PopupMenuMessageTable
			extends JSkinnablePopupMenu
			implements ActionListener, LanguageListener {

			private JMenuItem cancelItem = new JMenuItem();

			private JMenuItem markAllMessagesReadItem = new JMenuItem();
			private JMenuItem markMessageUnreadItem = new JMenuItem();
			private JMenuItem setBadItem = new JMenuItem();
			private JMenuItem setCheckItem = new JMenuItem();
			private JMenuItem setGoodItem = new JMenuItem();
			
			private JMenuItem deleteItem = new JMenuItem();
			private JMenuItem undeleteItem = new JMenuItem();

			/**
			 * 
			 */
			public PopupMenuMessageTable() {
				super();
				initialize();
			}

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == markMessageUnreadItem) {
					markMessageUnread();
				}
				if (e.getSource() == markAllMessagesReadItem) {
					markAllMessagesRead();
				}
				if (e.getSource() == setGoodItem) {
					setGood();
				}
				if (e.getSource() == setBadItem) {
					setBad();
				}
				if (e.getSource() == setCheckItem) {
					setCheck();
				}
				if (e.getSource() == deleteItem) {
					deleteMessage();
				}
				if (e.getSource() == undeleteItem) {
					undeleteMessage();
				}
			}

			/**
			 * 
			 */
			private void deleteMessage() {
				deleteSelectedMessage();				
			}
			
			/**
			 * 
			 *
			 */
			private void undeleteMessage(){
				undeleteSelectedMessage();
			}
			
			/**
			 * 
			 */
			private void initialize() {
				refreshLanguage();

				markMessageUnreadItem.addActionListener(this);
				markAllMessagesReadItem.addActionListener(this);
				setGoodItem.addActionListener(this);
				setBadItem.addActionListener(this);
				setCheckItem.addActionListener(this);
				deleteItem.addActionListener(this);
				undeleteItem.addActionListener(this);
			}

			/* (non-Javadoc)
			 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
			 */
			public void languageChanged(LanguageEvent event) {
				refreshLanguage();
			}

			/**
			 * 
			 */
			private void markAllMessagesRead() {
				TOF.getInstance().setAllMessagesRead(getMessageTable(), tofTreeModel.getSelectedNode());
			}

			/**
			 * 
			 */
			private void markMessageUnread() {
				markSelectedMessageUnread();
			}

			/**
			 * 
			 */
			private void refreshLanguage() {
				markMessageUnreadItem.setText(language.getString("Mark message unread"));
				markAllMessagesReadItem.setText(
						language.getString("Mark ALL messages read"));
				setGoodItem.setText(language.getString("help user (sets to GOOD)"));
				setBadItem.setText(language.getString("block user (sets to BAD)"));
				setCheckItem.setText(language.getString("set to neutral (CHECK)"));
				deleteItem.setText(language.getString("Delete message"));
				undeleteItem.setText(language.getString("Undelete message"));
				cancelItem.setText(language.getString("Cancel"));
			}

			/**
			 * 
			 */
			private void setBad() {
				setMessageTrust(new Boolean(false));
			}

			/**
			 * 
			 */
			private void setCheck() {
				setMessageTrust(null);
			}

			/**
			 * 
			 */
			private void setGood() {
				setMessageTrust(new Boolean(true));
			}
			/* (non-Javadoc)
			 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
			 */
			public void show(Component invoker, int x, int y) {
				if (!tofTreeModel.getSelectedNode().isFolder()) {
					removeAll();

					if (messageTable.getSelectedRow() > -1) {
						add(markMessageUnreadItem);
					}
					add(markAllMessagesReadItem);
					addSeparator();
					add(setGoodItem);
					add(setCheckItem);
					add(setBadItem);
					setGoodItem.setEnabled(false);
					setCheckItem.setEnabled(false);
					setBadItem.setEnabled(false);
					
					if (messageTable.getSelectedRow() > -1 && selectedMessage != null) {
						//fscking html on all these..
						if (selectedMessage.getStatus().indexOf(VerifyableMessageObject.VERIFIED)
							> -1) {
							setCheckItem.setEnabled(true);
							setBadItem.setEnabled(true);
						} else if (
							selectedMessage.getStatus().indexOf(VerifyableMessageObject.PENDING)
								> -1) {
							setGoodItem.setEnabled(true);
							setBadItem.setEnabled(true);
						} else if (
							selectedMessage.getStatus().indexOf(VerifyableMessageObject.FAILED)
								> -1) {
							setGoodItem.setEnabled(true);
							setCheckItem.setEnabled(true);
						} else
							logger.warning(
								"invalid message state : " + selectedMessage.getStatus());
					}
	
					if (selectedMessage != null) {
						addSeparator();
						add(deleteItem);
						add(undeleteItem);
						deleteItem.setEnabled(false);
						undeleteItem.setEnabled(false);
						if(selectedMessage.isDeleted()){
							undeleteItem.setEnabled(true);
						}else{
							deleteItem.setEnabled(true);
						}
					}
					
					addSeparator();
					add(cancelItem);
					// ATT: misuse of another menuitem displaying 'Cancel' ;)
					super.show(invoker, x, y);
				}
			}

		}

		/**
		 * 
		 */
		private class PopupMenuTofText
			extends JSkinnablePopupMenu
			implements ActionListener, LanguageListener, ClipboardOwner {

			private Clipboard clipboard;
			
			private JTextComponent sourceTextComponent;
			
			private JMenuItem copyItem = new JMenuItem();
			private JMenuItem cancelItem = new JMenuItem();
			private JMenuItem saveMessageItem = new JMenuItem();

			/**
			 * @param sourceTextComponent
			 */
			public PopupMenuTofText(JTextComponent sourceTextComponent) {
				super();
				this.sourceTextComponent = sourceTextComponent;
				initialize();
			}

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == saveMessageItem) {
					saveMessage();
				}
				if (e.getSource() == copyItem) {
					copySelectedText();
				}
			}

			/**
			 * 
			 */
			private void copySelectedText() {
				StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
				clipboard.setContents(selection, this);
			}

			/**
			 * 
			 */
			private void initialize() {
				refreshLanguage();
				
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				clipboard = toolkit.getSystemClipboard();

				copyItem.addActionListener(this);
				saveMessageItem.addActionListener(this);

				add(copyItem);
				addSeparator();
				add(saveMessageItem);
				addSeparator();
				add(cancelItem);
			}

			/* (non-Javadoc)
			 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
			 */
			public void languageChanged(LanguageEvent event) {
				refreshLanguage();
			}

			/**
			 * 
			 */
			private void refreshLanguage() {
				copyItem.setText(language.getString("Copy"));
				saveMessageItem.setText(language.getString("Save message to disk"));
				cancelItem.setText(language.getString("Cancel"));
			}

			/**
			 * 
			 */
			private void saveMessage() {
				FileAccess.saveDialog(
					parentFrame,
					sourceTextComponent.getText(),
					settings.getValue("lastUsedDirectory"),
					language.getString("Save message to disk"));
			}

			/* (non-Javadoc)
			 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
			 */
			public void show(Component invoker, int x, int y) {
				if ((selectedMessage != null) && (selectedMessage.getContent() != null)) {
					if (sourceTextComponent.getSelectedText() != null) {
						copyItem.setEnabled(true);
					} else {
						copyItem.setEnabled(false);
					}
					super.show(invoker, x, y);
				}
			}

			/* (non-Javadoc)
			 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
			 */
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				// Nothing here
			}

		}

		private Logger logger = Logger.getLogger(MainFrame.MessagePanel.class.getName());
		
		private SettingsClass settings;
		private Language language;
		private FrostIdentities identities;
		private JFrame parentFrame;

		private boolean initialized = false;

		private Listener listener = new Listener();
		
		private FrostMessageObject selectedMessage = new FrostMessageObject();
		private String lastSelectedMessage;

		private PopupMenuAttachmentBoard popupMenuAttachmentBoard = null;
		private PopupMenuAttachmentTable popupMenuAttachmentTable = null;
		private PopupMenuMessageTable popupMenuMessageTable = null;
		private PopupMenuTofText popupMenuTofText = null;

		private JButton checkTrustButton =
			new JButton(new ImageIcon(getClass().getResource("/data/check.gif")));
		private JButton downloadAttachmentsButton =
			new JButton(new ImageIcon(getClass().getResource("/data/attachment.gif")));
		private JButton downloadBoardsButton =
			new JButton(new ImageIcon(getClass().getResource("/data/attachmentBoard.gif")));
		private JButton newMessageButton =
			new JButton(new ImageIcon(getClass().getResource("/data/newmessage.gif")));
		private JButton notTrustButton =
			new JButton(new ImageIcon(getClass().getResource("/data/nottrust.gif")));
		private JButton replyButton =
			new JButton(new ImageIcon(getClass().getResource("/data/reply.gif")));
		private JButton saveMessageButton =
			new JButton(new ImageIcon(getClass().getResource("/data/save.gif")));
		private JButton trustButton =
			new JButton(new ImageIcon(getClass().getResource("/data/trust.gif")));
		private JButton updateButton =
			new JButton(new ImageIcon(getClass().getResource("/data/update.gif")));

		private AntialiasedTextArea messageTextArea = null;
		private JSplitPane mainSplitPane = null;
		private JSplitPane messageSplitPane = null;
		private JSplitPane attachmentsSplitPane = null;
		private JTable filesTable = null;
		private JTable boardsTable = null;
		private JScrollPane filesTableScrollPane;
		private JScrollPane boardsTableScrollPane;

		/**
		 * @param settings
		 */
		public MessagePanel(SettingsClass settings) {
			super();
			this.settings = settings;
			language = Language.getInstance();
			
			settings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, listener);
			settings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, listener);
			settings.addPropertyChangeListener(
				SettingsClass.MESSAGE_BODY_FONT_STYLE,
				listener);
			settings.addPropertyChangeListener("messageBodyAA", listener);
		}
		
		/**
		 * @param e
		 */
		private void checkTrustButton_actionPerformed(ActionEvent e) {
			trustButton.setEnabled(false);
			notTrustButton.setEnabled(false);
			checkTrustButton.setEnabled(false);
			if (selectedMessage != null) {
				startTruster(selectedMessage);
			}
		}

		/**
		 * Adds either the selected or all files from the attachmentTable to downloads table.
		 */
		public void downloadAttachments() {
			int[] selectedRows = filesTable.getSelectedRows();

			// If no rows are selected, add all attachments to download table
			if (selectedRows.length == 0) {
				Iterator it =
					selectedMessage.getAttachmentList().getAllOfType(Attachment.FILE).iterator();
				while (it.hasNext()) {
					FileAttachment fa = (FileAttachment) it.next();
					SharedFileObject sfo = fa.getFileObj();
					FrostSearchItem fsio =
						new FrostSearchItem(
							tofTreeModel.getSelectedNode(),
							sfo,
							FrostSearchItem.STATE_NONE);
					//FIXME: <-does this matter?
					FrostDownloadItem dlItem = new FrostDownloadItem(fsio);
					boolean added = getDownloadModel().addDownloadItem(dlItem);
				}

			} else {
				LinkedList attachments =
					selectedMessage.getAttachmentList().getAllOfType(Attachment.FILE);
				for (int i = 0; i < selectedRows.length; i++) {
					FileAttachment fo = (FileAttachment) attachments.get(selectedRows[i]);
					SharedFileObject sfo = fo.getFileObj();
					FrostSearchItem fsio =
						new FrostSearchItem(
							tofTreeModel.getSelectedNode(),
							sfo,
							FrostSearchItem.STATE_NONE);
					FrostDownloadItem dlItem = new FrostDownloadItem(fsio);
					boolean added = getDownloadModel().addDownloadItem(dlItem);
				}
			}
		}
		
		/**
		 * Adds all boards from the attachedBoardsTable to board list.
		 */
		private void downloadBoards() {
			logger.info("adding boards");
			int[] selectedRows = boardsTable.getSelectedRows();

			if (selectedRows.length == 0) {
				// add all rows
				boardsTable.selectAll();
				selectedRows = boardsTable.getSelectedRows();
				if (selectedRows.length == 0)
					return;
			}
			LinkedList boards = selectedMessage.getAttachmentList().getAllOfType(Attachment.BOARD);
			for (int i = 0; i < selectedRows.length; i++) {
				BoardAttachment ba = (BoardAttachment) boards.get(selectedRows[i]);
				Board fbo = ba.getBoardObj();
				String name = fbo.getName();

				// search board in exising boards list
				Board board = tofTreeModel.getBoardByName(name);

				//ask if we already have the board
				if (board != null) {
					if (JOptionPane
						.showConfirmDialog(
							this,
							"You already have a board named "
								+ name
								+ ".\n"
								+ "Are you sure you want to download this one over it?",
							"Board already exists",
							JOptionPane.YES_NO_OPTION)
						!= 0) {
						continue; // next row of table / next attached board
					} else {
						// change existing board keys to keys of new board
						board.setPublicKey(fbo.getPublicKey());
						board.setPrivateKey(fbo.getPrivateKey());
						updateTofTree(board);
					}
				} else {
					// its a new board
					tofTreeModel.addNodeToTree(fbo);
				}
			}
		}
		
		/**
		 * @return JToolBar 
		 */
		private JToolBar getButtonsToolbar() {
			// configure buttons	
			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(newMessageButton, "New message", "/data/newmessage_rollover.gif", language);
			toolkit.configureButton(updateButton, "Update", "/data/update_rollover.gif", language);
			toolkit.configureButton(replyButton, "Reply", "/data/reply_rollover.gif", language);
			toolkit.configureButton(
				downloadAttachmentsButton,
				"Download attachment(s)",
				"/data/attachment_rollover.gif",
				language);
			toolkit.configureButton(
				downloadBoardsButton,
				"Add Board(s)",
				"/data/attachmentBoard_rollover.gif",
				language);
			toolkit.configureButton(saveMessageButton, "Save message", "/data/save_rollover.gif", language);
			toolkit.configureButton(trustButton, "Trust", "/data/trust_rollover.gif", language);
			toolkit.configureButton(notTrustButton, "Do not trust", "/data/nottrust_rollover.gif", language);
			toolkit.configureButton(checkTrustButton, "Set to CHECK", "/data/check_rollover.gif", language);

			replyButton.setEnabled(false);
			downloadAttachmentsButton.setEnabled(false);
			downloadBoardsButton.setEnabled(false);
			saveMessageButton.setEnabled(false);
			trustButton.setEnabled(false);
			notTrustButton.setEnabled(false);
			checkTrustButton.setEnabled(false);

			// build buttons panel
			JToolBar buttonsToolbar = new JToolBar();
			buttonsToolbar.setRollover(true);
			buttonsToolbar.setFloatable(false);
			Dimension blankSpace = new Dimension(3, 3);

			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.add(saveMessageButton);
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.addSeparator();
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.add(newMessageButton);
			buttonsToolbar.add(replyButton);
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.addSeparator();
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.add(updateButton);
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.addSeparator();
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.add(downloadAttachmentsButton);
			buttonsToolbar.add(downloadBoardsButton);
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.addSeparator();
			buttonsToolbar.add(Box.createRigidArea(blankSpace));
			buttonsToolbar.add(trustButton);
			buttonsToolbar.add(checkTrustButton);
			buttonsToolbar.add(notTrustButton);

			buttonsToolbar.add(Box.createRigidArea(new Dimension(8, 0)));
			buttonsToolbar.add(Box.createHorizontalGlue());
			JLabel dummyLabel = new JLabel(allMessagesCountPrefix + "00000");
			dummyLabel.doLayout();
			Dimension labelSize = dummyLabel.getPreferredSize();
			allMessagesCountLabel.setPreferredSize(labelSize);
			allMessagesCountLabel.setMinimumSize(labelSize);
			newMessagesCountLabel.setPreferredSize(labelSize);
			newMessagesCountLabel.setMinimumSize(labelSize);
			buttonsToolbar.add(allMessagesCountLabel);
			buttonsToolbar.add(Box.createRigidArea(new Dimension(8, 0)));
			buttonsToolbar.add(newMessagesCountLabel);
			buttonsToolbar.add(Box.createRigidArea(blankSpace));

			// listeners
			newMessageButton.addActionListener(listener);
			updateButton.addActionListener(listener);
			replyButton.addActionListener(listener);
			downloadAttachmentsButton.addActionListener(listener);
			downloadBoardsButton.addActionListener(listener);
			saveMessageButton.addActionListener(listener);
			trustButton.addActionListener(listener);
			notTrustButton.addActionListener(listener);
			checkTrustButton.addActionListener(listener);

			return buttonsToolbar;

		}
		
		/**
		 * @return
		 */
		private PopupMenuAttachmentBoard getPopupMenuAttachmentBoard() {
			if (popupMenuAttachmentBoard == null) {
				popupMenuAttachmentBoard = new PopupMenuAttachmentBoard();
				language.addLanguageListener(popupMenuAttachmentBoard);
			}
			return popupMenuAttachmentBoard;
		}
		
		/**
		 * @return
		 */
		private PopupMenuAttachmentTable getPopupMenuAttachmentTable() {
			if (popupMenuAttachmentTable == null) {
				popupMenuAttachmentTable = new PopupMenuAttachmentTable();
				language.addLanguageListener(popupMenuAttachmentTable);
			}
			return popupMenuAttachmentTable;
		}
		
		/**
		 * @return
		 */
		private PopupMenuMessageTable getPopupMenuMessageTable() {
			if (popupMenuMessageTable == null) {
				popupMenuMessageTable = new PopupMenuMessageTable();
				language.addLanguageListener(popupMenuMessageTable);
			}
			return popupMenuMessageTable;
		}

		/**
		 * @return
		 */
		private PopupMenuTofText getPopupMenuTofText() {
			if (popupMenuTofText == null) {
				popupMenuTofText = new PopupMenuTofText(messageTextArea);
				language.addLanguageListener(popupMenuTofText);
			}
			return popupMenuTofText;
		}

		/**
		 * 
		 */
		public void initialize() {
			if (!initialized) {
				refreshLanguage();
				language.addLanguageListener(listener);

				// build messages list scroll pane
				messageTableModel = new MessageTableModel();
				language.addLanguageListener(messageTableModel);
				messageTable = new MessageTable(messageTableModel);
				messageTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
				messageTable.getSelectionModel().addListSelectionListener(listener);
				JScrollPane messageListScrollPane = new JScrollPane(messageTable);

				// build message body scroll pane
				messageTextArea = new AntialiasedTextArea();
				messageTextArea.setEditable(false);
				messageTextArea.setLineWrap(true);
				messageTextArea.setWrapStyleWord(true);
				messageTextArea.setAntiAliasEnabled(settings.getBoolValue("messageBodyAA"));
				JScrollPane messageBodyScrollPane = new JScrollPane(messageTextArea);

				// build attached files scroll pane
				AttachedFilesTableModel attachmentTableModel = new AttachedFilesTableModel();
				language.addLanguageListener(attachmentTableModel);
				filesTable = new JTable(attachmentTableModel);
				filesTableScrollPane = new JScrollPane(filesTable);

				// build attached boards scroll pane
				AttachedBoardTableModel boardTableModel = new AttachedBoardTableModel();
				language.addLanguageListener(boardTableModel);
				boardsTable = new JTable(boardTableModel);
				boardsTableScrollPane = new JScrollPane(boardsTable);

				fontChanged();

				//Put everything together
				attachmentsSplitPane =
					new JSplitPane(
						JSplitPane.VERTICAL_SPLIT,
						filesTableScrollPane,
						boardsTableScrollPane);
				attachmentsSplitPane.setResizeWeight(0.5);
				attachmentsSplitPane.setDividerSize(3);
				attachmentsSplitPane.setDividerLocation(0.5);
				
				messageSplitPane =
					new JSplitPane(
						JSplitPane.VERTICAL_SPLIT,
						messageBodyScrollPane,
						attachmentsSplitPane);
				messageSplitPane.setDividerSize(0);
				messageSplitPane.setDividerLocation(1.0);
				messageSplitPane.setResizeWeight(1.0);
				
				JSplitPane mainSplitPane =
					new JSplitPane(
						JSplitPane.VERTICAL_SPLIT,
						messageListScrollPane,
						messageSplitPane);
				mainSplitPane.setDividerSize(10);
				mainSplitPane.setDividerLocation(160);
				mainSplitPane.setResizeWeight(0.5d);
				mainSplitPane.setMinimumSize(new Dimension(50, 20));

				// build main panel
				setLayout(new BorderLayout());
				add(getButtonsToolbar(), BorderLayout.NORTH);
				add(mainSplitPane, BorderLayout.CENTER);

				//listeners
				messageTextArea.addMouseListener(listener);
				filesTable.addMouseListener(listener);
				boardsTable.addMouseListener(listener);
				messageTable.addMouseListener(listener);
				messageTable.addKeyListener(listener);

				//other listeners
				tofTree.addTreeSelectionListener(listener);
				tofTreeModel.addTreeModelListener(listener);

				// display welcome message if no boards are available
				if (((TreeNode) tofTreeModel.getRoot()).getChildCount() == 0) {
					messageTextArea.setText(language.getString("Welcome message"));
				}

				initialized = true;
			}
		}

		/**
		 * 
		 */
		private void fontChanged() {
			String fontName = settings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
			int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
			int fontSize = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
			Font font = new Font(fontName, fontStyle, fontSize);
			if (!font.getFamily().equals(fontName)) {
				logger.severe(
					"The selected font was not found in your system\n"
						+ "That selection will be changed to \"Monospaced\".");
				settings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
				font = new Font("Monospaced", fontStyle, fontSize);
			}
			messageTextArea.setFont(font);

			fontName = settings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
			fontStyle = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
			fontSize = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
			font = new Font(fontName, fontStyle, fontSize);
			if (!font.getFamily().equals(fontName)) {
				logger.severe(
					"The selected font was not found in your system\n"
						+ "That selection will be changed to \"SansSerif\".");
				settings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, "SansSerif");
				font = new Font("SansSerif", fontStyle, fontSize);
			}
			messageTable.setFont(font);
		}

		/**
		 * @param e
		 */
		private void messageTable_itemSelected(ListSelectionEvent e) {
			Board selectedBoard = tofTreeModel.getSelectedNode();
			if (selectedBoard.isFolder())
				return;
			selectedMessage = TOF.getInstance().evalSelection(e, messageTable, selectedBoard);
			if (selectedMessage != null) {
				displayNewMessageIcon(false);
				downloadAttachmentsButton.setEnabled(false);
				downloadBoardsButton.setEnabled(false);

				lastSelectedMessage = selectedMessage.getSubject();
				if (selectedBoard.isReadAccessBoard() == false) {
					replyButton.setEnabled(true);
				}

				if (selectedMessage.getStatus().trim().equals(VerifyableMessageObject.PENDING)) {
					trustButton.setEnabled(true);
					notTrustButton.setEnabled(true);
					checkTrustButton.setEnabled(false);
				} else if (
					selectedMessage.getStatus().trim().equals(VerifyableMessageObject.VERIFIED)) {
					trustButton.setEnabled(false);
					notTrustButton.setEnabled(true);
					checkTrustButton.setEnabled(true);
				} else if (
					selectedMessage.getStatus().trim().equals(VerifyableMessageObject.FAILED)) {
					trustButton.setEnabled(true);
					notTrustButton.setEnabled(false);
					checkTrustButton.setEnabled(true);
				} else {
					trustButton.setEnabled(false);
					notTrustButton.setEnabled(false);
					checkTrustButton.setEnabled(false);
				}

				messageTextArea.setText(selectedMessage.getContent());
				if (selectedMessage.getContent().length() > 0)
					saveMessageButton.setEnabled(true);
				else
					saveMessageButton.setEnabled(false);

				Vector fileAttachments = selectedMessage.getFileAttachments();
				Vector boardAttachments = selectedMessage.getBoardAttachments();

				positionDividers(fileAttachments.size(), boardAttachments.size());
				
	            ((DefaultTableModel) filesTable.getModel()).setDataVector(selectedMessage.getFileAttachments(),
	                    null);
	            ((DefaultTableModel) boardsTable.getModel()).setDataVector(selectedMessage.getBoardAttachments(),
	                    null);
				
			} else {
				// no msg selected
				messageTextArea.setText(
						language.getString("Select a message to view its content."));
				replyButton.setEnabled(false);
				saveMessageButton.setEnabled(false);
				downloadAttachmentsButton.setEnabled(false);
				downloadBoardsButton.setEnabled(false);
			}
		}
		
        /**
         * @param attachedFiles
         * @param attachedBoards
         */
        private void positionDividers(int attachedFiles, int attachedBoards) {
            if (attachedFiles == 0 && attachedBoards == 0) {
                // Neither files nor boards
                messageSplitPane.setBottomComponent(null);
                messageSplitPane.setDividerSize(0);
                return;
            }
            messageSplitPane.setDividerSize(3);
            messageSplitPane.setDividerLocation(0.75);
            if (attachedFiles != 0 && attachedBoards == 0) {
                //Only files
                messageSplitPane.setBottomComponent(filesTableScrollPane);
                return;
            }
            if (attachedFiles == 0 && attachedBoards != 0) {
                //Only boards
                messageSplitPane.setBottomComponent(boardsTableScrollPane);
                return;
            }
            if (attachedFiles != 0 && attachedBoards != 0) {
                //Both files and boards
                messageSplitPane.setBottomComponent(attachmentsSplitPane);
                attachmentsSplitPane.setTopComponent(filesTableScrollPane);
                attachmentsSplitPane.setBottomComponent(boardsTableScrollPane);
            }
        }
        
        /**
		 * @param e
		 */
		private void newMessageButton_actionPerformed(ActionEvent e) {
			tofNewMessageButton_actionPerformed(e);
		}
		/**
		 * @param e
		 */
		private void notTrustButton_actionPerformed(ActionEvent e) {
			if (selectedMessage != null) {
				if (identities.getFriends().containsKey(selectedMessage.getFrom())) {
					if (JOptionPane
						.showConfirmDialog(
							parentFrame,
							"Are you sure you want to revoke trust to user "
								+ selectedMessage.getFrom().substring(
									0,
									selectedMessage.getFrom().indexOf("@"))
								+ " ? \n If you choose yes, future messages from this user will be marked BAD",
							"revoke trust",
							JOptionPane.YES_NO_OPTION)
						!= 0) {
						return;
					}
				} else {
					startTruster(false, selectedMessage);
				}
			}
			trustButton.setEnabled(false);
			notTrustButton.setEnabled(false);
			checkTrustButton.setEnabled(false);
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			newMessageButton.setToolTipText(language.getString("New message"));
			replyButton.setToolTipText(language.getString("Reply"));
			downloadAttachmentsButton.setToolTipText(
					language.getString("Download attachment(s)"));
			downloadBoardsButton.setToolTipText(language.getString("Add Board(s)"));
			saveMessageButton.setToolTipText(language.getString("Save message"));
			trustButton.setToolTipText(language.getString("Trust"));
			notTrustButton.setToolTipText(language.getString("Do not trust"));
			checkTrustButton.setToolTipText(language.getString("Set to CHECK"));
			updateButton.setToolTipText(language.getString("Update"));
		}
		
		/**
		 * @param e
		 */
		private void replyButton_actionPerformed(ActionEvent e) {
			String subject = lastSelectedMessage;
			if (subject.startsWith("Re:") == false)
				subject = "Re: " + subject;
			/*
			 * if (settings.getBoolValue("useAltEdit")) { altEdit = new
			 * AltEdit(getSelectedNode(), subject, // subject
			 * getTofTextAreaText(), settings, this); altEdit.start(); }
			 * else {
			 */
			MessageFrame newMessageFrame = new MessageFrame(settings, parentFrame,
												identities.getMyId());
			newMessageFrame.setTofTree(tofTree);
			newMessageFrame.composeReply(tofTreeModel.getSelectedNode(), settings.getValue("userName"),
												subject, messageTextArea.getText());
		}

		/**
		 * @param e
		 */
		private void saveMessageButton_actionPerformed(ActionEvent e) {
			FileAccess.saveDialog(
				parentFrame,
				messageTextArea.getText(),
				settings.getValue("lastUsedDirectory"),
				language.getString("Save message to disk"));
		}
		
		/**
		 * @param e
		 */
		private void showAttachedBoardsPopupMenu(MouseEvent e) {
			getPopupMenuAttachmentBoard().show(e.getComponent(), e.getX(), e.getY());
		}

		/**
		 * @param e
		 */
		private void showAttachedFilesPopupMenu(MouseEvent e) {
			getPopupMenuAttachmentTable().show(e.getComponent(), e.getX(), e.getY());
		}

		/**
		 * @param e
		 */
		private void showMessageTablePopupMenu(MouseEvent e) {
			getPopupMenuMessageTable().show(e.getComponent(), e.getX(), e.getY());
		}

		/**
		 * @param e
		 */
		private void showTofTextAreaPopupMenu(MouseEvent e) {
			getPopupMenuTofText().show(e.getComponent(), e.getX(), e.getY());
		}
		
		/**
		 * 
		 */
		private void showCurrentMessagePopupWindow(){
			if  (!isCorrectlySelectedMessage() )
				return;
			getMessageWindow(selectedMessage, this.getSize()).setVisible(true);
			
		}
		
		private MessageWindow getMessageWindow(MessageObject message,Dimension size){
			MessageWindow messagewindow = new MessageWindow(message, size);
			return messagewindow;
		}
		
		/**
		 * @param e
		 */
		private void trustButton_actionPerformed(ActionEvent e) {
			if (selectedMessage != null) {
				if (identities.getEnemies().containsKey(selectedMessage.getFrom())) {
					if (JOptionPane
						.showConfirmDialog(
							parentFrame,
							"are you sure you want to grant trust to user "
								+ selectedMessage.getFrom().substring(
									0,
									selectedMessage.getFrom().indexOf("@"))
								+ " ? \n If you choose yes, future messages from this user will be marked GOOD",
							"re-grant trust",
							JOptionPane.YES_NO_OPTION)
						!= 0) {
						return;
					}
				} else {
					startTruster(true, selectedMessage);
				}
			}
			trustButton.setEnabled(false);
			notTrustButton.setEnabled(false);
			checkTrustButton.setEnabled(false);
		}
		
		/**
		 * @param e
		 */
		private void updateButton_actionPerformed(ActionEvent e) {
			// restarts all finished threads if there are some long running threads
			if (tofTree.isUpdateAllowed(tofTreeModel.getSelectedNode())) {
				tofTree.updateBoard(tofTreeModel.getSelectedNode());
			}
		}

		/**
		 * @param e
		 */
		private void boardsTree_actionPerformed(TreeSelectionEvent e) {

		    messageSplitPane.setBottomComponent(null);
            messageSplitPane.setDividerSize(0);

			if (((TreeNode) tofTreeModel.getRoot()).getChildCount() == 0) {
				//There are no boards. //TODO: check if there are really no boards (folders count as children)
				messageTextArea.setText(language.getString("Welcome message"));
			} else {
				//There are boards.
				Board node =
					(Board) tofTree.getLastSelectedPathComponent();
				if (node != null) {
					if (!node.isFolder()) {
						// node is a board
						messageTextArea.setText(
								language.getString("Select a message to view its content."));
						updateButton.setEnabled(true);
						saveMessageButton.setEnabled(false);
						replyButton.setEnabled(false);
						downloadAttachmentsButton.setEnabled(false);
						downloadBoardsButton.setEnabled(false);
						if (node.isReadAccessBoard()) {
							newMessageButton.setEnabled(false);
						} else {
							newMessageButton.setEnabled(true);
						}
					} else {
						// node is a folder
						messageTextArea.setText(
								language.getString("Select a board to view its content."));
						newMessageButton.setEnabled(false);
						updateButton.setEnabled(false);
					}
				}
			}
		}

		/**
		 * @param e
		 */
		private void boardsTreeNode_Changed(TreeModelEvent e) {
			Object[] path = e.getPath();
			Board board = (Board) path[path.length - 1];

			if (board == tofTreeModel.getSelectedNode()) { // is the board actually shown?
				if (board.isReadAccessBoard()) {
					newMessageButton.setEnabled(false);
				} else {
					newMessageButton.setEnabled(true);
				}
			}
		}

		/**
		 * @param evt
		 */
		private void antialiasing_propertyChanged(PropertyChangeEvent evt) {
			messageTextArea.setAntiAliasEnabled(settings.getBoolValue("messageBodyAA"));
		}
		
		/**
		 * returns true if message was correctly selected 
		 * @return
		 */
		private boolean isCorrectlySelectedMessage(){
			int row = messageTable.getSelectedRow();
			if (row < 0
				|| selectedMessage == null
				|| tofTreeModel.getSelectedNode() == null
				|| tofTreeModel.getSelectedNode().isFolder() == true)
				return false;
			
			return true;
		}
		
		/**
		 * 
		 */
		private void deleteSelectedMessage() {
			if(! isCorrectlySelectedMessage() )
				return;

			final FrostMessageObject targetMessage = selectedMessage;

			//if we show deleted messages we don't need to remove them from the table
			if ( ! settings.getBoolValue(SettingsClass.SHOW_DELETED_MESSAGES) ){
				messageTableModel.deleteRow(selectedMessage);
				updateMessageCountLabels(tofTreeModel.getSelectedNode());
			}
			
			targetMessage.setDeleted(true);
			
			//FIXME: is it needed? needs repaint or the line which crosses the message isn't completely seen
			this.repaint();
			
			Thread saver = new Thread() {
				public void run() {
					targetMessage.save();
				};
			};
			saver.start();	
		}
		
		private void undeleteSelectedMessage(){
			if(! isCorrectlySelectedMessage() )
					return;
			
			final FrostMessageObject targetMessage = selectedMessage;
			targetMessage.setDeleted(false);
			this.repaint();
			
			//FIXME: is this needed???not shure what it is doing
			Thread saver = new Thread() {
				public void run() {
					targetMessage.save();
				};
			};
			saver.start();	
		}
		
		/**
		 * @param identities
		 */
		public void setIdentities(FrostIdentities identities) {
			this.identities = identities;
		}
		
		/**
		 * @param parentFrame
		 */
		public void setParentFrame(JFrame parentFrame) {
			this.parentFrame = parentFrame;
		}

		/**
		 * @param what
		 * @param which
		 */
		public void startTruster(boolean what, FrostMessageObject which) {
			new Truster(identities, Boolean.valueOf(what), which.getFrom()).start();
		}

		/**
		 * @param which
		 */
		public void startTruster(FrostMessageObject which) {
			new Truster(identities, null, which.getFrom()).start();
		}

		/**
		 * Marks current selected message unread
		 */
		private void markSelectedMessageUnread() {
			int row = messageTable.getSelectedRow();
			if (row < 0
				|| selectedMessage == null
				|| tofTreeModel.getSelectedNode() == null
				|| tofTreeModel.getSelectedNode().isFolder() == true)
				return;
		
			FrostMessageObject targetMessage = selectedMessage;
		
			messageTable.removeRowSelectionInterval(0, messageTable.getRowCount() - 1);
		
			targetMessage.setMessageNew(true);
			// let renderer check for new state
			MessageTableModel model = (MessageTableModel) getMessageTable().getModel();
			model.updateRow(targetMessage);
		
			tofTreeModel.getSelectedNode().incNewMessageCount();
		
			updateMessageCountLabels(tofTreeModel.getSelectedNode());
			updateTofTree(tofTreeModel.getSelectedNode());
		}

		/**
		 * @param what
		 */
		private void setMessageTrust(Boolean what) {
			int row = messageTable.getSelectedRow();
			if (row < 0 || selectedMessage == null)
				return;
		
			String status = selectedMessage.getStatus();
		
			if (status.indexOf(VerifyableMessageObject.PENDING) > -1) {
				Identity owner = identities.getNeutrals().get(selectedMessage.getFrom());
				if (owner == null) {
					logger.warning("message was CHECK but not found in Neutral list");
					return;
				}
			}
		
			if (status.indexOf(VerifyableMessageObject.FAILED) > -1) {
				Identity owner = identities.getEnemies().get(selectedMessage.getFrom());
				if (owner == null) {
					logger.warning("message was BAD but not found in BAD list");
					return;
				}
		
			}
		
			if (status.indexOf(VerifyableMessageObject.VERIFIED) > -1) {
				Identity owner = identities.getFriends().get(selectedMessage.getFrom());
				if (owner == null) {
					logger.warning("message was GOOD but not found in GOOD list");
					return;
				}
			}
		
			Truster truster = new Truster(identities, what, selectedMessage.getFrom());
			truster.start();
		}
	}

	/**
	 * Search through .req files of this day in all boards and remove the
	 * dummy .req files that are created by requestThread on key collosions.
	 */
	private class RemoveDummyRequestFiles extends Thread {
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			Iterator i = tofTreeModel.getAllBoards().iterator();

			while (i.hasNext()) {
				Board board = (Board) i.next();

				String destination =
					new StringBuffer()
						.append(MainFrame.keypool)
						.append(board.getBoardFilename())
						.append(fileSeparator)
						.append(DateFun.getDate())
						.append(fileSeparator)
						.toString();
				File boarddir = new File(destination);
				if (boarddir.isDirectory()) {
					File[] entries = boarddir.listFiles();
					for (int x = 0; x < entries.length; x++) {
						File entry = entries[x];

						if (entry.getName().endsWith(".req.sha")
							&& FileAccess.readFileRaw(entry).indexOf(DownloadThread.KEYCOLL_INDICATOR)
								> -1) {
							entry.delete();
						}
					}
				}
			}
		}
	}
	
	private static Core core;

	private static String fileSeparator = System.getProperty("file.separator");
	// saved to frost.ini
	public static SettingsClass frostSettings = null;

	private static MainFrame instance = null; // set in constructor
	// "keypool.dir" is the corresponding key in frostSettings, is set in defaults of SettingsClass.java
	// this is the new way to access this value :)
	public static String keypool = null;

	/**
	 * Used to sort FrostBoardObjects by lastUpdateStartMillis ascending.
	 */
	private static final Comparator lastUpdateStartMillisCmp = new Comparator() {
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Board value1 = (Board) o1;
			Board value2 = (Board) o2;
			if (value1.getLastUpdateStartMillis() > value2.getLastUpdateStartMillis())
				return 1;
			else if (value1.getLastUpdateStartMillis() < value2.getLastUpdateStartMillis())
				return -1;
			else
				return 0;
		}
	};

	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	private static ImageIcon[] newMessage = new ImageIcon[2];

	/**
	 * Selects message icon in lower right corner
	 * @param showNewMessageIcon
	 */
	public static void displayNewMessageIcon(boolean showNewMessageIcon) {
		MainFrame mainFrame = MainFrame.getInstance();
		if (showNewMessageIcon) {
			ImageIcon frameIcon = new ImageIcon(MainFrame.class.getResource("/data/newmessage.gif"));
			mainFrame.setIconImage(frameIcon.getImage());
			mainFrame.statusMessageLabel.setIcon(newMessage[0]);
			// The title should never be changed on Windows systems (SystemTray.exe expects "Frost" as title)
			if ((System.getProperty("os.name").startsWith("Windows")) == false) {
				mainFrame.setTitle("*Frost*");
			}
		} else {
			ImageIcon frameIcon = new ImageIcon(MainFrame.class.getResource("/data/jtc.jpg"));
			mainFrame.setIconImage(frameIcon.getImage());
			mainFrame.statusMessageLabel.setIcon(newMessage[1]);
			// The title should never be changed on Windows systems (SystemTray.exe expects "Frost" as title)
			if ((System.getProperty("os.name").startsWith("Windows")) == false) {
				mainFrame.setTitle("Frost");
			}
		}
	}

	/**
	 * @return
	 */
	public static MainFrame getInstance() {
		return instance;
	}

	private final String allMessagesCountPrefix = "Msg: ";
	private JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix + "0");

	private JButton boardInfoButton = null;
	private long counter = 55;

	//Panels
	private DownloadModel downloadModel = null;
	private JMenuItem fileExitMenuItem = new JMenuItem();

	//File Menu
	private JMenu fileMenu = new JMenu();

	private JMenuItem helpAboutMenuItem = new JMenuItem();
	private JMenuItem helpHelpMenuItem = new JMenuItem();

	//Help Menu
	private JMenu helpMenu = new JMenu();
	private JButton knownBoardsButton = null;
	private JRadioButtonMenuItem languageBulgarianMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageDefaultMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageDutchMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageEnglishMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageFrenchMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageGermanMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageItalianMenuItem = new JRadioButtonMenuItem();
	private JRadioButtonMenuItem languageJapaneseMenuItem = new JRadioButtonMenuItem();

	//Language Menu
	private JMenu languageMenu = new JMenu();

	private Language language = null;
	private JRadioButtonMenuItem languageSpanishMenuItem = new JRadioButtonMenuItem();

	private Listener listener = new Listener();

	// The main menu
	private JMenuBar menuBar;
	private MessagePanel messagePanel = null;
	private MessageTable messageTable = null;
	private MessageTableModel messageTableModel;

	// buttons that are enabled/disabled later
	private JButton newBoardButton = null;
	private JButton newFolderButton = null;
	
	private JToolBar buttonToolBar;
	
	private JPanel extendableStatusPanel;

	private final String newMessagesCountPrefix = "New: ";
	private JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix + "0");

	//Options Menu
	private JMenu optionsMenu = new JMenu();
	private JMenuItem optionsPreferencesMenuItem = new JMenuItem();
	private JMenuItem pluginBrowserMenuItem = new JMenuItem();

	//Plugin Menu
	private JMenu pluginMenu = new JMenu();
	private JMenuItem pluginTranslateMenuItem = new JMenuItem();

	//Popups
	private JButton removeBoardButton = null;
	private JButton renameBoardButton = null;

	// labels that are updated later
	private JLabel statusLabel = null;
	private JLabel statusMessageLabel = null;
	private JButton systemTrayButton = null;

	private JTranslatableTabbedPane tabbedPane;
	private JLabel timeLabel = null;

	private JCheckBoxMenuItem tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem();
	private JMenuItem tofDecreaseFontSizeMenuItem = new JMenuItem();

	private JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem();
	private JMenuItem tofDisplayKnownBoards = new JMenuItem();

	private JMenuItem tofIncreaseFontSizeMenuItem = new JMenuItem();

	//Messages (tof) Menu
	private JMenu tofMenu = new JMenu();

	private TofTree tofTree = null;
	private TofTreeModel tofTreeModel = null;
	private UploadPanel uploadPanel = null;

	/**
	 * Construct the frame
	 * @param frostSettings
	 */
	public MainFrame(SettingsClass settings) {

		instance = this;
		core = Core.getInstance();
		frostSettings = settings;
		language = Language.getInstance();

		keypool = frostSettings.getValue("keypool.dir");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		frostSettings.addUpdater(this);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		ImageIcon frameIcon = new ImageIcon(getClass().getResource("/data/jtc.jpg"));
		setIconImage(frameIcon.getImage());
		setResizable(true);

		setTitle("Frost");

		addWindowListener(listener);
	}

	/**
	 * @param title
	 * @param panel
	 */
	public void addPanel(String title, JPanel panel) {
		getTabbedPane().add(title, panel);
	}
	
	/**
	 * This method inserts a panel into the extendable part of the status bar 
	 * at the given position 
	 * @param panel panel to add to the status bar
	 * @param position position to insert the panel at
	 */
	public void addStatusPanel(JPanel panel, int position) {
		getExtendableStatusPanel().add(panel, position);
	}
	
	/**
	 * This method adds a button to the button toolbar of the frame. It will insert it
	 * into an existing block or into a new one (where a block is a group of buttons 
	 * delimited by separators) at the given position.
	 * If the position number exceeds the number of buttons in that block, the button is
	 * added at the end of that block.
	 * @param button the button to add
	 * @param block the number of the block to insert the button into. If newBlock is true
	 * 			we will create a new block at that position. If it is false, we will use
	 * 			the existing one. If the block number exceeds the number of blocks in the 
	 * 			toolbar, a new block is created at the end of the toolbar and the button is 
	 * 			inserted there, no matter what the value of the newBlock parameter is. 
	 * @param position the position inside the block to insert the button at. If the position 
	 * 			number exceeds the number of buttons in the block, the button is added at the 
	 * 			end of the block.
	 * @param newBlock true to insert the button in a new block. False to use an existing one. 
	 */
	public void addButton(JButton button, int block, int position, boolean newBlock) {	
		int index = 0;
		int blockCount = 0;
		while ((index < getButtonToolBar().getComponentCount()) &&
			   (blockCount < block)) {
			Component component = getButtonToolBar().getComponentAtIndex(index);
			if (component instanceof JToolBar.Separator) {
				blockCount++;
			}
			index++;
		}
		if (blockCount < block) {
			// Block number exceeds the number of blocks in the toolbar or newBlock is true. 
			getButtonToolBar().addSeparator();
			getButtonToolBar().add(button);
			return;
		}
		if (newBlock) {
			// New block created and button put in there.
			getButtonToolBar().add(new JToolBar.Separator(), index);
			getButtonToolBar().add(button, index);
			return;
		}
		int posCount = 0;
		Component component = getButtonToolBar().getComponentAtIndex(index);
		while ((index < getButtonToolBar().getComponentCount()) &&
			   !(component instanceof JToolBar.Separator) &&
			   (posCount < position)) {
				index++;
				posCount++;
				component = getButtonToolBar().getComponentAtIndex(index);
		}
		getButtonToolBar().add(button, index);	
	}
	
	/**
	 * This method adds a menu item to one of the menus of the menu bar of the frame.
	 * It will insert it into an existing menu or into a new one. It will insert it
	 * into an existing block or into a new one (where a block is a group of menu items 
	 * delimited by separators) at the given position.
	 * If the position number exceeds the number of items in that block, the item is
	 * added at the end of that block.
	 * @param item the menu item to add
	 * @param menuNameKey the text (as a language key) of the menu to insert the item into. 
	 * 			If there is no menu with that text, a new one will be created at the end 
	 * 			of the menu bar and the item will be put inside.
	 * @param block the number of the block to insert the item into. If newBlock is true
	 * 			we will create a new block at that position. If it is false, we will use
	 * 			the existing one. If the block number exceeds the number of blocks in the 
	 * 			menu, a new block is created at the end of the menu and the item is 
	 * 			inserted there, no matter what the value of the newBlock parameter is. 
	 * @param position the position inside the block to insert the item at. If the position 
	 * 			number exceeds the number of items in the block, the item is added at the 
	 * 			end of the block.
	 * @param newBlock true to insert the item in a new block. False to use an existing one. 
	 */
	public void addMenuItem(JMenuItem item, String menuNameKey, int block, int position, boolean newBlock) {
		String menuName = language.getString(menuNameKey);
		int index = 0;
		JMenu menu = null;		
		while ((index < getMainMenuBar().getMenuCount()) &&
				(menu == null)) {
			JMenu aMenu = getMainMenuBar().getMenu(index);
			if ((aMenu != null) &&
				(menuName.equals(aMenu.getText()))) {
				menu = aMenu;
			}
			index++;
		}
		if (menu == null) {
			//There isn't any menu with that name, so we create a new one.
			menu = new JMenu(menuName);
			getMainMenuBar().add(menu);
			menu.add(item);
			return;
		}
		index = 0;
		int blockCount = 0;
		while ((index < menu.getItemCount()) &&
			   (blockCount < block)) {
			Component component = menu.getItem(index);
			if (component == null) {
				blockCount++;
			}
			index++;
		}
		if (blockCount < block) {
			// Block number exceeds the number of blocks in the menu or newBlock is true. 
			menu.addSeparator();
			menu.add(item);
			return;
		}
		if (newBlock) {
			// New block created and item put in there.
			menu.insertSeparator(index);
			menu.insert(item, index);
			return;
		}
		int posCount = 0;
		Component component = menu.getItem(index);
		while ((index < menu.getComponentCount()) &&
			   (component != null) &&
			   (posCount < position)) {
				index++;
				posCount++;
				component = menu.getItem(index);
		}
		menu.add(item, index);	
	}
	
	/**
	 * @return
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTranslatableTabbedPane(language);
		}
		return tabbedPane;
	}

	/**
	 * @return
	 */
	private JToolBar getButtonToolBar() {
		if (buttonToolBar == null) {
			buttonToolBar = new JToolBar();
			
			timeLabel = new JLabel("");
			// configure buttons
			knownBoardsButton = new JButton(new ImageIcon(getClass().getResource("/data/knownboards.gif")));
			newBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/newboard.gif")));
			newFolderButton = new JButton(new ImageIcon(getClass().getResource("/data/newfolder.gif")));
			removeBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/remove.gif")));
			renameBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/rename.gif")));
			boardInfoButton = new JButton(new ImageIcon(getClass().getResource("/data/info.gif")));
			systemTrayButton = new JButton(new ImageIcon(getClass().getResource("/data/tray.gif")));

			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(newBoardButton, "New board", "/data/newboard_rollover.gif",	language);
			toolkit.configureButton(newFolderButton, "New folder", "/data/newfolder_rollover.gif", language);
			toolkit.configureButton(removeBoardButton, "Remove board", "/data/remove_rollover.gif",	language);
			toolkit.configureButton(renameBoardButton, "Rename folder",	"/data/rename_rollover.gif", language);
			toolkit.configureButton(boardInfoButton, "Board Information Window", "/data/info_rollover.gif", language);
			toolkit.configureButton(systemTrayButton, "Minimize to System Tray", "/data/tray_rollover.gif", language);
			toolkit.configureButton(knownBoardsButton, "Display list of known boards", "/data/knownboards_rollover.gif", language);

			// add action listener
			knownBoardsButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofDisplayKnownBoardsMenuItem_actionPerformed(e);
				}
			});
			newBoardButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofTree.createNewBoard(MainFrame.this);
				}
			});
			newFolderButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofTree.createNewFolder(MainFrame.this);
				}
			});
			renameBoardButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					renameNode(tofTreeModel.getSelectedNode());
				}
			});
			removeBoardButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofTree.removeNode(tofTreeModel.getSelectedNode());
				}
			});
			systemTrayButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try { // Hide the Frost window
						if (JSysTrayIcon.getInstance() != null) {
							JSysTrayIcon.getInstance().showWindow(JSysTrayIcon.SHOW_CMD_HIDE);
						}
						//Process process = Runtime.getRuntime().exec("exec" +
						// fileSeparator + "SystemTrayHide.exe");
					} catch (IOException _IoExc) {
					}
				}
			});
			boardInfoButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofDisplayBoardInfoMenuItem_actionPerformed(e);
				}
			});
			
			// build panel			
			buttonToolBar.setRollover(true);
			buttonToolBar.setFloatable(false);
			Dimension blankSpace = new Dimension(3, 3);

			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.add(newBoardButton);
			buttonToolBar.add(newFolderButton);
			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.addSeparator();
			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.add(renameBoardButton);
			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.addSeparator();
			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.add(removeBoardButton);
			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.addSeparator();
			buttonToolBar.add(Box.createRigidArea(blankSpace));
			buttonToolBar.add(boardInfoButton);
			buttonToolBar.add(knownBoardsButton);
			if (JSysTrayIcon.getInstance() != null) {
				buttonToolBar.add(Box.createRigidArea(blankSpace));
				buttonToolBar.addSeparator();
				buttonToolBar.add(Box.createRigidArea(blankSpace));

				buttonToolBar.add(systemTrayButton);
			}
			buttonToolBar.add(Box.createHorizontalGlue());
			buttonToolBar.add(timeLabel);
			buttonToolBar.add(Box.createRigidArea(blankSpace));
		}
		return buttonToolBar;
	}

	/**
	 * Build the menu bar.
	 */
	private JMenuBar getMainMenuBar() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			MiscToolkit miscToolkit = MiscToolkit.getInstance();
			tofDisplayBoardInfoMenuItem.setIcon(miscToolkit.getScaledImage("/data/info.gif", 16, 16));
			tofAutomaticUpdateMenuItem.setSelected(true);
			tofDisplayKnownBoards.setIcon(miscToolkit.getScaledImage("/data/knownboards.gif", 16, 16));

			// add action listener
			fileExitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileExitMenuItem_actionPerformed(e);
				}
			});
			optionsPreferencesMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					optionsPreferencesMenuItem_actionPerformed(e);
				}
			});
			tofIncreaseFontSizeMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// make size of the message body font one point bigger
					int size = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
					frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, size + 1);
				}
			});
			tofDecreaseFontSizeMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// make size of the message body font one point smaller
					int size = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
					frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, size - 1);
				}
			});
			tofDisplayBoardInfoMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofDisplayBoardInfoMenuItem_actionPerformed(e);
				}
			});
			tofDisplayKnownBoards.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tofDisplayKnownBoardsMenuItem_actionPerformed(e);
				}
			});
			pluginBrowserMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BrowserFrame browser = new BrowserFrame(true);
					browser.setVisible(true);
				}
			});
			pluginTranslateMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TranslateFrame translate = new TranslateFrame(true);
					translate.setVisible(true);
				}
			});
			languageDefaultMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes");
					frostSettings.setValue("locale", "default");
					setLanguageResource(bundle);
				}
			});

			languageBulgarianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_bg.png", 16, 16));
			languageGermanMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_de.png", 16, 16));
			languageEnglishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_en.png", 16, 16));
			languageSpanishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_es.png", 16, 16));
			languageFrenchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_fr.png", 16, 16));
			languageItalianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_it.png", 16, 16));
			languageJapaneseMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_jp.png", 16, 16));
			languageDutchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_nl.png", 16, 16));

			languageGermanMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("de"));
					frostSettings.setValue("locale", "de");
					setLanguageResource(bundle);
				}
			});
			languageEnglishMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("en"));
					frostSettings.setValue("locale", "en");
					setLanguageResource(bundle);
				}
			});
			languageDutchMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("nl"));
					frostSettings.setValue("locale", "nl");
					setLanguageResource(bundle);
				}
			});
			languageFrenchMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("fr"));
					frostSettings.setValue("locale", "fr");
					setLanguageResource(bundle);
				}
			});
			languageJapaneseMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("ja"));
					frostSettings.setValue("locale", "ja");
					setLanguageResource(bundle);
				}
			});
			languageItalianMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("it"));
					frostSettings.setValue("locale", "it");
					setLanguageResource(bundle);
				}
			});
			languageSpanishMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("es"));
					frostSettings.setValue("locale", "es");
					setLanguageResource(bundle);
				}
			});
			languageBulgarianMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes",	new Locale("bg"));
					frostSettings.setValue("locale", "bg");
					setLanguageResource(bundle);
				}
			});
			helpHelpMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					HelpFrame dlg = new HelpFrame(MainFrame.this);
					dlg.setVisible(true);
				}
			});
			helpAboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					helpAboutMenuItem_actionPerformed(e);
				}
			});

			// construct menu
			// File Menu
			fileMenu.add(fileExitMenuItem);
			// News Menu
			tofMenu.add(tofAutomaticUpdateMenuItem);
			tofMenu.addSeparator();
			tofMenu.add(tofDisplayBoardInfoMenuItem);
			tofMenu.add(tofDisplayKnownBoards);
			// Options Menu
			optionsMenu.add(optionsPreferencesMenuItem);
			// Plugin Menu
			pluginMenu.add(pluginBrowserMenuItem);
			pluginMenu.add(pluginTranslateMenuItem);
			// Language Menu
			ButtonGroup languageMenuButtonGroup = new ButtonGroup();
			languageDefaultMenuItem.setSelected(true);
			languageMenuButtonGroup.add(languageDefaultMenuItem);
			languageMenuButtonGroup.add(languageDutchMenuItem);
			languageMenuButtonGroup.add(languageEnglishMenuItem);
			languageMenuButtonGroup.add(languageFrenchMenuItem);
			languageMenuButtonGroup.add(languageGermanMenuItem);
			languageMenuButtonGroup.add(languageItalianMenuItem);
			languageMenuButtonGroup.add(languageJapaneseMenuItem);
			languageMenuButtonGroup.add(languageSpanishMenuItem);
			languageMenuButtonGroup.add(languageBulgarianMenuItem);

			// Selects the language menu option according to the settings
			HashMap languageMenuItems = new HashMap();
			languageMenuItems.put("default", languageDefaultMenuItem);
			languageMenuItems.put("de", languageGermanMenuItem);
			languageMenuItems.put("en", languageEnglishMenuItem);
			languageMenuItems.put("nl", languageDutchMenuItem);
			languageMenuItems.put("fr", languageFrenchMenuItem);
			languageMenuItems.put("ja", languageJapaneseMenuItem);
			languageMenuItems.put("it", languageItalianMenuItem);
			languageMenuItems.put("es", languageSpanishMenuItem);
			languageMenuItems.put("bg", languageBulgarianMenuItem);

			String language = frostSettings.getValue("locale");
			Object languageItem = languageMenuItems.get(language);
			if (languageItem != null) {
				languageMenuButtonGroup.setSelected(((JMenuItem) languageItem).getModel(), true);
			}

			languageMenu.add(languageDefaultMenuItem);
			languageMenu.addSeparator();
			languageMenu.add(languageDutchMenuItem);
			languageMenu.add(languageEnglishMenuItem);
			languageMenu.add(languageFrenchMenuItem);
			languageMenu.add(languageGermanMenuItem);
			languageMenu.add(languageItalianMenuItem);
			languageMenu.add(languageJapaneseMenuItem);
			languageMenu.add(languageSpanishMenuItem);
			languageMenu.add(languageBulgarianMenuItem);
			// Help Menu
			helpMenu.add(helpHelpMenuItem);
			helpMenu.add(helpAboutMenuItem);
			// add all to bar
			menuBar.add(fileMenu);
			menuBar.add(tofMenu);
			menuBar.add(optionsMenu);
			menuBar.add(pluginMenu);
			menuBar.add(languageMenu);
			menuBar.add(helpMenu);

			translateMainMenu();
		}
		return menuBar;
	}

	/**
	 * This method builds the whole of the status bar (both the extendable and the
	 * static parts)
	 * @return
	 */
	private JPanel buildStatusBar() {
		JPanel panel = new JPanel(new BorderLayout());
			
		statusLabel = new JLabel(language.getString("Frost by Jantho"));
		statusMessageLabel = new JLabel();

		newMessage[0] = new ImageIcon(MainFrame.class.getResource("/data/messagebright.gif"));
		newMessage[1] = new ImageIcon(MainFrame.class.getResource("/data/messagedark.gif"));
		statusMessageLabel.setIcon(newMessage[1]);

		panel.add(getExtendableStatusPanel(), BorderLayout.WEST);
		panel.add(statusLabel, BorderLayout.CENTER); // Statusbar
		panel.add(statusMessageLabel, BorderLayout.EAST);
		
		return panel;
	}
	
	/**
	 * This method returns the extendable part of the status bar.
	 * @return
	 */
	private JPanel getExtendableStatusPanel() {
		if (extendableStatusPanel == null) {
			extendableStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		}
		return extendableStatusPanel;
	}

	/**
	 * @return
	 */
	private JPanel buildTofMainPanel() {
		//add a tab for buddies perhaps?
		getTabbedPane().insertTab("News", null, getMessagePanel(), null, 0);
		getTabbedPane().setSelectedIndex(0);

		JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
		// tofTree selection listener
		tofTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				tofTree_actionPerformed(e);
			}
		});

		JSplitPane treeAndTabbedPane =
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, getTabbedPane());
		treeAndTabbedPane.setDividerLocation(160);
		// Vertical Board Tree / MessagePane Divider

		JPanel tofMainPanel = new JPanel(new BorderLayout());
		tofMainPanel.add(treeAndTabbedPane, BorderLayout.CENTER); // TOF/Text
		return tofMainPanel;
	}

	/**
	 * Returns true if board is allowed to be updated.
	 * Also checks if board update is already running.
	 * @param board
	 * @return
	 */
	public boolean doUpdate(Board board) {
		if (tofTree.isUpdateAllowed(board) == false)
			return false;

		if (board.isUpdating())
			return false;

		return true;
	}

	/**
	 * File | Exit action performed
	 * @param e
	 */
	private void fileExitMenuItem_actionPerformed(ActionEvent e) {

		if (tofTree.getRunningBoardUpdateThreads().getRunningUploadThreadCount() > 0) {
			int result =
				JOptionPane.showConfirmDialog(
					this,
					language.getString("UploadsUnderway.body"),
					language.getString("UploadsUnderway.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		} else {
			System.exit(0);
		}
	}

	/**
	 * @return
	 */
	public DownloadModel getDownloadModel() {
		return downloadModel;
	}
	
	/**
	 * @return
	 */
	private MessagePanel getMessagePanel() {
		if (messagePanel == null) {
			messagePanel = new MessagePanel(frostSettings);
			messagePanel.setParentFrame(this);
			messagePanel.setIdentities(core.getIdentities());
			messagePanel.initialize();
		}
		return messagePanel;
	}
	
	/**
	 * @return
	 */
	public MessageTable getMessageTable() {
		return messageTable;
	}

	/**
	 * @return
	 */
	public TofTreeModel getTofTreeModel() {
		return tofTreeModel;
	}

	/**
	 * Help | About action performed
	 * @param e
	 */
	private void helpAboutMenuItem_actionPerformed(ActionEvent e) {
		AboutBox dlg = new AboutBox(this);
		dlg.setModal(true);
		dlg.setVisible(true);
	}

	/**
     * 
     */
    public void initialize() {

    	// Add components
		JPanel contentPanel = (JPanel) getContentPane();
		contentPanel.setLayout(new BorderLayout());

		contentPanel.add(getButtonToolBar(), BorderLayout.NORTH);
		contentPanel.add(buildTofMainPanel(), BorderLayout.CENTER);
		contentPanel.add(buildStatusBar(), BorderLayout.SOUTH);
		setJMenuBar(getMainMenuBar());
    	
        // step through all messages on disk up to maxMessageDisplay and check
        // if there are new messages
        // if a new message is in a folder, this folder is show yellow in tree
        TOF.getInstance().initialSearchNewMessages();

        if (core.isFreenetOnline()) {
            tofAutomaticUpdateMenuItem.setSelected(frostSettings.getBoolValue("automaticUpdate"));
        } else {
            tofAutomaticUpdateMenuItem.setSelected(false);
        }
        //      uploadActivateCheckBox.setSelected(frostSettings.getBoolValue("uploadingActivated"));
        //      reducedBlockCheckCheckBox.setSelected(frostSettings.getBoolValue("reducedBlockCheck"));

        if (tofTree.getRowCount() > frostSettings.getIntValue("tofTreeSelectedRow"))
            tofTree.setSelectionRow(frostSettings.getIntValue("tofTreeSelectedRow"));

        // make sure the font size isn't too small to see
        if (frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE) < 6)
            frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, 6);

        // load size, location and state of window
        int lastHeight = frostSettings.getIntValue("lastFrameHeight");
        int lastWidth = frostSettings.getIntValue("lastFrameWidth");
        int lastPosX = frostSettings.getIntValue("lastFramePosX");
        int lastPosY = frostSettings.getIntValue("lastFramePosY");
        boolean lastMaximized = frostSettings.getBoolValue("lastFrameMaximized");
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (lastWidth < 100) {
            lastWidth = 700;
        }
        if (lastWidth > scrSize.width) {
            lastWidth = scrSize.width;
        }

        if (lastHeight < 100) {
            lastHeight = 500;
        }
        if (lastHeight > scrSize.height) {
            lastWidth = scrSize.height;
        }

        if (lastPosX < 0) {
            lastPosX = 0;
        }
        if (lastPosY < 0) {
            lastPosY = 0;
        }

        if ((lastPosX + lastWidth) > scrSize.width) {
            lastPosX = scrSize.width / 10;
            lastWidth = (int) ((scrSize.getWidth() / 10.0) * 8.0);
        }

        if ((lastPosY + lastHeight) > scrSize.height) {
            lastPosY = scrSize.height / 10;
            lastHeight = (int) ((scrSize.getHeight() / 10.0) * 8.0);
        }

        setBounds(lastPosX, lastPosY, lastWidth, lastHeight);

        if (lastMaximized) {
            setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
        }

        //note: changed this from timertask so that I can give it a name --zab
        Thread tickerThread = new Thread("tick tack") {
            public void run() {
                while (true) {
                    Mixed.wait(1000);
                    //TODO: refactor this method in Core. lots of work :)
                    timer_actionPerformed();
                }
            }
        };
        tickerThread.start();

        validate();
    }

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		//Core.getOut().println("Clipboard contents replaced");
	}

	/**
	 * Options | Preferences action performed
	 * @param e
	 */
	private void optionsPreferencesMenuItem_actionPerformed(ActionEvent e) {
		try {
			frostSettings.save();
		} catch (StorageException se) {
			logger.log(Level.SEVERE, "Error while saving the settings.", se);
		}
		OptionsFrame optionsDlg = new OptionsFrame(this, frostSettings);
		boolean okPressed = optionsDlg.runDialog();
		if (okPressed) {
			// check if signed only+hideCheck+hideBad or blocking words settings changed
			if (optionsDlg.shouldReloadMessages()) {
				// update the new msg. count for all boards
				TOF.getInstance().initialSearchNewMessages();
				// reload all messages
				tofTree_actionPerformed(null);
			}

			tofTree.updateTree();
			// redraw whole tree, in case the update visualization was enabled or disabled (or others)

			// check if we switched from disableRequests=true to =false (requests now enabled)
			if (optionsDlg.shouldRemoveDummyReqFiles()) {
				new RemoveDummyRequestFiles().start();
			}
		}
	}

	/**
	 * Opens dialog to rename the board / folder.
	 * For boards it checks for double names.
	 * @param selected
	 */
	public void renameNode(Board selected) {
		if (selected == null)
			return;
		String newname = null;
		do {
			newname =
				JOptionPane.showInputDialog(
					this,
					"Please enter the new name:\n",
					selected.getName());
			if (newname == null)
				return; // cancel
			if (selected.isFolder() == false
				&& // double folder names are ok
			tofTreeModel.getBoardByName(newname) != null) {
				JOptionPane.showMessageDialog(
					this,
					"You already have a board with name '"
						+ newname
						+ "'!\nPlease choose a new name.");
				newname = ""; // loop again
			}
		} while (newname.length() == 0);

		selected.setName(newname);
		updateTofTree(selected);
	}

	/**
	 * Chooses the next FrostBoard to update (automatic update).
	 * First sorts by lastUpdateStarted time, then chooses first board
	 * that is allowed to update.
	 * Used only for automatic updating.
	 * Returns NULL if no board to update is found.
	 * @param boards
	 * @return
	 */
	public Board selectNextBoard(Vector boards) {
		Collections.sort(boards, lastUpdateStartMillisCmp);
		// now first board in list should be the one with latest update of all
		Board board;
		Board nextBoard = null;

		long curTime = System.currentTimeMillis();
		// get in minutes
		int minUpdateInterval =
			frostSettings.getIntValue("automaticUpdate.boardsMinimumUpdateInterval");
		// min -> ms
		long minUpdateIntervalMillis = minUpdateInterval * 60 * 1000;

		for (int i = 0; i < boards.size(); i++) {
			board = (Board) boards.get(i);
			if (nextBoard == null
				&& doUpdate(board)
				&& (curTime - minUpdateIntervalMillis) > board.getLastUpdateStartMillis()
				&& // minInterval
			 (
					(board.isConfigured() && board.getAutoUpdateEnabled())
						|| !board.isConfigured())) {
				nextBoard = board;
				break;
			}
		}
		if (nextBoard != null) {
			logger.info("*** Automatic board update started for: " + nextBoard.getName());
		} else {
			logger.info(
				"*** Automatic board update - min update interval not reached.  waiting...");
		}
		return nextBoard;
	}

	/**
	 * Setter for thelanguage resource bundle
	 * @param newLanguageResource
	 */
	private void setLanguageResource(ResourceBundle newLanguageResource) {
		language.setLanguageResource(newLanguageResource);
		translateMainMenu();
		translateButtons();
	}

	/**
	 * @param title
	 * @param enabled
	 */
	public void setPanelEnabled(String title, boolean enabled) {
		int position = getTabbedPane().indexOfTab(title);
		if (position != -1) {
			getTabbedPane().setEnabledAt(position, enabled);
		}
	}
	
	/**
	 * @param tofTree
	 */
	public void setTofTree(TofTree tofTree) {
		this.tofTree = tofTree;
	}
	
	/**
	 * @param tofTreeModel
	 */
	public void setTofTreeModel(TofTreeModel tofTreeModel) {
		this.tofTreeModel = tofTreeModel;
	}
	
	/**
	 * timer Action Listener (automatic download)
	 */
	public void timer_actionPerformed() {
		// this method is called by a timer each second, so this counter counts seconds
		counter++;

		//////////////////////////////////////////////////
		//   Automatic TOF update
		//////////////////////////////////////////////////
		if (counter % 15 == 0
			&& // check all 5 seconds if a board update could be started
		tofAutomaticUpdateMenuItem
				.isSelected()
			&& tofTree.getRunningBoardUpdateThreads().getUpdatingBoardCount()
				< frostSettings.getIntValue("automaticUpdate.concurrentBoardUpdates")) {
			Vector boards = tofTreeModel.getAllBoards();
			if (boards.size() > 0) {
				Board actualBoard = selectNextBoard(boards);
				if (actualBoard != null) {
					tofTree.updateBoard(actualBoard);
				}
			}
		}

		//////////////////////////////////////////////////
		//   Display time in button bar
		//////////////////////////////////////////////////
		timeLabel.setText(
			new StringBuffer()
				.append(DateFun.getVisibleExtendedDate())
				.append(" - ")
				.append(DateFun.getFullExtendedTime())
				.append(" GMT")
				.toString());

		/////////////////////////////////////////////////
		//   Update status bar
		/////////////////////////////////////////////////
		String newText =
			new StringBuffer()
				.append("   " + language.getString("TOFUP") + ": ")
				.append(tofTree.getRunningBoardUpdateThreads().getUploadingBoardCount())
				.append("B / ")
				.append(tofTree.getRunningBoardUpdateThreads().getRunningUploadThreadCount())
				.append("T")
				.append("   " + language.getString("TOFDO") + ": ")
				.append(tofTree.getRunningBoardUpdateThreads().getUpdatingBoardCount())
				.append("B / ")
				.append(tofTree.getRunningBoardUpdateThreads().getRunningDownloadThreadCount())
				.append("T")
				.append("   " + language.getString("Selected board") + ": ")
				.append(tofTreeModel.getSelectedNode().getName())
				.toString();
		statusLabel.setText(newText);
	}

	/**
	 * @param e
	 */
	private void tofDisplayBoardInfoMenuItem_actionPerformed(ActionEvent e) {
		if (BoardInfoFrame.isDialogShowing() == false) {
			BoardInfoFrame boardInfo = new BoardInfoFrame(this, tofTree);
			boardInfo.startDialog();
		}
	}

	/**
	 * @param e
	 */
	private void tofDisplayKnownBoardsMenuItem_actionPerformed(ActionEvent e) {
		KnownBoardsFrame knownBoards = new KnownBoardsFrame(this, tofTree);
		knownBoards.startDialog();
	}

	/** 
	 * tofNewMessageButton Action Listener (tof/ New Message)
	 * @param e
	 */
	private void tofNewMessageButton_actionPerformed(ActionEvent e) {
		/*
		 * if (frostSettings.getBoolValue("useAltEdit")) { // TODO: pass
		 * FrostBoardObject altEdit = new AltEdit(getSelectedNode(), subject, //
		 * subject "", // new msg frostSettings, this); altEdit.start(); } else {
		 */
		MessageFrame newMessageFrame = new MessageFrame(
												frostSettings, this, 
												core.getIdentities().getMyId());
		newMessageFrame.setTofTree(tofTree);
		newMessageFrame.composeNewMessage(tofTreeModel.getSelectedNode(), frostSettings.getValue("userName"), 
											"No subject", "");
	}

	/** TOF Board selected
	 * Core.getOut()
	 * if e == NULL, the method is called by truster or by the reloader after options were changed
	 * in this cases we usually should left select the actual message (if one) while reloading the table
	 * @param e
	 */
	public void tofTree_actionPerformed(TreeSelectionEvent e) {
		int i[] = tofTree.getSelectionRows();
		if (i != null && i.length > 0) {
			frostSettings.setValue("tofTreeSelectedRow", i[0]);
		}

		Board node = (Board) tofTree.getLastSelectedPathComponent();

		if (node != null) {
			if (node.isFolder() == false) {
				// node is a board
				removeBoardButton.setEnabled(true);

				updateButtons(node);

				logger.info(
					"Board " + node.getName() + " blocked count: " + node.getBlockedCount());

				uploadPanel.setAddFilesButtonEnabled(true);
				renameBoardButton.setEnabled(false);

				// read all messages for this board into message table
				TOF.getInstance().updateTofTable(node, keypool);
				messageTable.clearSelection();
			} else {
				// node is a folder
				MessageTableModel model = (MessageTableModel) getMessageTable().getModel();
				model.clearDataModel();
				updateMessageCountLabels(node);

				uploadPanel.setAddFilesButtonEnabled(false);
				renameBoardButton.setEnabled(true);
				if (node.isRoot()) {
					removeBoardButton.setEnabled(false);
				} else {
					removeBoardButton.setEnabled(true);
				}
			}
		}
	}

	/**
	 * 
	 */
	private void translateButtons() {
		newBoardButton.setToolTipText(language.getString("New board"));
		systemTrayButton.setToolTipText(language.getString("Minimize to System Tray"));
		knownBoardsButton.setToolTipText(
				language.getString("Display list of known boards"));
		boardInfoButton.setToolTipText(language.getString("Board Information Window"));
		newFolderButton.setToolTipText(language.getString("New folder"));
		removeBoardButton.setToolTipText(language.getString("Remove board"));
		renameBoardButton.setToolTipText(language.getString("Rename folder"));
	}
	
	/**
	 * 
	 */
	private void translateMainMenu() {
		fileMenu.setText(language.getString("File"));
		fileExitMenuItem.setText(language.getString("Exit"));
		tofMenu.setText(language.getString("News"));
		tofDisplayBoardInfoMenuItem.setText(
				language.getString("Display board information window"));
		tofAutomaticUpdateMenuItem.setText(language.getString("Automatic message update"));
		tofIncreaseFontSizeMenuItem.setText(language.getString("Increase Font Size"));
		tofDecreaseFontSizeMenuItem.setText(language.getString("Decrease Font Size"));
		tofDisplayKnownBoards.setText(language.getString("Display known boards"));
		optionsMenu.setText(language.getString("Options"));
		optionsPreferencesMenuItem.setText(language.getString("Preferences"));
		pluginMenu.setText(language.getString("Plugins"));
		pluginBrowserMenuItem.setText(language.getString("Experimental Freenet Browser"));
		pluginTranslateMenuItem.setText(
				language.getString("Translate Frost into another language"));
		languageMenu.setText(language.getString("Language"));
		languageDefaultMenuItem.setText(language.getString("Default"));
		languageDutchMenuItem.setText(language.getString("Dutch"));
		languageEnglishMenuItem.setText(language.getString("English"));
		languageFrenchMenuItem.setText(language.getString("French"));
		languageGermanMenuItem.setText(language.getString("German"));
		languageItalianMenuItem.setText(language.getString("Italian"));
		languageJapaneseMenuItem.setText(language.getString("Japanese"));
		languageSpanishMenuItem.setText(language.getString("Spanish"));
		languageBulgarianMenuItem.setText(language.getString("Bulgarian"));
		helpMenu.setText(language.getString("Help"));
		helpHelpMenuItem.setText(language.getString("Help"));
		helpAboutMenuItem.setText(language.getString("About"));
	}

	/**
	 * @param board
	 */
	private void updateButtons(Board board) {
		if (board.isReadAccessBoard()) {
			uploadPanel.setAddFilesButtonEnabled(false);
		} else {
			uploadPanel.setAddFilesButtonEnabled(true);
		}
	}

	/**
	 * Method that update the Msg and New counts for tof table
	 * Expects that the boards messages are shown in table
	 * @param board
	 */
	public void updateMessageCountLabels(Board board) {
		if (board.isFolder() == true) {
			allMessagesCountLabel.setText("");
			newMessagesCountLabel.setText("");
		} else {
			DefaultTableModel model = (DefaultTableModel) messageTable.getModel();

			int allMessages = model.getRowCount();
			allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

			int newMessages = board.getNewMessageCount();
			newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
		}
	}

	/* (non-Javadoc)
	 * @see frost.SettingsUpdater#updateSettings()
	 */
	public void updateSettings() {
		frostSettings.setValue("automaticUpdate", tofAutomaticUpdateMenuItem.isSelected());
	}
	
	/**
	 * Fires a nodeChanged (redraw) for this board and updates buttons.
	 */
	public void updateTofTree(Board board) {
		// fire update for node
		tofTreeModel.nodeChanged(board);
		// also update all parents
		TreeNode parentFolder = (Board) board.getParent();
		if (parentFolder != null) {
			tofTreeModel.nodeChanged(parentFolder);
			parentFolder = parentFolder.getParent();
		}

		if (board == tofTreeModel.getSelectedNode()) // is the board actually shown?
			{
			updateButtons(board);
		}
	}

	/**
	 * @param table
	 */
	public void setDownloadModel(DownloadModel table) {
		downloadModel = table;
	}

	/**
	 * @param panel
	 */
	public void setUploadPanel(UploadPanel panel) {
		uploadPanel = panel;
	}

}
