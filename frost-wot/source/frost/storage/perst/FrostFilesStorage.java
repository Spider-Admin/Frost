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
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import org.garret.perst.*;

import frost.*;
import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.fileTransfer.upload.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.gui.*;
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

    protected FrostFilesStorage() {
    }
    
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

            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public synchronized void commitStore() {
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
        storage.commit();
    }

    public void saveDownloadFiles(List<FrostDownloadItem> downloadFiles) {

        // clear old items and list
        for(Iterator i=storageRoot.downloadFiles.iterator(); i.hasNext(); ) {
            Persistent pi = (Persistent)i.next();
            i.remove();
            pi.deallocate();
        }
        
        for(Iterator i=downloadFiles.iterator(); i.hasNext(); ) {
            
            FrostDownloadItem dlItem = (FrostDownloadItem)i.next();
            
            if( dlItem.isExternal() ) {
                continue;
            }
            
            PerstFrostDownloadItem pi = new PerstFrostDownloadItem();
            pi.fileName = dlItem.getFilename();
            pi.targetPath = dlItem.getTargetPath();
            pi.fileSize = dlItem.getFileSize();
            pi.key = dlItem.getKey();
            pi.enabled = (dlItem.isEnabled()==null?true:dlItem.isEnabled().booleanValue());
            pi.state = dlItem.getState();
            pi.downloadAddedTime = dlItem.getDownloadAddedTime();
            pi.downloadStartedTime = dlItem.getDownloadStartedTime();
            pi.downloadFinishedTime = dlItem.getDownloadFinishedTime();
            pi.retries = dlItem.getRetries();
            pi.lastDownloadStopTime = dlItem.getLastDownloadStopTime();
            pi.gqIdentifier = dlItem.getGqIdentifier();
            pi.fileListFileSha = (dlItem.getFileListFileObject()==null?null:dlItem.getFileListFileObject().getSha());

            pi.makePersistent(storage);
            pi.modify(); // for already persistent items
            
            storageRoot.downloadFiles.add(pi);
        }
        
        storageRoot.downloadFiles.modify();
        
        storage.commit();
    }
    
    public List<FrostDownloadItem> loadDownloadFiles() {

        LinkedList<FrostDownloadItem> downloadItems = new LinkedList<FrostDownloadItem>();

        for(Iterator i=storageRoot.downloadFiles.iterator(); i.hasNext(); ) {

            PerstFrostDownloadItem pi = (PerstFrostDownloadItem)i.next();

            String filename = pi.fileName;
            String targetPath = pi.targetPath;
            long size = pi.fileSize;
            String key = pi.key;
            boolean enabledownload = pi.enabled;
            int state = pi.state;
            long downloadAddedTime = pi.downloadAddedTime;
            long downloadStartedTime = pi.downloadStartedTime;
            long downloadFinishedTime = pi.downloadFinishedTime;
            int retries = pi.retries;
            long lastDownloadStopTime = pi.lastDownloadStopTime;
            String gqId = pi.gqIdentifier;
            String sharedFileSha = pi.fileListFileSha;
            
            FrostFileListFileObject sharedFileObject = null;
            if( sharedFileSha != null && sharedFileSha.length() > 0 ) {
                try {
                    sharedFileObject = AppLayerDatabase.getFileListDatabaseTable().retrieveFileBySha(sharedFileSha);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error retrieving FileListFile from database", e);
                }
                if( sharedFileObject == null && key == null ) {
                    // no fileobject and no key -> we can't continue to download this file
                    logger.warning("DownloadUpload items file list file object does not exist, and there is no key. " +
                                   "Removed from upload files: "+filename);
                }
            }

            FrostDownloadItem dlItem = new FrostDownloadItem(
                    filename,
                    targetPath,
                    (size<=0 ? -1 : size),
                    key,
                    Boolean.valueOf(enabledownload),
                    state,
                    downloadAddedTime,
                    downloadStartedTime,
                    downloadFinishedTime,
                    retries,
                    lastDownloadStopTime,
                    gqId);
            
            dlItem.setFileListFileObject(sharedFileObject);

            downloadItems.add(dlItem);
        }
        return downloadItems;
    }
    
    // only used for migration
    public void savePerstFrostUploadFiles(List<PerstFrostUploadItem> uploadFiles) {
        for(Iterator<PerstFrostUploadItem> i=uploadFiles.iterator(); i.hasNext(); ) {
            PerstFrostUploadItem pi = i.next();
            pi.makePersistent(storage);
            storageRoot.uploadFiles.add(pi);
        }
        storageRoot.uploadFiles.modify();
        storage.commit();
    }

    public void saveUploadFiles(List<FrostUploadItem> uploadFiles) {

        // clear old items and list
        for(Iterator i=storageRoot.uploadFiles.iterator(); i.hasNext(); ) {
            Persistent pi = (Persistent)i.next();
            i.remove();
            pi.deallocate();
        }

        for(Iterator i=uploadFiles.iterator(); i.hasNext(); ) {

            FrostUploadItem ulItem = (FrostUploadItem)i.next();
            
            if( ulItem.isExternal() ) {
                continue;
            }
            
            PerstFrostUploadItem pi = new PerstFrostUploadItem();
            pi.filePath = ulItem.getFile().getPath();
            pi.fileSize = ulItem.getFileSize();
            pi.chkKey = ulItem.getKey();
            pi.enabled = (ulItem.isEnabled()==null?true:ulItem.isEnabled().booleanValue());
            pi.state = ulItem.getState();
            pi.uploadAddedMillis = ulItem.getUploadAddedMillis();
            pi.uploadStartedMillis = ulItem.getUploadStartedMillis();
            pi.uploadFinishedMillis = ulItem.getUploadFinishedMillis();
            pi.retries = ulItem.getRetries();
            pi.lastUploadStopTimeMillis = ulItem.getLastUploadStopTimeMillis();
            pi.gqIdentifier = ulItem.getGqIdentifier();
            
            pi.sharedFilesSha = (ulItem.getSharedFileItem()==null?null:ulItem.getSharedFileItem().getSha());

            pi.makePersistent(storage);
            pi.modify(); // for already persistent items
            
            storageRoot.uploadFiles.add(pi);
        }
        storageRoot.uploadFiles.modify();
        
        storage.commit();
    }
    
    public List<FrostUploadItem> loadUploadFiles(List sharedFiles) {

        LinkedList<FrostUploadItem> uploadItems = new LinkedList<FrostUploadItem>();

        Language language = Language.getInstance();

        for(Iterator i=storageRoot.uploadFiles.iterator(); i.hasNext(); ) {

            PerstFrostUploadItem pi = (PerstFrostUploadItem)i.next();

            String filepath = pi.filePath;
            long filesize = pi.fileSize;
            String key = pi.chkKey;
            boolean isEnabled = pi.enabled;
            int state = pi.state;
            long uploadAddedTime = pi.uploadAddedMillis;
            long uploadStartedTime = pi.uploadStartedMillis;
            long uploadFinishedTime = pi.uploadFinishedMillis;
            int retries = pi.retries;
            long lastUploadStopMillis = pi.lastUploadStopTimeMillis;
            String gqId = pi.gqIdentifier;
            
            String sharedFilesSha = pi.sharedFilesSha;
            
            File file = new File(filepath);
            if( !file.isFile() ) {
                String title = language.getString("StartupMessage.uploadFile.uploadFileNotFound.title");
                String text = language.formatMessage("StartupMessage.uploadFile.uploadFileNotFound.text", filepath);
                StartupMessage sm = new StartupMessage(
                        StartupMessage.MessageType.UploadFileNotFound,
                        title,
                        text,
                        JOptionPane.ERROR_MESSAGE,
                        true);
                MainFrame.enqueueStartupMessage(sm);
                logger.severe("Upload items file does not exist, removed from upload files: "+filepath);
                continue;
            }
            if( file.length() != filesize ) {
                String title = language.getString("StartupMessage.uploadFile.uploadFileSizeChanged.title");
                String text = language.formatMessage("StartupMessage.uploadFile.uploadFileSizeChanged.text", filepath);
                StartupMessage sm = new StartupMessage(
                        StartupMessage.MessageType.UploadFileSizeChanged,
                        title,
                        text,
                        JOptionPane.ERROR_MESSAGE,
                        true);
                MainFrame.enqueueStartupMessage(sm);
                logger.severe("Upload items file size changed, removed from upload files: "+filepath);
                continue;
            }
            
            FrostSharedFileItem sharedFileItem = null;
            if( sharedFilesSha != null && sharedFilesSha.length() > 0 ) {
                for(Iterator j = sharedFiles.iterator(); j.hasNext(); ) {
                    FrostSharedFileItem s = (FrostSharedFileItem)j.next();
                    if( s.getSha().equals(sharedFilesSha) ) {
                        sharedFileItem = s;
                        break;
                    }
                }
                if( sharedFileItem == null ) {
                    logger.severe("Upload items shared file object does not exist, removed from upload files: "+filepath);
                    continue;
                }
                if( !sharedFileItem.isValid() ) {
                    logger.severe("Upload items shared file is invalid, removed from upload files: "+filepath);
                    continue;
                }
            }
            
            FrostUploadItem ulItem = new FrostUploadItem(
                    file,
                    filesize,
                    key,
                    isEnabled,
                    state,
                    uploadAddedTime,
                    uploadStartedTime,
                    uploadFinishedTime,
                    retries,
                    lastUploadStopMillis,
                    gqId);
            
            ulItem.setSharedFileItem(sharedFileItem);

            uploadItems.add(ulItem);
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
        storage.commit();
    }

    public void saveSharedFiles(List<FrostSharedFileItem> sfFiles) {

        // clear old items and list
        for(Iterator i=storageRoot.sharedFiles.iterator(); i.hasNext(); ) {
            Persistent pi = (Persistent)i.next();
            i.remove();
            pi.deallocate();
        }
            
        for(Iterator<FrostSharedFileItem> i=sfFiles.iterator(); i.hasNext(); ) {

            FrostSharedFileItem sfItem = i.next();
            
            PerstFrostSharedFileItem pi = new PerstFrostSharedFileItem();
            pi.filePath = sfItem.getFile().getPath();
            pi.fileSize = sfItem.getFileSize();
            pi.key = sfItem.getKey();
            pi.sha = sfItem.getSha();
            pi.owner = sfItem.getOwner();
            pi.comment = sfItem.getComment();
            pi.rating = sfItem.getRating();
            pi.keywords = sfItem.getKeywords();
            pi.lastUploaded = sfItem.getLastUploaded();
            pi.uploadCount = sfItem.getUploadCount();
            pi.refLastSent = sfItem.getRefLastSent();
            pi.requestLastReceived = sfItem.getRequestLastReceived();
            pi.requestsReceived = sfItem.getRequestsReceived();
            pi.lastModified = sfItem.getLastModified();
            
            pi.makePersistent(storage);
            pi.modify(); // for already persistent items
            
            storageRoot.sharedFiles.add(pi);
        }
        storageRoot.sharedFiles.modify();
        
        storage.commit();
    }

    public List<FrostSharedFileItem> loadSharedFiles() {

        LinkedList<FrostSharedFileItem> sfItems = new LinkedList<FrostSharedFileItem>();
        
        Language language = Language.getInstance();
        for(Iterator i=storageRoot.sharedFiles.iterator(); i.hasNext(); ) {

            PerstFrostSharedFileItem pi = (PerstFrostSharedFileItem)i.next();
            
            String filepath = pi.filePath;
            long filesize = pi.fileSize;
            String key = pi.key;
            
            String sha = pi.sha;
            String owner = pi.owner;
            String comment = pi.comment;
            int rating = pi.rating;
            String keywords = pi.keywords;
            long lastUploaded = pi.lastUploaded;
            int uploadCount = pi.uploadCount;
            long refLastSent = pi.refLastSent;
            long requestLastReceived = pi.requestLastReceived;
            int requestsReceivedCount = pi.requestsReceived;
            long lastModified = pi.lastModified;

            boolean fileIsOk = true;
            File file = new File(filepath);

            // report modified/missing shared files only if filesharing is enabled
            if( !Core.frostSettings.getBoolValue(SettingsClass.DISABLE_FILESHARING) ) {
                if( !file.isFile() ) {
                    String title = language.getString("StartupMessage.sharedFile.sharedFileNotFound.title");
                    String text = language.formatMessage("StartupMessage.sharedFile.sharedFileNotFound.text", filepath);
                    StartupMessage sm = new StartupMessage(
                            StartupMessage.MessageType.SharedFileNotFound,
                            title,
                            text,
                            JOptionPane.WARNING_MESSAGE,
                            true);
                    MainFrame.enqueueStartupMessage(sm);
                    logger.severe("Shared file does not exist: "+filepath);
                    fileIsOk = false;
                } else if( file.length() != filesize ) {
                    String title = language.getString("StartupMessage.sharedFile.sharedFileSizeChanged.title");
                    String text = language.formatMessage("StartupMessage.sharedFile.sharedFileSizeChanged.text", filepath);
                    StartupMessage sm = new StartupMessage(
                            StartupMessage.MessageType.SharedFileSizeChanged,
                            title,
                            text,
                            JOptionPane.WARNING_MESSAGE,
                            true);
                    MainFrame.enqueueStartupMessage(sm);
                    logger.severe("Size of shared file changed: "+filepath);
                    fileIsOk = false;
                } else if( file.lastModified() != lastModified ) {
                    String title = language.getString("StartupMessage.sharedFile.sharedFileLastModifiedChanged.title");
                    String text = language.formatMessage("StartupMessage.sharedFile.sharedFileLastModifiedChanged.text", filepath);
                    StartupMessage sm = new StartupMessage(
                            StartupMessage.MessageType.SharedFileLastModifiedChanged,
                            title,
                            text,
                            JOptionPane.WARNING_MESSAGE,
                            true);
                    MainFrame.enqueueStartupMessage(sm);
                    logger.severe("Last modified date of shared file changed: "+filepath);
                    fileIsOk = false;
                }
            }
            
            FrostSharedFileItem sfItem = new FrostSharedFileItem(
                    file,
                    filesize,
                    key,
                    sha,
                    owner,
                    comment,
                    rating,
                    keywords,
                    lastUploaded,
                    uploadCount,
                    refLastSent,
                    requestLastReceived,
                    requestsReceivedCount,
                    lastModified,
                    fileIsOk);

            sfItems.add(sfItem);
        }
        return sfItems;
    }
    
    public void saveNewUploadFiles(List newUploadFiles) {

        // clear old items and list
        for(Iterator i=storageRoot.newUploadFiles.iterator(); i.hasNext(); ) {
            Persistent pi = (Persistent)i.next();
            i.remove();
            pi.deallocate();
        }

        for(Iterator i=newUploadFiles.iterator(); i.hasNext(); ) {
            NewUploadFile nuf = (NewUploadFile)i.next();
            nuf.makePersistent(storage);
            nuf.modify(); // for already persistent items
            
            storageRoot.newUploadFiles.add(nuf);
        }
        storageRoot.newUploadFiles.modify();
        
        storage.commit();
    }

    public LinkedList<NewUploadFile> loadNewUploadFiles() {

        LinkedList<NewUploadFile> newUploadFiles = new LinkedList<NewUploadFile>();

        for(Iterator i=storageRoot.newUploadFiles.iterator(); i.hasNext(); ) {
            NewUploadFile nuf = (NewUploadFile)i.next();
            File f = new File(nuf.getFilePath());
            if (!f.isFile()) {
                logger.warning("File ("+nuf.getFilePath()+") is missing. File removed.");
                continue;
            }
            newUploadFiles.add(nuf);
        }
        return newUploadFiles;
    }
}
