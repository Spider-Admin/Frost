/*
  OptionsFrame.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
--------------------------------------------------------------------------
  DESCRIPTION:
  This file contains the whole 'Options' dialog. It first reads the
  actual config from properties file, and on 'OK' it saves all
  settings to the properties file and informs the caller to reload
  this file.
*/
package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import frost.*;

/*******************************
 * TODO: - add thread listeners (listen to all running threads) to change the
 *         updating state (bold text in table row) on demand (from bback)
 *******************************/

public class OptionsFrame extends JDialog implements ListSelectionListener {

	/**
	 * Display Panel. Contains appearace options: skins and more in the future
	 */
	private class DisplayPanel extends JPanel {

		private SkinChooser skinChooser = null;
		private BorderLayout displayPanelBorderLayout = null;
		private JLabel moreSkinsLabel = null;

		/**
		 * Constructor
		 */
		public DisplayPanel() {
			super();
			initialize();
		}

		/**
		 * Initialize the class.
		 */
		private void initialize() {
			setName("DisplayPanel");
			setLayout(getDisplayPanelBorderLayout());
			add(getSkinChooser(), "Center");
			add(getMoreSkinsLabel(), "South");
		}

		public void ok() {
			getSkinChooser().commitChanges();
			saveSettings(frostSettings);
		}

		public void cancel() {
			getSkinChooser().cancelChanges();
		}

		/**
		 * Return the DisplayPanelBorderLayout property value.
		 * @return java.awt.BorderLayout
		 */
		private BorderLayout getDisplayPanelBorderLayout() {
			if (displayPanelBorderLayout == null) {
				/* Create part */
				displayPanelBorderLayout = new BorderLayout();
				displayPanelBorderLayout.setVgap(10);
			}
			return displayPanelBorderLayout;
		}

		/**
		 * Return the SkinChooser property value.
		 * @return pruebasSkins.SkinChooser
		 */
		private SkinChooser getSkinChooser() {
			if (skinChooser == null) {
				skinChooser = new SkinChooser();
				skinChooser.setName("SkinChooser");
			}
			return skinChooser;
		}
		
		/**
		 * Return the SkinChooser property value.
		 * @return pruebasSkins.SkinChooser
		 */
		private JLabel getMoreSkinsLabel() {
			if (moreSkinsLabel == null) {
				LangRes.getString("MoreSkinsAt");
				moreSkinsLabel = new JLabel(LangRes.getString("MoreSkinsAt") + " http://javootoo.l2fprod.com/plaf/skinlf/");
				moreSkinsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			return moreSkinsLabel;
		}

		/** 
		 * Save the settings of this panel
		 * @param displaySettings class where the settings will be stored
		 */
		private void saveSettings(SettingsClass displaySettings) {
			boolean skinsEnabled = getSkinChooser().isSkinsEnabled();
			displaySettings.setValue("skinsEnabled", skinsEnabled);

			String selectedSkin = getSkinChooser().getSelectedSkin();
			if (selectedSkin == null) {
				displaySettings.setValue("selectedSkin", "none");
			} else {
				displaySettings.setValue("selectedSkin", selectedSkin);
			}
		}

		/**
		 * Load the settings of this panel
		 * @param displaySettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass displaySettings) {
			boolean skinsEnabled = displaySettings.getBoolValue("skinsEnabled");
			getSkinChooser().setSkinsEnabled(skinsEnabled);
			String selectedSkinPath = displaySettings.getValue("selectedSkin");
			getSkinChooser().setSelectedSkin(selectedSkinPath);
		}

	}
	//------------------------------------------------------------------------
	// Class Vars
	//------------------------------------------------------------------------

	static java.util.ResourceBundle LangRes =
		java.util.ResourceBundle.getBundle("res.LangRes");
	SettingsClass frostSettings;

	boolean exitState;

	//------------------------------------------------------------------------
	// Generate objects
	//------------------------------------------------------------------------
	JPanel mainPanel = null;
	JPanel buttonPanel = null; // OK / Cancel
	JPanel downloadPanel = null;
	JPanel uploadPanel = null;
	JPanel tofPanel = null;
	JPanel tof2Panel = null;
	JPanel tof3Panel = null;
	DisplayPanel displayPanel = null;
	JPanel miscPanel = null;
	JPanel searchPanel = null;
	JPanel contentAreaPanel = null;
	JPanel optionsGroupsPanel = null;

	JTextArea tofTextArea = new JTextArea(4, 50);

	JTextField downloadDirectoryTextField = new JTextField(30);
	//    JTextField downloadMinHtlTextField = new JTextField(5);
	//    JTextField downloadMaxHtlTextField = new JTextField(5);
	JTextField downloadThreadsTextField = new JTextField(5);
	JTextField downloadSplitfileThreadsTextField = new JTextField(5);
	JTextField uploadHtlTextField = new JTextField(5);
	JTextField uploadThreadsTextField = new JTextField(5);
	JTextField uploadSplitfileThreadsTextField = new JTextField(5);
	JTextField uploadBatchSizeTextField = new JTextField(4);
	JTextField indexFileRedundancyTextField = new JTextField(1);
	JTextField tofUploadHtlTextField = new JTextField(5);
	JTextField tofDownloadHtlTextField = new JTextField(5);
	JTextField tofDisplayDaysTextField = new JTextField(5);
	JTextField tofDownloadDaysTextField = new JTextField(5);
	JTextField tofMessageBaseTextField = new JTextField(8);
	JTextField tofBlockMessageTextField = new JTextField(42);
	JTextField tofBlockMessageBodyTextField = new JTextField(42);
	JTextField miscKeyUploadHtlTextField = new JTextField(5);
	JTextField miscKeyDownloadHtlTextField = new JTextField(5);
	JTextField miscAvailableNodesTextField = new JTextField(35);
	//JTextField miscNodePortTextField = new JTextField(8);
	JTextField miscMaxKeysTextField = new JTextField(8);
	JTextField miscAltEditTextField = new JTextField(30);
	JTextField miscAutoSaveInterval = new JTextField(5);
	JCheckBox miscShowSystrayIcon = new JCheckBox();
	JTextField searchAudioExtensionTextField = new JTextField(30);
	JTextField searchVideoExtensionTextField = new JTextField(30);
	JTextField searchDocumentExtensionTextField = new JTextField(30);
	JTextField searchExecutableExtensionTextField = new JTextField(30);
	JTextField searchImageExtensionTextField = new JTextField(30);
	JTextField searchArchiveExtensionTextField = new JTextField(30);
	JTextField searchMaxSearchResults = new JTextField(8);

	JTextField TFautomaticUpdate_boardsMinimumUpdateInterval =
		new JTextField(5);
	JTextField TFautomaticUpdate_concurrentBoardUpdates = new JTextField(5);
	JCheckBox tofBoardUpdateVisualization = new JCheckBox();
	JCheckBox allowEvilBertCheckBox = new JCheckBox();
	JCheckBox miscAltEditCheckBox = new JCheckBox();
	JCheckBox miscSplashscreenCheckBox = new JCheckBox();
	JCheckBox signUploads = new JCheckBox();
	JCheckBox automaticIndexing = new JCheckBox();
	JCheckBox shareDownloads = new JCheckBox();
	JCheckBox helpFriends = new JCheckBox();
	JCheckBox hideBadFiles = new JCheckBox();
	JCheckBox hideAnonFiles = new JCheckBox();
	JCheckBox downloadRemoveFinishedDownloads = new JCheckBox();
	JCheckBox downloadRestartFailedDownloads = new JCheckBox();
	JTextField downloadRequestAfterTries = new JTextField(5);
	JCheckBox downloadEnableRequesting = new JCheckBox();
	JTextField downloadMaxRetries = new JTextField(5);
	JTextField downloadWaittime = new JTextField(5);
	JLabel downloadWaittimeLabel = new JLabel();
	JLabel downloadMaxRetriesLabel = new JLabel();
	JLabel downloadRequestAfterTriesLabel = new JLabel();

	JCheckBox downloadTryAllSegments = new JCheckBox();
	JCheckBox downloadDecodeAfterEachSegment = new JCheckBox();

	JList optionsGroupsList = null;

	// new options in WOT:
	// TODO: translation
	JTextField sampleInterval = new JTextField(5);
	JTextField spamTreshold = new JTextField(5);

	JCheckBox uploadDisableRequests = new JCheckBox();
	JCheckBox downloadDisableDownloads = new JCheckBox();
	JCheckBox signedOnly = new JCheckBox();
	JCheckBox hideBadMessages = new JCheckBox();
	JCheckBox hideCheckMessages = new JCheckBox();
	JCheckBox hideNAMessages = new JCheckBox();
	JCheckBox block = new JCheckBox();
	// TODO: translate
	JCheckBox blockBody = new JCheckBox();
	JCheckBox doBoardBackoff = new JCheckBox();
	JLabel interval = new JLabel();
	JLabel treshold = new JLabel();
	//    JLabel startRequestingAfterHtlLabel = new JLabel(LangRes.getString("Insert request if HTL tops:") + " (10)");
	JCheckBox cleanUP = new JCheckBox();

	JButton chooseBoardUpdSelectedBackgroundColor = new JButton("   ");
	JButton chooseBoardUpdNonSelectedBackgroundColor = new JButton("   ");

	Color boardUpdSelectedBackgroundColor = null;
	Color boardUpdNonSelectedBackgroundColor = null;

	// this vars hold some settings from start of dialog to the end.
	// then its checked if the settings are changed by user
	boolean checkDisableRequests;
	String checkMaxMessageDisplay;
	boolean checkSignedOnly;
	boolean checkHideBadMessages;
	boolean checkHideCheckMessages;
	boolean checkHideNAMessages;
	boolean checkBlock;
	boolean checkBlockBody;
	// the result of this
	boolean shouldRemoveDummyReqFiles = false;
	boolean shouldReloadMessages = false;
	boolean _signUploads, _helpFriends, _hideBad, _hideAnon;

	/**
	 * These translate* methods are used to apply translatable
	 * information to the GUI objects. If you add/remove GUI
	 * objects that use text, please update these methods. Do
	 * not apply text anywhere else.
	 */
	private void translateCheckBox() {
		miscSplashscreenCheckBox.setText(
			LangRes.getString("Disable splashscreen"));
		miscShowSystrayIcon.setText(LangRes.getString("Show systray icon"));
		downloadEnableRequesting.setText(
			LangRes.getString("Enable requesting of failed download files")
				+ " ("
				+ LangRes.getString("On") + ")");
		downloadTryAllSegments.setText(
			LangRes.getString(
				"Try to download all segments, even if one fails")
				+ " ("
				+ LangRes.getString("On") + ")");
		downloadDecodeAfterEachSegment.setText(
			LangRes.getString(
				"Decode each segment immediately after its download"));
		tofBoardUpdateVisualization.setText(
			LangRes.getString("Show board update visualization") + " (" + LangRes.getString("On") + ")");
		allowEvilBertCheckBox.setText(
			LangRes.getString("Allow 2 byte characters")
				+ " ("
				+ LangRes.getString("Off") + ")");
		miscAltEditCheckBox.setText(
			LangRes.getString("Use editor for writing messages")
				+ " ("
				+ LangRes.getString("Off") + ")");
		signUploads.setText(LangRes.getString("Sign shared files"));
		automaticIndexing.setText(LangRes.getString("Automatic Indexing"));
		shareDownloads.setText(LangRes.getString("Share Downloads"));
		helpFriends.setText(
			LangRes.getString("Help spread files from people marked GOOD"));
		hideBadFiles.setText(
			LangRes.getString("Hide files from people marked BAD"));
		hideAnonFiles.setText(
			LangRes.getString("Hide files from anonymous users"));
		downloadRemoveFinishedDownloads.setText(
			LangRes.getString("Remove finished downloads every 5 minutes")
				+ " ("
				+ LangRes.getString("Off") + ")");
		downloadRestartFailedDownloads.setText(
			LangRes.getString("Restart failed downloads"));
		uploadDisableRequests.setText(LangRes.getString("Disable uploads"));
		downloadDisableDownloads.setText(
			LangRes.getString("Disable downloads"));
		signedOnly.setText(LangRes.getString("Hide unsigned messages"));
		hideBadMessages.setText(
			LangRes.getString("Hide messages flagged BAD")
				+ " ("
				+ LangRes.getString("Off") + ")");
		hideCheckMessages.setText(
			LangRes.getString("Hide messages flagged CHECK")
				+ " ("
				+ LangRes.getString("Off") + ")");
		hideNAMessages.setText(
			LangRes.getString("Hide messages flagged N/A")
				+ " ("
				+ LangRes.getString("Off") + ")");
		block.setText(
			LangRes.getString(
				"Block messages with subject containing (separate by ';' )") + ": ");
		blockBody.setText(
			LangRes.getString(
				"Block messages with body containing (separate by ';' )") + ": ");
		doBoardBackoff.setText(LangRes.getString("Do spam detection"));
		cleanUP.setText(LangRes.getString("Clean the keypool"));
	}
	private void translateLabel() {
		downloadWaittimeLabel.setText(
			LangRes.getString("Waittime after each try") + " ("  + LangRes.getString("minutes") + "): ");
		downloadMaxRetriesLabel.setText(
			LangRes.getString("Maximum number of retries") + ": ");
		downloadRequestAfterTriesLabel.setText(
			LangRes.getString("Request file after this count of retries")
				+ ": ");
		interval.setText(LangRes.getString("Sample interval") + " (" + LangRes.getString("hours") + ")");
		treshold.setText(LangRes.getString("Threshold of blocked messages"));
	}

	/**
	 * Build up the whole GUI.
	 */
	private void Init() throws Exception {
		//------------------------------------------------------------------------
		// Configure objects
		//------------------------------------------------------------------------
		this.setTitle(LangRes.getString("Options"));
		// a program should always give users a chance to change the dialog size if needed
		this.setResizable(true);

		//------------------------------------------------------------------------
		// ChangeListener
		//------------------------------------------------------------------------
		miscAltEditCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(miscAltEditCheckBox))
					miscAltEditTextField.setEditable(
						miscAltEditCheckBox.isSelected());
			}
		});
		block.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(block))
					tofBlockMessageTextField.setEnabled(block.isSelected());
			}
		});
		blockBody.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(blockBody))
					tofBlockMessageBodyTextField.setEnabled(
						blockBody.isSelected());
			}
		});
		doBoardBackoff.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(doBoardBackoff)) {
					sampleInterval.setEnabled(doBoardBackoff.isSelected());
					spamTreshold.setEnabled(doBoardBackoff.isSelected());
					treshold.setEnabled(doBoardBackoff.isSelected());
					interval.setEnabled(doBoardBackoff.isSelected());
				}
			}
		});
		//------------------------------------------------------------------------

		mainPanel = new JPanel(new BorderLayout());
		this.getContentPane().add(mainPanel, null); // add Main panel

		// prepare content area panel
		contentAreaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentAreaPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		contentAreaPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 0, 5, 5),
				contentAreaPanel.getBorder()));

		mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		mainPanel.add(getOptionsGroupsPanel(), BorderLayout.WEST);

		// compute and set size of contentAreaPanel
		Dimension neededSize = computeMaxSize(optionsGroupsList.getModel());
		contentAreaPanel.setMinimumSize(neededSize);
		contentAreaPanel.setPreferredSize(neededSize);

		mainPanel.add(contentAreaPanel, BorderLayout.CENTER);
	}

	/**
	 * Computes the maximum width and height of the various options panels.
	 * Returns Dimension with max. x and y that is needed.
	 * Gets all panels from the ListModel of the option groups list.
	 */
	protected Dimension computeMaxSize(ListModel m) {
		if (m == null || m.getSize() == 0)
			return null;
		int maxX = -1;
		int maxY = -1;
		// misuse a JDialog to determine the panel size before showing
		JDialog dlgdummy = new JDialog();
		for (int x = 0; x < m.getSize(); x++) {
			ListBoxData lbdata = (ListBoxData) m.getElementAt(x);
			JPanel aPanel = lbdata.getPanel();

			contentAreaPanel.removeAll();
			contentAreaPanel.add(aPanel, BorderLayout.CENTER);
			dlgdummy.setContentPane(contentAreaPanel);
			dlgdummy.pack();
			// get size (including bordersize from contentAreaPane)
			int tmpX = contentAreaPanel.getWidth();
			int tmpY = contentAreaPanel.getHeight();
			maxX = Math.max(maxX, tmpX);
			maxY = Math.max(maxY, tmpY);
		}
		dlgdummy = null; // give some hint to gc() , in case its needed
		contentAreaPanel.removeAll();
		return new Dimension(maxX, maxY);
	}

	/**
	 * Build the panel containing the list of option groups.
	 */
	protected JPanel getOptionsGroupsPanel() {
		if (optionsGroupsPanel == null) {
			// init the list
			Vector listData = new Vector();
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("Downloads") + " ",
					getDownloadPanel()));
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("Uploads") + " ",
					getUploadPanel()));
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("News") + " (1) ",
					getTofPanel()));
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("News") + " (2) ",
					getTof2Panel()));
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("News") + " (3) ",
					getTof3Panel()));
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("Search") + " ",
					getSearchPanel()));
			listData.add( 
			    new ListBoxData( 
                    " " + LangRes.getString("Display") + " ",
                    getDisplayPanel()));
			listData.add(
				new ListBoxData(
					" " + LangRes.getString("Miscellaneous") + " ",
					getMiscPanel()));
			optionsGroupsList = new JList(listData);
			optionsGroupsList.setSelectionMode(
				DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
			optionsGroupsList.addListSelectionListener(this);

			optionsGroupsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.NORTHWEST;
			constr.fill = GridBagConstraints.BOTH;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			optionsGroupsPanel.add(optionsGroupsList, constr);
			optionsGroupsPanel.setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(5, 5, 5, 5),
					BorderFactory.createEtchedBorder()));
		}
		return optionsGroupsPanel;
	}

	/**
	 * Build the download panel.
	 */
	protected JPanel getDownloadPanel() {
		if (downloadPanel == null) {
			downloadEnableRequesting.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					downloadRequestAfterTries.setEnabled(
						downloadEnableRequesting.isSelected());
					downloadRequestAfterTriesLabel.setEnabled(
						downloadEnableRequesting.isSelected());
				}
			});
			downloadRestartFailedDownloads
				.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					downloadMaxRetries.setEnabled(
						downloadRestartFailedDownloads.isSelected());
					downloadWaittime.setEnabled(
						downloadRestartFailedDownloads.isSelected());
					downloadMaxRetriesLabel.setEnabled(
						downloadRestartFailedDownloads.isSelected());
					downloadWaittimeLabel.setEnabled(
						downloadRestartFailedDownloads.isSelected());
				}
			});

			downloadPanel = new JPanel(new GridBagLayout());

			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;

			downloadDisableDownloads
				.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// enable panel if checkbox is not selected
					setPanelEnabled(
						getDownloadPanel(),
						(downloadDisableDownloads.isSelected() == false));
				}
			});
			downloadPanel.add(downloadDisableDownloads, constr);

			constr.gridy++;
			constr.gridx = 0;
			downloadPanel.add(
				new JLabel(LangRes.getString("Download directory") + ": "),
				constr);
			downloadDirectoryTextField.setEditable(true);
			constr.gridx = 1;
			constr.gridwidth = 3;
			downloadPanel.add(downloadDirectoryTextField, constr);

			JButton browseDownloadDirectoryButton =
				new JButton(LangRes.getString("Browse") + "...");
			browseDownloadDirectoryButton
				.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					browseDownloadDirectoryButton_actionPerformed(e);
				}
			});

			constr.gridx = 1;
			constr.gridy++;
			constr.gridwidth = 2;
			constr.anchor = GridBagConstraints.NORTHWEST;
			constr.insets = new Insets(0, 5, 5, 5);
			downloadPanel.add(browseDownloadDirectoryButton, constr);
			constr.gridwidth = 1;
			constr.insets = new Insets(5, 5, 5, 5);

			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 1;
			downloadPanel.add(downloadRestartFailedDownloads, constr);

			constr.gridwidth = 2;
			constr.gridx = 1;
			JPanel subPanel = new JPanel(new GridBagLayout());
			GridBagConstraints subconstr = new GridBagConstraints();
			subconstr.gridx = 0;
			subconstr.gridy = 0;
			subconstr.insets = new Insets(0, 5, 5, 5);
			subconstr.anchor = GridBagConstraints.WEST;
			subPanel.add(downloadMaxRetriesLabel, subconstr);
			subconstr.gridx = 1;
			subPanel.add(downloadMaxRetries, subconstr);
			subconstr.gridy++;
			subconstr.gridx = 0;
			subPanel.add(downloadWaittimeLabel, subconstr);
			subconstr.gridx = 1;
			subPanel.add(downloadWaittime, subconstr);

			constr.gridx = 1;
			downloadPanel.add(subPanel, constr);

			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 3;
			constr.insets = new Insets(0, 5, 5, 5);
			downloadPanel.add(downloadEnableRequesting, constr);

			constr.gridy++;
			constr.insets = new Insets(0, 25, 15, 5);
			subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
			subPanel.add(downloadRequestAfterTriesLabel);
			subPanel.add(downloadRequestAfterTries);
			downloadPanel.add(subPanel, constr);

			constr.gridy++;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			downloadPanel.add(
				new JLabel(
					LangRes.getString("Number of simultaneous downloads")
						+ " (3)"),
				constr);
			constr.gridx = 1;
			downloadPanel.add(downloadThreadsTextField, constr);

			constr.gridy++;
			constr.gridx = 0;
			downloadPanel.add(
				new JLabel(
					LangRes.getString("Number of splitfile threads")
						+ " (30)"),
				constr);
			constr.gridx = 1;
			downloadPanel.add(downloadSplitfileThreadsTextField, constr);

			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 3;
			constr.insets = new Insets(5, 5, 5, 5);
			downloadPanel.add(downloadRemoveFinishedDownloads, constr);

			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 3;
			constr.insets = new Insets(0, 5, 5, 5);
			downloadPanel.add(downloadTryAllSegments, constr);

			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 3;
			constr.insets = new Insets(0, 5, 5, 5);
			downloadPanel.add(downloadDecodeAfterEachSegment, constr);

			// filler (glue)
			constr.gridy++;
			constr.gridx = 3;
			constr.gridwidth = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			downloadPanel.add(new JLabel(" "), constr);
		}
		return downloadPanel;
	}

	/**
	 * Build the upload panel.
	 */
	protected JPanel getUploadPanel() {
		if (uploadPanel == null) {
			uploadPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			uploadDisableRequests
				.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// enable panel if checkbox is not selected
					setPanelEnabled(
						getUploadPanel(),
						(uploadDisableRequests.isSelected() == false));
				}
			});
			uploadPanel.add(uploadDisableRequests, constr);
			constr.gridy++;
			constr.gridx = 0;
			uploadPanel.add(automaticIndexing, constr);
			constr.gridx += 2;
			uploadPanel.add(shareDownloads, constr);
			constr.gridy++;
			constr.gridx = 0;
			uploadPanel.add(signUploads, constr);
			constr.gridx = 2;
			uploadPanel.add(helpFriends, constr);
			constr.gridy++;
			constr.gridx = 0;
			uploadPanel.add(
				new JLabel(LangRes.getString("Upload HTL") + " (8)"),
				constr);
			constr.gridx = 1;
			uploadPanel.add(uploadHtlTextField, constr);
			constr.gridx++;
			uploadPanel.add(
				new JLabel(LangRes.getString("up htl explanation")),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			uploadPanel.add(
				new JLabel(
					LangRes.getString("Number of simultaneous uploads")
						+ " (3)"),
				constr);
			constr.gridx = 1;
			uploadPanel.add(uploadThreadsTextField, constr);
			constr.gridy++;
			constr.gridx = 0;

			constr.insets = new Insets(5, 5, 5, 5);
			uploadPanel.add(
				new JLabel(
					LangRes.getString("Number of splitfile threads")
						+ " (15)"),
				constr);
			constr.gridx = 1;
			uploadPanel.add(uploadSplitfileThreadsTextField, constr);
			constr.gridx++;
			uploadPanel.add(
				new JLabel(LangRes.getString("splitfile explanation")),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			uploadPanel.add(
				new JLabel(LangRes.getString("Upload batch size")),
				constr);
			constr.gridx++;
			uploadPanel.add(uploadBatchSizeTextField, constr);
			constr.gridx++;
			uploadPanel.add(
				new JLabel(LangRes.getString("batch explanation")),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			uploadPanel.add(
				new JLabel(LangRes.getString("Index file redundancy")),
				constr);
			constr.gridx++;
			uploadPanel.add(indexFileRedundancyTextField, constr);
			constr.gridx++;
			uploadPanel.add(
				new JLabel(LangRes.getString("redundancy explanation")),
				constr);

			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			uploadPanel.add(new JLabel(" "), constr);
		}
		return uploadPanel;
	}

	/**
	 * Build the tof panel.
	 */
	protected JPanel getTofPanel() {
		if (tofPanel == null) {
			tofPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			tofPanel.add(
				new JLabel(LangRes.getString("Message upload HTL") + " (21)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofUploadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(
					LangRes.getString("Message download HTL") + " (23)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofDownloadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(
					LangRes.getString("Number of days to display") + " (10)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofDisplayDaysTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(
					LangRes.getString("Number of days to download backwards")
						+ " (3)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofDownloadDaysTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(LangRes.getString("Message base") + " (news)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofMessageBaseTextField, constr);
			constr.gridy++;
			constr.gridx = 0;

			tofPanel.add(new JLabel(LangRes.getString("Signature")), constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 2;
			constr.weightx = 0.7;
			constr.fill = GridBagConstraints.HORIZONTAL;
			constr.insets = new Insets(0, 5, 5, 5);
			JScrollPane tofSignatureScrollPane = new JScrollPane();
			tofSignatureScrollPane.getViewport().add(tofTextArea);
			tofPanel.add(tofSignatureScrollPane, constr);
			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			tofPanel.add(new JLabel(" "), constr);
		}
		return tofPanel;
	}

	/**
	 * Build the tof2 panel (spam options).
	 */
	protected JPanel getTof2Panel() {
		if (tof2Panel == null) {
			tof2Panel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			constr.gridwidth = 2;
			tof2Panel.add(block, constr);
			constr.gridy++;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(tofBlockMessageTextField, constr);
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridy++;
			tof2Panel.add(blockBody, constr);
			constr.gridy++;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(tofBlockMessageBodyTextField, constr);
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridwidth = 1;
			constr.gridy++;
			constr.gridx = 0;
			tof2Panel.add(signedOnly, constr);
			constr.gridx = 1;
			tof2Panel.add(hideBadMessages, constr);
			constr.gridy++;
			constr.gridx = 0;
			tof2Panel.add(hideCheckMessages, constr);
			constr.gridx = 1;
			tof2Panel.add(hideNAMessages, constr);
			constr.gridy++;
			constr.gridx = 0;
			tof2Panel.add(doBoardBackoff, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(interval, constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 0, 5, 5);
			tof2Panel.add(sampleInterval, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(treshold, constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 0, 5, 5);
			tof2Panel.add(spamTreshold, constr);
			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			tof2Panel.add(new JLabel(" "), constr);
		}
		return tof2Panel;
	}

	/**
	 * Build the tof3 panel (automatic update options).
	 */
	protected JPanel getTof3Panel() {
		if (tof3Panel == null) {
			chooseBoardUpdSelectedBackgroundColor
				.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newCol =
						JColorChooser.showDialog(
							OptionsFrame.this,
							LangRes.getString("Choose updating color of SELECTED boards"),
							boardUpdSelectedBackgroundColor);
					if (newCol != null) {
						boardUpdSelectedBackgroundColor = newCol;
						chooseBoardUpdSelectedBackgroundColor.setBackground(
							boardUpdSelectedBackgroundColor);
					}
				}
			});
			chooseBoardUpdNonSelectedBackgroundColor
				.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newCol =
						JColorChooser.showDialog(
							OptionsFrame.this,
							LangRes.getString("Choose updating color of NON-SELECTED boards"),
							boardUpdNonSelectedBackgroundColor);
					if (newCol != null) {
						boardUpdNonSelectedBackgroundColor = newCol;
						chooseBoardUpdNonSelectedBackgroundColor.setBackground(
							boardUpdNonSelectedBackgroundColor);
					}
				}
			});

			final JPanel row1Panel =
				new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			final JPanel row2Panel =
				new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			row1Panel.add(chooseBoardUpdSelectedBackgroundColor);
			row1Panel.add(
				new JLabel(LangRes.getString("Choose background color if updating board is selected")));
			row2Panel.add(chooseBoardUpdNonSelectedBackgroundColor);
			row2Panel.add(
				new JLabel(LangRes.getString("Choose background color if updating board is not selected")));

			tofBoardUpdateVisualization
				.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setPanelEnabled(
						row1Panel,
						tofBoardUpdateVisualization.isSelected());
					setPanelEnabled(
						row2Panel,
						tofBoardUpdateVisualization.isSelected());
				}
			});

			tof3Panel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			tof3Panel.add(
				new JLabel(LangRes.getString("Automatic update options")),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 25, 5, 5);
			tof3Panel.add(
				new JLabel(
					LangRes.getString(
						"Minimum update interval of a board") + " (" + LangRes.getString("minutes")
						+ ") (45)"),
				constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 5, 5, 5);
			tof3Panel.add(
				TFautomaticUpdate_boardsMinimumUpdateInterval,
				constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 25, 5, 5);
			tof3Panel.add(
				new JLabel(
					LangRes.getString(
						"Number of concurrently updating boards")
						+ " (6)"),
				constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 5, 5, 5);
			tof3Panel.add(TFautomaticUpdate_concurrentBoardUpdates, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(15, 5, 5, 5);
			tof3Panel.add(tofBoardUpdateVisualization, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 25, 5, 5);
			tof3Panel.add(row1Panel, constr);
			constr.gridy++;
			tof3Panel.add(row2Panel, constr);
			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			tof3Panel.add(new JLabel(" "), constr);
		}
		return tof3Panel;
	}

	/**
	 * Build the misc. panel.
	 */
	protected JPanel getMiscPanel() {
		if (miscPanel == null) {
			miscPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			miscPanel.add(
				new JLabel(LangRes.getString("Keyfile upload HTL") + " (21)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscKeyUploadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(
				new JLabel(
					LangRes.getString("Keyfile download HTL") + " (24)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscKeyDownloadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(
				new JLabel(LangRes.getString("list of nodes")),
				constr);
			constr.insets = new Insets(0, 5, 5, 5);
			constr.gridy++;
			miscPanel.add(
				new JLabel(" (nodeA:port1, nodeB:port2, ...)"),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(miscAvailableNodesTextField, constr);
			//miscAvailableNodesTextField.setEnabled(false); 
			//constr.gridy++;
			//constr.gridx = 0;
			//miscPanel.add(new JLabel(LangRes.getString("Node port:") + " (8481)"), constr);
			//constr.gridx = 1;
			//miscPanel.add(miscNodePortTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 5, 5, 5);
			miscPanel.add(
				new JLabel(
					LangRes.getString("Maximum number of keys to store")
						+ " (100000)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscMaxKeysTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 2;
			miscPanel.add(allowEvilBertCheckBox, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(miscAltEditCheckBox, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(0, 25, 10, 5);
			miscPanel.add(miscAltEditTextField, constr);
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(cleanUP, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(
				new JLabel(
					LangRes.getString("Automatic saving interval") + " (15)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscAutoSaveInterval, constr);

			constr.gridy++;
			constr.gridx = 0;
			File splashchk = new File("nosplash.chk");
			if (splashchk.exists()) {
				miscSplashscreenCheckBox.setSelected(true);
			} else {
				miscSplashscreenCheckBox.setSelected(false);
			}
			miscPanel.add(miscSplashscreenCheckBox, constr);

			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			miscPanel.add(new JLabel(" "), constr);
		}
		return miscPanel;
	}

	/**
	 * Build the search panel
	 */
	protected JPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Image Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchImageExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Video Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchVideoExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Archive Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchArchiveExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Document Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchDocumentExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Audio Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchAudioExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Executable Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchExecutableExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(LangRes.getString("Maximum search results")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchMaxSearchResults, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(hideBadFiles, constr);
			constr.gridx = 1;
			searchPanel.add(hideAnonFiles, constr);

			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			searchPanel.add(new JLabel(" "), constr);
		}
		return searchPanel;
	}

	/**
	 * Build the button panel.
	 */
	protected JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			// OK / Cancel

			JButton okButton = new JButton(LangRes.getString("OK"));
			JButton cancelButton = new JButton(LangRes.getString("Cancel"));

			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okButton_actionPerformed(e);
				}
			});
			cancelButton
				.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelButton_actionPerformed(e);
				}
			});
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
		}
		return buttonPanel;
	}

	/**
	 * Implementing the ListSelectionListener.
	 * Must change the content of contentAreaPanel to the selected
	 * panel.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		JList theList = (JList) e.getSource();
		Object Olbdata = theList.getSelectedValue();

		contentAreaPanel.removeAll();

		if (Olbdata instanceof ListBoxData) {
			ListBoxData lbdata = (ListBoxData) Olbdata;
			JPanel newPanel = lbdata.getPanel();
			contentAreaPanel.add(newPanel);
			newPanel.revalidate();
		}
		contentAreaPanel.updateUI();
	}

	/**
	 * A simple helper class to store JPanels and their name into a JList.
	 */
	class ListBoxData {
		JPanel panel;
		String name;
		public ListBoxData(String n, JPanel p) {
			panel = p;
			name = n;
		}
		public String toString() {
			return name;
		}
		public JPanel getPanel() {
			return panel;
		}
	}

	/**
	 * okButton Action Listener (OK)
	 */
	private void okButton_actionPerformed(ActionEvent e) {
		ok();
	}

	/**
	 * cancelButton Action Listener (Cancel)
	 */
	private void cancelButton_actionPerformed(ActionEvent e) {
		cancel();
	}

	private void setPanelEnabled(JPanel panel, boolean enabled) {
		int componentCount = panel.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component c = panel.getComponent(x);
			if (c != downloadDisableDownloads && c != uploadDisableRequests) {
				c.setEnabled(enabled);
			}
		}
	}

	/**
	 * browseDownloadDirectoryButton Action Listener (Downloads / Browse)
	 */
	private void browseDownloadDirectoryButton_actionPerformed(ActionEvent e) {
		final JFileChooser fc =
			new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
		fc.setDialogTitle(LangRes.getString("Select download directory"));
		fc.setFileHidingEnabled(true);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(OptionsFrame.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileSeparator = System.getProperty("file.separator");
			File file = fc.getSelectedFile();
			frostSettings.setValue("lastUsedDirectory", file.getParent());
			downloadDirectoryTextField.setText(file.getPath() + fileSeparator);
		}
	}

	//------------------------------------------------------------------------

	/**
	 * Load settings
	 */
	private void setDataElements() {
		// first set some settings to check later if they are changed by user
		checkDisableRequests = frostSettings.getBoolValue("disableRequests");

		checkMaxMessageDisplay = frostSettings.getValue("maxMessageDisplay");
		checkSignedOnly = frostSettings.getBoolValue("signedOnly");
		checkHideBadMessages = frostSettings.getBoolValue("hideBadMessages");
		checkHideCheckMessages =
			frostSettings.getBoolValue("hideCheckMessages");
		checkHideNAMessages = frostSettings.getBoolValue("hideNAMessages");
		checkBlock = frostSettings.getBoolValue("blockMessageChecked");
		checkBlockBody = frostSettings.getBoolValue("blockMessageBodyChecked");
		_signUploads = frostSettings.getBoolValue("signUploads");
		_helpFriends = frostSettings.getBoolValue("helpFriends");
		_hideBad = frostSettings.getBoolValue("hideBadFiles");
		_hideAnon = frostSettings.getBoolValue("hideAnonFiles");

		// now load
		signUploads.setSelected(_signUploads);
		helpFriends.setSelected(_helpFriends);
		hideBadFiles.setSelected(_hideBad);
		hideAnonFiles.setSelected(_hideAnon);
		automaticIndexing.setSelected(
			frostSettings.getBoolValue("automaticIndexing"));
		shareDownloads.setSelected(
			frostSettings.getBoolValue("shareDownloads"));
		downloadRemoveFinishedDownloads.setSelected(
			frostSettings.getBoolValue("removeFinishedDownloads"));
		allowEvilBertCheckBox.setSelected(
			frostSettings.getBoolValue("allowEvilBert"));
		miscAltEditCheckBox.setSelected(
			frostSettings.getBoolValue("useAltEdit"));
		signedOnly.setSelected(frostSettings.getBoolValue("signedOnly"));
		doBoardBackoff.setSelected(
			frostSettings.getBoolValue("doBoardBackoff"));
		interval.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		treshold.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		sampleInterval.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		spamTreshold.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		sampleInterval.setText(frostSettings.getValue("sampleInterval"));
		spamTreshold.setText(frostSettings.getValue("spamTreshold"));
		hideBadMessages.setSelected(
			frostSettings.getBoolValue("hideBadMessages"));
		hideCheckMessages.setSelected(
			frostSettings.getBoolValue("hideCheckMessages"));
		hideNAMessages.setSelected(
			frostSettings.getBoolValue("hideNAMessages"));
		block.setSelected(frostSettings.getBoolValue("blockMessageChecked"));
		blockBody.setSelected(
			frostSettings.getBoolValue("blockMessageBodyChecked"));
		miscAltEditTextField.setEditable(miscAltEditCheckBox.isSelected());
		downloadDirectoryTextField.setText(
			frostSettings.getValue("downloadDirectory"));
		//        downloadMinHtlTextField.setText(frostSettings.getValue("htl"));
		//        downloadMaxHtlTextField.setText(frostSettings.getValue("htlMax"));
		downloadThreadsTextField.setText(
			frostSettings.getValue("downloadThreads"));
		uploadHtlTextField.setText(frostSettings.getValue("htlUpload"));
		uploadThreadsTextField.setText(frostSettings.getValue("uploadThreads"));
		uploadBatchSizeTextField.setText(
			frostSettings.getValue("uploadBatchSize"));
		indexFileRedundancyTextField.setText(
			frostSettings.getValue("indexFileRedundancy"));
		tofUploadHtlTextField.setText(frostSettings.getValue("tofUploadHtl"));
		tofDownloadHtlTextField.setText(
			frostSettings.getValue("tofDownloadHtl"));
		tofDisplayDaysTextField.setText(
			frostSettings.getValue("maxMessageDisplay"));
		tofDownloadDaysTextField.setText(
			frostSettings.getValue("maxMessageDownload"));
		miscKeyUploadHtlTextField.setText(
			frostSettings.getValue("keyUploadHtl"));
		miscKeyDownloadHtlTextField.setText(
			frostSettings.getValue("keyDownloadHtl"));
		miscShowSystrayIcon.setSelected(
			frostSettings.getBoolValue("showSystrayIcon"));
		downloadSplitfileThreadsTextField.setText(
			frostSettings.getValue("splitfileDownloadThreads"));
		uploadSplitfileThreadsTextField.setText(
			frostSettings.getValue("splitfileUploadThreads"));
		miscAvailableNodesTextField.setText(
			frostSettings.getValue("availableNodes"));
		//miscNodePortTextField.setText(frostSettings.getValue("nodePort"));
		miscAltEditTextField.setText(frostSettings.getValue("altEdit"));
		miscMaxKeysTextField.setText(frostSettings.getValue("maxKeys"));
		tofMessageBaseTextField.setText(frostSettings.getValue("messageBase"));
		tofBlockMessageTextField.setText(
			frostSettings.getValue("blockMessage"));
		tofBlockMessageTextField.setEnabled(
			frostSettings.getBoolValue("blockMessageChecked"));
		tofBlockMessageBodyTextField.setText(
			frostSettings.getValue("blockMessageBody"));
		tofBlockMessageBodyTextField.setEnabled(
			frostSettings.getBoolValue("blockMessageBodyChecked"));
		searchMaxSearchResults.setText(
			"" + frostSettings.getIntValue("maxSearchResults"));
		searchAudioExtensionTextField.setText(
			frostSettings.getValue("audioExtension"));
		searchImageExtensionTextField.setText(
			frostSettings.getValue("imageExtension"));
		searchVideoExtensionTextField.setText(
			frostSettings.getValue("videoExtension"));
		searchDocumentExtensionTextField.setText(
			frostSettings.getValue("documentExtension"));
		searchExecutableExtensionTextField.setText(
			frostSettings.getValue("executableExtension"));
		searchArchiveExtensionTextField.setText(
			frostSettings.getValue("archiveExtension"));
		cleanUP.setSelected(frostSettings.getBoolValue("doCleanUp"));
		uploadDisableRequests.setSelected(
			frostSettings.getBoolValue("disableRequests"));
		downloadDisableDownloads.setSelected(
			frostSettings.getBoolValue("disableDownloads"));

		TFautomaticUpdate_concurrentBoardUpdates.setText(
			frostSettings.getValue("automaticUpdate.concurrentBoardUpdates"));
		TFautomaticUpdate_boardsMinimumUpdateInterval.setText(
			frostSettings.getValue(
				"automaticUpdate.boardsMinimumUpdateInterval"));
		tofBoardUpdateVisualization.setSelected(
			frostSettings.getBoolValue("boardUpdateVisualization"));

		miscAutoSaveInterval.setText(
			"" + frostSettings.getIntValue("autoSaveInterval"));

		downloadRestartFailedDownloads.setSelected(
			frostSettings.getBoolValue("downloadRestartFailedDownloads"));
		downloadEnableRequesting.setSelected(
			frostSettings.getBoolValue("downloadEnableRequesting"));
		downloadRequestAfterTries.setText(
			"" + frostSettings.getIntValue("downloadRequestAfterTries"));
		downloadMaxRetries.setText(
			"" + frostSettings.getIntValue("downloadMaxRetries"));
		downloadWaittime.setText(
			"" + frostSettings.getIntValue("downloadWaittime"));
		downloadTryAllSegments.setSelected(
			frostSettings.getBoolValue("downloadTryAllSegments"));
		downloadDecodeAfterEachSegment.setSelected(
			frostSettings.getBoolValue("downloadDecodeAfterEachSegment"));

		downloadRequestAfterTries.setEnabled(
			downloadEnableRequesting.isSelected());
		downloadMaxRetries.setEnabled(
			downloadRestartFailedDownloads.isSelected());
		downloadWaittime.setEnabled(
			downloadRestartFailedDownloads.isSelected());
		downloadRequestAfterTriesLabel.setEnabled(
			downloadEnableRequesting.isSelected());
		downloadMaxRetriesLabel.setEnabled(
			downloadRestartFailedDownloads.isSelected());
		downloadWaittimeLabel.setEnabled(
			downloadRestartFailedDownloads.isSelected());

		boardUpdSelectedBackgroundColor =
			(Color) frostSettings.getObjectValue(
				"boardUpdatingSelectedBackgroundColor");
		boardUpdNonSelectedBackgroundColor =
			(Color) frostSettings.getObjectValue(
				"boardUpdatingNonSelectedBackgroundColor");
		chooseBoardUpdSelectedBackgroundColor.setBackground(
			boardUpdSelectedBackgroundColor);
		chooseBoardUpdNonSelectedBackgroundColor.setBackground(
			boardUpdNonSelectedBackgroundColor);
	}

	/**
	 * Save settings
	 */
	private void saveSettings() {
		String downlDirTxt = downloadDirectoryTextField.getText();
		String filesep = System.getProperty("file.separator");
		// always append a fileseparator to the end of string
		if ((!(downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)))
			|| downlDirTxt.lastIndexOf(filesep) < 0) {
			frostSettings.setValue("downloadDirectory", downlDirTxt + filesep);
		} else {
			frostSettings.setValue("downloadDirectory", downlDirTxt);
		}

		//        frostSettings.setValue("htl",  downloadMinHtlTextField.getText());
		//        frostSettings.setValue("htlMax",  downloadMaxHtlTextField.getText());
		frostSettings.setValue("htlUpload", uploadHtlTextField.getText());
		frostSettings.setValue(
			"uploadThreads",
			uploadThreadsTextField.getText());
		frostSettings.setValue(
			"uploadBatchSize",
			uploadBatchSizeTextField.getText());
		frostSettings.setValue(
			"indexFileRedundancy",
			indexFileRedundancyTextField.getText());
		frostSettings.setValue(
			"downloadThreads",
			downloadThreadsTextField.getText());
		frostSettings.setValue("tofUploadHtl", tofUploadHtlTextField.getText());
		frostSettings.setValue(
			"tofDownloadHtl",
			tofDownloadHtlTextField.getText());
		frostSettings.setValue(
			"keyUploadHtl",
			miscKeyUploadHtlTextField.getText());
		frostSettings.setValue(
			"keyDownloadHtl",
			miscKeyDownloadHtlTextField.getText());
		frostSettings.setValue(
			"maxMessageDisplay",
			tofDisplayDaysTextField.getText());
		frostSettings.setValue(
			"maxMessageDownload",
			tofDownloadDaysTextField.getText());
		frostSettings.setValue(
			"removeFinishedDownloads",
			downloadRemoveFinishedDownloads.isSelected());
		frostSettings.setValue(
			"splitfileUploadThreads",
			uploadSplitfileThreadsTextField.getText());
		frostSettings.setValue(
			"splitfileDownloadThreads",
			downloadSplitfileThreadsTextField.getText());
		frostSettings.setValue(
			"availableNodes",
			miscAvailableNodesTextField.getText());
		//frostSettings.setValue("nodePort", miscNodePortTextField.getText());
		frostSettings.setValue("maxKeys", miscMaxKeysTextField.getText());
		frostSettings.setValue(
			"messageBase",
			((tofMessageBaseTextField.getText()).trim()).toLowerCase());
		frostSettings.setValue(
			"showSystrayIcon",
			miscShowSystrayIcon.isSelected());

		frostSettings.setValue(
			"blockMessage",
			((tofBlockMessageTextField.getText()).trim()).toLowerCase());
		frostSettings.setValue("blockMessageChecked", block.isSelected());
		frostSettings.setValue(
			"blockMessageBody",
			((tofBlockMessageBodyTextField.getText()).trim()).toLowerCase());
		frostSettings.setValue(
			"blockMessageBodyChecked",
			blockBody.isSelected());
		frostSettings.setValue("doBoardBackoff", doBoardBackoff.isSelected());
		frostSettings.setValue("spamTreshold", spamTreshold.getText());
		frostSettings.setValue("sampleInterval", sampleInterval.getText());

		frostSettings.setValue(
			"allowEvilBert",
			allowEvilBertCheckBox.isSelected());
		frostSettings.setValue(
			"maxSearchResults",
			searchMaxSearchResults.getText());
		frostSettings.setValue(
			"audioExtension",
			searchAudioExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"imageExtension",
			searchImageExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"videoExtension",
			searchVideoExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"documentExtension",
			searchDocumentExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"executableExtension",
			searchExecutableExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"archiveExtension",
			searchArchiveExtensionTextField.getText().toLowerCase());
		frostSettings.setValue("useAltEdit", miscAltEditCheckBox.isSelected());
		frostSettings.setValue("signedOnly", signedOnly.isSelected());
		frostSettings.setValue("hideBadMessages", hideBadMessages.isSelected());
		frostSettings.setValue(
			"hideCheckMessages",
			hideCheckMessages.isSelected());
		frostSettings.setValue("hideNAMessages", hideNAMessages.isSelected());
		frostSettings.setValue("altEdit", miscAltEditTextField.getText());
		frostSettings.setValue("doCleanUp", cleanUP.isSelected());
		frostSettings.setValue(
			"disableRequests",
			uploadDisableRequests.isSelected());
		frostSettings.setValue(
			"disableDownloads",
			downloadDisableDownloads.isSelected());

		frostSettings.setValue(
			"automaticUpdate.concurrentBoardUpdates",
			TFautomaticUpdate_concurrentBoardUpdates.getText());
		frostSettings.setValue(
			"automaticUpdate.boardsMinimumUpdateInterval",
			TFautomaticUpdate_boardsMinimumUpdateInterval.getText());
		frostSettings.setValue(
			"boardUpdateVisualization",
			tofBoardUpdateVisualization.isSelected());

		frostSettings.setValue(
			"downloadRestartFailedDownloads",
			downloadRestartFailedDownloads.isSelected());
		frostSettings.setValue(
			"downloadEnableRequesting",
			downloadEnableRequesting.isSelected());
		frostSettings.setValue(
			"downloadRequestAfterTries",
			downloadRequestAfterTries.getText());
		frostSettings.setValue(
			"downloadMaxRetries",
			downloadMaxRetries.getText());
		frostSettings.setValue("downloadWaittime", downloadWaittime.getText());
		frostSettings.setValue(
			"downloadTryAllSegments",
			downloadTryAllSegments.isSelected());
		frostSettings.setValue(
			"downloadDecodeAfterEachSegment",
			downloadDecodeAfterEachSegment.isSelected());

		frostSettings.setObjectValue(
			"boardUpdatingSelectedBackgroundColor",
			boardUpdSelectedBackgroundColor);
		frostSettings.setObjectValue(
			"boardUpdatingNonSelectedBackgroundColor",
			boardUpdNonSelectedBackgroundColor);

		frostSettings.setValue(
			"autoSaveInterval",
			miscAutoSaveInterval.getText());
		frostSettings.setValue("signUploads", signUploads.isSelected());
		frostSettings.setValue(
			"automaticIndexing",
			automaticIndexing.isSelected());
		frostSettings.setValue("shareDownloads", shareDownloads.isSelected());
		frostSettings.setValue("helpFriends", helpFriends.isSelected());
		frostSettings.setValue("hideBadFiles", hideBadFiles.isSelected());
		frostSettings.setValue("hideAnonFiles", hideAnonFiles.isSelected());

		frostSettings.writeSettingsFile();

		// now check if some settings changed
		if (checkDisableRequests == true
			&& // BEFORE: uploads disabled?
		frostSettings.getBoolValue(
			"disableRequests")
				== false) // AFTER: uploads enabled?
			{
			shouldRemoveDummyReqFiles = true;
		}
		if (checkMaxMessageDisplay
			.equals(frostSettings.getValue("maxMessageDisplay"))
			== false
			|| checkSignedOnly != frostSettings.getBoolValue("signedOnly")
			|| checkHideBadMessages
				!= frostSettings.getBoolValue("hideBadMessages")
			|| checkHideCheckMessages
				!= frostSettings.getBoolValue("hideCheckMessages")
			|| checkHideNAMessages != frostSettings.getBoolValue("hideNAMessages")
			|| checkBlock != frostSettings.getBoolValue("blockMessageChecked")
			|| checkBlockBody
				!= frostSettings.getBoolValue("blockMessageBodyChecked")) {
			// at least one setting changed, reload messages
			shouldReloadMessages = true;
		}
	}

	/**
	 * Close window and save settings
	 */
	private void ok() {
		exitState = true;

		if (displayPanel != null) {
			//If the display panel has been used, commit its changes
			displayPanel.ok();
		}

		saveSettings();
		saveSignature();

		//Save splashchk
		try {
			File splashFile = new File("nosplash.chk");
			if (miscSplashscreenCheckBox.isSelected()) {
				splashFile.createNewFile();
			} else {
				splashFile.delete();
			}
		} catch (java.io.IOException ioex) {
			System.out.println(
				"Could not create splashscreen checkfile: " + ioex);
		}

		dispose();
	}

	/**
	 * Close window and do not save settings
	 */
	private void cancel() {
		exitState = false;

		if (displayPanel != null) {
			//If the display panel has been used, undo any possible skin preview
			displayPanel.cancel();
		}

		dispose();
	}

	/**
	 * Loads signature.txt into tofTextArea
	 */
	private void loadSignature() {
		File signature = new File("signature.txt");
		if (signature.isFile()) {
			tofTextArea.setText(FileAccess.readFile("signature.txt"));
		}
	}

	/**
	 * Saves signature.txt to disk
	 */
	private void saveSignature() {
		FileAccess.writeFile(tofTextArea.getText(), "signature.txt");
	}

	/**
	 * Is called after the dialog is hidden.
	 * This method should return true if:
	 *  - signedOnly, hideCheck or hideBad where changed by user
	 *  - a block settings was changed by user
	 * If it returns true, the messages table should be reloaded.
	 */
	public boolean shouldReloadMessages() {
		return shouldReloadMessages;
	}

	/**
	 * Is called after the dialog is hidden.
	 * This method should return true if:
	 *  - setting 'disableRequests' is switched from TRUE to FALSE (means uploading is enabled now)
	 * If it returns true, the dummy request files (created after a key collision)
	 * of all boards should be removed.
	 */
	public boolean shouldRemoveDummyReqFiles() {
		return shouldRemoveDummyReqFiles;
	}

	/**
	 * Can be called to run dialog and get its answer (true=OK, false=CANCEL)
	 */
	public boolean runDialog() {
		this.exitState = false;
		show(); // run dialog
		return this.exitState;
	}

	/**
	 * When window is about to close, do same as if CANCEL was pressed.
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	/**
	 * Constructor, reads init file and inits the gui.
	 */
	public OptionsFrame(Frame parent, java.util.ResourceBundle LangRes) {
		super(parent);
		OptionsFrame.LangRes = LangRes;
		setModal(true);
		translateCheckBox();
		translateLabel();

		frostSettings = new SettingsClass();
		setDataElements();
		loadSignature();

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			Init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// set initial selection (also sets panel)
		optionsGroupsList.setSelectedIndex(0);

		// enable or disable components
		// enable panel if checkbox is not selected
		setPanelEnabled(
			getDownloadPanel(),
			(downloadDisableDownloads.isSelected() == false));
		setPanelEnabled(
			getUploadPanel(),
			(uploadDisableRequests.isSelected() == false));
		// ... but not the checkboxes itself :)
		uploadDisableRequests.setEnabled(true);
		downloadDisableDownloads.setEnabled(true);

		// final layouting
		pack();

		// center dialog on parent
		setLocationRelativeTo(parent);
	}

	/**
	 * Build the display panel.
	 */

	protected JPanel getDisplayPanel() {
		if (displayPanel == null) {
			displayPanel = new DisplayPanel();
			displayPanel.loadSettings(frostSettings);
		}
		return displayPanel;
	}

}
