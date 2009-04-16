/*
  SortedTableModel.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
import java.util.logging.*;

import javax.swing.table.*;

import frost.gui.*;

public class SortedTableModel extends DefaultTableModel
{
    private static final Logger logger = Logger.getLogger(SortedTableModel.class.getName());

    private boolean bWasResized = false;
    private ArrayList rows = null;
    private SortedTable parentTable = null;

    // we always need to hold the actual sorting comparator to allow sorted insertion
    private ColumnComparator colComparator  = new ColumnComparator(0, true); // default

    public SortedTableModel()
    {
        super();
        rows = new ArrayList();
    }

    public void setParentTable(SortedTable t)
    {
        this.parentTable = t;
    }

    public boolean isSortable(int col)
    {
        return true;
    }

    @Override
    public int getRowCount()
    {
        if( rows == null )
            return 0;
        return rows.size();
    }

    public void sortModelColumn(int col, boolean ascending)
    {
        sortColumn(col,ascending);
    }
    private void sortColumn(int col, boolean ascending)
    {
        // sort this column
        colComparator = new ColumnComparator(col, ascending);
        if( rows.size() > 1 )
        {
            Collections.sort(rows, colComparator);
        }
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

        // uses implementation in ITableMember or default impl. in abstracttreemodel
        public int compare(Object one, Object two)
        {
            try {
                TableMember oOne = (TableMember)one;
                TableMember oTwo = (TableMember)two;

                if( ascending )
                {
                    return oOne.compareTo(oTwo, index);
                }
                else
                {
                    return oTwo.compareTo(oOne, index);
                }
            }
            catch(Exception e) { }
            return 1;
        }
    }

    /**
     * Adds a new row to this model. Updates display of this table. Row will be inserted sorted
     * if set by constructor or <I>setSortingColumn</I>.
     *
     * @see #setSortingColumn
     */
    public void addRow(TableMember member)
    {
        // compute pos to insert and insert node sorted into table
        int insertPos = Collections.binarySearch(rows, member, colComparator);
        if( insertPos < 0 )
        {
            // compute insertion pos
            insertPos = (insertPos+1)*-1;
        }
        else
        {
            // if such an item is already contained in search column,
            // determine last element and insert after
            insertPos = Collections.lastIndexOfSubList(rows, Collections.singletonList(rows.get(insertPos)));
            insertPos++; // insert AFTER last

        }
        insertRowAt(member, insertPos);
    }

    public void insertRowAt(TableMember member, int index)
    {
       if (index <= rows.size())
       {
          rows.add(index, member);
          fireTableRowsInserted(index,index);
       }
    }

    /**
     * Deletes the passed object obj.
     *
     * @param obj instance of ITableMember
     */
    public void deleteRow(TableMember obj)
    {
        if (obj!=null)
        {
            int i = rows.indexOf(obj);
            rows.remove(obj);
            if (i!=-1) fireTableRowsDeleted(i,i);
        }
    }

    /**
     * Updates the passed object obj.
     *
     * @param obj instance of ITableMember
     */
    public void updateRow(TableMember obj)
    {
        if (obj!=null)
        {
            int i = rows.indexOf(obj);
            if (i!=-1)
            {
                fireTableRowsUpdated(i,i);
                resortTable();
            }

        }
    }

    private void resortTable()
    {
        if( parentTable != null )
        {
            parentTable.resortTable();
        }
    }

    /**
     * Returns the row at index <I>row</I>.
     *
     * @param row Index of row
     * @return Instance of ITableMember at index row. <I>null</I> if index contains
     * no ITableMember
     */
    public TableMember getRow(int row)
    {
        if (row<getRowCount())
        {
            Object obj = rows.get(row);
            if (obj instanceof TableMember)
                return (TableMember) obj;
        }
        return null;
    }

    /**
     * Removes the row at index <I>row</I>.
     *
     * @param row Index of row
     * @return Instance of ITableMember at index row. <I>null</I> if index contains
     * no ITableMember
     */
    @Override
    public void removeRow(int row)
    {
        if (row<getRowCount())
        {
            Object obj = rows.get(row);
            if (obj instanceof TableMember)
                deleteRow((TableMember)obj);
        }
    }

    /**
     * Returns the value at <I>column</I> and <I>row</I>. Used by JTable.
     *
     * @param row Row for which the value will be returned.
     * @param column Column for which the value will be returned.
     * @return Value at <I>column</I> and <I>row</I>
     */
    @Override
    public Object getValueAt(int row, int column)
    {
        if (row>=getRowCount() || row<0) return null;

        TableMember obj = (TableMember)rows.get(row);
        if (obj == null)
            return null;
        else
            return obj.getValueAt(column);
    }

    /**
     * Clears this data model.
     */
    public void clearDataModel()
    {
        int size = rows.size();
        if (size>0)
        {
            rows = new ArrayList();
            fireTableRowsDeleted(0,size);
        }
        //System.gc();
    }

    /**
     * Indicates that the whole table should be repainted.
     */
    public void tableEntriesChanged()
    {
        fireTableRowsUpdated(0,getRowCount());
    }

    /**
     * Indicates that the rows <I>from</I> to <I>to</I> should be repainted.
     *
     * @param from first line that was changed
     * @param to last line that was changed
     */
    public void tableEntriesChanged(int from, int to)
    {
        fireTableRowsUpdated(from,to);
    }

    /**
     * Sets the value at <I>row</I> and <I>column</I> to <I>aValue</I>. This method
     * calls the <I>setValueAt</I> method of the <I>ITableMember</I> of row <I>row</I>.o
     *
     * @param aValue the new value
     * @param row Row for which the value will be changed.
     * @param column Column for which the value will be changed.
     */
    @Override
    public void setValueAt(Object aValue, int row, int column)
    {
        logger.severe("setValueAt() - ERROR: NOT IMPLEMENTED");
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
}


