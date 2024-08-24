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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import frost.Core;
import frost.MainFrame;
import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.FrostFileListFileObject;
import frost.fileTransfer.FrostFileListFileObjectOwner;
import frost.fileTransfer.search.SearchParameters;
import frost.identities.Identity;
import frost.identities.LocalIdentity;
import frost.messaging.frost.gui.MessagePanel.IdentityState;
import frost.util.ClipboardUtil;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.SelectRowOnRightClick;
import frost.util.gui.translation.Language;
import frost.util.gui.translation.LanguageEvent;
import frost.util.gui.translation.LanguageListener;
import frost.util.model.SortedModelTable;

@SuppressWarnings("serial")
public class FileListFileDetailsDialog extends JDialog {

    Language language = Language.getInstance();

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bclose = null;

    private SortedModelTable<FileListFileDetailsItem> modelTable = null;
    private FileListFileDetailsTableModel model = null;
    private FileListFileDetailsTableFormat tableFormat = null;

    private PopupMenu popupMenu = null;
    private final Listener listener = new Listener();

    private final boolean isOwnerSearchAllowed;

    public FileListFileDetailsDialog(final Frame owner) {
        this(owner, false);
    }

    public FileListFileDetailsDialog(final Frame owner, final boolean allowOwnerSearch) {
        super(owner);
        initialize(owner);
        isOwnerSearchAllowed = allowOwnerSearch;
    }

    /**
     * This method initializes this
     */
    private void initialize(final Frame owner) {
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

    private void showUploadTablePopupMenu(final MouseEvent e) {
        getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
    }

    private void loadLayout() {

        int lastHeight = Core.frostSettings.getIntValue("FileListFileDetailsDialog.height");
        int lastWidth = Core.frostSettings.getIntValue("FileListFileDetailsDialog.width");

        final Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

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
        final Rectangle bounds = getBounds();
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
            final FlowLayout flowLayout = new FlowLayout();
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

    private SortedModelTable<FileListFileDetailsItem> getModelTable() {
        if( modelTable == null ) {
            tableFormat = new FileListFileDetailsTableFormat();
            model = new FileListFileDetailsTableModel(tableFormat);
            modelTable = new SortedModelTable<FileListFileDetailsItem>(model);

            modelTable.getScrollPane().addMouseListener(listener);
            modelTable.getTable().addMouseListener(listener);
			modelTable.getTable().addMouseListener(new SelectRowOnRightClick(modelTable.getTable()));
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
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    saveLayout();
                    setVisible(false);
                }
            });
        }
        return Bclose;
    }

    public void startDialog(final FrostFileListFileObject fileObject) {
        for( final Iterator<FrostFileListFileObjectOwner> i = fileObject.getFrostFileListFileObjectOwnerIterator(); i.hasNext(); ) {
            final FrostFileListFileObjectOwner o = i.next();
            final FileListFileDetailsItem item = new FileListFileDetailsItem(o);
            model.addPropertiesItem(item);
        }
        setVisible(true);
    }

    private class PopupMenu extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

        private final JMenuItem copyKeysAndNamesItem = new JMenuItem();

        private final JMenu copyToClipboardMenu = new JMenu();

        private final JMenuItem showOwnerFilesItem = new JMenuItem();

        private final JMenuItem setBadItem = new JMenuItem();
        private final JMenuItem setCheckItem = new JMenuItem();
        private final JMenuItem setGoodItem = new JMenuItem();
        private final JMenuItem setObserveItem = new JMenuItem();

        public PopupMenu() {
            super();
            initialize();
        }

        private void initialize() {
            refreshLanguage();

            copyToClipboardMenu.add(copyKeysAndNamesItem);

            copyKeysAndNamesItem.addActionListener(this);
            showOwnerFilesItem.addActionListener(this);
            setGoodItem.addActionListener(this);
            setBadItem.addActionListener(this);
            setCheckItem.addActionListener(this);
            setObserveItem.addActionListener(this);
        }

        private void refreshLanguage() {
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));

            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");

            showOwnerFilesItem.setText(language.getString("FileListFileDetailsDialog.popupmenu.searchFilesOfOwner"));
            setGoodItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToGood"));
            setBadItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToBad"));
            setCheckItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToCheck"));
            setObserveItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToObserve"));
        }

        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == copyKeysAndNamesItem) {
                ClipboardUtil.copyKeysAndFilenames(modelTable.getSelectedItems().toArray());
            } else if (e.getSource() == showOwnerFilesItem) {
                searchFilesOfOwner();
            } else if (e.getSource() == setGoodItem) {
                changeTrustState(IdentityState.GOOD);
            } else if (e.getSource() == setBadItem) {
                changeTrustState(IdentityState.BAD);
            } else if (e.getSource() == setCheckItem) {
                changeTrustState(IdentityState.CHECK);
            } else if (e.getSource() == setObserveItem) {
                changeTrustState(IdentityState.OBSERVE);
            }
        }

        private void changeTrustState(final IdentityState is) {
            final List<FileListFileDetailsItem> selectedItems = modelTable.getSelectedItems();
            if (selectedItems.size() == 1) {
                final FileListFileDetailsItem item =  selectedItems.get(0);
                if( is == IdentityState.GOOD ) {
                    item.getOwnerIdentity().setGOOD();
                } else if( is == IdentityState.CHECK ) {
                    item.getOwnerIdentity().setCHECK();
                } else if( is == IdentityState.OBSERVE ) {
                    item.getOwnerIdentity().setOBSERVE();
                } else if( is == IdentityState.BAD ) {
                    item.getOwnerIdentity().setBAD();
                }
                modelTable.fireTableRowsUpdated(0, modelTable.getRowCount()-1);
                // also update message panel to reflect the identity change
                MainFrame.getInstance().getMessagePanel().updateTableAfterChangeOfIdentityState();
            }
        }

        private void searchFilesOfOwner() {
            final List<FileListFileDetailsItem> selectedItems = modelTable.getSelectedItems();
            if (selectedItems.size() == 1) {
                final FileListFileDetailsItem item = selectedItems.get(0);
                final String owner = item.getOwnerIdentity().getUniqueName();

                final SearchParameters sp = new SearchParameters(false);
                sp.setOwnerString(owner);
                FileTransferManager.inst().getSearchManager().getPanel().startNewSearch(sp);
            }
        }

        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        @Override
        public void show(final Component invoker, final int x, final int y) {
            removeAll();

            final List<FileListFileDetailsItem> selectedItems = modelTable.getSelectedItems();

            if( selectedItems.size() == 0 ) {
                return;
            }

            // if at least 1 item is selected
            add(copyToClipboardMenu);

            if (selectedItems.size() == 1) {

                addSeparator();

                add(setGoodItem);
                add(setObserveItem);
                add(setCheckItem);
                add(setBadItem);
                setGoodItem.setEnabled(false);
                setObserveItem.setEnabled(false);
                setCheckItem.setEnabled(false);
                setBadItem.setEnabled(false);

                final FileListFileDetailsItem item =  selectedItems.get(0);
                final Identity ownerId = item.getOwnerIdentity();

                if( ownerId instanceof LocalIdentity ) {
                    // keep all off
                } else if (ownerId.isGOOD()) {
                    setObserveItem.setEnabled(true);
                    setCheckItem.setEnabled(true);
                    setBadItem.setEnabled(true);
                } else if (ownerId.isCHECK()) {
                    setObserveItem.setEnabled(true);
                    setGoodItem.setEnabled(true);
                    setBadItem.setEnabled(true);
                } else if (ownerId.isBAD()) {
                    setObserveItem.setEnabled(true);
                    setGoodItem.setEnabled(true);
                    setCheckItem.setEnabled(true);
                } else if (ownerId.isOBSERVE()) {
                    setGoodItem.setEnabled(true);
                    setCheckItem.setEnabled(true);
                    setBadItem.setEnabled(true);
                } else {
                    // keep all off
                }
            }

            if( isOwnerSearchAllowed && selectedItems.size() == 1 ) {
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
        @Override
        public void mousePressed(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showUploadTablePopupMenu(e);
                }
            }
        }
        @Override
        public void mouseReleased(final MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showUploadTablePopupMenu(e);
                }
            }
        }
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
