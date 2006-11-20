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

import java.util.*;

import frost.*;
import frost.storage.*;

public class UploadManager {

    private UploadModel model;
    private UploadPanel panel;
    private UploadTicker ticker;
    private UploadStatusPanel statusPanel;

    public UploadManager() {
        super();
    }

    public void initialize(List sharedFiles) throws StorageException {
        getPanel();
        getStatusPanel();
        getModel().initialize(sharedFiles);
        if (Core.isFreenetOnline()) {
            getTicker().start();
        }
    }
    
    public void save() throws StorageException {
        getPanel().getTableFormat().saveTableLayout();
        getModel().save();
    }
    
    public void addPanelToMainFrame(MainFrame mainFrame) {
        mainFrame.addPanel("MainFrame.tabbedPane.uploads", getPanel());
        mainFrame.addStatusPanel(getStatusPanel(), 0);
    }

    public UploadPanel getPanel() {
        if (panel == null) {
            panel = new UploadPanel();
            panel.setModel(getModel());
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

    private UploadTicker getTicker() {
        if (ticker == null) {
            ticker = new UploadTicker(getModel(), getPanel());
        }
        return ticker;
    }

    public UploadModel getModel() {
        if (model == null) {
            model = new UploadModel();
        }
        return model;
    }
}
