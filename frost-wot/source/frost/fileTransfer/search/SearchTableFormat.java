/*
  SearchTableFormat.java / Frost
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
package frost.fileTransfer.search;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.gui.*;
import frost.util.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class SearchTableFormat extends SortedTableFormat implements LanguageListener, PropertyChangeListener {

    private static ImageIcon hasMoreInfoIcon = new ImageIcon((MainFrame.class.getResource("/data/info.png")));

    private Language language;

    private final static int COLUMN_COUNT = 9;

    private String offline;
    private String sharing;
    private String downloading;
    private String downloaded;
    
    private String sourceCountTooltip;
    
    private SortedModelTable modelTable;

    private boolean showColoredLines;

    public SearchTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new FileNameComparator(), 0);
        setComparator(new SizeComparator(), 1);
        setComparator(new StateComparator(), 2);
        setComparator(new LastUploadedComparator(), 3);
        setComparator(new LastReceivedComparator(), 4);
        setComparator(new RatingComparator(), 5);
        setComparator(new CommentComparator(), 6);
        setComparator(new KeywordsComparator(), 7);
        setComparator(new SourcesComparator(), 8);
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("SearchPane.resultTable.filename"));
        setColumnName(1, language.getString("SearchPane.resultTable.size"));
        setColumnName(2, language.getString("SearchPane.resultTable.state"));
        setColumnName(3, language.getString("SearchPane.resultTable.lastUploaded"));
        setColumnName(4, language.getString("SearchPane.resultTable.lastReceived"));
        setColumnName(5, language.getString("SearchPane.resultTable.rating"));
        setColumnName(6, language.getString("SearchPane.resultTable.comment"));
        setColumnName(7, language.getString("SearchPane.resultTable.keywords"));
        setColumnName(8, language.getString("SearchPane.resultTable.sources"));

        offline =     language.getString("SearchPane.resultTable.states.offline");
        sharing =     language.getString("SearchPane.resultTable.states.sharing");
        downloading = language.getString("SearchPane.resultTable.states.downloading");
        downloaded =  language.getString("SearchPane.resultTable.states.downloaded");
        
        sourceCountTooltip = language.getString("SearchPane.resultTable.sources.tooltip");

        refreshColumnNames();
    }

    public Object getCellValue(ModelItem item, int columnIndex) {
        if( item == null ) {
            return "*null*";
        }
        FrostSearchItem searchItem = (FrostSearchItem) item;
        switch (columnIndex) {
            case 0 :    //Filename
                return searchItem.getFilename();

            case 1 :    //Size
                return SizeFormatter.formatSize(searchItem.getSize().longValue());

            case 2 :    //State
                return getStateStr(searchItem.getState());

            case 3 :    //lastUploaded
                return searchItem.getLastUploadedStr();

            case 4 :    //lastReceived (=lastSeen)
                return searchItem.getLastReceivedString();

            case 5 :    //rating
                return RatingStringProvider.getRatingString(searchItem.getRating().intValue());

            case 6 :    //comment
                return searchItem.getComment();

            case 7 :    // keyword
                return searchItem.getKeywords();

            case 8 :    //sources
                return searchItem.getSourceCount();

            default:
                return "**ERROR**";
        }
    }

    private String getStateStr(int state) {
        String stateString = "";
        switch (state) {
            case FrostSearchItem.STATE_OFFLINE :
                stateString = offline;
                break;

            case FrostSearchItem.STATE_UPLOADING :
                stateString = sharing;
                break;

            case FrostSearchItem.STATE_DOWNLOADING :
                stateString = downloading;
                break;

            case FrostSearchItem.STATE_DOWNLOADED :
                stateString = downloaded;
                break;
        }
        return stateString;
    }

    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    public void customizeTable(ModelTable lModelTable) {
        super.customizeTable(lModelTable);
        
        modelTable = (SortedModelTable) lModelTable;
        
        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        
        modelTable.setSortedColumn(0, true);

        TableColumnModel columnModel = modelTable.getTable().getColumnModel();
        
        RightAlignRenderer rightAlignRenderer = new RightAlignRenderer();
        ShowColoredLinesRenderer showColoredLinesRenderer = new ShowColoredLinesRenderer();
        
        columnModel.getColumn(0).setCellRenderer(new FileNameRenderer()); // filename
        columnModel.getColumn(1).setCellRenderer(rightAlignRenderer); // size
        columnModel.getColumn(2).setCellRenderer(showColoredLinesRenderer); // age
        columnModel.getColumn(3).setCellRenderer(showColoredLinesRenderer); // last uploaded
        columnModel.getColumn(4).setCellRenderer(showColoredLinesRenderer); // last received
        columnModel.getColumn(5).setCellRenderer(showColoredLinesRenderer); // rating
        columnModel.getColumn(6).setCellRenderer(new ShowContentTooltipRenderer()); // comment
        columnModel.getColumn(7).setCellRenderer(showColoredLinesRenderer); // keywords
        columnModel.getColumn(8).setCellRenderer(new SourceCountRenderer()); // source count
        
        int[] widths = { 250, 30, 40, 20, 20, 10, 50, 80, 15 };
        for (int i = 0; i < widths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private class StateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSearchItem item1 = (FrostSearchItem) o1;
            FrostSearchItem item2 = (FrostSearchItem) o2;
            int i1 = item1.getState();
            int i2 = item2.getState();
            if( i1 < i2 ) {
                return -1;
            }
            if( i1 > i2 ) {
                return 1;
            }
            return 0;
        }
    }

    private class SizeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSearchItem item1 = (FrostSearchItem) o1;
            FrostSearchItem item2 = (FrostSearchItem) o2;
            return item1.getSize().compareTo(item2.getSize());
        }
    }

    private class FileNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostSearchItem item1 = (FrostSearchItem) o1;
            FrostSearchItem item2 = (FrostSearchItem) o2;
            return item1.getFilename().compareToIgnoreCase(item2.getFilename());
        }
    }

    private class CommentComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String comment1 = ((FrostSearchItem) o1).getComment();
            String comment2 = ((FrostSearchItem) o2).getComment();
            return comment1.compareToIgnoreCase(comment2);
        }
    }

    private class KeywordsComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String keywords1 = ((FrostSearchItem) o1).getKeywords();
            String keywords2 = ((FrostSearchItem) o2).getKeywords();
            return keywords1.compareToIgnoreCase(keywords2);
        }
    }

    private class RatingComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Integer rating1 = ((FrostSearchItem) o1).getRating();
            Integer rating2 = ((FrostSearchItem) o2).getRating();
            return rating1.compareTo(rating2);
        }
    }

    private class LastReceivedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            long l1 = ((FrostSearchItem) o1).getFrostFileListFileObject().getLastReceived();
            long l2 = ((FrostSearchItem) o2).getFrostFileListFileObject().getLastReceived();
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
            long l1 = ((FrostSearchItem) o1).getFrostFileListFileObject().getLastUploaded();
            long l2 = ((FrostSearchItem) o2).getFrostFileListFileObject().getLastUploaded();
            if( l1 < l2 ) {
                return -1;
            }
            if( l1 > l2 ) {
                return 1;
            }
            return 0;
        }
    }

    private class SourcesComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Integer sources1 = ((FrostSearchItem) o1).getSourceCount();
            Integer sources2 = ((FrostSearchItem) o2).getSourceCount();
            return sources1.compareTo(sources2);
        }
    }

    private class ShowContentTooltipRenderer extends ShowColoredLinesRenderer {
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
                if( tooltip.length() == 0 ) {
                    tooltip = null;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class RightAlignRenderer extends ShowColoredLinesRenderer {
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
     * This renderer renders the column "FileName" in different colors,
     * depending on state of search item.
     * States are: NONE, DOWNLOADED, DOWNLOADING, UPLOADING
     */
    private class FileNameRenderer extends ShowContentTooltipRenderer {

        public FileNameRenderer() {
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

            if (!isSelected) {
                ModelItem item = modelTable.getItemAt(row); //It may be null
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
    
    private class SourceCountRenderer extends ShowColoredLinesRenderer {

        final javax.swing.border.EmptyBorder border = new javax.swing.border.EmptyBorder(0, 0, 0, 3);
        
        public SourceCountRenderer() {
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

            ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                FrostSearchItem searchItem = (FrostSearchItem) item;
                if( searchItem.hasInfosFromMultipleSources().booleanValue() ) {
                    setIcon(hasMoreInfoIcon);
                } else {
                    setIcon(null);
                }
            } else {
                setIcon(null);
            }
            setToolTipText(sourceCountTooltip);
            return this;
        }
    }
    
    private class ShowColoredLinesRenderer extends DefaultTableCellRenderer {
        public ShowColoredLinesRenderer() {
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

            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
            modelTable.fireTableDataChanged();
        }
    }
}
