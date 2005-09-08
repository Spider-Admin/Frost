/*
  News2Panel.java / Frost
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

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import frost.SettingsClass;
import frost.util.gui.TextComponentClipboardMenu;
import frost.util.gui.translation.Language;

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
	private Language language = null;
	
	private JCheckBox blockBoardCheckBox = new JCheckBox();
	private JTextField blockBoardTextField = new JTextField();
	private JCheckBox blockBodyCheckBox = new JCheckBox();
	private JTextField blockBodyTextField = new JTextField();
	private JCheckBox blockSubjectCheckBox = new JCheckBox();
	private JTextField blockSubjectTextField = new JTextField();
	private JCheckBox doBoardBackoffCheckBox = new JCheckBox();
	private JCheckBox hideBadMessagesCheckBox = new JCheckBox();
	private JCheckBox hideCheckMessagesCheckBox = new JCheckBox();
	private JCheckBox hideObserveMessagesCheckBox = new JCheckBox();
	private JLabel intervalLabel = new JLabel();
		
	private Listener listener = new Listener();
		
	private JTextField sampleIntervalTextField = new JTextField(8);
		
	private JCheckBox signedOnlyCheckBox = new JCheckBox();
	private JTextField spamTresholdTextField = new JTextField(8);
		
	private JLabel tresholdLabel = new JLabel();

	/**
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected News2Panel(SettingsClass settings) {
		super();
		
		this.language = Language.getInstance();
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
		new TextComponentClipboardMenu(blockBoardTextField, language);
		new TextComponentClipboardMenu(blockBodyTextField, language);
		new TextComponentClipboardMenu(blockSubjectTextField, language);
		new TextComponentClipboardMenu(spamTresholdTextField, language);
		new TextComponentClipboardMenu(sampleIntervalTextField, language);
		
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
		add(hideObserveMessagesCheckBox, constraints);
						
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
		hideObserveMessagesCheckBox.setSelected(settings.getBoolValue("hideObserveMessages"));
			
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
		String hours = language.getString("hours");
		String off = language.getString("Off");

		intervalLabel.setText(
				language.getString("Sample interval") + " (" + hours + ")");
		tresholdLabel.setText(language.getString("Threshold of blocked messages"));
		signedOnlyCheckBox.setText(language.getString("Hide unsigned messages"));
		hideBadMessagesCheckBox.setText(
				language.getString("Hide messages flagged BAD") + " (" + off + ")");
		hideCheckMessagesCheckBox.setText(
				language.getString("Hide messages flagged CHECK") + " (" + off + ")");
		hideObserveMessagesCheckBox.setText(
				"Hide messages flagged OBSERVED" + " (" + off + ")");
		blockSubjectCheckBox.setText(
				language.getString(
				"Block messages with subject containing (separate by ';' )")
				+ ": ");
		blockBodyCheckBox.setText(
				language.getString(
				"Block messages with body containing (separate by ';' )")
				+ ": ");
		blockBoardCheckBox.setText(
				language.getString(
				"Block messages with these attached boards (separate by ';' )")
				+ ": ");
		doBoardBackoffCheckBox.setText(language.getString("Do spam detection"));
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
		settings.setValue("hideObserveMessages", hideObserveMessagesCheckBox.isSelected());
	}
}
