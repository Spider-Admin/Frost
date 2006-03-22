/*
  UploadPanel.java / Frost
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
package frost.fileTransfer.upload;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;

import frost.*;
import frost.boards.TofTreeModel;
import frost.ext.Execute;
import frost.gui.objects.Board;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.SortedModelTable;

/**
 * @author $Author$
 * @version $Revision$
 */
public class UploadPanel extends JPanel {
	
	private class PopupMenuUpload extends JSkinnablePopupMenu implements ActionListener, LanguageListener, ClipboardOwner {

		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem copyKeysAndNamesItem = new JMenuItem();
		private JMenuItem copyKeysItem = new JMenuItem();
		private JMenuItem copyExtendedInfoItem = new JMenuItem();
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
		
		private String keyNotAvailableMessage;
		private String fileMessage;
		private String keyMessage;
		private String bytesMessage;
		
		private Clipboard clipboard;

		public PopupMenuUpload() {
			super();
			initialize();
		}

		private void initialize() {
			refreshLanguage();

			copyToClipboardMenu.add(copyKeysAndNamesItem);
			copyToClipboardMenu.add(copyKeysItem);
			copyToClipboardMenu.add(copyExtendedInfoItem);

			copyKeysAndNamesItem.addActionListener(this);
			copyKeysItem.addActionListener(this);
			copyExtendedInfoItem.addActionListener(this);
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
			keyNotAvailableMessage = language.getString("Key not available yet");
			fileMessage = language.getString("clipboard.File:");
			keyMessage = language.getString("clipboard.Key:");
			bytesMessage = language.getString("clipboard.Bytes:");
			
			cancelItem.setText(language.getString("Cancel"));
			copyKeysItem.setText(language.getString("Copy keys only"));
			copyKeysAndNamesItem.setText(language.getString("Copy keys with filenames"));
			copyExtendedInfoItem.setText(language.getString("Copy extended info"));
			generateChkForSelectedFilesItem.setText(
					language.getString("Start encoding of selected files"));
			reloadAllFilesItem.setText(language.getString("Reload all files"));
			reloadSelectedFilesItem.setText(language.getString("Reload selected files"));
			removeAllFilesItem.setText(language.getString("Remove all files"));
			removeSelectedFilesItem.setText(language.getString("Remove selected files"));
			restoreDefaultFilenamesForAllFilesItem.setText(
					language.getString("Restore default filenames for all files"));
			restoreDefaultFilenamesForSelectedFilesItem.setText(
					language.getString("Restore default filenames for selected files"));
			setPrefixForAllFilesItem.setText(
					language.getString("Set prefix for all files"));
			setPrefixForSelectedFilesItem.setText(
					language.getString("Set prefix for selected files"));

			changeDestinationBoardMenu.setText(
					language.getString("Change destination board"));
			copyToClipboardMenu.setText(language.getString("Copy to clipboard") + "...");
		}
		
		/**
		 * @return
		 */
		private Clipboard getClipboard() {
			if (clipboard == null) {
				clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			}
			return clipboard;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == copyKeysItem) {
				copyKeys();
			}
			if (e.getSource() == copyKeysAndNamesItem) {
				copyKeysAndNames();
			}
			if (e.getSource() == copyExtendedInfoItem) {
				copyExtendedInfo();
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
			model.removePrefixFromAllItems();
		}

		/**
		 * Restore default filenames for selected files
		 */
		private void restoreDefaultFilenamesForSelectedFiles() {
			model.removePrefixFromItems(modelTable.getSelectedItems());
		}

		/**
		 * Set Prefix for all files
		 */
		private void setPrefixForAllFiles() {
			String prefix =
				JOptionPane.showInputDialog(
						language.getString(
							"Please enter the prefix you want to use for your files."));
			if (prefix != null) {
				model.setPrefixToAllItems(prefix);
			}
		}
		/**
		 * Set Prefix for selected files
		 */
		private void setPrefixForSelectedFiles() {
			String prefix =
				JOptionPane.showInputDialog(
						language.getString(
							"Please enter the prefix you want to use for your files."));
			if (prefix != null) {
				model.setPrefixToItems(modelTable.getSelectedItems(), prefix);
			}
		}

		/**
		 * Generate CHK for selected files 
		 */
		private void generateChkForSelectedFiles() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.generateChkItems(selectedItems);
		} 
		
		/**
		 * Reload all files
		 */
		private void reloadAllFiles() {
			model.requestAllItems();
		}

		/**
		 * Reload selected files
		 */
		private void reloadSelectedFiles() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.requestItems(selectedItems);
		}

		/**
		 * Remove all files
		 */
		private void removeAllFiles() {
			model.clear();
		}

		/**
		 * Remove selected files
		 */
		private void removeSelectedFiles() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
		}

		/**
		 * This method copies the CHK keys and file names of the selected items (if any) to
		 * the clipboard.
		 */
		private void copyKeysAndNames() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			if (selectedItems.length > 0) {
				StringBuffer textToCopy = new StringBuffer();
				for (int i = 0; i < selectedItems.length; i++) {
					FrostUploadItem item = (FrostUploadItem) selectedItems[i];
					String key = item.getKey();
					if (key == null) {
						key = keyNotAvailableMessage;
					}
					textToCopy.append(key);
					textToCopy.append("/");
					textToCopy.append(item.getFileName());
					textToCopy.append("\n");
				}				
				StringSelection selection = new StringSelection(textToCopy.toString());
				getClipboard().setContents(selection, this);	
			}
		}

		/**
		 * This method copies extended information about the selected items (if any) to
		 * the clipboard. That information is composed of the filename, the key and
		 * the size in bytes.
		 */
		private void copyExtendedInfo() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			if (selectedItems.length > 0) {
				StringBuffer textToCopy = new StringBuffer();
				for (int i = 0; i < selectedItems.length; i++) {
					FrostUploadItem item = (FrostUploadItem) selectedItems[i];
					String key = item.getKey();
					if (key == null) {
						key = keyNotAvailableMessage;
					}
					textToCopy.append(fileMessage);
					textToCopy.append(item.getFileName() + "\n");
					textToCopy.append(keyMessage);
					textToCopy.append(key + "\n");
					textToCopy.append(bytesMessage);
					textToCopy.append(item.getFileSize() + "\n\n");
				}				
				//We remove the additional \n at the end
				String result = textToCopy.substring(0, textToCopy.length() - 1);
				
				StringSelection selection = new StringSelection(result);
				getClipboard().setContents(selection, this);	
			}
		}
		
		/**
		 * This method copies the CHK keys of the selected items (if any) to
		 * the clipboard.
		 */
		private void copyKeys() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			if (selectedItems.length > 0) {
				StringBuffer textToCopy = new StringBuffer();
				for (int i = 0; i < selectedItems.length; i++) {
					FrostUploadItem item = (FrostUploadItem) selectedItems[i];
					String key = item.getKey();
					if (key == null) {
						key = keyNotAvailableMessage;
					}
					textToCopy.append(key);
					textToCopy.append("\n");
				}				
				StringSelection selection = new StringSelection(textToCopy.toString());
				getClipboard().setContents(selection, this);	
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

			ModelItem[] selectedItems = modelTable.getSelectedItems();

			if (selectedItems.length > 0) {
				// if at least 1 item is selected
				add(copyToClipboardMenu);
				addSeparator();
			}

			JMenu removeSubMenu = new JMenu(language.getString("Remove") + "...");
			if (selectedItems.length != 0) {
				//If at least 1 item is selected
				removeSubMenu.add(removeSelectedFilesItem);
			}
			removeSubMenu.add(removeAllFilesItem);

			add(removeSubMenu);
			addSeparator();
			if (selectedItems.length != 0) {
				// If at least 1 item is selected
				add(generateChkForSelectedFilesItem);
				add(reloadSelectedFilesItem);
			}
			add(reloadAllFilesItem);
			addSeparator();
			//these options available only if automatic indexing is off and file is not 
			//hashed already.
			//the options that operate on all files (not only selected) are removed for now
			//because making them check whether the file has been indexed or not is a pain
			//feel free to implement ;)
			if (!settingsClass.getBoolValue("automaticIndexing")) {
				boolean shouldEnable = true;
				for (int i = 0; i < selectedItems.length; i++) {
					FrostUploadItem item = (FrostUploadItem) selectedItems[i];
					if (item.getSHA1() != null && item.getSHA1().length() > 0) {
						shouldEnable = false;
						break;
					}
				}
				if (selectedItems.length != 0) {
					// If at least 1 item is selected
					add(setPrefixForSelectedFilesItem);
					setPrefixForSelectedFilesItem.setEnabled(shouldEnable);
				}
				//	add(setPrefixForAllFilesItem);
				//	addSeparator();
				if (selectedItems.length != 0) {
					// If at least 1 item is selected
					add(restoreDefaultFilenamesForSelectedFilesItem);
					restoreDefaultFilenamesForSelectedFilesItem.setEnabled(shouldEnable);
				}
				//	add(restoreDefaultFilenamesForAllFilesItem);
				addSeparator();
				if (selectedItems.length != 0) {
					// If at least 1 item is selected
					// Add boards to changeDestinationBoard submenu
					Vector boards = tofTreeModel.getAllBoards();
					Collections.sort(boards);
					changeDestinationBoardMenu.removeAll();
					for (int i = 0; i < boards.size(); i++) {
						final Board aBoard = (Board) boards.elementAt(i);
						JMenuItem boardMenuItem = new JMenuItem(aBoard.getName());
						changeDestinationBoardMenu.add(boardMenuItem);
						// add all boards to menu + set action listener for each board menu item
						boardMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// set new board for all selected rows
								ModelItem[] selectedItems = modelTable.getSelectedItems();
								for (int x = 0; x < selectedItems.length; x++) {
									FrostUploadItem ulItem = (FrostUploadItem) selectedItems[x];
									//also check whether the item has not been hashed, i.e. added to
									//an index already - big mess to change it if that's the case
									ulItem.setTargetBoard(aBoard);
								}
							}
						});
					}
					add(changeDestinationBoardMenu);
					changeDestinationBoardMenu.setEnabled(shouldEnable);
				}
			} //end of options which are available if automatic indexing turned off
			addSeparator();
			add(cancelItem);

			super.show(invoker, x, y);
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// Nothing here			
		}

	}

	/**
	 * 
	 */
	private class Listener
		extends MouseAdapter
		implements LanguageListener, KeyListener, ActionListener, MouseListener, PropertyChangeListener {

		/**
		 * 
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			if (e.getSource() == modelTable.getTable()) {
				uploadTable_keyPressed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == uploadAddFilesButton) {
				uploadAddFilesButton_actionPerformed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getSource() == modelTable.getTable()) {
					// Start file from download table. Is this a good idea?
					uploadTableDoubleClick(e);
				}
			} else if (e.isPopupTrigger())
				if ((e.getSource() == modelTable.getTable())
					|| (e.getSource() == modelTable.getScrollPane())) {
					showUploadTablePopupMenu(e);
				}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == modelTable.getTable())
					|| (e.getSource() == modelTable.getScrollPane())) {
					showUploadTablePopupMenu(e);
				}

			}
		}
		
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_NAME)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_SIZE)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_STYLE)) {
				fontChanged();
			}
		}
		
	}

	private PopupMenuUpload popupMenuUpload = null;

	private Listener listener = new Listener();
	
	private static Logger logger = Logger.getLogger(UploadPanel.class.getName());

	private UploadModel model = null;

	private TofTreeModel tofTreeModel = null;
	private SettingsClass settingsClass = null;

	private Language language = null;

	private JPanel uploadTopPanel = new JPanel();
	private JButton uploadAddFilesButton =
		new JButton(new ImageIcon(getClass().getResource("/data/browse.gif")));
	private SortedModelTable modelTable;

	private boolean initialized = false;

	/**
	 * @param settingsClass
	 */
	public UploadPanel(SettingsClass settingsClass) {
		super();
		this.settingsClass = settingsClass;
		
		language = Language.getInstance();
		language.addLanguageListener(listener);
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			// create the top panel
			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(uploadAddFilesButton, "/data/browse_rollover.gif");
			uploadTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
			uploadTopPanel.add(uploadAddFilesButton);

			// create the main upload panel
			UploadTableFormat tableFormat = new UploadTableFormat();

			modelTable = new SortedModelTable(model, tableFormat);
			setLayout(new BorderLayout());
			add(uploadTopPanel, BorderLayout.NORTH);
			add(modelTable.getScrollPane(), BorderLayout.CENTER);
			fontChanged();

			// listeners
			uploadAddFilesButton.addActionListener(listener);
			modelTable.getScrollPane().addMouseListener(listener);
			modelTable.getTable().addKeyListener(listener);
			modelTable.getTable().addMouseListener(listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

			initialized = true;
		}
	}

	/**
	 * @param enabled
	 */
	public void setAddFilesButtonEnabled(boolean enabled) {
		uploadAddFilesButton.setEnabled(enabled);
	}

	private void refreshLanguage() {
		uploadAddFilesButton.setToolTipText(language.getString("Browse") + "...");
	}

	/**
	 * @return
	 */
	private PopupMenuUpload getPopupMenuUpload() {
		if (popupMenuUpload == null) {
			popupMenuUpload = new PopupMenuUpload();
			language.addLanguageListener(popupMenuUpload);
		}
		return popupMenuUpload;
	}

	/**
	 * @param e
	 */
	private void uploadTable_keyPressed(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
			modelTable.getTable().clearSelection();
		}
	}

	/**
	 * @param e
	 */
	public void uploadAddFilesButton_actionPerformed(ActionEvent e) {
		Board board = tofTreeModel.getSelectedNode();
		if (board.isFolder())
			return;

		final JFileChooser fc = new JFileChooser(settingsClass.getValue("lastUsedDirectory"));
		fc.setDialogTitle(
			language.getString("Select files you want to upload to the")
				+ " "
				+ board.getName()
				+ " "
				+ language.getString("board")
				+ ".");
		fc.setFileHidingEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		fc.setPreferredSize(new Dimension(600, 400));

		int returnVal = fc.showOpenDialog(this); //TODO: does this work?
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				settingsClass.setValue("lastUsedDirectory", file.getParent());
				File[] selectedFiles = fc.getSelectedFiles();

				for (int i = 0; i < selectedFiles.length; i++) {
					// collect all choosed files + files in all choosed directories
					ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
					for (int j = 0; j < allFiles.size(); j++) {
						File newFile = (File) allFiles.get(j);
						if (newFile.isFile() && newFile.length() > 0) {
							FrostUploadItem ulItem =
								new FrostUploadItem(newFile, board);
							boolean isAdded = model.addUploadItem(ulItem);
						}
					}
				}
			}
		}
	}

	/**
	 * @param e
	 */
	private void showUploadTablePopupMenu(MouseEvent e) {
		getPopupMenuUpload().show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * @param tofTreeModel
	 */
	public void setTofTreeModel(TofTreeModel tofTreeModel) {
		this.tofTreeModel = tofTreeModel;
	}

	/**
	 * @param e
	 */
	private void uploadTableDoubleClick(MouseEvent e) {
		ModelItem[] selectedItems = modelTable.getSelectedItems();
		if (selectedItems.length != 0) {
			FrostUploadItem ulItem = (FrostUploadItem) selectedItems[0];
			File file = new File(ulItem.getFilePath());
			logger.info("Executing: " + file.getPath());
			if (file.exists()) {
                try {
                    Execute.run("exec.bat" + " \"" + file.getPath() + "\"", false);
                } catch(Throwable t) {
                    JOptionPane.showMessageDialog(this,
                            "Could not open the file: "+file.getName()+"\n"+t.toString(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);            
                }
			}
		}
	}
	
	private void fontChanged() {
		String fontName = settingsClass.getValue(SettingsClass.FILE_LIST_FONT_NAME);
		int fontStyle = settingsClass.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
		int fontSize = settingsClass.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
		Font font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n" +
						   "That selection will be changed to \"SansSerif\".");
			settingsClass.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
			font = new Font("SansSerif", fontStyle, fontSize);
		}
		modelTable.setFont(font);
	}

	/**
	 * @param model
	 */
	public void setModel(UploadModel model) {
		this.model = model;
	}
}
