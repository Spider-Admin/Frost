/*
 * Created on Apr 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.download;

import java.util.*;

import frost.*;

/**
 * 
 */
public class DownloadTicker extends Thread {

	private SettingsClass settings;

	private DownloadPanel panel;
	private DownloadTable table;

	private int counter;
	private int threadCount = 0;

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
		DownloadTable newTable,
		DownloadPanel newPanel) {
		super("Download");
		settings = newSettings;
		table = newTable;
		panel = newPanel;
	}

	/**
	 * 
	 */
	public synchronized void decreaseThreadCount() {
		threadCount--;
	}

	/**
	 * @return
	 */
	public synchronized int getThreadCount() {
		return threadCount;
	}

	/**
	 * 
	 */
	public synchronized void increaseThreadCount() {
		threadCount++;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		super.run();
		while (true) {
			mixed.wait(1000);
			timer_actionPerformed();
		}
	}

	/**
	 * 
	 */
	private void timer_actionPerformed() {
		// this method is called by a timer each second, so this counter counts seconds
		counter++;
		updateDownloadCountLabel();
		startDownloadThread();
		removeFinishedDownloads();
	}

	/**
	 * 
	 */
	private void removeFinishedDownloads() {
		if (counter % 300 == 0 && settings.getBoolValue("removeFinishedDownloads")) {
			table.removeFinishedDownloads();
		}
	}

	/**
	 * Updates the download items count label. The label shows all WAITING items in download table.
	 * Called periodically by timer_actionPerformed().
	 */
	public void updateDownloadCountLabel() {
		if (settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS) == true)
			return;

		DownloadTableModel model = (DownloadTableModel) table.getModel();
		int waitingItems = 0;
		for (int x = 0; x < model.getRowCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) model.getRow(x);
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
		int activeThreads = getThreadCount();

		// check all 3 seconds if a download could be started
		if (counter % 3 == 0
			&& activeThreads < settings.getIntValue("downloadThreads")
			&& panel.isDownloadingActivated()) {
			// choose first item
			FrostDownloadItem dlItem = selectNextDownloadItem();
			if (dlItem != null) {
				DownloadTableModel dlModel = (DownloadTableModel) table.getModel();

				dlItem.setState(FrostDownloadItem.STATE_TRYING);
				dlModel.updateRow(dlItem);

				DownloadThread newRequest = new DownloadThread(this, dlItem, table, settings);
				newRequest.start();
			}
		}

	}

	/**
	 * Chooses next download item to start from download table.
	 */
	private FrostDownloadItem selectNextDownloadItem() {
		DownloadTableModel dlModel = (DownloadTableModel) table.getModel();

		// get the item with state "Waiting", minimum htl and not over maximum htl
		ArrayList waitingItems = new ArrayList();
		for (int i = 0; i < dlModel.getRowCount(); i++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) dlModel.getRow(i);
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

		if (waitingItems.size() > 1) // performance issues
			{
			Collections.sort(waitingItems, downloadDlStopMillisCmp);
		}
		return (FrostDownloadItem) waitingItems.get(0);
	}

}
