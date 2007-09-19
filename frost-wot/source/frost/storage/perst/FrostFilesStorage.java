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
public class FrostFilesStorage extends AbstractFrostStorage implements Savable {

    private static final Logger logger = Logger.getLogger(FrostFilesStorage.class.getName());

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB

    private FrostFilesStorageRoot storageRoot = null;

    private static FrostFilesStorage instance = new FrostFilesStorage();

    protected FrostFilesStorage() {
        super();
    }

    public static FrostFilesStorage inst() {
        return instance;
    }

    public boolean initStorage() {
        final int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes
        return initStorage(pagePoolSize);
    }

    public boolean initStorage(final int pagePoolSize) {
        final String databaseFilePath = "store/filesStore.dbs"; // path to the database file

        open(databaseFilePath, pagePoolSize, true, true, false);

        storageRoot = (FrostFilesStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new FrostFilesStorageRoot();

            storageRoot.downloadFiles = getStorage().createScalableList();
            storageRoot.uploadFiles = getStorage().createScalableList();
            storageRoot.sharedFiles = getStorage().createScalableList();
            storageRoot.newUploadFiles = getStorage().createScalableList();

            storageRoot.hiddenBoardNames = getStorage().createScalableList();
            storageRoot.knownBoards = getStorage().createIndex(String.class, true);

            getStorage().setRoot(storageRoot);
            commitStore(); // commit transaction
        } else if( storageRoot.hiddenBoardNames == null ) {
            // add new root items
            storageRoot.hiddenBoardNames = getStorage().createScalableList();
            storageRoot.knownBoards = getStorage().createIndex(String.class, true);
            storageRoot.modify();
            commitStore(); // commit transaction
        }
        return true;
    }

    public void save() throws StorageException {
        close();
        storageRoot = null;
        System.out.println("INFO: FrostFilesStorage closed.");
    }

    // only used for migration
    public void savePerstFrostDownloadFiles(final List<PerstFrostDownloadItem> downloadFiles) {
        for( final PerstFrostDownloadItem pi : downloadFiles ) {
            pi.makePersistent(getStorage());
            storageRoot.downloadFiles.add(pi);
        }
        storageRoot.downloadFiles.modify();
        commitStore();
    }

    /**
     * Removes all items from the given List and deallocates each item from Storage.
     * @param plst  IPersistentList of persistent items
     */
    private void removeAllFromStorage(final IPersistentList<? extends Persistent> plst) {
        for(final Iterator<? extends Persistent> i=plst.iterator(); i.hasNext(); ) {
            final Persistent pi = i.next();
            i.remove(); // remove from List
            pi.deallocate(); // remove from Storage
        }
        plst.clear(); // paranoia
        commitStore();
    }

    public void saveDownloadFiles(final List<FrostDownloadItem> downloadFiles) {
        removeAllFromStorage(storageRoot.downloadFiles); // delete all old items
        for( final FrostDownloadItem dlItem : downloadFiles ) {
            if( dlItem.isExternal() ) {
                continue;
            }
            final PerstFrostDownloadItem pi = new PerstFrostDownloadItem(dlItem);
            storageRoot.downloadFiles.add(pi);
        }
        commitStore();
    }

    public List<FrostDownloadItem> loadDownloadFiles() {
        final LinkedList<FrostDownloadItem> downloadItems = new LinkedList<FrostDownloadItem>();
        for( final PerstFrostDownloadItem pi : storageRoot.downloadFiles ) {
            final FrostDownloadItem dlItem = pi.toFrostDownloadItem(logger);
            if( dlItem != null ) {
                downloadItems.add(dlItem);
            }
        }
        return downloadItems;
    }

    // only used for migration
    public void savePerstFrostUploadFiles(final List<PerstFrostUploadItem> uploadFiles) {
        for( final PerstFrostUploadItem pi : uploadFiles ) {
            storageRoot.uploadFiles.add(pi);
        }
        commitStore();
    }

    public void saveUploadFiles(final List<FrostUploadItem> uploadFiles) {
        removeAllFromStorage(storageRoot.uploadFiles); // delete all old items
        for( final FrostUploadItem ulItem : uploadFiles ) {
            if( ulItem.isExternal() ) {
                continue;
            }
            final PerstFrostUploadItem pi = new PerstFrostUploadItem(ulItem);
            storageRoot.uploadFiles.add(pi);
        }
        commitStore();
    }

    public List<FrostUploadItem> loadUploadFiles(final List<FrostSharedFileItem> sharedFiles) {

        final LinkedList<FrostUploadItem> uploadItems = new LinkedList<FrostUploadItem>();
        final Language language = Language.getInstance();

        for( final PerstFrostUploadItem pi : storageRoot.uploadFiles ) {
            final FrostUploadItem ulItem = pi.toFrostUploadItem(sharedFiles, logger, language);
            if( ulItem != null ) {
                uploadItems.add(ulItem);
            }
        }
        return uploadItems;
    }

    // only used for migration
    public void savePerstFrostSharedFiles(final List<PerstFrostSharedFileItem> sfFiles) {
        for( final PerstFrostSharedFileItem pi : sfFiles ) {
            pi.makePersistent(getStorage());
            storageRoot.sharedFiles.add(pi);
        }
        storageRoot.sharedFiles.modify();
        commitStore();
    }

    public void saveSharedFiles(final List<FrostSharedFileItem> sfFiles) {
        removeAllFromStorage(storageRoot.sharedFiles);
        for(final FrostSharedFileItem sfItem : sfFiles ) {
            final PerstFrostSharedFileItem pi = new PerstFrostSharedFileItem(sfItem);
            storageRoot.sharedFiles.add(pi);
        }
        commitStore();
    }

    public List<FrostSharedFileItem> loadSharedFiles() {
        final LinkedList<FrostSharedFileItem> sfItems = new LinkedList<FrostSharedFileItem>();
        final Language language = Language.getInstance();
        for( final PerstFrostSharedFileItem pi : storageRoot.sharedFiles ) {
            final FrostSharedFileItem sfItem = pi.toFrostSharedFileItem(logger, language);
            if( sfItem != null ) {
                sfItems.add(sfItem);
            }
        }
        return sfItems;
    }

    public void saveNewUploadFiles(final List<NewUploadFile> newUploadFiles) {

        removeAllFromStorage(storageRoot.newUploadFiles);

        for( final NewUploadFile nuf : newUploadFiles ) {
            nuf.makePersistent(getStorage());
            nuf.modify(); // for already persistent items

            storageRoot.newUploadFiles.add(nuf);
        }
        commitStore();
    }

    public LinkedList<NewUploadFile> loadNewUploadFiles() {

        final LinkedList<NewUploadFile> newUploadFiles = new LinkedList<NewUploadFile>();

        for( final NewUploadFile nuf : storageRoot.newUploadFiles ) {
            final File f = new File(nuf.getFilePath());
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
        final HashSet<String> result = new HashSet<String>();
        for( final PerstHiddenBoardName hbn : storageRoot.hiddenBoardNames ) {
            result.add(hbn.getHiddenBoardName());
        }
        return result;
    }

    /**
     * Clear table and save all hidden board names.
     */
    public void saveHiddenBoardNames(final HashSet<String> names) {
        removeAllFromStorage(storageRoot.hiddenBoardNames);
        for( final String s : names ) {
            final PerstHiddenBoardName h = new PerstHiddenBoardName(s);
            storageRoot.hiddenBoardNames.add(h);
        }
        commitStore();
    }

    private String buildBoardIndex(final Board b) {
        final StringBuilder sb = new StringBuilder();
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
        final List<KnownBoard> lst = new ArrayList<KnownBoard>();
        for(final PerstKnownBoard pkb : storageRoot.knownBoards) {
            final KnownBoard kb = new KnownBoard(pkb.getBoardName(), pkb.getPublicKey(), pkb.getPrivateKey(), pkb.getDescription());
            lst.add(kb);
        }
        return lst;
    }

    public synchronized boolean deleteKnownBoard(final Board b) {
        final String newIx = buildBoardIndex(b);
        final PerstKnownBoard pkb = storageRoot.knownBoards.get(newIx);
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
    public synchronized int addNewKnownBoards( final List<? extends Board> lst ) {
        if( lst == null || lst.size() == 0 ) {
            return 0;
        }
        int added = 0;
        for( final Board b : lst ) {
            final String newIx = buildBoardIndex(b);
            final PerstKnownBoard pkb = new PerstKnownBoard(b.getName(), b.getPublicKey(), b.getPrivateKey(), b.getDescription());
            if( storageRoot.knownBoards.put(newIx, pkb) ) {
                added++;
            }
        }
        commitStore();
        return added;
    }
}
