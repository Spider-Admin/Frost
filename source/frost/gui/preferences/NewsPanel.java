/*
  NewsPanel.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui.preferences;

import java.awt.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class NewsPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JLabel uploadHtlLabel = new JLabel();
    private JLabel downloadHtlLabel = new JLabel();
    private JLabel displayDaysLabel = new JLabel();
    private JLabel downloadDaysLabel = new JLabel();
    private JLabel messageBaseLabel = new JLabel();

    private JTextField uploadHtlTextField = new JTextField(8);
    private JTextField downloadHtlTextField = new JTextField(8);
    private JTextField displayDaysTextField = new JTextField(8);
    private JTextField downloadDaysTextField = new JTextField(8);
    private JTextField messageBaseTextField = new JTextField(16);
    
    private JCheckBox alwaysDownloadBackloadCheckBox = new JCheckBox();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected NewsPanel(SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
        
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            // disable 0.5-only items
            uploadHtlLabel.setEnabled(false);
            uploadHtlTextField.setEnabled(false);
            downloadHtlLabel.setEnabled(false);
            downloadHtlTextField.setEnabled(false);
        }
    }

    private void initialize() {
        setName("NewsPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(uploadHtlTextField, language);
        new TextComponentClipboardMenu(downloadHtlTextField, language);
        new TextComponentClipboardMenu(displayDaysTextField, language);
        new TextComponentClipboardMenu(downloadDaysTextField, language);
        new TextComponentClipboardMenu(messageBaseTextField, language);

        // Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weighty = 0.0;
        constraints.weightx = 0;

        constraints.insets = new Insets(0, 5, 5, 5);
        constraints.gridy = 0;
        
        constraints.gridx = 0;
        add(displayDaysLabel, constraints);
        constraints.gridx = 1;
        add(displayDaysTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        add(downloadDaysLabel, constraints);
        constraints.gridx = 1;
        add(downloadDaysTextField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth=2;
        add(alwaysDownloadBackloadCheckBox, constraints);
        constraints.gridwidth=1;

        constraints.gridx = 0;
        constraints.gridy++;
        add(messageBaseLabel, constraints);
        constraints.gridx = 1;
        add(messageBaseTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(uploadHtlLabel, constraints);
        constraints.gridx = 1;
        add(uploadHtlTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(downloadHtlLabel, constraints);
        constraints.gridx = 1;
        add(downloadHtlTextField, constraints);

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        uploadHtlTextField.setText(settings.getValue(SettingsClass.MESSAGE_UPLOAD_HTL));
        downloadHtlTextField.setText(settings.getValue(SettingsClass.MESSAGE_DOWNLOAD_HTL));
        displayDaysTextField.setText(settings.getValue(SettingsClass.MAX_MESSAGE_DISPLAY));
        downloadDaysTextField.setText(settings.getValue(SettingsClass.MAX_MESSAGE_DOWNLOAD));
        messageBaseTextField.setText(settings.getValue(SettingsClass.MESSAGE_BASE));
        alwaysDownloadBackloadCheckBox.setSelected(settings.getBoolValue(SettingsClass.ALWAYS_DOWNLOAD_MESSAGES_BACKLOAD));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        uploadHtlLabel.setText(language.getString("Options.news.1.messageUploadHtl") + " (21)");
        downloadHtlLabel.setText(language.getString("Options.news.1.messageDownloadHtl") + " (23)");
        displayDaysLabel.setText(language.getString("Options.news.1.numberOfDaysToDisplay") + " (15)");
        downloadDaysLabel.setText(language.getString("Options.news.1.numberOfDaysToDownloadBackwards") + " (5)");
        messageBaseLabel.setText(language.getString("Options.news.1.messageBase") + " (news)");
        alwaysDownloadBackloadCheckBox.setText(language.getString("Options.news.1.alwaysDownloadBackload"));
        alwaysDownloadBackloadCheckBox.setToolTipText(language.getString("Options.news.1.alwaysDownloadBackload.tooltip"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.MESSAGE_UPLOAD_HTL, uploadHtlTextField.getText());
        settings.setValue(SettingsClass.MESSAGE_DOWNLOAD_HTL, downloadHtlTextField.getText());
        settings.setValue(SettingsClass.MAX_MESSAGE_DISPLAY, displayDaysTextField.getText());
        settings.setValue(SettingsClass.MAX_MESSAGE_DOWNLOAD, downloadDaysTextField.getText());
        settings.setValue(SettingsClass.MESSAGE_BASE, messageBaseTextField.getText().trim().toLowerCase());
        settings.setValue(SettingsClass.ALWAYS_DOWNLOAD_MESSAGES_BACKLOAD, alwaysDownloadBackloadCheckBox.isSelected());
    }
}
