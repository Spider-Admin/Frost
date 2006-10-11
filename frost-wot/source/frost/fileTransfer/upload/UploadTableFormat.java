/*
  UploadTableFormat.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.upload;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

class UploadTableFormat extends SortedTableFormat implements LanguageListener {

    private static ImageIcon isSharedIcon = new ImageIcon((MainFrame.class.getResource("/data/shared.png")));

    NumberFormat numberFormat = NumberFormat.getInstance();
    SortedModelTable modelTable = null;
    
    /**
     * Renders DONE with green background and FAILED with red background.
     */
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
                    FrostUploadItem uploadItem = (FrostUploadItem) item;
                    if( uploadItem.getState() == FrostUploadItem.STATE_DONE) {
                        newBackground = Color.green;
                    } else if( uploadItem.getState() == FrostUploadItem.STATE_FAILED) {
                        newBackground = Color.red;
                    }
                }
                setBackground(newBackground);
                setForeground(Color.black);
            }
            
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

    /**
     * This inner class implements the renderer for the column "FileSize"
     */
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

    /**
     * This inner class implements the comparator for the column "Name"
     */
    private class NameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.getFile().getName().compareToIgnoreCase(item2.getFile().getName());
        }
    }

    /**
     * This inner class implements the comparator for the column "Last Upload"
     */
    private class StateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return getStateAsString(item1, item1.getState()).
                        compareToIgnoreCase(getStateAsString(item2, item2.getState()));
        }
    }

    /**
     * This inner class implements the comparator for the column "Tries"
     */
    private class TriesComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            int retries1 = ((FrostUploadItem) o1).getRetries();
            int retries2 = ((FrostUploadItem) o2).getRetries();
            return new Integer(retries1).compareTo(new Integer(retries2));
        }
    }
    
    private class IsSharedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            Boolean b1 = Boolean.valueOf( item1.isSharedFile() );
            Boolean b2 = Boolean.valueOf( item2.isSharedFile() );
            return b1.equals(b2) ? 0 : 1 ;
        }
    }

    /**
     * This inner class implements the comparator for the column "Path"
     */
    private class PathComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.getFile().getPath().compareToIgnoreCase(item2.getFile().getPath());
        }
    }

    /**
     * This inner class implements the comparator for the column "Enabled"
     */
    private class EnabledComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.isEnabled().equals(item2.isEnabled()) ? 0 : 1 ;
        }
    }

    /**
     * This inner class implements the comparator for the column "Key"
     */
    private class KeyComparator implements Comparator {
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
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            if( item1.getFileSize() > item2.getFileSize() ) {
                return 1;
            } else if( item1.getFileSize() < item2.getFileSize() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private Language language;

    private final static int COLUMN_COUNT = 8;

    private String stateDone;
    private String stateFailed;
    private String stateUploading;
    private String stateEncodingRequested;
    private String stateEncoding;
    private String stateWaiting;

    private String unknown;
    
    private String isSharedTooltip;

    public UploadTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new EnabledComparator(), 0);
        setComparator(new IsSharedComparator(), 1);
        setComparator(new NameComparator(), 2);
        setComparator(new FileSizeComparator(), 3);
        setComparator(new StateComparator(), 4);
        setComparator(new PathComparator(), 5);
        setComparator(new TriesComparator(), 6);
        setComparator(new KeyComparator(), 7);
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("UploadPane.fileTable.enabled"));
        setColumnName(1, language.getString("UploadPane.fileTable.shared"));
        setColumnName(2, language.getString("UploadPane.fileTable.filename"));
        setColumnName(3, language.getString("UploadPane.fileTable.size"));
        setColumnName(4, language.getString("UploadPane.fileTable.state"));
        setColumnName(5, language.getString("UploadPane.fileTable.path"));
        setColumnName(6, language.getString("UploadPane.fileTable.tries"));
        setColumnName(7, language.getString("UploadPane.fileTable.key"));

        stateDone =               language.getString("UploadPane.fileTable.state.done");
        stateFailed =             language.getString("UploadPane.fileTable.state.failed");
        stateUploading =          language.getString("UploadPane.fileTable.state.uploading");
        stateEncodingRequested =  language.getString("UploadPane.fileTable.state.encodeRequested");
        stateEncoding =           language.getString("UploadPane.fileTable.state.encodingFile") + "...";
        stateWaiting =            language.getString("UploadPane.fileTable.state.waiting");
        unknown =                 language.getString("UploadPane.fileTable.state.unknown");
        
        isSharedTooltip = language.getString("UploadPane.fileTable.shared.tooltip");

        refreshColumnNames();
    }

    public void setCellValue(Object value, ModelItem item, int columnIndex) {
        FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                Boolean valueBoolean = (Boolean) value;
                uploadItem.setEnabled(valueBoolean);
                break;

            default :
                super.setCellValue(value, item, columnIndex);
        }
    }

    public Object getCellValue(ModelItem item, int columnIndex) {

        FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                return uploadItem.isEnabled();

            case 1 :    // isShared
                return Boolean.valueOf(uploadItem.isSharedFile());

            case 2 :    //Filename
                return uploadItem.getFile().getName();

            case 3 :    //Size
                return numberFormat.format(uploadItem.getFileSize());

            case 4 :    // state
                return getStateAsString(uploadItem, uploadItem.getState());

            case 5 :    //Path
                return uploadItem.getFile().getPath();

            case 6 :    //Tries
                return new Integer(uploadItem.getRetries());

            case 7 :    //Key
                if (uploadItem.getKey() == null) {
                    return unknown;
                } else {
                    return uploadItem.getKey();
                }
            default:
                return "**ERROR**";
        }
    }

    private String getStateAsString(FrostUploadItem item, int state) {
        switch (state) {

            case FrostUploadItem.STATE_UPLOADING :
                return stateUploading;

            case FrostUploadItem.STATE_PROGRESS :
                return getUploadProgress(item.getTotalBlocks(), item.getDoneBlocks());

            case FrostUploadItem.STATE_ENCODING_REQUESTED :
                return stateEncodingRequested;

            case FrostUploadItem.STATE_ENCODING :
                return stateEncoding;

            case FrostUploadItem.STATE_FAILED :
                return stateFailed;

            case FrostUploadItem.STATE_DONE :
                return stateDone;

            case FrostUploadItem.STATE_WAITING :
                return stateWaiting;

            default :
                return "**ERROR**";
        }
    }

    private String getUploadProgress(int totalBlocks, int doneBlocks) {
        int percentDone = 0;

        if (totalBlocks > 0) {
            percentDone = (int) ((doneBlocks * 100) / totalBlocks);
        }
        return (doneBlocks + " / " + totalBlocks + " (" + percentDone + "%)");
    }

    public void customizeTable(ModelTable lModelTable) {
        super.customizeTable(lModelTable);

        lModelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        // Sets the relative widths of the columns
        TableColumnModel columnModel = lModelTable.getTable().getColumnModel();
        int[] widths = { 20, 20, 250, 65, 30, 60, 15, 70 };
        for (int i = 0; i < widths.length; i++) {
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

        BaseRenderer baseRenderer = new BaseRenderer();
        RightAlignRenderer numberRightRenderer = new RightAlignRenderer();
        ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();
        
        columnModel.getColumn(2).setCellRenderer(showContentTooltipRenderer); // filename
        columnModel.getColumn(3).setCellRenderer(numberRightRenderer); // filesize
        columnModel.getColumn(4).setCellRenderer(baseRenderer); // state
        columnModel.getColumn(5).setCellRenderer(showContentTooltipRenderer); // path
        columnModel.getColumn(6).setCellRenderer(numberRightRenderer); // tries
        columnModel.getColumn(7).setCellRenderer(showContentTooltipRenderer); // key
        
        modelTable = (SortedModelTable) lModelTable;
    }

    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }
}
