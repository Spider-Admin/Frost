/*
  DownloadPanel.java / Frost
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
import java.io.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class DownloadPanel extends JPanel {

    public class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == browseDirectoryButton) {
                browseDirectoryPressed();
            }
        }
    }

    private JDialog owner = null;
    private SettingsClass settings = null;
    private Language language = null;

    // 0.5 only
    private final JLabel splitfileThreadsLabel = new JLabel();
    private final JTextField splitfileThreadsTextField = new JTextField(6);
    private final JCheckBox decodeAfterEachSegmentCheckBox = new JCheckBox();
    private final JCheckBox tryAllSegmentsCheckBox = new JCheckBox();

    // 0.7 only
    private final JLabel priorityLabel = new JLabel();
    private final JTextField priorityTextField = new JTextField(6);

    // common
    private final JButton browseDirectoryButton = new JButton();
    private final JLabel directoryLabel = new JLabel();

    private final JTextField directoryTextField = new JTextField(20);

    private final Listener listener = new Listener();
    private final JLabel maxRetriesLabel = new JLabel();
    private final JTextField maxRetriesTextField = new JTextField(6);
    private final JTextField threadsTextField = new JTextField(6);
    private final JLabel threadsTextLabel = new JLabel();

    private final JCheckBox logDownloadsCheckBox = new JCheckBox();

    private final JLabel waitTimeLabel = new JLabel();
    private final JTextField waitTimeTextField = new JTextField(6);

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected DownloadPanel(final JDialog owner, final SettingsClass settings) {
        super();

        this.owner = owner;
        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();

        if( FcpHandler.isFreenet07() ) {
            // disable 0.5-only items
            splitfileThreadsLabel.setEnabled(false);
            splitfileThreadsTextField.setEnabled(false);
            tryAllSegmentsCheckBox.setEnabled(false);
            decodeAfterEachSegmentCheckBox.setEnabled(false);
        }
    }

    /**
     * browseDownloadDirectoryButton Action Listener (Downloads / Browse)
     */
    private void browseDirectoryPressed() {
        final JFileChooser fc = new JFileChooser(settings.getValue(SettingsClass.DIR_LAST_USED));
        fc.setDialogTitle(language.getString("Options.downloads.filechooser.title"));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        final int returnVal = fc.showOpenDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final String fileSeparator = System.getProperty("file.separator");
            final File file = fc.getSelectedFile();
            settings.setValue(SettingsClass.DIR_LAST_USED, file.getParent());
            directoryTextField.setText(file.getPath() + fileSeparator);
        }
    }

    private void initialize() {
        setName("DownloadPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        //We create the components
        new TextComponentClipboardMenu(directoryTextField, language);
        new TextComponentClipboardMenu(maxRetriesTextField, language);
        new TextComponentClipboardMenu(splitfileThreadsTextField, language);
        new TextComponentClipboardMenu(threadsTextField, language);
        new TextComponentClipboardMenu(waitTimeTextField, language);

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        final Insets insets0555 = new Insets(0, 5, 5, 5);

        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.insets = insets0555;
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        add(directoryLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(directoryTextField, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        add(browseDirectoryButton, constraints);

        constraints.gridy++;
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
        add(threadsTextLabel, constraints);
        constraints.gridx = 1;
        add(threadsTextField, constraints);

        if( FcpHandler.isFreenet07() ) {
            constraints.gridy++;
            constraints.gridx = 0;
            add(priorityLabel, constraints);
            constraints.gridx = 1;
            add(priorityTextField, constraints);
        } else {
            constraints.gridy++;
            constraints.gridx = 0;
            add(splitfileThreadsLabel, constraints);
            constraints.gridx = 1;
            add(splitfileThreadsTextField, constraints);

            constraints.gridwidth = 3;

            constraints.insets = new Insets(5,5,5,5);
            constraints.gridy++;
            constraints.gridx = 0;
            add(tryAllSegmentsCheckBox, constraints);

            constraints.insets = insets0555;

            constraints.gridy++;
            add(decodeAfterEachSegmentCheckBox, constraints);
        }

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.insets = insets0555;
        constraints.weighty = 1.0; // glue
        constraints.anchor = GridBagConstraints.NORTHWEST;
        add(logDownloadsCheckBox, constraints);

        // Add listeners
        browseDirectoryButton.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        directoryTextField.setText(settings.getValue(SettingsClass.DIR_DOWNLOAD));
        threadsTextField.setText(settings.getValue(SettingsClass.DOWNLOAD_MAX_THREADS));
        maxRetriesTextField.setText("" + settings.getIntValue(SettingsClass.DOWNLOAD_MAX_RETRIES));
        waitTimeTextField.setText("" + settings.getIntValue(SettingsClass.DOWNLOAD_WAITTIME));
        logDownloadsCheckBox.setSelected(settings.getBoolValue(SettingsClass.LOG_DOWNLOADS_ENABLED));
        if( FcpHandler.isFreenet07() ) {
            priorityTextField.setText(settings.getValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD));
        } else {
            splitfileThreadsTextField.setText(settings.getValue(SettingsClass.DOWNLOAD_MAX_SPLITFILE_THREADS));
            tryAllSegmentsCheckBox.setSelected(settings.getBoolValue(SettingsClass.DOWNLOAD_TRY_ALL_SEGMENTS));
            decodeAfterEachSegmentCheckBox.setSelected(settings.getBoolValue(SettingsClass.DOWNLOAD_DECODE_AFTER_EACH_SEGMENT));
        }
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        final String on = language.getString("Options.common.on");
        final String minutes = language.getString("Options.common.minutes");

        waitTimeLabel.setText(language.getString("Options.downloads.waittimeAfterEachTry") + " (" + minutes + ")");
        maxRetriesLabel.setText(language.getString("Options.downloads.maximumNumberOfRetries"));

        logDownloadsCheckBox.setText(language.getString("Options.downloads.logDownloads"));

        directoryLabel.setText(language.getString("Options.downloads.downloadDirectory"));
        browseDirectoryButton.setText(language.getString("Common.browse") + "...");
        threadsTextLabel.setText(language.getString("Options.downloads.numberOfSimultaneousDownloads") + " (3)");
        if( FcpHandler.isFreenet07() ) {
            priorityLabel.setText(language.getString("Options.downloads.downloadPriority") + " (3)");
        } else {
            splitfileThreadsLabel.setText(language.getString("Options.downloads.numberOfSplitfileThreads") + " (30)");
            tryAllSegmentsCheckBox.setText(language.getString("Options.downloads.tryToDownloadAllSegments") + " (" + on + ")");
            decodeAfterEachSegmentCheckBox.setText(language.getString("Options.downloads.decodeEachSegmentImmediately"));
        }
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        final String downlDirTxt = directoryTextField.getText();
        final String filesep = System.getProperty("file.separator");
        // always append a fileseparator to the end of string
        if ((!(downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)))
            || downlDirTxt.lastIndexOf(filesep) < 0) {
                settings.setValue(SettingsClass.DIR_DOWNLOAD, downlDirTxt + filesep);
        } else {
            settings.setValue(SettingsClass.DIR_DOWNLOAD, downlDirTxt);
        }
        settings.setValue(SettingsClass.DOWNLOAD_MAX_THREADS, threadsTextField.getText());

        settings.setValue(SettingsClass.DOWNLOAD_MAX_RETRIES, maxRetriesTextField.getText());
        settings.setValue(SettingsClass.DOWNLOAD_WAITTIME, waitTimeTextField.getText());

        settings.setValue(SettingsClass.LOG_DOWNLOADS_ENABLED, logDownloadsCheckBox.isSelected());
        if( FcpHandler.isFreenet07() ) {
            settings.setValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD, priorityTextField.getText());
        } else {
            settings.setValue(SettingsClass.DOWNLOAD_MAX_SPLITFILE_THREADS, splitfileThreadsTextField.getText());
            settings.setValue(SettingsClass.DOWNLOAD_TRY_ALL_SEGMENTS, tryAllSegmentsCheckBox.isSelected());
            settings.setValue(SettingsClass.DOWNLOAD_DECODE_AFTER_EACH_SEGMENT, decodeAfterEachSegmentCheckBox.isSelected());
        }
    }
}
