/*
  News3Panel.java / Frost
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
import javax.swing.border.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class News3Panel extends JPanel {
	
	private class Listener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == showUpdateCheckBox) {
				refreshUpdateState();
			}
			if (e.getSource() == selectedColorButton) {
				selectedColorPressed();
			}
			if (e.getSource() == notSelectedColorButton) {
				notSelectedColorPressed();
			}
		}
	}

	private SettingsClass settings = null;
	private Language language = null;

	private JLabel autoUpdateLabel = new JLabel();
	private JLabel minimumIntervalLabel = new JLabel();
	private JTextField minimumIntervalTextField = new JTextField(8);
	private JLabel concurrentUpdatesLabel = new JLabel();
	private JTextField concurrentUpdatesTextField = new JTextField(8);

	private JCheckBox showUpdateCheckBox = new JCheckBox();
    private JCheckBox automaticBoardUpdateCheckBox = new JCheckBox();
	private JButton selectedColorButton = new JButton();
	private JLabel selectedColorTextLabel = new JLabel();
	private JLabel selectedColorLabel = new JLabel();
	private JButton notSelectedColorButton = new JButton();
	private JLabel notSelectedColorTextLabel = new JLabel();
	private JLabel notSelectedColorLabel = new JLabel();
	
	private JCheckBox silentlyRetryCheckBox = new JCheckBox();
	private JCheckBox showDeletedMessagesCheckBox = new JCheckBox();

	private JPanel colorPanel = null;

	private Listener listener = new Listener();
	
	private Color selectedColor = null;
	private Color notSelectedColor = null;

	/**
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected News3Panel(SettingsClass settings) {
		super();

		this.language = Language.getInstance();
		this.settings = settings;

		initialize();
		loadSettings();
	}

	/**
	 * @return
	 */
	private Component getColorPanel() {
		if (colorPanel == null) {
			
			colorPanel = new JPanel(new GridBagLayout());
			colorPanel.setBorder(new EmptyBorder(5, 30, 5, 5));
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.weighty = 1;
			constraints.weightx = 1;
			constraints.anchor = GridBagConstraints.NORTHWEST;

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0.5;
			colorPanel.add(selectedColorTextLabel, constraints);
			constraints.fill = GridBagConstraints.VERTICAL;
			constraints.gridx = 1;
			constraints.weightx = 0.2;
			colorPanel.add(selectedColorLabel, constraints);
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridx = 2;
			constraints.weightx = 0.5;
			colorPanel.add(selectedColorButton, constraints);

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 0.5;
			colorPanel.add(notSelectedColorTextLabel, constraints);
			constraints.fill = GridBagConstraints.VERTICAL;
			constraints.gridx = 1;
			constraints.weightx = 0.2;
			colorPanel.add(notSelectedColorLabel, constraints);
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridx = 2;
			constraints.weightx = 0.5;
			colorPanel.add(notSelectedColorButton, constraints);
			
			selectedColorLabel.setOpaque(true);
			notSelectedColorLabel.setOpaque(true);
			selectedColorLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			notSelectedColorLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			selectedColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
			notSelectedColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		return colorPanel;
	}

	/**
	 * @return
	 */
	private Component getUpdatePanel() {
		JPanel updatePanel = new JPanel(new GridBagLayout());
		updatePanel.setBorder(new EmptyBorder(5, 30, 5, 5));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridy = 0;
        
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.weightx = 0.5;
		updatePanel.add(minimumIntervalLabel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 1;
		constraints.weightx = 1;
		updatePanel.add(minimumIntervalTextField, constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 0.5;
		updatePanel.add(concurrentUpdatesLabel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 1;
		constraints.weightx = 1;
		updatePanel.add(concurrentUpdatesTextField, constraints);
		
		return updatePanel;
	}

	/**
	 * 
	 */
	private void initialize() {
		setName("News3Panel");
		setLayout(new GridBagLayout());
		refreshLanguage();

		// We create the components
		new TextComponentClipboardMenu(minimumIntervalTextField, language);
		new TextComponentClipboardMenu(concurrentUpdatesTextField, language);
		
		// Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Insets insets5555 = new Insets(5, 5, 5, 5);
		constraints.insets = insets5555;
		constraints.weighty = 1;
		constraints.weightx = 1;
		constraints.gridwidth = 2;

		constraints.gridx = 0;
		constraints.gridy = 0;
		add(autoUpdateLabel, constraints);

        constraints.gridy++;
        add(automaticBoardUpdateCheckBox, constraints);

		constraints.gridy++;
		add(getUpdatePanel(), constraints);

		constraints.gridy++;
		add(showUpdateCheckBox, constraints);

		constraints.gridy++;
		add(getColorPanel(), constraints);
		
		constraints.gridy++;
		add(silentlyRetryCheckBox, constraints);
		
		constraints.gridy++;
		add(showDeletedMessagesCheckBox, constraints);

		// Add listeners
		showUpdateCheckBox.addActionListener(listener);
		selectedColorButton.addActionListener(listener);
		notSelectedColorButton.addActionListener(listener);
	}

	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		minimumIntervalTextField.setText(settings.getValue("automaticUpdate.boardsMinimumUpdateInterval"));
		concurrentUpdatesTextField.setText(settings.getValue("automaticUpdate.concurrentBoardUpdates"));
		
		showUpdateCheckBox.setSelected(settings.getBoolValue("boardUpdateVisualization"));
		refreshUpdateState();

        // this setting is in MainFrame 
        automaticBoardUpdateCheckBox.setSelected(MainFrame.getInstance().isAutomaticBoardUpdateEnabled());
        
		selectedColor = (Color) settings.getObjectValue("boardUpdatingSelectedBackgroundColor");
		notSelectedColor = (Color) settings.getObjectValue("boardUpdatingNonSelectedBackgroundColor");
		selectedColorLabel.setBackground(selectedColor);
		notSelectedColorLabel.setBackground(notSelectedColor);
		
		silentlyRetryCheckBox.setSelected(settings.getBoolValue(SettingsClass.SILENTLY_RETRY_MESSAGES));
		
		showDeletedMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_DELETED_MESSAGES));
	}
	
	private void notSelectedColorPressed() {
		Color newCol =
			JColorChooser.showDialog(
				getTopLevelAncestor(),
				language.getString("Choose updating color of NON-SELECTED boards"),
				notSelectedColor);
		if (newCol != null) {
			notSelectedColor = newCol;
			notSelectedColorButton.setBackground(notSelectedColor);
		}
	}
	
	public void ok() {
		saveSettings();
	}

	private void refreshLanguage() {
		String minutes = language.getString("minutes");
		String color = language.getString("Color");
		String on = language.getString("On");

		autoUpdateLabel.setText(language.getString("Automatic update options"));
		minimumIntervalLabel.setText(
				language.getString("Minimum update interval of a board") + " (" + minutes + ") (45)");
		concurrentUpdatesLabel.setText(
				language.getString("Number of concurrently updating boards") + " (6)");

        automaticBoardUpdateCheckBox.setText(language.getString("Automatic message update"));
		showUpdateCheckBox.setText(
				language.getString("Show board update visualization") + " (" + on + ")");
		selectedColorTextLabel.setText(
				language.getString("Background color if updating board is selected"));
		selectedColorLabel.setText("    " + color + "    ");
		selectedColorButton.setText(language.getString("Choose"));
		
		notSelectedColorTextLabel.setText(
				language.getString("Background color if updating board is not selected"));
		notSelectedColorLabel.setText("    " + color + "    ");
		notSelectedColorButton.setText(language.getString("Choose"));

		silentlyRetryCheckBox.setText(language.getString("Silently retry failed messages"));
		showDeletedMessagesCheckBox.setText(language.getString("Show deleted messages"));
	}
	
	private void refreshUpdateState() {
		MiscToolkit.getInstance().setContainerEnabled(colorPanel, showUpdateCheckBox.isSelected());
	}
	
	/**
	 * Save the settings of this panel 
	 */
	private void saveSettings() {
		settings.setValue("automaticUpdate.concurrentBoardUpdates", concurrentUpdatesTextField.getText());
		settings.setValue("automaticUpdate.boardsMinimumUpdateInterval", minimumIntervalTextField.getText());
		
		settings.setValue("boardUpdateVisualization", showUpdateCheckBox.isSelected());
        
        // settings.setValue("automaticUpdate", automaticBoardUpdateCheckBox.isSelected());
        // we change setting in MainFrame, this is auto-saved during frostSettings.save()
        MainFrame.getInstance().setAutomaticBoardUpdateEnabled(automaticBoardUpdateCheckBox.isSelected());

		settings.setObjectValue("boardUpdatingSelectedBackgroundColor", selectedColor);
		settings.setObjectValue("boardUpdatingNonSelectedBackgroundColor", notSelectedColor);
		
		settings.setValue(SettingsClass.SILENTLY_RETRY_MESSAGES, silentlyRetryCheckBox.isSelected());
		settings.setValue(SettingsClass.SHOW_DELETED_MESSAGES, showDeletedMessagesCheckBox.isSelected());
	}
	
	private void selectedColorPressed() {
		Color newCol =
			JColorChooser.showDialog(
				getTopLevelAncestor(),
				language.getString("Choose updating color of SELECTED boards"),
				selectedColor);
		if (newCol != null) {
			selectedColor = newCol;
			selectedColorButton.setBackground(selectedColor);
		}
	}
}
