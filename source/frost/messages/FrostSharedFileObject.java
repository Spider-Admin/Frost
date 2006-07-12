package frost.messages;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.upload.*;
import frost.storage.database.applayer.*;

public class FrostSharedFileObject {

    private static Logger logger = Logger.getLogger(FrostSharedFileObject.class.getName());

    Long primkey = null;
    String sha1 = null;  //SHA1 of the file
    long size = 0; // Filesize
    String key = null; // CHK key
    java.sql.Date lastDownloaded = null;
    java.sql.Date lastReceived = null;
    
    private List frostSharedFileObjectOwnerBoardList = new LinkedList();
    
    public static java.sql.Date defaultDate;
    static {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1, 1, 2000);
        defaultDate = new java.sql.Date(cal.getTime().getTime()); 
    }
    
    /**
     * Used if item is loaded from database.
     */
    public FrostSharedFileObject(
            long newPrimkey,
            String newSha1, 
            long newSize, 
            String newKey, 
            java.sql.Date newLastDownloaded, 
            java.sql.Date newLastReceived) 
    {
        primkey = new Long(newPrimkey);
        sha1 = newSha1;
        size = newSize;
        key = newKey;
        lastDownloaded = newLastDownloaded;
        lastReceived = newLastReceived;
    }

    /**
     * Create instance with data from SharedFilesXmlFile.
     * After creation this item should only be saved to database,
     * this merges the data frrom this item in case this files is
     * already in the filelist (adds new owner/board).
     */
    public FrostSharedFileObject(SharedFileXmlFile sfo) {
        sha1 = sfo.getSHA1();
        size = sfo.getSize().longValue();
        key = sfo.getKey();
        lastDownloaded = null;
        lastReceived = null; // set or updated after add  DateFun.getCurrentSqlDateGMT();

        java.sql.Date lastUploadDate = null;
        if( sfo.getKey() != null ) {
            if( sfo.getLastUploaded() != null ) {
                Calendar cal = DateFun.getCalendarFromDate(sfo.getLastUploaded());
                lastUploadDate = DateFun.getSqlDateOfCalendar(cal);
            } else {
                lastUploadDate = defaultDate;
            }
        }
        FrostSharedFileObjectOwnerBoard ob = new FrostSharedFileObjectOwnerBoard(
                sfo.getFilename(),
                sfo.getBoard(),
                sfo.getOwner(),
                DateFun.getCurrentSqlDateGMT(),
                lastUploadDate);
        addFrostSharedFileObjectOwnerBoard(ob);
    }

    /**
     * Save this item to the database (insert or update).
     */
    public boolean save() {
        try {
            AppLayerDatabase.getFileListDatabaseTable().insertOrUpdateFrostSharedFileObject(this);
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Error updating shared file object", e);
            return false;
        }
        return true;
    }
    
    public List getFrostSharedFileObjectOwnerBoardList() {
        return frostSharedFileObjectOwnerBoardList;
    }
    public void addFrostSharedFileObjectOwnerBoard(FrostSharedFileObjectOwnerBoard v) {
        // TODO: check for dups! board,owner
        frostSharedFileObjectOwnerBoardList.add(v);
    }
    public void deleteFrostSharedFileObjectOwnerBoard(FrostUploadItemOwnerBoard v) {
        frostSharedFileObjectOwnerBoardList.remove(v);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public java.sql.Date getLastDownloaded() {
        return lastDownloaded;
    }

    public void setLastDownloaded(java.sql.Date lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    public java.sql.Date getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(java.sql.Date lastReceived) {
        this.lastReceived = lastReceived;
    }

    public String getSha1() {
        return sha1;
    }

    public long getSize() {
        return size;
    }

    public Long getPrimkey() {
        return primkey;
    }
}
