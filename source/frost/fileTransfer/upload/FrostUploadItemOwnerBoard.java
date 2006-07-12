package frost.fileTransfer.upload;

import frost.gui.objects.*;

public class FrostUploadItemOwnerBoard {

    private Board targetBoard = null;
    private String owner = null;
    private java.sql.Date lastSharedDate = null;
    private FrostUploadItem parent = null;
    
    public FrostUploadItemOwnerBoard(FrostUploadItem p, Board board, String ownerName, java.sql.Date lastShared) {
        targetBoard = board;
        owner = ownerName;
        lastSharedDate = lastShared;
        parent = p;
    }
    
    public java.sql.Date getLastSharedDate() {
        return lastSharedDate;
    }
    public void setLastSharedDate(java.sql.Date d) {
        lastSharedDate = d;
    }
    public String getOwner() {
        return owner;
    }
    public Board getTargetBoard() {
        return targetBoard;
    }
    public FrostUploadItem getUploadItem() {
        return parent;
    }
}
