/*
  BoardSettingsFrame.java / Frost
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
*/

package frost.boards;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.*;

import frost.MainFrame;
import frost.fcp.*;
import frost.gui.objects.Board;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class BoardSettingsFrame extends JDialog {

	/**
	 * 
	 */
	private class Listener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == publicBoardRadioButton) { // Public board radio button
				radioButton_actionPerformed(e);
			} else if (e.getSource() == secureBoardRadioButton) { // Private board radio button
				radioButton_actionPerformed(e);
			} else if (e.getSource() == generateKeyButton) { // Generate key
				generateKeyButton_actionPerformed(e);
			} else if (e.getSource() == okButton) { // Ok
				okButton_actionPerformed(e);
			} else if (e.getSource() == cancelButton) { // Cancel
				cancelButton_actionPerformed(e);
			} else if (e.getSource() == overrideSettingsCheckBox) {	// Override settings
				overrideSettingsCheckBox_actionPerformed(e);	
			}
		}

	}
	
	private static Logger logger = Logger.getLogger(BoardSettingsFrame.class.getName());
	
	private Language language = null;
	
	private Listener listener = new Listener();

	private JCheckBox autoUpdateEnabled = new JCheckBox();
	private Board board;
	private JButton cancelButton = new JButton();
	private boolean exitState;
	private JButton generateKeyButton = new JButton();

	private JRadioButton hideBad_default = new JRadioButton();
	private JRadioButton hideBad_false = new JRadioButton();
	private JRadioButton hideBad_true = new JRadioButton();
	private JLabel hideBadMessagesLabel = new JLabel();

	private JRadioButton hideCheck_default = new JRadioButton();
	private JRadioButton hideCheck_false = new JRadioButton();
	private JRadioButton hideCheck_true = new JRadioButton();
	private JLabel hideCheckMessagesLabel = new JLabel();

	private JRadioButton hideNA_default = new JRadioButton();
	private JRadioButton hideNA_false = new JRadioButton();
	private JRadioButton hideNA_true = new JRadioButton();
	private JLabel hideNaMessagesLabel = new JLabel();
	private JLabel hideUnsignedMessagesLabel = new JLabel();

	private JRadioButton maxMsg_default = new JRadioButton();
	private JRadioButton maxMsg_set = new JRadioButton();
	private JTextField maxMsg_value = new JTextField(6);
	private JLabel messageDisplayDaysLabel = new JLabel();

	private JButton okButton = new JButton();

	private JCheckBox overrideSettingsCheckBox = new JCheckBox();
	private JLabel privateKeyLabel = new JLabel();

	private JTextField privateKeyTextField = new JTextField();

	private JRadioButton publicBoardRadioButton = new JRadioButton();

	private JLabel publicKeyLabel = new JLabel();
	private JTextField publicKeyTextField = new JTextField();
	private String returnValue;
	private JRadioButton secureBoardRadioButton = new JRadioButton();

	private JRadioButton signedOnly_default = new JRadioButton();
	private JRadioButton signedOnly_false = new JRadioButton();
	private JRadioButton signedOnly_true = new JRadioButton();
	
	JPanel settingsPanel = new JPanel(new GridBagLayout());
	
	private JLabel descriptionLabel = new JLabel();
	private JTextArea descriptionTextArea = new JTextArea(3, 40);
	private JScrollPane descriptionScrollPane;

	/**
	 * @param parent
	 * @param board
	 */
	public BoardSettingsFrame(
		Frame parent,
		Board board) {

		super(parent);

		this.board = board;
		this.language = Language.getInstance();
		setModal(true);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		initialize();
		pack();
		setLocationRelativeTo(parent);
	}

	/**
	 * Close window and do not save settings
	 */
	private void cancel() {
		exitState = false;
		dispose();
	}

	/**
	 * cancelButton Action Listener (Cancel)
	 * @param e
	 */
	private void cancelButton_actionPerformed(ActionEvent e) {
		cancel();
	}

	/**
	 * generateKeyButton Action Listener (OK)
	 * @param e
	 */
	private void generateKeyButton_actionPerformed(ActionEvent e) {
		FcpConnection connection = FcpFactory.getFcpConnectionInstance();
		if (connection == null)
			return;

		try {
			String[] keyPair = connection.getKeyPair();
			privateKeyTextField.setText(keyPair[0]);
			publicKeyTextField.setText(keyPair[1]);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), ex.toString(), // message
					language.getString("Warning"), JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * @param e
	 */
	private void overrideSettingsCheckBox_actionPerformed(ActionEvent e) {
		setPanelEnabled(settingsPanel, overrideSettingsCheckBox.isSelected());
	} 

	//------------------------------------------------------------------------

	/**Return exitState
	 * @return
	 */
	public boolean getExitState() {
		return exitState;
	}

	/**
	 * @return
	 */
	private JPanel getSettingsPanel() {
		settingsPanel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5,5,5,5)));
		settingsPanel.setLayout(new GridBagLayout());
		
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(maxMsg_default);
		bg2.add(maxMsg_set);
		ButtonGroup bg3 = new ButtonGroup();
		bg3.add(signedOnly_default);
		bg3.add(signedOnly_false);
		bg3.add(signedOnly_true);
		ButtonGroup bg4 = new ButtonGroup();
		bg4.add(hideBad_default);
		bg4.add(hideBad_true);
		bg4.add(hideBad_false);
		ButtonGroup bg5 = new ButtonGroup();
		bg5.add(hideCheck_default);
		bg5.add(hideCheck_true);
		bg5.add(hideCheck_false);
		ButtonGroup bg6 = new ButtonGroup();
		bg6.add(hideNA_default);
		bg6.add(hideNA_true);
		bg6.add(hideNA_false);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;

		constraints.gridwidth = 3;
		settingsPanel.add(overrideSettingsCheckBox, constraints);
		constraints.gridy = 1;
		constraints.insets = new Insets(5, 25, 0, 5);
		settingsPanel.add(autoUpdateEnabled, constraints);
		constraints.gridy = 2;

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.insets = new Insets(3, 25, 0, 5);
		settingsPanel.add(messageDisplayDaysLabel, constraints);
		constraints.insets = new Insets(0, 35, 0, 5);
		constraints.gridwidth = 1;
		constraints.gridy = 3;
		constraints.gridx = 0;
		settingsPanel.add(maxMsg_default, constraints);
		constraints.gridx = 1;
		settingsPanel.add(maxMsg_set, constraints);
		constraints.gridx = 2;
		settingsPanel.add(maxMsg_value, constraints);
		constraints.gridy = 4;

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.insets = new Insets(3, 25, 0, 5);
		settingsPanel.add(hideUnsignedMessagesLabel, constraints);
		constraints.insets = new Insets(0, 35, 0, 5);
		constraints.gridwidth = 1;
		constraints.gridy = 5;
		constraints.gridx = 0;
		settingsPanel.add(signedOnly_default, constraints);
		constraints.gridx = 1;
		settingsPanel.add(signedOnly_true, constraints);
		constraints.gridx = 2;
		settingsPanel.add(signedOnly_false, constraints);
		constraints.gridy = 6;

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.insets = new Insets(3, 25, 0, 5);
		settingsPanel.add(hideBadMessagesLabel, constraints);
		constraints.insets = new Insets(0, 35, 0, 5);
		constraints.gridwidth = 1;
		constraints.gridy = 7;
		constraints.gridx = 0;
		settingsPanel.add(hideBad_default, constraints);
		constraints.gridx = 1;
		settingsPanel.add(hideBad_true, constraints);
		constraints.gridx = 2;
		settingsPanel.add(hideBad_false, constraints);
		constraints.gridy = 8;

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.insets = new Insets(3, 25, 0, 5);
		settingsPanel.add(hideCheckMessagesLabel, constraints);
		constraints.insets = new Insets(0, 35, 0, 5);
		constraints.gridwidth = 1;
		constraints.gridy = 9;
		constraints.gridx = 0;
		settingsPanel.add(hideCheck_default, constraints);
		constraints.gridx = 1;
		settingsPanel.add(hideCheck_true, constraints);
		constraints.gridx = 2;
		settingsPanel.add(hideCheck_false, constraints);
		constraints.gridy = 10;

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.insets = new Insets(3, 25, 0, 5);
		settingsPanel.add(hideNaMessagesLabel, constraints);
		constraints.insets = new Insets(0, 35, 5, 5);
		constraints.gridwidth = 1;
		constraints.gridy = 11;
		constraints.gridx = 0;
		settingsPanel.add(hideNA_default, constraints);
		constraints.gridx = 1;
		settingsPanel.add(hideNA_true, constraints);
		constraints.gridx = 2;
		settingsPanel.add(hideNA_false, constraints);
		
		// Adds listeners
		overrideSettingsCheckBox.addActionListener(listener);

		setPanelEnabled(settingsPanel, board.isConfigured());

		return settingsPanel;
	}

	/**
	 * 
	 */
	private void initialize() {
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(10,10,10,10));
		setContentPane(contentPanel);
		contentPanel.setLayout(new GridBagLayout());
		refreshLanguage();

		// Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridwidth = 3;

		constraints.weightx = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		contentPanel.add(getKeysPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		contentPanel.add(descriptionLabel, constraints);
		constraints.gridx = 0;
		constraints.gridy = 2;
		descriptionScrollPane = new JScrollPane(descriptionTextArea);
		contentPanel.add(descriptionScrollPane, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		contentPanel.add(getSettingsPanel(), constraints);

		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridwidth = 1;
		constraints.weightx = 2;
		constraints.gridx = 0;
		constraints.gridy = 4;
		contentPanel.add(okButton, constraints);
		constraints.weightx = 0;
		constraints.gridx = 1;
		constraints.gridy = 4;
		contentPanel.add(cancelButton, constraints);

		descriptionTextArea.setEditable(false);
		publicBoardRadioButton.setSelected(true);
		privateKeyTextField.setEnabled(false);
		publicKeyTextField.setEnabled(false);
		generateKeyButton.setEnabled(false);

		// Adds listeners
		okButton.addActionListener(listener);
		cancelButton.addActionListener(listener);
		
		if (board.getDescription() != null) {
			descriptionTextArea.setText(board.getDescription());
		}
		loadKeypair();
		loadBoardSettings();
	}
	
	/**
	 * @return
	 */
	private JPanel getKeysPanel() {
		JPanel keysPanel = new JPanel();
		keysPanel.setLayout(new GridBagLayout());

		ButtonGroup isSecureGroup = new ButtonGroup();
		isSecureGroup.add(publicBoardRadioButton);
		isSecureGroup.add(secureBoardRadioButton);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridwidth = 1;
		constraints.weighty = 1;
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.2;
		keysPanel.add(publicBoardRadioButton, constraints);
		
		constraints.weightx = 0.2;
		constraints.gridy = 1;
		keysPanel.add(secureBoardRadioButton, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0.8;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		keysPanel.add(generateKeyButton, constraints);
		
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 0.2;
		keysPanel.add(privateKeyLabel, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0.8;
		keysPanel.add(privateKeyTextField, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 0.2;
		keysPanel.add(publicKeyLabel, constraints);
		constraints.gridx = 1;
		constraints.weightx = 0.8;
		keysPanel.add(publicKeyTextField, constraints);
		
		// Adds listeners
		publicBoardRadioButton.addActionListener(listener);
		secureBoardRadioButton.addActionListener(listener);
		generateKeyButton.addActionListener(listener);
		
		return keysPanel;
	}

	/**
	 * Set initial values for board settings.
	 */
	private void loadBoardSettings() {
		overrideSettingsCheckBox.setSelected(board.isConfigured());

		if (!board.isConfigured() || board.getMaxMessageDisplayObj() == null)
			maxMsg_default.setSelected(true);
		else {
			maxMsg_set.setSelected(true);
			maxMsg_value.setText("" + board.getMaxMessageDisplay());
		}

		if (!board.isConfigured())
			autoUpdateEnabled.setSelected(true); // default
		else if (board.getAutoUpdateEnabled())
			autoUpdateEnabled.setSelected(true);
		else
			autoUpdateEnabled.setSelected(false);

		if (!board.isConfigured() || board.getShowSignedOnlyObj() == null)
			signedOnly_default.setSelected(true);
		else if (board.getShowSignedOnly())
			signedOnly_true.setSelected(true);
		else
			signedOnly_false.setSelected(true);

		if (!board.isConfigured() || board.getHideBadObj() == null)
			hideBad_default.setSelected(true);
		else if (board.getHideBad())
			hideBad_true.setSelected(true);
		else
			hideBad_false.setSelected(true);

		if (!board.isConfigured() || board.getHideCheckObj() == null)
			hideCheck_default.setSelected(true);
		else if (board.getHideCheck())
			hideCheck_true.setSelected(true);
		else
			hideCheck_false.setSelected(true);

		if (!board.isConfigured() || board.getHideNAObj() == null)
			hideNA_default.setSelected(true);
		else if (board.getHideNA())
			hideNA_true.setSelected(true);
		else
			hideNA_false.setSelected(true);
	}

	/** 
	 * Loads keypair from file 
	 */
	private void loadKeypair() {
		String privateKey = board.getPrivateKey();
		String publicKey = board.getPublicKey();

		if (privateKey != null)
			privateKeyTextField.setText(privateKey);
		else
			privateKeyTextField.setText(language.getString("Not available"));

		if (publicKey != null)
			publicKeyTextField.setText(publicKey);
		else
			publicKeyTextField.setText(language.getString("Not available"));

		if (board.isWriteAccessBoard() || board.isReadAccessBoard()) {
			privateKeyTextField.setEnabled(true);
			publicKeyTextField.setEnabled(true);
			generateKeyButton.setEnabled(true);
			secureBoardRadioButton.setSelected(true);
		} else // its a public board
			{
			privateKeyTextField.setEnabled(false);
			publicKeyTextField.setEnabled(false);
			generateKeyButton.setEnabled(false);
			publicBoardRadioButton.setSelected(true);
		}
	}


	/**
	 * Close window and save settings
	 */
	private void ok() {

		if (secureBoardRadioButton.isSelected()) {
			String privateKey = privateKeyTextField.getText();
			String publicKey = publicKeyTextField.getText();
			if (publicKey.startsWith("SSK@")) {
				board.setPublicKey(publicKey);
			}
			if (privateKey.startsWith("SSK@")) {
				board.setPrivateKey(privateKey);
			}
		} else {
			int result =
				JOptionPane.showConfirmDialog(
					this,
					language.getString("BoardSettingsFrame.confirmBody"),
					language.getString("BoardSettingsFrame.confirmTitle"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				board.setPublicKey(null);
				board.setPrivateKey(null);
			} else {
				return;
			}
		}
		if (overrideSettingsCheckBox.isSelected()) {
			board.setConfigured(true);
			board.setAutoUpdateEnabled(autoUpdateEnabled.isSelected());
			if (maxMsg_default.isSelected() == false) {
				board.setMaxMessageDays(new Integer(maxMsg_value.getText()));
			} else {
				board.setMaxMessageDays(null);
			}
			if (signedOnly_default.isSelected() == false) {
				board.setShowSignedOnly(Boolean.valueOf(signedOnly_true.isSelected()));
			} else {
				board.setShowSignedOnly(null);
			}
			if (hideBad_default.isSelected() == false) {
				board.setHideBad(Boolean.valueOf(hideBad_true.isSelected()));
			} else {
				board.setHideBad(null);
			}
			if (hideCheck_default.isSelected() == false) {
				board.setHideCheck(Boolean.valueOf(hideCheck_true.isSelected()));
			} else {
				board.setHideCheck(null);
			}
			if (hideNA_default.isSelected() == false) {
				board.setHideNA(Boolean.valueOf(hideNA_true.isSelected()));
			} else {
				board.setHideNA(null);
			}
		} else {
			board.setConfigured(false);
		}
		dispose();
		
		exitState = true;
	}

	/**
	 * okButton Action Listener (OK)
	 * @param e
	 */
	private void okButton_actionPerformed(ActionEvent e) {
		ok();
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#processWindowEvent(java.awt.event.WindowEvent)
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			dispose();
		}
		super.processWindowEvent(e);
	}

	/**
	 * radioButton Action Listener (OK)
	 * @param e
	 */
	private void radioButton_actionPerformed(ActionEvent e) {
		if (publicBoardRadioButton.isSelected()) {
			privateKeyTextField.setEnabled(false);
			publicKeyTextField.setEnabled(false);
			generateKeyButton.setEnabled(false);
		} else {
			privateKeyTextField.setEnabled(true);
			publicKeyTextField.setEnabled(true);
			generateKeyButton.setEnabled(true);
		}
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		setTitle(language.getString("Settings for board") + " '" + board.getName() + "'");

		publicBoardRadioButton.setText(language.getString("Public board"));
		secureBoardRadioButton.setText(language.getString("Secure board"));
		okButton.setText(language.getString("OK"));
		cancelButton.setText(language.getString("Cancel"));
		generateKeyButton.setText(language.getString("Generate new keypair"));

		overrideSettingsCheckBox.setText(language.getString("Override default settings"));
		maxMsg_default.setText(language.getString("Use default"));
		maxMsg_set.setText(language.getString("Set to") + ":");
		signedOnly_default.setText(language.getString("Use default"));
		signedOnly_true.setText(language.getString("Yes"));
		signedOnly_false.setText(language.getString("No"));
		hideBad_default.setText(language.getString("Use default"));
		hideBad_true.setText(language.getString("Yes"));
		hideBad_false.setText(language.getString("No"));
		hideCheck_default.setText(language.getString("Use default"));
		hideCheck_true.setText(language.getString("Yes"));
		hideCheck_false.setText(language.getString("No"));
		hideNA_default.setText(language.getString("Use default"));
		hideNA_true.setText(language.getString("Yes"));
		hideNA_false.setText(language.getString("No"));
		autoUpdateEnabled.setText(language.getString("Enable automatic board update"));

		publicKeyLabel.setText(language.getString("Public key") + " :");
		privateKeyLabel.setText(language.getString("Private key") + " :");
		messageDisplayDaysLabel.setText(
				language.getString("Maximum message display (days)"));
		hideUnsignedMessagesLabel.setText(language.getString("Hide unsigned messages"));
		hideBadMessagesLabel.setText(language.getString("Hide messages flagged BAD"));
		hideCheckMessagesLabel.setText(language.getString("Hide messages flagged CHECK"));
		hideNaMessagesLabel.setText(language.getString("Hide messages flagged N/A"));
		
		descriptionLabel.setText(language.getString("BoardSettingsFrame.description"));
	}

	/**
	 * @return
	 */
	public boolean runDialog() {
		setModal(true); // paranoia
		setVisible(true);
		return exitState;
	}

	/**
	 * @param panel
	 * @param enabled
	 */
	private void setPanelEnabled(JPanel panel, boolean enabled) {
		int componentCount = panel.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component c = panel.getComponent(x);
			if (c != overrideSettingsCheckBox) {
				c.setEnabled(enabled);
			}
		}
	}

}
