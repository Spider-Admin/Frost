/*
 * Created on May 13, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.download;

import java.awt.Component;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.*;

import frost.gui.objects.Board;
import frost.util.gui.BooleanCell;
import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.*;

/**
 * @author $Author$
 * @version $Revision$
 */
class DownloadTableFormat extends SortedTableFormat implements LanguageListener {

	/**
	 * This inner class implements the renderer for the column "Size"
	 */
	private class SizeRenderer extends DefaultTableCellRenderer {

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
	 * This inner class implements the comparator for the column "Key"
	 */
	private class KeyComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String key1 = ((FrostDownloadItem) o1).getKey();
			String key2 = ((FrostDownloadItem) o2).getKey();
			if (key1 == null) {
				key1 = "";
			}
			if (key2 == null) {
				key2 = "";
			}
			return key1.compareToIgnoreCase(key2);
		}

	}
	
	/**
	 * This inner class implements the comparator for the column "From"
	 */
	private class FromComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String owner1 = ((FrostDownloadItem) o1).getOwner();
			String owner2 = ((FrostDownloadItem) o2).getOwner();
			if (owner1 == null) {
				owner1 = "";
			}
			if (owner2 == null) {
				owner2 = "";
			}
			return owner1.compareToIgnoreCase(owner2);
		}

	}
	
	/**
	 * This inner class implements the comparator for the column "Source"
	 */
	private class SourceComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Board source1 = ((FrostDownloadItem) o1).getSourceBoard();
			Board source2 = ((FrostDownloadItem) o2).getSourceBoard();
			String name1 = "";
			String name2 = "";
			if (source1 != null) {
				name1 = source1.getBoardName();
			}
			if (source2 != null) {
				name2 = source2.getBoardName();
			}
			return name1.compareToIgnoreCase(name2);
		}

	}
	
	/**
	 * This inner class implements the comparator for the column "Tries"
	 */
	private class TriesComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			int retries1 = ((FrostDownloadItem) o1).getRetries();
			int retries2 = ((FrostDownloadItem) o2).getRetries();
			return new Integer(retries1).compareTo(new Integer(retries2));
		}

	}
	
	/**
	 * This inner class implements the comparator for the column "Blocks"
	 */
	private class BlocksComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
			String blocks1 =
				getBlocksAsString(
					item1.getTotalBlocks(),
					item1.getDoneBlocks(),
					item1.getRequiredBlocks());
			String blocks2 =
				getBlocksAsString(
					item2.getTotalBlocks(),
					item2.getDoneBlocks(),
					item2.getRequiredBlocks());
			return blocks1.compareToIgnoreCase(blocks2); 
		}

	}
	
	/**
	 * This inner class implements the comparator for the column "State"
	 */
	private class StateComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
			String state1 =	getStateAsString(
								item1.getState(),
								item1.getTotalBlocks(),
								item1.getDoneBlocks(),
								item1.getRequiredBlocks());
			String state2 =	getStateAsString(
								item2.getState(),
								item2.getTotalBlocks(),
								item2.getDoneBlocks(),
								item2.getRequiredBlocks());
			return state1.compareToIgnoreCase(state2);
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Age"
	 */
	private class AgeComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String age1 = ((FrostDownloadItem) o1).getFileAge();
			String age2 = ((FrostDownloadItem) o2).getFileAge();
			if (age1 == null) {
				age1 = "";	
			}
			if (age2 == null) {
				age2 = "";	
			}
			return age1.compareToIgnoreCase(age2);
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Size"
	 */
	private class SizeComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Long size1 = ((FrostDownloadItem) o1).getFileSize();
			Long size2 = ((FrostDownloadItem) o2).getFileSize();
			if (size1 == null) {
				size1 = new Long(-1);
			}
			if (size2 == null) {
				size2 = new Long(-1);
			}	
			return size1.compareTo(size2);
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Filename"
	 */
	private class FileNameComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
			return item1.getFileName().compareToIgnoreCase(item2.getFileName());
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Enabled"
	 */
	private class EnabledComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
			return item1.getEnableDownload().equals(item2.getEnableDownload()) ? 0 : 1 ;
		}
	}
		
	private Language language;
	
	private final static int COLUMN_COUNT = 10;
	
	String stateWaiting;
	String stateTrying;
	String stateFailed;
	String stateDone;
	String stateRequesting;
	String stateRequested;
	String stateDecoding;
	
	String offline;
	String unknown;
	String anonymous;

	/**
	 *
	 */
	public DownloadTableFormat() {
		super(COLUMN_COUNT);

		language = Language.getInstance();
		language.addLanguageListener(this);
		refreshLanguage();

		setComparator(new EnabledComparator(), 0);
		setComparator(new FileNameComparator(), 1);
		setComparator(new SizeComparator(), 2);
		setComparator(new AgeComparator(), 3);
		setComparator(new StateComparator(), 4);
		setComparator(new BlocksComparator(), 5);
		setComparator(new TriesComparator(), 6);
		setComparator(new SourceComparator(), 7);
		setComparator(new FromComparator(), 8);
		setComparator(new KeyComparator(), 9);
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		setColumnName(0, language.getString("DownloadTableFormat.Enabled"));
		setColumnName(1, language.getString("Filename"));
		setColumnName(2, language.getString("Size"));
		setColumnName(3, language.getString("Age"));
		setColumnName(4, language.getString("State"));
		setColumnName(5, language.getString("Blocks"));
		setColumnName(6, language.getString("Tries"));
		setColumnName(7, language.getString("Source"));
		setColumnName(8, language.getString("From"));
		setColumnName(9, language.getString("Key"));
		
		stateWaiting = language.getString("Waiting");
		stateTrying = language.getString("Trying");
		stateFailed = language.getString("Failed");
		stateDone = language.getString("Done");
		stateRequesting = language.getString("Requesting");
		stateRequested = language.getString("Requested");
		stateDecoding = language.getString("Decoding segment") + "...";
	
		offline = language.getString("FrostSearchItemObject.Offline");
		unknown = language.getString("Unknown");
		anonymous = language.getString("FrostSearchItemObject.Anonymous");
		
		refreshColumnNames();
	}

	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		refreshLanguage();	
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getCellValue(frost.util.model.ModelItem, int)
	 */
	public Object getCellValue(ModelItem item, int columnIndex) {
		FrostDownloadItem downloadItem = (FrostDownloadItem) item;
		switch (columnIndex) {

			case 0 : //Enabled
				return downloadItem.getEnableDownload();

			case 1 : //Filename
				return downloadItem.getFileName();

			case 2 : //Size
				if (downloadItem.getFileSize() == null) {
					return unknown;
				} else {
					return downloadItem.getFileSize();
				}

			case 3 : //Age
				if (downloadItem.getFileAge() == null) {
					return offline;
				} else {
					return downloadItem.getFileAge();
				}

			case 4 : //State
				return getStateAsString(
					downloadItem.getState(),
					downloadItem.getTotalBlocks(),
					downloadItem.getDoneBlocks(),
					downloadItem.getRequiredBlocks());

			case 5 : //Blocks
				return getBlocksAsString(
					downloadItem.getTotalBlocks(),
					downloadItem.getDoneBlocks(),
					downloadItem.getRequiredBlocks());

			case 6 : //Tries
				return new Integer(downloadItem.getRetries());

			case 7 : //Source
				if (downloadItem.getSourceBoard() == null) {
					return "";
				} else {
					return downloadItem.getSourceBoard();
				}

			case 8 : //From
				if (downloadItem.getOwner() == null) {
					return anonymous;
				} else {
					return downloadItem.getOwner();
				}

			case 9 : //Key
				if (downloadItem.getKey() == null) {
					return " ?";
				} else {
					return downloadItem.getKey();
				}

			default :
				return "**ERROR**";
		}
	}
	
	/**
	 * @param totalBlocks
	 * @param doneBlocks
	 * @param requiredBlocks
	 * @return
	 */
	private String getBlocksAsString(int totalBlocks, int doneBlocks, int requiredBlocks) {
		if (totalBlocks == 0) {
			return "";
		} else {
			return (doneBlocks + " / " + requiredBlocks + " (" + totalBlocks + ")");
		}
	}

	/**
	 * @param state
	 * @param totalBlocks
	 * @param doneBlocks
	 * @param requiredBlocks
	 * @return
	 */
	private String getStateAsString(int state, int totalBlocks, int doneBlocks, int requiredBlocks) {
		switch (state) {
			case FrostDownloadItem.STATE_WAITING :
				return stateWaiting;

			case FrostDownloadItem.STATE_TRYING :
				return stateTrying;

			case FrostDownloadItem.STATE_FAILED :
				return stateFailed;

			case FrostDownloadItem.STATE_DONE :
				return stateDone;

			case FrostDownloadItem.STATE_REQUESTING :
				return stateRequesting;

			case FrostDownloadItem.STATE_REQUESTED :
				return stateRequested;
				
			case FrostDownloadItem.STATE_DECODING :
				return stateDecoding;
			
			case FrostDownloadItem.STATE_PROGRESS :
				if (totalBlocks > 0) {
					return (int) ((doneBlocks * 100) / requiredBlocks) + "%";
				} else {
					return "0%";
				}
				
			default :
				return "**ERROR**";
		}
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getColumnNumbers(int)
	 */
	public int[] getColumnNumbers(int fieldID) {
		switch (fieldID) {
			case FrostDownloadItem.FIELD_ID_DONE_BLOCKS :
				return new int[] {4, 5};	//State, Blocks
			
			case FrostDownloadItem.FIELD_ID_ENABLED :
				return new int[] {0};	//Enabled
			
			case FrostDownloadItem.FIELD_ID_FILE_AGE :
				return new int[] {3};	//Age
			
			case FrostDownloadItem.FIELD_ID_FILE_NAME :
				return new int[] {1};	//Filename
			
			case FrostDownloadItem.FIELD_ID_FILE_SIZE :
				return new int[] {2};	//Size
			
			case FrostDownloadItem.FIELD_ID_KEY :
				return new int[] {9};	//Key
			
			case FrostDownloadItem.FIELD_ID_OWNER :
				return new int[] {8};	//From
			
			case FrostDownloadItem.FIELD_ID_REQUIRED_BLOCKS :
				return new int[] {4, 5};	//State, Blocks
			
			case FrostDownloadItem.FIELD_ID_RETRIES :
				return new int[] {6};	//Tries
			
			case FrostDownloadItem.FIELD_ID_SHA1 :
				return new int[] {};	//None
			
			case FrostDownloadItem.FIELD_ID_STATE :
				return new int[] {4};	//State
			
			case FrostDownloadItem.FIELD_ID_SOURCE_BOARD :
				return new int[] {7};	//Source
			
			case FrostDownloadItem.FIELD_ID_TOTAL_BLOCKS :
				return new int[] {4, 5};	//State, Blocks

			default :
				return new int[] {};
		}
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(frost.util.model.gui.ModelTable)
	 */
	public void customizeTable(ModelTable modelTable) {
		super.customizeTable(modelTable);
		
		// Sets the relative widths of the columns
		TableColumnModel columnModel = modelTable.getTable().getColumnModel();
		int[] widths = { 30, 170, 80, 70, 80, 85, 25, 60, 60, 30 };
		for (int i = 0; i < widths.length; i++) { // col 0 default width
			columnModel.getColumn(i).setPreferredWidth(widths[i]);
		}
		
		// Column "Enabled"
		columnModel.getColumn(0).setCellRenderer(BooleanCell.RENDERER);
		columnModel.getColumn(0).setCellEditor(BooleanCell.EDITOR);
		setColumnEditable(0, true);
		
		//Column "Size"
		columnModel.getColumn(2).setCellRenderer(new SizeRenderer());
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#setCellValue(java.lang.Object, frost.util.model.ModelItem, int)
	 */
	public void setCellValue(Object value, ModelItem item, int columnIndex) {
		FrostDownloadItem downloadItem = (FrostDownloadItem) item;
		switch (columnIndex) {

			case 0 : //Enabled
				Boolean valueBoolean = (Boolean) value;
				downloadItem.setEnableDownload(valueBoolean);
				break;

			default :
				super.setCellValue(value, item, columnIndex);
		}
	}

}
