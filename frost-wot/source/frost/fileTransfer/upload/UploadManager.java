/*
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.upload;

import java.beans.*;

import frost.*;
import frost.boards.TofTreeModel;
import frost.identities.LocalIdentity;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class UploadManager implements PropertyChangeListener {

	private LocalIdentity myID;
	private TofTreeModel tofTreeModel;
	private MainFrame mainFrame; 
	private SettingsClass settings;
	
	private UploadModel model;
	private UploadPanel panel;
	private UploadTicker ticker;

	private boolean freenetIsOnline;

	/**
	 * @param settings
	 */
	public UploadManager(SettingsClass settings) {
		super();
		this.settings = settings;
	}
	
	/**
	 * 
	 */
	public void initialize() throws StorageException {
		mainFrame.addPanel("Uploads", getPanel());
		settings.addPropertyChangeListener(SettingsClass.DISABLE_REQUESTS, this);
		updateUploadStatus();
		getModel().initialize();
		if (freenetIsOnline) {
			getTicker().start();
		}
	}
	
	/**
	 * @param tofTreeModel
	 */
	public void setTofTreeModel(TofTreeModel tofTreeModel) {
		this.tofTreeModel = tofTreeModel;
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
			panel.setTofTreeModel(tofTreeModel);
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
