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

public class SortedTable extends JTable
{
    protected int sortedColumnIndex = -1;
    protected boolean sortedColumnAscending = true;

    public SortedTable(TableModel model)
    {
        super(model);

        // model needs to know parent table to invoke sorting after updateRow
        if( model instanceof SortedTableModel )
        {
            ((SortedTableModel)model).setParentTable( this );
        }
        initSortHeader();
    }

    public void sortColumn(int col, boolean ascending)
    {
        if( ! (getModel() instanceof SortedTableModel) )
            return;
        SortedTableModel model = (SortedTableModel)getModel();

        // TODO: remember selected items, deselect all,
        //       sort and reselect the rememered items

        // sort this column
        model.sortModelColumn( col, ascending );
    }

    public void resortTable()
    {
        sortColumn( sortedColumnIndex, sortedColumnAscending );
    }


    public void setSavedSettings( int val, boolean val2 )
    {
        sortedColumnIndex = val;
        sortedColumnAscending = val2;
        SortedTableModel model = (SortedTableModel)getModel();
        sortColumn(sortedColumnIndex, sortedColumnAscending);
    }

    protected void initSortHeader()
    {
        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new SortHeaderRenderer());
        header.addMouseListener(new HeaderMouseListener());
    }

    public int getSortedColumnIndex()
    {
        return sortedColumnIndex;
    }

    public boolean isSortedColumnAscending()
    {
        return sortedColumnAscending;
    }

    // used by TablePopupMenuMouseListener
    public SortedTable instance()
    {
        return this;
    }

    class HeaderMouseListener implements MouseListener
    {
        public void mouseReleased(MouseEvent event) {}
        public void mousePressed(MouseEvent event) {}
        public void mouseClicked(MouseEvent event)
        {
            TableColumnModel colModel = getColumnModel();
            int index = colModel.getColumnIndexAtX(event.getX());
            int modelIndex = colModel.getColumn(index).getModelIndex();

            SortedTableModel model = (SortedTableModel)getModel();
            if( model.isSortable(modelIndex) )
            {
                // toggle ascension, if already sorted
                if( sortedColumnIndex == modelIndex )
                {
                    sortedColumnAscending = !sortedColumnAscending;
                }
                sortedColumnIndex = modelIndex;

                sortColumn(modelIndex, sortedColumnAscending);
            }
        }
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}
    }
}

