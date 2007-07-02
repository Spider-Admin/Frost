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

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.model.*;

public class FrostDownloadItem extends ModelItem implements CopyToClipboardItem {
    
    private transient static final Logger logger = Logger.getLogger(FrostDownloadItem.class.getName());
    
    // the constants representing download states
    public transient final static int STATE_WAITING    = 1; // wait for start
    public transient final static int STATE_TRYING     = 2; // download running
    public transient final static int STATE_DONE       = 3;
    public transient final static int STATE_FAILED     = 4;
    public transient final static int STATE_PROGRESS   = 5; // download runs
    public transient final static int STATE_DECODING   = 6; // decoding runs

	private String fileName = null;
    private String targetPath = null;
	private long fileSize = -1;	
	private String key = null;
    
    private Boolean enabled = Boolean.TRUE;
    private int state = STATE_WAITING;
    private long downloadAddedTime = 0;
    private long downloadStartedTime = 0;
    private long downloadFinishedTime = 0;
	private int retries = 0;
    private long lastDownloadStopTime = 0;
    private String gqIdentifier = null;
    
    // if this downloadfile is a shared file then this object is set
    private transient FrostFileListFileObject fileListFileObject = null;
    
    // non persistent fields
	private transient int doneBlocks = 0;
	private transient int requiredBlocks = 0;	
	private transient int totalBlocks = 0;
    private transient Boolean isFinalized = null;
    private transient String errorCodeDescription = null;
    private transient int priority = -1;

    private transient boolean isDirect = false;
    private transient boolean isExternal = false;
    
    private transient boolean internalRemoveExpected = false;
    
    /**
     * Add a file from download text box.
     */ 
	public FrostDownloadItem(String fileName, String key) {
		
        fileName = FileTransferManager.inst().getDownloadManager().ensureUniqueFilename(fileName);

        this.fileName = Mixed.makeFilename(fileName);
		this.key = key;

        gqIdentifier = buildGqIdentifier(fileName);
        
        downloadAddedTime = System.currentTimeMillis();

		state = STATE_WAITING;
	}

    /**
     * Add a file attachment.
     */ 
    public FrostDownloadItem(String fileName, String key, long s) {
        this(fileName, key);
        this.fileSize = s;
    }

    /**
     * Add a shared file from filelist (user searched file and choosed one of the names).
     */
    public FrostDownloadItem(FrostFileListFileObject newSfo, String newName) {

        newName = FileTransferManager.inst().getDownloadManager().ensureUniqueFilename(newName);

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
        fileSize = sfo.getSize();
        key = sfo.getKey();

        gqIdentifier = buildGqIdentifier(fileName);

        setFileListFileObject(sfo);

        downloadAddedTime = System.currentTimeMillis();

        state = STATE_WAITING;
    }
    
    /**
     * Add a saved file from database.
     */
	public FrostDownloadItem(
            String newFilename,
            String newTargetPath,
            long newSize,
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
        enabled = newEnabledownload;
        state = newState;
        downloadAddedTime = newDownloadAddedTime;
        downloadStartedTime = newDownloadStartedTime;
        downloadFinishedTime = newDownloadFinishedTime;
        retries = newRetries;
        lastDownloadStopTime = newLastDownloadStopTime;
        gqIdentifier = newGqId;

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
	public String getFilename() {
		return fileName;
	}

    public long getFileSize() {
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

	public Boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled new enable status of the item. If null, the current status is inverted
	 */
	public void setEnabled(Boolean newEnabled) {
		if (newEnabled == null && enabled != null) {
			//Invert the enable status
			boolean enable = enabled.booleanValue();
			newEnabled = new Boolean(!enable);
		}
		enabled = newEnabled;
        fireChange();
	}

	public int getDoneBlocks() {
		return doneBlocks;
	}
	public void setDoneBlocks(int newDoneBlocks) {
	    doneBlocks = newDoneBlocks;
	}
    
	public int getRequiredBlocks() {
		return requiredBlocks;
	}
	public void setRequiredBlocks(int newRequiredBlocks) {
	    requiredBlocks = newRequiredBlocks;
	}
	
	public int getTotalBlocks() {
		return totalBlocks;
	}
	public void setTotalBlocks(int newTotalBlocks) {
		totalBlocks = newTotalBlocks;
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

    public long getDownloadAddedMillis() {
        return downloadAddedTime;
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

    public String getGqIdentifier() {
        return gqIdentifier;
    }

    public void setGqIdentifier(String gqId) {
        this.gqIdentifier = gqId;
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
        
        int newState = -1;
        
        // if lastUploaded value changed, maybe restart failed download
        if( sharedFileObject != null && this.fileListFileObject != null ) {
//            if( sharedFileObject.getLastUploaded() > this.fileListFileObject.getLastUploaded() ) {
                if( getState() == STATE_FAILED ) {
                    newState = STATE_WAITING;
                }
//            }
        }
        
        this.fileListFileObject = sharedFileObject;
        
        if( this.fileListFileObject != null ) {
            this.fileListFileObject.addListener(this);
        }
        // take over key and update gui
        fireValueChanged();
        
        if( newState > -1 ) {
            setState(newState);
        }
    }
    
    /**
     * Called by a FrostFileListFileObject if a value interesting for FrostDownloadItem was set.
     */
    public void fireValueChanged() {
        // maybe take over the key, or set new key
        if( this.fileListFileObject != null ) {
//            if( getKey() == null || getKey().length() == 0 ) {
                if( this.fileListFileObject.getKey() != null && this.fileListFileObject.getKey().length() > 0 ) {
                    setKey( this.fileListFileObject.getKey() );
                }
//            }
        }
        // remaining values are dynamically fetched from FrostFileListFileObject
        super.fireChange();
    }

    /**
     * Builds a global queue identifier if running on 0.7.
     * Returns null on 0.5.
     */
    private String buildGqIdentifier(String filename) {
        if( FcpHandler.isFreenet07() ) {
            return new StringBuilder()
                .append("Frost-")
                .append(filename.replace(' ', '_'))
                .append("-")
                .append(System.currentTimeMillis())
                .append(Core.getCrypto().getSecureRandom().nextInt(10)) // 0-9
                .toString();
        } else {
            return null;
        }
    }

    public String getErrorCodeDescription() {
        return errorCodeDescription;
    }
    public void setErrorCodeDescription(String errorCodeDescription) {
        this.errorCodeDescription = errorCodeDescription;
    }
    
    /**
     * @return  true if this item is an external global queue item
     */
    public boolean isExternal() {
        return isExternal;
    }
    public void setExternal(boolean e) {
        isExternal = e;
    }
    
    public boolean isDirect() {
        return isDirect;
    }
    public void setDirect(boolean d) {
        isDirect = d;
        super.fireChange();
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
        super.fireChange();
    }

    /**
     * @return  true if the remove of this request was expected and item should not be removed from the table
     */
    public boolean isInternalRemoveExpected() {
        return internalRemoveExpected;
    }
    /**
     * Set to true if we restart a download (code=11 or 27). 
     * onPersistentRequestRemoved method checks this and does not remove the request
     * from the table if the remove was expected. 
     */
    public void setInternalRemoveExpected(boolean internalRemoveExpected) {
        this.internalRemoveExpected = internalRemoveExpected;
    }
    
    public String toString() {
        return getFilename();
    }
}
