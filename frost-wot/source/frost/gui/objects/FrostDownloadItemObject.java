package frost.gui.objects;

import frost.gui.model.*;

public class FrostDownloadItemObject implements FrostDownloadItem, TableMember
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    // the constants representing download states
    public final static int STATE_WAITING    = 1;
    public final static int STATE_TRYING     = 2;
    public final static int STATE_DONE       = 3;
    public final static int STATE_FAILED     = 4;
    public final static int STATE_REQUESTING = 5;
    public final static int STATE_PROGRESS   = 6; // download runs
    // the strings that are shown in table for the states
    private final static String STATE_WAITING_STR    = LangRes.getString("Waiting");
    private final static String STATE_TRYING_STR     = LangRes.getString("Trying");
    private final static String STATE_DONE_STR       = LangRes.getString("Done");
    private final static String STATE_FAILED_STR     = LangRes.getString("Failed");
    private final static String STATE_REQUESTING_STR = "Requesting";
    private final static String STATE_PROGRESS_STR   = " kb";

    private String fileName = null;
    private Long fileSize = null;
    private String fileAge = null;
    private String key = null;
    private FrostBoardObject sourceBoard = null;
    private Integer htl = null;

    private int state = 0;
    private long downloadProgress = 0; // the count of downloaded bytes

    private long lastDownloadStartTimeMillis = 0; // used for one by one update mode

    public FrostDownloadItemObject( FrostSearchItem searchItem, int initialHtl )
    {
        fileName = searchItem.getFilename();
        fileSize = searchItem.getSize();
        fileAge = searchItem.getDate();
        key = searchItem.getKey();
        sourceBoard = searchItem.getBoard();

        htl = new Integer( initialHtl );
        state = STATE_WAITING;
    }

    public FrostDownloadItemObject( String fileName, String key, FrostBoardObject board, int initialHtl )
    {
        this.fileName = fileName;
        fileSize = new Long(-1);//null; // not set yet
        fileAge = null;
        this.key = key;
        sourceBoard = board;

        htl = new Integer( initialHtl );
        state = STATE_WAITING;
    }

    public FrostDownloadItemObject( String fileName,
                                    String fileSize,
                                    String fileAge,
                                    String key,
                                    String htl,
                                    int state,
                                    FrostBoardObject board )
    {
        this.fileName = fileName;
        this.fileSize = new Long( fileSize );
        this.fileAge = fileAge;
        this.key = key;
        this.sourceBoard = board;
        this.htl = new Integer( htl );
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
        String aFileAge = ( (fileAge==null) ? "Unknown" : fileAge );
        Long aFileSize =  ( (fileSize==null) ? new Long(-1) : fileSize );

        switch(column) {
            case 0: return fileName;                //LangRes.getString("Filename"),
            case 1: return aFileSize;               //LangRes.getString("Size"),
            case 2: return aFileAge;                //LangRes.getString("Age"),
            case 3: return getStateString( state ); //LangRes.getString("State"),
            case 4: return htl;                     //LangRes.getString("HTL"),
            case 5: return sourceBoard.toString();  //LangRes.getString("Source"),
            case 6: return key;                     //LangRes.getString("Key")
            default: return "*ERR*";
        }
    }

    public int compareTo( TableMember anOther, int tableColumIndex )
    {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

    public String getFileName()
    {
        return fileName;
    }
    public Long getFileSize()
    {
        return fileSize;
    }
    public String getFileAge()
    {
        return fileAge;
    }
    public Integer getHtl()
    {
        return htl;
    }
    public void setHtl(int v)
    {
        htl = new Integer(v);
    }
    public String getKey()
    {
        return key;
    }
    public FrostBoardObject getSourceBoard()
    {
        return sourceBoard;
    }

    public int getState()
    {
        return state;
    }
    public void setState(int v)
    {
        state = v;
    }
    public String getStateString(int state)
    {
        String statestr = "*ERR*";
        switch( state )
        {
        case STATE_WAITING:     statestr = STATE_WAITING_STR; break;
        case STATE_TRYING:      statestr = STATE_TRYING_STR; break;
        case STATE_FAILED:      statestr = STATE_FAILED_STR; break;
        case STATE_DONE:        statestr = STATE_DONE_STR; break;
        case STATE_REQUESTING:  statestr = STATE_REQUESTING_STR; break;
        case STATE_PROGRESS:    statestr = (downloadProgress/1024) + STATE_PROGRESS_STR; break;
        }
        return statestr;
    }

    public long getLastDownloadStartTimeMillis()
    {
        return lastDownloadStartTimeMillis;
    }
    public void setLastDownloadStartTimeMillis( long val )
    {
        lastDownloadStartTimeMillis = val;
    }

    public long getDownloadProgress()
    {
        return downloadProgress;
    }
    public void setDownloadProgress( long val )
    {
        downloadProgress = val;
    }
}
