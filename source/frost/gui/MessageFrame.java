/*
MessageFrame.java / Frost
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
package frost.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.ext.*;
import frost.gui.model.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.textpane.*;
import frost.util.gui.translation.*;

public class MessageFrame extends JFrame {
    
    private static final Logger logger = Logger.getLogger(MessageFrame.class.getName());

    private Language language;

    private Listener listener = new Listener();

    private boolean initialized = false;

    private Window parentWindow;

    private Board board;
    private String repliedMsgId;
    private SettingsClass frostSettings;

    private MFAttachedBoardsTable boardsTable;
    private MFAttachedFilesTable filesTable;
    private MFAttachedBoardsTableModel boardsTableModel;
    private MFAttachedFilesTableModel filesTableModel;

    private JSplitPane messageSplitPane = null;
    private JSplitPane attachmentsSplitPane = null;
    private JScrollPane filesTableScrollPane;
    private JScrollPane boardsTableScrollPane;

    private JSkinnablePopupMenu attFilesPopupMenu;
    private JSkinnablePopupMenu attBoardsPopupMenu;
    private MessageBodyPopupMenu messageBodyPopupMenu;
    
    private JButton Bsend = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
    private JButton Bcancel = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
    private JButton BattachFile = new JButton(new ImageIcon(this.getClass().getResource("/data/attachment.gif")));
    private JButton BattachBoard= new JButton(new ImageIcon(MainFrame.class.getResource("/data/attachmentBoard.gif")));

    private JCheckBox sign = new JCheckBox();
    private JCheckBox encrypt = new JCheckBox();
    private JComboBox buddies;

    private JLabel Lboard = new JLabel();
    private JLabel Lfrom = new JLabel();
    private JLabel Lsubject = new JLabel();
    private JTextField TFboard = new JTextField(); // Board (To)
    private JTextField subjectTextField = new JTextField(); // Subject

    private AntialiasedTextArea messageTextArea = new AntialiasedTextArea(); // Text
    private ImmutableArea headerArea = null;
//    private TextHighlighter textHighlighter = null;
    private String oldSender = null;
    private String currentSignature = null;

    private FrostMessageObject repliedMessage = null;

    private JComboBox ownIdentitiesComboBox = null;
    
    private static int openInstanceCount = 0;

    public MessageFrame(SettingsClass newSettings, Window tparentWindow) {
        super();
        parentWindow = tparentWindow;
        this.language = Language.getInstance();
        frostSettings = newSettings;
        
        incOpenInstanceCount();

        String fontName = frostSettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
        int fontStyle = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
        int fontSize = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
        Font tofFont = new Font(fontName, fontStyle, fontSize);
        if (!tofFont.getFamily().equals(fontName)) {
            logger.severe("The selected font was not found in your system\n"
                    + "That selection will be changed to \"Monospaced\".");
            frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
            tofFont = new Font("Monospaced", fontStyle, fontSize);
        }
        messageTextArea.setFont(tofFont);
        messageTextArea.setAntiAliasEnabled(frostSettings.getBoolValue(SettingsClass.MESSAGE_BODY_ANTIALIAS));
        ImmutableAreasDocument messageDocument = new ImmutableAreasDocument();
        headerArea = new ImmutableArea(messageDocument);
        messageDocument.addImmutableArea(headerArea); // user must not change the header of the message
        messageTextArea.setDocument(messageDocument);
//        textHighlighter = new TextHighlighter(Color.LIGHT_GRAY);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                windowIsClosing(e);
            }
            public void windowClosed(WindowEvent e) {
                windowWasClosed(e);
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    private void windowIsClosing(WindowEvent e) {
        String title = language.getString("MessageFrame.discardMessage.title");
        String text = language.getString("MessageFrame.discardMessage.text");
        int answer = JOptionPane.showConfirmDialog(
                this, 
                text, 
                title, 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if( answer == JOptionPane.YES_OPTION ) {
            dispose();
        }
    }

    private void windowWasClosed(WindowEvent e) {
        decOpenInstanceCount();
    }

    private void attachBoards_actionPerformed(ActionEvent e) {

        // get and sort all boards
        List allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        if (allBoards.size() == 0) {
            return;
        }
        Collections.sort(allBoards);

        BoardsChooser chooser = new BoardsChooser(this, allBoards);
        chooser.setLocationRelativeTo(this);
        List chosenBoards = chooser.runDialog();
        if (chosenBoards == null || chosenBoards.size() == 0) { // nothing chosed or cancelled
            return;
        }

        for (int i = 0; i < chosenBoards.size(); i++) {
            Board chosedBoard = (Board) chosenBoards.get(i);

            String privKey = chosedBoard.getPrivateKey();

            if (privKey != null) {
                int answer =
                    JOptionPane.showConfirmDialog(this,
                        language.formatMessage("MessageFrame.attachBoard.sendPrivateKeyConfirmationDialog.body", chosedBoard.getName()),    
                        language.getString("MessageFrame.attachBoard.sendPrivateKeyConfirmationDialog.title"),
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    privKey = null; // don't provide privkey
                }
            }
            // build a new board because maybe privKey shouldn't be uploaded
            Board aNewBoard =
                new Board(chosedBoard.getName(), chosedBoard.getPublicKey(), privKey, chosedBoard.getDescription());
            MFAttachedBoard ab = new MFAttachedBoard(aNewBoard);
            boardsTableModel.addRow(ab);
        }
        positionDividers();
    }

    private void attachFile_actionPerformed(ActionEvent e) {
        String lastUsedDirectory = frostSettings.getValue(SettingsClass.DIR_LAST_USED);
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(language.getString("MessageFrame.fileChooser.title"));
        fc.setFileHidingEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(MessageFrame.this);
        if( returnVal == JFileChooser.APPROVE_OPTION ) {
            File[] selectedFiles = fc.getSelectedFiles();
            for( int i = 0; i < selectedFiles.length; i++ ) {
                // for convinience remember last used directory
                frostSettings.setValue(SettingsClass.DIR_LAST_USED, selectedFiles[i].getPath());

                // collect all choosed files + files in all choosed directories
                ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
                for (int j = 0; j < allFiles.size(); j++) {
                    File aFile = (File)allFiles.get(j);
                    if (aFile.isFile() && aFile.length() > 0) {
                        MFAttachedFile af = new MFAttachedFile( aFile );
                        filesTableModel.addRow( af );
                    }
                }
            }
        }
        positionDividers();
    }

    private void cancel_actionPerformed(ActionEvent e) {
        dispose();
    }
    
    /**
     * Finally called to start composing a message. Uses alternate editor if configured.
     */
    private void composeMessage(
            Board newBoard,
            String newSubject,
            String inReplyTo,
            String newText,
            boolean isReply,
            Identity recipient,
            LocalIdentity senderId,   // if given compose encrypted reply
            FrostMessageObject msg) { 
        
        repliedMessage = msg;
        
        if (isReply) {
            newText += "\n\n";
        }

        if (frostSettings.getBoolValue("useAltEdit")) {
            // build our transfer object that the parser will provide us in its callback
            TransferObject to = new TransferObject();
            to.newBoard = newBoard;
            to.newSubject = newSubject;
            to.inReplyTo = inReplyTo;
            to.newText = newText;
            to.isReply = isReply;
            to.recipient = recipient;
            to.senderId = senderId;
            // create a temporary editText that is show in alternate editor
            // the editor will return only new text to us
            DateTime now = new DateTime(DateTimeZone.UTC);
            String date = DateFun.FORMAT_DATE_EXT.print(now)
            + " - " 
            + DateFun.FORMAT_TIME_EXT.print(now);
            String fromLine = "----- (sender) ----- " + date + " -----";
            String editText = newText + fromLine + "\n\n";
            
            AltEdit ae = new AltEdit(newSubject, editText, MainFrame.getInstance(), to, this);
            ae.start();
        } else {
            // invoke frame directly, no alternate editor
            composeMessageContinued(newBoard, newSubject, inReplyTo, newText, null, isReply, recipient, senderId);
        }
    }
    
    public void altEditCallback(Object toObj, String newAltSubject, String newAltText) {
        TransferObject to = (TransferObject)toObj;
        if( newAltSubject == null ) {
            newAltSubject = to.newSubject; // use original subject
        }
        composeMessageContinued(
                to.newBoard, 
                newAltSubject, 
                to.inReplyTo, 
                to.newText, 
                newAltText, 
                to.isReply, 
                to.recipient,
                to.senderId);
    }

    /**
     * This method is either invoked by ComposeMessage OR by the callback of the AltEdit class.
     */
    private void composeMessageContinued(
        Board newBoard,
        String newSubject,
        String inReplyTo,
        String newText,
        String altEditText,
        boolean isReply,
        Identity recipient,       // if given compose encrypted reply
        LocalIdentity senderId)   // if given compose encrypted reply
    {
        headerArea.setEnabled(false);
        board = newBoard;
        repliedMsgId = inReplyTo; // maybe null

        String from;
        boolean isInitializedSigned;
        if( senderId != null ) {
            // encrypted reply!
            from = senderId.getUniqueName();
            isInitializedSigned = true;
        } else {
            // use remembered sender name, maybe per board
            String userName = Core.frostSettings.getValue("userName."+board.getBoardFilename());
            if( userName == null || userName.length() == 0 ) {
                userName = Core.frostSettings.getValue(SettingsClass.LAST_USED_FROMNAME);
            }
            if( Core.getIdentities().isMySelf(userName) ) {
                // isSigned
                from = userName;
                isInitializedSigned = true;
            } else if( userName.indexOf("@") > 0 ) {
                // invalid, only LocalIdentities are allowed to contain an @
                from = "Anonymous";
                isInitializedSigned = false;
            } else {
                from = userName;
                isInitializedSigned = false;
            }
        }
        oldSender = from;
        
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            initialize(newBoard, newSubject);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in composeMessage(...)", e);
        }
        
        sign.setEnabled(false);
        
        ImageIcon signedIcon = new ImageIcon(this.getClass().getResource("/data/signed.gif"));
        ImageIcon unsignedIcon = new ImageIcon(this.getClass().getResource("/data/unsigned.gif"));
        sign.setDisabledSelectedIcon(signedIcon);
        sign.setDisabledIcon(unsignedIcon);
        sign.setSelectedIcon(signedIcon);
        sign.setIcon(unsignedIcon);
        
        sign.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateSignToolTip();
            }
        });
        
        // maybe prepare to reply to an encrypted message
        if( recipient != null ) {
            // set correct sender identity
            for(int x=0; x < getOwnIdentitiesComboBox().getItemCount(); x++) {
                Object obj = getOwnIdentitiesComboBox().getItemAt(x);
                if( obj instanceof LocalIdentity ) {
                    LocalIdentity li = (LocalIdentity)obj;
                    if( senderId.getUniqueName().equals(li.getUniqueName()) ) {
                        getOwnIdentitiesComboBox().setSelectedIndex(x);
                        break;
                    }
                }
            }
            getOwnIdentitiesComboBox().setEnabled(false);
            // set and lock controls (after we set the identity, the itemlistener would reset the controls!)
            sign.setSelected(true);
            encrypt.setSelected(true);
            buddies.removeAllItems();
            buddies.addItem(recipient);
            buddies.setSelectedItem(recipient);
            // dont allow to disable signing/encryption
            encrypt.setEnabled(false);
            buddies.setEnabled(false);
        } else {
            if( isInitializedSigned ) {
                // set saved sender identity
                for(int x=0; x < getOwnIdentitiesComboBox().getItemCount(); x++) {
                    Object obj = getOwnIdentitiesComboBox().getItemAt(x);
                    if( obj instanceof LocalIdentity ) {
                        LocalIdentity li = (LocalIdentity)obj;
                        if( from.equals(li.getUniqueName()) ) {
                            getOwnIdentitiesComboBox().setSelectedIndex(x);
                            sign.setSelected(true);
                            getOwnIdentitiesComboBox().setEditable(false);
                            break;
                        }
                    }
                }
            } else {
                // initialized unsigned/anonymous
                getOwnIdentitiesComboBox().setSelectedIndex(0);
                getOwnIdentitiesComboBox().getEditor().setItem(from);
                sign.setSelected(false);
                getOwnIdentitiesComboBox().setEditable(true);
            }

            if( sign.isSelected() && buddies.getItemCount() > 0 ) {
                encrypt.setEnabled(true);
            } else {
                encrypt.setEnabled(false);
            }
            encrypt.setSelected(false);
            buddies.setEnabled(false);
        }

        updateSignToolTip();
        
        // prepare message text
        DateTime now = new DateTime(DateTimeZone.UTC);
        String date = DateFun.FORMAT_DATE_EXT.print(now)
                        + " - " 
                        + DateFun.FORMAT_TIME_EXT.print(now);
        String fromLine = "----- " + from + " ----- " + date + " -----";

        int headerAreaStart = newText.length();// begin of non-modifiable area
        newText += fromLine + "\n\n";
        int headerAreaEnd = newText.length() - 2; // end of non-modifiable area

        if( altEditText != null ) {
            newText += altEditText; // maybe append text entered in alternate editor
        }

        // later set cursor to this position in text
        int caretPos = newText.length();

        // set sig if msg is marked as signed
        currentSignature = null;
        if( sign.isSelected()  ) {
            // maybe append a signature
            LocalIdentity li = (LocalIdentity)getOwnIdentitiesComboBox().getSelectedItem();
            if( li.getSignature() != null ) {
                currentSignature = "\n-- \n" + li.getSignature();
                newText += currentSignature;
            }
        }

        messageTextArea.setText(newText);
        headerArea.setStartPos(headerAreaStart);
        headerArea.setEndPos(headerAreaEnd);
        headerArea.setEnabled(true);
        
//        textHighlighter.highlight(messageTextArea, headerAreaStart, headerAreaEnd-headerAreaStart, true);

        setVisible(true);

        // reset the splitpanes
        positionDividers();

        // Properly positions the caret (AKA cursor)
        messageTextArea.requestFocusInWindow();
        messageTextArea.getCaret().setDot(caretPos);
        messageTextArea.getCaret().setVisible(true);
    }

    public void composeNewMessage(Board newBoard, String newSubject, String newText) {
        composeMessage(newBoard, newSubject, null, newText, false, null, null, null);
    }

    public void composeReply(
            Board newBoard, 
            String newSubject,
            String inReplyTo,
            String newText,
            FrostMessageObject msg) {
        composeMessage(newBoard, newSubject, inReplyTo, newText, true, null, null, msg);
    }

    public void composeEncryptedReply(
            Board newBoard, 
            String newSubject, 
            String inReplyTo,
            String newText,
            Identity recipient,
            LocalIdentity senderId,
            FrostMessageObject msg) {
        composeMessage(newBoard, newSubject, inReplyTo, newText, true, recipient, senderId, msg);
    }

    public void dispose() {
        if (initialized) {
            language.removeLanguageListener(listener);
            initialized = false;
        }
        super.dispose();
    }

    private MessageBodyPopupMenu getMessageBodyPopupMenu() {
        if (messageBodyPopupMenu == null) {
            messageBodyPopupMenu = new MessageBodyPopupMenu(messageTextArea);
        }
        return messageBodyPopupMenu;
    }

    private void initialize(Board targetBoard, String subject) throws Exception {
        if (!initialized) {
            refreshLanguage();
            language.addLanguageListener(listener);

            ImageIcon frameIcon = new ImageIcon(getClass().getResource("/data/newmessage.gif"));
            setIconImage(frameIcon.getImage());
            setResizable(true);

            boardsTableModel = new MFAttachedBoardsTableModel();
            boardsTable = new MFAttachedBoardsTable(boardsTableModel);
            boardsTableScrollPane = new JScrollPane(boardsTable);
            boardsTableScrollPane.setWheelScrollingEnabled(true);
            boardsTable.addMouseListener(listener);

            filesTableModel = new MFAttachedFilesTableModel();
            filesTable = new MFAttachedFilesTable(filesTableModel);
            filesTableScrollPane = new JScrollPane(filesTable);
            filesTableScrollPane.setWheelScrollingEnabled(true);
            filesTable.addMouseListener(listener);

// FIXME: option to show own identities in list, or to hide them
            List<Identity> budList = Core.getIdentities().getAllGOODIdentities();
            Identity id = null;
            if( repliedMessage != null ) {
                id = repliedMessage.getFromIdentity();
            }
            if( budList.size() > 0 || id != null ) {
                Collections.sort( budList, new BuddyComparator() );
                if( id != null ) { 
                    if( id.isGOOD() == true ) {
                        budList.remove(id); // remove before put to top of list
                    }
                    // add id to top of list in case the user enables 'encrypt'
                    budList.add(0, id);
                }
                buddies = new JComboBox(new Vector<Identity>(budList));
                buddies.setSelectedItem(budList.get(0));
            } else {
                buddies = new JComboBox();
            }
            buddies.setMaximumSize(new Dimension(300, 25)); // dirty fix for overlength combobox on linux

            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(Bsend, "MessageFrame.toolbar.tooltip.sendMessage", "/data/send_rollover.gif", language);
            toolkit.configureButton(Bcancel, "Common.cancel", "/data/remove_rollover.gif", language);
            toolkit.configureButton(
                BattachFile,
                "MessageFrame.toolbar.tooltip.addFileAttachments",
                "/data/attachment_rollover.gif",
                language);
            toolkit.configureButton(
                BattachBoard,
                "MessageFrame.toolbar.tooltip.addBoardAttachments",
                "/data/attachmentBoard_rollover.gif",
                language);

            TFboard.setEditable(false);
            TFboard.setText(targetBoard.getName());
            
            new TextComponentClipboardMenu(TFboard, language);
            new TextComponentClipboardMenu((TextComboBoxEditor)getOwnIdentitiesComboBox().getEditor(), language);
            new TextComponentClipboardMenu(subjectTextField, language);
            subjectTextField.setText(subject);
            messageTextArea.setLineWrap(true);
            messageTextArea.setWrapStyleWord(true);
            messageTextArea.addMouseListener(listener);
            
            sign.setOpaque(false);
            encrypt.setOpaque(false);

            //------------------------------------------------------------------------
            // Actionlistener
            //------------------------------------------------------------------------
            Bsend.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    send_actionPerformed(e);
                }
            });
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel_actionPerformed(e);
                }
            });
            BattachFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    attachFile_actionPerformed(e);
                }
            });
            BattachBoard.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    attachBoards_actionPerformed(e);
                }
            });
            encrypt.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    encrypt_ActionPerformed(e);
                }
            });

            //------------------------------------------------------------------------
            // Append objects
            //------------------------------------------------------------------------
            JPanel panelMain = new JPanel(new BorderLayout()); // Main Panel
            JPanel panelTextfields = new JPanel(new BorderLayout()); // Textfields
            JPanel panelToolbar = new JPanel(new BorderLayout()); // Toolbar / Textfields
            JPanel panelLabels = new JPanel(new BorderLayout()); // Labels
            
            JToolBar panelButtons = new JToolBar(); // toolbar
            panelButtons.setRollover(true);
            panelButtons.setFloatable(false);

            JScrollPane bodyScrollPane = new JScrollPane(messageTextArea); // Textscrollpane
            bodyScrollPane.setWheelScrollingEnabled(true);
            bodyScrollPane.setMinimumSize(new Dimension(100, 50));

            panelLabels.add(Lboard, BorderLayout.NORTH);
            panelLabels.add(Lfrom, BorderLayout.CENTER);
            panelLabels.add(Lsubject, BorderLayout.SOUTH);

            panelTextfields.add(TFboard, BorderLayout.NORTH);
            panelTextfields.add(getOwnIdentitiesComboBox(), BorderLayout.CENTER);
            panelTextfields.add(subjectTextField, BorderLayout.SOUTH);

            panelButtons.add(Bsend);
            panelButtons.add(Bcancel);
            panelButtons.add(BattachFile);
            panelButtons.add(BattachBoard);
            panelButtons.add(sign);
            panelButtons.add(encrypt);
            panelButtons.add(buddies);
//            panelButtons.add(addAttachedFilesToUploadTable);

            //ScrollableBar panelButtonsScrollable = new ScrollableBar(panelButtons);

            JPanel dummyPanel = new JPanel(new BorderLayout());
            dummyPanel.add(panelLabels, BorderLayout.WEST);
            dummyPanel.add(panelTextfields, BorderLayout.CENTER);

//            panelToolbar.add(panelButtonsScrollable, BorderLayout.PAGE_START);
            panelToolbar.add(panelButtons, BorderLayout.PAGE_START);
            panelToolbar.add(dummyPanel, BorderLayout.CENTER);

            //Put everything together
            attachmentsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filesTableScrollPane,
                    boardsTableScrollPane);
            attachmentsSplitPane.setResizeWeight(0.5);
            attachmentsSplitPane.setDividerSize(3);
            attachmentsSplitPane.setDividerLocation(0.5);

            messageSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bodyScrollPane,
                    attachmentsSplitPane);
            messageSplitPane.setDividerSize(0);
            messageSplitPane.setDividerLocation(1.0);
            messageSplitPane.setResizeWeight(1.0);

            panelMain.add(panelToolbar, BorderLayout.NORTH);
            panelMain.add(messageSplitPane, BorderLayout.CENTER);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(panelMain, BorderLayout.CENTER);

            initPopupMenu();

            pack();

            // window is now packed to needed size. Check if packed width is smaller than
            // 75% of the parent frame and use the larger size.
            // pack is needed to ensure that all dialog elements are shown (was problem on linux).
            int width = getWidth();
            if( width < (int)(parentWindow.getWidth() * 0.75) ) {
                width = (int)(parentWindow.getWidth() * 0.75);
            }

            setSize( width, (int)(parentWindow.getHeight() * 0.75) ); // always set height to 75% of parent
            setLocationRelativeTo(parentWindow);

            initialized = true;
        }
    }

    protected void initPopupMenu() {
        attFilesPopupMenu = new JSkinnablePopupMenu();
        attBoardsPopupMenu = new JSkinnablePopupMenu();

        JMenuItem removeFiles = new JMenuItem(language.getString("MessageFrame.attachmentTables.popupmenu.remove"));
        JMenuItem removeBoards = new JMenuItem(language.getString("MessageFrame.attachmentTables.popupmenu.remove"));

        removeFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(filesTable);
            }
        });
        removeBoards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(boardsTable);
            }
        });

        attFilesPopupMenu.add( removeFiles );
        attBoardsPopupMenu.add( removeBoards );
    }

    private void positionDividers() {
        int attachedFiles = filesTableModel.getRowCount();
        int attachedBoards = boardsTableModel.getRowCount();
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
            messageSplitPane.setBottomComponent(filesTableScrollPane);
            return;
        }
        if (attachedFiles == 0 && attachedBoards != 0) {
            //Only boards
            messageSplitPane.setBottomComponent(boardsTableScrollPane);
            return;
        }
        if (attachedFiles != 0 && attachedBoards != 0) {
            //Both files and boards
            messageSplitPane.setBottomComponent(attachmentsSplitPane);
            attachmentsSplitPane.setTopComponent(filesTableScrollPane);
            attachmentsSplitPane.setBottomComponent(boardsTableScrollPane);
        }
    }

    private void refreshLanguage() {
        setTitle(language.getString("MessageFrame.createMessage.title"));

        Bsend.setToolTipText(language.getString("MessageFrame.toolbar.tooltip.sendMessage"));
        Bcancel.setToolTipText(language.getString("Common.cancel"));
        BattachFile.setToolTipText(language.getString("MessageFrame.toolbar.tooltip.addFileAttachments"));
        BattachBoard.setToolTipText(language.getString("MessageFrame.toolbar.tooltip.addBoardAttachments"));

        encrypt.setText(language.getString("MessageFrame.toolbar.encryptFor"));

        Lboard.setText(language.getString("MessageFrame.board") + ": ");
        Lfrom.setText(language.getString("MessageFrame.from") + ": ");
        Lsubject.setText(language.getString("MessageFrame.subject") + ": ");
        
        updateSignToolTip();
    }
    
    private void updateSignToolTip() {
        boolean isSelected = sign.isSelected();
        if( isSelected ) {
            sign.setToolTipText(language.getString("MessagePane.toolbar.tooltip.isSigned"));
        } else {
            sign.setToolTipText(language.getString("MessagePane.toolbar.tooltip.isUnsigned"));
        }
    }

    protected void removeSelectedItemsFromTable( JTable tbl ) {
        SortedTableModel m = (SortedTableModel)tbl.getModel();
        int[] sel = tbl.getSelectedRows();
        for(int x=sel.length-1; x>=0; x--)
        {
            m.removeRow(sel[x]);
        }
        positionDividers();
    }

    private void send_actionPerformed(ActionEvent e) {
        
        LocalIdentity senderId = null;
        String from;
        if( getOwnIdentitiesComboBox().getSelectedItem() instanceof LocalIdentity ) {
            senderId = (LocalIdentity)getOwnIdentitiesComboBox().getSelectedItem();
            from = senderId.getUniqueName();
        } else {
            from = getOwnIdentitiesComboBox().getEditor().getItem().toString();
        }
        
        String subject = subjectTextField.getText().trim();
        subjectTextField.setText(subject); // if a pbl occurs show the subject we checked
        String text = messageTextArea.getText().trim();

        if( subject.equals("No subject") ) {
            int n = JOptionPane.showConfirmDialog( this,
                                language.getString("MessageFrame.defaultSubjectWarning.text"),
                                language.getString("MessageFrame.defaultSubjectWarning.title"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
            if( n == JOptionPane.YES_OPTION ) {
                return;
            }
        }

        if( subject.length() == 0) {
            JOptionPane.showMessageDialog( this,
                                language.getString("MessageFrame.noSubjectError.text"),
                                language.getString("MessageFrame.noSubjectError.title"),
                                JOptionPane.ERROR);
            return;
        }
        if( from.length() == 0) {
            JOptionPane.showMessageDialog( this,
                                language.getString("MessageFrame.noSenderError.text"),
                                language.getString("MessageFrame.noSenderError.title"),
                                JOptionPane.ERROR);
            return;
        }
        int maxTextLength = (60*1024);
        int msgSize = text.length() + subject.length() + from.length() + ((repliedMsgId!=null)?repliedMsgId.length():0); 
        if( msgSize > maxTextLength ) {
            JOptionPane.showMessageDialog( this,
                    language.formatMessage("MessageFrame.textTooLargeError.text", 
                            Integer.toString(text.length()), 
                            Integer.toString(maxTextLength)),
                    language.getString("MessageFrame.textTooLargeError.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int idLinePos = headerArea.getStartPos();
        int idLineLen = headerArea.getEndPos() - headerArea.getStartPos();
        if( text.length() == headerArea.getEndPos() ) {
            JOptionPane.showMessageDialog( this,
                    language.getString("MessageFrame.noContentError.text"),
                    language.getString("MessageFrame.noContentError.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // for convinience set last used user
        if( from.indexOf("@") < 0 ) {
            // only save anonymous usernames
            frostSettings.setValue(SettingsClass.LAST_USED_FROMNAME, from);
        }
        frostSettings.setValue("userName."+board.getBoardFilename(), from);
        
        FrostUnsentMessageObject newMessage = new FrostUnsentMessageObject();
        newMessage.setMessageId(Mixed.createUniqueId()); // new message, create a new unique msg id
        newMessage.setInReplyTo(repliedMsgId);
        newMessage.setBoard(board);
        newMessage.setFromName(from);
        newMessage.setSubject(subject);
        newMessage.setContent(text);
        newMessage.setIdLinePos(idLinePos);
        newMessage.setIdLineLen(idLineLen);

        // MessageUploadThread will set date + time !

        // attach all files and boards the user chosed
        if( filesTableModel.getRowCount() > 0 ) {
            for(int x=0; x < filesTableModel.getRowCount(); x++) {
                MFAttachedFile af = (MFAttachedFile)filesTableModel.getRow(x);
                File aChosedFile = af.getFile();
                FileAttachment fa = new FileAttachment(aChosedFile);
                newMessage.addAttachment(fa);
            }
            newMessage.setHasFileAttachments(true);
        }
        if( boardsTableModel.getRowCount() > 0 ) {
            for(int x=0; x < boardsTableModel.getRowCount(); x++) {
                MFAttachedBoard ab = (MFAttachedBoard)boardsTableModel.getRow(x);
                Board aChosedBoard = ab.getBoardObject();
                BoardAttachment ba = new BoardAttachment(aChosedBoard);
                newMessage.addAttachment(ba);
            }
            newMessage.setHasBoardAttachments(true);
        }

        Identity recipient = null;
        if( encrypt.isSelected() ) {
            recipient = (Identity)buddies.getSelectedItem();
            if( recipient == null ) {
                JOptionPane.showMessageDialog( this,
                        language.getString("MessageFrame.encryptErrorNoRecipient.body"),
                        language.getString("MessageFrame.encryptErrorNoRecipient.title"),
                        JOptionPane.ERROR);
                return;
            }
            newMessage.setRecipientName(recipient.getUniqueName());
        }

        UnsentMessagesManager.addNewUnsentMessage(newMessage);

//        // zip the xml file and check for maximum size
//        File tmpFile = FileAccess.createTempFile("msgframe_", "_tmp");
//        tmpFile.deleteOnExit();
//        if( mo.saveToFile(tmpFile) == true ) {
//            File zipFile = new File(tmpFile.getPath() + ".zipped");
//            zipFile.delete(); // just in case it already exists
//            zipFile.deleteOnExit(); // so that it is deleted when Frost exits
//            FileAccess.writeZipFile(FileAccess.readByteArray(tmpFile), "entry", zipFile);
//            long zipLen = zipFile.length();
//            tmpFile.delete();
//            zipFile.delete();
//            if( zipLen > 30000 ) { // 30000 because data+metadata must be smaller than 32k
//                JOptionPane.showMessageDialog( this,
//                        "The zipped message is too large ("+zipLen+" bytes, "+30000+" allowed)! Remove some text.",
//                        "Message text too large!",
//                        JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//        } else {
//            JOptionPane.showMessageDialog( this,
//                    "Error verifying the resulting message size.",
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        // TODO: if user deletes the unsent msg then the replied state keeps (see below)
        //  We would have to set the replied state after the msg was successfully sent, because
        //  we can't remove the state later, maybe the msg was replied twice and we would remove
        //  the replied state from first reply...
        
        // set isReplied to replied message
        if( repliedMessage != null ) {
            if( repliedMessage.isReplied() == false ) {
                repliedMessage.setReplied(true);
                final FrostMessageObject saveMsg = repliedMessage;
                Thread saver = new Thread() {
                    public void run() {
                        // save the changed isreplied state into the database
                        try {
                            AppLayerDatabase.getMessageTable().updateMessage(saveMsg);
                        } catch (SQLException ex) {
                            logger.log(Level.SEVERE, "Error updating a message object", ex);
                        }
                    }
                };
                saver.start();
            }
        }

        setVisible(false);
        dispose();
    }

    private void senderChanged(LocalIdentity selectedId) {
        
        boolean isSigned;
        if( selectedId == null ) {
            isSigned = false;
        } else {
            isSigned = true;
        }
        
        sign.setSelected(isSigned);
        
        if (isSigned) {
            if( buddies.getItemCount() > 0 ) {
                encrypt.setEnabled(true);
                if( encrypt.isSelected() ) {
                    buddies.setEnabled(true);
                } else {
                    buddies.setEnabled(false);
                }
            }
            
            removeSignatureFromText(currentSignature); // remove signature if existing
            currentSignature = addSignatureToText(selectedId.getSignature()); // add new signature if not existing
        } else {
            encrypt.setSelected(false);
            encrypt.setEnabled(false);
            buddies.setEnabled(false);
            removeSignatureFromText(currentSignature); // remove signature if existing
            currentSignature = null;
        }
    }
    
    private String addSignatureToText(String sig) {
        if( sig == null ) {
            return null;
        }
        String newSig = "\n-- \n" + sig;
        if (!messageTextArea.getText().endsWith(newSig)) {
            try {
                messageTextArea.getDocument().insertString(messageTextArea.getText().length(), newSig, null);
            } catch (BadLocationException e1) {
                logger.log(Level.SEVERE, "Error while updating the signature ", e1);
            }
        }
        return newSig;
    }
    
    private void removeSignatureFromText(String sig) {
        if( sig == null ) {
            return;
        }
        if (messageTextArea.getText().endsWith(sig)) {
            try {
                messageTextArea.getDocument().remove(messageTextArea.getText().length()-sig.length(), sig.length());
            } catch (BadLocationException e1) {
                logger.log(Level.SEVERE, "Error while updating the signature ", e1);
            }
        }
    }

    private void encrypt_ActionPerformed(ActionEvent e) {
        if( encrypt.isSelected() ) {
            buddies.setEnabled(true);
        } else {
            buddies.setEnabled(false);
        }
    }

    protected void updateHeaderArea(String sender) {
        if( !headerArea.isEnabled() ) {
            return; // ignore updates
        }
        if( sender == null || oldSender == null || oldSender.equals(sender) ) {
            return;
        }
        try {
            // TODO: add grey background! highlighter mit headerArea um pos zu finden
            headerArea.setEnabled(false);
            messageTextArea.getDocument().remove(headerArea.getStartPos() + 6, oldSender.length());
            messageTextArea.getDocument().insertString(headerArea.getStartPos() + 6, sender, null);
            oldSender = sender;
            headerArea.setEnabled(true);
//            textHighlighter.highlight(messageTextArea, headerArea.getStartPos(), headerArea.getEndPos()-headerArea.getStartPos(), true);
        } catch (BadLocationException exception) {
            logger.log(Level.SEVERE, "Error while updating the message header", exception);
        }
//        String s= messageTextArea.getText().substring(headerArea.getStartPos(), headerArea.getEndPos());
//        System.out.println("DBG: "+headerArea.getStartPos()+" ; "+headerArea.getEndPos()+": '"+s+"'");
     
// DBG: 0 ; 77: '----- blubb2@xpDZ5ZfXK9wYiHB_hkVGRCwJl54 ----- 2006.10.13 - 18:20:12GMT -----'
// DBG: 39 ; 119: '----- wegdami t@plewLcBTHKmPwpWakJNpUdvWSR8 ----- 2006.10.13 - 18:20:12GMT -----'        
    }

    class TextComboBoxEditor extends JTextField implements ComboBoxEditor {
        boolean isSigned;
        public TextComboBoxEditor() {
            super();
        }
        public Component getEditorComponent() {
            return this;
        }
        public void setItem(Object arg0) {
            if( arg0 instanceof LocalIdentity ) {
                isSigned = true;
            } else {
                isSigned = false;
            }
            setText(arg0.toString());
        }
        public Object getItem() {
            return getText();
        }
        public boolean isSigned() {
            return isSigned;
        }
    }

    private JComboBox getOwnIdentitiesComboBox() {
        if( ownIdentitiesComboBox == null ) {
            ownIdentitiesComboBox = new JComboBox();
            ownIdentitiesComboBox.addItem("Anonymous");
            // sort own unique names
            TreeMap<String,LocalIdentity> sortedIds = new TreeMap<String,LocalIdentity>();
            for(Iterator i=Core.getIdentities().getLocalIdentities().iterator(); i.hasNext(); ) {
                LocalIdentity li = (LocalIdentity)i.next();
                sortedIds.put(li.getUniqueName(), li);
            }
            for(Iterator i=sortedIds.values().iterator(); i.hasNext(); ) {
                ownIdentitiesComboBox.addItem(i.next());
            }
            
            final TextComboBoxEditor editor = new TextComboBoxEditor();
            
            editor.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    updateHeaderArea2();
                }
                public void insertUpdate(DocumentEvent e) {
                    updateHeaderArea2();
                }
                public void removeUpdate(DocumentEvent e) {
                    updateHeaderArea2();
                }
                private void updateHeaderArea2() {
                    String sender = (String)getOwnIdentitiesComboBox().getEditor().getItem().toString();
                    updateHeaderArea(sender);
                }
            });

            AbstractDocument doc = (AbstractDocument) editor.getDocument();
            doc.setDocumentFilter(new DocumentFilter() {
                public void insertString(DocumentFilter.FilterBypass fb, int offset, String string,
                        AttributeSet attr) throws BadLocationException 
                {
                    if (((TextComboBoxEditor)getOwnIdentitiesComboBox().getEditor()).isSigned() == false ) {
                        string = string.replaceAll("@","");
                    }
                    super.insertString(fb, offset, string, attr);
                }
                public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string,
                        AttributeSet attrs) throws BadLocationException 
                {
                    if (((TextComboBoxEditor)getOwnIdentitiesComboBox().getEditor()).isSigned() == false ) {
                        string = string.replaceAll("@","");
                    }
                    super.replace(fb, offset, length, string, attrs);
                }
            });
            
            ownIdentitiesComboBox.setEditor(editor);
            
            ownIdentitiesComboBox.setEditable(true);

//            ownIdentitiesComboBox.getEditor().selectAll();
            ownIdentitiesComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if( e.getStateChange() == ItemEvent.DESELECTED ) {
                        return;
                    }
                    LocalIdentity selectedId = null;
                    if( ownIdentitiesComboBox.getSelectedIndex() == 0 ) {
                        ownIdentitiesComboBox.setEditable(true); // original anonymous
//                        ownIdentitiesComboBox.getEditor().selectAll();
                    } else if( ownIdentitiesComboBox.getSelectedIndex() < 0 ) {
                        ownIdentitiesComboBox.setEditable(true); // own value, anonymous
//                        ownIdentitiesComboBox.getEditor().selectAll();
                    } else {
                        ownIdentitiesComboBox.setEditable(false);
                        selectedId = (LocalIdentity) ownIdentitiesComboBox.getSelectedItem();
                    }
                    String sender = (String)getOwnIdentitiesComboBox().getSelectedItem().toString();
                    updateHeaderArea(sender);
                    senderChanged(selectedId);
                }
            });
        }
        return ownIdentitiesComboBox;
    }
    
    class BuddyComparator implements Comparator<Identity> {
        public int compare(Identity id1, Identity id2) {
            String s1 = id1.getUniqueName();
            String s2 = id2.getUniqueName();
            return s1.toLowerCase().compareTo( s2.toLowerCase() );
        }
    }
    
    private class Listener implements MouseListener, LanguageListener {
        protected void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == boardsTable) {
                    attBoardsPopupMenu.show(boardsTable, e.getX(), e.getY());
                }
                if (e.getSource() == filesTable) {
                    attFilesPopupMenu.show(filesTable, e.getX(), e.getY());
                }
                if (e.getSource() == messageTextArea) {
                    getMessageBodyPopupMenu().show(messageTextArea, e.getX(), e.getY());
                }
            }
        }
        public void mouseClicked(MouseEvent event) {}
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    }

    private class MessageBodyPopupMenu
        extends JSkinnablePopupMenu
        implements ActionListener, ClipboardOwner {

        private Clipboard clipboard;

        private JTextComponent sourceTextComponent;

        private JMenuItem cutItem = new JMenuItem();
        private JMenuItem copyItem = new JMenuItem();
        private JMenuItem pasteItem = new JMenuItem();
        private JMenuItem cancelItem = new JMenuItem();

        public MessageBodyPopupMenu(JTextComponent sourceTextComponent) {
            super();
            this.sourceTextComponent = sourceTextComponent;
            initialize();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cutItem) {
                cutSelectedText();
            }
            if (e.getSource() == copyItem) {
                copySelectedText();
            }
            if (e.getSource() == pasteItem) {
                pasteText();
            }
        }

        private void copySelectedText() {
            StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
            clipboard.setContents(selection, this);
        }

        private void cutSelectedText() {
            StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
            clipboard.setContents(selection, this);

            int start = sourceTextComponent.getSelectionStart();
            int end = sourceTextComponent.getSelectionEnd();
            try {
                sourceTextComponent.getDocument().remove(start, end - start);
            } catch (BadLocationException ble) {
                logger.log(Level.SEVERE, "Problem while cutting text.", ble);
            }
        }

        private void pasteText() {
            Transferable clipboardContent = clipboard.getContents(this);
            try {
                String text = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);

                Caret caret = sourceTextComponent.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());

                Document document = sourceTextComponent.getDocument();

                if (document instanceof PlainDocument) {
                    ((PlainDocument) document).replace(p0, p1 - p0, text, null);
                } else {
                    if (p0 != p1) {
                        document.remove(p0, p1 - p0);
                    }
                    document.insertString(p0, text, null);
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Problem while pasting text.", ioe);
            } catch (UnsupportedFlavorException ufe) {
                logger.log(Level.SEVERE, "Problem while pasting text.", ufe);
            } catch (BadLocationException ble) {
                logger.log(Level.SEVERE, "Problem while pasting text.", ble);
            }
        }

        private void initialize() {
            refreshLanguage();

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            clipboard = toolkit.getSystemClipboard();

            cutItem.addActionListener(this);
            copyItem.addActionListener(this);
            pasteItem.addActionListener(this);

            add(cutItem);
            add(copyItem);
            add(pasteItem);
            addSeparator();
            add(cancelItem);
        }

        private void refreshLanguage() {
            cutItem.setText(language.getString("Common.cut"));
            copyItem.setText(language.getString("Common.copy"));
            pasteItem.setText(language.getString("Common.paste"));
            cancelItem.setText(language.getString("Common.cancel"));
        }

        public void lostOwnership(Clipboard nclipboard, Transferable contents) {}

        public void show(Component invoker, int x, int y) {
            if (sourceTextComponent.getSelectedText() != null) {
                cutItem.setEnabled(true);
                copyItem.setEnabled(true);
            } else {
                cutItem.setEnabled(false);
                copyItem.setEnabled(false);
            }
            Transferable clipboardContent = clipboard.getContents(this);
            if ((clipboardContent != null) &&
                    (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
                pasteItem.setEnabled(true);
            } else {
                pasteItem.setEnabled(false);
            }
            super.show(invoker, x, y);
        }
    }

    private class MFAttachedBoard implements TableMember {
        Board aBoard;

        public MFAttachedBoard(Board ab) {
            aBoard = ab;
        }

        public int compareTo( TableMember anOther, int tableColumIndex ) {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }

        public Board getBoardObject() {
            return aBoard;
        }

        public Object getValueAt(int column) {
            switch (column) {
                case 0 : return aBoard.getName();
                case 1 : return (aBoard.getPublicKey() == null) ? "N/A" : aBoard.getPublicKey();
                case 2 : return (aBoard.getPrivateKey() == null) ? "N/A" : aBoard.getPrivateKey();
                case 3 : return (aBoard.getDescription() == null) ? "N/A" : aBoard.getDescription();
            }
            return "*ERR*";
        }
    }

    private class MFAttachedBoardsTable extends SortedTable {
        public MFAttachedBoardsTable(MFAttachedBoardsTableModel m) {
            super(m);
            // set column sizes
            int[] widths = {250, 80, 80};
            for (int i = 0; i < widths.length; i++) {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }

    private class MFAttachedBoardsTableModel extends SortedTableModel {
        protected final Class columnClasses[] = {
            String.class,
            String.class,
            String.class,
            String.class
        };
        protected final String columnNames[] = {
            language.getString("MessageFrame.boardAttachmentTable.boardname"),
            language.getString("MessageFrame.boardAttachmentTable.publicKey"),
            language.getString("MessageFrame.boardAttachmentTable.privateKey"),
            language.getString("MessageFrame.boardAttachmentTable.description")
        };

        public MFAttachedBoardsTableModel() {
            super();
        }
        public Class getColumnClass(int column) {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public int getColumnCount() {
            return columnNames.length;
        }
        public String getColumnName(int column) {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }
        public boolean isCellEditable(int row, int col) {
            return false;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }

    private class MFAttachedFile implements TableMember {
        File aFile;
        public MFAttachedFile(File af) {
            aFile = af;
        }
        public int compareTo( TableMember anOther, int tableColumIndex ) {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        public File getFile() {
            return aFile;
        }
        public Object getValueAt(int column)  {
            switch(column) {
                case 0: return aFile.getName();
                case 1: return Long.toString(aFile.length());
            }
            return "*ERR*";
        }
    }

    private class MFAttachedFilesTable extends SortedTable {
        public MFAttachedFilesTable(MFAttachedFilesTableModel m) {
            super(m);
            // set column sizes
            int[] widths = {250, 80};
            for (int i = 0; i < widths.length; i++) {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }

    private class MFAttachedFilesTableModel extends SortedTableModel {
        protected final Class columnClasses[] = {
            String.class,
            String.class
        };
        protected final String columnNames[] = {
            language.getString("MessageFrame.fileAttachmentTable.filename"),
            language.getString("MessageFrame.fileAttachmentTable.size")
        };
        public MFAttachedFilesTableModel() {
            super();
        }
        public Class getColumnClass(int column) {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public int getColumnCount() {
            return columnNames.length;
        }
        public String getColumnName(int column) {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }
        public boolean isCellEditable(int row, int col) {
            return false;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }
    
    private class TransferObject {
        public Board newBoard;
        public String newSubject;
        public String inReplyTo;
        public String newText;
        public boolean isReply;
        public Identity recipient = null;;
        public LocalIdentity senderId = null;
    }
    
    public static synchronized int getOpenInstanceCount() {
        return openInstanceCount;
    }
    private static synchronized void incOpenInstanceCount() {
        openInstanceCount++;
    }
    private static synchronized void decOpenInstanceCount() {
        if( openInstanceCount > 0 ) { // paranoia
            openInstanceCount--;
        }
    }
}
