package frost.fileTransfer.upload;

import java.io.File;

import frost.gui.model.TableMember;
import frost.gui.objects.FrostBoardObject;

public class FrostUploadItemObject implements FrostUploadItem, TableMember
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    // the constants representing upload states
    public final static int STATE_IDLE       = 1; // shows either last date uploaded or Never
    public final static int STATE_REQUESTED  = 2; // a start of uploading is requested
    public final static int STATE_UPLOADING  = 3;
    public final static int STATE_PROGRESS   = 4; // upload runs, shows "... kb"
    public final static int STATE_ENCODING_REQUESTED  = 5; // an encoding of file is requested
    public final static int STATE_ENCODING   = 6; // the encode is running
    // the strings that are shown in table for the states
    
    //Warning: when localizing STATE_UPLOADING_STR, be careful with TableXmlIO.getUploadItemFromElement()
    private final static String STATE_UPLOADED_NEVER_STR     = LangRes.getString("Never");
    private final static String STATE_REQUESTED_STR          = LangRes.getString("Requested");
    private final static String STATE_UPLOADING_STR          = LangRes.getString("Uploading");
    private final static String STATE_ENCODING_REQUESTED_STR = LangRes.getString("Encode requested");
    private final static String STATE_ENCODING_STR           = LangRes.getString("Encoding file") + "...";

    private String fileName = null;
    private String filePath = null;
    private Long fileSize = null;
    private int state = 0;
    private int nextState = 0;
    private String lastUploadDate = null; // is null as long as NEVER uploaded
    private int uploadProgressTotalBlocks = -1;
    private int uploadProgressDoneBlocks = -1;

    private String key = null;
    private String SHA1 = null;
    private FrostBoardObject targetBoard = null;
    private String batch = null;

    public FrostUploadItemObject(File file, FrostBoardObject board)
    {
        //this.fileName = mixed.makeFilename( file.getName() ); //users weren't happy with this
    	if (file!=null) {
    		this.fileName = file.getName();
        	this.filePath = file.getPath();
        	this.fileSize = new Long( file.length() );
    	} else 
    		assert(board==null) : "constructor called with null file, but not null board";
        this.targetBoard = board;
        this.state = STATE_IDLE;
        this.lastUploadDate = null;
        this.key = null;
	this.SHA1 = null;
    }

    /**
     * Constructor used by loadUploadTable
     */
    public FrostUploadItemObject(String filename, String filepath, long filesize, FrostBoardObject board,
                                 int state, String lastUploadDate, String key, String SHA1)
    {
        this.fileName = filename;
        this.filePath = filepath;
        this.fileSize = new Long( filesize );
        this.targetBoard = board;
        this.state = state;
        this.lastUploadDate = lastUploadDate;
        this.key = key;
	this.SHA1 = SHA1;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column)
    {
        switch(column) {
            case 0: 
	    	if (SHA1 == null)
			return "<html><font color=\"gray\">"+fileName+"</font></html>";
		      else return "<html><b>"+fileName+"</b></html>";               //LangRes.getString("Filename"),
            case 1: return fileSize;               //LangRes.getString("Size"),
            case 2: return getStateString(state);  //LangRes.getString("Last upload"),
            case 3: return filePath;               //LangRes.getString("Path"),
            case 4: return targetBoard.toString(); //LangRes.getString("Destination"),
            case 5: return ((key==null) ? LangRes.getString("Unknown") : key); //LangRes.getString("Key")
        }
        return "*ERR*";
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
    public void setFileName( String val )
    {
        fileName = val;
    }

    public String getFilePath()
    {
        return filePath;
    }
    public void setFilePath( String val )
    {
        filePath = val;
    }

    public Long getFileSize()
    {
        return fileSize;
    }
    public void setFileSize( Long val )
    {
        fileSize = val;
    }

    public String getKey()
    {
        return key;
    }
    public void setKey( String val )
    {
        key = val;
    }
    
    public String getSHA1() {
    	return SHA1;
    }
    public void setSHA1(String val) {
    	SHA1 = val;
    }

    public FrostBoardObject getTargetBoard()
    {
        return targetBoard;
    }
    public void setTargetBoard( FrostBoardObject val )
    {
        targetBoard = val;
    }

    public int getState()
    {
        return state;
    }
    public void setState(int v)
    {
        state = v;
    }
    /**
     * If nextState is set (value > 0), this is the next state for this icon.
     * Currently used by GetRequestsThread if the requested item is
     * currently ENCODING, insertThread will then set state to
     * nextState after encoding.
     * @return
     */
    public int getNextState()
    {
        return nextState;
    }
    public void setNextState(int v)
    {
        nextState = v;
    }
    public String getBatch() {
    	return batch;
    }
    public void setBatch(String batch) {
    	this.batch=batch;
    }
    public String getStateString(int state)
    {
        String statestr = "*ERR*";
        switch( state )
        {
        case STATE_REQUESTED:   statestr = STATE_REQUESTED_STR; break;
        case STATE_UPLOADING:   statestr = STATE_UPLOADING_STR; break;
        case STATE_PROGRESS:    statestr = getUploadProgress(); break;
        case STATE_ENCODING_REQUESTED:    statestr = STATE_ENCODING_REQUESTED_STR; break;
        case STATE_ENCODING:    statestr = STATE_ENCODING_STR; break;
        case STATE_IDLE:        statestr = ( (lastUploadDate==null) ? STATE_UPLOADED_NEVER_STR : lastUploadDate );
        }
        return statestr;
    }

    public String getUploadProgress()
    {
        int percentDone = 0;
        if( uploadProgressTotalBlocks > 0 )
            percentDone = (int)((uploadProgressDoneBlocks * 100) / uploadProgressTotalBlocks);
        return( uploadProgressDoneBlocks + " / " + uploadProgressTotalBlocks + " ("+percentDone+"%)" );
    }

    public String getLastUploadDate()
    {
        return lastUploadDate;
    }
    public void setLastUploadDate( String val )
    {
        lastUploadDate = val;
    }

    public void setUploadProgressTotalBlocks( int val )
    {
        uploadProgressTotalBlocks = val;
    }

    public void setUploadProgressDoneBlocks( int val )
    {
        uploadProgressDoneBlocks = val;
    }
    public void incUploadProgressDoneBlocks()
    {
        uploadProgressDoneBlocks++;
    }
}

