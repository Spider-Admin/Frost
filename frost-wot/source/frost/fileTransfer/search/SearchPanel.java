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
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

class SearchPanel extends JPanel {
    
    // FIXME: open tabs for new searches, use CloseableTabbedPane
    
    private SearchManager searchManager;

    private static Logger logger = Logger.getLogger(SearchPanel.class.getName());

    private PopupMenuSearch popupMenuSearch = null;

    private Listener listener = new Listener();

    private Language language = null;

    private JPanel searchTopPanel = new JPanel();
    private JTranslatableComboBox searchComboBox = null;
    private JButton searchButton = new JButton(new ImageIcon(getClass().getResource("/data/search.gif")));
    private JButton downloadButton = new JButton(new ImageIcon(getClass().getResource("/data/save.gif")));
    private JLabel searchResultsCountLabel = new JLabel();
    private JTextField searchTextField = new JTextField(25);

    // move into a tabbed pane
    private SortedModelTable modelTable;
    private SearchModel model = null;

    private boolean initialized = false;

    private long searchResultsCount = 0;

    public SearchPanel(SearchManager searchManager) {
        super();
        this.searchManager = searchManager;

        language = Language.getInstance();
        language.addLanguageListener(listener);
    }

    public void initialize() {
        if (!initialized) {
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
            toolkit.configureButton(downloadButton, "/data/save_rollover.gif");
            downloadButton.setEnabled(false);
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
            searchTopPanel.add(Box.createRigidArea(blankSpace));
            searchTopPanel.add(downloadButton);
            searchTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
            searchTopPanel.add(Box.createHorizontalGlue());
            searchTopPanel.add(searchResultsCountLabel);

            // create the main search panel
            SearchTableFormat tableFormat = new SearchTableFormat();

            modelTable = new SortedModelTable(model, tableFormat);
            setLayout(new BorderLayout());
            add(searchTopPanel, BorderLayout.NORTH);
            add(modelTable.getScrollPane(), BorderLayout.CENTER);
            fontChanged();

            // listeners
            searchTextField.addActionListener(listener);
            downloadButton.addActionListener(listener);
            searchButton.addActionListener(listener);
            modelTable.getScrollPane().addMouseListener(listener);
            modelTable.getTable().addMouseListener(listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);
            modelTable.getTable().getSelectionModel().addListSelectionListener(listener);
            model.addModelListener(listener);

            initialized = true;
        }
    }

    private void refreshLanguage() {
        searchButton.setToolTipText(language.getString("SearchPane.toolbar.tooltip.search"));
        downloadButton.setToolTipText(language.getString("SearchPane.toolbar.tooltip.downloadSelectedKeys"));

        String results = language.getString("SearchPane.toolbar.results");
        Dimension labelSize = calculateLabelSize(results + " : 00000");
        searchResultsCountLabel.setPreferredSize(labelSize);
        searchResultsCountLabel.setMinimumSize(labelSize);
        searchResultsCountLabel.setText(results + " : " + searchResultsCount);
    }

    private Dimension calculateLabelSize(String text) {
        JLabel dummyLabel = new JLabel(text);
        dummyLabel.doLayout();
        return dummyLabel.getPreferredSize();
    }

    /**
     * searchButton Action Listener (Search)
     */
    private void searchButton_actionPerformed(ActionEvent e) {
        searchButton.setEnabled(false);
        model.clear();

        SearchThread searchThread =
            new SearchThread(
                searchTextField.getText(),
                searchComboBox.getSelectedKey(),
                searchManager);

        searchThread.start();
    }

    /**
     * searchTextField Action Listener (search)
     * @param e
     */
    private void searchTextField_actionPerformed(ActionEvent e) {
        if (searchButton.isEnabled()) {
            searchButton_actionPerformed(e);
        }
    }

    private void downloadButton_actionPerformed(ActionEvent e) {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        model.addItemsToDownloadModel(selectedItems);
    }

    private void showSearchTablePopupMenu(MouseEvent e) {
        getPopupMenuSearch().show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Updates the search result count.
     * Called by search thread.
     */
    private void updateSearchResultCountLabel() {
        searchResultsCount = model.getItemCount();
        String results = language.getString("SearchPane.toolbar.results");
        if (searchResultsCount == 0) {
            searchResultsCountLabel.setText(results + " : 0");
        }
        searchResultsCountLabel.setText(results + " : " + searchResultsCount);
    }

    private void fontChanged() {
        String fontName = Core.frostSettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
        int fontStyle = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
        int fontSize = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
            logger.severe("The selected font was not found in your system\n" +
                           "That selection will be changed to \"SansSerif\".");
            Core.frostSettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
            font = new Font("SansSerif", fontStyle, fontSize);
        }
        modelTable.setFont(font);
    }

    private void searchTableDoubleClick(MouseEvent e) {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        model.addItemsToDownloadModel(selectedItems);
    }

    public void setSearchEnabled(boolean b) {
        searchButton.setEnabled(b);
    }

    public void setModel(SearchModel newModel) {
        model = newModel;
    }

    private PopupMenuSearch getPopupMenuSearch() {
        if (popupMenuSearch == null) {
            popupMenuSearch = new PopupMenuSearch();
            language.addLanguageListener(popupMenuSearch);
        }
        return popupMenuSearch;
    }

    private void listSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (modelTable.getSelectedCount() > 0) {
                downloadButton.setEnabled(true);
            } else {
                downloadButton.setEnabled(false);
            }
        }
    }
    
    private class PopupMenuSearch
        extends JSkinnablePopupMenu
        implements ActionListener, LanguageListener, ClipboardOwner {
    
        JMenuItem cancelItem = new JMenuItem();
        JMenuItem downloadAllKeysItem = new JMenuItem();
        JMenuItem downloadSelectedKeysItem = new JMenuItem();
    
        private JMenu copyToClipboardMenu = new JMenu();
        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();

        private JMenuItem detailsItem = new JMenuItem();

        private String keyNotAvailableMessage;
        private String fileMessage;
        private String keyMessage;
        private String bytesMessage;
    
        private Clipboard clipboard;
    
        public PopupMenuSearch() {
            super();
            initialize();
        }
    
        private void initialize() {
            refreshLanguage();
    
            copyToClipboardMenu.add(copyKeysAndNamesItem);
            copyToClipboardMenu.add(copyKeysItem);
            copyToClipboardMenu.add(copyExtendedInfoItem);
    
            downloadSelectedKeysItem.addActionListener(this);
            downloadAllKeysItem.addActionListener(this);
    
            copyKeysAndNamesItem.addActionListener(this);
            copyKeysItem.addActionListener(this);
            copyExtendedInfoItem.addActionListener(this);
            
            detailsItem.addActionListener(this);
        }
    
        private void refreshLanguage() {
            downloadSelectedKeysItem.setText(language.getString("SearchPane.resultTable.popupmenu.downloadSelectedKeys"));
            downloadAllKeysItem.setText(language.getString("SearchPane.resultTable.popupmenu.downloadAllKeys"));
            cancelItem.setText(language.getString("Common.cancel"));
    
            keyNotAvailableMessage = language.getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
            fileMessage = language.getString("Common.copyToClipBoard.extendedInfo.file")+" ";
            keyMessage = language.getString("Common.copyToClipBoard.extendedInfo.key")+" ";
            bytesMessage = language.getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";
    
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");
            
            detailsItem.setText(language.getString("Common.details"));
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == downloadSelectedKeysItem) {
                downloadSelectedKeys();
            }
            if (e.getSource() == downloadAllKeysItem) {
                downloadAllKeys();
            }
            if (e.getSource() == copyKeysItem) {
                copyKeys();
            }
            if (e.getSource() == copyKeysAndNamesItem) {
                copyKeysAndNames();
            }
            if (e.getSource() == copyExtendedInfoItem) {
                copyExtendedInfo();
            }
            if (e.getSource() == detailsItem) {
                showDetails();
            }
        }
        
        private void showDetails() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length != 1) {
                return;
            }
            FrostSearchItem item = (FrostSearchItem) selectedItems[0];
            new FileListFileDetailsDialog(MainFrame.getInstance()).startDialog(item.getFrostFileListFileObject());
        }
    
        /**
         * This method copies the CHK keys and file names of the selected items (if any) to
         * the clipboard.
         */
        private void copyKeysAndNames() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length > 0) {
                StringBuffer textToCopy = new StringBuffer();
                for (int i = 0; i < selectedItems.length; i++) {
                    FrostSearchItem item = (FrostSearchItem) selectedItems[i];
                    String key = item.getKey();
                    if (key == null) {
                        key = keyNotAvailableMessage;
                    }
                    textToCopy.append(key);
                    textToCopy.append("/");
                    textToCopy.append(item.getFilename());
                    textToCopy.append("\n");
                }
                StringSelection selection = new StringSelection(textToCopy.toString());
                getClipboard().setContents(selection, this);
            }
        }
    
        private Clipboard getClipboard() {
            if (clipboard == null) {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
            return clipboard;
        }
    
        public void lostOwnership(Clipboard tclipboard, Transferable contents) {
        }
    
        /**
         * This method copies extended information about the selected items (if any) to
         * the clipboard. That information is composed of the filename, the key and
         * the size in bytes.
         */
        private void copyExtendedInfo() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length > 0) {
                StringBuffer textToCopy = new StringBuffer();
                for (int i = 0; i < selectedItems.length; i++) {
                    FrostSearchItem item = (FrostSearchItem) selectedItems[i];
                    String key = item.getKey();
                    if (key == null) {
                        key = keyNotAvailableMessage;
                    }
                    textToCopy.append(fileMessage);
                    textToCopy.append(item.getFilename() + "\n");
                    textToCopy.append(keyMessage);
                    textToCopy.append(key + "\n");
                    textToCopy.append(bytesMessage);
                    textToCopy.append(item.getSize() + "\n\n");
                }
                //We remove the additional \n at the end
                String result = textToCopy.substring(0, textToCopy.length() - 1);
    
                StringSelection selection = new StringSelection(result);
                getClipboard().setContents(selection, this);
            }
        }
    
        /**
         * This method copies the CHK keys of the selected items (if any) to
         * the clipboard.
         */
        private void copyKeys() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length > 0) {
                StringBuffer textToCopy = new StringBuffer();
                for (int i = 0; i < selectedItems.length; i++) {
                    FrostSearchItem item = (FrostSearchItem) selectedItems[i];
                    String key = item.getKey();
                    if (key == null) {
                        key = keyNotAvailableMessage;
                    }
                    textToCopy.append(key);
                    textToCopy.append("\n");
                }
                StringSelection selection = new StringSelection(textToCopy.toString());
                getClipboard().setContents(selection, this);
            }
        }
    
        private void downloadAllKeys() {
            model.addAllItemsToDownloadModel();
        }
    
        private void downloadSelectedKeys() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.addItemsToDownloadModel(selectedItems);
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    
        public void show(Component invoker, int x, int y) {
            removeAll();
    
            ModelItem[] selectedItems = modelTable.getSelectedItems();
    
            if (selectedItems.length > 0) {
                add(copyToClipboardMenu);
                addSeparator();
            }
    
            if (selectedItems.length != 0) {
                // If at least 1 item is selected
                add(downloadSelectedKeysItem);
                addSeparator();
            }
            add(downloadAllKeysItem);

            if (selectedItems.length == 1) {
                addSeparator();
                add(detailsItem);
            }
    
            super.show(invoker, x, y);
        }
    }

    private class Listener
        extends MouseAdapter
        implements
            ActionListener,
            MouseListener,
            LanguageListener,
            PropertyChangeListener,
            ListSelectionListener,
            ModelListener {
    
        public Listener() {
            super();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == searchTextField) {
                searchTextField_actionPerformed(e);
            }
            if (e.getSource() == downloadButton) {
                downloadButton_actionPerformed(e);
            }
            if (e.getSource() == searchButton) {
                searchButton_actionPerformed(e);
            }
        }
    
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (e.getSource() == modelTable.getTable()) {
                    searchTableDoubleClick(e);
                }
            } else if (e.isPopupTrigger()) {
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showSearchTablePopupMenu(e);
                }
            }
        }
    
        public void mouseReleased(MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
    
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showSearchTablePopupMenu(e);
                }
            }
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_NAME)) {
                fontChanged();
            }
            if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_SIZE)) {
                fontChanged();
            }
            if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_STYLE)) {
                fontChanged();
            }
        }
    
        public void valueChanged(ListSelectionEvent e) {
            listSelectionChanged(e);
        }
    
        public void itemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {
        }
    
        public void itemChanged(ModelItem item) {
        }
    
        public void itemAdded(ModelItem item) {
            updateSearchResultCountLabel();
        }
    
        public void modelCleared() {
            updateSearchResultCountLabel();
        }
    
        public void itemsRemoved(ModelItem[] items) {
            updateSearchResultCountLabel();
        }
    }
}
