/*
 * Created on Oct 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import frost.SettingsClass;
import frost.util.gui.JClipboardTextField;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author $author$
 * @version $revision$
 */
class News2Panel extends JPanel {
		
	/**
	 * 
	 */
	private class Listener implements ActionListener {

		/* (non-Javadoc) 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == blockSubjectCheckBox) {
				blockSubjectPressed();
			}
			if (e.getSource() == blockBodyCheckBox) {
				blockBodyPressed();
			}
			if (e.getSource() == blockBoardCheckBox) {
				blockBoardPressed();
			}
			if (e.getSource() == doBoardBackoffCheckBox) {
				refreshSpamDetectionState();
			}
		}
	}
	
	private SettingsClass settings = null;
	private UpdatingLanguageResource languageResource = null;
	
	private JCheckBox blockBoardCheckBox = new JCheckBox();
	private JClipboardTextField blockBoardTextField = null;
	private JCheckBox blockBodyCheckBox = new JCheckBox();
	private JClipboardTextField blockBodyTextField = null;
	private JCheckBox blockSubjectCheckBox = new JCheckBox();
	private JClipboardTextField blockSubjectTextField = null;
	private JCheckBox doBoardBackoffCheckBox = new JCheckBox();
	private JCheckBox hideBadMessagesCheckBox = new JCheckBox();
	private JCheckBox hideCheckMessagesCheckBox = new JCheckBox();
	private JCheckBox hideNAMessagesCheckBox = new JCheckBox();
	private JLabel intervalLabel = new JLabel();
		
	private Listener listener = new Listener();
		
	private JClipboardTextField sampleIntervalTextField = null;
		
	private JCheckBox signedOnlyCheckBox = new JCheckBox();
	private JClipboardTextField spamTresholdTextField = null;
		
	private JLabel tresholdLabel = new JLabel();

	/**
	 * @param languageResource the LanguageResource to get localized strings from
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected News2Panel(UpdatingLanguageResource languageResource, SettingsClass settings) {
		super();
		
		this.languageResource = languageResource;
		this.settings = settings;
		
		initialize();
		loadSettings();
	}
		
	/**
	 * 
	 */
	private void blockBoardPressed() {
		blockBoardTextField.setEnabled(blockBoardCheckBox.isSelected());				
	}
		

	/**
	 * 
	 */
	private void blockBodyPressed() {
		blockBodyTextField.setEnabled(blockBodyCheckBox.isSelected());				
	}

	/**
	 * 
	 */
	private void blockSubjectPressed() {
		blockSubjectTextField.setEnabled(blockSubjectCheckBox.isSelected());	
	}		
		
	/**
	 * @return
	 */
	private JPanel getSpamPanel() {
		JPanel spamPanel = new JPanel(new GridBagLayout());
		spamPanel.setBorder(new EmptyBorder(5, 30, 5, 5));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1; 
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.NORTHWEST;

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.5;
		spamPanel.add(intervalLabel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 1;
		constraints.weightx = 1;
		spamPanel.add(sampleIntervalTextField, constraints);
			
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		spamPanel.add(tresholdLabel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 1;
		constraints.weightx = 1;
		spamPanel.add(spamTresholdTextField, constraints);

		return spamPanel;
	}
		
	/**
	 * 
	 */
	private void initialize() {
		setName("News2Panel");
		setLayout(new GridBagLayout());
		refreshLanguage();
			
		// We create the components
		blockBoardTextField = new JClipboardTextField(languageResource);
		blockBodyTextField = new JClipboardTextField(languageResource);
		blockSubjectTextField = new JClipboardTextField(languageResource);
		spamTresholdTextField =new JClipboardTextField(8, languageResource);
		sampleIntervalTextField = new JClipboardTextField(8, languageResource);
		
		// Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Insets insets5555 = new Insets(5, 5, 5, 5);
		Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
		constraints.insets = insets5555;
		constraints.weighty = 1;
		constraints.weightx = 1;
		constraints.gridwidth = 2;

		constraints.gridx = 0;
		constraints.gridy = 0;
		add(blockSubjectCheckBox, constraints);
		constraints.insets = insets5_30_5_5;
		constraints.gridy = 1;
		add(blockSubjectTextField, constraints);
			
		constraints.insets = insets5555;
		constraints.gridy = 2;
		add(blockBodyCheckBox, constraints);
		constraints.insets = insets5_30_5_5;
		constraints.gridy = 3;
		add(blockBodyTextField, constraints);
			
		constraints.insets = insets5555;
		constraints.gridy = 4;
		add(blockBoardCheckBox, constraints);
		constraints.insets = insets5_30_5_5;
		constraints.gridy = 5;
		add(blockBoardTextField, constraints);
						
		constraints.insets = insets5555;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 6;
		add(signedOnlyCheckBox, constraints);
		constraints.gridx = 1;
		add(hideBadMessagesCheckBox, constraints);
		constraints.gridx = 0;
		constraints.gridy = 7;
		add(hideCheckMessagesCheckBox, constraints);
		constraints.gridx = 1;
		add(hideNAMessagesCheckBox, constraints);
						
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 8;
		add(doBoardBackoffCheckBox, constraints);
		constraints.gridy = 9;
		constraints.weighty = 0;
		add(getSpamPanel(), constraints);
						
		// Add listeners
		blockSubjectCheckBox.addActionListener(listener);
		blockBodyCheckBox.addActionListener(listener);
		blockBoardCheckBox.addActionListener(listener);
		doBoardBackoffCheckBox.addActionListener(listener);						
	}
		
	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		signedOnlyCheckBox.setSelected(settings.getBoolValue("signedOnly"));
		hideBadMessagesCheckBox.setSelected(settings.getBoolValue("hideBadMessages"));
		hideCheckMessagesCheckBox.setSelected(settings.getBoolValue("hideCheckMessages"));
		hideNAMessagesCheckBox.setSelected(settings.getBoolValue("hideNAMessages"));
			
		blockSubjectCheckBox.setSelected(settings.getBoolValue("blockMessageChecked"));
		blockSubjectTextField.setEnabled(blockSubjectCheckBox.isSelected());
		blockSubjectTextField.setText(settings.getValue("blockMessage"));
		blockBodyCheckBox.setSelected(settings.getBoolValue("blockMessageBodyChecked"));
		blockBodyTextField.setEnabled(blockBodyCheckBox.isSelected());
		blockBodyTextField.setText(settings.getValue("blockMessageBody"));
		blockBoardCheckBox.setSelected(settings.getBoolValue("blockMessageBoardChecked"));
		blockBoardTextField.setEnabled(blockBoardCheckBox.isSelected());
		blockBoardTextField.setText(settings.getValue("blockMessageBoard"));
			
		doBoardBackoffCheckBox.setSelected(settings.getBoolValue("doBoardBackoff"));
		sampleIntervalTextField.setText(settings.getValue("sampleInterval"));
		spamTresholdTextField.setText(settings.getValue("spamTreshold"));
		refreshSpamDetectionState();
	}
		
	public void ok() {
		saveSettings();
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		String hours = languageResource.getString("hours");
		String off = languageResource.getString("Off");

		intervalLabel.setText(
			languageResource.getString("Sample interval") + " (" + hours + ")");
		tresholdLabel.setText(languageResource.getString("Threshold of blocked messages"));
		signedOnlyCheckBox.setText(languageResource.getString("Hide unsigned messages"));
		hideBadMessagesCheckBox.setText(
			languageResource.getString("Hide messages flagged BAD") + " (" + off + ")");
		hideCheckMessagesCheckBox.setText(
			languageResource.getString("Hide messages flagged CHECK") + " (" + off + ")");
		hideNAMessagesCheckBox.setText(
			languageResource.getString("Hide messages flagged N/A") + " (" + off + ")");
		blockSubjectCheckBox.setText(
			languageResource.getString(
				"Block messages with subject containing (separate by ';' )")
				+ ": ");
		blockBodyCheckBox.setText(
			languageResource.getString(
				"Block messages with body containing (separate by ';' )")
				+ ": ");
		blockBoardCheckBox.setText(
			languageResource.getString(
				"Block messages with these attached boards (separate by ';' )")
				+ ": ");
		doBoardBackoffCheckBox.setText(languageResource.getString("Do spam detection"));
	}
		
	/**
	 * 
	 */
	private void refreshSpamDetectionState() {
		boolean enableSpamDetection = doBoardBackoffCheckBox.isSelected();
		sampleIntervalTextField.setEnabled(enableSpamDetection);
		spamTresholdTextField.setEnabled(enableSpamDetection);
		tresholdLabel.setEnabled(enableSpamDetection);
		intervalLabel.setEnabled(enableSpamDetection);
	}
		
	/**
	 * Save the settings of this panel 
	 */
	private void saveSettings() {
		settings.setValue("blockMessage", ((blockSubjectTextField.getText()).trim()).toLowerCase());
		settings.setValue("blockMessageChecked", blockSubjectCheckBox.isSelected());
		settings.setValue("blockMessageBody", ((blockBodyTextField.getText()).trim()).toLowerCase());
		settings.setValue("blockMessageBodyChecked", blockBodyCheckBox.isSelected());
		settings.setValue("blockMessageBoard", ((blockBoardTextField.getText()).trim()).toLowerCase());
		settings.setValue("blockMessageBoardChecked", blockBoardCheckBox.isSelected());
		settings.setValue("doBoardBackoff", doBoardBackoffCheckBox.isSelected());
		settings.setValue("spamTreshold", spamTresholdTextField.getText());
		settings.setValue("sampleInterval", sampleIntervalTextField.getText());
		settings.setValue("signedOnly", signedOnlyCheckBox.isSelected());
		settings.setValue("hideBadMessages", hideBadMessagesCheckBox.isSelected());
		settings.setValue("hideCheckMessages", hideCheckMessagesCheckBox.isSelected());
		settings.setValue("hideNAMessages", hideNAMessagesCheckBox.isSelected());
	}
}
