/*
  ExpirationPanel.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class ExpirationPanel extends JPanel {
        
    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == RbKeepExpiredMessages) {
                radioButtonChanged();
            }
            if (e.getSource() == RbArchiveExpiredMessages) {
                radioButtonChanged();
            }
            if (e.getSource() == RbDeleteExpiredMessages) {
                radioButtonChanged();
            }
        }
        private void radioButtonChanged() {
            LarchiveFolder.setEnabled(RbArchiveExpiredMessages.isSelected());
            TfArchiveFolder.setEnabled(RbArchiveExpiredMessages.isSelected());
        }
    }
    
    private SettingsClass settings = null;
    private Language language = null;
    
    private JRadioButton RbKeepExpiredMessages = new JRadioButton();
    private JRadioButton RbArchiveExpiredMessages = new JRadioButton();
    private JRadioButton RbDeleteExpiredMessages = new JRadioButton();
    private ButtonGroup BgExpiredMessages = new ButtonGroup();
    
    private JLabel LmessageExpireDays = new JLabel();
    private JTextField TfMessageExpireDays = new JTextField(8);

    private JLabel LarchiveFolder = new JLabel();
    private JTextField TfArchiveFolder = new JTextField(30);
    
    private Listener listener = new Listener();
        
    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
     */
    protected ExpirationPanel(SettingsClass settings) {
        super();
        
        this.language = Language.getInstance();
        this.settings = settings;
        
        initialize();
        loadSettings();
    }
        
    private void initialize() {
        setName("ExpirationPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();
            
        // We create the components
        new TextComponentClipboardMenu(TfMessageExpireDays, language);
        
        // Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        Insets insets5555 = new Insets(5, 5, 5, 5);
        Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
        constraints.insets = insets5555;

        constraints.gridy = 0;

        constraints.gridx = 0;
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridBagLayout());
        subPanel.add(LmessageExpireDays, constraints);
        constraints.gridx = 1;
        subPanel.add(TfMessageExpireDays, constraints);
        
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        add(subPanel, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;

        constraints.gridx = 0;
        constraints.gridwidth = 2;
        add(RbKeepExpiredMessages, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;

        constraints.gridx = 0;
        constraints.gridwidth = 2;
        add(RbArchiveExpiredMessages, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;

        constraints.gridx = 0;
        constraints.insets = insets5_30_5_5;
        add(LarchiveFolder, constraints);
        constraints.gridx = 1;
        constraints.insets = insets5555;
        add(TfArchiveFolder, constraints);
        
        constraints.gridy++;

        constraints.gridx = 0;
        constraints.gridwidth = 2;
        add(RbDeleteExpiredMessages, constraints);
        constraints.gridwidth = 1;
        
        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        // Add listeners
        RbKeepExpiredMessages.addActionListener(listener);
        RbArchiveExpiredMessages.addActionListener(listener);
        RbDeleteExpiredMessages.addActionListener(listener);

        // add radiobuttons to buttongroup
        BgExpiredMessages.add( RbKeepExpiredMessages );
        BgExpiredMessages.add( RbArchiveExpiredMessages );
        BgExpiredMessages.add( RbDeleteExpiredMessages );
    }
        
    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
          
        TfMessageExpireDays.setText(settings.getValue("messageExpireDays"));
        TfArchiveFolder.setText(settings.getValue("archive.dir"));

        String mode = settings.getValue("messageExpirationMode");
        if( mode.toUpperCase().equals("KEEP") ) {
            RbKeepExpiredMessages.doClick();
        } else if( mode.toUpperCase().equals("ARCHIVE") ) {
            RbArchiveExpiredMessages.doClick();
        } else if( mode.toUpperCase().equals("DELETE") ) {
            RbDeleteExpiredMessages.doClick();
        } else {
            RbKeepExpiredMessages.doClick(); // // unknown value, use default
        }
    }

    /**
     * Save the settings of this panel 
     */
    private void saveSettings() {

        settings.setValue("messageExpireDays", TfMessageExpireDays.getText());
        settings.setValue("archive.dir", TfArchiveFolder.getText());

        if( RbKeepExpiredMessages.isSelected() ) {
            settings.setValue("messageExpirationMode", "KEEP");
        } else if( RbArchiveExpiredMessages.isSelected() ) {
            settings.setValue("messageExpirationMode", "ARCHIVE");
        } else if( RbDeleteExpiredMessages.isSelected() ) {
            settings.setValue("messageExpirationMode", "DELETE");
        } else {
            settings.setValue("messageExpirationMode", "KEEP");
        }
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        RbKeepExpiredMessages.setText(language.getString("Keep expired messages in keypool"));
        RbArchiveExpiredMessages.setText(language.getString("Archive expired messages"));
        RbDeleteExpiredMessages.setText(language.getString("Delete expired messages from keypool"));
        
        LmessageExpireDays.setText(language.getString("Number of days before a message expires") + " (30)");
        LarchiveFolder.setText(language.getString("Archive folder"));
    }
}
