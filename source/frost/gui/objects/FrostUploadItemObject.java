package frost.gui.objects;

import java.io.File;

import frost.mixed;
import frost.gui.model.TableMember;

public class FrostUploadItemObject implements FrostUploadItem, TableMember
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    // the constants representing upload states
    public final static int STATE_IDLE       = 1; // shows either last date uploaded or Never
    public final static int STATE_REQUESTED  = 2;
    public final static int STATE_UPLOADING  = 3;
    public final static int STATE_PROGRESS   = 4; // upload runs, shows "... kb"
    // the strings that are shown in table for the states
    private final static String STATE_UPLOADED_NEVER_STR = LangRes.getString("Never");
    private final static String STATE_REQUESTED_STR      = LangRes.getString("Requested");
    private final static String STATE_UPLOADING_STR      = LangRes.getString("Uploading");
//    private final static String STATE_PROGRESS_STR       = " kb";

    private String fileName = null;
    private String filePath = null;
    private Long fileSize = null;
    private int state = 0;
    private String lastUploadDate = null; // is null as long as NEVER uploaded
    private int uploadProgressTotalBlocks = -1;
    private int uploadProgressDoneBlocks = -1;

    private String key = null;
    private String SHA1 = null;
    private FrostBoardObject targetBoard = null;

    public FrostUploadItemObject(File file, FrostBoardObject board)
    {
        this.fileName = mixed.makeFilename( file.getName() );
        this.filePath = file.getPath();
        this.fileSize = new Long( file.length() );
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
            //case 5: return ((key==null) ? LangRes.getString("Unknown") : key); //LangRes.getString("Key")
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
    public String getStateString(int state)
    {
        String statestr = "*ERR*";
        switch( state )
        {
        case STATE_REQUESTED:   statestr = STATE_REQUESTED_STR; break;
        case STATE_UPLOADING:   statestr = STATE_UPLOADING_STR; break;
        case STATE_PROGRESS:    statestr = getUploadProgress(); break;
//        case STATE_PROGRESS:    statestr = (uploadProgress/1024) + STATE_PROGRESS_STR; break;
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

