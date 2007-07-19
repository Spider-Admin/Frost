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
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.storage.perst.*;
import frost.threads.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.search.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class SharedFilesPanel extends JPanel {

    private PopupMenu popupMenuUpload = null;

    private Listener listener = new Listener();

    private static final Logger logger = Logger.getLogger(SharedFilesPanel.class.getName());

    private SharedFilesModel model = null;

    private Language language = null;

    private JToolBar sharedFilesToolBar = new JToolBar();
    private JButton addSharedFilesButton = new JButton(new ImageIcon(getClass().getResource("/data/browse.gif")));
    
    private int sharedFilesCount = 0;
    private JLabel sharedFilesCountLabel = new JLabel();

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
            toolkit.configureButton(addSharedFilesButton, "/data/browse_rollover.gif");

            sharedFilesToolBar.setRollover(true);
            sharedFilesToolBar.setFloatable(false);

            sharedFilesToolBar.add(addSharedFilesButton);
            sharedFilesToolBar.add(Box.createRigidArea(new Dimension(80, 0)));
            sharedFilesToolBar.add(Box.createHorizontalGlue());
            sharedFilesToolBar.add(sharedFilesCountLabel);

            // create the main upload panel
            modelTable = new SortedModelTable(model);
            new TableFindAction().install(modelTable.getTable());
            setLayout(new BorderLayout());
            add(sharedFilesToolBar, BorderLayout.NORTH);
            add(modelTable.getScrollPane(), BorderLayout.CENTER);
            fontChanged();

            // listeners
            addSharedFilesButton.addActionListener(listener);
            modelTable.getScrollPane().addMouseListener(listener);
            modelTable.getTable().addKeyListener(listener);
            modelTable.getTable().addMouseListener(listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

            initialized = true;
        }
    }
    
    public SharedFilesTableFormat getTableFormat() {
        return (SharedFilesTableFormat) modelTable.getTableFormat();
    }

    public ModelTable getModelTable() {
        return modelTable;
    }
    
    public void setAddFilesButtonEnabled(boolean enabled) {
        addSharedFilesButton.setEnabled(enabled);
    }

    private Dimension calculateLabelSize(String text) {
        JLabel dummyLabel = new JLabel(text);
        dummyLabel.doLayout();
        return dummyLabel.getPreferredSize();
    }

    private void refreshLanguage() {
        addSharedFilesButton.setToolTipText(language.getString("SharedFilesPane.toolbar.tooltip.browse") + "...");
        
        String waiting = language.getString("SharedFilesPane.toolbar.files");
        Dimension labelSize = calculateLabelSize(waiting + ": 00000");
        sharedFilesCountLabel.setPreferredSize(labelSize);
        sharedFilesCountLabel.setMinimumSize(labelSize);
        sharedFilesCountLabel.setText(waiting + ": " + sharedFilesCount);

    }

    private PopupMenu getPopupMenuUpload() {
        if (popupMenuUpload == null) {
            popupMenuUpload = new PopupMenu();
            language.addLanguageListener(popupMenuUpload);
        }
        return popupMenuUpload;
    }

    private void uploadTable_keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
            removeSelectedFiles();
        }
    }

    private void removeSelectedFiles() {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        model.removeItems(selectedItems);

        modelTable.getTable().clearSelection();

        // currently running upload items are removed during next startup
        
        // notify list upload thread that user changed something
        FileListUploadThread.getInstance().userActionOccured();
    }

    public void uploadAddFilesButton_actionPerformed(ActionEvent e) {

        final JFileChooser fc = new JFileChooser(Core.frostSettings.getValue(SettingsClass.DIR_LAST_USED));
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
        List<File> uploadFileItems = new LinkedList<File>();
        for (int i = 0; i < selectedFiles.length; i++) {
            // collect all choosed files + files in all choosed directories
            ArrayList<File> allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
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
            Core.frostSettings.setValue(SettingsClass.DIR_LAST_USED, parentDir);
        }
        // ask for owner to use
        SharedFilesOwnerDialog dlg = 
            new SharedFilesOwnerDialog(MainFrame.getInstance(), "Choose an owner for the upload files");
        if( dlg.showDialog() == SharedFilesOwnerDialog.CANCEL ) {
            return;
        }
        String owner = dlg.getChoosedIdentityName();

        List<NewUploadFile> uploadItems = new LinkedList<NewUploadFile>();
        for(Iterator<File> i=uploadFileItems.iterator(); i.hasNext(); ) {
            File file = i.next();
            NewUploadFile nuf = new NewUploadFile(file, owner);
            uploadItems.add(nuf);
        }
        
        // notify list upload thread that user changed something
        FileListUploadThread.getInstance().userActionOccured();
        
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
        
        model.addOrderedModelListener(new SortedModelListener() {
            public void modelCleared() {
                updateSharedFilesItemCount();
            }
            public void itemAdded(int position, ModelItem item) {
                updateSharedFilesItemCount();
            }
            public void itemChanged(int position, ModelItem item) {
            }
            public void itemsRemoved(int[] positions, ModelItem[] items) {
                updateSharedFilesItemCount();
            }
        });
    }

    private void showProperties() {
        ModelItem[] selectedItems = modelTable.getSelectedItems();
        if( selectedItems.length == 0 ) {
            return;
        }
        
        List<FrostSharedFileItem> items = new LinkedList<FrostSharedFileItem>();
        for (int i = 0; i < selectedItems.length; i++) {
            FrostSharedFileItem item = (FrostSharedFileItem) selectedItems[i];
            items.add(item);
        }
        FrostSharedFileItem defaultItem = items.get(0);
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
        
        for( FrostSharedFileItem item : items ) {
            // check if item was really changed, calling a setter will mark the item changed
            String oldStr, newStr;
            
            oldStr = item.getComment();
            newStr = dlg.getComment();
            if( !stringsEqual(oldStr, newStr) ) {
                item.setComment( dlg.getComment() );
            }
            
            oldStr = item.getKeywords();
            newStr = dlg.getKeywords();
            if( !stringsEqual(oldStr, newStr) ) {
                item.setKeywords( dlg.getKeywords() );
            }
            
            if( item.getRating() != dlg.getRating() ) {
                item.setRating( dlg.getRating() );
            }
        }
    }
    
    private void updateSharedFilesItemCount() {
        sharedFilesCount = model.getItemCount();
        String s =
            new StringBuilder()
                .append(language.getString("SharedFilesPane.toolbar.files"))
                .append(": ")
                .append(sharedFilesCount)
                .toString();
        sharedFilesCountLabel.setText(s);
    }

    private boolean stringsEqual(String oldStr, String newStr) {
        if( oldStr == null && newStr != null ) {
            return false;
        }
        if( oldStr != null && newStr == null ) {
            return false;
        }
        if( oldStr == null && newStr == null ) {
            return true;
        }
        if( oldStr.equals(newStr) ) {
            return true;
        } else {
            return false;
        }
    }
    
    private class PopupMenu extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();
        private JMenuItem uploadSelectedFilesItem = new JMenuItem();
        private JMenuItem removeSelectedFilesItem = new JMenuItem();
        private JMenuItem propertiesItem = new JMenuItem();

        private JMenuItem setPathItem = new JMenuItem();

        private JMenu copyToClipboardMenu = new JMenu();

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
            copyToClipboardMenu.add(copyExtendedInfoItem);

            copyKeysAndNamesItem.addActionListener(this);
            copyKeysItem.addActionListener(this);
            copyExtendedInfoItem.addActionListener(this);
            removeSelectedFilesItem.addActionListener(this);
            uploadSelectedFilesItem.addActionListener(this);
            propertiesItem.addActionListener(this);
            setPathItem.addActionListener(this);
        }

        private void refreshLanguage() {
            propertiesItem.setText(language.getString("Common.properties"));
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            uploadSelectedFilesItem.setText(language.getString("SharedFilesPane.fileTable.popupmenu.uploadSelectedFiles"));
            removeSelectedFilesItem.setText(language.getString("SharedFilesPane.fileTable.popupmenu.removeSelectedFiles"));
            setPathItem.setText(language.getString("SharedFilesPane.fileTable.popupmenu.setPath"));

            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == copyKeysItem) {
                CopyToClipboard.copyKeys(modelTable.getSelectedItems());
            }
            if (e.getSource() == copyKeysAndNamesItem) {
                CopyToClipboard.copyKeysAndFilenames(modelTable.getSelectedItems());
            }
            if (e.getSource() == copyExtendedInfoItem) {
                CopyToClipboard.copyExtendedInfo(modelTable.getSelectedItems());
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
            if( e.getSource() == setPathItem ) {
                setPath();
            }
        }
        
        private void setPath() {
            
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if( selectedItems.length != 1 ) {
                return;
            }
            
            final FrostSharedFileItem sfItem = (FrostSharedFileItem) selectedItems[0];
            
            final JFileChooser fc = new JFileChooser(Core.frostSettings.getValue(SettingsClass.DIR_LAST_USED));
            fc.setDialogTitle(language.getString("SharedFilesPane.filechooser.title"));
            fc.setFileHidingEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            fc.setPreferredSize(new Dimension(600, 400));
            fc.setAcceptAllFileFilterUsed(false);
            javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    if( f.isDirectory() || f.getName().equals(sfItem.getFile().getName()) ) {
                        return true;
                    } else {
                        return false;
                    }
                }
                public String getDescription() {
                    return sfItem.getFile().getName();
                }
            };
            fc.addChoosableFileFilter(ff);

            if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = fc.getSelectedFile();
            if( selectedFile == null ) {
                return;
            }
            
            // check if size matches
            if( sfItem.getFileSize() != selectedFile.length() ) {
                JOptionPane.showMessageDialog(
                        MainFrame.getInstance(),
                        language.getString("SharedFilesPane.sizeChangedErrorDialog.text"), 
                        language.getString("SharedFilesPane.sizeChangedErrorDialog.title"), 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            sfItem.setLastModified(selectedFile.lastModified());
            sfItem.setFile(selectedFile);
            sfItem.setValid(true);
        }

        /**
         * Reload selected files
         */
        private void uploadSelectedFiles() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.requestItems(selectedItems);
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
            
            // check if the one invalid selected item is selected, allow removal and set path
            if( selectedItems.length == 1 
                    && ! ((FrostSharedFileItem) selectedItems[0]).isValid() )
            {
                add(setPathItem);
                addSeparator();
                add(removeSelectedFilesItem);
                super.show(invoker, x, y);
                return;
            }

            // if all selected items are valid, then show long menu
            boolean allValid = true;
            for(int i=0; i < selectedItems.length; i++) {
                FrostSharedFileItem sfItem = (FrostSharedFileItem) selectedItems[i];
                if( !sfItem.isValid() ) {
                    allValid = false;
                    break;
                }
            }
            if( allValid ) {
                add(copyToClipboardMenu);
                addSeparator();
                add(removeSelectedFilesItem);
                addSeparator();
                add(uploadSelectedFilesItem);
                addSeparator();
                add(propertiesItem);
            } else {
                // we have either valid+invalid items selected, or multiple invalid items
                // allow removal
                add(removeSelectedFilesItem);
            }
            super.show(invoker, x, y);
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
            if (e.getSource() == addSharedFilesButton) {
                uploadAddFilesButton_actionPerformed(e);
            }
        }
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) {
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
