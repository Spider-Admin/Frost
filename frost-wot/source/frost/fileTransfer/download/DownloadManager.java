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

import frost.*;
import frost.storage.*;

public class DownloadManager {

	private DownloadModel model;
	private DownloadPanel panel;
	private DownloadTicker ticker;
	private DownloadStatusPanel statusPanel;

	public DownloadManager() {
		super();
	}
	
	public void initialize() throws StorageException {
        getPanel();
        getStatusPanel();
		getModel().initialize();
		if (Core.isFreenetOnline()) {
			getTicker().start();
		}
	}
    
    public void addPanelToMainFrame(MainFrame mainFrame) {
        mainFrame.addPanel("MainFrame.tabbedPane.downloads", getPanel());
        mainFrame.addStatusPanel(getStatusPanel(), 0);
    }
	
	private DownloadStatusPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new DownloadStatusPanel(getTicker());
		}
		return statusPanel;
	}

	public DownloadPanel getPanel() {
		if (panel == null) {
			panel = new DownloadPanel();
			panel.setModel(getModel());
			panel.initialize();
		}
		return panel;
	}
	
	public DownloadModel getModel() {
		if (model == null) {
			model = new DownloadModel();	
		}
		return model;
	}
	
	private DownloadTicker getTicker() {
		if (ticker == null) {
			ticker = new DownloadTicker(getModel(), getPanel());
		}
		return ticker;
	}
}
