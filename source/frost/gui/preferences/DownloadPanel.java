/*
 * Created on Oct 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

import frost.SettingsClass;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author $author$
 * @version $revision$
 */
class DownloadPanel extends JPanel {

	/**
	 * 
	 */
	public class Listener implements ChangeListener, ActionListener {

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
			if (e.getSource() == disableDownloadsCheckBox) {
				refreshComponentsState();
			}
			if (e.getSource() == browseDirectoryButton) {
				browseDirectoryPressed();
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == enableRequestingCheckBox) {
				enableRequestingChanged();
			}
			if (e.getSource() == restartFailedDownloadsCheckBox) {
				restartFailedDownloadsChanged();
			}

		}

	}
	
	private JDialog owner = null;
	private SettingsClass settings = null;
	private UpdatingLanguageResource languageResource = null;

	private JButton browseDirectoryButton = new JButton();
	private JCheckBox decodeAfterEachSegmentCheckBox = new JCheckBox();
	private JLabel directoryLabel = new JLabel();

	private JTextField directoryTextField = new JTextField();

	private JCheckBox disableDownloadsCheckBox = new JCheckBox();
	private JCheckBox enableRequestingCheckBox = new JCheckBox();

	private Listener listener = new Listener();
	private JLabel maxRetriesLabel = new JLabel();
	private JTextField maxRetriesTextField = new JTextField(8);
	private JCheckBox removeFinishedDownloadsCheckBox = new JCheckBox();
	private JLabel requestAfterTriesLabel = new JLabel();
	private JTextField requestAfterTriesTextField = new JTextField(8);
	private JCheckBox restartFailedDownloadsCheckBox = new JCheckBox();
	private JLabel splitfileThreadsLabel = new JLabel();
	private JTextField splitfileThreadsTextField = new JTextField(8);
	private JTextField threadsTextField = new JTextField(8);
	private JLabel threadsTextLabel = new JLabel();
	private JCheckBox tryAllSegmentsCheckBox = new JCheckBox();

	private JLabel waitTimeLabel = new JLabel();
	private JTextField waitTimeTextField = new JTextField(8);

	/**
	 * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
	 * @param languageResource the LanguageResource to get localized strings from
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
	 */
	protected DownloadPanel(JDialog owner, UpdatingLanguageResource languageResource, SettingsClass settings) {
		super();
		
		this.owner = owner;
		this.languageResource = languageResource;
		this.settings = settings;
		
		initialize();
		loadSettings();
	}

	/**
	 * browseDownloadDirectoryButton Action Listener (Downloads / Browse)
	 */
	private void browseDirectoryPressed() {
		final JFileChooser fc = new JFileChooser(settings.getValue("lastUsedDirectory"));
		fc.setDialogTitle(languageResource.getString("Select download directory"));
		fc.setFileHidingEnabled(true);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileSeparator = System.getProperty("file.separator");
			File file = fc.getSelectedFile();
			settings.setValue("lastUsedDirectory", file.getParent());
			directoryTextField.setText(file.getPath() + fileSeparator);
		}
	}

	/**
	 * 
	 */
	private void enableRequestingChanged() {
		requestAfterTriesTextField.setEnabled(enableRequestingCheckBox.isSelected());
		requestAfterTriesLabel.setEnabled(enableRequestingCheckBox.isSelected());
	}

	private JPanel getRequestPanel() {
		JPanel subPanel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		subPanel.add(requestAfterTriesLabel, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0;
		subPanel.add(requestAfterTriesTextField, constraints);

		return subPanel;
	}

	private JPanel getRetriesPanel() {
		JPanel subPanel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		subPanel.add(maxRetriesLabel, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0;
		subPanel.add(maxRetriesTextField, constraints);

		constraints.gridy = 1;
		constraints.gridx = 0;
		constraints.weightx = 1;
		subPanel.add(waitTimeLabel, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0;
		subPanel.add(waitTimeTextField, constraints);

		return subPanel;
	}

	/**
	 * 
	 */
	private void initialize() {
		setName("DownloadPanel");
		setLayout(new GridBagLayout());
		refreshLanguage();

		//Adds all of the components			
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Insets insets0555 = new Insets(0, 5, 5, 5);
		Insets insets5555 = new Insets(5, 5, 5, 5);
		Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
		constraints.weighty = 1;

		constraints.gridwidth = 4;
		constraints.insets = insets0555;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(disableDownloadsCheckBox, constraints);

		constraints.insets = insets5_30_5_5;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		constraints.gridwidth = 1;
		add(directoryLabel, constraints);
		constraints.insets = insets5555;
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.gridwidth = 2;
		add(directoryTextField, constraints);
		constraints.gridx = 3;
		constraints.weightx = 0.1;
		constraints.gridwidth = 1;
		add(browseDirectoryButton, constraints);

		constraints.insets = insets5_30_5_5;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		add(restartFailedDownloadsCheckBox, constraints);
		constraints.gridwidth = 3;
		constraints.insets = insets5555;
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		add(getRetriesPanel(), constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 4;
		constraints.insets = insets5_30_5_5;
		constraints.gridy = 3;
		constraints.gridx = 0;
		add(enableRequestingCheckBox, constraints);
		constraints.gridwidth = 3;
		constraints.insets = insets5555;
		constraints.gridy = 4;
		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(getRequestPanel(), constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.insets = insets5_30_5_5;
		constraints.gridy = 5;
		constraints.gridx = 0;
		constraints.weightx = 1;
		add(threadsTextLabel, constraints);
		constraints.gridx = 1;
		constraints.insets = insets5555;
		constraints.weightx = 0;
		add(threadsTextField, constraints);

		constraints.insets = insets5_30_5_5;
		constraints.gridy = 6;
		constraints.gridx = 0;
		constraints.weightx = 1;
		add(splitfileThreadsLabel, constraints);
		constraints.gridx = 1;
		constraints.insets = insets5555;
		constraints.weightx = 0;
		add(splitfileThreadsTextField, constraints);

		constraints.insets = insets5_30_5_5;
		constraints.gridwidth = 4;
		constraints.gridy = 7;
		constraints.gridx = 0;
		constraints.weightx = 1;
		add(removeFinishedDownloadsCheckBox, constraints);
		constraints.gridy = 8;
		add(tryAllSegmentsCheckBox, constraints);
		constraints.gridy = 9;
		add(decodeAfterEachSegmentCheckBox, constraints);

		// Add listeners
		enableRequestingCheckBox.addChangeListener(listener);
		restartFailedDownloadsCheckBox.addChangeListener(listener);
		disableDownloadsCheckBox.addActionListener(listener);
		browseDirectoryButton.addActionListener(listener);
	}

	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		removeFinishedDownloadsCheckBox.setSelected(settings.getBoolValue("removeFinishedDownloads"));
		directoryTextField.setText(settings.getValue("downloadDirectory"));
		threadsTextField.setText(settings.getValue("downloadThreads"));
		splitfileThreadsTextField.setText(settings.getValue("splitfileDownloadThreads"));
		disableDownloadsCheckBox.setSelected(settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS));
		restartFailedDownloadsCheckBox.setSelected(settings.getBoolValue("downloadRestartFailedDownloads"));
		enableRequestingCheckBox.setSelected(settings.getBoolValue("downloadEnableRequesting"));
		requestAfterTriesTextField.setText("" + settings.getIntValue("downloadRequestAfterTries"));
		maxRetriesTextField.setText("" + settings.getIntValue("downloadMaxRetries"));
		waitTimeTextField.setText("" + settings.getIntValue("downloadWaittime"));
		tryAllSegmentsCheckBox.setSelected(settings.getBoolValue("downloadTryAllSegments"));
		decodeAfterEachSegmentCheckBox.setSelected(settings.getBoolValue("downloadDecodeAfterEachSegment"));

		refreshComponentsState();
	}

	public void ok() {
		saveSettings();
	}

	/**
	 * 
	 */
	private void refreshComponentsState() {
		boolean downloadsEnabled = !disableDownloadsCheckBox.isSelected();
		if (downloadsEnabled) {
			setEnabled(true);
			requestAfterTriesTextField.setEnabled(enableRequestingCheckBox.isSelected());
			maxRetriesTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			waitTimeTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			requestAfterTriesLabel.setEnabled(enableRequestingCheckBox.isSelected());
			maxRetriesLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			waitTimeLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
		} else {
			setEnabled(false);
		}
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		String off = languageResource.getString("Off");
		String on = languageResource.getString("On");
		String minutes = languageResource.getString("minutes");
		removeFinishedDownloadsCheckBox.setText(
			languageResource.getString("Remove finished downloads every 5 minutes") + " (" + off + ")");
		restartFailedDownloadsCheckBox.setText(languageResource.getString("Restart failed downloads"));
		waitTimeLabel.setText(languageResource.getString("Waittime after each try") + " (" + minutes + "): ");
		maxRetriesLabel.setText(languageResource.getString("Maximum number of retries") + ": ");
		requestAfterTriesLabel.setText(languageResource.getString("Request file after this count of retries") + ": ");
		enableRequestingCheckBox.setText(
			languageResource.getString("Enable requesting of failed download files") + " (" + on + ")");
		tryAllSegmentsCheckBox.setText(
			languageResource.getString("Try to download all segments, even if one fails") + " (" + on + ")");
		decodeAfterEachSegmentCheckBox.setText(
			languageResource.getString("Decode each segment immediately after its download"));
		disableDownloadsCheckBox.setText(languageResource.getString("Disable downloads"));

		directoryLabel.setText(languageResource.getString("Download directory") + ": ");
		browseDirectoryButton.setText(languageResource.getString("Browse") + "...");
		threadsTextLabel.setText(languageResource.getString("Number of simultaneous downloads") + " (3)");
		splitfileThreadsLabel.setText(languageResource.getString("Number of splitfile threads") + " (30)");
	}

	/**
	 * 
	 */
	private void restartFailedDownloadsChanged() {
		maxRetriesTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
		maxRetriesLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
		waitTimeTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
		waitTimeLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
	}

	/**
	 * Save the settings of this panel
	 */
	private void saveSettings() {
		String downlDirTxt = directoryTextField.getText();
		String filesep = System.getProperty("file.separator");
		// always append a fileseparator to the end of string
		if ((!(downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)))
			|| downlDirTxt.lastIndexOf(filesep) < 0) {
				settings.setValue("downloadDirectory", downlDirTxt + filesep);
		} else {
			settings.setValue("downloadDirectory", downlDirTxt);
		}
		settings.setValue("downloadThreads", threadsTextField.getText());
		settings.setValue("removeFinishedDownloads", removeFinishedDownloadsCheckBox.isSelected());

		settings.setValue("splitfileDownloadThreads", splitfileThreadsTextField.getText());
		settings.setValue(SettingsClass.DISABLE_DOWNLOADS, disableDownloadsCheckBox.isSelected());
		settings.setValue("downloadRestartFailedDownloads", restartFailedDownloadsCheckBox.isSelected());
		settings.setValue("downloadEnableRequesting", enableRequestingCheckBox.isSelected());
		settings.setValue("downloadRequestAfterTries", requestAfterTriesTextField.getText());
		settings.setValue("downloadMaxRetries", maxRetriesTextField.getText());
		settings.setValue("downloadWaittime", waitTimeTextField.getText());
		settings.setValue("downloadTryAllSegments", tryAllSegmentsCheckBox.isSelected());
		settings.setValue("downloadDecodeAfterEachSegment", decodeAfterEachSegmentCheckBox.isSelected());
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		ArrayList exceptions = new ArrayList();
		exceptions.add(disableDownloadsCheckBox);
		MiscToolkit.getInstance().setContainerEnabled(this, enabled, exceptions);
	}

}
