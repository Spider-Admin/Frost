/*
  SkinPanel.java / Frost
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
package frost.gui.preferences;

import java.awt.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.translation.*;

public class SkinPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JLabel moreSkinsLabel = new JLabel();
    private SkinChooser skinChooser = null;

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected SkinPanel(JDialog owner, SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    public void cancel() {
        skinChooser.cancelChanges();
    }

    /**
     * Initialize the class.
     */
    private void initialize() {
        setName("SkinPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        //Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        Insets inset1515 = new Insets(1, 5, 1, 5);

        constraints.insets = inset1515;
        constraints.gridx = 0;
        constraints.gridy = 0;
        skinChooser = new SkinChooser(language);
        add(skinChooser, constraints);

        constraints.insets = inset1515;
        constraints.gridx = 0;
        constraints.gridy = 1;
        moreSkinsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(moreSkinsLabel, constraints);

        // glue
        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JLabel(""), constraints);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        boolean skinsEnabled = settings.getBoolValue(SettingsClass.SKINS_ENABLED);
        skinChooser.setSkinsEnabled(skinsEnabled);
        String selectedSkinPath = settings.getValue(SettingsClass.SKIN_NAME);
        skinChooser.setSelectedSkin(selectedSkinPath);
    }

    public void ok() {
        skinChooser.commitChanges();
        saveSettings();
    }

    private void refreshLanguage() {
        moreSkinsLabel.setText(language.getString("Options.display.youCanGetMoreSkinsAt") + " http://javootoo.l2fprod.com/plaf/skinlf/");
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        boolean skinsEnabled = skinChooser.isSkinsEnabled();
        settings.setValue(SettingsClass.SKINS_ENABLED, skinsEnabled);

        String selectedSkin = skinChooser.getSelectedSkin();
        if (selectedSkin == null) {
            settings.setValue(SettingsClass.SKIN_NAME, "none");
        } else {
            settings.setValue(SettingsClass.SKIN_NAME, selectedSkin);
        }
    }
}
