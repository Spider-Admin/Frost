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
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;

import frost.components.BrowserFrame;
import frost.components.translate.TranslateFrame;
import frost.crypt.crypt;
import frost.ext.*;
import frost.gui.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.gui.translation.*;
import frost.identities.*;
import frost.messages.VerifyableMessageObject;
import frost.threads.*;
import frost.threads.maintenance.Truster;

//++++ TODO: rework identities stuff + save to xml
//             - save identities together (not separated friends,enemies)
//           - each identity have 3 states: GOOD, BAD, NEUTRAL
//             - filter out enemies on read of messages

// after removing a board, let actual board selected (currently if you delete another than selected board
//   the tofTree is updated)

public class frame1 extends JFrame implements ClipboardOwner {
	/**
	 * 
	 */
	private class PopupMenuTofTree extends JSkinnablePopupMenu implements LanguageListener, ActionListener {

		private JMenuItem descriptionItem = new JMenuItem();
		private JMenuItem refreshItem = new JMenuItem(getScaledImage("/data/update.gif"));
		private JMenuItem addBoardItem = new JMenuItem(getScaledImage("/data/newboard.gif"));
		private JMenuItem addFolderItem = new JMenuItem(getScaledImage("/data/newfolder.gif"));
		private JMenuItem removeNodeItem = new JMenuItem(getScaledImage("/data/remove.gif"));
		private JMenuItem cutNodeItem = new JMenuItem(getScaledImage("/data/cut.gif"));
		private JMenuItem pasteNodeItem = new JMenuItem(getScaledImage("/data/paste.gif"));
		private JMenuItem configureBoardItem = new JMenuItem(getScaledImage("/data/configure.gif"));
		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem sortFolderItem = new JMenuItem(getScaledImage("/data/sort.gif"));
		
		private FrostBoardObject selectedTreeNode = null;

		/**
		 * 
		 */
		public PopupMenuTofTree() {
			super();
			initialize();
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			addBoardItem.setText(languageResource.getString("Add new board"));
			addFolderItem.setText(languageResource.getString("Add new folder"));
			configureBoardItem.setText(languageResource.getString("Configure selected board"));
			cancelItem.setText(languageResource.getString("Cancel"));
			sortFolderItem.setText(languageResource.getString("Sort folder"));
		}

		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			descriptionItem.setEnabled(false);

			// add listeners
			refreshItem.addActionListener(this);
			addBoardItem.addActionListener(this);
			addFolderItem.addActionListener(this);
			removeNodeItem.addActionListener(this);
			cutNodeItem.addActionListener(this);
			pasteNodeItem.addActionListener(this);
			configureBoardItem.addActionListener(this);
			sortFolderItem.addActionListener(this);
		}

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			int selRow = getTofTree().getRowForLocation(x, y);

			if (selRow != -1) { // only if a node is selected
				removeAll();

				TreePath selPath = getTofTree().getPathForLocation(x, y);
				selectedTreeNode = (FrostBoardObject) selPath.getLastPathComponent();

				String folderOrBoard1 =
					((selectedTreeNode.isFolder())
						? languageResource.getString("Folder")
						: languageResource.getString("Board"));
				String folderOrBoard2 =
					((selectedTreeNode.isFolder())
						? languageResource.getString("folder")
						: languageResource.getString("board"));

				descriptionItem.setText(folderOrBoard1 + " : " + selectedTreeNode);
				refreshItem.setText(languageResource.getString("Refresh") + " " + folderOrBoard2);
				removeNodeItem.setText(languageResource.getString("Remove") + " " + folderOrBoard2);
				cutNodeItem.setText(languageResource.getString("Cut") + " " + folderOrBoard2);

				add(descriptionItem);
				addSeparator();
				add(refreshItem);
				addSeparator();
				if (selectedTreeNode.isFolder() == false) {
					add(configureBoardItem);
				} else {
					add(sortFolderItem);	
				}
				addSeparator();
				add(addBoardItem);
				add(addFolderItem);
				if (selectedTreeNode.isRoot() == false) {
					add(removeNodeItem);
				}
				addSeparator();
				if (selectedTreeNode.isRoot() == false) {
					add(cutNodeItem);
				}
				if (clipboard != null && selectedTreeNode.isFolder()) {
					String folderOrBoard3 =
						((clipboard.isFolder())
							? languageResource.getString("folder")
							: languageResource.getString("board"));
					pasteNodeItem.setText(
						languageResource.getString("Paste")
							+ " "
							+ folderOrBoard3
							+ " '"
							+ clipboard.toString()
							+ "'");
					add(pasteNodeItem);
				}
				addSeparator();
				add(cancelItem);

				super.show(invoker, x, y);
			}
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == refreshItem) {
				refreshSelected();
			}
			if (e.getSource() == addBoardItem) {
				addBoardSelected();
			}
			if (e.getSource() == addFolderItem) {
				addFolderSelected();
			}
			if (e.getSource() == removeNodeItem) {
				removeNodeSelected();
			}
			if (e.getSource() == cutNodeItem) {
				cutNodeSelected();
			}
			if (e.getSource() == pasteNodeItem) {
				pasteNodeSelected();
			}
			if (e.getSource() == configureBoardItem) {
				configureBoardSelected();
			}
			if (e.getSource() == sortFolderItem) {
				sortFolderSelected();
			}
		}

		/**
		 * 
		 */
		private void sortFolderSelected() {
			selectedTreeNode.sortChildren();
			DefaultTreeModel tofTreeModel = (DefaultTreeModel) getTofTree().getModel();
			tofTreeModel.nodeStructureChanged(selectedTreeNode);
		}

		/**
		 * 
		 */
		private void configureBoardSelected() {
			tofConfigureBoardMenuItem_actionPerformed(selectedTreeNode);
		}

		/**
		 * 
		 */
		private void pasteNodeSelected() {
			if (clipboard != null) {
				pasteFromClipboard(selectedTreeNode);
			}
		}

		/**
		 * 
		 */
		private void cutNodeSelected() {
			cutNode(selectedTreeNode);
		}
		
		/**
		 * 
		 */
		private void removeNodeSelected() {
			removeNode(selectedTreeNode);
		}

		/**
		 * 
		 */
		private void addFolderSelected() {
			getTofTree().createNewFolder(frame1.this);
		}

		/**
		 * 
		 */
		private void addBoardSelected() {
			getTofTree().createNewBoard(frame1.this);
		}

		/**
		 * 
		 */
		private void refreshSelected() {
			refreshNode(selectedTreeNode);
		}
	}
	/**
	 * 
	 */
	private class PopupMenuMessageTable
		extends JPopupMenu
		implements ActionListener, LanguageListener {

		private JMenuItem markAllMessagesReadItem = new JMenuItem();
		private JMenuItem markMessageUnreadItem = new JMenuItem();
		private JMenuItem setGoodItem = new JMenuItem();
		private JMenuItem setBadItem = new JMenuItem();
		private JMenuItem setCheckItem = new JMenuItem();
		private JMenuItem cancelItem = new JMenuItem();

		/**
		 * 
		 */
		public PopupMenuMessageTable() {
			super();
			initialize();
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
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			markMessageUnreadItem.setText(
				languageResource.getString("Mark message unread"));
			markAllMessagesReadItem.setText(
				languageResource.getString("Mark ALL messages read"));
			setGoodItem.setText(
				languageResource.getString("help user (sets to GOOD)"));
			setBadItem.setText(
				languageResource.getString("block user (sets to BAD)"));
			setCheckItem.setText(
				languageResource.getString("set to neutral (CHECK)"));
			cancelItem.setText(languageResource.getString("Cancel"));
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
		private void setBad() {
			setMessageTrust(new Boolean(false));
		}

		/**
		 * 
		 */
		private void setGood() {
			setMessageTrust(new Boolean(true));
		}

		/**
		 * 
		 */
		private void markAllMessagesRead() {
			TOF.setAllMessagesRead(getMessageTable(), getSelectedNode());
		}

		/**
		 * 
		 */
		private void markMessageUnread() {
			markSelectedMessageUnread();
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}
		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			if (!getSelectedNode().isFolder()) {
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
				if (messageTable.getSelectedRow() > -1 && selectedMessage != null ) {
					//fscking html on all these..
					if (selectedMessage
						.getStatus()
						.indexOf(VerifyableMessageObject.VERIFIED)
						> -1) {
						setCheckItem.setEnabled(true);
						setBadItem.setEnabled(true);
					} else if (
						selectedMessage.getStatus().indexOf(
							VerifyableMessageObject.PENDING)
							> -1) {
						setGoodItem.setEnabled(true);
						setBadItem.setEnabled(true);
					} else if (
						selectedMessage.getStatus().indexOf(
							VerifyableMessageObject.FAILED)
							> -1) {
						setGoodItem.setEnabled(true);
						setCheckItem.setEnabled(true);
					} else 
						Core.getOut().println(
							"invalid message state : "
								+ selectedMessage.getStatus());
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
	private class PopupMenuAttachmentBoard
		extends JPopupMenu
		implements ActionListener, LanguageListener {

		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem saveBoardItem = new JMenuItem();
		private JMenuItem saveBoardsItem = new JMenuItem();

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
		/**
		 * 
		 */
		public PopupMenuAttachmentBoard() {
			super();
			initialize();
		}
		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			saveBoardsItem.addActionListener(this);
			saveBoardItem.addActionListener(this);
		}
		/**
		 * 
		 */
		private void refreshLanguage() {
			saveBoardsItem.setText(languageResource.getString("Add Board(s)"));
			saveBoardItem.setText(languageResource.getString("Add selected board"));
			cancelItem.setText(languageResource.getString("Cancel"));
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
		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			removeAll();

			if (boardTable.getSelectedRow() == -1) {
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
		extends JPopupMenu
		implements ActionListener, LanguageListener {
			
		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem saveAttachmentItem = new JMenuItem();
		private JMenuItem saveAttachmentsItem = new JMenuItem();
			
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
		/**
		 * @throws java.awt.HeadlessException
		 */
		public PopupMenuAttachmentTable() throws HeadlessException {
			super();
			initialize();
		}
		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			saveAttachmentsItem.addActionListener(this);
			saveAttachmentItem.addActionListener(this);
		}
		/**
		 * 
		 */
		private void refreshLanguage() {
			saveAttachmentsItem.setText(
				languageResource.getString("Download attachment(s)"));
			saveAttachmentItem.setText(
				languageResource.getString("Download selected attachment"));
			cancelItem.setText(languageResource.getString("Cancel"));
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
		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			removeAll();

			if (attachmentTable.getSelectedRow() == -1) {
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
	private class PopupMenuTofText
		extends JPopupMenu
		implements ActionListener, LanguageListener {

		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem saveMessageItem = new JMenuItem();

		/**
		 * 
		 */
		public PopupMenuTofText() {
			super();
			initialize();
		}

		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			saveMessageItem.addActionListener(this);

			add(saveMessageItem);
			addSeparator();
			add(cancelItem);
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			saveMessageItem.setText(
				languageResource.getString("Save message to disk"));
			cancelItem.setText(languageResource.getString("Cancel"));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == saveMessageItem) {
				saveMessage();
			}
		}

		/**
		 * 
		 */
		private void saveMessage() {
			FileAccess.saveDialog(
				getInstance(),
				getTofTextAreaText(),
				frostSettings.getValue("lastUsedDirectory"),
				languageResource.getString("Save message to disk"));
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			if ((selectedMessage != null)
				&& (selectedMessage.getContent() != null)) {
				super.show(invoker, x, y);
			}
		}

	}

	/**
	 * Getter for the language resource bundle
	 */
	public ResourceBundle getLanguageResource() {
		return languageResource.getResourceBundle();
	}

	/**
	 * Setter for thelanguage resource bundle
	 */
	public void setLanguageResource(ResourceBundle newLanguageResource) {
		languageResource.setLanguageResource(newLanguageResource);
		translateMainMenu();
		translateTabbedPane();
		translateButtons();
	}

	private void translateTabbedPane() {
		tabbedPane.setTitleAt(0, languageResource.getString("News"));
		tabbedPane.setTitleAt(1, languageResource.getString("Search"));
		tabbedPane.setTitleAt(2, languageResource.getString("Downloads"));
		tabbedPane.setTitleAt(3, languageResource.getString("Uploads"));
	}
	private void translateButtons() {
		newBoardButton.setToolTipText(languageResource.getString("New board"));
		systemTrayButton.setToolTipText(
			languageResource.getString("Minimize to System Tray"));
		knownBoardsButton.setToolTipText(
			languageResource.getString("Display list of known boards"));
		boardInfoButton.setToolTipText(
			languageResource.getString("Board Information Window"));
		newFolderButton.setToolTipText(
			languageResource.getString("New folder"));
		pasteBoardButton.setToolTipText(
			languageResource.getString("Paste board"));
		configBoardButton.setToolTipText(
			languageResource.getString("Configure board"));
		removeBoardButton.setToolTipText(
			languageResource.getString("Remove board"));
		cutBoardButton.setToolTipText(languageResource.getString("Cut board"));
		renameBoardButton.setToolTipText(
			languageResource.getString("Rename folder"));
		tofNewMessageButton.setToolTipText(
			languageResource.getString("New message"));
		tofReplyButton.setToolTipText(languageResource.getString("Reply"));
		downloadAttachmentsButton.setToolTipText(
			languageResource.getString("Download attachment(s)"));
		downloadBoardsButton.setToolTipText(
			languageResource.getString("Add Board(s)"));
		saveMessageButton.setToolTipText(
			languageResource.getString("Save message"));
		trustButton.setToolTipText(languageResource.getString("Trust"));
		notTrustButton.setToolTipText(
			languageResource.getString("Do not trust"));
		checkTrustButton.setToolTipText(
			languageResource.getString("Set to CHECK"));
		tofUpdateButton.setToolTipText(languageResource.getString("Update"));
	}
	private void translateMainMenu() {
		fileMenu.setText(languageResource.getString("File"));
		fileExitMenuItem.setText(languageResource.getString("Exit"));
		tofMenu.setText(languageResource.getString("News"));
		tofConfigureBoardMenuItem.setText(
			languageResource.getString("Configure selected board"));
		tofDisplayBoardInfoMenuItem.setText(
			languageResource.getString("Display board information window"));
		tofAutomaticUpdateMenuItem.setText(
			languageResource.getString("Automatic message update"));
		tofIncreaseFontSizeMenuItem.setText(
			languageResource.getString("Increase Font Size"));
		tofDecreaseFontSizeMenuItem.setText(
			languageResource.getString("Decrease Font Size"));
		tofDisplayKnownBoards.setText(
			languageResource.getString("Display known boards"));
		optionsMenu.setText(languageResource.getString("Options"));
		optionsPreferencesMenuItem.setText(
			languageResource.getString("Preferences"));
		pluginMenu.setText(languageResource.getString("Plugins"));
		pluginBrowserMenuItem.setText(
			languageResource.getString("Experimental Freenet Browser"));
		pluginTranslateMenuItem.setText(
			languageResource.getString(
				"Translate Frost into another language"));
		languageMenu.setText(languageResource.getString("Language"));
		languageDefaultMenuItem.setText(languageResource.getString("Default"));
		languageDutchMenuItem.setText(languageResource.getString("Dutch"));
		languageEnglishMenuItem.setText(languageResource.getString("English"));
		languageFrenchMenuItem.setText(languageResource.getString("French"));
		languageGermanMenuItem.setText(languageResource.getString("German"));
		languageItalianMenuItem.setText(languageResource.getString("Italian"));
		languageJapaneseMenuItem.setText(
			languageResource.getString("Japanese"));
		languageSpanishMenuItem.setText(languageResource.getString("Spanish"));
		languageBulgarianMenuItem.setText(
			languageResource.getString("Bulgarian"));
		helpMenu.setText(languageResource.getString("Help"));
		helpHelpMenuItem.setText(languageResource.getString("Help"));
		helpAboutMenuItem.setText(languageResource.getString("About"));
	}

	/**Save settings*/
	public void saveSettings() {
		frostSettings.setValue("downloadingActivated", getDownloadPanel().isDownloadingActivated());
		//      frostSettings.setValue("uploadingActivated", uploadActivateCheckBox.isSelected());
		frostSettings.setValue("searchAllBoards", getSearchPanel().isAllBoardsSelected());
		//      frostSettings.setValue("reducedBlockCheck", reducedBlockCheckCheckBox.isSelected());
		frostSettings.setValue("automaticUpdate", tofAutomaticUpdateMenuItem.isSelected());

		// save size and location of window
		Dimension actSize = getSize();
		//        Point actPos = this.getLocationOnScreen();
		frostSettings.setValue("lastFrameHeight", "" + (int) actSize.getHeight());
		frostSettings.setValue("lastFrameWidth", "" + (int) actSize.getWidth());
		//        frostSettings.setValue("lastFrameLocX", ""+(int)actPos.getX() );
		//        frostSettings.setValue("lastFrameLocY", ""+(int)actPos.getY() );

		frostSettings.writeSettingsFile();
		// all other stuff is saved in class Saver
	}

	private UpdatingLanguageResource languageResource =
		new UpdatingLanguageResource(ResourceBundle.getBundle("res.LangRes"));

	private Splashscreen splashscreen;

	private RunningBoardUpdateThreads runningBoardUpdateThreads = null;

	public static Core core;
	FrostBoardObject clipboard = null;
	long counter = 55;

	private static frame1 instance = null; // set in constructor
	public static String fileSeparator = System.getProperty("file.separator");
	// "keypool.dir" is the corresponding key in frostSettings, is set in defaults of SettingsClass.java
	// this is the new way to access this value :)
	public static String keypool = null;
	//	public static String newMessageHeader = new String("");
	//	public static String oldMessageHeader = new String("");
	public static int activeUploadThreads = 0;
	public static int activeDownloadThreads = 0;
	private String lastSelectedMessage;

	public static FrostMessageObject selectedMessage = new FrostMessageObject();
	private static boolean isGeneratingCHK = false;

	public static volatile Object threadCountLock = new Object();

	java.util.Timer guiUpdateTimer = null;

	//the identity stuff.  This really shouldn't be here but where else?
	public static ObjectInputStream id_reader;
	// saved to frost.ini
	public static SettingsClass frostSettings = null;
	public static AltEdit altEdit;

	//------------------------------------------------------------------------
	// Generate objects
	//------------------------------------------------------------------------

	// The main menu
	JMenuBar menuBar = new JMenuBar();

	//File Menu
	JMenu fileMenu = new JMenu();
	JMenuItem fileExitMenuItem = new JMenuItem();

	//Messages (tof) Menu
	JMenu tofMenu = new JMenu();
	JMenuItem tofConfigureBoardMenuItem = new JMenuItem();

	JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem();

	JMenuItem tofIncreaseFontSizeMenuItem = new JMenuItem();
	JMenuItem tofDecreaseFontSizeMenuItem = new JMenuItem();
	JMenuItem tofDisplayKnownBoards = new JMenuItem();

	//Options Menu
	JMenu optionsMenu = new JMenu();
	JMenuItem optionsPreferencesMenuItem = new JMenuItem();

	//Plugin Menu
	JMenu pluginMenu = new JMenu();
	JMenuItem pluginBrowserMenuItem = new JMenuItem();
	JMenuItem pluginTranslateMenuItem = new JMenuItem();

	//Language Menu
	JMenu languageMenu = new JMenu();
	JRadioButtonMenuItem languageDefaultMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageDutchMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageEnglishMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageFrenchMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageGermanMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageItalianMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageJapaneseMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageSpanishMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem languageBulgarianMenuItem = new JRadioButtonMenuItem();

	//Help Menu
	JMenu helpMenu = new JMenu();
	JRadioButtonMenuItem helpHelpMenuItem = new JRadioButtonMenuItem();
	JRadioButtonMenuItem helpAboutMenuItem = new JRadioButtonMenuItem();

	// buttons that are enabled/disabled later
	JButton newBoardButton = null;
	JButton systemTrayButton = null;
	JButton knownBoardsButton = null;
	JButton boardInfoButton = null;
	JButton newFolderButton = null;
	JButton pasteBoardButton = null;
	JButton configBoardButton = null;
	JButton removeBoardButton = null;
	JButton cutBoardButton = null;
	JButton renameBoardButton = null;

	JButton tofNewMessageButton = null;
	JButton tofReplyButton = null;
	JButton downloadAttachmentsButton = null;
	JButton downloadBoardsButton = null;
	JButton saveMessageButton = null;
	JButton trustButton = null;
	JButton notTrustButton = null;
	JButton checkTrustButton = null;
	JButton tofUpdateButton = null;

	// labels that are updated later
	JLabel statusLabel = null;
	JLabel statusMessageLabel = null;
	static ImageIcon[] newMessage = new ImageIcon[2];
	JLabel timeLabel = null;

	JCheckBoxMenuItem tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem();

	JTabbedPane tabbedPane = null;

	JSplitPane attachmentSplitPane = null;
	JSplitPane boardSplitPane = null;

	final String allMessagesCountPrefix = "Msg: ";
	final String newMessagesCountPrefix = "New: ";
	JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix + "0");
	JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix + "0");

	TofTree tofTree = null;

	private UploadTable uploadTable = null;
	private MessageTable messageTable = null;
	private SearchTable searchTable = null;
	private DownloadTable downloadTable = null;
	private JTable attachmentTable = null;
	private JTable boardTable = null;
	private HealingTable healingTable = null;

	private JTextArea tofTextArea = null;
	
	//Panels
	private DownloadPanel downloadPanel = null;
	private UploadPanel uploadPanel = null;
	private SearchPanel searchPanel = null;
	
	//Popups
	private PopupMenuAttachmentBoard popupMenuAttachmentBoard = null;
	private PopupMenuAttachmentTable popupMenuAttachmentTable = null;
	private PopupMenuMessageTable popupMenuMessageTable = null;
	private PopupMenuTofText popupMenuTofText = null;
	private PopupMenuTofTree popupMenuTofTree = null;

	public static Hashtable getMyBatches() {
		return Core.getMyBatches();
	}
	//------------------------------------------------------------------------

	/*************************
	 * GETTER + SETTER       *
	 *************************/
	public static frame1 getInstance() {
		return instance;
	}
	public UploadTable getUploadTable() {
		if (uploadTable == null) {
			UploadTableModel uploadTableModel =
				new UploadTableModel(languageResource);
			uploadTable = new UploadTable(uploadTableModel);
			languageResource.addLanguageListener(uploadTableModel);
		}
		return uploadTable;
	}

	/**
	 * @return
	 */
	private UploadPanel getUploadPanel() {
		if (uploadPanel == null) {
			uploadPanel = new UploadPanel();
			uploadPanel.setUploadTable(getUploadTable());
			uploadPanel.setTofTree(getTofTree());
			uploadPanel.setSettingsClass(frostSettings);
			uploadPanel.setLanguageResource(languageResource);
			uploadPanel.initialize();
		}
		return uploadPanel;
	}
	public MessageTable getMessageTable() {
		return messageTable;
	}
	public SearchTable getSearchTable() {
		if (searchTable == null) {
			SearchTableModel searchTableModel = new SearchTableModel(languageResource);
			searchTable = new SearchTable(searchTableModel);
			languageResource.addLanguageListener(searchTableModel);
		}
		return searchTable;
	}
	
	/**
		 * @return
		 */
	private SearchPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new SearchPanel();
			searchPanel.setSearchTable(getSearchTable());
			searchPanel.setDownloadTable(getDownloadTable());
			searchPanel.setTofTree(getTofTree());
			searchPanel.setKeypool(keypool);
			searchPanel.setLanguageResource(languageResource);
			searchPanel.initialize();
		}
		return searchPanel;		
	}
	public DownloadTable getDownloadTable() {
		if (downloadTable == null) {
			DownloadTableModel downloadTableModel =
				new DownloadTableModel(languageResource);
			downloadTable = new DownloadTable(downloadTableModel);
			languageResource.addLanguageListener(downloadTableModel);
		}
		return downloadTable;
	}
	private HealingTable getHealingTable() {
		if (healingTable == null) {
			HealingTableModel htModel = new HealingTableModel();
			healingTable = new HealingTable(htModel);
		}
		return healingTable;
	}
	public JTable getAttachmentTable() {
		return attachmentTable;
	}
	public JTable getAttachedBoardsTable() {
		return boardTable;
	}
	public String getTofTextAreaText() {
		return tofTextArea.getText();
	}
	public void setTofTextAreaText(String txt) {
		tofTextArea.setText(txt);
	}
	public TofTree getTofTree() {
		if (tofTree == null) {
			// this rootnode is discarded later, but if we create the tree without parameters,
			// a new Model is created wich contains some sample data by default (swing)
			// this confuses our renderer wich only expects FrostBoardObjects in the tree
			FrostBoardObject dummyRootNode =
				new FrostBoardObject("Frost Message System", true);
			tofTree = new TofTree(dummyRootNode);
		}
		return tofTree;
	}
	public RunningBoardUpdateThreads getRunningBoardUpdateThreads() {
		return runningBoardUpdateThreads;
	}

	public static boolean isGeneratingCHK() {
		return isGeneratingCHK;
	}
	public static void setGeneratingCHK(boolean val) {
		isGeneratingCHK = val;
	}

	public FrostBoardObject getSelectedNode() { //TODO: move this method to TofTree
		FrostBoardObject node =
			(FrostBoardObject) getTofTree().getLastSelectedPathComponent();
		if (node == null) {
			// nothing selected? unbelievable ! so select the root ...
			getTofTree().setSelectionRow(0);
			node = (FrostBoardObject) getTofTree().getModel().getRoot();
		}
		return node;
	}

	/**Construct the frame*/
	public frame1(
		SettingsClass newSettings,
		String locale,
		Splashscreen splashscreen) {
		this.splashscreen = splashscreen;

		splashscreen.setText("Initializing Mainframe");
		splashscreen.setProgress(20);

		if (!locale.equals("default"))
			languageResource.setLanguageResource(
				ResourceBundle.getBundle("res.LangRes", new Locale(locale)));
		instance = this;
		frostSettings = newSettings;
		keypool = frostSettings.getValue("keypool.dir");

		splashscreen.setText("Hypercube fluctuating!");
		splashscreen.setProgress(50);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		// enable the machine ;)
		try {
			core = new Core();
			splashscreen.setText("sending IP address to NSA");
			splashscreen.setProgress(60);
			jbInit();
			splashscreen.setText("wasting more time");
			splashscreen.setProgress(70);
			core.init();

			splashscreen.setText("Reaching ridiculous speed...");
			splashscreen.setProgress(80);

			runningBoardUpdateThreads = new RunningBoardUpdateThreads();
			this.guiUpdateTimer = new java.util.Timer();
			//note: changed this from timertask so that I can give it a name --zab
			Thread tickerThread = new Thread("tick tack") {
				public void run() {
					while (true) {
						mixed.wait(1000);
						//TODO: refactor this method in Core. lots of work :)
						timer_actionPerformed();
					}

				}
			};
			tickerThread.start();
		} catch (Throwable t) {
			t.printStackTrace(Core.getOut());
		}

		//Close the splashscreen
		splashscreen.closeMe();

	}

	/**
	 * Configures a button to be a default icon button
	 * @param button The new icon button
	 * @param toolTipText Is displayed when the mousepointer is some seconds over a button
	 * @param rolloverIcon Displayed when mouse is over button
	 */
	protected void configureButton(
		JButton button,
		String toolTipText,
		String rolloverIcon) {
		String text = null;
		try {
			text = languageResource.getString(toolTipText);
		} catch (MissingResourceException ex) {
			text = toolTipText; // better than nothing ;)
		}
		button.setToolTipText(text);

		button.setRolloverIcon(
			new ImageIcon(frame1.class.getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
	}
	private JToolBar buildButtonPanel() {
		timeLabel = new JLabel("");
		// configure buttons
		this.pasteBoardButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/paste.gif")));
		this.configBoardButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/configure.gif")));

		knownBoardsButton =
			new JButton(
				new ImageIcon(
					frame1.class.getResource("/data/knownboards.gif")));
		newBoardButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/newboard.gif")));
		newFolderButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/newfolder.gif")));
		removeBoardButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/remove.gif")));
		renameBoardButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/rename.gif")));
		cutBoardButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/cut.gif")));
		boardInfoButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/info.gif")));
		systemTrayButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/tray.gif")));
		configureButton(
			newBoardButton,
			languageResource.getString("New board"),
			"/data/newboard_rollover.gif");
		configureButton(
			newFolderButton,
			languageResource.getString("New folder"),
			"/data/newfolder_rollover.gif");
		configureButton(
			removeBoardButton,
			languageResource.getString("Remove board"),
			"/data/remove_rollover.gif");
		configureButton(
			renameBoardButton,
			languageResource.getString("Rename folder"),
			"/data/rename_rollover.gif");
		configureButton(
			configBoardButton,
			languageResource.getString("Configure board"),
			"/data/configure_rollover.gif");
		configureButton(
			cutBoardButton,
			languageResource.getString("Cut board"),
			"/data/cut_rollover.gif");
		configureButton(
			pasteBoardButton,
			languageResource.getString("Paste board"),
			"/data/paste_rollover.gif");
		configureButton(
			boardInfoButton,
			languageResource.getString("Board Information Window"),
			"/data/info_rollover.gif");
		configureButton(
			systemTrayButton,
			languageResource.getString("Minimize to System Tray"),
			"/data/tray_rollover.gif");
		configureButton(
			knownBoardsButton,
			languageResource.getString("Display list of known boards"),
			"/data/knownboards_rollover.gif");

		// add action listener
		knownBoardsButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tofDisplayKnownBoardsMenuItem_actionPerformed(e);
			}
		});
		newBoardButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getTofTree().createNewBoard(frame1.getInstance());
			}
		});
		newFolderButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getTofTree().createNewFolder(frame1.getInstance());
			}
		});
		renameBoardButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameNode(getSelectedNode());
			}
		});
		removeBoardButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeNode(getSelectedNode());
			}
		});
		cutBoardButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cutNode(getSelectedNode());
			}
		});
		pasteBoardButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pasteFromClipboard(getSelectedNode());
			}
		});
		configBoardButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tofConfigureBoardMenuItem_actionPerformed(getSelectedNode());
			}
		});
		systemTrayButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try { // Hide the Frost window
					if (JSysTrayIcon.getInstance() != null) {
						JSysTrayIcon.getInstance().showWindow(
							JSysTrayIcon.SHOW_CMD_HIDE);
					}
					//Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayHide.exe");
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
		JToolBar buttonPanel = new JToolBar();
		buttonPanel.setRollover(true);
		buttonPanel.setFloatable(false);
		Dimension blankSpace = new Dimension(3, 3);

		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.add(newBoardButton);
		buttonPanel.add(newFolderButton);
		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.addSeparator();
		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.add(configBoardButton);
		buttonPanel.add(renameBoardButton);
		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.addSeparator();
		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.add(cutBoardButton);
		buttonPanel.add(pasteBoardButton);
		buttonPanel.add(removeBoardButton);
		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.addSeparator();
		buttonPanel.add(Box.createRigidArea(blankSpace));
		buttonPanel.add(boardInfoButton);
		buttonPanel.add(knownBoardsButton);
		if (JSysTrayIcon.getInstance() != null) {
			buttonPanel.add(Box.createRigidArea(blankSpace));
			buttonPanel.addSeparator();
			buttonPanel.add(Box.createRigidArea(blankSpace));

			buttonPanel.add(systemTrayButton);
		}
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(timeLabel);
		buttonPanel.add(Box.createRigidArea(blankSpace));

		return buttonPanel;
	}

	private JPanel buildStatusPanel() {
		statusLabel = new JLabel(languageResource.getString("Frost by Jantho"));
		statusMessageLabel = new JLabel();

		newMessage[0] =
			new ImageIcon(frame1.class.getResource("/data/messagebright.gif"));
		newMessage[1] =
			new ImageIcon(frame1.class.getResource("/data/messagedark.gif"));
		statusMessageLabel.setIcon(newMessage[1]);

		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(statusLabel, BorderLayout.CENTER); // Statusbar
		statusPanel.add(statusMessageLabel, BorderLayout.EAST);
		// Statusbar / new Message
		return statusPanel;
	}

	private JPanel buildTofMainPanel() {
		this.tabbedPane = new JTabbedPane();
		//add a tab for buddies perhaps?
		tabbedPane.add(languageResource.getString("News"), buildMessagePane());
		tabbedPane.add(languageResource.getString("Search"), getSearchPanel());
		tabbedPane.add(
			languageResource.getString("Downloads"),
			getDownloadPanel());
		tabbedPane.add(languageResource.getString("Uploads"), getUploadPanel());

		updateOptionsAffectedComponents();

		JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
		getTofTree().setRootVisible(true);
		tofTree.setCellRenderer(new TofTreeCellRenderer());
		tofTree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		// tofTree selection listener
		tofTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				tofTree_actionPerformed(e);
			}
		});
		//tofTree / KeyEvent
		tofTree.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
				tofTree_keyPressed(e);
			}
			public void keyReleased(KeyEvent e) {
			}
		});

		JSplitPane treeAndTabbedPane =
			new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				tofTreeScrollPane,
				tabbedPane);
		treeAndTabbedPane.setDividerLocation(160);
		// Vertical Board Tree / MessagePane Divider

		JPanel tofMainPanel = new JPanel(new BorderLayout());
		tofMainPanel.add(treeAndTabbedPane, BorderLayout.CENTER); // TOF/Text
		return tofMainPanel;
	}

	private JPanel buildMessagePane() {
		// configure buttons
		this.tofNewMessageButton =
			new JButton(
				new ImageIcon(
					frame1.class.getResource("/data/newmessage.gif")));
		this.tofUpdateButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/update.gif")));
		this.tofReplyButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/reply.gif")));
		this.downloadAttachmentsButton =
			new JButton(
				new ImageIcon(
					frame1.class.getResource("/data/attachment.gif")));
		this.downloadBoardsButton =
			new JButton(
				new ImageIcon(
					frame1.class.getResource("/data/attachmentBoard.gif")));
		this.saveMessageButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/save.gif")));
		this.trustButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/trust.gif")));
		this.notTrustButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/nottrust.gif")));
		this.checkTrustButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/check.gif")));

		configureButton(
			tofNewMessageButton,
			languageResource.getString("New message"),
			"/data/newmessage_rollover.gif");
		configureButton(
			tofUpdateButton,
			languageResource.getString("Update"),
			"/data/update_rollover.gif");
		configureButton(
			tofReplyButton,
			languageResource.getString("Reply"),
			"/data/reply_rollover.gif");
		configureButton(
			downloadAttachmentsButton,
			languageResource.getString("Download attachment(s)"),
			"/data/attachment_rollover.gif");
		configureButton(
			downloadBoardsButton,
			languageResource.getString("Add Board(s)"),
			"/data/attachmentBoard_rollover.gif");
		configureButton(
			saveMessageButton,
			languageResource.getString("Save message"),
			"/data/save_rollover.gif");
		configureButton(trustButton, "Trust", "/data/trust_rollover.gif");
		configureButton(
			notTrustButton,
			languageResource.getString("Do not trust"),
			"/data/nottrust_rollover.gif");
		configureButton(
			checkTrustButton,
			languageResource.getString("Set to CHECK"),
			"/data/check_rollover.gif");

		// add action listener to buttons
		tofUpdateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) { // Update selected board
				// restarts all finished threads if there are some long running threads
				if (isUpdateAllowed(getSelectedNode())) {
					updateBoard(getSelectedNode());
				}
			}
		});
		tofNewMessageButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tofNewMessageButton_actionPerformed(e);
			}
		});
		downloadAttachmentsButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadAttachments();
			}
		});
		downloadBoardsButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadBoards();
			}
		});
		tofReplyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tofReplyButton_actionPerformed(e);
			}
		});
		saveMessageButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileAccess.saveDialog(
					getInstance(),
					getTofTextAreaText(),
					frostSettings.getValue("lastUsedDirectory"),
					languageResource.getString("Save message to disk"));
			}
		});
		trustButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				trustButton_actionPerformed(e);
			}
		});
		notTrustButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notTrustButton_actionPerformed(e);
			}
		});
		checkTrustButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkTrustButton_actionPerformed(e);
			}
		});
		// build buttons panel
		JToolBar tofTopPanel = new JToolBar();
		tofTopPanel.setRollover(true);
		tofTopPanel.setFloatable(false);
		Dimension blankSpace = new Dimension(3, 3);

		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.add(saveMessageButton); // TOF/ Save Message
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.addSeparator();
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.add(tofNewMessageButton); // TOF/ New Message
		tofTopPanel.add(tofReplyButton); // TOF/ Reply
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.addSeparator();
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.add(tofUpdateButton); // TOF/ Update
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.addSeparator();
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.add(downloadAttachmentsButton);
		// TOF/ Download Attachments
		tofTopPanel.add(downloadBoardsButton); // TOF/ Download Boards
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.addSeparator();
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		tofTopPanel.add(trustButton); //TOF /trust
		tofTopPanel.add(checkTrustButton); //TOF /check trust
		tofTopPanel.add(notTrustButton); //TOF /do not trust

		tofTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		tofTopPanel.add(Box.createHorizontalGlue());
		JLabel dummyLabel = new JLabel(allMessagesCountPrefix + "00000");
		dummyLabel.doLayout();
		Dimension labelSize = dummyLabel.getPreferredSize();
		allMessagesCountLabel.setPreferredSize(labelSize);
		allMessagesCountLabel.setMinimumSize(labelSize);
		newMessagesCountLabel.setPreferredSize(labelSize);
		newMessagesCountLabel.setMinimumSize(labelSize);
		tofTopPanel.add(allMessagesCountLabel);
		tofTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		tofTopPanel.add(newMessagesCountLabel);
		tofTopPanel.add(Box.createRigidArea(blankSpace));
		// build panel wich shows the message list + message
		MessageTableModel messageTableModel = new MessageTableModel(languageResource);
		languageResource.addLanguageListener(messageTableModel);
		this.messageTable = new MessageTable(messageTableModel);
		messageTable.setSelectionMode(
			DefaultListSelectionModel.SINGLE_SELECTION);
		messageTable
			.getSelectionModel()
			.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				messageTableListModel_valueChanged(e);
			}
		});
		JScrollPane messageTableScrollPane = new JScrollPane(messageTable);

		this.tofTextArea = new JTextArea();
		JScrollPane tofTextAreaScrollPane = new JScrollPane(tofTextArea);
		tofTextArea.setEditable(false);
		tofTextArea.setLineWrap(true);
		tofTextArea.setWrapStyleWord(true);

		AttachedFilesTableModel attachmentTableModel =
			new AttachedFilesTableModel();
		this.attachmentTable = new JTable(attachmentTableModel);
		JScrollPane attachmentTableScrollPane =
			new JScrollPane(attachmentTable);

		AttachedBoardTableModel boardTableModel = new AttachedBoardTableModel();
		this.boardTable = new JTable(boardTableModel);
		JScrollPane boardTableScrollPane = new JScrollPane(boardTable);

		this.attachmentSplitPane =
			new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				tofTextAreaScrollPane,
				attachmentTableScrollPane);
		this.boardSplitPane =
			new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				attachmentSplitPane,
				boardTableScrollPane);

		JSplitPane tofSplitPane =
			new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				messageTableScrollPane,
				boardSplitPane);
		tofSplitPane.setDividerSize(10);
		tofSplitPane.setDividerLocation(160);
		tofSplitPane.setResizeWeight(0.5d);
		tofSplitPane.setMinimumSize(new Dimension(50, 20));

		// build panel
		JPanel messageTablePanel = new JPanel(new BorderLayout());
		messageTablePanel.add(tofTopPanel, BorderLayout.NORTH);
		messageTablePanel.add(tofSplitPane, BorderLayout.CENTER);
		return messageTablePanel;
	}

	/**
	 * Called by frost after call of show().
	 * Sets the initial states of the message splitpanes.
	 * Must be called AFTER frame is shown.
	 **/
	public void resetMessageViewSplitPanes() {
		// initially hide the attachment tables
		attachmentSplitPane.setDividerSize(0);
		attachmentSplitPane.setDividerLocation(1.0);
		boardSplitPane.setDividerSize(0);
		boardSplitPane.setDividerLocation(1.0);
		setTofTextAreaText(
			languageResource.getString(
				"Select a message to view its content."));
	}

	//**********************************************************************************************
	//**********************************************************************************************
	//**********************************************************************************************
	/**Component initialization*/
	private void jbInit() throws Exception {
		setIconImage(
			Toolkit.getDefaultToolkit().createImage(
				frame1.class.getResource("/data/jtc.jpg")));
		this.setResizable(true);
	
		this.setTitle("Frost");
	
		JPanel contentPanel = (JPanel) this.getContentPane();
		contentPanel.setLayout(new BorderLayout());
	
		contentPanel.add(buildButtonPanel(), BorderLayout.NORTH);
		// buttons toolbar
		contentPanel.add(buildTofMainPanel(), BorderLayout.CENTER);
		// tree / tabbed pane
		contentPanel.add(buildStatusPanel(), BorderLayout.SOUTH); // Statusbar
	
		buildMenuBar();
	
		//**********************************************************************************************
		//**********************************************************************************************
		//**********************************************************************************************
	
		/*configureCheckBox(searchAllBoardsCheckBox,
		             "Search all boards",
		             "data/allboards_rollover.gif",
		             "data/allboards_selected.gif",
		             "data/allboards_selected_rollover.gif");*/
	
		// Add Popup listeners
		MouseListener popupListener = new PopupListener();
		tofTextArea.addMouseListener(popupListener);
		getTofTree().addMouseListener(popupListener);
		getAttachmentTable().addMouseListener(popupListener);
		getAttachedBoardsTable().addMouseListener(popupListener);
		messageTable.addMouseListener(popupListener);
	
		//**********************************************************************************************
		//**********************************************************************************************
		//**********************************************************************************************
	
		//------------------------------------------------------------------------
	
		tofReplyButton.setEnabled(false);
		downloadAttachmentsButton.setEnabled(false);
		downloadBoardsButton.setEnabled(false);
		saveMessageButton.setEnabled(false);
		pasteBoardButton.setEnabled(false);
		trustButton.setEnabled(false);
		notTrustButton.setEnabled(false);
		checkTrustButton.setEnabled(false);
	
		//on with other stuff
	
		getTofTree().initialize();
	
		// step through all messages on disk up to maxMessageDisplay and check if there are new messages
		// if a new message is in a folder, this folder is show yellow in tree
		TOF.initialSearchNewMessages();
	
		if (core.isFreenetOnline()) {
			getDownloadPanel().setDownloadingActivated(
				frostSettings.getBoolValue("downloadingActivated"));
			tofAutomaticUpdateMenuItem.setSelected(
				frostSettings.getBoolValue("automaticUpdate"));
		} else {
			getDownloadPanel().setDownloadingActivated(false);
			tofAutomaticUpdateMenuItem.setSelected(false);
		}
		getSearchPanel().setAllBoardsSelected(frostSettings.getBoolValue("searchAllBoards"));
		//      uploadActivateCheckBox.setSelected(frostSettings.getBoolValue("uploadingActivated"));
		//      reducedBlockCheckCheckBox.setSelected(frostSettings.getBoolValue("reducedBlockCheck"));
	
		if (getTofTree().getRowCount()
			> frostSettings.getIntValue("tofTreeSelectedRow"))
			getTofTree().setSelectionRow(
				frostSettings.getIntValue("tofTreeSelectedRow"));
	
		// make sure the font size isn't too small to see
		if (frostSettings.getFloatValue("tofFontSize") < 6.0f)
			frostSettings.setValue("tofFontSize", 6.0f);
	
		// always use monospaced font for tof text
		Font tofFont =
			new Font(
				"Monospaced",
				Font.PLAIN,
				(int) frostSettings.getFloatValue("tofFontSize"));
		tofTextArea.setFont(tofFont);
	
		// Load table settings
		getDownloadTable().load();
		getUploadTable().load();
	
		// load size and location of window
		int lastHeight = frostSettings.getIntValue("lastFrameHeight");
		int lastWidth = frostSettings.getIntValue("lastFrameWidth");
		//    int lastX = frostSettings.getIntValue("lastFrameLocX" );
		//    int lastY = frostSettings.getIntValue("lastFrameLocY" );
	
		if (lastWidth < 100 || lastHeight < 100) {
			// set default size
			this.setSize(new Dimension(790, 580));
		} else {
			this.setSize(lastWidth, lastHeight);
			//        this.setLocation( lastX, lastY );
		}
	
	} // ************** end-of: jbInit()

	private ImageIcon getScaledImage(String imgPath) {
		ImageIcon icon = new ImageIcon(frame1.class.getResource(imgPath));
		icon =
			new ImageIcon(
				icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		return icon;
	}

    /**
	 * Build the menu bar.
	 * Should be called only once.
	 */
	private void buildMenuBar() {
		tofConfigureBoardMenuItem.setIcon(
			getScaledImage("/data/configure.gif"));
		tofDisplayBoardInfoMenuItem.setIcon(getScaledImage("/data/info.gif"));
		tofAutomaticUpdateMenuItem.setSelected(true);
		tofDisplayKnownBoards.setIcon(getScaledImage("/data/knownboards.gif"));

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
				// make the font size in the TOF text area one point bigger
				Font f = tofTextArea.getFont();
				frostSettings.setValue("tofFontSize", f.getSize() + 1.0f);
				f = f.deriveFont(frostSettings.getFloatValue("tofFontSize"));
				tofTextArea.setFont(f);
			}
		});
		tofDecreaseFontSizeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// make the font size in the TOF text area one point smaller
				Font f = tofTextArea.getFont();
				frostSettings.setValue("tofFontSize", f.getSize() - 1.0f);
				f = f.deriveFont(frostSettings.getFloatValue("tofFontSize"));
				tofTextArea.setFont(f);
			}
		});
		tofConfigureBoardMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tofConfigureBoardMenuItem_actionPerformed(getSelectedNode());
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
				browser.show();
			}
		});
		pluginTranslateMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TranslateFrame translate = new TranslateFrame(true);
				translate.show();
			}
		});
		languageDefaultMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle("res.LangRes");
				setLanguageResource(bundle);
			}
		});
		languageGermanMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("de"));
				setLanguageResource(bundle);
			}
		});
		languageEnglishMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("en"));
				setLanguageResource(bundle);
			}
		});
		languageDutchMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("nl"));
				setLanguageResource(bundle);
			}
		});
		languageFrenchMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("fr"));
				setLanguageResource(bundle);
			}
		});
		languageJapaneseMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("ja"));
				setLanguageResource(bundle);
			}
		});
		languageItalianMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("it"));
				setLanguageResource(bundle);
			}
		});
		languageSpanishMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("es"));
				setLanguageResource(bundle);
			}
		});
		languageBulgarianMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.ResourceBundle bundle =
					java.util.ResourceBundle.getBundle(
						"res.LangRes",
						new Locale("bg"));
				setLanguageResource(bundle);
			}
		});
		helpHelpMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpFrame dlg = new HelpFrame(getInstance());
				dlg.show();
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
		tofMenu.add(tofIncreaseFontSizeMenuItem);
		tofMenu.add(tofDecreaseFontSizeMenuItem);
		tofMenu.addSeparator();
		tofMenu.add(tofConfigureBoardMenuItem);
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
		
		this.setJMenuBar(menuBar);
	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	/**
	 * Adds either the selected or all files from the attachmentTable to downloads table.
	 */
	public void downloadAttachments() {
		int[] selectedRows = attachmentTable.getSelectedRows();

		// If no rows are selected, add all attachments to download table
		if (selectedRows.length == 0) {
			for (int i = 0;
				i < getAttachmentTable().getModel().getRowCount();
				i++) {
				String filename =
					(String) getAttachmentTable().getModel().getValueAt(i, 0);
				String key =
					(String) getAttachmentTable().getModel().getValueAt(i, 1);
				FrostDownloadItemObject dlItem =
					new FrostDownloadItemObject(
						filename,
						key,
						getSelectedNode());
				boolean added = getDownloadTable().addDownloadItem(dlItem);
			}
		} else {
			for (int i = 0; i < selectedRows.length; i++) {
				String filename =
					(String) getAttachmentTable().getModel().getValueAt(
						selectedRows[i],
						0);
				String key =
					(String) getAttachmentTable().getModel().getValueAt(
						selectedRows[i],
						1);
				FrostDownloadItemObject dlItem =
					new FrostDownloadItemObject(
						filename,
						key,
						getSelectedNode());
				boolean added = getDownloadTable().addDownloadItem(dlItem);
			}
		}
	}

	/**
	 * Adds all boards from the attachedBoardsTable to board list.
	 */
	private void downloadBoards() {
		Core.getOut().println("adding boards");
		int[] selectedRows = getAttachedBoardsTable().getSelectedRows();

		if (selectedRows.length == 0) {
			// add all rows
			getAttachedBoardsTable().selectAll();
			selectedRows = getAttachedBoardsTable().getSelectedRows();
			if (selectedRows.length == 0)
				return;
		}
		for (int i = 0; i < selectedRows.length; i++) {
			String name =
				(String) getAttachedBoardsTable().getModel().getValueAt(
					selectedRows[i],
					0);
			String pubKey =
				(String) getAttachedBoardsTable().getModel().getValueAt(
					selectedRows[i],
					1);
			String privKey =
				(String) getAttachedBoardsTable().getModel().getValueAt(
					selectedRows[i],
					2);

			// prepare key vars for creation of FrostBoardObject (val=null if key is empty)
			if (privKey.compareTo("N/A") == 0 || privKey.length() == 0) {
				privKey = null;
			}
			if (pubKey.compareTo("N/A") == 0 || pubKey.length() == 0) {
				pubKey = null;
			}

			// search board in exising boards list
			FrostBoardObject board = getTofTree().getBoardByName(name);

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
					board.setPublicKey(pubKey);
					board.setPrivateKey(privKey);
					updateTofTree(board);
				}
			} else {
				// its a new board
				getTofTree().addNodeToTree(
					new FrostBoardObject(name, pubKey, privKey));
			}
		}
	}

	/**
	 * Returns true if board is allowed to be updated.
	 * Also checks if board update is already running.
	 */
	public boolean doUpdate(FrostBoardObject board) {
		if (isUpdateAllowed(board) == false)
			return false;

		if (board.isUpdating())
			return false;

		return true;
	}

	/**
	 * Returns true if board is allowed to be updated.
	 * Does NOT check if board update is already running.
	 */
	public boolean isUpdateAllowed(FrostBoardObject board) {
		if (board == null)
			return false;
		// Do not allow folders to update
		if (board.isFolder())
			return false;

		if (board.isSpammed())
			return false;

		return true;
	}

	/**tof / Update*/
	/**
	 * Starts the board update threads, getRequest thread and update id thread.
	 * Checks for each type of thread if its already running, and starts allowed
	 * not-running threads for this board.
	 */
	public void updateBoard(FrostBoardObject board) {
		if (board == null || board.isFolder())
			return;

		boolean threadStarted = false;
		ChangeUpdateStateListener listener = new ChangeUpdateStateListener();

		// first download the messages of today
		if (getRunningBoardUpdateThreads()
			.isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_TODAY)
			== false) {
			getRunningBoardUpdateThreads().startMessageDownloadToday(
				board,
				frostSettings,
				listener);
			Core.getOut().println(
				"Starting update (MSG_TODAY) of " + board.toString());
			threadStarted = true;
		}

		// maybe get the files list
		if (!frostSettings.getBoolValue("disableRequests")
			&& !getRunningBoardUpdateThreads().isThreadOfTypeRunning(
				board,
				BoardUpdateThread.BOARD_FILE_UPLOAD)) {
			getRunningBoardUpdateThreads().startBoardFilesUpload(
				board,
				frostSettings,
				listener);
			Core.getOut().println(
				"Starting update (BOARD_UPLOAD) of " + board.toString());
			threadStarted = true;
		}

		if (!frostSettings.getBoolValue("disableDownloads")
			&& !getRunningBoardUpdateThreads().isThreadOfTypeRunning(
				board,
				BoardUpdateThread.BOARD_FILE_DNLOAD)) {
			getRunningBoardUpdateThreads().startBoardFilesDownload(
				board,
				frostSettings,
				listener);
			Core.getOut().println(
				"Starting update (BOARD_DOWNLOAD) of " + board.toString());
			threadStarted = true;
		}

		// finally get the older messages
		if (getRunningBoardUpdateThreads()
			.isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_BACK)
			== false) {
			getRunningBoardUpdateThreads().startMessageDownloadBack(
				board,
				frostSettings,
				listener);
			Core.getOut().println(
				"Starting update (MSG_BACKLOAD) of " + board.toString());
			threadStarted = true;
		}

		// if there was a new thread started, update the lastUpdateStartTimeMillis
		if (threadStarted == true) {
			board.setLastUpdateStartMillis(System.currentTimeMillis());
		}
	}

	/**
	 * The listeners changes the 'updating' state of a board if a thread starts/finished.
	 */
	private class ChangeUpdateStateListener
		implements BoardUpdateThreadListener {
		public void boardUpdateThreadFinished(final BoardUpdateThread thread) {
			int running =
				getRunningBoardUpdateThreads()
					.getDownloadThreadsForBoard(thread.getTargetBoard())
					.size();
			//+ getRunningBoardUpdateThreads().getUploadThreadsForBoard(thread.getTargetBoard()).size();
			if (running == 0) {
				// remove update state from board
				thread.getTargetBoard().setUpdating(false);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateTofTree(thread.getTargetBoard());
					}
				});
			}
		}
		public void boardUpdateThreadStarted(final BoardUpdateThread thread) {
			thread.getTargetBoard().setUpdating(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateTofTree(thread.getTargetBoard());
				}
			});
		}
	}

	/**
	 * Fires a nodeChanged (redraw) for this board and updates buttons.
	 */
	public void updateTofTree(FrostBoardObject board) {
		// fire update for node
		DefaultTreeModel model = (DefaultTreeModel) getTofTree().getModel();
		model.nodeChanged(board);
		// also update all parents
		TreeNode parentFolder = (FrostBoardObject) board.getParent();
		if (parentFolder != null) {
			model.nodeChanged(parentFolder);
			parentFolder = parentFolder.getParent();
		}

		if (board == getSelectedNode()) // is the board actually shown?
			{
			updateButtons(board);
		}
	}
	/**
	 * Fires a nodeChanged (redraw) for all boards.
	 * ONLY used to redraw tree after run of OptionsFrame.
	 */
	public void updateTofTree() {
		// fire update for node
		DefaultTreeModel model = (DefaultTreeModel) getTofTree().getModel();
		Enumeration e =
			((FrostBoardObject) model.getRoot()).depthFirstEnumeration();
		while (e.hasMoreElements()) {
			model.nodeChanged(((FrostBoardObject) e.nextElement()));
		}
	}

	private void updateButtons(FrostBoardObject board) {
		if (board.isReadAccessBoard()) {
			tofNewMessageButton.setEnabled(false);
			getUploadPanel().setAddFilesButtonEnabled(false);
		} else {
			tofNewMessageButton.setEnabled(true);
			getUploadPanel().setAddFilesButtonEnabled(true);
		}
	}

	/**TOF Board selected*/
	// Core.getOut()
	// if e == NULL, the method is called by truster or by the reloader after options were changed
	// in this cases we usually should left select the actual message (if one) while reloading the table
	public void tofTree_actionPerformed(TreeSelectionEvent e) {
		int i[] = getTofTree().getSelectionRows();
		if (i != null && i.length > 0) {
			frostSettings.setValue("tofTreeSelectedRow", i[0]);
		}

		FrostBoardObject node =
			(FrostBoardObject) getTofTree().getLastSelectedPathComponent();

		resetMessageViewSplitPanes(); // clear message view

		if (node != null) {
			if (node.isFolder() == false) {
				// node is a board
				configBoardButton.setEnabled(true);
				tofNewMessageButton.setEnabled(true);
				tofUpdateButton.setEnabled(true);

				saveMessageButton.setEnabled(false);
				removeBoardButton.setEnabled(true);

				updateButtons(node);

				Core.getOut().println(
					"Board "
						+ node.toString()
						+ " blocked count: "
						+ node.getBlockedCount());

				getUploadPanel().setAddFilesButtonEnabled(true);
				renameBoardButton.setEnabled(false);
				tofReplyButton.setEnabled(false);
				downloadAttachmentsButton.setEnabled(false);
				downloadBoardsButton.setEnabled(false);

				// read all messages for this board into message table
				TOF.updateTofTable(node, keypool);
				messageTable.clearSelection();
			} else {
				// node is a folder
				MessageTableModel model =
					(MessageTableModel) getMessageTable().getModel();
				model.clearDataModel();
				updateMessageCountLabels(node);

				getUploadPanel().setAddFilesButtonEnabled(false);
				renameBoardButton.setEnabled(true);
				configBoardButton.setEnabled(false);
				tofNewMessageButton.setEnabled(false);
				tofUpdateButton.setEnabled(false);
				if (node.isRoot()) {
					removeBoardButton.setEnabled(false);
					cutBoardButton.setEnabled(false);
				} else {
					removeBoardButton.setEnabled(true);
					cutBoardButton.setEnabled(true);
				}
			}
		}
	}

	/**
	 * starts update for the selected board, or for all childs (and their childs) of a folder
	 */
	private void refreshNode(FrostBoardObject node) {
		if (node == null)
			return;

		if (node.isFolder() == false) {
			if (isUpdateAllowed(node)) {
				updateBoard(node);
			}
		} else {
			// update all childs recursiv
			Enumeration leafs = node.children();
			while (leafs.hasMoreElements())
				refreshNode((FrostBoardObject) leafs.nextElement());
		}
	}

	/**
	 * Removes the given tree node, asks before deleting.
	 */
	public void removeNode(FrostBoardObject selectedNode) {
		String txt;
		if (selectedNode.isFolder()) {
			txt =
				"Do you really want to delete folder '"
					+ selectedNode.toString()
					+ "' ???"
					+ "\nNOTE: Removing it will also remove all boards/folders inside this folder!!!";
		} else {
			txt =
				"Do you really want to delete board '"
					+ selectedNode.toString()
					+ "' ???";
		}

		int answer =
			JOptionPane.showConfirmDialog(
				this,
				txt,
				"Delete '" + selectedNode.toString() + "'?",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.NO_OPTION) {
			return;
		}

		// ask user if to delete board directory also
		boolean deleteDirectory = false;
		String boardRelDir =
			frostSettings.getValue("keypool.dir")
				+ selectedNode.getBoardFilename();
		if (selectedNode.isFolder() == false) {
			txt =
				"Do you want to delete also the board directory '"
					+ boardRelDir
					+ "' ?\n"
					+ "This directory contains all received messages and file lists for this board.\n"
					+ "(NOTE: The board MUST not updating to delete it!\n"
					+ "Currently there is no way to stop the updating of a board,\n"
					+ "so please ensure this board is'nt updating right now,\n"
					+ "or you have to live with the consequences ;) )\n\n"
					+ "You can also delete the directory by yourself after shutdown of Frost.";
			answer =
				JOptionPane.showConfirmDialog(
					this,
					txt,
					"Delete directory of '" + selectedNode.toString() + "'?",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				deleteDirectory = true;
			} else if (answer == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}

		// delete node from tree
		getTofTree().removeNode(selectedNode);

		// maybe delete board dir (in a thread, do not block gui)
		if (deleteDirectory) {
			if (selectedNode.isUpdating() == false) {
				core.deleteDir(boardRelDir);
			} else {
				Core.getOut().println(
					"WARNING: Although being warned, you tried to delete a board with is updating! Skipped ...");
			}
		}
	}

	/**
	 * Opens dialog to rename the board / folder.
	 * For boards it checks for double names.
	 */
	public void renameNode(FrostBoardObject selected) {
		if (selected == null)
			return;
		String newname = null;
		do {
			newname =
				JOptionPane.showInputDialog(
					this,
					"Please enter the new name:\n",
					selected.toString());
			if (newname == null)
				return; // cancel
			if (selected.isFolder() == false
				&& // double folder names are ok
			getTofTree().getBoardByName(newname)
					!= null) {
				JOptionPane.showMessageDialog(
					this,
					"You already have a board with name '"
						+ newname
						+ "'!\nPlease choose a new name.");
				newname = ""; // loop again
			}
		} while (newname.length() == 0);

		selected.setBoardName(newname);
		updateTofTree(selected);
	}

	public void pasteFromClipboard(FrostBoardObject node) {
		if (clipboard == null) {
			pasteBoardButton.setEnabled(false);
			return;
		}

		if (getTofTree().pasteFromClipboard(clipboard, node) == true) {
			clipboard = null;
			pasteBoardButton.setEnabled(false);
		}
	}

	public void cutNode(FrostBoardObject cuttedNode) {
		cuttedNode = getTofTree().cutNode(cuttedNode);
		if (cuttedNode != null) {
			clipboard = cuttedNode;
			pasteBoardButton.setEnabled(true);
		}
	}

	/**Get keyTyped for tofTree*/
	public void tofTree_keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if (!getTofTree().isEditing()) {
			if (key == KeyEvent.VK_DELETE)
				removeNode(getSelectedNode());
			if (key == KeyEvent.VK_N)
				getTofTree().createNewBoard(frame1.getInstance());
			if (key == KeyEvent.VK_X)
				cutNode(getSelectedNode());
			if (key == KeyEvent.VK_V)
				pasteFromClipboard(getSelectedNode());
		}
	}

	/**valueChanged messageTable (messageTableListModel / TOF)*/
	public void messageTableListModel_valueChanged(ListSelectionEvent e) {
		FrostBoardObject selectedBoard = getSelectedNode();
		if (selectedBoard.isFolder())
			return;
		selectedMessage = TOF.evalSelection(e, messageTable, selectedBoard);
		if (selectedMessage != null) {
			displayNewMessageIcon(false);
			downloadAttachmentsButton.setEnabled(false);
			downloadBoardsButton.setEnabled(false);

			lastSelectedMessage = selectedMessage.getSubject();
			if (selectedBoard.isReadAccessBoard() == false) {
				tofReplyButton.setEnabled(true);
			}

			if (selectedMessage
				.getStatus()
				.trim()
				.equals(VerifyableMessageObject.PENDING)) {
				trustButton.setEnabled(true);
				notTrustButton.setEnabled(true);
				checkTrustButton.setEnabled(false);
			} else if (
				selectedMessage.getStatus().trim().equals(
					VerifyableMessageObject.VERIFIED)) {
				trustButton.setEnabled(false);
				notTrustButton.setEnabled(true);
				checkTrustButton.setEnabled(true);
			} else if (
				selectedMessage.getStatus().trim().equals(
					VerifyableMessageObject.FAILED)) {
				trustButton.setEnabled(true);
				notTrustButton.setEnabled(false);
				checkTrustButton.setEnabled(true);
			} else {
				trustButton.setEnabled(false);
				notTrustButton.setEnabled(false);
				checkTrustButton.setEnabled(false);
			}

			setTofTextAreaText(selectedMessage.getContent());
			if (selectedMessage.getContent().length() > 0)
				saveMessageButton.setEnabled(true);
			else
				saveMessageButton.setEnabled(false);

			Vector fileAttachments = selectedMessage.getFileAttachments();
			Vector boardAttachments = selectedMessage.getBoardAttachments();

			if (fileAttachments.size() == 0 && boardAttachments.size() == 0) {
				// Move divider to 100% and make it invisible
				attachmentSplitPane.setDividerSize(0);
				attachmentSplitPane.setDividerLocation(1.0);
				boardSplitPane.setDividerSize(0);
				boardSplitPane.setDividerLocation(1.0);
			} else {
				// Attachment available
				if (fileAttachments.size() > 0) {
					// Add attachments to table
					(
						(DefaultTableModel) getAttachmentTable()
							.getModel())
							.setDataVector(
						selectedMessage.getFileAttachments(),
						null);

					if (boardAttachments.size() == 0) {
						boardSplitPane.setDividerSize(0);
						boardSplitPane.setDividerLocation(1.0);
					}
					attachmentSplitPane.setDividerLocation(0.75);
					attachmentSplitPane.setDividerSize(3);

					downloadAttachmentsButton.setEnabled(true);
				}
				// Board Available
				if (boardAttachments.size() > 0) {
					// Add attachments to table
					(
						(DefaultTableModel) getAttachedBoardsTable()
							.getModel())
							.setDataVector(
						selectedMessage.getBoardAttachments(),
						null);

					//only a board, no attachments.
					if (fileAttachments.size() == 0) {
						attachmentSplitPane.setDividerSize(0);
						attachmentSplitPane.setDividerLocation(1.0);
					}
					boardSplitPane.setDividerLocation(0.75);
					boardSplitPane.setDividerSize(3);

					downloadBoardsButton.setEnabled(true);
					//TODO: downloadBoardsButton
				}
			}
		} else {
			// no msg selected
			resetMessageViewSplitPanes(); // clear message view
			tofReplyButton.setEnabled(false);
			saveMessageButton.setEnabled(false);
			downloadAttachmentsButton.setEnabled(false);
			downloadBoardsButton.setEnabled(false);
		}
	}

	/**Selects message icon in lower right corner*/
	public static void displayNewMessageIcon(boolean showNewMessageIcon) {
		frame1 frame1inst = frame1.getInstance();
		if (showNewMessageIcon) {
			frame1inst.setIconImage(
				Toolkit.getDefaultToolkit().createImage(
					frame1.class.getResource("/data/newmessage.gif")));
			frame1inst.statusMessageLabel.setIcon(newMessage[0]);
			// The title should never be changed on Windows systems (SystemTray.exe expects "Frost" as title)
			if ((System.getProperty("os.name").startsWith("Windows"))
				== false) {
				frame1inst.setTitle("*Frost*");
			}
		} else {
			frame1inst.setIconImage(
				Toolkit.getDefaultToolkit().createImage(
					frame1.class.getResource("/data/jtc.jpg")));
			frame1inst.statusMessageLabel.setIcon(newMessage[1]);
			// The title should never be changed on Windows systems (SystemTray.exe expects "Frost" as title)
			if ((System.getProperty("os.name").startsWith("Windows"))
				== false) {
				frame1inst.setTitle("Frost");
			}
		}
	}

	public void prepareUploadHashes() {
		UploadTableModel ulModel =
			(UploadTableModel) getUploadTable().getModel();
		if (ulModel.getRowCount() > 0)
			for (int i = 0; i < ulModel.getRowCount(); i++) {
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject) ulModel.getRow(i);
				if (ulItem.getSHA1() == null) {
					setGeneratingCHK(true);
					ulItem.setKey("Working...");
					ulModel.updateRow(ulItem);
					insertThread newInsert =
						new insertThread(
							ulItem,
							frostSettings,
							insertThread.MODE_GENERATE_SHA1);
					newInsert.start();
					break; //start only one thread/second
				}
			}

	}

	/**timer Action Listener (automatic download)*/
	public void timer_actionPerformed() {
		// this method is called by a timer each second, so this counter counts seconds
		counter++;

		// Display welcome message if no boards are available
		if (((TreeNode) getTofTree().getModel().getRoot()).getChildCount()
			== 0) {
			attachmentSplitPane.setDividerSize(0);
			attachmentSplitPane.setDividerLocation(1.0);
			setTofTextAreaText(languageResource.getString("Welcome message"));
		}

		//////////////////////////////////////////////////
		//   Misc. stuff
		//////////////////////////////////////////////////
		if (counter % 180 == 0) // Check uploadTable every 3 minutes
			{
			getUploadTable().removeNotExistingFiles();
		}

		if (counter % 300 == 0
			&& frostSettings.getBoolValue("removeFinishedDownloads")) {
			getDownloadTable().removeFinishedDownloads();
		}

		updateDownloadCountLabel();

		//////////////////////////////////////////////////
		//   Automatic TOF update
		//////////////////////////////////////////////////
		if (counter % 15 == 0
			&& // check all 5 seconds if a board update could be started
		tofAutomaticUpdateMenuItem
				.isSelected()
			&& getRunningBoardUpdateThreads().getUpdatingBoardCount()
				< frostSettings.getIntValue(
					"automaticUpdate.concurrentBoardUpdates")) {
			Vector boards = getTofTree().getAllBoards();
			if (boards.size() > 0) {
				FrostBoardObject actualBoard = selectNextBoard(boards);
				if (actualBoard != null) {
					updateBoard(actualBoard);
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
				.append(languageResource.getString("Up") + ": ")
				.append(activeUploadThreads)
				.append("   " + languageResource.getString("Down") + ": ")
				.append(activeDownloadThreads)
				.append("   " + languageResource.getString("TOFUP") + ": ")
				.append(getRunningBoardUpdateThreads().getUploadingBoardCount())
				.append("B / ")
				.append(
					getRunningBoardUpdateThreads()
						.getRunningUploadThreadCount())
				.append("T")
				.append("   " + languageResource.getString("TOFDO") + ": ")
				.append(getRunningBoardUpdateThreads().getUpdatingBoardCount())
				.append("B / ")
				.append(
					getRunningBoardUpdateThreads()
						.getRunningDownloadThreadCount())
				.append("T")
				.append(
					"   "
						+ languageResource.getString("Selected board")
						+ ": ")
				.append(getSelectedNode().toString())
				.toString();
		statusLabel.setText(newText);

		//////////////////////////////////////////////////
		// Generate CHK's for upload table entries
		//////////////////////////////////////////////////
		/**  Do not generate CHKs, get SHA1 only! */
		//do this only if the automatic index handling is set
		/**  and generate CHK if requested ... */
		boolean automaticIndexing =
			frostSettings.getBoolValue("automaticIndexing");
		if (isGeneratingCHK() == false)
			// do not start another generate if there is already 1 running
			{
			if (automaticIndexing)
				prepareUploadHashes();
			UploadTableModel ulModel =
				(UploadTableModel) getUploadTable().getModel();
			if (ulModel.getRowCount() > 0) {
				for (int i = 0; i < ulModel.getRowCount(); i++) {
					FrostUploadItemObject ulItem =
						(FrostUploadItemObject) ulModel.getRow(i);
					if (ulItem.getState()
						== FrostUploadItemObject.STATE_ENCODING_REQUESTED
						|| (ulItem.getKey() == null
							&& ulItem.getState()
								== FrostUploadItemObject.STATE_REQUESTED)) {
						setGeneratingCHK(true);
						insertThread newInsert = null;
						if (ulItem.getState()
							== FrostUploadItemObject.STATE_REQUESTED) {
							// set next state for item to REQUESTED, default is IDLE
							// needed to keep the REQUESTED state for real uploading
							newInsert =
								new insertThread(
									ulItem,
									frostSettings,
									insertThread.MODE_GENERATE_CHK,
									FrostUploadItemObject.STATE_REQUESTED);
						} else {
							// next state will be IDLE (=default)
							newInsert =
								new insertThread(
									ulItem,
									frostSettings,
									insertThread.MODE_GENERATE_CHK);
						}
						ulItem.setState(FrostUploadItemObject.STATE_ENCODING);
						ulModel.updateRow(ulItem);
						newInsert.start();
						break; // start only 1 thread per loop (=second)
					}
				}
			}
		}

		//////////////////////////////////////////////////
		// Start upload thread
		//////////////////////////////////////////////////
		int activeUthreads = 0;
		synchronized (threadCountLock) {
			activeUthreads = activeUploadThreads;
		}
		if (activeUthreads < frostSettings.getIntValue("uploadThreads")) {
			UploadTableModel ulModel =
				(UploadTableModel) getUploadTable().getModel();
			if (ulModel.getRowCount() > 0) {
				for (int i = 0; i < ulModel.getRowCount(); i++) {
					FrostUploadItemObject ulItem =
						(FrostUploadItemObject) ulModel.getRow(i);
					if (ulItem.getState()
						== FrostUploadItemObject.STATE_REQUESTED
						&& ulItem.getSHA1() != null
						&& ulItem.getKey() != null)
						// file have key after encoding
						{
						ulItem.setState(FrostUploadItemObject.STATE_UPLOADING);
						ulModel.updateRow(ulItem);
						insertThread newInsert =
							new insertThread(
								ulItem,
								frostSettings,
								insertThread.MODE_UPLOAD);
						newInsert.start();
						break; // start only 1 thread per loop (=second)
					}
				}
			}
		}

		//////////////////////////////////////////////////
		// Start download thread
		//////////////////////////////////////////////////
		int activeDthreads = 0;
		synchronized (threadCountLock) {
			activeDthreads = activeDownloadThreads;
		}
		if (counter % 3 == 0
			&& // check all 3 seconds if a download could be started
		activeDthreads
				< frostSettings.getIntValue("downloadThreads")
			&& getDownloadPanel().isDownloadingActivated()) {
			// choose first item
			FrostDownloadItemObject dlItem = selectNextDownloadItem();
			if (dlItem != null) {
				DownloadTableModel dlModel =
					(DownloadTableModel) getDownloadTable().getModel();

				dlItem.setState(FrostDownloadItemObject.STATE_TRYING);
				dlModel.updateRow(dlItem);

				requestThread newRequest =
					new requestThread(dlItem, getDownloadTable());
				newRequest.start();
			}
		}
	}

	/**
	 * Chooses the next FrostBoard to update (automatic update).
	 * First sorts by lastUpdateStarted time, then chooses first board
	 * that is allowed to update.
	 * Used only for automatic updating.
	 * Returns NULL if no board to update is found.
	 */
	public FrostBoardObject selectNextBoard(Vector boards) {
		Collections.sort(boards, lastUpdateStartMillisCmp);
		// now first board in list should be the one with latest update of all
		FrostBoardObject board;
		FrostBoardObject nextBoard = null;

		long curTime = System.currentTimeMillis();
		// get in minutes
		int minUpdateInterval =
			frostSettings.getIntValue(
				"automaticUpdate.boardsMinimumUpdateInterval");
		// min -> ms
		long minUpdateIntervalMillis = minUpdateInterval * 60 * 1000;

		for (int i = 0; i < boards.size(); i++) {
			board = (FrostBoardObject) boards.get(i);
			if (nextBoard == null
				&& doUpdate(board)
				&& (curTime - minUpdateIntervalMillis)
					> board.getLastUpdateStartMillis()
				&& // minInterval
			 (
					(board.isConfigured() && board.getAutoUpdateEnabled())
						|| !board.isConfigured())) {
				nextBoard = board;
				break;
			}
		}
		if (nextBoard != null) {
			Core.getOut().println(
				"*** Automatic board update started for: "
					+ nextBoard.toString());
		} else {
			Core.getOut().println(
				"*** Automatic board update - min update interval not reached.  waiting...");
		}
		return nextBoard;
	}

	/**
	 * Chooses next download item to start from download table.
	 */
	protected FrostDownloadItemObject selectNextDownloadItem() {
		DownloadTableModel dlModel =
			(DownloadTableModel) getDownloadTable().getModel();

		// get the item with state "Waiting", minimum htl and not over maximum htl
		ArrayList waitingItems = new ArrayList();
		for (int i = 0; i < dlModel.getRowCount(); i++) {
			FrostDownloadItemObject dlItem =
				(FrostDownloadItemObject) dlModel.getRow(i);
			if ((dlItem.getState() == FrostDownloadItemObject.STATE_WAITING
				&& (dlItem.getEnableDownload() == null
					|| dlItem.getEnableDownload().booleanValue()
						== true) //                && dlItem.getRetries() <= frame1.frostSettings.getIntValue("downloadMaxRetries")
			)
				|| ((dlItem.getState() == FrostDownloadItemObject.STATE_REQUESTED
					|| dlItem.getState()
						== FrostDownloadItemObject.STATE_REQUESTING)
					&& dlItem.getKey() != null
					&& (dlItem.getEnableDownload() == null
						|| dlItem.getEnableDownload().booleanValue() == true))) {
				// check if waittime is expired
				long waittimeMillis =
					frostSettings.getIntValue("downloadWaittime") * 60 * 1000;
				// min->millisec
				if (frostSettings
					.getBoolValue("downloadRestartFailedDownloads")
					&& (System.currentTimeMillis()
						- dlItem.getLastDownloadStopTimeMillis())
						> waittimeMillis) {
					waitingItems.add(dlItem);
				}
			}
		}
		if (waitingItems.size() == 0)
			return null;

		if (waitingItems.size() > 1) // performance issues
			{
			Collections.sort(waitingItems, downloadDlStopMillisCmp);
		}
		return (FrostDownloadItemObject) waitingItems.get(0);
	}

	/**
	 * Used to sort FrostDownloadItemObjects by lastUpdateStartTimeMillis ascending.
	 */
	static final Comparator downloadDlStopMillisCmp = new Comparator() {
		public int compare(Object o1, Object o2) {
			FrostDownloadItemObject value1 = (FrostDownloadItemObject) o1;
			FrostDownloadItemObject value2 = (FrostDownloadItemObject) o2;
			if (value1.getLastDownloadStopTimeMillis()
				> value2.getLastDownloadStopTimeMillis())
				return 1;
			else if (
				value1.getLastDownloadStopTimeMillis()
					< value2.getLastDownloadStopTimeMillis())
				return -1;
			else
				return 0;
		}
	};

	/**
	 * Used to sort FrostBoardObjects by lastUpdateStartMillis ascending.
	 */
	static final Comparator lastUpdateStartMillisCmp = new Comparator() {
		public int compare(Object o1, Object o2) {
			FrostBoardObject value1 = (FrostBoardObject) o1;
			FrostBoardObject value2 = (FrostBoardObject) o2;
			if (value1.getLastUpdateStartMillis()
				> value2.getLastUpdateStartMillis())
				return 1;
			else if (
				value1.getLastUpdateStartMillis()
					< value2.getLastUpdateStartMillis())
				return -1;
			else
				return 0;
		}
	};

	/**tofNewMessageButton Action Listener (tof/ New Message)*/
	private void tofNewMessageButton_actionPerformed(ActionEvent e) {
		/*
				if (frostSettings.getBoolValue("useAltEdit")) {
					// TODO: pass FrostBoardObject
						altEdit = new AltEdit(getSelectedNode(), subject, // subject
				"", // new msg
			frostSettings, this);
					altEdit.start();
				} else {*/
		MessageFrame newMessage =
			new MessageFrame(
				getSelectedNode(),
				frostSettings.getValue("userName"),
				"No subject",
			// subject
		"", // content empty for new msg 
	frostSettings, this, languageResource.getResourceBundle());
		//}
	}

	/**tofReplyButton Action Listener (tof/Reply)*/
	private void tofReplyButton_actionPerformed(ActionEvent e) {
		String subject = lastSelectedMessage;
		if (subject.startsWith("Re:") == false)
			subject = "Re: " + subject;
		/*
				if (frostSettings.getBoolValue("useAltEdit")) {
						altEdit = new AltEdit(getSelectedNode(), subject, // subject
			getTofTextAreaText(), frostSettings, this);
					altEdit.start();
				} else {*/
		MessageFrame newMessage =
			new MessageFrame(
				getSelectedNode(),
				frostSettings.getValue("userName"),
				subject,
				getTofTextAreaText(),
				frostSettings,
				this,
				languageResource.getResourceBundle());
		//		}
	}

	private void tofDisplayBoardInfoMenuItem_actionPerformed(ActionEvent e) {
		if (BoardInfoFrame.isDialogShowing() == false) {
			BoardInfoFrame boardInfo =
				new BoardInfoFrame(this, languageResource.getResourceBundle());
			boardInfo.startDialog();
		}
	}

	private void tofDisplayKnownBoardsMenuItem_actionPerformed(ActionEvent e) {
		KnownBoardsFrame knownBoards =
			new KnownBoardsFrame(this, languageResource.getResourceBundle());
		knownBoards.startDialog();
	}

	/**File | Exit action performed*/
	private void fileExitMenuItem_actionPerformed(ActionEvent e) {
		// Remove the tray icon
		// - not needed any longer, JSysTray unloads itself via ShutdownHook
		/*    try {
		        Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayKill.exe");
		    }catch(IOException _IoExc) { }*/

		System.exit(0);
	}

	/**News | Configure Board action performed*/
	private void tofConfigureBoardMenuItem_actionPerformed(FrostBoardObject board) {
		if (board == null || board.isFolder())
			return;

		BoardSettingsFrame newFrame = new BoardSettingsFrame(this, board);
		if (newFrame.runDialog() == true) // OK pressed?
			{
			updateTofTree(board);
			// update the new msg. count for board
			TOF.initialSearchNewMessages(board);

			if (board == getSelectedNode()) {
				// reload all messages if board is shown
				tofTree_actionPerformed(null);
			}
		}
	}

	/**Options | Preferences action performed*/
	private void optionsPreferencesMenuItem_actionPerformed(ActionEvent e) {
		saveSettings();
		OptionsFrame optionsDlg =
			new OptionsFrame(this, languageResource.getResourceBundle());
		boolean okPressed = optionsDlg.runDialog();
		if (okPressed) {
			// read new settings
			frostSettings.readSettingsFile();

			// check if signed only+hideCheck+hideBad or blocking words settings changed
			if (optionsDlg.shouldReloadMessages()) {
				// update the new msg. count for all boards
				TOF.initialSearchNewMessages();
				// reload all messages
				tofTree_actionPerformed(null);
			}

			updateTofTree();
			// redraw whole tree, in case the update visualization was enabled or disabled (or others)

			// check if we switched from disableRequests=true to =false (requests now enabled)
			if (optionsDlg.shouldRemoveDummyReqFiles()) {
				new RemoveDummyRequestFiles().start();
			}

			// update gui parts
			updateOptionsAffectedComponents();
		}
	}

	/**
	 * Search through .req files of this day in all boards and remove the
	 * dummy .req files that are created by requestThread on key collosions.
	 */
	private class RemoveDummyRequestFiles extends Thread {
		public void run() {
			Iterator i = getTofTree().getAllBoards().iterator();

			while (i.hasNext()) {
				FrostBoardObject board = (FrostBoardObject) i.next();

				String destination =
					new StringBuffer()
						.append(frame1.keypool)
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
							&& FileAccess.readFileRaw(entry).indexOf(
								requestThread.KEYCOLL_INDICATOR)
								> -1) {
							entry.delete();
						}
					}
				}
			}
		}
	}

	/**Help | About action performed*/
	private void helpAboutMenuItem_actionPerformed(ActionEvent e) {
		AboutBox dlg = new AboutBox(this);
		dlg.setModal(true);
		dlg.show();
	}

	/**Overridden so we can exit when window is closed*/
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			fileExitMenuItem_actionPerformed(null);
		}
	}

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() != 2)
				maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() == false) {
				return;
			} else if (
				e.getComponent().equals(tofTextArea)) { // TOF text popup
				showTofTextAreaPopupMenu(e);
			} else if (e.getComponent().equals(boardTable)) {
				// Board attached popup
				showAttachmentBoardPopupMenu(e);
			} else if (e.getComponent().equals(attachmentTable)) {
				// Board attached popup
				showAttachmentTablePopupMenu(e);
			} else if (
				e.getComponent().equals(getTofTree())) { // TOF tree popup
				showTofTreePopupMenu(e);
			} else if (
				e.getComponent().equals(messageTable)) { // TOF tree popup
				showMessageTablePopupMenu(e);
			}
		}
	} // end of class popuplistener

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		//Core.getOut().println("Clipboard contents replaced");
	}

	/**
	 * Method that update the Msg and New counts for tof table
	 * Expects that the boards messages are shown in table
	 */
	public void updateMessageCountLabels(FrostBoardObject board) {
		if (board.isFolder() == true) {
			allMessagesCountLabel.setText("");
			newMessagesCountLabel.setText("");
		} else {
			DefaultTableModel model =
				(DefaultTableModel) messageTable.getModel();

			int allMessages = model.getRowCount();
			allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

			int newMessages = board.getNewMessageCount();
			newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
		}
	}

	/**
	 * Updates the download items count label. The label shows all WAITING items in download table.
	 * Called periodically by timer_actionPerformed().
	 */
	public void updateDownloadCountLabel() {
		if (frostSettings.getBoolValue("disableDownloads") == true)
			return;

		DownloadTableModel model =
			(DownloadTableModel) getDownloadTable().getModel();
		int waitingItems = 0;
		for (int x = 0; x < model.getRowCount(); x++) {
			FrostDownloadItemObject dlItem =
				(FrostDownloadItemObject) model.getRow(x);
			if (dlItem.getState() == FrostDownloadItemObject.STATE_WAITING) {
				waitingItems++;
			}
		}
		getDownloadPanel().setDownloadItemCount(waitingItems);
	}

	private void trustButton_actionPerformed(ActionEvent e) {
		if (selectedMessage != null) {
			if (getEnemies().containsKey(selectedMessage.getFrom())) {
				if (JOptionPane
					.showConfirmDialog(
						getInstance(),
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
				core.startTruster(true, selectedMessage);
			}
		}
		trustButton.setEnabled(false);
		notTrustButton.setEnabled(false);
		checkTrustButton.setEnabled(false);
	}

	private void notTrustButton_actionPerformed(ActionEvent e) {
		if (selectedMessage != null) {
			if (getFriends().containsKey(selectedMessage.getFrom())) {
				if (JOptionPane
					.showConfirmDialog(
						getInstance(),
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
				core.startTruster(false, selectedMessage);
			}
		}
		trustButton.setEnabled(false);
		notTrustButton.setEnabled(false);
		checkTrustButton.setEnabled(false);
	}

	private void checkTrustButton_actionPerformed(ActionEvent e) {
		trustButton.setEnabled(false);
		notTrustButton.setEnabled(false);
		checkTrustButton.setEnabled(false);
		if (selectedMessage != null) {
			core.startTruster(selectedMessage);
		}
	}

	/**
	 * Called after the OptionsFrame changed some settings to reflect
	 * the new settings in the GUI.
	 *
	 * E.g. if downloads are disabled, it removes the tabbed panes
	 * 'Search' and 'Downloads'
	 */
	protected void updateOptionsAffectedComponents() {
		if (frostSettings.getBoolValue("disableDownloads") == false) {
			// search + downloads enabled
			tabbedPane.setEnabledAt(
				tabbedPane.indexOfTab(languageResource.getString("Search")),
				true);
			tabbedPane.setEnabledAt(
				tabbedPane.indexOfTab(languageResource.getString("Downloads")),
				true);
		} else {
			// search + downloads disabled
			tabbedPane.setEnabledAt(
				tabbedPane.indexOfTab(languageResource.getString("Search")),
				false);
			tabbedPane.setEnabledAt(
				tabbedPane.indexOfTab(languageResource.getString("Downloads")),
				false);
		}

		if (frostSettings.getBoolValue("disableRequests") == false) {
			// uploads enabled
			tabbedPane.setEnabledAt(
				tabbedPane.indexOfTab(languageResource.getString("Uploads")),
				true);
		} else {
			// uploads disabled
			tabbedPane.setEnabledAt(
				tabbedPane.indexOfTab(languageResource.getString("Uploads")),
				false);
		}
	}

	protected void showTofTreePopupMenu(MouseEvent e) {
		getPopupMenuTofTree().show(
			e.getComponent(),
			e.getX(),
			e.getY());
	}

	protected void showAttachmentTablePopupMenu(MouseEvent e) {
		getPopupMenuAttachmentTable().show(
			e.getComponent(),
			e.getX(),
			e.getY());
	}

	protected void showAttachmentBoardPopupMenu(MouseEvent e) {
		getPopupMenuAttachmentBoard().show(
			e.getComponent(),
			e.getX(),
			e.getY());
	}

	protected void showTofTextAreaPopupMenu(MouseEvent e) {
		getPopupMenuTofText().show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showMessageTablePopupMenu(MouseEvent e) {
		getPopupMenuMessageTable().show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Marks current selected message unread
	 */
	private void markSelectedMessageUnread() {
		int row = messageTable.getSelectedRow();
		if (row < 0
			|| selectedMessage == null
			|| getSelectedNode() == null
			|| getSelectedNode().isFolder() == true)
			return;

		FrostMessageObject targetMessage = selectedMessage;
        
        messageTable.removeRowSelectionInterval(
            0,
            messageTable.getRowCount() - 1);
        
        targetMessage.setMessageNew(true);
        // let renderer check for new state
        MessageTableModel model = (MessageTableModel)getMessageTable().getModel();
        model.updateRow(targetMessage);    

		getSelectedNode().incNewMessageCount();
        
        updateMessageCountLabels(getSelectedNode());
        updateTofTree(getSelectedNode());
	}

	/**
	 * @return
	 */
	public static Hashtable getBadIds() {
		return Core.getBadIds();
	}

	/**
	 * @return
	 */
	public static crypt getCrypto() {
		return Core.getCrypto();
	}

	/**
	 * @return
	 */
	public static BuddyList getEnemies() {
		return Core.getEnemies();
	}

	/**
	 * @return
	 */
	public static BuddyList getFriends() {
		return Core.getFriends();
	}

	/**
	 * @return
	 */
	public static Hashtable getGoodIds() {
		return Core.getGoodIds();
	}

	/**
	 * @return
	 */
	public static LocalIdentity getMyId() {
		return Core.getMyId();
	}

	/**
	 * @return
	 */
	private PopupMenuTofText getPopupMenuTofText() {
		if (popupMenuTofText == null) {
			popupMenuTofText = new PopupMenuTofText();
			languageResource.addLanguageListener(popupMenuTofText);
		}
		return popupMenuTofText;
	}
	
	/**
	 * @return
	 */
	private PopupMenuTofTree getPopupMenuTofTree() {
		if (popupMenuTofTree == null) {
			popupMenuTofTree = new PopupMenuTofTree();
			languageResource.addLanguageListener(popupMenuTofTree);
		}
		return popupMenuTofTree;
	}

	/**
	 * @return
	 */
	private PopupMenuAttachmentBoard getPopupMenuAttachmentBoard() {
		if (popupMenuAttachmentBoard == null) {
			popupMenuAttachmentBoard = new PopupMenuAttachmentBoard();
			languageResource.addLanguageListener(popupMenuAttachmentBoard);
		}
		return popupMenuAttachmentBoard;
	}

	/**
	 * @return
	 */
	private PopupMenuMessageTable getPopupMenuMessageTable() {
		if (popupMenuMessageTable == null) {
			popupMenuMessageTable = new PopupMenuMessageTable();
			languageResource.addLanguageListener(popupMenuMessageTable);
		}
		return popupMenuMessageTable;
	}

	/**
	 * @return
	 */
	private PopupMenuAttachmentTable getPopupMenuAttachmentTable() {
		if (popupMenuAttachmentTable == null) {
			popupMenuAttachmentTable = new PopupMenuAttachmentTable();
			languageResource.addLanguageListener(popupMenuAttachmentTable);
		}
		return popupMenuAttachmentTable;
	}

	private void setMessageTrust(Boolean what) {
		int row = messageTable.getSelectedRow();
		if (row < 0 || selectedMessage == null)
			return;

		String status = selectedMessage.getStatus();

		if (status.indexOf(VerifyableMessageObject.PENDING) > -1) {
			Identity owner = Core.getNeutral().Get(selectedMessage.getFrom());
			if (owner == null) {
				Core.getOut().println(
					"message was CHECK but not found in Neutral list");
				return;
			}
		}

		if (status.indexOf(VerifyableMessageObject.FAILED) > -1) {
			Identity owner = Core.getEnemies().Get(selectedMessage.getFrom());
			if (owner == null) {
				Core.getOut().println(
					"message was BAD but not found in BAD list");
				return;
			}

		}

		if (status.indexOf(VerifyableMessageObject.VERIFIED) > -1) {
			Identity owner = Core.getFriends().Get(selectedMessage.getFrom());
			if (owner == null) {
				Core.getOut().println(
					"message was GOOD but not found in GOOD list");
				return;
			}
		}

		Truster truster =
			new Truster(Core.getInstance(), what, selectedMessage.getFrom());
		truster.start();
	}

	/**
	 * @return
	 */
	private DownloadPanel getDownloadPanel() {
		if (downloadPanel == null) {
			downloadPanel = new DownloadPanel();
			downloadPanel.setDownloadTable(getDownloadTable());
			downloadPanel.setHealingTable(getHealingTable());
			downloadPanel.setLanguageResource(languageResource);
			downloadPanel.setSettingsClass(frostSettings);
			downloadPanel.initialize();
		}
		return downloadPanel;
	}

}
