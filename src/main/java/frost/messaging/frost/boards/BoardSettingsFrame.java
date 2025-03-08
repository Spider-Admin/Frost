/*
  BoardSettingsFrame.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

package frost.messaging.frost.boards;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import frost.MainFrame;
import frost.fcp.BoardKeyPair;
import frost.fcp.FcpHandler;
import frost.util.gui.TextComponentClipboardMenu;
import frost.util.gui.translation.Language;

/**
 * Settingsdialog for a single board or a folder.
 */
public class BoardSettingsFrame extends JDialog {

	private static final long serialVersionUID = 1L;

	private class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
			if (e.getSource() == generateKeyButton) { // Generate key
                generateKeyButton_actionPerformed(e);
            } else if (e.getSource() == okButton) { // Ok
                okButton_actionPerformed(e);
            } else if (e.getSource() == cancelButton) { // Cancel
                cancelButton_actionPerformed(e);
            } else if (e.getSource() == overrideSettingsCheckBox) { // Override settings
                overrideSettingsCheckBox_actionPerformed(e);
            }
        }
    }

	private final transient Language language;
	private final AbstractNode node;
	private final JFrame parentFrame;

	private final transient Listener listener = new Listener();

    private final JCheckBox autoUpdateEnabled = new JCheckBox();
    private final JButton cancelButton = new JButton();
    private boolean exitState;
    private final JButton generateKeyButton = new JButton();

    private final JRadioButton storeSentMessages_default = new JRadioButton();
    private final JRadioButton storeSentMessages_false = new JRadioButton();
    private final JRadioButton storeSentMessages_true = new JRadioButton();
    private final JLabel storeSentMessagesLabel = new JLabel();

    private final JRadioButton hideBad_default = new JRadioButton();
    private final JRadioButton hideBad_false = new JRadioButton();
    private final JRadioButton hideBad_true = new JRadioButton();
    private final JLabel hideBadMessagesLabel = new JLabel();

    private final JRadioButton hideCheck_default = new JRadioButton();
    private final JRadioButton hideCheck_false = new JRadioButton();
    private final JRadioButton hideCheck_true = new JRadioButton();
    private final JLabel hideCheckMessagesLabel = new JLabel();

    private final JRadioButton hideObserve_default = new JRadioButton();
    private final JRadioButton hideObserve_false = new JRadioButton();
    private final JRadioButton hideObserve_true = new JRadioButton();
    private final JLabel hideObserveMessagesLabel = new JLabel();
    private final JLabel hideUnsignedMessagesLabel = new JLabel();

    private final JRadioButton hideMessageCount_default = new JRadioButton();
    private final JRadioButton hideMessageCount_set = new JRadioButton();
    private final JTextField hideMessageCount_value = new JTextField(6);
    private final JLabel hideMessageCountLabel = new JLabel();

    private final JRadioButton maxMessageDisplay_default = new JRadioButton();
    private final JRadioButton maxMessageDisplay_set = new JRadioButton();
    private final JTextField maxMessageDisplay_value = new JTextField(6);
    private final JLabel maxMessageDisplayDaysLabel = new JLabel();

    private final JRadioButton maxMessageDownload_default = new JRadioButton();
    private final JRadioButton maxMessageDownload_set = new JRadioButton();
    private final JTextField maxMessageDownload_value = new JTextField(6);
    private final JLabel maxMessageDownloadDaysLabel = new JLabel();

    private final JButton okButton = new JButton();

    private final JCheckBox overrideSettingsCheckBox = new JCheckBox();
    private final JLabel privateKeyLabel = new JLabel();

    private final JTextField privateKeyTextField = new JTextField();

    private final JRadioButton publicBoardRadioButton = new JRadioButton();

    private final JLabel publicKeyLabel = new JLabel();
    private final JTextField publicKeyTextField = new JTextField();

    private final JRadioButton secureBoardRadioButton = new JRadioButton();

    private final JRadioButton signedOnly_default = new JRadioButton();
    private final JRadioButton signedOnly_false = new JRadioButton();
    private final JRadioButton signedOnly_true = new JRadioButton();

    JPanel settingsPanel = new JPanel(new GridBagLayout());

    private final JLabel descriptionLabel = new JLabel();
    private final JTextArea descriptionTextArea = new JTextArea(3, 40);
    private JScrollPane descriptionScrollPane;

    public BoardSettingsFrame(final JFrame parentFrame, final AbstractNode node) {
        super(parentFrame);

        this.parentFrame = parentFrame;
        this.node = node;
        this.language = Language.getInstance();

        setModal(true);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        initialize();
        //pack();
        setSize(430,615);
        setLocationRelativeTo(parentFrame);

		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				changeJRadioButtonState();
			}
		};
		maxMessageDisplay_set.addItemListener(itemListener);
		maxMessageDownload_set.addItemListener(itemListener);
		hideMessageCount_set.addItemListener(itemListener);
		secureBoardRadioButton.addItemListener(itemListener);
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
    private void cancelButton_actionPerformed(final ActionEvent e) {
        cancel();
    }

    /**
     * generateKeyButton Action Listener (OK)
     * @param e
     */
    private void generateKeyButton_actionPerformed(final ActionEvent e) {
        try {
            final BoardKeyPair kp = FcpHandler.inst().generateBoardKeyPair();
            if( kp != null ) {
                privateKeyTextField.setText(kp.getPrivateBoardKey());
                publicKeyTextField.setText(kp.getPublicBoardKey());
            }
        } catch (final Throwable ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.toString(), // message
                    language.getString("BoardSettings.generateKeyPairErrorDialog.title"), JOptionPane.WARNING_MESSAGE);
        }
    }

	private void overrideSettingsCheckBox_actionPerformed(final ActionEvent e) {
		setOverridePanelEnabled(overrideSettingsCheckBox.isSelected());
	}

    //------------------------------------------------------------------------

    public boolean getExitState() {
        return exitState;
    }

    private JPanel getSettingsPanel() {
        settingsPanel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5,5,5,5)));
        settingsPanel.setLayout(new GridBagLayout());

        final ButtonGroup bg2 = new ButtonGroup();
        bg2.add(maxMessageDisplay_default);
        bg2.add(maxMessageDisplay_set);
        final ButtonGroup bg1 = new ButtonGroup();
        bg1.add(maxMessageDownload_default);
        bg1.add(maxMessageDownload_set);
        final ButtonGroup bg3 = new ButtonGroup();
        bg3.add(signedOnly_default);
        bg3.add(signedOnly_false);
        bg3.add(signedOnly_true);
        final ButtonGroup bg4 = new ButtonGroup();
        bg4.add(hideBad_default);
        bg4.add(hideBad_true);
        bg4.add(hideBad_false);
        final ButtonGroup bg5 = new ButtonGroup();
        bg5.add(hideCheck_default);
        bg5.add(hideCheck_true);
        bg5.add(hideCheck_false);
        final ButtonGroup bg6 = new ButtonGroup();
        bg6.add(hideObserve_default);
        bg6.add(hideObserve_true);
        bg6.add(hideObserve_false);
        final ButtonGroup bg7 = new ButtonGroup();
        bg7.add(storeSentMessages_default);
        bg7.add(storeSentMessages_true);
        bg7.add(storeSentMessages_false);
        final ButtonGroup bg8 = new ButtonGroup();
        bg8.add(hideMessageCount_default);
        bg8.add(hideMessageCount_set);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.gridwidth = 3;
        settingsPanel.add(overrideSettingsCheckBox, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(5, 25, 0, 5);
        settingsPanel.add(autoUpdateEnabled, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(maxMessageDisplayDaysLabel, constraints);
        constraints.insets = new Insets(0, 35, 0, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(maxMessageDisplay_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(maxMessageDisplay_set, constraints);
        constraints.gridx = 2;
        settingsPanel.add(maxMessageDisplay_value, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(maxMessageDownloadDaysLabel, constraints);
        constraints.insets = new Insets(0, 35, 0, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(maxMessageDownload_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(maxMessageDownload_set, constraints);
        constraints.gridx = 2;
        settingsPanel.add(maxMessageDownload_value, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(hideUnsignedMessagesLabel, constraints);
        constraints.insets = new Insets(0, 35, 0, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(signedOnly_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(signedOnly_true, constraints);
        constraints.gridx = 2;
        settingsPanel.add(signedOnly_false, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(hideBadMessagesLabel, constraints);
        constraints.insets = new Insets(0, 35, 0, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(hideBad_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(hideBad_true, constraints);
        constraints.gridx = 2;
        settingsPanel.add(hideBad_false, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(hideCheckMessagesLabel, constraints);
        constraints.insets = new Insets(0, 35, 0, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(hideCheck_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(hideCheck_true, constraints);
        constraints.gridx = 2;
        settingsPanel.add(hideCheck_false, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(hideObserveMessagesLabel, constraints);
        constraints.insets = new Insets(0, 35, 5, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(hideObserve_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(hideObserve_true, constraints);
        constraints.gridx = 2;
        settingsPanel.add(hideObserve_false, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(hideMessageCountLabel, constraints);
        constraints.insets = new Insets(0, 35, 0, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(hideMessageCount_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(hideMessageCount_set, constraints);
        constraints.gridx = 2;
        settingsPanel.add(hideMessageCount_value, constraints);
        constraints.gridy++;

        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.insets = new Insets(3, 25, 0, 5);
        settingsPanel.add(storeSentMessagesLabel, constraints);
        constraints.insets = new Insets(0, 35, 5, 5);
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        settingsPanel.add(storeSentMessages_default, constraints);
        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.gridx = 1;
        settingsPanel.add(storeSentMessages_true, constraints);
        constraints.gridx = 2;
        settingsPanel.add(storeSentMessages_false, constraints);

        // Adds listeners
        overrideSettingsCheckBox.addActionListener(listener);

		setOverridePanelEnabled(node.isBoard() && ((Board) node).isConfigured());

		return settingsPanel;
	}

    private void initialize() {
        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(6,6,6,6));
        setContentPane(contentPanel);
        contentPanel.setLayout(new GridBagLayout());
        refreshLanguage();

        // Adds all of the components
        new TextComponentClipboardMenu(maxMessageDisplay_value, language);
        new TextComponentClipboardMenu(maxMessageDownload_value, language);
        new TextComponentClipboardMenu(privateKeyTextField, language);
        new TextComponentClipboardMenu(publicKeyTextField, language);
        new TextComponentClipboardMenu(descriptionTextArea, language);
        new TextComponentClipboardMenu(hideMessageCount_value, language);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(3, 3, 3, 3);
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
        constraints.weightx = 1;
        constraints.weighty = 1;
        descriptionScrollPane = new JScrollPane(descriptionTextArea);
        contentPanel.add(descriptionScrollPane, constraints);
        constraints.weightx = 0;
        constraints.weighty = 0;

        constraints.gridx = 0;
        constraints.gridy = 3;
        contentPanel.add(getSettingsPanel(), constraints);

        constraints.insets = new Insets(3, 3, 0, 3);
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

        // Adds listeners
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);

        loadKeypair();
        loadBoardSettings();

		Boolean isBoard = node.isBoard();
		publicBoardRadioButton.setEnabled(isBoard);
		secureBoardRadioButton.setEnabled(isBoard);
		descriptionLabel.setEnabled(isBoard);
		descriptionTextArea.setEnabled(isBoard);

		changeJRadioButtonState();
	}

    private JPanel getKeysPanel() {
        final JPanel keysPanel = new JPanel();
        keysPanel.setLayout(new GridBagLayout());

        final ButtonGroup isSecureGroup = new ButtonGroup();
        isSecureGroup.add(publicBoardRadioButton);
        isSecureGroup.add(secureBoardRadioButton);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 3, 0);
        constraints.weighty = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 0.2;
        keysPanel.add(publicBoardRadioButton, constraints);
        constraints.insets = new Insets(3, 0, 3, 0);

        constraints.weightx = 0.2;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        keysPanel.add(secureBoardRadioButton, constraints);

        constraints.gridx = 2;
        constraints.gridwidth = 1;
        constraints.weightx = 0.8;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        keysPanel.add(generateKeyButton, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        keysPanel.add(privateKeyLabel, constraints);
        constraints.insets = new Insets(3, 3, 3, 0);
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.8;
        keysPanel.add(privateKeyTextField, constraints);

        constraints.insets = new Insets(3, 0, 3, 0);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        keysPanel.add(publicKeyLabel, constraints);
        constraints.insets = new Insets(3, 3, 3, 0);
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        keysPanel.add(publicKeyTextField, constraints);

		generateKeyButton.addActionListener(listener);

        return keysPanel;
    }

	/**
	 * Set initial values for board settings.
	 */
	private void loadBoardSettings() {
		if (node.isFolder()) {
			overrideSettingsCheckBox.setSelected(false);
			autoUpdateEnabled.setSelected(true);
			maxMessageDisplay_default.setSelected(true);
			maxMessageDownload_default.setSelected(true);
			signedOnly_default.setSelected(true);
			hideBad_default.setSelected(true);
			hideCheck_default.setSelected(true);
			hideObserve_default.setSelected(true);
			hideMessageCount_default.setSelected(true);
			storeSentMessages_default.setSelected(true);
		} else if (node.isBoard()) {
			final Board board = (Board) node;
			if (board.getDescription() != null) {
				descriptionTextArea.setText(board.getDescription());
			}

			overrideSettingsCheckBox.setSelected(board.isConfigured());

			if (!board.isConfigured()) {
				autoUpdateEnabled.setSelected(true);
			} else {
				autoUpdateEnabled.setSelected(board.getAutoUpdateEnabled());
			}

			if (!board.isConfigured() || board.getMaxMessageDisplayObj() == null) {
				maxMessageDisplay_default.setSelected(true);
			} else {
				maxMessageDisplay_set.setSelected(true);
				maxMessageDisplay_value.setText("" + board.getMaxMessageDisplay());
			}

			if (!board.isConfigured() || board.getMaxMessageDownloadObj() == null) {
				maxMessageDownload_default.setSelected(true);
			} else {
				maxMessageDownload_set.setSelected(true);
				maxMessageDownload_value.setText("" + board.getMaxMessageDownload());
			}

			if (!board.isConfigured() || board.getShowSignedOnlyObj() == null) {
				signedOnly_default.setSelected(true);
			} else if (board.getShowSignedOnly()) {
				signedOnly_true.setSelected(true);
			} else {
				signedOnly_false.setSelected(true);
			}

			if (!board.isConfigured() || board.getHideBadObj() == null) {
				hideBad_default.setSelected(true);
			} else if (board.getHideBad()) {
				hideBad_true.setSelected(true);
			} else {
				hideBad_false.setSelected(true);
			}

			if (!board.isConfigured() || board.getHideCheckObj() == null) {
				hideCheck_default.setSelected(true);
			} else if (board.getHideCheck()) {
				hideCheck_true.setSelected(true);
			} else {
				hideCheck_false.setSelected(true);
			}

			if (!board.isConfigured() || board.getHideObserveObj() == null) {
				hideObserve_default.setSelected(true);
			} else if (board.getHideObserve()) {
				hideObserve_true.setSelected(true);
			} else {
				hideObserve_false.setSelected(true);
			}

			if (!board.isConfigured() || board.getHideMessageCountObj() == null) {
				hideMessageCount_default.setSelected(true);
			} else {
				hideMessageCount_set.setSelected(true);
				hideMessageCount_value.setText("" + board.getHideMessageCount());
			}

			if (!board.isConfigured() || board.getStoreSentMessagesObj() == null) {
				storeSentMessages_default.setSelected(true);
			} else if (board.getStoreSentMessages()) {
				storeSentMessages_true.setSelected(true);
			} else {
				storeSentMessages_false.setSelected(true);
			}
		}
	}

	/**
	 * Loads keypair
	 */
	private void loadKeypair() {
		if (node.isFolder()) {
			publicBoardRadioButton.setSelected(true);
		} else if (node.isBoard()) {
            final Board board = (Board)node;
            final String privateKey = board.getPrivateKey();
            final String publicKey = board.getPublicKey();

            if (privateKey != null) {
                privateKeyTextField.setText(privateKey);
            } else {
                privateKeyTextField.setText(language.getString("BoardSettings.text.keyNotAvailable"));
            }

            if (publicKey != null) {
                publicKeyTextField.setText(publicKey);
            } else {
                publicKeyTextField.setText(language.getString("BoardSettings.text.keyNotAvailable"));
            }

			if (board.isWriteAccessBoard() || board.isReadAccessBoard()) {
				secureBoardRadioButton.setSelected(true);
			} else { // its a public board
				publicBoardRadioButton.setSelected(true);
			}
		}
	}

    /**
     * Close window and save settings
     */
    private void ok() {

        if( node.isBoard() ) {
            // if board was secure before and now its public, ask user if ok to remove the keys
            if( publicBoardRadioButton.isSelected() && (((Board)node).isPublicBoard() == false) ) {
                final int result = JOptionPane.showConfirmDialog(
                        this,
                        language.getString("BoardSettings.looseKeysWarningDialog.body"),
                        language.getString("BoardSettings.looseKeysWarningDialog.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            applySettingsToBoard((Board)node);
        } else if(node.isFolder()) {
            // apply settings to all boards in a folder
            applySettingsToFolder(node);
        }

        // finally update all involved boards before we close the dialog
        updateBoard(node); // board or folder

        exitState = true;
        dispose();
    }

    private void applySettingsToFolder(final AbstractNode b) {

        // process all childs recursiv
        if( b.isFolder() ) {
            for(int x=0; x < b.getChildCount(); x++) {
                final AbstractNode b2 = (AbstractNode)b.getChildAt(x);
                applySettingsToFolder(b2);
            }
            return;
        }

        if( !(b instanceof Board) ) {
            return;
        }

        final Board board = (Board)b;
        // apply set settings to the board, unset options are not changed
        if (overrideSettingsCheckBox.isSelected()) {
            board.setConfigured(true);

            board.setAutoUpdateEnabled(autoUpdateEnabled.isSelected());

            if( maxMessageDisplay_default.isSelected() || maxMessageDisplay_set.isSelected() ) {
                if (maxMessageDisplay_default.isSelected() == false) {
					board.setMaxMessageDays(Integer.parseInt(maxMessageDisplay_value.getText()));
                } else {
                    board.setMaxMessageDays(null);
                }
            }
            if( maxMessageDownload_default.isSelected() || maxMessageDownload_set.isSelected() ) {
                if (maxMessageDownload_default.isSelected() == false) {
					board.setMaxMessageDownload(Integer.parseInt(maxMessageDownload_value.getText()));
                } else {
                    board.setMaxMessageDownload(null);
                }
            }
            if( signedOnly_default.isSelected() || signedOnly_true.isSelected() || signedOnly_false.isSelected() ) {
                if (signedOnly_default.isSelected() == false) {
                    board.setShowSignedOnly(Boolean.valueOf(signedOnly_true.isSelected()));
                } else {
                    board.setShowSignedOnly(null);
                }
            }
            if( hideBad_default.isSelected() || hideBad_true.isSelected() || hideBad_false.isSelected() ) {
                if (hideBad_default.isSelected() == false) {
                    board.setHideBad(Boolean.valueOf(hideBad_true.isSelected()));
                } else {
                    board.setHideBad(null);
                }
            }
            if( hideCheck_default.isSelected() || hideCheck_true.isSelected() || hideCheck_false.isSelected() ) {
                if (hideCheck_default.isSelected() == false) {
                    board.setHideCheck(Boolean.valueOf(hideCheck_true.isSelected()));
                } else {
                    board.setHideCheck(null);
                }
            }
            if( hideObserve_default.isSelected() || hideObserve_true.isSelected() || hideObserve_false.isSelected() ) {
                if (hideObserve_default.isSelected() == false) {
                    board.setHideObserve(Boolean.valueOf(hideObserve_true.isSelected()));
                } else {
                    board.setHideObserve(null);
                }
            }
            if( hideMessageCount_default.isSelected() || hideMessageCount_set.isSelected() ) {
                if (hideMessageCount_default.isSelected() == false) {
					board.setHideMessageCount(Integer.parseInt(hideMessageCount_value.getText()));
                } else {
                    board.setHideMessageCount(null);
                }
            }
            if( storeSentMessages_default.isSelected() || storeSentMessages_true.isSelected() || storeSentMessages_false.isSelected() ) {
                if (storeSentMessages_default.isSelected() == false) {
                    board.setStoreSentMessages(Boolean.valueOf(storeSentMessages_true.isSelected()));
                } else {
                    board.setStoreSentMessages(null);
                }
            }
        } else {
            board.setConfigured(false);
        }
    }

    private void applySettingsToBoard(final Board board) {
        final String desc = descriptionTextArea.getText().trim();
        if( desc.length() > 0 ) {
            board.setDescription(desc);
        } else {
            board.setDescription(null);
        }

        if (secureBoardRadioButton.isSelected()) {
            final String privateKey = privateKeyTextField.getText();
            final String publicKey = publicKeyTextField.getText();
            if (publicKey.startsWith("SSK@")) {
                board.setPublicKey(publicKey);
            } else {
                board.setPublicKey(null);
            }
            if (privateKey.startsWith("SSK@")) {
                board.setPrivateKey(privateKey);
            } else {
                board.setPrivateKey(null);
            }
        } else {
            board.setPublicKey(null);
            board.setPrivateKey(null);
        }

        if (overrideSettingsCheckBox.isSelected()) {
            board.setConfigured(true);
            board.setAutoUpdateEnabled(autoUpdateEnabled.isSelected());
            if (maxMessageDisplay_default.isSelected() == false) {
				board.setMaxMessageDays(Integer.parseInt(maxMessageDisplay_value.getText()));
            } else {
                board.setMaxMessageDays(null);
            }
            if (maxMessageDownload_default.isSelected() == false) {
				board.setMaxMessageDownload(Integer.parseInt(maxMessageDownload_value.getText()));
            } else {
                board.setMaxMessageDownload(null);
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
            if (hideObserve_default.isSelected() == false) {
                board.setHideObserve(Boolean.valueOf(hideObserve_true.isSelected()));
            } else {
                board.setHideObserve(null);
            }
            if (hideMessageCount_default.isSelected() == false) {
				board.setHideMessageCount(Integer.parseInt(hideMessageCount_value.getText()));
            } else {
                board.setHideMessageCount(null);
            }
            if (storeSentMessages_default.isSelected() == false) {
                board.setStoreSentMessages(Boolean.valueOf(storeSentMessages_true.isSelected()));
            } else {
                board.setStoreSentMessages(null);
            }
        } else {
            board.setConfigured(false);
        }
    }

    private void updateBoard(final AbstractNode b) {
        if( b.isBoard() ) {
            MainFrame.getInstance().updateTofTree(b);
            // update the new msg. count for board
            TOF.getInstance().searchUnreadMessages((Board)b);

            if (b == MainFrame.getInstance().getMessagingTab().getTofTreeModel().getSelectedNode()) {
                // reload all messages if board is shown
                MainFrame.getInstance().tofTree_actionPerformed(null);
            }
        } else if( b.isFolder() ) {
            for(int x=0; x < b.getChildCount(); x++) {
                final AbstractNode b2 = (AbstractNode)b.getChildAt(x);
                updateBoard(b2);
            }
        }
    }

    /**
     * okButton Action Listener (OK)
     * @param e
     */
    private void okButton_actionPerformed(final ActionEvent e) {
        ok();
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            dispose();
        }
        super.processWindowEvent(e);
    }

    private void refreshLanguage() {
        if( node.isFolder() ) {
            setTitle(language.getString("BoardSettings.title.folderSettings") + " '" + node.getName() + "'");
        } else if( node.isBoard() ) {
            setTitle(language.getString("BoardSettings.title.boardSettings") + " '" + node.getName() + "'");
        }

        publicBoardRadioButton.setText(language.getString("BoardSettings.label.publicBoard"));
        secureBoardRadioButton.setText(language.getString("BoardSettings.label.secureBoard"));
        okButton.setText(language.getString("Common.ok"));
        cancelButton.setText(language.getString("Common.cancel"));
        generateKeyButton.setText(language.getString("BoardSettings.button.generateNewKeypair"));

        overrideSettingsCheckBox.setText(language.getString("BoardSettings.label.overrideDefaultSettings"));
        final String useDefault = language.getString("BoardSettings.label.useDefault");
        final String yes = language.getString("BoardSettings.label.yes");
        final String no  = language.getString("BoardSettings.label.no");
        maxMessageDisplay_default.setText(useDefault);
        maxMessageDisplay_set.setText(language.getString("BoardSettings.label.setTo") + ":");
        maxMessageDownload_default.setText(useDefault);
        maxMessageDownload_set.setText(language.getString("BoardSettings.label.setTo") + ":");
        signedOnly_default.setText(useDefault);
        signedOnly_true.setText(yes);
        signedOnly_false.setText(no);
        hideBad_default.setText(useDefault);
        hideBad_true.setText(yes);
        hideBad_false.setText(no);
        hideCheck_default.setText(useDefault);
        hideCheck_true.setText(yes);
        hideCheck_false.setText(no);
        hideObserve_default.setText(useDefault);
        hideObserve_true.setText(yes);
        hideObserve_false.setText(no);
        hideMessageCount_default.setText(useDefault);
        hideMessageCount_set.setText(language.getString("BoardSettings.label.setTo") + ":");
        storeSentMessages_default.setText(useDefault);
        storeSentMessages_true.setText(yes);
        storeSentMessages_false.setText(no);
        autoUpdateEnabled.setText(language.getString("BoardSettings.label.enableAutomaticBoardUpdate"));

        publicKeyLabel.setText(language.getString("BoardSettings.label.publicKey") + " :");
        privateKeyLabel.setText(language.getString("BoardSettings.label.privateKey") + " :");
        maxMessageDisplayDaysLabel.setText(language.getString("BoardSettings.label.maximumMessageDisplay"));
        maxMessageDownloadDaysLabel.setText(language.getString("BoardSettings.label.maximumMessageDownload"));
        hideUnsignedMessagesLabel.setText(language.getString("BoardSettings.label.hideUnsignedMessages"));
        hideBadMessagesLabel.setText(language.getString("BoardSettings.label.hideBadMessages"));
        hideCheckMessagesLabel.setText(language.getString("BoardSettings.label.hideCheckMessages"));
        hideObserveMessagesLabel.setText(language.getString("BoardSettings.label.hideObserveMessages"));
        hideMessageCountLabel.setText(language.getString("BoardSettings.label.hideMessageCountDisplay"));
        storeSentMessagesLabel.setText(language.getString("BoardSettings.label.storeSentMessages"));

        descriptionLabel.setText(language.getString("BoardSettings.label.description"));
    }

    public boolean runDialog() {
        setModal(true); // paranoia
        setVisible(true);
        return exitState;
    }

	private void changeJRadioButtonState() {
		maxMessageDisplay_value.setEnabled(maxMessageDisplay_set.isSelected());
		maxMessageDownload_value.setEnabled(maxMessageDownload_set.isSelected());
		hideMessageCount_value.setEnabled(hideMessageCount_set.isSelected());

		Boolean isSecureBoard = secureBoardRadioButton.isSelected();
		privateKeyLabel.setEnabled(isSecureBoard);
		privateKeyTextField.setEnabled(isSecureBoard);
		publicKeyLabel.setEnabled(isSecureBoard);
		publicKeyTextField.setEnabled(isSecureBoard);
		generateKeyButton.setEnabled(isSecureBoard);
	}

    private void setOverridePanelEnabled(final boolean enabled) {
        final int componentCount = settingsPanel.getComponentCount();
        for (int x = 0; x < componentCount; x++) {
            final Component c = settingsPanel.getComponent(x);
            if (c != overrideSettingsCheckBox) {
                c.setEnabled(enabled);
			}
		}

		if (enabled) {
			changeJRadioButtonState();
		}
	}
}
