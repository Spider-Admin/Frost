/*
  MiscPanel.java / Frost
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

import javax.swing.*;
import javax.swing.event.*;

import frost.*;
import frost.fcp.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class MiscPanel extends JPanel {

    private class Listener implements ChangeListener, ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == enableLoggingCheckBox) {
                refreshLoggingState();
            }
        }
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == altEditCheckBox) {
                altEditChanged();
            }
        }
    }

    private SettingsClass settings = null;
    private Language language = null;

    private JCheckBox altEditCheckBox = new JCheckBox();
    private JTextField altEditTextField = new JTextField();
    private JLabel autoSaveIntervalLabel = new JLabel();
    private JTextField autoSaveIntervalTextField = new JTextField(8);
    private JLabel availableNodesLabel1 = new JLabel();
    private JTextField availableNodesTextField = new JTextField();
    
    private JCheckBox useDDACheckBox = new JCheckBox();
    private JCheckBox usePersistenceCheckBox = new JCheckBox();
    private JCheckBox enableLoggingCheckBox = new JCheckBox();
    
    private Listener listener = new Listener();
    private JLabel logFileSizeLabel = new JLabel();
    private JTextField logFileSizeTextField = new JTextField(8);

    private JTranslatableComboBox logLevelComboBox = null;
    private JLabel logLevelLabel = new JLabel();
    private JCheckBox showSystrayIconCheckBox = new JCheckBox();
    private JCheckBox minimizeToSystrayCheckBox = new JCheckBox();
    private JCheckBox splashScreenCheckBox = new JCheckBox();
//    private JCheckBox compactDatabaseAtNextStartupCheckBox = new JCheckBox();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected MiscPanel(SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
        
        if( FcpHandler.isFreenet05() ) {
            // disable 0.7-only items
            useDDACheckBox.setEnabled(false);
            usePersistenceCheckBox.setEnabled(false);
        }
    }

    private void altEditChanged() {
        altEditTextField.setEnabled(altEditCheckBox.isSelected());
    }

    private JPanel getLoggingPanel() {
        JPanel subPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        Insets insets5055 = new Insets(5, 0, 5, 5);
        Insets insets5_30_5_0 = new Insets(5, 30, 5, 0);
        Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
        constraints.insets = insets5055;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        
        subPanel.add(enableLoggingCheckBox, constraints);

        constraints.insets = insets5_30_5_5;
        constraints.gridwidth = 1;
        constraints.gridy++;
        subPanel.add(logLevelLabel, constraints);
        constraints.gridx = 1;
        String[] searchComboBoxKeys =
            { Logging.VERY_LOW, Logging.LOW, Logging.MEDIUM, Logging.HIGH, Logging.VERY_HIGH };
        logLevelComboBox = new JTranslatableComboBox(language, searchComboBoxKeys);
        subPanel.add(logLevelComboBox, constraints);

        constraints.gridx = 2;
        subPanel.add(logFileSizeLabel, constraints);
        constraints.insets = insets5_30_5_0;
        constraints.gridx = 3;
        constraints.weightx = 0;
        subPanel.add(logFileSizeTextField, constraints);

        return subPanel;
    }

    private void initialize() {
        setName("MiscPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(altEditTextField, language);
        new TextComponentClipboardMenu(autoSaveIntervalTextField, language);
        new TextComponentClipboardMenu(availableNodesTextField, language);
        new TextComponentClipboardMenu(logFileSizeTextField, language);

        // Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        Insets insets5555 = new Insets(5, 5, 5, 5);

        constraints.weightx = 0;
        constraints.insets = insets5555;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        add(availableNodesLabel1, constraints);
        constraints.gridy++;
        constraints.weightx = 1;
        add(availableNodesTextField, constraints);

        constraints.weightx = 0;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy++;
        add(useDDACheckBox, constraints);

        constraints.weightx = 0;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy++;
        add(usePersistenceCheckBox, constraints);

        constraints.weightx = 0;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy++;
        add(altEditCheckBox, constraints);
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        add(altEditTextField, constraints);

        constraints.weightx = 0;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;
        add(autoSaveIntervalLabel, constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.NONE;
        add(autoSaveIntervalTextField, constraints);

        constraints.weightx = 0;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy++;
        add(splashScreenCheckBox, constraints);
        constraints.gridy++;
        add(showSystrayIconCheckBox, constraints);
        constraints.gridy++;
        add(minimizeToSystrayCheckBox, constraints);
//        constraints.gridy++;
//        add(compactDatabaseAtNextStartupCheckBox, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 3;
        add(getLoggingPanel(), constraints);
        
        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        // Add listeners
        enableLoggingCheckBox.addActionListener(listener);
        altEditCheckBox.addChangeListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        altEditCheckBox.setSelected(settings.getBoolValue(SettingsClass.ALTERNATE_EDITOR_ENABLED));
        altEditTextField.setEnabled(altEditCheckBox.isSelected());
        showSystrayIconCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_SYSTRAY_ICON));
        minimizeToSystrayCheckBox.setSelected(settings.getBoolValue(SettingsClass.MINIMIZE_TO_SYSTRAY));
//        compactDatabaseAtNextStartupCheckBox.setSelected(settings.getBoolValue(SettingsClass.COMPACT_DBTABLES));
        availableNodesTextField.setText(settings.getValue(SettingsClass.AVAILABLE_NODES));
        altEditTextField.setText(settings.getValue(SettingsClass.ALTERNATE_EDITOR_COMMAND));
        autoSaveIntervalTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL)));
        enableLoggingCheckBox.setSelected(settings.getBoolValue(SettingsClass.LOG_TO_FILE));
        logFileSizeTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.LOG_FILE_SIZE_LIMIT)));

        logLevelComboBox.setSelectedKey(settings.getDefaultValue(SettingsClass.LOG_LEVEL));
        logLevelComboBox.setSelectedKey(settings.getValue(SettingsClass.LOG_LEVEL));

        splashScreenCheckBox.setSelected(settings.getBoolValue(SettingsClass.DISABLE_SPLASHSCREEN));
        useDDACheckBox.setSelected(settings.getBoolValue(SettingsClass.FCP2_USE_DDA));
        usePersistenceCheckBox.setSelected(settings.getBoolValue(SettingsClass.FCP2_USE_PERSISTENCE));

        refreshLoggingState();
    }

    public void ok() {
        
        String nodes = availableNodesTextField.getText();
        if( nodes.indexOf(",") > -1 ) {
            if( usePersistenceCheckBox.isSelected() ) {
                MiscToolkit.getInstance().showMessage(
                        "Persistence is not possible with more than 1 node. Persistence disabled.",
                        JOptionPane.ERROR_MESSAGE,
                        "Warning: Persistence is not possible");
                usePersistenceCheckBox.setSelected(false);
            }
        }
        
        saveSettings();
    }

    private void refreshLanguage() {
        availableNodesLabel1.setText(language.getString("Options.miscellaneous.listOfFcpNodes")+" "+language.getString("Options.miscellaneous.listOfFcpNodesExplanation"));
        useDDACheckBox.setText(language.getString("Options.miscellaneous.useDDA"));
        useDDACheckBox.setToolTipText(language.getString("Options.miscellaneous.useDDA.tooltip"));
        
        usePersistenceCheckBox.setText(language.getString("Options.miscellaneous.usePersistence"));
        
        autoSaveIntervalLabel.setText(language.getString("Options.miscellaneous.automaticSavingInterval") + 
                " (60 "+language.getString("Options.common.minutes")+")");
        splashScreenCheckBox.setText(language.getString("Options.miscellaneous.disableSplashscreen"));
        showSystrayIconCheckBox.setText(language.getString("Options.miscellaneous.showSysTrayIcon"));
        minimizeToSystrayCheckBox.setText(language.getString("Options.miscellaneous.minimizeToSystray"));
//        compactDatabaseAtNextStartupCheckBox.setText(language.getString("Options.miscellaneous.compactDatabaseAtNextStartup"));
        String off = language.getString("Options.common.off");
        altEditCheckBox.setText(language.getString("Options.miscellaneous.useEditorForWritingMessages") + " (" + off + ")");

        enableLoggingCheckBox.setText(language.getString("Options.miscellaneous.enableLogging"));
        logLevelLabel.setText(language.getString("Options.miscellaneous.loggingLevel") +
                    " (" + language.getString("Options.miscellaneous.logLevel.low") + ") ");
        logFileSizeLabel.setText(language.getString("Options.miscellaneous.logFileSizeLimit"));
    }

    private void refreshLoggingState() {
        boolean enableLogging = enableLoggingCheckBox.isSelected();
        logLevelLabel.setEnabled(enableLogging);
        logLevelComboBox.setEnabled(enableLogging);
        logFileSizeLabel.setEnabled(enableLogging);
        logFileSizeTextField.setEnabled(enableLogging);
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.AVAILABLE_NODES, availableNodesTextField.getText());
        settings.setValue(SettingsClass.SHOW_SYSTRAY_ICON, showSystrayIconCheckBox.isSelected());
        settings.setValue(SettingsClass.MINIMIZE_TO_SYSTRAY, minimizeToSystrayCheckBox.isSelected());
//        settings.setValue(SettingsClass.COMPACT_DBTABLES, compactDatabaseAtNextStartupCheckBox.isSelected());
        settings.setValue(SettingsClass.ALTERNATE_EDITOR_ENABLED, altEditCheckBox.isSelected());
        settings.setValue(SettingsClass.ALTERNATE_EDITOR_COMMAND, altEditTextField.getText());
        settings.setValue(SettingsClass.AUTO_SAVE_INTERVAL, autoSaveIntervalTextField.getText());
        settings.setValue(SettingsClass.LOG_TO_FILE, enableLoggingCheckBox.isSelected());
        settings.setValue(SettingsClass.LOG_FILE_SIZE_LIMIT, logFileSizeTextField.getText());
        settings.setValue(SettingsClass.LOG_LEVEL, logLevelComboBox.getSelectedKey());
        settings.setValue(SettingsClass.DISABLE_SPLASHSCREEN, splashScreenCheckBox.isSelected());
        settings.setValue(SettingsClass.FCP2_USE_DDA, useDDACheckBox.isSelected());
        settings.setValue(SettingsClass.FCP2_USE_PERSISTENCE, usePersistenceCheckBox.isSelected());
    }
}
