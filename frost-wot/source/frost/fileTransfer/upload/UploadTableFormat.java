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
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.*;

import frost.util.gui.BooleanCell;
import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.*;

/**
 * @author $Author$
 * @version $Revision$
 */
class UploadTableFormat extends SortedTableFormat implements LanguageListener {

    /**
     * This inner class implements the renderer for the column "Name"
     */
    private class NameRenderer extends DefaultTableCellRenderer {

        private SortedModelTable modelTable;

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
     * This inner class implements the comparator for the column "Tries"
     */
    private class TriesComparator implements Comparator {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            int retries1 = ((FrostUploadItem) o1).getRetries();
            int retries2 = ((FrostUploadItem) o2).getRetries();
            return new Integer(retries1).compareTo(new Integer(retries2));
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
            String boardName1 = ((FrostUploadItem) o1).getTargetBoard().getName();
            String boardName2 = ((FrostUploadItem) o2).getTargetBoard().getName();
            return boardName1.compareToIgnoreCase(boardName2);
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
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.isEnabled().equals(item2.isEnabled()) ? 0 : 1 ;
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

    private Language language;

    private final static int COLUMN_COUNT = 8;

    private String stateUploadedNever;
    private String stateRequested;
    private String stateUploading;
    private String stateEncodingRequested;
    private String stateEncoding;
    private String stateWaiting;

    private String unknown;

    public UploadTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new EnabledComparator(), 0);
        setComparator(new NameComparator(), 1);
        setComparator(new FileSizeComparator(), 2);
        setComparator(new StateComparator(), 3);
        setComparator(new PathComparator(), 4);
        setComparator(new TriesComparator(), 5);
        setComparator(new DestinationComparator(), 6);
        setComparator(new KeyComparator(), 7);
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("UploadPane.fileTable.enabled"));
        setColumnName(1, language.getString("UploadPane.fileTable.filename"));
        setColumnName(2, language.getString("UploadPane.fileTable.size"));
        setColumnName(3, language.getString("UploadPane.fileTable.lastUpload"));
        setColumnName(4, language.getString("UploadPane.fileTable.path"));
        setColumnName(5, language.getString("UploadPane.fileTable.tries"));
        setColumnName(6, language.getString("UploadPane.fileTable.destination"));
        setColumnName(7, language.getString("UploadPane.fileTable.key"));

        stateUploadedNever =      language.getString("UploadPane.fileTable.state.never");
        stateRequested =          language.getString("UploadPane.fileTable.state.requested");
        stateUploading =          language.getString("UploadPane.fileTable.state.uploading");
        stateEncodingRequested =  language.getString("UploadPane.fileTable.state.encodeRequested");
        stateEncoding =           language.getString("UploadPane.fileTable.state.encodingFile") + "...";
        stateWaiting =            language.getString("UploadPane.fileTable.state.waiting");
        unknown =                 language.getString("UploadPane.fileTable.state.unknown");

        refreshColumnNames();
    }

    /* (non-Javadoc)
     * @see frost.util.model.gui.ModelTableFormat#setCellValue(java.lang.Object, frost.util.model.ModelItem, int)
     */
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

    /* (non-Javadoc)
     * @see frost.util.model.gui.ModelTableFormat#getCellValue(frost.util.model.ModelItem, int)
     */
    public Object getCellValue(ModelItem item, int columnIndex) {
        FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                return uploadItem.isEnabled();

            case 1 :    //Filename
                return uploadItem.getFileName();

            case 2 :    //Size
                return uploadItem.getFileSize();

            case 3 :    //Last upload
                return getStateAsString(uploadItem, uploadItem.getState());

            case 4 :    //Path
                return uploadItem.getFilePath();

            case 5 :    //Tries
                return new Integer(uploadItem.getRetries());

            case 6 :    //Destination
                return uploadItem.getTargetBoard().getName();

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

    /**
     * @param item
     * @param state
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

            case FrostUploadItem.STATE_WAITING :
                return stateWaiting;

            default :
                return "**ERROR**";
        }
    }

    /**
     * @param totalBlocks
     * @param doneBlocks
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
        int[] widths = { 25, 250, 80, 80, 60, 25, 70, 40 };
        for (int i = 0; i < widths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        // Column "Enabled"
        columnModel.getColumn(0).setCellRenderer(BooleanCell.RENDERER);
        columnModel.getColumn(0).setCellEditor(BooleanCell.EDITOR);
        setColumnEditable(0, true);

        // Column "Name"
        columnModel.getColumn(1).setCellRenderer(new NameRenderer((SortedModelTable) modelTable));

        // Column "Size"
        columnModel.getColumn(2).setCellRenderer(new FileSizeRenderer());
    }

    /* (non-Javadoc)
     * @see frost.util.model.gui.ModelTableFormat#getColumnNumber(int)
     */
    public int[] getColumnNumbers(int fieldID) {
        switch (fieldID) {
            case FrostUploadItem.FIELD_ID_DONE_BLOCKS :
                return new int[] {3};   //Last upload

            case FrostUploadItem.FIELD_ID_FILE_NAME :
                return new int[] {1};   //Filename

            case FrostUploadItem.FIELD_ID_FILE_PATH :
                return new int[] {4};   //Path

            case FrostUploadItem.FIELD_ID_FILE_SIZE :
                return new int[] {2};   //Size

            case FrostUploadItem.FIELD_ID_KEY :
                return new int[] {7};   //Key

            case FrostUploadItem.FIELD_ID_LAST_UPLOAD_DATE :
                return new int[] {3};   //Last upload

            case FrostUploadItem.FIELD_ID_SHA1 :
                return new int[] {1};   //Filename

            case FrostUploadItem.FIELD_ID_STATE :
                return new int[] {3};   //Last upload

            case FrostUploadItem.FIELD_ID_TARGET_BOARD :
                return new int[] {6};   //Destination

            case FrostUploadItem.FIELD_ID_TOTAL_BLOCKS :
                return new int[] {3};   //Last upload

            case FrostUploadItem.FIELD_ID_ENABLED :
                return new int[] {0};   //Enabled

            case FrostUploadItem.FIELD_ID_RETRIES :
                return new int[] {5};   //Source

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
