/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.search;

import java.beans.*;

import frost.*;
import frost.fileTransfer.download.DownloadTable;
import frost.fileTransfer.upload.UploadModel;
import frost.gui.TofTree;
import frost.gui.translation.UpdatingLanguageResource;
import frost.identities.FrostIdentities;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SearchManager implements PropertyChangeListener {

	private TofTree tofTree;
	private DownloadTable downloadTable;
	private UploadModel uploadModel;
	private String keypool;
	private FrostIdentities identities;
	private frame1 mainFrame;
	private SettingsClass settings;
	private UpdatingLanguageResource languageResource;

	private SearchPanel panel;
	private SearchTableModel tableModel;

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
	public void setMainFrame(frame1 newMainFrame) {
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
	 * @param table
	 */
	public void setDownloadTable(DownloadTable newDownloadTable) {
		downloadTable = newDownloadTable;		
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
			panel.setTableModel(getTableModel());
			panel.setDownloadTable(downloadTable);
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
	public SearchTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new SearchTableModel(languageResource);
		}
		return tableModel;
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
