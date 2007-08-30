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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
    private JButton refreshButton;
    private JTable jTable1;
    private JScrollPane jScrollPane1;

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
        this.setPreferredSize(new java.awt.Dimension(363, 190));
        refreshLanguage();

        //Adds all of the components
        this.add(enablePlugins, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
        enablePlugins.setText(language.getString("Options.plugins.enabletext"));

        this.add(directoryLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
        directoryLabel.setText(language.getString("Options.plugins.dirtext"));
        this.add(directoryTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 0), 0, 0));
        this.add(browseDirectoryButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
   
		//START >>  refreshButton
		refreshButton = new JButton();
		this.add(refreshButton, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		refreshButton.setText(language.getString("Options.plugins.refresch"));
		//END <<  refreshButton
		//START >>  jScrollPane1
		jScrollPane1 = new JScrollPane();
		this.add(jScrollPane1, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 15, 0), 0, 0));
		//START >>  jTable1
		TableModel jTable1Model = new DefaultTableModel(new String[][] {
				{ "One", "Two" }, { "Three", "Four" } }, new String[] {
				"Path", "Plugin", "loaded", "load on startup" });
		jTable1 = new JTable();
		jScrollPane1.setViewportView(jTable1);
		jTable1.setModel(jTable1Model);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//END <<  jTable1
		//END <<  jScrollPane1

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
