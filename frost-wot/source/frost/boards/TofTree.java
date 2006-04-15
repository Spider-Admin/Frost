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
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import frost.*;
import frost.fcp.*;
import frost.gui.*;
import frost.gui.objects.*;
import frost.storage.*;
import frost.threads.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class TofTree extends JDragTree implements Savable {

    // pubkey for 0.5: "SSK@7i~oLj~57mQVRrKfMxYgLULJ2r0PAgM"
    // pubkey for 0.7: "SSK@ub2QMcPy4jmtmqyEIML0cDdbbSTFGBgX3jEYLGoN9lg,IUYrv~GBW0~dn6k3orf9CRKUBz9CLZSA6wGrax73BCk,AQABAAE"
    private static final String FROST_ANNOUNCE_NAME = "frost-announce";
    private static final String FREENET_05_FROST_ANNOUNCE_PUBKEY = "SSK@7i~oLj~57mQVRrKfMxYgLULJ2r0PAgM";
    private static final String FREENET_07_FROST_ANNOUNCE_PUBKEY = "SSK@ub2QMcPy4jmtmqyEIML0cDdbbSTFGBgX3jEYLGoN9lg,IUYrv~GBW0~dn6k3orf9CRKUBz9CLZSA6wGrax73BCk,AQABAAE";

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

            frost.util.gui.SwingWorker worker = new frost.util.gui.SwingWorker(this) {

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
            addBoardItem.setText(language.getString("Add new board"));
            addFolderItem.setText(language.getString("Add new folder"));
            configureBoardItem.setText(language.getString("Configure selected board"));
            configureFolderItem.setText(language.getString("Configure selected folder"));
            cancelItem.setText(language.getString("Cancel"));
            sortFolderItem.setText(language.getString("Sort folder"));
            markAllReadItem.setText(language.getString("Mark ALL messages read"));
            renameFolderItem.setText(language.getString("Rename folder"));
        }

        private void refreshSelected() {
            refreshNode(selectedTreeNode);
        }

        private void markAllReadSelected() {
            markAllRead(selectedTreeNode);
        }

        private void removeNodeSelected() {
            removeNode(selectedTreeNode);
        }

        /* (non-Javadoc)
         * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
         */
        public void show(Component invoker, int x, int y) {
            int selRow = getRowForLocation(x, y);

            if (selRow != -1) { // only if a node is selected
                removeAll();

                TreePath selPath = getPathForLocation(x, y);
                selectedTreeNode = (Board) selPath.getLastPathComponent();

                String folderOrBoard1 =
                    ((selectedTreeNode.isFolder())
                        ? language.getString("Folder")
                        : language.getString("Board"));
                String folderOrBoard2 =
                    ((selectedTreeNode.isFolder())
                        ? language.getString("folder")
                        : language.getString("board"));

                descriptionItem.setText(folderOrBoard1 + " : " + selectedTreeNode.getName());
                refreshItem.setText(language.getString("Refresh") + " " + folderOrBoard2);
                removeNodeItem.setText(language.getString("Remove") + " " + folderOrBoard2);
                cutNodeItem.setText(language.getString("Cut") + " " + folderOrBoard2);

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
                            ? language.getString("folder")
                            : language.getString("board"));
                    pasteNodeItem.setText(
                            language.getString("Paste")
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
            MainFrame.getInstance().renameNode( selectedTreeNode );
        }
    }

    private class Listener extends MouseAdapter implements LanguageListener, ActionListener,
                                KeyListener, TreeSelectionListener, BoardUpdateThreadListener  {

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
            if (e.getSource() == cutBoardButton) {
                cutNode(model.getSelectedNode());
            }
            if (e.getSource() == pasteBoardButton) {
                pasteNode(model.getSelectedNode());
            }
            if (e.getSource() == configBoardButton) {
                configureBoard(model.getSelectedNode());
            }
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
         * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
         */
        public void valueChanged(TreeSelectionEvent e) {
            if (e.getSource() == TofTree.this) {
                selectionChanged();
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

        ImageIcon writeAccessIcon;
        ImageIcon writeAccessNewIcon;
        ImageIcon readAccessIcon;
        ImageIcon readAccessNewIcon;
        ImageIcon boardIcon;
        ImageIcon boardNewIcon;
        ImageIcon boardSpammedIcon;
        String fileSeparator;

        Font boldFont = null;
        Font normalFont = null;

        /**
         *
         */
        public CellRenderer() {
            fileSeparator = System.getProperty("file.separator");
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

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

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
            } else {
                // set the special text (board name + if new msg. a ' (2)' is appended and bold)
                if (containsNewMessage) {
                    setFont(boldFont);
                    setText(board.getName() + " (" + board.getNewMessageCount() + ")");
                } else {
                    setFont(normalFont);
                    setText(board.getName());
                }
            }

            // maybe update visualization
            if (settings.getBoolValue("boardUpdateVisualization")
                && board.isUpdating() == true) {
                // set special updating colors
                Color c;
                c = (Color) settings.getObjectValue("boardUpdatingNonSelectedBackgroundColor");
                setBackgroundNonSelectionColor(c);

                c = (Color) settings.getObjectValue("boardUpdatingSelectedBackgroundColor");
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
            return this;
        }

    }

    private Language language;
    private SettingsClass settings;
    private Core core;
    private MainFrame mainFrame;

    private Listener listener = new Listener();

    private PopupMenuTofTree popupMenuTofTree;

    private static Logger logger = Logger.getLogger(TofTree.class.getName());

    private TofTreeModel model;

    private JButton cutBoardButton = new JButton();
    private JButton pasteBoardButton = new JButton();
    private JButton configBoardButton = new JButton();

    private JMenuItem configBoardMenuItem = new JMenuItem();

    private Board clipboard = null;

    private RunningBoardUpdateThreads runningBoardUpdateThreads = null;

    public TofTree(TofTreeModel model) {
        super(model);
        this.model = model;
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

        MiscToolkit toolkit = MiscToolkit.getInstance();
        cutBoardButton.setIcon(new ImageIcon(getClass().getResource("/data/cut.gif")));
        pasteBoardButton.setIcon(new ImageIcon(getClass().getResource("/data/paste.gif")));
        configBoardButton.setIcon(new ImageIcon(getClass().getResource("/data/configure.gif")));
        toolkit.configureButton(cutBoardButton, "/data/cut_rollover.gif");
        toolkit.configureButton(pasteBoardButton, "/data/paste_rollover.gif");
        toolkit.configureButton(configBoardButton, "/data/configure_rollover.gif");
        configBoardMenuItem.setIcon(toolkit.getScaledImage("/data/configure.gif", 16, 16));
        refreshLanguage();

        pasteBoardButton.setEnabled(false);

        putClientProperty("JTree.lineStyle", "Angled"); // I like this look

        setRootVisible(true);
        setCellRenderer(new CellRenderer());
        setSelectionModel(model.getSelectionModel());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Add listeners
        addTreeSelectionListener(listener);
        addKeyListener(listener);
        addMouseListener(listener);
        cutBoardButton.addActionListener(listener);
        pasteBoardButton.addActionListener(listener);
        configBoardButton.addActionListener(listener);
        configBoardMenuItem.addActionListener(listener);

        // load nodes from disk
        loadTree();

        // enable the machine ;)
        runningBoardUpdateThreads = new RunningBoardUpdateThreads(mainFrame);
    }

    /**
     * @param cuttedNode
     */
    public void cutNode(Board node) {
        if (node != null) {
            model.removeNode(node);
            clipboard = node;
            pasteBoardButton.setEnabled(true);
        }
    }

    /**
     * @param position
     * @return
     */
    public void pasteNode(Board position) {
        if (clipboard == null) {
            pasteBoardButton.setEnabled(false);
            return;
        }
        if (position == null || !position.isFolder()) {
            return; // We only allow pasting under folders
        }

        position.add(clipboard);
        clipboard = null;
        pasteBoardButton.setEnabled(false);

        int insertedIndex[] = { position.getChildCount() - 1 }; // last in list is the newly added
        model.nodesWereInserted(position, insertedIndex);
    }

    /**
     *
     */
    private void refreshLanguage() {
        cutBoardButton.setToolTipText(language.getString("Cut board"));
        pasteBoardButton.setToolTipText(language.getString("Paste board"));
        configBoardButton.setToolTipText(language.getString("Configure board"));
        configBoardMenuItem.setText(language.getString("Configure selected board"));
    }

    /**
     * Get keyTyped for tofTree
     * @param e
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
     * Loads a tree description file
     */
    private boolean loadTree() {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = settings.getValue("config.dir") + "boards.xml";
        // the call changes the toftree and loads nodes into it
        File iniFile = new File(boardIniFilename);
        if( iniFile.exists() == false ) {
            logger.warning("boards.xml file not found, reading default file (will be saved to boards.xml on exit).");
            String defaultBoardsFile;
            if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
                defaultBoardsFile = "boards.xml.default";
            } else {
                defaultBoardsFile = "boards.xml.default07";
            }
            boardIniFilename = settings.getValue("config.dir") + defaultBoardsFile;
        }
        
        boolean loadWasOk = xmlio.loadBoardTree( this, model, boardIniFilename );
        if( !loadWasOk ) {
            return loadWasOk;
        }

        // check if the board 'frost-announce' is contained in the list, add it if not found
        String expectedPubkey;
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
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
    public void save() throws StorageException
    {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = settings.getValue("config.dir") + "boards.xml";
        File check = new File( boardIniFilename );
        if( check.exists() )
        {
            // rename old file to .bak, overwrite older .bak
            String bakBoardIniFilename = settings.getValue("config.dir") + "boards.xml.bak";
            File bakFile = new File(bakBoardIniFilename);
            if( bakFile.exists() )
            {
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
     * @param parent
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
                        language.getString("You already have a board with name")
                            + " '"
                            + boardName
                            + "'!\n"
                            + language.getString("Please choose a new name"));
                } else {
                    Board newBoard = new Board(boardName, boardDescription);
                    model.addNodeToTree(newBoard);
                    // maybe this boardfolder already exists, scan for new messages
                    TOF.getInstance().initialSearchNewMessages(newBoard);
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
                    language.getString("You already have a board with name")
                        + " '"
                        + bname
                        + "'!\n"
                        + language.getString("Do you really want to overwrite it?")
                        + ""
                        + "\n("
                        + language.getString("This will not delete messages")
                        + ")",
                        language.getString("Warning"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.NO_OPTION) {
                return; // do not add
            }
        }
        Board newBoard = new Board(bname, bpubkey, bprivkey, description);
        model.addNodeToTree(newBoard);
        // maybe this boardfolder already exists, scan for new messages
        TOF.getInstance().initialSearchNewMessages(newBoard);
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
                    language.getString("Please enter a name for the new folder") + ":",
                    language.getString("New Folder Name"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    language.getString("newfolder"));

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
        String txt;
        if (node.isFolder()) {
            txt =
                "Do you really want to delete folder '"
                    + node.getName()
                    + "' ???"
                    + "\nNOTE: Removing it will also remove all boards/folders inside this folder!!!";
        } else {
            txt = "Do you really want to delete board '" + node.getName() + "' ???";
        }

        int answer =
            JOptionPane.showConfirmDialog(
                this,
                txt,
                "Delete '" + node.getName() + "'?",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.NO_OPTION) {
            return;
        }

        // ask user if to delete board directory also
        boolean deleteDirectory = false;
        String boardRelDir = settings.getValue("keypool.dir") + node.getBoardFilename();
        if (node.isFolder() == false) {
            txt =
                "Do you want to delete also the board directory '"
                    + boardRelDir
                    + "' ?\n"
                    + "This directory contains all received messages and file lists for this board.\n"
                    + "(NOTE: The board MUST not updating to delete it!\n"
                    + "Currently there is no way to stop the updating of a board,\n"
                    + "so please ensure this board is'nt updating right now,\n"
                    + "or you have to live with the consequences ;) )\n\n"
                    + "You can also delete the directory by yourself after shutdown of Frost.";
            answer =
                JOptionPane.showConfirmDialog(
                    this,
                    txt,
                    "Delete directory of '" + node.getName() + "'?",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                deleteDirectory = true;
            } else if (answer == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        // delete node from tree
        model.removeNode(node);

        // maybe delete board dir (in a thread, do not block gui)
        if (deleteDirectory) {
            if (node.isUpdating() == false) {
                core.deleteDir(boardRelDir);
            } else {
                logger.warning(
                    "WARNING: Although being warned, you tried to delete a board with is updating! Skipped ...");
            }
        }
    }

    /**
     * @param settings
     */
    public void setSettings(SettingsClass settings) {
        this.settings = settings;
    }

    /**
     * @param core
     */
    public void setCore(Core core) {
        this.core = core;
    }

    /**
     * @param parentFrame
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * @param e
     */
    private void showTofTreePopupMenu(MouseEvent e) {
        getPopupMenuTofTree().show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * starts update for the selected board, or for all childs (and their childs) of a folder
     * @param node
     */
    private void refreshNode(Board node) {
        if (node == null)
            return;

        if (node.isFolder() == false) {
            if (isUpdateAllowed(node)) {
                updateBoard(node);
            }
        } else {
            // update all childs recursiv
            Enumeration leafs = node.children();
            while (leafs.hasMoreElements()) {
                refreshNode((Board) leafs.nextElement());
            }
        }
    }

    private void markAllRead(Board node) {
        if (node == null) {
            return;
        }
        if (node.isFolder() == false) {
            TOF.getInstance().setAllMessagesRead(node);
        } else {
            // process all childs recursiv
            Enumeration leafs = node.children();
            while (leafs.hasMoreElements()) {
                markAllRead((Board)leafs.nextElement());
            }
        }
    }

    /**
     * Returns true if board is allowed to be updated.
     * Does NOT check if board update is already running.
     * @param board
     * @return
     */
    public boolean isUpdateAllowed(Board board) {
        if (board == null)
            return false;
        // Do not allow folders to update
        if (board.isFolder())
            return false;

        if (board.isSpammed())
            return false;

        return true;
    }

    /**
     * @return
     */
    public RunningBoardUpdateThreads getRunningBoardUpdateThreads() {
        return runningBoardUpdateThreads;
    }

    /**
     *
     */
    private void selectionChanged() {
        Board node = (Board) getLastSelectedPathComponent();
        if (node != null) {
            if (node.isFolder() == false) {
                // Node is a board
            } else {
                // Node is a folder
                if (node.isRoot()) {
                    cutBoardButton.setEnabled(false);
                } else {
                    cutBoardButton.setEnabled(true);
                }
            }
        }
    }

    /**
     * News | Configure Board action performed
     * @param board
     */
    private void configureBoard(Board board) {
        if (board == null ) {
            return;
        }

        BoardSettingsFrame newFrame = new BoardSettingsFrame(mainFrame, board);
        newFrame.runDialog();

        // all needed updates of boards are done by the dialog before it closes

//      if (newFrame.runDialog() == true) { // OK pressed?
//
//          mainFrame.updateTofTree(board);
//          // update the new msg. count for board
//          TOF.getInstance().initialSearchNewMessages(board);
//
//          if (board == model.getSelectedNode()) {
//              // reload all messages if board is shown
//              mainFrame.tofTree_actionPerformed(null);
//          }
//      }
    }

    /**
     * Starts the board update threads, getRequest thread and update id thread.
     * Checks for each type of thread if its already running, and starts allowed
     * not-running threads for this board.
     * @param board
     */
    public void updateBoard(Board board) {
        if (board == null || board.isFolder()) {
            return;
        }

        boolean threadStarted = false;

        // first download the messages of today
        if (getRunningBoardUpdateThreads()
            .isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_TODAY)
            == false) {
            getRunningBoardUpdateThreads().startMessageDownloadToday(
                board,
                settings,
                listener);
            logger.info("Starting update (MSG_TODAY) of " + board.getName());
            threadStarted = true;
        }

        // maybe get the files list
        if (!settings.getBoolValue(SettingsClass.DISABLE_REQUESTS)
            && !getRunningBoardUpdateThreads().isThreadOfTypeRunning(
                board,
                BoardUpdateThread.BOARD_FILE_UPLOAD)) {
            getRunningBoardUpdateThreads().startBoardFilesUpload(board, settings, listener);
            logger.info("Starting update (BOARD_UPLOAD) of " + board.getName());
            threadStarted = true;
        }

        if (!settings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS)
            && !getRunningBoardUpdateThreads().isThreadOfTypeRunning(
                board,
                BoardUpdateThread.BOARD_FILE_DNLOAD)) {
            getRunningBoardUpdateThreads().startBoardFilesDownload(board, settings, listener);
            logger.info("Starting update (BOARD_DOWNLOAD) of " + board.getName());
            threadStarted = true;
        }

        // finally get the older messages
        if (getRunningBoardUpdateThreads()
            .isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_BACK)
            == false) {
            getRunningBoardUpdateThreads().startMessageDownloadBack(board, settings, listener);
            logger.info("Starting update (MSG_BACKLOAD) of " + board.getName());
            threadStarted = true;
        }

        // if there was a new thread started, update the lastUpdateStartTimeMillis
        if (threadStarted == true) {
            board.setLastUpdateStartMillis(System.currentTimeMillis());
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
    protected JButton getConfigBoardButton() {
        return configBoardButton;
    }
    protected JMenuItem getConfigBoardMenuItem() {
        return configBoardMenuItem;
    }
    protected JButton getCutBoardButton() {
        return cutBoardButton;
    }
    protected JButton getPasteBoardButton() {
        return pasteBoardButton;
    }
}