/*
  TableFun.java
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  
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

package frost;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * Things that can be done with a JTable
 * @author Jantho
 */
public class TableFun {

    /**
     * Compares a String with all JTable elements of a certain column.
     * @param table the JTable
     * @param item the Element of which the existance should be checked
     * @param column the column in jTable probably containing the element
     * @return true if element exists, else false.
     */
    public static boolean exists(JTable table, String item, int column) {
	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
    	// Need to synchronize table accesses
	synchronized (table) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int rowCount = tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
		    if (((String)tableModel.getValueAt(i, column)).equals(item))
			return true;
		}
	    }
	    catch (Exception e){System.out.println("tableFun.exists NOT GOOD "+e.toString());}
	}
	return false;
    }

    /**
     * Sets values of one column of all selected rows to one value.
     * @param jTable the JTable
     * @param column the column
     * @param value the value
     */
    public static void setSelectedRowsColumnValue(JTable jTable, int column, String value) {
	DefaultTableModel tableModel = (DefaultTableModel)jTable.getModel();
    	// Need to synchronize table accesses
	synchronized (jTable) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int[] selectedRows = jTable.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++)
		    tableModel.setValueAt(value, selectedRows[i], column);
	    }
	    catch (Exception e){System.out.println("tablefun.setSelectedRows NOT GOOD "+e.toString());}
	}
    }

    /**
     * Removes selected rows from a JTable
     * @param table Table to remove selected rows from
     */
    public static void removeSelectedRows(JTable jTable) {
	DefaultTableModel tableModel = (DefaultTableModel)jTable.getModel();
    	// Need to synchronize table accesses
	synchronized (jTable) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int[] selectedRows = jTable.getSelectedRows();
		for (int i = selectedRows.length - 1; i >= 0; i--)
		    tableModel.removeRow(selectedRows[i]);
	    }
	    catch (Exception e){System.out.println("tableFun.removeSelectedRows NOT GOOD "+e.toString());}
	}
    }

    /**
     * Removes all rows from a JTable
     * @param table The table to be cleared
     */
    public static void removeAllRows(JTable table) {
    	// Need to synchronize table accesses
	synchronized (table) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		((DefaultTableModel)table.getModel()).getDataVector().clear();
	    }
	    catch (Exception e){System.out.println("tableFun.removeAllRows NOT GOOD "+e.toString());}
	}
	table.updateUI();
    }

    /**
     * Moves selected table entries to the top
     * @param table Table to use
     */
    public static void moveSelectedEntriesUp(JTable table) {
	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
	int columnCount = table.getColumnCount();
    	// Need to synchronize table accesses
	synchronized (table) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int[] selectedRows = table.getSelectedRows();

		String[] first = new String[columnCount];
		String[] second = new String[columnCount];
		String[] third = new String[columnCount];

		for (int i = 0; i < selectedRows.length; i++) {

		    // Backup original data
		    for (int j = 0; j < columnCount; j++)
			first[j]= (String)tableModel.getValueAt(selectedRows[i], j);

		    // Move rows one down
		    for (int j = selectedRows[i]; j > 0; j--) {
			for (int k = 0; k < columnCount; k++) {
			    second[k] = (String)tableModel.getValueAt(j - 1, k);
			    third[k] = (String)tableModel.getValueAt(j, k);
			    tableModel.setValueAt(second[k], j, k);
			    tableModel.setValueAt(third[k], j - 1, k);
			}
		    }

		    // Move row i on top
		    for (int j = 0; j < columnCount; j++)
			tableModel.setValueAt(first[j], 0, j);

		    table.setRowSelectionInterval(0, 0);
		}
	    }
	    catch (Exception e){System.out.println("tableFun.moveSelectedUP NOT GOOD "+e.toString());}
	}
    }

    /**
     * Moves selected table entries to the bottom
     * @param table Table to use
     */
    public static void moveSelectedEntriesDown(JTable table) {
	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
	int columnCount = table.getColumnCount();
    	// Need to synchronize table accesses
	synchronized (table) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int[] selectedRows = table.getSelectedRows();
		int rows = table.getRowCount();

		String[] first = new String[columnCount];
		String[] second = new String[columnCount];
		String[] third = new String[columnCount];

		for (int i = selectedRows.length - 1; i >= 0; i--) {

		    // Backup original data
		    for (int j = 0; j < columnCount; j++)
			first[j] = (String)tableModel.getValueAt(selectedRows[i], j);

		    // Move rows one down
		    for (int j = selectedRows[i]; j < rows - 1; j++) {
			for (int k = 0; k < columnCount; k++) {
			    second[k] = (String)tableModel.getValueAt(j + 1, k);
			    third[k] = (String)tableModel.getValueAt(j, k);
			    tableModel.setValueAt(second[k], j, k);
			    tableModel.setValueAt(third[k], j + 1, k);
			}
		    }

		    // Move row i on top
		    for (int j = 0; j < columnCount; j++)
			tableModel.setValueAt(first[j], rows - 1, j);

		    table.setRowSelectionInterval(rows - 1, rows - 1);
		}
	    }
	    catch (Exception e){System.out.println("tableFun.moveSelctedDOWN NOT GOOD "+e.toString());}
	}
    }

    /**
     * Sets the preferred width of each column of a table
     * @param table The table to use
     * @param width Integer array of column widths
     */
    public static void setColumnWidth(JTable table, int[] width) {
    	// Don't think we need to synchronize here, right???
	for (int i = 0; i < width.length; i++)
	    table.getColumnModel().getColumn(i).setPreferredWidth(width[i]);
    }

    /**
     * Loads table entries into a table
     * @param table The table to load the data into
     * @param file The file to read the data from
     * @param replace If true, existing tabledata will be replaced.
     * If false, the new data will be added at the end of the table
     */
    public static void loadTable(JTable table, File file, boolean replace) {
	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
	Vector tmp = FileAccess.readLines(file);
	int lineCount = tmp.size();
	int columnCount = tableModel.getColumnCount();
	String[] row = new String[columnCount];
    	// Need to synchronize table accesses
	synchronized (table) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		if (replace)
		    TableFun.removeAllRows(table);

		if (file.isFile()) {
		    for (int i = 0; i < lineCount; i += columnCount) {
			if (i + columnCount < lineCount + 1) {
			    for (int j = 0; j < columnCount; j++)
				row[j] = (String)tmp.elementAt(i + j);
			    tableModel.addRow(row);
			}
		    }
		}
	    }
	    catch (Exception e){System.out.println("tableFun.loadTable NOT GOOD "+e.toString());}
	}
    }

    /**
     * Saves table entries to a file
     * @param table The table containing the entries to save
     * @param file The destination file. If it exists it will be overwritten.
     */
    public static void saveTable(JTable table, File file) {
	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
	StringBuffer text = new StringBuffer();
	int columnCount = tableModel.getColumnCount();
    	// Need to synchronize table accesses
	synchronized (table) {
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int rowCount = tableModel.getRowCount();

		for (int i = 0; i < rowCount; i++) {
		    for (int j = 0; j < columnCount; j++)
			text.append(tableModel.getValueAt(i, j)+"\r\n");
		}
	    }
	    catch (Exception e){System.out.println("tableFun.saveTable NOT GOOD "+e.toString());}
	}
	FileAccess.writeFile(text.toString(), file);
    }
}
