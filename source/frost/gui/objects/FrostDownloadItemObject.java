package frost.gui.objects;

import frost.gui.model.TableMember;
import frost.search.*;
import frost.Core;
import java.util.Observable;

public class FrostDownloadItemObject extends Observable implements FrostDownloadItem, TableMember
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    // the constants representing download states
    public final static int STATE_WAITING    = 1;
    public final static int STATE_TRYING     = 2;
    public final static int STATE_DONE       = 3;
    public final static int STATE_FAILED     = 4;
    public final static int STATE_REQUESTING = 5;
    public final static int STATE_PROGRESS   = 6; // download runs
    public final static int STATE_REQUESTED  = 7;
    public final static int STATE_DECODING   = 8; // decoding runs

    // the strings that are shown in table for the states
    private final static String STATE_WAITING_STR    = LangRes.getString("Waiting");
    private final static String STATE_TRYING_STR     = LangRes.getString("Trying");
    private final static String STATE_DONE_STR       = LangRes.getString("Done");
    private final static String STATE_FAILED_STR     = LangRes.getString("Failed");
    private final static String STATE_REQUESTING_STR = "Requesting";
    private final static String STATE_REQUESTED_STR  = "Requested";
    private final static String STATE_DECODING_STR   = "Decoding segment...";

    private String fileName = null;
    private Long fileSize = null;
    private String fileAge = null;
    private String key = null;
    private FrostBoardObject sourceBoard = null;
    private Integer retries = null;
    private Boolean enableDownload = null;
    private String owner = null;
    private String SHA1 = null;
    private String batch = null;

    private String redirect;


    private int state = 0;

    int doneBlocks = 0;
    int requiredBlocks = 0;
    int totalBlocks = 0;

    private long lastDownloadStopTimeMillis = 0; // time when download try finished, used for pause between tries

    public FrostDownloadItemObject( FrostSearchItem searchItem )
    {
        fileName = searchItem.getFilename();
        fileSize = searchItem.getSize();
        fileAge = searchItem.getDate();
        key = searchItem.getKey();
	owner = searchItem.getOwner();
        sourceBoard = searchItem.getBoard();
	SHA1 = searchItem.getSHA1();
	batch = searchItem.getBatch();
        retries = new Integer(0);

        state = STATE_WAITING;

        redirect = searchItem.getRedirect();

    }

    //TODO: add .redirect to this or fix it to use SharedFileObject
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
				    String from,
				    String SHA1,
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
	this.SHA1 = SHA1;
        this.enableDownload = Boolean.valueOf(isDownloadEnabled);
	owner = from;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column)
    {
        String aFileAge = ( (fileAge==null && fileSize !=null) ? "offline" : fileAge );
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
            case 8: return owner==null ? "anonymous" : owner;                     //LangRes.getString("Key")
            case 9: return key==null ? " ?" : key;                     //LangRes.getString("Key")
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
	    case STATE_REQUESTED:   statestr = STATE_REQUESTED_STR; break;
        case STATE_DECODING:    statestr = STATE_DECODING_STR; break;
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
        if( tableColumIndex != 2 )
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

    public void setNotifyable(boolean what) {
    	if (what) {
		assert Core.getEmailNotifier() != null :
		"FrostDownloadItemObject.setNotifyable was called with true without the emailNotifier " +
		"being instantiated.";

		addObserver(Core.getEmailNotifier());
	}
	else
		deleteObservers();
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
    public void setDate(String date) {
    	fileAge=date;
    }
    public String getKey()
    {
        return key;
    }
    public void setKey(String newkey) {
    	key = newkey;
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
	if (state == STATE_DONE) {
		setChanged();
		//deliver the notification in the same thread.
		//I don't see any locking issues
		notifyObservers(getFileName());
		deleteObservers(); //only once! 
	}
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
    public String getBatch() {
    	return batch;
    }
    public void setBatch(String batch) {
    	this.batch = batch;
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

    public String getOwner() {
    	return owner;
    }

    public void setOwner(String owner) {
    	this.owner = owner;
    }

    public String getSHA1() {
    	return SHA1;
    }

    public void setSHA1(String sha1) {
    	SHA1 = sha1;
    }

    /**
     * @param string
     */
    public void setFileName(String string)
    {
        fileName = string;
    }

	/**
	 * @return Returns the redirect.
	 */
	public String getRedirect() {
		return redirect;
	}

	/**
	 * @param redirect The redirect to set.
	 */
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

}
