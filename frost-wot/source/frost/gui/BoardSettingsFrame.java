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
    ButtonGroup isSecureGroup = new ButtonGroup();


    JButton okButton = new JButton(LangRes.getString("OK"));
    JButton cancelButton = new JButton(LangRes.getString("Cancel"));
    JButton generateKeyButton = new JButton(LangRes.getString("Generate new keypair"));

    JTextField privateKeyTextField = new JTextField(32);
    JTextField publicKeyTextField = new JTextField(32);

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
        keyPanel.add(secureBoardRadioButton, constr);
        constr.gridx=1;
        constr.anchor=constr.EAST;
        keyPanel.add( generateKeyButton , constr );
        constr.gridx=0;
        constr.anchor=constr.WEST;

        constr.gridy++;
        constr.insets = new Insets(5, 25, 5, 5);
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

        constr.gridy++;
        constr.gridwidth=2;
        constr.fill=constr.HORIZONTAL;
        JPanel settings = getSettingsPanel();
        keyPanel.add(settings, constr);

        // OK / Cancel buttons
        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT, 8, 5) );
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        constr.gridy++;
        constr.gridwidth=2;
        constr.fill=constr.HORIZONTAL;
        keyPanel.add( buttonPanel, constr );

        this.getContentPane().add(keyPanel, BorderLayout.CENTER); // add Main panel

        publicBoardRadioButton.setSelected(true);
        privateKeyTextField.setEnabled(false);
        publicKeyTextField.setEnabled(false);
        generateKeyButton.setEnabled(false);

        loadKeypair();
        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel getSettingsPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.anchor = GridBagConstraints.WEST;
        constr.insets = new Insets(5, 5, 5, 5);
        constr.gridx = 0; constr.gridy = 0;

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

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            dispose();
        }
        super.processWindowEvent(e);
    }

}

