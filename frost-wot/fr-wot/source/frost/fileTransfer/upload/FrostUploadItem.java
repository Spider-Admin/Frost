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

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.sharing.*;
import frost.util.*;
import frost.util.model.*;

/**
 * Represents a file to upload.
 */
public class FrostUploadItem extends ModelItem implements CopyToClipboardItem {

    // the constants representing upload states
    public final static int STATE_DONE       = 1;   // a start of uploading is requested
//    public final static int STATE_UPLOADING  = 3;
    public final static int STATE_PROGRESS   = 4;   // upload runs, shows "... kb"
    public final static int STATE_ENCODING_REQUESTED  = 5; // an encoding of file is requested
    public final static int STATE_ENCODING   = 6;   // the encode is running
    public final static int STATE_WAITING    = 7;   // waiting until the next retry
    public final static int STATE_FAILED     = 8;

    private File file = null;
    private long fileSize = 0;   
    private String chkKey = null;
    private Boolean enabled = Boolean.TRUE;
    private int state;
    private long uploadAddedMillis = 0;
    private long uploadStartedMillis = 0;
    private long uploadFinishedMillis = 0;
    private int retries = 0;
    private long lastUploadStopTimeMillis = 0; // millis when upload stopped the last time, needed to schedule uploads
    private String gqIdentifier = null;
    
    // non-persistent fields
    private int totalBlocks = -1;
    private int doneBlocks = -1; 
    private Boolean isFinalized = null;
    private String errorCodeDescription = null;
    private int priority = -1;

    // is only set if this uploaditem is a shared file
    private FrostSharedFileItem sharedFileItem = null;
    
    private boolean isExternal = false;

    /**
     * Dummy to use for uploads of attachments. Is never saved.
     * Attachment uploads must never be persistent on 0.7.
     * We indicate this with gqIdentifier == null
     * Also used for external global queue items on 0.7.
     */
    public FrostUploadItem() {
    }

    /**
     * Used to add a new file to upload.
     * Either manually added or a shared file.
     */
    public FrostUploadItem(File newFile) {

        file = newFile;
        fileSize = file.length();
        
        gqIdentifier = buildGqIdentifier(file.getName());
        
        uploadAddedMillis = System.currentTimeMillis();
        
        state = STATE_WAITING;
    }

    /**
     * Constructor used by loadUploadTable.
     */
    public FrostUploadItem(
            File newFile,
            long newFilesize,
            String newKey,
            boolean newIsEnabled,
            int newState,
            long newUploadAdded,
            long newUploadStarted,
            long newUploadFinished,
            int newRetries,
            long newLastUploadStopTimeMillis,
            String newGqIdentifier)
    {
        file = newFile;
        fileSize = newFilesize;
        chkKey = newKey;
        enabled = Boolean.valueOf(newIsEnabled);
        state = newState;
        uploadAddedMillis = newUploadAdded;
        uploadStartedMillis = newUploadStarted;
        uploadFinishedMillis = newUploadFinished;
        retries = newRetries;
        lastUploadStopTimeMillis = newLastUploadStopTimeMillis;
        gqIdentifier = newGqIdentifier;

        // set correct state
        if ((state == FrostUploadItem.STATE_PROGRESS) /*|| (state == FrostUploadItem.STATE_UPLOADING)*/ ) {
            state = FrostUploadItem.STATE_WAITING;
        } else if ((state == FrostUploadItem.STATE_ENCODING) || (state == FrostUploadItem.STATE_ENCODING_REQUESTED)) {
            state = FrostUploadItem.STATE_WAITING;
        }
    }
    
    public boolean isSharedFile() {
        return getSharedFileItem() != null;
    }

    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long newFileSize) {
        fileSize = newFileSize.longValue();
        fireChange();
    }

    public String getKey() {
        return chkKey;
    }
    public void setKey(String newKey) {
        chkKey = newKey;
        fireChange();
    }

    public int getState() {
        return state;
    }
    public void setState(int newState) {
        state = newState;
        fireChange();
    }
    
    public int getTotalBlocks() {
        return totalBlocks;
    }
    public void setTotalBlocks(int newTotalBlocks) {
        totalBlocks = newTotalBlocks;
    }

    public int getRetries() {
        return retries;
    }
    public void setRetries(int newRetries) {
        retries = newRetries;
        fireChange();
    }

    public int getDoneBlocks() {
        return doneBlocks;
    }
    public void setDoneBlocks(int newDoneBlocks) {
        doneBlocks = newDoneBlocks;
    }

    /**
     * @param enabled new enable status of the item. If null, the current status is inverted
     */
    public void setEnabled(Boolean newEnabled) {
        if (newEnabled == null && enabled != null) {
            //Invert the enable status
            boolean temp = enabled.booleanValue();
            newEnabled = Boolean.valueOf(!temp);
        }
        enabled = newEnabled;
        fireChange();
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public long getLastUploadStopTimeMillis() {
        return lastUploadStopTimeMillis;
    }
    public void setLastUploadStopTimeMillis(long lastUploadStopTimeMillis) {
        this.lastUploadStopTimeMillis = lastUploadStopTimeMillis;
    }
    
    public long getUploadAddedMillis() {
        return uploadAddedMillis;
    }
    public long getUploadStartedMillis() {
        return uploadStartedMillis;
    }
    public void setUploadStartedMillis(long v) {
        uploadStartedMillis = v;
        fireChange();
    }

    public long getUploadFinishedMillis() {
        return uploadFinishedMillis;
    }
    public void setUploadFinishedMillis(long v) {
        uploadFinishedMillis = v;
        fireChange();
    }

    public String getGqIdentifier() {
        return gqIdentifier;
    }
    public void setGqIdentifier(String i) {
        gqIdentifier = i;
    }

    public FrostSharedFileItem getSharedFileItem() {
        return sharedFileItem;
    }

    public void setSharedFileItem(FrostSharedFileItem sharedFileItem) {
        this.sharedFileItem = sharedFileItem;
    }
    
    public String getFilename() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }
    public void setFile(File f) {
        file = f;
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
    
    public void fireValueChanged() {
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        super.fireChange();
    }
    
    public String toString() {
        return getFilename();
    }
}
