package frost.gui.objects;

public interface FrostSearchItem
{
    public String getFilename();

    public Long getSize();

    public String getDate();

    public String getKey();
    
    public String getOwner();
    
    public String getSHA1();
    
    public String getBatch();

    public FrostBoardObject getBoard();
    
    public String getRedirect();
}
