/*
  FrostUploadItem.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.io.File;

import frost.gui.model.TableMember;
import frost.gui.objects.Board;
import frost.util.model.ModelItem;

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
    public final static int STATE_IDLE       = 1; 	// shows either last date uploaded or Never
    public final static int STATE_REQUESTED  = 2; 	// a start of uploading is requested
    public final static int STATE_UPLOADING  = 3;
    public final static int STATE_PROGRESS   = 4; 	// upload runs, shows "... kb"
    public final static int STATE_ENCODING_REQUESTED  = 5; // an encoding of file is requested
    public final static int STATE_ENCODING   = 6;	// the encode is running
    public final static int STATE_WAITING 	 = 7;  	// waiting until the next retry
    
	//the fields
	private int state;					//FIELD_ID_STATE
    private String fileName;			//FIELD_ID_FILE_NAME
    private String filePath;			//FIELD_ID_FILE_PATH
    private Long fileSize;				//FIELD_ID_FILE_SIZE
	private String key;					//FIELD_ID_KEY
	private String sha1;				//FIELD_ID_SHA
	private Board targetBoard;			//FIELD_ID_TARGET_BOARD
	private int totalBlocks = -1;		//FIELD_ID_TOTAL_BLOCKS
	private int doneBlocks = -1;		//FIELD_ID_DONE_BLOCKS
	private String lastUploadDate;					//FIELD_ID_LAST_UPLOAD_DATE (null as long as NEVER uploaded)
	private Boolean enabled = new Boolean(true);	//FIELD_ID_ENABLED
	private int retries;							//FIELD_ID_RETRIES
    
    private int nextState = 0;

    private String batch = null;
    
    private long lastUploadStopTimeMillis = 0;

	/**
	 * @param file
	 * @param newBoard
	 */
	public FrostUploadItem(File file, Board newBoard) {
		if (file != null) {
			fileName = file.getName();
			filePath = file.getPath();
			fileSize = new Long(file.length());
		} else
			assert(newBoard == null) : "constructor called with null file, but not null board";
		targetBoard = newBoard;
		state = STATE_IDLE;
		lastUploadDate = null;
		key = null;
		sha1 = null;
	}

    /**
     * Constructor used by loadUploadTable
     */
    public FrostUploadItem(String newFilename, String newFilepath, long newFilesize, Board newBoard,
                                 int newState, String newLastUploadDate, String newKey, String newSha1)
    {
        fileName = newFilename;
        filePath = newFilepath;
        fileSize = new Long(newFilesize);
        targetBoard = newBoard;
        state = newState;
        lastUploadDate = newLastUploadDate;
        key = newKey;
		sha1 = newSha1;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column)
    {
       return null;
    }

    public int compareTo( TableMember anOther, int tableColumIndex )
    {
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
	public Long getFileSize() {
		return fileSize;
	}
	/**
	 * @param val
	 */
	public void setFileSize(Long newFileSize) {
		Long oldFileSize = fileSize;
		fileSize = newFileSize;
		fireFieldChange(FIELD_ID_FILE_SIZE, oldFileSize, newFileSize);
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
	public Board getTargetBoard() {
		return targetBoard;
	}
	/**
	 * @param val
	 */
	public void setTargetBoard(Board newBoard) {
		Board oldBoard = targetBoard;
		targetBoard = newBoard;
		fireFieldChange(FIELD_ID_TARGET_BOARD, oldBoard, newBoard);
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
    public String getBatch() {
    	return batch;
    }
	/**
	 * @param batch
	 */
	public void setBatch(String batch) {
		this.batch = batch;
	}
	/**
	 * @return
	 */
	public String getLastUploadDate() {
		return lastUploadDate;
	}
	/**
	 * @param newLastUploadDate
	 */
	public void setLastUploadDate(String newLastUploadDate) {
		String oldLastUploadDate = lastUploadDate;
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
	 * 		  status is inverted
	 */
	public void setEnabled(Boolean newEnabled) {
		if (newEnabled == null && enabled != null) {
			//Invert the enable status
			boolean temp = enabled.booleanValue();
			newEnabled = new Boolean(!temp);
		}
		Boolean oldEnabled = enabled;
		enabled = newEnabled;
		fireFieldChange(FIELD_ID_ENABLED, oldEnabled, newEnabled);
	}
	
	/**
	 * @return
	 */
	public int getDoneBlocks() {
		return doneBlocks;
	}
	
	/**
	 * @return
	 */
	public Boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return
	 */
	public int getTotalBlocks() {
		return totalBlocks;
	}

	/**
	 * @return
	 */
	public long getLastUploadStopTimeMillis() {
		return lastUploadStopTimeMillis;
	}
	/**
	 * @param lastUploadStopTimeMillis
	 */
	public void setLastUploadStopTimeMillis(long lastUploadStopTimeMillis) {
		this.lastUploadStopTimeMillis = lastUploadStopTimeMillis;
	}
}

