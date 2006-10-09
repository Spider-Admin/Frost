/*
  SearchPanel.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.search;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import frost.util.gui.*;
import frost.util.gui.translation.*;

class SearchPanel extends JPanel {
    
    private Listener listener = new Listener();

    private Language language = Language.getInstance();

    private JPanel searchTopPanel = new JPanel();
    private JTranslatableComboBox searchComboBox = null;
    private JButton searchButton = new JButton(new ImageIcon(getClass().getResource("/data/search.gif")));
    private JTextField searchTextField = new JTextField(25);

    private boolean isInitialized = false;
    
    CloseableTabbedPane searchTabs = new CloseableTabbedPane();

    public SearchPanel() {
        super();

        language.addLanguageListener(listener);
    }

    public void initialize() {
        if (!isInitialized) {
            refreshLanguage();

            // create the top panel
            new TextComponentClipboardMenu(searchTextField, language);

            String[] searchComboBoxKeys =
                { "SearchPane.fileTypes.allFiles", 
                  "SearchPane.fileTypes.audio", 
                  "SearchPane.fileTypes.video", 
                  "SearchPane.fileTypes.images", 
                  "SearchPane.fileTypes.documents", 
                  "SearchPane.fileTypes.executables", 
                  "SearchPane.fileTypes.archives" };
            searchComboBox = new JTranslatableComboBox(language, searchComboBoxKeys);

            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(searchButton, "/data/search_rollover.gif");
            searchComboBox.setMaximumSize(searchComboBox.getPreferredSize());
            searchTextField.setMaximumSize(searchTextField.getPreferredSize());

            BoxLayout dummyLayout = new BoxLayout(searchTopPanel, BoxLayout.X_AXIS);
            Dimension blankSpace = new Dimension(8, 0);
            searchTopPanel.setLayout(dummyLayout);
            searchTopPanel.add(searchTextField);
            searchTopPanel.add(Box.createRigidArea(blankSpace));
            searchTopPanel.add(searchComboBox);
            searchTopPanel.add(Box.createRigidArea(blankSpace));
            searchTopPanel.add(searchButton);
//            searchTopPanel.add(Box.createRigidArea(blankSpace));
//            searchTopPanel.add(downloadButton);
//            searchTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
            searchTopPanel.add(Box.createHorizontalGlue());
//            searchTopPanel.add(searchResultsCountLabel);

            // create the main search panel
            
            searchTabs = new CloseableTabbedPane();
            
            setLayout(new BorderLayout());
            add(searchTopPanel, BorderLayout.NORTH);
            add(searchTabs, BorderLayout.CENTER);

            // listeners
            searchTextField.addActionListener(listener);
            searchButton.addActionListener(listener);
            
            isInitialized = true;
        }
    }
    
    private void refreshLanguage() {
        searchButton.setToolTipText(language.getString("SearchPane.toolbar.tooltip.search"));
    }

    /**
     * searchButton Action Listener (Search)
     */
    private void searchButton_actionPerformed(ActionEvent e) {
        startNewSearch();
    }

    private void searchTextField_actionPerformed(ActionEvent e) {
        startNewSearch();
    }
    
    private void startNewSearch() {

        String searchText = searchTextField.getText().trim();
        if( searchText.length() == 0 ) {
            searchText = "*";
        }

        SearchTableFormat tableFormat = new SearchTableFormat();
        SearchModel model = new SearchModel();
        SearchTable modelTable = new SearchTable(model, tableFormat, searchTabs, searchText);
        
        searchTabs.addTab(searchText + " (...)", modelTable.getScrollPane());
        
        SearchThread searchThread =
            new SearchThread(
                searchText,
                searchComboBox.getSelectedKey(),
                modelTable);

        searchThread.start();
    }

    private class Listener
        extends MouseAdapter
        implements
            ActionListener,
            LanguageListener {
    
        public Listener() {
            super();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == searchTextField) {
                searchTextField_actionPerformed(e);
            }
            if (e.getSource() == searchButton) {
                searchButton_actionPerformed(e);
            }
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    }
}
