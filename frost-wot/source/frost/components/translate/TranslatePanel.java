/*
  TranslatePanel.java
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost.components.translate;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

import javax.swing.*;
//import javax.swing.event.*;
//import javax.swing.table.*;

/**
 * Translate Component
 * Should be used by the users to translate Frost into other languages. The translated files 
 * get hopefully sent to us (within Frost or by mail). The user would have to recompile
 * Frost in order to use the files, a warning describing this will be displayed on
 * startup.
 * @author Jantho
 */
public class TranslatePanel extends JPanel {
    
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    JFrame parent;
    
    //
    // GUI objects
    //
    JPanel contentPanel;

    //Table for original text and translation
    TranslateTableModel tableModel = new TranslateTableModel();
    JTable table = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(table);

    //ToolBar
    JToolBar toolBar = new JToolBar();
    JComboBox comboBox = new JComboBox();
    JButton saveButton = new JButton("Save translation");
    JButton loadButton = new JButton("Load translation from file");

    /**Constructor*/
    public TranslatePanel(JFrame parent) {
	this.parent = parent;
	init();
    }

    /**Initialize the panel*/
    private void init() {
	//assemble gui objects
	generateComboBox();
	comboBox.setMaximumRowCount(25);

	toolBar.add(comboBox);
	toolBar.add(saveButton);
	toolBar.add(loadButton);

	contentPanel = this;
	contentPanel.setLayout(new BorderLayout());		
	contentPanel.add(toolBar, BorderLayout.NORTH);
	contentPanel.add(scrollPane, BorderLayout.CENTER);

	generateButtonListeners();
	generateComboBoxListeners();

	configureTable("en");
    }
    
    /**
     * Clears the table, get's the strings for the
     * requested language and inserts them into
     * the tableModel
     * @param language Language strings as defined in the locale object
     */
    private void configureTable(String language) {
	java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("res.LangRes", 
									     new Locale(getSelectedLanguageCode()));
	
	tableModel.setRowCount(0);
	for (Enumeration e = bundle.getKeys(); e.hasMoreElements(); ) {
	    String originalText = (String)e.nextElement();
	    String row[] = {originalText, bundle.getString(originalText)};
	    tableModel.addRow(row);
	}
    }

    /**
     * Enters some default entries into the
     * comboBox.
     */
    private void generateComboBox() {
	Locale actualLocale = LangRes.getLocale();
	Locale availableLocales[] = actualLocale.getAvailableLocales();
	for (int i = 0; i < availableLocales.length; i++) {
	    comboBox.addItem(availableLocales[i].getDisplayName() + " (" +  
			     availableLocales[i].getLanguage() + ")");
	}
    }

    /**
     * Adds action listeners for the comboBox
     */
    private void generateComboBoxListeners() {
	comboBox.addItemListener(new java.awt.event.ItemListener() {
		public void itemStateChanged(java.awt.event.ItemEvent e) {
		    configureTable((String)comboBox.getSelectedItem());
		}
	    });
    }

    private String getSelectedLanguageCode() {
	String selectedElement = (String)comboBox.getSelectedItem();
	String languageCode = selectedElement.substring(selectedElement.length() - 3, 
							selectedElement.length() - 1);
	return languageCode;
    }

    /**
     * Generates button listeners
     */
    private void generateButtonListeners() {
	saveButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String languageCode = getSelectedLanguageCode();
		    String content = LanguageFile.generateFile(tableModel, languageCode);
		    if (languageCode.equals("en")) {
			WriteAccess.writeFile(content, 
					      new File("LangRes.java"));
		    }
		    else {
			WriteAccess.writeFile(content, 
					      new File("LangRes_" + languageCode + ".java"));
		    }
		}
	    });
	loadButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    loadLanguageFile();
		}
	    });
    }
    
    private void loadLanguageFile() {
        final JFileChooser fc = new JFileChooser(new File("."));
        fc.setDialogTitle("Select " + getSelectedLanguageCode() + " language file");
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showOpenDialog(this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File file = fc.getSelectedFile();
            if( file != null )
            {
                if( !file.isDirectory() )
                {
		    LanguageFile.readLanguageFile(tableModel, file);
                }
            }
        }
    }

}
