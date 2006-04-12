package frost.transferlayer;

public class IndexFileDownloaderResult {
    
    public static final int SUCCESS = 1;
    public static final int BROKEN_DATA = 2;
    public static final int DUPLICATE_FILE = 3;
    public static final int INVALID_DATA = 4;
    public static final int BROKEN_METADATA = 5;
    public static final int TAMPERED_DATA = 6;
    public static final int BAD_USER = 7;
    public static final int ANONYMOUS_BLOCKED = 8;
    
    public int errorMsg = -1;
}
