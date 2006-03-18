/*
 * Created on 28-jun-2005
 * 
 */
package frost.fileTransfer;

import frost.*;
import frost.boards.TofTreeModel;
import frost.fileTransfer.download.DownloadManager;
import frost.fileTransfer.search.SearchManager;
import frost.fileTransfer.upload.UploadManager;
import frost.identities.FrostIdentities;
import frost.storage.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class FileTransferManager implements Savable {

	private SettingsClass settings;
	
	private MainFrame mainFrame; 
	
	private TofTreeModel tofTreeModel;
	
	private boolean isOnline;
	
	private FrostIdentities identities;	
	
	private String keypool;
	
	private DownloadManager downloadManager;
	private SearchManager searchManager;
	private UploadManager uploadManager;

	
	/**
	 * @param settings
	 */
	public FileTransferManager(SettingsClass settings) {
		super();
		this.settings = settings;
	}

	/**
	 * @throws StorageException
	 */
	public void initialize() throws StorageException {
		getDownloadManager().initialize();
		getSearchManager().initialize();
		getUploadManager().initialize();
		Index.initialize(getDownloadManager().getModel());
		
		//Until the downloads and uploads are fully separated from frame1:
		mainFrame.getMessagePanel().setDownloadModel(getDownloadManager().getModel());
		mainFrame.setUploadPanel(getUploadManager().getPanel());
		
		Thread requestsThread =
			new GetRequestsThread(
				settings.getIntValue("tofDownloadHtl"),
				settings.getValue("keypool.dir"),
				getUploadManager().getModel(),
				identities);
		requestsThread.start();
	}
	
	/**
	 * @param mainFrame
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;	
	}

	/**
	 * @param tofTreeModel
	 */
	public void setTofTreeModel(TofTreeModel tofTreeModel) {
		this.tofTreeModel = tofTreeModel;	
	}

	/**
	 * @param isOnline
	 */
	public void setFreenetIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	/**
	 * @param identities
	 */
	public void setIdentities(FrostIdentities identities) {
		this.identities = identities;		
	}

	/**
	 * @param keypool
	 */
	public void setKeypool(String keypool) {
		this.keypool = keypool;		
	}

	/**
	 * 
	 */
	private DownloadManager getDownloadManager() {
		if (downloadManager == null) {
			downloadManager = new DownloadManager(settings);
			downloadManager.setMainFrame(mainFrame);
			downloadManager.setFreenetIsOnline(isOnline);
		}
		return downloadManager;
	}

	/**
	 * 
	 */
	private SearchManager getSearchManager() {
		if (searchManager == null) {
			searchManager = new SearchManager(settings);
			searchManager.setMainFrame(mainFrame);
			searchManager.setDownloadModel(getDownloadManager().getModel());
			searchManager.setUploadModel(getUploadManager().getModel());
			searchManager.setTofTreeModel(tofTreeModel);
			searchManager.setKeypool(keypool);
			searchManager.setIdentities(identities);
		}
		return searchManager;
	}

	/**
	 * 
	 */
	private UploadManager getUploadManager() {
		if (uploadManager == null) {
			uploadManager = new UploadManager(settings);
			uploadManager.setMainFrame(mainFrame);
			uploadManager.setTofTreeModel(tofTreeModel);
			uploadManager.setFreenetIsOnline(isOnline);
			uploadManager.setMyID(identities.getMyId());
		}
		return uploadManager;
	}

	/* (non-Javadoc)
	 * @see frost.storage.Savable#save()
	 */
	public void save() throws StorageException {
		getDownloadManager().getModel().save();
		getUploadManager().getModel().save();
	}
	
}
