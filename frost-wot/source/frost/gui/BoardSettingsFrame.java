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

package frost.gui;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;

import frost.*;
import frost.gui.objects.*;
import frost.FcpTools.*;

public class BoardSettingsFrame extends JDialog
{
    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------

    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    public boolean exitState;
    public String returnValue;
    public FrostBoardObject board;

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------

    JRadioButton publicBoardRadioButton = new JRadioButton(LangRes.getString("Public board"));
    JRadioButton secureBoardRadioButton = new JRadioButton(LangRes.getString("Secure board"));

    JButton okButton = new JButton(LangRes.getString("OK"));
    JButton cancelButton = new JButton(LangRes.getString("Cancel"));
    JButton generateKeyButton = new JButton(LangRes.getString("Generate new keypair"));

    JTextField privateKeyTextField = new JTextField(32);
    JTextField publicKeyTextField = new JTextField(32);

    JCheckBox overrideSettings = new JCheckBox("Override default settings");

    JRadioButton maxMsg_default = new JRadioButton("Use default");
    JRadioButton maxMsg_set = new JRadioButton("Set to:");
    JTextField maxMsg_value = new JTextField(6);

    JRadioButton signedOnly_default = new JRadioButton("Use default");
    JRadioButton signedOnly_true = new JRadioButton("Yes");
    JRadioButton signedOnly_false = new JRadioButton("No");

    JRadioButton hideBad_default = new JRadioButton("Use default");
    JRadioButton hideBad_true = new JRadioButton("Yes");
    JRadioButton hideBad_false = new JRadioButton("No");

    JRadioButton hideCheck_default = new JRadioButton("Use default");
    JRadioButton hideCheck_true = new JRadioButton("Yes");
    JRadioButton hideCheck_false = new JRadioButton("No");

    JRadioButton hideNA_default = new JRadioButton("Use default");
    JRadioButton hideNA_true = new JRadioButton("Yes");
    JRadioButton hideNA_false = new JRadioButton("No");

    JCheckBox autoUpdateEnabled = new JCheckBox("Enable automatic board update");

    /**Constructor*/
    public BoardSettingsFrame(Frame parent, FrostBoardObject board)
    {
        super(parent);
        setModal(true);
        this.board = board;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init(parent);
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public boolean runDialog()
    {
        setModal(true); // paranoia
        show();
        return exitState;
    }

    private void Init(Frame parent) throws Exception
    {
        this.setTitle("Settings for board '" + board + "'");
        this.setResizable(false);

        //------------------------------------------------------------------------
        // Actionlistener
        //------------------------------------------------------------------------

        // Public board radio button
        publicBoardRadioButton.addActionListener(new java.awt.event.ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             radioButton_actionPerformed(e);
                         } });
        // Private board radio button
        secureBoardRadioButton.addActionListener(new java.awt.event.ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             radioButton_actionPerformed(e);
                         } });
        // generate key
        generateKeyButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            generateKeyButton_actionPerformed(e);
                        } });
        // Ok
        okButton.addActionListener(new java.awt.event.ActionListener() {
                       public void actionPerformed(ActionEvent e) {
                           okButton_actionPerformed(e);
                       } });
        // Cancel
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
                       public void actionPerformed(ActionEvent e) {
                           cancelButton_actionPerformed(e);
                       } });
        //------------------------------------------------------------------------
        // Append objects
        //------------------------------------------------------------------------

        ButtonGroup isSecureGroup = new ButtonGroup();
        isSecureGroup.add(publicBoardRadioButton);
        isSecureGroup.add(secureBoardRadioButton);

        // key panel
        JPanel keyPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.anchor = GridBagConstraints.WEST;
        constr.insets = new Insets(5, 5, 5, 5);
        constr.gridx = 0; constr.gridy = 0;

        keyPanel.add(publicBoardRadioButton, constr);
        constr.gridy++;
        constr.insets = new Insets(0, 5, 5, 5);
        keyPanel.add(secureBoardRadioButton, constr);
        constr.gridx=1;
        constr.anchor=constr.EAST;
        keyPanel.add( generateKeyButton , constr );
        constr.gridx=0;
        constr.anchor=constr.WEST;

        constr.gridy++;
        constr.insets = new Insets(0, 25, 5, 5);
        keyPanel.add( new JLabel(LangRes.getString("Private key :")) , constr );
        constr.gridx = 1;
        constr.fill=constr.HORIZONTAL;
        keyPanel.add( privateKeyTextField , constr );
        constr.fill=constr.NONE;
        constr.gridx = 0;
        constr.gridy++;
        keyPanel.add( new JLabel(LangRes.getString("Public key :")) , constr );
        constr.gridx = 1;
        constr.fill=constr.HORIZONTAL;
        keyPanel.add( publicKeyTextField , constr );

        constr.fill=constr.NONE;
        constr.gridx = 0;
        constr.insets = new Insets(0, 0, 0, 0);
        constr.gridy++;
        constr.gridwidth=2;
        constr.weightx=0.7;
        constr.fill=constr.HORIZONTAL;
        JPanel settings = getSettingsPanel();
        settings.setBorder( new CompoundBorder( new EmptyBorder(new Insets(5,5,5,5)),
                                                new CompoundBorder(new EtchedBorder(),
                                                                   new EmptyBorder(new Insets(5,5,5,5)))
                                               )
                            );
        keyPanel.add(settings, constr);

        // OK / Cancel buttons
        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT, 8, 3) );
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        constr.gridy++;
        constr.gridwidth=2;
        constr.fill=constr.HORIZONTAL;
        constr.insets = new Insets(0, 0, 4, 0);
        keyPanel.add( buttonPanel, constr );

        this.getContentPane().add(keyPanel, BorderLayout.CENTER); // add Main panel

        publicBoardRadioButton.setSelected(true);
        privateKeyTextField.setEnabled(false);
        publicKeyTextField.setEnabled(false);
        generateKeyButton.setEnabled(false);

        loadKeypair();
        loadBoardSettings();
        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel getSettingsPanel()
    {
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

        final JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.anchor = GridBagConstraints.WEST;
        constr.insets = new Insets(5, 5, 5, 5);
        constr.gridx = 0; constr.gridy = 0;

        constr.gridwidth=3;
        panel.add(overrideSettings, constr);
        constr.gridy++;
        constr.insets = new Insets(5, 25, 0, 5);
        panel.add( autoUpdateEnabled, constr );
        constr.gridy++;

        constr.gridwidth=3;
        constr.gridx=0;
        constr.insets = new Insets(3, 25, 0, 5);
        panel.add(new JLabel("Maximum message display (days)"), constr);
        constr.insets = new Insets(0, 35, 0, 5);
        constr.gridwidth=1;
        constr.gridy++;
        constr.gridx=0;
        panel.add(maxMsg_default, constr);
        constr.gridx=1;
        panel.add(maxMsg_set, constr);
        constr.gridx=2;
        panel.add(maxMsg_value, constr);
        constr.gridy++;

        constr.gridwidth=3;
        constr.gridx=0;
        constr.insets = new Insets(3, 25, 0, 5);
        panel.add(new JLabel("Hide unsigned messages"), constr);
        constr.insets = new Insets(0, 35, 0, 5);
        constr.gridwidth=1;
        constr.gridy++;
        constr.gridx=0;
        panel.add(signedOnly_default, constr);
        constr.gridx=1;
        panel.add(signedOnly_true, constr);
        constr.gridx=2;
        panel.add(signedOnly_false, constr);
        constr.gridy++;

        constr.gridwidth=3;
        constr.gridx=0;
        constr.insets = new Insets(3, 25, 0, 5);
        panel.add(new JLabel("Hide messages flagged BAD"), constr);
        constr.insets = new Insets(0, 35, 0, 5);
        constr.gridwidth=1;
        constr.gridy++;
        constr.gridx=0;
        panel.add(hideBad_default, constr);
        constr.gridx=1;
        panel.add(hideBad_true, constr);
        constr.gridx=2;
        panel.add(hideBad_false, constr);
        constr.gridy++;

        constr.gridwidth=3;
        constr.gridx=0;
        constr.insets = new Insets(3, 25, 0, 5);
        panel.add(new JLabel("Hide messages flagged CHECK"), constr);
        constr.insets = new Insets(0, 35, 0, 5);
        constr.gridwidth=1;
        constr.gridy++;
        constr.gridx=0;
        panel.add(hideCheck_default, constr);
        constr.gridx=1;
        panel.add(hideCheck_true, constr);
        constr.gridx=2;
        panel.add(hideCheck_false, constr);
        constr.gridy++;

        constr.gridwidth=3;
        constr.gridx=0;
        constr.insets = new Insets(3, 25, 0, 5);
        panel.add(new JLabel("Hide messages flagged N/A"), constr);
        constr.insets = new Insets(0, 35, 0, 5);
        constr.gridwidth=1;
        constr.gridy++;
        constr.gridx=0;
        panel.add(hideNA_default, constr);
        constr.gridx=1;
        panel.add(hideNA_true, constr);
        constr.gridx=2;
        panel.add(hideNA_false, constr);

        // filler (glue)
        constr.gridx = 3;
        constr.gridwidth = 1;
        constr.weightx = 0.7;
        constr.weighty = 0.7;
        constr.insets = new Insets(0,0,0,0);
        constr.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(" "), constr);

        overrideSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setPanelEnabled( panel, overrideSettings.isSelected() );
            } });

        setPanelEnabled( panel, board.isConfigured() );

        return panel;
    }

    //------------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------------

    /**radioButton Action Listener (OK)*/
    private void radioButton_actionPerformed(ActionEvent e)
    {
        if( publicBoardRadioButton.isSelected() )
        {
            privateKeyTextField.setEnabled(false);
            publicKeyTextField.setEnabled(false);
            generateKeyButton.setEnabled(false);
        }
        else
        {
            privateKeyTextField.setEnabled(true);
            publicKeyTextField.setEnabled(true);
            generateKeyButton.setEnabled(true);
        }
    }

    /**generateKeyButton Action Listener (OK)*/
    private void generateKeyButton_actionPerformed(ActionEvent e)
    {
        try
        {
            FcpConnection connection = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"),
                                                         frame1.frostSettings.getValue("nodePort"));
            try
            {
                String[] keyPair = connection.getKeyPair();
                privateKeyTextField.setText(keyPair[0]);
                publicKeyTextField.setText(keyPair[1]);
            }
            catch( IOException ex )
            {
                frame1.displayWarning(ex.toString());
            }
        }
        catch( FcpToolsException ex )
        {
            System.out.println("BoardSettingsFrame.generateKey: FcpToolsException " + ex);
        }
        catch( IOException ex )
        {
            frame1.displayWarning(ex.toString());
        }
    }

    /**okButton Action Listener (OK)*/
    private void okButton_actionPerformed(ActionEvent e)
    {
        ok();
    }

    /**cancelButton Action Listener (Cancel)*/
    private void cancelButton_actionPerformed(ActionEvent e)
    {
        cancel();
    }

    //------------------------------------------------------------------------

    /**Return exitState*/
    public boolean getExitState()
    {
        return exitState;
    }

    //------------------------------------------------------------------------

    /**Close window and save settings*/
    private void ok()
    {
        exitState = true;
        String privateKey = privateKeyTextField.getText();
        String publicKey = publicKeyTextField.getText();

        if( secureBoardRadioButton.isSelected() )
        {
            if( publicKey.startsWith("SSK@") )
            {
                board.setPublicKey( publicKey );
            }
            if( privateKey.startsWith("SSK@") )
            {
                board.setPrivateKey( privateKey );
            }
        }
        if( overrideSettings.isSelected() )
        {
            board.setConfigured(true);
            board.setAutoUpdateEnabled( autoUpdateEnabled.isSelected() );
            if( maxMsg_default.isSelected() == false )
                board.setMaxMessageDays( new Integer( maxMsg_value.getText() ) );
            else
                board.setMaxMessageDays( null );

            if( signedOnly_default.isSelected() == false )
                board.setShowSignedOnly( Boolean.valueOf( signedOnly_true.isSelected() ) );
            else
                board.setShowSignedOnly( null );

            if( hideBad_default.isSelected() == false )
                board.setHideBad( Boolean.valueOf( hideBad_true.isSelected() ) );
            else
                board.setHideBad( null );

            if( hideCheck_default.isSelected() == false )
                board.setHideCheck( Boolean.valueOf( hideCheck_true.isSelected() ) );
            else
                board.setHideCheck( null );

            if( hideNA_default.isSelected() == false )
                board.setHideNA( Boolean.valueOf( hideNA_true.isSelected() ) );
            else
                board.setHideNA( null );
        }
        else
        {
            board.setConfigured(false);
        }
        dispose();
    }

    /**Close window and do not save settings*/
    private void cancel()
    {
        exitState = false;
        dispose();
    }

    /** Loads keypair from file */
    private void loadKeypair()
    {
        String privateKey = board.getPrivateKey();
        String publicKey = board.getPublicKey();

        if( privateKey != null )
            privateKeyTextField.setText(privateKey);
        else
            privateKeyTextField.setText(LangRes.getString("Not available"));

        if( publicKey != null )
            publicKeyTextField.setText(publicKey);
        else
            publicKeyTextField.setText(LangRes.getString("Not available"));


        if( board.isWriteAccessBoard() || board.isReadAccessBoard() )
        {
            privateKeyTextField.setEnabled(true);
            publicKeyTextField.setEnabled(true);
            generateKeyButton.setEnabled(true);
            secureBoardRadioButton.setSelected(true);
        }
        else // its a public board
        {
            privateKeyTextField.setEnabled(false);
            publicKeyTextField.setEnabled(false);
            generateKeyButton.setEnabled(false);
            publicBoardRadioButton.setSelected(true);
        }
    }

    /**
     * Set initial values for board settings.
     */
    private void loadBoardSettings()
    {
        overrideSettings.setSelected(board.isConfigured());

        if( !board.isConfigured() || board.getMaxMessageDisplayObj() == null )
            maxMsg_default.setSelected(true);
        else
        {
            maxMsg_set.setSelected(true);
            maxMsg_value.setText( ""+board.getMaxMessageDisplay() );
        }

        if( !board.isConfigured() )
            autoUpdateEnabled.setSelected(true); // default
        else if( board.getAutoUpdateEnabled() )
            autoUpdateEnabled.setSelected(true);
        else
            autoUpdateEnabled.setSelected(false);

        if( !board.isConfigured() || board.getShowSignedOnlyObj() == null )
            signedOnly_default.setSelected(true);
        else if(board.getShowSignedOnly())
            signedOnly_true.setSelected(true);
        else
            signedOnly_false.setSelected(true);

        if( !board.isConfigured() || board.getHideBadObj() == null )
            hideBad_default.setSelected(true);
        else if(board.getHideBad())
            hideBad_true.setSelected(true);
        else
            hideBad_false.setSelected(true);

        if( !board.isConfigured() || board.getHideCheckObj() == null )
            hideCheck_default.setSelected(true);
        else if(board.getHideCheck())
            hideCheck_true.setSelected(true);
        else
            hideCheck_false.setSelected(true);

        if( !board.isConfigured() || board.getHideNAObj() == null )
            hideNA_default.setSelected(true);
        else if(board.getHideNA())
            hideNA_true.setSelected(true);
        else
            hideNA_false.setSelected(true);
    }

    private void setPanelEnabled(JPanel panel, boolean enabled)
    {
        int componentCount = panel.getComponentCount();
        for( int x=0; x<componentCount; x++ )
        {
            Component c = panel.getComponent(x);
            if( c != overrideSettings )
            {
                c.setEnabled( enabled );
            }
        }
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            dispose();
        }
        super.processWindowEvent(e);
    }

}

