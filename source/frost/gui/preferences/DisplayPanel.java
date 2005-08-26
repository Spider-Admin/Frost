/*
  DisplayPanel.java / Frost
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
import frost.util.gui.*;
import frost.util.gui.translation.Language;

/**
 * Display Panel. Contains appearace options: skins and more in the future
 */
class DisplayPanel extends JPanel {

	/**
	 * 
	 */
	public class Listener implements ActionListener {
		/**
		 * 
		 */
		public Listener() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == messageBodyButton) {
				messageBodyButtonPressed();	
			}
			if (e.getSource() == messageListButton) {
				messageListButtonPressed();	
			}	
			if (e.getSource() == fileListButton) {
				fileListButtonPressed();	
			}
		}
	}
	
	private JDialog owner = null;
	private SettingsClass settings = null;
	private Language language = null;
	
	private Listener listener = new Listener();
	
	private JLabel fontsLabel = new JLabel();
	private JLabel moreSkinsLabel = new JLabel();
				
	private JCheckBox messageBodyAACheckBox = new JCheckBox();
		
	private JLabel messageBodyLabel = new JLabel();
	private JLabel fileListLabel = new JLabel();
	private JLabel messageListLabel = new JLabel();
	
	private JButton fileListButton = new JButton();
	private JButton messageListButton = new JButton();
	private JButton messageBodyButton = new JButton();
	
	private JLabel selectedFileListFontLabel = new JLabel();
	private JLabel selectedMessageBodyFontLabel = new JLabel();
	private JLabel selectedMessageListFontLabel = new JLabel();
	
	private Font selectedBodyFont = null;
	private Font selectedFileListFont = null;
	private Font selectedMessageListFont = null;
		
	private SkinChooser skinChooser = null;

	/**
	 * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
	 */
	protected DisplayPanel(JDialog owner, SettingsClass settings) {
		super();
		
		this.owner = owner;
		this.language = Language.getInstance();
		this.settings = settings;
		
		initialize();
		loadSettings();
	}

	/**
	 * 
	 */
	public void cancel() {
		skinChooser.cancelChanges();
	}
		
	/**
	 * 
	 */
	private void fileListButtonPressed() {
		FontChooser fontChooser = new FontChooser(owner, language);
		fontChooser.setModal(true);
		fontChooser.setSelectedFont(selectedFileListFont);
		fontChooser.setVisible(true);
		Font selectedFontTemp = fontChooser.getSelectedFont();
		if (selectedFontTemp != null) {
			selectedFileListFont = selectedFontTemp;
			selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
		}
	}

	/**
	 * @param font
	 * @return
	 */
	private String getFontLabel(Font font) {
		if (font == null) {
			return "";
		} else {
			StringBuffer returnValue = new StringBuffer();
			returnValue.append(font.getFamily());
			if (font.isBold()) {
				returnValue.append(" " + language.getString("Bold"));
			}
			if (font.isItalic()) {
				returnValue.append(" " + language.getString("Italic"));
			}
			returnValue.append(", " + font.getSize());
			return returnValue.toString();
		}
	}
		
	/**
	 * @return
	 */
	private JPanel getFontsPanel() {
		JPanel fontsPanel = new JPanel(new GridBagLayout());
		fontsPanel.setBorder(new EmptyBorder(5, 80, 5, 5));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weighty = 1;
		Insets inset1515 = new Insets(1, 5, 1, 5);
		Insets inset1519 = new Insets(1, 5, 1, 9);

		constraints.insets = inset1515;
		constraints.gridx = 0; 
		constraints.gridy = 0;
		constraints.weightx = 0.8;
		fontsPanel.add(messageBodyLabel, constraints);
		constraints.insets = inset1519;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 0.1;
		fontsPanel.add(messageBodyButton, constraints);
		constraints.insets = inset1515;
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.weightx = 1;
		fontsPanel.add(selectedMessageBodyFontLabel, constraints);

		constraints.insets = inset1515;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0.8;
		fontsPanel.add(messageListLabel, constraints);
		constraints.insets = inset1519;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 0.1;
		fontsPanel.add(messageListButton, constraints);
		constraints.insets = inset1515;
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.weightx = 1;
		fontsPanel.add(selectedMessageListFontLabel, constraints);
			
		constraints.insets = inset1515;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 0.8;
		fontsPanel.add(fileListLabel, constraints);
		constraints.insets = inset1519;
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.weightx = 0.1;
		fontsPanel.add(fileListButton, constraints);
		constraints.insets = inset1515;
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.weightx = 1;
		fontsPanel.add(selectedFileListFontLabel, constraints);
			
		return fontsPanel;
	}

	/**
	 * Initialize the class.
	 */
	private void initialize() {
		setName("DisplayPanel");
		setLayout(new GridBagLayout());
		refreshLanguage();

		//Adds all of the components			
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		Insets inset5511 = new Insets(5, 5, 1, 1);
		Insets inset1515 = new Insets(1, 5, 1, 5);

		constraints.insets = inset1515;
		constraints.gridx = 0;
		constraints.gridy = 0;
		skinChooser = new SkinChooser(language.getResourceBundle());
		add(skinChooser, constraints);

		constraints.insets = inset1515;
		constraints.gridx = 0;
		constraints.gridy = 1;
		moreSkinsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(moreSkinsLabel, constraints);

		constraints.insets = inset5511;
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(fontsLabel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 4;
		add(getFontsPanel(), constraints);
			
		constraints.gridx = 0;
		constraints.gridy = 5;
		add(messageBodyAACheckBox, constraints);

		//Add listeners
		messageBodyButton.addActionListener(listener);
		messageListButton.addActionListener(listener);
		fileListButton.addActionListener(listener);
	}

	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		boolean skinsEnabled = settings.getBoolValue("skinsEnabled");
		skinChooser.setSkinsEnabled(skinsEnabled);
		String selectedSkinPath = settings.getValue("selectedSkin");
		skinChooser.setSelectedSkin(selectedSkinPath);

		String fontName = settings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
		int fontSize = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
		int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
		selectedBodyFont = new Font(fontName, fontStyle, fontSize);
		selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));

		fontName = settings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
		fontSize = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
		fontStyle = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
		selectedMessageListFont = new Font(fontName, fontStyle, fontSize);
		selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));

		fontName = settings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
		fontSize = settings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
		fontStyle = settings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
		selectedFileListFont = new Font(fontName, fontStyle, fontSize);
		selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));

		messageBodyAACheckBox.setSelected(settings.getBoolValue("messageBodyAA"));
	}
		
	/**
	 * 
	 */
	private void messageBodyButtonPressed() {
		FontChooser fontChooser = new FontChooser(owner, language);
		fontChooser.setModal(true);
		fontChooser.setSelectedFont(selectedBodyFont);
		fontChooser.setVisible(true);
		Font selectedFontTemp = fontChooser.getSelectedFont();
		if (selectedFontTemp != null) {
			selectedBodyFont = selectedFontTemp;
			selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));
		}
	}

	/**
	 * 
	 */
	private void messageListButtonPressed() {
		FontChooser fontChooser = new FontChooser(owner, language);
		fontChooser.setModal(true);
		fontChooser.setSelectedFont(selectedMessageListFont);
		fontChooser.setVisible(true);
		Font selectedFontTemp = fontChooser.getSelectedFont();
		if (selectedFontTemp != null) {
			selectedMessageListFont = selectedFontTemp;
			selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
		}
	}

	/**
	 * 
	 */
	public void ok() {
		skinChooser.commitChanges();
		saveSettings();
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		moreSkinsLabel.setText(language.getString("MoreSkinsAt") + " http://javootoo.l2fprod.com/plaf/skinlf/");
		fontsLabel.setText(language.getString("Fonts"));
		messageBodyLabel.setText(language.getString("Message Body"));
		messageBodyButton.setText(language.getString("Choose"));
		selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));
		messageListLabel.setText(language.getString("Message List"));
		messageListButton.setText(language.getString("Choose"));
		selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
		fileListLabel.setText(language.getString("File List"));
		fileListButton.setText(language.getString("Choose"));
		selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
		messageBodyAACheckBox.setText(language.getString("EnableMessageBodyAA"));
	}

	/** 
	 * Save the settings of this panel
	 */
	private void saveSettings() {
		boolean skinsEnabled = skinChooser.isSkinsEnabled();
		settings.setValue("skinsEnabled", skinsEnabled);

		String selectedSkin = skinChooser.getSelectedSkin();
		if (selectedSkin == null) {
			settings.setValue("selectedSkin", "none");
		} else {
			settings.setValue("selectedSkin", selectedSkin);
		}
		if (selectedBodyFont != null) {
			settings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, selectedBodyFont.getFamily());
			settings.setValue(SettingsClass.MESSAGE_BODY_FONT_STYLE, selectedBodyFont.getStyle());
			settings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, selectedBodyFont.getSize());
		}
		if (selectedMessageListFont != null) {
			settings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, selectedMessageListFont.getFamily());
			settings.setValue(SettingsClass.MESSAGE_LIST_FONT_STYLE, selectedMessageListFont.getStyle());
			settings.setValue(SettingsClass.MESSAGE_LIST_FONT_SIZE, selectedMessageListFont.getSize());
		}
		if (selectedFileListFont != null) {
			settings.setValue(SettingsClass.FILE_LIST_FONT_NAME, selectedFileListFont.getFamily());
			settings.setValue(SettingsClass.FILE_LIST_FONT_STYLE, selectedFileListFont.getStyle());
			settings.setValue(SettingsClass.FILE_LIST_FONT_SIZE, selectedFileListFont.getSize());
		}
		settings.setValue("messageBodyAA", messageBodyAACheckBox.isSelected());
	}

}
