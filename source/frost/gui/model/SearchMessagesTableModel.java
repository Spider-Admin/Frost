/*
  SearchMessagesTableModel.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
