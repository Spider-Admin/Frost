/*
 SearchItemPropertiesDialog.java / Frost
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
package frost.fileTransfer.common;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.fileTransfer.search.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class FileListFileDetailsDialog extends JDialog {

    Language language = Language.getInstance();
    
    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bclose = null;

    private SortedModelTable modelTable = null;
    private FileListFileDetailsTableModel model = null;
    private FileListFileDetailsTableFormat tableFormat = null;
    
    private PopupMenu popupMenu = null;
    private Listener listener = new Listener();
    
    private boolean isOwnerSearchAllowed = false;

    public FileListFileDetailsDialog(Frame owner) {
        super(owner);
        initialize(owner);
    }

    public FileListFileDetailsDialog(Frame owner, boolean allowOwnerSearch) {
        super(owner);
        initialize(owner);
        isOwnerSearchAllowed = allowOwnerSearch;
    }

    /**
     * This method initializes this
     */
    private void initialize(Frame owner) {
        this.setContentPane(getJContentPane());
        this.setTitle(language.getString("FileListFileDetailsDialog.title"));
        loadLayout();
        setLocationRelativeTo(owner);
    }
    
    private PopupMenu getPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new PopupMenu();
        }
        return popupMenu;
    }
    
    private void showUploadTablePopupMenu(MouseEvent e) {
        // select row where rightclick occurred if row under mouse is NOT selected 
        Point p = e.getPoint();
        int y = modelTable.getTable().rowAtPoint(p);
        if( y < 0 ) {
            return;
        }
        if( !modelTable.getTable().getSelectionModel().isSelectedIndex(y) ) {
            modelTable.getTable().getSelectionModel().setSelectionInterval(y, y);
        }
        getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
    }

    private void loadLayout() {
        
        int lastHeight = Core.frostSettings.getIntValue("FileListFileDetailsDialog.height");
        int lastWidth = Core.frostSettings.getIntValue("FileListFileDetailsDialog.width");
        
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (lastWidth < 100) {
            lastWidth = 600;
        }
        if (lastWidth > scrSize.width) {
            lastWidth = scrSize.width;
        }

        if (lastHeight < 100) {
            lastHeight = 370;
        }
        if (lastHeight > scrSize.height) {
            lastWidth = scrSize.height;
        }
        setSize(lastWidth, lastHeight);
    }

    private void saveLayout() {
        // dialog size
        Rectangle bounds = getBounds();
        Core.frostSettings.setValue("FileListFileDetailsDialog.height", bounds.height);
        Core.frostSettings.setValue("FileListFileDetailsDialog.width", bounds.width);
        
        tableFormat.saveTableLayout(getModelTable());
    }
    
    /**
     * This method initializes jContentPane
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBclose(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes mainPanel	
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add( getModelTable().getScrollPane(), BorderLayout.CENTER);
        }
        return mainPanel;
    }
    
    private SortedModelTable getModelTable() {
        if( modelTable == null ) {
            tableFormat = new FileListFileDetailsTableFormat();
            model = new FileListFileDetailsTableModel(tableFormat);
            modelTable = new SortedModelTable(model);
            
            modelTable.getScrollPane().addMouseListener(listener);
            modelTable.getTable().addMouseListener(listener);
        }
        return modelTable;
    }
    
    /**
     * This method initializes Bok	
     */
    private JButton getBclose() {
        if( Bclose == null ) {
            Bclose = new JButton();
            Bclose.setText(language.getString("FileListFileDetailsDialog.button.close"));
            Bclose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    saveLayout();
                    setVisible(false);
                }
            });
        }
        return Bclose;
    }
    
    public void startDialog(FrostFileListFileObject fileObject) {
        List<FrostFileListFileObjectOwner> lst = fileObject.getFrostFileListFileObjectOwnerList();
        for( Iterator<FrostFileListFileObjectOwner> i = lst.iterator(); i.hasNext(); ) {
            FrostFileListFileObjectOwner o = i.next();
            FileListFileDetailsItem item = new FileListFileDetailsItem(o); 
            model.addPropertiesItem(item);
        }
        setVisible(true);
    }
    
    private class PopupMenu extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();

        private JMenu copyToClipboardMenu = new JMenu();

        private JMenuItem showOwnerFilesItem = new JMenuItem();

        public PopupMenu() {
            super();
            initialize();
        }

        private void initialize() {
            refreshLanguage();

            copyToClipboardMenu.add(copyKeysAndNamesItem);
            if( FcpHandler.isFreenet05() ) {
                copyToClipboardMenu.add(copyKeysItem);
            }

            copyKeysAndNamesItem.addActionListener(this);
            copyKeysItem.addActionListener(this);
            showOwnerFilesItem.addActionListener(this);
        }

        private void refreshLanguage() {
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));

            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");
            
            showOwnerFilesItem.setText(language.getString("Search files of owner"));
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == copyKeysItem) {
                CopyToClipboard.copyKeys(modelTable.getSelectedItems());
            } else if (e.getSource() == copyKeysAndNamesItem) {
                CopyToClipboard.copyKeysAndFilenames(modelTable.getSelectedItems());
            } else if (e.getSource() == showOwnerFilesItem) {
                searchFilesOfOwner();
            }
        }
        
        private void searchFilesOfOwner() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length == 1) {
                FileListFileDetailsItem item = (FileListFileDetailsItem) selectedItems[0];
                String owner = item.getOwnerIdentity().getUniqueName();
                
                SearchParameters sp = new SearchParameters(false);
                sp.setOwnerString(owner);
                FileTransferManager.inst().getSearchManager().getPanel().startNewSearch(sp);
            }
        }

        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }

        public void show(Component invoker, int x, int y) {
            removeAll();

            ModelItem[] selectedItems = modelTable.getSelectedItems();
            
            if( selectedItems.length == 0 ) {
                return;
            }
            
            // if at least 1 item is selected
            add(copyToClipboardMenu);
            if( isOwnerSearchAllowed && selectedItems.length == 1 ) {
                addSeparator();
                add(showOwnerFilesItem);
            }

            super.show(invoker, x, y);
        }
    }
    
    private class Listener extends MouseAdapter implements MouseListener {
        public Listener() {
            super();
        }
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showUploadTablePopupMenu(e);
                }
        }
        public void mouseReleased(MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showUploadTablePopupMenu(e);
                }
            }
        }
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
