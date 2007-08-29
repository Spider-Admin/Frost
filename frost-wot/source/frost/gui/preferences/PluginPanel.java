/*
  DisplayPanel.java / Frost
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import frost.SettingsClass;
import frost.util.gui.translation.Language;

/**
 * Plugin Panel. Contains plugin options
 */
class PluginPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	browseDirectoryPressed();
        }
    }

    private JDialog owner = null;
    private SettingsClass settings = null;
    private Language language = null;

    private Listener listener = new Listener();

    private JCheckBox enablePlugins = new JCheckBox();

    private JButton browseDirectoryButton = new JButton();
    private JLabel directoryLabel = new JLabel();
    private JTextField directoryTextField = new JTextField(20);
    
    private JList pluginList = new JList();
    private JLabel pluginlistLabel = new JLabel();

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected PluginPanel(JDialog owner, SettingsClass settings) {
        super();

        this.owner = owner;
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
        setName("PluginPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        //Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        Insets inset5511 = new Insets(5, 5, 1, 1);
        Insets insets2 = new Insets(15,5,1,1);
        
        constraints.insets = inset5511;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(enablePlugins, constraints);

        constraints.gridy++;
        
        constraints.gridx = 0;
        add(directoryLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(directoryTextField, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        add(browseDirectoryButton, constraints);
   
        constraints.insets = inset5511;

        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JLabel(""), constraints);

        // add listeners
        browseDirectoryButton.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        enablePlugins.setSelected(settings.getBoolValue(SettingsClass.PLUGIN_ENABLED));
        directoryTextField.setText(settings.getValue(SettingsClass.PLUGIN_DIRS));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
    	enablePlugins.setText(language.getString("Options.plugins.enabletext"));
    	directoryLabel.setText(language.getString("Options.plugins.dir") + ": ");
        browseDirectoryButton.setText(language.getString("Common.browse") + "...");
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.PLUGIN_ENABLED, enablePlugins.isSelected());
        settings.setValue(SettingsClass.PLUGIN_DIRS, directoryTextField.getText());
    }
    
    private void browseDirectoryPressed() {
        final JFileChooser fc = new JFileChooser(settings.getValue(SettingsClass.DIR_LAST_USED));
        fc.setDialogTitle(language.getString("Options.plugins.dirchooser.title"));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showOpenDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileSeparator = System.getProperty("file.separator");
            File file = fc.getSelectedFile();
            directoryTextField.setText(file.getPath() + fileSeparator);
        }
    }
}
