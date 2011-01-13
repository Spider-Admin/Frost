package frost.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import frost.Core;
import frost.SettingsClass;
import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.upload.FrostUploadItem;
import frost.gui.model.SortedTableModel;
import frost.gui.model.TableMember;
import frost.util.FileAccess;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.Language;

@SuppressWarnings("serial")
public class AddNewUploadsDialog extends JFrame {

	private final Language language;

	private AddNewUploadsTableModel addNewUploadsTableModel;
	private AddNewUploadsTable addNewUploadsTable;

	private final Frame parentFrame;

	/**
	 * If true, uploads in model will be added to upload list when closing the window
	 */
	
	public AddNewUploadsDialog(final JFrame frame) {
		parentFrame = frame;
		language = Language.getInstance();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		initGui();
	}
	
	
	private void initGui() {
		try {
			setTitle(language.getString("AddNewUploadsDialog.title"));
			
			int width = (int) (parentFrame.getWidth() * 0.75);
			int height = (int) (parentFrame.getHeight() * 0.75);

			if( width < 1000 ) {
				Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

				if( screenSize.width > 1300 ) {
					width = 1200;

				} else if( screenSize.width > 1000 ) {
					width = (int) (parentFrame.getWidth() * 0.99);
				}
			}

			if( height < 500 ) {
				Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

				if( screenSize.width > 900 ) {
					height = 800;
				} else {
					height = (int) (screenSize.width * 0.85);
				}
			}
			
			setSize(width, height);
			this.setResizable(true);
			
			setIconImage(MiscToolkit.loadImageIcon("/data/toolbar/go-up.png").getImage());
			
			
			// Add Button
			final JButton addButton = new JButton(language.getString("Common.add"));
			addButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					List<FrostUploadItem> frostUploadItmeList = addFileChooser();
					for( final FrostUploadItem frotUploadItem : frostUploadItmeList) {
						addNewUploadsTableModel.addRow(new AddNewUploadsTableMember(frotUploadItem));
					}
				}
			});
			
			// Remove selected button
			final JButton removeSelectedButton = new JButton(language.getString("AddNewUploadsDialog.button.removeSelected"));
			removeSelectedButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addNewUploadsTable.removeSelected();
				}
			});

			// Remove but selected button
			final JButton removeButSeelctedButton = new JButton(language.getString("AddNewUploadsDialog.button.removeButSelected"));
			removeButSeelctedButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addNewUploadsTable.removeButSelected();
				}
			});
			
			// OK Button
			final JButton okButton = new JButton(language.getString("Common.ok"));
			okButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					FileTransferManager.inst().getUploadManager().getModel().addUploadItemList(getUploads());
					
					dispose();
				}
			});

			// Cancel Button
			final JButton cancelButton = new JButton(language.getString("Common.cancel"));
			cancelButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addNewUploadsTableModel.clearDataModel();
					dispose();
				}
			});

			// Button row
			final JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setLayout( new BoxLayout( buttonsPanel, BoxLayout.X_AXIS ));

			buttonsPanel.add( addButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( removeSelectedButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( removeButSeelctedButton );

			buttonsPanel.add( Box.createHorizontalGlue() );

			buttonsPanel.add( cancelButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( okButton );
			buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
			
			
			// Upload Table
			addNewUploadsTableModel = new AddNewUploadsTableModel();
			addNewUploadsTable = new AddNewUploadsTable( addNewUploadsTableModel );
			addNewUploadsTable.setRowSelectionAllowed(true);
			addNewUploadsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			addNewUploadsTable.setRowHeight(18);
			final JScrollPane scrollPane = new JScrollPane(addNewUploadsTable);
			scrollPane.setWheelScrollingEnabled(true);

			// Main panel
			final JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add( scrollPane, BorderLayout.CENTER );
			mainPanel.add( buttonsPanel, BorderLayout.SOUTH );
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));

			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(mainPanel, null);
			
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void startDialog() {
		// Open file picker
		List<FrostUploadItem> frostUploadItmeList = addFileChooser();
		
		// load data into table
		this.loadNewUploadsIntoTable(frostUploadItmeList);
		setLocationRelativeTo(parentFrame);

		// display table
		setVisible(true); // blocking!
		
	}
	
	private void loadNewUploadsIntoTable(final List<FrostUploadItem> frostUploadItmeList) {
		this.addNewUploadsTableModel.clearDataModel();
		for( final FrostUploadItem frotUploadItem : frostUploadItmeList) {
			this.addNewUploadsTableModel.addRow(new AddNewUploadsTableMember(frotUploadItem));
		}
	}
	
	private class AddNewUploadsTableMember implements TableMember {
		
		FrostUploadItem frostUploadItem;
		
		public AddNewUploadsTableMember(final FrostUploadItem frostUploadItem){
			this.frostUploadItem = frostUploadItem;
		}
		
		@Override
		public Comparable<?> getValueAt(final int column) {
			try {
				switch( column ) {
					case 0:
						return frostUploadItem.getFilename();
					case 1:
						return frostUploadItem.getFile().getCanonicalPath();
					case 2:
						return new Long(frostUploadItem.getFileSize());
					default :
						throw new RuntimeException("Unknown Column pos");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int compareTo(final TableMember anOther, final int tableColumIndex) {
			final String c1 = (String) getValueAt(tableColumIndex);
			final String c2 = (String) anOther.getValueAt(tableColumIndex);
			return c1.compareToIgnoreCase(c2);
		}

		public FrostUploadItem getUploadItem(){
			return frostUploadItem;
		}
	}
	
	private List<FrostUploadItem> getUploads() {
		List<FrostUploadItem> frostUploadItmeList = new LinkedList<FrostUploadItem>();
		final int numberOfRows = addNewUploadsTableModel.getRowCount();
		for( int indexPos = 0; indexPos < numberOfRows; indexPos++) {
			frostUploadItmeList.add( addNewUploadsTableModel.getRow(indexPos).getUploadItem() );
		}
		return frostUploadItmeList;
	}
	
	
	private List<FrostUploadItem> addFileChooser() {
		
		List<FrostUploadItem> frostUploadItemList = new ArrayList<FrostUploadItem>();
		
		final JFileChooser fc = new JFileChooser(Core.frostSettings.getValue(SettingsClass.DIR_LAST_USED));
		fc.setDialogTitle(language.getString("AddNewUploadsDialog.filechooser.title"));
		fc.setFileHidingEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		fc.setPreferredSize(new Dimension(600, 400));

		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return frostUploadItemList;
		}
		final File[] selectedFiles = fc.getSelectedFiles();
		if( selectedFiles == null ) {
			return frostUploadItemList;
		}

		final List<File> uploadFileItems = new LinkedList<File>();
		for( final File element : selectedFiles ) {
			// collect all choosed files + files in all choosed directories
			uploadFileItems.addAll( FileAccess.getAllEntries(element) );
		}

		// remember last upload dir
		if (uploadFileItems.size() > 0) {
			final File file = uploadFileItems.get(0);
			Core.frostSettings.setValue(SettingsClass.DIR_LAST_USED, file.getParent());
		}

		for(final File file : uploadFileItems ) {
			frostUploadItemList.add( new FrostUploadItem(file));
		}

		return frostUploadItemList;
	}
	
	

	private static class AddNewUploadsTableModel extends SortedTableModel<AddNewUploadsTableMember>{ 
		private Language language = null;

		protected final static String columnNames[] = new String[3];

		protected final static Class<?> columnClasses[] =  {
			String.class,
			String.class,
			Long.class
		};

		public AddNewUploadsTableModel() {
			super();
			assert columnClasses.length == columnNames.length;
			language = Language.getInstance();
			refreshLanguage();
		}

		private void refreshLanguage() {
			columnNames[0] = language.getString("AddNewUploadsDialog.table.name");
			columnNames[1] = language.getString("AddNewUploadsDialog.table.path");
			columnNames[2] = language.getString("AddNewUploadsDialog.table.size");
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
	
	private class AddNewUploadsTable extends SortedTable<AddNewUploadsTableMember> {
		
		public AddNewUploadsTable(SortedTableModel<AddNewUploadsTableMember> model) {
			super(model);
			this.setIntercellSpacing(new Dimension(5, 1));
		}

		public String getToolTipText(final MouseEvent mouseEvent) {
			final java.awt.Point point = mouseEvent.getPoint();
			final int rowIndex = rowAtPoint(point);
			final int colIndex = columnAtPoint(point);
			final int realColumnIndex = convertColumnIndexToModel(colIndex);
			final AddNewUploadsTableModel tableModel = (AddNewUploadsTableModel) getModel();
			
			switch(realColumnIndex){
				case 0:
				case 1:
				case 2:
					return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
				default:
					assert false;
			}
			return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
		}
		
	}
	
	
}
