/*
  BoardInfoTableModel.java / Frost
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

import frost.gui.translation.*;


public class BoardInfoTableModel extends SortedTableModel implements LanguageListener
{
	private UpdatingLanguageResource languageResource = null;

	protected final static String columnNames[] = new String[5];

    protected final static Class columnClasses[] =  {
        String.class, //LangRes.getString("Board"),
        String.class, //LangRes.getString("State"),
        Integer.class, //LangRes.getString("Messages"),
        Integer.class, //LangRes.getString("New messages"),
        Integer.class //LangRes.getString("Files")
    };

	public BoardInfoTableModel(UpdatingLanguageResource newLanguageResource) {
		super();
		languageResource = newLanguageResource;
		refreshLanguage();
	}

    /**
	 * 
	 */
	private void refreshLanguage() {
	   	columnNames[0] = languageResource.getString("Board");
		columnNames[1] = languageResource.getString("State");
		columnNames[2] = languageResource.getString("Messages");
		columnNames[3] = languageResource.getString("Messages Today");
		columnNames[4] = languageResource.getString("Files");
		
		fireTableStructureChanged();		
	}
	
	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		refreshLanguage();
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
