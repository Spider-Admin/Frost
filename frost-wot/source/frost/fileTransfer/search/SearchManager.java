/*
  SearchManager.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

    public SearchManager(SettingsClass settings) {
        super();
        this.settings = settings;
    }

    public void initialize() {
        mainFrame.addPanel("MainFrame.tabbedPane.search", getPanel());
        settings.addPropertyChangeListener(SettingsClass.DISABLE_DOWNLOADS, this);
        updateDownloadStatus();
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void setIdentities(FrostIdentities identities) {
        this.identities = identities;
    }

    public void setKeypool(String keypool) {
        this.keypool = keypool;
    }

    public void setDownloadModel(DownloadModel model) {
        downloadModel = model;
    }

    public void setTofTreeModel(TofTreeModel tofTreeModel) {
        this.tofTreeModel = tofTreeModel;
    }

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

    private void updateDownloadStatus() {
        boolean disableDownloads = settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS);
        mainFrame.setPanelEnabled("MainFrame.tabbedPane.search", !disableDownloads);
    }

    public FrostIdentities getIdentities() {
        return identities;
    }

    public String getKeypool() {
        return keypool;
    }

    public SettingsClass getSettings() {
        return settings;
    }

    public void setUploadModel(UploadModel model) {
        uploadModel = model;
    }
}
