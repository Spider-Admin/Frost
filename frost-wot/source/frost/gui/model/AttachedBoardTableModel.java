package frost.gui.model;

import swingwtx.swing.table.DefaultTableModel;

public class AttachedBoardTableModel extends DefaultTableModel
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    protected final static String columnNames[] = {
        "Board Name",
        "Access rights", 
		"Description"
    };
    protected final static Class columnClasses[] = {
        String.class, //"Board Name",
        String.class, //"Access rights"
		String.class, //"Description"
    };

    public AttachedBoardTableModel()
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
}
