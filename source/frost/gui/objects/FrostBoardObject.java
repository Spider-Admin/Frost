package frost.gui.objects;

import javax.swing.tree.*;

public class FrostBoardObject extends DefaultMutableTreeNode implements FrostBoard
{
    private String boardName = null;

    public FrostBoardObject(String name)
    {
        super();
        boardName = name;
        tries = 0;
        success = 0;
        lastAccess = 0;
        spammed=false;
        numberBlocked=0;
    }

    public String getBoardName()
    {
        return boardName;
    }

//////////////////////////////////////////////
    // From BoardStats

    private int tries;
    private int success;
    private int lastAccess;
    private String board;
    private boolean spammed;
    private int numberBlocked;

    public void incBlocked()
    {
        numberBlocked++;
    }

    public void resetBlocked()
    {
        numberBlocked=0;
    }

    public int getNumberBlocked()
    {
        return numberBlocked;
    }

    public void spam()
    {
        spammed=true;
    }

    public void unspam()
    {
        spammed=false;
    }

    public boolean spammed()
    {
        return spammed;
    }

    public void incTries()
    {
        tries++;
    }
    public void setSuccess(int value)
    {
        success = value;
    }
    public void incSuccess()
    {
        success++;
    }
    public void incAccess()
    {
        lastAccess++;
    }
    public void resetAccess()
    {
        lastAccess = 0;
    }
    public int getLastAccess()
    {
        return lastAccess;
    }
    public int getSuccess()
    {
        return success;
    }
    public String getBoard()
    {
        return board;
    }
    public int getCp()
    {
        return success + lastAccess - tries;
    }

}
