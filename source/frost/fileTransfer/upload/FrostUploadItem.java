/*
  FrostUploadItem.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.fileTransfer.upload;

import java.io.*;
import java.util.*;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.util.model.*;

public class FrostUploadItem extends ModelItem
{
    // the constants representing field IDs
    public final static int FIELD_ID_DONE_BLOCKS = 100;
    public final static int FIELD_ID_FILE_NAME = 101;
    public final static int FIELD_ID_FILE_PATH = 102;
    public final static int FIELD_ID_FILE_SIZE = 103;
    public final static int FIELD_ID_KEY = 104;
    public final static int FIELD_ID_LAST_UPLOAD_DATE = 105;
    public final static int FIELD_ID_TOTAL_BLOCKS = 106;
    public final static int FIELD_ID_SHA1 = 107;
    public final static int FIELD_ID_STATE = 108;
    public final static int FIELD_ID_TARGET_BOARD = 109;
    public final static int FIELD_ID_ENABLED = 110;
    public final static int FIELD_ID_RETRIES = 111;

    // the constants representing upload states
    public final static int STATE_IDLE       = 1;   // shows either last date uploaded or Never
    public final static int STATE_REQUESTED  = 2;   // a start of uploading is requested
    public final static int STATE_UPLOADING  = 3;
    public final static int STATE_PROGRESS   = 4;   // upload runs, shows "... kb"
    public final static int STATE_ENCODING_REQUESTED  = 5; // an encoding of file is requested
    public final static int STATE_ENCODING   = 6;   // the encode is running
    public final static int STATE_WAITING    = 7;   // waiting until the next retry

    //the fields
    private int state;                  //FIELD_ID_STATE
    private String fileName = null;     //FIELD_ID_FILE_NAME
    private String filePath = null;     //FIELD_ID_FILE_PATH
    private long fileSize = 0;       //FIELD_ID_FILE_SIZE
    private String key= null;           //FIELD_ID_KEY
    private String sha1 = null;         //FIELD_ID_SHA
    private java.sql.Date lastUploadDate = null; //FIELD_ID_LAST_UPLOAD_DATE (null as long as NEVER uploaded)
    private int uploadCount = 0;
    private java.sql.Date lastRequestedDate = null;           
    private int requestedCount = 0;
    private Boolean enabled = Boolean.TRUE;    //FIELD_ID_ENABLED
    private int retries = 0;                        //FIELD_ID_RETRIES
    private long lastUploadStopTimeMillis = 0;
    
    // contains board,owner,lastSharedDate
    private List frostUploadItemOwnerBoardList = new LinkedList();

    // non-persistent fields
    private int nextState = 0;
    private int totalBlocks = -1;       //FIELD_ID_TOTAL_BLOCKS
    private int doneBlocks = -1;        //FIELD_ID_DONE_BLOCKS


    /**
     * Dummy to use for uploads of attachments. Is never saved.
     */
    public FrostUploadItem() {
    }

    /**
     * Used to add a new file. SHA1 must be created and must not be already in table!
     */
    public FrostUploadItem(File file, Board newBoard, String owner, String s1) {

        fileName = file.getName();
        filePath = file.getPath();
        fileSize = file.length();
        sha1 = s1;
        
        FrostUploadItemOwnerBoard ob = new FrostUploadItemOwnerBoard(this, newBoard, owner, null);
        addFrostUploadItemOwnerBoard(ob);

        state = STATE_IDLE;
    }

    /**
     * Constructor used by loadUploadTable
     */
    public FrostUploadItem(
            String newSha1,
            String newFilename,
            String newFilepath,
            long newFilesize,
            String newKey,
            java.sql.Date newLastUploadDate,
            int newUploadCount,
            java.sql.Date newLastRequestedDate,
            int newRequestedCount,
            int newState,
            boolean newIsEnabled,
            long newLastUploadStopTimeMillis,
            int newRetries)
    {
        sha1 = newSha1;
        fileName = newFilename;
        filePath = newFilepath;
        fileSize = newFilesize;
        key = newKey;
        lastUploadDate = newLastUploadDate;
        uploadCount = newUploadCount;
        lastRequestedDate = newLastRequestedDate;
        requestedCount = newRequestedCount;
        state = newState;
        enabled = Boolean.valueOf(newIsEnabled);
        lastUploadStopTimeMillis = newLastUploadStopTimeMillis;
        retries = newRetries;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column) {
       return null;
    }

    public int compareTo( TableMember anOther, int tableColumIndex ) {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

    /**
     * @return
     */
    public String getFileName() {
        return fileName;
    }
    public int getUploadCount() {
        return uploadCount;
    }
    /**
     * @param val
     */
    public void setFileName(String newFileName) {
        String oldFileName = fileName;
        fileName = newFileName;
        fireFieldChange(FIELD_ID_FILE_NAME, oldFileName, newFileName);
    }

    /**
     * @return
     */
    public String getFilePath() {
        return filePath;
    }
    /**
     * @param val
     */
    public void setFilePath(String newFilePath) {
        String oldFilePath = filePath;
        filePath = newFilePath;
        fireFieldChange(FIELD_ID_FILE_PATH, oldFilePath, newFilePath);
    }

    /**
     * @return
     */
    public long getFileSize() {
        return fileSize;
    }
    /**
     * @param val
     */
    public void setFileSize(Long newFileSize) {
        fileSize = newFileSize.longValue();
        fireFieldChange(FIELD_ID_FILE_SIZE, null, newFileSize);
    }

    /**
     * @return
     */
    public String getKey() {
        return key;
    }
    /**
     * @param newKey
     */
    public void setKey(String newKey) {
        String oldKey = key;
        key = newKey;
        fireFieldChange(FIELD_ID_KEY, oldKey, newKey);
    }

    /**
     * @return
     */
    public String getSHA1() {
        return sha1;
    }
    /**
     * @param newSha1
     */
    public void setSHA1(String newSha1) {
        String oldSha1 = sha1;
        sha1 = newSha1;
        fireFieldChange(FIELD_ID_SHA1, oldSha1, newSha1);
    }

    /**
     * @return
     */
    public int getState() {
        return state;
    }
    /**
     * @param newState
     */
    public void setState(int newState) {
        int oldState = state;
        state = newState;
        fireFieldChange(FIELD_ID_STATE, oldState, newState);
    }
    /**
     * If nextState is set (value > 0), this is the next state for this icon.
     * Currently used by GetRequestsThread if the requested item is
     * currently ENCODING, insertThread will then set state to
     * nextState after encoding.
     * @return
     */
    public int getNextState() {
        return nextState;
    }
    /**
     * @param v
     */
    public void setNextState(int v) {
        nextState = v;
    }
    /**
     * @return
     */
    public java.sql.Date getLastUploadDate() {
        return lastUploadDate;
    }
    /**
     * @param newLastUploadDate
     */
    public void setLastUploadDate(java.sql.Date newLastUploadDate) {
        java.sql.Date oldLastUploadDate = lastUploadDate;
        lastUploadDate = newLastUploadDate;
        fireFieldChange(FIELD_ID_LAST_UPLOAD_DATE, oldLastUploadDate, newLastUploadDate);
    }

    /**
     * @param newTotalBlocks
     */
    public void setTotalBlocks(int newTotalBlocks) {
        int oldTotalBlocks = totalBlocks;
        totalBlocks = newTotalBlocks;
        fireFieldChange(FIELD_ID_TOTAL_BLOCKS, oldTotalBlocks, newTotalBlocks);
    }

    /**
     * @return
     */
    public int getRetries() {
        return retries;
    }

    /**
     * @param newRetries
     */
    public void setRetries(int newRetries) {
        int oldRetries = retries;
        retries = newRetries;
        fireFieldChange(FIELD_ID_RETRIES, oldRetries, newRetries);
    }

    /**
     * @param newDoneBlocks
     */
    public void setDoneBlocks(int newDoneBlocks) {
        int oldDoneBlocks = doneBlocks;
        doneBlocks = newDoneBlocks;
        fireFieldChange(FIELD_ID_DONE_BLOCKS, oldDoneBlocks, newDoneBlocks);
    }

    /**
     * @param enabled new enable status of the item. If null, the current
     *        status is inverted
     */
    public void setEnabled(Boolean newEnabled) {
        if (newEnabled == null && enabled != null) {
            //Invert the enable status
            boolean temp = enabled.booleanValue();
            newEnabled = Boolean.valueOf(!temp);
        }
        Boolean oldEnabled = enabled;
        enabled = newEnabled;
        fireFieldChange(FIELD_ID_ENABLED, oldEnabled, newEnabled);
    }

    public int getDoneBlocks() {
        return doneBlocks;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public long getLastUploadStopTimeMillis() {
        return lastUploadStopTimeMillis;
    }
    public void setLastUploadStopTimeMillis(long lastUploadStopTimeMillis) {
        this.lastUploadStopTimeMillis = lastUploadStopTimeMillis;
    }

    public java.sql.Date getLastRequestedDate() {
        return lastRequestedDate;
    }

    public void setLastRequestedDate(java.sql.Date lastRequestedDate) {
        this.lastRequestedDate = lastRequestedDate;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(int requestedCount) {
        this.requestedCount = requestedCount;
    }

    public List getFrostUploadItemOwnerBoardList() {
        return frostUploadItemOwnerBoardList;
    }
    public void addFrostUploadItemOwnerBoard(FrostUploadItemOwnerBoard v) {
        // TODO: check for dups! board,owner
        frostUploadItemOwnerBoardList.add(v);
    }
    public void deleteFrostUploadItemOwnerBoard(FrostUploadItemOwnerBoard v) {
        frostUploadItemOwnerBoardList.remove(v);
    }

    public void setUploadCount(int uploadCount) {
        this.uploadCount = uploadCount;
    }
    
    /**
     * Builds an instance that can be serialized to XML.
     * Used to send file references in index file.
     */
    public SharedFileXmlFile getSharedFileXmlFileInstance(FrostUploadItemOwnerBoard v) {
        SharedFileXmlFile sfxf = new SharedFileXmlFile();

        sfxf.setSHA1(getSHA1());
        sfxf.setSize(getFileSize());
        sfxf.setKey(getKey());
        sfxf.setFilename(getFileName());
        if( getLastUploadDate() != null ) {
            sfxf.setLastUploaded(DateFun.getExtendedDateFromSqlDate(getLastUploadDate()));
        } else {
            sfxf.setLastUploaded(null);
        }
        sfxf.setOwner(v.getOwner());
        sfxf.setBoard(v.getTargetBoard());
        
        return sfxf;
    }
}
