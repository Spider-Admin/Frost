/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.upload;

import java.awt.*;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.*;

import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
class UploadTableFormat extends SortedTableFormat implements LanguageListener {
	/**
	 * This inner class implements the renderer for the column "Name"
	 */
	private class NameRenderer extends DefaultTableCellRenderer {

		private SortedModelTable modelTable;

		/**
		 * 
		 */
		public NameRenderer(SortedModelTable newModelTable) {
			super();
			modelTable = newModelTable;
		}
			
		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			ModelItem item = modelTable.getItemAt(row); //It may be null
			if (item != null) {
				FrostUploadItem uploadItem = (FrostUploadItem) item;
				if (uploadItem.getSHA1() != null) {
					Font font = getFont();
					setFont(font.deriveFont(Font.BOLD));
				}
			}
			return this;
		}

}
	
	/**
	 * This inner class implements the renderer for the column "FileSize"
	 */
	private class FileSizeRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(SwingConstants.RIGHT);
			// col is right aligned, give some space to next column
			setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 3));
			return this;
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Name"
	 */
	private class NameComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostUploadItem item1 = (FrostUploadItem) o1;
			FrostUploadItem item2 = (FrostUploadItem) o2;
			return item1.getFileName().compareToIgnoreCase(item2.getFileName());
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Last Upload"
	 */
	private class StateComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostUploadItem item1 = (FrostUploadItem) o1;
			FrostUploadItem item2 = (FrostUploadItem) o2;
			return getStateAsString(item1, item1.getState()).
						compareToIgnoreCase(getStateAsString(item2, item2.getState()));
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Path"
	 */
	private class PathComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostUploadItem item1 = (FrostUploadItem) o1;
			FrostUploadItem item2 = (FrostUploadItem) o2;
			return item1.getFilePath().compareToIgnoreCase(item2.getFilePath());
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Destination"
	 */
	private class DestinationComparator implements Comparator {

		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String boardName1 = ((FrostUploadItem) o1).getTargetBoard().getBoardName();
			String boardName2 = ((FrostUploadItem) o2).getTargetBoard().getBoardName();
			return boardName1.compareToIgnoreCase(boardName2);
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Key"
	 */
	private class KeyComparator implements Comparator {

		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String key1 = ((FrostUploadItem) o1).getKey();
			String key2 = ((FrostUploadItem) o2).getKey();
			if (key1 == null) {
				key1 = unknown;
			}
			if (key2 == null) {
				key2 = unknown;
			}
			return key1.compareToIgnoreCase(key2);
		}

	}
	
	/**
	 * This inner class implements the comparator for the column "FileSize"
	 */
	private class FileSizeComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostUploadItem item1 = (FrostUploadItem) o1;
			FrostUploadItem item2 = (FrostUploadItem) o2;
			return item1.getFileSize().compareTo(item2.getFileSize());
		}
	}

	private UpdatingLanguageResource languageResource;

	private final static int COLUMN_COUNT = 6;
	
	private String stateUploadedNever;
	private String stateRequested;
	private String stateUploading;
	private String stateEncodingRequested;
	private String stateEncoding;
	
	private String unknown;

	/**
	 * 
	 */
	public UploadTableFormat(UpdatingLanguageResource newLanguageResource) {
		super(COLUMN_COUNT);
		
		languageResource = newLanguageResource;
		languageResource.addLanguageListener(this);
		refreshLanguage();
		
		setComparator(new NameComparator(), 0);
		setComparator(new FileSizeComparator(), 1);
		setComparator(new StateComparator(), 2);
		setComparator(new PathComparator(), 3);
		setComparator(new DestinationComparator(), 4);
		setComparator(new KeyComparator(), 5);
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		setColumnName(0, languageResource.getString("Filename"));
		setColumnName(1, languageResource.getString("Size"));
		setColumnName(2, languageResource.getString("Last upload"));
		setColumnName(3, languageResource.getString("Path"));
		setColumnName(4, languageResource.getString("Destination"));
		setColumnName(5, languageResource.getString("Key"));
		
		stateUploadedNever = languageResource.getString("Never");
		stateRequested = languageResource.getString("Requested");
		stateUploading = languageResource.getString("Uploading");
		stateEncodingRequested = languageResource.getString("Encode requested");
		stateEncoding = languageResource.getString("Encoding file") + "...";
		unknown = languageResource.getString("Unknown");
		
		refreshColumnNames();
	}
	
	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getCellValue(frost.util.model.ModelItem, int)
	 */
	public Object getCellValue(ModelItem item, int columnIndex) {
		FrostUploadItem uploadItem = (FrostUploadItem) item;
		switch (columnIndex) {
			case 0 :	//Filename
				return uploadItem.getFileName();
								
			case 1 :	//Size
				return uploadItem.getFileSize();
				
			case 2 :	//Last upload
				return getStateAsString(uploadItem, uploadItem.getState());
				
			case 3 :	//Path
				return uploadItem.getFilePath();
				
			case 4 :	//Destination
				return uploadItem.getTargetBoard();
				
			case 5 :	//Key
				if (uploadItem.getKey() == null) {
					return unknown;
				} else {
					return uploadItem.getKey();
				}
			default: 
				return "**ERROR**";
		}	
	}

	/**
	 * @param i
	 * @return
	 */
	private String getStateAsString(FrostUploadItem item, int state) {
		switch (state) {
			case FrostUploadItem.STATE_REQUESTED :
				return stateRequested;

			case FrostUploadItem.STATE_UPLOADING :
				return stateUploading;

			case FrostUploadItem.STATE_PROGRESS :
				return getUploadProgress(item.getTotalBlocks(), item.getDoneBlocks());

			case FrostUploadItem.STATE_ENCODING_REQUESTED :
				return stateEncodingRequested;

			case FrostUploadItem.STATE_ENCODING :
				return stateEncoding;

			case FrostUploadItem.STATE_IDLE :
				if (item.getLastUploadDate() == null) {
					return stateUploadedNever;
				} else {
					return item.getLastUploadDate();
				}
			default :
				return "**ERROR**";
		}
	}

	/**
	 * @param item
	 * @return
	 */
	private String getUploadProgress(int totalBlocks, int doneBlocks) {
		int percentDone = 0;

		if (totalBlocks > 0) {
			percentDone = (int) ((doneBlocks * 100) / totalBlocks);
		}
		return (doneBlocks + " / " + totalBlocks + " (" + percentDone + "%)");
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(frost.util.model.gui.ModelTable)
	 */
	public void customizeTable(ModelTable modelTable) {
		super.customizeTable(modelTable);
		
		//Sets the relative widths of the columns
		TableColumnModel columnModel = modelTable.getTable().getColumnModel();
		int[] widths = { 250, 80, 80, 80, 80 };
		for (int i = 0; i < widths.length; i++) {
			columnModel.getColumn(i).setPreferredWidth(widths[i]);
		}
		
		// Column "Name"
		columnModel.getColumn(0).setCellRenderer(new NameRenderer((SortedModelTable) modelTable));
		
		// Column "Size"
		columnModel.getColumn(1).setCellRenderer(new FileSizeRenderer());		
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getColumnNumber(int)
	 */
	public int[] getColumnNumbers(int fieldID) {
		switch (fieldID) {
			case FrostUploadItem.FIELD_ID_DONE_BLOCKS :
				return new int[] {2};	//Last upload
				
			case FrostUploadItem.FIELD_ID_FILE_NAME :
				return new int[] {0};	//Filename
				
			case FrostUploadItem.FIELD_ID_FILE_PATH :
				return new int[] {3};	//Path
			
			case FrostUploadItem.FIELD_ID_FILE_SIZE :
				return new int[] {1};	//Size
			
			case FrostUploadItem.FIELD_ID_KEY :
				return new int[] {5};	//Key
				
			case FrostUploadItem.FIELD_ID_LAST_UPLOAD_DATE :
				return new int[] {2};	//Last upload
			
			case FrostUploadItem.FIELD_ID_SHA1 :
				return new int[] {0};	//Filename
					
			case FrostUploadItem.FIELD_ID_STATE :
				return new int[] {2};	//Last upload
				
			case FrostUploadItem.FIELD_ID_TARGET_BOARD :
				return new int[] {4};	//Destination
				
			case FrostUploadItem.FIELD_ID_TOTAL_BLOCKS :
				return new int[] {2};	//Last upload
				
			default :	
				return new int[] {};
		}
	}

	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		refreshLanguage();			
	}

}
