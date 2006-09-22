/*
  UploadPanel.java / Frost
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

class UploadPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JLabel htlExplanationLabel = new JLabel();

    private JLabel htlLabel = new JLabel();

    private JTextField htlTextField = new JTextField(6);

    private JLabel maxRetriesLabel = new JLabel();
    private JTextField maxRetriesTextField = new JTextField(6);
    private JLabel waitTimeLabel = new JLabel();
    private JTextField waitTimeTextField = new JTextField(6);

    private JLabel splitfileThreadsExplanationLabel = new JLabel();
    private JLabel splitfileThreadsLabel = new JLabel();
    private JTextField splitfileThreadsTextField = new JTextField(6);
    private JLabel threadsLabel = new JLabel();
    private JTextField threadsTextField = new JTextField(6);

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected UploadPanel(SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
        
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            // disable 0.5-only items
            splitfileThreadsLabel.setEnabled(false);
            splitfileThreadsTextField.setEnabled(false);
            splitfileThreadsExplanationLabel.setEnabled(false);
            htlLabel.setEnabled(false);
            htlTextField.setEnabled(false);
            htlExplanationLabel.setEnabled(false);
        }
    }

    private void initialize() {
        setName("UploadPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(htlTextField, language);
        new TextComponentClipboardMenu(splitfileThreadsTextField, language);
        new TextComponentClipboardMenu(threadsTextField, language);
        new TextComponentClipboardMenu(maxRetriesTextField, language);
        new TextComponentClipboardMenu(waitTimeTextField, language);

        // Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();

        Insets insets0555 = new Insets(0, 5, 5, 5);

        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.insets = insets0555;
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        add(maxRetriesLabel, constraints);
        constraints.gridx = 1;
        add(maxRetriesTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(waitTimeLabel, constraints);
        constraints.gridx = 1;
        add(waitTimeTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(threadsLabel, constraints);
        constraints.gridx = 1;
        add(threadsTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(htlLabel, constraints);
        constraints.gridx = 1;
        add(htlTextField, constraints);
        constraints.gridx = 2;
        add(htlExplanationLabel, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(splitfileThreadsLabel, constraints);
        constraints.gridx = 1;
        add(splitfileThreadsTextField, constraints);
        constraints.gridx = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        add(splitfileThreadsExplanationLabel, constraints);
        
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
        htlTextField.setText(settings.getValue("htlUpload"));
        threadsTextField.setText(settings.getValue("uploadThreads"));
        splitfileThreadsTextField.setText(settings.getValue("splitfileUploadThreads"));
        maxRetriesTextField.setText("" + settings.getIntValue(SettingsClass.UPLOAD_MAX_RETRIES));
        waitTimeTextField.setText("" + settings.getIntValue(SettingsClass.UPLOAD_RETRIES_WAIT_TIME));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        String minutes = language.getString("Options.common.minutes");
        
        waitTimeLabel.setText(language.getString("Options.uploads.waittimeAfterEachTry") + " (" + minutes + "): ");
        maxRetriesLabel.setText(language.getString("Options.uploads.maximumNumberOfRetries") + ": ");
        htlLabel.setText(language.getString("Options.uploads.uploadHtl") + " (21)");
        htlExplanationLabel.setText(language.getString("Options.uploads.uploadHtlExplanation"));
        threadsLabel.setText(
                language.getString("Options.uploads.numberOfSimultaneousUploads") + " (3)");
        splitfileThreadsLabel.setText(
                language.getString("Options.uploads.numberOfSplitfileThreads") + " (15)");
        splitfileThreadsExplanationLabel.setText(language.getString("Options.uploads.numberOfSplitfileThreadsExplanation"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue("htlUpload", htlTextField.getText());
        settings.setValue("uploadThreads", threadsTextField.getText());
        settings.setValue("splitfileUploadThreads", splitfileThreadsTextField.getText());
        settings.setValue(SettingsClass.UPLOAD_MAX_RETRIES, maxRetriesTextField.getText());
        settings.setValue(SettingsClass.UPLOAD_RETRIES_WAIT_TIME, waitTimeTextField.getText());
    }
}
