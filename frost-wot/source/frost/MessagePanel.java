/*
  MessagePanel.java / Frost
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
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.boards.*;
import frost.gui.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class MessagePanel extends JPanel {
    
    private MessageTable messageTable = null;
    private MessageTableModel messageTableModel = null;
    private MessageTextPane messageTextPane = null;
    private JScrollPane messageListScrollPane = null;

    MainFrame mainFrame;
    
    private class Listener
    extends MouseAdapter
    implements
        ActionListener,
        ListSelectionListener,
        TreeSelectionListener,
        TreeModelListener,
        LanguageListener,
        KeyListener
        {

        public Listener() {
            super();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == updateButton) {
                updateButton_actionPerformed(e);
            } else if (e.getSource() == newMessageButton) {
                newMessageButton_actionPerformed(e);
            } else if (e.getSource() == replyButton) {
                replyButton_actionPerformed(e);
            } else if (e.getSource() == saveMessageButton) {
                getMessageTextPane().saveMessageButton_actionPerformed();
            } else if (e.getSource() == nextUnreadMessageButton) {
                selectNextUnreadMessage();
            } else if (e.getSource() == setGoodButton) {
                setGoodButton_actionPerformed(e);
            } else if (e.getSource() == setBadButton) {
                setBadButton_actionPerformed(e);
            } else if (e.getSource() == setCheckButton) {
                setCheckButton_actionPerformed(e);
            } else if (e.getSource() == setObserveButton) {
                setObserveButton_actionPerformed(e);
            }
        }
    
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getComponent() == messageTable) {
                    showMessageTablePopupMenu(e);
                }
                //if leftbtn double click on message show this message
                //in popup window
            } else if(SwingUtilities.isLeftMouseButton(e)) {
                //accepting only mouse pressed event as double click,
                //overwise it will be triggered twice
                if(e.getID() == MouseEvent.MOUSE_PRESSED )
                    if(e.getClickCount() == 2 &&
                            e.getComponent() == messageTable )
                        showCurrentMessagePopupWindow();
            }
        }
    
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
    
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
    
        /**
         * Handles keystrokes for message table.
         * Currently implemented:
         * - 'n' for next message
         * - 'b' mark BAD
         * - 'c' mark CHECK
         * - 'o' mark OBSERVE
         * - 'g' mark GOOD
         */
        public void keyTyped(KeyEvent e){
            if( e == null ) {
                return;
            }
            if ( (e.getSource() == messageTable || 
                  e.getSource() == mainFrame.getTofTree() ) && 
                e.getKeyChar() == 'n') {
    
                selectNextUnreadMessage();
    
            } else if (e.getSource() == messageTable ) { 
                if( selectedMessage == null || 
                    selectedMessage.getSignatureStatus() != MessageObject.SIGNATURESTATUS_VERIFIED) 
                {
                    // change only for signed messages 
                    return;
                }
                if (e.getKeyChar() == 'b')  {
                    setMessageTrust(FrostIdentities.ENEMY);
                } else if (e.getKeyChar() == 'g') {
                    setMessageTrust(FrostIdentities.FRIEND);
                } else if (e.getKeyChar() == 'c') {
                    setMessageTrust(FrostIdentities.NEUTRAL);
                } else if (e.getKeyChar() == 'o') {
                    setMessageTrust(FrostIdentities.OBSERVE);
                }
            }
        }
    
        public void keyPressed(KeyEvent e){
            if(e.getSource() == messageTable && e.getKeyChar() == KeyEvent.VK_DELETE) {
                deleteSelectedMessage();
            }
        }
    
        public void keyReleased(KeyEvent e){
            //Nothing here
        }
    
        public void valueChanged(ListSelectionEvent e) {
            messageTable_itemSelected(e);
        }
    
        public void valueChanged(TreeSelectionEvent e) {
            boardsTree_actionPerformed(e);
        }
    
        public void treeNodesChanged(TreeModelEvent e) {
    //        boardsTreeNode_Changed(e);
        }
    
        public void treeNodesInserted(TreeModelEvent e) {
            //Nothing here
        }
    
        public void treeNodesRemoved(TreeModelEvent e) {
            //Nothing here
        }
    
        public void treeStructureChanged(TreeModelEvent e) {
            //Nothing here
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    }
    
    private class PopupMenuMessageTable
        extends JSkinnablePopupMenu
        implements ActionListener, LanguageListener {
    
        private JMenuItem cancelItem = new JMenuItem();
    
        private JMenuItem markAllMessagesReadItem = new JMenuItem();
        private JMenuItem markMessageUnreadItem = new JMenuItem();
        private JMenuItem setBadItem = new JMenuItem();
        private JMenuItem setCheckItem = new JMenuItem();
        private JMenuItem setGoodItem = new JMenuItem();
        private JMenuItem setObserveItem = new JMenuItem();
    
        private JMenuItem deleteItem = new JMenuItem();
        private JMenuItem undeleteItem = new JMenuItem();
    
        public PopupMenuMessageTable() {
            super();
            initialize();
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == markMessageUnreadItem) {
                markSelectedMessageUnread();
            } else if (e.getSource() == markAllMessagesReadItem) {
                Board board = mainFrame.getTofTreeModel().getSelectedNode();
                TOF.getInstance().setAllMessagesRead(board);
            } else if (e.getSource() == setGoodItem) {
                setMessageTrust(FrostIdentities.FRIEND);
            } else if (e.getSource() == setBadItem) {
                setMessageTrust(FrostIdentities.ENEMY);
            } else if (e.getSource() == setCheckItem) {
                setMessageTrust(FrostIdentities.NEUTRAL);
            } else if (e.getSource() == setObserveItem) {
                setMessageTrust(FrostIdentities.OBSERVE);
            } else if (e.getSource() == deleteItem) {
                deleteSelectedMessage();
            } else if (e.getSource() == undeleteItem) {
                undeleteSelectedMessage();
            }
        }
    
        private void initialize() {
            refreshLanguage();
    
            markMessageUnreadItem.addActionListener(this);
            markAllMessagesReadItem.addActionListener(this);
            setGoodItem.addActionListener(this);
            setBadItem.addActionListener(this);
            setCheckItem.addActionListener(this);
            setObserveItem.addActionListener(this);
            deleteItem.addActionListener(this);
            undeleteItem.addActionListener(this);
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    
        private void refreshLanguage() {
            markMessageUnreadItem.setText(language.getString("Mark message unread"));
            markAllMessagesReadItem.setText(language.getString("Mark ALL messages read"));
            setGoodItem.setText(language.getString("help user (sets to GOOD)"));
            setBadItem.setText(language.getString("block user (sets to BAD)"));
            setCheckItem.setText(language.getString("set to neutral (CHECK)"));
            setObserveItem.setText(language.getString("observe user (OBSERVE)"));
            deleteItem.setText(language.getString("Delete message"));
            undeleteItem.setText(language.getString("Undelete message"));
            cancelItem.setText(language.getString("Cancel"));
        }
    
        public void show(Component invoker, int x, int y) {
            if (!mainFrame.getTofTreeModel().getSelectedNode().isFolder()) {
                removeAll();
    
                if (messageTable.getSelectedRow() > -1) {
                    add(markMessageUnreadItem);
                }
                add(markAllMessagesReadItem);
                addSeparator();
                add(setGoodItem);
                add(setObserveItem);
                add(setCheckItem);
                add(setBadItem);
                setGoodItem.setEnabled(false);
                setObserveItem.setEnabled(false);
                setCheckItem.setEnabled(false);
                setBadItem.setEnabled(false);
    
                if (messageTable.getSelectedRow() > -1 && selectedMessage != null) {
                    if( identities.isMySelf(selectedMessage.getFrom()) ) {
                        // keep all off
                    } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xGOOD) {
                        setObserveItem.setEnabled(true);
                        setCheckItem.setEnabled(true);
                        setBadItem.setEnabled(true);
                    } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xCHECK) {
                        setObserveItem.setEnabled(true);
                        setGoodItem.setEnabled(true);
                        setBadItem.setEnabled(true);
                    } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xBAD) {
                        setObserveItem.setEnabled(true);
                        setGoodItem.setEnabled(true);
                        setCheckItem.setEnabled(true);
                    } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xOBSERVE) {
                        setGoodItem.setEnabled(true);
                        setCheckItem.setEnabled(true);
                        setBadItem.setEnabled(true);
                    } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xOLD) {
                        // keep all buttons disabled
                    } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xTAMPERED) {
                        // keep all buttons disabled
                    } else {
                        logger.warning("invalid message state : " + selectedMessage.getMsgStatus());
                    }
                }
    
                if (selectedMessage != null) {
                    addSeparator();
                    add(deleteItem);
                    add(undeleteItem);
                    deleteItem.setEnabled(false);
                    undeleteItem.setEnabled(false);
                    if(selectedMessage.isDeleted()) {
                        undeleteItem.setEnabled(true);
                    } else {
                        deleteItem.setEnabled(true);
                    }
                }
    
                addSeparator();
                add(cancelItem);
                // ATT: misuse of another menuitem displaying 'Cancel' ;)
                super.show(invoker, x, y);
            }
        }
    }
    
    private Logger logger = Logger.getLogger(MessagePanel.class.getName());
    
    private SettingsClass settings;
    private Language language;
    private FrostIdentities identities;
    private JFrame parentFrame;
    
    private boolean initialized = false;
    
    private Listener listener = new Listener();
    
    private FrostMessageObject selectedMessage;
    
    private PopupMenuMessageTable popupMenuMessageTable = null;
    
    private JButton setCheckButton =
        new JButton(new ImageIcon(getClass().getResource("/data/check.gif")));
    //private JButton downloadAttachmentsButton =
    //  new JButton(new ImageIcon(getClass().getResource("/data/attachment.gif")));
    //private JButton downloadBoardsButton =
    //  new JButton(new ImageIcon(getClass().getResource("/data/attachmentBoard.gif")));
    private JButton newMessageButton =
        new JButton(new ImageIcon(getClass().getResource("/data/newmessage.gif")));
    private JButton setBadButton =
        new JButton(new ImageIcon(getClass().getResource("/data/nottrust.gif")));
    private JButton setObserveButton =
        new JButton(new ImageIcon(getClass().getResource("/data/observe.gif")));
    private JButton replyButton =
        new JButton(new ImageIcon(getClass().getResource("/data/reply.gif")));
    private JButton saveMessageButton =
        new JButton(new ImageIcon(getClass().getResource("/data/save.gif")));
    protected JButton nextUnreadMessageButton = 
        new JButton(new ImageIcon(getClass().getResource("/data/nextunreadmessage.gif"))); // TODO!
    private JButton setGoodButton =
        new JButton(new ImageIcon(getClass().getResource("/data/trust.gif")));
    private JButton updateButton =
        new JButton(new ImageIcon(getClass().getResource("/data/update.gif")));
    
    private final String allMessagesCountPrefix = "Msg: "; // TODO: translate
    private JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix + "0");

    private final String newMessagesCountPrefix = "New: "; // TODO: translate
    private JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix + "0");

    public MessagePanel(SettingsClass settings, MainFrame mf) {
        super();
        this.settings = settings;
        mainFrame = mf;
        language = Language.getInstance();
    }
    
    private JToolBar getButtonsToolbar() {
        // configure buttons
        MiscToolkit toolkit = MiscToolkit.getInstance();
        toolkit.configureButton(newMessageButton, "New message", "/data/newmessage_rollover.gif", language);
        toolkit.configureButton(updateButton, "Update", "/data/update_rollover.gif", language);
        toolkit.configureButton(replyButton, "Reply", "/data/reply_rollover.gif", language);
    //  toolkit.configureButton(
    //      downloadAttachmentsButton,
    //      "Download attachment(s)",
    //      "/data/attachment_rollover.gif",
    //      language);
    //  toolkit.configureButton(
    //      downloadBoardsButton,
    //      "Add Board(s)",
    //      "/data/attachmentBoard_rollover.gif",
    //      language);
        toolkit.configureButton(saveMessageButton, "Save message", "/data/save_rollover.gif", language);
        toolkit.configureButton(nextUnreadMessageButton, "Next unread message", "/data/nextunreadmessage_rollover.gif", language);
        toolkit.configureButton(setGoodButton, "Trust", "/data/trust_rollover.gif", language);
        toolkit.configureButton(setBadButton, "Do not trust", "/data/nottrust_rollover.gif", language);
        toolkit.configureButton(setCheckButton, "Set to CHECK", "/data/check_rollover.gif", language);
        toolkit.configureButton(setObserveButton, "Set to OBSERVE", "/data/observe_rollover.gif", language);
    
        replyButton.setEnabled(false);
    //  downloadAttachmentsButton.setEnabled(false);
    //  downloadBoardsButton.setEnabled(false);
        saveMessageButton.setEnabled(false);
        setGoodButton.setEnabled(false);
        setCheckButton.setEnabled(false);
        setBadButton.setEnabled(false);
        setObserveButton.setEnabled(false);
    
        // build buttons panel
        JToolBar buttonsToolbar = new JToolBar();
        buttonsToolbar.setRollover(true);
        buttonsToolbar.setFloatable(false);
        Dimension blankSpace = new Dimension(3, 3);
    
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(nextUnreadMessageButton);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.addSeparator();
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(saveMessageButton);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.addSeparator();
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(newMessageButton);
        buttonsToolbar.add(replyButton);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.addSeparator();
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(updateButton);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.addSeparator();
    //  buttonsToolbar.add(Box.createRigidArea(blankSpace));
    //  buttonsToolbar.add(downloadAttachmentsButton);
    //  buttonsToolbar.add(downloadBoardsButton);
    //  buttonsToolbar.add(Box.createRigidArea(blankSpace));
    //  buttonsToolbar.addSeparator();
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(setGoodButton);
        buttonsToolbar.add(setObserveButton);
        buttonsToolbar.add(setCheckButton);
        buttonsToolbar.add(setBadButton);
    
        buttonsToolbar.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonsToolbar.add(Box.createHorizontalGlue());
        JLabel dummyLabel = new JLabel(allMessagesCountPrefix + "00000");
        dummyLabel.doLayout();
        Dimension labelSize = dummyLabel.getPreferredSize();
        allMessagesCountLabel.setPreferredSize(labelSize);
        allMessagesCountLabel.setMinimumSize(labelSize);
        newMessagesCountLabel.setPreferredSize(labelSize);
        newMessagesCountLabel.setMinimumSize(labelSize);
        buttonsToolbar.add(allMessagesCountLabel);
        buttonsToolbar.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonsToolbar.add(newMessagesCountLabel);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
    
        // listeners
        newMessageButton.addActionListener(listener);
        updateButton.addActionListener(listener);
        replyButton.addActionListener(listener);
    //  downloadAttachmentsButton.addActionListener(listener);
    //  downloadBoardsButton.addActionListener(listener);
        saveMessageButton.addActionListener(listener);
        nextUnreadMessageButton.addActionListener(listener);
        setGoodButton.addActionListener(listener);
        setCheckButton.addActionListener(listener);
        setBadButton.addActionListener(listener);
        setObserveButton.addActionListener(listener);
    
        return buttonsToolbar;
    }
    
    private PopupMenuMessageTable getPopupMenuMessageTable() {
        if (popupMenuMessageTable == null) {
            popupMenuMessageTable = new PopupMenuMessageTable();
            language.addLanguageListener(popupMenuMessageTable);
        }
        return popupMenuMessageTable;
    }
    
    public void initialize() {
        if (!initialized) {
            refreshLanguage();
            language.addLanguageListener(listener);
    
            // build messages list scroll pane
            messageTableModel = new MessageTableModel();
            language.addLanguageListener(messageTableModel);
            messageTable = new MessageTable(messageTableModel);
            messageTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
            messageTable.getSelectionModel().addListSelectionListener(listener);
            messageListScrollPane = new JScrollPane(messageTable);
            messageListScrollPane.setWheelScrollingEnabled(true);
            
            messageTextPane = new MessageTextPane(mainFrame);
            
            // load message table layout
            messageTable.loadLayout(settings);
    
            fontChanged();

            JSplitPane mainSplitPane =
                new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    messageListScrollPane,
                    messageTextPane);
            mainSplitPane.setDividerSize(10);
            mainSplitPane.setDividerLocation(160);
            mainSplitPane.setResizeWeight(0.5d);
            mainSplitPane.setMinimumSize(new Dimension(50, 20));

            // build main panel
            setLayout(new BorderLayout());
            add(getButtonsToolbar(), BorderLayout.NORTH);
            add(mainSplitPane, BorderLayout.CENTER);
    
            //listeners
            messageTable.addMouseListener(listener);
            messageTable.addKeyListener(listener);
    
            //other listeners
            mainFrame.getTofTree().addTreeSelectionListener(listener);
            mainFrame.getTofTree().addKeyListener(listener);
            mainFrame.getTofTreeModel().addTreeModelListener(listener); // TODO!

            // display welcome message if no boards are available
            boardsTree_actionPerformed(null); // set initial states

            initialized = true;
        }
    }
    
    private void fontChanged() {
        String fontName = settings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
        int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
        int fontSize = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
            logger.severe(
                "The selected font was not found in your system\n"
                    + "That selection will be changed to \"SansSerif\".");
            settings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, "SansSerif");
            font = new Font("SansSerif", fontStyle, fontSize);
        }
        messageTable.setFont(font);
    }
    
    private void messageTable_itemSelected(ListSelectionEvent e) {
        Board selectedBoard = mainFrame.getTofTreeModel().getSelectedNode();
        if (selectedBoard.isFolder()) {
            setGoodButton.setEnabled(false);
            setCheckButton.setEnabled(false);
            setBadButton.setEnabled(false);
            setObserveButton.setEnabled(false);
            replyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);
            return;
        }
        
        // board selected
    
        FrostMessageObject newSelectedMessage = TOF.getInstance().evalSelection(e, messageTable, selectedBoard);
        if( newSelectedMessage == selectedMessage ) {
            return; // user is reading a message, selection did NOT change
        } else {
            selectedMessage = newSelectedMessage;
        }
    
        if (selectedMessage != null) {
            MainFrame.displayNewMessageIcon(false);
    //      downloadAttachmentsButton.setEnabled(false);
    //      downloadBoardsButton.setEnabled(false);
    
            if (selectedBoard.isReadAccessBoard() == false) {
                replyButton.setEnabled(true);
            } else {
                replyButton.setEnabled(false);
            }
    
            if( identities.isMySelf(selectedMessage.getFrom()) ) {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(false);
                setBadButton.setEnabled(false);
                setObserveButton.setEnabled(false);
            } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xCHECK) {
                setCheckButton.setEnabled(false);
                setGoodButton.setEnabled(true);
                setBadButton.setEnabled(true);
                setObserveButton.setEnabled(true);
            } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xGOOD) {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(true);
                setBadButton.setEnabled(true);
                setObserveButton.setEnabled(true);
            } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xBAD) {
                setBadButton.setEnabled(false);
                setGoodButton.setEnabled(true);
                setCheckButton.setEnabled(true);
                setObserveButton.setEnabled(true);
            } else if (selectedMessage.getMsgStatus() == VerifyableMessageObject.xOBSERVE) {
                setObserveButton.setEnabled(false);
                setGoodButton.setEnabled(true);
                setCheckButton.setEnabled(true);
                setBadButton.setEnabled(true);
            } else {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(false);
                setBadButton.setEnabled(false);
                setObserveButton.setEnabled(false);
            }
            getMessageTextPane().update_messageSelected(selectedMessage);
            if (selectedMessage.getContent().length() > 0) {
                saveMessageButton.setEnabled(true);
            } else {
                saveMessageButton.setEnabled(false);
            }
    
        } else {
            // no msg selected
            getMessageTextPane().update_boardSelected();
            replyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);
            
            setGoodButton.setEnabled(false);
            setCheckButton.setEnabled(false);
            setBadButton.setEnabled(false);
            setObserveButton.setEnabled(false);
    //      downloadAttachmentsButton.setEnabled(false);
    //      downloadBoardsButton.setEnabled(false);
        }
    }
    
    private void newMessageButton_actionPerformed(ActionEvent e) {
        tofNewMessageButton_actionPerformed(e);
    }
    
    private void setBadButton_actionPerformed(ActionEvent e) {
        if (selectedMessage != null) {
            Identity id = identities.getIdentity(selectedMessage.getFrom());
            if( id == null ) {
                return;
            }
            if(id.getState() == FrostIdentities.FRIEND) {
                if (JOptionPane
                    .showConfirmDialog(
                        parentFrame,
                        "Are you sure you want to revoke trust to user " // TODO: translate
                            + selectedMessage.getFrom().substring(
                                0,
                                selectedMessage.getFrom().indexOf("@"))
                            + " ? \n If you choose yes, future messages from this user will be marked BAD",
                        "Revoke trust",
                        JOptionPane.YES_NO_OPTION)
                    != 0) {
                    return;
                }
            } else {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(false);
                setBadButton.setEnabled(false);
                setObserveButton.setEnabled(false);
                setMessageTrust(FrostIdentities.ENEMY);
            }
        }
    }
    
    private void setCheckButton_actionPerformed(ActionEvent e) {
        setGoodButton.setEnabled(false);
        setCheckButton.setEnabled(false);
        setBadButton.setEnabled(false);
        setObserveButton.setEnabled(false);
        setMessageTrust(FrostIdentities.NEUTRAL);
    }
    
    private void setObserveButton_actionPerformed(ActionEvent e) {
        setGoodButton.setEnabled(false);
        setCheckButton.setEnabled(false);
        setBadButton.setEnabled(false);
        setObserveButton.setEnabled(false);
        setMessageTrust(FrostIdentities.OBSERVE);
    }
    
    private void setGoodButton_actionPerformed(ActionEvent e) {
        if (selectedMessage != null) {
            Identity id = identities.getIdentity(selectedMessage.getFrom());
            if( id == null ) {
                return;
            }
            if(id.getState() == FrostIdentities.ENEMY) {
                if (JOptionPane
                    .showConfirmDialog(
                        parentFrame,
                        "Are you sure you want to grant trust to user " // TODO: translate
                            + selectedMessage.getFrom().substring(
                                0,
                                selectedMessage.getFrom().indexOf("@"))
                            + " ? \n If you choose yes, future messages from this user will be marked GOOD",
                        "Grant trust",
                        JOptionPane.YES_NO_OPTION)
                    != 0) {
                    return;
                }
            } else {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(false);
                setBadButton.setEnabled(false);
                setObserveButton.setEnabled(false);
                setMessageTrust(FrostIdentities.FRIEND);
            }
        }
    }
    
    private void refreshLanguage() {
        newMessageButton.setToolTipText(language.getString("New message"));
        replyButton.setToolTipText(language.getString("Reply"));
    //  downloadAttachmentsButton.setToolTipText(language.getString("Download attachment(s)"));
    //  downloadBoardsButton.setToolTipText(language.getString("Add Board(s)"));
        saveMessageButton.setToolTipText(language.getString("Save message"));
        nextUnreadMessageButton.setToolTipText(language.getString("Next unread message"));
        setGoodButton.setToolTipText(language.getString("Trust"));
        setBadButton.setToolTipText(language.getString("Do not trust"));
        setCheckButton.setToolTipText(language.getString("Set to CHECK"));
        setObserveButton.setToolTipText(language.getString("Set to OBSERVE"));
        updateButton.setToolTipText(language.getString("Update"));
    }
    
    private void replyButton_actionPerformed(ActionEvent e) {
        FrostMessageObject origMessage = selectedMessage;
        composeReply(origMessage, parentFrame);
    }
    
    public void composeReply(FrostMessageObject origMessage, Window parent) {
        String subject = origMessage.getSubject();
        if (subject.startsWith("Re:") == false) {
            subject = "Re: " + subject;
        }
        MessageFrame newMessageFrame = new MessageFrame(settings, parent, identities.getMyId(), mainFrame.getTofTree());
        if( origMessage.getRecipient() != null && 
            origMessage.getRecipient().equals( identities.getMyId().getUniqueName() ) ) 
        {
            // this message was for me, reply encrypted
            if( origMessage.getFromIdentity() == null ) {
                JOptionPane.showMessageDialog( parent,
                        "Can't reply encrypted, recipients public key is missing!", // TODO: translate
                        "ERROR",
                        JOptionPane.ERROR);
                return;                               
            }
            newMessageFrame.composeEncryptedReply(
                    mainFrame.getTofTreeModel().getSelectedNode(), 
                    identities.getMyId().getUniqueName(),
                    subject, 
                    origMessage.getContent(), 
                    origMessage.getFromIdentity());
    
        } else {
            newMessageFrame.composeReply(
                    mainFrame.getTofTreeModel().getSelectedNode(), 
                    settings.getValue("userName"),
                    subject, 
                    origMessage.getContent());
        }
    }
    
    private void showMessageTablePopupMenu(MouseEvent e) {
        getPopupMenuMessageTable().show(e.getComponent(), e.getX(), e.getY());
    }
    
    private void showCurrentMessagePopupWindow(){
        if( !isCorrectlySelectedMessage() ) {
            return;
        }
        MessageWindow messageWindow = new MessageWindow( mainFrame, selectedMessage, this.getSize() );
        messageWindow.setVisible(true);
    }
    
    private void updateButton_actionPerformed(ActionEvent e) {
        // restarts all finished threads if there are some long running threads
        if (mainFrame.getTofTree().isUpdateAllowed(mainFrame.getTofTreeModel().getSelectedNode())) {
            mainFrame.getTofTree().updateBoard(mainFrame.getTofTreeModel().getSelectedNode());
        }
    }
    
    private void boardsTree_actionPerformed(TreeSelectionEvent e) {
    
        if (((TreeNode) mainFrame.getTofTreeModel().getRoot()).getChildCount() == 0) {
            //There are no boards. //TODO: check if there are really no boards (folders count as children)
            getMessageTextPane().update_noBoardsFound();
        } else {
            //There are boards.
            Board node = (Board) mainFrame.getTofTree().getLastSelectedPathComponent();
            if (node != null) {
                if (!node.isFolder()) {
                    // node is a board
                    getMessageTextPane().update_boardSelected();
                    updateButton.setEnabled(true);
                    saveMessageButton.setEnabled(false);
                    replyButton.setEnabled(false);
    //              downloadAttachmentsButton.setEnabled(false);
    //              downloadBoardsButton.setEnabled(false);
                    if (node.isReadAccessBoard()) {
                        newMessageButton.setEnabled(false);
                    } else {
                        newMessageButton.setEnabled(true);
                    }
                } else {
                    // node is a folder
                    getMessageTextPane().update_folderSelected();
                    newMessageButton.setEnabled(false);
                    saveMessageButton.setEnabled(false);
                    updateButton.setEnabled(false);
                }
            }
        }
    }
    
    /**
     * returns true if message was correctly selected
     * @return
     */
    private boolean isCorrectlySelectedMessage(){
        int row = messageTable.getSelectedRow();
        if (row < 0
            || selectedMessage == null
            || mainFrame.getTofTreeModel().getSelectedNode() == null
            || mainFrame.getTofTreeModel().getSelectedNode().isFolder() == true)
            return false;
    
        return true;
    }
    
    private void deleteSelectedMessage() {
    
        if(! isCorrectlySelectedMessage() ) {
            return;
        }
    
        final FrostMessageObject targetMessage = selectedMessage;
    
        targetMessage.setDeleted(true);
    
        if ( ! settings.getBoolValue(SettingsClass.SHOW_DELETED_MESSAGES) ){
            // if we show deleted messages we don't need to remove them from the table
            messageTableModel.deleteRow(selectedMessage);
            updateMessageCountLabels(mainFrame.getTofTreeModel().getSelectedNode());
        } else {
            // needs repaint or the line which crosses the message isn't completely seen
            getMessageTableModel().updateRow(targetMessage);
        }
    
        Thread saver = new Thread() {
            public void run() {
                // save message, we must save the changed deleted state into the xml file
                targetMessage.save();
            };
        };
        saver.start();
    }
    
    private void undeleteSelectedMessage(){
        if(! isCorrectlySelectedMessage() )
                return;
    
        final FrostMessageObject targetMessage = selectedMessage;
        targetMessage.setDeleted(false);
        this.repaint();
    
        Thread saver = new Thread() {
            public void run() {
                // save message, we must save the changed deleted state into the xml file
                targetMessage.save();
            };
        };
        saver.start();
    }
    
    public void setIdentities(FrostIdentities identities) {
        this.identities = identities;
    }
    
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }
    
    public void startTruster( FrostMessageObject which, int trustState ) {
        identities.changeTrust(which.getFrom(), trustState);
    }
    
    /**
     * Marks current selected message unread
     */
    private void markSelectedMessageUnread() {
        int row = messageTable.getSelectedRow();
        if (row < 0
            || selectedMessage == null
            || mainFrame.getTofTreeModel().getSelectedNode() == null
            || mainFrame.getTofTreeModel().getSelectedNode().isFolder() == true)
            return;
    
        FrostMessageObject targetMessage = selectedMessage;
    
        messageTable.removeRowSelectionInterval(0, messageTable.getRowCount() - 1);
    
        targetMessage.setMessageNew(true);
        // let renderer check for new state
        getMessageTableModel().updateRow(targetMessage);
    
        mainFrame.getTofTreeModel().getSelectedNode().incNewMessageCount();
    
        updateMessageCountLabels(mainFrame.getTofTreeModel().getSelectedNode());
        mainFrame.updateTofTree(mainFrame.getTofTreeModel().getSelectedNode());
    }
    
    /**
     * Method that update the Msg and New counts for tof table
     * Expects that the boards messages are shown in table
     * @param board
     */
    public void updateMessageCountLabels(Board board) {
        if (board.isFolder() == true) {
            allMessagesCountLabel.setText("");
            newMessagesCountLabel.setText("");
            nextUnreadMessageButton.setEnabled(false);
        } else {
            DefaultTableModel model = (DefaultTableModel)getMessageTableModel();

            int allMessages = model.getRowCount();
            allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

            int newMessages = board.getNewMessageCount();
            newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
            if( newMessages > 0 ) {
                nextUnreadMessageButton.setEnabled(true);
            } else {
                nextUnreadMessageButton.setEnabled(false);
            }
        }
    }

    private void setMessageTrust(int newState) {
        int row = messageTable.getSelectedRow();
        if (row < 0 || selectedMessage == null) {
            return;
        }
        if( selectedMessage.getSignatureStatus() == VerifyableMessageObject.SIGNATURESTATUS_VERIFIED ) {
            identities.changeTrust(selectedMessage.getFrom(), newState);
        }
    }
    
    /**
     * tofNewMessageButton Action Listener (tof/ New Message)
     * @param e
     */
    private void tofNewMessageButton_actionPerformed(ActionEvent e) {
        MessageFrame newMessageFrame = new MessageFrame(
                                                settings, mainFrame,
                                                Core.getInstance().getIdentities().getMyId(),
                                                mainFrame.getTofTree());
        newMessageFrame.composeNewMessage(mainFrame.getTofTreeModel().getSelectedNode(), 
                                          settings.getValue("userName"),
                                          "No subject", 
                                          "");
    }

    /**
     * Search through all messages, find next unread message by date (earliest message in table).
     */
    public void selectNextUnreadMessage() {
        int nextMessage = -1;

        final MessageTableModel tableModel = getMessageTableModel();
        FrostMessageObject earliestMessage = null;
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            final FrostMessageObject message = (FrostMessageObject)tableModel.getRow(row);
            if (message.isMessageNew()) {
                if( earliestMessage == null ) {
                    earliestMessage = message;
                    nextMessage = row;
                } else {
                    if( earliestMessage.getDateAndTime().compareTo(message.getDateAndTime()) > 0 ) {
                        earliestMessage = message;
                        nextMessage = row;
                    }
                }
            }
        }

        if (nextMessage == -1) {
            // TODO: code to move to next board.
        } else {
            messageTable.addRowSelectionInterval(nextMessage, nextMessage);
            messageListScrollPane.getVerticalScrollBar().setValue(nextMessage * messageTable.getRowHeight());
        }
    }

    public MessageTableModel getMessageTableModel() {
        return messageTableModel;
    }
    public MessageTable getMessageTable() {
        return messageTable;
    }
    public MessageTextPane getMessageTextPane() {
        return messageTextPane;
    }
}
