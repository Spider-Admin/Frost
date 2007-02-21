/*
  MessageTextPane.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>
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
package frost.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

import frost.*;
import frost.boards.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.gui.model.*;
import frost.messages.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.textpane.*;
import frost.util.gui.translation.*;

public class MessageTextPane extends JPanel {

    private Language language = Language.getInstance();
    private Logger logger = Logger.getLogger(MessageTextPane.class.getName());

    private AntialiasedTextPane messageTextArea = null;
    private JSplitPane messageSplitPane = null;
    private JSplitPane attachmentsSplitPane = null;

    private AttachedFilesTableModel attachedFilesModel;
    private AttachedBoardTableModel attachedBoardsModel;
    private JTable filesTable = null;
    private JTable boardsTable = null;
    private JScrollPane filesTableScrollPane;
    private JScrollPane boardsTableScrollPane;
    private JScrollPane messageBodyScrollPane;

    private PopupMenuAttachmentBoard popupMenuAttachmentBoard = null;
    private PopupMenuAttachmentFile popupMenuAttachmentTable = null;
    private PopupMenuHyperLink popupMenuHyperLink = null;
    private PopupMenuTofText popupMenuTofText = null;

    private FrostMessageObject selectedMessage;

    private MainFrame mainFrame = MainFrame.getInstance();

    private Component parentFrame;

    private PropertyChangeListener propertyChangeListener;

    private SearchMessagesConfig searchMessagesConfig = null;
    private TextHighlighter textHighlighter = null;
    private static Color highlightColor = new Color(0x20, 0xFF, 0x20); // light green
    private static Color idLineHighlightColor = Color.LIGHT_GRAY;
    private TextHighlighter idLineTextHighlighter = new TextHighlighter(idLineHighlightColor);

    public MessageTextPane(Component parentFrame) {
        this(parentFrame, null);
    }

    public MessageTextPane(Component parentFrame, SearchMessagesConfig smc) {
        super();
        this.parentFrame = parentFrame;
        this.searchMessagesConfig = smc;
        initialize();
    }

    /**
     * Called if there are no boards in the board list.
     */
    public void update_noBoardsFound() {
        messageSplitPane.setBottomComponent(null);
        messageSplitPane.setDividerSize(0);
        setMessageText(language.getString("MessagePane.defaultText.welcomeMessage"));
    }

    /**
     * Called if a board is selected, but no message in message table.
     */
    public void update_boardSelected() {
        messageSplitPane.setBottomComponent(null);
        messageSplitPane.setDividerSize(0);
        setMessageText(language.getString("MessagePane.defaultText.noMessageSelected"));
    }

    /**
     * Called if a folder is selected.
     */
    public void update_folderSelected() {
        messageSplitPane.setBottomComponent(null);
        messageSplitPane.setDividerSize(0);
        setMessageText(language.getString("MessagePane.defaultText.noBoardSelected"));
    }

    private void setMessageText(String txt) {
        idLineTextHighlighter.removeHighlights(messageTextArea);
        messageTextArea.setText(txt);
    }

    /**
     * Find the offset in text where the caret must be positioned to
     * show the line at 'offset' on top of visible text.
     * Scans through the visible text and counts 'linesDown' visible lines (maybe wrapped!). 
     */
    private int calculateCaretPosition(JTextComponent c, int offset, int linesDown) {
        int len = c.getDocument().getLength();
        try {
            while (offset < len) {
                int end = Utilities.getRowEnd(c, offset);
                if (end < 0) {
                    break;
                }
    
                // Include the last character on the line
                end = Math.min(end+1, len);
    
                offset = end;
                linesDown--;
                if( linesDown == 0 ) {
                    return offset;
                }
            }
        } catch (BadLocationException e) {
        }
        return len;
    }

    /**
     * Called if a message is selected.
     */
    public void update_messageSelected(FrostMessageObject msg) {

        selectedMessage = msg;
        
        if( textHighlighter != null ) {
            textHighlighter.removeHighlights(messageTextArea);
        }

        List fileAttachments = selectedMessage.getAttachmentsOfType(Attachment.FILE);
        List boardAttachments = selectedMessage.getAttachmentsOfType(Attachment.BOARD);
        attachedFilesModel.setData(fileAttachments);
        attachedBoardsModel.setData(boardAttachments);

        int textViewHeight = positionDividers(fileAttachments.size(), boardAttachments.size());

        setMessageText(selectedMessage.getContent());

        messageBodyScrollPane.getVerticalScrollBar().setValueIsAdjusting(true);
        messageBodyScrollPane.getVerticalScrollBar().setValue(0);
        
        // for search messages don't scroll down to begin of text
        if( searchMessagesConfig == null ) {
            int pos = selectedMessage.getIdLinePos();
            int len = selectedMessage.getIdLineLen();
            if( pos > -1 && len > 10 ) {
                // highlite id line if there are valid infos abpout the idline in message
                idLineTextHighlighter.highlight(messageTextArea, pos, len, true);
            } else {
                // fallback
                pos = selectedMessage.getContent().lastIndexOf("----- "+selectedMessage.getFromName()+" ----- ");
            }

            if( pos >= 0 ) {
                // scroll to begin of reply
                int h = messageTextArea.getFontMetrics(messageTextArea.getFont()).getHeight();
                int s = textViewHeight; // messageBodyScrollPane.getViewport().getHeight();
                int v = s/h - 1; // how many lines are visible?
                
                pos = calculateCaretPosition(messageTextArea, pos, v);
                
                messageTextArea.getCaret().setDot(pos);
            } else {
                // scroll to end of message
                pos = selectedMessage.getContent().length();
                messageTextArea.getCaret().setDot(pos);
            }
        }

        messageBodyScrollPane.getVerticalScrollBar().setValueIsAdjusting(false);

        if( searchMessagesConfig != null && 
            searchMessagesConfig.content != null && 
            searchMessagesConfig.content.size() > 0 )
        {
            // highlight words in content that the user searched for
            if( textHighlighter == null ) {
                textHighlighter = new TextHighlighter(highlightColor, true);
            }
            textHighlighter.highlight(messageTextArea, searchMessagesConfig.content, false);
        }
    }
    
    private void initialize() {

        setLayout(new BorderLayout());

        MessageDecoder decoder = new MessageDecoder();
        decoder.setSmileyDecode(Core.frostSettings.getBoolValue(SettingsClass.SHOW_SMILEYS));
        decoder.setFreenetKeysDecode(Core.frostSettings.getBoolValue(SettingsClass.SHOW_KEYS_AS_HYPERLINKS));
        messageTextArea = new AntialiasedTextPane(decoder);
        messageTextArea.setEditable(false);
        messageTextArea.setDoubleBuffered(true);
        messageTextArea.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
//        messageTextArea.setLineWrap(true);
//        messageTextArea.setWrapStyleWord(true);
        
        messageTextArea.setAntiAliasEnabled(Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BODY_ANTIALIAS));

        messageBodyScrollPane = new JScrollPane(messageTextArea);
        messageBodyScrollPane.setWheelScrollingEnabled(true);

        // build attached files scroll pane
        attachedFilesModel = new AttachedFilesTableModel();
        filesTable = new JTable(attachedFilesModel);
        attachedFilesModel.configureTable(filesTable);
        filesTableScrollPane = new JScrollPane(filesTable);
        filesTableScrollPane.setWheelScrollingEnabled(true);

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
        boardsTableScrollPane.setWheelScrollingEnabled(true);

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
                } else if(e.getKeyChar() == 'n' && parentFrame == mainFrame ) {
                    mainFrame.getMessagePanel().selectNextUnreadMessage();
                } else if(e.getKeyChar() == KeyEvent.VK_DELETE && parentFrame == mainFrame ) {
                    mainFrame.getMessagePanel().deleteSelectedMessage();
                }
            }
        });
        messageTextArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if( !(evt instanceof MouseHyperlinkEvent) ) {
                    System.out.println("INTERNAL ERROR, hyperlinkevent is wrong object!");
                    return;
                }
                MouseHyperlinkEvent e = (MouseHyperlinkEvent) evt;
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    // user clicked on 'clickedKey', List 'allKeys' contains all keys
                    List allKeys = ((MessageDecoder)messageTextArea.getDecoder()).getHyperlinkedKeys();
                    String clickedKey = e.getDescription();
                    // show menu to download this/all keys and copy this/all to clipboard
                    showHyperLinkPopupMenu(
                            e, 
                            clickedKey, 
                            allKeys, 
                            e.getMouseEvent().getX(), 
                            e.getMouseEvent().getY());
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
                if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_ANTIALIAS)) {
                    messageTextArea.setAntiAliasEnabled(Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BODY_ANTIALIAS));
                } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_NAME)) {
                    fontChanged();
                } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_SIZE)) {
                    fontChanged();
                } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_STYLE)) {
                    fontChanged();
                } else if (evt.getPropertyName().equals(SettingsClass.SHOW_SMILEYS)) {
                    ((MessageDecoder)messageTextArea.getDecoder()).setSmileyDecode(Core.frostSettings.getBoolValue(SettingsClass.SHOW_SMILEYS));
                    if( selectedMessage != null ) {
                        update_messageSelected(selectedMessage);
                    } else {
                        setMessageText(messageTextArea.getText());
                    }
                } else if (evt.getPropertyName().equals(SettingsClass.SHOW_KEYS_AS_HYPERLINKS)) {
                    ((MessageDecoder)messageTextArea.getDecoder()).setFreenetKeysDecode(Core.frostSettings.getBoolValue(SettingsClass.SHOW_KEYS_AS_HYPERLINKS));
                    if( selectedMessage != null ) {
                        update_messageSelected(selectedMessage);
                    } else {
                        setMessageText(messageTextArea.getText());
                    }
                }
            }
        };

        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_STYLE, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_ANTIALIAS, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_SMILEYS, propertyChangeListener);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_KEYS_AS_HYPERLINKS, propertyChangeListener);
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

    private int positionDividers(int attachedFiles, int attachedBoards) {

        if (attachedFiles == 0 && attachedBoards == 0) {
            // Neither files nor boards
            messageSplitPane.setBottomComponent(null);
            messageSplitPane.setDividerSize(0);
            messageSplitPane.setDividerLocation(1.0);
            return messageSplitPane.getDividerLocation();
        }

        messageSplitPane.setDividerSize(3);
        messageSplitPane.setDividerLocation(0.75);

        if (attachedFiles != 0 && attachedBoards == 0) {
            // Only files
            attachmentsSplitPane.setTopComponent(null);
            attachmentsSplitPane.setBottomComponent(null);

            messageSplitPane.setBottomComponent(filesTableScrollPane);
            return messageSplitPane.getDividerLocation();
        }
        if (attachedFiles == 0 && attachedBoards != 0) {
            // Only boards
            attachmentsSplitPane.setTopComponent(null);
            attachmentsSplitPane.setBottomComponent(null);

            messageSplitPane.setBottomComponent(boardsTableScrollPane);
            return messageSplitPane.getDividerLocation();
        }
        if (attachedFiles != 0 && attachedBoards != 0) {
            // Both files and boards
            attachmentsSplitPane.setTopComponent(filesTableScrollPane);
            attachmentsSplitPane.setBottomComponent(boardsTableScrollPane);

            messageSplitPane.setBottomComponent(attachmentsSplitPane);
        }
        return messageSplitPane.getDividerLocation();
    }

    public void saveMessageButton_actionPerformed() {
        FileAccess.saveDialog(
            MainFrame.getInstance(),
            messageTextArea.getText(),
            Core.frostSettings.getValue(SettingsClass.DIR_LAST_USED),
            language.getString("MessagePane.messageText.saveDialog.title"));
    }
    
    private void addBoardsToKnownBoards() {
        int[] selectedRows = boardsTable.getSelectedRows();

        if (selectedRows.length == 0) {
            // add all rows
            boardsTable.selectAll();
            selectedRows = boardsTable.getSelectedRows();
            if (selectedRows.length == 0) {
                return;
            }
        }
        LinkedList boards = selectedMessage.getAttachmentsOfType(Attachment.BOARD);
        LinkedList<Board> addBoards = new LinkedList<Board>();
        for (int i = 0; i < selectedRows.length; i++) {
            BoardAttachment ba = (BoardAttachment) boards.get(selectedRows[i]);
            addBoards.add(ba.getBoardObj());
        }

        KnownBoardsManager.addNewKnownBoards(addBoards);
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

    private void showAttachedBoardsPopupMenu(MouseEvent e) {
        if (popupMenuAttachmentBoard == null) {
            popupMenuAttachmentBoard = new PopupMenuAttachmentBoard();
            language.addLanguageListener(popupMenuAttachmentBoard);
        }
        popupMenuAttachmentBoard.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showAttachedFilesPopupMenu(MouseEvent e) {
        if (popupMenuAttachmentTable == null) {
            popupMenuAttachmentTable = new PopupMenuAttachmentFile();
            language.addLanguageListener(popupMenuAttachmentTable);
        }
        popupMenuAttachmentTable.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showHyperLinkPopupMenu(HyperlinkEvent e, String clickedKey, List allKeys, int x, int y) {
        if (popupMenuHyperLink == null) {
            popupMenuHyperLink = new PopupMenuHyperLink();
            language.addLanguageListener(popupMenuHyperLink);
        }
        popupMenuHyperLink.setClickedKey(clickedKey);
        popupMenuHyperLink.setAllKeys(allKeys);
        
        popupMenuHyperLink.show(messageTextArea, x, y);
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
        private JMenuItem addBoardsToKnownBoards = new JMenuItem();

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
            } else if( e.getSource() == addBoardsToKnownBoards ) {
                addBoardsToKnownBoards();
            }
        }

        private void initialize() {
            languageChanged(null);

            saveBoardsItem.addActionListener(this);
            saveBoardsToFolderItem.addActionListener(this);
            addBoardsToKnownBoards.addActionListener(this);
        }

        public void languageChanged(LanguageEvent event) {
            saveBoardsItem.setText(language.getString("MessagePane.boardAttachmentTable.popupmenu.addBoards"));
            saveBoardsToFolderItem.setText(language.getString("MessagePane.boardAttachmentTable.popupmenu.addBoardsToFolder")+" ...");
            addBoardsToKnownBoards.setText(language.getString("MessagePane.boardAttachmentTable.popupmenu.addBoardsToKnownBoards"));
            cancelItem.setText(language.getString("Common.cancel"));
        }

        public void show(Component invoker, int x, int y) {
            removeAll();

            add(saveBoardsItem);
            add(saveBoardsToFolderItem);
            add(addBoardsToKnownBoards);
            addSeparator();
            add(cancelItem);

            super.show(invoker, x, y);
        }
    }

    private class PopupMenuAttachmentFile
        extends JSkinnablePopupMenu
        implements ActionListener, LanguageListener, ClipboardOwner {

        private JMenuItem cancelItem = new JMenuItem();
        private JMenuItem saveAttachmentItem = new JMenuItem();
        private JMenuItem saveAttachmentsItem = new JMenuItem();

        private JMenu copyToClipboardMenu = new JMenu();
        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();

        private Clipboard clipboard;

        private String fileMessage;
        private String keyMessage;
        private String bytesMessage;
        private String keyNotAvailableMessage;
        
        public PopupMenuAttachmentFile() throws HeadlessException {
            super();
            initialize();
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveAttachmentsItem || e.getSource() == saveAttachmentItem) {
                downloadAttachments();
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
        }

        private void initialize() {
            languageChanged(null);

            copyToClipboardMenu.add(copyKeysAndNamesItem);
            copyToClipboardMenu.add(copyKeysItem);
            copyToClipboardMenu.add(copyExtendedInfoItem);

            copyKeysAndNamesItem.addActionListener(this);
            if( FcpHandler.isFreenet05() ) {
                copyToClipboardMenu.add(copyKeysItem);
            }
            copyExtendedInfoItem.addActionListener(this);

            saveAttachmentsItem.addActionListener(this);
            saveAttachmentItem.addActionListener(this);
        }

        public void languageChanged(LanguageEvent event) {
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");

            saveAttachmentsItem.setText(language.getString("MessagePane.fileAttachmentTable.popupmenu.downloadAttachments"));
            saveAttachmentItem.setText(language.getString("MessagePane.fileAttachmentTable.popupmenu.downloadSelectedAttachment"));
            cancelItem.setText(language.getString("Common.cancel"));

            keyNotAvailableMessage = language.getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
            fileMessage = language.getString("Common.copyToClipBoard.extendedInfo.file")+" ";
            keyMessage = language.getString("Common.copyToClipBoard.extendedInfo.key")+" ";
            bytesMessage = language.getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";
        }

        public void show(Component invoker, int x, int y) {
            removeAll();

            add(copyToClipboardMenu);
            addSeparator();

            if (filesTable.getSelectedRow() == -1) {
                add(saveAttachmentsItem);
            } else {
                add(saveAttachmentItem);
            }
            addSeparator();
            add(cancelItem);

            super.show(invoker, x, y);
        }

        /**
         * Adds either the selected or all files from the attachmentTable to downloads table.
         */
        private void downloadAttachments() {
            Iterator it = getItems().iterator();
            while (it.hasNext()) {
                FileAttachment fa = (FileAttachment) it.next();
                FrostDownloadItem dlItem = new FrostDownloadItem(
                        fa.getFilename(), 
                        fa.getKey(), 
                        fa.getSize().longValue()); 
                getDownloadModel().addDownloadItem(dlItem);
            }
        }

        /**
         * Returns a list of all items to process, either selected ones or all.
         */
        private List<FileAttachment> getItems() {
            List<FileAttachment> items = null;
            int[] selectedRows = filesTable.getSelectedRows();
            if (selectedRows.length == 0) {
                // If no rows are selected, add all attachments to download table
                items = selectedMessage.getAttachmentsOfType(Attachment.FILE);
            } else {
                LinkedList attachments = selectedMessage.getAttachmentsOfType(Attachment.FILE);
                items = new LinkedList<FileAttachment>();
                for (int i = 0; i < selectedRows.length; i++) {
                    FileAttachment fo = (FileAttachment) attachments.get(selectedRows[i]);
                    items.add(fo);
                }
            }
            return items;
        }

        /**
         * This method copies the CHK keys and file names of the selected or all items to the clipboard.
         */
        private void copyKeysAndNames() {

            List items = getItems();
            if( items.size() == 0 ) {
                return;
            }

            StringBuilder textToCopy = new StringBuilder();
            for(Iterator i = items.iterator(); i.hasNext(); ) {
                FileAttachment fa = (FileAttachment) i.next();
                String key = fa.getKey();
                if (key == null) {
                    key = keyNotAvailableMessage;
                } else {
                    textToCopy.append(key);
                    if( key.startsWith("CHK@") ) {
                        // CHK
                        if( key.indexOf('/') < 0 ) {
                            textToCopy.append("/");
                            textToCopy.append(fa.getFilename());
                        }
                    } 
//                    else {
//                        // KSK, SSK or USK
//                        // don't append filename, key is enough
//                    }
                }
                textToCopy.append("\n");
            }
            StringSelection selection = new StringSelection(textToCopy.toString());
            getClipboard().setContents(selection, this);
        }

        /**
         * This method copies extended information about the selected items (if any) to
         * the clipboard. That information is composed of the filename, the key and
         * the size in bytes.
         */
        private void copyExtendedInfo() {
            List items = getItems();
            if( items.size() == 0 ) {
                return;
            }

            StringBuilder textToCopy = new StringBuilder();
            for(Iterator i = items.iterator(); i.hasNext(); ) {
                FileAttachment fa = (FileAttachment) i.next();

                String key = fa.getKey();
                if (key == null) {
                    key = keyNotAvailableMessage;
                }
                textToCopy.append(fileMessage);
                textToCopy.append(fa.getFilename() + "\n");
                textToCopy.append(keyMessage);
                textToCopy.append(key + "\n");
                textToCopy.append(bytesMessage);
                textToCopy.append(fa.getSize() + "\n\n");
            }
            //We remove the additional \n at the end
            String result = textToCopy.substring(0, textToCopy.length() - 1);

            StringSelection selection = new StringSelection(result);
            getClipboard().setContents(selection, this);
        }

        /**
         * This method copies the CHK keys of the selected items (if any) to the clipboard.
         */
        private void copyKeys() {
            List items = getItems();
            if( items.size() == 0 ) {
                return;
            }

            StringBuilder textToCopy = new StringBuilder();
            for(Iterator i = items.iterator(); i.hasNext(); ) {
                FileAttachment fa = (FileAttachment) i.next();
                String key = fa.getKey();
                if (key == null) {
                    key = keyNotAvailableMessage;
                }
                textToCopy.append(key);
                textToCopy.append("\n");
            }
            StringSelection selection = new StringSelection(textToCopy.toString());
            getClipboard().setContents(selection, this);
        }

        private Clipboard getClipboard() {
            if (clipboard == null) {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
            return clipboard;
        }

        public void lostOwnership(Clipboard tclipboard, Transferable contents) {
            // Nothing here
        }
    }

    private class PopupMenuHyperLink 
    extends JSkinnablePopupMenu
    implements ActionListener, LanguageListener, ClipboardOwner {

        private JMenuItem cancelItem = new JMenuItem();

        private JMenuItem copyKeyOnlyToClipboard = new JMenuItem();
        
        private JMenuItem copyFreesiteLinkToClipboard = new JMenuItem();
        
        private JMenuItem copyFileLinkToClipboard = new JMenuItem();
        private JMenuItem copyAllFileLinksToClipboard = new JMenuItem();

        private JMenuItem downloadFile = new JMenuItem();
        private JMenuItem downloadAllFiles = new JMenuItem();
        
        private Clipboard clipboard;
    
        private String clickedKey = null;
        private List allKeys = null;
        
        public PopupMenuHyperLink() throws HeadlessException {
            super();
            initialize();
        }
        
        public void setClickedKey(String s) {
            clickedKey = s;
        }
        public void setAllKeys(List l) {
            allKeys = l;
        }
        
        public void actionPerformed(ActionEvent e) {
            if( e.getSource() == copyKeyOnlyToClipboard ) {
                copyToClipboard(false);
            } else if( e.getSource() == copyFreesiteLinkToClipboard ) {
                copyToClipboard(false);
            } else if( e.getSource() == copyFileLinkToClipboard ) {
                copyToClipboard(false);
            } else if( e.getSource() == copyAllFileLinksToClipboard ) {
                copyToClipboard(true);
            } else if( e.getSource() == downloadFile ) {
                downloadItems(false);
            } else if( e.getSource() == downloadAllFiles ) {
                downloadItems(true);
            }
        }
    
        private void initialize() {
            languageChanged(null);
            
            copyKeyOnlyToClipboard.addActionListener(this);
            copyFreesiteLinkToClipboard.addActionListener(this);
            copyFileLinkToClipboard.addActionListener(this);
            copyAllFileLinksToClipboard.addActionListener(this);
            downloadFile.addActionListener(this);
            downloadAllFiles.addActionListener(this);
        }
    
        public void languageChanged(LanguageEvent event) {
            copyKeyOnlyToClipboard.setText(language.getString("MessagePane.hyperlink.popupmenu.copyKeyToClipboard"));
            copyFreesiteLinkToClipboard.setText(language.getString("MessagePane.hyperlink.popupmenu.copyFreesiteLinkToClipboard"));
            copyFileLinkToClipboard.setText(language.getString("MessagePane.hyperlink.popupmenu.copyFileKeyToClipboard"));
            copyAllFileLinksToClipboard.setText(language.getString("MessagePane.hyperlink.popupmenu.copyAllFileKeysToClipboard"));
            downloadFile.setText(language.getString("MessagePane.hyperlink.popupmenu.downloadFileKey"));
            downloadAllFiles.setText(language.getString("MessagePane.hyperlink.popupmenu.downloadAllFileKeys"));

            cancelItem.setText(language.getString("Common.cancel"));
        }
    
        public void show(Component invoker, int x, int y) {
            removeAll();
            
            // if clickedLink conatins no '/', its only a key without file, allow to copy this to clipboard only
            // if clickedLink ends with a '/' its a freesite link, allow to copy this to clipboard only
            // else the clickedLink is a filelink, allow to copy/download this link or ALL filelinks
            
            if( clickedKey.indexOf("/") < 0 || 
                !Character.isLetterOrDigit(clickedKey.charAt(clickedKey.length()-1)) ) 
            {
                // key only
                add(copyKeyOnlyToClipboard);
            } else if( clickedKey.endsWith("/") ) {
                // freesite link
                add(copyFreesiteLinkToClipboard);
            } else {
                // file key
                add(copyFileLinkToClipboard);
                if( allKeys.size() > 1 ) {
                    add(copyAllFileLinksToClipboard);
                }
                addSeparator();
                add(downloadFile);
                if( allKeys.size() > 1 ) {
                    add(downloadAllFiles);
                }
            }
    
            addSeparator();
            add(cancelItem);
    
            super.show(invoker, x, y);
        }
    
        /**
         * Adds either the selected or all files from the attachmentTable to downloads table.
         */
        private void downloadItems(boolean getAll) {

            List items = getItems(getAll);
            if( items == null ) {
                return;
            }

            Iterator it = items.iterator();
            while (it.hasNext()) {
                String item = (String)it.next();
                String key;
                // 0.5: remove filename from key; 0.7: use key/filename
                if( FcpHandler.isFreenet05() ) {
                    key = item.substring(0, item.indexOf("/") );
                } else {
                    key = item;
                }
                String name = item.substring(item.lastIndexOf("/")+1);
                FrostDownloadItem dlItem = new FrostDownloadItem(name, key); 
                getDownloadModel().addDownloadItem(dlItem);
            }
        }
        
        private List getItems(boolean getAll) {
            List items; 
            if( getAll ) {
                items = allKeys;
            } else {
                items = Collections.singletonList(clickedKey);
            }
            if( items == null || items.size() == 0 ) {
                return null;
            }
            return items;
        }
    
        /**
         * This method copies the CHK keys and file names of the selected or all items to the clipboard.
         */
        private void copyToClipboard(boolean getAll) {

            List items = getItems(getAll);
            if( items == null ) {
                return;
            }
            
            StringSelection selection;
            if( items.size() > 1 ) {
                StringBuilder textToCopy = new StringBuilder();
            	for(Iterator i = items.iterator(); i.hasNext(); ) {
            		String key = (String)i.next();
            		textToCopy.append(key).append("\n");
            	}
            	selection = new StringSelection(textToCopy.toString());
            } else {
            	// don't include a trailing \n if we only have one item
            	selection = new StringSelection((String)items.get(0));
            }
            getClipboard().setContents(selection, this);
        }
    
        private Clipboard getClipboard() {
            if (clipboard == null) {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
            return clipboard;
        }

        public void lostOwnership(Clipboard tclipboard, Transferable contents) {
            // Nothing here
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
            copyItem.setText(language.getString("MessagePane.messageText.popupmenu.copy"));
            saveMessageItem.setText(language.getString("MessagePane.messageText.popupmenu.saveMessageToDisk"));
            cancelItem.setText(language.getString("Common.cancel"));
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

    private DownloadModel getDownloadModel() {
        return FileTransferManager.inst().getDownloadManager().getModel();
    }

    public void close() {
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, propertyChangeListener);
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, propertyChangeListener);
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_STYLE, propertyChangeListener);
        Core.frostSettings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_ANTIALIAS, propertyChangeListener);

        if (popupMenuAttachmentBoard != null) {
            language.removeLanguageListener(popupMenuAttachmentBoard);
        }
        if (popupMenuAttachmentTable != null) {
            language.removeLanguageListener(popupMenuAttachmentTable);
        }
        if (popupMenuTofText != null) {
            language.removeLanguageListener(popupMenuTofText);
        }
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
