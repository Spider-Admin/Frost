/*
 * Created on Oct 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import frost.SettingsClass;
import frost.util.gui.*;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
class UploadPanel extends JPanel {

	/**
	 * 
	 */
	private class Listener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == disableRequestsCheckBox) {
				disableUploadsPressed();
			}
		}
	}
	
	private SettingsClass settings = null;
	private Language language = null;
	
	private JCheckBox automaticIndexingCheckBox = new JCheckBox();
	private JLabel batchSizeExplanationLabel = new JLabel();
	private JLabel batchSizeLabel = new JLabel();
	private JClipboardTextField batchSizeTextField;
		
	private JCheckBox disableRequestsCheckBox = new JCheckBox();
	private JCheckBox helpFriendsCheckBox = new JCheckBox();
	private JLabel htlExplanationLabel = new JLabel();
		
	private JLabel htlLabel = new JLabel();
	
	private JClipboardTextField htlTextField;
	private JLabel indexFileRedundancyExplanationLabel = new JLabel();
	private JLabel indexFileRedundancyLabel = new JLabel();
	private JClipboardTextField indexFileRedundancyTextField;
		
	private Listener listener = new Listener();
	private JCheckBox shareDownloadsCheckBox = new JCheckBox();
	private JCheckBox signUploadsCheckBox = new JCheckBox();
	private JLabel splitfileThreadsExplanationLabel = new JLabel();
	private JLabel splitfileThreadsLabel = new JLabel();
	private JClipboardTextField splitfileThreadsTextField;
	private JLabel threadsLabel = new JLabel();
	private JClipboardTextField threadsTextField;

	/**
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected UploadPanel(SettingsClass settings) {
		super();
		
		this.language = Language.getInstance();
		this.settings = settings;
		
		initialize();
		loadSettings();
	}
		
	/**
	 * 
	 */
	private void disableUploadsPressed() {
		// enable panel if checkbox is not selected
		setEnabled(!disableRequestsCheckBox.isSelected());
	}

	/**
	 * 
	 */
	private void initialize() {
		setName("UploadPanel");
		setLayout(new GridBagLayout());
		refreshLanguage();

		// We create the components
		batchSizeTextField = new JClipboardTextField(8, language);
		htlTextField = new JClipboardTextField(8, language);
		indexFileRedundancyTextField = new JClipboardTextField(8, language);
		splitfileThreadsTextField = new JClipboardTextField(8, language);
		threadsTextField = new JClipboardTextField(8, language);
		
		//Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Insets insets0555 = new Insets(0, 5, 5, 5);
		Insets insets5555 = new Insets(5, 5, 5, 5);
		Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
		constraints.weighty = 1;
			
		constraints.weightx = 1;
		constraints.gridwidth = 3;
		constraints.insets = insets0555;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(disableRequestsCheckBox, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		add(automaticIndexingCheckBox, constraints);
		constraints.insets = insets5555;			
		constraints.gridx = 2;
		constraints.gridwidth = 1;
		add(shareDownloadsCheckBox, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		add(signUploadsCheckBox, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 2;
		constraints.gridwidth = 1;
		add(helpFriendsCheckBox, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(htlLabel, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 1;
		add(htlTextField, constraints);
		constraints.gridx = 2;
		add(htlExplanationLabel, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 4;
		add(threadsLabel, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 1;
		add(threadsTextField, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 5;
		add(splitfileThreadsLabel, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 1;
		add(splitfileThreadsTextField, constraints);
		constraints.gridx = 2;
		add(splitfileThreadsExplanationLabel, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 6;
		add(batchSizeLabel, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 1;
		add(batchSizeTextField, constraints);
		constraints.gridx = 2;
		add(batchSizeExplanationLabel, constraints);
			
		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 7;
		add(indexFileRedundancyLabel, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 1;
		add(indexFileRedundancyTextField, constraints);
		constraints.gridx = 2;
		add(indexFileRedundancyExplanationLabel, constraints);

		// Add listeners
		disableRequestsCheckBox.addActionListener(listener);

	}
		
	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		signUploadsCheckBox.setSelected(settings.getBoolValue("signUploads"));
		helpFriendsCheckBox.setSelected(settings.getBoolValue("helpFriends"));
		automaticIndexingCheckBox.setSelected(settings.getBoolValue("automaticIndexing"));
		shareDownloadsCheckBox.setSelected(settings.getBoolValue("shareDownloads"));
		htlTextField.setText(settings.getValue("htlUpload"));
		threadsTextField.setText(settings.getValue("uploadThreads"));
		batchSizeTextField.setText(settings.getValue("uploadBatchSize"));
		indexFileRedundancyTextField.setText(settings.getValue("indexFileRedundancy"));
		splitfileThreadsTextField.setText(settings.getValue("splitfileUploadThreads"));
		disableRequestsCheckBox.setSelected(settings.getBoolValue(SettingsClass.DISABLE_REQUESTS));

		setEnabled(!disableRequestsCheckBox.isSelected());
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
		disableRequestsCheckBox.setText(language.getString("Disable uploads"));
		signUploadsCheckBox.setText(language.getString("Sign shared files"));
		automaticIndexingCheckBox.setText(language.getString("Automatic Indexing"));
		shareDownloadsCheckBox.setText(language.getString("Share Downloads"));
		helpFriendsCheckBox.setText(
				language.getString("Help spread files from people marked GOOD"));
		htlLabel.setText(language.getString("Upload HTL") + " (8)");
		htlExplanationLabel.setText(language.getString("up htl explanation"));
		threadsLabel.setText(
				language.getString("Number of simultaneous uploads") + " (3)");
		splitfileThreadsLabel.setText(
				language.getString("Number of splitfile threads") + " (15)");
		splitfileThreadsExplanationLabel.setText(
				language.getString("splitfile explanation"));
		batchSizeLabel.setText(language.getString("Upload batch size"));
		batchSizeExplanationLabel.setText(language.getString("batch explanation"));
		indexFileRedundancyLabel.setText(language.getString("Index file redundancy"));
		indexFileRedundancyExplanationLabel.setText(
				language.getString("redundancy explanation"));
	}
		
	/**
	 * Save the settings of this panel 
	 */
	private void saveSettings() {
		settings.setValue("htlUpload", htlTextField.getText());
		settings.setValue("uploadThreads", threadsTextField.getText());
		settings.setValue("uploadBatchSize", batchSizeTextField.getText());
		settings.setValue("indexFileRedundancy", indexFileRedundancyTextField.getText());
		settings.setValue("splitfileUploadThreads", splitfileThreadsTextField.getText());
		settings.setValue(SettingsClass.DISABLE_REQUESTS, disableRequestsCheckBox.isSelected());
		settings.setValue("signUploads", signUploadsCheckBox.isSelected());
		settings.setValue("automaticIndexing", automaticIndexingCheckBox.isSelected());
		settings.setValue("shareDownloads", shareDownloadsCheckBox.isSelected());
		settings.setValue("helpFriends", helpFriendsCheckBox.isSelected());
	}
		
	/* (non-Javadoc)
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		ArrayList exceptions = new ArrayList();
		exceptions.add(disableRequestsCheckBox);
		MiscToolkit.getInstance().setContainerEnabled(this, enabled, exceptions);
	}
}
