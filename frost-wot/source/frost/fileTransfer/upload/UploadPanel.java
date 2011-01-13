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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import frost.Core;
import frost.MainFrame;
import frost.SettingsClass;
import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.PersistenceManager;
import frost.fileTransfer.sharing.FrostSharedFileItem;
import frost.gui.AddNewUploadsDialog;
import frost.util.CopyToClipboard;
import frost.util.FileAccess;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.MiscToolkit;
import frost.util.gui.search.TableFindAction;
import frost.util.gui.translation.Language;
import frost.util.gui.translation.LanguageEvent;
import frost.util.gui.translation.LanguageListener;
import frost.util.model.SortedModelTable;

@SuppressWarnings("serial")
public class UploadPanel extends JPanel {

    private PopupMenuUpload popupMenuUpload = null;

    private final Listener listener = new Listener();

    private static final Logger logger = Logger.getLogger(UploadPanel.class.getName());

    private UploadModel model = null;

    private Language language = null;

    private final JToolBar uploadToolBar = new JToolBar();
    private final JButton uploadAddFilesButton = new JButton(MiscToolkit.loadImageIcon("/data/toolbar/folder-open.png"));
    private final JCheckBox removeFinishedUploadsCheckBox = new JCheckBox();
    private final JCheckBox showExternalGlobalQueueItems = new JCheckBox();
    private final JCheckBox compressUploadsCheckBox = new JCheckBox();

    private SortedModelTable<FrostUploadItem> modelTable;

    private final JLabel uploadItemCountLabel = new JLabel();
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
            compressUploadsCheckBox.setOpaque(false);

            // create the top panel
            MiscToolkit.configureButton(uploadAddFilesButton);
            uploadToolBar.add(uploadAddFilesButton);
            uploadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
            uploadToolBar.add(removeFinishedUploadsCheckBox);
            if( PersistenceManager.isPersistenceEnabled() ) {
                uploadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
                uploadToolBar.add(showExternalGlobalQueueItems);
            }
            uploadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
            uploadToolBar.add(compressUploadsCheckBox);
            
            uploadToolBar.add(Box.createRigidArea(new Dimension(80, 0)));
            uploadToolBar.add(Box.createHorizontalGlue());
            uploadToolBar.add(uploadItemCountLabel);

            // create the main upload panel
            modelTable = new SortedModelTable<FrostUploadItem>(model);
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
            compressUploadsCheckBox.addItemListener(listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

            removeFinishedUploadsCheckBox.setSelected(Core.frostSettings.getBoolValue(SettingsClass.UPLOAD_REMOVE_FINISHED));
            showExternalGlobalQueueItems.setSelected(Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD));
            compressUploadsCheckBox.setSelected(Core.frostSettings.getBoolValue(SettingsClass.COMPRESS_UPLOADS));

            assignHotkeys();

            initialized = true;
        }
    }

    private Dimension calculateLabelSize(final String text) {
        final JLabel dummyLabel = new JLabel(text);
        dummyLabel.doLayout();
        return dummyLabel.getPreferredSize();
    }

    private void refreshLanguage() {
        uploadAddFilesButton.setToolTipText(language.getString("UploadPane.toolbar.tooltip.browse") + "...");

        final String waiting = language.getString("UploadPane.toolbar.waiting");
        final Dimension labelSize = calculateLabelSize(waiting + ": 00000");
        uploadItemCountLabel.setPreferredSize(labelSize);
        uploadItemCountLabel.setMinimumSize(labelSize);
        uploadItemCountLabel.setText(waiting + ": " + uploadItemCount);
        removeFinishedUploadsCheckBox.setText(language.getString("UploadPane.removeFinishedUploads"));
        showExternalGlobalQueueItems.setText(language.getString("UploadPane.showExternalGlobalQueueItems"));
        compressUploadsCheckBox.setText(language.getString("UploadPane.compressUploads"));
    }

    private PopupMenuUpload getPopupMenuUpload() {
        if (popupMenuUpload == null) {
            popupMenuUpload = new PopupMenuUpload();
            language.addLanguageListener(popupMenuUpload);
        }
        return popupMenuUpload;
    }

    private void uploadTable_keyPressed(final KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
            removeSelectedFiles();
        }
    }

    /**
     * Remove selected files
     */
    private void removeSelectedFiles() {
        final List<FrostUploadItem> selectedItems = modelTable.getSelectedItems();

        final List<String> externalRequestsToRemove = new LinkedList<String>();
        final List<FrostUploadItem> requestsToRemove = new LinkedList<FrostUploadItem>();
        for( final FrostUploadItem mi : selectedItems ) {
            requestsToRemove.add(mi);
            if( mi.isExternal() ) {
                externalRequestsToRemove.add(mi.getGqIdentifier());
            }
        }
        model.removeItems(requestsToRemove);

        modelTable.getTable().clearSelection();

        if( FileTransferManager.inst().getPersistenceManager() != null && externalRequestsToRemove.size() > 0 ) {
            new Thread() {
                @Override
                public void run() {
                    FileTransferManager.inst().getPersistenceManager().removeRequests(externalRequestsToRemove);
                }
            }.start();
        }
    }

    public void uploadAddFilesButton_actionPerformed(final ActionEvent e) {

        final JFileChooser fc = new JFileChooser(Core.frostSettings.getValue(SettingsClass.DIR_LAST_USED));
        fc.setDialogTitle(language.getString("UploadPane.filechooser.title"));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setPreferredSize(new Dimension(600, 400));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File[] selectedFiles = fc.getSelectedFiles();
        if( selectedFiles == null ) {
            return;
        }

        final List<File> uploadFileItems = new LinkedList<File>();
        for( final File element : selectedFiles ) {
            // collect all choosed files + files in all choosed directories
            uploadFileItems.addAll( FileAccess.getAllEntries(element) );
        }

        // remember last upload dir
        if (uploadFileItems.size() > 0) {
            final File file = uploadFileItems.get(0);
            Core.frostSettings.setValue(SettingsClass.DIR_LAST_USED, file.getParent());
        }
        
        List<FrostUploadItem> frostUploadItemList = new ArrayList<FrostUploadItem>();
        for(final File file : uploadFileItems ) {
        	frostUploadItemList.add( new FrostUploadItem(file));
        }
        
        final AddNewUploadsDialog addNewUploadsDialog = new AddNewUploadsDialog(MainFrame.getInstance());
    	addNewUploadsDialog.startDialog(frostUploadItemList);
    	
//        for(final File file : uploadFileItems ) {
//            model.addNewUploadItem(
//            	new FrostUploadItem(file, compressUploadsCheckBox.isSelected())
//            );
//        }
    }

    private void showUploadTablePopupMenu(final MouseEvent e) {
        // select row where rightclick occurred if row under mouse is NOT selected
        final Point p = e.getPoint();
        final int y = modelTable.getTable().rowAtPoint(p);
        if( y < 0 ) {
            return;
        }
        if( !modelTable.getTable().getSelectionModel().isSelectedIndex(y) ) {
            modelTable.getTable().getSelectionModel().setSelectionInterval(y, y);
        }
        getPopupMenuUpload().show(e.getComponent(), e.getX(), e.getY());
    }

    private void uploadTableDoubleClick(final MouseEvent e) {
        final List<FrostUploadItem> selectedItems = modelTable.getSelectedItems();
        if (selectedItems.size() == 0) {
        	return;
        }
        final FrostUploadItem ulItem = selectedItems.get(0);
        if( ! ulItem.isSharedFile() ) {
            return;
        }
        // jump to associated shared file in shared files table
        final FrostSharedFileItem sfi = ulItem.getSharedFileItem();
        FileTransferManager.inst().getSharedFilesManager().selectTab();
        FileTransferManager.inst().getSharedFilesManager().selectModelItem(sfi);
    }

    private void fontChanged() {
        final String fontName = Core.frostSettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
        final int fontStyle = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
        final int fontSize = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
            logger.severe("The selected font was not found in your system\n" +
                           "That selection will be changed to \"SansSerif\".");
            Core.frostSettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
            font = new Font("SansSerif", fontStyle, fontSize);
        }
        modelTable.setFont(font);
    }

    public void setModel(final UploadModel model) {
        this.model = model;
    }

    public void setUploadItemCount(final int newUploadItemCount) {
        uploadItemCount = newUploadItemCount;

        final String s =
            new StringBuilder()
                .append(language.getString("UploadPane.toolbar.waiting"))
                .append(": ")
                .append(uploadItemCount)
                .toString();
        uploadItemCountLabel.setText(s);
    }
    
    public void changeItemPriorites(final List<FrostUploadItem> items, final int newPrio) {
        if (items == null || items.size() == 0 || FileTransferManager.inst().getPersistenceManager() == null) {
            return;
        }
        for (final FrostUploadItem ui : items) {
            String gqid = null;
            if (ui.getState() == FrostUploadItem.STATE_PROGRESS) {
                ui.setPriority(newPrio);
                gqid = ui.getGqIdentifier();
            }
            if (gqid != null) {
            	FileTransferManager.inst().getPersistenceManager().getFcpTools().changeRequestPriority(gqid, newPrio);
            }
        }
    }

    private void assignHotkeys() {

        // assign keys 1-6 - set priority of selected items
            final Action setPriorityAction = new AbstractAction() {
                public void actionPerformed(final ActionEvent event) {
                    final int prio = new Integer(event.getActionCommand()).intValue();
                    final List<FrostUploadItem> selectedItems = modelTable.getSelectedItems();
                    changeItemPriorites(selectedItems, prio);
                }
            };
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "SETPRIO");
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "SETPRIO");
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "SETPRIO");
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "SETPRIO");
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0), "SETPRIO");
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0), "SETPRIO");
            getActionMap().put("SETPRIO", setPriorityAction);
        }

    private class PopupMenuUpload extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

        private final JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private final JMenuItem copyExtendedInfoItem = new JMenuItem();
        private final JMenuItem generateChkForSelectedFilesItem = new JMenuItem();
        private final JMenuItem uploadSelectedFilesItem = new JMenuItem();
        private final JMenuItem removeSelectedFilesItem = new JMenuItem();
        private final JMenuItem showSharedFileItem = new JMenuItem();
        private final JMenuItem startSelectedUploadsNow = new JMenuItem();

        private final JMenuItem disableAllDownloadsItem = new JMenuItem();
        private final JMenuItem disableSelectedDownloadsItem = new JMenuItem();
        private final JMenuItem enableAllDownloadsItem = new JMenuItem();
        private final JMenuItem enableSelectedDownloadsItem = new JMenuItem();
        private final JMenuItem invertEnabledAllItem = new JMenuItem();
        private final JMenuItem invertEnabledSelectedItem = new JMenuItem();

        private JMenu changePriorityMenu = null;
        private JMenuItem prio0Item = null;
        private JMenuItem prio1Item = null;
        private JMenuItem prio2Item = null;
        private JMenuItem prio3Item = null;
        private JMenuItem prio4Item = null;
        private JMenuItem prio5Item = null;
        private JMenuItem prio6Item = null;
        private JMenuItem removeFromGqItem = null;

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

                removeFromGqItem = new JMenuItem();

                prio0Item.addActionListener(this);
                prio1Item.addActionListener(this);
                prio2Item.addActionListener(this);
                prio3Item.addActionListener(this);
                prio4Item.addActionListener(this);
                prio5Item.addActionListener(this);
                prio6Item.addActionListener(this);
                removeFromGqItem.addActionListener(this);
            }

            refreshLanguage();

            copyKeysAndNamesItem.addActionListener(this);
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
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            generateChkForSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.startEncodingOfSelectedFiles"));
            uploadSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.uploadSelectedFiles"));
            startSelectedUploadsNow.setText(language.getString("UploadPane.fileTable.popupmenu.startSelectedUploadsNow"));
            removeSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.remove.removeSelectedFiles"));
            showSharedFileItem.setText(language.getString("UploadPane.fileTable.popupmenu.showSharedFile"));

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
                removeFromGqItem.setText(language.getString("UploadPane.fileTable.popupmenu.removeFromGlobalQueue"));
            }
        }

        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == copyKeysAndNamesItem) {
                CopyToClipboard.copyKeysAndFilenames(modelTable.getSelectedItems().toArray());
            } else if (e.getSource() == copyExtendedInfoItem) {
                CopyToClipboard.copyExtendedInfo(modelTable.getSelectedItems().toArray());
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
            } else if (e.getSource() == removeFromGqItem) {
                removeSelectedUploadsFromGlobalQueue();
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

        private void removeSelectedUploadsFromGlobalQueue() {
            if( FileTransferManager.inst().getPersistenceManager() == null ) {
                return;
            }
            final List<FrostUploadItem> selectedItems = modelTable.getSelectedItems();
            final List<String> requestsToRemove = new ArrayList<String>();
            final List<FrostUploadItem> itemsToUpdate = new ArrayList<FrostUploadItem>();
            for(final FrostUploadItem item : selectedItems) {
                if( FileTransferManager.inst().getPersistenceManager().isItemInGlobalQueue(item) ) {
                    requestsToRemove.add( item.getGqIdentifier() );
                    itemsToUpdate.add(item);
                    item.setInternalRemoveExpected(true);
                }
            }
            FileTransferManager.inst().getPersistenceManager().removeRequests(requestsToRemove);
            // after remove, update state of removed items
            for(final FrostUploadItem item : itemsToUpdate) {
                item.setState(FrostUploadItem.STATE_WAITING);
                item.setEnabled(false);
                item.setPriority(-1);
                item.fireValueChanged();
            }
        }

        private void changePriority(final int prio) {
        	changeItemPriorites(modelTable.getSelectedItems(), prio);
        }

        /**
         * Generate CHK for selected files
         */
        private void generateChkForSelectedFiles() {
            model.generateChkItems(modelTable.getSelectedItems());
        }

        private void startSelectedUploadsNow() {

            final List<FrostUploadItem> itemsToStart = new LinkedList<FrostUploadItem>();
            
            for( final FrostUploadItem frostUploadItem : modelTable.getSelectedItems() ) {
                if( frostUploadItem.isExternal() ) {
                    continue;
                }
                if( frostUploadItem.getState() != FrostUploadItem.STATE_WAITING ) {
                    continue;
                }
                itemsToStart.add(frostUploadItem);
            }

            for(final FrostUploadItem ulItem : itemsToStart) {
                ulItem.setEnabled(true);
                FileTransferManager.inst().getUploadManager().startUpload(ulItem);
            }
        }

        /**
         * Reload selected files
         */
        private void uploadSelectedFiles() {
            model.uploadItems(modelTable.getSelectedItems());
        }

        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        private void invertEnabledSelected() {
            model.setItemsEnabled(null, modelTable.getSelectedItems());
        }

        private void invertEnabledAll() {
            model.setAllItemsEnabled(null);
        }

        private void disableSelectedDownloads() {
            model.setItemsEnabled(Boolean.FALSE, modelTable.getSelectedItems());
        }

        private void enableSelectedDownloads() {
            model.setItemsEnabled(Boolean.TRUE, modelTable.getSelectedItems());
        }

        private void disableAllDownloads() {
            model.setAllItemsEnabled(Boolean.FALSE);
        }

        private void enableAllDownloads() {
            model.setAllItemsEnabled(Boolean.TRUE);
        }

        @Override
        public void show(final Component invoker, final int x, final int y) {
            removeAll();

            final List<FrostUploadItem> selectedItems = modelTable.getSelectedItems();

            if( selectedItems.size() == 0 ) {
                return;
            }

            // if at least 1 item is selected
            add(copyKeysAndNamesItem);
            add(copyExtendedInfoItem);
            addSeparator();

            if( FileTransferManager.inst().getPersistenceManager() != null ) {
                add(changePriorityMenu);
                addSeparator();
            }

            final JMenu enabledSubMenu = new JMenu(language.getString("UploadPane.fileTable.popupmenu.enableUploads") + "...");
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
            if(  FileTransferManager.inst().getPersistenceManager() != null && selectedItems != null ) {
                // add only if there are removable items selected
                for(final FrostUploadItem frostUploadItem : selectedItems) {
                    if(  FileTransferManager.inst().getPersistenceManager().isItemInGlobalQueue(frostUploadItem) ) {
                        add(removeFromGqItem);
                        break;
                    }
                }
            }
            if( selectedItems.size() == 1 ) {
                if( selectedItems.get(0).isSharedFile() ) {
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
        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }
        public void keyPressed(final KeyEvent e) {
            if (e.getSource() == modelTable.getTable()) {
                uploadTable_keyPressed(e);
            }
        }
        public void keyReleased(final KeyEvent e) {
            // Nothing here
        }
        public void keyTyped(final KeyEvent e) {
            // Nothing here
        }
        public void actionPerformed(final ActionEvent e) {
        	if (e.getSource() == uploadAddFilesButton) {
        		uploadAddFilesButton_actionPerformed(e);
        	}
        }
        @Override
        public void mousePressed(final MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (e.getSource() == modelTable.getTable()) {
                    // Start file from download table. Is this a good idea?
                    uploadTableDoubleClick(e);
                }
            } else if (e.isPopupTrigger()) {
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
        public void propertyChange(final PropertyChangeEvent evt) {
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
        public void itemStateChanged(final ItemEvent e) {
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
