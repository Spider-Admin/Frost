/*
  DownloadManager.java / Frost

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.fileTransfer.download;

import java.beans.*;

import frost.*;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class DownloadManager implements PropertyChangeListener {

	private MainFrame mainFrame; 
	private SettingsClass settings;
	
	private DownloadModel model;
	private DownloadPanel panel;
	private DownloadTicker ticker;
	private DownloadStatusPanel statusPanel;

	private boolean freenetIsOnline;

	/**
	 * 
	 */
	public DownloadManager(SettingsClass newSettings) {

		super();
		settings = newSettings;
	}
	
	/**
	 * @param mainFrame
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;	
	}

	public void initialize() throws StorageException {
		mainFrame.addPanel("MainFrame.tabbedPane.downloads", getPanel());
		mainFrame.addStatusPanel(getStatusPanel(), 0);
		settings.addPropertyChangeListener(SettingsClass.DISABLE_DOWNLOADS, this);
		updateDownloadStatus();
		getModel().initialize();
		if (freenetIsOnline) {
			getTicker().start();
		}
	}
	
	/**
	 * @return
	 */
	private DownloadStatusPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new DownloadStatusPanel(getTicker());
		}
		return statusPanel;
	}

	private void updateDownloadStatus() {
		boolean disableDownloads = settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS);
		mainFrame.setPanelEnabled("Downloads", !disableDownloads && freenetIsOnline);
	}
	
	/**
	 * @return
	 */
	public DownloadPanel getPanel() {
		if (panel == null) {
			panel = new DownloadPanel(settings);
			panel.setModel(getModel());
			panel.initialize();
		}
		return panel;
	}
	
	/**
	 * @return
	 */
	public DownloadModel getModel() {
		if (model == null) {
			model = new DownloadModel(settings);	
		}
		return model;
	}
	
	/**
	 * @return
	 */
	private DownloadTicker getTicker() {
		if (ticker == null) {
			ticker = new DownloadTicker(settings, getModel(), getPanel());
		}
		return ticker;
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
	 * description
	 * 
	 * @param freenetIsOnline description
	 */
	public void setFreenetIsOnline(boolean freenetIsOnline) {
		this.freenetIsOnline = freenetIsOnline;
	}
}
