package frost.messages;

import frost.gui.objects.*;

public class FrostSharedFileObjectOwnerBoard {

    protected long refkey;
    protected String name;
    protected Board board;
    protected String owner;
    protected java.sql.Date lastReceived = null;
    protected java.sql.Date lastUploaded = null;
    
    public FrostSharedFileObjectOwnerBoard(
            long newRefkey,
            String newName,
            Board newBoard,
            String newOwner,
            java.sql.Date newLastReceived,
            java.sql.Date newLastUploaded) 
    {
        refkey = newRefkey;
        name = newName;
        board = newBoard;
        owner = newOwner;
        lastReceived = newLastReceived;
        lastUploaded = newLastUploaded;
    }

    public FrostSharedFileObjectOwnerBoard(
            String newName,
            Board newBoard,
            String newOwner,
            java.sql.Date newLastReceived,
            java.sql.Date newLastUploaded) 
    {
        this(0, newName, newBoard, newOwner, newLastReceived, newLastUploaded);
    }

    public Board getBoard() {
        return board;
    }
    public java.sql.Date getLastReceived() {
        return lastReceived;
    }
    public String getName() {
        return name;
    }
    public String getOwner() {
        return owner;
    }
    public long getRefkey() {
        return refkey;
    }

    public java.sql.Date getLastUploaded() {
        return lastUploaded;
    }

    public void setRefkey(long refkey) {
        this.refkey = refkey;
    }

    public void setLastReceived(java.sql.Date lastReceived) {
        this.lastReceived = lastReceived;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastUploaded(java.sql.Date newLastUploaded) {
        this.lastUploaded = newLastUploaded;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
