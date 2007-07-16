/*
  UploadPanel.java / Frost
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
package frost.fileTransfer.upload;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.fileTransfer.sharing.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.search.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class UploadPanel extends JPanel {

    private PopupMenuUpload popupMenuUpload = null;

    private Listener listener = new Listener();

    private static final Logger logger = Logger.getLogger(UploadPanel.class.getName());

    private UploadModel model = null;

    private Language language = null;

    private JToolBar uploadToolBar = new JToolBar();
    private JButton uploadAddFilesButton = new JButton(new ImageIcon(getClass().getResource("/data/browse.gif")));
    private JCheckBox removeFinishedUploadsCheckBox = new JCheckBox();
    private JCheckBox showExternalGlobalQueueItems = new JCheckBox();

    private SortedModelTable modelTable;
    
    private JLabel uploadItemCountLabel = new JLabel();
    private int uploadItemCount = 0;

    private boolean initialized = false;

    public UploadPanel() {
        super();

        language = Language.getInstance();
        language.addLanguageListener(listener);
    }

    public UploadTableFormat getTableFormat() {
        return (UploadTableFormat) modelTable.getTableFormat();
    }

    public void initialize() {
        if (!initialized) {
            refreshLanguage();
            
            uploadToolBar.setRollover(true);
            uploadToolBar.setFloatable(false);
            
            removeFinishedUploadsCheckBox.setOpaque(false);
            showExternalGlobalQueueItems.setOpaque(false);

            // create the top panel
            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(uploadAddFilesButton, "/data/browse_rollover.gif");
            uploadToolBar.add(uploadAddFilesButton);
            uploadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
            uploadToolBar.add(removeFinishedUploadsCheckBox);
            if( PersistenceManager.isPersistenceEnabled() ) {
                uploadToolBar.add(showExternalGlobalQueueItems);
            }
            uploadToolBar.add(Box.createRigidArea(new Dimension(80, 0)));
            uploadToolBar.add(Box.createHorizontalGlue());
            uploadToolBar.add(uploadItemCountLabel);
            
            // create the main upload panel
            modelTable = new SortedModelTable(model);
            new TableFindAction().install(modelTable.getTable());
            setLayout(new BorderLayout());
            add(uploadToolBar, BorderLayout.NORTH);
            add(modelTable.getScrollPane(), BorderLayout.CENTER);
            fontChanged();

            // listeners
            uploadAddFilesButton.addActionListener(listener);
            modelTable.getScrollPane().addMouseListener(listener);
            modelTable.getTable().addKeyListener(listener);
            modelTable.getTable().addMouseListener(listener);
            removeFinishedUploadsCheckBox.addItemListener(listener);
            showExternalGlobalQueueItems.addItemListener(listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);
            
            removeFinishedUploadsCheckBox.setSelected(Core.frostSettings.getBoolValue(SettingsClass.UPLOAD_REMOVE_FINISHED));
            showExternalGlobalQueueItems.setSelected(Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD));

            initialized = true;
        }
    }

    private Dimension calculateLabelSize(String text) {
        JLabel dummyLabel = new JLabel(text);
        dummyLabel.doLayout();
        return dummyLabel.getPreferredSize();
    }

    private void refreshLanguage() {
        uploadAddFilesButton.setToolTipText(language.getString("UploadPane.toolbar.tooltip.browse") + "...");
        
        String waiting = language.getString("UploadPane.toolbar.waiting");
        Dimension labelSize = calculateLabelSize(waiting + ": 00000");
        uploadItemCountLabel.setPreferredSize(labelSize);
        uploadItemCountLabel.setMinimumSize(labelSize);
        uploadItemCountLabel.setText(waiting + ": " + uploadItemCount);
        removeFinishedUploadsCheckBox.setText(language.getString("UploadPane.removeFinishedUploads"));
        showExternalGlobalQueueItems.setText(language.getString("UploadPane.showExternalGlobalQueueItems"));
    }

    private PopupMenuUpload getPopupMenuUpload() {
        if (popupMenuUpload == null) {
            popupMenuUpload = new PopupMenuUpload();
            language.addLanguageListener(popupMenuUpload);
        }
        return popupMenuUpload;
    }

    private void uploadTable_keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
            removeSelectedFiles();
        }
    }

    /**
     * Remove selected files
     */
    private void removeSelectedFiles() {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        
        final List<String> externalRequestsToRemove = new LinkedList<String>();
        final List<ModelItem> requestsToRemove = new LinkedList<ModelItem>();
        for( ModelItem mi : selectedItems ) {
            FrostUploadItem i = (FrostUploadItem)mi;
            requestsToRemove.add(mi);
            if( i.isExternal() ) {
                externalRequestsToRemove.add(i.getGqIdentifier());
            }
        }

        ModelItem[] ri = (ModelItem[]) requestsToRemove.toArray(new ModelItem[requestsToRemove.size()]);
        model.removeItems(ri);

        modelTable.getTable().clearSelection();

        if( FileTransferManager.inst().getPersistenceManager() != null && externalRequestsToRemove.size() > 0 ) {
            new Thread() {
                public void run() {
                    FileTransferManager.inst().getPersistenceManager().removeRequests(externalRequestsToRemove);
                }
            }.start();
        }
    }

    public void uploadAddFilesButton_actionPerformed(ActionEvent e) {

        final JFileChooser fc = new JFileChooser(Core.frostSettings.getValue(SettingsClass.DIR_LAST_USED));
        fc.setDialogTitle(language.getString("UploadPane.filechooser.title"));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setPreferredSize(new Dimension(600, 400));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File[] selectedFiles = fc.getSelectedFiles();
        if( selectedFiles == null ) {
            return;
        }
        String parentDir = null;
        List<File> uploadFileItems = new LinkedList<File>();
        for (int i = 0; i < selectedFiles.length; i++) {
            // collect all choosed files + files in all choosed directories
            ArrayList<File> allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
            for (File newFile : allFiles) {
                if (newFile.isFile() && newFile.length() > 0) {
                    uploadFileItems.add(newFile);
                    if( parentDir == null ) {
                        parentDir = newFile.getParent(); // remember last upload dir
                    }
                }
            }
        }
        if( parentDir != null ) {
            Core.frostSettings.setValue(SettingsClass.DIR_LAST_USED, parentDir);
        }

        for(File file : uploadFileItems ) {
            FrostUploadItem ulItem = new FrostUploadItem(file);
            model.addNewUploadItem(ulItem);
        }
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
        getPopupMenuUpload().show(e.getComponent(), e.getX(), e.getY());
    }

    private void uploadTableDoubleClick(MouseEvent e) {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        if (selectedItems.length != 0) {
            FrostUploadItem ulItem = (FrostUploadItem) selectedItems[0];
            if( !ulItem.isSharedFile() ) {
                return;
            }
            // jump to associated shared file in shared files table
            FrostSharedFileItem sfi = ulItem.getSharedFileItem();
            FileTransferManager.inst().getSharedFilesManager().selectTab();
            FileTransferManager.inst().getSharedFilesManager().selectModelItem(sfi);
        }
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

    public void setModel(UploadModel model) {
        this.model = model;
    }
    
    public void setUploadItemCount(int newUploadItemCount) {
        uploadItemCount = newUploadItemCount;

        String s =
            new StringBuilder()
                .append(language.getString("UploadPane.toolbar.waiting"))
                .append(": ")
                .append(uploadItemCount)
                .toString();
        uploadItemCountLabel.setText(s);
    }
    
    private class PopupMenuUpload extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();
        private JMenuItem generateChkForSelectedFilesItem = new JMenuItem();
        private JMenuItem uploadSelectedFilesItem = new JMenuItem();
        private JMenuItem removeSelectedFilesItem = new JMenuItem();
        private JMenuItem showSharedFileItem = new JMenuItem();
        private JMenuItem startSelectedUploadsNow = new JMenuItem();
        private JMenu copyToClipboardMenu = new JMenu();
        
        private JMenuItem disableAllDownloadsItem = new JMenuItem();
        private JMenuItem disableSelectedDownloadsItem = new JMenuItem();
        private JMenuItem enableAllDownloadsItem = new JMenuItem();
        private JMenuItem enableSelectedDownloadsItem = new JMenuItem();
        private JMenuItem invertEnabledAllItem = new JMenuItem();
        private JMenuItem invertEnabledSelectedItem = new JMenuItem();

        private JMenu changePriorityMenu = null;
        private JMenuItem prio0Item = null;
        private JMenuItem prio1Item = null;
        private JMenuItem prio2Item = null;
        private JMenuItem prio3Item = null;
        private JMenuItem prio4Item = null;
        private JMenuItem prio5Item = null;
        private JMenuItem prio6Item = null;

        public PopupMenuUpload() {
            super();
            initialize();
        }

        private void initialize() {
            
            if( PersistenceManager.isPersistenceEnabled() ) {
                changePriorityMenu = new JMenu();
                prio0Item = new JMenuItem();
                prio1Item = new JMenuItem();
                prio2Item = new JMenuItem();
                prio3Item = new JMenuItem();
                prio4Item = new JMenuItem();
                prio5Item = new JMenuItem();
                prio6Item = new JMenuItem();
                
                changePriorityMenu.add(prio0Item);
                changePriorityMenu.add(prio1Item);
                changePriorityMenu.add(prio2Item);
                changePriorityMenu.add(prio3Item);
                changePriorityMenu.add(prio4Item);
                changePriorityMenu.add(prio5Item);
                changePriorityMenu.add(prio6Item);
                
                prio0Item.addActionListener(this);
                prio1Item.addActionListener(this);
                prio2Item.addActionListener(this);
                prio3Item.addActionListener(this);
                prio4Item.addActionListener(this);
                prio5Item.addActionListener(this);
                prio6Item.addActionListener(this);
            }
            
            refreshLanguage();

            copyToClipboardMenu.add(copyKeysAndNamesItem);
            if( FcpHandler.isFreenet05() ) {
                copyToClipboardMenu.add(copyKeysItem);
            }
            copyToClipboardMenu.add(copyExtendedInfoItem);

            copyKeysAndNamesItem.addActionListener(this);
            copyKeysItem.addActionListener(this);
            copyExtendedInfoItem.addActionListener(this);
            removeSelectedFilesItem.addActionListener(this);
            uploadSelectedFilesItem.addActionListener(this);
            startSelectedUploadsNow.addActionListener(this);
            generateChkForSelectedFilesItem.addActionListener(this);
            showSharedFileItem.addActionListener(this);
            
            enableAllDownloadsItem.addActionListener(this);
            disableAllDownloadsItem.addActionListener(this);
            enableSelectedDownloadsItem.addActionListener(this);
            disableSelectedDownloadsItem.addActionListener(this);
            invertEnabledAllItem.addActionListener(this);
            invertEnabledSelectedItem.addActionListener(this);
        }

        private void refreshLanguage() {
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            generateChkForSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.startEncodingOfSelectedFiles"));
            uploadSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.uploadSelectedFiles"));
            startSelectedUploadsNow.setText(language.getString("UploadPane.fileTable.popupmenu.startSelectedUploadsNow"));
            removeSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.remove.removeSelectedFiles"));
            showSharedFileItem.setText(language.getString("UploadPane.fileTable.popupmenu.showSharedFile"));
            
            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");

            enableAllDownloadsItem.setText(language.getString("UploadPane.fileTable.popupmenu.enableUploads.enableAllUploads"));
            disableAllDownloadsItem.setText(language.getString("UploadPane.fileTable.popupmenu.enableUploads.disableAllUploads"));
            enableSelectedDownloadsItem.setText(language.getString("UploadPane.fileTable.popupmenu.enableUploads.enableSelectedUploads"));
            disableSelectedDownloadsItem.setText(language.getString("UploadPane.fileTable.popupmenu.enableUploads.disableSelectedUploads"));
            invertEnabledAllItem.setText(language.getString("UploadPane.fileTable.popupmenu.enableUploads.invertEnabledStateForAllUploads"));
            invertEnabledSelectedItem.setText(language.getString("UploadPane.fileTable.popupmenu.enableUploads.invertEnabledStateForSelectedUploads"));

            if( PersistenceManager.isPersistenceEnabled() ) {
                changePriorityMenu.setText(language.getString("Common.priority.changePriority"));
                prio0Item.setText(language.getString("Common.priority.priority0"));
                prio1Item.setText(language.getString("Common.priority.priority1"));
                prio2Item.setText(language.getString("Common.priority.priority2"));
                prio3Item.setText(language.getString("Common.priority.priority3"));
                prio4Item.setText(language.getString("Common.priority.priority4"));
                prio5Item.setText(language.getString("Common.priority.priority5"));
                prio6Item.setText(language.getString("Common.priority.priority6"));
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == copyKeysItem) {
                CopyToClipboard.copyKeys(modelTable.getSelectedItems());
            } else if (e.getSource() == copyKeysAndNamesItem) {
                CopyToClipboard.copyKeysAndFilenames(modelTable.getSelectedItems());
            } else if (e.getSource() == copyExtendedInfoItem) {
                CopyToClipboard.copyExtendedInfo(modelTable.getSelectedItems());
            } else if (e.getSource() == removeSelectedFilesItem) {
                removeSelectedFiles();
            } else if (e.getSource() == uploadSelectedFilesItem) {
                uploadSelectedFiles();
            } else if (e.getSource() == generateChkForSelectedFilesItem) {
                generateChkForSelectedFiles();
            } else if (e.getSource() == showSharedFileItem) {
                uploadTableDoubleClick(null);
            } else if (e.getSource() == prio0Item) {
                changePriority(0);
            } else if (e.getSource() == prio1Item) {
                changePriority(1);
            } else if (e.getSource() == prio2Item) {
                changePriority(2);
            } else if (e.getSource() == prio3Item) {
                changePriority(3);
            } else if (e.getSource() == prio4Item) {
                changePriority(4);
            } else if (e.getSource() == prio5Item) {
                changePriority(5);
            } else if (e.getSource() == prio6Item) {
                changePriority(6);
            } else if (e.getSource() == enableAllDownloadsItem) {
                enableAllDownloads();
            } else if (e.getSource() == disableAllDownloadsItem) {
                disableAllDownloads();
            } else if (e.getSource() == enableSelectedDownloadsItem) {
                enableSelectedDownloads();
            } else if (e.getSource() == disableSelectedDownloadsItem) {
                disableSelectedDownloads();
            } else if (e.getSource() == invertEnabledAllItem) {
                invertEnabledAll();
            } else if (e.getSource() == invertEnabledSelectedItem) {
                invertEnabledSelected();
            } else if (e.getSource() == startSelectedUploadsNow ) {
                startSelectedUploadsNow();
            }
        }
        
        private void changePriority(int prio) {
            if( FileTransferManager.inst().getPersistenceManager() != null ) {
                ModelItem[] selectedItems = modelTable.getSelectedItems();
                FileTransferManager.inst().getPersistenceManager().changeItemPriorites(selectedItems, prio);
            }
        }

        /**
         * Generate CHK for selected files
         */
        private void generateChkForSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.generateChkItems(selectedItems);
        }

        private void startSelectedUploadsNow() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            
            final List<FrostUploadItem> itemsToStart = new LinkedList<FrostUploadItem>();
            for( ModelItem mi : selectedItems ) {
                FrostUploadItem i = (FrostUploadItem)mi;
                if( i.isExternal() ) {
                    continue;
                }
                if( i.getState() != FrostUploadItem.STATE_WAITING ) {
                    continue;
                }
                itemsToStart.add(i);
            }
            
            for(FrostUploadItem ulItem : itemsToStart) {
                FileTransferManager.inst().getUploadManager().startUpload(ulItem);
            }
        }

        /**
         * Reload selected files
         */
        private void uploadSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.uploadItems(selectedItems);
        }

        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }

        private void invertEnabledSelected() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(null, selectedItems);
        }
    
        private void invertEnabledAll() {
            model.setAllItemsEnabled(null);
        }
    
        private void disableSelectedDownloads() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(Boolean.FALSE, selectedItems);
        }
    
        private void enableSelectedDownloads() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(Boolean.TRUE, selectedItems);
        }
    
        private void disableAllDownloads() {
            model.setAllItemsEnabled(Boolean.FALSE);
        }
    
        private void enableAllDownloads() {
            model.setAllItemsEnabled(Boolean.TRUE);
        }

        public void show(Component invoker, int x, int y) {
            removeAll();

            ModelItem[] selectedItems = modelTable.getSelectedItems();
            
            if( selectedItems.length == 0 ) {
                return;
            }

            // if at least 1 item is selected
            add(copyToClipboardMenu);
            addSeparator();
            
            if( PersistenceManager.isPersistenceEnabled() ) {
                add(changePriorityMenu);
                addSeparator();
            }

            JMenu enabledSubMenu = new JMenu(language.getString("UploadPane.fileTable.popupmenu.enableUploads") + "...");
            enabledSubMenu.add(enableSelectedDownloadsItem);
            enabledSubMenu.add(disableSelectedDownloadsItem);
            enabledSubMenu.add(invertEnabledSelectedItem);
            enabledSubMenu.addSeparator();

            enabledSubMenu.add(enableAllDownloadsItem);
            enabledSubMenu.add(disableAllDownloadsItem);
            enabledSubMenu.add(invertEnabledAllItem);
            add(enabledSubMenu);
            
            add(startSelectedUploadsNow);
            add(generateChkForSelectedFilesItem);
            add(uploadSelectedFilesItem);
            addSeparator();
            add(removeSelectedFilesItem);
            if( selectedItems.length == 1 ) {
                FrostUploadItem item = (FrostUploadItem) selectedItems[0];
                if( item.isSharedFile() ) {
                    addSeparator();
                    add(showSharedFileItem);
                }
            }
            super.show(invoker, x, y);
        }
    }

    private class Listener extends MouseAdapter
        implements LanguageListener, KeyListener, ActionListener, MouseListener, PropertyChangeListener, ItemListener
    {
        public Listener() {
            super();
        }
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
        public void keyPressed(KeyEvent e) {
            if (e.getSource() == modelTable.getTable()) {
                uploadTable_keyPressed(e);
            }
        }
        public void keyReleased(KeyEvent e) {
            // Nothing here
        }
        public void keyTyped(KeyEvent e) {
            // Nothing here
        }
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == uploadAddFilesButton) {
                uploadAddFilesButton_actionPerformed(e);
            }
        }
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (e.getSource() == modelTable.getTable()) {
                    // Start file from download table. Is this a good idea?
                    uploadTableDoubleClick(e);
                }
            } else if (e.isPopupTrigger())
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
        public void itemStateChanged(ItemEvent e) {
            if( removeFinishedUploadsCheckBox.isSelected() ) {
                Core.frostSettings.setValue(SettingsClass.UPLOAD_REMOVE_FINISHED, true);
                model.removeFinishedUploads();
            } else {
                Core.frostSettings.setValue(SettingsClass.UPLOAD_REMOVE_FINISHED, false);
            }
            if( showExternalGlobalQueueItems.isSelected() ) {
                Core.frostSettings.setValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD, true);
            } else {
                Core.frostSettings.setValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD, false);
                model.removeExternalUploads();
            }
        }
    }
}
