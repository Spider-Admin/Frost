/*
  ManageTrackedDownloads.java / Frost
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
import java.io.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import frost.*;
import frost.fcp.*;
import frost.gui.model.*;
import frost.storage.perst.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class ManageTrackedDownloads extends javax.swing.JDialog {
	private final Language language;
	private final TrackDownloadKeysStorage trackDownloadKeysStorage;

	private TrackedDownloadsModel trackedDownloadsModel;
	private TrackedDownloadsTable trackedDownloadsTable;

	private JTextField maxAgeTextField;
	private JButton maxAgeButton;
	private JButton addKeysButton;
	private JButton closeButton;

	private JSkinnablePopupMenu tablePopupMenu;

	private static final long serialVersionUID = 1L;

	public ManageTrackedDownloads(final JFrame frame) {
		super(frame);
		setModal(true);
		language = Language.getInstance();
		trackDownloadKeysStorage = TrackDownloadKeysStorage.inst();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		initGUI();
	}

	private void initGUI() {
		try {
			setTitle(language.getString("ManageDownloadTrackingDialog.title"));
			setSize(800, 600);
			this.setResizable(true);

			// Max Age
			final JLabel maxAgeLabel = new JLabel(language.getString("ManageDownloadTrackingDialog.button.maxAge"));
			maxAgeTextField = new JTextField(6);
			maxAgeTextField.setText("100");
			maxAgeTextField.setMaximumSize(new Dimension(30,20));
			maxAgeButton = new JButton(language.getString("ManageDownloadTrackingDialog.button.maxAgeButton"));
			maxAgeButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					maxAgeButton_actionPerformed(e);
				}
			});
			maxAgeButton.setToolTipText(language.getString("ManageDownloadTrackingDialog.buttonTooltip.maxAgeButton"));

			// Load files
			addKeysButton = new JButton(language.getString("ManageDownloadTrackingDialog.button.addKeys"));
			addKeysButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addKeysButton_actionPerformed(e);
				}
			});
			addKeysButton.setToolTipText(language.getString("ManageDownloadTrackingDialog.buttonTooltip.addKeys"));

			// Close Button
			closeButton = new JButton(language.getString("Common.close"));
			closeButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					dispose();
				}
			});

			// Button row
			final JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setLayout( new BoxLayout( buttonsPanel, BoxLayout.X_AXIS ));

			buttonsPanel.add( maxAgeLabel );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( maxAgeTextField );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( maxAgeButton );

			buttonsPanel.add( Box.createHorizontalGlue() );

			buttonsPanel.add( addKeysButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(20,3)));
			buttonsPanel.add( closeButton );

			// Download Table
			trackedDownloadsModel = new TrackedDownloadsModel();
			trackedDownloadsTable = new TrackedDownloadsTable( trackedDownloadsModel );
			trackedDownloadsTable.setRowSelectionAllowed(true);
			trackedDownloadsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			trackedDownloadsTable.setRowHeight(18);
			final JScrollPane scrollPane = new JScrollPane(trackedDownloadsTable);
			scrollPane.setWheelScrollingEnabled(true);

			// main panel
			final JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add( scrollPane, BorderLayout.CENTER );
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));

			this.getContentPane().setLayout(new BorderLayout());
			mainPanel.add( buttonsPanel, BorderLayout.SOUTH );
			this.getContentPane().add(mainPanel, null);

			this.initPopupMenu();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void initPopupMenu() {
		tablePopupMenu = new JSkinnablePopupMenu();

		// remove
		final JMenuItem removeMenuItem = new JMenuItem(language.getString("ManageDownloadTrackingDialog.button.remove"));
		removeMenuItem.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removeMenuItem_actionPerformed(actionEvent);
			}
		});

		// remove all from same Board
		final JMenuItem removeSameBoardMenuItem = new JMenuItem(language.getString("ManageDownloadTrackingDialog.button.removeSameBoard"));
		removeSameBoardMenuItem.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removeSameBoardMenuItem_actionPerformed(actionEvent);
			}
		});

		// Compose popup menu
		tablePopupMenu.add(removeMenuItem);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(removeSameBoardMenuItem);

		this.trackedDownloadsTable.addMouseListener(new TablePopupMenuMouseListener());
	}

	public void startDialog(final Frame owner) {
		this.loadTrackedDownloadsIntoTable();
		setLocationRelativeTo(owner);

		setVisible(true); // blocking!
	}

	private void loadTrackedDownloadsIntoTable() {
		this.trackedDownloadsModel.clearDataModel();
		for( final TrackDownloadKeys trackDownloadkey : trackDownloadKeysStorage.getDownloadKeyList()) {
			final TrackedDownloadTableMember trackedDownloadTableMember = new TrackedDownloadTableMember(trackDownloadkey);
			this.trackedDownloadsModel.addRow(trackedDownloadTableMember);
		}
	}

	private void removeMenuItem_actionPerformed(final ActionEvent e) {
		final int[] selectedRows = trackedDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			for( int z = selectedRows.length - 1; z > -1; z-- ) {
				final int rowIx = selectedRows[z];

				if( rowIx >= trackedDownloadsModel.getRowCount() ) {
					continue; // paranoia
				}

				final TrackedDownloadTableMember row = (TrackedDownloadTableMember) trackedDownloadsModel.getRow(rowIx);
				trackDownloadKeysStorage.removeItemByKey(row.getTrackDownloadKeys().getChkKey());
				trackedDownloadsModel.deleteRow(row);
			}
			trackedDownloadsTable.clearSelection();
		}
	}

	private void removeSameBoardMenuItem_actionPerformed(final ActionEvent e) {
		final int selectedRowIdx = trackedDownloadsTable.getSelectedRow();
		if( selectedRowIdx < 0 || selectedRowIdx >= trackedDownloadsModel.getRowCount()) {
			return;
		}
		final TrackedDownloadTableMember selectedRow = (TrackedDownloadTableMember) trackedDownloadsModel.getRow(selectedRowIdx);
		if( selectedRow == null ) {
			return;
		}
		final String boardName = selectedRow.getTrackDownloadKeys().getBoardName();

		for( int z = trackedDownloadsModel.getRowCount() -1 ; z >= 0; z--) {
			final TrackedDownloadTableMember row = (TrackedDownloadTableMember) trackedDownloadsModel.getRow(z);
			if( boardName.compareTo(row.getTrackDownloadKeys().getBoardName()) == 0 ) {
				trackDownloadKeysStorage.removeItemByKey(row.getTrackDownloadKeys().getChkKey());
				trackedDownloadsModel.deleteRow(row);
			}
		}
		trackedDownloadsTable.clearSelection();
	}

	private void addKeysButton_actionPerformed(final ActionEvent e) {
		// Open choose Directory dialog
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(
			new java.io.File(
				Core.frostSettings.getDefaultValue(SettingsClass.DIR_DOWNLOAD)
			)
		);
		fileChooser.setDialogTitle(language.getString("AddNewDownloadsDialog.changeDirDialog.title"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		try {
			final File selectedFile =fileChooser.getSelectedFile();
			final FileInputStream fileInputStrem = new FileInputStream(selectedFile);
			final DataInputStream dataInputStream = new DataInputStream(fileInputStrem);
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
			String strLine;
			while ((strLine = bufferedReader.readLine()) != null)   {
				if( strLine.startsWith("CHK@") && FreenetKeys.isValidKey(strLine) ) {
					final String fileName = strLine.substring(strLine.lastIndexOf("/")+1);
					trackDownloadKeysStorage.storeItem(new TrackDownloadKeys(strLine, fileName, "", selectedFile.length(), System.currentTimeMillis()));
				}
			}
		} catch(final FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		loadTrackedDownloadsIntoTable();
	}

	private void maxAgeButton_actionPerformed(final ActionEvent e) {
		int max_age = 4;
		try {
			max_age = Integer.parseInt(this.maxAgeTextField.getText());
		} catch( final NumberFormatException ex ) {
			return;
		}

		if( max_age < 0) {
			return;
		}

		trackDownloadKeysStorage.cleanupTable(max_age);
		loadTrackedDownloadsIntoTable();
	}
	
	private static class TrackedDownloadsModel extends SortedTableModel<TrackedDownloadTableMember> {
		private static final long serialVersionUID = 1L;

		private Language language = null;

		protected final static String columnNames[] = new String[5];

		protected final static Class<?> columnClasses[] =  {
			String.class,
			String.class,
			String.class,
			String.class,
			String.class
		};

		public TrackedDownloadsModel() {
			super();
			assert columnClasses.length == columnNames.length;
			language = Language.getInstance();
			refreshLanguage();
		}

		private void refreshLanguage() {
			columnNames[0] = language.getString("ManageDownloadTrackingDialog.table.name");
			columnNames[1] = language.getString("ManageDownloadTrackingDialog.table.key");
			columnNames[2] = language.getString("ManageDownloadTrackingDialog.table.board");
			columnNames[3] = language.getString("ManageDownloadTrackingDialog.table.size");
			columnNames[4] = language.getString("ManageDownloadTrackingDialog.table.finished");
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public String getColumnName(int column) {
			if( column >= 0 && column < columnNames.length )
				return columnNames[column];
			return null;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Class<?> getColumnClass(int column) {
			if( column >= 0 && column < columnClasses.length )
				return columnClasses[column];
			return null;
		}
	}
	

	private class TrackedDownloadTableMember implements TableMember {

		TrackDownloadKeys trackDownloadKey;

		public TrackedDownloadTableMember(final TrackDownloadKeys trackDownloadkey){
			this.trackDownloadKey = trackDownloadkey;
		}

		@SuppressWarnings("unchecked")
		public Comparable getValueAt(final int column) {
			switch( column ) {
				case 0:
					return trackDownloadKey.getFileName();
				case 1:
					return trackDownloadKey.getChkKey();
				case 2:
					return trackDownloadKey.getBoardName();
				case 3:
					return FormatterUtils.formatSize(trackDownloadKey.getFileSize());
				case 4:
					final long date = trackDownloadKey.getDownloadFinishedTime();
					return new StringBuilder()
					.append(DateFun.FORMAT_DATE_VISIBLE.print(date))
					.append(" ")
					.append(DateFun.FORMAT_TIME_VISIBLE.print(date))
					.toString();
				default :
					throw new RuntimeException("Unknown Column pos");
			}
		}

		public TrackDownloadKeys getTrackDownloadKeys() {
			return this.trackDownloadKey;
		}

		public int compareTo(final TableMember anOther, final int tableColumIndex) {
			final String c1 = (String) getValueAt(tableColumIndex);
			final String c2 = (String) anOther.getValueAt(tableColumIndex);
			return c1.compareToIgnoreCase(c2);
		}
	}

	private class TablePopupMenuMouseListener implements MouseListener {
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
				if( trackedDownloadsTable.getSelectedRowCount() > 0 ) {
					tablePopupMenu.show(trackedDownloadsTable, e.getX(), e.getY());
				}
			}
		}
	}
	
	
	private class TrackedDownloadsTable extends SortedTable<TrackedDownloadTableMember> {
		private static final long serialVersionUID = 1L;
		
		final TableCellRenderer sizeColumnRenderer;

		public TrackedDownloadsTable(final TrackedDownloadsModel trackDownloadsModel) {
			super(trackDownloadsModel);
			this.setIntercellSpacing(new Dimension(5, 1));
			
			sizeColumnRenderer = new SizeColumnTableCellRenderer();
		}

		public String getToolTipText(final MouseEvent mouseEvent) {
			final java.awt.Point point = mouseEvent.getPoint();
			final int rowIndex = rowAtPoint(point);
			final int colIndex = columnAtPoint(point);
			final int realColumnIndex = convertColumnIndexToModel(colIndex);
			final TableModel tableModel = getModel();
			return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
		}
		
		public TableCellRenderer getCellRenderer(final int row, final int column) {
			if(column == 3) {
				return sizeColumnRenderer;
			}
			return super.getCellRenderer(row, column);
		}
		
		private class SizeColumnTableCellRenderer extends JLabel implements TableCellRenderer {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(final JTable table,
					final Object value, final boolean isSelected, final boolean hasFocus,
					final int row, final int column) {
				this.setText(value.toString());
				this.setHorizontalAlignment(SwingConstants.RIGHT);
				return this;
			}
		}
	}
}
