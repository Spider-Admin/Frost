/*
  FcpPersistentQueue.java / Frost
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
package frost.fcp.fcp07.persistence;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import frost.fcp.fcp07.*;
import frost.fileTransfer.*;
import frost.util.*;

public class FcpPersistentQueue {

    private static Logger logger = Logger.getLogger(FcpPersistentQueue.class.getName());

    private FcpPersistentConnectionTools fcpTools;
    private IFcpPersistentRequestsHandler persistenceHandler;
    
    private SyncThread syncThread;
    private NodeMessageHandler messageHandler;
    
    public FcpPersistentQueue(FcpPersistentConnectionTools tools, PersistenceManager pman) {
        fcpTools = tools;
        persistenceHandler = pman;
        messageHandler = new NodeMessageHandler();
        syncThread = new SyncThread(messageHandler);
    }
    
    public void startThreads() {
        FcpPersistentConnection.getInstance().addNodeMessageListener(messageHandler);
        fcpTools.watchGlobal();

        syncThread.start();        
    }

    public void awakeSyncThread() {
        if( syncThread != null && !syncThread.isInterrupted() ) {
            syncThread.interrupt();
        }
    }

    public Map<String,FcpPersistentPut> getUploadRequests() {
        return messageHandler.getUploadRequestsCopy();
    }
    public Map<String,FcpPersistentGet> getDownloadRequests() {
        return messageHandler.getDownloadRequestsCopy();
    }

    /**
     * Receives persistent requests list from node and syncs them against the items in
     * upload and download model.
     * Starts uploads and downloads, adds / removes external requests 
     */
    private class SyncThread extends Thread {
        
        NodeMessageHandler nodeMessageHandler;
        
        public SyncThread(NodeMessageHandler newNodeMessageHandler) {
            super();
            nodeMessageHandler = newNodeMessageHandler;
        }
        
        public void run() {
            final int maxAllowedExceptions = 5;
            int occuredExceptions = 0;
            
            // initial delay
            Mixed.wait(1000);

            while(true) {
                try {
                    nodeMessageHandler.clearKnownIdentifiers();
                    fcpTools.listPersistentRequests();
                    
                    // wait for end of list
                    nodeMessageHandler.waitForEnd();
                    
                    // notify manager
                    persistenceHandler.requestsUpdated(getUploadRequests(), getDownloadRequests());
                    
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
    
    private class NodeMessageHandler implements NodeMessageListener {

        private HashMap<String,FcpPersistentPut> uploadRequests = new HashMap<String,FcpPersistentPut>();
        private HashMap<String,FcpPersistentGet> downloadRequests = new HashMap<String,FcpPersistentGet>();

        private Semaphore waitForEndLock = new Semaphore(1);
        
        private boolean isProcessingList = false; // true during processing of ListPersistentRequests
        
        public synchronized void clearKnownIdentifiers() {
            isProcessingList = true;
            uploadRequests.clear();
            downloadRequests.clear();
            if( waitForEndLock.tryAcquire() == false ) {
                System.out.println("ALERT: semaphore could not be acquired!");
                logger.severe("ALERT: semaphore could not be acquired!");
            }
        }

        @SuppressWarnings("unchecked")
        public synchronized Map<String,FcpPersistentPut> getUploadRequestsCopy() {
            return (Map<String,FcpPersistentPut>)uploadRequests.clone();
        }
        
        @SuppressWarnings("unchecked")
        public synchronized Map<String,FcpPersistentGet> getDownloadRequestsCopy() {
            return (Map<String,FcpPersistentGet>)downloadRequests.clone();
        }
        
        /**
         * Called by SyncThread to wait for end of list.
         */
        public void waitForEnd() {
            waitForEndLock.acquireUninterruptibly();
            // give back immediately for next run
            waitForEndLock.release();
        }
        
        private boolean isProcessingList() {
            return isProcessingList;
        }

        public void handleNodeMessage(NodeMessage nm) {
            if( nm.isMessageName("EndListPersistentRequests") ) {
                // unlock syncthread
                isProcessingList = false;
                waitForEndLock.release();
            }
        }
        
        public void handleNodeMessage(String id, NodeMessage nm) {

//            System.out.println("MSG="+nm);
            
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
                System.out.println("### INFO - Unhandled msg: "+nm);
            }
        }
        
        protected void onPersistentGet(String id, NodeMessage nm) {
            if( downloadRequests.containsKey(id) ) {
                FcpPersistentGet pg = downloadRequests.get(id);
                pg.setRequest(nm);
                persistenceHandler.downloadRequestUpdated(pg);
                return;
            } else {
                FcpPersistentGet fpg = new FcpPersistentGet(nm, id);
                downloadRequests.put(id, fpg);
                // we don't add new items on the fly
            }
        }
        protected void onDataFound(String id, NodeMessage nm) {
            if( !downloadRequests.containsKey(id) ) {
                System.out.println("No item in download queue: "+nm);
                return;
            } else {
                FcpPersistentGet pg = downloadRequests.get(id); 
                pg.setSuccess(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.downloadRequestUpdated(pg);
                }
            }
        }
        protected void onGetFailed(String id, NodeMessage nm) {
            if( !downloadRequests.containsKey(id) ) {
                System.out.println("No item in download queue: "+nm);
                return;
            } else {
                FcpPersistentGet pg = downloadRequests.get(id); 
                pg.setFailed(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.downloadRequestUpdated(pg);
                }
            }
        }
        protected void onPersistentPut(String id, NodeMessage nm) {
            if( uploadRequests.containsKey(id) ) {
                FcpPersistentPut pg = uploadRequests.get(id);
                pg.setRequest(nm);
                persistenceHandler.uploadRequestUpdated(pg);
                return;
            } else {
                FcpPersistentPut fpg = new FcpPersistentPut(nm, id);
                uploadRequests.put(id, fpg);
                // we don't add new items on the fly
            }
        }
        protected void onPutSuccessful(String id, NodeMessage nm) {
            if( !uploadRequests.containsKey(id) ) {
                System.out.println("No item in upload queue: "+nm);
                return;
            } else {
                FcpPersistentPut pg = uploadRequests.get(id); 
                pg.setSuccess(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.uploadRequestUpdated(pg);
                }
            }
        }
        protected void onPutFailed(String id, NodeMessage nm) {
            if( !uploadRequests.containsKey(id) ) {
                System.out.println("No item in upload queue: "+nm);
                return;
            } else {
                FcpPersistentPut pp = uploadRequests.get(id); 
                pp.setFailed(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.uploadRequestUpdated(pp);
                }
            }
        }
        protected void onSimpleProgress(String id, NodeMessage nm) {
            if( downloadRequests.containsKey(id) ) {
                FcpPersistentGet pg = downloadRequests.get(id); 
                pg.setProgress(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.downloadRequestUpdated(pg);
                }

            } else if( uploadRequests.containsKey(id) ) {
                FcpPersistentPut pg = uploadRequests.get(id); 
                pg.setProgress(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.uploadRequestUpdated(pg);
                }
            } else {
                System.out.println("No item in queue: "+nm);
                return;
            }
        }
        protected void onProtocolError(String id, NodeMessage nm) {
            if( downloadRequests.containsKey(id) ) {
                FcpPersistentGet pg = downloadRequests.get(id); 
                pg.setFailed(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.downloadRequestUpdated(pg);
                }
            } else if( uploadRequests.containsKey(id) ) {
                FcpPersistentPut pg = uploadRequests.get(id); 
                pg.setFailed(nm);
                if( !isProcessingList() ) {
                    persistenceHandler.uploadRequestUpdated(pg);
                }
            } else {
                System.out.println("No item in queue: +nm");
                return;
            }
        }
        protected void onIdentifierCollision(String id, NodeMessage nm) {
            // since we use the same unique gqid, most likly this request already runs!
            System.out.println("### ATTENTION ###: "+nm);
        }
    }
}
