/*
  DownloadTicker.java / Frost

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
package frost.fileTransfer.download;

import java.util.*;

import javax.swing.event.EventListenerList;

import frost.*;
import frost.util.*;

public class DownloadTicker extends Thread {

	private DownloadPanel panel;
	private DownloadModel model;

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
			if (value1.getLastDownloadStopTime() > value2.getLastDownloadStopTime())
				return 1;
			else if (
				value1.getLastDownloadStopTime() < value2.getLastDownloadStopTime())
				return -1;
			else
				return 0;
		}
	};

	public DownloadTicker(DownloadModel newModel, DownloadPanel newPanel) {
		super("Download");
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
			if (allocatedThreads < Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_THREADS)) {
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
			// this method is called by a timer each second
			updateDownloadCountLabel();
			startDownloadThread();
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
		int waitingItems = 0;
		for (int x = 0; x < model.getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(x);
			if (dlItem.getState() != FrostDownloadItem.STATE_DONE 
                    && dlItem.getState() != FrostDownloadItem.STATE_FAILED) 
            {
				waitingItems++;
			}
		}
		panel.setDownloadItemCount(waitingItems);
	}

	private void startDownloadThread() {
		if (Core.isFreenetOnline() && panel.isDownloadingActivated() && allocateThread()) {
			boolean threadLaunched = false;

			FrostDownloadItem dlItem = selectNextDownloadItem();
			if (dlItem != null) {
				dlItem.setState(FrostDownloadItem.STATE_TRYING);

				DownloadThread newRequest = new DownloadThread(this, dlItem, model);
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
	 * @return the next download item to start downloading or null if a suitable
	 * 			one was not found.
	 */
	private FrostDownloadItem selectNextDownloadItem() {

		// get the item with state "Waiting", minimum htl and not over maximum htl
		ArrayList waitingItems = new ArrayList();
		for (int i = 0; i < model.getItemCount(); i++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(i);
            boolean itemIsEnabled = (dlItem.isEnabled()==null?true:dlItem.isEnabled().booleanValue());
            if( !itemIsEnabled ) {
                continue;
            }
            if( dlItem.getKey() == null ) {
                // still no key, wait
                continue;
            }
            
			if( dlItem.getState() == FrostDownloadItem.STATE_WAITING ) {
				// check if waittime is expired
				long waittimeMillis = (long)Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_WAITTIME) * 60L * 1000L;
				// min->millisec
				if (dlItem.getLastDownloadStopTime() == 0 // never started
					|| (System.currentTimeMillis() - dlItem.getLastDownloadStopTime()) > waittimeMillis) 
                {
					waitingItems.add(dlItem);
				}
			}
		}

        if (waitingItems.size() == 0) {
			return null;
        }

		if (waitingItems.size() > 1) { // performance issues
			Collections.sort(waitingItems, downloadDlStopMillisCmp);
		}
		return (FrostDownloadItem) waitingItems.get(0);
	}
}
