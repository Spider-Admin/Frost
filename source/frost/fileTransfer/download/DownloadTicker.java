/*
 * Created on Apr 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.download;

import java.util.*;

import javax.swing.event.EventListenerList;

import frost.*;

/**
 * 
 */
public class DownloadTicker extends Thread {

	private SettingsClass settings;

	private DownloadPanel panel;
	private DownloadModel model;

	private int counter;
	
	/**
	 * The number of allocated threads is used to limit the total of threads
	 * that can be running at a given time, whereas the number of running
	 * threads is the number of threads that are actually running.
	 */
	private int allocatedThreads = 0;
	private int runningThreads = 0;
	
	private Object threadCountLock = new Object();
	
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Used to sort FrostDownloadItems by lastUpdateStartTimeMillis ascending.
	 */
	static final Comparator downloadDlStopMillisCmp = new Comparator() {
		public int compare(Object o1, Object o2) {
			FrostDownloadItem value1 = (FrostDownloadItem) o1;
			FrostDownloadItem value2 = (FrostDownloadItem) o2;
			if (value1.getLastDownloadStopTimeMillis() > value2.getLastDownloadStopTimeMillis())
				return 1;
			else if (
				value1.getLastDownloadStopTimeMillis() < value2.getLastDownloadStopTimeMillis())
				return -1;
			else
				return 0;
		}
	};

	/**
	 * @param name
	 */
	public DownloadTicker(
		SettingsClass newSettings,
		DownloadModel newModel,
		DownloadPanel newPanel) {

		super("Download");
		settings = newSettings;
		model = newModel;
		panel = newPanel;
	}
	
	/**
	 * Adds a <code>DownloadTickerListener</code> to the DownloadTicker.
	 * @param listener the <code>DownloadTickerListener</code> to be added
	 */
	public void addDownloadTickerListener(DownloadTickerListener listener) {
		listenerList.add(DownloadTickerListener.class, listener);
	}

	/**
	 * This method is called to find out if a new thread can start. It temporarily 
	 * allocates it and it will have to be relased when it is no longer
	 * needed (no matter whether the thread was actually used or not).
	 * @return true if a new thread can start. False otherwise.
	 */
	private boolean allocateThread() {
		synchronized (threadCountLock) {
			if (allocatedThreads < settings.getIntValue("downloadThreads")) {
				allocatedThreads++;
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
	protected void fireThreadCountChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == DownloadTickerListener.class) {
				((DownloadTickerListener) listeners[i + 1]).threadCountChanged();
			}
		}
	}
	
	/**
	 * This method is used to release a thread.
	 */
	private void releaseThread() {
		synchronized (threadCountLock) {
			if (allocatedThreads > 0) {
				allocatedThreads--;
			} 
		}
	}

	/**
	 * This method returns the number of threads that are running
	 * @return the number of threads that are running
	 */
	public int getRunningThreads() {
		return runningThreads;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		super.run();
		while (true) {
			Mixed.wait(1000);
			// this method is called by a timer each second, so this counter counts seconds
			counter++;
			updateDownloadCountLabel();
			startDownloadThread();
			removeFinishedDownloads();
		}
	}
	
	/**
	 * This method is usually called from a thread to notify the ticker that
	 * the thread has finished (so that it can notify its listeners of the fact). It also
	 * releases the thread so that new threads can start if needed.
	 */
	void threadFinished() {
		runningThreads--;
		fireThreadCountChanged();
		releaseThread();
	}
	
	/**
	 * This method is called from a thread to notify the ticker that
	 * the thread has started (so that it can notify its listeners of the fact)
	 */
	void threadStarted() {
		runningThreads++;
		fireThreadCountChanged();
	}

	/**
	 * 
	 */
	private void removeFinishedDownloads() {
		if (counter % 300 == 0 && settings.getBoolValue("removeFinishedDownloads")) {
			model.removeFinishedDownloads();
		}
	}
	
	/**
	 * Removes an <code>DownloadTickerListener</code> from the DownloadTicker.
	 * @param listener the <code>DownloadTickerListener</code> to be removed
	 */
	public void removeDownloadTickerListener(DownloadTickerListener listener) {
		listenerList.remove(DownloadTickerListener.class, listener);
	}

	/**
	 * Updates the download items count label. The label shows all WAITING items in download table.
	 * Called periodically by timer_actionPerformed().
	 */
	public void updateDownloadCountLabel() {
		if (settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS) == true)
			return;

		int waitingItems = 0;
		for (int x = 0; x < model.getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(x);
			if (dlItem.getState() == FrostDownloadItem.STATE_WAITING) {
				waitingItems++;
			}
		}
		panel.setDownloadItemCount(waitingItems);
	}

	/**
	 * 
	 */
	private void startDownloadThread() {
		if (panel.isDownloadingActivated() && allocateThread()) {
			boolean threadLaunched = false;

			FrostDownloadItem dlItem = selectNextDownloadItem();
			if (dlItem != null) {
				dlItem.setState(FrostDownloadItem.STATE_TRYING);

				DownloadThread newRequest = new DownloadThread(this, dlItem, model, settings);
				newRequest.start();
				threadLaunched = true;
			}

			if (!threadLaunched) {
				releaseThread();
			}
		}
	}



	/**
	 * Chooses next download item to start from download table.
	 */
	private FrostDownloadItem selectNextDownloadItem() {

		// get the item with state "Waiting", minimum htl and not over maximum htl
		ArrayList waitingItems = new ArrayList();
		for (int i = 0; i < model.getItemCount(); i++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(i);
			if ((dlItem.getState() == FrostDownloadItem.STATE_WAITING
				&& (dlItem.getEnableDownload() == null
					|| dlItem.getEnableDownload().booleanValue()
						== true) //                && dlItem.getRetries() <= frame1.frostSettings.getIntValue("downloadMaxRetries")
			)
				|| ((dlItem.getState() == FrostDownloadItem.STATE_REQUESTED
					|| dlItem.getState() == FrostDownloadItem.STATE_REQUESTING)
					&& dlItem.getKey() != null
					&& (dlItem.getEnableDownload() == null
						|| dlItem.getEnableDownload().booleanValue() == true))) {
				// check if waittime is expired
				long waittimeMillis = settings.getIntValue("downloadWaittime") * 60 * 1000;
				// min->millisec
				if (settings.getBoolValue("downloadRestartFailedDownloads")
					&& (System.currentTimeMillis() - dlItem.getLastDownloadStopTimeMillis())
						> waittimeMillis) {
					waitingItems.add(dlItem);
				}
			}
		}
		if (waitingItems.size() == 0)
			return null;

		if (waitingItems.size() > 1) { // performance issues
			Collections.sort(waitingItems, downloadDlStopMillisCmp);
		}
		return (FrostDownloadItem) waitingItems.get(0);
	}

}
