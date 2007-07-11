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

import java.awt.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.identities.*;

public class SharedFilesOwnerDialog extends JDialog {
    
    public static int OK = 1;
    public static int CANCEL = 2;

    private String title;
    
    private int returnCode = CANCEL;
    private String choosedIdentity = null;

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bcancel = null;
    private JButton Bok = null;
    private JLabel jLabel = null;
    private JComboBox CBidentities = null;
    
    private Frame parent;

    /**
     * This is the default constructor
     */
    public SharedFilesOwnerDialog(Frame newParent, String newTitle) {
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
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
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
     * @return javax.swing.JPanel	
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints11.gridy = 3;
            gridBagConstraints11.weightx = 1.0;
            gridBagConstraints11.insets = new java.awt.Insets(2,20,0,5);
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints11.gridx = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5,5,0,5);
            gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Choose the owner identity for the new shared files:");
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(jLabel, gridBagConstraints);
            mainPanel.add(getCBidentities(), gridBagConstraints11);
        }
        return mainPanel;
    }

    /**
     * This method initializes Bok	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton("Cancel");
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
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
     * @return javax.swing.JButton	
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton("Ok");
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    returnCode = OK;
                    choosedIdentity = (String)getCBidentities().getSelectedItem();
                    setVisible(false);
                }
            });
        }
        return Bok;
    }

    /**
     * This method initializes CBidentities	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getCBidentities() {
        if( CBidentities == null ) {
            CBidentities = new JComboBox();
            for(Iterator i=Core.getIdentities().getLocalIdentities().iterator(); i.hasNext(); ) {
                LocalIdentity id = (LocalIdentity)i.next();
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

}  //  @jve:decl-index=0:visual-constraint="19,14"
