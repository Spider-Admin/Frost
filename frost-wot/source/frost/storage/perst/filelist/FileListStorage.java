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
            FrostFileListFileObjectOwner o = flf.getFrostFileListFileObjectOwnerList().get(0);
            // maybe the owner already shares other files
            PerstIdentitiesFiles pif = storageRoot.getIdentitiesFiles().get(o.getOwner());
            if( pif == null ) {
                pif = new PerstIdentitiesFiles(o.getOwner(), getStorage());
                storageRoot.getIdentitiesFiles().put(o.getOwner(), pif);
            }
            pif.addFileToIdentity(o);
        } else {
            // update existing
            pflf.updateFromOtherFileListFile(flf);
        }
        if( doCommit ) {
            commitStore();
        }
        return true;
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
            if( fof.getFrostFileListFileObjectOwnerList().size() == 0 && fof.getKey() == null ) {
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
    public boolean updateFrostFileListFileObjectAfterRequestReceived(String sha, long requestLastReceived) {

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
    public boolean updateFrostFileListFileObjectAfterDownload(String sha, long lastDownloaded) {

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
    public void retrieveFiles(
            FileListCallback callback,
            List<String> names,
            List<String> comments,
            List<String> keywords,
            List<String> owners) 
    {
        for( FrostFileListFileObject fof : storageRoot.getFileListFileObjects() ) {
            if( (names == null || names.size() == 0) 
                    && (comments == null || comments.size() == 0 ) 
                    && (keywords == null || keywords.size() == 0)
                    && (owners == null || owners.size() == 0) )
            {
                // find all
                if(callback.fileRetrieved(fof)) return;
                continue;
            }
            if( fof.getFrostFileListFileObjectOwnerList() == null ) {
                continue;
            }
            INNER_LOOP:
            for( FrostFileListFileObjectOwner o : fof.getFrostFileListFileObjectOwnerList() ) {
                if( names != null && names.size() > 0 ) {
                    for(String name : names) {
                        if( o.getName() != null && o.getName().toLowerCase().indexOf(name) > -1 ) {
                            if(callback.fileRetrieved(fof)) return;
                            break INNER_LOOP;
                        }
                    }
                }
                if( comments != null && comments.size() > 0 ) {
                    for(String comment : comments) {
                        if( o.getComment() != null && o.getComment().toLowerCase().indexOf(comment) > -1 ) {
                            if(callback.fileRetrieved(fof)) return;
                            break INNER_LOOP;
                        }
                    }
                }
                if( keywords != null && keywords.size() > 0 ) {
                    for(String keyword : keywords) {
                        if( o.getKeywords() != null && o.getKeywords().toLowerCase().indexOf(keyword) > -1 ) {
                            if(callback.fileRetrieved(fof)) return;
                            break INNER_LOOP;
                        }
                    }
                }
                if( owners != null && owners.size() > 0 ) {
                    for(String owner : owners) {
                        if( o.getOwner() != null && o.getOwner().toLowerCase().indexOf(owner) > -1 ) {
                            if(callback.fileRetrieved(fof)) return;
                            break INNER_LOOP;
                        }
                    }
                }
            }
        }
/*        
SQL='SELECT DISTINCT refkey FROM FILEOWNERLIST WHERE LOWER(name) LIKE ? OR LOWER(name) LIKE ? OR LOWER(comment) LIKE ? OR LOWER(comment) LIKE ? OR LOWER(keywords) LIKE ? OR LOWER(keywords) LIKE ?'        
*/        
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//        // select only files that have an owner
//        String sql = "SELECT DISTINCT refkey FROM FILEOWNERLIST";
//
//        List<String> values = new LinkedList<String>();
//
//        if( (names != null && names.size() > 0) 
//         || (comments != null && comments.size() > 0 ) 
//         || (keywords != null && keywords.size() > 0)
//         || (owners != null && owners.size() > 0) )
//        {
//            sql += " WHERE";
//
//            if( names != null && names.size() > 0 ) {
//                for(String name : names) {
//                    sql += " LOWER(name) LIKE ? OR";
//                    values.add(name);
//                }
//            }
//            if( comments != null && comments.size() > 0 ) {
//                for(String comment : comments) {
//                    sql += " LOWER(comment) LIKE ? OR";
//                    values.add(comment);
//                }
//            }
//            if( keywords != null && keywords.size() > 0 ) {
//                for(String keyword : keywords) {
//                    sql += " LOWER(keywords) LIKE ? OR";
//                    values.add(keyword);
//                }
//            }
//            if( owners != null && owners.size() > 0 ) {
//                for(String owner : owners) {
//                    sql += " LOWER(owner) LIKE ? OR";
//                    values.add(owner);
//                }
//            }
//            // remove last OR
//            sql = sql.substring(0, sql.length() - 3);
//        }
//        
//        PreparedStatement ps = db.prepareStatement(sql);
//
//        int ix = 1;
//        for( String value : values ) {
//            ps.setString(ix++,"%"+value+"%");
//        }
//        
//        ResultSet rs = ps.executeQuery();
//        while( rs.next() ) {
//            long refkey = rs.getLong(1);
//            
//            FrostFileListFileObject fo = getFrostFileListFileObject(refkey);
//            if( fo == null ) {
//                // db corrupted, no file for this owner refkey, should not be possible due to constraints
//                continue;
//            }
//            List<FrostFileListFileObjectOwner> obs = getFrostFileListFileObjectOwnerList(refkey);
//            fo.getFrostFileListFileObjectOwnerList().addAll(obs);
//            
//            boolean shouldStop = callback.fileRetrieved(fo); // pass to callback
//            if( shouldStop ) {
//                break;
//            }
//        }
//        rs.close();
//        ps.close();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        rememberSharedFileDownloaded = Core.frostSettings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED);
    }
}
