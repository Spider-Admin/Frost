/*
  FileListStorageRoot.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.filelist;

import org.garret.perst.IPersistentSet;
import org.garret.perst.Index;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

import frost.fileTransfer.FrostFileListFileObject;

public class FileListStorageRoot extends Persistent {

    // bit constants for storageStatus
    public static transient final int OLD_07_CHK_KEYS_REMOVED = 2;

    private int storageStatus;

    private Index<FrostFileListFileObject> fileListFileObjects;

    private Index<PerstIdentitiesFiles> identitiesFiles;

    private Index<PerstFileListIndexEntry> fileNameIndex;
    private Index<PerstFileListIndexEntry> fileCommentIndex;
    private Index<PerstFileListIndexEntry> fileKeywordIndex;
    private Index<PerstFileListIndexEntry> fileOwnerIndex;

    private IPersistentSet<PerstHiddenFileOid> hiddenFileOids;

    public FileListStorageRoot() {}

    public FileListStorageRoot(final Storage storage) {
        fileListFileObjects = storage.createIndex(String.class, true);
        identitiesFiles = storage.createIndex(String.class, true);

        fileNameIndex = storage.createIndex(String.class, true);
        fileCommentIndex = storage.createIndex(String.class, true);
        fileKeywordIndex = storage.createIndex(String.class, true);
        fileOwnerIndex = storage.createIndex(String.class, true);

        // new Set to hold the oid of currently hidden file list files
        hiddenFileOids = storage.createSet();
    }

    /**
     * Ensure that the new fields are already created.
     */
    public void createNewFields(final Storage storage) {
        if (hiddenFileOids == null) {
            hiddenFileOids = storage.createSet();
        }
    }

    public IPersistentSet<PerstHiddenFileOid> getHiddenFileOids() {
        return hiddenFileOids;
    }

    public Index<FrostFileListFileObject> getFileListFileObjects() {
        return fileListFileObjects;
    }

    public Index<PerstIdentitiesFiles> getIdentitiesFiles() {
        return identitiesFiles;
    }

    public Index<PerstFileListIndexEntry> getFileNameIndex() {
        return fileNameIndex;
    }

    public Index<PerstFileListIndexEntry> getFileCommentIndex() {
        return fileCommentIndex;
    }

    public Index<PerstFileListIndexEntry> getFileKeywordIndex() {
        return fileKeywordIndex;
    }

    public Index<PerstFileListIndexEntry> getFileOwnerIndex() {
        return fileOwnerIndex;
    }

    public int getStorageStatus() {
        return storageStatus;
    }

    public void setStorageStatus(final int storageStatus) {
        this.storageStatus = storageStatus;
    }
}
