/*
  TofTree.java / Frost
  Copyright (C) 2002  Frost Project <jtcfrost.sourceforge.net>

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
package frost.boards;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import frost.*;
import frost.fcp.*;
import frost.gui.*;
import frost.storage.*;
import frost.threads.*;
import frost.util.gui.*;
import frost.util.gui.search.*;
import frost.util.gui.translation.*;

public class TofTree extends JDragTree implements AutoSavable, ExitSavable, PropertyChangeListener {

    private static final String FROST_ANNOUNCE_NAME = "frost-announce";
    private static final String FREENET_05_FROST_ANNOUNCE_PUBKEY = "SSK@7i~oLj~57mQVRrKfMxYgLULJ2r0PAgM";
    // new 0.7 key
    private static final String FREENET_07_FROST_ANNOUNCE_PUBKEY = "SSK@l4YxTKAc-sCho~6w-unV6pl-uxIbfuGnGRzo3BJH0ck,4N48yl8E4rh9UPPV26Ev1ZGrRRgeGOTgw1Voka6lk4g,AQACAAE";

    private boolean showBoardDescriptionToolTips;
    private boolean showBoardUpdatedCount;
    private boolean showBoardUpdateVisualization;
    private boolean showFlaggedStarredIndicators;

    private class PopupMenuTofTree
        extends JSkinnablePopupMenu
        implements LanguageListener, ActionListener {

        private final JMenuItem addBoardItem = new JMenuItem();
        private final JMenuItem addFolderItem = new JMenuItem();
        private final JMenuItem configureBoardItem = new JMenuItem();
        private final JMenuItem configureFolderItem = new JMenuItem();
        private final JMenuItem cutNodeItem = new JMenuItem();

        private final JMenuItem descriptionItem = new JMenuItem();
        private final JMenuItem pasteNodeItem = new JMenuItem();
        private final JMenuItem refreshItem = new JMenuItem();
        private final JMenuItem removeNodeItem = new JMenuItem();
        private final JMenuItem renameFolderItem = new JMenuItem();
        private final JMenuItem searchMessagesItem = new JMenuItem();

        private final JMenuItem markAllReadItem = new JMenuItem();

        private AbstractNode selectedTreeNode = null;
        private final JMenuItem sortFolderItem = new JMenuItem();

        public PopupMenuTofTree() {
            super();
            initialize();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            final Object source = e.getSource();

            final frost.util.gui.FrostSwingWorker worker = new frost.util.gui.FrostSwingWorker(this) {

                @Override
                protected void doNonUILogic() throws RuntimeException {
                    if (source == refreshItem) {
                        refreshSelected();
                    } else if (source == addBoardItem) {
                        addBoardSelected();
                    } else if (source == addFolderItem) {
                        addFolderSelected();
                    } else if (source == removeNodeItem) {
                        removeNodeSelected();
                    } else if (source == cutNodeItem) {
                        cutNodeSelected();
                    } else if (source == pasteNodeItem) {
                        pasteNodeSelected();
                    } else if (source == configureBoardItem || source == configureFolderItem) {
                        configureBoardSelected();
                    } else if (source == sortFolderItem) {
                        sortFolderSelected();
                    } else if( source == markAllReadItem ) {
                        markAllReadSelected();
                    } else if( source == renameFolderItem ) {
                        renameFolderSelected();
                    } else if( source == searchMessagesItem ) {
                        searchMessagesSelected();
                    }
                }

                @Override
                protected void doUIUpdateLogic() throws RuntimeException {
                    //Nothing here
                }

            };
            worker.start();
        }

        private void addBoardSelected() {
            createNewBoard(mainFrame);
        }

        private void addFolderSelected() {
            createNewFolder(mainFrame);
        }

        private void configureBoardSelected() {
            configureBoard(selectedTreeNode);
        }

        private void cutNodeSelected() {
            cutNode(selectedTreeNode);
        }

        private void initialize() {
            refreshLanguage();

            final MiscToolkit miscToolkit = MiscToolkit.getInstance();
            addBoardItem.setIcon(miscToolkit.getScaledImage("/data/newboard.gif", 16, 16));
            addFolderItem.setIcon(miscToolkit.getScaledImage("/data/newfolder.gif", 16, 16));
            configureBoardItem.setIcon(miscToolkit.getScaledImage("/data/configure.gif", 16, 16));
            configureFolderItem.setIcon(miscToolkit.getScaledImage("/data/configure.gif", 16, 16));
            cutNodeItem.setIcon(miscToolkit.getScaledImage("/data/cut.gif", 16, 16));
            pasteNodeItem.setIcon(miscToolkit.getScaledImage("/data/paste.gif", 16, 16));
            refreshItem.setIcon(miscToolkit.getScaledImage("/data/update.gif", 16, 16));
            removeNodeItem.setIcon(miscToolkit.getScaledImage("/data/remove.gif", 16, 16));
            sortFolderItem.setIcon(miscToolkit.getScaledImage("/data/sort.gif", 16, 16));
            renameFolderItem.setIcon(miscToolkit.getScaledImage("/data/rename.gif", 16, 16));
            searchMessagesItem.setIcon(miscToolkit.getScaledImage("/data/searchmessages.gif", 16, 16));

            descriptionItem.setEnabled(false);

            // add listeners
            refreshItem.addActionListener(this);
            addBoardItem.addActionListener(this);
            addFolderItem.addActionListener(this);
            removeNodeItem.addActionListener(this);
            cutNodeItem.addActionListener(this);
            pasteNodeItem.addActionListener(this);
            configureBoardItem.addActionListener(this);
            configureFolderItem.addActionListener(this);
            sortFolderItem.addActionListener(this);
            markAllReadItem.addActionListener(this);
            renameFolderItem.addActionListener(this);
            searchMessagesItem.addActionListener(this);
        }

        /* (non-Javadoc)
         * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
         */
        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        private void pasteNodeSelected() {
            if (clipboard != null) {
                pasteNode(selectedTreeNode);
            }
        }

        private void refreshLanguage() {
            addBoardItem.setText(language.getString("BoardTree.popupmenu.addNewBoard"));
            addFolderItem.setText(language.getString("BoardTree.popupmenu.addNewFolder"));
            configureBoardItem.setText(language.getString("BoardTree.popupmenu.configureSelectedBoard"));
            configureFolderItem.setText(language.getString("BoardTree.popupmenu.configureSelectedFolder"));
            sortFolderItem.setText(language.getString("BoardTree.popupmenu.sortFolder"));
            markAllReadItem.setText(language.getString("BoardTree.popupmenu.markAllMessagesRead"));
            renameFolderItem.setText(language.getString("BoardTree.popupmenu.renameFolder"));
            searchMessagesItem.setText(language.getString("BoardTree.popupmenu.searchMessages")+"...");
        }

        private void refreshSelected() {
            refreshNode(selectedTreeNode);
        }

        private void markAllReadSelected() {
            TOF.getInstance().markAllMessagesRead(selectedTreeNode); // folder or board
        }

        private void removeNodeSelected() {
            removeNode(selectedTreeNode);
        }

        private void searchMessagesSelected() {
            final List<Board> boardsToSearch = new ArrayList<Board>();
            if( selectedTreeNode.isBoard() ) {
                boardsToSearch.add((Board)selectedTreeNode);
            } else {
                // folder, add all child boards
                final Enumeration<AbstractNode> e = selectedTreeNode.breadthFirstEnumeration();
                while(e.hasMoreElements() ) {
                    final AbstractNode n = e.nextElement();
                    if( n.isBoard() ) {
                        boardsToSearch.add((Board)n);
                    }
                }
            }
            if( !boardsToSearch.isEmpty() ) {
                MainFrame.getInstance().startSearchMessagesDialog(boardsToSearch);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
         */
        @Override
        public void show(final Component invoker, final int x, final int y) {
            final int selRow = getRowForLocation(x, y);

            if (selRow != -1) { // only if a node is selected
                removeAll();

                final TreePath selPath = getPathForLocation(x, y);
                selectedTreeNode = (AbstractNode) selPath.getLastPathComponent();

                if( !selectedTreeNode.isFolder() && !selectedTreeNode.isBoard() ) {
                    return; // no menu for sent/unsent
                }

                final String folderOrBoard1 =
                    ((selectedTreeNode.isFolder())
                        ? language.getString("BoardTree.popupmenu.Folder")
                        : language.getString("BoardTree.popupmenu.Board"));
                final String folderOrBoard2 =
                    ((selectedTreeNode.isFolder())
                        ? language.getString("BoardTree.popupmenu.folder")
                        : language.getString("BoardTree.popupmenu.board"));

                descriptionItem.setText(folderOrBoard1 + " : " + selectedTreeNode.getName());

                refreshItem.setText(language.getString("BoardTree.popupmenu.refresh") + " " + folderOrBoard2);
                removeNodeItem.setText(language.getString("BoardTree.popupmenu.remove") + " " + folderOrBoard2);
                cutNodeItem.setText(language.getString("BoardTree.popupmenu.cut") + " " + folderOrBoard2);

                add(descriptionItem);
                addSeparator();
                add(refreshItem);
                add(searchMessagesItem);
                addSeparator();
                add(markAllReadItem);
                addSeparator();
                if (selectedTreeNode.isFolder() == true) {
                    add(renameFolderItem);
                    add(configureFolderItem);
                    add(sortFolderItem);
                } else {
                    add(configureBoardItem);
                }
                addSeparator();
                add(addBoardItem);
                add(addFolderItem);
                if (selectedTreeNode.isRoot() == false) {
                    add(removeNodeItem);
                }
                addSeparator();
                if (selectedTreeNode.isRoot() == false) {
                    add(cutNodeItem);
                }
                if (clipboard != null && selectedTreeNode.isFolder()) {
                    final String folderOrBoard3 =
                        ((clipboard.isFolder())
                            ? language.getString("BoardTree.popupmenu.folder")
                            : language.getString("BoardTree.popupmenu.board"));
                    pasteNodeItem.setText(language.getString("BoardTree.popupmenu.paste")
                            + " "
                            + folderOrBoard3
                            + " '"
                            + clipboard.getName()
                            + "'");
                    add(pasteNodeItem);
                }

                super.show(invoker, x, y);
            }
        }

        private void sortFolderSelected() {
            ((Folder)selectedTreeNode).sortChildren();
            model.nodeStructureChanged(selectedTreeNode);
        }

        private void renameFolderSelected() {
            MainFrame.getInstance().renameFolder( (Folder)selectedTreeNode );
        }
    }

    private class Listener extends MouseAdapter implements LanguageListener, ActionListener,
                                KeyListener, BoardUpdateThreadListener
    {
        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == configBoardMenuItem) {
                configureBoard(model.getSelectedNode());
            }
        }

        public void keyPressed(final KeyEvent e) {
            final char key = e.getKeyChar();
            pressedKey(key);
        }

        public void keyTyped(final KeyEvent e) {
        }

        public void keyReleased(final KeyEvent e) {
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == TofTree.this) {
                    showTofTreePopupMenu(e);
                }
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == TofTree.this) {
                    showTofTreePopupMenu(e);
                }
            }
        }

        public void boardUpdateThreadFinished(final BoardUpdateThread thread) {
            final int running =
                getRunningBoardUpdateThreads()
                    .getDownloadThreadsForBoard(thread.getTargetBoard())
                    .size();
            //+ getRunningBoardUpdateThreads().getUploadThreadsForBoard(thread.getTargetBoard()).size();
            if (running == 0) {
                // remove update state from board
                thread.getTargetBoard().setUpdating(false);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mainFrame.updateTofTree(thread.getTargetBoard());
                    }
                });
            }
        }

        public void boardUpdateThreadStarted(final BoardUpdateThread thread) {
            thread.getTargetBoard().setUpdating(true);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mainFrame.updateTofTree(thread.getTargetBoard());
                }
            });
        }
    }

    private class CellRenderer extends DefaultTreeCellRenderer {

        private final Border borderFlaggedAndStarredMsgs = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.red),    // outside
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue) ); // inside
        private final Border borderStarredMsgs = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 2, 0, 0),                // outside
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue) ); // inside
        private final Border borderFlaggedMsgs = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.red),    // outside
                BorderFactory.createEmptyBorder(0, 2, 0, 0) );              // inside
        private final Border borderEmpty = BorderFactory.createEmptyBorder(0, 4, 0, 0);

        private final ImageIcon writeAccessIcon;
        private final ImageIcon writeAccessNewIcon;
        private final ImageIcon readAccessIcon;
        private final ImageIcon readAccessNewIcon;
        private final ImageIcon boardIcon;
        private final ImageIcon boardNewIcon;
        private final ImageIcon boardSpammedIcon;

        private final ImageIcon sentMessagesFolderIcon;
        private final ImageIcon unsentMessagesFolderIcon;

        private Font boldFont = null;
        private Font normalFont = null;

        public CellRenderer() {
            boardIcon = new ImageIcon(getClass().getResource("/data/board.gif"));
            boardNewIcon = new ImageIcon(getClass().getResource("/data/boardnew.gif"));
            boardSpammedIcon = new ImageIcon(getClass().getResource("/data/boardspam.gif"));
            writeAccessIcon = new ImageIcon(getClass().getResource("/data/waboard.jpg"));
            writeAccessNewIcon = new ImageIcon(getClass().getResource("/data/waboardnew.jpg"));
            readAccessIcon = new ImageIcon(getClass().getResource("/data/raboard.jpg"));
            readAccessNewIcon = new ImageIcon(getClass().getResource("/data/raboardnew.jpg"));
            sentMessagesFolderIcon = new ImageIcon(getClass().getResource("/data/messagedark.gif"));
            unsentMessagesFolderIcon = new ImageIcon(getClass().getResource("/data/messagebright.gif"));
            this.setLeafIcon(new ImageIcon(getClass().getResource("/data/board.gif")));
            this.setClosedIcon(new ImageIcon(getClass().getResource("/data/closed.gif")));
            this.setOpenIcon(new ImageIcon(getClass().getResource("/data/open.gif")));

            final JTable dummyTable = new JTable();
            normalFont = dummyTable.getFont();
            boldFont = normalFont.deriveFont(Font.BOLD);
        }

        @Override
        public Component getTreeCellRendererComponent(
            final JTree tree,
            final Object value,
            final boolean sel,
            final boolean expanded,
            final boolean leaf,
            final int row,
            final boolean localHasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, localHasFocus);

            final AbstractNode node = (AbstractNode)value;

            final boolean containsNewMessage = node.containsNewMessages();

            if (node.isFolder()) {
                final Folder folder = (Folder) node;
                // if this is a folder, check board for new messages
                setText(folder.getName());
                if (containsNewMessage) {
                    setFont(boldFont);
                } else {
                    setFont(normalFont);
                }
                if( showFlaggedStarredIndicators ) {
                    setBorder(borderEmpty);
                } else {
                    setBorder(null);
                }

            } else if(node.isBoard()) {

                final Board board = (Board) node;
                // set the special text (board name + if new msg. a ' (2)' is appended and bold)
                if (containsNewMessage) {
                    setFont(boldFont);
                    if( showBoardUpdatedCount ) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(board.getName()).append(" (").append(board.getNewMessageCount()).append(") [");
                        sb.append(board.getTimesUpdatedCount()).append("]");
                        setText(sb.toString());
                    } else {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(board.getName()).append(" (").append(board.getNewMessageCount()).append(")");
                        setText(sb.toString());
                    }
                } else {
                    setFont(normalFont);
                    if( showBoardUpdatedCount ) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(board.getName()).append(" [").append(board.getTimesUpdatedCount()).append("]");
                        setText(sb.toString());
                    } else {
                        setText(board.getName());
                    }
                }

                // set the icon
                if (board.isPublicBoard()) {
                    if (containsNewMessage) {
                        setIcon(boardNewIcon);
                    } else {
                        setIcon(boardIcon);
                    }
                } else if (board.isSpammed()) {
                    setIcon(boardSpammedIcon);
                } else if (board.isWriteAccessBoard()) {
                    if (containsNewMessage) {
                        setIcon(writeAccessNewIcon);
                    } else {
                        setIcon(writeAccessIcon);
                    }
                } else if (board.isReadAccessBoard()) {
                    if (containsNewMessage) {
                        setIcon(readAccessNewIcon);
                    } else {
                        setIcon(readAccessIcon);
                    }
                }

                // for a board we set indicators if board contains flagged or starred messages
                if( showFlaggedStarredIndicators ) {
                    boolean hasStarred = board.hasStarredMessages();
                    boolean hasFlagged = board.hasFlaggedMessages();
                    if( hasStarred && !hasFlagged ) {
                        // unread and no marked
                        setBorder(borderStarredMsgs);
                    } else if( !hasStarred && hasFlagged ) {
                        // no unread and marked
                        setBorder(borderFlaggedMsgs);
                    } else if( !hasStarred && !hasFlagged ) {
                        // nothing
                        setBorder(borderEmpty);
                    } else {
                        // both
                        setBorder(borderFlaggedAndStarredMsgs);
                    }
                } else {
                    setBorder(null);
                }
            } else {
                // sent/unsent folder
                final AbstractNode folder = node;
                setText(folder.getName());
                setFont(normalFont);
                if( folder.isSentMessagesFolder() ) {
                    setIcon(sentMessagesFolderIcon);
                } else if( folder.isUnsentMessagesFolder() ) {
                    setIcon(unsentMessagesFolderIcon);
                }
                if( showFlaggedStarredIndicators ) {
                    setBorder(borderEmpty);
                } else {
                    setBorder(null);
                }
            }

            // maybe update visualization
            final boolean isUpdating;
            if( node.isBoard() && ((Board)node).isUpdating() ) {
                isUpdating = true;
            } else {
                isUpdating = false;
            }
            if (showBoardUpdateVisualization && isUpdating) {
                // set special updating colors
                Color c;
                c = (Color) settings.getObjectValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_BGCOLOR_NOT_SELECTED);
                setBackgroundNonSelectionColor(c);

                c = (Color) settings.getObjectValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_BGCOLOR_SELECTED);
                setBackgroundSelectionColor(c);

            } else {
                // refresh colours from the L&F
                setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
                setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
                setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
                setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
            }

            // set board description as tooltip
            if( showBoardDescriptionToolTips
                    && node.isBoard()
                    && ((Board)node).getDescription() != null
                    && ((Board)node).getDescription().length() > 0 )
            {
                setToolTipText(((Board)node).getDescription());
            } else {
                setToolTipText(null);
            }

            return this;
        }
    }

    private Language language;
    private SettingsClass settings;
    private MainFrame mainFrame;

    private final Listener listener = new Listener();

    private PopupMenuTofTree popupMenuTofTree;

    private static final Logger logger = Logger.getLogger(TofTree.class.getName());

    private final TofTreeModel model;

    private final JMenuItem configBoardMenuItem = new JMenuItem();

    private AbstractNode clipboard = null;

    private RunningBoardUpdateThreads runningBoardUpdateThreads = null;

    public TofTree(final TofTreeModel model) {
        super(model);
        this.model = model;

        showBoardDescriptionToolTips = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARDDESC_TOOLTIPS);
        showBoardUpdatedCount = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARD_UPDATED_COUNT);
        showBoardUpdateVisualization = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARD_UPDATE_VISUALIZATION);
        showFlaggedStarredIndicators = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR);
    }

    private PopupMenuTofTree getPopupMenuTofTree() {
        if (popupMenuTofTree == null) {
            popupMenuTofTree = new PopupMenuTofTree();
            language.addLanguageListener(popupMenuTofTree);
        }
        return popupMenuTofTree;
    }

    public void initialize() {

        language = Language.getInstance();
        language.addLanguageListener(listener);

        new TreeFindAction().install(this);

        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARDDESC_TOOLTIPS, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARD_UPDATED_COUNT, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARD_UPDATE_VISUALIZATION, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR, this);

        final MiscToolkit toolkit = MiscToolkit.getInstance();
        configBoardMenuItem.setIcon(toolkit.getScaledImage("/data/configure.gif", 16, 16));
        refreshLanguage();

        putClientProperty("JTree.lineStyle", "Angled"); // I like this look

        setRootVisible(true);
        setCellRenderer(new CellRenderer());
        setSelectionModel(model.getSelectionModel());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Add listeners
        addKeyListener(listener);
        addMouseListener(listener);
        configBoardMenuItem.addActionListener(listener);

        // enable tooltips for this tree
        ToolTipManager.sharedInstance().registerComponent(this);

        // load nodes from disk
        loadTree();

        // only select folder if a board in this folder was selected before
        // (this is a fix for a problem that occurs only on linux: collapse a folder with a selected
        //  board inside and the selection jumps to the root node rather than to the collapsed node)
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(final TreeExpansionEvent event) {
                final TreePath selectedPath = getSelectionPath();
                final TreePath collapsedPath = event.getPath();
                if( collapsedPath.isDescendant(selectedPath) ) {
                    setSelectionPath(event.getPath());
                }
            }
            public void treeExpanded(final TreeExpansionEvent event) {
            }
        });

        // enable the machine ;)
        runningBoardUpdateThreads = new RunningBoardUpdateThreads();
    }

    private void cutNode(final AbstractNode node) {
        if (node != null) {
            clipboard = node;
        }
    }

    private void pasteNode(final AbstractNode position) {
        if (clipboard == null) {
            return;
        }
        if (position == null || !position.isFolder()) {
            return; // We only allow pasting under folders
        }

        model.removeNode(clipboard, false);

        position.add(clipboard);
        clipboard = null;

        final int insertedIndex[] = { position.getChildCount() - 1 }; // last in list is the newly added
        model.nodesWereInserted(position, insertedIndex);
    }

    private void refreshLanguage() {
        configBoardMenuItem.setText(language.getString("BoardTree.popupmenu.configureSelectedBoard"));
    }

    /**
     * Get keyTyped for tofTree
     */
    public void pressedKey(final char key) {
        if (!isEditing()) {
            if (key == 'x' || key == 'X') {
                cutNode(model.getSelectedNode());
            }
            if (key == 'v' || key == 'V') {
                pasteNode(model.getSelectedNode());
            }
        }
    }

    @Override
    protected void processKeyEvent(final KeyEvent e) {
        // ugly hack to prevent the standard JTree idiom (selects nodes starting with pressed key)
        if( e.getID()==KeyEvent.KEY_TYPED && (e.getKeyChar() == 'n' || e.getKeyChar() == 'N') ) {
            MainFrame.getInstance().getMessagePanel().selectNextUnreadMessage();
        } else if( e.getID()==KeyEvent.KEY_TYPED && (e.getKeyChar() == 'b' || e.getKeyChar() == 'B') ) {
            MainFrame.getInstance().getMessagePanel().setTrustState_actionPerformed(MessagePanel.IdentityState.BAD);
        } else if( e.getID()==KeyEvent.KEY_TYPED && (e.getKeyChar() == 'c' || e.getKeyChar() == 'C') ) {
            MainFrame.getInstance().getMessagePanel().setTrustState_actionPerformed(MessagePanel.IdentityState.CHECK);
        } else if( e.getID()==KeyEvent.KEY_TYPED && (e.getKeyChar() == 'o' || e.getKeyChar() == 'O') ) {
            MainFrame.getInstance().getMessagePanel().setTrustState_actionPerformed(MessagePanel.IdentityState.OBSERVE);
        } else if( e.getID()==KeyEvent.KEY_TYPED && (e.getKeyChar() == 'g' || e.getKeyChar() == 'G') ) {
            MainFrame.getInstance().getMessagePanel().setTrustState_actionPerformed(MessagePanel.IdentityState.GOOD);
        } else if( Character.isLetter(e.getKeyChar()) || Character.isDigit(e.getKeyChar()) ) {
            // ignore
        } else {
            super.processKeyEvent(e);
        }
    }

    /**
     * Loads a tree description file.
     */
    private boolean loadTree() {
        final TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = settings.getValue(SettingsClass.DIR_CONFIG) + "boards.xml";
        // the call changes the toftree and loads nodes into it
        final File iniFile = new File(boardIniFilename);
        if( iniFile.exists() == false ) {
            logger.warning("boards.xml file not found, reading default file (will be saved to boards.xml on exit).");
            String defaultBoardsFile;
            if( FcpHandler.isFreenet05() ) {
                defaultBoardsFile = "boards.xml.default";
            } else {
                defaultBoardsFile = "boards.xml.default07";
            }
            boardIniFilename = settings.getValue(SettingsClass.DIR_CONFIG) + defaultBoardsFile;
        }

        final String unsentName = language.getString("UnsentMessages.folderName");
        final String sentName = language.getString("SentMessages.folderName");
        final UnsentMessagesFolder unsentMsgs = new UnsentMessagesFolder("["+unsentName+"]");
        final SentMessagesFolder sentMsgs = new SentMessagesFolder("["+sentName+"]");

        boolean loadWasOk = xmlio.loadBoardTree( this, model, boardIniFilename, unsentMsgs, sentMsgs );
        if( !loadWasOk ) {
            return loadWasOk;
        }

        // check if the board 'frost-announce' is contained in the list, add it if not found
        final String expectedPubkey;
        if( FcpHandler.isFreenet05() ) {
            expectedPubkey = FREENET_05_FROST_ANNOUNCE_PUBKEY;
        } else {
            expectedPubkey = FREENET_07_FROST_ANNOUNCE_PUBKEY;
        }

        final List<Board> existingBoards = model.getAllBoards();
        boolean boardFound = false;
        for( final Object element : existingBoards ) {
            final Board b = (Board)element;
            if( b.getName().equals(FROST_ANNOUNCE_NAME) ) {
                boardFound = true;
                // check if pubkey is correct
                if( b.getPublicKey().equals(expectedPubkey) == false ) {
                    b.setPublicKey(expectedPubkey);
                    break;
                }
            }
        }
        if( !boardFound ) {
            final Board newBoard = new Board(FROST_ANNOUNCE_NAME, "Announcement of new Frost versions");
            newBoard.setPublicKey(expectedPubkey);
            final Folder root = (Folder)model.getRoot();
            model.addNodeToTree(newBoard, root);
        }

        return loadWasOk;
    }

    public void autoSave() throws StorageException {
        save();
    }
    public void exitSave() throws StorageException {
        save();
    }

    /**
     * Save TOF tree's content to a file
     */
    public void save() throws StorageException {

        // board list is important, create bak files bak, bak2, bak3, bak4
        final String configDir = settings.getValue(SettingsClass.DIR_CONFIG);
        final File xmlFile = new File(configDir + "boards.xml");
        final File bakFile = new File(configDir + "boards.xml.bak");
        final File bak2File = new File(configDir + "boards.xml.bak2");
        final File bak3File = new File(configDir + "boards.xml.bak3");
        final File bak4File = new File(configDir + "boards.xml.bak4");

        // first check for bak4 and maybe delete
        if( bak4File.isFile() ) {
            bak4File.delete();
        }

        // maybe rename bak3 to bak4
        if( bak3File.isFile() ) {
            bak3File.renameTo(bak4File);
        }

        // maybe rename bak2 to bak3
        if( bak2File.isFile() ) {
            bak2File.renameTo(bak3File);
        }

        // maybe rename bak to bak2
        if( bakFile.isFile() ) {
            bakFile.renameTo(bak2File);
        }

        // maybe rename xml to bak
        if( xmlFile.isFile() ) {
            xmlFile.renameTo(bakFile);
        }

        // save to xml file
        final TofTreeXmlIO xmlio = new TofTreeXmlIO();
        // the method scans the toftree
        if (!xmlio.saveBoardTree( this, model, xmlFile )) {
            throw new StorageException("Error while saving the TofTree.");
        }
    }

    /**
     * Opens dialog, gets new name for board, checks for double names, adds node to tree
     */
    public void createNewBoard(final Frame parent) {
        boolean isDone = false;

        while (!isDone) {
            final NewBoardDialog dialog = new NewBoardDialog(parent);
            dialog.setVisible(true);

            if (dialog.getChoice() == NewBoardDialog.CHOICE_CANCEL) {
                isDone = true; //cancelled
            } else {
                final String boardName = dialog.getBoardName();
                final String boardDescription = dialog.getBoardDescription();

                if (model.getBoardByName(boardName) != null) {
                    JOptionPane.showMessageDialog(
                        parent,
                        language.formatMessage("BoardTree.duplicateNewBoardNameError.body", boardName),
                        language.getString("BoardTree.duplicateNewBoardNameError.title"),
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    final Board newBoard = new Board(boardName, boardDescription);
                    model.addNodeToTree(newBoard);
                    // maybe this boardfolder already exists, scan for new messages
                    TOF.getInstance().searchNewMessages(newBoard);
                    isDone = true; //added
                }
            }
        }
    }

    /**
     * Checks if board is already existent, adds board to board tree.
     * @param bname
     * @param bpubkey
     * @param bprivkey
     * @param description
     */
    private void addNewBoard(final String bname, final String bpubkey, final String bprivkey, final String description) {
        final Board existingBoard = model.getBoardByName(bname);
        if (existingBoard != null) {
            final int answer =
                JOptionPane.showConfirmDialog(
                    getTopLevelAncestor(),
                    language.formatMessage("BoardTree.overWriteBoardConfirmation.body", bname),
                    language.getString("BoardTree.overWriteBoardConfirmation.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                // overwrite keys and description of existing board
                existingBoard.setPublicKey(bpubkey);
                existingBoard.setPrivateKey(bprivkey);
                existingBoard.setDescription(description);
                model.nodeChanged(existingBoard); // refresh board icon
            }
        } else {
            final Board newBoard = new Board(bname, bpubkey, bprivkey, description);
            model.addNodeToTree(newBoard);
            // maybe this boardfolder already exists, scan for new messages
            TOF.getInstance().searchNewMessages(newBoard);
        }
    }

    /**
     * Checks if board is already existent, adds board to board tree.
     * @param fbobj
     */
    public void addNewBoard(final Board fbobj) {
        addNewBoard(
            fbobj.getName(),
            fbobj.getPublicKey(),
            fbobj.getPrivateKey(),
            fbobj.getDescription());
    }

    /**
     * Opens dialog, gets new name for folder, checks for double names, adds node to tree
     * @param parent
     */
    public void createNewFolder(final Frame parent) {
        String nodeName = null;
        do {
            final Object nodeNameOb =
                JOptionPane.showInputDialog(
                    parent,
                    language.getString("BoardTree.newFolderDialog.body") + ":",
                    language.getString("BoardTree.newFolderDialog.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    language.getString("BoardTree.newFolderDialog.defaultName"));

            nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString());

            if (nodeName == null) {
                return; // cancelled
            }

        } while (nodeName.length() == 0);

        model.addNodeToTree(new Folder(nodeName));
    }

    /**
     * Removes the given tree node, asks before deleting.
     * @param node
     */
    public void removeNode(final AbstractNode node) {
        int answer;
        if (node.isFolder()) {
            answer = JOptionPane.showConfirmDialog(
                    this,
                    language.formatMessage("BoardTree.removeFolderConfirmation.body", node.getName()),
                    language.formatMessage("BoardTree.removeFolderConfirmation.title", node.getName()),
                    JOptionPane.YES_NO_OPTION);
        } else if(node.isBoard()) {
            answer = JOptionPane.showConfirmDialog(
                    this,
                    language.formatMessage("BoardTree.removeBoardConfirmation.body", node.getName()),
                    language.formatMessage("BoardTree.removeBoardConfirmation.title", node.getName()),
                    JOptionPane.YES_NO_OPTION);
        } else {
            return;
        }

        if (answer == JOptionPane.NO_OPTION) {
            return;
        }

        // delete node from tree
        model.removeNode(node, true);
    }

    public void setSettings(final SettingsClass settings) {
        this.settings = settings;
    }

    public void setMainFrame(final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private void showTofTreePopupMenu(final MouseEvent e) {
        getPopupMenuTofTree().show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * starts update for the selected board, or for all childs (and their childs) of a folder
     */
    private void refreshNode(final AbstractNode node) {
        if (node == null) {
            return;
        }

        if (node.isBoard()) {
            if (((Board)node).isManualUpdateAllowed()) {
                updateBoard((Board)node);
            }
        } else if (node.isFolder()) {
            // update all childs recursiv
            final Enumeration<AbstractNode> leafs = node.children();
            while (leafs.hasMoreElements()) {
                refreshNode(leafs.nextElement());
            }
        }
    }

    public RunningBoardUpdateThreads getRunningBoardUpdateThreads() {
        return runningBoardUpdateThreads;
    }

    /**
     * News | Configure Board action performed
     * @param board
     */
    public void configureBoard(final AbstractNode board) {
        if (board == null ) {
            return;
        }
        final BoardSettingsFrame newFrame = new BoardSettingsFrame(mainFrame, board);
        newFrame.runDialog(); // all needed updates of boards are done by the dialog before it closes
    }

    /**
     * Starts the board update threads, getRequest thread and update id thread.
     * Checks for each type of thread if its already running, and starts allowed
     * not-running threads for this board.
     * @param board
     */
    public void updateBoard(final Board board) {
        if (board == null || !board.isBoard()) {
            return;
        }

        // TODO: the gui buttons for boardupdate should be disabled instead
        if (!Core.isFreenetOnline()) {
        	return;
        }

        boolean threadStarted = false;

        // download the messages of today
        if (getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_TODAY) == false) {
            getRunningBoardUpdateThreads().startMessageDownloadToday(
                board,
                settings,
                listener);
            logger.info("Starting update (MSG_TODAY) of " + board.getName());
            threadStarted = true;
        }

        final long now = System.currentTimeMillis();

        if (getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_BACK) == false) {

            // get the older messages, if configured start backload only after 12 hours
            final long before12hours = now - (12L * 60L * 60L * 1000L); // 12 hours
            boolean downloadCompleteBackload;
            if( Core.frostSettings.getBoolValue(SettingsClass.ALWAYS_DOWNLOAD_MESSAGES_BACKLOAD) == false
                    && before12hours < board.getLastBackloadUpdateFinishedMillis() )
            {
                downloadCompleteBackload = false;
            } else {
                // we start a complete backload
                downloadCompleteBackload = true;
            }

            getRunningBoardUpdateThreads().startMessageDownloadBack(board, settings, listener, downloadCompleteBackload);
            logger.info("Starting update (MSG_BACKLOAD) of " + board.getName());
            threadStarted = true;
        }

        // if there was a new thread started, update the lastUpdateStartTimeMillis
        if (threadStarted == true) {
            board.setLastUpdateStartMillis(now);
            board.incTimesUpdatedCount();
        }
    }

    /**
     * Fires a nodeChanged (redraw) for all boards.
     * ONLY used to redraw tree after run of OptionsFrame.
     */
    public void updateTree() {
        // fire update for node
        final Enumeration<AbstractNode> e = (model.getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            model.nodeChanged(e.nextElement());
        }
    }
    protected JMenuItem getConfigBoardMenuItem() {
        return configBoardMenuItem;
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_BOARDDESC_TOOLTIPS)) {
            showBoardDescriptionToolTips = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARDDESC_TOOLTIPS);
        } else if (evt.getPropertyName().equals(SettingsClass.SHOW_BOARD_UPDATED_COUNT)) {
            showBoardUpdatedCount = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARD_UPDATED_COUNT);
            updateTree(); // redraw tree nodes
        } else if (evt.getPropertyName().equals(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR)) {
            showFlaggedStarredIndicators = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR);
            updateTree(); // redraw tree nodes
        } else if (evt.getPropertyName().equals(SettingsClass.SHOW_BOARD_UPDATE_VISUALIZATION)) {
            showBoardUpdateVisualization = Core.frostSettings.getBoolValue(SettingsClass.SHOW_BOARD_UPDATE_VISUALIZATION);
            updateTree(); // redraw tree nodes
        }
    }
}
