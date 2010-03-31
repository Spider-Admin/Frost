/*
  BoardInfoTableModel.java / Frost
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

package frost.gui.model;

import frost.util.gui.translation.*;

public class BoardInfoTableModel extends SortedTableModel implements LanguageListener
{
    private Language language = null;

    protected final static String columnNames[] = new String[8];

    protected final static Class columnClasses[] =  {
        String.class,   // board name
        String.class,   // board state
        Integer.class,  // message count
        Integer.class,  // today
        Integer.class,  // flagged
        Integer.class,  // starred
        Integer.class,  // unread
        String.class,   // date of last valid msg
    };

    public BoardInfoTableModel() {
        super();
        language = Language.getInstance();
        refreshLanguage();
    }

    private void refreshLanguage() {
        columnNames[0] = language.getString("BoardInfoFrame.table.board");
        columnNames[1] = language.getString("BoardInfoFrame.table.state");
        columnNames[2] = language.getString("BoardInfoFrame.table.messages");
        columnNames[3] = language.getString("BoardInfoFrame.table.messagesToday");
        columnNames[4] = language.getString("BoardInfoFrame.table.messagesFlagged");
        columnNames[5] = language.getString("BoardInfoFrame.table.messagesStarred");
        columnNames[6] = language.getString("BoardInfoFrame.table.messagesUnread");
        columnNames[7] = language.getString("BoardInfoFrame.table.lastMsgDate");

        fireTableStructureChanged();
    }

    /* (non-Javadoc)
     * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
     */
    public void languageChanged(final LanguageEvent event) {
        refreshLanguage();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(final int row, final int col) {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(final int column) {
        if( column >= 0 && column < columnNames.length ) {
            return columnNames[column];
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(final int column) {
        if( column >= 0 && column < columnClasses.length ) {
            return columnClasses[column];
        }
        return null;
    }
}
