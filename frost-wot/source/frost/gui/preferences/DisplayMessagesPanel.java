/*
  DisplayMessagesPanel.java / Frost
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

public class DisplayMessagesPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JCheckBox messageBodyAACheckBox = new JCheckBox();
    private JCheckBox msgTableMultilineSelectCheckBox = new JCheckBox();
    private JCheckBox msgTableScrollHorizontalCheckBox = new JCheckBox();
    private JCheckBox sortThreadRootMsgsAscendingCheckBox = new JCheckBox();
    private JCheckBox showCollapsedThreadsCheckBox = new JCheckBox();
    private JCheckBox showDeletedMessagesCheckBox = new JCheckBox();
    private JCheckBox dontShowOwnMessagesAsNewCheckBox = new JCheckBox();

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected DisplayMessagesPanel(JDialog owner, SettingsClass settings) {
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
        add(messageBodyAACheckBox, constraints);

        constraints.insets = inset5511;
        constraints.gridy++;
        add(msgTableMultilineSelectCheckBox, constraints);
        
        constraints.gridy++;
        add(msgTableScrollHorizontalCheckBox, constraints);
        
        constraints.gridy++;
        add(sortThreadRootMsgsAscendingCheckBox, constraints);

        constraints.gridy++;
        add(showCollapsedThreadsCheckBox, constraints);

        constraints.gridy++;
        add(showDeletedMessagesCheckBox, constraints);

        constraints.gridy++;
        add(dontShowOwnMessagesAsNewCheckBox, constraints);

        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JLabel(""), constraints);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        messageBodyAACheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_BODY_ANTIALIAS));
        msgTableMultilineSelectCheckBox.setSelected(settings.getBoolValue(SettingsClass.MSGTABLE_MULTILINE_SELECT));
        msgTableScrollHorizontalCheckBox.setSelected(settings.getBoolValue(SettingsClass.MSGTABLE_SCROLL_HORIZONTAL));
        sortThreadRootMsgsAscendingCheckBox.setSelected(settings.getBoolValue(SettingsClass.SORT_THREADROOTMSGS_ASCENDING));
        showCollapsedThreadsCheckBox.setSelected(settings.getBoolValue(SettingsClass.MSGTABLE_SHOW_COLLAPSED_THREADS));
        showDeletedMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_DELETED_MESSAGES));
        dontShowOwnMessagesAsNewCheckBox.setSelected(settings.getBoolValue(SettingsClass.HANDLE_OWN_MESSAGES_AS_NEW_DISABLED));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        messageBodyAACheckBox.setText(language.getString("Options.display.enableAntialiasingForMessageBody"));
        msgTableScrollHorizontalCheckBox.setText(language.getString("Options.display.showHorizontalScrollbarInMessageTable"));
        msgTableMultilineSelectCheckBox.setText(language.getString("Options.display.enableMultilineSelectionsInMessageTable"));
        sortThreadRootMsgsAscendingCheckBox.setText(language.getString("Options.display.sortThreadRootMsgsAscending"));
        showCollapsedThreadsCheckBox.setText(language.getString("Options.display.showCollapsedThreads"));
        showDeletedMessagesCheckBox.setText(language.getString("Options.news.3.showDeletedMessages"));
        dontShowOwnMessagesAsNewCheckBox.setText(language.getString("Options.news.3.dontHandleOwnMessagesAsNew"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.MESSAGE_BODY_ANTIALIAS, messageBodyAACheckBox.isSelected());
        settings.setValue(SettingsClass.MSGTABLE_MULTILINE_SELECT, msgTableMultilineSelectCheckBox.isSelected());
        settings.setValue(SettingsClass.MSGTABLE_SCROLL_HORIZONTAL, msgTableScrollHorizontalCheckBox.isSelected());
        settings.setValue(SettingsClass.SORT_THREADROOTMSGS_ASCENDING, sortThreadRootMsgsAscendingCheckBox.isSelected());
        settings.setValue(SettingsClass.MSGTABLE_SHOW_COLLAPSED_THREADS, showCollapsedThreadsCheckBox.isSelected());
        settings.setValue(SettingsClass.SHOW_DELETED_MESSAGES, showDeletedMessagesCheckBox.isSelected());
        settings.setValue(SettingsClass.HANDLE_OWN_MESSAGES_AS_NEW_DISABLED, dontShowOwnMessagesAsNewCheckBox.isSelected());
    }
}
