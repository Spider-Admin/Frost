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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.common.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class SearchTable extends SortedModelTable {

    private SearchModel searchModel;
    private CloseableTabbedPane tabPane;
    private String searchText;

    private PopupMenuSearch popupMenuSearch = null;
    private Language language = Language.getInstance();
    
    private java.util.List<FrostSearchItem> searchItems = new LinkedList<FrostSearchItem>();
    
    public SearchTable(SearchModel m, CloseableTabbedPane t, String searchText) {
        super(m);
        
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
    public void searchFinished(Component tabComponent) {
        // add all chached items to model
        for( Iterator<FrostSearchItem> i = searchItems.iterator(); i.hasNext(); ) {
            FrostSearchItem fsi = i.next();
            searchModel.addSearchItem(fsi);
        }
        searchItems.clear();
        
        int myIx = tabPane.indexOfComponent(tabComponent);
        String newTitle = searchText + " ("+searchModel.getItemCount()+")";
        tabPane.setTitleAt(myIx, newTitle);
    }

    public void searchCancelled() {
        searchItems.clear();
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

        if( row > -1 && col == 8 ) {
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
            // add all items
            selectedItems = (ModelItem[]) searchModel.getItems().toArray();
        }

        searchModel.addItemsToDownloadTable(selectedItems);

        // redraw items in model
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
    implements ActionListener, LanguageListener {

        JMenuItem cancelItem = new JMenuItem();
        JMenuItem downloadAllKeysItem = new JMenuItem();
        JMenuItem downloadSelectedKeysItem = new JMenuItem();
    
        private JMenu copyToClipboardMenu = new JMenu();
        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();
    
        private JMenuItem detailsItem = new JMenuItem();
    
        public PopupMenuSearch() {
            super();
            initialize();
        }
    
        private void initialize() {
            refreshLanguage();
    
            copyToClipboardMenu.add(copyKeysAndNamesItem);
            if( FcpHandler.isFreenet05() ) {
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
                CopyToClipboard.copyKeys(getSelectedItems());
            }
            if (e.getSource() == copyKeysAndNamesItem) {
                CopyToClipboard.copyKeysAndFilenames(getSelectedItems());
            }
            if (e.getSource() == copyExtendedInfoItem) {
                CopyToClipboard.copyExtendedInfo(getSelectedItems());
            }
            if (e.getSource() == detailsItem) {
                showDetails();
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
