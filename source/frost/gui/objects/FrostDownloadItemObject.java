package frost.gui.objects;

import frost.gui.model.*;

public class FrostDownloadItemObject implements FrostDownloadItem, TableMember
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    private final static String STATE_WAITING = LangRes.getString("Waiting");

    String fileName = null;
    Long fileSize = null;
    String fileAge = null;
    String key = null;
    FrostBoardObject sourceBoard = null;
    Integer htl = null;

    String state = null;

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
                                    String state,
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
            case 0: return fileName;               //LangRes.getString("Filename"),
            case 1: return aFileSize;              //LangRes.getString("Size"),
            case 2: return aFileAge;               //LangRes.getString("Age"),
            case 3: return state;                  //LangRes.getString("State"),
            case 4: return htl;                    //LangRes.getString("HTL"),
            case 5: return sourceBoard.toString(); //LangRes.getString("Source"),
            case 6: return key;                    //LangRes.getString("Key")
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

    public String getState()
    {
        return state;
    }
    public void setState(String v)
    {
        state = v;
    }
}
