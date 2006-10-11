/*
  SharedFilesPanel.java / Frost
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
package frost.fileTransfer.sharing;

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
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class SharedFilesPanel extends JPanel {

    private PopupMenuUpload popupMenuUpload = null;

    private Listener listener = new Listener();

    private static Logger logger = Logger.getLogger(SharedFilesPanel.class.getName());

    private SharedFilesModel model = null;

    private Language language = null;

    private JPanel uploadTopPanel = new JPanel();
    private JButton uploadAddFilesButton = new JButton(new ImageIcon(getClass().getResource("/data/browse.gif")));

    private SortedModelTable modelTable;

    private boolean initialized = false;

    public SharedFilesPanel() {
        super();

        language = Language.getInstance();
        language.addLanguageListener(listener);
    }

    public void initialize() {
        if (!initialized) {
            refreshLanguage();

            // create the top panel
            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(uploadAddFilesButton, "/data/browse_rollover.gif");
            uploadTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
            uploadTopPanel.add(uploadAddFilesButton);

            // create the main upload panel
            SharedFilesTableFormat tableFormat = new SharedFilesTableFormat();

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
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

            initialized = true;
        }
    }

    public void setAddFilesButtonEnabled(boolean enabled) {
        uploadAddFilesButton.setEnabled(enabled);
    }

    private void refreshLanguage() {
        uploadAddFilesButton.setToolTipText(language.getString("UploadPane.toolbar.tooltip.browse") + "...");
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

        final JFileChooser fc = new JFileChooser(Core.frostSettings.getValue("lastUsedDirectory"));
        fc.setDialogTitle(language.getString("SharedFilesPane.filechooser.title"));
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
        List uploadFileItems = new LinkedList();
        for (int i = 0; i < selectedFiles.length; i++) {
            // collect all choosed files + files in all choosed directories
            ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
            for (int j = 0; j < allFiles.size(); j++) {
                File newFile = (File) allFiles.get(j);
                if (newFile.isFile() && newFile.length() > 0) {
                    uploadFileItems.add(newFile);
                    if( parentDir == null ) {
                        parentDir = newFile.getParent(); // remember last upload dir
                    }
                }
            }
        }
        if( parentDir != null ) {
            Core.frostSettings.setValue("lastUsedDirectory", parentDir);
        }
        // ask for owner to use
        SharedFilesOwnerDialog dlg = 
            new SharedFilesOwnerDialog(MainFrame.getInstance(), "Choose an owner for the upload files");
        if( dlg.showDialog() == SharedFilesOwnerDialog.CANCEL ) {
            return;
        }
        String owner = dlg.getChoosedIdentityName();

        List uploadItems = new LinkedList();
        for(Iterator i=uploadFileItems.iterator(); i.hasNext(); ) {
            File file = (File)i.next();
            NewUploadFile nuf = new NewUploadFile(file, owner);
            uploadItems.add(nuf);
        }
        
        Core.getInstance().getFileTransferManager().getNewUploadFilesManager().addNewUploadFiles(uploadItems);
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

    public void setModel(SharedFilesModel model) {
        this.model = model;
    }

    private void showProperties() {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        if( selectedItems.length == 0 ) {
            return;
        }
        
        List items = new LinkedList();
        for (int i = 0; i < selectedItems.length; i++) {
            FrostSharedFileItem item = (FrostSharedFileItem) selectedItems[i];
            items.add(item);
        }
        FrostSharedFileItem defaultItem = (FrostSharedFileItem)items.get(0);
        SharedFilesPropertiesDialog dlg = new SharedFilesPropertiesDialog(MainFrame.getInstance());
        
        String singleFilename = null;
        int fileCount = 0;
        
        if( items.size() == 1 ) {
            singleFilename = defaultItem.getFile().getName();
        } else {
            fileCount = items.size();
        }
        
        boolean okClicked = dlg.startDialog(singleFilename, fileCount, defaultItem);
        if( !okClicked ) {
            return;
        }
        
        for(Iterator i = items.iterator(); i.hasNext(); ) {
            FrostSharedFileItem item = (FrostSharedFileItem) i.next();
            // FIXME: check if item was really changed before resetting time
            item.setComment( dlg.getComment() );
            item.setRating( dlg.getRating() );
            item.setKeywords( dlg.getKeywords() );
            
            item.itemWasChanged();
        }
    }

    
    private class PopupMenuUpload extends JSkinnablePopupMenu 
      implements ActionListener, LanguageListener, ClipboardOwner {

        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();
        private JMenuItem uploadSelectedFilesItem = new JMenuItem();
        private JMenuItem removeSelectedFilesItem = new JMenuItem();
        private JMenuItem propertiesItem = new JMenuItem();

        private JMenu changeDestinationBoardMenu = new JMenu();
        private JMenu copyToClipboardMenu = new JMenu();

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
            refreshLanguage();

            copyToClipboardMenu.add(copyKeysAndNamesItem);
            copyToClipboardMenu.add(copyKeysItem);
            copyToClipboardMenu.add(copyExtendedInfoItem);

            copyKeysAndNamesItem.addActionListener(this);
            copyKeysItem.addActionListener(this);
            copyExtendedInfoItem.addActionListener(this);
            removeSelectedFilesItem.addActionListener(this);
            uploadSelectedFilesItem.addActionListener(this);
            propertiesItem.addActionListener(this);
        }

        private void refreshLanguage() {
            keyNotAvailableMessage = language.getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
            fileMessage = language.getString("Common.copyToClipBoard.extendedInfo.file")+" ";
            keyMessage = language.getString("Common.copyToClipBoard.extendedInfo.key")+" ";
            bytesMessage = language.getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";

            propertiesItem.setText(language.getString("Common.properties"));
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            uploadSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.uploadSelectedFiles"));
            removeSelectedFilesItem.setText(language.getString("UploadPane.fileTable.popupmenu.remove.removeSelectedFiles"));

            changeDestinationBoardMenu.setText(language.getString("UploadPane.fileTable.popupmenu.changeDestinationBoard"));
            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");
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
            }
            if (e.getSource() == copyKeysAndNamesItem) {
                copyKeysAndNames();
            }
            if (e.getSource() == copyExtendedInfoItem) {
                copyExtendedInfo();
            }
            if (e.getSource() == removeSelectedFilesItem) {
                removeSelectedFiles();
            }
            if (e.getSource() == uploadSelectedFilesItem) {
                uploadSelectedFiles();
            }
            if( e.getSource() == propertiesItem ) {
                showProperties();
            }
        }

        /**
         * Reload selected files
         */
        private void uploadSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.requestItems(selectedItems);
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
                    FrostSharedFileItem item = (FrostSharedFileItem) selectedItems[i];
                    String key = item.getChkKey();
                    if (key == null) {
                        key = keyNotAvailableMessage;
                    }
                    textToCopy.append(key);
                    textToCopy.append("/");
                    textToCopy.append(item.getFile().getName());
                    textToCopy.append("\n");
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
                    FrostSharedFileItem item = (FrostSharedFileItem) selectedItems[i];
                    String key = item.getChkKey();
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
                    FrostSharedFileItem item = (FrostSharedFileItem) selectedItems[i];
                    String key = item.getChkKey();
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
            add(removeSelectedFilesItem);
            addSeparator();
            add(uploadSelectedFilesItem);
            addSeparator();
            add(propertiesItem);

            super.show(invoker, x, y);
        }

        public void lostOwnership(Clipboard cb, Transferable contents) {
        }
    }

    private class Listener
        extends MouseAdapter
        implements LanguageListener, KeyListener, ActionListener, MouseListener, PropertyChangeListener {

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
                    showProperties();
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
    }
}
