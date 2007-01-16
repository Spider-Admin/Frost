/*
  PersistenceManager.java / Frost
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
package frost.fileTransfer;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fcp.fcp07.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.util.*;
import frost.util.model.*;

/**
 * This class starts/stops/monitors the persistent requests on Freenet 0.7.
 */
public class PersistenceManager {

    private static Logger logger = Logger.getLogger(PersistenceManager.class.getName());

    /* FIXME:
     * - update count in status bar
     * - show if external in table
     */

    // this would belong to the models, but not needed for 0.5 or without persistence, hence we maintain it here
    private static Hashtable<String,FrostUploadItem> uploadModelItems = new Hashtable<String,FrostUploadItem>(); 
    private static Hashtable<String,FrostDownloadItem> downloadModelItems = new Hashtable<String,FrostDownloadItem>();
    
    private static UploadModel uploadModel;
    private static DownloadModel downloadModel;
    
    private static SyncThread syncThread;
    
    private static DirectTransferQueue directTransferQueue;
    private static DirectTransferThread directTransferThread;
    
    private static NodeMessageHandler nodeMessageHandler = new NodeMessageHandler();
    
    private static boolean showExternalItemsDownload;
    private static boolean showExternalItemsUpload;
    
    /**
     * @return  true if Frost is configured to use persistent uploads and downloads, false if not
     */
    public static boolean isPersistenceEnabled() {
        if( FcpHandler.isFreenet07()
                && Core.frostSettings.getBoolValue(SettingsClass.FCP2_USE_PERSISTENCE) ) 
        {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDDA() {
        if( FcpHandler.isFreenet07()
                && Core.frostSettings.getBoolValue(SettingsClass.FCP2_USE_DDA) 
                && FcpPersistentConnection.getInstance().isDDA() ) 
        {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Must be called after the upload and download model is initialized!
     */
    public static void initialize(UploadModel um, DownloadModel dm) {
        
        showExternalItemsDownload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD);
        showExternalItemsUpload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD);
        
        Core.frostSettings.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( evt.getPropertyName().equals(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD) ) {
                    showExternalItemsDownload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD);
                    if( showExternalItemsDownload ) {
                        awakeSyncThread();
                    }
                } else if( evt.getPropertyName().equals(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD) ) {
                    showExternalItemsUpload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD);
                    if( showExternalItemsUpload ) {
                        awakeSyncThread();
                    }
                }
            }
        });
        
        uploadModel = um;
        downloadModel = dm;
        
        // initially get all items from model
        for(int x=0; x < uploadModel.getItemCount(); x++) {
            FrostUploadItem ul = (FrostUploadItem) uploadModel.getItemAt(x);
            if( ul.getGqIdentifier() != null ) {
                uploadModelItems.put(ul.getGqIdentifier(), ul);
            }
        }
        for(int x=0; x < downloadModel.getItemCount(); x++) {
            FrostDownloadItem ul = (FrostDownloadItem) downloadModel.getItemAt(x);
            if( ul.getGqIdentifier() != null ) {
                downloadModelItems.put(ul.getGqIdentifier(), ul);
            }
        }
        
        // enqueue listeners to keep updated about the model items
        uploadModel.addModelListener(
                new ModelListener() {
                    public void itemAdded(ModelItem item) {
                        FrostUploadItem ul = (FrostUploadItem) item;
                        uploadModelItems.put(ul.getGqIdentifier(), ul);
                        awakeSyncThread();
                    }
                    public void itemsRemoved(ModelItem[] items) {
                        for(ModelItem item : items) {
                            FrostUploadItem ul = (FrostUploadItem) item;
                            uploadModelItems.remove(ul.getGqIdentifier());
                            if( ul.isExternal() == false ) {
                                FcpPersistentConnectionTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                    }
                    public void modelCleared() {
                        for( FrostUploadItem ul : uploadModelItems.values() ) {
                            if( ul.isExternal() == false ) {
                                FcpPersistentConnectionTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                        uploadModelItems.clear();
                    }

                    public void itemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {}

                    public void itemChanged(ModelItem item) {}
                });

        downloadModel.addModelListener(
                new ModelListener() {
                    public void itemAdded(ModelItem item) {
                        FrostDownloadItem ul = (FrostDownloadItem) item;
                        downloadModelItems.put(ul.getGqIdentifier(), ul);
                        awakeSyncThread();
                    }
                    public void itemsRemoved(ModelItem[] items) {
                        for(ModelItem item : items) {
                            FrostDownloadItem ul = (FrostDownloadItem) item;
                            downloadModelItems.remove(ul.getGqIdentifier());
                            if( ul.isExternal() == false ) {
                                FcpPersistentConnectionTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                    }
                    public void modelCleared() {
                        for( FrostDownloadItem ul : downloadModelItems.values() ) {
                            if( ul.isExternal() == false ) {
                                FcpPersistentConnectionTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                        downloadModelItems.clear();
                    }

                    public void itemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {}

                    public void itemChanged(ModelItem item) {}
                });
        
        directTransferQueue = new DirectTransferQueue();
        directTransferThread = new DirectTransferThread();
        syncThread = new SyncThread();
    }
    
    public static void startThreads() {
        
        FcpPersistentConnection.getInstance().addNodeMessageListener(nodeMessageHandler);
        FcpPersistentConnectionTools.watchGlobal();

        directTransferThread.start();
        syncThread.start();        
    }
    
    /**
     * Awake waiting syncthread to maybe start new requests immediately.
     */
    public static void awakeSyncThread() {
        if( syncThread != null && !syncThread.isInterrupted() ) {
            syncThread.interrupt();
        }
    }
    
    public static void changeItemPriorites(ModelItem[] items, int newPrio) {
        if( items == null || items.length == 0 ) {
            return;
        }
        for( int i = 0; i < items.length; i++ ) {
            ModelItem item = items[i];
            String gqid = null;
            if( item instanceof FrostUploadItem ) {
                FrostUploadItem ui = (FrostUploadItem) item; 
                gqid = ui.getGqIdentifier();
            } else if( item instanceof FrostUploadItem ) {
                FrostDownloadItem di = (FrostDownloadItem) item; 
                gqid = di.getGqIdentifier();
            }
            if( gqid != null ) {
                FcpPersistentConnectionTools.changeRequestPriority(gqid, newPrio);
            }
        }
    }
    
    /**
     * Receives persistent requests list from node and syncs them against the items in
     * upload and download model.
     * Starts uploads and downloads, adds / removes external requests 
     */
    private static class SyncThread extends Thread {
        
        public SyncThread() {
            super();
        }
        
        public void run() {
            final int maxAllowedExceptions = 5;
            int occuredExceptions = 0;
            
            // initial delay
            Mixed.wait(1000);

            while(true) {
                try {
                    
                    nodeMessageHandler.clearKnownIdentifiers();
                    FcpPersistentConnectionTools.listPersistentRequests();
                    
                    // wait for end of list
                    Set<String> identifiers = nodeMessageHandler.waitForEnd();
                    
                    // find items that disappeared from the global queue
                    handleDisappearedItems(identifiers);
                    
                    // start new requests
                    // search for waiting internal items and start them if this would'nt exceed the limits
                    // before put into queue set state to progress!
                    startNewUploads();
                    startNewDownloads();
                    
                    // delay until next run
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // if we were interrupted then new items were added
                        // wait some more time to let batch adds finished, then begin process
                        // we hold the interrupted state to not get interrupted during this wait again
                        Mixed.wait(500);
                    }
                    interrupted(); // clear interrupted state
                    
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception catched",t);
                    occuredExceptions++;
                }
                
                if( occuredExceptions > maxAllowedExceptions ) {
                    logger.log(Level.SEVERE, "Stopping SyncThread because of too much exceptions");
                    break;
                }
            }
        }
    }
    
    private static void handleDisappearedItems(Set<String> existingIds) {

        // remove external items, or flag internal items as cancelled
        {
            List<ModelItem> itemsToRemove = new LinkedList<ModelItem>();
            for(int x=0; x < uploadModel.getItemCount(); x++) {
                FrostUploadItem ulItem = (FrostUploadItem) uploadModel.getItemAt(x);
                if( ulItem.getGqIdentifier() != null ) {
                    if( ulItem.getState() == FrostUploadItem.STATE_PROGRESS || ulItem.isExternal() ) {
                        // this item should be in the global queue
                        boolean isInGlobalQueue = existingIds.contains(ulItem.getGqIdentifier());
                        if( !isInGlobalQueue ) {
                            if( ulItem.isExternal() ) {
                                itemsToRemove.add(ulItem);
                            } else {
                                ulItem.setEnabled(false);
                                ulItem.setState(FrostUploadItem.STATE_FAILED);
                                ulItem.setErrorCodeDescription("Disappeared from global queue");
                            }
                        }
                    }
                }
            }
            ModelItem[] ri = (ModelItem[]) itemsToRemove.toArray(new ModelItem[itemsToRemove.size()]);
            uploadModel.removeItems(ri);
        }
        {
            List<ModelItem> itemsToRemove = new LinkedList<ModelItem>();
            for(int x=0; x < downloadModel.getItemCount(); x++) {
                FrostDownloadItem dlItem = (FrostDownloadItem) downloadModel.getItemAt(x);
                if( dlItem.getGqIdentifier() != null ) {
                    if( dlItem.getState() == FrostDownloadItem.STATE_PROGRESS || dlItem.isExternal() ) {
                        // this item should be in the global queue
                        boolean isInGlobalQueue = existingIds.contains(dlItem.getGqIdentifier());
                        if( !isInGlobalQueue ) {
                            if( dlItem.isExternal() ) {
                                itemsToRemove.add(dlItem);
                            } else {
                                dlItem.setEnabled(false);
                                dlItem.setState(FrostUploadItem.STATE_FAILED);
                                dlItem.setErrorCodeDescription("Disappeared from global queue");
                            }
                        }
                    }
                }
            }
            ModelItem[] ri = (ModelItem[]) itemsToRemove.toArray(new ModelItem[itemsToRemove.size()]);
            downloadModel.removeItems(ri);
        }
    }

    private static void startNewUploads() {
        boolean isLimited = true;
        int currentAllowedUploadCount = 0;
        {
            int allowedConcurrentUploads = Core.frostSettings.getIntValue(SettingsClass.UPLOAD_MAX_THREADS);
            if( allowedConcurrentUploads <= 0 ) {
                isLimited = false;
            } else {
                int runningUploads = 0;
                for(FrostUploadItem ulItem : uploadModelItems.values() ) {
                    if( ulItem.getState() == FrostUploadItem.STATE_PROGRESS) {
                        runningUploads++;
                    }
                }
                currentAllowedUploadCount = allowedConcurrentUploads - runningUploads;
                if( currentAllowedUploadCount < 0 ) {
                    currentAllowedUploadCount = 0;
                }
            }
        }
        {
            while( !isLimited || currentAllowedUploadCount > 0 ) {
                FrostUploadItem ulItem = FileTransferManager.inst().getUploadManager().selectNextUploadItem();
                if( ulItem == null ) {
                    break;
                }
                // start the upload
                if( isDDA() ) {
                    boolean doMime;
                    if( ulItem.isSharedFile() ) {
                        doMime = false;
                    } else {
                        doMime = true;
                    }
                    FcpPersistentConnectionTools.startPersistentPut(
                            ulItem.getGqIdentifier(),
                            ulItem.getFile(),
                            doMime);
                } else {
                    directTransferQueue.appendItemToQueue(ulItem);
                }
                
                ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                currentAllowedUploadCount--;
            }
        }
    }
    
    private static void startNewDownloads() {
        boolean isLimited = true;
        int currentAllowedDownloadCount = 0;
        {
            int allowedConcurrentDownloads = Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_THREADS);
            if( allowedConcurrentDownloads <= 0 ) {
                isLimited = false;
            } else {
                int runningDownloads = 0;
                for(FrostDownloadItem dlItem : downloadModelItems.values() ) {
                    if( dlItem.getState() == FrostDownloadItem.STATE_PROGRESS) {
                        runningDownloads++;
                    }
                }
                currentAllowedDownloadCount = allowedConcurrentDownloads - runningDownloads;
                if( currentAllowedDownloadCount < 0 ) {
                    currentAllowedDownloadCount = 0;
                }
            }
        }
        {
            while( !isLimited || currentAllowedDownloadCount > 0 ) {
                FrostDownloadItem dlItem = FileTransferManager.inst().getDownloadManager().selectNextDownloadItem();
                if (dlItem == null) {
                    break;
                }
                // start the download
                String gqid = dlItem.getGqIdentifier();
                File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                FcpPersistentConnectionTools.startPersistentGet(
                        dlItem.getKey(),
                        gqid,
                        targetFile);
                
                dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                currentAllowedDownloadCount--;
            }
        }
    }
    
    private static class DirectTransferThread extends Thread {
        
        public void run() {
            
            final int maxAllowedExceptions = 5;
            int occuredExceptions = 0;

            while(true) {
                try {
                    // if there is no work in queue this call waits for a new queue item
                    ModelItem item = directTransferQueue.getItemFromQueue();
                    
                    if( item == null ) {
                        // paranoia, should never happen
                        Mixed.wait(5*1000);
                        continue;
                    }
                    
                    if( item instanceof FrostUploadItem ) {
                        // transfer bytes to node
                        FrostUploadItem ulItem = (FrostUploadItem) item;
                        // FIXME: provide item, state=Transfer to node, % shows progress
                        String gqid = ulItem.getGqIdentifier();
                        File sourceFile = ulItem.getFile();
                        boolean doMime;
                        if( ulItem.isSharedFile() ) {
                            doMime = false;
                        } else {
                            doMime = true;
                        }
                        NodeMessage answer = FcpPersistentConnectionTools.startDirectPersistentPut(gqid, sourceFile, doMime);
                        if( answer == null ) {
                            logger.severe("Could not open a new fcp socket for direct put!");
                        } else {
                            nodeMessageHandler.handleNodeMessage(gqid, answer);
                        }
                        
                    } else if( item instanceof FrostDownloadItem ) {
                        // transfer bytes from node
                        FrostDownloadItem dlItem = (FrostDownloadItem) item;
                        // FIXME: provide item, state=Transfer from node, % shows progress
                        String gqid = dlItem.getGqIdentifier();
                        File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());

                        NodeMessage answer = FcpPersistentConnectionTools.startDirectPersistentGet(gqid, targetFile);
                        if( answer != null ) {
                            FcpResultGet result = new FcpResultGet(true);
                            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);

                            FcpPersistentConnectionTools.removeRequest(dlItem.getGqIdentifier());
                        } else {
                            logger.severe("Could not open a new fcp socket for direct get!");
                            FcpResultGet result = new FcpResultGet(false);
                            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                        }
                    }
                    
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception catched",t);
                    occuredExceptions++;
                }
                
                if( occuredExceptions > maxAllowedExceptions ) {
                    logger.log(Level.SEVERE, "Stopping DirectTransferThread because of too much exceptions");
                    break;
                }
            }
        }
    }
    
    /**
     * A queue class that queues items waiting for its direct transfer (put to node or get from node).
     */
    private static class DirectTransferQueue {
        
        private LinkedList<ModelItem> queue = new LinkedList<ModelItem>();
        
        public synchronized ModelItem getItemFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    wait();
                }
            } catch (InterruptedException e) {
                return null; // waiting abandoned
            }
            
            if( queue.isEmpty() == false ) {
                ModelItem item = queue.removeFirst();
                return item;
            }
            return null;
        }

        public synchronized void appendItemToQueue(ModelItem item) {
            queue.addLast(item);
            notifyAll(); // notify all waiters (if any) of new record
        }

        public synchronized int getQueueSize() {
            return queue.size();
        }
    }
    
    private static class NodeMessageHandler implements NodeMessageListener {
        
        private Set<String> knownIdentifiers = new HashSet<String>();
        private Semaphore waitForEndLock = new Semaphore(1);
        private boolean collectingIdentifiers = false;
        
        public synchronized void clearKnownIdentifiers() {
            knownIdentifiers.clear();
            if( waitForEndLock.tryAcquire() == false ) {
                System.out.println("ALERT: semaphore could not be acquired!");
            }
            collectingIdentifiers = true;
        }
        
        public synchronized void addIdentifier(String id) {
            knownIdentifiers.add(id);
        }
        
        /**
         * Called by SyncThread to wait for end of list.
         */
        public Set<String> waitForEnd() {
            waitForEndLock.acquireUninterruptibly();
            // give back immediately for next run
            waitForEndLock.release();
            // return collected identifiers
            collectingIdentifiers = false;
            return knownIdentifiers;
        }

        public void handleNodeMessage(NodeMessage nm) {
            // bei start hashtables vorbereiten
            if( nm.isMessageName("EndListPersistentRequests") ) {
                // unlock syncthread
                waitForEndLock.release();
            }
        }
        
        public void handleNodeMessage(String id, NodeMessage nm) {

            if( collectingIdentifiers ) {
                addIdentifier(id);
            }
            
            if( nm.isMessageName("PersistentGet") ) {
                onPersistentGet(id, nm);
            } else if( nm.isMessageName("DataFound") ) {
                onDataFound(id, nm);
            } else if( nm.isMessageName("GetFailed") ) {
                onGetFailed(id, nm);
            } else if( nm.isMessageName("PersistentPut") ) {
                onPersistentPut(id, nm);
            } else if( nm.isMessageName("PutSuccessful") ) {
                onPutSuccessful(id, nm);
            } else if( nm.isMessageName("PutFailed") ) {
                onPutFailed(id, nm);
            } else if( nm.isMessageName("SimpleProgress") ) {
                onSimpleProgress(id, nm);
            } else if( nm.isMessageName("IdentifierCollision") ) {
                onIdentifierCollision(id, nm);
            } else if( nm.isMessageName("ProtocolError") ) {
                onProtocolError(id, nm);
            } else {
                // unhandled msg
//                System.out.println("### INFO - Unhandled msg: "+nm);
            }
        }
        
        protected void onPersistentGet(String id, NodeMessage nm) {
            int prio = nm.getIntValue("PriorityClass");
            FrostDownloadItem dlItem = downloadModelItems.get(id);
            if( dlItem == null && showExternalItemsDownload ) {
                dlItem = new FrostDownloadItem(
                        nm.getStringValue("Filename"), 
                        nm.getStringValue("URI"));
                dlItem.setExternal(true);
                dlItem.setGqIdentifier(id);
                dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                dlItem.setPriority(prio);
                downloadModel.addExternalItem(dlItem);
                return;
            } else {
                if( dlItem.getState() == FrostDownloadItem.STATE_WAITING ) {
                    dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                }
                dlItem.setPriority(prio);
                String isDirect = nm.getStringValue("ReturnType");
                if( isDirect.equalsIgnoreCase("disk") ) {
                    dlItem.setDirect(false);
                } else {
                    dlItem.setDirect(true);
                }
            }
        }
        protected void onDataFound(String id, NodeMessage nm) {
            // get request completed
            FrostDownloadItem dlItem = downloadModelItems.get(id);
            if( dlItem == null ) {
                return;
            }
            if( dlItem.isExternal() ) {
                dlItem.setState(FrostDownloadItem.STATE_DONE);
                dlItem.setFileSize(nm.getLongValue("DataLength"));
                return;
            }
            if( dlItem.isDirect() ) {
                directTransferQueue.appendItemToQueue(dlItem);
            } else {
                FcpResultGet result = new FcpResultGet(true);
                File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                FcpPersistentConnectionTools.removeRequest(dlItem.getGqIdentifier());
            }
        }
        protected void onGetFailed(String id, NodeMessage nm) {
            FrostDownloadItem dlItem = downloadModelItems.get(id);
            if( dlItem == null ) {
                return;
            }
            String desc = nm.getStringValue("CodeDescription");
            if( dlItem.isExternal() ) {
                dlItem.setState(FrostDownloadItem.STATE_FAILED);
                dlItem.setErrorCodeDescription(desc);
                return;
            }
            int code = nm.getIntValue("Code");
            boolean isFatal = nm.getBoolValue("Fatal");
            
            FcpResultGet result = new FcpResultGet(false, code, desc, isFatal);
            File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);

            FcpPersistentConnectionTools.removeRequest(dlItem.getGqIdentifier());
        }
        protected void onPersistentPut(String id, NodeMessage nm) {
            int prio = nm.getIntValue("PriorityClass");
            FrostUploadItem ulItem = uploadModelItems.get(id);
            if( ulItem == null && showExternalItemsUpload ) {
                ulItem = new FrostUploadItem();
                ulItem.setGqIdentifier(id);
                ulItem.setExternal(true);
                ulItem.setFile(new File(nm.getStringValue("Filename")));
                ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                ulItem.setPriority(prio);
                uploadModel.addExternalItem(ulItem);
                return;
            } else {
                ulItem.setPriority(prio);
                if( ulItem.getState() == FrostUploadItem.STATE_WAITING ) {
                    ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                }
            }
        }
        protected void onPutSuccessful(String id, NodeMessage nm) {
            FrostUploadItem dlItem = uploadModelItems.get(id);
            if( dlItem == null ) {
                return;
            }
            String chkKey = nm.getStringValue("URI");
            int pos = chkKey.indexOf("CHK@"); 
            if( pos > -1 ) {
                chkKey = chkKey.substring(pos).trim();
            }
            if( dlItem.isExternal() ) {
                dlItem.setState(FrostDownloadItem.STATE_DONE);
                dlItem.setKey(chkKey);
                return;
            }
            FcpResultPut result = new FcpResultPut(FcpResultPut.Success, chkKey);
            FileTransferManager.inst().getUploadManager().notifyUploadFinished(dlItem, result);
        }
        protected void onPutFailed(String id, NodeMessage nm) {
            FrostUploadItem dlItem = uploadModelItems.get(id);
            if( dlItem == null ) {
                return;
            }
            String desc = nm.getStringValue("CodeDescription");
            if( dlItem.isExternal() ) {
                dlItem.setState(FrostUploadItem.STATE_FAILED);
                dlItem.setErrorCodeDescription(desc);
                return;
            }
            int returnCode = nm.getIntValue("Code");
            boolean isFatal = nm.getBoolValue("Fatal");
            
            FcpResultPut result;
            if( returnCode == 9 ) {
                result = new FcpResultPut(FcpResultPut.KeyCollision, returnCode, desc, isFatal);
            } else if( returnCode == 5 ) {
                result = new FcpResultPut(FcpResultPut.Retry, returnCode, desc, isFatal);
            } else {
                result = new FcpResultPut(FcpResultPut.Error, returnCode, desc, isFatal);
            }
            FileTransferManager.inst().getUploadManager().notifyUploadFinished(dlItem, result);
        }
        protected void onSimpleProgress(String id, NodeMessage nm) {
            if( uploadModelItems.containsKey(id) ) {
                FrostUploadItem ulItem = uploadModelItems.get(id);
                int doneBlocks = nm.getIntValue("Succeeded");
                int totalBlocks = nm.getIntValue("Total");
                boolean isFinalized = nm.getBoolValue("FinalizedTotal");
                if( totalBlocks > 0 ) {
                    ulItem.setDoneBlocks(doneBlocks);
                    ulItem.setTotalBlocks(totalBlocks);
                    ulItem.setFinalized(isFinalized);
                    ulItem.fireValueChanged();
                }
                if( ulItem.getState() != FrostUploadItem.STATE_PROGRESS ) {
                    ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                }
            } else if( downloadModelItems.containsKey(id) ) {
                FrostDownloadItem dlItem = downloadModelItems.get(id);
                int doneBlocks = nm.getIntValue("Succeeded");
                int requiredBlocks = nm.getIntValue("Required");
                int totalBlocks = nm.getIntValue("Total");
                boolean isFinalized = nm.getBoolValue("FinalizedTotal");
                if( totalBlocks > 0 ) {
                    dlItem.setDoneBlocks(doneBlocks);
                    dlItem.setRequiredBlocks(requiredBlocks);
                    dlItem.setTotalBlocks(totalBlocks);
                    dlItem.setFinalized(isFinalized);
                    dlItem.fireValueChanged();
                }
                if( dlItem.getState() != FrostDownloadItem.STATE_PROGRESS ) {
                    dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                }
            }
        }
        protected void onProtocolError(String id, NodeMessage nm) {
            // should not happen, set item to failed
            if( uploadModelItems.containsKey(id) ) {
                FrostUploadItem ulItem = uploadModelItems.get(id);
                String desc = nm.getStringValue("CodeDescription");
                ulItem.setEnabled(false);
                ulItem.setState(FrostUploadItem.STATE_FAILED);
                ulItem.setErrorCodeDescription(nm.getMessageName()+": "+desc);
            } else if( downloadModelItems.containsKey(id) ) {
                FrostDownloadItem dlItem = downloadModelItems.get(id);
                String desc = nm.getStringValue("CodeDescription");
                dlItem.setEnabled(false);
                dlItem.setState(FrostUploadItem.STATE_FAILED);
                dlItem.setErrorCodeDescription(nm.getMessageName()+": "+desc);
            }            
        }
        protected void onIdentifierCollision(String id, NodeMessage nm) {
            // since we use the same unique gqid, most likly this request already runs!
            System.out.println("### ATTENTION ###: "+nm);
        }
    }
}
