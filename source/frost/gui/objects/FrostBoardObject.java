package frost.gui.objects;

import javax.swing.tree.*;

import frost.*;

public class FrostBoardObject extends DefaultMutableTreeNode implements FrostBoard
{
    private String boardName = null;
    private String boardFileName = null;
    private long lastUpdateStartMillis = -1; // never updated

    private int newMessageCount = 0;

    private boolean isFolder = false;

    private boolean spammed = false;
    private int numberBlocked = 0; // number of blocked messages for this board

    /**
     * Constructs a new FrostBoardObject wich is a Board.
     */
    public FrostBoardObject(String name)
    {
        super();
        boardName = name;
        boardFileName = mixed.makeFilename( boardName );
    }
    /**
     * Constructs a new FrostBoardObject.
     * If isFold is true, this will be a folder, else a board.
     */
    public FrostBoardObject(String name, boolean isFold)
    {
        this(name);
        isFolder = isFold;
    }


    public String getBoardName()
    {
        return boardName;
    }
    public String getBoardFilename()
    {
        return boardFileName;
    }

    public String toString()
    {
        return boardName;
    }

    public boolean isFolder()
    {
        return isFolder;
    }


//////////////////////////////////////////////
// From BoardStats

    public void incBlocked()
    {
        numberBlocked++;
    }

    public void resetBlocked()
    {
        numberBlocked=0;
    }

    public int getBlockedCount()
    {
        return numberBlocked;
    }

    public void setSpammed(boolean val)
    {
        spammed=val;
    }

    public boolean isSpammed()
    {
        return spammed;
    }

    public long getLastUpdateStartMillis()
    {
        return lastUpdateStartMillis;
    }

    public void setLastUpdateStartMillis(long millis)
    {
        lastUpdateStartMillis = millis;
    }

    public int getNewMessageCount()
    {
        return newMessageCount;
    }
    public void setNewMessageCount( int val )
    {
        newMessageCount = val;
    }
    public void incNewMessageCount()
    {
        newMessageCount++;
    }
    public void decNewMessageCount()
    {
        newMessageCount--;
    }

    public boolean containsNewMessage()
    {
        if( getNewMessageCount() > 0 )
            return true;
        return false;
    }
}
