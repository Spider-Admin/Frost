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

    private String publicKey = null;
    private String privateKey = null;

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
     * Constructs a new FrostBoardObject wich is a Board.
     */
    public FrostBoardObject(String name, String pubKey, String privKey)
    {
        this(name);
        this.publicKey = pubKey;
        this.privateKey = privKey;
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

    public void setBoardName(String newname)
    {
        boardName = newname;
        boardFileName = mixed.makeFilename( boardName );
    }

    public boolean isWriteAccessBoard()
    {
        if( publicKey != null && privateKey != null )
            return true;
        return false;
    }
    public boolean isReadAccessBoard()
    {
        if( publicKey != null && privateKey == null )
            return true;
        return false;
    }
    public boolean isPublicBoard()
    {
        if( publicKey == null && privateKey == null )
            return true;
        return false;
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

    public String getPublicKey()
    {
        return publicKey;
    }
    public void setPublicKey( String val )
    {
        publicKey = val;
    }
    public String getPrivateKey()
    {
        return privateKey;
    }
    public void setPrivateKey( String val )
    {
        privateKey = val;
    }

    public String getStateString()
    {
        // TODO: translate
        if( isReadAccessBoard() )
        {
            return "read access";
        }
        else if( isWriteAccessBoard() )
        {
            return "write access";
        }
        else if( isPublicBoard() )
        {
            return "public board";
        }
        return "*ERROR*";
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
