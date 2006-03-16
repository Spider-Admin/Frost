package frost.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.gui.model.*;
import frost.gui.objects.*;
import frost.messages.*;

public class SearchMessagesResultTable extends SortedTable {
    
    private CellRenderer cellRenderer = new CellRenderer();
    
    public SearchMessagesResultTable(SearchMessagesTableModel m) {
        super(m);

        setDefaultRenderer(Object.class, cellRenderer);

        // default for messages: sort by date descending
        sortedColumnIndex = 5;
        sortedColumnAscending = false;
        resortTable();
    }
    
    /**
     * Save the current column positions and column sizes for restore on next startup.
     * 
     * @param frostSettings
     */
//    public void saveLayout(SettingsClass frostSettings) {
//        TableColumnModel tcm = getColumnModel();
//        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
//            TableColumn tc = tcm.getColumn(columnIndexInTable);
//            int columnIndexInModel = tc.getModelIndex();
//            // save the current index in table for column with the fix index in model
//            frostSettings.setValue("messagetable.tableindex.modelcolumn."+columnIndexInModel, columnIndexInTable);
//            // save the current width of the column
//            int columnWidth = tc.getWidth();
//            frostSettings.setValue("messagetable.columnwidth.modelcolumn."+columnIndexInModel, columnWidth);
//        }
//    }

    /**
     * Load the saved column positions and column sizes.
     * 
     * @param frostSettings
     */
//    public void loadLayout(SettingsClass frostSettings) {
//        TableColumnModel tcm = getColumnModel();
//        
//        // load the saved tableindex for each column in model, and its saved width
//        int[] tableToModelIndex = new int[tcm.getColumnCount()];
//        int[] columnWidths = new int[tcm.getColumnCount()];
//        
//        for(int x=0; x < tableToModelIndex.length; x++) {
//            String indexKey = "messagetable.tableindex.modelcolumn."+x;
//            if( frostSettings.getObjectValue(indexKey) == null ) {
//                return; // column not found, abort
//            }
//            // build array of table to model associations
//            int tableIndex = frostSettings.getIntValue(indexKey);
//            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
//                return; // invalid table index value
//            }
//            tableToModelIndex[tableIndex] = x;
//            
//            String widthKey = "messagetable.columnwidth.modelcolumn."+x;
//            if( frostSettings.getObjectValue(widthKey) == null ) {
//                return; // column not found, abort
//            }
//            // build array of table to model associations
//            int columnWidth = frostSettings.getIntValue(widthKey);
//            if( columnWidth <= 0 ) {
//                return; // invalid column width
//            }
//            columnWidths[x] = columnWidth;
//        }
//        // columns are currently added in model order, remove them all and save in an array
//        // while on it, set the loaded width of each column 
//        TableColumn[] tcms = new TableColumn[tcm.getColumnCount()];
//        for(int x=tcms.length-1; x >= 0; x--) {
//            tcms[x] = tcm.getColumn(x);
//            tcm.removeColumn(tcms[x]);
//            
//            tcms[x].setPreferredWidth(columnWidths[x]);
//        }
//        // add the columns in order loaded from settings
//        for(int x=0; x < tableToModelIndex.length; x++) {
//            tcm.addColumn(tcms[tableToModelIndex[x]]);
//        }
//    }

    /**
     * This renderer renders rows in different colors.
     * New messages gets a bold look, messages with attachments a blue color.
     * Encrypted messages get a red color, no matter if they have attachments.
     */
    private class CellRenderer extends DefaultTableCellRenderer
    {
        private Font boldFont = null;
        private Font normalFont = null;
        private boolean isDeleted = false;
        private final Color col_good    = new Color(0x00, 0x80, 0x00);
        private final Color col_check   = new Color(0xFF, 0xCC, 0x00);
        private final Color col_observe = new Color(0x00, 0xD0, 0x00);
        private final Color col_bad     = new Color(0xFF, 0x00, 0x00);
        
        public CellRenderer() {
            Font baseFont = SearchMessagesResultTable.this.getFont();
            normalFont = baseFont.deriveFont(Font.PLAIN);
            boldFont = baseFont.deriveFont(Font.BOLD);
        }
        
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            if(isDeleted) {
                Dimension size = getSize();
                g.drawLine(0, size.height / 2, size.width, size.height / 2);
            }
        }
        
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            SearchMessagesTableModel model = (SearchMessagesTableModel) getModel();
            FrostSearchResultMessageObject msg = (FrostSearchResultMessageObject) model.getRow(row);
            
            // get the original model column index (maybe columns were reordered by user) 
            TableColumn tableColumn = getColumnModel().getColumn(column);
            column = tableColumn.getModelIndex();

            // do nice things for FROM and SIG column and BOARD column
            if( column == 1 ) {
                // FROM
                // first set font, bold for new msg or normal
                if (msg.isMessageNew()) {
                    setFont(boldFont);
                } else {
                    setFont(normalFont);
                }
                // now set color
                if (!isSelected) {
                    if( msg.getRecipient() != null && msg.getRecipient().length() > 0) {
                        setForeground(Color.RED);
                    } else if (msg.containsAttachments()) {
                        setForeground(Color.BLUE);
                    } else {
                        setForeground(Color.BLACK);
                    }
                }
            } else if( column == 2 ) {
                // BOARD - gray for archived msgs
                setFont(normalFont);
                if (!isSelected) {
                    if( msg.isMessageArchived() ) {
                        setForeground(Color.GRAY);
                    } else {
                        setForeground(Color.BLACK);
                    }
                }
            } else if( column == 4 ) {
                // SIG
                // state == good/bad/check/observe -> bold and coloured
                int state = msg.getMsgStatus();
                if( state == VerifyableMessageObject.xGOOD ) {
                    setFont(boldFont);
                    setForeground(col_good);
                } else if( state == VerifyableMessageObject.xCHECK ) {
                    setFont(boldFont);
                    setForeground(col_check);
                } else if( state == VerifyableMessageObject.xOBSERVE ) {
                    setFont(boldFont);
                    setForeground(col_observe);
                } else if( state == VerifyableMessageObject.xBAD ) {
                    setFont(boldFont);
                    setForeground(col_bad);
                } else {
                    setFont(normalFont);
                    if (!isSelected) {
                        setForeground(Color.BLACK);
                    }
                }
            } else {
                setFont(normalFont);
                if (!isSelected) {
                    setForeground(Color.BLACK);
                }
            }
            
            setDeleted(msg.isDeleted());
            
            return this;
        }

        /* (non-Javadoc)
         * @see java.awt.Component#setFont(java.awt.Font)
         */
        public void setFont(Font font) {
            super.setFont(font);
            normalFont = font.deriveFont(Font.PLAIN);
            boldFont = font.deriveFont(Font.BOLD);
        }
        
        public void setDeleted(boolean value) {
            isDeleted = value;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#createDefaultColumnsFromModel()
     */
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();

        // set column sizes
        int[] widths = { 30, 125, 80, 250, 75, 150 };
        for (int i = 0; i < widths.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
        super.setFont(font);
        if (cellRenderer != null) {
            cellRenderer.setFont(font);
        }
        setRowHeight(font.getSize() + 5);
    }
}
