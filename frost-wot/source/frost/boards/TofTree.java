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
import frost.util.gui.translation.*;

public class TofTree extends JDragTree implements Savable, PropertyChangeListener {

    // pubkey for 0.5: "SSK@7i~oLj~57mQVRrKfMxYgLULJ2r0PAgM"
    // pubkey for 0.7: "SSK@ub2QMcPy4jmtmqyEIML0cDdbbSTFGBgX3jEYLGoN9lg,IUYrv~GBW0~dn6k3orf9CRKUBz9CLZSA6wGrax73BCk,AQABAAE"
    // NEW pubkey for 0.7: "SSK@l4YxTKAc-sCho~6w-unV6pl-uxIbfuGnGRzo3BJH0ck,4N48yl8E4rh9UPPV26Ev1ZGrRRgeGOTgw1Voka6lk4g,AQACAAE"
    private static final String FROST_ANNOUNCE_NAME = "frost-announce";
    private static final String FREENET_05_FROST_ANNOUNCE_PUBKEY = "SSK@7i~oLj~57mQVRrKfMxYgLULJ2r0PAgM";
    //FIXME: new keys
    private static final String FREENET_07_FROST_ANNOUNCE_PUBKEY = "SSK@ub2QMcPy4jmtmqyEIML0cDdbbSTFGBgX3jEYLGoN9lg,IUYrv~GBW0~dn6k3orf9CRKUBz9CLZSA6wGrax73BCk,AQABAAE";
    
    private boolean showBoardDescriptionToolTips;
    private boolean showBoardUpdatedCount;
    private boolean showBoardUpdateVisualization;
    private boolean showFlaggedStarredIndicators;

    private class PopupMenuTofTree
        extends JSkinnablePopupMenu
        implements LanguageListener, ActionListener {

        private JMenuItem addBoardItem = new JMenuItem();
        private JMenuItem addFolderItem = new JMenuItem();
        private JMenuItem cancelItem = new JMenuItem();
        private JMenuItem configureBoardItem = new JMenuItem();
        private JMenuItem configureFolderItem = new JMenuItem();
        private JMenuItem cutNodeItem = new JMenuItem();

        private JMenuItem descriptionItem = new JMenuItem();
        private JMenuItem pasteNodeItem = new JMenuItem();
        private JMenuItem refreshItem = new JMenuItem();
        private JMenuItem removeNodeItem = new JMenuItem();
        private JMenuItem renameFolderItem = new JMenuItem();

        private JMenuItem markAllReadItem = new JMenuItem();

        private Board selectedTreeNode = null;
        private JMenuItem sortFolderItem = new JMenuItem();

        public PopupMenuTofTree() {
            super();
            initialize();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            final Object source = e.getSource();

            frost.util.gui.FrostSwingWorker worker = new frost.util.gui.FrostSwingWorker(this) {

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
                    }
                }

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

            MiscToolkit miscToolkit = MiscToolkit.getInstance();
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
        }

        /* (non-Javadoc)
         * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
         */
        public void languageChanged(LanguageEvent event) {
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
            cancelItem.setText(language.getString("Common.cancel"));
            sortFolderItem.setText(language.getString("BoardTree.popupmenu.sortFolder"));
            markAllReadItem.setText(language.getString("BoardTree.popupmenu.markAllMessagesRead"));
            renameFolderItem.setText(language.getString("BoardTree.popupmenu.renameFolder"));
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

        /* (non-Javadoc)
         * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
         */
        public void show(Component invoker, int x, int y) {
            int selRow = getRowForLocation(x, y);
// FIXME: NEW NODE, popupmenu!!!
            if (selRow != -1) { // only if a node is selected
                removeAll();

                TreePath selPath = getPathForLocation(x, y);
                selectedTreeNode = (Board) selPath.getLastPathComponent();

                String folderOrBoard1 =
                    ((selectedTreeNode.isFolder())
                        ? language.getString("BoardTree.popupmenu.Folder")
                        : language.getString("BoardTree.popupmenu.Board"));
                String folderOrBoard2 =
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
                    String folderOrBoard3 =
                        ((clipboard.isFolder())
                            ? language.getString("BoardTree.popupmenu.folder")
                            : language.getString("BoardTree.popupmenu.board"));
                    pasteNodeItem.setText(
                            language.getString("BoardTree.popupmenu.paste")
                            + " "
                            + folderOrBoard3
                            + " '"
                            + clipboard.getName()
                            + "'");
                    add(pasteNodeItem);
                }
                addSeparator();
                add(cancelItem);

                super.show(invoker, x, y);
            }
        }

        private void sortFolderSelected() {
            selectedTreeNode.sortChildren();
            model.nodeStructureChanged(selectedTreeNode);
        }

        private void renameFolderSelected() {
            MainFrame.getInstance().renameFolder( selectedTreeNode );
        }
    }

    private class Listener extends MouseAdapter implements LanguageListener, ActionListener,
                                KeyListener, BoardUpdateThreadListener  {

        /* (non-Javadoc)
         * @see frost.util.gui.translation.LanguageListener#languageChanged(frost.util.gui.translation.LanguageEvent)
         */
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == configBoardMenuItem) {
                configureBoard(model.getSelectedNode());
            }
        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {
            char key = e.getKeyChar();
            pressedKey(key);
        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
         */
        public void keyTyped(KeyEvent e) {
            // Nothing here
        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent e) {
            // Nothing here
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == TofTree.this) {
                    showTofTreePopupMenu(e);
                }
            }
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == TofTree.this) {
                    showTofTreePopupMenu(e);
                }
            }
        }

        /* (non-Javadoc)
         * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadFinished(frost.threads.BoardUpdateThread)
         */
        public void boardUpdateThreadFinished(final BoardUpdateThread thread) {
            int running =
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

        /* (non-Javadoc)
         * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadStarted(frost.threads.BoardUpdateThread)
         */
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

        private Border borderFlaggedAndStarredMsgs = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.red),    // outside
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue) ); // inside
        private Border borderStarredMsgs = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 2, 0, 0),                // outside
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue) ); // inside
        private Border borderFlaggedMsgs = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.red),    // outside
                BorderFactory.createEmptyBorder(0, 2, 0, 0) );              // inside
        private Border borderEmpty = BorderFactory.createEmptyBorder(0, 4, 0, 0);

        private final ImageIcon writeAccessIcon;
        private final ImageIcon writeAccessNewIcon;
        private final ImageIcon readAccessIcon;
        private final ImageIcon readAccessNewIcon;
        private final ImageIcon boardIcon;
        private final ImageIcon boardNewIcon;
        private final ImageIcon boardSpammedIcon;

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
            this.setLeafIcon(new ImageIcon(getClass().getResource("/data/board.gif")));
            this.setClosedIcon(new ImageIcon(getClass().getResource("/data/closed.gif")));
            this.setOpenIcon(new ImageIcon(getClass().getResource("/data/open.gif")));

            JTable dummyTable = new JTable();
            normalFont = dummyTable.getFont();
            boldFont = normalFont.deriveFont(Font.BOLD);
        }

        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean localHasFocus) 
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, localHasFocus);

            Board board = null;
            if (value instanceof Board) {
                board = (Board) value;
            } else {
                logger.severe(
                    "Error - TofTreeCellRenderer: got a tree value wich is no FrostBoardObject:\n"
                        + "   node value='"
                        + value
                        + "'  ;  node class='"
                        + value.getClass()
                        + "'\n"
                        + "This should never happen, please report the error.");
                return this;
            }

            boolean containsNewMessage = board.containsNewMessages();

            if (board.isFolder()) {
                // if this is a folder, check board for new messages
                setText(board.getName());
                if (containsNewMessage) {
                    setFont(boldFont);
                } else {
                    setFont(normalFont);
                }
                setBorder(borderEmpty);
            } else if(board.isBoard()) {
                // set the special text (board name + if new msg. a ' (2)' is appended and bold)
                if (containsNewMessage) {
                    setFont(boldFont);
                    if( showBoardUpdatedCount ) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(board.getName()).append(" (").append(board.getNewMessageCount()).append(") [");
                        sb.append(board.getTimesUpdatedCount()).append("]");
                        setText(sb.toString());
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(board.getName()).append(" (").append(board.getNewMessageCount()).append(")");
                        setText(sb.toString());
                    }
                } else {
                    setFont(normalFont);
                    if( showBoardUpdatedCount ) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(board.getName()).append(" [").append(board.getTimesUpdatedCount()).append("]");
                        setText(sb.toString());
                    } else {
                        setText(board.getName());
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
            }

            // maybe update visualization
            if (showBoardUpdateVisualization && board.isUpdating() == true) {
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

            // set the icon
            if (leaf == true) {
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
            }
            
            // set board description as tooltip
            if( showBoardDescriptionToolTips && 
                board.getDescription() != null && 
                board.getDescription().length() > 0 ) 
            {
                setToolTipText(board.getDescription());
            } else {
                setToolTipText(null);
            }
            
            return this;
        }

    }

    private Language language;
    private SettingsClass settings;
    private MainFrame mainFrame;

    private Listener listener = new Listener();

    private PopupMenuTofTree popupMenuTofTree;

    private static Logger logger = Logger.getLogger(TofTree.class.getName());

    private TofTreeModel model;

    private JMenuItem configBoardMenuItem = new JMenuItem();

    private Board clipboard = null;

    private RunningBoardUpdateThreads runningBoardUpdateThreads = null;

    public TofTree(TofTreeModel model) {
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
        
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARDDESC_TOOLTIPS, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARD_UPDATED_COUNT, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARD_UPDATE_VISUALIZATION, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR, this);

        MiscToolkit toolkit = MiscToolkit.getInstance();
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
            public void treeCollapsed(TreeExpansionEvent event) {
                TreePath selectedPath = getSelectionPath();
                TreePath collapsedPath = event.getPath();
                if( collapsedPath.isDescendant(selectedPath) ) {
                    setSelectionPath(event.getPath());
                }
            }
            public void treeExpanded(TreeExpansionEvent event) {
            }
        });

        // enable the machine ;)
        runningBoardUpdateThreads = new RunningBoardUpdateThreads();
    }

    private void cutNode(Board node) {
        if (node != null) {
            clipboard = node;
        }
    }

    private void pasteNode(Board position) {
        if (clipboard == null) {
            return;
        }
        if (position == null || !position.isFolder()) {
            return; // We only allow pasting under folders
        }

        model.removeNode(clipboard, false);

        position.add(clipboard);
        clipboard = null;

        int insertedIndex[] = { position.getChildCount() - 1 }; // last in list is the newly added
        model.nodesWereInserted(position, insertedIndex);
    }

    private void refreshLanguage() {
        configBoardMenuItem.setText(language.getString("BoardTree.popupmenu.configureSelectedBoard"));
    }

    /**
     * Get keyTyped for tofTree
     */
    public void pressedKey(char key ) {
        if (!isEditing()) {
            if (key == KeyEvent.VK_DELETE)
                removeNode(model.getSelectedNode());
            if (key == KeyEvent.VK_N)
                createNewBoard(mainFrame);
            if (key == KeyEvent.VK_X)
                cutNode(model.getSelectedNode());
            if (key == KeyEvent.VK_V)
                pasteNode(model.getSelectedNode());
        }
    }

    /**
     * Loads a tree description file.
     */
    private boolean loadTree() {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = settings.getValue(SettingsClass.DIR_CONFIG) + "boards.xml";
        // the call changes the toftree and loads nodes into it
        File iniFile = new File(boardIniFilename);
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
        
        boolean loadWasOk = xmlio.loadBoardTree( this, model, boardIniFilename );
        if( !loadWasOk ) {
            return loadWasOk;
        }

        // check if the board 'frost-announce' is contained in the list, add it if not found
        String expectedPubkey;
        if( FcpHandler.isFreenet05() ) {
            expectedPubkey = FREENET_05_FROST_ANNOUNCE_PUBKEY;
        } else {
            expectedPubkey = FREENET_07_FROST_ANNOUNCE_PUBKEY;
        }

        List existingBoards = model.getAllBoards();
        boolean boardFound = false;
        for(Iterator i=existingBoards.iterator(); i.hasNext(); ) {
            Board b = (Board)i.next();
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
            Board newBoard = new Board(FROST_ANNOUNCE_NAME, false);
            newBoard.setPublicKey(expectedPubkey);
            Board root = (Board)model.getRoot();
            model.addNodeToTree(newBoard, root);
        }
        
        return loadWasOk;
    }

    /**
     * Save TOF tree's content to a file
     */
    public void save() throws StorageException {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = settings.getValue(SettingsClass.DIR_CONFIG) + "boards.xml";
        File check = new File( boardIniFilename );
        if( check.exists() ) {
            // rename old file to .bak, overwrite older .bak
            String bakBoardIniFilename = settings.getValue(SettingsClass.DIR_CONFIG) + "boards.xml.bak";
            File bakFile = new File(bakBoardIniFilename);
            if( bakFile.exists() ) {
                bakFile.delete();
            }
            check.renameTo(bakFile);
        }
        // the method scans the toftree
        if (!xmlio.saveBoardTree( this, model, boardIniFilename )) {
            throw new StorageException("Error while saving the TofTree.");
        }
    }

    /**
     * Opens dialog, gets new name for board, checks for double names, adds node to tree
     */
    public void createNewBoard(Frame parent) {
        boolean isDone = false;

        while (!isDone) {
            NewBoardDialog dialog = new NewBoardDialog(parent);
            dialog.setVisible(true);

            if (dialog.getChoice() == NewBoardDialog.CHOICE_CANCEL) {
                isDone = true; //cancelled
            } else {
                String boardName = dialog.getBoardName();
                String boardDescription = dialog.getBoardDescription();

                if (model.getBoardByName(boardName) != null) {
                    JOptionPane.showMessageDialog(
                        parent,
                        language.formatMessage("BoardTree.duplicateNewBoardNameError.body", boardName),
                        language.getString("BoardTree.duplicateNewBoardNameError.title"),
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    Board newBoard = new Board(boardName, boardDescription);
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
    private void addNewBoard(String bname, String bpubkey, String bprivkey, String description) {
        if (model.getBoardByName(bname) != null) {
            int answer =
                JOptionPane.showConfirmDialog(
                    getTopLevelAncestor(),
                    language.formatMessage("BoardTree.overWriteBoardConfirmation.body", bname),
                    language.getString("BoardTree.overWriteBoardConfirmation.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.NO_OPTION) {
                return; // do not add
            }
        }
        Board newBoard = new Board(bname, bpubkey, bprivkey, description);
        model.addNodeToTree(newBoard);
        // maybe this boardfolder already exists, scan for new messages
        TOF.getInstance().searchNewMessages(newBoard);
    }

    /**
     * Checks if board is already existent, adds board to board tree.
     * @param fbobj
     */
    public void addNewBoard(Board fbobj) {
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
    public void createNewFolder(Frame parent) {
        String nodeName = null;
        do {
            Object nodeNameOb =
                JOptionPane.showInputDialog(
                    parent,
                    language.getString("BoardTree.newFolderDialog.body") + ":",
                    language.getString("BoardTree.newFolderDialog.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    language.getString("BoardTree.newFolderDialog.defaultName"));

            nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString());

            if (nodeName == null)
                return; // cancelled

        } while (nodeName.length() == 0);

        model.addNodeToTree(new Board(nodeName, true));
    }

    /**
     * Removes the given tree node, asks before deleting.
     * @param node
     */
    public void removeNode(Board node) {
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

    public void setSettings(SettingsClass settings) {
        this.settings = settings;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private void showTofTreePopupMenu(MouseEvent e) {
        getPopupMenuTofTree().show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * starts update for the selected board, or for all childs (and their childs) of a folder
     */
    private void refreshNode(Board node) {
        if (node == null)
            return;

        if (node.isBoard()) {
            if (node.isManualUpdateAllowed()) {
                updateBoard(node);
            }
        } else if (node.isFolder()) {
            // update all childs recursiv
            Enumeration leafs = node.children();
            while (leafs.hasMoreElements()) {
                refreshNode((Board) leafs.nextElement());
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
    public void configureBoard(Board board) {
        if (board == null ) {
            return;
        }

        BoardSettingsFrame newFrame = new BoardSettingsFrame(mainFrame, board);
        newFrame.runDialog(); // all needed updates of boards are done by the dialog before it closes
    }

    /**
     * Starts the board update threads, getRequest thread and update id thread.
     * Checks for each type of thread if its already running, and starts allowed
     * not-running threads for this board.
     * @param board
     */
    public void updateBoard(Board board) {
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

        long now = System.currentTimeMillis();

        if (getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_BACK) == false) {
            
            // get the older messages, if configured start backload only after 12 hours
            long before12hours = now - (12L * 60L * 60L * 1000L); // 12 hours
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
        Enumeration e = ((Board) model.getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            model.nodeChanged(((Board) e.nextElement()));
        }
    }
    protected JMenuItem getConfigBoardMenuItem() {
        return configBoardMenuItem;
    }

    public void propertyChange(PropertyChangeEvent evt) {
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
