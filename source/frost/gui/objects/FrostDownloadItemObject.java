package frost.gui.objects;

import javax.swing.*;

import frost.*;
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
    private Integer retries = null;
    private Boolean enableDownload = null;

    private int state = 0;

    int doneBlocks = 0;
    int requiredBlocks = 0;
    int totalBlocks = 0;

    private long lastDownloadStartTimeMillis = 0; // used for one by one update mode
    private long lastDownloadStopTimeMillis = 0; // time when download try finished, used for pause between tries

    public FrostDownloadItemObject( FrostSearchItem searchItem )
    {
        fileName = searchItem.getFilename();
        fileSize = searchItem.getSize();
        fileAge = searchItem.getDate();
        key = searchItem.getKey();
        sourceBoard = searchItem.getBoard();
        retries = new Integer(0);

        state = STATE_WAITING;
    }

    public FrostDownloadItemObject( String fileName, String key, FrostBoardObject board )
    {
        this.fileName = fileName;
        fileSize = null; // not set yet
        fileAge = null;
        this.key = key;
        sourceBoard = board;
        retries = new Integer(0);

        state = STATE_WAITING;
    }

    public FrostDownloadItemObject( String fileName,
                                    String fileSize,
                                    String fileAge,
                                    String key,
                                    String tries,
                                    int state,
                                    boolean isDownloadEnabled,
                                    FrostBoardObject board )
    {
        this.fileName = fileName;
        if( fileSize != null )
            this.fileSize = new Long( fileSize );

        if(tries != null )
            retries = new Integer(tries);
        else
            retries = new Integer(0);

        this.fileAge = fileAge;
        this.key = key;
        this.sourceBoard = board;
        this.state = state;
        this.enableDownload = Boolean.valueOf(isDownloadEnabled);
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
        Object aFileSize;
        if( fileSize == null ) // unknown
            aFileSize = "Unknown";
        else
            aFileSize = fileSize;

        String blocks;
        if( totalBlocks > 0 )
            blocks = getBlockProgressStr();
        else
            blocks = "";

        String board;
        if( sourceBoard != null )
            board = sourceBoard.toString();
        else
            board = "";

        Boolean downloadEnabled;
        if( getEnableDownload() == null )
            downloadEnabled = Boolean.valueOf(true); // default = enabled
        else
            downloadEnabled = getEnableDownload();

        switch(column) {
            case 0: return downloadEnabled;         //LangRes.getString("on"),
            case 1: return fileName;                //LangRes.getString("Filename"),
            case 2: return aFileSize;               //LangRes.getString("Size"),
            case 3: return aFileAge;                //LangRes.getString("Age"),
            case 4: return getStateString( state ); //LangRes.getString("State"),
            case 5: return blocks;                  //LangRes.getString("Blocks"),
            case 6: return retries;                 //LangRes.getString("Retries"),
            case 7: return board;                   //LangRes.getString("Source"),
            case 8: return key;                     //LangRes.getString("Key")
            default: return "*ERR*";
        }
    }

    private String getBlockProgressStr()
    {
        return( doneBlocks + " / " +requiredBlocks + " ("+totalBlocks+")" );
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
        case STATE_PROGRESS:    if( totalBlocks > 0 )
                                    statestr = (int)((doneBlocks*100)/requiredBlocks) + "%";
                                else
                                    statestr = "0%";
                                break;
        }
        return statestr;
    }

    public int compareTo( TableMember anOther, int tableColumIndex )
    {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        if( tableColumIndex != 1 )
            return c1.compareTo( c2 );
        // handle the size column. The values are either Integer or String ("Unknown")
        // sort strings below numbers
        if( c1 instanceof String && c2 instanceof String )
            return 0;
        if( c1 instanceof String && !(c2 instanceof String) )
            return 1;
        if( !(c1 instanceof String) && c2 instanceof String )
            return -1;
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
    public void setFileSize(long s)
    {
        fileSize = new Long( s );
    }

    public String getFileAge()
    {
        return fileAge;
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

    public long getLastDownloadStartTimeMillis()
    {
        return lastDownloadStartTimeMillis;
    }
    public void setLastDownloadStartTimeMillis( long val )
    {
        lastDownloadStartTimeMillis = val;
    }


    public long getLastDownloadStopTimeMillis()
    {
        return lastDownloadStopTimeMillis;
    }
    public void setLastDownloadStopTimeMillis( long val )
    {
        lastDownloadStopTimeMillis = val;
    }

    public int getRetries()
    {
        return retries.intValue();
    }
    public void setRetries( int val )
    {
        retries = new Integer(val);
    }

    public void setBlockProgress( int actualBlocks, int requiredBlocks, int allAvailableBlocks )
    {
        this.doneBlocks = actualBlocks;
        this.requiredBlocks = requiredBlocks;
        this.totalBlocks = allAvailableBlocks;
    }


    public Boolean getEnableDownload()
    {
        return enableDownload;
    }
    public void setEnableDownload( Boolean val )
    {
        enableDownload = val;
    }

}
