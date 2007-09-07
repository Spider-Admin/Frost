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

public class FileListStorage implements Savable, PropertyChangeListener {

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB
    
    private Storage storage = null;
    private FileListStorageRoot storageRoot = null;
    
    private static FileListStorage instance = new FileListStorage();

    private boolean rememberSharedFileDownloaded;

    protected FileListStorage() {}
    
    public static FileListStorage inst() {
        return instance;
    }
    
    private Storage getStorage() {
        return storage;
    }
    
    public boolean initStorage() {
        
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED, this);
        
        String databaseFilePath = "store/filelist.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // modify() during iteration
        storage.setProperty("perst.string.encoding", "UTF-8");
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (FileListStorageRoot)storage.getRoot();
        if (storageRoot == null) { 
            // Storage was not initialized yet
            storageRoot = new FileListStorageRoot(storage);
            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public synchronized void commitStore() {
        if( getStorage() == null ) {
            return;
        }
        getStorage().commit();
    }

    public void save() throws StorageException {

        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: FileListStorage closed.");
    }
    
    public IPersistentList createList() {
        return storage.createScalableList();
    }

    public synchronized boolean insertOrUpdateFileListFileObject(FrostFileListFileObject flf) {
        return insertOrUpdateFileListFileObject(flf, true);
    }

    public synchronized boolean insertOrUpdateFileListFileObject(FrostFileListFileObject flf, boolean doCommit) {
        // check for dups and update them!
        FrostFileListFileObject pflf = storageRoot.getFileListFileObjects().get(flf.getSha());
        if( pflf == null ) {
            // insert new
            storageRoot.getFileListFileObjects().put(flf.getSha(), flf);

            for( Iterator<FrostFileListFileObjectOwner> i = flf.getFrostFileListFileObjectOwnerIterator(); i.hasNext(); ) {
                FrostFileListFileObjectOwner o = i.next();
                addFileListFileOwnerToIndices(o);
            }
        } else {
            // update existing
            updateFileListFileFromOtherFileListFile(pflf, flf);
        }
        if( doCommit ) {
            commitStore();
        }
        return true;
    }
    
    /**
     * Adds a new FrostFileListFileObjectOwner to the indices.
     */
    private void addFileListFileOwnerToIndices(FrostFileListFileObjectOwner o) {
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
    
    private void maybeAddFileListFileInfoToIndex(String lName, FrostFileListFileObjectOwner o, Index<PerstFileListIndexEntry> ix) {
        if( lName == null || lName.length() == 0 ) {
            return;
        }
        lName = lName.toLowerCase();
        PerstFileListIndexEntry ie = ix.get(lName);
        if( ie == null ) {
            ie = new PerstFileListIndexEntry(storage);
            ix.put(lName, ie);
        }
        ie.getFileOwnersWithText().add(o);
    }
    
    public FrostFileListFileObject getFileBySha(String sha) {
        return storageRoot.getFileListFileObjects().get(sha);
    }
    
    public int getFileCount() {
        return storageRoot.getFileListFileObjects().size();
    }

    public int getFileCount(String idUniqueName) {
        PerstIdentitiesFiles pif = storageRoot.getIdentitiesFiles().get(idUniqueName);
        if( pif != null ) {
            return pif.getFilesFromIdentity().size();
        } else {
            return 0;
        }
    }
    
    public int getSharerCount() {
        return storageRoot.getIdentitiesFiles().size();
    }
    
    public long getFileSizes() {
        long sizes = 0;
        for( FrostFileListFileObject fo : storageRoot.getFileListFileObjects() ) {
            sizes += fo.getSize();
        }
        return sizes;
    }
    
    private void maybeRemoveFileListFileInfoFromIndex(
            String lName, 
            FrostFileListFileObjectOwner o, 
            Index<PerstFileListIndexEntry> ix) 
    {
        if( lName != null && lName.length() > 0 ) {
            PerstFileListIndexEntry ie = ix.get(lName.toLowerCase());
            if( ie != null ) {
//                System.out.println("ix-remove: "+o.getOid());
                ie.getFileOwnersWithText().remove(o);
            }
        }
    }

    /**
     * Remove owners that were not seen for more than MINIMUM_DAYS_OLD days and have no CHK key set.
     */
    public int cleanupFileListFileOwners(int maxDaysOld) {

        int count = 0;
        long minVal = System.currentTimeMillis() - ((long)maxDaysOld * 24L * 60L * 60L * 1000L);
        
        for(PerstIdentitiesFiles pif : storageRoot.getIdentitiesFiles()) {
            for(Iterator<FrostFileListFileObjectOwner> i = pif.getFilesFromIdentity().iterator(); i.hasNext(); ) {
                FrostFileListFileObjectOwner o = i.next();
                if( o.getLastReceived() < minVal && o.getKey() == null ) {
                    // remove this owner file info from file list object
                    FrostFileListFileObject fof = o.getFileListFileObject();
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
                    count++;
                }
            }
            if( pif.getFilesFromIdentity().size() == 0 ) {
                // no more files for this identity, remove
                storageRoot.getIdentitiesFiles().remove(pif.getUniqueName());
                pif.deallocate();
            }
        }
        commitStore();
        return count;
    }

    /**
     * Remove files that have no owner and no CHK key. 
     */
    public int cleanupFileListFiles() {
        int count = 0;
        for(Iterator<FrostFileListFileObject> i=storageRoot.getFileListFileObjects().iterator(); i.hasNext(); ) {
            FrostFileListFileObject fof = i.next();
            if( fof.getFrostFileListFileObjectOwnerListSize() == 0 && fof.getKey() == null ) {
                i.remove();
                fof.deallocate();
                count++;
            }
        }
        commitStore();
        return count;
    }

    /**
     * Reset the lastdownloaded column for all file entries.
     */
    public synchronized void resetLastDownloaded() {
        
        for( FrostFileListFileObject fof : storageRoot.getFileListFileObjects() ) {
            fof.setLastDownloaded(0);
            fof.modify();
        }
        commitStore();
    }

    /**
     * Update the item with SHA, set requestlastsent and requestssentcount.
     * Does NOT commit!
     */
    public synchronized boolean updateFrostFileListFileObjectAfterRequestSent(String sha, long requestLastSent) {

        FrostFileListFileObject oldSfo = getFileBySha(sha);
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
    public synchronized boolean updateFrostFileListFileObjectAfterRequestReceived(String sha, long requestLastReceived) {

        FrostFileListFileObject oldSfo = getFileBySha(sha);
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
    public synchronized boolean updateFrostFileListFileObjectAfterDownload(String sha, long lastDownloaded) {

        if( !rememberSharedFileDownloaded ) {
            return true;
        }

        FrostFileListFileObject oldSfo = getFileBySha(sha);
        if( oldSfo == null ) {
            return false;
        }
        
        oldSfo.setLastDownloaded(lastDownloaded);
        
        oldSfo.modify();
        
        commitStore();
        
        return true;
    }
    
    /**
     * Retrieves a list of FrostSharedFileOjects.
     */
    public synchronized void retrieveFiles(
            final FileListCallback callback,
            final List<String> names,
            final List<String> comments,
            final List<String> keywords,
            final List<String> owners,
            String[] extensions) 
    {
        System.out.println("Starting file search...");
        long t = System.currentTimeMillis();
        
        boolean searchForNames = true;
        boolean searchForComments = true;
        boolean searchForKeywords = true;
        boolean searchForOwners = true;
        boolean searchForExtensions = true;
        
        if( names == null    || names.size() == 0 )    { searchForNames = false; }
        if( comments == null || comments.size() == 0 ) { searchForComments = false; }
        if( keywords == null || keywords.size() == 0 ) { searchForKeywords = false; }
        if( owners == null   || owners.size() == 0 )   { searchForOwners = false; }
        if( extensions == null   || extensions.length == 0 )   { searchForExtensions = false; }
        if( !searchForNames && !searchForComments && ! searchForKeywords && !searchForOwners && !searchForExtensions) {
            // find ALL files
            for(FrostFileListFileObject o : storageRoot.getFileListFileObjects()) {
                if(callback.fileRetrieved(o)) return;
            }
            return;
        }
        
        if( !searchForExtensions ) {
            extensions = null;
        }
 
        try {
            HashSet<Integer> ownerOids = new HashSet<Integer>();
            
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
            
            HashSet<Integer> fileOids = new HashSet<Integer>();
            for( Integer i : ownerOids ) {
//                System.out.println("search-oid: "+i);
                FrostFileListFileObjectOwner o = (FrostFileListFileObjectOwner)storage.getObjectByOID(i);
                int oid = o.getFileListFileObject().getOid();
                fileOids.add(oid);
            }

            for( Integer i : fileOids ) {
                FrostFileListFileObject o = (FrostFileListFileObject)storage.getObjectByOID(i);
                if( o != null ) {
                    if(callback.fileRetrieved(o)) return;
                }
            }
        } finally {
            System.out.println("Finished file search, duration="+(System.currentTimeMillis() - t));
        }
    }
    
    private void searchForFiles(
            HashSet<Integer> oids, 
            List<String> searchStrings,
            String[] extensions, // only used for name search
            Index<PerstFileListIndexEntry> ix) 
    {
        for(Map.Entry<Object,PerstFileListIndexEntry> entry : ix.entryIterator() ) {
            String key = (String)entry.getKey();
            if( searchStrings != null ) {
                for(String searchString : searchStrings) {
                    if( key.indexOf(searchString) > -1 ) {
                        // add all owner oids
                        Iterator<FrostFileListFileObjectOwner> i = entry.getValue().getFileOwnersWithText().iterator();
                        while(i.hasNext()) {
                            int oid = ((PersistentIterator)i).nextOid();
                            oids.add(oid);
                        }
                    }
                }
            }
            if( extensions != null ) {
                for(int x=0; x < extensions.length; x++) {
                    String extension = extensions[x];
                    if( key.endsWith(extension) ) {
                        // add all owner oids
                        Iterator<FrostFileListFileObjectOwner> i = entry.getValue().getFileOwnersWithText().iterator();
                        while(i.hasNext()) {
                            int oid = ((PersistentIterator)i).nextOid();
                            oids.add(oid);
                        }
                    }
                }
            }
        }
    }

    public synchronized boolean updateFileListFileFromOtherFileListFile(FrostFileListFileObject oldFof, FrostFileListFileObject newFof) {
        // file is already in FILELIST table, maybe add new FILEOWNER and update fields
        // maybe update oldSfo
        boolean doUpdate = false;
        if( oldFof.getKey() == null && newFof.getKey() != null ) {
            oldFof.setKey(newFof.getKey()); doUpdate = true;
        } else if( oldFof.getKey() != null && newFof.getKey() != null ) {
            // fix to replace 0.7 keys before 1010 on the fly
            if( FreenetKeys.isOld07ChkKey(oldFof.getKey()) && !FreenetKeys.isOld07ChkKey(newFof.getKey()) ) {
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
        
        for(Iterator<FrostFileListFileObjectOwner> i=newFof.getFrostFileListFileObjectOwnerIterator(); i.hasNext(); ) {
            
            FrostFileListFileObjectOwner obNew = i.next();
            
            // check if we have an owner object for this sharer
            FrostFileListFileObjectOwner obOld = null;
            for(Iterator<FrostFileListFileObjectOwner> j=oldFof.getFrostFileListFileObjectOwnerIterator(); j.hasNext(); ) {
                FrostFileListFileObjectOwner o = j.next();
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
            String oldValue, 
            String newValue, 
            FrostFileListFileObjectOwner o, 
            Index<PerstFileListIndexEntry> ix) 
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

    public void propertyChange(PropertyChangeEvent evt) {
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
    }
}
