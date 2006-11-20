/*
  DownloadTableFormat.java / Frost

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
package frost.fileTransfer.download;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.*;
import frost.fileTransfer.common.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

class DownloadTableFormat extends SortedTableFormat implements LanguageListener, PropertyChangeListener {
    
    private static ImageIcon isSharedIcon = new ImageIcon((MainFrame.class.getResource("/data/shared.png")));
    private static ImageIcon isRequestedIcon = new ImageIcon((MainFrame.class.getResource("/data/signal.png")));
    
    private SortedModelTable modelTable = null;

    private boolean showColoredLines;
    
    private class BaseRenderer extends DefaultTableCellRenderer {
        public BaseRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if( !isSelected ) {
                
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                
                ModelItem item = modelTable.getItemAt(row);
                if (item != null) {
                    FrostDownloadItem downloadItem = (FrostDownloadItem) item;
                    int itemState = downloadItem.getState();
                    if( itemState == FrostDownloadItem.STATE_DONE) {
                        newBackground = TableBackgroundColors.getBackgroundColorDone(table, row, showColoredLines);
                    } else if( itemState == FrostDownloadItem.STATE_FAILED) {
                        newBackground = TableBackgroundColors.getBackgroundColorFailed(table, row, showColoredLines);
                    }
                }
                setBackground(newBackground);
                setForeground(Color.black);
            }
            return this;
        }
    }
    
    private class BlocksProgressRenderer extends JProgressBar implements TableCellRenderer {
        public BlocksProgressRenderer() {
            super();
            setMinimum(0);
            setMaximum(100);
            setStringPainted(true);
            setBorderPainted(false);
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
            setBackground(newBackground);

            ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                FrostDownloadItem downloadItem = (FrostDownloadItem) item;
                
                int totalBlocks = downloadItem.getTotalBlocks();
                int doneBlocks = downloadItem.getDoneBlocks();
                int requiredBlocks = downloadItem.getRequiredBlocks();
                
                if( totalBlocks > 0 ) {
                    // format: ~0% 0/60 [60]
                    
                    int percentDone = 0;

                    if (requiredBlocks > 0) {
                        percentDone = (int) ((doneBlocks * 100) / requiredBlocks);
                    }
                    if( percentDone > 100 ) {
                        percentDone = 100;
                    }
                    setValue(percentDone);
                }
            }
            setString(value.toString());

            return this;
        }
    }

	private class RightAlignRenderer extends BaseRenderer {
        final javax.swing.border.EmptyBorder border = new javax.swing.border.EmptyBorder(0, 0, 0, 3);
        public RightAlignRenderer() {
            super();
        }
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
			setBorder(border);
			return this;
		}
	}

    private class ShowContentTooltipRenderer extends BaseRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltip = null;
            if( value != null ) {
                tooltip = value.toString();
                if( tooltip.length() == 0 ) {
                    tooltip = null;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class ShowStateContentTooltipRenderer extends BaseRenderer {
        public ShowStateContentTooltipRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltip = null;
            ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                FrostDownloadItem uploadItem = (FrostDownloadItem) item;
                String errorCodeDescription = uploadItem.getErrorCodeDescription();
                if( errorCodeDescription != null && errorCodeDescription.length() > 0 ) {
                    tooltip = "Last error: "+errorCodeDescription;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class IsSharedRenderer extends BaseRenderer {
        public IsSharedRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Boolean b = (Boolean)value;
            setText("");
            if( b.booleanValue() ) {
                // show shared icon
                setIcon(isSharedIcon);
            } else {
                setIcon(null);
            }
            setToolTipText(isSharedTooltip);
            return this;
        }
    }

    private class IsRequestedRenderer extends BaseRenderer {
        public IsRequestedRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Boolean b = (Boolean)value;
            setText("");
            if( b.booleanValue() ) {
                // show icon
                setIcon(isRequestedIcon);
            } else {
                setIcon(null);
            }
            setToolTipText(isRequestedTooltip);
            return this;
        }
    }

	private class KeyComparator implements Comparator {
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
	
	private class TriesComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			int retries1 = ((FrostDownloadItem) o1).getRetries();
			int retries2 = ((FrostDownloadItem) o2).getRetries();
			return new Integer(retries1).compareTo(new Integer(retries2));
		}
	}
	
	private class BlocksComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
//			String blocks1 =
//				getBlocksAsString(
//					item1.getTotalBlocks(),
//					item1.getDoneBlocks(),
//					item1.getRequiredBlocks());
//			String blocks2 =
//				getBlocksAsString(
//					item2.getTotalBlocks(),
//					item2.getDoneBlocks(),
//					item2.getRequiredBlocks());
//			return blocks1.compareToIgnoreCase(blocks2); 
            return new Integer(item1.getDoneBlocks()).compareTo(new Integer(item2.getDoneBlocks()));
		}
	}
	
	private class StateComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
			String state1 =	getStateAsString(item1.getState());
			String state2 =	getStateAsString(item2.getState());
			return state1.compareToIgnoreCase(state2);
		}
	}
	
	private class SizeComparator implements Comparator {
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
	
	private class FileNameComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
			return item1.getFileName().compareToIgnoreCase(item2.getFileName());
		}
	}
	
	private class EnabledComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			FrostDownloadItem item1 = (FrostDownloadItem) o1;
			FrostDownloadItem item2 = (FrostDownloadItem) o2;
            return item1.getEnableDownload().equals(item2.getEnableDownload()) ? 0 : 1 ;
		}
	}

    private class IsSharedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostDownloadItem item1 = (FrostDownloadItem) o1;
            FrostDownloadItem item2 = (FrostDownloadItem) o2;
            Boolean b1 = Boolean.valueOf( item1.isSharedFile() );
            Boolean b2 = Boolean.valueOf( item2.isSharedFile() );
            return b1.equals(b2) ? 0 : 1 ;
        }
    }

    private class IsRequestedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostDownloadItem item1 = (FrostDownloadItem) o1;
            FrostDownloadItem item2 = (FrostDownloadItem) o2;
            Boolean b1 = getIsRequested(item1.getFileListFileObject());
            Boolean b2 = getIsRequested(item2.getFileListFileObject());
            return b1.equals(b2) ? 0 : 1 ;
        }
    }

    private class LastReceivedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostDownloadItem item1 = (FrostDownloadItem) o1;
            FrostDownloadItem item2 = (FrostDownloadItem) o2;
            long l1 = item1.getLastReceived();
            long l2 = item2.getLastReceived();
            if( l1 < l2 ) {
                return -1;
            }
            if( l1 > l2 ) {
                return 1;
            }
            return 0;
        }
    }

    private class LastUploadedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostDownloadItem item1 = (FrostDownloadItem) o1;
            FrostDownloadItem item2 = (FrostDownloadItem) o2;
            long l1 = item1.getLastUploaded();
            long l2 = item2.getLastUploaded();
            if( l1 < l2 ) {
                return -1;
            }
            if( l1 > l2 ) {
                return 1;
            }
            return 0;
        }
    }

	private Language language;
	
	private final static int COLUMN_COUNT = 11;
	
    private String stateWaiting;
    private String stateTrying;
    private String stateFailed;
    private String stateDone;
    private String stateDecoding;
    private String stateDownloading;
	
    private String unknown;
    
    private String isSharedTooltip;
    private String isRequestedTooltip;

	public DownloadTableFormat() {
		super(COLUMN_COUNT);

		language = Language.getInstance();
		language.addLanguageListener(this);
		refreshLanguage();

		setComparator(new EnabledComparator(), 0);
        setComparator(new IsSharedComparator(), 1);
        setComparator(new IsRequestedComparator(), 2);
		setComparator(new FileNameComparator(), 3);
		setComparator(new SizeComparator(), 4);
		setComparator(new StateComparator(), 5);
        setComparator(new LastReceivedComparator(), 6);
        setComparator(new LastUploadedComparator(), 7);
		setComparator(new BlocksComparator(), 8);
		setComparator(new TriesComparator(), 9);
		setComparator(new KeyComparator(), 10);
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
	}

	private void refreshLanguage() {
		setColumnName(0, language.getString("DownloadPane.fileTable.enabled"));
        setColumnName(1, language.getString("DownloadPane.fileTable.shared"));
        setColumnName(2, language.getString("DownloadPane.fileTable.requested"));
		setColumnName(3, language.getString("DownloadPane.fileTable.filename"));
		setColumnName(4, language.getString("DownloadPane.fileTable.size"));
		setColumnName(5, language.getString("DownloadPane.fileTable.state"));
        setColumnName(6, language.getString("DownloadPane.fileTable.lastReceived"));
        setColumnName(7, language.getString("DownloadPane.fileTable.lastUploaded"));
		setColumnName(8, language.getString("DownloadPane.fileTable.blocks"));
		setColumnName(9, language.getString("DownloadPane.fileTable.tries"));
		setColumnName(10, language.getString("DownloadPane.fileTable.key"));
		
		stateWaiting =  language.getString("DownloadPane.fileTable.states.waiting");
		stateTrying =   language.getString("DownloadPane.fileTable.states.trying");
		stateFailed =   language.getString("DownloadPane.fileTable.states.failed");
		stateDone =     language.getString("DownloadPane.fileTable.states.done");
		stateDecoding = language.getString("DownloadPane.fileTable.states.decodingSegment") + "...";
        stateDownloading = language.getString("DownloadPane.fileTable.states.downloading");
	
		unknown =   language.getString("DownloadPane.fileTable.states.unknown");
        
        isSharedTooltip = language.getString("DownloadPane.fileTable.shared.tooltip");
        isRequestedTooltip = language.getString("DownloadPane.fileTable.requested.tooltip");
		
		refreshColumnNames();
	}

	public void languageChanged(LanguageEvent event) {
		refreshLanguage();	
	}
    
	public Object getCellValue(ModelItem item, int columnIndex) {
		FrostDownloadItem downloadItem = (FrostDownloadItem) item;
		switch (columnIndex) {

			case 0 : //Enabled
				return downloadItem.getEnableDownload();

            case 1 : // isShared
                return Boolean.valueOf( downloadItem.isSharedFile() );

            case 2 : // isRequested
                return getIsRequested( downloadItem.getFileListFileObject() ); 

			case 3 : // Filename
				return downloadItem.getFileName();

			case 4 : // Size
				if (downloadItem.getFileSize() == null) {
					return unknown;
				} else {
					return SizeFormatter.formatSize(downloadItem.getFileSize().longValue());
				}

			case 5 : // State
				return getStateAsString(downloadItem.getState());

            case 6 : // lastReceived
                if( downloadItem.getLastReceived() > 0 ) {
                    return DateFun.getExtendedDateFromMillis(downloadItem.getLastReceived());
                } else {
                    return "";
                }
                
            case 7 : // lastUploaded
                if( downloadItem.getLastUploaded() > 0 ) {
                    return DateFun.getExtendedDateFromMillis(downloadItem.getLastUploaded());
                } else {
                    return "";
                }
                
			case 8 : // Blocks
				return getBlocksAsString(downloadItem);

			case 9 : // Tries
				return new Integer(downloadItem.getRetries());

			case 10 : // Key
				if (downloadItem.getKey() == null) {
					return " ?";
				} else {
					return downloadItem.getKey();
				}

			default :
				return "**ERROR**";
		}
	}
    
    private Boolean getIsRequested(FrostFileListFileObject flfo) {
        if( flfo == null ) {
            return Boolean.FALSE;
        }
        long now = System.currentTimeMillis();
        long before24hours = now - (24L * 60L * 60L * 1000L);
        if( flfo.getRequestLastReceived() > before24hours 
                || flfo.getRequestLastSent() > before24hours) 
        {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
	
	private String getBlocksAsString(FrostDownloadItem downloadItem) {
        
        int totalBlocks = downloadItem.getTotalBlocks();
        int doneBlocks = downloadItem.getDoneBlocks();
        int requiredBlocks = downloadItem.getRequiredBlocks();
        Boolean isFinalized = downloadItem.isFinalized();
        
        if( totalBlocks <= 0 ) {
            return "";
        }
        
        // format: ~0% 0/60 [60]
        
        int percentDone = 0;

        if (requiredBlocks > 0) {
            percentDone = (int) ((doneBlocks * 100) / requiredBlocks);
        }
        if( percentDone > 100 ) {
            percentDone = 100;
        }
        
        StringBuffer sb = new StringBuffer();
        
        if( isFinalized != null && !isFinalized.booleanValue() ) {
            sb.append("~");
        }

        sb.append(percentDone).append("% ");
        sb.append(doneBlocks).append("/").append(requiredBlocks).append(" [").append(totalBlocks).append("]");

		return sb.toString();
	}

	private String getStateAsString(int state) {
		switch (state) {
			case FrostDownloadItem.STATE_WAITING :
				return stateWaiting;

			case FrostDownloadItem.STATE_TRYING :
				return stateTrying;

			case FrostDownloadItem.STATE_FAILED :
				return stateFailed;

			case FrostDownloadItem.STATE_DONE :
				return stateDone;

			case FrostDownloadItem.STATE_DECODING :
				return stateDecoding;
			
			case FrostDownloadItem.STATE_PROGRESS :
                return stateDownloading;
				
			default :
				return "**ERROR**";
		}
	}

	public void customizeTable(ModelTable lModelTable) {
		super.customizeTable(lModelTable);

        modelTable = (SortedModelTable) lModelTable;
        modelTable.setSortedColumn(3, true);

        lModelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        TableColumnModel columnModel = lModelTable.getTable().getColumnModel();

        // Column "Enabled"
        columnModel.getColumn(0).setCellRenderer(BooleanCell.RENDERER);
        columnModel.getColumn(0).setCellEditor(BooleanCell.EDITOR);
        setColumnEditable(0, true);
        // hard set sizes of checkbox column
        columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);
        columnModel.getColumn(0).setPreferredWidth(20);
        // hard set sizes of icon column
        columnModel.getColumn(1).setMinWidth(20);
        columnModel.getColumn(1).setMaxWidth(20);
        columnModel.getColumn(1).setPreferredWidth(20);
        columnModel.getColumn(1).setCellRenderer(new IsSharedRenderer());
        // hard set sizes of icon column
        columnModel.getColumn(2).setMinWidth(20);
        columnModel.getColumn(2).setMaxWidth(20);
        columnModel.getColumn(2).setPreferredWidth(20);
        columnModel.getColumn(2).setCellRenderer(new IsRequestedRenderer());

        BaseRenderer baseRenderer = new BaseRenderer();
        RightAlignRenderer rightAlignRenderer = new RightAlignRenderer();
        ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();
        
        columnModel.getColumn(3).setCellRenderer(showContentTooltipRenderer); // filename 
        columnModel.getColumn(4).setCellRenderer(rightAlignRenderer); // size
        columnModel.getColumn(5).setCellRenderer(new ShowStateContentTooltipRenderer()); // state
        columnModel.getColumn(6).setCellRenderer(baseRenderer); // lastReceived
        columnModel.getColumn(7).setCellRenderer(baseRenderer); // lastUploaded
        columnModel.getColumn(8).setCellRenderer(new BlocksProgressRenderer()); // blocks
        columnModel.getColumn(9).setCellRenderer(rightAlignRenderer); // tries
        columnModel.getColumn(10).setCellRenderer(showContentTooltipRenderer); // key

        if( !loadTableLayout(columnModel) ) {
    		// Sets the relative widths of the columns
    		int[] widths = { 20,20,20, 150, 30, 30, 20, 20, 70, 10, 60 };
    		for (int i = 0; i < widths.length; i++) { 
    			columnModel.getColumn(i).setPreferredWidth(widths[i]);
    		}
        }
	}
    
    public void saveTableLayout() {
        TableColumnModel tcm = modelTable.getTable().getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            TableColumn tc = tcm.getColumn(columnIndexInTable);
            int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            Core.frostSettings.setValue("DownloadTable.tableindex.modelcolumn."+columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            int columnWidth = tc.getWidth();
            Core.frostSettings.setValue("DownloadTable.columnwidth.modelcolumn."+columnIndexInModel, columnWidth);
        }
    }
    
    private boolean loadTableLayout(TableColumnModel tcm) {
        
        // load the saved tableindex for each column in model, and its saved width
        int[] tableToModelIndex = new int[tcm.getColumnCount()];
        int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            String indexKey = "DownloadTable.tableindex.modelcolumn."+x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            String widthKey = "DownloadTable.columnwidth.modelcolumn."+x;
            if( Core.frostSettings.getObjectValue(widthKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int columnWidth = Core.frostSettings.getIntValue(widthKey);
            if( columnWidth <= 0 ) {
                return false; // invalid column width
            }
            columnWidths[x] = columnWidth;
        }
        // columns are currently added in model order, remove them all and save in an array
        // while on it, set the loaded width of each column
        TableColumn[] tcms = new TableColumn[tcm.getColumnCount()];
        for(int x=tcms.length-1; x >= 0; x--) {
            tcms[x] = tcm.getColumn(x);
            tcm.removeColumn(tcms[x]);
            // keep icon columns 0,1,2 as is
            if(x != 0 && x != 1 && x != 2) {
                tcms[x].setPreferredWidth(columnWidths[x]);
            }
        }
        // add the columns in order loaded from settings
        for(int x=0; x < tableToModelIndex.length; x++) {
            tcm.addColumn(tcms[tableToModelIndex[x]]);
        }
        return true;
    }

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

    public int[] getColumnNumbers(int fieldID) {
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
            modelTable.fireTableDataChanged();
        }
    }
}
