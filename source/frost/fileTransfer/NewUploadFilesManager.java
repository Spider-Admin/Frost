package frost.fileTransfer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.*;
import frost.storage.database.applayer.*;

public class NewUploadFilesManager implements Savable {
    
    private static Logger logger = Logger.getLogger(NewUploadFilesManager.class.getName());

    LinkedList newUploadFiles;
    
    public void initialize() throws StorageException {
        
        try {
            newUploadFiles = AppLayerDatabase.getNewUploadFilesTable().loadNewUploadFiles();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading new upload files", e);
            throw new StorageException("Error loading new upload files");
        }
    }

    public void save() throws StorageException {
        try {
            AppLayerDatabase.getNewUploadFilesTable().saveNewUploadFiles(newUploadFiles);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving new upload files", e);
            throw new StorageException("Error saving new upload files");
        }
    }
    
    public void addNewUploadFiles(List newFiles) {
        for(Iterator i=newFiles.iterator(); i.hasNext(); ) {
            NewUploadFile nuf = (NewUploadFile)i.next();
            newUploadFiles.add(nuf);
        }
    }
    
    public NewUploadFile getNewUploadFile() {
        if( newUploadFiles.size() == 0 ) {
            return null;
        }
        return (NewUploadFile)newUploadFiles.getFirst();
    }
    
    public void deleteNewUploadFile(NewUploadFile nuf) {
        newUploadFiles.remove(nuf);
    }
}
