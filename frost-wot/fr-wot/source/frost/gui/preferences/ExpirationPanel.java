/*
  ExpirationPanel.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

import javax.swing.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class ExpirationPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JRadioButton RbKeepExpiredMessages = new JRadioButton();
    private JRadioButton RbArchiveExpiredMessages = new JRadioButton();
    private JRadioButton RbDeleteExpiredMessages = new JRadioButton();
    private ButtonGroup BgExpiredMessages = new ButtonGroup();

    private JLabel LmessageExpireDays = new JLabel();
    private JTextField TfMessageExpireDays = new JTextField(8);

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected ExpirationPanel(JDialog owner, SettingsClass settings) {
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
        constraints.anchor = GridBagConstraints.WEST;
        Insets insets0555 = new Insets(0, 5, 5, 5);

        int maxGridWidth = 2;

        constraints.insets = insets0555;
        constraints.gridy = 0;

        constraints.gridx = 0;
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridBagLayout());
        subPanel.add(LmessageExpireDays, constraints);
        constraints.gridx = 1;
        subPanel.add(TfMessageExpireDays, constraints);

        constraints.gridx = 0;
        constraints.gridwidth = maxGridWidth;
        add(subPanel, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;

        constraints.gridx = 0;
        constraints.gridwidth = maxGridWidth;
        add(RbKeepExpiredMessages, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;

        constraints.gridx = 0;
        constraints.gridwidth = maxGridWidth;
        add(RbArchiveExpiredMessages, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;

        constraints.gridx = 0;
        constraints.gridwidth = maxGridWidth;
        add(RbDeleteExpiredMessages, constraints);
        constraints.gridwidth = 1;

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = maxGridWidth;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        // add radiobuttons to buttongroup
        BgExpiredMessages.add( RbKeepExpiredMessages );
        BgExpiredMessages.add( RbArchiveExpiredMessages );
        BgExpiredMessages.add( RbDeleteExpiredMessages );
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {

        TfMessageExpireDays.setText(settings.getValue(SettingsClass.MESSAGE_EXPIRE_DAYS));

        String mode = settings.getValue(SettingsClass.MESSAGE_EXPIRATION_MODE);
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

        settings.setValue(SettingsClass.MESSAGE_EXPIRE_DAYS, TfMessageExpireDays.getText());

        if( RbKeepExpiredMessages.isSelected() ) {
            settings.setValue(SettingsClass.MESSAGE_EXPIRATION_MODE, "KEEP");
        } else if( RbArchiveExpiredMessages.isSelected() ) {
            settings.setValue(SettingsClass.MESSAGE_EXPIRATION_MODE, "ARCHIVE");
        } else if( RbDeleteExpiredMessages.isSelected() ) {
            settings.setValue(SettingsClass.MESSAGE_EXPIRATION_MODE, "DELETE");
        } else {
            settings.setValue(SettingsClass.MESSAGE_EXPIRATION_MODE, "KEEP");
        }
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        RbKeepExpiredMessages.setText(language.getString("Options.expiration.keepExpiredMessages"));
        RbArchiveExpiredMessages.setText(language.getString("Options.expiration.archiveExpiredMessages"));
        RbDeleteExpiredMessages.setText(language.getString("Options.expiration.deleteExpiredMessages"));

        LmessageExpireDays.setText(language.getString("Options.expiration.numberOfDaysBeforeMessageExpires") + " (90)");
    }
}
