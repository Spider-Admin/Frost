/*
 * Created on Oct 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.preferences;

import java.awt.*;

import javax.swing.*;

import frost.SettingsClass;
import frost.util.gui.TextComponentClipboardMenu;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
class SearchPanel extends JPanel {
		
	private SettingsClass settings = null;
	private Language language = null;
		
	private JLabel archiveExtensionLabel = new JLabel();
		
	private JTextField archiveExtensionTextField = new JTextField();
	private JLabel audioExtensionLabel = new JLabel();
	private JTextField audioExtensionTextField = new JTextField();
	private JLabel documentExtensionLabel = new JLabel();
	private JTextField documentExtensionTextField = new JTextField();
	private JLabel executableExtensionLabel = new JLabel();
	private JTextField executableExtensionTextField = new JTextField();
		
	private JCheckBox hideAnonFilesCheckBox = new JCheckBox();
	private JCheckBox hideBadFilesCheckBox = new JCheckBox();
	private JLabel imageExtensionLabel = new JLabel();
	private JTextField imageExtensionTextField = new JTextField();
	private JLabel maxSearchResultsLabel = new JLabel();
	private JTextField maxSearchResultsTextField = new JTextField(8);
	private JLabel videoExtensionLabel = new JLabel();
	private JTextField videoExtensionTextField = new JTextField();
	
	/**
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected SearchPanel(SettingsClass settings) {
		super();
		
		this.language = Language.getInstance();
		this.settings = settings;
		
		initialize();
		loadSettings();
	}

	/**
	 * 
	 */
	private void initialize() {
		setName("SearchPanel");
		setLayout(new GridBagLayout());
		refreshLanguage();

		// We create the components
		new TextComponentClipboardMenu(archiveExtensionTextField, language);
		new TextComponentClipboardMenu(audioExtensionTextField, language);
		new TextComponentClipboardMenu(documentExtensionTextField, language);
		new TextComponentClipboardMenu(executableExtensionTextField, language);
			
		new TextComponentClipboardMenu(imageExtensionTextField, language);
		new TextComponentClipboardMenu(maxSearchResultsTextField, language);
		new TextComponentClipboardMenu(videoExtensionTextField, language);
		
		// Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1;
		constraints.gridwidth = 1;
			
		constraints.weightx = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(imageExtensionLabel, constraints);
		constraints.weightx = 1;
		constraints.gridx = 1;
		add(imageExtensionTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridy = 1;
		constraints.gridx = 0;
		add(videoExtensionLabel, constraints);
		constraints.weightx = 1;
		constraints.gridx = 1;
		add(videoExtensionTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridy = 2;
		constraints.gridx = 0;
		add(archiveExtensionLabel, constraints);
		constraints.weightx = 1;
		constraints.gridx = 1;
		add(archiveExtensionTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridy = 3;
		constraints.gridx = 0;
		add(documentExtensionLabel, constraints);
		constraints.weightx = 1;
		constraints.gridx = 1;
		add(documentExtensionTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridy = 4;
		constraints.gridx = 0;
		add(audioExtensionLabel, constraints);
		constraints.weightx = 1;
		constraints.gridx = 1;
		add(audioExtensionTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridy = 5;
		constraints.gridx = 0;
		add(executableExtensionLabel, constraints);
		constraints.weightx = 1;
		constraints.gridx = 1;
		add(executableExtensionTextField, constraints);
			
		constraints.weightx = 0;
		constraints.gridy = 6;
		constraints.gridx = 0;
		add(maxSearchResultsLabel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		add(maxSearchResultsTextField, constraints);
			
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridwidth = 2;
		constraints.gridy = 7;
		constraints.gridx = 0;
		add(hideBadFilesCheckBox, constraints);
		constraints.gridy = 8;
		add(hideAnonFilesCheckBox, constraints);

	}
		
	/**
	 * Loads the settings of this panel
	 */
	private void loadSettings() {
		audioExtensionTextField.setText(settings.getValue("audioExtension"));
		imageExtensionTextField.setText(settings.getValue("imageExtension"));
		videoExtensionTextField.setText(settings.getValue("videoExtension"));
		documentExtensionTextField.setText(settings.getValue("documentExtension"));
		executableExtensionTextField.setText(settings.getValue("executableExtension"));
		archiveExtensionTextField.setText(settings.getValue("archiveExtension"));
		maxSearchResultsTextField.setText(Integer.toString(settings.getIntValue("maxSearchResults")));
		hideBadFilesCheckBox.setSelected(settings.getBoolValue("hideBadFiles"));
		hideAnonFilesCheckBox.setSelected(settings.getBoolValue("hideAnonFiles"));
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
		imageExtensionLabel.setText(language.getString("Image Extension"));
		videoExtensionLabel.setText(language.getString("Video Extension"));
		archiveExtensionLabel.setText(language.getString("Archive Extension"));
		documentExtensionLabel.setText(language.getString("Document Extension"));
		audioExtensionLabel.setText(language.getString("Audio Extension"));
		executableExtensionLabel.setText(language.getString("Executable Extension"));
		maxSearchResultsLabel.setText(language.getString("Maximum search results"));
			
		hideBadFilesCheckBox.setText(language.getString("Hide files from people marked BAD"));
		hideAnonFilesCheckBox.setText(language.getString("Hide files from anonymous users"));
	}
		
	/**
	 * Save the settings of this panel
	 */
	private void saveSettings() {
		settings.setValue("audioExtension", audioExtensionTextField.getText().toLowerCase());
		settings.setValue("imageExtension", imageExtensionTextField.getText().toLowerCase());
		settings.setValue("videoExtension", videoExtensionTextField.getText().toLowerCase());
		settings.setValue("documentExtension", documentExtensionTextField.getText().toLowerCase());
		settings.setValue("executableExtension", executableExtensionTextField.getText().toLowerCase());
		settings.setValue("archiveExtension", archiveExtensionTextField.getText().toLowerCase());
		settings.setValue("maxSearchResults", maxSearchResultsTextField.getText());

		settings.setValue("hideBadFiles", hideBadFilesCheckBox.isSelected());
		settings.setValue("hideAnonFiles", hideAnonFilesCheckBox.isSelected());
	}
		
}
