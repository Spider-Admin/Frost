package frost.gui.objects;

import frost.KeyClass;
import frost.gui.model.TableMember;


public class FrostSearchItemObject implements FrostSearchItem, TableMember
{
    FrostBoardObject board;
    KeyClass key;
    int state;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table

    public FrostSearchItemObject( FrostBoardObject board, KeyClass key, int state )
    {
        this.board = board;
        this.key = key;
        this.state = state;
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

////// Implementing the FrostSearchItem interface //////

    public String getFilename()
    {
        return key.getFilename();
    }

    public Long getSize()
    {
        return key.getSize();
    }

    public String getDate()
    {
        return key.getDate();
    }

    public String getKey()
    {
        return key.getKey();
    }

    public FrostBoardObject getBoard()
    {
        return board;
    }

    public int getState()
    {
        return state;
    }
}
