package frost.gui.objects;

import java.io.*;

import frost.*;
import frost.gui.model.*;

public class FrostUploadItemObject implements FrostUploadItem, TableMember
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    String fileName = null;
    String filePath = null;
    Long fileSize = null;
    String state = null; // is the lastUploadDate

    String key = null;
    FrostBoardObject targetBoard = null;

    public FrostUploadItemObject(File file, FrostBoardObject board)
    {
        this.fileName = mixed.makeFilename( file.getName() );
        this.filePath = file.getPath();
        this.fileSize = new Long( file.length() );
        this.targetBoard = board;
        this.state = LangRes.getString("Never");
        this.key = LangRes.getString("Unknown");
    }

    /**
     * Constructor used by loadUploadTable
     */
    public FrostUploadItemObject(String filename, String filepath, long filesize, FrostBoardObject board,
                                 String state, String key)
    {
        this.fileName = filename;
        this.filePath = filepath;
        this.fileSize = new Long( filesize );
        this.targetBoard = board;
        this.state = state;
        this.key = key;
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
            case 0: return fileName;           //LangRes.getString("Filename"),
            case 1: return fileSize;           //LangRes.getString("Size"),
            case 2: return state;              //LangRes.getString("Last upload"),
            case 3: return filePath;           //LangRes.getString("Path"),
            case 4: return targetBoard.toString();   //LangRes.getString("Destination"),
            case 5: return key;                //LangRes.getString("Key")
            default: return "*ERR*";
        }
    }

    public int compareTo( TableMember anOther, int tableColumIndex )
    {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

    public String getState()
    {
        return state;
    }
    public void setState(String val)
    {
        state = val;
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

    public FrostBoardObject getTargetBoard()
    {
        return targetBoard;
    }
    public void setTargetBoard( FrostBoardObject val )
    {
        targetBoard = val;
    }

}
