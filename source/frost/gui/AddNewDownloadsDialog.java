/*
  AddNewDownloadsDialog.java / Frost
  Copyright (C) 2010  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.gui.model.*;
import frost.storage.perst.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class AddNewDownloadsDialog extends javax.swing.JDialog {

	private final Language language;
	private final TrackDownloadKeysStorage trackDownloadKeysStorage;

	private AddNewDownloadsTableModel addNewDownloadsTableModel;
	private SortedTable addNewDownloadsTable;
	private JButton removeAlreadyDownloadedButton;
	private JButton removeAlreadyExistsButton;
	private JButton okButton;
	private JButton cancelButton;
	private JSkinnablePopupMenu tablePopupMenu;

	private boolean addDownloads;

	private final Frame parentFrame;

	private static final long serialVersionUID = 1L;

	public AddNewDownloadsDialog(final JFrame frame, final List<FrostDownloadItem> frostDownloadItemList) {
		super(frame);
		parentFrame = frame;
		setModal(true);
		addDownloads = false;
		language = Language.getInstance();
		trackDownloadKeysStorage = TrackDownloadKeysStorage.inst();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		initGUI();
	}

	private void initGUI() {
		try {
			setTitle(language.getString("AddNewDownloadsDialog.title"));
			setSize(800, 500);
			this.setResizable(true);

			// Remove already Downloaded Button
			removeAlreadyDownloadedButton = new JButton(language.getString("AddNewDownloadsDialog.button.removeAlreadyDownloadedButton"));
			removeAlreadyDownloadedButton.setToolTipText(language.getString("AddNewDownloadsDialog.buttonTooltip.removeAlreadyDownloadedButton"));
			removeAlreadyDownloadedButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					removeAlreadyDownloadedButton_actionPerformed(e);
				}
			});

			// Remove already exists Button
			removeAlreadyExistsButton = new JButton(language.getString("AddNewDownloadsDialog.button.removeAlreadyExistsButton"));
			removeAlreadyExistsButton.setToolTipText(language.getString("AddNewDownloadsDialog.buttonTooltip.removeAlreadyExistsButton"));
			removeAlreadyExistsButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					removeAlreadyExistsButton_actionPerformed(e);
				}
			});

			// OK Button
			okButton = new JButton(language.getString("Common.ok"));
			okButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addDownloads = true;
					dispose();
				}
			});

			// Cancel Button
			cancelButton = new JButton(language.getString("Common.cancel"));
			cancelButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addNewDownloadsTableModel.clearDataModel();
					dispose();
				}
			});

			// Button row
			final JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setLayout( new BoxLayout( buttonsPanel, BoxLayout.X_AXIS ));

			buttonsPanel.add( removeAlreadyDownloadedButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( removeAlreadyExistsButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));

			buttonsPanel.add( Box.createHorizontalGlue() );

			buttonsPanel.add( cancelButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( okButton );
			buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

			// Download Table
			addNewDownloadsTableModel = new AddNewDownloadsTableModel();
			addNewDownloadsTable = new SortedTable( addNewDownloadsTableModel );
			addNewDownloadsTable.setRowSelectionAllowed(true);
			addNewDownloadsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			addNewDownloadsTable.setRowHeight(18);
			final JScrollPane scrollPane = new JScrollPane(addNewDownloadsTable);
			scrollPane.setWheelScrollingEnabled(true);

			// main panel
			final JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add( scrollPane, BorderLayout.CENTER );
			mainPanel.add( buttonsPanel, BorderLayout.SOUTH );
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));

			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(mainPanel, null);

			this.initPopupMenu();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public List<FrostDownloadItem> startDialog(List<FrostDownloadItem> frostDownloadItmeList) {
		// load data into table
		this.loadNewDownloadsIntoTable(frostDownloadItmeList);
		setLocationRelativeTo(parentFrame);

		// display table
		setVisible(true); // blocking!

		// return items in table
		frostDownloadItmeList = new LinkedList<FrostDownloadItem>();
		if( addDownloads ) {
			final int numberOfRows = addNewDownloadsTableModel.getRowCount();
			for( int indexPos = 0; indexPos < numberOfRows; indexPos++) {
				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(indexPos);
				frostDownloadItmeList.add( row.getDownloadItem() );
			}
		}

		return frostDownloadItmeList;
	}

	private void loadNewDownloadsIntoTable(final List<FrostDownloadItem> frostDownloadItmeList) {
		this.addNewDownloadsTableModel.clearDataModel();
		for( final FrostDownloadItem froDownloadItem : frostDownloadItmeList) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember = new AddNewDownloadsTableMember(froDownloadItem);
			this.addNewDownloadsTableModel.addRow(addNewDownloadsTableMember);
		}
	}

	private void initPopupMenu() {
		tablePopupMenu = new JSkinnablePopupMenu();

		// Change Download Directory
		final JMenuItem changeDownloadDir = new JMenuItem(language.getString("AddNewDownloadsDialog.button.changeDownloadDir"));
		changeDownloadDir.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				changeDownloadDir_actionPerformed(actionEvent);
			}
		});

		// Remove item from list
		final JMenuItem removeDownload = new JMenuItem(language.getString("AddNewDownloadsDialog.button.removeDownload"));
		removeDownload.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removeDownload_actionPerformed(actionEvent);
			}
		});

		// Change Priority
		final JMenu changePriorityMenu = new JMenu(language.getString("Common.priority.changePriority"));
		final int numberOfPriorities = 7;
		final JMenuItem[] prioItemList = new JMenuItem[numberOfPriorities];

		for(int i = 0; i < numberOfPriorities; i++) {
			final int priority = i;
			prioItemList[priority] = new JMenuItem(language.getString("Common.priority.priority" + priority));
			changePriorityMenu.add(prioItemList[priority]);
			prioItemList[priority].addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					changePriority_actionPerformed(actionEvent, priority);
				}
			});
		}

		// Compose popup menu
		tablePopupMenu.add(changeDownloadDir);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(removeDownload);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(changePriorityMenu);

		this.addNewDownloadsTable.addMouseListener(new TablePopupMenuMouseListener());
	}

	private void removeAlreadyDownloadedButton_actionPerformed(final ActionEvent actionEvent) {
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		for( int indexPos = numberOfRows -1; indexPos >= 0; indexPos--) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember =
				(AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(indexPos);
			if( trackDownloadKeysStorage.searchItemKey( addNewDownloadsTableMember.getDownloadItem().getKey() ) ) {
				addNewDownloadsTableModel.deleteRow(addNewDownloadsTableMember);
			}
		}
		addNewDownloadsTable.clearSelection();
	}

	private void removeAlreadyExistsButton_actionPerformed(final ActionEvent actionEvent) {
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		for( int indexPos = numberOfRows -1; indexPos >= 0; indexPos--) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember =
				(AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(indexPos);
			final FrostDownloadItem frostDownloadItem = addNewDownloadsTableMember.getDownloadItem();
			if(new java.io.File(frostDownloadItem.getDownloadDir() + frostDownloadItem.getFilename()).exists() ) {
				addNewDownloadsTableModel.deleteRow(addNewDownloadsTableMember);
			}
		}
		addNewDownloadsTable.clearSelection();
	}

	private void changeDownloadDir_actionPerformed(final ActionEvent actionEvent) {
		final int[] selectedRows = addNewDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			// Open choose Directory dialog
			final JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(
					new java.io.File(
							Core.frostSettings.getDefaultValue(SettingsClass.DIR_DOWNLOAD)
					)
			);
			chooser.setDialogTitle(language.getString("AddNewDownloadsDialog.changeDirDialog.title"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			// Set new dir for selected items
			for( final int rowIndex : selectedRows ) {
				if( rowIndex >= addNewDownloadsTable.getRowCount() ) {
					continue; // paranoia
				}

				// add the board(s) to board tree and remove it from table
				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIndex);
				row.getDownloadItem().setDownloadDir(chooser.getSelectedFile().toString());
				row.updateComment();
			}
			addNewDownloadsTable.clearSelection();
		}
	}

	private void removeDownload_actionPerformed(final ActionEvent e) {
		final int[] selectedRows = addNewDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			for( int z = selectedRows.length - 1; z > -1; z-- ) {
				final int rowIx = selectedRows[z];

				if( rowIx >= addNewDownloadsTableModel.getRowCount() ) {
					continue; // paranoia
				}

				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIx);
				addNewDownloadsTableModel.deleteRow(row);
			}
			addNewDownloadsTable.clearSelection();
		}
	}

	private void changePriority_actionPerformed(final ActionEvent e, final int priority) {
		final int[] selectedRows = addNewDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			for( int z = selectedRows.length - 1; z > -1; z-- ) {
				final int rowIx = selectedRows[z];

				if( rowIx >= addNewDownloadsTableModel.getRowCount() ) {
					continue; // paranoia
				}

				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIx);
				row.getDownloadItem().setPriority(priority);
			}
			addNewDownloadsTable.clearSelection();
		}
	}

	class AddNewDownloadsTableMember implements TableMember {

		FrostDownloadItem frostDownloadItem;
		String comment;

		public AddNewDownloadsTableMember(final FrostDownloadItem frostDownloadItem){
			this.frostDownloadItem = frostDownloadItem;
		}

		public Object getValueAt(final int column) {
			switch( column ) {
				case 0:
					return frostDownloadItem.getFilename();
				case 1:
					return frostDownloadItem.getKey();
				case 2:
					if( frostDownloadItem.getPriority() == -1 ) {
						frostDownloadItem.setPriority( Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD ));
					}
					final String prio = "Common.priority.priority" + frostDownloadItem.getPriority();
					return language.getString(prio);
				case 3:
					return frostDownloadItem.getDownloadDir();
				case 4:
					if(comment == null) {
						this.updateComment();
					}
					return comment;
				default :
					throw new RuntimeException("Unknown Column pos");
			}
		}

		public void updateComment() {
		    comment = "";

		    // Has this key already been downloaded?
		    if (trackDownloadKeysStorage.searchItemKey( frostDownloadItem.getKey() )) {
				comment += language.getString("AddNewDownloadsDialog.table.alreadyDownloaded");
			}

            // Does target file already exist?
		    if (new java.io.File(frostDownloadItem.getDownloadDir() + frostDownloadItem.getFilename()).exists() ) {
			    if (comment.length() > 0) {
			        comment += ", ";
			    }
			    comment += language.getString("AddNewDownloadsDialog.table.alreadyExists");
			}
		}

		public int compareTo(final TableMember anOther, final int tableColumIndex) {
			final String c1 = (String) getValueAt(tableColumIndex);
			final String c2 = (String) anOther.getValueAt(tableColumIndex);
			return c1.compareToIgnoreCase(c2);
		}

		public FrostDownloadItem getDownloadItem(){
			return frostDownloadItem;
		}
	}

	class TablePopupMenuMouseListener implements MouseListener {
		public void mouseReleased(final MouseEvent event) {
			maybeShowPopup(event);
		}
		public void mousePressed(final MouseEvent event) {
			maybeShowPopup(event);
		}
		public void mouseClicked(final MouseEvent event) {}
		public void mouseEntered(final MouseEvent event) {}
		public void mouseExited(final MouseEvent event) {}

		protected void maybeShowPopup(final MouseEvent e) {
			if( e.isPopupTrigger() ) {
				if( addNewDownloadsTable.getSelectedRowCount() > 0 ) {
					// don't show menu if nothing is selected
					tablePopupMenu.show(addNewDownloadsTable, e.getX(), e.getY());
				}
			}
		}
	}
}
