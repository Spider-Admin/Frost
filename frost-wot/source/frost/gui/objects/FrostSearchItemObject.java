package frost.gui.objects;

import frost.*;
import frost.gui.model.*;


public class FrostSearchItemObject implements FrostSearchItem, TableMember
{
    FrostBoardObject board;
    KeyClass key;

    public FrostSearchItemObject( FrostBoardObject board, KeyClass key )
    {
        this.board = board;
        this.key = key;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column)
    {
        switch(column) {
            case 0: return key.getFilename();
            case 1: return key.getSize();
            case 2: return key.getDate();
            case 3: return key.getKey();
            case 4: return board.toString();
            default: return "*ERR*";
        }
    }

    public int compareTo( TableMember anOther, int tableColumIndex )
    {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

}
