/*
 * Created on Oct 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.event.*;

import frost.*;
import frost.util.gui.JClipboardTextField;
import frost.util.gui.translation.*;
import frost.util.gui.translation.JTranslatableComboBox;

/**
 * @author $author$
 * @version $revision$
 */
class MiscPanel extends JPanel {

	/**
	 * 
	 */
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
	private UpdatingLanguageResource languageResource = null;	

	private JCheckBox allowEvilBertCheckBox = new JCheckBox();
	private JCheckBox altEditCheckBox = new JCheckBox();
	private JClipboardTextField altEditTextField;
	private JLabel autoSaveIntervalLabel = new JLabel();
	private JClipboardTextField autoSaveIntervalTextField;
	private JLabel availableNodesLabel1 = new JLabel();
	private JLabel availableNodesLabel2 = new JLabel();
	private JClipboardTextField availableNodesTextField;
	private JCheckBox cleanupCheckBox = new JCheckBox();
	private JCheckBox enableLoggingCheckBox = new JCheckBox();
	private JLabel keyDownloadHtlLabel = new JLabel();
	private JClipboardTextField keyDownloadHtlTextField;

	private JLabel keyUploadHtlLabel = new JLabel();

	private JClipboardTextField keyUploadHtlTextField;

	private Listener listener = new Listener();
	private JLabel logFileSizeLabel = new JLabel();
	private JClipboardTextField logFileSizeTextField;
		
	private JTranslatableComboBox logLevelComboBox = null;
	private JLabel logLevelLabel = new JLabel();
	private JLabel maxKeysLabel = new JLabel();
	private JClipboardTextField maxKeysTextField;
	private JCheckBox showSystrayIconCheckBox = new JCheckBox();
	private JCheckBox splashScreenCheckBox = new JCheckBox();

	/**
	 * @param languageResource the LanguageResource to get localized strings from
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
	 */
	protected MiscPanel(UpdatingLanguageResource languageResource, SettingsClass settings) {
		super();
		
		this.languageResource = languageResource;
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
		logLevelComboBox = new JTranslatableComboBox(languageResource, searchComboBoxKeys);
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
		altEditTextField = new JClipboardTextField(languageResource);
		autoSaveIntervalTextField = new JClipboardTextField(8, languageResource);
		availableNodesTextField = new JClipboardTextField(languageResource);
		keyDownloadHtlTextField = new JClipboardTextField(8, languageResource);
		keyUploadHtlTextField = new JClipboardTextField(8, languageResource);
		logFileSizeTextField = new JClipboardTextField(8, languageResource);
		maxKeysTextField = new JClipboardTextField(8, languageResource);

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
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		add(cleanupCheckBox, constraints);	
			
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
		cleanupCheckBox.setSelected(settings.getBoolValue("doCleanUp"));
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

	public void ok() {
		saveSettings();
	}
		
	/**
	 * 
	 */
	private void refreshLanguage() {
		keyUploadHtlLabel.setText(languageResource.getString("Keyfile upload HTL") + " (21)");
		keyDownloadHtlLabel.setText(
			languageResource.getString("Keyfile download HTL") + " (24)");
		availableNodesLabel1.setText(languageResource.getString("list of nodes"));
		availableNodesLabel2.setText(languageResource.getString("list of nodes 2"));
		maxKeysLabel.setText(
			languageResource.getString("Maximum number of keys to store") + " (100000)");
		autoSaveIntervalLabel.setText(
			languageResource.getString("Automatic saving interval") + " (15)");
		splashScreenCheckBox.setText(languageResource.getString("Disable splashscreen"));
		showSystrayIconCheckBox.setText(languageResource.getString("Show systray icon"));
		String off = languageResource.getString("Off");
		allowEvilBertCheckBox.setText(
			languageResource.getString("Allow 2 byte characters") + " (" + off + ")");
		altEditCheckBox.setText(
			languageResource.getString("Use editor for writing messages") + " (" + off + ")");
		cleanupCheckBox.setText(languageResource.getString("Clean the keypool"));

		enableLoggingCheckBox.setText(languageResource.getString("Enable logging"));
		logLevelLabel.setText(
			languageResource.getString("Logging level")
				+ " ("
				+ languageResource.getString("Low")
				+ ") ");
		logFileSizeLabel.setText(languageResource.getString("Log file size limit (in KB)"));

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
		settings.setValue("doCleanUp", cleanupCheckBox.isSelected());
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
