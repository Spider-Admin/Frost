/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.search;

import java.beans.*;

import frost.*;
import frost.boards.TofTreeModel;
import frost.fileTransfer.download.DownloadModel;
import frost.fileTransfer.upload.UploadModel;
import frost.identities.FrostIdentities;

/**
 * @author $Author$
 * @version $Revision$
 */
public class SearchManager implements PropertyChangeListener {

	private TofTreeModel tofTreeModel;
	private DownloadModel downloadModel;
	private UploadModel uploadModel;
	private String keypool;
	private FrostIdentities identities;
	private MainFrame mainFrame;
	private SettingsClass settings;

	private SearchModel model;
	private SearchPanel panel;

	/**
	 * @param settings
	 */
	public SearchManager(SettingsClass settings) {
		super();
		this.settings = settings;
	}

	/**
	 * 
	 */
	public void initialize() {
		mainFrame.addPanel("Search", getPanel());	
		settings.addPropertyChangeListener(SettingsClass.DISABLE_DOWNLOADS, this);	
		updateDownloadStatus();
	}

	/**
	 * @param mainFrame
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;	
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
	 * @param model
	 */
	public void setDownloadModel(DownloadModel model) {
		downloadModel = model;		
	}

	/**
	 * @param tofTreeModel
	 */
	public void setTofTreeModel(TofTreeModel tofTreeModel) {
		this.tofTreeModel = tofTreeModel;
	}

	/**
	 * @return
	 */
	public SearchPanel getPanel() {
		if (panel == null) {
			panel = new SearchPanel(settings, this); 
			panel.setModel(getModel());
			panel.setDownloadModel(downloadModel);
			panel.setUploadModel(uploadModel);
			panel.setTofTreeModel(tofTreeModel);
			panel.setKeypool(keypool);
			panel.setIdentities(identities);
			panel.initialize();
		}
		return panel;
	}

	/**
	 * @return
	 */
	public SearchModel getModel() {
		if (model == null) {
			model = new SearchModel(settings);	
			model.setDownloadModel(downloadModel);
			model.setIdentities(identities);
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SettingsClass.DISABLE_DOWNLOADS)) {
			updateDownloadStatus();
		}
	}

	/**
	 * 
	 */
	private void updateDownloadStatus() {
		boolean disableDownloads = settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS);
		mainFrame.setPanelEnabled("Search", !disableDownloads);
	}

	/**
	 * @return
	 */
	public FrostIdentities getIdentities() {
		return identities;
	}

	/**
	 * @return
	 */
	public String getKeypool() {
		return keypool;
	}

	/**
	 * @return
	 */
	public SettingsClass getSettings() {
		return settings;
	}

	/**
	 * @param model
	 */
	public void setUploadModel(UploadModel model) {
		uploadModel = model;
	}

}
