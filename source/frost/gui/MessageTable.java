package frost.gui;

import javax.swing.table.TableModel;

public class MessageTable extends SortedTable
{
    public MessageTable(TableModel m)
    {
        super(m);
        // set column sizes
        int[] widths = {30, 150, 250, 50, 150};
        for (int i = 0; i < widths.length; i++)
        {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // default for messages: sort by date descending
        sortedColumnIndex = 4;
        sortedColumnAscending = false;
        resortTable();
    }
}

