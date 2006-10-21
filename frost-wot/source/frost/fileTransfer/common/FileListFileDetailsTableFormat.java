/*
  SearchItemPropertiesTableFormat.java / Frost
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
package frost.fileTransfer.common;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.gui.*;
import frost.identities.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class FileListFileDetailsTableFormat extends SortedTableFormat implements LanguageListener {
    
    private String stateNever;

    private Language language = Language.getInstance();;
    
    private static ImageIcon ICON_GOOD = null;
    private static ImageIcon ICON_OBSERVE = null;
    private static ImageIcon ICON_CHECK = null;
    private static ImageIcon ICON_BAD = null;

    private final static int COLUMN_COUNT = 8;

    public FileListFileDetailsTableFormat() {
        super(COLUMN_COUNT);

        refreshLanguage();

        setComparator(new FileNameComparator(), 0);
        setComparator(new OwnerComparator(), 1);
        setComparator(new IdentityStateComparator(), 2);
        setComparator(new RatingComparator(), 3);
        setComparator(new CommentComparator(), 4);
        setComparator(new KeywordsComparator(), 5);
        setComparator(new LastUploadedComparator(), 6);
        setComparator(new LastReceivedComparator(), 7);
        
        loadIcons();
    }
    
    private void loadIcons() {
        if( ICON_GOOD == null ) {
            // load all icons
            ICON_GOOD = new ImageIcon(getClass().getResource("/data/trust.gif"));
            ICON_GOOD = new ImageIcon(ICON_GOOD.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH));
            ICON_OBSERVE = new ImageIcon(getClass().getResource("/data/observe.gif"));
            ICON_OBSERVE = new ImageIcon(ICON_OBSERVE.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH));
            ICON_CHECK = new ImageIcon(getClass().getResource("/data/check.gif"));
            ICON_CHECK = new ImageIcon(ICON_CHECK.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH));
            ICON_BAD = new ImageIcon(getClass().getResource("/data/nottrust.gif"));
            ICON_BAD = new ImageIcon(ICON_BAD.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH));
        }
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("FileListFileDetailsDialog.table.filename"));
        setColumnName(1, language.getString("FileListFileDetailsDialog.table.owner"));
        setColumnName(2, language.getString("FileListFileDetailsDialog.table.trustState"));
        setColumnName(3, language.getString("FileListFileDetailsDialog.table.rating"));
        setColumnName(4, language.getString("FileListFileDetailsDialog.table.comment"));
        setColumnName(5, language.getString("FileListFileDetailsDialog.table.keywords"));
        setColumnName(6, language.getString("FileListFileDetailsDialog.table.lastUploaded"));
        setColumnName(7, language.getString("FileListFileDetailsDialog.table.lastReceived"));

        stateNever = language.getString("FileListFileDetailsDialog.table.state.never");

        refreshColumnNames();
    }

    public Object getCellValue(ModelItem item, int columnIndex) {
        FileListFileDetailsItem searchItem = (FileListFileDetailsItem) item;
        switch (columnIndex) {
            case 0 :    // filename
                return searchItem.getFileOwner().getName();

            case 1 :    // owner
                return searchItem.getFileOwner().getOwner();

            case 2 :    // state
                return searchItem.getOwnerIdentity();

            case 3 :    // rating
                return RatingStringProvider.getRatingString( searchItem.getFileOwner().getRating() );

            case 4 :    // comment
                return searchItem.getDisplayComment();

            case 5 :    // keyword
                return searchItem.getDisplayKeywords();

            case 6 :    // lastUploaded
                if( searchItem.getDisplayLastUploaded().length() == 0 ) {
                    return stateNever;
                } else {
                    return searchItem.getDisplayLastUploaded();
                }

            case 7 :    // lastReceived
                if( searchItem.getDisplayLastReceived().length() == 0 ) {
                    return stateNever;
                } else {
                    return searchItem.getDisplayLastReceived();
                }

            default:
                return "**ERROR**";
        }
    }

    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    public void customizeTable(ModelTable modelTable) {
        super.customizeTable(modelTable);
        
        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        TableColumnModel columnModel = modelTable.getTable().getColumnModel();

        ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();
        
        columnModel.getColumn(0).setCellRenderer(showContentTooltipRenderer); // filename
        columnModel.getColumn(1).setCellRenderer(showContentTooltipRenderer); // owner
        columnModel.getColumn(2).setCellRenderer(new IdentityStateRenderer()); // id state
        
        columnModel.getColumn(4).setCellRenderer(showContentTooltipRenderer); // comment
        columnModel.getColumn(5).setCellRenderer(showContentTooltipRenderer); // keywords

        if( !loadTableLayout(columnModel) ) {
            // Sets the relative widths of the columns
            int[] widths = { 150, 80, 20, 20, 80, 80, 55, 55 };
            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }
    
    public void saveTableLayout(ModelTable modelTable) {
        TableColumnModel tcm = modelTable.getTable().getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            TableColumn tc = tcm.getColumn(columnIndexInTable);
            int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            Core.frostSettings.setValue("FileListFileDetailsDialog.tableindex.modelcolumn."+columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            int columnWidth = tc.getWidth();
            Core.frostSettings.setValue("FileListFileDetailsDialog.columnwidth.modelcolumn."+columnIndexInModel, columnWidth);
        }
    }
    
    private boolean loadTableLayout(TableColumnModel tcm) {
        
        // load the saved tableindex for each column in model, and its saved width
        int[] tableToModelIndex = new int[tcm.getColumnCount()];
        int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            String indexKey = "FileListFileDetailsDialog.tableindex.modelcolumn."+x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            String widthKey = "FileListFileDetailsDialog.columnwidth.modelcolumn."+x;
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
            tcms[x].setPreferredWidth(columnWidths[x]);
        }
        // add the columns in order loaded from settings
        for(int x=0; x < tableToModelIndex.length; x++) {
            tcm.addColumn(tcms[tableToModelIndex[x]]);
        }
        return true;
    }

    private class FileNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getFileOwner().getName().compareToIgnoreCase(item2.getFileOwner().getName());
        }
    }

    private class OwnerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getFileOwner().getOwner().compareToIgnoreCase(item2.getFileOwner().getOwner());
        }
    }

    private class IdentityStateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            Integer i1 = new Integer(item1.getOwnerIdentity().getState());
            Integer i2 = new Integer(item2.getOwnerIdentity().getState());
            return i1.compareTo(i2);
        }
    }

    private class RatingComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            int val1 = ((FileListFileDetailsItem) o1).getFileOwner().getRating();
            int val2 = ((FileListFileDetailsItem) o2).getFileOwner().getRating();
            return new Long(val1).compareTo(new Long(val2));
        }
    }

    private class CommentComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getDisplayComment().compareToIgnoreCase(item2.getDisplayComment());
        }
    }

    private class KeywordsComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getDisplayKeywords().compareToIgnoreCase(item2.getDisplayKeywords());
        }
    }

    private class LastUploadedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            long val1 = ((FileListFileDetailsItem) o1).getFileOwner().getLastUploaded();
            long val2 = ((FileListFileDetailsItem) o2).getFileOwner().getLastUploaded();
            return new Long(val1).compareTo(new Long(val2));
        }
    }

    private class LastReceivedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            long val1 = ((FileListFileDetailsItem) o1).getFileOwner().getLastReceived();
            long val2 = ((FileListFileDetailsItem) o2).getFileOwner().getLastReceived();
            return new Long(val1).compareTo(new Long(val2));
        }
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
                if( tooltip.length() == 0 ) {
                    tooltip = null;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }
    
    private class IdentityStateRenderer extends DefaultTableCellRenderer {
        public IdentityStateRenderer() {
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
            
            Identity id = (Identity) value;
            if( id == null ) {
                setIcon(null);
            } else if( id.isGOOD() ) {
                setIcon(ICON_GOOD);
            } else if( id.isOBSERVE() ) {
                setIcon(ICON_OBSERVE);
            } else if( id.isCHECK() ) {
                setIcon(ICON_CHECK);
            } else if( id.isBAD() ) {
                setIcon(ICON_BAD);
            } else {
                setIcon(null);
            }
            setText("");
            return this;
        }
    }

}
