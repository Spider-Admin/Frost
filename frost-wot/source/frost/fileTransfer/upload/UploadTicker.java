/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.upload;

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
	private UploadTable table;

	private int counter;
	private int uploadingThreadCount = 0;
	private int generatingThreadCount = 0;

	/**
	 * 
	 */
	public UploadTicker(SettingsClass newSettings, UploadTable newTable, UploadPanel newPanel, LocalIdentity newMyID) {
		super("Upload");
		settings = newSettings;
		table = newTable;
		panel = newPanel;
		myID = newMyID;
	}

	/**
	 * 
	 */
	public synchronized boolean allocateUploadingThread() {
		if (uploadingThreadCount < settings.getIntValue("uploadThreads")) {
			uploadingThreadCount++;
			return true;
		} else {	
			return false;	
		}
	}
	
	/**
	 * @return
	 */
	public synchronized int getUploadingThreadCount() {
		return uploadingThreadCount;
	}
	
	/**
	 * 
	 */
	public synchronized boolean allocateGeneratingThread() {
		if (generatingThreadCount < MAX_GENERATING_THREADS) {
			generatingThreadCount++;
			return true;
		} else {
			return false;	
		}
	}

	/**
	 * 
	 */
	public synchronized void releaseUploadingThread() {
		if (uploadingThreadCount > 0) {
			uploadingThreadCount--;
		}
	}
	
	/**
	 * 
	 */
	public synchronized void releaseGeneratingThread() {
		if (generatingThreadCount > 0) {
			generatingThreadCount--;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		super.run();
		while (true) {
			mixed.wait(1000);
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
			UploadTableModel ulModel = (UploadTableModel) table.getModel();
			boolean threadLaunched = false;

			for (int i = 0; i < ulModel.getRowCount() && !threadLaunched; i++) {
				FrostUploadItemObject ulItem = (FrostUploadItemObject) ulModel.getRow(i);
				if (ulItem.getState() == FrostUploadItemObject.STATE_ENCODING_REQUESTED
					|| (ulItem.getKey() == null
						&& ulItem.getState() == FrostUploadItemObject.STATE_REQUESTED)) {
					UploadThread newInsert = null;
					if (ulItem.getState() == FrostUploadItemObject.STATE_REQUESTED) {
						// set next state for item to REQUESTED, default is IDLE
						// needed to keep the REQUESTED state for real uploading
						newInsert =
							new UploadThread(
								this,
								ulItem,
								settings,
								UploadThread.MODE_GENERATE_CHK,
								FrostUploadItemObject.STATE_REQUESTED,
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
					ulItem.setState(FrostUploadItemObject.STATE_ENCODING);
					ulModel.updateRow(ulItem);
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
			UploadTableModel ulModel = (UploadTableModel) table.getModel();
			boolean threadLaunched = false;
			
			for (int i = 0; i < ulModel.getRowCount() && !threadLaunched; i++) {
				FrostUploadItemObject ulItem = (FrostUploadItemObject) ulModel.getRow(i);
				if (ulItem.getState() == FrostUploadItemObject.STATE_REQUESTED
					&& ulItem.getSHA1() != null
					&& ulItem.getKey() != null)
					// file have key after encoding
					{
					ulItem.setState(FrostUploadItemObject.STATE_UPLOADING);
					ulModel.updateRow(ulItem);
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
			UploadTableModel ulModel = (UploadTableModel) table.getModel();
			boolean threadLaunched = false;
			
			for (int i = 0; i < ulModel.getRowCount() && !threadLaunched; i++) {
				FrostUploadItemObject ulItem = (FrostUploadItemObject) ulModel.getRow(i);
				if (ulItem.getSHA1() == null) {
					ulItem.setKey("Working...");
					ulModel.updateRow(ulItem);
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
		if (counter % 180 == 0) // Check uploadTable every 3 minutes
			{
			table.removeNotExistingFiles();
		}
	}

}
