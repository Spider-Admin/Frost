/*
  UploadStatusPanel.java / Frost
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

import java.awt.FlowLayout;

import javax.swing.*;

import frost.util.gui.translation.*;

public class UploadStatusPanel extends JPanel {

    private class Listener implements UploadTickerListener, LanguageListener {
        public void uploadingCountChanged() {
            numberChanged();
        }
        public void generatingCountChanged() {
            numberChanged();
        }
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    }

    private UploadTicker ticker;
    private Language language;

    private JLabel uploadingLabel = new JLabel();
    private JLabel countLabel = new JLabel();
    private JLabel filesLabel = new JLabel();

    int count = 0;

    private Listener listener = new Listener();

    public UploadStatusPanel(UploadTicker ticker) {
        super();
        this.ticker = ticker;
        language = Language.getInstance();
        initialize();
    }

    private void initialize() {
        refreshLanguage();
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));

        // Init count
        count = ticker.getRunningUploadingThreads();
        countLabel.setText("" + count);

        // Add components
        add(uploadingLabel);
        add(countLabel);
        add(filesLabel);

        // Add listeners
        ticker.addUploadTickerListener(listener);
        language.addLanguageListener(listener);
    }

    private void refreshLanguage() {
        uploadingLabel.setText(language.getString("MainFrame.statusBar.uploading")+": ");
        if (count == 1) {
            filesLabel.setText(language.getString("MainFrame.statusBar.file"));
        } else {
            filesLabel.setText(language.getString("MainFrame.statusBar.files"));
        }
    }

    private void numberChanged() {
        count = ticker.getRunningUploadingThreads();
        countLabel.setText("" + count);
        refreshLanguage();
    }
}
