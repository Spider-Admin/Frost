package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.gui.model.*;
import frost.gui.objects.*;
import frost.*;

public class SearchTable extends SortedTable
{
    public SearchTable(TableModel m)
    {
        super(m);
        setDefaultRenderer( Object.class, new cellRenderer() );

        // set column sizes
        int[] widths = {250, 80, 80, 80, 80};
        for (int i = 0; i < widths.length; i++)
        {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // default for sort: sort by name ascending ?
        sortedColumnIndex = 0;
        sortedColumnAscending = true;
        resortTable();
    }

    /**
     * Adds all selected items in searchtable to download table.
     */
    public void addSelectedSearchItemsToDownloadTable(DownloadTable dlTable, int initialHTL)
    {
        SearchTableModel searchTableModel = (SearchTableModel)getModel();
        int[] selectedRows = getSelectedRows();

        for (int i = 0; i < selectedRows.length; i++)
        {
            FrostSearchItem searchItem = (FrostSearchItem)searchTableModel.getRow( selectedRows[i] );
            FrostDownloadItemObject dlItem = new FrostDownloadItemObject(searchItem, initialHTL);

            boolean isAdded = dlTable.addDownloadItem( dlItem ); // will not add if item is already in table
        }
    }

    /**
     * Builds a String with contains all selected files from searchtable as attachements.
     */
    public String getSelectedSearchItemsAsAttachmentsString()
    {
        SearchTableModel searchTableModel = (SearchTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        String attachments = "";
        for( int i = 0; i < selectedRows.length; i++ )
        {
            FrostSearchItemObject srItem = (FrostSearchItemObject)searchTableModel.getRow( selectedRows[i] );

            String key = srItem.getKey();
            String filename = srItem.getFilename();
            attachments += "<attached>" + filename + " * " + key + "</attached>\n";
        }
        return(attachments);
    }

    /**
     * This renderer renders rows in different colors, depending on state of search item.
     * States are: NONE, DOWNLOADED, DOWNLOADING
     */
    private class cellRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

            SearchTableModel model = (SearchTableModel)getModel();
            FrostSearchItemObject sItem = (FrostSearchItemObject)model.getRow(row);

            if( sItem.getState() == sItem.STATE_DOWNLOADED )
            {
                setForeground( Color.LIGHT_GRAY );
            }
            else if( sItem.getState() == sItem.STATE_DOWNLOADING )
            {
                setForeground( Color.BLUE );
            }
            else
            {
                // normal item, drawn in black
                setForeground( Color.BLACK );
            }
            return this;
        }
    }
}

