package frost.gui.objects;

public interface FrostDownloadItem
{
    public String getFileName();
    public Long getFileSize();
    public String getFileAge();
    public Integer getHtl();
    public String getKey();
    public FrostBoardObject getSourceBoard();

    public String getState();
}