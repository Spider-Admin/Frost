/*
 * Created on May 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.search;

import java.awt.*;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.*;

import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class SearchTableFormat extends SortedTableFormat implements LanguageListener {
	
	/**
	 * This inner class implements the comparator for the column "Age"
	 */
	private class AgeComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostSearchItem item1 = (FrostSearchItem) o1;
			FrostSearchItem item2 = (FrostSearchItem) o2;
			
			String age1 = getAgeString(item1.getDate(), item1.getState());
			String age2 = getAgeString(item2.getDate(), item2.getState());
			
			return age1.compareToIgnoreCase(age2);
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Size"
	 */
	private class SizeComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostSearchItem item1 = (FrostSearchItem) o1;
			FrostSearchItem item2 = (FrostSearchItem) o2;
			return item1.getSize().compareTo(item2.getSize());
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "FileName"
	 */
	private class FileNameComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			FrostSearchItem item1 = (FrostSearchItem) o1;
			FrostSearchItem item2 = (FrostSearchItem) o2;
			return item1.getFilename().compareToIgnoreCase(item2.getFilename());
		}
	}
	
	/**
	 * This inner class implements the comparator for the column "Board"
	 */
	private class BoardComparator implements Comparator {
	
		/* (non-Javadoc)
		 * @see freenet.support.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String boardName1 = ((FrostSearchItem) o1).getBoard().getName();
			String boardName2 = ((FrostSearchItem) o2).getBoard().getName();
			return boardName1.compareToIgnoreCase(boardName2);
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
			String owner1 = ((FrostSearchItem) o1).getOwner();
			String owner2 = ((FrostSearchItem) o2).getOwner();
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
	 * This renderer renders the column "FileName" in different colors, 
	 * depending on state of search item.
	 * States are: NONE, DOWNLOADED, DOWNLOADING, UPLOADING
	 */
	private class FileNameRenderer extends DefaultTableCellRenderer {
		
		private SortedModelTable modelTable;
		
		/**
		 * 
		 */
		public FileNameRenderer(SortedModelTable newModelTable) {
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

			if (!isSelected) {
				ModelItem item = modelTable.getItemAt(row);	//It may be null
				if (item != null) {
					FrostSearchItem searchItem = (FrostSearchItem) item;

					if (searchItem.getState() == FrostSearchItem.STATE_DOWNLOADED) {
						setForeground(Color.LIGHT_GRAY);
					} else if (searchItem.getState() == FrostSearchItem.STATE_DOWNLOADING) {
						setForeground(Color.BLUE);
					} else if (searchItem.getState() == FrostSearchItem.STATE_UPLOADING) {
						setForeground(Color.MAGENTA);
					} else if (searchItem.getState() == FrostSearchItem.STATE_OFFLINE) {
						setForeground(Color.DARK_GRAY);
					} else {
						// normal item, drawn in black
						setForeground(Color.BLACK);
					}
				} else {
					return this;
				}
			}
			return this;
		}
	}

	private Language language;
	
	private final static int COLUMN_COUNT = 5;
	
	private String anonymous;
	private String offline;
	private String uploading;
	private String downloading;
	private String downloaded;

	/**
	 *
	 */
	public SearchTableFormat() {
		super(COLUMN_COUNT);
		
		language = Language.getInstance();
		language.addLanguageListener(this);
		refreshLanguage();
		
		setComparator(new FileNameComparator(), 0);
		setComparator(new SizeComparator(), 1);
		setComparator(new AgeComparator(), 2);
		setComparator(new FromComparator(), 3);
		setComparator(new BoardComparator(), 4);
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
		setColumnName(0, language.getString("Filename"));
		setColumnName(1, language.getString("Size"));
		setColumnName(2, language.getString("Age"));
		setColumnName(3, language.getString("From"));
		setColumnName(4, language.getString("Board"));
		
		anonymous = language.getString("FrostSearchItemObject.Anonymous");
		offline = language.getString("FrostSearchItemObject.Offline");
		uploading = language.getString("SearchTableFormat.Uploading");
		downloading = language.getString("SearchTableFormat.Downloading");
		downloaded = language.getString("SearchTableFormat.Downloaded");
		
		refreshColumnNames();
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getCellValue(frost.util.model.ModelItem, int)
	 */
	public Object getCellValue(ModelItem item, int columnIndex) {
		FrostSearchItem searchItem = (FrostSearchItem) item;
		switch (columnIndex) {
			case 0 :	//Filename
				return searchItem.getFilename();
			
			case 1 :	//Size
				return searchItem.getSize();
			
			case 2 :	//Age
				return getAgeString(searchItem.getDate(), searchItem.getState());
				
			case 3 :	//From
				if (searchItem.getOwner() == null || searchItem.getOwner().length() == 0) {
					return anonymous;	
				} else {
					return searchItem.getOwner();
				} 
			
			case 4 :	//Board
				return searchItem.getBoard();
			
			default: 
				return "**ERROR**";	
		}
	}

	/**
	 * @param date
	 * @param state
	 * @return
	 */
	private String getAgeString(String date, int state) {
		String stateString = null;
		switch (state) {
			case FrostSearchItem.STATE_OFFLINE :
				stateString = offline;
				break;

			case FrostSearchItem.STATE_UPLOADING :
				stateString = uploading;
				break;

			case FrostSearchItem.STATE_DOWNLOADING :
				stateString = downloading;
				break;

			case FrostSearchItem.STATE_DOWNLOADED :
				stateString = downloaded;
		}

		if ((date == null) || (date.length() == 0)) {
			if (state == FrostSearchItem.STATE_NONE) {
				return "**ERROR**";	//No date, no state
			} else {
				return stateString;	//State, but no date
			}
		} else {
			if (state == FrostSearchItem.STATE_NONE) {
				return date;			//Date, but no state
			} else {
				return stateString + " (" + date + ")";	//Both state and date
			}
		}
	}
	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getColumnNumbers(int)
	 */
	public int[] getColumnNumbers(int fieldID) {
		return new int[] {};
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(frost.util.model.gui.ModelTable)
	 */
	public void customizeTable(ModelTable modelTable) {
		super.customizeTable(modelTable);
		
		// Sets the relative widths of the columns
		TableColumnModel columnModel = modelTable.getTable().getColumnModel();
		int[] widths = { 250, 80, 80, 80, 80 };
		for (int i = 0; i < widths.length; i++) {
			columnModel.getColumn(i).setPreferredWidth(widths[i]);
		}

		// Column FileName
		FileNameRenderer cellRenderer = new FileNameRenderer((SortedModelTable) modelTable);
		columnModel.getColumn(0).setCellRenderer(cellRenderer);
		
		// Column "Size"
		columnModel.getColumn(1).setCellRenderer(new SizeRenderer());
	}

}
