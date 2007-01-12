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

import java.io.*;
import java.util.*;
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

    /*
     * - sekuendlich die gq prüfen + items in beiden models updaten
     * - hashtable für schnelles finden verwenden? wo pflegen? modellistener?
     * 
     * - neue uploads/downloads sofort rein, max. aus options beachten (neue option ohne max?)
     * - wenn DIRECT, dann in eigene queue + thread, der nur directs überträgt,
     *   STATE ist dann transfer to/from node, mit %.
     *   -->> beachten: entweder haben alle DDA oder nicht. nicht zuviele zu starten!
     * 
     * - prios ermöglichen, auch delay usw.
     * 
     */
    
    private static Hashtable<String,FrostUploadItem> uploadModelItems = new Hashtable<String,FrostUploadItem>(); 
    private static Hashtable<String,FrostDownloadItem> downloadModelItems = new Hashtable<String,FrostDownloadItem>();
    
    private static UploadModel uploadModel;
    private static DownloadModel downloadModel;
    
    private static SyncThread syncThread;
    
    private static DirectTransferQueue directTransferQueue;
    private static DirectTransferThread directTransferThread;

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
        
        FcpPersistentConnectionTools.watchGlobal();

        directTransferThread.start();
        syncThread.start();        
    }
    
    /**
     * Receives persistent requests list from node and syncs them against the items in
     * upload and download model.
     * Starts uploads and downloads, adds / removes external requests 
     */
    private static class SyncThread extends Thread {
        
        public SyncThread() {
        }
        
        public void run() {
            final int maxAllowedExceptions = 5;
            int occuredExceptions = 0;

            while(true) {
                try {
                    Mixed.wait(1300); // refresh time

                    // FIXME: gegencheck, was wenn gelöscht aus global queue waehrend frost laeuft??
                    //  -> auf failed setzen mit msg das user gecancelt hat
                    // check: alle internal die wir als progress haben sollten noch in gq sein,
                    //        externals die weg sind aus table löschen
                    Hashtable<String,FrostUploadItem> runningUploads = new Hashtable<String,FrostUploadItem>(); 
                    Hashtable<String,FrostDownloadItem> runningDownloads = new Hashtable<String,FrostDownloadItem>(); 
                    for(int x=0; x < uploadModel.getItemCount(); x++) {
                        FrostUploadItem ul = (FrostUploadItem) uploadModel.getItemAt(x);
                        if( ul.getGqIdentifier() != null ) {
                            if( ul.getState() == FrostUploadItem.STATE_PROGRESS || ul.isExternal() ) {
                                runningUploads.put(ul.getGqIdentifier(), ul);
                            }
                        }
                    }
                    for(int x=0; x < downloadModel.getItemCount(); x++) {
                        FrostDownloadItem ul = (FrostDownloadItem) downloadModel.getItemAt(x);
                        if( ul.getGqIdentifier() != null ) {
                            if( ul.getState() == FrostDownloadItem.STATE_PROGRESS || ul.isExternal() ) {
                                runningDownloads.put(ul.getGqIdentifier(), ul);
                            }
                        }
                    }

                    List<NodeMessage> nodeMsgs = FcpPersistentConnectionTools.listPersistentRequests();
                    
                    for(NodeMessage nm : nodeMsgs) {
                        // expected:
                        // ----------
                        //  PersistentGet
                        //  DataFound -> entweder holen oder fertig
                        //  GetFailed
                        //
                        //  PersistentPut
                        //  PutSuccessful
                        //  PutFailed
                        //
                        //  SimpleProgress
                        
                        if( nm.isMessageName("PersistentGet") ) {
                            String id = nm.getStringValue("Identifier");
                            FrostDownloadItem dlItem = downloadModelItems.get(id);
                            runningDownloads.remove(id);
                            if( dlItem == null ) {
                                FrostDownloadItem newDlItem = new FrostDownloadItem(
                                        nm.getStringValue("Filename"), 
                                        nm.getStringValue("URI"));
                                newDlItem.setExternal(true);
                                newDlItem.setGqIdentifier(id);
                                downloadModel.addExternalItem(newDlItem);
                                continue;
                            }
                            String isDirect = nm.getStringValue("ReturnType");
                            if( isDirect.equalsIgnoreCase("disk") ) {
                                dlItem.setDirect(false);
                            } else {
                                dlItem.setDirect(true);
                            }
                            
                        } else if( nm.isMessageName("DataFound") ) {
                            // get request completed
                            String id = nm.getStringValue("Identifier");
                            FrostDownloadItem dlItem = downloadModelItems.get(id);
                            if( dlItem == null ) {
                                continue;
                            }
                            if( dlItem.isExternal() ) {
                                dlItem.setState(FrostDownloadItem.STATE_DONE);
                                dlItem.setFileSize(nm.getLongValue("DataLength"));
                                continue;
                            }
                            if( dlItem.isDirect() ) {
                                directTransferQueue.appendItemToQueue(dlItem);
                            } else {
                                FcpResultGet result = new FcpResultGet(true);
                                File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                                FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                                FcpPersistentConnectionTools.removeRequest(dlItem.getGqIdentifier());
                            }
                            
                        } else if( nm.isMessageName("GetFailed") ) {
                            String id = nm.getStringValue("Identifier");
                            FrostDownloadItem dlItem = downloadModelItems.get(id);
                            if( dlItem == null ) {
                                continue;
                            }
                            String desc = nm.getStringValue("CodeDescription");
                            if( dlItem.isExternal() ) {
                                dlItem.setState(FrostDownloadItem.STATE_FAILED);
                                dlItem.setErrorCodeDescription(desc);
                                continue;
                            }
                            int code = nm.getIntValue("Code");
                            boolean isFatal = nm.getBoolValue("Fatal");
                            
                            FcpResultGet result = new FcpResultGet(false, code, desc, isFatal);
                            File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);

                            FcpPersistentConnectionTools.removeRequest(dlItem.getGqIdentifier());

                        } else if( nm.isMessageName("PersistentPut") ) {
                            String id = nm.getStringValue("Identifier");
                            FrostUploadItem ulItem = uploadModelItems.get(id);
                            runningUploads.remove(id);
                            if( ulItem == null ) {
                                FrostUploadItem newUlItem = new FrostUploadItem();
                                newUlItem.setGqIdentifier(id);
                                newUlItem.setExternal(true);
                                newUlItem.setFile(new File(nm.getStringValue("Filename")));
                                uploadModel.addExternalItem(newUlItem);
                                continue;
                            }
                        } else if( nm.isMessageName("PutSuccessful") ) {
                            String id = nm.getStringValue("Identifier");
                            FrostUploadItem dlItem = uploadModelItems.get(id);
                            if( dlItem == null ) {
                                continue;
                            }
                            String chkKey = nm.getStringValue("URI");
                            int pos = chkKey.indexOf("CHK@"); 
                            if( pos > -1 ) {
                                chkKey = chkKey.substring(pos).trim();
                            }
                            if( dlItem.isExternal() ) {
                                dlItem.setState(FrostDownloadItem.STATE_DONE);
                                dlItem.setKey(chkKey);
                                continue;
                            }
                            FcpResultPut result = new FcpResultPut(FcpResultPut.Success, chkKey);
                            FileTransferManager.inst().getUploadManager().notifyUploadFinished(dlItem, result);

                        } else if( nm.isMessageName("PutFailed") ) {
                            String id = nm.getStringValue("Identifier");
                            FrostUploadItem dlItem = uploadModelItems.get(id);
                            if( dlItem == null ) {
                                continue;
                            }
                            String desc = nm.getStringValue("CodeDescription");
                            if( dlItem.isExternal() ) {
                                dlItem.setState(FrostUploadItem.STATE_FAILED);
                                dlItem.setErrorCodeDescription(desc);
                                continue;
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
                            
                        } else if( nm.isMessageName("SimpleProgress") ) {
                            // item auf progress setzen

                            int doneBlocks = nm.getIntValue("Succeeded");
                            int totalBlocks = nm.getIntValue("Total");
                            boolean isFinalized = nm.getBoolValue("FinalizedTotal");
                            
                            String id = nm.getStringValue("Identifier");
                            if( uploadModelItems.containsKey(id) ) {
                                FrostUploadItem ulItem = uploadModelItems.get(id);
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
                                if( totalBlocks > 0 ) {
                                    dlItem.setDoneBlocks(doneBlocks);
                                    dlItem.setTotalBlocks(totalBlocks);
                                    dlItem.setFinalized(isFinalized);
                                    dlItem.fireValueChanged();
                                }
                                if( dlItem.getState() != FrostDownloadItem.STATE_PROGRESS ) {
                                    dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                                }
                            }
                        } else if( nm.isMessageName("EndListPersistentRequests") ) {
                        } else {
                            System.out.println("SyncThread: Unhandled NodeMessage: "+nm);
                        }
                    }
                    
                    // all items still in runningUploads and runningDownloads were not in the nodelist
                    // remove external items, or flag internal items as cancelled
                    {
                        List<ModelItem> itemsToRemove = new LinkedList<ModelItem>();
                        for(FrostUploadItem ui : runningUploads.values()) {
                            if( ui.isExternal() ) {
                                itemsToRemove.add(ui);
                            } else {
                                ui.setState(FrostUploadItem.STATE_FAILED);
                                ui.setErrorCodeDescription("Disappeared from global queue");
                            }
                        }
                        ModelItem[] ri = (ModelItem[]) itemsToRemove.toArray(new ModelItem[itemsToRemove.size()]);
                        uploadModel.removeItems(ri);
                    }
                    {
                        List<ModelItem> itemsToRemove = new LinkedList<ModelItem>();
                        for(FrostDownloadItem ui : runningDownloads.values()) {
                            if( ui.isExternal() ) {
                                itemsToRemove.add(ui);
                            } else {
                                ui.setState(FrostDownloadItem.STATE_FAILED);
                                ui.setErrorCodeDescription("Disappeared from global queue");
                            }
                        }
                        ModelItem[] ri = (ModelItem[]) itemsToRemove.toArray(new ModelItem[itemsToRemove.size()]);
                        downloadModel.removeItems(ri);
                    }
                    
                    // now search for waiting internal items and start them if this would'nt exceed the limits
                    // before put into queue set state to progress!
                    startNewUploads();
                    startNewDownloads();
                    
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

    private static void startNewUploads() {
        int currentAllowedUploadCount = 0;
        {
            int allowedConcurrentUploads = Core.frostSettings.getIntValue(SettingsClass.UPLOAD_MAX_THREADS);
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
        {                    
            for(FrostUploadItem ulItem : uploadModelItems.values() ) {
                if( currentAllowedUploadCount <= 0 ) {
                    break;
                }
                if( ulItem.isExternal() ) {
                    continue;
                }
                if( ulItem.getState() != FrostUploadItem.STATE_WAITING ) {
                    continue;
                }
                // start the upload
                if( isDDA() ) {
                    boolean doMime;
                    if( ulItem.isSharedFile() ) {
                        doMime = false;
                    } else {
                        doMime = true;
                    }
                    NodeMessage answer = FcpPersistentConnectionTools.startPersistentPut(
                            ulItem.getGqIdentifier(),
                            ulItem.getFile(),
                            doMime);
                    handleStartPersistentPutAnswer(answer, ulItem);
                } else {
                    directTransferQueue.appendItemToQueue(ulItem);
                }
                
                ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                currentAllowedUploadCount--;
            }
        }
    }
    
    private static void startNewDownloads() {
        int currentAllowedDownloadCount = 0;
        {
            int allowedConcurrentDownloads = Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_THREADS);
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
        {
            for(FrostDownloadItem dlItem : downloadModelItems.values() ) {
                if( currentAllowedDownloadCount <= 0 ) {
                    break;
                }
                if( dlItem.isExternal() ) {
                    continue;
                }
                if( dlItem.getState() != FrostDownloadItem.STATE_WAITING ) {
                    continue;
                }
                // start the download
                String gqid = dlItem.getGqIdentifier();
                File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                NodeMessage answer = FcpPersistentConnectionTools.startPersistentGet(
                        dlItem.getKey(),
                        gqid,
                        targetFile);
                handleStartPersistentGetAnswer(answer, dlItem);
                
                dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                currentAllowedDownloadCount--;
            }
        }
    }
    
    /**
     * Called to handle the node answer after the transfer of a persistent get.
     */
    private static void handleStartPersistentGetAnswer(NodeMessage answer, FrostDownloadItem dlItem) {
        if( answer == null ) {
            dlItem.setState(FrostDownloadItem.STATE_FAILED);
            dlItem.setErrorCodeDescription("Unable to start persistent request");
        } else if(answer.isMessageName("PersistentGet")) {
            dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
        } else if(answer.isMessageName("ProtocolError")) {
            dlItem.setState(FrostUploadItem.STATE_FAILED);
            dlItem.setErrorCodeDescription(answer.getStringValue("CodeDescription"));
        } else {
            dlItem.setState(FrostUploadItem.STATE_FAILED);
            dlItem.setErrorCodeDescription(answer.getMessageName());
        }
    }

    /**
     * Called to handle the node answer after the start of a persistent put.
     */
    private static void handleStartPersistentPutAnswer(NodeMessage answer, FrostUploadItem ulItem) {
        if( answer == null ) {
            ulItem.setState(FrostUploadItem.STATE_FAILED);
            ulItem.setErrorCodeDescription("Unable to start persistent request");
        } else if(answer.isMessageName("PersistentPut")) {
            ulItem.setState(FrostUploadItem.STATE_PROGRESS);
        } else if(answer.isMessageName("ProtocolError")) {
            ulItem.setState(FrostUploadItem.STATE_FAILED);
            ulItem.setErrorCodeDescription(answer.getStringValue("CodeDescription"));
        } else {
            ulItem.setState(FrostUploadItem.STATE_FAILED);
            ulItem.setErrorCodeDescription(answer.getMessageName());
        }
    }

    private static class DirectTransferThread extends Thread {
        
        public DirectTransferThread() {
        }
        
        public void run() {
            
            final int maxAllowedExceptions = 5;
            int occuredExceptions = 0;

            while(true) {
                try {
                    // if there is no work in queue this call waits for a new queue item
                    ModelItem item = directTransferQueue.getItemFromQueue();
                    
                    if( item == null ) {
                        // paranoia
                        Mixed.wait(60*1000);
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
                        handleStartPersistentPutAnswer(answer, ulItem);
                        
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
                        } else {
                            FcpResultGet result = new FcpResultGet(false);
                            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                        }
                        FcpPersistentConnectionTools.removeRequest(dlItem.getGqIdentifier());
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
    
    private static class DirectTransferQueue {
        
        private LinkedList<ModelItem> queue = new LinkedList<ModelItem>();
        private int uploadItemCount = 0;
        private int downloadItemCount = 0;
        
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
                if( item instanceof FrostUploadItem ) {
                    uploadItemCount--;
                } else if( item instanceof FrostDownloadItem ) {
                    downloadItemCount--;
                }
                return item;
            }
            return null;
        }

        public synchronized void appendItemToQueue(FrostUploadItem item) {
            queue.addLast(item);
            uploadItemCount++;
            notifyAll(); // notify all waiters (if any) of new record
        }

        public synchronized void appendItemToQueue(FrostDownloadItem item) {
            queue.addLast(item);
            downloadItemCount++;
            notifyAll(); // notify all waiters (if any) of new record
        }
        
        public synchronized int getQueueSize() {
            return queue.size();
        }

        public int getDownloadItemCount() {
            return downloadItemCount;
        }

        public int getUploadItemCount() {
            return uploadItemCount;
        }
    }
}
