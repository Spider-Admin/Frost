/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.search;

import java.beans.*;

import frost.*;
import frost.fileTransfer.download.DownloadModel;
import frost.fileTransfer.upload.UploadModel;
import frost.gui.TofTree;
import frost.identities.FrostIdentities;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SearchManager implements PropertyChangeListener {

	private TofTree tofTree;
	private DownloadModel downloadModel;
	private UploadModel uploadModel;
	private String keypool;
	private FrostIdentities identities;
	private MainFrame mainFrame;
	private SettingsClass settings;
	private UpdatingLanguageResource languageResource;

	private SearchModel model;
	private SearchPanel panel;

	/**
	 * @param languageResource
	 * @param frostSettings
	 */
	public SearchManager(UpdatingLanguageResource newLanguageResource, SettingsClass newSettings) {
		super();
		languageResource = newLanguageResource;
		settings = newSettings;
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
	public void setMainFrame(MainFrame newMainFrame) {
		mainFrame = newMainFrame;	
	}

	/**
	 * @param identities
	 */
	public void setIdentities(FrostIdentities newIdentities) {
		identities = newIdentities;		
	}

	/**
	 * @param string
	 */
	public void setKeypool(String newKeypool) {
		keypool = newKeypool;		
	}

	/**
	 * @param model
	 */
	public void setDownloadModel(DownloadModel model) {
		downloadModel = model;		
	}

	/**
	 * @param tree
	 */
	public void setTofTree(TofTree newTofTree) {
		tofTree = newTofTree;
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
			panel.setTofTree(tofTree);
			panel.setKeypool(keypool);
			panel.setLanguageResource(languageResource);
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
