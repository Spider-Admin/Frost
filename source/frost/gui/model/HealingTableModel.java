package frost.gui.model;

public class HealingTableModel extends SortedTableModel
{
    protected final static String columnNames[] = {
        " "
    };
    protected final static Class columnClasses[] = {
        String.class 
    };

    public HealingTableModel()
    {
        super();
    }

    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    public String getColumnName(int column)
    {
        if( column >= 0 && column < columnNames.length )
            return columnNames[column];
        return null;
    }
    public int getColumnCount()
    {
        return columnNames.length;
    }
    public Class getColumnClass(int column)
    {
        if( column >= 0 && column < columnClasses.length )
            return columnClasses[column];
        return null;
    }

    public void setValueAt(Object aValue, int row, int column)
    {
    }

}
