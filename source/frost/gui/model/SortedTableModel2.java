/*
  SortedTableModel2.java / Frost
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
package frost.gui.model;

import java.util.*;

import javax.swing.table.DefaultTableModel;

public class SortedTableModel2 extends DefaultTableModel
{
    private boolean bWasResized = false;

    // we always need to hold the actual sorting comparator to allow sorted insertion
    private ColumnComparator colComparator  = new ColumnComparator(0, true); // default

    public SortedTableModel2() { super(); }
    public SortedTableModel2(int rowCount, int columnCount) { super(rowCount,columnCount); }
    public SortedTableModel2(Object[][] data, Object[] columnNames) { super(data,columnNames); }
    public SortedTableModel2(Object[] columnNames, int rowCount) { super(columnNames,rowCount); }
    public SortedTableModel2(Vector columnNames, int rowCount) { super(columnNames,rowCount); }
    public SortedTableModel2(Vector data, Vector columnNames) { super(data,columnNames); }

    public boolean isSortable(int col)
    {
        return true;
    }

    public void sortModelColumn(int col, boolean ascending)
    {
        sortColumn(col,ascending);
    }

    private void sortColumn(int col, boolean ascending)
    {
        // sort this column
        colComparator = new ColumnComparator(col, ascending);
        Collections.sort(getDataVector(), colComparator);
    }

    public class ColumnComparator implements Comparator
    {
        protected int index;
        protected boolean ascending;

        public ColumnComparator(int index, boolean ascending)
        {
            this.index = index;
            this.ascending = ascending;
        }

        public int compare(Object one, Object two)
        {
            try {
                Vector vOne = (Vector)one;
                Vector vTwo = (Vector)two;
                Comparable cOne = (Comparable)vOne.elementAt(index);
                Comparable cTwo = (Comparable)vTwo.elementAt(index);

                if( ascending )
                {
                    return cOne.compareTo(cTwo);
                }
                else
                {
                    return cTwo.compareTo(cOne);
                }
            }
            catch(Exception e) { }
            return 1;
        }
    }

    /**
     * Adds a new row to this model. Updates display of this table. Row will be inserted sorted
     * if setted by constructor or <I>setSortingColumn</I>.
     *
     * @see #setSortingColumn
     */
    public void addRow(Vector row)
    {
        // compute pos to insert and insert node sorted into table
        int insertPos = Collections.binarySearch(getDataVector(), row, colComparator);
        if( insertPos < 0 )
        {
            // compute insertion pos
            insertPos = (insertPos+1)*-1;
        }
        else
        {
            // if such an item is already contained in search column,
            // determine last element and insert after
            insertPos =
                Collections.lastIndexOfSubList(getDataVector(), Collections.singletonList(getDataVector().elementAt(insertPos)));
            insertPos++; // insert AFTER last

        }
        insertRow(insertPos, row);
    }
    public void addRow(Object rowdata[])
    {
        addRow( new Vector( Arrays.asList(rowdata)) );
    }

    /**
     * Sets the value at <I>row</I> and <I>column</I> to <I>aValue</I>. This method
     * calls the <I>setValueAt</I> method of the <I>ITableMember</I> of row <I>row</I>.o
     *
     * @param aValue the new value
     * @param row Row for which the value will be changed.
     * @param column Column for which the value will be changed.
     */
    public void setValueAt(Object aValue, int row, int column)
    {
        super.setValueAt(aValue,row,column);
        Collections.sort(getDataVector(), colComparator);
    }

    /**
     * Returns true if this tableModel was resized by @see Utilities#sizeColumnWidthsToMaxMember
     *
     * return true if was resized otherwise false
     */
     public boolean wasResized()
     {
        return bWasResized;
     }

    /**
     * Sets that this model was resized or not
     *
     * @param newValue new value to be set
     */
    public void setResized(boolean newValue)
    {
        bWasResized = newValue;
    }

    /**
     * Indicates that the whole table should be repainted.
     */
    public void tableEntriesChanged()
    {
        fireTableRowsUpdated(0,getRowCount());
    }

}

