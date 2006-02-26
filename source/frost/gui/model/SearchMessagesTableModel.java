package frost.gui.model;

import javax.swing.table.*;

public class SearchMessagesTableModel extends DefaultTableModel {

    static String columnNames[] = {
        "Col1",
        "Col2"
    };
    
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }
    public int getRowCount() { return 5; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col) {
        return "DATA!";
    }
    public boolean isCellEditable(int row, int col)
        { return true; }
    
    public void setValueAt(Object value, int row, int col) {
    }

}
