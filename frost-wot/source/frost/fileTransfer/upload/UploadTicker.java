/*
  UploadTicker.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.upload;

import java.util.*;

import javax.swing.event.*;

import frost.*;
import frost.fcp.*;
import frost.util.*;

public class UploadTicker extends Thread {

//    private static Logger logger = Logger.getLogger(UploadTicker.class.getName());

    //To be able to increase this value, we have to add support for that. Without it,
    //the loops in generateCHK and prepareUpladHashes would process the same file
    //several times.
    private final int MAX_GENERATING_THREADS = 1;

    private UploadModel model;

    private int removeNotExistingFilesCounter = 0;

    /**
     * Used to sort FrostUploadItems by lastUploadStopTimeMillis ascending.
     */
    static final Comparator uploadDlStopMillisCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
            FrostUploadItem value1 = (FrostUploadItem) o1;
            FrostUploadItem value2 = (FrostUploadItem) o2;
            if (value1.getLastUploadStopTimeMillis() > value2.getLastUploadStopTimeMillis())
                return 1;
            else if (value1.getLastUploadStopTimeMillis() < value2.getLastUploadStopTimeMillis())
                return -1;
            else
                return 0;
        }
    };

    /**
     * The number of allocated threads is used to limit the total of threads
     * that can be running at a given time, whereas the number of running
     * threads is the number of threads that are actually running.
     */
    private int allocatedUploadingThreads = 0;
    private int allocatedGeneratingThreads = 0;
    private int runningUploadingThreads = 0;
    private int runningGeneratingThreads = 0;

    private Object uploadingCountLock = new Object();
    private Object generatingCountLock = new Object();

    protected EventListenerList listenerList = new EventListenerList();

    /**
     * @param newSettings
     * @param newModel
     * @param newPanel
     * @param newMyID
     */
    public UploadTicker(UploadModel newModel) {
        super("Upload");
        model = newModel;
    }

    /**
     * Adds an <code>UploadTickerListener</code> to the UploadTicker.
     * @param listener the <code>UploadTickerListener</code> to be added
     */
    public void addUploadTickerListener(UploadTickerListener listener) {
        listenerList.add(UploadTickerListener.class, listener);
    }

    /**
     * This method is called to find out if a new uploading thread can start. It
     * temporarily allocates it and it will have to be relased when it is no longer
     * needed (no matter whether the thread was actually used or not).
     * @return true if a new uploading thread can start. False otherwise.
     */
    private boolean allocateUploadingThread() {
        synchronized (uploadingCountLock) {
            if (allocatedUploadingThreads < Core.frostSettings.getIntValue("uploadThreads")) {
                allocatedUploadingThreads++;
                return true;
            }
        }
        return false;
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.
     *
     * @see EventListenerList
     */
    protected void fireUploadingCountChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == UploadTickerListener.class) {
                ((UploadTickerListener) listeners[i + 1]).uploadingCountChanged();
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.
     *
     * @see EventListenerList
     */
    protected void fireGeneratingCountChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == UploadTickerListener.class) {
                ((UploadTickerListener) listeners[i + 1]).generatingCountChanged();
            }
        }
    }

    /**
     * This method is called to find out if a new generating thread can start. It
     * temporarily allocates it and it will have to be relased when it is no longer
     * needed (no matter whether the thread was actually used or not).
     * @return true if a new generating thread can start. False otherwise.
     */
    private boolean allocateGeneratingThread() {
        synchronized (generatingCountLock) {
            if (allocatedGeneratingThreads < MAX_GENERATING_THREADS) {
                allocatedGeneratingThreads++;
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called from a generating thread to notify the ticker that
     * the thread has started (so that it can notify its listeners of the fact)
     */
    public void generatingThreadStarted() {
        runningGeneratingThreads++;
        fireGeneratingCountChanged();
    }

    /**
     * This method is usually called from a generating thread to notify the ticker that
     * the thread has finished (so that it can notify its listeners of the fact). It also
     * releases the thread so that new generating threads can start if needed.
     */
    public void generatingThreadFinished() {
        runningGeneratingThreads--;
        fireGeneratingCountChanged();
        releaseGeneratingThread();
    }

    /**
     * This method is called from an uploading thread to notify the ticker that
     * the thread has started (so that it can notify its listeners of the fact)
     */
    void uploadingThreadStarted() {
        runningUploadingThreads++;
        fireUploadingCountChanged();
    }

    /**
     * This method is called from an uploading thread to notify the ticker that the
     * thread has finished (so that it can notify its listeners of the fact). It also
     * releases the thread so that new generating threads can start if needed.
     */
    void uploadingThreadFinished() {
        runningUploadingThreads--;
        fireUploadingCountChanged();
        releaseUploadingThread();
    }

    /**
     * This method is used to release an uploading thread.
     */
    private void releaseUploadingThread() {
        synchronized (uploadingCountLock) {
            if (allocatedUploadingThreads > 0) {
                allocatedUploadingThreads--;
            }
        }
    }

    /**
     * This method is used to release a generating thread.
     */
    private void releaseGeneratingThread() {
        synchronized (generatingCountLock) {
            if (allocatedGeneratingThreads > 0) {
                allocatedGeneratingThreads--;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        super.run();
        while (true) {
            Mixed.wait(1000);
            // this is executed each second, so this counter counts seconds
            removeNotExistingFilesCounter++;
            removeNotExistingFiles();
            generateCHKs();
            startUploadThread();
        }
    }

    /**
     * This method generates CHK's for upload table entries
     */
    private void generateCHKs() {

        if (Core.isFreenetOnline() && allocateGeneratingThread()) {
            boolean threadLaunched = false;

            for (int i = 0; i < model.getItemCount() && !threadLaunched; i++) {
                FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);
                // encode if requested by user, OR
                // if state is WAITING for upload and its 0.5 and there is no chkKey, we must encode the file
                if (ulItem.getState() == FrostUploadItem.STATE_ENCODING_REQUESTED
                    || ( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05
                         && ulItem.getKey() == null
                         && ulItem.getState() == FrostUploadItem.STATE_WAITING ) 
                   )    
                {
                    // next state will be IDLE (=default)
                    GenerateChkThread newInsert = new GenerateChkThread(this, ulItem);
                    ulItem.setState(FrostUploadItem.STATE_ENCODING);
                    newInsert.start();
                    threadLaunched = true;  // start only 1 thread per loop (=second)
                }
            }
            if (!threadLaunched) {
                releaseGeneratingThread();
            }
        }
    }

    private void startUploadThread() {
        if (Core.isFreenetOnline() && allocateUploadingThread()) {
            FrostUploadItem item = selectNextUploadItem();
            if (item != null) {
                item.setState(FrostUploadItem.STATE_UPLOADING);
                UploadThread newInsert = new UploadThread(this, item);
                newInsert.start();
            } else {
                releaseUploadingThread();
            }
        }
    }

    /**
     * Chooses next upload item to start from upload table.
     * @return the next upload item to start uploading or null if a suitable one was not found.
     */
    private FrostUploadItem selectNextUploadItem() {

        ArrayList waitingItems = new ArrayList();

        for (int i = 0; i < model.getItemCount(); i++) {
            FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);
            // we choose items that are waiting, enabled and for 0.5 the encoding must be done before
            if (ulItem.getState() == FrostUploadItem.STATE_WAITING
                && (ulItem.isEnabled() == null || ulItem.isEnabled().booleanValue())
                && (ulItem.getKey() != null || FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) )
            {
                // check if waittime has expired
                long waittimeMillis = (long)Core.frostSettings.getIntValue(SettingsClass.UPLOAD_RETRIES_WAIT_TIME) * 60L * 1000L;
                if ((System.currentTimeMillis() - ulItem.getLastUploadStopTimeMillis()) > waittimeMillis) {
                    waitingItems.add(ulItem);
                }
            }
        }

        if (waitingItems.size() == 0) {
            return null;
        }

        if (waitingItems.size() > 1) { // performance issues
            Collections.sort(waitingItems, uploadDlStopMillisCmp);
        }
        return (FrostUploadItem) waitingItems.get(0);
    }

    private void removeNotExistingFiles() {
        // Check uploadTable every 3 minutes
        if (removeNotExistingFilesCounter >= 180 ) {
            model.removeNotExistingFiles();
            removeNotExistingFilesCounter = 0;
        }
    }

    /**
     * Removes an <code>UploadTickerListener</code> from the UploadTicker.
     * @param listener the <code>UploadTickerListener</code> to be removed
     */
    public void removeUploadTickerListener(UploadTickerListener listener) {
        listenerList.remove(UploadTickerListener.class, listener);
    }

    /**
     * This method returns the number of generating threads that are running
     * @return the number of generating threads that are running
     */
    public int getRunningGeneratingThreads() {
        return runningGeneratingThreads;
    }

    /**
     * This method returns the number of uploading threads that are running
     * @return the number of uploading threads that are running
     */
    public int getRunningUploadingThreads() {
        return runningUploadingThreads;
    }
}
