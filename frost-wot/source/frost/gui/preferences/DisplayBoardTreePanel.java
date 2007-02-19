/*
  DisplayBoardTreePanel.java / Frost
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

public class DisplayBoardTreePanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JCheckBox showBoardDescTooltipsCheckBox = new JCheckBox();
    private JCheckBox showBoardUpdateCountCheckBox = new JCheckBox();
    private JCheckBox preventBoardtreeReordering = new JCheckBox();
    private JCheckBox showFlaggedStarredIndicators = new JCheckBox();

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected DisplayBoardTreePanel(JDialog owner, SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    public void cancel() {
    }

    /**
     * Initialize the class.
     */
    private void initialize() {
        setName("DisplayPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        Insets inset5511 = new Insets(5, 5, 1, 1);
        
        constraints.insets = inset5511;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(showBoardUpdateCountCheckBox, constraints);

        constraints.gridy++;
        add(showBoardDescTooltipsCheckBox, constraints);

        constraints.gridy++;
        add(preventBoardtreeReordering, constraints);
        
        constraints.gridy++;
        add(showFlaggedStarredIndicators, constraints);

        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JLabel(""), constraints);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        showBoardUpdateCountCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_BOARD_UPDATED_COUNT));
        showBoardDescTooltipsCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_BOARDDESC_TOOLTIPS));
        preventBoardtreeReordering.setSelected(settings.getBoolValue(SettingsClass.PREVENT_BOARDTREE_REORDERING));
        showFlaggedStarredIndicators.setSelected(settings.getBoolValue(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        showBoardUpdateCountCheckBox.setText(language.getString("Options.display.showBoardUpdateCount"));
        showBoardDescTooltipsCheckBox.setText(language.getString("Options.display.showTooltipWithBoardDescriptionInBoardTree"));
        preventBoardtreeReordering.setText(language.getString("Options.display.preventBoardtreeReordering"));
        showFlaggedStarredIndicators.setText(language.getString("Options.display.showBoardtreeFlaggedStarredIndicators"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.SHOW_BOARD_UPDATED_COUNT, showBoardUpdateCountCheckBox.isSelected());
        settings.setValue(SettingsClass.SHOW_BOARDDESC_TOOLTIPS, showBoardDescTooltipsCheckBox.isSelected());
        settings.setValue(SettingsClass.PREVENT_BOARDTREE_REORDERING, preventBoardtreeReordering.isSelected());
        settings.setValue(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR, showFlaggedStarredIndicators.isSelected());
    }
}
