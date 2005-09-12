/*
  MiscPanel.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import frost.*;
import frost.Logging;
import frost.util.gui.TextComponentClipboardMenu;
import frost.util.gui.translation.*;

class MiscPanel extends JPanel {

	private class Listener implements ChangeListener, ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == enableLoggingCheckBox) {
				refreshLoggingState();
			}				
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == altEditCheckBox) {
				altEditChanged();	
			}				
		}
	}	
	
	private static Logger logger = Logger.getLogger(MiscPanel.class.getName());
	
	private SettingsClass settings = null;
	private Language language = null;	

	private JCheckBox allowEvilBertCheckBox = new JCheckBox();
	private JCheckBox altEditCheckBox = new JCheckBox();
	private JTextField altEditTextField = new JTextField();
	private JLabel autoSaveIntervalLabel = new JLabel();
	private JTextField autoSaveIntervalTextField = new JTextField(8);
	private JLabel availableNodesLabel1 = new JLabel();
	private JLabel availableNodesLabel2 = new JLabel();
	private JTextField availableNodesTextField = new JTextField();
	private JCheckBox enableLoggingCheckBox = new JCheckBox();
	private JLabel keyDownloadHtlLabel = new JLabel();
	private JTextField keyDownloadHtlTextField = new JTextField(8);

	private JLabel keyUploadHtlLabel = new JLabel();

	private JTextField keyUploadHtlTextField = new JTextField(8);

	private Listener listener = new Listener();
	private JLabel logFileSizeLabel = new JLabel();
	private JTextField logFileSizeTextField = new JTextField(8);
		
	private JTranslatableComboBox logLevelComboBox = null;
	private JLabel logLevelLabel = new JLabel();
	private JLabel maxKeysLabel = new JLabel();
	private JTextField maxKeysTextField = new JTextField(8);
	private JCheckBox showSystrayIconCheckBox = new JCheckBox();
	private JCheckBox splashScreenCheckBox = new JCheckBox();

	/**
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
	 */
	protected MiscPanel(SettingsClass settings) {
		super();
		
		this.language = Language.getInstance();
		this.settings = settings;
		
		initialize();
		loadSettings();
	}
		
	/**
	 * 
	 */
	private void altEditChanged() {
		altEditTextField.setEnabled(altEditCheckBox.isSelected());
	}
		
	/**
	 * @return
	 */
	private JPanel getLoggingPanel() {
		JPanel subPanel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
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
		constraints.gridy = 1;
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

	/**
	 * 
	 */
	private void initialize() {
		setName("MiscPanel");
		setLayout(new GridBagLayout());
		refreshLanguage();
		
		// We create the components
		new TextComponentClipboardMenu(altEditTextField, language);
		new TextComponentClipboardMenu(autoSaveIntervalTextField, language);
		new TextComponentClipboardMenu(availableNodesTextField, language);
		new TextComponentClipboardMenu(keyDownloadHtlTextField, language);
		new TextComponentClipboardMenu(keyUploadHtlTextField, language);
		new TextComponentClipboardMenu(logFileSizeTextField, language);
		new TextComponentClipboardMenu(maxKeysTextField, language);

		// Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Insets insets5555 = new Insets(5, 5, 5, 5);
		constraints.weighty = 1;
			
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.insets = insets5555;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(keyUploadHtlLabel, constraints);
		constraints.gridx = 1;
		add(keyUploadHtlTextField, constraints);
			
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(keyDownloadHtlLabel, constraints);
		constraints.gridx = 1;
		add(keyDownloadHtlTextField, constraints);
			
		constraints.anchor = GridBagConstraints.SOUTH;
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(availableNodesLabel1, constraints);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.gridy = 3;
		add(availableNodesLabel2, constraints);
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.gridwidth = 2;
		add(availableNodesTextField, constraints);

		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 4;
		add(maxKeysLabel, constraints);
		constraints.gridx = 1;
		add(maxKeysTextField, constraints);
			
		constraints.gridx = 0;
		constraints.gridy = 5;
		add(altEditCheckBox, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		add(altEditTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 6;
		add(autoSaveIntervalLabel, constraints);
		constraints.gridx = 1;
		add(autoSaveIntervalTextField, constraints);
	
		constraints.gridx = 0;
		constraints.gridy = 7;
		add(allowEvilBertCheckBox, constraints);			
//		constraints.gridx = 1;
//		constraints.gridwidth = 2;
//		constraints.weightx = 1;
//		add(cleanupCheckBox, constraints);	
			
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 8;
		add(splashScreenCheckBox, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		add(showSystrayIconCheckBox, constraints);	
			
		constraints.gridx = 0;
		constraints.gridy = 9;
		constraints.gridwidth = 3;
		add(getLoggingPanel(), constraints);
			
		// Add listeners
		enableLoggingCheckBox.addActionListener(listener);
		altEditCheckBox.addChangeListener(listener);
	}

	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		allowEvilBertCheckBox.setSelected(settings.getBoolValue("allowEvilBert"));
		altEditCheckBox.setSelected(settings.getBoolValue("useAltEdit"));
		altEditTextField.setEnabled(altEditCheckBox.isSelected());
		keyUploadHtlTextField.setText(settings.getValue("keyUploadHtl"));
		keyDownloadHtlTextField.setText(settings.getValue("keyDownloadHtl"));
		showSystrayIconCheckBox.setSelected(settings.getBoolValue("showSystrayIcon"));
		availableNodesTextField.setText(settings.getValue("availableNodes"));
		altEditTextField.setText(settings.getValue("altEdit"));
		maxKeysTextField.setText(settings.getValue("maxKeys"));
		autoSaveIntervalTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL)));
		enableLoggingCheckBox.setSelected(settings.getBoolValue(SettingsClass.LOG_TO_FILE));
		logFileSizeTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.LOG_FILE_SIZE_LIMIT)));

		logLevelComboBox.setSelectedKey(settings.getDefaultValue(SettingsClass.LOG_LEVEL));
		logLevelComboBox.setSelectedKey(settings.getValue(SettingsClass.LOG_LEVEL));

		// "Load" splashchk
		File splashchk = new File("nosplash.chk");
		if (splashchk.exists()) {
			splashScreenCheckBox.setSelected(true);
		} else {
			splashScreenCheckBox.setSelected(false);
		}

		refreshLoggingState();
	}

	/**
	 * 
	 */
	public void ok() {
		saveSettings();
	}
		
	/**
	 * 
	 */
	private void refreshLanguage() {
		keyUploadHtlLabel.setText(language.getString("Keyfile upload HTL") + " (21)");
		keyDownloadHtlLabel.setText(
				language.getString("Keyfile download HTL") + " (24)");
		availableNodesLabel1.setText(language.getString("list of nodes"));
		availableNodesLabel2.setText(language.getString("list of nodes 2"));
		maxKeysLabel.setText(
				language.getString("Maximum number of keys to store") + " (100000)");
		autoSaveIntervalLabel.setText(
				language.getString("Automatic saving interval") + " (15)");
		splashScreenCheckBox.setText(language.getString("Disable splashscreen"));
		showSystrayIconCheckBox.setText(language.getString("Show systray icon"));
		String off = language.getString("Off");
		allowEvilBertCheckBox.setText(
				language.getString("Allow 2 byte characters") + " (" + off + ")");
		altEditCheckBox.setText(
				language.getString("Use editor for writing messages") + " (" + off + ")");

		enableLoggingCheckBox.setText(language.getString("Enable logging"));
		logLevelLabel.setText(
				language.getString("Logging level")
				+ " ("
				+ language.getString("Low")
				+ ") ");
		logFileSizeLabel.setText(language.getString("Log file size limit (in KB)"));

	}
		
	/**
	 * 
	 */
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
		settings.setValue("keyUploadHtl", keyUploadHtlTextField.getText());
		settings.setValue("keyDownloadHtl", keyDownloadHtlTextField.getText());
		settings.setValue("availableNodes", availableNodesTextField.getText());
		settings.setValue("maxKeys", maxKeysTextField.getText());
		settings.setValue("showSystrayIcon", showSystrayIconCheckBox.isSelected());
		settings.setValue("allowEvilBert", allowEvilBertCheckBox.isSelected());
		settings.setValue("useAltEdit", altEditCheckBox.isSelected());
		settings.setValue("altEdit", altEditTextField.getText());
		settings.setValue(SettingsClass.AUTO_SAVE_INTERVAL, autoSaveIntervalTextField.getText());
		settings.setValue(SettingsClass.LOG_TO_FILE, enableLoggingCheckBox.isSelected());
		settings.setValue(
			SettingsClass.LOG_FILE_SIZE_LIMIT,
			logFileSizeTextField.getText());
		settings.setValue(SettingsClass.LOG_LEVEL, logLevelComboBox.getSelectedKey());

		// Save splashchk
		try {
			File splashFile = new File("nosplash.chk");
			if (splashScreenCheckBox.isSelected()) {
				splashFile.createNewFile();
			} else {
				splashFile.delete();
			}
		} catch (IOException ioex) {
			logger.log(Level.SEVERE, "Could not create splashscreen checkfile", ioex);
		}
	}

}
