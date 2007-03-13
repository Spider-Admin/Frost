/*
 MessagePanel.java / Frost
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
import java.awt.event.*;
import java.beans.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import frost.*;
import frost.boards.*;
import frost.gui.messagetreetable.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class MessagePanel extends JPanel implements PropertyChangeListener {

    private MessageTreeTable messageTable = null;
    private MessageTextPane messageTextPane = null;
    private JScrollPane messageListScrollPane = null;
    private JSplitPane msgTableAndMsgTextSplitpane = null;
    private JLabel subjectLabel = new JLabel();
    private JLabel subjectTextLabel = new JLabel();

    MainFrame mainFrame;

    private class Listener
    extends MouseAdapter
    implements
        ActionListener,
        ListSelectionListener,
        TreeSelectionListener,
        LanguageListener
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
            } else if (e.getSource() == toggleShowUnreadOnly) {
                toggleShowUnreadOnly_actionPerformed(e);
            } else if (e.getSource() == toggleShowThreads) {
                toggleShowThreads_actionPerformed(e);
            } else if (e.getSource() == toggleShowSmileys) {
                toggleShowSmileys_actionPerformed(e);
            } else if (e.getSource() == toggleShowHyperlinks) {
                toggleShowHyperlinks_actionPerformed(e);
            }
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getComponent() == messageTable) {
                    showMessageTablePopupMenu(e);
                } else if( e.getComponent() == subjectTextLabel ) {
                    getPopupMenuSubjectText().show(e.getComponent(), e.getX(), e.getY());
                }
                // if leftbtn double click on message show this message in a new window
            } else if(SwingUtilities.isLeftMouseButton(e)) {
                // accepting only mouse pressed event as double click, otherwise it will be triggered twice
                if(e.getID() == MouseEvent.MOUSE_PRESSED ) {
                    if(e.getClickCount() == 2 && e.getComponent() == messageTable ) {
                        showCurrentMessagePopupWindow();
                    } else if(e.getClickCount() == 1 && e.getComponent() == messageTable ) {
                        // 'edit' the icon columns, toggle state flagged/starred
                        int row = messageTable.rowAtPoint(e.getPoint());
                        int col = messageTable.columnAtPoint(e.getPoint());
                        if( row > -1 && col > -1 ) {
                            int modelCol = messageTable.getColumnModel().getColumn(col).getModelIndex();
                            editIconColumn(row, modelCol);
                        }
                    }
                }
            }
        }
        
        /**
         * Left click onto a row/col occurred. Check if the click was over an icon column and maybe toggle
         * its state (starred/flagged).
         */
        protected void editIconColumn(int row, int modelCol) {
            if( modelCol > 1 ) {
                return; // icon columns are at 0,1
            }
            final FrostMessageObject message = (FrostMessageObject)getMessageTableModel().getRow(row);
            if( message == null || message.isDummy() ) {
                return;
            }
            if( modelCol == 0 ) {
                message.setFlagged( !message.isFlagged() );
                getMessageTableModel().fireTableCellUpdated(row, modelCol);
            } else if( modelCol == 1 ) {
                message.setStarred( !message.isStarred() );
                getMessageTableModel().fireTableCellUpdated(row, modelCol);
            }
            
            // determine thread root msg of this msg
            FrostMessageObject threadRootMsg = message.getThreadRootMessage();
            
            // update thread root to update the marker border
            if( threadRootMsg != message && threadRootMsg != null ) {
                getMessageTreeModel().nodeChanged(threadRootMsg);
            }
            
            // update flagged/starred indicators in board tree
            boolean hasStarredWork = false;
            boolean hasFlaggedWork = false;
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)message.getRoot(); 
            for(Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                FrostMessageObject mo = (FrostMessageObject)e.nextElement();
                if( !hasStarredWork && mo.isStarred() ) {
                    hasStarredWork = true;
                }
                if( !hasFlaggedWork && mo.isFlagged() ) {
                    hasFlaggedWork = true;
                }
                if( hasFlaggedWork && hasStarredWork ) {
                    break; // finished
                }
            }
            message.getBoard().hasFlaggedMessages(hasFlaggedWork);
            message.getBoard().hasStarredMessages(hasStarredWork);
            MainFrame.getInstance().updateTofTree(message.getBoard());

            Thread saver = new Thread() {
                public void run() {
                    try {
                        AppLayerDatabase.getMessageTable().updateMessage(message);
                    } catch (SQLException ex) {
                        logger.log(Level.SEVERE, "Error updating a message object", ex);
                    }
                }
            };
            saver.start();
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void valueChanged(ListSelectionEvent e) {
            messageTable_itemSelected(e);
        }

        public void valueChanged(TreeSelectionEvent e) {
            boardsTree_actionPerformed(e);
        }

        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    }

    private class PopupMenuSubjectText
        extends JSkinnablePopupMenu
        implements ActionListener, LanguageListener 
    {
        JMenuItem copySubjectText = new JMenuItem();

        public PopupMenuSubjectText() {
            super();
            initialize();
        }

        private void initialize() {
            refreshLanguage();
            copySubjectText.addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == copySubjectText) {
                CopyToClipboard.copyText(subjectTextLabel.getText());
            }
        }

        public void show(Component invoker, int x, int y) {
            removeAll();
            add(copySubjectText);
            super.show(invoker, x, y);
        }

        private void refreshLanguage() {
            copySubjectText.setText(language.getString("MessagePane.subjectText.popupmenu.copySubjectText"));
        }
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    }

    private class PopupMenuMessageTable
        extends JSkinnablePopupMenu
        implements ActionListener, LanguageListener {

        private JMenuItem markAllMessagesReadItem = new JMenuItem();
        private JMenuItem markThreadReadItem = new JMenuItem();
        private JMenuItem markMessageUnreadItem = new JMenuItem();
        private JMenuItem setBadItem = new JMenuItem();
        private JMenuItem setCheckItem = new JMenuItem();
        private JMenuItem setGoodItem = new JMenuItem();
        private JMenuItem setObserveItem = new JMenuItem();

        private JMenuItem deleteItem = new JMenuItem();
        private JMenuItem undeleteItem = new JMenuItem();
        
        private JMenuItem expandAllItem = new JMenuItem();
        private JMenuItem collapseAllItem = new JMenuItem();
        
        private JMenuItem expandThreadItem = new JMenuItem();
        private JMenuItem collapseThreadItem = new JMenuItem();

        public PopupMenuMessageTable() {
            super();
            initialize();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == markMessageUnreadItem) {
                markSelectedMessageUnread();
            } else if (e.getSource() == markAllMessagesReadItem) {
                TOF.getInstance().markAllMessagesRead(mainFrame.getTofTreeModel().getSelectedNode());
            } else if (e.getSource() == markThreadReadItem) {
                markThreadRead();
            } else if (e.getSource() == deleteItem) {
                deleteSelectedMessage();
            } else if (e.getSource() == undeleteItem) {
                undeleteSelectedMessage();
            } else if (e.getSource() == expandAllItem) {
                getMessageTable().expandAll(true);
            } else if (e.getSource() == collapseAllItem) {
                getMessageTable().expandAll(false);
            } else if (e.getSource() == expandThreadItem) {
                getMessageTable().expandThread(true, selectedMessage);
            } else if (e.getSource() == collapseThreadItem) {
                getMessageTable().expandThread(false, selectedMessage);
            } else if (e.getSource() == setGoodItem) {
                setGoodButton_actionPerformed(e);
            } else if (e.getSource() == setBadItem) {
                setBadButton_actionPerformed(e);
            } else if (e.getSource() == setCheckItem) {
                setCheckButton_actionPerformed(e);
            } else if (e.getSource() == setObserveItem) {
                setObserveButton_actionPerformed(e);
            }
        }

        private void initialize() {
            refreshLanguage();

            markMessageUnreadItem.addActionListener(this);
            markAllMessagesReadItem.addActionListener(this);
            markThreadReadItem.addActionListener(this);
            setGoodItem.addActionListener(this);
            setBadItem.addActionListener(this);
            setCheckItem.addActionListener(this);
            setObserveItem.addActionListener(this);
            deleteItem.addActionListener(this);
            undeleteItem.addActionListener(this);
            expandAllItem.addActionListener(this);
            collapseAllItem.addActionListener(this);
            expandThreadItem.addActionListener(this);
            collapseThreadItem.addActionListener(this);
        }

        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }

        private void refreshLanguage() {
            markMessageUnreadItem.setText(language.getString("MessagePane.messageTable.popupmenu.markMessageUnread"));
            markAllMessagesReadItem.setText(language.getString("MessagePane.messageTable.popupmenu.markAllMessagesRead"));
            markThreadReadItem.setText(language.getString("MessagePane.messageTable.popupmenu.markThreadRead"));
            setGoodItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToGood"));
            setBadItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToBad"));
            setCheckItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToCheck"));
            setObserveItem.setText(language.getString("MessagePane.messageTable.popupmenu.setToObserve"));
            deleteItem.setText(language.getString("MessagePane.messageTable.popupmenu.deleteMessage"));
            undeleteItem.setText(language.getString("MessagePane.messageTable.popupmenu.undeleteMessage"));
            expandAllItem.setText(language.getString("MessagePane.messageTable.popupmenu.expandAll"));
            collapseAllItem.setText(language.getString("MessagePane.messageTable.popupmenu.collapseAll"));
            expandThreadItem.setText(language.getString("MessagePane.messageTable.popupmenu.expandThread"));
            collapseThreadItem.setText(language.getString("MessagePane.messageTable.popupmenu.collapseThread"));
        }

        public void show(Component invoker, int x, int y) {

            if( messageTable.getSelectedRowCount() < 1 ) {
                return;
            }
            
            if (mainFrame.getTofTreeModel().getSelectedNode().isBoard()) {
                
                removeAll();
                
                if( Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS) ) {
                    if( messageTable.getSelectedRowCount() == 1 ) {
                        add(expandThreadItem);
                        add(collapseThreadItem);
                    }
                    add(expandAllItem);
                    add(collapseAllItem);
                    addSeparator();
                }
                
                if( messageTable.getSelectedRowCount() > 1 ) {
                    deleteItem.setEnabled(true);
                    undeleteItem.setEnabled(true);
                    add(deleteItem);
                    add(undeleteItem);
                    super.show(invoker, x, y);
                    return;
                }

                boolean itemAdded = false;
                if (messageTable.getSelectedRow() > -1) {
                    add(markMessageUnreadItem);
                    itemAdded = true;
                }
                if( selectedMessage != null && selectedMessage.getBoard().getNewMessageCount() > 0 ) {
                    add(markAllMessagesReadItem);
                    add(markThreadReadItem);
                    itemAdded = true;
                }
                if( itemAdded ) {
                    addSeparator();
                }
                add(setGoodItem);
                add(setObserveItem);
                add(setCheckItem);
                add(setBadItem);
                setGoodItem.setEnabled(false);
                setObserveItem.setEnabled(false);
                setCheckItem.setEnabled(false);
                setBadItem.setEnabled(false);

                if (messageTable.getSelectedRow() > -1 && selectedMessage != null) {
                    if( identities.isMySelf(selectedMessage.getFromName()) ) {
                        // keep all off
                    } else if (selectedMessage.isMessageStatusGOOD()) {
                        setObserveItem.setEnabled(true);
                        setCheckItem.setEnabled(true);
                        setBadItem.setEnabled(true);
                    } else if (selectedMessage.isMessageStatusCHECK()) {
                        setObserveItem.setEnabled(true);
                        setGoodItem.setEnabled(true);
                        setBadItem.setEnabled(true);
                    } else if (selectedMessage.isMessageStatusBAD()) {
                        setObserveItem.setEnabled(true);
                        setGoodItem.setEnabled(true);
                        setCheckItem.setEnabled(true);
                    } else if (selectedMessage.isMessageStatusOBSERVE()) {
                        setGoodItem.setEnabled(true);
                        setCheckItem.setEnabled(true);
                        setBadItem.setEnabled(true);
                    } else if (selectedMessage.isMessageStatusOLD()) {
                        // keep all buttons disabled
                    } else if (selectedMessage.isMessageStatusTAMPERED()) {
                        // keep all buttons disabled
                    } else {
                        logger.warning("invalid message state");
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
                
                super.show(invoker, x, y);
            }
        }
    }

    private Logger logger = Logger.getLogger(MessagePanel.class.getName());

    private SettingsClass settings;
    private Language language  = Language.getInstance();
    private FrostIdentities identities;
    private JFrame parentFrame;

    private boolean initialized = false;

    private Listener listener = new Listener();

    private FrostMessageObject selectedMessage;

    private PopupMenuMessageTable popupMenuMessageTable = null;
    private PopupMenuSubjectText popupMenuSubjectText = null;

    //private JButton downloadAttachmentsButton =
    //  new JButton(new ImageIcon(getClass().getResource("/data/attachment.gif")));
    //private JButton downloadBoardsButton =
    //  new JButton(new ImageIcon(getClass().getResource("/data/attachmentBoard.gif")));
    private JButton newMessageButton =
        new JButton(new ImageIcon(getClass().getResource("/data/newmessage.gif")));
    private JButton replyButton =
        new JButton(new ImageIcon(getClass().getResource("/data/reply.gif")));
    private JButton saveMessageButton =
        new JButton(new ImageIcon(getClass().getResource("/data/save.gif")));
    protected JButton nextUnreadMessageButton =
        new JButton(new ImageIcon(getClass().getResource("/data/nextunreadmessage.gif")));
    private JButton updateButton =
        new JButton(new ImageIcon(getClass().getResource("/data/update.gif")));
    
    private JButton setGoodButton =
        new JButton(new ImageIcon(getClass().getResource("/data/trust.gif")));
    private JButton setObserveButton =
        new JButton(new ImageIcon(getClass().getResource("/data/observe.gif")));
    private JButton setCheckButton =
        new JButton(new ImageIcon(getClass().getResource("/data/check.gif")));
    private JButton setBadButton =
        new JButton(new ImageIcon(getClass().getResource("/data/nottrust.gif")));
    
    private JToggleButton toggleShowUnreadOnly = new JToggleButton("");

    private JToggleButton toggleShowThreads = new JToggleButton("");
    private JToggleButton toggleShowSmileys = new JToggleButton("");
    private JToggleButton toggleShowHyperlinks = new JToggleButton("");

    private final String allMessagesCountPrefix = "Msg: "; // TODO: translate
    private JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix + "0");

    private final String newMessagesCountPrefix = "New: "; // TODO: translate
    private JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix + "0");

    public MessagePanel(SettingsClass settings, MainFrame mf) {
        super();
        this.settings = settings;
        mainFrame = mf;
    }

    private JToolBar getButtonsToolbar() {
        // configure buttons
        MiscToolkit toolkit = MiscToolkit.getInstance();
        toolkit.configureButton(newMessageButton, "MessagePane.toolbar.tooltip.newMessage", "/data/newmessage_rollover.gif", language);
        toolkit.configureButton(updateButton, "MessagePane.toolbar.tooltip.update", "/data/update_rollover.gif", language);
        toolkit.configureButton(replyButton, "MessagePane.toolbar.tooltip.reply", "/data/reply_rollover.gif", language);
        toolkit.configureButton(saveMessageButton, "MessagePane.toolbar.tooltip.saveMessage", "/data/save_rollover.gif", language);
        toolkit.configureButton(nextUnreadMessageButton, "MessagePane.toolbar.tooltip.nextUnreadMessage", "/data/nextunreadmessage_rollover.gif", language);
        toolkit.configureButton(setGoodButton, "MessagePane.toolbar.tooltip.setToGood", "/data/trust_rollover.gif", language);
        toolkit.configureButton(setBadButton, "MessagePane.toolbar.tooltip.setToBad", "/data/nottrust_rollover.gif", language);
        toolkit.configureButton(setCheckButton, "MessagePane.toolbar.tooltip.setToCheck", "/data/check_rollover.gif", language);
        toolkit.configureButton(setObserveButton, "MessagePane.toolbar.tooltip.setToObserve", "/data/observe_rollover.gif", language);
        // toolkit.configureButton(downloadAttachmentsButton,"Download attachment(s)","/data/attachment_rollover.gif",language);
        // toolkit.configureButton(downloadBoardsButton,"Add Board(s)","/data/attachmentBoard_rollover.gif",language);

        replyButton.setEnabled(false);
        saveMessageButton.setEnabled(false);
        setGoodButton.setEnabled(false);
        setCheckButton.setEnabled(false);
        setBadButton.setEnabled(false);
        setObserveButton.setEnabled(false);

        toggleShowUnreadOnly.setSelected(Core.frostSettings.getBoolValue(SettingsClass.SHOW_UNREAD_ONLY));
        toggleShowUnreadOnly.setIcon(new ImageIcon(getClass().getResource("/data/showunreadonly.gif")));
        toggleShowUnreadOnly.setMargin(new Insets(0, 0, 0, 0));
        toggleShowUnreadOnly.setPreferredSize(new Dimension(24,24));
        toggleShowUnreadOnly.setFocusPainted(false);
        toggleShowUnreadOnly.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowUnreadOnly"));

        toggleShowThreads.setSelected(Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS));
        toggleShowThreads.setIcon(new ImageIcon(getClass().getResource("/data/togglethreads.gif")));
        toggleShowThreads.setMargin(new Insets(0, 0, 0, 0));
        toggleShowThreads.setPreferredSize(new Dimension(24,24));
        toggleShowThreads.setFocusPainted(false);
        toggleShowThreads.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowThreads"));
        
        toggleShowSmileys.setSelected(Core.frostSettings.getBoolValue(SettingsClass.SHOW_SMILEYS));
        toggleShowSmileys.setIcon(new ImageIcon(getClass().getResource("/data/togglesmileys.gif")));
        toggleShowSmileys.setMargin(new Insets(0, 0, 0, 0));
        toggleShowSmileys.setPreferredSize(new Dimension(24,24));
        toggleShowSmileys.setFocusPainted(false);
        toggleShowSmileys.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowSmileys"));
        
        toggleShowHyperlinks.setSelected(Core.frostSettings.getBoolValue(SettingsClass.SHOW_KEYS_AS_HYPERLINKS));
        toggleShowHyperlinks.setIcon(new ImageIcon(getClass().getResource("/data/togglehyperlinks.gif")));
        toggleShowHyperlinks.setMargin(new Insets(0, 0, 0, 0));
        toggleShowHyperlinks.setPreferredSize(new Dimension(24,24));
        toggleShowHyperlinks.setFocusPainted(false);
        toggleShowHyperlinks.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowHyperlinks"));

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
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.addSeparator();
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(toggleShowUnreadOnly);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.addSeparator();
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(toggleShowThreads);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(toggleShowSmileys);
        buttonsToolbar.add(Box.createRigidArea(blankSpace));
        buttonsToolbar.add(toggleShowHyperlinks);

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
        toggleShowUnreadOnly.addActionListener(listener);
        toggleShowThreads.addActionListener(listener);
        toggleShowSmileys.addActionListener(listener);
        toggleShowHyperlinks.addActionListener(listener);
        
        return buttonsToolbar;
    }

    private PopupMenuMessageTable getPopupMenuMessageTable() {
        if (popupMenuMessageTable == null) {
            popupMenuMessageTable = new PopupMenuMessageTable();
            language.addLanguageListener(popupMenuMessageTable);
        }
        return popupMenuMessageTable;
    }

    private PopupMenuSubjectText getPopupMenuSubjectText() {
        if (popupMenuSubjectText == null) {
            popupMenuSubjectText = new PopupMenuSubjectText();
            language.addLanguageListener(popupMenuSubjectText);
        }
        return popupMenuSubjectText;
    }

    public void initialize() {
        if (!initialized) {
            refreshLanguage();
            language.addLanguageListener(listener);

            FrostMessageObject.sortThreadRootMsgsAscending = settings.getBoolValue(SettingsClass.SORT_THREADROOTMSGS_ASCENDING);

            Core.frostSettings.addPropertyChangeListener(SettingsClass.SORT_THREADROOTMSGS_ASCENDING, this);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.MSGTABLE_MULTILINE_SELECT, this);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.MSGTABLE_SCROLL_HORIZONTAL, this);
            
            // build messages list scroll pane
            MessageTreeTableModel messageTableModel = new MessageTreeTableModel(new DefaultMutableTreeNode());
            language.addLanguageListener(messageTableModel);
            messageTable = new MessageTreeTable(messageTableModel);
            updateMsgTableResizeMode();
            updateMsgTableMultilineSelect();
            messageTable.getSelectionModel().addListSelectionListener(listener);
            messageListScrollPane = new JScrollPane(messageTable);
            messageListScrollPane.setWheelScrollingEnabled(true);
            messageListScrollPane.getViewport().setBackground(messageTable.getBackground());

            messageTextPane = new MessageTextPane(mainFrame);
            
            JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,3,0));
            subjectPanel.add(subjectLabel);
            subjectPanel.add(subjectTextLabel);
            subjectPanel.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
            
            subjectTextLabel.addMouseListener(listener);

            // load message table layout
            messageTable.loadLayout(settings);

            fontChanged();
            
            JPanel dummyPanel = new JPanel(new BorderLayout());
            dummyPanel.add(subjectPanel, BorderLayout.NORTH);
            dummyPanel.add(messageTextPane, BorderLayout.CENTER);

            msgTableAndMsgTextSplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageListScrollPane, dummyPanel);
            msgTableAndMsgTextSplitpane.setDividerSize(10);
            msgTableAndMsgTextSplitpane.setResizeWeight(0.5d);
            msgTableAndMsgTextSplitpane.setMinimumSize(new Dimension(50, 20));
            int dividerLoc = Core.frostSettings.getIntValue("MessagePanel.msgTableAndMsgTextSplitpaneDividerLocation");
            if( dividerLoc < 10 ) {
                dividerLoc = 160;
            }
            msgTableAndMsgTextSplitpane.setDividerLocation(dividerLoc);

            // build main panel
            setLayout(new BorderLayout());
            add(getButtonsToolbar(), BorderLayout.NORTH);
            add(msgTableAndMsgTextSplitpane, BorderLayout.CENTER);

            // listeners
            messageTable.addMouseListener(listener);

            mainFrame.getTofTree().addTreeSelectionListener(listener);

            assignHotkeys();

            // display welcome message if no boards are available
            boardsTree_actionPerformed(null); // set initial states

            initialized = true;
        }
    }
    
    private void assignHotkeys() {
        // assign DELETE key - delete message
        Action deleteMessageAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                deleteSelectedMessage();
            }
        };
        MainFrame.getInstance().setKeyActionForNewsTab(deleteMessageAction, "DEL_MSG", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        // remove ENTER assignment from table
        messageTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
        // assign ENTER key - open message viewer
        Action openMessageAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                showCurrentMessagePopupWindow();
            }
        };
        MainFrame.getInstance().setKeyActionForNewsTab(openMessageAction, "OPEN_MSG", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        // assign N key - next unread (to whole new panel, including tree)
        Action nextUnreadAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                selectNextUnreadMessage();
            }
        };
        MainFrame.getInstance().setKeyActionForNewsTab(nextUnreadAction, "NEXT_MSG", KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        
        // assign B key - set BAD
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "SET_BAD");
        this.getActionMap().put("SET_BAD", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                setBadButton_actionPerformed(event);
            }
        });

        // assign G key - set GOOD
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "SET_GOOD");
        this.getActionMap().put("SET_GOOD", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                setGoodButton_actionPerformed(event);
            }
        });

        // assign C key - set CHECK
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "SET_CHECK");
        this.getActionMap().put("SET_CHECK", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                setCheckButton_actionPerformed(event);
            }
        });

        // assign O key - set OBSERVE
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "SET_OBSERVE");
        this.getActionMap().put("SET_OBSERVE", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                setObserveButton_actionPerformed(event);
            }
        });
    }
    
    public void saveLayout(SettingsClass frostSettings) {
        frostSettings.setValue("MessagePanel.msgTableAndMsgTextSplitpaneDividerLocation", 
                msgTableAndMsgTextSplitpane.getDividerLocation());
        
        getMessageTable().saveLayout(frostSettings);
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

    /**
     * Gets the content of the message selected in the tofTable.
     * @param e This selectionEv ent is needed to determine if the Table is just being edited
     * @param table The tofTable
     * @param messages A Vector containing all MessageObjects that are just displayed by the table
     * @return The content of the message
     */
    private FrostMessageObject evalSelection(ListSelectionEvent e, JTable table, Board board) {
//        if( (!e.getValueIsAdjusting() && !table.isEditing()) ) {
        if( !table.isEditing() ) {

            // more than 1 selected row is handled specially, only used to delete/undelete messages
            if( table.getSelectedRowCount() > 1 ) {
                return null;
            }
            int row = table.getSelectedRow();
            if( row != -1 && row < getMessageTableModel().getRowCount() ) {

                final FrostMessageObject message = (FrostMessageObject)getMessageTableModel().getRow(row);

                if( message != null ) {
                    
                    if( message.isNew() == false ) {
                        // its a read message, nothing more to do here ...
                        return message;
                    }

                    // this is a new message
                    message.setNew(false); // mark as read

                    getMessageTableModel().fireTableRowsUpdated(row, row);

                    // determine thread root msg of this msg
                    FrostMessageObject threadRootMsg = message.getThreadRootMessage();
                    
                    // update thread root to reset unread msg childs marker
                    if( threadRootMsg != message && threadRootMsg != null ) {
                        getMessageTreeModel().nodeChanged(threadRootMsg);
                    }
                    
                    board.decNewMessageCount();

                    MainFrame.getInstance().updateMessageCountLabels(board);
                    MainFrame.getInstance().updateTofTree(board);
                    
                    Thread saver = new Thread() {
                        public void run() {
                            // save the changed isnew state into the database
                            try {
                                AppLayerDatabase.getMessageTable().updateMessage(message);
                            } catch (SQLException ex) {
                                logger.log(Level.SEVERE, "Error updating a message object", ex);
                            }
                        }
                    };
                    saver.start();

                    return message;
                }
            }
        }
        return null;
    }
    
    private void messageTable_itemSelected(ListSelectionEvent e) {

        AbstractNode selectedNode = mainFrame.getTofTreeModel().getSelectedNode();
        if (selectedNode.isFolder()) {
            setGoodButton.setEnabled(false);
            setCheckButton.setEnabled(false);
            setBadButton.setEnabled(false);
            setObserveButton.setEnabled(false);
            replyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);
            return;
        } else if(!selectedNode.isBoard()) {
            return;
        }
        
        Board selectedBoard = (Board) selectedNode; 

        // board selected
        FrostMessageObject newSelectedMessage = evalSelection(e, messageTable, selectedBoard);
        if( newSelectedMessage == selectedMessage ) {
            return; // user is reading a message, selection did NOT change
        } else {
            selectedMessage = newSelectedMessage;
        }

        if (selectedMessage != null) {
            
            if( selectedMessage.isDummy() ) {
                getMessageTextPane().update_boardSelected();
                subjectTextLabel.setText("");
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(false);
                setBadButton.setEnabled(false);
                setObserveButton.setEnabled(false);
                replyButton.setEnabled(false);
                saveMessageButton.setEnabled(false);
                return;
            }
            
            MainFrame.getInstance().displayNewMessageIcon(false);

            if (selectedBoard.isReadAccessBoard() == false) {
                replyButton.setEnabled(true);
            } else {
                replyButton.setEnabled(false);
            }

            if( identities.isMySelf(selectedMessage.getFromName()) ) {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(false);
                setBadButton.setEnabled(false);
                setObserveButton.setEnabled(false);
            } else if (selectedMessage.isMessageStatusCHECK()) {
                setCheckButton.setEnabled(false);
                setGoodButton.setEnabled(true);
                setBadButton.setEnabled(true);
                setObserveButton.setEnabled(true);
            } else if (selectedMessage.isMessageStatusGOOD()) {
                setGoodButton.setEnabled(false);
                setCheckButton.setEnabled(true);
                setBadButton.setEnabled(true);
                setObserveButton.setEnabled(true);
            } else if (selectedMessage.isMessageStatusBAD()) {
                setBadButton.setEnabled(false);
                setGoodButton.setEnabled(true);
                setCheckButton.setEnabled(true);
                setObserveButton.setEnabled(true);
            } else if (selectedMessage.isMessageStatusOBSERVE()) {
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
            subjectTextLabel.setText(selectedMessage.getSubject());
            
            if (selectedMessage.getContent().length() > 0) {
                saveMessageButton.setEnabled(true);
            } else {
                saveMessageButton.setEnabled(false);
            }

        } else {
            // no msg selected
            getMessageTextPane().update_boardSelected();
            subjectTextLabel.setText("");
            replyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);

            setGoodButton.setEnabled(false);
            setCheckButton.setEnabled(false);
            setBadButton.setEnabled(false);
            setObserveButton.setEnabled(false);
        }
    }

    private void newMessageButton_actionPerformed(ActionEvent e) {
        tofNewMessageButton_actionPerformed(e);
    }

    private void setBadButton_actionPerformed(ActionEvent e) {
        Identity id = getSelectedMessageFromIdentity();
        if( id == null || id.isBAD() ) {
            return;
        }
        if(id.isGOOD()) {
            String title = language.getString("MessagePane.setGoodUserToBadConfirmation.title");
            String userName = selectedMessage.getFromName().substring(0, selectedMessage.getFromName().indexOf("@"));
            String txt = language.formatMessage("MessagePane.setGoodUserToBadConfirmation.text", userName);
            int answer = JOptionPane.showConfirmDialog(parentFrame, txt, title, JOptionPane.YES_NO_OPTION); 
            if (answer == JOptionPane.NO_OPTION) {
                return;
            }
        } 
        // now mark BAD
        setGoodButton.setEnabled(true);
        setCheckButton.setEnabled(true);
        setBadButton.setEnabled(false);
        setObserveButton.setEnabled(true);
        id.setBAD();
        updateTableAfterChangeOfIdentityState();
    }

    private void setCheckButton_actionPerformed(ActionEvent e) {
        Identity id = getSelectedMessageFromIdentity();
        if( id == null || id.isCHECK() ) {
            return;
        }
        setGoodButton.setEnabled(true);
        setCheckButton.setEnabled(false);
        setBadButton.setEnabled(true);
        setObserveButton.setEnabled(true);
        id.setCHECK();
        updateTableAfterChangeOfIdentityState();
    }

    private void setObserveButton_actionPerformed(ActionEvent e) {
        Identity id = getSelectedMessageFromIdentity();
        if( id == null || id.isOBSERVE() ) {
            return;
        }
        setGoodButton.setEnabled(true);
        setCheckButton.setEnabled(true);
        setBadButton.setEnabled(true);
        setObserveButton.setEnabled(false);
        id.setOBSERVE();
        updateTableAfterChangeOfIdentityState();
    }

    private void setGoodButton_actionPerformed(ActionEvent e) {
        Identity id = getSelectedMessageFromIdentity();
        if( id == null || id.isGOOD() ) {
            return;
        }
        if(id.isBAD()) {
            String title = language.getString("MessagePane.setBadUserToGoodConfirmation.title");
            String userName = selectedMessage.getFromName().substring(0, selectedMessage.getFromName().indexOf("@"));
            String txt = language.formatMessage("MessagePane.setBadUserToGoodConfirmation.text", userName);
            int answer = JOptionPane.showConfirmDialog(parentFrame, txt, title, JOptionPane.YES_NO_OPTION); 
            if (answer == JOptionPane.NO_OPTION) {
                return;
            }
        }
        // now mark GOOD
        setGoodButton.setEnabled(false);
        setCheckButton.setEnabled(true);
        setBadButton.setEnabled(true);
        setObserveButton.setEnabled(true);
        id.setGOOD();
        updateTableAfterChangeOfIdentityState();
    }
    
    private void toggleShowUnreadOnly_actionPerformed(ActionEvent e) {
        boolean oldValue = Core.frostSettings.getBoolValue(SettingsClass.SHOW_UNREAD_ONLY);
        boolean newValue = !oldValue;
        Core.frostSettings.setValue(SettingsClass.SHOW_UNREAD_ONLY, newValue);
        // reload messages
        MainFrame.getInstance().tofTree_actionPerformed(null, true);
    }

    private void toggleShowThreads_actionPerformed(ActionEvent e) {
        boolean oldValue = Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS);
        boolean newValue = !oldValue;
        Core.frostSettings.setValue(SettingsClass.SHOW_THREADS, newValue);
        // reload messages
        MainFrame.getInstance().tofTree_actionPerformed(null, true);
    }

    private void toggleShowSmileys_actionPerformed(ActionEvent e) {
        boolean oldValue = Core.frostSettings.getBoolValue(SettingsClass.SHOW_SMILEYS);
        boolean newValue = !oldValue;
        Core.frostSettings.setValue(SettingsClass.SHOW_SMILEYS, newValue);
        // redraw is done in textpane by propertychangelistener!
    }

    private void toggleShowHyperlinks_actionPerformed(ActionEvent e) {
        boolean oldValue = Core.frostSettings.getBoolValue(SettingsClass.SHOW_KEYS_AS_HYPERLINKS);
        boolean newValue = !oldValue;
        Core.frostSettings.setValue(SettingsClass.SHOW_KEYS_AS_HYPERLINKS, newValue);
        // redraw is done in textpane by propertychangelistener!
    }

    private void refreshLanguage() {
        newMessageButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.newMessage"));
        replyButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.reply"));
        saveMessageButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.saveMessage"));
        nextUnreadMessageButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.nextUnreadMessage"));
        setGoodButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.setToGood"));
        setBadButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.setToBad"));
        setCheckButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.setToCheck"));
        setObserveButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.setToObserve"));
        updateButton.setToolTipText(language.getString("MessagePane.toolbar.tooltip.update"));
        toggleShowUnreadOnly.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowUnreadOnly"));
        toggleShowThreads.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowThreads"));
        toggleShowSmileys.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowSmileys"));
        toggleShowHyperlinks.setToolTipText(language.getString("MessagePane.toolbar.tooltip.toggleShowHyperlinks"));

        subjectLabel.setText(language.getString("MessageWindow.subject")+": ");
    }

    private void replyButton_actionPerformed(ActionEvent e) {
        FrostMessageObject origMessage = selectedMessage;
        composeReply(origMessage, parentFrame);
    }

    public void composeReply(FrostMessageObject origMessage, Window parent) {

        Board targetBoard = mainFrame.getTofTreeModel().getBoardByName(origMessage.getBoard().getName());
        if( targetBoard == null ) {
            String title = language.getString("MessagePane.missingBoardError.title");
            String txt = language.formatMessage("MessagePane.missingBoardError.text", origMessage.getBoard().getName());
            JOptionPane.showMessageDialog(parent, txt, title, JOptionPane.ERROR);
            return;
        }
        
        String subject = origMessage.getSubject();
        if (subject.startsWith("Re:") == false) {
            subject = "Re: " + subject;
        }

        // add msgId we answer to the inReplyTo list
        String inReplyTo = null;
        if( origMessage.getMessageId() != null ) {
            inReplyTo = origMessage.getInReplyTo();
            if( inReplyTo == null ) {
                inReplyTo = origMessage.getMessageId();
            } else {
                inReplyTo += ","+origMessage.getMessageId();
            }
        }
        
        if( origMessage.getRecipientName() != null ) {
            // this message was for me, reply encrypted

            if( origMessage.getFromIdentity() == null ) {
                String title = language.getString("MessagePane.unknownRecipientError.title");
                String txt = language.formatMessage("MessagePane.unknownRecipientError.text", origMessage.getFromName());
                JOptionPane.showMessageDialog(parent, txt, title, JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalIdentity senderId = null;
            if( origMessage.getFromIdentity() instanceof LocalIdentity ) {
                // we want to reply to our own message
                senderId = (LocalIdentity)origMessage.getFromIdentity();
            } else {
                // we want to reply, find our identity that was the recipient of this message
                senderId = identities.getLocalIdentity(origMessage.getRecipientName());
                if( senderId == null ) {
                    String title = language.getString("MessagePane.missingLocalIdentityError.title");
                    String txt = language.formatMessage("MessagePane.missingLocalIdentityError.text", origMessage.getRecipientName());
                    JOptionPane.showMessageDialog(parent, txt, title, JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            MessageFrame newMessageFrame = new MessageFrame(settings, parent);
            newMessageFrame.composeEncryptedReply(
                    targetBoard,
                    subject,
                    inReplyTo,
                    origMessage.getContent(),
                    origMessage.getFromIdentity(),
                    senderId,
                    origMessage);
        } else {
            MessageFrame newMessageFrame = new MessageFrame(settings, parent);
            newMessageFrame.composeReply(
                    targetBoard,
                    subject,
                    inReplyTo,
                    origMessage.getContent(),
                    origMessage);
        }
    }

    private void showMessageTablePopupMenu(MouseEvent e) {
        // select row where rightclick occurred if row under mouse is NOT selected 
        Point p = e.getPoint();
        int y = messageTable.rowAtPoint(p);
        if( y < 0 ) {
            return;
        }
        if( !messageTable.getSelectionModel().isSelectedIndex(y) ) {
            messageTable.getSelectionModel().setSelectionInterval(y, y);
        }
        // show popup menu
        getPopupMenuMessageTable().show(e.getComponent(), e.getX(), e.getY());
    }

    private void showCurrentMessagePopupWindow() {
        if( !isCorrectlySelectedMessage() ) {
            return;
        }
        MessageWindow messageWindow = new MessageWindow( mainFrame, selectedMessage, this.getSize() );
        messageWindow.setVisible(true);
    }

    private void updateButton_actionPerformed(ActionEvent e) {
        // restarts all finished threads if there are some long running threads
        AbstractNode node = mainFrame.getTofTreeModel().getSelectedNode();
        if (node != null && node.isBoard() ) {
            Board b = (Board) node;
            if( b.isManualUpdateAllowed() ) {
                mainFrame.getTofTree().updateBoard(b);
            }
        }
    }

    private void boardsTree_actionPerformed(TreeSelectionEvent e) {
        
        if (((TreeNode) mainFrame.getTofTreeModel().getRoot()).getChildCount() == 0) {
            // There are no boards
            getMessageTextPane().update_noBoardsFound();
            subjectTextLabel.setText("");
        } else {
            // There are boards
            AbstractNode node = (AbstractNode)mainFrame.getTofTree().getLastSelectedPathComponent();
            if (node != null) {
                if (node.isBoard()) {
                    // node is a board
                    // FIXME: reset message history!
                    getMessageTextPane().update_boardSelected();
                    subjectTextLabel.setText("");
                    updateButton.setEnabled(true);
                    saveMessageButton.setEnabled(false);
                    replyButton.setEnabled(false);
                    if (((Board)node).isReadAccessBoard()) {
                        newMessageButton.setEnabled(false);
                    } else {
                        newMessageButton.setEnabled(true);
                    }
                } else if(node.isFolder()) {
                    // node is a folder
                    newMessageButton.setEnabled(false);
                    saveMessageButton.setEnabled(false);
                    updateButton.setEnabled(false);
                    getMessageTextPane().update_folderSelected();
                    subjectTextLabel.setText("");
                }
            }
        }
    }

    /**
     * returns true if message was correctly selected
     * @return
     */
    private boolean isCorrectlySelectedMessage() {
        int row = messageTable.getSelectedRow();
        if (row < 0
            || selectedMessage == null
            || mainFrame.getTofTreeModel().getSelectedNode() == null
            || !mainFrame.getTofTreeModel().getSelectedNode().isBoard()
            || selectedMessage.isDummy() )
        {
            return false;
        }
        return true;
    }
    
    private void markThreadRead() {
        if( selectedMessage == null ) {
            return;
        }

        TreeNode[] rootPath = selectedMessage.getPath();
        if( rootPath.length < 2 ) {
            return;
        }

        FrostMessageObject levelOneMsg = (FrostMessageObject)rootPath[1];

        DefaultTreeModel model = MainFrame.getInstance().getMessagePanel().getMessageTreeModel();
        AbstractNode node = mainFrame.getTofTreeModel().getSelectedNode();
        if( node == null || !node.isBoard() ) {
            return;
        }
        Board board = (Board) node;
        final LinkedList<FrostMessageObject> msgList = new LinkedList<FrostMessageObject>();
        
        for(Enumeration e = levelOneMsg.depthFirstEnumeration(); e.hasMoreElements(); ) {
            FrostMessageObject mo = (FrostMessageObject)e.nextElement();
            if( mo.isNew() ) { 
                msgList.add(mo);
                mo.setNew(false);
                model.nodeChanged(mo);
                board.decNewMessageCount();
            }
        }
        
        updateMessageCountLabels(board);
        mainFrame.updateTofTree(board);
        
        Thread saver = new Thread() {
            public void run() {
                // save message, we must save the changed deleted state into the database
                for( Iterator i = msgList.iterator(); i.hasNext(); ) {
                    FrostMessageObject mo = (FrostMessageObject) i.next();
                    try {
                        AppLayerDatabase.getMessageTable().updateMessage(mo);
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Error updating a message object", e);
                    }
                }
            }
        };
        saver.start();
    }

    public void deleteSelectedMessage() {

        AbstractNode node = mainFrame.getTofTreeModel().getSelectedNode();
        if( node == null || !node.isBoard() ) {
            return;
        }
        Board board = (Board) node;
        
        if( messageTable.getSelectedRowCount() <= 1 && !isCorrectlySelectedMessage() ) {
            return;
        }

        // set all selected messages deleted
        int[] rows = messageTable.getSelectedRows();
        final ArrayList<FrostMessageObject> saveMessages = new ArrayList<FrostMessageObject>();
        DefaultTreeModel model = (DefaultTreeModel)MainFrame.getInstance().getMessagePanel().getMessageTable().getTree().getModel();        
        for(int x=rows.length-1; x >= 0; x--) {
            FrostMessageObject targetMessage = (FrostMessageObject)getMessageTableModel().getRow(rows[x]);
            targetMessage.setDeleted(true);
            if( targetMessage.isNew() ) {
                targetMessage.setNew(false);
                board.decNewMessageCount();
            }
            // we don't remove the message immediately, they are not loaded during next change to this board
            // needs repaint or the line which crosses the message isn't completely seen
            model.nodeChanged(targetMessage);
            saveMessages.add(targetMessage);
        }

        // update new and shown message count
        updateMessageCountLabels(board);
        mainFrame.updateTofTree(board);

        Thread saver = new Thread() {
            public void run() {
                // save message, we must save the changed deleted state
                for(Iterator i=saveMessages.iterator(); i.hasNext(); ) {
                    FrostMessageObject targetMessage = (FrostMessageObject)i.next();
                    try {
                        AppLayerDatabase.getMessageTable().updateMessage(targetMessage);
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Error updating a message object", e);
                    }
                }
            }
        };
        saver.start();
    }

    private void undeleteSelectedMessage(){
        if( messageTable.getSelectedRowCount() <= 1 && !isCorrectlySelectedMessage() ) {
            return;
        }

        // set all selected messages deleted
        int[] rows = messageTable.getSelectedRows();
        final ArrayList<FrostMessageObject> saveMessages = new ArrayList<FrostMessageObject>();
        DefaultTreeModel model = (DefaultTreeModel)MainFrame.getInstance().getMessagePanel().getMessageTable().getTree().getModel();
        for(int x=0; x < rows.length; x++) {
            FrostMessageObject targetMessage = (FrostMessageObject)getMessageTableModel().getRow(rows[x]);
            if( !targetMessage.isDeleted() ) {
                continue;
            }
            targetMessage.setDeleted(false);

            // needs repaint or the line which crosses the message isn't completely seen
            model.nodeChanged(targetMessage);

            saveMessages.add(targetMessage);
        }
        
        Thread saver = new Thread() {
            public void run() {
                // save message, we must save the changed deleted state
                for(Iterator i=saveMessages.iterator(); i.hasNext(); ) {
                    FrostMessageObject targetMessage = (FrostMessageObject)i.next();
                    try {
                        AppLayerDatabase.getMessageTable().updateMessage(targetMessage);
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Error updating a message object", e);
                    }
                }
            }
        };
        saver.start();
    }

    public void setIdentities(FrostIdentities identities) {
        this.identities = identities;
    }

    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }
    
    /**
     * Marks current selected message unread
     */
    private void markSelectedMessageUnread() {
        if( !isCorrectlySelectedMessage() ) {
            return;
        }

        final FrostMessageObject targetMessage = selectedMessage;
        
        if( targetMessage.isDeleted() ) {
            return;
        }

        AbstractNode node = mainFrame.getTofTreeModel().getSelectedNode();
        if( node == null || !node.isBoard() ) {
            return;
        }
        Board board = (Board) node;

        messageTable.removeRowSelectionInterval(0, messageTable.getRowCount() - 1);

        targetMessage.setNew(true);

        // let renderer check for new state
        getMessageTreeModel().nodeChanged(targetMessage);

        board.incNewMessageCount();

        updateMessageCountLabels(board);
        mainFrame.updateTofTree(board);
        
        Thread saver = new Thread() {
            public void run() {
                // save message, we must save the changed deleted state into the database
                try {
                    AppLayerDatabase.getMessageTable().updateMessage(targetMessage);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error updating a message object", e);
                }
            }
        };
        saver.start();
    }

    /**
     * Method that update the Msg and New counts for tof table
     * Expects that the boards messages are shown in table
     * @param board
     */
    public void updateMessageCountLabels(AbstractNode node) {
        if (node.isFolder()) {
            allMessagesCountLabel.setText("");
            newMessagesCountLabel.setText("");
            nextUnreadMessageButton.setEnabled(false);
        } else if (node.isBoard()) {
            int allMessages = 0;
            FrostMessageObject rootNode = (FrostMessageObject)MainFrame.getInstance().getMessageTreeModel().getRoot();
            for(Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                FrostMessageObject mo = (FrostMessageObject)e.nextElement();
                if( !mo.isDummy() ) {
                    allMessages++;
                }
            }
            allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

            int newMessages = ((Board)node).getNewMessageCount();
            newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
            if( newMessages > 0 ) {
                nextUnreadMessageButton.setEnabled(true);
            } else {
                nextUnreadMessageButton.setEnabled(false);
            }
        }
    }
    
    private Identity getSelectedMessageFromIdentity() {
        if( !isCorrectlySelectedMessage() ) {
            return null;
        }
        if( !selectedMessage.isSignatureStatusVERIFIED() ) {
            return null;
        }
        Identity ident = selectedMessage.getFromIdentity();
        if(ident == null ) {
            logger.severe("no identity in list for from: "+selectedMessage.getFromName());
            return null;
        }
        if( ident instanceof LocalIdentity ) {
            logger.info("Ignored request to change my own ID state");
            return null;
        }
        return ident;
    }
    
    public FrostMessageObject getSelectedMessage() {
        if( !isCorrectlySelectedMessage() ) {
            return null;
        }
        return selectedMessage;
    }

    public void updateTableAfterChangeOfIdentityState() {
        // walk through shown messages and remove unneeded (e.g. if hideBad)
        // remember selected msg and select next
        AbstractNode node = mainFrame.getTofTreeModel().getSelectedNode();
        if( node == null || !node.isBoard() ) {
            return;
        }
        Board board = (Board) node;
        // a board is selected and shown
        DefaultTreeModel model = getMessageTreeModel();
        DefaultMutableTreeNode rootnode = (DefaultMutableTreeNode)model.getRoot();
        
        for(Enumeration e=rootnode.depthFirstEnumeration(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            if( !(o instanceof FrostMessageObject) ) {
                continue;
            }
            FrostMessageObject message = (FrostMessageObject)o;
            int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(message);
            if( row >= 0 ) {
                getMessageTableModel().fireTableRowsUpdated(row, row);
            }
        }
        MainFrame.getInstance().updateMessageCountLabels(board);
    }

    /**
     * tofNewMessageButton Action Listener (tof/ New Message)
     */
    private void tofNewMessageButton_actionPerformed(ActionEvent e) {
        AbstractNode node = mainFrame.getTofTreeModel().getSelectedNode();
        if( node == null || !node.isBoard() ) {
            return;
        }
        Board board = (Board) node;

        MessageFrame newMessageFrame = new MessageFrame(settings, mainFrame);
        newMessageFrame.composeNewMessage(board, "No subject", "");
    }

    /**
     * Search through all messages, find next unread message by date (earliest message in table).
     */
    public void selectNextUnreadMessage() {

        FrostMessageObject nextMessage = null;

        final DefaultTreeModel tableModel = getMessageTreeModel();

        // use a different method based on threaded or not threaded view
        if( Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS) ) {

            FrostMessageObject initial = getSelectedMessage();
            
            if (initial != null) { // none selcted

            	TreeNode[] path = initial.getPath();
            	java.util.List path_list = java.util.Arrays.asList(path);

            	for( int idx = initial.getLevel(); idx > 0 && nextMessage == null; idx-- ) {
            		FrostMessageObject parent = (FrostMessageObject) path[idx];
            		LinkedList<FrostMessageObject> queue = new LinkedList<FrostMessageObject>();
            		for( queue.add(parent); !queue.isEmpty() && nextMessage == null; ) {
            			final FrostMessageObject message = (FrostMessageObject) queue.removeFirst();
            			if( message.isNew() ) {
            				nextMessage = message;
            				break;
            			}

            			Enumeration children = message.children();
            			while( children.hasMoreElements() ) {
                            FrostMessageObject t = (FrostMessageObject) children.nextElement();
            				if( !path_list.contains(t) ) {
            					queue.add(t);
            				}
            			}
            		}
            	}
            }
        }
        if( nextMessage == null ) {
            for( Enumeration e = ((DefaultMutableTreeNode) tableModel.getRoot()).depthFirstEnumeration(); 
                 e.hasMoreElements(); ) 
            {
                final FrostMessageObject message = (FrostMessageObject) e.nextElement();
                if( message.isNew() ) {
                    if( nextMessage == null ) {
                        nextMessage = message;
                    } else {
                        if( nextMessage.getDateAndTimeString().compareTo(message.getDateAndTimeString()) > 0 ) {
                            nextMessage = message;
                        }
                    }
                }
            }
        }

        if( nextMessage == null ) {
            // code to move to next board???
        } else {
            messageTable.removeRowSelectionInterval(0, getMessageTableModel().getRowCount() - 1);
            messageTable.getTree().makeVisible(new TreePath(nextMessage.getPath()));
            int row = messageTable.getRowForNode(nextMessage);
            if( row >= 0 ) {
                messageTable.addRowSelectionInterval(row, row);
                messageListScrollPane.getVerticalScrollBar().setValue(
                        (row == 0 ? row : row - 1) * messageTable.getRowHeight());
            }
        }
    }

    public TreeTableModelAdapter getMessageTableModel() {
        return (TreeTableModelAdapter)getMessageTable().getModel();
    }
    public DefaultTreeModel getMessageTreeModel() {
        return (DefaultTreeModel)getMessageTable().getTree().getModel();
    }
    public MessageTreeTable getMessageTable() {
        return messageTable;
    }
    public MessageTextPane getMessageTextPane() {
        return messageTextPane;
    }

    private void updateMsgTableMultilineSelect() {
        if( Core.frostSettings.getBoolValue(SettingsClass.MSGTABLE_MULTILINE_SELECT) ) {
            messageTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            messageTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        }
    }

    private void updateMsgTableResizeMode() {
        if( Core.frostSettings.getBoolValue(SettingsClass.MSGTABLE_SCROLL_HORIZONTAL) ) {
            // show horizontal scrollbar if needed
            getMessageTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        } else {
            // auto-resize columns, no horizontal scrollbar
            getMessageTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.MSGTABLE_MULTILINE_SELECT)) {
            updateMsgTableMultilineSelect();
        } else if (evt.getPropertyName().equals(SettingsClass.MSGTABLE_SCROLL_HORIZONTAL)) {
            updateMsgTableResizeMode();
        } else if (evt.getPropertyName().equals(SettingsClass.SORT_THREADROOTMSGS_ASCENDING)) {
            FrostMessageObject.sortThreadRootMsgsAscending = settings.getBoolValue(SettingsClass.SORT_THREADROOTMSGS_ASCENDING);
        }
    }
}
