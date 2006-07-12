/*
  FrostDownloadItem.java / Frost

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
package frost.fileTransfer.download;

import frost.fileTransfer.search.FrostSearchItem;
import frost.gui.objects.Board;
import frost.util.model.ModelItem;

public class FrostDownloadItem extends ModelItem {

	// the constants representing field IDs

	public final static int FIELD_ID_DONE_BLOCKS = 100;
	public final static int FIELD_ID_ENABLED = 101;
	public final static int FIELD_ID_FILE_NAME = 103; 
	public final static int FIELD_ID_FILE_SIZE = 104;
	public final static int FIELD_ID_KEY = 105;
	public final static int FIELD_ID_OWNER = 106;
	public final static int FIELD_ID_REQUIRED_BLOCKS = 107;
	public final static int FIELD_ID_RETRIES = 108;
	public final static int FIELD_ID_SHA1 = 109;
	public final static int FIELD_ID_STATE = 110;
	public final static int FIELD_ID_SOURCE_BOARD = 111;
	public final static int FIELD_ID_TOTAL_BLOCKS = 112;
	

	// the constants representing download states
	public final static int STATE_WAITING    = 1; // wait for start
	public final static int STATE_TRYING     = 2; // download running
	public final static int STATE_DONE       = 3;
	public final static int STATE_FAILED     = 4;
	public final static int STATE_REQUESTING = 5; // requesting missing key
	public final static int STATE_PROGRESS   = 6; // download runs
	public final static int STATE_REQUESTED  = 7; // missing key requested
	public final static int STATE_DECODING   = 8; // decoding runs

	// the fields
	private String fileName = null;		//FIELD_ID_FILE_NAME
	private Long fileSize = null;			//FIELD_ID_FILE_SIZE
	private String key = null;			//FIELD_ID_KEY
	private Board sourceBoard;	//FIELD_ID_SOURCE_BOARD
	private int retries=0;			//FIELD_ID_RETRIES
	private Boolean enableDownload = new Boolean(true);			//FIELD_ID_ENABLED
	private String owner = null;			//FIELD_ID_OWNER
	private String sha1 = null;			//FIELD_ID_SHA1
	private int state = STATE_WAITING;				//FIELD_ID_STATE
    private int requestedCount = 0;
    private java.sql.Date lastRequestedDate = null;
    // time when download try finished, used for pause between tries
    private long lastDownloadStopTimeMillis = 0;
    
    // non persistent fields
	private int doneBlocks = 0;			//FIELD_ID_DONE_BLOCKS
	private int requiredBlocks = 0;		//FIELD_ID_REQUIRED_BLOCKS
	private int totalBlocks = 0;			//FIELD_ID_TOTAL_BLOCKS
	
	/**
	 * @param searchItem
	 */
	public FrostDownloadItem(FrostSearchItem searchItem) {
		fileName = searchItem.getFilename();
		fileSize = searchItem.getSize();
		key = searchItem.getKey();
		owner = searchItem.getOwner();
		sourceBoard = searchItem.getBoard();
		sha1 = searchItem.getSHA1();
		retries = 0;

		state = STATE_WAITING;
	}

    // add a file from download text box
	public FrostDownloadItem(String fileName, String key) {
		
		this.fileName = fileName;
		fileSize = null; // not set yet
		this.key = key;
		sourceBoard = null;
		retries = 0;

		state = STATE_WAITING;
	}

    // add a file attachment
    public FrostDownloadItem(String fileName, String key, Long s) {
        
        this.fileName = fileName;
        fileSize = s;
        this.key = key;
        sourceBoard = null;
        retries = 0;

        state = STATE_WAITING;
    }

    // add a saved file 
	public FrostDownloadItem(
		String fileName,
		Long fileSize,
		String key,
		int tries,
		String from,
		String SHA1,
		int state,
		boolean isDownloadEnabled,
		Board board,
        int requested,
        java.sql.Date lastReqDate,
        long lastStopped) 
    {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.retries = tries;
		this.key = key;
		this.sourceBoard = board;
		this.state = state;
		this.sha1 = SHA1;
		this.enableDownload = Boolean.valueOf(isDownloadEnabled);
		this.owner = from;
        this.requestedCount = requested;
        this.lastRequestedDate = lastReqDate;
        this.lastDownloadStopTimeMillis = lastStopped;
	}

	public String getFileName() {
		return fileName;
	}
	public Long getFileSize() {
		return fileSize;
	}
	/**
	 * @param newFileSize
	 */
	public void setFileSize(Long newFileSize) {
		Long oldFileSize = fileSize;
		fileSize = newFileSize;
		fireFieldChange(FIELD_ID_FILE_SIZE, oldFileSize, newFileSize);		
	}

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
	public Board getSourceBoard() {
		return sourceBoard;
	}

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

	public long getLastDownloadStopTimeMillis() {
		return lastDownloadStopTimeMillis;
	}
	public void setLastDownloadStopTimeMillis(long val) {
		lastDownloadStopTimeMillis = val;
	}

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

	public Boolean getEnableDownload() {
		return enableDownload;
	}
	/**
	 * @param enabled new enable status of the item. If null, the current 
	 * 		  status is inverted
	 */
	public void setEnableDownload(Boolean newEnabled) {
		if (newEnabled == null && enableDownload != null) {
			//Invert the enable status
			boolean enable = enableDownload.booleanValue();
			newEnabled = new Boolean(!enable);
		}
		Boolean oldEnabled = enableDownload;
		enableDownload = newEnabled;
		fireFieldChange(FIELD_ID_ENABLED, oldEnabled, newEnabled);
	}
	public String getOwner() {
		return owner;
	}

	/**
	 * @param newOwner
	 */
	public void setOwner(String newOwner) {
		String oldOwner = owner;
		owner = newOwner;
		fireFieldChange(FIELD_ID_OWNER, oldOwner, newOwner);
	}

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
	 * @param newFileName
	 */
	public void setFileName(String newFileName) {
		String oldFileName = fileName;
		fileName = newFileName;
		fireFieldChange(FIELD_ID_FILE_NAME, oldFileName, newFileName);
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
	public int getRequiredBlocks() {
		return requiredBlocks;
	}

	/**
	 * @return
	 */
	public int getTotalBlocks() {
		return totalBlocks;
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
	 * @param newRequiredBlocks
	 */
	public void setRequiredBlocks(int newRequiredBlocks) {
		int oldRequiredBlocks = requiredBlocks;
		requiredBlocks = newRequiredBlocks;
		fireFieldChange(FIELD_ID_REQUIRED_BLOCKS, oldRequiredBlocks, newRequiredBlocks);
	}

	/**
	 * @param newTotalBlocks
	 */
	public void setTotalBlocks(int newTotalBlocks) {
		int oldTotalBlocks = totalBlocks; 
		totalBlocks = newTotalBlocks;
		fireFieldChange(FIELD_ID_TOTAL_BLOCKS, oldTotalBlocks, newTotalBlocks);
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
}
