/*
  MessageTextPane.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  Some changes by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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
package frost;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import frost.fileTransfer.download.*;
import frost.fileTransfer.search.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class MessageTextPane extends JPanel {

    private Language language = Language.getInstance();
    private Logger logger = Logger.getLogger(MessageTextPane.class.getName());

    private AntialiasedTextArea messageTextArea = null;
    private JSplitPane messageSplitPane = null;
    private JSplitPane attachmentsSplitPane = null;
    
    private AttachedFilesTableModel attachedFilesModel;
    private AttachedBoardTableModel attachedBoardsModel;
    private JTable filesTable = null;
    private JTable boardsTable = null;
    private JScrollPane filesTableScrollPane;
    private JScrollPane boardsTableScrollPane;

    private PopupMenuAttachmentBoard popupMenuAttachmentBoard = null;
    private PopupMenuAttachmentTable popupMenuAttachmentTable = null;
    private PopupMenuTofText popupMenuTofText = null;

    private FrostMessageObject selectedMessage;

    private MainFrame mainFrame = MainFrame.getInstance();
    private DownloadModel downloadModel = null;
    
    private Component parentFrame;
    
    private PropertyChangeListener propertyChangeListener;
    
    public MessageTextPane(Component parentFrame) {
        this.parentFrame = parentFrame;
        initialize();
    }
    
    /**
     * Called if there are no boards in the board list.
     */
    public void update_noBoardsFound() {
        messageSplitPane.setBottomComponent(null);
        messageSplitPane.setDividerSize(0);
        messageTextArea.setText(language.getString("Welcome message"));
    }

    /**
     * Called if a board is selected, but no message in message table.
     */
    public void update_boardSelected() {
        messageSplitPane.setBottomComponent(null);
        messageSplitPane.setDividerSize(0);
        messageTextArea.setText(language.getString("Select a message to view its content."));
    }

    /**
     * Called if a folder is selected.
     */
    public void update_folderSelected() {
        messageSplitPane.setBottomComponent(null);
        messageSplitPane.setDividerSize(0);
        messageTextArea.setText(language.getString("Select a board to view its content."));
    }
    
    /**
     * Called if a message is selected.
     */
    public void update_messageSelected(FrostMessageObject msg) {
        
        selectedMessage = msg;

        messageTextArea.setText(selectedMessage.getContent());

        List fileAttachments = selectedMessage.getAttachmentsOfType(Attachment.FILE);
        List boardAttachments = selectedMessage.getAttachmentsOfType(Attachment.BOARD);
        attachedFilesModel.setData(fileAttachments);
        attachedBoardsModel.setData(boardAttachments);

        positionDividers(fileAttachments.size(), boardAttachments.size());
    }

    private void initialize() {
        
        setLayout(new BorderLayout());
        
        // build message body scroll pane
        messageTextArea = new AntialiasedTextArea();
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setAntiAliasEnabled(Core.frostSettings.getBoolValue("messageBodyAA"));
        JScrollPane messageBodyScrollPane = new JScrollPane(messageTextArea);

        // build attached files scroll pane
        attachedFilesModel = new AttachedFilesTableModel();
        filesTable = new JTable(attachedFilesModel);
        filesTableScrollPane = new JScrollPane(filesTable);

        // build attached boards scroll pane
        attachedBoardsModel = new AttachedBoardTableModel();
        boardsTable = new JTable(attachedBoardsModel) {
            DescColumnRenderer descColRenderer = new DescColumnRenderer();
            public TableCellRenderer getCellRenderer(int row, int column) {
                if( column == 2 ) {
                    return descColRenderer;
                }
                return super.getCellRenderer(row, column);
            }
            // renderer that show a tooltip text, used for the description column
            class DescColumnRenderer extends DefaultTableCellRenderer {
                public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column)
                {
                    super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column);

                    String sval = (String)value;
                    if( sval != null &&
                        sval.length() > 0 )
                    {
                        setToolTipText(sval);
                    } else {
                        setToolTipText(null);
                    }
                    return this;
                }
            }
        };
        boardsTableScrollPane = new JScrollPane(boardsTable);

        fontChanged();
        
        //Put everything together
        attachmentsSplitPane =
            new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                filesTableScrollPane,
                boardsTableScrollPane);
        attachmentsSplitPane.setResizeWeight(0.5);
        attachmentsSplitPane.setDividerSize(3);
        attachmentsSplitPane.setDividerLocation(0.5);

        messageSplitPane =
            new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                messageBodyScrollPane,
                attachmentsSplitPane);
        messageSplitPane.setDividerSize(0);
        messageSplitPane.setDividerLocation(1.0);
        messageSplitPane.setResizeWeight(1.0);
        
        add(messageSplitPane, BorderLayout.CENTER);

        messageTextArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTofTextAreaPopupMenu(e);
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTofTextAreaPopupMenu(e);
                }
            }
        });
        messageTextArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if( e == null ) {
                    return;
                }
                if(e.getKeyChar() == 'n' && parentFrame == mainFrame ) {
                    mainFrame.getMessagePanel().selectNextUnreadMessage();
                }
            }
        });
        filesTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showAttachedFilesPopupMenu(e);
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showAttachedFilesPopupMenu(e);
                }
            }
        });
        boardsTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showAttachedBoardsPopupMenu(e);
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showAttachedBoardsPopupMenu(e);
                }
            }
        });
        
        propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("messageBodyAA")) {
                    messageTextArea.setAntiAliasEnabled(Core.frostSettings.getBoolValue("messageBodyAA"));
                } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_NAME)) {
                    fontChanged();
                } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_SIZE)) {
                    fontChanged();
                } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_STYLE)) {
                    fontChanged();
                }
            }
        };
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_STYLE, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener("messageBodyAA", propertyChangeListener);
    }

    private void fontChanged() {
        String fontName = Core.frostSettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
        int fontStyle = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
        int fontSize = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
            logger.severe(
                "The selected font was not found in your system\n"
                    + "That selection will be changed to \"Monospaced\".");
            Core.frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
            font = new Font("Monospaced", fontStyle, fontSize);
        }
        messageTextArea.setFont(font);
    }
    
    private void positionDividers(int attachedFiles, int attachedBoards) {
        
        if (attachedFiles == 0 && attachedBoards == 0) {
            // Neither files nor boards
            messageSplitPane.setBottomComponent(null);
            messageSplitPane.setDividerSize(0);
            return;
        }
        
        messageSplitPane.setDividerSize(3);
        messageSplitPane.setDividerLocation(0.75);
        
        if (attachedFiles != 0 && attachedBoards == 0) {
            //Only files
            attachmentsSplitPane.setTopComponent(null);
            attachmentsSplitPane.setBottomComponent(null);

            messageSplitPane.setBottomComponent(filesTableScrollPane);
            return;
        }
        if (attachedFiles == 0 && attachedBoards != 0) {
            //Only boards
            attachmentsSplitPane.setTopComponent(null);
            attachmentsSplitPane.setBottomComponent(null);

            messageSplitPane.setBottomComponent(boardsTableScrollPane);
            return;
        }
        if (attachedFiles != 0 && attachedBoards != 0) {
            //Both files and boards
            attachmentsSplitPane.setTopComponent(filesTableScrollPane);
            attachmentsSplitPane.setBottomComponent(boardsTableScrollPane);

            messageSplitPane.setBottomComponent(attachmentsSplitPane);
        }
    }

    public void saveMessageButton_actionPerformed() {
        FileAccess.saveDialog(
            MainFrame.getInstance(),
            messageTextArea.getText(),
            Core.frostSettings.getValue("lastUsedDirectory"),
            language.getString("Save message to disk"));
    }

    /**
     * Adds all boards from the attachedBoardsTable to board list.
     * If targetFolder is null the boards are added to the root folder.
     */
    private void downloadBoards(Board targetFolder) {
        logger.info("adding boards");
        int[] selectedRows = boardsTable.getSelectedRows();
    
        if (selectedRows.length == 0) {
            // add all rows
            boardsTable.selectAll();
            selectedRows = boardsTable.getSelectedRows();
            if (selectedRows.length == 0)
                return;
        }
        LinkedList boards = selectedMessage.getAttachmentsOfType(Attachment.BOARD);
        for (int i = 0; i < selectedRows.length; i++) {
            BoardAttachment ba = (BoardAttachment) boards.get(selectedRows[i]);
            Board fbo = ba.getBoardObj();
            String name = fbo.getName();
    
            // search board in exising boards list
            Board board = mainFrame.getTofTreeModel().getBoardByName(name);
    
            //ask if we already have the board
            if (board != null) {
                if (JOptionPane.showConfirmDialog(
                        this,
                        "You already have a board named " + name + ".\n" +
                            "Are you sure you want to add this one over it?",
                        "Board already exists",
                        JOptionPane.YES_NO_OPTION) != 0) 
                {
                    continue; // next row of table / next attached board
                } else {
                    // change existing board keys to keys of new board
                    board.setPublicKey(fbo.getPublicKey());
                    board.setPrivateKey(fbo.getPrivateKey());
                    mainFrame.updateTofTree(board);
                }
            } else {
                // its a new board
                if(targetFolder == null) {
                    mainFrame.getTofTreeModel().addNodeToTree(fbo);
                } else {
                    mainFrame.getTofTreeModel().addNodeToTree(fbo, targetFolder);
                }
            }
        }
    }

    /**
     * Adds either the selected or all files from the attachmentTable to downloads table.
     */
    private void downloadAttachments() {
        int[] selectedRows = filesTable.getSelectedRows();
    
        // If no rows are selected, add all attachments to download table
        if (selectedRows.length == 0) {
            Iterator it = selectedMessage.getAttachmentsOfType(Attachment.FILE).iterator();
            while (it.hasNext()) {
                FileAttachment fa = (FileAttachment) it.next();
                SharedFileObject sfo = fa.getFileObj();
                FrostSearchItem fsio = new FrostSearchItem(
                        mainFrame.getTofTreeModel().getBoardByName(selectedMessage.getBoard()),
                        sfo,
                        FrostSearchItem.STATE_NONE);
                //FIXME: <-does this matter?
                FrostDownloadItem dlItem = new FrostDownloadItem(fsio);
                downloadModel.addDownloadItem(dlItem);
            }
    
        } else {
            LinkedList attachments = selectedMessage.getAttachmentsOfType(Attachment.FILE);
            for (int i = 0; i < selectedRows.length; i++) {
                FileAttachment fo = (FileAttachment) attachments.get(selectedRows[i]);
                SharedFileObject sfo = fo.getFileObj();
                FrostSearchItem fsio = new FrostSearchItem(
                        mainFrame.getTofTreeModel().getBoardByName(selectedMessage.getBoard()),
                        sfo,
                        FrostSearchItem.STATE_NONE);
                FrostDownloadItem dlItem = new FrostDownloadItem(fsio);
                downloadModel.addDownloadItem(dlItem);
            }
        }
    }

    private void showAttachedBoardsPopupMenu(MouseEvent e) {
        if (popupMenuAttachmentBoard == null) {
            popupMenuAttachmentBoard = new PopupMenuAttachmentBoard();
            language.addLanguageListener(popupMenuAttachmentBoard);
        }
        popupMenuAttachmentBoard.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private void showAttachedFilesPopupMenu(MouseEvent e) {
        if (popupMenuAttachmentTable == null) {
            popupMenuAttachmentTable = new PopupMenuAttachmentTable();
            language.addLanguageListener(popupMenuAttachmentTable);
        }
        popupMenuAttachmentTable.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showTofTextAreaPopupMenu(MouseEvent e) {
        if (popupMenuTofText == null) {
            popupMenuTofText = new PopupMenuTofText(messageTextArea);
            language.addLanguageListener(popupMenuTofText);
        }
        popupMenuTofText.show(e.getComponent(), e.getX(), e.getY());
    }

    private class PopupMenuAttachmentBoard
    extends JSkinnablePopupMenu
    implements ActionListener, LanguageListener {

        private JMenuItem cancelItem = new JMenuItem();
        private JMenuItem saveBoardsItem = new JMenuItem();
        private JMenuItem saveBoardsToFolderItem = new JMenuItem();
    
        public PopupMenuAttachmentBoard() {
            super();
            initialize();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveBoardsItem) {
                downloadBoards(null);
            } else if (e.getSource() == saveBoardsToFolderItem) {
                TargetFolderChooser tfc = new TargetFolderChooser(mainFrame.getTofTreeModel());
                Board targetFolder = tfc.startDialog();
                if( targetFolder != null ) {
                    downloadBoards(targetFolder);
                }
            }
        }
    
        private void initialize() {
            languageChanged(null);
    
            saveBoardsItem.addActionListener(this);
            saveBoardsToFolderItem.addActionListener(this);
        }
    
        public void languageChanged(LanguageEvent event) {
            saveBoardsItem.setText(language.getString("Add Board(s)"));
            saveBoardsToFolderItem.setText(language.getString("Add Board(s) to folder")+" ...");
            cancelItem.setText(language.getString("Cancel"));
        }
    
        public void show(Component invoker, int x, int y) {
            removeAll();
    
            add(saveBoardsItem);
            add(saveBoardsToFolderItem);
            addSeparator();
            add(cancelItem);
    
            super.show(invoker, x, y);
        }
    }
    
    private class PopupMenuAttachmentTable
        extends JSkinnablePopupMenu
        implements ActionListener, LanguageListener {
    
        private JMenuItem cancelItem = new JMenuItem();
        private JMenuItem saveAttachmentItem = new JMenuItem();
        private JMenuItem saveAttachmentsItem = new JMenuItem();
    
        public PopupMenuAttachmentTable() throws HeadlessException {
            super();
            initialize();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveAttachmentsItem || e.getSource() == saveAttachmentItem) {
                downloadAttachments();
            }
        }
    
        private void initialize() {
            languageChanged(null);
    
            saveAttachmentsItem.addActionListener(this);
            saveAttachmentItem.addActionListener(this);
        }
    
        /* (non-Javadoc)
         * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
         */
        public void languageChanged(LanguageEvent event) {
            saveAttachmentsItem.setText(language.getString("Download attachment(s)"));
            saveAttachmentItem.setText(language.getString("Download selected attachment"));
            cancelItem.setText(language.getString("Cancel"));
        }
    
        public void show(Component invoker, int x, int y) {
            removeAll();
    
            if (filesTable.getSelectedRow() == -1) {
                add(saveAttachmentsItem);
            } else {
                add(saveAttachmentItem);
            }
            addSeparator();
            add(cancelItem);
    
            super.show(invoker, x, y);
        }
    }

    private class PopupMenuTofText
    extends JSkinnablePopupMenu
    implements ActionListener, LanguageListener, ClipboardOwner {

        private Clipboard clipboard;
    
        private JTextComponent sourceTextComponent;
    
        private JMenuItem copyItem = new JMenuItem();
        private JMenuItem cancelItem = new JMenuItem();
        private JMenuItem saveMessageItem = new JMenuItem();
    
        public PopupMenuTofText(JTextComponent sourceTextComponent) {
            super();
            this.sourceTextComponent = sourceTextComponent;
            initialize();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveMessageItem) {
                saveMessageButton_actionPerformed();
            } else if (e.getSource() == copyItem) {
                // copy selected text
                StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
                clipboard.setContents(selection, this);
            }
        }
    
        private void initialize() {
            languageChanged(null);
    
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            clipboard = toolkit.getSystemClipboard();
    
            copyItem.addActionListener(this);
            saveMessageItem.addActionListener(this);
    
            add(copyItem);
            addSeparator();
            add(saveMessageItem);
            addSeparator();
            add(cancelItem);
        }
    
        public void languageChanged(LanguageEvent event) {
            copyItem.setText(language.getString("Copy"));
            saveMessageItem.setText(language.getString("Save message to disk"));
            cancelItem.setText(language.getString("Cancel"));
        }
    
        public void show(Component invoker, int x, int y) {
            if ((selectedMessage != null) && (selectedMessage.getContent() != null)) {
                if (sourceTextComponent.getSelectedText() != null) {
                    copyItem.setEnabled(true);
                } else {
                    copyItem.setEnabled(false);
                }
                super.show(invoker, x, y);
            }
        }
    
        public void lostOwnership(Clipboard tclipboard, Transferable contents) {
            // Nothing here
        }
    }

    public void setDownloadModel(DownloadModel table) {
        downloadModel = table;
    }
    
    public void close() {
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, propertyChangeListener);
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, propertyChangeListener);
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_STYLE, propertyChangeListener);
        Core.frostSettings.removePropertyChangeListener("messageBodyAA", propertyChangeListener);       
    }
    
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        messageTextArea.addKeyListener(l);
        filesTable.addKeyListener(l);
        boardsTable.addKeyListener(l);
    }
    public void removeKeyListener(KeyListener l) {
        super.removeKeyListener(l);
        messageTextArea.removeKeyListener(l);
        filesTable.removeKeyListener(l);
        boardsTable.removeKeyListener(l);
    }
}
