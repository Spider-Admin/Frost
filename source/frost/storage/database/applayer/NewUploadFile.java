package frost.storage.database.applayer;

import java.io.*;

import frost.gui.objects.*;

/**
 * Holds the data for a new upload file.
 */
public class NewUploadFile {
    
    protected File file;
    Board targetBoard;
    protected String from;
    
    public NewUploadFile(File f, Board board, String fromName) {
        file = f;
        targetBoard = board;
        from = fromName;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getFrom() {
        return from;
    }
    
    public Board getTargetBoard() {
        return targetBoard;
    }
}
