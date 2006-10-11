/*
  SharedFilesTableFormat.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.sharing;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

class SharedFilesTableFormat extends SortedTableFormat implements LanguageListener {

    private Language language;

    private final static int COLUMN_COUNT = 13;

    private String stateNever;
    private String unknown;
    
    NumberFormat numberFormat = NumberFormat.getInstance();

    public SharedFilesTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new NameComparator(), 0);
        setComparator(new FileSizeComparator(), 1);
        setComparator(new OwnerComparator(), 2);
        setComparator(new UploadCountComparator(), 3);
        setComparator(new LastUploadComparator(), 4);
        setComparator(new RequestCountComparator(), 5);
        setComparator(new LastRequestComparator(), 6);
        setComparator(new KeyComparator(), 7);
        setComparator(new RatingComparator(), 8);
        setComparator(new CommentComparator(), 9);
        setComparator(new KeywordsComparator(), 10);
        setComparator(new LastSharedComparator(), 11);
        setComparator(new PathComparator(), 12);
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("SharedFilesPane.fileTable.filename"));
        setColumnName(1, language.getString("SharedFilesPane.fileTable.size"));
        setColumnName(2, language.getString("SharedFilesPane.fileTable.owner"));
        setColumnName(3, language.getString("SharedFilesPane.fileTable.uploadCount"));
        setColumnName(4, language.getString("SharedFilesPane.fileTable.lastUpload"));
        setColumnName(5, language.getString("SharedFilesPane.fileTable.requestCount"));
        setColumnName(6, language.getString("SharedFilesPane.fileTable.lastRequest"));
        setColumnName(7, language.getString("SharedFilesPane.fileTable.key"));
        setColumnName(8, language.getString("SharedFilesPane.fileTable.rating"));
        setColumnName(9, language.getString("SharedFilesPane.fileTable.comment"));
        setColumnName(10, language.getString("SharedFilesPane.fileTable.keywords"));
        setColumnName(11, language.getString("SharedFilesPane.fileTable.lastShared"));
        setColumnName(12, language.getString("SharedFilesPane.fileTable.path"));

        stateNever = language.getString("SharedFilesPane.fileTable.state.never");
        unknown =    language.getString("SharedFilesPane.fileTable.state.unknown");

        refreshColumnNames();
    }

    public void setCellValue(Object value, ModelItem item, int columnIndex) { }

    public Object getCellValue(ModelItem item, int columnIndex) {

        FrostSharedFileItem sfItem = (FrostSharedFileItem) item;
        switch (columnIndex) {
            case 0 : // name
                return sfItem.getFile().getName();
            case 1 : // size
                return numberFormat.format(sfItem.getFileSize());
            case 2 : // owner
                return sfItem.getOwner();
            case 3 : // uploadCount
                return ""+sfItem.getUploadCount();
            case 4 : // lastUploaded
                if( sfItem.getLastUploaded() == 0 ) {
                    return stateNever;
                } else {
                    return DateFun.getExtendedDateFromMillis(sfItem.getLastUploaded());
                }
            case 5 : // requestCount
                return ""+sfItem.getRequestsReceived();
            case 6 : // lastRequestDate
                if( sfItem.getRequestLastReceived() == 0 ) {
                    return stateNever;
                } else {
                    return DateFun.getExtendedDateFromMillis(sfItem.getRequestLastReceived());
                }
            case 7 :    // Key
                if (sfItem.getChkKey() == null) {
                    return unknown;
                } else {
                    return sfItem.getChkKey();
                }
            case 8 : // rating
                return RatingStringProvider.getRatingString(sfItem.getRating());
            case 9 : // comment
                if( sfItem.getComment() == null ) {
                    return "";
                } else {
                    return sfItem.getComment();
                }
            case 10: // keywords
                if( sfItem.getKeywords() == null ) {
                    return "";
                } else {
                    return sfItem.getKeywords();
                }
            case 11: // refLastSend
                if( sfItem.getRefLastSent() == 0 ) {
                    return stateNever;
                } else {
                    return DateFun.getExtendedDateFromMillis(sfItem.getRefLastSent());
                }
            case 12 : // path
                return sfItem.getFile().getPath();
            default:
                return "**ERROR**";
        }
    }

    public void customizeTable(ModelTable modelTable) {
        super.customizeTable(modelTable);

        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        //Sets the relative widths of the columns
        TableColumnModel columnModel = modelTable.getTable().getColumnModel();
        int[] widths = { 150, 65, 80, 40, 60, 40, 60, 80, 30, 50, 50, 60, 50 };
        for (int i = 0; i < widths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();
        RightAlignRenderer numberRightRenderer = new RightAlignRenderer();

        columnModel.getColumn(0).setCellRenderer(showContentTooltipRenderer); // filename
        columnModel.getColumn(1).setCellRenderer(numberRightRenderer); // fileSize
        columnModel.getColumn(2).setCellRenderer(showContentTooltipRenderer); // owner
        columnModel.getColumn(3).setCellRenderer(numberRightRenderer); // uploadCount
//        columnModel.getColumn(4).setCellRenderer(showContentTooltipRenderer); // lastUpload
        columnModel.getColumn(5).setCellRenderer(numberRightRenderer); // requestCount
//        columnModel.getColumn(6).setCellRenderer(showContentTooltipRenderer); // lastRequest
        columnModel.getColumn(7).setCellRenderer(showContentTooltipRenderer); // key
//        columnModel.getColumn(8).setCellRenderer(showContentTooltipRenderer); // rating
        columnModel.getColumn(9).setCellRenderer(showContentTooltipRenderer); // comment
        columnModel.getColumn(10).setCellRenderer(showContentTooltipRenderer); // keywords
//        columnModel.getColumn(11).setCellRenderer(showContentTooltipRenderer); // lastShared
        columnModel.getColumn(12).setCellRenderer(showContentTooltipRenderer); // path
    }

    public int[] getColumnNumbers(int fieldID) {
        return null;
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private class ShowContentTooltipRenderer extends DefaultTableCellRenderer {
        public ShowContentTooltipRenderer() {
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
            String tooltip = null;
            if( value != null ) {
                tooltip = value.toString();
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {
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
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            // col is right aligned, give some space to next column
            setBorder(border);
            return this;
        }
    }

    private class NameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            return item1.getFile().getName().compareToIgnoreCase(item2.getFile().getName());
        }
    }
    private class FileSizeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getFileSize() > item2.getFileSize() ) {
                return 1;
            } else if( item1.getFileSize() < item2.getFileSize() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class OwnerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            return item1.getOwner().compareToIgnoreCase(item2.getOwner());
        }
    }
    private class UploadCountComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getUploadCount() > item2.getUploadCount() ) {
                return 1;
            } else if( item1.getUploadCount() < item2.getUploadCount() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class LastUploadComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getLastUploaded() > item2.getLastUploaded() ) {
                return 1;
            } else if( item1.getLastUploaded() < item2.getLastUploaded() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class RequestCountComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getRequestsReceived() > item2.getRequestsReceived() ) {
                return 1;
            } else if( item1.getRequestsReceived() < item2.getRequestsReceived() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class LastRequestComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getRequestLastReceived() > item2.getRequestLastReceived() ) {
                return 1;
            } else if( item1.getRequestLastReceived() < item2.getRequestLastReceived() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class KeyComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String key1 = ((FrostSharedFileItem) o1).getChkKey();
            String key2 = ((FrostSharedFileItem) o2).getChkKey();
            if (key1 == null) {
                key1 = unknown;
            }
            if (key2 == null) {
                key2 = unknown;
            }
            return key1.compareToIgnoreCase(key2);
        }
    }
    private class RatingComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getRating() > item2.getRating() ) {
                return 1;
            } else if( item1.getRating() < item2.getRating() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class CommentComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String key1 = ((FrostSharedFileItem) o1).getComment();
            String key2 = ((FrostSharedFileItem) o2).getComment();
            if (key1 == null) {
                key1 = unknown;
            }
            if (key2 == null) {
                key2 = unknown;
            }
            return key1.compareToIgnoreCase(key2);
        }
    }
    private class KeywordsComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String key1 = ((FrostSharedFileItem) o1).getKeywords();
            String key2 = ((FrostSharedFileItem) o2).getKeywords();
            if (key1 == null) {
                key1 = unknown;
            }
            if (key2 == null) {
                key2 = unknown;
            }
            return key1.compareToIgnoreCase(key2);
        }
    }
    private class LastSharedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            if( item1.getRefLastSent() > item2.getRefLastSent() ) {
                return 1;
            } else if( item1.getRefLastSent() < item2.getRefLastSent() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    private class PathComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSharedFileItem item1 = (FrostSharedFileItem) o1;
            FrostSharedFileItem item2 = (FrostSharedFileItem) o2;
            return item1.getFile().getPath().compareToIgnoreCase(item2.getFile().getPath());
        }
    }
}
