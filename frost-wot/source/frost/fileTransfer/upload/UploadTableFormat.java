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

class UploadTableFormat extends SortedTableFormat implements LanguageListener, PropertyChangeListener {

    private static final String CFGKEY_SORTSTATE_SORTEDCOLUMN = "UploadTable.sortState.sortedColumn";
    private static final String CFGKEY_SORTSTATE_SORTEDASCENDING = "UploadTable.sortState.sortedAscending";
    private static final String CFGKEY_COLUMN_TABLEINDEX = "UploadTable.tableindex.modelcolumn.";
    private static final String CFGKEY_COLUMN_WIDTH = "UploadTable.columnwidth.modelcolumn.";

    private static ImageIcon isSharedIcon = MiscToolkit.loadImageIcon("/data/shared.png");

    private SortedModelTable modelTable = null;

    private boolean showColoredLines;

    /**
     * Renders DONE with green background and FAILED with red background.
     */
    private class BaseRenderer extends DefaultTableCellRenderer {
        public BaseRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if( !isSelected ) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);

                final ModelItem item = modelTable.getItemAt(row);
                if (item != null) {
                    final FrostUploadItem uploadItem = (FrostUploadItem) item;
                    final int itemState = uploadItem.getState();
                    if( itemState == FrostUploadItem.STATE_DONE) {
                        newBackground = TableBackgroundColors.getBackgroundColorDone(table, row, showColoredLines);
                    } else if( itemState == FrostUploadItem.STATE_FAILED) {
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
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {

            final Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
            setBackground(newBackground);

            setValue(0);

            final ModelItem item = modelTable.getItemAt(row);
            if (item != null) {
                final FrostUploadItem uploadItem = (FrostUploadItem) item;

                final int totalBlocks = uploadItem.getTotalBlocks();
                final int doneBlocks = uploadItem.getDoneBlocks();

                if( totalBlocks > 0 ) {
                    // format: ~0% 0/60 [60]

                    int percentDone = 0;

                    percentDone = ((doneBlocks * 100) / totalBlocks);
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

    private class ShowContentTooltipRenderer extends BaseRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
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

    private class ShowNameTooltipRenderer extends BaseRenderer {
        public ShowNameTooltipRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String tooltip = null;
            final ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                final FrostUploadItem uploadItem = (FrostUploadItem) item;
                final StringBuilder sb = new StringBuilder();
                sb.append("<html>").append(uploadItem.getFilename());
                if( uploadItem.getUploadAddedMillis() > 0 ) {
                    sb.append("<br>Added: ");
                    sb.append(DateFun.FORMAT_DATE_VISIBLE.print(uploadItem.getUploadAddedMillis()));
                    sb.append("  ");
                    sb.append(DateFun.FORMAT_TIME_VISIBLE.print(uploadItem.getUploadAddedMillis()));
                }
                if( uploadItem.getUploadStartedMillis() > 0 ) {
                    sb.append("<br>Started: ");
                    sb.append(DateFun.FORMAT_DATE_VISIBLE.print(uploadItem.getUploadStartedMillis()));
                    sb.append("  ");
                    sb.append(DateFun.FORMAT_TIME_VISIBLE.print(uploadItem.getUploadStartedMillis()));
                }
                if( uploadItem.getUploadFinishedMillis() > 0 ) {
                    sb.append("<br>Finished: ");
                    sb.append(DateFun.FORMAT_DATE_VISIBLE.print(uploadItem.getUploadFinishedMillis()));
                    sb.append("  ");
                    sb.append(DateFun.FORMAT_TIME_VISIBLE.print(uploadItem.getUploadFinishedMillis()));
                }
                sb.append("</html>");
                tooltip = sb.toString();
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class ShowStateContentTooltipRenderer extends BaseRenderer {
        public ShowStateContentTooltipRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltip = null;
            final ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                final FrostUploadItem uploadItem = (FrostUploadItem) item;
                final String errorCodeDescription = uploadItem.getErrorCodeDescription();
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
        }
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
        {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            final ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                final FrostUploadItem uploadItem = (FrostUploadItem) item;
                if( uploadItem.isExternal() ) {
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
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            final Boolean b = (Boolean)value;
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
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
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
    private class NameComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
            return item1.getFile().getName().compareToIgnoreCase(item2.getFile().getName());
        }
    }

    private class PriorityComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem o1, final FrostUploadItem o2) {
            final int prio1 = o1.getPriority();
            final int prio2 = o2.getPriority();
            return Mixed.compareInt(prio1, prio2);
//          return new Integer(retries1).compareTo(new Integer(retries2));
        }
    }

    /**
     * This inner class implements the comparator for the column "Last Upload"
     */
    private class StateComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
            return Mixed.compareInt(item1.getState(), item2.getState());
//            return getStateAsString(item1, item1.getState()).
//                        compareToIgnoreCase(getStateAsString(item2, item2.getState()));
        }
    }

    private class BlocksComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
//          String blocks1 =
//              getBlocksAsString(
//                  item1.getTotalBlocks(),
//                  item1.getDoneBlocks(),
//                  item1.getRequiredBlocks());
//          String blocks2 =
//              getBlocksAsString(
//                  item2.getTotalBlocks(),
//                  item2.getDoneBlocks(),
//                  item2.getRequiredBlocks());
//          return blocks1.compareToIgnoreCase(blocks2);
//            return new Integer(item1.getDoneBlocks()).compareTo(new Integer(item2.getDoneBlocks()));
            return Mixed.compareInt(item1.getDoneBlocks(), item2.getDoneBlocks());
        }
    }

    /**
     * This inner class implements the comparator for the column "Tries"
     */
    private class TriesComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem o1, final FrostUploadItem o2) {
            final int retries1 = o1.getRetries();
            final int retries2 = o2.getRetries();
            return Mixed.compareInt(retries1, retries2);
//            return new Integer(retries1).compareTo(new Integer(retries2));
        }
    }

    private class IsSharedComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
            final Boolean b1 = Boolean.valueOf( item1.isSharedFile() );
            final Boolean b2 = Boolean.valueOf( item2.isSharedFile() );
            return b1.compareTo(b2);
        }
    }

    /**
     * This inner class implements the comparator for the column "Path"
     */
    private class PathComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
            return item1.getFile().getPath().compareToIgnoreCase(item2.getFile().getPath());
        }
    }

    /**
     * This inner class implements the comparator for the column "Enabled"
     */
    private class EnabledComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
            final Boolean b1 = Boolean.valueOf( item1.isEnabled().booleanValue() );
            final Boolean b2 = Boolean.valueOf( item2.isEnabled().booleanValue() );
            return b1.compareTo(b2);
        }
    }

    /**
     * This inner class implements the comparator for the column "Key"
     */
    private class KeyComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem o1, final FrostUploadItem o2) {
            String key1 = o1.getKey();
            String key2 = o2.getKey();
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
    private class FileSizeComparator implements Comparator<FrostUploadItem> {
        public int compare(final FrostUploadItem item1, final FrostUploadItem item2) {
            return Mixed.compareLong(item1.getFileSize(), item2.getFileSize());
//            if( item1.getFileSize() > item2.getFileSize() ) {
//                return 1;
//            } else if( item1.getFileSize() < item2.getFileSize() ) {
//                return -1;
//            } else {
//                return 0;
//            }
        }
    }

    private final Language language;

    // with persistence we have 1 additional column: priority
    private final static int COLUMN_COUNT = ( PersistenceManager.isPersistenceEnabled() ? 10 : 9 );

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
        setComparator(new BlocksComparator(), 6);
        setComparator(new TriesComparator(), 7);
        setComparator(new KeyComparator(), 8);
        if( PersistenceManager.isPersistenceEnabled() ) {
            setComparator(new PriorityComparator(), 9);
        }

        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("UploadPane.fileTable.enabled"));
        setColumnName(1, language.getString("UploadPane.fileTable.shared"));
        setColumnName(2, language.getString("UploadPane.fileTable.filename"));
        setColumnName(3, language.getString("UploadPane.fileTable.size"));
        setColumnName(4, language.getString("UploadPane.fileTable.state"));
        setColumnName(5, language.getString("UploadPane.fileTable.path"));
        setColumnName(6, language.getString("UploadPane.fileTable.blocks"));
        setColumnName(7, language.getString("UploadPane.fileTable.tries"));
        setColumnName(8, language.getString("UploadPane.fileTable.key"));
        if( PersistenceManager.isPersistenceEnabled() ) {
            setColumnName(9, language.getString("UploadPane.fileTable.priority"));
        }

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

    @Override
    public void setCellValue(final Object value, final ModelItem item, final int columnIndex) {
        final FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                final Boolean valueBoolean = (Boolean) value;
                uploadItem.setEnabled(valueBoolean);
                FileTransferManager.inst().getUploadManager().notifyUploadItemEnabledStateChanged(uploadItem);
                break;

            default :
                super.setCellValue(value, item, columnIndex);
        }
    }

    public Object getCellValue(final ModelItem item, final int columnIndex) {
        if( item == null ) {
            return "*null*";
        }
        final FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                return uploadItem.isEnabled();

            case 1 :    // isShared
                return Boolean.valueOf(uploadItem.isSharedFile());

            case 2 :    //Filename
                return uploadItem.getFile().getName();

            case 3 :    //Size
                return FormatterUtils.formatSize(uploadItem.getFileSize());

            case 4 :    // state
                return getStateAsString(uploadItem, uploadItem.getState());

            case 5 :    //Path
                return uploadItem.getFile().getPath();

            case 6 :    //blocks
                return getUploadProgress(uploadItem);

            case 7 :    //Tries
                return new Integer(uploadItem.getRetries());

            case 8 :    //Key
                if (uploadItem.getKey() == null) {
                    return unknown;
                } else {
                    return uploadItem.getKey();
                }

            case 9: // Priority
                final int value = uploadItem.getPriority();
                if( value < 0 ) {
                    return "-";
                } else {
                    return new Integer(value);
                }

            default:
                return "**ERROR**";
        }
    }

    private String getStateAsString(final FrostUploadItem item, final int state) {
        switch (state) {

            case FrostUploadItem.STATE_PROGRESS :
                return stateUploading;

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

    private String getUploadProgress(final FrostUploadItem uploadItem) {

        final int totalBlocks = uploadItem.getTotalBlocks();
        final int doneBlocks = uploadItem.getDoneBlocks();
        final Boolean isFinalized = uploadItem.isFinalized();

        // format: ~0% 0/60 [60]

        if( totalBlocks <= 0 ) {
            return "";
        }

        int percentDone = 0;
        if (totalBlocks > 0) {
            percentDone = ((doneBlocks * 100) / totalBlocks);
        }
        if( percentDone > 100 ) {
            percentDone = 100;
        }

        final StringBuilder sb = new StringBuilder();

        if( isFinalized != null && !isFinalized.booleanValue() ) {
            sb.append("~");
        }

        sb.append(percentDone).append("% ");
        sb.append(doneBlocks).append("/").append(totalBlocks).append(" [").append(totalBlocks).append("]");

        return sb.toString();
    }

    @Override
    public void customizeTable(final ModelTable lModelTable) {
        super.customizeTable(lModelTable);

        modelTable = (SortedModelTable) lModelTable;

        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES)
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDCOLUMN) != null
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDASCENDING) != null )
        {
            final int sortedColumn = Core.frostSettings.getIntValue(CFGKEY_SORTSTATE_SORTEDCOLUMN);
            final boolean isSortedAsc = Core.frostSettings.getBoolValue(CFGKEY_SORTSTATE_SORTEDASCENDING);
            if( sortedColumn > -1 ) {
                modelTable.setSortedColumn(sortedColumn, isSortedAsc);
            }
        } else {
            modelTable.setSortedColumn(2, true);
        }

        lModelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        final TableColumnModel columnModel = lModelTable.getTable().getColumnModel();

        // Column "Enabled"
        columnModel.getColumn(0).setCellRenderer(BooleanCell.RENDERER);
        columnModel.getColumn(0).setCellEditor(BooleanCell.EDITOR);
        setColumnEditable(0, true);
        columnModel.getColumn(0).setCellRenderer(new IsEnabledRenderer());
        // hard set sizes of checkbox column
        columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);
        columnModel.getColumn(0).setPreferredWidth(20);
        // hard set sizes of icon column
        columnModel.getColumn(1).setMinWidth(20);
        columnModel.getColumn(1).setMaxWidth(20);
        columnModel.getColumn(1).setPreferredWidth(20);
        columnModel.getColumn(1).setCellRenderer(new IsSharedRenderer());
        if( PersistenceManager.isPersistenceEnabled() ) {
            // hard set sizes of priority column
            columnModel.getColumn(9).setMinWidth(20);
            columnModel.getColumn(9).setMaxWidth(20);
            columnModel.getColumn(9).setPreferredWidth(20);
        }

        final RightAlignRenderer numberRightRenderer = new RightAlignRenderer();
        final ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();

        columnModel.getColumn(2).setCellRenderer(new ShowNameTooltipRenderer()); // filename
        columnModel.getColumn(3).setCellRenderer(numberRightRenderer); // filesize
        columnModel.getColumn(4).setCellRenderer(new ShowStateContentTooltipRenderer()); // state
        columnModel.getColumn(5).setCellRenderer(showContentTooltipRenderer); // path
        columnModel.getColumn(6).setCellRenderer(new BlocksProgressRenderer()); // blocks
        columnModel.getColumn(7).setCellRenderer(numberRightRenderer); // tries
        columnModel.getColumn(8).setCellRenderer(showContentTooltipRenderer); // key
        if( PersistenceManager.isPersistenceEnabled() ) {
            columnModel.getColumn(9).setCellRenderer(numberRightRenderer);
        }

        if( !loadTableLayout(columnModel) ) {
            // Sets the relative widths of the columns
            int[] widths;
            if( PersistenceManager.isPersistenceEnabled() ) {
                final int[] newWidths = { 20, 20, 200, 65, 30, 60, 50, 15, 70, 20 };
                widths = newWidths;
            } else {
                final int[] newWidths = { 20, 20, 200, 65, 30, 60, 50, 15, 70 };
                widths = newWidths;
            }

            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }

    public void saveTableLayout() {
        final TableColumnModel tcm = modelTable.getTable().getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            final TableColumn tc = tcm.getColumn(columnIndexInTable);
            final int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            Core.frostSettings.setValue(CFGKEY_COLUMN_TABLEINDEX + columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            final int columnWidth = tc.getWidth();
            Core.frostSettings.setValue(CFGKEY_COLUMN_WIDTH + columnIndexInModel, columnWidth);
        }

        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES) && modelTable.getSortedColumn() > -1 ) {
            final int sortedColumn = modelTable.getSortedColumn();
            final boolean isSortedAsc = modelTable.isSortedAscending();
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDCOLUMN, sortedColumn);
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDASCENDING, isSortedAsc);
        }
    }

    private boolean loadTableLayout(final TableColumnModel tcm) {

        // load the saved tableindex for each column in model, and its saved width
        final int[] tableToModelIndex = new int[tcm.getColumnCount()];
        final int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            final String indexKey = CFGKEY_COLUMN_TABLEINDEX + x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            final int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            final String widthKey = CFGKEY_COLUMN_WIDTH + x;
            if( Core.frostSettings.getObjectValue(widthKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            final int columnWidth = Core.frostSettings.getIntValue(widthKey);
            if( columnWidth <= 0 ) {
                return false; // invalid column width
            }
            columnWidths[x] = columnWidth;
        }
        // columns are currently added in model order, remove them all and save in an array
        // while on it, set the loaded width of each column
        final TableColumn[] tcms = new TableColumn[tcm.getColumnCount()];
        for(int x=tcms.length-1; x >= 0; x--) {
            tcms[x] = tcm.getColumn(x);
            tcm.removeColumn(tcms[x]);
            // keep icon columns 0,1 as is
            if(x != 0 && x != 1) {
                tcms[x].setPreferredWidth(columnWidths[x]);
            }
        }
        // add the columns in order loaded from settings
        for( final int element : tableToModelIndex ) {
            tcm.addColumn(tcms[element]);
        }
        return true;
    }


    public int[] getColumnNumbers(final int fieldID) {
        return new int[] {};
    }

    public void languageChanged(final LanguageEvent event) {
        refreshLanguage();
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
            modelTable.fireTableDataChanged();
        }
    }
}
