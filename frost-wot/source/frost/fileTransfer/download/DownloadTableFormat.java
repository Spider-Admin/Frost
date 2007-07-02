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
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.fileTransfer.common.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

class DownloadTableFormat extends SortedTableFormat implements LanguageListener, PropertyChangeListener {

    private static final String CFGKEY_SORTSTATE_SORTEDCOLUMN = "DownloadTable.sortState.sortedColumn";
    private static final String CFGKEY_SORTSTATE_SORTEDASCENDING = "DownloadTable.sortState.sortedAscending";
    private static final String CFGKEY_COLUMN_TABLEINDEX = "DownloadTable.tableindex.modelcolumn.";
    private static final String CFGKEY_COLUMN_WIDTH = "DownloadTable.columnwidth.modelcolumn.";

    private static ImageIcon isSharedIcon = new ImageIcon((MainFrame.class.getResource("/data/shared.png")));
    private static ImageIcon isRequestedIcon = new ImageIcon((MainFrame.class.getResource("/data/signal.png")));
    private static ImageIcon isDDAIcon = new ImageIcon((MainFrame.class.getResource("/data/hook.png")));
    
    private static final long CONST_32k = 32 * 1024;
    
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

            setValue(0);

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
                FrostDownloadItem downloadItem = (FrostDownloadItem) item;
                String errorCodeDescription = downloadItem.getErrorCodeDescription();
                if( errorCodeDescription != null && errorCodeDescription.length() > 0 ) {
                    tooltip = "Last error: "+errorCodeDescription;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class IsEnabledRenderer extends JCheckBox implements TableCellRenderer {
        public IsEnabledRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);  
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                FrostDownloadItem downloadItem = (FrostDownloadItem) item;
                if( downloadItem.isExternal() ) {
                    setEnabled(false);
                    setSelected(true); // external items are always enabled
                } else {
                    setEnabled(true);
                    setSelected((value != null && ((Boolean) value).booleanValue()));
                }
            }            
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
            int column) 
        {
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
            int column) 
        {
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

    private class IsDDARenderer extends BaseRenderer {
        public IsDDARenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Boolean b = (Boolean)value;
            setText("");
            if( b.booleanValue() ) {
                // show icon
                setIcon(isDDAIcon);
            } else {
                setIcon(null);
            }
            setToolTipText(isDDATooltip);
            return this;
        }
    }

	private class KeyComparator implements Comparator<FrostDownloadItem> {
		public int compare(FrostDownloadItem o1, FrostDownloadItem o2) {
			String key1 = o1.getKey();
			String key2 = o2.getKey();
			if (key1 == null) {
				key1 = "";
			}
			if (key2 == null) {
				key2 = "";
			}
			return key1.compareToIgnoreCase(key2);
		}
	}
	
	private class TriesComparator implements Comparator<FrostDownloadItem> {
		public int compare(FrostDownloadItem o1, FrostDownloadItem o2) {
			int retries1 = o1.getRetries();
			int retries2 = o2.getRetries();
			return Mixed.compareInt(retries1, retries2);
		}
	}

    private class PriorityComparator implements Comparator<FrostDownloadItem> {
        public int compare(FrostDownloadItem o1, FrostDownloadItem o2) {
            int prio1 = o1.getPriority();
            int prio2 = o2.getPriority();
            return Mixed.compareInt(prio1, prio2);
        }
    }

	private class BlocksComparator implements Comparator<FrostDownloadItem> {
		public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
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
            return Mixed.compareInt(item1.getDoneBlocks(), item2.getDoneBlocks());
//            return new Integer(item1.getDoneBlocks()).compareTo(new Integer(item2.getDoneBlocks()));
		}
	}
	
	private class StateComparator implements Comparator<FrostDownloadItem> {
		public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
			String state1 =	getStateAsString(item1.getState());
			String state2 =	getStateAsString(item2.getState());
			return state1.compareToIgnoreCase(state2);
		}
	}
	
	private class SizeComparator implements Comparator<FrostDownloadItem> {
        private Long unknownSize = new Long(-1);
		public int compare(FrostDownloadItem dli1, FrostDownloadItem dli2) {
			Long size1 = dli1.getFileSize();
			Long size2 = dli2.getFileSize();
            
			if (dli1.getFileSize() >= 0) {
                size1 = dli1.getFileSize();
            } else if( FcpHandler.isFreenet07() 
                           && dli1.getTotalBlocks() > 0 
                           && dli1.isFinalized() != null 
                           && dli1.isFinalized().booleanValue() == true ) 
            {
                // on 0.7, compute appr. size out of finalized block count
                long apprSize = dli1.getTotalBlocks() * CONST_32k;
                size1 = new Long(apprSize);
            } else {
                size1 = unknownSize;
            }
            
            if (dli2.getFileSize() >= 0) {
                size2 = dli2.getFileSize();
            } else if( FcpHandler.isFreenet07() 
                           && dli2.getTotalBlocks() > 0 
                           && dli2.isFinalized() != null 
                           && dli2.isFinalized().booleanValue() == true ) 
            {
                // on 0.7, compute appr. size out of finalized block count
                long apprSize = dli2.getTotalBlocks() * CONST_32k;
                size2 = new Long(apprSize);
            } else {
                size2 = unknownSize;
            }
            
			return size1.compareTo(size2);
		}
	}
	
	private class FileNameComparator implements Comparator<FrostDownloadItem> {
		public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
			return item1.getFilename().compareToIgnoreCase(item2.getFilename());
		}
	}
	
	private class EnabledComparator implements Comparator<FrostDownloadItem> {
		public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
            Boolean b1 = Boolean.valueOf( item1.isEnabled().booleanValue() );
            Boolean b2 = Boolean.valueOf( item2.isEnabled().booleanValue() );
            return b1.compareTo(b2);
		}
	}

    private class IsSharedComparator implements Comparator<FrostDownloadItem> {
        public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
            Boolean b1 = Boolean.valueOf( item1.isSharedFile() );
            Boolean b2 = Boolean.valueOf( item2.isSharedFile() );
            return b1.compareTo(b2);
        }
    }

    private class IsRequestedComparator implements Comparator<FrostDownloadItem> {
        public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
            Boolean b1 = getIsRequested(item1.getFileListFileObject());
            Boolean b2 = getIsRequested(item2.getFileListFileObject());
            return b1.compareTo(b2);
        }
    }

    private class IsDDAComparator implements Comparator<FrostDownloadItem> {
        public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
            Boolean b1 = Boolean.valueOf(item1.isDirect());
            Boolean b2 = Boolean.valueOf(item2.isDirect());
            return b1.compareTo(b2);
        }
    }

    private class LastReceivedComparator implements Comparator<FrostDownloadItem> {
        public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
            return Mixed.compareLong(item1.getLastReceived(), item2.getLastReceived());
        }
    }

    private class LastUploadedComparator implements Comparator<FrostDownloadItem> {
        public int compare(FrostDownloadItem item1, FrostDownloadItem item2) {
            return Mixed.compareLong(item1.getLastUploaded(), item2.getLastUploaded());
        }
    }

	private Language language;
	
    // with persistence we have 2 additional columns: priority and isDDA
    private final static int COLUMN_COUNT = ( PersistenceManager.isPersistenceEnabled() ? 13 : 11 );
	
    private String stateWaiting;
    private String stateTrying;
    private String stateFailed;
    private String stateDone;
    private String stateDecoding;
    private String stateDownloading;
	
    private String unknown;
    
    private String isSharedTooltip;
    private String isRequestedTooltip;
    private String isDDATooltip;

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
        if( PersistenceManager.isPersistenceEnabled() ) {
            setComparator(new IsDDAComparator(), 11);
            setComparator(new PriorityComparator(), 12);
        }
        
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
        if( PersistenceManager.isPersistenceEnabled() ) {
            setColumnName(11, language.getString("DownloadPane.fileTable.isDDA"));
            setColumnName(12, language.getString("DownloadPane.fileTable.priority"));
        }
		
		stateWaiting =  language.getString("DownloadPane.fileTable.states.waiting");
		stateTrying =   language.getString("DownloadPane.fileTable.states.trying");
		stateFailed =   language.getString("DownloadPane.fileTable.states.failed");
		stateDone =     language.getString("DownloadPane.fileTable.states.done");
		stateDecoding = language.getString("DownloadPane.fileTable.states.decodingSegment") + "...";
        stateDownloading = language.getString("DownloadPane.fileTable.states.downloading");
	
		unknown =   language.getString("DownloadPane.fileTable.states.unknown");
        
        isSharedTooltip = language.getString("DownloadPane.fileTable.shared.tooltip");
        isRequestedTooltip = language.getString("DownloadPane.fileTable.requested.tooltip");
        isDDATooltip = language.getString("DownloadPane.fileTable.isDDA.tooltip");
		
		refreshColumnNames();
	}

	public void languageChanged(LanguageEvent event) {
		refreshLanguage();	
	}
    
	public Object getCellValue(ModelItem item, int columnIndex) {
        if( item == null ) {
            return "*null*";
        }
		FrostDownloadItem downloadItem = (FrostDownloadItem) item;
		switch (columnIndex) {

			case 0 : // Enabled
				return downloadItem.isEnabled();

            case 1 : // isShared
                return Boolean.valueOf( downloadItem.isSharedFile() );

            case 2 : // isRequested
                return getIsRequested( downloadItem.getFileListFileObject() ); 

			case 3 : // Filename
				return downloadItem.getFilename();

			case 4 : // Size
                if( downloadItem.getFileSize() >= 0 ) {
                    // size is set
                    return SizeFormatter.formatSize(downloadItem.getFileSize());

                } else if( FcpHandler.isFreenet07() 
                           && downloadItem.getRequiredBlocks() > 0 
                           && downloadItem.isFinalized() != null 
                           && downloadItem.isFinalized().booleanValue() == true ) 
                {
                    // on 0.7, compute appr. size out of finalized block count
                    long apprSize = downloadItem.getRequiredBlocks() * CONST_32k;
                    return "~" + SizeFormatter.formatSize(apprSize);
                } else {
					return unknown;
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

            case 11: // IsDDA
                return Boolean.valueOf(!downloadItem.isDirect());

            case 12: // Priority
                int value = downloadItem.getPriority();
                if( value < 0 ) {
                    return "-";
                } else {
                    return new Integer(value);
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
        
        StringBuilder sb = new StringBuilder();
        
        if( isFinalized != null && isFinalized.booleanValue() == false ) {
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
        
        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES)
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDCOLUMN) != null
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDASCENDING) != null )
        {
            int sortedColumn = Core.frostSettings.getIntValue(CFGKEY_SORTSTATE_SORTEDCOLUMN);
            boolean isSortedAsc = Core.frostSettings.getBoolValue(CFGKEY_SORTSTATE_SORTEDASCENDING);
            if( sortedColumn > -1 ) {
                modelTable.setSortedColumn(sortedColumn, isSortedAsc);
            }
        } else {
            modelTable.setSortedColumn(3, true);
        }

        lModelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        TableColumnModel columnModel = lModelTable.getTable().getColumnModel();

        // Column "Enabled"
//        columnModel.getColumn(0).setCellRenderer(BooleanCell.RENDERER);
        columnModel.getColumn(0).setCellEditor(BooleanCell.EDITOR);
        setColumnEditable(0, true);
        // hard set sizes of checkbox column
        columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);
        columnModel.getColumn(0).setPreferredWidth(20);
        columnModel.getColumn(0).setCellRenderer(new IsEnabledRenderer());
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
        if( PersistenceManager.isPersistenceEnabled() ) {
            // hard set sizes of IsDDA column
            columnModel.getColumn(11).setMinWidth(20);
            columnModel.getColumn(11).setMaxWidth(20);
            columnModel.getColumn(11).setPreferredWidth(20);
            // hard set sizes of priority column
            columnModel.getColumn(12).setMinWidth(20);
            columnModel.getColumn(12).setMaxWidth(20);
            columnModel.getColumn(12).setPreferredWidth(20);
        }
        
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
        if( PersistenceManager.isPersistenceEnabled() ) {
            columnModel.getColumn(11).setCellRenderer(new IsDDARenderer());
            columnModel.getColumn(12).setCellRenderer(rightAlignRenderer);
        }

        if( !loadTableLayout(columnModel) ) {
    		// Sets the relative widths of the columns
    		int[] widths;
            if( PersistenceManager.isPersistenceEnabled() ) {
                int[] newWidths = { 20,20,20, 150, 30, 30, 20, 20, 70, 10, 60, 20, 20 };
                widths = newWidths;
            } else {
                int[] newWidths = { 20,20,20, 150, 30, 30, 20, 20, 70, 10, 60 };
                widths = newWidths;
            }

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
            Core.frostSettings.setValue(CFGKEY_COLUMN_TABLEINDEX + columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            int columnWidth = tc.getWidth();
            Core.frostSettings.setValue(CFGKEY_COLUMN_WIDTH + columnIndexInModel, columnWidth);
        }
        
        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES) && modelTable.getSortedColumn() > -1 ) {
            int sortedColumn = modelTable.getSortedColumn();
            boolean isSortedAsc = modelTable.isSortedAscending();
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDCOLUMN, sortedColumn);
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDASCENDING, isSortedAsc);
        }
    }
    
    private boolean loadTableLayout(TableColumnModel tcm) {
        
        // load the saved tableindex for each column in model, and its saved width
        int[] tableToModelIndex = new int[tcm.getColumnCount()];
        int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            String indexKey = CFGKEY_COLUMN_TABLEINDEX + x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            String widthKey = CFGKEY_COLUMN_WIDTH + x;
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
				downloadItem.setEnabled(valueBoolean);
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
