/*
  FrostFilesStorage.java / Frost
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
package frost.storage.perst;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.garret.perst.*;

import frost.boards.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.fileTransfer.upload.*;
import frost.gui.*;
import frost.storage.*;
import frost.util.gui.translation.*;

/**
 * A Storage for FrostDownloadFiles, FrostUploadFiles and SharedFiles.
 * Loaded during startup, and saved during shutdown (or during autosave).
 */
public class FrostFilesStorage implements Savable {

    private static final Logger logger = Logger.getLogger(FrostFilesStorage.class.getName());

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB
    
    private Storage storage = null;
    private FrostFilesStorageRoot storageRoot = null;
    
    private static FrostFilesStorage instance = new FrostFilesStorage();

    protected FrostFilesStorage() {}
    
    public static FrostFilesStorage inst() {
        return instance;
    }
    
    private Storage getStorage() {
        return storage;
    }
    
    public boolean initStorage() {
        String databaseFilePath = "store/filesStore.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.string.encoding", "UTF-8"); // use UTF-8 to store strings
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // remove() during iteration (for cleanup)
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (FrostFilesStorageRoot)storage.getRoot();
        if (storageRoot == null) { 
            // Storage was not initialized yet
            storageRoot = new FrostFilesStorageRoot();
            
            storageRoot.downloadFiles = storage.createScalableList();
            storageRoot.uploadFiles = storage.createScalableList();
            storageRoot.sharedFiles = storage.createScalableList();
            storageRoot.newUploadFiles = storage.createScalableList();

            storageRoot.hiddenBoardNames = storage.createScalableList();
            storageRoot.knownBoards = storage.createIndex(String.class, true);

            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        } else if( storageRoot.hiddenBoardNames == null ) {
            // add new root items
            storageRoot.hiddenBoardNames = storage.createScalableList();
            storageRoot.knownBoards = storage.createIndex(String.class, true);
            storageRoot.modify();
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
        System.out.println("INFO: FrostFilesStorage closed.");
    }
    
    // only used for migration
    public void savePerstFrostDownloadFiles(List<PerstFrostDownloadItem> downloadFiles) {
        for(Iterator<PerstFrostDownloadItem> i=downloadFiles.iterator(); i.hasNext(); ) {
            PerstFrostDownloadItem pi = i.next();
            pi.makePersistent(storage);
            storageRoot.downloadFiles.add(pi);
        }
        storageRoot.downloadFiles.modify();
        commitStore();
    }

    /**
     * Removes all items from the given List and deallocates each item from Storage.
     * @param plst  IPersistentList of persistent items
     */
    private void removeAllFromStorage(IPersistentList<? extends Persistent> plst) {
        for(Iterator<? extends Persistent> i=plst.iterator(); i.hasNext(); ) {
            Persistent pi = (Persistent)i.next();
            i.remove(); // remove from List
            pi.deallocate(); // remove from Storage
        }
        plst.clear(); // paranoia
        commitStore();
    }

    public void saveDownloadFiles(List<FrostDownloadItem> downloadFiles) {
        removeAllFromStorage(storageRoot.downloadFiles); // delete all old items
        for( FrostDownloadItem dlItem : downloadFiles ) {
            if( dlItem.isExternal() ) {
                continue;
            }
            PerstFrostDownloadItem pi = new PerstFrostDownloadItem(dlItem);
            storageRoot.downloadFiles.add(pi);
        }
        commitStore();
    }
    
    public List<FrostDownloadItem> loadDownloadFiles() {
        LinkedList<FrostDownloadItem> downloadItems = new LinkedList<FrostDownloadItem>();
        for( PerstFrostDownloadItem pi : storageRoot.downloadFiles ) {
            FrostDownloadItem dlItem = pi.toFrostDownloadItem(logger);
            if( dlItem != null ) {
                downloadItems.add(dlItem);
            }
        }
        return downloadItems;
    }
    
    // only used for migration
    public void savePerstFrostUploadFiles(List<PerstFrostUploadItem> uploadFiles) {
        for( PerstFrostUploadItem pi : uploadFiles ) {
            storageRoot.uploadFiles.add(pi);
        }
        commitStore();
    }

    public void saveUploadFiles(List<FrostUploadItem> uploadFiles) {
        removeAllFromStorage(storageRoot.uploadFiles); // delete all old items
        for( FrostUploadItem ulItem : uploadFiles ) {
            if( ulItem.isExternal() ) {
                continue;
            }
            PerstFrostUploadItem pi = new PerstFrostUploadItem(ulItem);
            storageRoot.uploadFiles.add(pi);
        }
        commitStore();
    }
    
    public List<FrostUploadItem> loadUploadFiles(List<FrostSharedFileItem> sharedFiles) {

        LinkedList<FrostUploadItem> uploadItems = new LinkedList<FrostUploadItem>();
        Language language = Language.getInstance();

        for( PerstFrostUploadItem pi : storageRoot.uploadFiles ) {
            FrostUploadItem ulItem = pi.toFrostUploadItem(sharedFiles, logger, language);
            if( ulItem != null ) {
                uploadItems.add(ulItem);
            }
        }
        return uploadItems;
    }
    
    // only used for migration
    public void savePerstFrostSharedFiles(List<PerstFrostSharedFileItem> sfFiles) {
        for(Iterator<PerstFrostSharedFileItem> i=sfFiles.iterator(); i.hasNext(); ) {
            PerstFrostSharedFileItem pi = i.next();
            pi.makePersistent(storage);
            storageRoot.sharedFiles.add(pi);
        }
        storageRoot.sharedFiles.modify();
        commitStore();
    }

    public void saveSharedFiles(List<FrostSharedFileItem> sfFiles) {
        removeAllFromStorage(storageRoot.sharedFiles);
        for(FrostSharedFileItem sfItem : sfFiles ) {
            PerstFrostSharedFileItem pi = new PerstFrostSharedFileItem(sfItem);
            storageRoot.sharedFiles.add(pi);
        }
        commitStore();
    }

    public List<FrostSharedFileItem> loadSharedFiles() {
        LinkedList<FrostSharedFileItem> sfItems = new LinkedList<FrostSharedFileItem>();
        Language language = Language.getInstance();
        for( PerstFrostSharedFileItem pi : storageRoot.sharedFiles ) {
            FrostSharedFileItem sfItem = pi.toFrostSharedFileItem(logger, language);
            if( sfItem != null ) {
                sfItems.add(sfItem);
            }
        }
        return sfItems;
    }
    
    public void saveNewUploadFiles(List<NewUploadFile> newUploadFiles) {

        removeAllFromStorage(storageRoot.newUploadFiles);

        for(Iterator<NewUploadFile> i=newUploadFiles.iterator(); i.hasNext(); ) {
            NewUploadFile nuf = i.next();
            nuf.makePersistent(storage);
            nuf.modify(); // for already persistent items
            
            storageRoot.newUploadFiles.add(nuf);
        }
        commitStore();
    }

    public LinkedList<NewUploadFile> loadNewUploadFiles() {

        LinkedList<NewUploadFile> newUploadFiles = new LinkedList<NewUploadFile>();

        for( NewUploadFile nuf : storageRoot.newUploadFiles ) {
            File f = new File(nuf.getFilePath());
            if (!f.isFile()) {
                logger.warning("File ("+nuf.getFilePath()+") is missing. File removed.");
                continue;
            }
            newUploadFiles.add(nuf);
        }
        return newUploadFiles;
    }
    
    /**
     * Load all hidden board names.
     */
    public HashSet<String> loadHiddenBoardNames() {
        HashSet<String> result = new HashSet<String>();
        for( PerstHiddenBoardName hbn : storageRoot.hiddenBoardNames ) {
            result.add(hbn.getHiddenBoardName());
        }
        return result;
    }

    /**
     * Clear table and save all hidden board names.
     */
    public void saveHiddenBoardNames(HashSet<String> names) {
        removeAllFromStorage(storageRoot.hiddenBoardNames);
        for( String s : names ) {
            PerstHiddenBoardName h = new PerstHiddenBoardName(s);
            storageRoot.hiddenBoardNames.add(h);
        }
        commitStore();
    }
    
    private String buildBoardIndex(Board b) {
        StringBuilder sb = new StringBuilder();
        sb.append(b.getNameLowerCase());
        if( b.getPublicKey() != null ) {
            sb.append(b.getPublicKey());
        }
        if( b.getPrivateKey() != null ) {
            sb.append(b.getPrivateKey());
        }
        return sb.toString();
    }
    
    /**
     * @return  List of KnownBoard
     */
    public List<KnownBoard> getKnownBoards() {
        List<KnownBoard> lst = new ArrayList<KnownBoard>();
        for(PerstKnownBoard pkb : storageRoot.knownBoards) {
            KnownBoard kb = new KnownBoard(pkb.getBoardName(), pkb.getPublicKey(), pkb.getPrivateKey(), pkb.getDescription());
            lst.add(kb);
        }
        return lst;
    }
    
    public synchronized boolean deleteKnownBoard(Board b) {
        String newIx = buildBoardIndex(b);
        PerstKnownBoard pkb = storageRoot.knownBoards.get(newIx);
        if( pkb != null ) {
            storageRoot.knownBoards.remove(newIx, pkb);
            pkb.deallocate();
            commitStore();
            return true;
        }
        return false;
    }
    
    /**
     * Called with a list of Board, should add all boards that are not contained already
     * @param lst  List of Board
     * @return  number of added boards
     */
    public synchronized int addNewKnownBoards( List<? extends Board> lst ) {
        if( lst == null || lst.size() == 0 ) {
            return 0;
        }
        int added = 0;
        for( Board b : lst ) {
            String newIx = buildBoardIndex(b);
            PerstKnownBoard pkb = new PerstKnownBoard(b.getName(), b.getPublicKey(), b.getPrivateKey(), b.getDescription());
            if( storageRoot.knownBoards.put(newIx, pkb) ) {
                added++;
            }
        }
        commitStore();
        return added;
    }
}
