/*
  KnownBoardsTableModel.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class KnownBoardsTableModel extends SortedTableModel
{
	private Language language = null;

	protected final static String columnNames[] = new String[4];

    protected final static Class columnClasses[] =  {
        String.class,
        String.class,
        String.class,
        String.class
    };

	/**
	 * 
	 */
	public KnownBoardsTableModel() {
		super();
		language = Language.getInstance();
		refreshLanguage();
	}

    /**
	 * 
	 */
	private void refreshLanguage() {
		columnNames[0] = language.getString("KnownBoardsTableModel.Boardname");
		columnNames[1] = language.getString("Public key");
		columnNames[2] = language.getString("Private key");		
		columnNames[3] = language.getString("Description");		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int column)
    {
        if( column >= 0 && column < columnNames.length )
            return columnNames[column];
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int column)
    {
        if( column >= 0 && column < columnClasses.length )
            return columnClasses[column];
        return null;
    }
}
