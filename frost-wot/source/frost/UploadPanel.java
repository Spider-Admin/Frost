/*
 * Created on Nov 14, 2003
 */
package frost;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;

import frost.ext.Execute;
import frost.gui.*;
import frost.gui.components.JSkinnablePopupMenu;
import frost.gui.model.UploadTableModel;
import frost.gui.objects.*;
import frost.gui.translation.*;

/**
 * 
 */
public class UploadPanel extends JPanel {
	/**
	 *  
	 */
	private class PopupMenuUpload extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

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
			copyChkKeyAndFilenameToClipboardItem.setText(
				languageResource.getString("CHK key + filename"));
			copyChkKeyToClipboardItem.setText(languageResource.getString("CHK key"));
			generateChkForSelectedFilesItem.setText(
				languageResource.getString("Start encoding of selected files"));
			reloadAllFilesItem.setText(languageResource.getString("Reload all files"));
			reloadSelectedFilesItem.setText(languageResource.getString("Reload selected files"));
			removeAllFilesItem.setText(languageResource.getString("Remove all files"));
			removeSelectedFilesItem.setText(languageResource.getString("Remove selected files"));
			restoreDefaultFilenamesForAllFilesItem.setText(
				languageResource.getString("Restore default filenames for all files"));
			restoreDefaultFilenamesForSelectedFilesItem.setText(
				languageResource.getString("Restore default filenames for selected files"));
			setPrefixForAllFilesItem.setText(
				languageResource.getString("Set prefix for all files"));
			setPrefixForSelectedFilesItem.setText(
				languageResource.getString("Set prefix for selected files"));

			changeDestinationBoardMenu.setText(
				languageResource.getString("Change destination board"));
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
			uploadTable.selectAll();
			uploadTable.restoreOriginalFilenamesForSelectedRows();
		}

		/**
		 * Restore default filenames for selected files
		 */
		private void restoreDefaultFilenamesForSelectedFiles() {
			uploadTable.restoreOriginalFilenamesForSelectedRows();
		}

		/**
		 * Set Prefix for all files
		 */
		private void setPrefixForAllFiles() {
			uploadTable.selectAll();
			uploadTable.setPrefixForSelectedFiles();
		}

		/**
		 * Set Prefix for selected files
		 */
		private void setPrefixForSelectedFiles() {
			uploadTable.setPrefixForSelectedFiles();
		}

		/**
		 * Generate CHK for selected files 
		 */
		private void generateChkForSelectedFiles() {
			UploadTableModel tableModel = (UploadTableModel) uploadTable.getModel();
			int[] selectedRows = uploadTable.getSelectedRows();
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
		} /**
					 * Reload all files
					 */
		private void reloadAllFiles() {
			UploadTableModel tableModel = (UploadTableModel) uploadTable.getModel();
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
			UploadTableModel tableModel = (UploadTableModel) uploadTable.getModel();
			int[] selectedRows = uploadTable.getSelectedRows();
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
			UploadTableModel model = (UploadTableModel) uploadTable.getModel();
			model.clearDataModel();
		}

		/**
		 * Remove selected files
		 */
		private void removeSelectedFiles() {
			uploadTable.removeSelectedRows();
		}

		/**
		 * add CHK key + filename to clipboard 
		 */
		private void copyChkKeyAndFilenameToClipboard() {
			UploadTableModel tableModel = (UploadTableModel) uploadTable.getModel();
			int selectedRow = uploadTable.getSelectedRow();
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
			UploadTableModel tableModel = (UploadTableModel) uploadTable.getModel();
			int selectedRow = uploadTable.getSelectedRow();
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

			if (uploadTable.getSelectedRowCount() == 1) {
				// if 1 item is selected
				FrostUploadItemObject ulItem =
					(FrostUploadItemObject) ((UploadTableModel) uploadTable.getModel()).getRow(
						uploadTable.getSelectedRow());
				if (ulItem.getKey() != null) {
					add(copyToClipboardMenu);
					addSeparator();
				}
			}

			JMenu removeSubMenu = new JMenu(languageResource.getString("Remove") + "...");
			if (uploadTable.getSelectedRow() > -1) {
				removeSubMenu.add(removeSelectedFilesItem);
			}
			removeSubMenu.add(removeAllFilesItem);

			add(removeSubMenu);
			addSeparator();
			if (uploadTable.getSelectedRow() > -1) {
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
			if (!Core.frostSettings.getBoolValue("automaticIndexing")) {
				UploadTableModel ulModel = (UploadTableModel) uploadTable.getModel();
				boolean shouldEnable = true;
				int[] selectedRows = uploadTable.getSelectedRows();
				for (int xy = 0; xy < selectedRows.length;xy++) {
					FrostUploadItemObject ulItem =
					(FrostUploadItemObject) ulModel.getRow(selectedRows[xy]);
					if (ulItem.getSHA1() != null && ulItem.getSHA1().length()>0){
						shouldEnable=false;
						break;
					}	
				}
			if (uploadTable.getSelectedRow() > -1) {
				add(setPrefixForSelectedFilesItem);
				setPrefixForSelectedFilesItem.setEnabled(shouldEnable);
			}
		//	add(setPrefixForAllFilesItem);
		//	addSeparator();
			if (uploadTable.getSelectedRow() > -1) {
				add(restoreDefaultFilenamesForSelectedFilesItem);
				restoreDefaultFilenamesForSelectedFilesItem.setEnabled(shouldEnable);
			}
		//	add(restoreDefaultFilenamesForAllFilesItem);
			addSeparator();
			if (uploadTable.getSelectedRow() > -1) {
				// Add boards to changeDestinationBoard submenu
				Vector boards = tofTree.getAllBoards();
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
							UploadTableModel ulModel = (UploadTableModel) uploadTable.getModel();
							int[] selectedRows = uploadTable.getSelectedRows();
							for (int x = 0; x < selectedRows.length; x++) {
								FrostUploadItemObject ulItem =
									(FrostUploadItemObject) ulModel.getRow(selectedRows[x]);
								//also check whether the item has not been hashed, i.e. added to
								//an index already - big mess to change it if that's the case
								ulItem.setTargetBoard(aBoard);
								ulModel.updateRow(ulItem);
							}
						}
					});
				}
				add(changeDestinationBoardMenu);
				changeDestinationBoardMenu.setEnabled(shouldEnable);
			}
			}//end of options which are available if automatic indexing turned off
			addSeparator();
			add(cancelItem);

			super.show(invoker, x, y);
		}

	}

	/**
	 * 
	 */
	private class Listener
		implements LanguageListener, KeyListener, ActionListener, MouseListener {

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
			if (e.getSource() == uploadTable) {
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
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			// Nothing here			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getSource() == uploadTable) {
					// Start file from download table. Is this a good idea?
					uploadTableDoubleClick(e);
				}
			} else if (e.isPopupTrigger())
				if ((e.getSource() == uploadTable) || (e.getSource() == uploadTableScrollPane)) {
					showUploadTablePopupMenu(e);
				}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == uploadTable) || (e.getSource() == uploadTableScrollPane)) {
					showUploadTablePopupMenu(e);
				}

			}
		}
	}

	private PopupMenuUpload popupMenuUpload = null;

	private Listener listener = new Listener();
	
	private static Logger logger = Logger.getLogger(UploadPanel.class.getName());

	private UploadTable uploadTable = null;
	private TofTree tofTree = null;
	private SettingsClass settingsClass = null;

	private UpdatingLanguageResource languageResource = null;

	private JPanel uploadTopPanel = new JPanel();
	private JButton uploadAddFilesButton =
		new JButton(new ImageIcon(getClass().getResource("/data/browse.gif")));
	private JScrollPane uploadTableScrollPane = null;

	private boolean initialized = false;

	/**
	 * 
	 */
	public UploadPanel() {
		super();
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			// create the top panel
			configureButton(uploadAddFilesButton, "/data/browse_rollover.gif");
			uploadTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
			uploadTopPanel.add(uploadAddFilesButton);

			// create the main upload panel
			uploadTableScrollPane = new JScrollPane(uploadTable);
			setLayout(new BorderLayout());
			add(uploadTopPanel, BorderLayout.NORTH);
			add(uploadTableScrollPane, BorderLayout.CENTER);

			// listeners
			uploadAddFilesButton.addActionListener(listener);
			uploadTableScrollPane.addMouseListener(listener);

			initialized = true;
		}
	}

	/**
	 * @param bundle
	 */
	public void setLanguageResource(UpdatingLanguageResource newLanguageResource) {
		if (languageResource != null) {
			languageResource.removeLanguageListener(listener);
		}
		languageResource = newLanguageResource;
		languageResource.addLanguageListener(listener);
	}

	/**
	 * description
	 * 
	 * @param downloadTable description
	 */
	public void setUploadTable(UploadTable newUploadTable) {
		if (uploadTable != null) {
			uploadTable.removeKeyListener(listener);
			uploadTable.removeMouseListener(listener);
		}
		uploadTable = newUploadTable;
		uploadTable.addKeyListener(listener);
		uploadTable.addMouseListener(listener);
	}

	public void setAddFilesButtonEnabled(boolean enabled) {
		uploadAddFilesButton.setEnabled(enabled);
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		uploadAddFilesButton.setToolTipText(languageResource.getString("Browse") + "...");
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
	 * @param e
	 */
	private void uploadTable_keyPressed(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_DELETE && !uploadTable.isEditing())
			uploadTable.removeSelectedRows();
	}

	private FrostBoardObject getSelectedNode() { //TODO: move this method to TofTree
		FrostBoardObject node = (FrostBoardObject) tofTree.getLastSelectedPathComponent();
		if (node == null) {
			// nothing selected? unbelievable ! so select the root ...
			tofTree.setSelectionRow(0);
			node = (FrostBoardObject) tofTree.getModel().getRoot();
		}
		return node;
	}

	//------------------------------------------------------------------------

	public void uploadAddFilesButton_actionPerformed(ActionEvent e) {
		FrostBoardObject board = getSelectedNode();
		if (board.isFolder())
			return;

		final JFileChooser fc = new JFileChooser(settingsClass.getValue("lastUsedDirectory"));
		fc.setDialogTitle(
			languageResource.getString("Select files you want to upload to the")
				+ " "
				+ board.toString()
				+ " "
				+ languageResource.getString("board")
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
							FrostUploadItemObject ulItem =
								new FrostUploadItemObject(newFile, board);
							boolean isAdded = uploadTable.addUploadItem(ulItem);
						}
					}
				}
			}
		}
	}

	private void showUploadTablePopupMenu(MouseEvent e) {
		getPopupMenuUpload().show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Configures a button to be a default icon button
	 * @param button The new icon button
	 * @param rolloverIcon Displayed when mouse is over button
	 */
	private void configureButton(JButton button, String rolloverIcon) {
		button.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
	}

	/**
	 * @param tree
	 */
	public void setTofTree(TofTree newTree) {
		tofTree = newTree;
	}

	/**
	 * @param class1
	 */
	public void setSettingsClass(SettingsClass newSettingsClass) {
		settingsClass = newSettingsClass;
	}

	/**
	 * @param e
	 */
	private void uploadTableDoubleClick(MouseEvent e) {
		UploadTableModel ulModel = (UploadTableModel) uploadTable.getModel();
		FrostUploadItemObject ulItem =
			(FrostUploadItemObject) ulModel.getRow(uploadTable.getSelectedRow());
		File file = new File(ulItem.getFilePath());
		logger.info("Executing: " + file.getPath());
		if (file.exists()) {
			Execute.run("exec.bat" + " \"" + file.getPath() + "\"");
		}
	}

}
