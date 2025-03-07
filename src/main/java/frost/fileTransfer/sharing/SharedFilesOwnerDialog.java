/*
  SharedFilesOwnerDialog.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.sharing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import frost.Core;
import frost.identities.LocalIdentity;
import frost.util.gui.translation.Language;

public class SharedFilesOwnerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static int OK = 1;
    public static int CANCEL = 2;

    private final String title;

    private int returnCode = CANCEL;
    private String choosedIdentity = null;

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bcancel = null;
    private JButton Bok = null;

    private JLabel LaskForIdentity = null;
	private JComboBox<String> CBidentities = null;

    private JLabel LaskIfToReplace = null;
    private JRadioButton RBignoreExistingFile = null;
    private JRadioButton RBreplaceExistingFilePath = null;
    private ButtonGroup BGaskIfToReplace = null;

    private final Frame parent;
    private boolean replacePathIfFileExists = false;

	private final transient Language language = Language.getInstance();

    public SharedFilesOwnerDialog(final Frame newParent, final String newTitle) {
        super(newParent);
        title = newTitle;
        parent = newParent;
        setModal(true);

        initialize();
        pack();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(397, 213);
        this.setTitle(title);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     *
     * @return JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel
     *
     * @return JPanel
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            final FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBok(), null);
            buttonPanel.add(getBcancel(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes mainPanel
     *
     * @return JPanel
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {

            LaskForIdentity = new JLabel(language.getString("SharedFilesOwnerDialog.askForIdentity") + ":");
            LaskIfToReplace = new JLabel(language.getString("SharedFilesOwnerDialog.askIfToReplace") + ":");
            RBignoreExistingFile = new JRadioButton(language.getString("SharedFilesOwnerDialog.ignoreNewFile"));
            RBreplaceExistingFilePath = new JRadioButton(language.getString("SharedFilesOwnerDialog.replaceExistingFilePath"));

            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            {
                final GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new Insets(5,5,5,5);
                gridBagConstraints.fill = GridBagConstraints.NONE;

                mainPanel.add(LaskForIdentity, gridBagConstraints);
            }
            {
                final GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new Insets(2,20,10,5);
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.fill = GridBagConstraints.NONE;

                mainPanel.add(getCBidentities(), gridBagConstraints);
            }
            {
                final GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new Insets(5,5,5,5);
                gridBagConstraints.fill = GridBagConstraints.NONE;

                mainPanel.add(LaskIfToReplace, gridBagConstraints);
            }
            {
                final GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new Insets(2,20,0,5);
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.fill = GridBagConstraints.NONE;

                mainPanel.add(RBignoreExistingFile, gridBagConstraints);
                gridBagConstraints.gridy = 4;
                gridBagConstraints.insets = new Insets(2,20,10,5);
                mainPanel.add(RBreplaceExistingFilePath, gridBagConstraints);
            }
            BGaskIfToReplace = new ButtonGroup();
            BGaskIfToReplace.add(RBignoreExistingFile);
            BGaskIfToReplace.add(RBreplaceExistingFilePath);

            RBignoreExistingFile.setSelected(true);
        }
        return mainPanel;
    }

    /**
     * This method initializes Bok
     *
     * @return JButton
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton(language.getString("Common.cancel"));
            Bcancel.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    returnCode = CANCEL;
                    setVisible(false);
                }
            });
        }
        return Bcancel;
    }

    /**
     * This method initializes jButton
     *
     * @return JButton
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton(language.getString("Common.ok"));
            Bok.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    returnCode = OK;
                    choosedIdentity = (String)getCBidentities().getSelectedItem();
                    replacePathIfFileExists = RBreplaceExistingFilePath.isSelected();
                    setVisible(false);
                }
            });
        }
        return Bok;
    }

    /**
     * This method initializes CBidentities
     *
     * @return JComboBox
     */
	private JComboBox<String> getCBidentities() {
        if( CBidentities == null ) {
			CBidentities = new JComboBox<>();
            for( final LocalIdentity localIdentity : Core.getIdentitiesManager().getLocalIdentities() ) {
                final LocalIdentity id = localIdentity;
                CBidentities.addItem(id.getUniqueName());
            }
        }
        return CBidentities;
    }

    public String getChoosedIdentityName() {
        return choosedIdentity;
    }

    public int showDialog() {
        setLocationRelativeTo(parent);
        setVisible(true);

        return returnCode;
    }

    public boolean isReplacePathIfFileExists() {
        return replacePathIfFileExists;
    }
}  //  @jve:decl-index=0:visual-constraint="19,14"
