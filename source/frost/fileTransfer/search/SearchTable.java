/*
  SearchTable.java / Frost
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
package frost.fileTransfer.search;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.common.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class SearchTable extends SortedModelTable {

    private SearchModel searchModel;
    private CloseableTabbedPane tabPane;
    private String searchText;

    private PopupMenuSearch popupMenuSearch = null;
    private Language language = Language.getInstance();
    
    private java.util.List searchItems = new LinkedList();
    
    public SearchTable(SearchModel m, SearchTableFormat f, CloseableTabbedPane t, String searchText) {
        super(m, f);
        
        searchModel = m;
        tabPane = t;
        this.searchText = searchText;
        
        setupTableFont();
        
        Listener l = new Listener();
        getTable().addMouseListener(l);
        getScrollPane().addMouseListener(l);
    }
    
    public void addSearchItem(FrostSearchItem i) {
        searchItems.add(i);
    }
    
    /**
     * Called if the searchthread finished.
     */
    public void searchFinished() {
        // add all chached items to model
        for( Iterator i = searchItems.iterator(); i.hasNext(); ) {
            FrostSearchItem fsi = (FrostSearchItem) i.next();
            searchModel.addSearchItem(fsi);
        }
        
        int myIx = tabPane.indexOfComponent(this.getScrollPane());
        String newTitle = searchText + " ("+searchModel.getItemCount()+")";
        tabPane.setTitleAt(myIx, newTitle);
    }

    private PopupMenuSearch getPopupMenuSearch() {
        if (popupMenuSearch == null) {
            popupMenuSearch = new PopupMenuSearch();
            language.addLanguageListener(popupMenuSearch);
        }
        return popupMenuSearch;
    }

    private void setupTableFont() {
        String fontName = Core.frostSettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
        int fontStyle = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
        int fontSize = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
            Core.frostSettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
            font = new Font("SansSerif", fontStyle, fontSize);
        }
        getTable().setFont(font);
    }

    private void searchTableDoubleClick(MouseEvent e) {
        // if double click was on the sourceCount cell then maybe show details
        int row = getTable().rowAtPoint(e.getPoint());
        int col = getTable().columnAtPoint(e.getPoint());

        if( row > -1 && col == 7 ) {
            showDetails();
            return;
        }
        addItemsToDownloadTable( getSelectedItems() );
    }

    private void showDetails() {
        ModelItem[] selectedItems = getSelectedItems();
        if (selectedItems.length != 1) {
            return;
        }
        FrostSearchItem item = (FrostSearchItem) selectedItems[0];
        new FileListFileDetailsDialog(MainFrame.getInstance(), true).startDialog(item.getFrostFileListFileObject());
    }
    
    /**
     * Add selected items, or all item if called with null, to the download table.
     * Updates state of item in search table.
     */
    private void addItemsToDownloadTable(ModelItem[] selectedItems) {
        if( selectedItems == null ) {
            // add all
            searchModel.addAllItemsToDownloadModel();
        } else {
            // add selected
            searchModel.addItemsToDownloadModel(selectedItems);
        }

        // update items in model
        for(int x=0; x < selectedItems.length; x++) {
            FrostSearchItem si = (FrostSearchItem) selectedItems[x];
            int i = model.indexOf(si);
            fireTableRowsUpdated(i,i);
        }
    }

    private class Listener extends MouseAdapter implements MouseListener {

        public Listener() {
            super();
        }
    
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (e.getSource() == getTable()) {
                    searchTableDoubleClick(e);
                }
            } else if (e.isPopupTrigger()) {
                if ((e.getSource() == getTable())
                    || (e.getSource() == getScrollPane())) {
                    showSearchTablePopupMenu(e);
                }
            }
        }
    
        public void mouseReleased(MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
    
                if ((e.getSource() == getTable())
                    || (e.getSource() == getScrollPane())) {
                    showSearchTablePopupMenu(e);
                }
            }
        }
    
        private void showSearchTablePopupMenu(MouseEvent e) {
            // select row where rightclick occurred if row under mouse is NOT selected 
            Point p = e.getPoint();
            int y = getTable().rowAtPoint(p);
            if( y < 0 ) {
                return;
            }
            if( !getTable().getSelectionModel().isSelectedIndex(y) ) {
                getTable().getSelectionModel().setSelectionInterval(y, y);
            }
            getPopupMenuSearch().show(e.getComponent(), e.getX(), e.getY());
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
            if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05) {
                copyToClipboardMenu.add(copyKeysItem);
            }
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
        
        /**
         * This method copies the CHK keys and file names of the selected items (if any) to
         * the clipboard.
         */
        private void copyKeysAndNames() {
            ModelItem[] selectedItems = getSelectedItems();
            if (selectedItems.length > 0) {
                StringBuffer textToCopy = new StringBuffer();
                for (int i = 0; i < selectedItems.length; i++) {
                    FrostSearchItem item = (FrostSearchItem) selectedItems[i];
                    String key = item.getKey();
                    if (key == null) {
                        key = keyNotAvailableMessage;
                    }
                    textToCopy.append(key);
                    if( key.indexOf('/') < 0 ) {
                        textToCopy.append("/");
                        textToCopy.append(item.getFilename());
                    }
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
            ModelItem[] selectedItems = getSelectedItems();
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
            ModelItem[] selectedItems = getSelectedItems();
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
            addItemsToDownloadTable( null );
        }
    
        private void downloadSelectedKeys() {
            addItemsToDownloadTable( getSelectedItems() );
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    
        public void show(Component invoker, int x, int y) {
            removeAll();
    
            ModelItem[] selectedItems = getSelectedItems();
    
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
}
