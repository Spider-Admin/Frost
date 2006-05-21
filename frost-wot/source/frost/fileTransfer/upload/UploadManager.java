/*
  UploadManager.java / Frost
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
    private UploadStatusPanel statusPanel;

    private boolean freenetIsOnline;

    public UploadManager(SettingsClass settings) {
        super();
        this.settings = settings;
    }

    public void initialize() throws StorageException {
        mainFrame.addPanel("MainFrame.tabbedPane.uploads", getPanel());
        mainFrame.addStatusPanel(getStatusPanel(), 0);
        settings.addPropertyChangeListener(SettingsClass.DISABLE_REQUESTS, this);
        updateUploadStatus();
        getModel().initialize();
        if (freenetIsOnline) {
            getTicker().start();
        }
    }

    public void setTofTreeModel(TofTreeModel tofTreeModel) {
        this.tofTreeModel = tofTreeModel;
    }

    public void setMainFrame(MainFrame newMainFrame) {
        mainFrame = newMainFrame;
    }

    public void setFreenetIsOnline(boolean freenetIsOnline) {
        this.freenetIsOnline = freenetIsOnline;
    }

    public UploadPanel getPanel() {
        if (panel == null) {
            panel = new UploadPanel(settings);
            panel.setModel(getModel());
            panel.setTofTreeModel(tofTreeModel);
            panel.initialize();
        }
        return panel;
    }

    private UploadStatusPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new UploadStatusPanel(getTicker());
        }
        return statusPanel;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.DISABLE_REQUESTS)) {
            updateUploadStatus();
        }
    }

    private void updateUploadStatus() {
        boolean disableUploads = settings.getBoolValue(SettingsClass.DISABLE_REQUESTS);
        mainFrame.setPanelEnabled("MainFrame.tabbedPane.uploads", !disableUploads && freenetIsOnline);
    }

    private UploadTicker getTicker() {
        if (ticker == null) {
            ticker = new UploadTicker(settings, getModel(), getPanel(), myID);
        }
        return ticker;
    }

    public void setMyID(LocalIdentity identity) {
        myID = identity;
    }

    public UploadModel getModel() {
        if (model == null) {
            model = new UploadModel(settings);
        }
        return model;
    }
}
