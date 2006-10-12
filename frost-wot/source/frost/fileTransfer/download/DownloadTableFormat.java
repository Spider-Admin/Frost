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
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

class DownloadTableFormat extends SortedTableFormat implements LanguageListener {
    
    private static ImageIcon isSharedIcon = new ImageIcon((MainFrame.class.getResource("/data/shared.png")));
    private static ImageIcon isRequestedIcon = new ImageIcon((MainFrame.class.getResource("/data/signal.png")));
    
    NumberFormat numberFormat = NumberFormat.getInstance();
    SortedModelTable modelTable = null;
    
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
                Color newBackground = Color.white;
                
                ModelItem item = modelTable.getItemAt(row); //It may be null
                if (item != null) {
                    FrostDownloadItem downloadItem = (FrostDownloadItem) item;
                    if( downloadItem.getState() == FrostDownloadItem.STATE_DONE) {
                        newBackground = Color.green;
                    } else if( downloadItem.getState() == FrostDownloadItem.STATE_FAILED) {
                        newBackground = Color.red;
                    }
                }
                setBackground(newBackground);
                setForeground(Color.black);
            }
            
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
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class IsSharedRenderer extends DefaultTableCellRenderer {
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

    private class IsRequestedRenderer extends DefaultTableCellRenderer {
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
	
	private class StateComparator implements Comparator {
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
					return numberFormat.format(downloadItem.getFileSize().longValue());
				}

			case 5 : // State
				return getStateAsString(
					downloadItem.getState(),
					downloadItem.getTotalBlocks(),
					downloadItem.getDoneBlocks(),
					downloadItem.getRequiredBlocks());

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
				return getBlocksAsString(
					downloadItem.getTotalBlocks(),
					downloadItem.getDoneBlocks(),
					downloadItem.getRequiredBlocks());

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
	
	private String getBlocksAsString(int totalBlocks, int doneBlocks, int requiredBlocks) {
		if (totalBlocks == 0) {
			return "";
		} else {
			return (doneBlocks + " / " + requiredBlocks + " (" + totalBlocks + ")");
		}
	}

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

	public void customizeTable(ModelTable lModelTable) {
		super.customizeTable(lModelTable);
        
        lModelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		// Sets the relative widths of the columns
		TableColumnModel columnModel = lModelTable.getTable().getColumnModel();
		int[] widths = { 20,20,20, 150, 30, 30, 20, 20, 70, 10, 60 };
		for (int i = 0; i < widths.length; i++) { // col 0 default width
			columnModel.getColumn(i).setPreferredWidth(widths[i]);
		}

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
        columnModel.getColumn(5).setCellRenderer(baseRenderer); // state
        columnModel.getColumn(6).setCellRenderer(baseRenderer); // lastReceived
        columnModel.getColumn(7).setCellRenderer(baseRenderer); // lastUploaded
        columnModel.getColumn(8).setCellRenderer(baseRenderer); // blocks
        columnModel.getColumn(9).setCellRenderer(rightAlignRenderer); // tries
        columnModel.getColumn(10).setCellRenderer(showContentTooltipRenderer); // key
        
        modelTable = (SortedModelTable) lModelTable;
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
}
