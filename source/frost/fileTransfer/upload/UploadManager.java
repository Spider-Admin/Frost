/*
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.upload;

import java.beans.*;

import frost.*;
import frost.gui.TofTree;
import frost.identities.LocalIdentity;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UploadManager implements PropertyChangeListener {

	private LocalIdentity myID;
	private TofTree tofTree;
	private MainFrame mainFrame; 
	private SettingsClass settings;
	private UpdatingLanguageResource languageResource;
	
	private UploadModel model;
	private UploadPanel panel;
	private UploadTicker ticker;

	private boolean freenetIsOnline;

	/**
	 * 
	 */
	public UploadManager(UpdatingLanguageResource newLanguageResource, SettingsClass newSettings) {
		super();
		languageResource = newLanguageResource;
		settings = newSettings;
	}
	/**
	 * 
	 */
	public void initialize() {
		mainFrame.addPanel("Uploads", getPanel());
		settings.addPropertyChangeListener(SettingsClass.DISABLE_REQUESTS, this);
		updateUploadStatus();
		getModel().load();
		if (freenetIsOnline) {
			getTicker().start();
		}
	}
	
	/**
	 * @param tree
	 */
	public void setTofTree(TofTree newTofTree) {
		tofTree = newTofTree;
	}
	
	/**
	 * @param mainFrame
	 */
	public void setMainFrame(MainFrame newMainFrame) {
		mainFrame = newMainFrame;	
	}
	
	/**
	 * description
	 * 
	 * @param freenetIsOnline description
	 */
	public void setFreenetIsOnline(boolean freenetIsOnline) {
		this.freenetIsOnline = freenetIsOnline;
	}
	
	/**
	 * @return
	 */
	public UploadPanel getPanel() {
		if (panel == null) {
			panel = new UploadPanel(settings);
			panel.setModel(getModel());
			panel.setTofTree(tofTree);
			panel.setLanguageResource(languageResource);
			panel.initialize();
		}
		return panel;
	}
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SettingsClass.DISABLE_REQUESTS)) {
			updateUploadStatus();
		}
	}
	
	/**
	 * 
	 */
	private void updateUploadStatus() {
		boolean disableUploads = settings.getBoolValue(SettingsClass.DISABLE_REQUESTS);
		mainFrame.setPanelEnabled("Uploads", !disableUploads && freenetIsOnline);
	}
	
	/**
	 * @return
	 */
	public UploadTicker getTicker() {
		if (ticker == null) {
			ticker = new UploadTicker(settings, getModel(), getPanel(), myID);
		}
		return ticker;
	}

	/**
	 * @param identity
	 */
	public void setMyID(LocalIdentity identity) {
		myID = identity;
	}

	/**
	 * @return
	 */
	public UploadModel getModel() {
		if (model == null) {
			model = new UploadModel(settings);	
		}
		return model;
	}

}
