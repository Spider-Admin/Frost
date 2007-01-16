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
import java.awt.datatransfer.*;
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
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class UploadPanel extends JPanel {

    private PopupMenuUpload popupMenuUpload = null;

    private Listener listener = new Listener();

    private static Logger logger = Logger.getLogger(UploadPanel.class.getName());

    private UploadModel model = null;

    private Language language = null;

    private JPanel uploadTopPanel = new JPanel();
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

            // create the top panel
            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(uploadAddFilesButton, "/data/browse_rollover.gif");
            BoxLayout dummyLayout = new BoxLayout(uploadTopPanel, BoxLayout.X_AXIS);
            uploadTopPanel.setLayout(dummyLayout);
            uploadTopPanel.add(uploadAddFilesButton);
            uploadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
            uploadTopPanel.add(removeFinishedUploadsCheckBox);
            if( PersistenceManager.isPersistenceEnabled() ) {
                uploadTopPanel.add(showExternalGlobalQueueItems);
            }
            uploadTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
            uploadTopPanel.add(Box.createHorizontalGlue());
            uploadTopPanel.add(uploadItemCountLabel);
            
            // create the main upload panel
            UploadTableFormat tableFormat = new UploadTableFormat();

            modelTable = new SortedModelTable(model, tableFormat);
            setLayout(new BorderLayout());
            add(uploadTopPanel, BorderLayout.NORTH);
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
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.removeItems(selectedItems);
            modelTable.getTable().clearSelection();
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
            new StringBuffer()
                .append(language.getString("UploadPane.toolbar.waiting"))
                .append(": ")
                .append(uploadItemCount)
                .toString();
        uploadItemCountLabel.setText(s);
    }
    
    private class PopupMenuUpload extends JSkinnablePopupMenu implements ActionListener, LanguageListener, ClipboardOwner {

        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();
        private JMenuItem generateChkForSelectedFilesItem = new JMenuItem();
        private JMenuItem uploadSelectedFilesItem = new JMenuItem();
        private JMenuItem removeSelectedFilesItem = new JMenuItem();
        private JMenuItem showSharedFileItem = new JMenuItem();
        private JMenu copyToClipboardMenu = new JMenu();

        private JMenu changePriorityMenu = null;
        private JMenuItem prio0Item = null;
        private JMenuItem prio1Item = null;
        private JMenuItem prio2Item = null;
        private JMenuItem prio3Item = null;
        private JMenuItem prio4Item = null;
        private JMenuItem prio5Item = null;
        private JMenuItem prio6Item = null;

        private String keyNotAvailableMessage;
        private String fileMessage;
        private String keyMessage;
        private String bytesMessage;

        private Clipboard clipboard;

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
            generateChkForSelectedFilesItem.addActionListener(this);
            showSharedFileItem.addActionListener(this);
        }

        private void refreshLanguage() {
            keyNotAvailableMessage = language.getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
            fileMessage = language.getString("Common.copyToClipBoard.extendedInfo.file")+" ";
            keyMessage = language.getString("Common.copyToClipBoard.extendedInfo.key")+" ";
            bytesMessage = language.getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";

            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            generateChkForSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.startEncodingOfSelectedFiles"));
            uploadSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.uploadSelectedFiles"));
            removeSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.remove.removeSelectedFiles"));
            showSharedFileItem.setText(language.getString("UploadPane.fileTable.popupmenu.showSharedFile"));
            
            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");
            
            // FIXME: translate
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

        private Clipboard getClipboard() {
            if (clipboard == null) {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
            return clipboard;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == copyKeysItem) {
                copyKeys();
            } else if (e.getSource() == copyKeysAndNamesItem) {
                copyKeysAndNames();
            } else if (e.getSource() == copyExtendedInfoItem) {
                copyExtendedInfo();
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
            } 
        }
        
        private void changePriority(int prio) {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            PersistenceManager.changeItemPriorites(selectedItems, prio);
        }

        /**
         * Generate CHK for selected files
         */
        private void generateChkForSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.generateChkItems(selectedItems);
        }

        /**
         * Reload selected files
         */
        private void uploadSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.uploadItems(selectedItems);
        }

        /**
         * Remove selected files
         */
        private void removeSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.removeItems(selectedItems);
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
                    FrostUploadItem item = (FrostUploadItem) selectedItems[i];
                    Mixed.appendKeyAndFilename(textToCopy, item.getKey(), item.getFile().getName(), keyNotAvailableMessage);
                }
                StringSelection selection = new StringSelection(textToCopy.toString());
                getClipboard().setContents(selection, this);
            }
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
                    FrostUploadItem item = (FrostUploadItem) selectedItems[i];
                    String key = item.getKey();
                    if (key == null) {
                        key = keyNotAvailableMessage;
                    }
                    textToCopy.append(fileMessage);
                    textToCopy.append(item.getFile().getName() + "\n");
                    textToCopy.append(keyMessage);
                    textToCopy.append(key + "\n");
                    textToCopy.append(bytesMessage);
                    textToCopy.append(item.getFileSize() + "\n\n");
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
                    FrostUploadItem item = (FrostUploadItem) selectedItems[i];
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
            addSeparator();
            
            if( PersistenceManager.isPersistenceEnabled() ) {
                add(changePriorityMenu);
                addSeparator();
            }
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

        public void lostOwnership(Clipboard cb, Transferable contents) {
            // Nothing here
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
