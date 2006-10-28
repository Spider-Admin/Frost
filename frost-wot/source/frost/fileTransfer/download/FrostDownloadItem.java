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

import java.util.logging.*;

import frost.fileTransfer.*;
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.model.*;

public class FrostDownloadItem extends ModelItem {
    
    private static Logger logger = Logger.getLogger(FrostDownloadItem.class.getName());
    
    // the constants representing download states
    public final static int STATE_WAITING    = 1; // wait for start
    public final static int STATE_TRYING     = 2; // download running
    public final static int STATE_DONE       = 3;
    public final static int STATE_FAILED     = 4;
    public final static int STATE_PROGRESS   = 5; // download runs
    public final static int STATE_DECODING   = 6; // decoding runs

	private String fileName = null;
    private String targetPath = null;
	private Long fileSize = null;	
	private String key = null;
    
    private Boolean enableDownload = Boolean.TRUE;
    private int state = STATE_WAITING;
    private long downloadAddedTime = 0;
    private long downloadStartedTime = 0;
    private long downloadFinishedTime = 0;
	private int retries = 0;
    private long lastDownloadStopTime = 0;
    private String gqId = null;
    
    // if this downloadfile is a shared file then this object is set
    private FrostFileListFileObject fileListFileObject = null;
    
    // non persistent fields
	private int doneBlocks = 0;
	private int requiredBlocks = 0;	
	private int totalBlocks = 0;
    private Boolean isFinalized = null;
    
    // add a file from download text box
	public FrostDownloadItem(String fileName, String key) {
		
		this.fileName = fileName;
		this.key = key;
        
        gqId = fileName.replace(' ', '_')+"-"+Mixed.createUniqueId();

		state = STATE_WAITING;
	}

    // add a file attachment
    public FrostDownloadItem(String fileName, String key, Long s) {
        
        this.fileName = fileName;
        fileSize = s;
        this.key = key;
        
        gqId = fileName.replace(' ', '_')+"-"+Mixed.createUniqueId();

        state = STATE_WAITING;
    }

    // add a shared file from filelist (user searched file and choosed one of the names)
    public FrostDownloadItem(FrostFileListFileObject newSfo, String newName) {
        
        FrostFileListFileObject sfo = null;
        
        // update the shared file object from database (key, owner, sources, ... may have changed)
        FrostFileListFileObject updatedSfo = null;
        try {
            updatedSfo = AppLayerDatabase.getFileListDatabaseTable().retrieveFileBySha(newSfo.getSha());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception in retrieveFileBySha", t);
        }
        if( updatedSfo != null ) {
            sfo = updatedSfo;
        } else {
            // paranoia fallback
            sfo = newSfo;
        }
        
        fileName = newName;
        fileSize = new Long(sfo.getSize());
        key = sfo.getKey();

        gqId = fileName.replace(' ', '_')+"-"+Mixed.createUniqueId();

        setFileListFileObject(sfo);

        state = STATE_WAITING;
    }

    // add a saved file 
	public FrostDownloadItem(
            String newFilename,
            String newTargetPath,
            Long newSize,
            String newKey,
            Boolean newEnabledownload,
            int newState,
            long newDownloadAddedTime,
            long newDownloadStartedTime,
            long newDownloadFinishedTime,
            int newRetries,
            long newLastDownloadStopTime,
            String newGqId)
    {
        fileName = newFilename;
        targetPath = newTargetPath;
        fileSize = newSize;   
        key = newKey;
        enableDownload = newEnabledownload;
        state = newState;
        downloadAddedTime = newDownloadAddedTime;
        downloadStartedTime = newDownloadStartedTime;
        downloadFinishedTime = newDownloadFinishedTime;
        retries = newRetries;
        lastDownloadStopTime = newLastDownloadStopTime;
        gqId = newGqId;

        // set correct state
        if (this.state != FrostDownloadItem.STATE_DONE) {
            this.state = FrostDownloadItem.STATE_WAITING;
        }
	}

    public boolean isSharedFile() {
        return getFileListFileObject() != null;
    }
    
    /**
     * Used only to set a new name if an item with same name is already in download table.
     */
    public void setFileName(String s) {
        fileName = s;
    }
	public String getFileName() {
		return fileName;
	}

    public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long newFileSize) {
		fileSize = newFileSize;
        fireChange();
	}

	public String getKey() {
		return key;
	}
	public void setKey(String newKey) {
		key = newKey;
        fireChange();
	}

	public int getState() {
		return state;
	}
	public void setState(int newState) {
		state = newState; 
        fireChange();
	}

	public long getLastDownloadStopTime() {
		return lastDownloadStopTime;
	}
	public void setLastDownloadStopTime(long val) {
        lastDownloadStopTime = val;
	}

	public int getRetries() {
		return retries;
	}
	public void setRetries(int newRetries) {
		retries = newRetries;
        fireChange();
	}

	public Boolean getEnableDownload() {
		return enableDownload;
	}
	/**
	 * @param enabled new enable status of the item. If null, the current status is inverted
	 */
	public void setEnableDownload(Boolean newEnabled) {
		if (newEnabled == null && enableDownload != null) {
			//Invert the enable status
			boolean enable = enableDownload.booleanValue();
			newEnabled = new Boolean(!enable);
		}
		enableDownload = newEnabled;
        fireChange();
	}

	public int getDoneBlocks() {
		return doneBlocks;
	}
	public void setDoneBlocks(int newDoneBlocks) {
	    doneBlocks = newDoneBlocks;
	    fireChange();
	}
    
	public int getRequiredBlocks() {
		return requiredBlocks;
	}
	public void setRequiredBlocks(int newRequiredBlocks) {
	    requiredBlocks = newRequiredBlocks;
	    fireChange();
	}
	
	public int getTotalBlocks() {
		return totalBlocks;
	}
	public void setTotalBlocks(int newTotalBlocks) {
		totalBlocks = newTotalBlocks;
        fireChange();
	}

    public Boolean isFinalized() {
        return isFinalized;
    }
    public void setFinalized(boolean finalized) {
        if( finalized ) {
            isFinalized = Boolean.TRUE;
        } else {
            isFinalized = Boolean.FALSE;
        }
    }

    public long getDownloadAddedTime() {
        return downloadAddedTime;
    }

    public void setDownloadAddedTime(long downloadAddedTime) {
        this.downloadAddedTime = downloadAddedTime;
    }

    public long getDownloadFinishedTime() {
        return downloadFinishedTime;
    }

    public void setDownloadFinishedTime(long downloadFinishedTime) {
        this.downloadFinishedTime = downloadFinishedTime;
    }

    public long getDownloadStartedTime() {
        return downloadStartedTime;
    }

    public void setDownloadStartedTime(long downloadStartedTime) {
        this.downloadStartedTime = downloadStartedTime;
    }

    public String getGqId() {
        return gqId;
    }

    public void setGqId(String gqId) {
        this.gqId = gqId;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
    
    public long getLastReceived() {
        if( getFileListFileObject() == null ) {
            return 0;
        } else {
            return getFileListFileObject().getLastReceived();
        }
    }

    public long getLastUploaded() {
        if( getFileListFileObject() == null ) {
            return 0;
        } else {
            return getFileListFileObject().getLastUploaded();
        }
    }

    public FrostFileListFileObject getFileListFileObject() {
        return fileListFileObject;
    }

    public void setFileListFileObject(FrostFileListFileObject sharedFileObject) {
        if( this.fileListFileObject != null ) {
            this.fileListFileObject.removeListener(this);
        }
        this.fileListFileObject = sharedFileObject;
        if( this.fileListFileObject != null ) {
            this.fileListFileObject.addListener(this);
        }
        // take over key and update gui
        fireValueChanged();
    }
    
    /**
     * Called by a FrostFileListFileObject if a value interesting for FrostDownloadItem was set.
     */
    public void fireValueChanged() {
        // maybe take over the key
        if( this.fileListFileObject != null ) {
            if( getKey() == null || getKey().length() == 0 ) {
                if( this.fileListFileObject.getKey() != null && this.fileListFileObject.getKey().length() > 0 ) {
                    setKey( this.fileListFileObject.getKey() );
                }
            }
        }
        // remaining values are dynamically fetched from FrostFileListFileObject
        super.fireChange();
    }
}
