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
import frost.util.gui.search.*;
import frost.util.gui.translation.*;

public class SearchPanel extends JPanel implements LanguageListener {
    
    private Language language = Language.getInstance();

    private SearchSimpleToolBar searchSimpleToolBar;
    private SearchAdvancedToolBar searchAdvancedToolBar;
    
    private ImageIcon searchIcon = new ImageIcon(getClass().getResource("/data/search.gif"));
    private ImageIcon clearIcon = new ImageIcon(getClass().getResource("/data/remove.gif"));
    
    TableFindAction tableFindAction = new TableFindAction();
    
    private boolean isInitialized = false;
    
    private CloseableTabbedPane searchTabs;

    private String[] searchComboBoxKeys =
        { "SearchPane.fileTypes.allFiles", 
          "SearchPane.fileTypes.audio", 
          "SearchPane.fileTypes.video", 
          "SearchPane.fileTypes.images", 
          "SearchPane.fileTypes.documents", 
          "SearchPane.fileTypes.executables", 
          "SearchPane.fileTypes.archives" };

    public SearchPanel() {
        super();

        language.addLanguageListener(this);
    }

    public void initialize() {
        if (!isInitialized) {
            
            searchSimpleToolBar = new SearchSimpleToolBar();
            searchAdvancedToolBar = new SearchAdvancedToolBar();

            languageChanged(null);

            setLayout(new BorderLayout());
            
            searchTabs = new SearchCloseableTabbedPane();
            add(searchTabs, BorderLayout.CENTER);

            // adds simple top panel
            toggleMode(true); // start in simple mode
            
            isInitialized = true;
        }
    }
    
    private void toggleMode(boolean toSimpleMode) {
        if( toSimpleMode ) {
            // switch to simple
            remove(searchAdvancedToolBar);
            add(searchSimpleToolBar, BorderLayout.NORTH);
        } else {
            // switch to extented
            remove(searchSimpleToolBar);
            add(searchAdvancedToolBar, BorderLayout.NORTH);
        }
        updateUI();
    }
    
    public void languageChanged(LanguageEvent event) {
        searchSimpleToolBar.refreshLanguage();
        searchAdvancedToolBar.refreshLanguage();
    }
    
    public void startNewSearch(SearchParameters searchParams) {
        
        String tabText = searchParams.getTabText();

        SearchModel model = new SearchModel(new SearchTableFormat());
        SearchTable modelTable = new SearchTable(model, searchTabs, tabText);
        tableFindAction.install( modelTable.getTable() );

        ProxyPanel pp = new ProxyPanel(modelTable.getScrollPane(), model, modelTable.getTable());
        
        searchTabs.addTab(tabText + " (...)", pp);
        
        SearchThread searchThread = new SearchThread(searchParams, modelTable, pp);
        searchThread.start();
    }

    private class SearchSimpleToolBar extends JToolBar implements ActionListener {

        private JTranslatableComboBox searchComboBox = null;
        private JButton searchButton = new JButton(searchIcon);
        private JTextField searchTextField = new JTextField(30);
        private JButton toggleModeButtonToAdvanced = new JButton(">>");
        private JCheckBox withKeyOnlyCheckBox = new JCheckBox();

        public SearchSimpleToolBar() {
            super();
            initialize();
        }
        
        private void initialize() {
            
            setRollover(true);
            setFloatable(false);

            new TextComponentClipboardMenu(searchTextField, language);

            searchComboBox = new JTranslatableComboBox(language, searchComboBoxKeys);
            withKeyOnlyCheckBox.setOpaque(false);
            toggleModeButtonToAdvanced.setOpaque(false);

            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(searchButton, "/data/search_rollover.gif");
            searchComboBox.setMaximumSize(searchComboBox.getPreferredSize());
            searchTextField.setMaximumSize(searchTextField.getPreferredSize());
            
            toggleModeButtonToAdvanced.setMargin(new Insets(0, 0, 0, 0));
            toggleModeButtonToAdvanced.setBorderPainted(false);
            toggleModeButtonToAdvanced.setFocusPainted(false);
            toggleModeButtonToAdvanced.setRolloverEnabled(true);

            Dimension blankSpace = new Dimension(8, 0);
            add(toggleModeButtonToAdvanced);
            add(Box.createRigidArea(blankSpace));
            add(searchTextField);
            add(Box.createRigidArea(blankSpace));
            add(searchComboBox);
            add(Box.createRigidArea(blankSpace));
            add(withKeyOnlyCheckBox);
            add(Box.createRigidArea(blankSpace));
            add(searchButton);
            add(Box.createHorizontalGlue());
            
            searchTextField.addActionListener(this);
            searchButton.addActionListener(this);
            toggleModeButtonToAdvanced.addActionListener(this);
        }
        
        public SearchParameters getSearchParameters() {
            SearchParameters sp = new SearchParameters(true);
            sp.setExtensions(searchComboBox.getSelectedKey());
            sp.setSimpleSearchString(searchTextField.getText());
            sp.setWithKeyOnly(withKeyOnlyCheckBox.isSelected());
            return sp;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == searchButton || e.getSource() == searchTextField) {
                startNewSearch(getSearchParameters());
            }
            if (e.getSource() == toggleModeButtonToAdvanced) {
                toggleMode(false);
            }
        }

        public void refreshLanguage() {
            searchButton.setToolTipText(language.getString("SearchPane.toolbar.tooltip.search"));
            toggleModeButtonToAdvanced.setToolTipText(language.getString("SearchPane.toolbar.tooltip.toggleMode"));
            withKeyOnlyCheckBox.setText(language.getString("SearchPane.toolbar.chkKey"));
            withKeyOnlyCheckBox.setToolTipText(language.getString("SearchPane.toolbar.tooltip.chkKey"));
        }
    }

    private class SearchAdvancedToolBar extends JToolBar implements ActionListener {

        private JTranslatableComboBox searchComboBox = null;
        private JButton searchButton = new JButton(searchIcon);
        private JButton clearButton = new JButton(clearIcon);
        private JButton toggleModeButtonToSimple = new JButton("<<");

        private JLabel searchNameLabel = new JLabel();
        private JLabel searchCommentLabel = new JLabel();
        private JLabel searchKeywordsLabel = new JLabel();
        private JLabel searchOwnerLabel = new JLabel();
        
        private JTextField searchNameTextField = new JTextField(18);
        private JTextField searchCommentTextField = new JTextField(18);
        private JTextField searchKeywordsTextField = new JTextField(18);
        private JTextField searchOwnerTextField = new JTextField(18);

        private JCheckBox withKeyOnlyCheckBox = new JCheckBox();

        public SearchAdvancedToolBar() {
            super();
            initialize();
        }
        
        private void initialize() {

            setRollover(true);
            setFloatable(false);

            new TextComponentClipboardMenu(searchNameTextField, language);
            new TextComponentClipboardMenu(searchCommentTextField, language);
            new TextComponentClipboardMenu(searchKeywordsTextField, language);
            new TextComponentClipboardMenu(searchOwnerTextField, language);

            searchComboBox = new JTranslatableComboBox(language, searchComboBoxKeys);
            withKeyOnlyCheckBox.setOpaque(false);
            toggleModeButtonToSimple.setOpaque(false);

            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(searchButton, "/data/search_rollover.gif");
            toolkit.configureButton(clearButton, "/data/remove_rollover.gif");
            searchComboBox.setMaximumSize(searchComboBox.getPreferredSize());
            searchNameTextField.setMaximumSize(searchNameTextField.getPreferredSize());
            searchCommentTextField.setMaximumSize(searchCommentTextField.getPreferredSize());
            searchKeywordsTextField.setMaximumSize(searchKeywordsTextField.getPreferredSize());
            searchOwnerTextField.setMaximumSize(searchOwnerTextField.getPreferredSize());
            
            toggleModeButtonToSimple.setMargin(new Insets(0, 0, 0, 0));
            toggleModeButtonToSimple.setBorderPainted(false);
            toggleModeButtonToSimple.setFocusPainted(false);
            toggleModeButtonToSimple.setRolloverEnabled(true);

            Dimension blankSpace = new Dimension(8, 0);
            Dimension smallSpace = new Dimension(3, 0);
            add(toggleModeButtonToSimple);
            add(Box.createRigidArea(blankSpace));
            add(searchNameLabel);
            add(Box.createRigidArea(smallSpace));
            add(searchNameTextField);
            add(Box.createRigidArea(blankSpace));
            add(searchCommentLabel);
            add(Box.createRigidArea(smallSpace));
            add(searchCommentTextField);
            add(Box.createRigidArea(blankSpace));
            add(searchKeywordsLabel);
            add(Box.createRigidArea(smallSpace));
            add(searchKeywordsTextField);
            add(Box.createRigidArea(blankSpace));
            add(searchOwnerLabel);
            add(Box.createRigidArea(smallSpace));
            add(searchOwnerTextField);
            add(Box.createRigidArea(blankSpace));
            add(searchComboBox);
            add(Box.createRigidArea(blankSpace));
            add(withKeyOnlyCheckBox);
            add(Box.createRigidArea(smallSpace));
            add(clearButton);
            add(Box.createRigidArea(blankSpace));
            add(searchButton);
            add(Box.createHorizontalGlue());
            
            searchNameTextField.addActionListener(this);
            searchCommentTextField.addActionListener(this);
            searchKeywordsTextField.addActionListener(this);
            searchOwnerTextField.addActionListener(this);
            searchButton.addActionListener(this);
            clearButton.addActionListener(this);
            toggleModeButtonToSimple.addActionListener(this);
        }

        public SearchParameters getSearchParameters() {
            SearchParameters sp = new SearchParameters(false);
            sp.setExtensions(searchComboBox.getSelectedKey());
            sp.setNameString(searchNameTextField.getText());
            sp.setCommentString(searchCommentTextField.getText());
            sp.setKeywordString(searchKeywordsTextField.getText());
            sp.setOwnerString(searchOwnerTextField.getText());
            sp.setWithKeyOnly(withKeyOnlyCheckBox.isSelected());
            return sp;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == searchButton 
                    || e.getSource() == searchNameTextField
                    || e.getSource() == searchCommentTextField
                    || e.getSource() == searchKeywordsTextField
                    || e.getSource() == searchOwnerTextField)
            {
                startNewSearch(getSearchParameters());
            } else if (e.getSource() == toggleModeButtonToSimple) {
                toggleMode(true);
            } else if (e.getSource() == clearButton) {
                clearTextfields();
            }
        }
        
        private void clearTextfields() {
            searchNameTextField.setText("");
            searchCommentTextField.setText("");
            searchKeywordsTextField.setText("");
            searchOwnerTextField.setText("");
        }

        public void refreshLanguage() {
            searchButton.setToolTipText(language.getString("SearchPane.toolbar.tooltip.search"));
            clearButton.setToolTipText(language.getString("SearchPane.toolbar.tooltip.clear"));
            toggleModeButtonToSimple.setToolTipText(language.getString("SearchPane.toolbar.tooltip.toggleMode"));
            searchNameLabel.setText(language.getString("SearchPane.toolbar.name")+":");
            searchCommentLabel.setText(language.getString("SearchPane.toolbar.comment")+":");
            searchKeywordsLabel.setText(language.getString("SearchPane.toolbar.keywords")+":");
            searchOwnerLabel.setText(language.getString("SearchPane.toolbar.owner")+":");
            withKeyOnlyCheckBox.setText(language.getString("SearchPane.toolbar.chkKey"));
            withKeyOnlyCheckBox.setToolTipText(language.getString("SearchPane.toolbar.tooltip.chkKey"));
        }
    }

    /**
     * Own closeable tabbed pane to get notified if a tab was closed.
     * Needed to explicitely clear the tablemodel.
     */
    private class SearchCloseableTabbedPane extends CloseableTabbedPane {
        public SearchCloseableTabbedPane() {
            super();
        }
        protected void tabWasClosed(Component c) {
            if( c instanceof ProxyPanel ) {
                // explicitely clear the model after tab was closed to make life easier for the gc
                ProxyPanel pp = (ProxyPanel)c;
                pp.tabWasClosed(); // stop search thread
                pp.getModel().clear();
            }
        }
    }
    
    /**
     * Panel component that holds a SearchModel.
     * Is added to a tabbed pane.
     */
    public class ProxyPanel extends JPanel {
        SearchModel model;
        SearchThread thread = null;
        JTable table;
        public ProxyPanel(Component c, SearchModel m, JTable table) {
            model = m;
            this.table = table;
            setLayout(new BorderLayout());
            add(c, BorderLayout.CENTER);
        }
        public void setSearchThread(SearchThread t) {
            thread = t;
        }
        public void tabWasClosed() {
            if( thread != null ) {
                thread.requestStop();
            }
            tableFindAction.deinstall(table);
        }
        public SearchModel getModel() {
            return model;
        }
    }

//    public class RolloverBorderButton extends JButton {
//
//        private boolean isOver = false;
//
//        public RolloverBorderButton() {
//            super();
//
//            addMouseListener(new MouseAdapter() {
//                public void mouseEntered(MouseEvent e) {
//                    isOver = true;
//                    repaint();
//                }
//                public void mouseExited(MouseEvent e) {
//                    isOver = false;
//                    repaint();
//                }
//            });
//        }
//
//        public void paint(Graphics g) {
//            
//            super.paint(g);
//
//            if( isOver ) {
////                g.setColor(Color.blue);
//                g.drawRect(getInsets().left, getInsets().top, getWidth(), getHeight());
//            }
//        }
//    }
}
