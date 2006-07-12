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
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

import frost.SettingsClass;
import frost.util.gui.*;
import frost.util.gui.translation.Language;

class UploadPanel extends JPanel {

    private class Listener implements ChangeListener,ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == disableRequestsCheckBox) {
                refreshComponentsState();
            }
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == restartFailedUploadsCheckBox) {
                refreshComponentsState();
            }
        }
    }

    private SettingsClass settings = null;
    private Language language = null;

    private Listener listener = new Listener();

    private JCheckBox disableRequestsCheckBox = new JCheckBox();
    private JLabel htlExplanationLabel = new JLabel();

    private JLabel htlLabel = new JLabel();

    private JTextField htlTextField = new JTextField(8);

    private JCheckBox restartFailedUploadsCheckBox = new JCheckBox();
    private JLabel maxRetriesLabel = new JLabel();
    private JTextField maxRetriesTextField = new JTextField(8);
    private JLabel waitTimeLabel = new JLabel();
    private JTextField waitTimeTextField = new JTextField(8);

    private JLabel splitfileThreadsExplanationLabel = new JLabel();
    private JLabel splitfileThreadsLabel = new JLabel();
    private JTextField splitfileThreadsTextField = new JTextField(8);
    private JLabel threadsLabel = new JLabel();
    private JTextField threadsTextField = new JTextField(8);

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected UploadPanel(SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private JPanel getRetriesPanel() {
        JPanel subPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        subPanel.add(maxRetriesLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0;
        subPanel.add(maxRetriesTextField, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.weightx = 1;
        subPanel.add(waitTimeLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0;
        subPanel.add(waitTimeTextField, constraints);

        return subPanel;
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

        //Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        Insets insets0555 = new Insets(0, 5, 5, 5);
        Insets insets5555 = new Insets(5, 5, 5, 5);
        Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);

        constraints.weightx = 1;
        constraints.gridwidth = 3;
        constraints.insets = insets0555;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(disableRequestsCheckBox, constraints);

        constraints.insets = insets5_30_5_5;
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridwidth = 1;
        add(restartFailedUploadsCheckBox, constraints);
        constraints.gridwidth = 3;
        constraints.insets = insets5555;
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(getRetriesPanel(), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.insets = insets5_30_5_5;
//        constraints.gridx = 0;
//        constraints.gridy = 2;
//        constraints.gridwidth = 2;
////        add(automaticIndexingCheckBox, constraints);
//        constraints.insets = insets5555;
//        constraints.gridx = 2;
//        constraints.gridwidth = 1;
////        add(shareDownloadsCheckBox, constraints);

//        constraints.insets = insets5_30_5_5;
//        constraints.gridx = 0;
//        constraints.gridy = 3;
//        constraints.gridwidth = 2;
////        add(signUploadsCheckBox, constraints);
//        constraints.insets = insets5555;
//        constraints.gridx = 2;
//        constraints.gridwidth = 1;
////        add(helpFriendsCheckBox, constraints);

        constraints.insets = insets5_30_5_5;
        constraints.gridx = 0;
        constraints.gridy++;
        add(htlLabel, constraints);
        constraints.insets = insets5555;
        constraints.gridx = 1;
        add(htlTextField, constraints);
        constraints.gridx = 2;
        add(htlExplanationLabel, constraints);

        constraints.insets = insets5_30_5_5;
        constraints.gridx = 0;
        constraints.gridy++;
        add(threadsLabel, constraints);
        constraints.insets = insets5555;
        constraints.gridx = 1;
        add(threadsTextField, constraints);

        constraints.insets = insets5_30_5_5;
        constraints.gridx = 0;
        constraints.gridy++;
        add(splitfileThreadsLabel, constraints);
        constraints.insets = insets5555;
        constraints.gridx = 1;
        add(splitfileThreadsTextField, constraints);
        constraints.gridx = 2;
        constraints.weighty = 1; 
        add(splitfileThreadsExplanationLabel, constraints);

        // Add listeners
        disableRequestsCheckBox.addActionListener(listener);
        restartFailedUploadsCheckBox.addChangeListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        htlTextField.setText(settings.getValue("htlUpload"));
        threadsTextField.setText(settings.getValue("uploadThreads"));
        splitfileThreadsTextField.setText(settings.getValue("splitfileUploadThreads"));
        disableRequestsCheckBox.setSelected(settings.getBoolValue(SettingsClass.DISABLE_REQUESTS));
        restartFailedUploadsCheckBox.setSelected(settings.getBoolValue(SettingsClass.RESTART_FAILED_UPLOADS));
        maxRetriesTextField.setText("" + settings.getIntValue(SettingsClass.UPLOAD_MAX_RETRIES));
        waitTimeTextField.setText("" + settings.getIntValue(SettingsClass.UPLOAD_RETRIES_WAIT_TIME));

        refreshComponentsState();
    }

    private void refreshComponentsState() {
        boolean uploadsEnabled = !disableRequestsCheckBox.isSelected();
        if (uploadsEnabled) {
            setEnabled(true);
            maxRetriesTextField.setEnabled(restartFailedUploadsCheckBox.isSelected());
            waitTimeTextField.setEnabled(restartFailedUploadsCheckBox.isSelected());
            maxRetriesLabel.setEnabled(restartFailedUploadsCheckBox.isSelected());
            waitTimeLabel.setEnabled(restartFailedUploadsCheckBox.isSelected());
        } else {
            setEnabled(false);
        }
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        String minutes = language.getString("Options.common.minutes");
        
        disableRequestsCheckBox.setText(language.getString("Options.uploads.disableUploads"));
        restartFailedUploadsCheckBox.setText(language.getString("Options.uploads.restartFailedUploads"));
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
        settings.setValue(SettingsClass.DISABLE_REQUESTS, disableRequestsCheckBox.isSelected());
        settings.setValue(SettingsClass.RESTART_FAILED_UPLOADS, restartFailedUploadsCheckBox.isSelected());
        settings.setValue(SettingsClass.UPLOAD_MAX_RETRIES, maxRetriesTextField.getText());
        settings.setValue(SettingsClass.UPLOAD_RETRIES_WAIT_TIME, waitTimeTextField.getText());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        ArrayList exceptions = new ArrayList();
        exceptions.add(disableRequestsCheckBox);
        MiscToolkit.getInstance().setContainerEnabled(this, enabled, exceptions);
    }
}
