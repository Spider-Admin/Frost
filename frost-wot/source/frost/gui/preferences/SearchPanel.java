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
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author $author$
 * @version $revision$
 */
class SearchPanel extends JPanel {
		
	private SettingsClass settings = null;
	private UpdatingLanguageResource languageResource = null;
		
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
	 * @param languageResource the LanguageResource to get localized strings from
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected SearchPanel(UpdatingLanguageResource languageResource, SettingsClass settings) {
		super();
		
		this.languageResource = languageResource;
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
		
	public void ok() {
		saveSettings();
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		imageExtensionLabel.setText(languageResource.getString("Image Extension"));
		videoExtensionLabel.setText(languageResource.getString("Video Extension"));
		archiveExtensionLabel.setText(languageResource.getString("Archive Extension"));
		documentExtensionLabel.setText(languageResource.getString("Document Extension"));
		audioExtensionLabel.setText(languageResource.getString("Audio Extension"));
		executableExtensionLabel.setText(languageResource.getString("Executable Extension"));
		maxSearchResultsLabel.setText(languageResource.getString("Maximum search results"));
			
		hideBadFilesCheckBox.setText(languageResource.getString("Hide files from people marked BAD"));
		hideAnonFilesCheckBox.setText(languageResource.getString("Hide files from anonymous users"));
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
