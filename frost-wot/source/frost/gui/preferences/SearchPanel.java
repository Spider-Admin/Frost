/*
  SearchPanel.java / Frost
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

import frost.SettingsClass;
import frost.util.gui.TextComponentClipboardMenu;
import frost.util.gui.translation.Language;

class SearchPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private JLabel archiveExtensionLabel = new JLabel();

    private JTextField archiveExtensionTextField = new JTextField();
    private JLabel audioExtensionLabel = new JLabel();
    private JTextField audioExtensionTextField = new JTextField();
    private JLabel documentExtensionLabel = new JLabel();
    private JTextField documentExtensionTextField = new JTextField();
    private JLabel executableExtensionLabel = new JLabel();
    private JTextField executableExtensionTextField = new JTextField();

    private JCheckBox hideBadFilesCheckBox = new JCheckBox();
    private JLabel imageExtensionLabel = new JLabel();
    private JTextField imageExtensionTextField = new JTextField();
    private JLabel maxSearchResultsLabel = new JLabel();
    private JTextField maxSearchResultsTextField = new JTextField(8);
    private JLabel videoExtensionLabel = new JLabel();
    private JTextField videoExtensionTextField = new JTextField();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected SearchPanel(SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private void initialize() {
        setName("SearchPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(archiveExtensionTextField, language);
        new TextComponentClipboardMenu(audioExtensionTextField, language);
        new TextComponentClipboardMenu(documentExtensionTextField, language);
        new TextComponentClipboardMenu(executableExtensionTextField, language);

        new TextComponentClipboardMenu(imageExtensionTextField, language);
        new TextComponentClipboardMenu(maxSearchResultsTextField, language);
        new TextComponentClipboardMenu(videoExtensionTextField, language);

        // Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 5, 5, 5);
        constraints.weighty = 0;
        constraints.gridwidth = 1;
        constraints.gridy = 0;

        constraints.weightx = 0;
        constraints.gridx = 0;
        add(imageExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(imageExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(videoExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(videoExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(archiveExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(archiveExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(documentExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(documentExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(audioExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(audioExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(executableExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(executableExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(maxSearchResultsLabel, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        add(maxSearchResultsTextField, constraints);

        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        add(hideBadFilesCheckBox, constraints);

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);
    }

    /**
     * Loads the settings of this panel
     */
    private void loadSettings() {
        audioExtensionTextField.setText(settings.getValue("audioExtension"));
        imageExtensionTextField.setText(settings.getValue("imageExtension"));
        videoExtensionTextField.setText(settings.getValue("videoExtension"));
        documentExtensionTextField.setText(settings.getValue("documentExtension"));
        executableExtensionTextField.setText(settings.getValue("executableExtension"));
        archiveExtensionTextField.setText(settings.getValue("archiveExtension"));
        maxSearchResultsTextField.setText(Integer.toString(settings.getIntValue("maxSearchResults")));
        hideBadFilesCheckBox.setSelected(settings.getBoolValue("hideBadFiles"));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        imageExtensionLabel.setText(language.getString("Options.search.imageExtension"));
        videoExtensionLabel.setText(language.getString("Options.search.videoExtension"));
        archiveExtensionLabel.setText(language.getString("Options.search.archiveExtension"));
        documentExtensionLabel.setText(language.getString("Options.search.documentExtension"));
        audioExtensionLabel.setText(language.getString("Options.search.audioExtension"));
        executableExtensionLabel.setText(language.getString("Options.search.executableExtension"));
        maxSearchResultsLabel.setText(language.getString("Options.search.maximumSearchResults"));

        hideBadFilesCheckBox.setText(language.getString("Options.search.hideFilesFromPeopleMarkedBad"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue("audioExtension", audioExtensionTextField.getText().toLowerCase());
        settings.setValue("imageExtension", imageExtensionTextField.getText().toLowerCase());
        settings.setValue("videoExtension", videoExtensionTextField.getText().toLowerCase());
        settings.setValue("documentExtension", documentExtensionTextField.getText().toLowerCase());
        settings.setValue("executableExtension", executableExtensionTextField.getText().toLowerCase());
        settings.setValue("archiveExtension", archiveExtensionTextField.getText().toLowerCase());
        settings.setValue("maxSearchResults", maxSearchResultsTextField.getText());

        settings.setValue("hideBadFiles", hideBadFilesCheckBox.isSelected());
    }
}
