/*
  FrostSharedFileItem.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.sharing;

import java.io.File;
import java.util.List;

import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.SharedFileXmlFile;
import frost.fileTransfer.filelist.FileListUploadThread;
import frost.fileTransfer.upload.FrostUploadItem;
import frost.util.CopyToClipboardItem;
import frost.util.DateFun;
import frost.util.model.ModelItem;

/**
 * This item is shown in the shared files panel, and it can be shown in the
 * table of current uploads.
 */
public class FrostSharedFileItem extends ModelItem<FrostSharedFileItem> implements CopyToClipboardItem {

    String sha = null;

    File file = null;
    long fileSize = 0;
    String key = null;

    String owner = null;
    String comment = null;
    int rating = 0;
    String keywords = null;

    long lastUploaded = 0;
    int uploadCount = 0;

    long refLastSent = 0;              // last time we sent this file inside a CHK file; set to 0 if item was changed

    long requestLastReceived = 0;      // time when we received the last request for this sha
    int requestsReceived = 0;          // received requests count

    long lastModified = 0;

    boolean isValid = true;

    /**
     * Used to add a new file. SHA1 must be created and must not be already in table!
     */
    public FrostSharedFileItem(final File newFile, final String newOwner, final String newSha) {
        file = newFile;
        fileSize = file.length();
        lastModified = newFile.lastModified();
        owner = newOwner;
        sha = newSha;
    }

    /**
     * Complete constructor used when loading from database table.
     */
    public FrostSharedFileItem(
            final File newFile,
            final long newFilesize,
            final String newKey,
            final String newSha,
            final String newOwner,
            final String newComment,
            final int newRating,
            final String newKeywords,
            final long newLastUploaded,
            final int newUploadCount,
            final long newRefLastSent,
            final long newRequestLastReceived,
            final int newRequestsReceived,
            final long newLastModified,
            final boolean newIsValid)
    {
        sha = newSha;
        file = newFile;
        fileSize = newFilesize;
        key = newKey;
        owner = newOwner;
        comment = newComment;
        rating = newRating;
        keywords = newKeywords;
        lastUploaded = newLastUploaded;
        uploadCount = newUploadCount;
        refLastSent = newRefLastSent;
        requestLastReceived = newRequestLastReceived;
        requestsReceived = newRequestsReceived;
        lastModified = newLastModified;
        isValid = newIsValid;
    }

    /**
     * Builds an instance that can be serialized to XML.
     * Used to send file references in index file.
     */
    public SharedFileXmlFile getSharedFileXmlFileInstance() {
        final SharedFileXmlFile sfxf = new SharedFileXmlFile();

        sfxf.setSha(getSha());
        sfxf.setSize(getFileSize());
        sfxf.setKey(getKey());
        sfxf.setFilename(getFile().getName());
        if( getLastUploaded() != 0 ) {
            sfxf.setLastUploaded(DateFun.FORMAT_DATE_EXT.print(getLastUploaded()));
        } else {
            sfxf.setLastUploaded(null);
        }
        sfxf.setComment(getComment());
        sfxf.setRating(getRating());
        sfxf.setKeywords(getKeywords());

        sfxf.ensureValidity();

        return sfxf;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(final String comment) {
        this.comment = comment;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(final String keywords) {
        this.keywords = keywords;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public long getRefLastSent() {
        return refLastSent;
    }
    public void setRefLastSent(final long refLastSent) {
        this.refLastSent = refLastSent;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }
    public void setLastUploaded(final long lastUploaded) {
        this.lastUploaded = lastUploaded;
        fireChange();
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(final String owner) {
        this.owner = owner;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public int getRating() {
        return rating;
    }
    public void setRating(final int rating) {
        this.rating = rating;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public long getRequestLastReceived() {
        return requestLastReceived;
    }
    public void setRequestLastReceived(final long requestLastReceived) {
        this.requestLastReceived = requestLastReceived;
        fireChange();
    }

    public int getRequestsReceived() {
        return requestsReceived;
    }
    public void setRequestsReceived(final int requestsReceived) {
        this.requestsReceived = requestsReceived;
        fireChange();
    }

    public int getUploadCount() {
        return uploadCount;
    }
    public void setUploadCount(final int uploadCount) {
        this.uploadCount = uploadCount;
        fireChange();
    }

    public String getSha() {
        return sha;
    }

    public void itemWasChanged() {
        // force a send in CHK next time
        this.refLastSent = 0;
    }

    public void userActionOccured() {
        // notify list upload thread that user changed something
        FileListUploadThread.getInstance().userActionOccured();
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String chkKey) {
        this.key = chkKey;
        itemWasChanged();
    }

    public File getFile() {
        return file;
    }
    public void setFile(final File f) {
        // caller ensured that size is the same
        file = f;
    }

    public String getFileName() {
        return file.getName();
    }

    public long getFileSize() {
        return fileSize;
    }

    public void notifySuccessfulUpload(final String newKey) {
        // an upload of this file was successful
        setKey(newKey);
        setLastUploaded(System.currentTimeMillis());
        setUploadCount(getUploadCount() + 1);

        itemWasChanged();
    }

    public boolean isCurrentlyUploading() {
        final List<FrostUploadItem> uploadItems = FileTransferManager.inst().getUploadManager().getModel().getItems();
        for( final FrostUploadItem ulItem : uploadItems ) {
            final FrostSharedFileItem sfi = ulItem.getSharedFileItem();
            if( sfi == this ) {
                // this upload item is for this shared file
                // the file is only in upload table if it really uploads currently
                return true;
            }
        }
        return false; // not in upload table
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(final boolean isValid) {
        this.isValid = isValid;
        if( isValid ) {
            fireChange();
            itemWasChanged();
            userActionOccured();
        } else {
            fireChange();
        }
    }

    @Override
    public String toString() {
        return getFileName();
    }
}
