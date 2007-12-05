/*
  JunkPanel.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
import frost.util.gui.translation.*;

public class JunkPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private final JCheckBox hideJunkMessagesCheckBox = new JCheckBox();
    private final JCheckBox markJunkIdentityBadCheckBox = new JCheckBox();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected JunkPanel(final SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private void initialize() {
        setName("JunkPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        final Insets insets5555 = new Insets(5, 5, 5, 5);
        constraints.insets = insets5555;
        constraints.gridwidth = 1;
        constraints.gridy=0;

        constraints.insets = insets5555;
        constraints.gridx = 0;

        add(hideJunkMessagesCheckBox, constraints);

        constraints.gridy++;

        add(markJunkIdentityBadCheckBox, constraints);

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        hideJunkMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.JUNK_HIDE_JUNK_MESSAGES));
        markJunkIdentityBadCheckBox.setSelected(settings.getBoolValue(SettingsClass.JUNK_MARK_JUNK_IDENTITY_BAD));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        hideJunkMessagesCheckBox.setText(language.getString("Options.junk.hideJunkMessages"));
        markJunkIdentityBadCheckBox.setText(language.getString("Options.junk.markJunkIdentityBad"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.JUNK_HIDE_JUNK_MESSAGES, hideJunkMessagesCheckBox.isSelected());
        settings.setValue(SettingsClass.JUNK_MARK_JUNK_IDENTITY_BAD, markJunkIdentityBadCheckBox.isSelected());
    }
}
