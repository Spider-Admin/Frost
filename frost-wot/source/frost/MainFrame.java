/*
  MainFrame.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>
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
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import frost.boards.*;
import frost.components.translate.*;
import frost.ext.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.UploadPanel;
import frost.gui.*;
import frost.gui.help.*;
import frost.gui.objects.*;
import frost.gui.preferences.*;
import frost.storage.*;
import frost.threads.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.gui.treetable.*;

 /**
  * TODO: - after removing a board, let current board selected (currently if you
  *          delete another than selected board the tofTree is updated)
  */
public class MainFrame extends JFrame implements ClipboardOwner, SettingsUpdater, LanguageListener {
    /**
     * This listener changes the 'updating' state of a board if a thread starts/finishes.
     * It also launches popup menus
     */
    private class Listener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            fileExitMenuItem_actionPerformed(null);
        }
    }

    private HelpBrowserFrame helpBrowser = null;
    private SearchMessagesDialog searchMessagesDialog = null;
    private MemoryMonitor memoryMonitor = null;

    /**
     * Search through .req files of this day in all boards and remove the
     * dummy .req files that are created by requestThread on key collosions.
     */
    private class RemoveDummyRequestFiles extends Thread {

        public void run() {
            Iterator i = tofTreeModel.getAllBoards().iterator();

            while (i.hasNext()) {
                Board board = (Board) i.next();

                String destination =
                    new StringBuffer()
                        .append(MainFrame.keypool)
                        .append(board.getBoardFilename())
                        .append(System.getProperty("file.separator"))
                        .append(DateFun.getDate())
                        .append(System.getProperty("file.separator"))
                        .toString();
                File boarddir = new File(destination);
                if (boarddir.isDirectory()) {
                    File[] entries = boarddir.listFiles();
                    for (int x = 0; x < entries.length; x++) {
                        File entry = entries[x];
                        if (entry.getName().endsWith(".req.sha") &&
                            FileAccess.readFile(entry).indexOf(DownloadThread.KEYCOLL_INDICATOR) > -1)
                        {
                            entry.delete();
                        }
                    }
                }
            }
        }
    }

    private static Core core;

    // saved to frost.ini
    private static SettingsClass frostSettings = null;

    private static MainFrame instance = null; // set in constructor
    // "keypool.dir" is the corresponding key in frostSettings, is set in defaults of SettingsClass.java
    // this is the new way to access this value :)
    public static String keypool = null;

    /**
     * Used to sort FrostBoardObjects by lastUpdateStartMillis ascending.
     */
    private static final Comparator lastUpdateStartMillisCmp = new Comparator() {
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            Board value1 = (Board) o1;
            Board value2 = (Board) o2;
            if (value1.getLastUpdateStartMillis() > value2.getLastUpdateStartMillis())
                return 1;
            else if (value1.getLastUpdateStartMillis() < value2.getLastUpdateStartMillis())
                return -1;
            else
                return 0;
        }
    };

    private static Logger logger = Logger.getLogger(MainFrame.class.getName());
    private static ImageIcon[] newMessage = new ImageIcon[2];

    /**
     * Selects message icon in lower right corner
     * @param showNewMessageIcon
     */
    public static void displayNewMessageIcon(boolean showNewMessageIcon) {
        MainFrame mainFrame = MainFrame.getInstance();
        if (showNewMessageIcon) {
            ImageIcon frameIcon = new ImageIcon(MainFrame.class.getResource("/data/newmessage.gif"));
            mainFrame.setIconImage(frameIcon.getImage());
            mainFrame.statusMessageLabel.setIcon(newMessage[0]);
            // The title should never be changed on Windows systems (SystemTray.exe expects "Frost" as title)
            if( System.getProperty("os.name").startsWith("Windows") == false ) {
                String t = mainFrame.getTitle();
                // if not already done, append * on begin and end of title string
                if( !t.equals("*") && !(t.startsWith("*") && t.endsWith("*")) ) {
                    t = "*" + t + "*";
                }
                mainFrame.setTitle(t);
            }
        } else {
            ImageIcon frameIcon = new ImageIcon(MainFrame.class.getResource("/data/jtc.jpg"));
            mainFrame.setIconImage(frameIcon.getImage());
            mainFrame.statusMessageLabel.setIcon(newMessage[1]);
            // The title should never be changed on Windows systems (SystemTray.exe expects "Frost" as title)
            if( System.getProperty("os.name").startsWith("Windows") == false ) {
                String t = mainFrame.getTitle();
                // if not already done, append * on begin and end of title string
                if( !t.equals("*") && t.startsWith("*") && t.endsWith("*") ) {
                    // remove * on begin and end
                    t = t.substring(1, t.length()-1);
                }
                mainFrame.setTitle(t);
            }
        }
    }

    public static MainFrame getInstance() {
        return instance;
    }

    private JButton boardInfoButton = null;
    private long counter = 55;

    //Panels
    private JMenuItem fileExitMenuItem = new JMenuItem();

    private JButton knownBoardsButton = null;
    private JButton searchMessagesButton = null;

    //File Menu
    private JMenu fileMenu = new JMenu();

    private JMenuItem helpAboutMenuItem = new JMenuItem();
    private JMenuItem helpHelpMenuItem = new JMenuItem();
    private JMenuItem helpMemMonMenuItem = new JMenuItem();

    //Help Menu
    private JMenu helpMenu = new JMenu();

    //Language Menu
    private JMenu languageMenu = new JMenu();

    private Language language = null;

    private Listener listener = new Listener();

    // The main menu
    private JMenuBar menuBar;
    private MessagePanel messagePanel = null;

    // buttons that are enabled/disabled later
    private JButton newBoardButton = null;
    private JButton newFolderButton = null;

    private JToolBar buttonToolBar;

    private JPanel extendableStatusPanel;

    //Options Menu
    private JMenu optionsMenu = new JMenu();
    private JMenuItem optionsPreferencesMenuItem = new JMenuItem();
    private JMenuItem optionsManageLocalIdentitiesMenuItem = new JMenuItem();
    private JMenuItem optionsManageIdentitiesMenuItem = new JMenuItem();
//    private JMenuItem pluginBrowserMenuItem = new JMenuItem();

    //Plugin Menu
    private JMenu pluginMenu = new JMenu();
    private JMenuItem pluginTranslateMenuItem = new JMenuItem();

    //Popups
    private JButton removeBoardButton = null;
    private JButton renameFolderButton = null;

    // labels that are updated later
    private JLabel statusLabel = null;
    private JLabel statusMessageLabel = null;
    private JButton systemTrayButton = null;

    private JTranslatableTabbedPane tabbedPane;
    private JLabel timeLabel = null;

    private JCheckBoxMenuItem tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem();

    private JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem();
    private JMenuItem tofDisplayKnownBoards = new JMenuItem();
    private JMenuItem tofSearchMessages = new JMenuItem();

    //Messages (tof) Menu
    private JMenu tofMenu = new JMenu();

    private TofTree tofTree = null;
    private TofTreeModel tofTreeModel = null;
    private UploadPanel uploadPanel = null;
    
    private JSplitPane treeAndTabbedPaneSplitpane = null;

    public TofTree getTofTree() {
        return tofTree;
    }

    public TofTreeModel getTofTreeModel() {
        return tofTreeModel;
    }

    /**
     * Construct the frame
     * @param frostSettings
     */
    public MainFrame(SettingsClass settings, String title) {

        instance = this;
        core = Core.getInstance();
        frostSettings = settings;
        language = Language.getInstance();

        keypool = frostSettings.getValue("keypool.dir");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        frostSettings.addUpdater(this);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        ImageIcon frameIcon = new ImageIcon(getClass().getResource("/data/jtc.jpg"));
        setIconImage(frameIcon.getImage());
        setResizable(true);

        setTitle(title);

        addWindowListener(listener);
    }

    public void addPanel(String title, JPanel panel) {
        getTabbedPane().add(title, panel);
    }

    /**
     * This method inserts a panel into the extendable part of the status bar
     * at the given position
     * @param panel panel to add to the status bar
     * @param position position to insert the panel at
     */
    public void addStatusPanel(JPanel panel, int position) {
        getExtendableStatusPanel().add(panel, position);
    }

    /**
     * This method adds a button to the button toolbar of the frame. It will insert it
     * into an existing block or into a new one (where a block is a group of buttons
     * delimited by separators) at the given position.
     * If the position number exceeds the number of buttons in that block, the button is
     * added at the end of that block.
     * @param button the button to add
     * @param block the number of the block to insert the button into. If newBlock is true
     *          we will create a new block at that position. If it is false, we will use
     *          the existing one. If the block number exceeds the number of blocks in the
     *          toolbar, a new block is created at the end of the toolbar and the button is
     *          inserted there, no matter what the value of the newBlock parameter is.
     * @param position the position inside the block to insert the button at. If the position
     *          number exceeds the number of buttons in the block, the button is added at the
     *          end of the block.
     * @param newBlock true to insert the button in a new block. False to use an existing one.
     */
    public void addButton(JButton button, int block, int position, boolean newBlock) {
        int index = 0;
        int blockCount = 0;
        while ((index < getButtonToolBar().getComponentCount()) &&
               (blockCount < block)) {
            Component component = getButtonToolBar().getComponentAtIndex(index);
            if (component instanceof JToolBar.Separator) {
                blockCount++;
            }
            index++;
        }
        if (blockCount < block) {
            // Block number exceeds the number of blocks in the toolbar or newBlock is true.
            getButtonToolBar().addSeparator();
            getButtonToolBar().add(button);
            return;
        }
        if (newBlock) {
            // New block created and button put in there.
            getButtonToolBar().add(new JToolBar.Separator(), index);
            getButtonToolBar().add(button, index);
            return;
        }
        int posCount = 0;
        Component component = getButtonToolBar().getComponentAtIndex(index);
        while ((index < getButtonToolBar().getComponentCount()) &&
               !(component instanceof JToolBar.Separator) &&
               (posCount < position)) {
                index++;
                posCount++;
                component = getButtonToolBar().getComponentAtIndex(index);
        }
        getButtonToolBar().add(button, index);
    }

    /**
     * This method adds a menu item to one of the menus of the menu bar of the frame.
     * It will insert it into an existing menu or into a new one. It will insert it
     * into an existing block or into a new one (where a block is a group of menu items
     * delimited by separators) at the given position.
     * If the position number exceeds the number of items in that block, the item is
     * added at the end of that block.
     * @param item the menu item to add
     * @param menuNameKey the text (as a language key) of the menu to insert the item into.
     *          If there is no menu with that text, a new one will be created at the end
     *          of the menu bar and the item will be put inside.
     * @param block the number of the block to insert the item into. If newBlock is true
     *          we will create a new block at that position. If it is false, we will use
     *          the existing one. If the block number exceeds the number of blocks in the
     *          menu, a new block is created at the end of the menu and the item is
     *          inserted there, no matter what the value of the newBlock parameter is.
     * @param position the position inside the block to insert the item at. If the position
     *          number exceeds the number of items in the block, the item is added at the
     *          end of the block.
     * @param newBlock true to insert the item in a new block. False to use an existing one.
     */
    public void addMenuItem(JMenuItem item, String menuNameKey, int block, int position, boolean newBlock) {
        String menuName = language.getString(menuNameKey);
        int index = 0;
        JMenu menu = null;
        while ((index < getMainMenuBar().getMenuCount()) &&
                (menu == null)) {
            JMenu aMenu = getMainMenuBar().getMenu(index);
            if ((aMenu != null) &&
                (menuName.equals(aMenu.getText()))) {
                menu = aMenu;
            }
            index++;
        }
        if (menu == null) {
            //There isn't any menu with that name, so we create a new one.
            menu = new JMenu(menuName);
            getMainMenuBar().add(menu);
            menu.add(item);
            return;
        }
        index = 0;
        int blockCount = 0;
        while ((index < menu.getItemCount()) &&
               (blockCount < block)) {
            Component component = menu.getItem(index);
            if (component == null) {
                blockCount++;
            }
            index++;
        }
        if (blockCount < block) {
            // Block number exceeds the number of blocks in the menu or newBlock is true.
            menu.addSeparator();
            menu.add(item);
            return;
        }
        if (newBlock) {
            // New block created and item put in there.
            menu.insertSeparator(index);
            menu.insert(item, index);
            return;
        }
        int posCount = 0;
        Component component = menu.getItem(index);
        while ((index < menu.getComponentCount()) &&
               (component != null) &&
               (posCount < position)) {
                index++;
                posCount++;
                component = menu.getItem(index);
        }
        menu.add(item, index);
    }

    private JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTranslatableTabbedPane(language);
        }
        return tabbedPane;
    }

    private JToolBar getButtonToolBar() {
        if (buttonToolBar == null) {
            buttonToolBar = new JToolBar();

            timeLabel = new JLabel("");
            // configure buttons
            knownBoardsButton = new JButton(new ImageIcon(getClass().getResource("/data/knownboards.gif")));
            searchMessagesButton = new JButton(new ImageIcon(getClass().getResource("/data/search.gif")));
            newBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/newboard.gif")));
            newFolderButton = new JButton(new ImageIcon(getClass().getResource("/data/newfolder.gif")));
            removeBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/remove.gif")));
            renameFolderButton = new JButton(new ImageIcon(getClass().getResource("/data/rename.gif")));
            boardInfoButton = new JButton(new ImageIcon(getClass().getResource("/data/info.gif")));
            systemTrayButton = new JButton(new ImageIcon(getClass().getResource("/data/tray.gif")));

            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(newBoardButton, "MainFrame.toolbar.tooltip.newBoard", "/data/newboard_rollover.gif", language);
            toolkit.configureButton(newFolderButton, "MainFrame.toolbar.tooltip.newFolder", "/data/newfolder_rollover.gif", language);
            toolkit.configureButton(removeBoardButton, "MainFrame.toolbar.tooltip.removeBoard", "/data/remove_rollover.gif", language);
            toolkit.configureButton(renameFolderButton, "MainFrame.toolbar.tooltip.renameFolder", "/data/rename_rollover.gif", language);
            toolkit.configureButton(boardInfoButton, "MainFrame.toolbar.tooltip.boardInformationWindow", "/data/info_rollover.gif", language);
            toolkit.configureButton(systemTrayButton, "MainFrame.toolbar.tooltip.minimizeToSystemTray", "/data/tray_rollover.gif", language);
            toolkit.configureButton(knownBoardsButton, "MainFrame.toolbar.tooltip.displayListOfKnownBoards", "/data/knownboards_rollover.gif", language);
            toolkit.configureButton(searchMessagesButton, "MainFrame.toolbar.tooltip.searchMessages", "/data/search_rollover.gif", language);

            // add action listener
            knownBoardsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofDisplayKnownBoardsMenuItem_actionPerformed(e);
                }
            });
            searchMessagesButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startSearchMessagesDialog();
                }
            });
            newBoardButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofTree.createNewBoard(MainFrame.this);
                }
            });
            newFolderButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofTree.createNewFolder(MainFrame.this);
                }
            });
            renameFolderButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    renameFolder(tofTreeModel.getSelectedNode());
                }
            });
            removeBoardButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofTree.removeNode(tofTreeModel.getSelectedNode());
                }
            });
            systemTrayButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try { // Hide the Frost window
                        if (JSysTrayIcon.getInstance() != null) {
                            JSysTrayIcon.getInstance().showWindow(JSysTrayIcon.SHOW_CMD_HIDE);
                        }
                        //Process process = Runtime.getRuntime().exec("exec" +
                        // fileSeparator + "SystemTrayHide.exe");
                    } catch (IOException _IoExc) {
                    }
                }
            });
            boardInfoButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofDisplayBoardInfoMenuItem_actionPerformed(e);
                }
            });

            // build panel
            buttonToolBar.setRollover(true);
            buttonToolBar.setFloatable(false);
            Dimension blankSpace = new Dimension(3, 3);

            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.add(newBoardButton);
            buttonToolBar.add(newFolderButton);
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.addSeparator();
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.add(renameFolderButton);
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.addSeparator();
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.add(removeBoardButton);
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.addSeparator();
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.add(boardInfoButton);
            buttonToolBar.add(knownBoardsButton);
            buttonToolBar.add(searchMessagesButton);
            if (JSysTrayIcon.getInstance() != null) {
                buttonToolBar.add(Box.createRigidArea(blankSpace));
                buttonToolBar.addSeparator();
                buttonToolBar.add(Box.createRigidArea(blankSpace));

                buttonToolBar.add(systemTrayButton);
            }
            buttonToolBar.add(Box.createHorizontalGlue());
            buttonToolBar.add(timeLabel);
            buttonToolBar.add(Box.createRigidArea(blankSpace));
        }
        return buttonToolBar;
    }

    /**
     * Build the menu bar.
     */
    private JMenuBar getMainMenuBar() {
        if (menuBar == null) {
            menuBar = new JMenuBar();
            MiscToolkit miscToolkit = MiscToolkit.getInstance();
            tofDisplayBoardInfoMenuItem.setIcon(miscToolkit.getScaledImage("/data/info.gif", 16, 16));
            tofAutomaticUpdateMenuItem.setSelected(true);
            tofDisplayKnownBoards.setIcon(miscToolkit.getScaledImage("/data/knownboards.gif", 16, 16));
            tofSearchMessages.setIcon(miscToolkit.getScaledImage("/data/search.gif", 16, 16));

            // add action listener
            fileExitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileExitMenuItem_actionPerformed(e);
                }
            });
            optionsPreferencesMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    optionsPreferencesMenuItem_actionPerformed(e);
                }
            });
            optionsManageLocalIdentitiesMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    optionsManageLocalIdentitiesMenuItem_actionPerformed(e);
                }
            });
            optionsManageIdentitiesMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    optionsManageIdentitiesMenuItem_actionPerformed(e);
                }
            });
            tofDisplayBoardInfoMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofDisplayBoardInfoMenuItem_actionPerformed(e);
                }
            });
            tofDisplayKnownBoards.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofDisplayKnownBoardsMenuItem_actionPerformed(e);
                }
            });
            tofSearchMessages.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startSearchMessagesDialog();
                }
            });
//            pluginBrowserMenuItem.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    BrowserFrame browser = new BrowserFrame(true);
//                    browser.setVisible(true);
//                }
//            });
            pluginTranslateMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	getTranslationDialog().setVisible(true);
                }
            });
            helpHelpMenuItem.setIcon(miscToolkit.getScaledImage("/data/help.png", 16, 16));
            helpHelpMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showHtmlHelp("index.html");
                    //HelpFrame dlg = new HelpFrame(MainFrame.this);
                    //dlg.setVisible(true);
                }
            });
            
            if( Core.isHelpHtmlSecure() == false ) {
                helpHelpMenuItem.setEnabled(false);
            }
            
            helpAboutMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    helpAboutMenuItem_actionPerformed(e);
                }
            });
            
            helpMemMonMenuItem.setIcon(miscToolkit.getScaledImage("/data/memmon.png", 16, 16));
            helpMemMonMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getMemoryMonitor().showDialog();
                }
            });

            // construct menu
            // File Menu
            fileMenu.add(fileExitMenuItem);
            // News Menu
            tofMenu.add(tofAutomaticUpdateMenuItem);
            tofMenu.addSeparator();
            tofMenu.add(tofDisplayBoardInfoMenuItem);
            tofMenu.add(tofDisplayKnownBoards);
            tofMenu.add(tofSearchMessages);
            // Options Menu
            optionsMenu.add(optionsPreferencesMenuItem);
            optionsMenu.add(optionsManageLocalIdentitiesMenuItem);
            optionsMenu.add(optionsManageIdentitiesMenuItem);
            // Plugin Menu
//            pluginMenu.add(pluginBrowserMenuItem);
            pluginMenu.add(pluginTranslateMenuItem);
            
            // Language Menu
            LanguageGuiSupport.getInstance().buildInitialLanguageMenu(languageMenu);
            
            // Help Menu
            helpMenu.add(helpMemMonMenuItem);
            helpMenu.add(helpHelpMenuItem);
            helpMenu.add(helpAboutMenuItem);
            // add all to bar
            menuBar.add(fileMenu);
            menuBar.add(tofMenu);
            menuBar.add(optionsMenu);
            menuBar.add(pluginMenu);
            menuBar.add(languageMenu);
            menuBar.add(helpMenu);

            translateMainMenu();

            language.addLanguageListener(this);
        }
        return menuBar;
    }

    /**
     * This method builds the whole of the status bar (both the extendable and the
     * static parts)
     * @return
     */
    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());

        statusLabel = new JLabel();
        statusMessageLabel = new JLabel();

        newMessage[0] = new ImageIcon(MainFrame.class.getResource("/data/messagebright.gif"));
        newMessage[1] = new ImageIcon(MainFrame.class.getResource("/data/messagedark.gif"));
        statusMessageLabel.setIcon(newMessage[1]);

        panel.add(getExtendableStatusPanel(), BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.CENTER); // Statusbar
        panel.add(statusMessageLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * This method returns the extendable part of the status bar.
     * @return
     */
    private JPanel getExtendableStatusPanel() {
        if (extendableStatusPanel == null) {
            extendableStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        }
        return extendableStatusPanel;
    }

    private JPanel buildTofMainPanel() {
        // add a tab for buddies perhaps?
        getTabbedPane().insertTab("MainFrame.tabbedPane.news", null, getMessagePanel(), null, 0);
        getTabbedPane().setSelectedIndex(0);

        JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
        tofTreeScrollPane.setWheelScrollingEnabled(true);
        // tofTree selection listener
        tofTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                tofTree_actionPerformed(e);
            }
        });

        // Vertical Board Tree / MessagePane Divider
        treeAndTabbedPaneSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, getTabbedPane());
        
        int dividerLoc = frostSettings.getIntValue("MainFrame.treeAndTabbedPaneSplitpaneDividerLocation");
        if( dividerLoc < 10 ) {
            dividerLoc = 160;
        }
        treeAndTabbedPaneSplitpane.setDividerLocation(dividerLoc);

        JPanel tofMainPanel = new JPanel(new BorderLayout());
        tofMainPanel.add(treeAndTabbedPaneSplitpane, BorderLayout.CENTER); // TOF/Text
        return tofMainPanel;
    }

    /**
     * Returns true if board is allowed to be updated.
     * Also checks if board update is already running.
     * @param board
     * @return
     */
    public boolean doUpdate(Board board) {
        // TODO: hook in to stop running updates (and prevent new starts) if board is currently deleted
        if (tofTree.isUpdateAllowed(board) == false) {
            return false;
        }

        if (board.isUpdating()) {
            return false;
        }

        return true;
    }

    /**
     * save size,location and state of window
     * let save message panel layouts
     */
    public void saveLayout() {
        Rectangle bounds = getBounds();
        boolean isMaximized = ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);

        frostSettings.setValue("lastFrameMaximized", isMaximized);

        if (!isMaximized) { // Only save the current dimension if frame is not maximized
            frostSettings.setValue("lastFrameHeight", bounds.height);
            frostSettings.setValue("lastFrameWidth", bounds.width);
            frostSettings.setValue("lastFramePosX", bounds.x);
            frostSettings.setValue("lastFramePosY", bounds.y);
        }
        
        frostSettings.setValue("MainFrame.treeAndTabbedPaneSplitpaneDividerLocation", 
                treeAndTabbedPaneSplitpane.getDividerLocation());

        // let save component layouts
        getMessagePanel().getMessageTable().saveLayout(frostSettings);
        getMessagePanel().saveLayout(frostSettings);
    }

    /**
     * File | Exit action performed
     * @param e
     */
    private void fileExitMenuItem_actionPerformed(ActionEvent e) {

        if (tofTree.getRunningBoardUpdateThreads().getRunningUploadThreadCount() > 0) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    language.getString("MainFrame.runningUploadsWarning.body"),
                    language.getString("MainFrame.runningUploadsWarning.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.NO_OPTION) {
                return;
            }
        }
        
        saveLayout();
        
        System.exit(0);
    }

    public MessagePanel getMessagePanel() {
        if (messagePanel == null) {
            messagePanel = new MessagePanel(frostSettings, this);
            messagePanel.setParentFrame(this);
            messagePanel.setIdentities(Core.getIdentities());
//            messagePanel.addKeyListener(messagePanel.listener);
            messagePanel.initialize();
        }
        return messagePanel;
    }

    /**
     * Help | About action performed
     */
    private void helpAboutMenuItem_actionPerformed(ActionEvent e) {
        AboutBox dlg = new AboutBox(this);
        dlg.setVisible(true);
    }

    public void initialize() {

        // Add components
        JPanel contentPanel = (JPanel) getContentPane();
        contentPanel.setLayout(new BorderLayout());

        contentPanel.add(getButtonToolBar(), BorderLayout.NORTH);
        contentPanel.add(buildTofMainPanel(), BorderLayout.CENTER);
        contentPanel.add(buildStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(getMainMenuBar());

        // step through all messages on disk up to maxMessageDisplay and check
        // if there are new messages
        // if a new message is in a folder, this folder is show yellow in tree
        TOF.getInstance().initialSearchAllNewMessages();

        if (core.isFreenetOnline()) {
            tofAutomaticUpdateMenuItem.setSelected(frostSettings.getBoolValue("automaticUpdate"));
        } else {
            tofAutomaticUpdateMenuItem.setSelected(false);
        }
        //      uploadActivateCheckBox.setSelected(frostSettings.getBoolValue("uploadingActivated"));
        //      reducedBlockCheckCheckBox.setSelected(frostSettings.getBoolValue("reducedBlockCheck"));

        if (tofTree.getRowCount() > frostSettings.getIntValue("tofTreeSelectedRow"))
            tofTree.setSelectionRow(frostSettings.getIntValue("tofTreeSelectedRow"));

        // make sure the font size isn't too small to see
        if (frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE) < 6)
            frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, 6);

        // load size, location and state of window
        int lastHeight = frostSettings.getIntValue("lastFrameHeight");
        int lastWidth = frostSettings.getIntValue("lastFrameWidth");
        int lastPosX = frostSettings.getIntValue("lastFramePosX");
        int lastPosY = frostSettings.getIntValue("lastFramePosY");
        boolean lastMaximized = frostSettings.getBoolValue("lastFrameMaximized");
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (lastWidth < 100) {
            lastWidth = 700;
        }
        if (lastWidth > scrSize.width) {
            lastWidth = scrSize.width;
        }

        if (lastHeight < 100) {
            lastHeight = 500;
        }
        if (lastHeight > scrSize.height) {
            lastWidth = scrSize.height;
        }

        if (lastPosX < 0) {
            lastPosX = 0;
        }
        if (lastPosY < 0) {
            lastPosY = 0;
        }

        if ((lastPosX + lastWidth) > scrSize.width) {
            lastPosX = scrSize.width / 10;
            lastWidth = (int) ((scrSize.getWidth() / 10.0) * 8.0);
        }

        if ((lastPosY + lastHeight) > scrSize.height) {
            lastPosY = scrSize.height / 10;
            lastHeight = (int) ((scrSize.getHeight() / 10.0) * 8.0);
        }

        setBounds(lastPosX, lastPosY, lastWidth, lastHeight);

        if (lastMaximized) {
            setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
        }

        //note: changed this from timertask so that I can give it a name --zab
        Thread tickerThread = new Thread("tick tack") {
            public void run() {
                while (true) {
                    Mixed.wait(1000);
                    //TODO: refactor this method in Core. lots of work :)
                    timer_actionPerformed();
                }
            }
        };
        tickerThread.start();

        validate();
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //Core.getOut().println("Clipboard contents replaced");
    }

    /**
     * Options | Preferences action performed
     * @param e
     */
    private void optionsPreferencesMenuItem_actionPerformed(ActionEvent e) {
        try {
            frostSettings.save();
        } catch (StorageException se) {
            logger.log(Level.SEVERE, "Error while saving the settings.", se);
        }

        OptionsFrame optionsDlg = new OptionsFrame(this, frostSettings);
        boolean okPressed = optionsDlg.runDialog();
        if (okPressed) {
            // check if signed only+hideCheck+hideBad or blocking words settings changed
            if (optionsDlg.shouldReloadMessages()) {
                // update the new msg. count for all boards
                TOF.getInstance().initialSearchAllNewMessages();
                // reload all messages
                tofTree_actionPerformed(null);
            }

            tofTree.updateTree();
            // redraw whole tree, in case the update visualization was enabled or disabled (or others)

            // check if we switched from disableRequests=true to =false (requests now enabled)
            if (optionsDlg.shouldRemoveDummyReqFiles()) {
                new RemoveDummyRequestFiles().start();
            }
        }
    }
    
    private void optionsManageLocalIdentitiesMenuItem_actionPerformed(ActionEvent e) {
        new ManageLocalIdentitiesDialog().setVisible(true);
    }

    private void optionsManageIdentitiesMenuItem_actionPerformed(ActionEvent e) {
        new IdentitiesBrowser(this).startDialog();
    }

    /**
     * Opens dialog to rename a folder.
     */
    public void renameFolder(Board selected) {
        if (selected == null) {
            return;
        }
        if(selected.isFolder() == false) {
            return; // must never be called for boards
        }
        String newname = null;
        do {
            newname = JOptionPane.showInputDialog(
                    this,
                    "Please enter the new name:\n",  // TODO: translate
                    selected.getName());
            if (newname == null) {
                return; // cancel
            }
        } while (newname.length() == 0);

        selected.setName(newname);
        updateTofTree(selected);
    }

    /**
     * Chooses the next FrostBoard to update (automatic update).
     * First sorts by lastUpdateStarted time, then chooses first board
     * that is allowed to update.
     * Used only for automatic updating.
     * Returns NULL if no board to update is found.
     * @param boards
     * @return
     */
    public Board selectNextBoard(List boards) {
        Collections.sort(boards, lastUpdateStartMillisCmp);
        // now first board in list should be the one with latest update of all
        Board board;
        Board nextBoard = null;

        long curTime = System.currentTimeMillis();
        // get in minutes
        int minUpdateInterval =
            frostSettings.getIntValue("automaticUpdate.boardsMinimumUpdateInterval");
        // min -> ms
        long minUpdateIntervalMillis = minUpdateInterval * 60 * 1000;

        for (Iterator i=boards.iterator(); i.hasNext(); ) {
            board = (Board)i.next();
            if (nextBoard == null
                && doUpdate(board)
                && (curTime - minUpdateIntervalMillis) > board.getLastUpdateStartMillis()
                && // minInterval
             (
                    (board.isConfigured() && board.getAutoUpdateEnabled())
                        || !board.isConfigured())) {
                nextBoard = board;
                break;
            }
        }
        if (nextBoard != null) {
            logger.info("*** Automatic board update started for: " + nextBoard.getName());
        } else {
            logger.info("*** Automatic board update - min update interval not reached.  waiting...");
        }
        return nextBoard;
    }

    /**
     * Refresh the texts in MainFrame with new language.
     */
    public void languageChanged(LanguageEvent e) {
        translateMainMenu();
        LanguageGuiSupport.getInstance().translateLanguageMenu();
        translateButtons();
    }

    public void setPanelEnabled(String title, boolean enabled) {
        int position = getTabbedPane().indexOfTab(title);
        if (position != -1) {
            getTabbedPane().setEnabledAt(position, enabled);
        }
    }

    public void setTofTree(TofTree tofTree) {
        this.tofTree = tofTree;
    }

    public void setTofTreeModel(TofTreeModel tofTreeModel) {
        this.tofTreeModel = tofTreeModel;
    }

    /**
     * timer Action Listener (automatic download), gui updates
     */
    public void timer_actionPerformed() {
        // this method is called by a timer each second, so this counter counts seconds
        counter++;

        RunningMessageThreadsInformation info = tofTree.getRunningBoardUpdateThreads().getRunningMessageThreadsInformation();

        //////////////////////////////////////////////////
        //   Automatic TOF update
        //////////////////////////////////////////////////
        if (counter % 15 == 0 && // check all 5 seconds if a board update could be started
           isAutomaticBoardUpdateEnabled() &&
           info.getDownloadingBoardCount() < frostSettings.getIntValue("automaticUpdate.concurrentBoardUpdates"))
        {
            List boards = tofTreeModel.getAllBoards();
            if (boards.size() > 0) {
                Board actualBoard = selectNextBoard(boards);
                if (actualBoard != null) {
                    tofTree.updateBoard(actualBoard);
                }
            }
        }

        //////////////////////////////////////////////////
        //   Display time in button bar
        //////////////////////////////////////////////////
        timeLabel.setText(
            new StringBuffer()
                .append(DateFun.getVisibleExtendedDate())
                .append(" - ")
                .append(DateFun.getFullExtendedTime())
                .append(" GMT")
                .toString());

        /////////////////////////////////////////////////
        //   Update status bar
        /////////////////////////////////////////////////
        String newText =
            new StringBuffer()
                .append("   ").append(language.getString("MainFrame.statusBar.TOFUP")).append(": ")
                .append(info.getUploadingBoardCount())
                .append("B / ")
                .append(info.getRunningUploadThreadCount())
                .append("T / ")
                .append(info.getAttachmentsToUploadRemainingCount())
                .append("A   ")
                .append(language.getString("MainFrame.statusBar.TOFDO")).append(": ")
                .append(info.getDownloadingBoardCount())
                .append("B / ")
                .append(info.getRunningDownloadThreadCount())
                .append("T   ")
                .append(language.getString("MainFrame.statusBar.selectedBoard")).append(": ")
                .append(tofTreeModel.getSelectedNode().getName())
                .toString();
        statusLabel.setText(newText);
    }

    private void tofDisplayBoardInfoMenuItem_actionPerformed(ActionEvent e) {
        if (BoardInfoFrame.isDialogShowing() == false) {
            BoardInfoFrame boardInfo = new BoardInfoFrame(this, tofTree);
            boardInfo.startDialog();
        }
    }

    private void tofDisplayKnownBoardsMenuItem_actionPerformed(ActionEvent e) {
        KnownBoardsFrame knownBoards = new KnownBoardsFrame(this, tofTree);
        knownBoards.startDialog();
    }

    /** TOF Board selected
     * Core.getOut()
     * if e == NULL, the method is called by truster or by the reloader after options were changed
     * in this cases we usually should left select the actual message (if one) while reloading the table
     * @param e
     */
    public void tofTree_actionPerformed(TreeSelectionEvent e) {
        int i[] = tofTree.getSelectionRows();
        if (i != null && i.length > 0) {
            frostSettings.setValue("tofTreeSelectedRow", i[0]);
        }

        Board node = (Board) tofTree.getLastSelectedPathComponent();

        if (node != null) {
            if (node.isFolder() == false) {
                // node is a board
                removeBoardButton.setEnabled(true);

                updateButtons(node);

                logger.info("Board " + node.getName() + " blocked count: " + node.getBlockedCount());

                uploadPanel.setAddFilesButtonEnabled(true);
                renameFolderButton.setEnabled(false);

                // remove previous msgs
                getMessagePanel().getMessageTable().setNewRootNode(new FrostMessageObject(true));
                getMessagePanel().updateMessageCountLabels(node);

                // read all messages for this board into message table
                TOF.getInstance().updateTofTable(node);
                
                getMessagePanel().getMessageTable().clearSelection();
            } else {
                // node is a folder
                getMessagePanel().getMessageTable().setNewRootNode(new FrostMessageObject(true));
                getMessagePanel().updateMessageCountLabels(node);

                uploadPanel.setAddFilesButtonEnabled(false);
                renameFolderButton.setEnabled(true);
                if (node.isRoot()) {
                    removeBoardButton.setEnabled(false);
                } else {
                    removeBoardButton.setEnabled(true);
                }
            }
        }
    }

    private void translateButtons() {
        newBoardButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.newBoard"));
        newFolderButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.newFolder"));
        systemTrayButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.minimizeToSystemTray"));
        knownBoardsButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.displayListOfKnownBoards"));
        searchMessagesButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.searchMessages"));
        boardInfoButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.boardInformationWindow"));
        removeBoardButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.removeBoard"));
        renameFolderButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.renameFolder"));
    }

    private void translateMainMenu() {
        fileMenu.setText(language.getString("MainFrame.menu.file"));
        fileExitMenuItem.setText(language.getString("Common.exit"));
        tofMenu.setText(language.getString("MainFrame.menu.news"));
        tofDisplayBoardInfoMenuItem.setText(language.getString("MainFrame.menu.news.displayBoardInformationWindow"));
        tofAutomaticUpdateMenuItem.setText(language.getString("MainFrame.menu.news.automaticBoardUpdate"));
        tofDisplayKnownBoards.setText(language.getString("MainFrame.menu.news.displayKnownBoards"));
        tofSearchMessages.setText(language.getString("MainFrame.menu.news.searchMessages"));
        optionsMenu.setText(language.getString("MainFrame.menu.options"));
        optionsPreferencesMenuItem.setText(language.getString("MainFrame.menu.options.preferences"));
        optionsManageLocalIdentitiesMenuItem.setText(language.getString("MainFrame.menu.options.manageLocalIdentities"));
        optionsManageIdentitiesMenuItem.setText(language.getString("MainFrame.menu.options.manageIdentities"));
        pluginMenu.setText(language.getString("MainFrame.menu.plugins"));
//        pluginBrowserMenuItem.setText(language.getString("Experimental Freenet Browser"));
        pluginTranslateMenuItem.setText(language.getString("MainFrame.menu.plugins.translateFrost"));
        languageMenu.setText(language.getString("MainFrame.menu.language"));
        helpMenu.setText(language.getString("MainFrame.menu.help"));
        helpMemMonMenuItem.setText(language.getString("MainFrame.menu.help.showMemoryMonitor"));
        helpHelpMenuItem.setText(language.getString("MainFrame.menu.help.help"));
        helpAboutMenuItem.setText(language.getString("MainFrame.menu.help.aboutFrost"));
    }

    private void updateButtons(Board board) {
        if (board.isReadAccessBoard()) {
            uploadPanel.setAddFilesButtonEnabled(false);
        } else {
            uploadPanel.setAddFilesButtonEnabled(true);
        }
    }

    /* (non-Javadoc)
     * @see frost.SettingsUpdater#updateSettings()
     */
    public void updateSettings() {
        frostSettings.setValue("automaticUpdate", tofAutomaticUpdateMenuItem.isSelected());
    }

    /**
     * Fires a nodeChanged (redraw) for this board and updates buttons.
     */
    public void updateTofTree(Board board) {
        // fire update for node
        tofTreeModel.nodeChanged(board);
        // also update all parents
        TreeNode parentFolder = (Board) board.getParent();
        if (parentFolder != null) {
            tofTreeModel.nodeChanged(parentFolder);
            parentFolder = parentFolder.getParent();
        }

        if (board == tofTreeModel.getSelectedNode()) { // is the board currently shown?
            updateButtons(board);
        }
    }

    public void setUploadPanel(UploadPanel panel) {
        uploadPanel = panel;
    }

    public void setAutomaticBoardUpdateEnabled(boolean state) {
        tofAutomaticUpdateMenuItem.setSelected(state);
    }

    public boolean isAutomaticBoardUpdateEnabled() {
        return tofAutomaticUpdateMenuItem.isSelected();
    }

    private MemoryMonitor getMemoryMonitor() {
        if( memoryMonitor == null ) {
            memoryMonitor = new MemoryMonitor();
        }
        return memoryMonitor;
    }

    private TranslationStartDialog getTranslationDialog () {
        return new TranslationStartDialog(this);
//        if( translationDialog == null ) {
//        	translationDialog = new TranslationDialog(this);
//        }
//        return translationDialog; 
    }
    
    public void showHtmlHelp(String item) {
        if( Core.isHelpHtmlSecure() == false ) {
            return;
        }
        if( helpBrowser == null ) {
            helpBrowser = new HelpBrowserFrame(frostSettings.getValue("locale"), "help/help.zip");
        }
        // show first time or bring to front
        helpBrowser.setVisible(true);
        helpBrowser.showHelpPage(item);
    }

    public void startSearchMessagesDialog() {
        if( getSearchMessagesDialog() == null ) {
            setSearchMessagesDialog(new SearchMessagesDialog());
        }
        // show first time or bring to front
        getSearchMessagesDialog().setVisible(true);
    }

    public void setSearchMessagesDialog(SearchMessagesDialog d) {
        searchMessagesDialog = d;
    }
    public SearchMessagesDialog getSearchMessagesDialog() {
        return searchMessagesDialog;
    }

    public void updateMessageCountLabels(Board board) {
        // forward to MessagePanel
        getMessagePanel().updateMessageCountLabels(board);
    }

    public TreeTableModelAdapter getMessageTableModel() {
        // forward to MessagePanel
        return getMessagePanel().getMessageTableModel();
    }
    public DefaultTreeModel getMessageTreeModel() {
        // forward to MessagePanel
        return getMessagePanel().getMessageTreeModel();
    }
    public MessageTreeTable getMessageTreeTable() {
        // forward to MessagePanel
        return getMessagePanel().getMessageTable();
    }
}
