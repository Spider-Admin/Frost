/*
  SearchTable.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui;

import java.awt.*;
import java.util.LinkedList;

import javax.swing.JTable;
import javax.swing.table.*;

import frost.frame1;
import frost.gui.model.SearchTableModel;
import frost.gui.objects.*;
import frost.identities.Identity;

public class SearchTable extends SortedTable
{
    public SearchTable(TableModel m)
    {
        super(m);
        CellRenderer cellRenderer = new CellRenderer();
        setDefaultRenderer( Object.class, cellRenderer );
        setDefaultRenderer( Number.class, cellRenderer );

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
    public void addSelectedSearchItemsToDownloadTable(DownloadTable dlTable)
    {
        SearchTableModel searchTableModel = (SearchTableModel)getModel();
        int[] selectedRows = getSelectedRows();

        for (int i = 0; i < selectedRows.length; i++)
        {
            FrostSearchItem searchItem = (FrostSearchItem)searchTableModel.getRow( selectedRows[i] );
            FrostDownloadItemObject dlItem = new FrostDownloadItemObject(searchItem);

            boolean isAdded = dlTable.addDownloadItem( dlItem ); // will not add if item is already in table
        }
    }
    
    /**
     * returns a list of the identities of the owners of the selected items
     */
    public java.util.List getSelectedItemsOwners() {
    	SearchTableModel searchTableModel = (SearchTableModel)getModel();
        int[] selectedRows = getSelectedRows();
	java.util.List result = new LinkedList();
	for (int i =0;i<selectedRows.length;i++) {
		FrostSearchItemObject srItem = (FrostSearchItemObject)searchTableModel.getRow( selectedRows[i] );
		String owner = srItem.getOwner();
		//check if null or from myself
		if (owner == null ||
			owner.compareTo(frame1.getMyId().getUniqueName())==0) continue;
			
		//see if already on some list
		Identity id = frame1.getFriends().Get(owner);
		if (id==null)
			id = frame1.getEnemies().Get(owner);
		//and if still null, add the string
		if (id==null)
			result.add(owner);
		else 
			result.add(id);
		
	}
	return result;
	
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
     * States are: NONE, DOWNLOADED, DOWNLOADING, UPLOADING
     */
    private class CellRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

            if( !isSelected )
            {
                SearchTableModel model = (SearchTableModel)getModel();
                FrostSearchItemObject sItem = (FrostSearchItemObject)model.getRow(row);

                if( sItem.getState() == FrostSearchItemObject.STATE_DOWNLOADED )
                {
                    setForeground( Color.LIGHT_GRAY );
                }
                else if( sItem.getState() == FrostSearchItemObject.STATE_DOWNLOADING )
                {
                    setForeground( Color.BLUE );
                }
                else if(sItem.getState() == FrostSearchItemObject.STATE_UPLOADING )
                {
                    setForeground( Color.GREEN );
                }
                else
                {
                    // normal item, drawn in black
                    setForeground( Color.BLACK );
                }
            }
            return this;
        }
    }
}

