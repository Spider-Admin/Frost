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
import frost.gui.translation.UpdatingLanguageResource;
import frost.identities.LocalIdentity;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UploadManager implements PropertyChangeListener {

	private LocalIdentity myID;
	private TofTree tofTree;
	private frame1 mainFrame; 
	private SettingsClass settings;
	private UpdatingLanguageResource languageResource;
	
	private UploadTable table;
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
		getTable().load();
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
	public void setMainFrame(frame1 newMainFrame) {
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
	public UploadTable getTable() {
		if (table == null) { 
			UploadTableModel uploadTableModel = new UploadTableModel(languageResource);
			table = new UploadTable(uploadTableModel);
		}
		return table;
	}
	
	/**
	 * @return
	 */
	public UploadPanel getPanel() {
		if (panel == null) {
			panel = new UploadPanel(settings);
			panel.setUploadTable(getTable());
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
			ticker = new UploadTicker(settings, getTable(), getPanel(), myID);
		}
		return ticker;
	}

	/**
	 * @param identity
	 */
	public void setMyID(LocalIdentity identity) {
		myID = identity;
	}

}
