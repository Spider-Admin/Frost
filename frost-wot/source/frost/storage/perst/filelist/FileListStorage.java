/*
  FileListStorage.java / Frost
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

import java.beans.*;
import java.util.*;

import org.garret.perst.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.storage.*;
import frost.storage.perst.*;

public class FileListStorage extends AbstractFrostStorage implements ExitSavable, PropertyChangeListener {

    private FileListStorageRoot storageRoot = null;

    private static final String STORAGE_FILENAME = "filelist.dbs";

    private static FileListStorage instance = new FileListStorage();

    private boolean rememberSharedFileDownloaded;

    protected FileListStorage() {
        super();
    }

    public static FileListStorage inst() {
        return instance;
    }

    @Override
    public String getStorageFilename() {
        return STORAGE_FILENAME;
    }

    @Override
    public boolean initStorage() {
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED, this);

        final String databaseFilePath = buildStoragePath(getStorageFilename()); // path to the database file
        final int pagePoolSize = getPagePoolSize(SettingsClass.PERST_PAGEPOOLSIZE_FILELIST);

        open(databaseFilePath, pagePoolSize, true, true, false);

        storageRoot = (FileListStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new FileListStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commit(); // commit transaction
        }
        return true;
    }

    public void silentClose() {
        close();
        storageRoot = null;
    }

    public void exitSave() {
        close();
        storageRoot = null;
        System.out.println("INFO: FileListStorage closed.");
    }

    public IPersistentList createList() {
        return getStorage().createScalableList();
    }

    public boolean insertOrUpdateFileListFileObject(final FrostFileListFileObject flf) {
        // check for dups and update them!
        final FrostFileListFileObject pflf = storageRoot.getFileListFileObjects().get(flf.getSha());
        if( pflf == null ) {
            // insert new
            storageRoot.getFileListFileObjects().put(flf.getSha(), flf);

            for( final Iterator<FrostFileListFileObjectOwner> i = flf.getFrostFileListFileObjectOwnerIterator(); i.hasNext(); ) {
                final FrostFileListFileObjectOwner o = i.next();
                addFileListFileOwnerToIndices(o);
            }
        } else {
            // update existing
            updateFileListFileFromOtherFileListFile(pflf, flf);
        }
        return true;
    }

    /**
     * Adds a new FrostFileListFileObjectOwner to the indices.
     */
    private void addFileListFileOwnerToIndices(final FrostFileListFileObjectOwner o) {
        // maybe the owner already shares other files
        PerstIdentitiesFiles pif = storageRoot.getIdentitiesFiles().get(o.getOwner());
        if( pif == null ) {
            pif = new PerstIdentitiesFiles(o.getOwner(), getStorage());
            storageRoot.getIdentitiesFiles().put(o.getOwner(), pif);
        }
        pif.addFileToIdentity(o);

        // add to indices
        maybeAddFileListFileInfoToIndex(o.getName(), o, storageRoot.getFileNameIndex());
        maybeAddFileListFileInfoToIndex(o.getComment(), o, storageRoot.getFileCommentIndex());
        maybeAddFileListFileInfoToIndex(o.getKeywords(), o, storageRoot.getFileKeywordIndex());
        maybeAddFileListFileInfoToIndex(o.getOwner(), o, storageRoot.getFileOwnerIndex());
    }

    private void maybeAddFileListFileInfoToIndex(String lName, final FrostFileListFileObjectOwner o, final Index<PerstFileListIndexEntry> ix) {
        if( lName == null || lName.length() == 0 ) {
            return;
        }
        lName = lName.toLowerCase();
        PerstFileListIndexEntry ie = ix.get(lName);
        if( ie == null ) {
            ie = new PerstFileListIndexEntry(getStorage());
            ix.put(lName, ie);
        }
        ie.getFileOwnersWithText().add(o);
    }

    public FrostFileListFileObject getFileBySha(final String sha) {
        if( !beginCooperativeThreadTransaction() ) {
            return null;
        }
        final FrostFileListFileObject o;
        try {
            o = storageRoot.getFileListFileObjects().get(sha);
        } finally {
            endThreadTransaction();
        }
        return o;
    }

    public int getFileCount() {
        if( !beginCooperativeThreadTransaction() ) {
            return 0;
        }
        final int count;
        try {
            count = storageRoot.getFileListFileObjects().size();
        } finally {
            endThreadTransaction();
        }
        return count;
    }

    public int getFileCount(final String idUniqueName) {
        if( !beginCooperativeThreadTransaction() ) {
            return 0;
        }
        final int count;
        try {
            final PerstIdentitiesFiles pif = storageRoot.getIdentitiesFiles().get(idUniqueName);
            if( pif != null ) {
                count = pif.getFilesFromIdentity().size();
            } else {
                count = 0;
            }
        } finally {
            endThreadTransaction();
        }
        return count;
    }

    public int getSharerCount() {
        if( !beginCooperativeThreadTransaction() ) {
            return 0;
        }
        final int count;
        try {
            count = storageRoot.getIdentitiesFiles().size();
        } finally {
            endThreadTransaction();
        }
        return count;
    }

    public long getFileSizes() {
        if( !beginCooperativeThreadTransaction() ) {
            return 0L;
        }
        long sizes = 0;
        try {
            for( final FrostFileListFileObject fo : storageRoot.getFileListFileObjects() ) {
                sizes += fo.getSize();
            }
        } finally {
            endThreadTransaction();
        }
        return sizes;
    }

    private void maybeRemoveFileListFileInfoFromIndex(
            final String lName,
            final FrostFileListFileObjectOwner o,
            final Index<PerstFileListIndexEntry> ix)
    {
        if( lName != null && lName.length() > 0 ) {
            final String lowerCaseName = lName.toLowerCase();
            final PerstFileListIndexEntry ie = ix.get(lowerCaseName);
            if( ie != null ) {
//                System.out.println("ix-remove: "+o.getOid());
                ie.getFileOwnersWithText().remove(o);

                if( ie.getFileOwnersWithText().size() == 0 ) {
                    // no more owners for this text, remove from index
                    if( ix.remove(lowerCaseName) != null ) {
                        ie.deallocate();
                    }
                }
            }
        }
    }

    /**
     * Remove owners that were not seen for more than MINIMUM_DAYS_OLD days and have no CHK key set.
     */
    public int cleanupFileListFileOwners(final int maxDaysOld) {

        if( !beginExclusiveThreadTransaction() ) {
            return 0;
        }

        final boolean removeOld07ChkKeys;
        if( FcpHandler.isFreenet07()
                && (storageRoot.getStorageStatus() & FileListStorageRoot.OLD_07_CHK_KEYS_REMOVED) == 0 )
        {
            removeOld07ChkKeys = true;
        } else {
            removeOld07ChkKeys = false;
        }

        int count = 0;
        try {
            final long minVal = System.currentTimeMillis() - (maxDaysOld * 24L * 60L * 60L * 1000L);
            for( final PerstIdentitiesFiles pif : storageRoot.getIdentitiesFiles() ) {
                for( final Iterator<FrostFileListFileObjectOwner> i = pif.getFilesFromIdentity().iterator(); i.hasNext(); ) {
                    final FrostFileListFileObjectOwner o = i.next();
                    boolean remove = false;

                    if( o.getLastReceived() < minVal && o.getKey() == null ) {
                        // outdated and no key
                        remove = true;

                    } else if( removeOld07ChkKeys && FreenetKeys.isOld07ChkKey(o.getKey()) ) {
                        // we have a key (outdated or not)
                        // check if the key is an old 0.7 CHK key
                        remove = true;
                    }

                    if( remove ) {
                        // remove this owner file info from file list object
                        final FrostFileListFileObject fof = o.getFileListFileObject();
                        o.setFileListFileObject(null);
                        fof.deleteFrostFileListFileObjectOwner(o);

                        // remove from indices
                        maybeRemoveFileListFileInfoFromIndex(o.getName(), o, storageRoot.getFileNameIndex());
                        maybeRemoveFileListFileInfoFromIndex(o.getComment(), o, storageRoot.getFileCommentIndex());
                        maybeRemoveFileListFileInfoFromIndex(o.getKeywords(), o, storageRoot.getFileKeywordIndex());
                        maybeRemoveFileListFileInfoFromIndex(o.getOwner(), o, storageRoot.getFileOwnerIndex());

                        //                    System.out.println("dealloc: "+o.getOid());

                        // remove this owner file info from identities files
                        i.remove();
                        // delete from store
                        o.deallocate();

                        fof.modify();

                        count++;
                    }
                }
                if( pif.getFilesFromIdentity().size() == 0 ) {
                    // no more files for this identity, remove
                    if( storageRoot.getIdentitiesFiles().remove(pif.getUniqueName()) != null ) {
                        pif.deallocate();
                    }
                }
            }
        } finally {
            endThreadTransaction();
        }
        return count;
    }

    /**
     * Remove files that have no owner and no CHK key.
     */
    public int cleanupFileListFiles(final boolean removeOfflineFilesWithKey, final int offlineFilesMaxDaysOld) {
        if( !beginExclusiveThreadTransaction() ) {
            return 0;
        }

        final boolean removeOld07ChkKeys;
        if( FcpHandler.isFreenet07()
                && (storageRoot.getStorageStatus() & FileListStorageRoot.OLD_07_CHK_KEYS_REMOVED) == 0 )
        {
            removeOld07ChkKeys = true;
        } else {
            removeOld07ChkKeys = false;
        }

        final long minVal;
        if( removeOfflineFilesWithKey ) {
            minVal = System.currentTimeMillis() - (offlineFilesMaxDaysOld * 24L * 60L * 60L * 1000L);
        } else {
            minVal = 0;
        }

        int count = 0;
        try {
            for( final Iterator<FrostFileListFileObject> i = storageRoot.getFileListFileObjects().iterator(); i.hasNext(); ) {
                final FrostFileListFileObject fof = i.next();
                if( fof.getFrostFileListFileObjectOwnerListSize() == 0 ) {
                    boolean remove = false;

                    if( fof.getKey() == null ) {
                        remove = true;
                    } else if( removeOfflineFilesWithKey && fof.getLastReceived() < minVal) {
                        remove = true;
                    } else if( removeOld07ChkKeys && FreenetKeys.isOld07ChkKey(fof.getKey()) ) {
                        remove = true;
                    }

                    if( remove ) {
                        i.remove();
                        fof.deallocate();
                        count++;
                    }
                }
            }
        } finally {
            endThreadTransaction();
        }

        if( removeOld07ChkKeys ) {
            // we removed the keys, update flag
            final int newStorageStatus = storageRoot.getStorageStatus() | FileListStorageRoot.OLD_07_CHK_KEYS_REMOVED;
            storageRoot.setStorageStatus( newStorageStatus );
            storageRoot.modify();
        }

        return count;
    }

    /**
     * Reset the lastdownloaded column for all file entries.
     */
    public void resetLastDownloaded() {

        if( !beginExclusiveThreadTransaction() ) {
            return;
        }
        try {
            for( final FrostFileListFileObject fof : storageRoot.getFileListFileObjects() ) {
                fof.setLastDownloaded(0);
                fof.modify();
            }
        } finally {
            endThreadTransaction();
        }
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount.
     * Does NOT commit!
     */
    public boolean updateFrostFileListFileObjectAfterRequestSent(final String sha, final long requestLastSent) {

        final FrostFileListFileObject oldSfo = getFileBySha(sha);
        if( oldSfo == null ) {
            return false;
        }

        oldSfo.setRequestLastSent(requestLastSent);
        oldSfo.setRequestsSentCount(oldSfo.getRequestsSentCount() + 1);

        oldSfo.modify();

        return true;
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount
     * Does NOT commit!
     */
    public boolean updateFrostFileListFileObjectAfterRequestReceived(final String sha, long requestLastReceived) {

        final FrostFileListFileObject oldSfo = getFileBySha(sha);
        if( oldSfo == null ) {
            return false;
        }

        if( oldSfo.getRequestLastReceived() > requestLastReceived ) {
            requestLastReceived = oldSfo.getRequestLastReceived();
        }

        oldSfo.setRequestLastReceived(requestLastReceived);
        oldSfo.setRequestsReceivedCount(oldSfo.getRequestsReceivedCount() + 1);

        oldSfo.modify();

        return true;
    }

    /**
     * Update the item with SHA, set lastdownloaded
     */
    public boolean updateFrostFileListFileObjectAfterDownload(final String sha, final long lastDownloaded) {

        if( !rememberSharedFileDownloaded ) {
            return true;
        }

        if( !beginExclusiveThreadTransaction() ) {
            return false;
        }

        try {
            final FrostFileListFileObject oldSfo = getFileBySha(sha);
            if( oldSfo == null ) {
                endThreadTransaction();
                return false;
            }
            oldSfo.setLastDownloaded(lastDownloaded);
            oldSfo.modify();
        } finally {
            endThreadTransaction();
        }

        return true;
    }

    /**
     * Retrieves a list of FrostSharedFileOjects.
     */
    public void retrieveFiles(
            final FileListCallback callback,
            final List<String> names,
            final List<String> comments,
            final List<String> keywords,
            final List<String> owners,
            String[] extensions)
    {
        if( !beginCooperativeThreadTransaction() ) {
            return;
        }

        System.out.println("Starting file search...");
        final long t = System.currentTimeMillis();

        boolean searchForNames = true;
        boolean searchForComments = true;
        boolean searchForKeywords = true;
        boolean searchForOwners = true;
        boolean searchForExtensions = true;

        if( names == null    || names.size() == 0 )    { searchForNames = false; }
        if( comments == null || comments.size() == 0 ) { searchForComments = false; }
        if( keywords == null || keywords.size() == 0 ) { searchForKeywords = false; }
        if( owners == null   || owners.size() == 0 )   { searchForOwners = false; }
        if( extensions == null || extensions.length == 0 )   { searchForExtensions = false; }

        try {
            if( !searchForNames && !searchForComments && ! searchForKeywords && !searchForOwners && !searchForExtensions) {
                // find ALL files
                for(final FrostFileListFileObject o : storageRoot.getFileListFileObjects()) {
                    if(callback.fileRetrieved(o)) {
                        return; // stop requested
                    }
                }
                return;
            }

            if( !searchForExtensions ) {
                extensions = null;
            }

            final HashSet<Integer> ownerOids = new HashSet<Integer>();

            if( searchForNames || searchForExtensions ) {
                searchForFiles(ownerOids, names, extensions, storageRoot.getFileNameIndex());
            }

            if( searchForComments ) {
                searchForFiles(ownerOids, comments, null, storageRoot.getFileCommentIndex());
            }

            if( searchForKeywords ) {
                searchForFiles(ownerOids, keywords, null, storageRoot.getFileKeywordIndex());
            }

            if( searchForOwners ) {
                searchForFiles(ownerOids, owners, null, storageRoot.getFileOwnerIndex());
            }

            final HashSet<Integer> fileOids = new HashSet<Integer>();
            for( final Integer i : ownerOids ) {
//                System.out.println("search-oid: "+i);
                final FrostFileListFileObjectOwner o = (FrostFileListFileObjectOwner)getStorage().getObjectByOID(i);
                final int oid = o.getFileListFileObject().getOid();
                fileOids.add(oid);
            }

            for( final Integer i : fileOids ) {
                final FrostFileListFileObject o = (FrostFileListFileObject)getStorage().getObjectByOID(i);
                if( o != null ) {
                    if(callback.fileRetrieved(o)) {
                        return;
                    }
                }
            }
        } finally {
            System.out.println("Finished file search, duration="+(System.currentTimeMillis() - t));
            endThreadTransaction();
        }
    }

    private void searchForFiles(
            final HashSet<Integer> oids,
            final List<String> searchStrings,
            final String[] extensions, // only used for name search
            final Index<PerstFileListIndexEntry> ix)
    {
        for(final Map.Entry<Object,PerstFileListIndexEntry> entry : ix.entryIterator() ) {
            final String key = (String)entry.getKey();
            if( searchStrings != null ) {
                for(final String searchString : searchStrings) {
                    if( key.indexOf(searchString) > -1 ) {
                        // add all owner oids
                        final Iterator<FrostFileListFileObjectOwner> i = entry.getValue().getFileOwnersWithText().iterator();
                        while(i.hasNext()) {
                            final int oid = ((PersistentIterator)i).nextOid();
                            oids.add(oid);
                        }
                    }
                }
            }
            if( extensions != null ) {
                for( final String extension : extensions ) {
                    if( key.endsWith(extension) ) {
                        // add all owner oids
                        final Iterator<FrostFileListFileObjectOwner> i = entry.getValue().getFileOwnersWithText().iterator();
                        while(i.hasNext()) {
                            final int oid = ((PersistentIterator)i).nextOid();
                            oids.add(oid);
                        }
                    }
                }
            }
        }
    }

    private boolean updateFileListFileFromOtherFileListFile(final FrostFileListFileObject oldFof, final FrostFileListFileObject newFof) {
        // file is already in FILELIST table, maybe add new FILEOWNER and update fields
        // maybe update oldSfo
        boolean doUpdate = false;
        if( oldFof.getKey() == null && newFof.getKey() != null ) {
            oldFof.setKey(newFof.getKey()); doUpdate = true;
        } else if( oldFof.getKey() != null && newFof.getKey() != null ) {
            // fix to replace 0.7 keys before 1010 on the fly
            if( FreenetKeys.isOld07ChkKey(oldFof.getKey()) ) {
                // replace old chk key with new one
                oldFof.setKey(newFof.getKey()); doUpdate = true;
            }
        }
        if( oldFof.getFirstReceived() > newFof.getFirstReceived() ) {
            oldFof.setFirstReceived(newFof.getFirstReceived()); doUpdate = true;
        }
        if( oldFof.getLastReceived() < newFof.getLastReceived() ) {
            oldFof.setLastReceived(newFof.getLastReceived()); doUpdate = true;
        }
        if( oldFof.getLastUploaded() < newFof.getLastUploaded() ) {
            oldFof.setLastUploaded(newFof.getLastUploaded()); doUpdate = true;
        }
        if( oldFof.getLastDownloaded() < newFof.getLastDownloaded() ) {
            oldFof.setLastDownloaded(newFof.getLastDownloaded()); doUpdate = true;
        }
        if( oldFof.getRequestLastReceived() < newFof.getRequestLastReceived() ) {
            oldFof.setRequestLastReceived(newFof.getRequestLastReceived()); doUpdate = true;
        }
        if( oldFof.getRequestLastSent() < newFof.getRequestLastSent() ) {
            oldFof.setRequestLastSent(newFof.getRequestLastSent()); doUpdate = true;
        }
        if( oldFof.getRequestsReceivedCount() < newFof.getRequestsReceivedCount() ) {
            oldFof.setRequestsReceivedCount(newFof.getRequestsReceivedCount()); doUpdate = true;
        }
        if( oldFof.getRequestsSentCount() < newFof.getRequestsSentCount() ) {
            oldFof.setRequestsSentCount(newFof.getRequestsSentCount()); doUpdate = true;
        }

        for(final Iterator<FrostFileListFileObjectOwner> i=newFof.getFrostFileListFileObjectOwnerIterator(); i.hasNext(); ) {

            final FrostFileListFileObjectOwner obNew = i.next();

            // check if we have an owner object for this sharer
            FrostFileListFileObjectOwner obOld = null;
            for(final Iterator<FrostFileListFileObjectOwner> j=oldFof.getFrostFileListFileObjectOwnerIterator(); j.hasNext(); ) {
                final FrostFileListFileObjectOwner o = j.next();
                if( o.getOwner().equals(obNew.getOwner()) ) {
                    obOld = o;
                    break;
                }
            }

            if( obOld == null ) {
                // add new
                oldFof.addFrostFileListFileObjectOwner(obNew);
                addFileListFileOwnerToIndices(obNew);
                doUpdate = true;
            } else {
                // update existing
                if( obOld.getLastReceived() < obNew.getLastReceived() ) {

                    maybeUpdateFileListInfoInIndex(obOld.getName(), obNew.getName(), obOld, storageRoot.getFileNameIndex());
                    obOld.setName(obNew.getName());

                    maybeUpdateFileListInfoInIndex(obOld.getComment(), obNew.getComment(), obOld, storageRoot.getFileCommentIndex());
                    obOld.setComment(obNew.getComment());

                    maybeUpdateFileListInfoInIndex(obOld.getKeywords(), obNew.getKeywords(), obOld, storageRoot.getFileKeywordIndex());
                    obOld.setKeywords(obNew.getKeywords());

                    obOld.setLastReceived(obNew.getLastReceived());
                    obOld.setLastUploaded(obNew.getLastUploaded());
                    obOld.setRating(obNew.getRating());
                    obOld.setKey(obNew.getKey());

                    obOld.modify();

                    doUpdate = true;
                }
            }
        }

        if( doUpdate ) {
            oldFof.modify();
        }

        return doUpdate;
    }

    private void maybeUpdateFileListInfoInIndex(
            final String oldValue,
            final String newValue,
            final FrostFileListFileObjectOwner o,
            final Index<PerstFileListIndexEntry> ix)
    {
        // remove current value from index of needed, add new value to index if needed
        if( oldValue != null ) {
            if( newValue != null ) {
                if( oldValue.toLowerCase().equals(newValue.toLowerCase()) ) {
                    // value not changed, ignore index change
                    return;
                }
                // we have to add this value to the index
                maybeAddFileListFileInfoToIndex(newValue, o, ix);
            }
            // we have to remove the old value from index
            maybeRemoveFileListFileInfoFromIndex(oldValue, o, ix);
        }
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
    }
}
