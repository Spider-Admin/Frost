/*
  TranslateTableSorter.java
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
package frost.components.translate;

import java.awt.event.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.JTable;
import javax.swing.table.*;

/**
 * Sorting the table
 * @author Jan-Thomas Czornack
 */
public class TranslateTableSorter {

    private static Logger logger = Logger.getLogger(TranslateTableSorter.class.getName());

    public static int selectedColumn = 0;
    public static String columnName = "";

    /**
    * Removes all rows from a JTable
     *  @param table The table to be cleared
     */
    public static void removeAllRows(JTable table) {
        // Need to synchronize table accesses
        synchronized (table) {
            // Does an exception prevent release of the lock, better catch them
            try {
                ((DefaultTableModel) table.getModel()).getDataVector().clear();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "tableFun.removeAllRows NOT GOOD", e);
            }
        }
        table.updateUI();
    }

    /**
     * Adds a mouselistener to a JTable which is used to sort columns
     * @param table the JTable to add the mouselistener to
     */
    public static void addMouseListenerToHeaderInTable(JTable table) {
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    boolean ascending = (shiftPressed == 0);
                    sortByColumn(tableView, column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    /**
     * Compares two rows in a JTable. If you want to change the sorting behaviour
     * of JTables, do it here! Given is the selectedColumn and the columnName.
     */
    static final Comparator rowCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
            String value1 =
                (((String) ((Vector) o1).elementAt(selectedColumn)).trim())
                    .toLowerCase();
            String value2 =
                (((String) ((Vector) o2).elementAt(selectedColumn)).trim())
                    .toLowerCase();

            // Sort by alphabet on default
            return value1.compareTo(value2);
        }
    };

    /**
     * Sorts a JTable.
     * @param jTable the JTable to sort
     * @param column the column to sort
     * @param the direction
     */
    public static void sortByColumn(
        JTable jTable,
        int column,
        boolean ascending) {
        DefaultTableModel tableModel = (DefaultTableModel) jTable.getModel();
        int rowCount = tableModel.getRowCount();
        columnName = tableModel.getColumnName(column);
        logger.fine("Sorting the " + columnName + " column.");
        if (rowCount > 1) {
            int columnCount = tableModel.getColumnCount();
            selectedColumn = column;

            // Make new Array with TableData
            Vector[] tableArray = new Vector[rowCount];

            for (int i = 0; i < rowCount; i++) {
                Vector row = new Vector();
                for (int j = 0; j < columnCount; j++)
                    row.add((String) tableModel.getValueAt(i, j));
                tableArray[i] = row;
            }

            // Sort Array
            Arrays.sort(tableArray, rowCmp);

            // Erase old Tabledata
            removeAllRows(jTable);

            // Add rows
            String[] row = new String[columnCount];
            if (ascending) {
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++)
                        row[j] = (String) (tableArray[i].elementAt(j));
                    tableModel.addRow(row);
                }
            } else {
                for (int i = rowCount - 1; i >= 0; i--) {
                    for (int j = 0; j < columnCount; j++)
                        row[j] = (String) (tableArray[i].elementAt(j));
                    tableModel.addRow(row);
                }
            }
        }
    }

}
