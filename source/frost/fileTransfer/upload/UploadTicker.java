/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.upload;

import javax.swing.event.EventListenerList;

import frost.*;
import frost.identities.LocalIdentity;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UploadTicker extends Thread {

	//To be able to increase this value, we have to add support for that. Without it,
	//the loops in generateCHK and prepareUpladHashes would process the same file
	//several times.
	private final int MAX_GENERATING_THREADS = 1;

	private LocalIdentity myID;

	private SettingsClass settings;

	private UploadPanel panel;
	private UploadModel model;

	private int counter;
	
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
	public UploadTicker(SettingsClass newSettings, UploadModel newModel, UploadPanel newPanel, LocalIdentity newMyID) {
		super("Upload");
		settings = newSettings;
		model = newModel;
		panel = newPanel;
		myID = newMyID;
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
			if (allocatedUploadingThreads < settings.getIntValue("uploadThreads")) {
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
	void generatingThreadStarted() {
		runningGeneratingThreads++;
		fireGeneratingCountChanged();
	}
	
	/**
	 * This method is usually called from a generating thread to notify the ticker that
	 * the thread has finished (so that it can notify its listeners of the fact). It also
	 * releases the thread so that new generating threads can start if needed.
	 */
	void generatingThreadFinished() {
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
			counter++;
			removeNotExistingFiles();
			prepareUploadHashes();
			generateCHKs();
			startUploadThread();
		}
	}

	/**
	 * This method generates CHK's for upload table entries
	 */
	private void generateCHKs() {
		/**  Do not generate CHKs, get SHA1 only! */
		/**  and generate CHK if requested ... */

		if (allocateGeneratingThread()) {
			boolean threadLaunched = false;

			for (int i = 0; i < model.getItemCount() && !threadLaunched; i++) {
				FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);
				if (ulItem.getState() == FrostUploadItem.STATE_ENCODING_REQUESTED
					|| (ulItem.getKey() == null
						&& ulItem.getState() == FrostUploadItem.STATE_REQUESTED)) {
					UploadThread newInsert = null;
					if (ulItem.getState() == FrostUploadItem.STATE_REQUESTED) {
						// set next state for item to REQUESTED, default is IDLE
						// needed to keep the REQUESTED state for real uploading
						newInsert =
							new UploadThread(
								this,
								ulItem,
								settings,
								UploadThread.MODE_GENERATE_CHK,
								FrostUploadItem.STATE_REQUESTED,
								myID);
					} else {
						// next state will be IDLE (=default)
						newInsert =
							new UploadThread(
								this,
								ulItem,
								settings,
								UploadThread.MODE_GENERATE_CHK,
								myID);
					}
					ulItem.setState(FrostUploadItem.STATE_ENCODING);
					newInsert.start();
					threadLaunched = true; 	// start only 1 thread per loop (=second)
				}
			}
			if (!threadLaunched) {
				releaseGeneratingThread();	
			}
		}
	}

	/**
	 * 
	 */
	private void startUploadThread() {
		if (allocateUploadingThread()) {
			boolean threadLaunched = false;
			
			for (int i = 0; i < model.getItemCount() && !threadLaunched; i++) {
				FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);
				if (ulItem.getState() == FrostUploadItem.STATE_REQUESTED
					&& ulItem.getSHA1() != null
					&& ulItem.getKey() != null)
					// file have key after encoding
					{
					ulItem.setState(FrostUploadItem.STATE_UPLOADING);
					UploadThread newInsert =
						new UploadThread(this, ulItem, settings, UploadThread.MODE_UPLOAD, myID);
					newInsert.start();
					threadLaunched = true; 	// start only 1 thread per loop (=second)
				}
			}
			
			if (!threadLaunched) {
				releaseUploadingThread();	
			}
		}
	}
	
	/**
	 * 
	 */
	private void prepareUploadHashes() {
		// do this only if the automatic index handling is set
		if (settings.getBoolValue("automaticIndexing") && allocateGeneratingThread()) {
			boolean threadLaunched = false;
			
			for (int i = 0; i < model.getItemCount() && !threadLaunched; i++) {
				FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(i);
				if (ulItem.getSHA1() == null) {
					ulItem.setKey("Working...");
					UploadThread newInsert =
						new UploadThread(
							this,
							ulItem,
							settings,
							UploadThread.MODE_GENERATE_SHA1,
							myID);
					newInsert.start();
					threadLaunched = true; 	// start only 1 thread per loop (=second)
				}
			}	
			if (!threadLaunched) {
				releaseGeneratingThread();	
			}			
		}
	}

	/**
	 * 
	 */
	private void removeNotExistingFiles() {
		// Check uploadTable every 3 minutes
		if (counter % 180 == 0) {
			model.removeNotExistingFiles();
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
