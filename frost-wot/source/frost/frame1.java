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
//             - each identity have 3 states: GOOD, BAD, NEUTRAL
//             - filter out enemies on read of messages

// after removing a board, let actual board selected (currently if you delete another than selected board
//   the tofTree is updated)

public class frame1 extends JFrame implements ClipboardOwner {
	/**
	 * 
	 */
	private class PopupMenuMessageTable extends JPopupMenu implements ActionListener, LanguageListener {
		
		JMenuItem markAllMessagesReadItem = new JMenuItem();
		JMenuItem markMessageUnreadItem = new JMenuItem();
		JMenuItem setGoodItem = new JMenuItem();
		JMenuItem setBadItem = new JMenuItem();
		JMenuItem setCheckItem = new JMenuItem();
		JMenuItem cancelItem = new JMenuItem();

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
			markMessageUnreadItem.setText(languageResource.getString("Mark message unread"));
			markAllMessagesReadItem.setText(languageResource.getString("Mark ALL messages read"));
			setGoodItem.setText(languageResource.getString("help user (sets to GOOD)"));
			setBadItem.setText(languageResource.getString("block user (sets to BAD)"));
			setCheckItem.setText(languageResource.getString("set to neutral (CHECK)"));
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
				if (messageTable.getSelectedRow() > -1) { //fscking html on all these..
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
						selectedMessage.getStatus().indexOf(VerifyableMessageObject.FAILED) > -1) {
						setGoodItem.setEnabled(true);
						setCheckItem.setEnabled(true);
					} else
						Core.getOut().println(
							"invalid message state : " + selectedMessage.getStatus());
				}
				
				addSeparator();
				add(cancelItem);
				// ATT: misuse of another menuitem displaying 'Cancel' ;)
				super.show(invoker, x, y);
			}
		}

	}
	
	private PopupMenuMessageTable popupMenuMessageTable = null;
	
	/**
	 * 
	 */
	private class PopupMenuAttachmentBoard extends JPopupMenu implements ActionListener, LanguageListener {
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
		JMenuItem cancelItem = new JMenuItem();
		JMenuItem saveBoardItem = new JMenuItem();
		JMenuItem saveBoardsItem = new JMenuItem();

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
	
	private PopupMenuAttachmentBoard popupMenuAttachmentBoard = null;
	
	/**
	 * 
	 */
	private class PopupMenuAttachmentTable extends JPopupMenu implements ActionListener, LanguageListener {
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

		JMenuItem cancelItem = new JMenuItem();
		JMenuItem saveAttachmentItem = new JMenuItem();
		JMenuItem saveAttachmentsItem = new JMenuItem();

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
			saveAttachmentsItem.setText(languageResource.getString("Download attachment(s)"));
			saveAttachmentItem.setText(languageResource.getString("Download selected attachment"));
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
	
	private PopupMenuAttachmentTable popupMenuAttachmentTable = null;
	
	/**
	 * 
	 */
	private class PopupMenuTofText extends JPopupMenu implements ActionListener, LanguageListener {

		JMenuItem cancelItem = new JMenuItem();
		JMenuItem saveMessageItem = new JMenuItem();

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
			saveMessageItem.setText(languageResource.getString("Save message to disk"));
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
			if ((selectedMessage != null) && (selectedMessage.getContent() != null)) {
				super.show(invoker, x, y);
			}
		}

	}
	
	private PopupMenuTofText popupMenuTofText = null;
	
	/**
	 * 
	 */
	private class PopupMenuSearch extends JPopupMenu implements ActionListener, LanguageListener {
		
		JMenuItem cancelItem = new JMenuItem();
		JMenuItem downloadAllKeysItem = new JMenuItem();
		JMenuItem downloadSelectedKeysItem = new JMenuItem();
		JMenuItem setBadItem = new JMenuItem();
		JMenuItem setGoodItem = new JMenuItem();

		/**
		 * 
		 */
		public PopupMenuSearch() {
			super();
			initialize();
		}
		
		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			downloadSelectedKeysItem.addActionListener(this);
			downloadAllKeysItem.addActionListener(this);
			setGoodItem.addActionListener(this);
			setBadItem.addActionListener(this);

			/*		copyAttachmentItem.addActionListener(new ActionListener() {
						 public void actionPerformed(ActionEvent e) {
							 String srcData =
								 getSearchTable()
									 .getSelectedSearchItemsAsAttachmentsString();
							 Clipboard clipboard = getToolkit().getSystemClipboard();
							 StringSelection contents = new StringSelection(srcData);
							 clipboard.setContents(contents, frame1.this);
						 }
					 });
			*/
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			downloadSelectedKeysItem.setText(languageResource.getString("Download selected keys"));
			downloadAllKeysItem.setText(languageResource.getString("Download all keys"));
			setGoodItem.setText(languageResource.getString("help user (sets to GOOD)"));
			setBadItem.setText(languageResource.getString("block user (sets to BAD)"));
			cancelItem.setText(languageResource.getString("Cancel"));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == downloadSelectedKeysItem) {
				downloadSelectedKeys();
			}
			if (e.getSource() == downloadAllKeysItem) {
				downloadAllKeys();
			}
			if (e.getSource() == setGoodItem) {
				setGood();
			}
			if (e.getSource() == setBadItem) {
				setBad();
			}
		}

		/**
		 * 
		 */
		private void setBad() {
			java.util.List l = getSearchTable().getSelectedItemsOwners();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Identity owner_id = (Identity) i.next();

				Truster truster =
					new Truster(Core.getInstance(), new Boolean(false), owner_id.getUniqueName());
				truster.start();
			}
		}

		/**
		 * 
		 */
		private void setGood() {
			java.util.List l = getSearchTable().getSelectedItemsOwners();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Identity owner_id = (Identity) i.next();

				Truster truster =
					new Truster(Core.getInstance(), new Boolean(true), owner_id.getUniqueName());
				truster.start();
			}
		}

		/**
		 * 
		 */
		private void downloadAllKeys() {
			searchTable.selectAll();
			getSearchTable().addSelectedSearchItemsToDownloadTable(getDownloadTable());
		}

		/**
		 * 
		 */
		private void downloadSelectedKeys() {
			getSearchTable().addSelectedSearchItemsToDownloadTable(getDownloadTable());
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

			if (searchTable.getSelectedRow() > -1) {
				add(downloadSelectedKeysItem);
				addSeparator();
			}
			add(downloadAllKeysItem);
			addSeparator();
			if (searchTable.getSelectedRow() > -1) {
				//	add(copyAttachmentItem);
				//	addSeparator();
				add(setGoodItem);
				add(setBadItem);
				addSeparator();
			}
			add(cancelItem);

			super.show(invoker, x, y);
		}

	}
	
	private PopupMenuSearch popupMenuSearch = null;
	
	/**
	 * 
	 */
	private class PopupMenuDownload extends JPopupMenu implements ActionListener, LanguageListener {
		
		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem copyChkKeyAndFilenameToClipboardItem = new JMenuItem();
		private JMenuItem copyChkKeyToClipboardItem = new JMenuItem();
		private JMenuItem disableAllDownloadsItem = new JMenuItem();
		private JMenuItem disableSelectedDownloadsItem = new JMenuItem();
		private JMenuItem enableAllDownloadsItem = new JMenuItem();
		private JMenuItem enableSelectedDownloadsItem = new JMenuItem();
		private JMenuItem invertEnabledAllItem = new JMenuItem();
		private JMenuItem invertEnabledSelectedItem = new JMenuItem();
		private JMenuItem removeAllDownloadsItem = new JMenuItem();
		private JMenuItem removeFinishedItem = new JMenuItem();
		private JMenuItem removeSelectedDownloadsItem = new JMenuItem();
		private JMenuItem restartSelectedDownloadsItem = new JMenuItem();
		
		private JMenu copyToClipboardMenu = new JMenu();

		/**
		 * 
		 */
		public PopupMenuDownload() {
			super();
			initialize();
		}

		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			// TODO: implement cancel of downloading

			copyToClipboardMenu.add(copyChkKeyToClipboardItem);
			copyToClipboardMenu.add(copyChkKeyAndFilenameToClipboardItem);

			copyChkKeyToClipboardItem.addActionListener(this);
			copyChkKeyAndFilenameToClipboardItem.addActionListener(this);
			restartSelectedDownloadsItem.addActionListener(this);
			removeSelectedDownloadsItem.addActionListener(this);
			removeAllDownloadsItem.addActionListener(this);
			removeFinishedItem.addActionListener(this);
			enableAllDownloadsItem.addActionListener(this);
			disableAllDownloadsItem.addActionListener(this);
			enableSelectedDownloadsItem.addActionListener(this);
			disableSelectedDownloadsItem.addActionListener(this);
			invertEnabledAllItem.addActionListener(this);
			invertEnabledSelectedItem.addActionListener(this);
		}
		
		private void refreshLanguage() {
			restartSelectedDownloadsItem.setText(languageResource.getString("Restart selected downloads"));
			removeSelectedDownloadsItem.setText(languageResource.getString("Remove selected downloads"));
			removeAllDownloadsItem.setText(languageResource.getString("Remove all downloads"));
			//downloadPopupResetHtlValues = new JMenuItem(LangRes.getString("Retry selected downloads"));
			removeFinishedItem.setText(languageResource.getString("Remove finished downloads"));
			enableAllDownloadsItem.setText(languageResource.getString("Enable all downloads"));
			disableAllDownloadsItem.setText(languageResource.getString("Disable all downloads"));
			enableSelectedDownloadsItem.setText(languageResource.getString("Enable selected downloads"));
			disableSelectedDownloadsItem.setText(languageResource.getString("Disable selected downloads"));
			invertEnabledAllItem.setText(languageResource.getString("Invert enabled state for all downloads"));
			invertEnabledSelectedItem.setText(languageResource.getString("Invert enabled state for selected downloads"));
			cancelItem.setText(languageResource.getString("Cancel"));
			copyChkKeyToClipboardItem.setText(languageResource.getString("CHK key"));
			copyChkKeyAndFilenameToClipboardItem.setText(languageResource.getString("CHK key + filename"));
			
			copyToClipboardMenu.setText(languageResource.getString("Copy to clipboard") + "...");
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == copyChkKeyToClipboardItem) {
				copyChkKeyToClipboard();
			}
			if (e.getSource() == copyChkKeyAndFilenameToClipboardItem) {
				copyChkKeyAndFilenameToClipboard();
			}
			if (e.getSource() == restartSelectedDownloadsItem) {
				restartSelectedDownloads();
			}
			if (e.getSource() == removeSelectedDownloadsItem) {
				removeSelectedDownloads();
			}
			if (e.getSource() == removeAllDownloadsItem) {
				removeAllDownloads();
			}
			if (e.getSource() == removeFinishedItem) {
				removeFinished();
			}
			if (e.getSource() == enableAllDownloadsItem) {
				enableAllDownloads();
			}
			if (e.getSource() == disableAllDownloadsItem) {
				disableAllDownloads();
			}
			if (e.getSource() == enableSelectedDownloadsItem) {
				enableSelectedDownloads();
			}
			if (e.getSource() == disableSelectedDownloadsItem) {
				disableSelectedDownloads();
			}
			if (e.getSource() == invertEnabledAllItem) {
				invertEnabledAll();
			}
			if (e.getSource() == invertEnabledSelectedItem) {
				invertEnabledSelected();
			}
		}

		/**
		 * 
		 */
		private void invertEnabledSelected() {
			getDownloadTable().setDownloadEnabled(2, false);
			// 2=invert , false means SELECTED in table!
		}

		/**
		 * 
		 */
		private void invertEnabledAll() {
			getDownloadTable().setDownloadEnabled(2, true);
			// 2=invert , true means ALL in table!
		}

		/**
		 * 
		 */
		private void disableSelectedDownloads() {
			getDownloadTable().setDownloadEnabled(0, false);
			// 0=disabled , false means SELECTED in table!
		}

		/**
		 * 
		 */
		private void enableSelectedDownloads() {
			getDownloadTable().setDownloadEnabled(1, false);
			// 1=enabled , false means SELECTED in table!
		}

		/**
		 * 
		 */
		private void disableAllDownloads() {
			getDownloadTable().setDownloadEnabled(0, true);
			// 0=disabled , true means ALL in table!
		}

		/**
		 * 
		 */
		private void enableAllDownloads() {
			getDownloadTable().setDownloadEnabled(1, true);
			// 1=enabled , true means ALL in table!	
		}

		/**
		 * 
		 */
		private void removeFinished() {
			getDownloadTable().removeFinishedDownloads();
		}

		/**
		 * 
		 */
		private void removeAllDownloads() {
			getDownloadTable().removeAllItemsFromTable();	
		}

		/**
		 * 
		 */
		private void removeSelectedDownloads() {
			getDownloadTable().removeSelectedItemsFromTable();	
		}

		/**
		 * 
		 */
		private void restartSelectedDownloads() {
			getDownloadTable().restartSelectedDownloads();
		}

		/**
		 * add CHK key + filename to clipboard 
		 */
		private void copyChkKeyAndFilenameToClipboard() {
			DownloadTableModel tableModel = (DownloadTableModel) getDownloadTable().getModel();
			int selectedRow = getDownloadTable().getSelectedRow();
			if (selectedRow > -1) {
				FrostDownloadItemObject dlItem =
					(FrostDownloadItemObject) tableModel.getRow(selectedRow);
				String chkKey = dlItem.getKey();
				String filename = dlItem.getFileName();
				if (chkKey != null && filename != null) {
					mixed.setSystemClipboard(chkKey + "/" + filename);
				}
			}
		}

		/**
		 * add CHK key to clipboard
		 */
		private void copyChkKeyToClipboard() {
			DownloadTableModel tableModel = (DownloadTableModel) getDownloadTable().getModel();
			int selectedRow = getDownloadTable().getSelectedRow();
			if (selectedRow > -1) {
				FrostDownloadItemObject dlItem =
					(FrostDownloadItemObject) tableModel.getRow(selectedRow);
				String chkKey = dlItem.getKey();
				if (chkKey != null) {
					mixed.setSystemClipboard(chkKey);
				}
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

			if (getDownloadTable().getSelectedRowCount() == 1) {
				// if 1 item is selected
				FrostDownloadItemObject dlItem =
					(FrostDownloadItemObject)
						((DownloadTableModel) getDownloadTable().getModel()).getRow(
						getDownloadTable().getSelectedRow());
				if (dlItem.getKey() != null) {
					add(copyToClipboardMenu);
					addSeparator();
				}
			}

			if (getDownloadTable().getSelectedRow() > -1) {
				add(restartSelectedDownloadsItem);
				addSeparator();
			}

			JMenu enabledSubMenu =
				new JMenu(languageResource.getString("Enable downloads") + "...");
			if (getDownloadTable().getSelectedRow() > -1) {
				enabledSubMenu.add(enableSelectedDownloadsItem);
				enabledSubMenu.add(disableSelectedDownloadsItem);
				enabledSubMenu.add(invertEnabledSelectedItem);
				enabledSubMenu.addSeparator();
			}
			enabledSubMenu.add(enableAllDownloadsItem);
			enabledSubMenu.add(disableAllDownloadsItem);
			enabledSubMenu.add(invertEnabledAllItem);
			add(enabledSubMenu);

			JMenu removeSubMenu = new JMenu(languageResource.getString("Remove") + "...");
			if (getDownloadTable().getSelectedRow() > -1) {
				removeSubMenu.add(removeSelectedDownloadsItem);
			}
			removeSubMenu.add(removeAllDownloadsItem);
			add(removeSubMenu);

			addSeparator();
			add(removeFinishedItem);
			addSeparator();
			add(cancelItem);

			super.show(invoker, x, y);
		}

	}
	
	private PopupMenuDownload popupMenuDownload = null;
	
	/**
	 * 
	 */
	private class PopupMenuUpload extends JPopupMenu implements ActionListener, LanguageListener {
		
		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem copyChkKeyAndFilenameToClipboardItem = new JMenuItem();
		private JMenuItem copyChkKeyToClipboardItem = new JMenuItem();
		private JMenuItem generateChkForSelectedFilesItem = new JMenuItem();
		private JMenuItem reloadAllFilesItem = new JMenuItem();
		private JMenuItem reloadSelectedFilesItem = new JMenuItem();
		private JMenuItem removeAllFilesItem = new JMenuItem();
		private JMenuItem removeSelectedFilesItem = new JMenuItem();
		private JMenuItem restoreDefaultFilenamesForAllFilesItem = new JMenuItem();
		private JMenuItem restoreDefaultFilenamesForSelectedFilesItem = new JMenuItem();
		private JMenuItem setPrefixForAllFilesItem = new JMenuItem();
		private JMenuItem setPrefixForSelectedFilesItem = new JMenuItem();
		
		private JMenu changeDestinationBoardMenu = new JMenu();
		private JMenu copyToClipboardMenu = new JMenu();
				
		/**
		 * 
		 */
		public PopupMenuUpload() {
			super();
			initialize();
		}
		
		private void initialize() {
			refreshLanguage();

			copyToClipboardMenu.add(copyChkKeyToClipboardItem);
			copyToClipboardMenu.add(copyChkKeyAndFilenameToClipboardItem);

			copyChkKeyToClipboardItem.addActionListener(this);
			copyChkKeyAndFilenameToClipboardItem.addActionListener(this);
			removeSelectedFilesItem.addActionListener(this);
			removeAllFilesItem.addActionListener(this);
			reloadSelectedFilesItem.addActionListener(this);
			reloadAllFilesItem.addActionListener(this);
			generateChkForSelectedFilesItem.addActionListener(this);
			setPrefixForSelectedFilesItem.addActionListener(this);
			setPrefixForAllFilesItem.addActionListener(this);
			restoreDefaultFilenamesForSelectedFilesItem.addActionListener(this);
			restoreDefaultFilenamesForAllFilesItem.addActionListener(this);
		}
		
		private void refreshLanguage() {
			cancelItem.setText(languageResource.getString("Cancel"));
			copyChkKeyAndFilenameToClipboardItem.setText(languageResource.getString("CHK key + filename"));
			copyChkKeyToClipboardItem.setText(languageResource.getString("CHK key"));
			generateChkForSelectedFilesItem.setText(languageResource.getString("Start encoding of selected files"));
			reloadAllFilesItem.setText(languageResource.getString("Reload all files"));
			reloadSelectedFilesItem.setText(languageResource.getString("Reload selected files"));
			removeAllFilesItem.setText(languageResource.getString("Remove all files"));
			removeSelectedFilesItem.setText(languageResource.getString("Remove selected files"));
			restoreDefaultFilenamesForAllFilesItem.setText(languageResource.getString("Restore default filenames for all files"));
			restoreDefaultFilenamesForSelectedFilesItem.setText(languageResource.getString("Restore default filenames for selected files"));
			setPrefixForAllFilesItem.setText(languageResource.getString("Set prefix for all files"));
			setPrefixForSelectedFilesItem.setText(languageResource.getString("Set prefix for selected files"));
			
			changeDestinationBoardMenu.setText(languageResource.getString("Change destination board"));
			copyToClipboardMenu.setText(languageResource.getString("Copy to clipboard") + "...");
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == copyChkKeyToClipboardItem) {
				copyChkKeyToClipboard();
			}
			if (e.getSource() == copyChkKeyAndFilenameToClipboardItem) {
				copyChkKeyAndFilenameToClipboard();
			}
			if (e.getSource() == removeSelectedFilesItem) {
				removeSelectedFiles();
			}
			if (e.getSource() == removeAllFilesItem) {
				removeAllFiles();
			}
			if (e.getSource() == reloadSelectedFilesItem) {
				reloadSelectedFiles();
			}
			if (e.getSource() == reloadAllFilesItem) {
				reloadAllFiles();
			}
			if (e.getSource() == generateChkForSelectedFilesItem) {
				generateChkForSelectedFiles();
			}
			if (e.getSource() == setPrefixForSelectedFilesItem) {
				setPrefixForSelectedFiles();
			}
			if (e.getSource() == setPrefixForAllFilesItem) {
				setPrefixForAllFiles();
			}
			if (e.getSource() == restoreDefaultFilenamesForSelectedFilesItem) {
				restoreDefaultFilenamesForSelectedFiles();
			}
			if (e.getSource() == restoreDefaultFilenamesForAllFilesItem) {
				restoreDefaultFilenamesForAllFiles();
			}
		}

		/**
		 * Restore default filenames for all files
		 */
		private void restoreDefaultFilenamesForAllFiles() {
			getUploadTable().selectAll();
			getUploadTable().restoreOriginalFilenamesForSelectedRows();
		}

		/**
		 * Restore default filenames for selected files
		 */
		private void restoreDefaultFilenamesForSelectedFiles() {
			getUploadTable().restoreOriginalFilenamesForSelectedRows();
		}

		/**
		 * Set Prefix for all files
		 */
		private void setPrefixForAllFiles() {
			getUploadTable().selectAll();
			getUploadTable().setPrefixForSelectedFiles();
		}

		/**
		 * Set Prefix for selected files
		 */
		private void setPrefixForSelectedFiles() {
			getUploadTable().setPrefixForSelectedFiles();
		}

		/**
		 * Generate CHK for selected files 
		 */
		private void generateChkForSelectedFiles() {
			UploadTableModel tableModel = (UploadTableModel) getUploadTable().getModel();
			int[] selectedRows = getUploadTable().getSelectedRows();
			for (int i = 0; i < selectedRows.length; i++) {
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject) tableModel.getRow(selectedRows[i]);
				// start gen chk only if IDLE
				if (ulItem.getState() == FrostUploadItemObject.STATE_IDLE
					&& ulItem.getKey() == null) {
					ulItem.setState(FrostUploadItemObject.STATE_ENCODING_REQUESTED);
					tableModel.updateRow(ulItem);
				}
			}
		}	/**
		 * Reload all files
		 */
		private void reloadAllFiles() {
			UploadTableModel tableModel = (UploadTableModel) getUploadTable().getModel();
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				FrostUploadItemObject ulItem = (FrostUploadItemObject) tableModel.getRow(i);
				// Since it is difficult to identify the states where we are allowed to
				// start an upload we decide based on the states in which we are not allowed
				if (ulItem.getState() != FrostUploadItemObject.STATE_UPLOADING
					&& ulItem.getState() != FrostUploadItemObject.STATE_PROGRESS
					&& ulItem.getState() != FrostUploadItemObject.STATE_ENCODING) {
					ulItem.setState(FrostUploadItemObject.STATE_REQUESTED);
					tableModel.updateRow(ulItem);
				}
			}
		}

		/**
		 * Reload selected files
		 */
		private void reloadSelectedFiles() {
			UploadTableModel tableModel = (UploadTableModel) getUploadTable().getModel();
			int[] selectedRows = getUploadTable().getSelectedRows();
			for (int i = 0; i < selectedRows.length; i++) {
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject) tableModel.getRow(selectedRows[i]);
				// Since it is difficult to identify the states where we are allowed to
				// start an upload we decide based on the states in which we are not allowed
				if (ulItem.getState() != FrostUploadItemObject.STATE_UPLOADING
					&& ulItem.getState() != FrostUploadItemObject.STATE_PROGRESS
					&& ulItem.getState() != FrostUploadItemObject.STATE_ENCODING) {
					ulItem.setState(FrostUploadItemObject.STATE_REQUESTED);
					tableModel.updateRow(ulItem);
				}
			}
		}

		/**
		 * Remove all files
		 */
		private void removeAllFiles() {
			UploadTableModel model = (UploadTableModel) getUploadTable().getModel();
			model.clearDataModel();
		}

		/**
		 * Remove selected files
		 */
		private void removeSelectedFiles() {
			getUploadTable().removeSelectedRows();
		}

		/**
		 * add CHK key + filename to clipboard 
		 */
		private void copyChkKeyAndFilenameToClipboard() {
			UploadTableModel tableModel = (UploadTableModel) getUploadTable().getModel();
			int selectedRow = getUploadTable().getSelectedRow();
			if (selectedRow > -1) {
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject) tableModel.getRow(selectedRow);
				String chkKey = ulItem.getKey();
				String filename = ulItem.getFileName();
				if (chkKey != null && filename != null) {
					mixed.setSystemClipboard(chkKey + "/" + filename);
				}
			}
		}

		/**
		 * add CHK key to clipboard
		 */
		private void copyChkKeyToClipboard() {
			UploadTableModel tableModel = (UploadTableModel) getUploadTable().getModel();
			int selectedRow = getUploadTable().getSelectedRow();
			if (selectedRow > -1) {
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject) tableModel.getRow(selectedRow);
				String chkKey = ulItem.getKey();
				if (chkKey != null) {
					mixed.setSystemClipboard(chkKey);
				}
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

			if (getUploadTable().getSelectedRowCount() == 1) {
				// if 1 item is selected
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject)
						((UploadTableModel) getUploadTable().getModel()).getRow(
						getUploadTable().getSelectedRow());
				if (ulItem.getKey() != null) {
					add(copyToClipboardMenu);
					addSeparator();
				}
			}

			JMenu removeSubMenu = new JMenu(languageResource.getString("Remove") + "...");
			if (getUploadTable().getSelectedRow() > -1) {
				removeSubMenu.add(removeSelectedFilesItem);
			}
			removeSubMenu.add(removeAllFilesItem);

			add(removeSubMenu);
			addSeparator();
			if (getUploadTable().getSelectedRow() > -1) {
				add(generateChkForSelectedFilesItem);
				add(reloadSelectedFilesItem);
			}
			add(reloadAllFilesItem);
			addSeparator();
			if (getUploadTable().getSelectedRow() > -1) {
				add(setPrefixForSelectedFilesItem);
			}
			add(setPrefixForAllFilesItem);
			addSeparator();
			if (getUploadTable().getSelectedRow() > -1) {
				add(restoreDefaultFilenamesForSelectedFilesItem);
			}
			add(restoreDefaultFilenamesForAllFilesItem);
			addSeparator();
			if (getUploadTable().getSelectedRow() > -1) {
				// Add boards to changeDestinationBoard submenu
				Vector boards = getTofTree().getAllBoards();
				Collections.sort(boards);
				changeDestinationBoardMenu.removeAll();
				for (int i = 0; i < boards.size(); i++) {
					final FrostBoardObject aBoard = (FrostBoardObject) boards.elementAt(i);
					JMenuItem boardMenuItem = new JMenuItem(aBoard.toString());
					changeDestinationBoardMenu.add(boardMenuItem);
					// add all boards to menu + set action listener for each board menu item
					boardMenuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// set new board for all selected rows
							UploadTableModel ulModel =
								(UploadTableModel) getUploadTable().getModel();
							int[] selectedRows = getUploadTable().getSelectedRows();
							for (int x = 0; x < selectedRows.length; x++) {
								FrostUploadItemObject ulItem =
									(FrostUploadItemObject) ulModel.getRow(selectedRows[x]);
								ulItem.setTargetBoard(aBoard);
								ulModel.updateRow(ulItem);
							}
						}
					});
				}
				add(changeDestinationBoardMenu);
			}
			addSeparator();
			add(cancelItem);

			super.show(invoker, x, y);
		}

	}
	
	private PopupMenuUpload popupMenuUpload = null;
	
	
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
		translateMenuEntries();
		translateTabbedPane();
		translateButtons();
		translateCheckBox();
	}

	/**
	 * We really have to use ONE single point to configure all translateable 
	 * objecs. This way we can change the resourceBundle and change
	 * Frost's language on runtime.
	 */
	private void translateCheckBox() {
		searchAllBoardsCheckBox.setText(languageResource.getString("all boards"));
		downloadActivateCheckBox.setToolTipText(languageResource.getString("Activate downloading"));
	}
	private void translateMenuEntries() {
		buildMenuBar();
	}
	private void translateTabbedPane() {
		tabbedPane.setTitleAt(0, languageResource.getString("News"));
		tabbedPane.setTitleAt(1, languageResource.getString("Search"));
		tabbedPane.setTitleAt(2, languageResource.getString("Downloads"));
		tabbedPane.setTitleAt(3, languageResource.getString("Uploads"));
	}
	private void translateButtons() {
		newBoardButton.setToolTipText(languageResource.getString("New board"));
		searchDownloadButton.setToolTipText(
			languageResource.getString("Download selected keys"));
		systemTrayButton.setToolTipText(
			languageResource.getString("Minimize to System Tray"));
		knownBoardsButton.setToolTipText(
			languageResource.getString("Display list of known boards"));
		boardInfoButton.setToolTipText(
			languageResource.getString("Board Information Window"));
		newFolderButton.setToolTipText(languageResource.getString("New folder"));
		pasteBoardButton.setToolTipText(languageResource.getString("Paste board"));
		configBoardButton.setToolTipText(languageResource.getString("Configure board"));
		removeBoardButton.setToolTipText(languageResource.getString("Remove board"));
		cutBoardButton.setToolTipText(languageResource.getString("Cut board"));
		renameBoardButton.setToolTipText(languageResource.getString("Rename folder"));
		tofNewMessageButton.setToolTipText(languageResource.getString("New message"));
		tofReplyButton.setToolTipText(languageResource.getString("Reply"));
		downloadAttachmentsButton.setToolTipText(
			languageResource.getString("Download attachment(s)"));
		downloadBoardsButton.setToolTipText(languageResource.getString("Add Board(s)"));
		saveMessageButton.setToolTipText(languageResource.getString("Save message"));
		trustButton.setToolTipText(languageResource.getString("Trust"));
		notTrustButton.setToolTipText(languageResource.getString("Do not trust"));
		checkTrustButton.setToolTipText(languageResource.getString("Set to CHECK"));
		tofUpdateButton.setToolTipText(languageResource.getString("Update"));
		uploadAddFilesButton.setToolTipText(languageResource.getString("Browse")+"...");
		downloadShowHealingInfo.setToolTipText(
			languageResource.getString("Show healing information"));
		searchButton.setToolTipText(languageResource.getString("Search"));
	}

	/**Save settings*/
	public void saveSettings() {
		frostSettings.setValue(
			"downloadingActivated",
			downloadActivateCheckBox.isSelected());
		//      frostSettings.setValue("uploadingActivated", uploadActivateCheckBox.isSelected());
		frostSettings.setValue(
			"searchAllBoards",
			searchAllBoardsCheckBox.isSelected());
		//      frostSettings.setValue("reducedBlockCheck", reducedBlockCheckCheckBox.isSelected());
		frostSettings.setValue(
			"automaticUpdate",
			tofAutomaticUpdateMenuItem.isSelected());

		// save size and location of window
		Dimension actSize = getSize();
		//        Point actPos = this.getLocationOnScreen();
		frostSettings.setValue(
			"lastFrameHeight",
			"" + (int) actSize.getHeight());
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

	// buttons that are enabled/disabled later
	JButton newBoardButton = null;
	JButton searchDownloadButton = null;
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

	JButton uploadAddFilesButton = null;
	JButton downloadShowHealingInfo = null;
	JButton searchButton = null;

	// labels that are updated later
	JLabel statusLabel = null;
	JLabel statusMessageLabel = null;
	static ImageIcon[] newMessage = new ImageIcon[2];
	JLabel timeLabel = null;

	JCheckBox searchAllBoardsCheckBox = null;
	JCheckBox downloadActivateCheckBox = null;
	JCheckBoxMenuItem tofAutomaticUpdateMenuItem = null;

	JComboBox searchComboBox = null;

	JTabbedPane tabbedPane = null;

	JSplitPane attachmentSplitPane = null;
	JSplitPane boardSplitPane = null;

	final String allMessagesCountPrefix = "Msg: ";
	final String newMessagesCountPrefix = "New: ";
	JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix + "0");
	JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix + "0");

	JLabel downloadItemCountLabel = new JLabel();

	private String searchResultsCountPrefix = null;
	JLabel searchResultsCountLabel = new JLabel();

	TofTree tofTree = null;

	private UploadTable uploadTable = null;
	private MessageTable messageTable = null;
	private SearchTable searchTable = null;
	private DownloadTable downloadTable = null;
	private JTable attachmentTable = null;
	private JTable boardTable = null;
	private HealingTable healingTable = null;

	private JTextArea tofTextArea = null;
	private JTextField downloadTextField = null;
	private JTextField searchTextField = null;
	
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
		return uploadTable;
	}
	public MessageTable getMessageTable() {
		return messageTable;
	}
	public SearchTable getSearchTable() {
		return searchTable;
	}
	public DownloadTable getDownloadTable() {
		return downloadTable;
	}
	public HealingTable getHealingTable() {
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
		return tofTree;
	}
	public JButton getSearchButton() {
		return searchButton;
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

	public FrostBoardObject getSelectedNode() {
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
	public frame1(SettingsClass newSettings, String locale, Splashscreen splashscreen) {
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
	/**
	 * Configures a CheckBox to be a default icon CheckBox
	 * @param checkBox The new icon CheckBox
	 * @param toolTipText Is displayed when the mousepointer is some seconds over the CheckBox
	 * @param rolloverIcon Displayed when mouse is over the CheckBox
	 * @param selectedIcon Displayed when CheckBox is checked
	 * @param rolloverSelectedIcon Displayed when mouse is over the selected CheckBox
	 */
	public void configureCheckBox(
		JCheckBox checkBox,
		String toolTipText,
		String rolloverIcon,
		String selectedIcon,
		String rolloverSelectedIcon) {
		String text = null;
		try {
			text = languageResource.getString(toolTipText);
		} catch (MissingResourceException ex) {
			text = toolTipText; // better than nothing ;)
		}
		checkBox.setToolTipText(text);
		checkBox.setRolloverIcon(
			new ImageIcon(frame1.class.getResource(rolloverIcon)));
		checkBox.setSelectedIcon(
			new ImageIcon(frame1.class.getResource(selectedIcon)));
		checkBox.setRolloverSelectedIcon(
			new ImageIcon(frame1.class.getResource(rolloverSelectedIcon)));
		checkBox.setMargin(new Insets(0, 0, 0, 0));
		checkBox.setFocusPainted(false);
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
				tofConfigureBoardMenuItem_actionPerformed(e, getSelectedNode());
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
		tabbedPane.add(languageResource.getString("Search"), buildSearchPane());
		tabbedPane.add(languageResource.getString("Downloads"), buildDownloadPane());
		tabbedPane.add(languageResource.getString("Uploads"), buildUploadPane());

		updateOptionsAffectedComponents();

		// this rootnode is discarded later, but if we create the tree without parameters,
		// a new Model is created wich contains some sample data by default (swing)
		// this confuses our renderer wich only expects FrostBoardObjects in the tree
		FrostBoardObject dummyRootNode =
			new FrostBoardObject("Frost Message System", true);
		tofTree = new TofTree(dummyRootNode);
		JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
		tofTree.setRootVisible(true);
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
		MessageTableModel messageTableModel = new MessageTableModel();
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
			languageResource.getString("Select a message to view its content."));
	}

	private JPanel buildSearchPane() {
		// create objects for button toolbar
		this.searchAllBoardsCheckBox =
			new JCheckBox(languageResource.getString("all boards"), true);
		searchDownloadButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/save.gif")));
		this.searchButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/search.gif")));

		this.searchTextField = new JTextField(25);
		String[] searchComboBoxItems =
			{
				"All files",
				"Audio",
				"Video",
				"Images",
				"Documents",
				"Executables",
				"Archives" };
		this.searchComboBox = new JComboBox(searchComboBoxItems);
		// configure buttons
		configureButton(
			searchButton,
			languageResource.getString("Search"),
			"/data/search_rollover.gif");
		configureButton(
			searchDownloadButton,
			languageResource.getString("Download selected keys"),
			"/data/save_rollover.gif");
		// build button toolbar panel
		JPanel searchTopPanel = new JPanel();
		BoxLayout dummyLayout = new BoxLayout(searchTopPanel, BoxLayout.X_AXIS);
		searchTopPanel.setLayout(dummyLayout);

		searchTextField.setMaximumSize(searchTextField.getPreferredSize());
		searchTopPanel.add(searchTextField); // Search / text
		searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		searchComboBox.setMaximumSize(searchComboBox.getPreferredSize());
		searchTopPanel.add(searchComboBox);
		searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		searchTopPanel.add(searchButton); // Search / Search button
		searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		searchTopPanel.add(searchDownloadButton);
		// Search / Download selected files
		searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		searchTopPanel.add(searchAllBoardsCheckBox);
		searchTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
		searchTopPanel.add(Box.createHorizontalGlue());

		searchResultsCountPrefix = "   " + languageResource.getString("Results") + ": ";
		JLabel dummyLabel = new JLabel(searchResultsCountPrefix + "00000");
		dummyLabel.doLayout();
		Dimension labelSize = dummyLabel.getPreferredSize();
		searchResultsCountLabel.setPreferredSize(labelSize);
		searchResultsCountLabel.setMinimumSize(labelSize);
		searchResultsCountLabel.setText(searchResultsCountPrefix + "0");
		searchTopPanel.add(searchResultsCountLabel);
		// build table
		SearchTableModel searchTableModel = new SearchTableModel();
		this.searchTable = new SearchTable(searchTableModel);
		JScrollPane searchTableScrollPane = new JScrollPane(searchTable);
		// add action listener
		searchTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTextField_actionPerformed(e);
			}
		});
		searchDownloadButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getSearchTable().addSelectedSearchItemsToDownloadTable(
					getDownloadTable());
			}
		});
		searchButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchButton_actionPerformed(e);
			}
		});
		// build panel
		JPanel searchMainPanel = new JPanel(new BorderLayout());
		searchMainPanel.add(searchTopPanel, BorderLayout.NORTH);
		// Search / Buttons
		searchMainPanel.add(searchTableScrollPane, BorderLayout.CENTER);
		// Search / Results
		return searchMainPanel;
	}

	private JPanel buildDownloadPane() {
		// create objects for buttons toolbar panel
		this.downloadActivateCheckBox =
			new JCheckBox(
				new ImageIcon(frame1.class.getResource("/data/down.gif")),
				true);
		configureCheckBox(
			downloadActivateCheckBox,
			languageResource.getString("Activate downloading"),
			"/data/down_rollover.gif",
			"/data/down_selected.gif",
			"/data/down_selected_rollover.gif");

		this.downloadTextField = new JTextField(25);
		downloadTextField
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadTextField_actionPerformed(e);
			}
		});
		this.downloadShowHealingInfo =
			new JButton(
				new ImageIcon(
					frame1.class.getResource("/data/healinginfo.gif")));
		configureButton(
			this.downloadShowHealingInfo,
			languageResource.getString("Show healing information"),
			"/data/healinginfo_rollover.gif");
		// disabled until implemented ;)
		this.downloadShowHealingInfo.setEnabled(false);

		// create buttons toolbar panel
		JPanel downloadTopPanel = new JPanel();
		BoxLayout dummyLayout =
			new BoxLayout(downloadTopPanel, BoxLayout.X_AXIS);
		downloadTopPanel.setLayout(dummyLayout);
		downloadTextField.setMaximumSize(downloadTextField.getPreferredSize());
		downloadTopPanel.add(downloadTextField); //Download/Quickload
		downloadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		downloadTopPanel.add(downloadActivateCheckBox);
		//Download/Start transfer
		downloadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		downloadTopPanel.add(this.downloadShowHealingInfo);
		downloadTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
		downloadTopPanel.add(Box.createHorizontalGlue());

		String downloadCountPrefix = languageResource.getString("Waiting");
		JLabel dummyLabel = new JLabel(downloadCountPrefix + " : 00000");
		dummyLabel.doLayout();
		Dimension labelSize = dummyLabel.getPreferredSize();
		downloadItemCountLabel.setPreferredSize(labelSize);
		downloadItemCountLabel.setMinimumSize(labelSize);
		downloadItemCountLabel.setText(downloadCountPrefix + " : 0");
		downloadTopPanel.add(downloadItemCountLabel);

		// create downloadTable
		DownloadTableModel downloadTableModel = new DownloadTableModel();
		this.downloadTable = new DownloadTable(downloadTableModel);
		JScrollPane downloadTableScrollPane = new JScrollPane(downloadTable);

		// create healing table, is not contained in downloadPanel, but belongs to here
		HealingTableModel htModel = new HealingTableModel();
		this.healingTable = new HealingTable(htModel);

		//Downloads / KeyEvent
		downloadTable.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
				downloadTable_keyPressed(e);
			}
			public void keyReleased(KeyEvent e) {
			}
		});
		// create the main download panel
		JPanel downloadMainPanel = new JPanel(new BorderLayout());
		downloadMainPanel.add(downloadTopPanel, BorderLayout.NORTH);
		// Download/Buttons
		downloadMainPanel.add(downloadTableScrollPane, BorderLayout.CENTER);
		//Downloadlist
		return downloadMainPanel;
	}

	private JPanel buildUploadPane() {
		// create upload table
		UploadTableModel uploadTableModel = new UploadTableModel();
		this.uploadTable = new UploadTable(uploadTableModel);
		JScrollPane uploadTableScrollPane = new JScrollPane(uploadTable);
		// uploadTable / KeyEvent
		uploadTable.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_DELETE
					&& !uploadTable.isEditing())
					getUploadTable().removeSelectedRows();
			}
			public void keyReleased(KeyEvent e) {
			}
		});
		// create toolbar button
		this.uploadAddFilesButton =
			new JButton(
				new ImageIcon(frame1.class.getResource("/data/browse.gif")));
		configureButton(
			uploadAddFilesButton,
			"Browse...",
			"/data/browse_rollover.gif");
		uploadAddFilesButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadAddFilesButton_actionPerformed(e);
			}
		});
		// add button to top panel
		JPanel uploadTopPanel =
			new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		uploadTopPanel.add(uploadAddFilesButton);
		// add all to main upload panel
		JPanel uploadMainPanel = new JPanel(new BorderLayout());
		uploadMainPanel.add(uploadTopPanel, BorderLayout.NORTH);
		uploadMainPanel.add(uploadTableScrollPane, BorderLayout.CENTER);
		return uploadMainPanel;
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
		getDownloadTable().addMouseListener(popupListener);
		getSearchTable().addMouseListener(popupListener);
		getUploadTable().addMouseListener(popupListener);
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
		searchAllBoardsCheckBox.setSelected(true);
		trustButton.setEnabled(false);
		notTrustButton.setEnabled(false);
		checkTrustButton.setEnabled(false);

		//on with other stuff

		getTofTree().initialize();

		// step through all messages on disk up to maxMessageDisplay and check if there are new messages
		// if a new message is in a folder, this folder is show yellow in tree
		TOF.initialSearchNewMessages();

		if (core.isFreenetOnline()) {
			downloadActivateCheckBox.setSelected(
				frostSettings.getBoolValue("downloadingActivated"));
			tofAutomaticUpdateMenuItem.setSelected(
				frostSettings.getBoolValue("automaticUpdate"));
		} else {
			downloadActivateCheckBox.setSelected(false);
			tofAutomaticUpdateMenuItem.setSelected(false);
		}
		searchAllBoardsCheckBox.setSelected(
			frostSettings.getBoolValue("searchAllBoards"));
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
		// create objects
		JMenuBar menuBar = new JMenuBar();

		//File Menu
		JMenu fileMenu = new JMenu(languageResource.getString("File"));
		JMenuItem fileExitMenuItem = new JMenuItem(languageResource.getString("Exit"));

		//Messages (tof) Menu
		JMenu tofMenu = new JMenu(languageResource.getString("News"));
		JMenuItem tofConfigureBoardMenuItem =
			new JMenuItem(languageResource.getString("Configure selected board"));
		tofConfigureBoardMenuItem.setIcon(
			getScaledImage("/data/configure.gif"));

		JMenuItem tofDisplayBoardInfoMenuItem =
			new JMenuItem(
				languageResource.getString("Display board information window"));
		tofDisplayBoardInfoMenuItem.setIcon(getScaledImage("/data/info.gif"));

		this.tofAutomaticUpdateMenuItem =
			new JCheckBoxMenuItem(
		languageResource.getString("Automatic message update"),
				true);
		JMenuItem tofIncreaseFontSizeMenuItem =
			new JMenuItem(languageResource.getString("Increase Font Size"));
		JMenuItem tofDecreaseFontSizeMenuItem =
			new JMenuItem(languageResource.getString("Decrease Font Size"));
		JMenuItem tofDisplayKnownBoards =
			new JMenuItem(languageResource.getString("Display known boards"));
		tofDisplayKnownBoards.setIcon(getScaledImage("/data/knownboards.gif"));

		//Options Menu
		JMenu optionsMenu = new JMenu(languageResource.getString("Options"));
		JMenuItem optionsPreferencesMenuItem =
			new JMenuItem(languageResource.getString("Preferences"));

		//Plugin Menu
		JMenu pluginMenu = new JMenu("Plugins");
		JMenuItem pluginBrowserMenuItem =
			new JMenuItem(languageResource.getString("Experimental Freenet Browser"));
		JMenuItem pluginTranslateMenuItem =
			new JMenuItem(languageResource.getString("Translate Frost into another language"));

		//Language Menu
		JMenu languageMenu = new JMenu(languageResource.getString("Language"));
		JMenuItem languageDefaultMenuItem = new JMenuItem(languageResource.getString("Default"));
		JMenuItem languageDutchMenuItem = new JMenuItem(languageResource.getString("Dutch"));
		JMenuItem languageEnglishMenuItem = new JMenuItem(languageResource.getString("English"));
		JMenuItem languageFrenchMenuItem = new JMenuItem(languageResource.getString("French"));
		JMenuItem languageGermanMenuItem = new JMenuItem(languageResource.getString("German"));
		JMenuItem languageItalianMenuItem = new JMenuItem(languageResource.getString("Italian"));
		JMenuItem languageJapaneseMenuItem = new JMenuItem(languageResource.getString("Japanese"));
		JMenuItem languageSpanishMenuItem = new JMenuItem(languageResource.getString("Spanish"));
		JMenuItem languageBulgarianMenuItem = new JMenuItem(languageResource.getString("Bulgarian"));
		
		//Help Menu
		JMenu helpMenu = new JMenu(languageResource.getString("Help"));
		JMenuItem helpHelpMenuItem = new JMenuItem(languageResource.getString("Help"));
		JMenuItem helpAboutMenuItem = new JMenuItem(languageResource.getString("About"));
		
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
				tofConfigureBoardMenuItem_actionPerformed(e, getSelectedNode());
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
					java.util.ResourceBundle.getBundle(
						"res.LangRes");
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
			uploadAddFilesButton.setEnabled(false);
		} else {
			tofNewMessageButton.setEnabled(true);
			uploadAddFilesButton.setEnabled(true);
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

				uploadAddFilesButton.setEnabled(true);
				renameBoardButton.setEnabled(false);
				tofReplyButton.setEnabled(false);
				downloadAttachmentsButton.setEnabled(false);
				downloadBoardsButton.setEnabled(false);

				// read all messages for this board into message table
				TOF.updateTofTable(node, keypool);
				messageTable.clearSelection();
			} else {
				// node is a folder
				DefaultTableModel model =
					(DefaultTableModel) getMessageTable().getModel();
				model.setRowCount(0);
				updateMessageCountLabels(node);

				uploadAddFilesButton.setEnabled(false);
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

	/**Get keyTyped for downloadTable*/
	public void downloadTable_keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if (key == KeyEvent.VK_DELETE && !getDownloadTable().isEditing()) {
			getDownloadTable().removeSelectedChunks();
			getDownloadTable().removeSelectedRows();
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
				.append("   " + languageResource.getString("Selected board") + ": ")
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
			&& downloadActivateCheckBox.isSelected()) {
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

	/**searchTextField Action Listener (search)*/
	private void searchTextField_actionPerformed(ActionEvent e) {
		if (searchButton.isEnabled()) {
			searchButton_actionPerformed(e);
		}
	}

	/**downloadTextField Action Listener (Download/Quickload)*/
	private void downloadTextField_actionPerformed(ActionEvent e) {
		String key = (downloadTextField.getText()).trim();
		if (key.length() > 0) {
			// strip the 'browser' prefix
			String stripMe = "http://127.0.0.1:8888/";
			if (key.startsWith(stripMe)) {
				key = key.substring(stripMe.length());
			}
			// strip the 'freenet:' prefix
			stripMe = "freenet:";
			if (key.startsWith(stripMe)) {
				key = key.substring(stripMe.length());
			}

			String validkeys[] = { "SSK@", "CHK@", "KSK@" };
			int keyType = -1; // invalid

			for (int i = 0; i < validkeys.length; i++) {
				if (key
					.substring(0, validkeys[i].length())
					.equals(validkeys[i])) {
					keyType = i;
					break;
				}
			}

			if (keyType > -1) {
				// added a way to specify a file name. The filename is preceeded by a colon.
				String fileName;

				int sepIndex = key.lastIndexOf(":");

				if (sepIndex != -1) {
					fileName = key.substring(sepIndex + 1);
					key = key.substring(0, sepIndex);
				}
				// take the filename from the last part the SSK or KSK
				else if (-1 != (sepIndex = key.lastIndexOf("/"))) {
					fileName = key.substring(sepIndex + 1);
				} else {
					fileName = key.substring(4);
				}
				//  zab, why did you comment this out, its needed, because otherwise you
				//  use a wrong CHK key for download! i pasted a CHK@afcdf432dk/mytargetfilename.data
				//FIXED: it happened when sha1 was still kept in this variable - and sha1 can contain "/"

				// remove filename from key for CHK
				if (keyType == 1) // CHK?
					key = key.substring(0, key.indexOf("/"));

				// add valid key to download table
				FrostDownloadItemObject dlItem =
					new FrostDownloadItemObject(fileName,
					//users weren't happy with '_'
	key, null);
				boolean isAdded = getDownloadTable().addDownloadItem(dlItem);

				if (isAdded == true)
					downloadTextField.setText("");
			} else {
				// show messagebox that key is invalid
				String keylist = "";
				for (int i = 0; i < validkeys.length; i++) {
					if (i > 0)
						keylist += ", ";
					keylist += validkeys[i];
				}
				JOptionPane.showMessageDialog(
					this,
					languageResource.getString(
						"Invalid key.  Key must begin with one of")
						+ ": "
						+ keylist,
					languageResource.getString("Invalid key"),
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**searchButton Action Listener (Search)*/
	private void searchButton_actionPerformed(ActionEvent e) {
		searchButton.setEnabled(false);
		getSearchTable().clearSelection();
		((SearchTableModel) getSearchTable().getModel()).clearDataModel();
		Vector boardsToSearch;
		if (searchAllBoardsCheckBox.isSelected()) {
			// search in all boards
			boardsToSearch = getTofTree().getAllBoards();
		} else {
			if (getSelectedNode().isFolder() == false) {
				// search in selected board
				boardsToSearch = new Vector();
				boardsToSearch.add(getSelectedNode());
			} else {
				// search in all boards below the selected folder
				Enumeration enu = getSelectedNode().depthFirstEnumeration();
				boardsToSearch = new Vector();
				while (enu.hasMoreElements()) {
					FrostBoardObject b = (FrostBoardObject) enu.nextElement();
					if (b.isFolder() == false) {
						boardsToSearch.add(b);
					}
				}
			}
		}

		SearchThread searchThread =
			new SearchThread(
				searchTextField.getText(),
				boardsToSearch,
				keypool,
				(String) searchComboBox.getSelectedItem());
		searchThread.start();
	}

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
			BoardInfoFrame boardInfo = new BoardInfoFrame(this, languageResource.getResourceBundle());
			boardInfo.startDialog();
		}
	}

	private void tofDisplayKnownBoardsMenuItem_actionPerformed(ActionEvent e) {
		KnownBoardsFrame knownBoards = new KnownBoardsFrame(this, languageResource.getResourceBundle());
		knownBoards.startDialog();
	}

	//------------------------------------------------------------------------

	private void uploadAddFilesButton_actionPerformed(ActionEvent e) {
		FrostBoardObject board = getSelectedNode();
		if (board.isFolder())
			return;

		final JFileChooser fc =
			new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
		fc.setDialogTitle(
			languageResource.getString("Select files you want to upload to the") + " "
				+ board.toString()
				+ " " + languageResource.getString("board") + ".");
		fc.setFileHidingEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		fc.setPreferredSize(new Dimension(600, 400));

		int returnVal = fc.showOpenDialog(frame1.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				frostSettings.setValue("lastUsedDirectory", file.getParent());
				File[] selectedFiles = fc.getSelectedFiles();

				for (int i = 0; i < selectedFiles.length; i++) {
					// collect all choosed files + files in all choosed directories
					ArrayList allFiles =
						FileAccess.getAllEntries(selectedFiles[i], "");
					for (int j = 0; j < allFiles.size(); j++) {
						File newFile = (File) allFiles.get(j);
						if (newFile.isFile() && newFile.length() > 0) {
							FrostUploadItemObject ulItem =
								new FrostUploadItemObject(newFile, board);
							boolean isAdded =
								getUploadTable().addUploadItem(ulItem);
						}
					}
				}
			}
		}
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
	private void tofConfigureBoardMenuItem_actionPerformed(
		ActionEvent e,
		FrostBoardObject board) {
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
		OptionsFrame optionsDlg = new OptionsFrame(this, languageResource.getResourceBundle());
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
			if (e.getClickCount() == 2) {
				// Start file from download table
				if (e.getComponent().equals(getDownloadTable())) {
					int clickedCol =
						getDownloadTable().columnAtPoint(e.getPoint());
					int modelIx =
						getDownloadTable()
							.getColumnModel()
							.getColumn(clickedCol)
							.getModelIndex();
					if (modelIx == 0)
						return;

					DownloadTableModel dlModel =
						(DownloadTableModel) getDownloadTable().getModel();
					FrostDownloadItemObject dlItem =
						(FrostDownloadItemObject) dlModel.getRow(
							getDownloadTable().getSelectedRow());
					String execFilename =
						new StringBuffer()
							.append(System.getProperty("user.dir"))
							.append(fileSeparator)
							.append(frostSettings.getValue("downloadDirectory"))
							.append(dlItem.getFileName())
							.toString();
					File file = new File(execFilename);
					Core.getOut().println("Executing: " + file.getPath());
					if (file.exists()) {
						Execute.run("exec.bat" + " \"" + file.getPath() + "\"");
					}
				}

				// Start file from upload table
				if (e.getComponent().equals(getUploadTable())) {
					UploadTableModel ulModel =
						(UploadTableModel) getUploadTable().getModel();
					FrostUploadItemObject ulItem =
						(FrostUploadItemObject) ulModel.getRow(
							getUploadTable().getSelectedRow());
					File file = new File(ulItem.getFilePath());
					Core.getOut().println("Executing: " + file.getPath());
					if (file.exists()) {
						Execute.run("exec.bat" + " \"" + file.getPath() + "\"");
					}
				}

				// Add search result to download table
				if (e.getComponent().equals(searchTable)) {
					getSearchTable().addSelectedSearchItemsToDownloadTable(
						getDownloadTable());
				}

			} else {
				maybeShowPopup(e);
			}
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() == false) {
				return;
			} else if (
				e.getComponent().equals(getUploadTable())) { // Upload Popup
				showUploadTablePopupMenu(e);
			} else if (e.getComponent().equals(searchTable)) { // Search Popup
				showSearchTablePopupMenu(e);
			} else if (
				e.getComponent().equals(
					getDownloadTable())) { // Downloads Popup
				showDownloadTablePopupMenu(e);
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
	 * Updates the search result count.
	 * Called by search thread.
	 */
	public void updateSearchResultCountLabel() {
		DefaultTableModel model = (DefaultTableModel) searchTable.getModel();
		int searchResults = model.getRowCount();
		if (searchResults == 0) {
			searchResultsCountLabel.setText(searchResultsCountPrefix + "0");
		}
		searchResultsCountLabel.setText(
			searchResultsCountPrefix + searchResults);
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
		String s =
			new StringBuffer()
				.append(languageResource.getString("Waiting"))
				.append(" : ")
				.append(waitingItems)
				.toString();
		downloadItemCountLabel.setText(s);
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
		int selRow = getTofTree().getRowForLocation(e.getX(), e.getY());
		if (selRow == -1) {
			// no node is clicked -> no menu
			return;
		}

		TreePath selPath = getTofTree().getPathForLocation(e.getX(), e.getY());
		final FrostBoardObject board =
			(FrostBoardObject) selPath.getLastPathComponent();

		// create menu objects
		String dtxt = ((board.isFolder()) ? "Folder" : "Board");
		JMenuItem description = new JMenuItem(dtxt + ": " + board.toString());
		String dtxt2 = ((board.isFolder()) ? "folder" : "board");
		description.setEnabled(false);
		JMenuItem tofTreePopupRefresh = new JMenuItem(languageResource.getString("Refresh")  + " "+ languageResource.getString(dtxt2));
		tofTreePopupRefresh.setIcon(getScaledImage("/data/update.gif"));

		JMenuItem tofTreePopupAddNode = new JMenuItem(languageResource.getString("Add new board"));
		// TODO: translate
		tofTreePopupAddNode.setIcon(getScaledImage("/data/newboard.gif"));

		JMenuItem tofTreePopupAddFolder = new JMenuItem(languageResource.getString("Add new folder"));
		tofTreePopupAddFolder.setIcon(getScaledImage("/data/newfolder.gif"));

		JMenuItem tofTreePopupRemoveNode = new JMenuItem(languageResource.getString("Remove") + " " + languageResource.getString(dtxt2));
		tofTreePopupRemoveNode.setIcon(getScaledImage("/data/remove.gif"));

		JMenuItem tofTreePopupCutNode = new JMenuItem(languageResource.getString("Cut") + " " + languageResource.getString(dtxt2));
		tofTreePopupCutNode.setIcon(getScaledImage("/data/cut.gif"));

		JMenuItem tofTreePopupPasteNode = null;
		if (clipboard != null) {
			String dtxt3 = ((clipboard.isFolder()) ? "folder" : "board");
			tofTreePopupPasteNode =
				new JMenuItem(
					languageResource.getString("Paste") + " " + languageResource.getString(dtxt3) + " '" + clipboard.toString() + "'");
			tofTreePopupPasteNode.setIcon(getScaledImage("/data/paste.gif"));
			tofTreePopupPasteNode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pasteFromClipboard(board);
				}
			});
		}

		JMenuItem tofTreePopupConfigureBoard =
			new JMenuItem(languageResource.getString("Configure selected board"));
		tofTreePopupConfigureBoard.setIcon(
			getScaledImage("/data/configure.gif"));
		JMenuItem tofTreePopupCancel =
			new JMenuItem(languageResource.getString("Cancel"));
		// add action listeners
		tofTreePopupAddNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getTofTree().createNewBoard(frame1.getInstance());
			}
		});
		tofTreePopupAddFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getTofTree().createNewFolder(frame1.getInstance());
			}
		});
		tofTreePopupRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshNode(board);
			}
		});
		tofTreePopupRemoveNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeNode(board);
			}
		});
		tofTreePopupCutNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cutNode(board);
			}
		});
		tofTreePopupConfigureBoard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tofConfigureBoardMenuItem_actionPerformed(e, board);
			}
		});

		JPopupMenu pmenu = new JPopupMenu();
		pmenu.add(description);
		pmenu.addSeparator();
		pmenu.add(tofTreePopupRefresh);
		if (board.isFolder() == false) {
			pmenu.addSeparator();
			pmenu.add(tofTreePopupConfigureBoard);
		}
		pmenu.addSeparator();
		pmenu.add(tofTreePopupAddNode);
		pmenu.add(tofTreePopupAddFolder);
		if (board.isRoot() == false) {
			pmenu.add(tofTreePopupRemoveNode);
		}
		pmenu.addSeparator();
		if (board.isRoot() == false) {
			pmenu.add(tofTreePopupCutNode);
		}
		if (clipboard != null
			&& tofTreePopupPasteNode != null
			&& board.isFolder()) {
			pmenu.add(tofTreePopupPasteNode);
		}
		pmenu.addSeparator();
		pmenu.add(tofTreePopupCancel);
		pmenu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showAttachmentTablePopupMenu(MouseEvent e) {
		getPopupMenuAttachmentTable().show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showAttachmentBoardPopupMenu(MouseEvent e) {
		getPopupMenuAttachmentBoard().show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showTofTextAreaPopupMenu(MouseEvent e) {
		getPopupMenuTofText().show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showDownloadTablePopupMenu(MouseEvent e) {		
		getPopupMenuDownload().show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showUploadTablePopupMenu(MouseEvent e) {
		getPopupMenuUpload().show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showSearchTablePopupMenu(MouseEvent e) {
		getPopupMenuSearch().show(e.getComponent(), e.getX(), e.getY());
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

		File msgFile = targetMessage.getFile();
		FileAccess.writeFile(
			"This message is new!",
			msgFile.getPath() + ".lck");

		messageTable.removeRowSelectionInterval(
			0,
			messageTable.getRowCount() - 1);

		String from = (String) messageTable.getModel().getValueAt(row, 1);
		if (from.indexOf("<font color=\"blue\">") != -1) {
			StringBuffer sbtmp = new StringBuffer();
			sbtmp.append("<html><font color=\"blue\"><b>");
			sbtmp.append(targetMessage.getFrom());
			sbtmp.append("</b></font></html>");
			messageTable.getModel().setValueAt(sbtmp.toString(), row, 1);
			// Message with attachment
		} else {
			StringBuffer sbtmp = new StringBuffer();
			sbtmp.append("<html><b>");
			sbtmp.append(targetMessage.getFrom());
			sbtmp.append("</b></html>");
			messageTable.getModel().setValueAt(sbtmp.toString(), row, 1);
		}

		getSelectedNode().incNewMessageCount();
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
	private PopupMenuUpload getPopupMenuUpload() {
		if (popupMenuUpload == null) {
			popupMenuUpload = new PopupMenuUpload();
			languageResource.addLanguageListener(popupMenuUpload);	
		}
		return popupMenuUpload;
	}
	
	/**
	 * @return
	 */
	private PopupMenuSearch getPopupMenuSearch() {
		if (popupMenuSearch == null) {
			popupMenuSearch = new PopupMenuSearch();
			languageResource.addLanguageListener(popupMenuSearch);
		}
		return popupMenuSearch;
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
	private PopupMenuDownload getPopupMenuDownload() {
		if (popupMenuDownload == null) {
			popupMenuDownload = new PopupMenuDownload();
			languageResource.addLanguageListener(popupMenuDownload);	
		}
		return popupMenuDownload;
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
				Core.getOut().println("message was CHECK but not found in Neutral list");
				return;
			}
		}

		if (status.indexOf(VerifyableMessageObject.FAILED) > -1) {
			Identity owner = Core.getEnemies().Get(selectedMessage.getFrom());
			if (owner == null) {
				Core.getOut().println("message was BAD but not found in BAD list");
				return;
			}

		}

		if (status.indexOf(VerifyableMessageObject.VERIFIED) > -1) {
			Identity owner = Core.getFriends().Get(selectedMessage.getFrom());
			if (owner == null) {
				Core.getOut().println("message was GOOD but not found in GOOD list");
				return;
			}
		}

		Truster truster = new Truster(Core.getInstance(), what, selectedMessage.getFrom());
		truster.start();
	}

}
