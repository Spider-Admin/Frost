/*
  TranslateTableModel.java
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

import javax.swing.table.DefaultTableModel;

public class TranslateTableModel extends DefaultTableModel
{
    public TranslateTableModel() {
        super();
    }

    protected final static String columnNames[] = {
    "Hardcoded text",
        "Translation",
    };

    protected final static Class columnClasses[] = {
    String.class, //"Hardcoded text",
    String.class, //"Translation",
    };

    public boolean isCellEditable(int row, int col) {
    if (col == 1)
        return true;
        return false;
    }

    public String getColumnName(int column) {
    if( column >= 0 && column < columnNames.length )
        return columnNames[column];
    return null;
    }

    public int getColumnCount() {
    return columnNames.length;
    }

    public Class getColumnClass(int column) {
    if( column >= 0 && column < columnClasses.length )
        return columnClasses[column];
    return null;
    }
}
