/*
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.download;

import java.beans.*;

import frost.*;
import frost.gui.translation.UpdatingLanguageResource;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DownloadManager implements PropertyChangeListener {

	private frame1 mainFrame;
	private SettingsClass settings;
	private UpdatingLanguageResource languageResource;

	/**
	 * 
	 */
	public DownloadManager(
		UpdatingLanguageResource newLanguageResource,
		SettingsClass newSettings) {

		super();
		languageResource = newLanguageResource;
		settings = newSettings;
	}
	
	/**
	 * @param mainFrame
	 */
	public void setMainFrame(frame1 newMainFrame) {
		mainFrame = newMainFrame;	
	}

	/**
	 * 
	 */
	public void initialize() {
		//mainFrame.addPanel("Download", getPanel());	
		settings.addPropertyChangeListener(SettingsClass.DISABLE_DOWNLOADS, this);	
		updateDownloadStatus();
	}

	/**
	 * 
	 */
	private void updateDownloadStatus() {
		boolean disableDownloads = settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS);
		//mainFrame.setPanelEnabled("Download", !disableDownloads);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SettingsClass.DISABLE_DOWNLOADS)) {
			updateDownloadStatus();
		}
	}

}
