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
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.joda.time.*;

import frost.boards.*;
import frost.components.translate.*;
import frost.ext.*;
import frost.fileTransfer.*;
import frost.gui.*;
import frost.gui.help.*;
import frost.gui.messagetreetable.*;
import frost.gui.preferences.*;
import frost.gui.sentmessages.*;
import frost.gui.unsentmessages.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.threads.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class MainFrame extends JFrame implements SettingsUpdater, LanguageListener {
    
    private static final Logger logger = Logger.getLogger(MainFrame.class.getName());

    private final ImageIcon frameIconDefault = new ImageIcon(MainFrame.class.getResource("/data/jtc.jpg"));
    private final ImageIcon frameIconNewMessage = new ImageIcon(MainFrame.class.getResource("/data/newmessage.gif"));

    private HelpBrowserFrame helpBrowser = null;
    private SearchMessagesDialog searchMessagesDialog = null;
    private MemoryMonitor memoryMonitor = null;

    private MessagePanel messagePanel = null;
    
    private SentMessagesPanel sentMessagesPanel = null;
    private UnsentMessagesPanel unsentMessagesPanel = null;
    
    private ImageIcon progressIconRunning = null;
    private ImageIcon progressIconIdle = null;
    private JLabel progressIconLabel = null;
    
    private JLabel disconnectedLabel = null;

    private static SettingsClass frostSettings = null;

    private static MainFrame instance = null; // set in constructor
    
    private JButton boardInfoButton = null;
    private long counter = 55;
    
    private static java.util.List<StartupMessage> queuedStartupMessages = new LinkedList<StartupMessage>();

    //Panels

    private JButton knownBoardsButton = null;
    private JButton searchMessagesButton = null;

    //File Menu
    private JMenu fileMenu = new JMenu();
    private JMenuItem fileExitMenuItem = new JMenuItem();
    private JMenuItem fileStatisticsMenuItem = new JMenuItem();

    private JMenuItem helpAboutMenuItem = new JMenuItem();
    private JMenuItem helpHelpMenuItem = new JMenuItem();
    private JMenuItem helpMemMonMenuItem = new JMenuItem();

    //Help Menu
    private JMenu helpMenu = new JMenu();

    //Language Menu
    private JMenu languageMenu = new JMenu();

    private Language language = null;

    // The main menu
    private JMenuBar menuBar;

    // buttons that are enabled/disabled later
    private JButton newBoardButton = null;
    private JButton newFolderButton = null;

    private JToolBar buttonToolBar;
    
    private MainFrameStatusBar statusBar;

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
    private JButton configBoardButton = null;

    private JButton systemTrayButton = null;

    private JTranslatableTabbedPane tabbedPane;
    private JLabel timeLabel = new JLabel("");

    private JCheckBoxMenuItem tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem();

    private JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem();
    private JMenuItem tofDisplayKnownBoards = new JMenuItem();
    private JMenuItem tofSearchMessages = new JMenuItem();

    private JMenu tofMenu = new JMenu();

    private TofTree tofTree = null;
    private TofTreeModel tofTreeModel = null;
    
    private JSplitPane treeAndTabbedPaneSplitpane = null;
    
    private GlassPane glassPane = null;
    
    private List<JRadioButtonMenuItem> lookAndFeels = new ArrayList<JRadioButtonMenuItem>();
    
    public MainFrame(SettingsClass settings, String title) {

        instance = this;
        Core.getInstance();
        frostSettings = settings;
        language = Language.getInstance();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        frostSettings.addUpdater(this);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        setIconImage(frameIconDefault.getImage());
        setResizable(true);

        setTitle(title);
        
        // we don't want all of our tooltips to hide after 4 seconds, they should be shown forever
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        addWindowListener(new WindowClosingListener()); 
        addWindowStateListener(new WindowStateListener());
    }

    public static MainFrame getInstance() {
        return instance;
    }

    public TofTree getTofTree() {
        return tofTree;
    }

    public TofTreeModel getTofTreeModel() {
        return tofTreeModel;
    }

    public void addPanel(String title, JPanel panel) {
        getTabbedPane().add(title, panel);
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
    
    public void selectTabbedPaneTab(String title) {
        int position = getTabbedPane().indexOfTab(title);
        if (position != -1) {
            getTabbedPane().setSelectedIndex(position);
        }
    }

    private JToolBar getButtonToolBar() {
        if (buttonToolBar == null) {
            buttonToolBar = new JToolBar();
            
            // configure buttons
            knownBoardsButton = new JButton(new ImageIcon(getClass().getResource("/data/knownboards.gif")));
            searchMessagesButton = new JButton(new ImageIcon(getClass().getResource("/data/searchmessages.gif")));
            newBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/newboard.gif")));
            newFolderButton = new JButton(new ImageIcon(getClass().getResource("/data/newfolder.gif")));
            removeBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/remove.gif")));
            renameFolderButton = new JButton(new ImageIcon(getClass().getResource("/data/rename.gif")));
            configBoardButton = new JButton(new ImageIcon(getClass().getResource("/data/configure.gif")));
            
            boardInfoButton = new JButton(new ImageIcon(getClass().getResource("/data/info.gif")));
            systemTrayButton = new JButton(new ImageIcon(getClass().getResource("/data/tray.gif")));

            progressIconRunning = new ImageIcon(getClass().getResource("/data/progress_running.gif"));
            progressIconIdle = new ImageIcon(getClass().getResource("/data/progress_idle.gif"));
            progressIconLabel = new JLabel(progressIconIdle);
            disconnectedLabel = new JLabel("");

            MiscToolkit toolkit = MiscToolkit.getInstance();
            toolkit.configureButton(newBoardButton, "MainFrame.toolbar.tooltip.newBoard", "/data/newboard_rollover.gif", language);
            toolkit.configureButton(newFolderButton, "MainFrame.toolbar.tooltip.newFolder", "/data/newfolder_rollover.gif", language);
            toolkit.configureButton(removeBoardButton, "MainFrame.toolbar.tooltip.removeBoard", "/data/remove_rollover.gif", language);
            toolkit.configureButton(renameFolderButton, "MainFrame.toolbar.tooltip.renameFolder", "/data/rename_rollover.gif", language);
            toolkit.configureButton(boardInfoButton, "MainFrame.toolbar.tooltip.boardInformationWindow", "/data/info_rollover.gif", language);
            toolkit.configureButton(systemTrayButton, "MainFrame.toolbar.tooltip.minimizeToSystemTray", "/data/tray_rollover.gif", language);
            toolkit.configureButton(knownBoardsButton, "MainFrame.toolbar.tooltip.displayListOfKnownBoards", "/data/knownboards_rollover.gif", language);
            toolkit.configureButton(searchMessagesButton, "MainFrame.toolbar.tooltip.searchMessages", "/data/searchmessages_rollover.gif", language);
            
            toolkit.configureButton(configBoardButton, "MainFrame.toolbar.tooltip.configureBoard", "/data/configure_rollover.gif", language);

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
                    renameFolder((Folder)tofTreeModel.getSelectedNode());
                }
            });
            removeBoardButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofTree.removeNode(tofTreeModel.getSelectedNode());
                }
            });

            configBoardButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tofTree.configureBoard(tofTreeModel.getSelectedNode());
                }
            });

            systemTrayButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try { 
                        // Hide the Frost window
                        // JSysTray icon automatically detects if we were maximized or not
                        if (JSysTrayIcon.getInstance() != null) {
                            JSysTrayIcon.getInstance().showWindow(JSysTrayIcon.SHOW_CMD_HIDE);
                        }
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
            buttonToolBar.add(configBoardButton);
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
            buttonToolBar.add(disconnectedLabel);
            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.add(progressIconLabel);
            buttonToolBar.add(Box.createRigidArea(blankSpace));
        }
        return buttonToolBar;
    }
    
    public void showProgress() {
        progressIconLabel.setIcon(progressIconRunning);
    }
    public void hideProgress() {
        progressIconLabel.setIcon(progressIconIdle);
    }
    
    public void setDisconnected() {
        disconnectedLabel.setOpaque(true);
        disconnectedLabel.setBackground(Color.yellow);
        disconnectedLabel.setText(" DISCONNECTED ");
    }
    public void setConnected() {
        disconnectedLabel.setOpaque(false);
        disconnectedLabel.setBackground(progressIconLabel.getBackground());
        disconnectedLabel.setText("");
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
            tofSearchMessages.setIcon(miscToolkit.getScaledImage("/data/searchmessages.gif", 16, 16));

            // add action listener
            fileExitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileExitMenuItem_actionPerformed(e);
                }
            });
            fileStatisticsMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileStatisticsMenuItem_actionPerformed(e);
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
            fileMenu.add(fileStatisticsMenuItem);
            fileMenu.addSeparator();
            fileMenu.add(fileExitMenuItem);
            // News Menu
            tofMenu.add(tofAutomaticUpdateMenuItem);
            tofMenu.addSeparator();
            tofMenu.add(tofDisplayBoardInfoMenuItem);
            tofMenu.add(tofDisplayKnownBoards);
            tofMenu.add(tofSearchMessages);
            // Options Menu
            optionsMenu.add(optionsManageLocalIdentitiesMenuItem);
            optionsMenu.add(optionsManageIdentitiesMenuItem);
            optionsMenu.addSeparator();
            optionsMenu.add( getLookAndFeelMenu() );
            optionsMenu.addSeparator();
            optionsMenu.add(optionsPreferencesMenuItem);
            // Plugin Menu
//            pluginMenu.add(pluginBrowserMenuItem);
            pluginMenu.add(pluginTranslateMenuItem);
            // Language Menu
            LanguageGuiSupport.getInstance().buildInitialLanguageMenu(languageMenu);
            // Help Menu
            helpMenu.add(helpMemMonMenuItem);
            helpMenu.add(helpHelpMenuItem);
            helpMenu.addSeparator();
            helpMenu.add(helpAboutMenuItem);

            // add all to bar
            menuBar.add(fileMenu);
            menuBar.add(tofMenu);
            menuBar.add(optionsMenu);
            menuBar.add(pluginMenu);
            menuBar.add(languageMenu);
            menuBar.add(helpMenu);

            // add time label
            menuBar.add(Box.createHorizontalGlue());
            menuBar.add(timeLabel);
            menuBar.add(Box.createRigidArea(new Dimension(3,3)));
            
            translateMainMenu();

            language.addLanguageListener(this);
        }
        return menuBar;
    }

    private JMenu getLookAndFeelMenu() {
        // init look and feel menu
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        JMenu lfMenu = new JMenu("Look and feel");

        ButtonGroup group = new ButtonGroup();
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String lfName = e.getActionCommand();
                try {
                    UIManager.setLookAndFeel(lfName);
                    updateComponentTreesUI();
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception changing l&f", t);
                }
            }
        };
        
        for(int i=0;i<info.length;i++) {
            String lfClassName = info[i].getClassName();
            try {
                LookAndFeel laf = (LookAndFeel) Class.forName(lfClassName).newInstance();
                if (laf.isSupportedLookAndFeel()) {
                    JRadioButtonMenuItem rmItem = new JRadioButtonMenuItem(laf.getName()+"  ["+lfClassName+"]");
                    rmItem.setActionCommand(lfClassName);
                    rmItem.setSelected(UIManager.getLookAndFeel().getName().equals(laf.getName()));
                    group.add(rmItem);
                    rmItem.addActionListener(al);
                    lfMenu.add(rmItem);
                    lookAndFeels.add(rmItem);
                }
            }
            catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception adding l&f menu", t);
            }
        }
        return lfMenu;
    }
    
    private MainFrameStatusBar getStatusBar() {
        if( statusBar == null ) {
            statusBar = new MainFrameStatusBar();
        }
        return statusBar;
    }

    private JTabbedPane buildMainPanel() {

        JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
        tofTreeScrollPane.setWheelScrollingEnabled(true);

        tofTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                tofTree_actionPerformed(e);
            }
        });

        // Vertical Board Tree / MessagePane Divider
        JPanel p = new JPanel(new BorderLayout());
        treeAndTabbedPaneSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, p);

        p.add(getMessagePanel(), BorderLayout.CENTER);
        
        int dividerLoc = frostSettings.getIntValue("MainFrame.treeAndTabbedPaneSplitpaneDividerLocation");
        if( dividerLoc < 10 ) {
            dividerLoc = 160;
        }
        treeAndTabbedPaneSplitpane.setDividerLocation(dividerLoc);

        getTabbedPane().insertTab("MainFrame.tabbedPane.news", null, treeAndTabbedPaneSplitpane, null, 0);
        getTabbedPane().setSelectedIndex(0);

        return getTabbedPane();
    }
    
    public void setKeyActionForNewsTab(Action action, String actionName, KeyStroke keyStroke) {
        treeAndTabbedPaneSplitpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, actionName);
        treeAndTabbedPaneSplitpane.getActionMap().put(actionName, action);
    }
    
    public void showMessagePanelInSplitpane() {
        if( treeAndTabbedPaneSplitpane != null ) {
            final JPanel p = (JPanel)treeAndTabbedPaneSplitpane.getRightComponent();
            if( p.getComponent(0) == getMessagePanel() ) {
                return; // already shown
            }
            p.removeAll();
            p.add(getMessagePanel(), BorderLayout.CENTER);
            p.repaint();

            if( getSentMessagesPanel().isShown() ) {
                getSentMessagesPanel().cleanupAfterLeave();
            }
            if( getUnsentMessagesPanel().isShown() ) {
                getUnsentMessagesPanel().cleanupAfterLeave();
            }
        }
    }

    public void showSentMessagePanelInSplitpane() {
        if( treeAndTabbedPaneSplitpane != null ) {
            final JPanel p = (JPanel)treeAndTabbedPaneSplitpane.getRightComponent();
            if( p.getComponent(0) == getSentMessagesPanel() ) {
                return; // already shown
            }
            Thread t = new Thread() {
                public void run() {
                    try { setPriority(getPriority() - 1); } catch(Throwable lt) {}
                    getSentMessagesPanel().prepareForShow(); // load from db
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            p.removeAll();
                            p.add(getSentMessagesPanel(), BorderLayout.CENTER);
                            deactivateGlassPane(); // unblock gui
                            if( getUnsentMessagesPanel().isShown() ) {
                                getUnsentMessagesPanel().cleanupAfterLeave();
                            }
                        }
                    });
                }
            };
            activateGlassPane(); // block gui during load from database
            t.start();
        }
    }

    public void showUnsentMessagePanelInSplitpane() {
        if( treeAndTabbedPaneSplitpane != null ) {
            final JPanel p = (JPanel)treeAndTabbedPaneSplitpane.getRightComponent();
            if( p.getComponent(0) == getUnsentMessagesPanel() ) {
                return; // already shown
            }
            Thread t = new Thread() {
                public void run() {
                    try { setPriority(getPriority() - 1); } catch(Throwable lt) {}
                    getUnsentMessagesPanel().prepareForShow(); // load from db
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            p.removeAll();
                            p.add(getUnsentMessagesPanel(), BorderLayout.CENTER);
                            deactivateGlassPane(); // unblock gui
                            if( getSentMessagesPanel().isShown() ) {
                                getSentMessagesPanel().cleanupAfterLeave();
                            }
                        }
                    });
                }
            };
            activateGlassPane(); // block gui during load from database
            t.start();
        }
    }

    public SentMessagesPanel getSentMessagesPanel() {
        if( sentMessagesPanel == null ) {
            sentMessagesPanel = new SentMessagesPanel();
        }
        return sentMessagesPanel;
    }

    public UnsentMessagesPanel getUnsentMessagesPanel() {
        if( unsentMessagesPanel == null ) {
            unsentMessagesPanel = new UnsentMessagesPanel();
        }
        return unsentMessagesPanel;
    }

    /**
     * save size,location and state of window
     * let save message panel layouts
     */
    public void saveLayout() {
        Rectangle bounds = getBounds();
        boolean isMaximized = ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);

        frostSettings.setValue(SettingsClass.MAINFRAME_LAST_MAXIMIZED, isMaximized);

        if (!isMaximized) { // Only save the current dimension if frame is not maximized
            frostSettings.setValue(SettingsClass.MAINFRAME_LAST_HEIGHT, bounds.height);
            frostSettings.setValue(SettingsClass.MAINFRAME_LAST_WIDTH, bounds.width);
            frostSettings.setValue(SettingsClass.MAINFRAME_LAST_X, bounds.x);
            frostSettings.setValue(SettingsClass.MAINFRAME_LAST_Y, bounds.y);
        }
        
        frostSettings.setValue("MainFrame.treeAndTabbedPaneSplitpaneDividerLocation", 
                treeAndTabbedPaneSplitpane.getDividerLocation());
        
        for( JRadioButtonMenuItem rbmi : lookAndFeels ) {
            if( rbmi.isSelected() ) {
                frostSettings.setValue(SettingsClass.LOOK_AND_FEEL, rbmi.getActionCommand());
            }
        }

        // let save component layouts
        getMessagePanel().saveLayout(frostSettings);
        getSentMessagesPanel().saveTableFormat();
        getUnsentMessagesPanel().saveTableFormat();
    }

    /**
     * File | Exit action performed
     */
    private void fileExitMenuItem_actionPerformed(ActionEvent e) {
        
        // warn if create message windows are open
        if (MessageFrame.getOpenInstanceCount() > 0 ) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    language.getString("MainFrame.openCreateMessageWindows.body"),
                    language.getString("MainFrame.openCreateMessageWindows.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.NO_OPTION) {
                return;
            }
        }

        // warn if messages are currently uploading
        if (UnsentMessagesManager.getRunningMessageUploads() > 0 ) {
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

    /**
     * File | Statistics action performed
     */
    private void fileStatisticsMenuItem_actionPerformed(ActionEvent evt) {
        int msgCount = -1;
        int arcMsgCount = -1;
        int idCount = -1;
        int sharerCount = -1;
        int fileCount = -1;
        long fileSizes = -1;
        
        activateGlassPane(); // lock gui, show progress
        
        try {
            msgCount = AppLayerDatabase.getMessageTable().getMessageCount();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving msg count from db", e);
        }

        try {
            arcMsgCount = AppLayerDatabase.getMessageArchiveTable().getMessageCount();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving arc msg count from db", e);
        }

        try {
            idCount = AppLayerDatabase.getIdentitiesDatabaseTable().getIdentityCount();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving id count from db", e);
        }
        
        try {
            fileCount = AppLayerDatabase.getFileListDatabaseTable().getFileCount();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving file count from db", e);
        }

        try {
            sharerCount = AppLayerDatabase.getFileListDatabaseTable().getSharerCount();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving sharer count from db", e);
        }

        try {
            fileSizes = AppLayerDatabase.getFileListDatabaseTable().getFileSizes();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving file sizes from db", e);
        }

        deactivateGlassPane();
        
        StatisticsDialog dlg = new StatisticsDialog(this);
        dlg.startDialog(msgCount, arcMsgCount, idCount, sharerCount, fileCount, fileSizes);
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
        contentPanel.add(buildMainPanel(), BorderLayout.CENTER);
        contentPanel.add(getStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(getMainMenuBar());

        // step through all messages on disk up to maxMessageDisplay and check if there are new messages
        TOF.getInstance().searchAllNewMessages(false);

        tofAutomaticUpdateMenuItem.setSelected(frostSettings.getBoolValue(SettingsClass.BOARD_AUTOUPDATE_ENABLED));

        if (tofTree.getRowCount() > frostSettings.getIntValue(SettingsClass.BOARDLIST_LAST_SELECTED_BOARD)) {
            tofTree.setSelectionRow(frostSettings.getIntValue(SettingsClass.BOARDLIST_LAST_SELECTED_BOARD));
        }

        // make sure the font size isn't too small to see
        if (frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE) < 6) {
            frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, 6);
        }

        // load size, location and state of window
        int lastHeight = frostSettings.getIntValue(SettingsClass.MAINFRAME_LAST_HEIGHT);
        int lastWidth = frostSettings.getIntValue(SettingsClass.MAINFRAME_LAST_WIDTH);
        int lastPosX = frostSettings.getIntValue(SettingsClass.MAINFRAME_LAST_X);
        int lastPosY = frostSettings.getIntValue(SettingsClass.MAINFRAME_LAST_Y);
        boolean lastMaximized = frostSettings.getBoolValue(SettingsClass.MAINFRAME_LAST_MAXIMIZED);
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

        validate();
    }

    /**
     * Start the ticker that makes the mainframe waggle and starts board updates.
     */
    public void startTickerThread() {
        Thread tickerThread = new Thread("tick tack") {
            public void run() {
                while (true) {
                    Mixed.wait(1000);
                    // refactor this method in Core. lots of work :)
                    timer_actionPerformed();
                }
            }
        };
        tickerThread.start();
    }

    /**
     * Options | Preferences action performed
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
                TOF.getInstance().searchAllNewMessages(true);
                // reload all messages
                tofTree_actionPerformed(null);
            }
            if( optionsDlg.shouldResetLastBackloadUpdateFinishedMillis() ) {
                // reset lastBackloadUpdatedMillis for all boards
                getTofTreeModel().resetLastBackloadUpdateFinishedMillis();
            }
            if( optionsDlg.shouldResetSharedFilesLastDownloaded() ) {
                // reset lastDownloaded of all shared files
                Thread t = new Thread() {
                    public void run() {
                        try {
                            AppLayerDatabase.getFileListDatabaseTable().resetLastDownloaded();
                        } catch(Throwable tt) {
                            logger.log(Level.SEVERE, "Exception during resetLastDownloaded", tt);
                        }
                    }
                };
                t.start();
            }

            // repaint whole tree, in case the update visualization was enabled or disabled (or others)
            tofTree.updateTree();
        }
    }
    
    private void optionsManageLocalIdentitiesMenuItem_actionPerformed(ActionEvent e) {
        ManageLocalIdentitiesDialog dlg = new ManageLocalIdentitiesDialog();
        dlg.setVisible(true); // modal
        if( dlg.isIdentitiesImported() ) {
            // identities were imported, reload message table to show 'ME' for imported local identities
            tofTree_actionPerformed(null);
        }
    }

    private void optionsManageIdentitiesMenuItem_actionPerformed(ActionEvent e) {
        new IdentitiesBrowser(this).startDialog();
    }

    /**
     * Opens dialog to rename a folder.
     */
    public void renameFolder(Folder selected) {
        if (selected == null) {
            return;
        }
        String newname = null;
        do {
            newname = JOptionPane.showInputDialog(
                    this,
                    language.getString("MainFrame.dialog.renameFolder")+":\n",
                    selected.getName());
            if (newname == null) {
                return; // cancel
            }
        } while (newname.length() == 0);

        selected.setName(newname);
        updateTofTree(selected);
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

        RunningMessageThreadsInformation msgInfo = tofTree.getRunningBoardUpdateThreads().getRunningMessageThreadsInformation();
        FileTransferInformation fileInfo = Core.getInstance().getFileTransferManager().getFileTransferInformation();

        //////////////////////////////////////////////////
        //   Automatic TOF update
        //////////////////////////////////////////////////
        if (Core.isFreenetOnline() &&
            counter % 15 == 0 && // check all 15 seconds if a board update could be started
            isAutomaticBoardUpdateEnabled() &&
            msgInfo.getDownloadingBoardCount() < frostSettings.getIntValue(SettingsClass.BOARD_AUTOUPDATE_CONCURRENT_UPDATES))
        {
            Board nextBoard = BoardUpdateBoardSelector.selectNextBoard(tofTreeModel);
            if (nextBoard != null) {
                tofTree.updateBoard(nextBoard);
                logger.info("*** Automatic board update started for: " + nextBoard.getName());
            } else {
                logger.info("*** Automatic board update - min update interval not reached.  waiting...");
            }
        }

        //////////////////////////////////////////////////
        //   Display time in button bar
        //////////////////////////////////////////////////
        DateTime now = new DateTime(DateTimeZone.UTC);
        timeLabel.setText(
            new StringBuilder()
                .append(DateFun.FORMAT_DATE_VISIBLE.print(now))
                .append(" - ")
                .append(DateFun.FORMAT_TIME_VISIBLE.print(now))
                .toString());

        /////////////////////////////////////////////////
        //   Update status bar and file count in panels 
        /////////////////////////////////////////////////
        getStatusBar().setStatusBarInformations(fileInfo, msgInfo, tofTreeModel.getSelectedNode().getName());

        Core.getInstance().getFileTransferManager().updateWaitingCountInPanels(fileInfo);
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
        tofTree_actionPerformed(e, false);
    }
    
    public void tofTree_actionPerformed(TreeSelectionEvent e, boolean reload) {
        int i[] = tofTree.getSelectionRows();
        if (i != null && i.length > 0) {
            frostSettings.setValue(SettingsClass.BOARDLIST_LAST_SELECTED_BOARD, i[0]);
        }

        AbstractNode node = (AbstractNode) tofTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        boolean showSentMessagesPanel = false;
        boolean showUnsentMessagesPanel = false;
        
        if (node.isBoard()) {
            // node is a board
            removeBoardButton.setEnabled(true);
            renameFolderButton.setEnabled(false);
            configBoardButton.setEnabled(true);
            
            // save the selected message for later re-select if we changed between threaded/flat view
            FrostMessageObject previousMessage = null;
            if( reload ) {
                int[] rows = getMessageTreeTable().getSelectedRows();
                if( rows != null && rows.length > 0 ) {
                    previousMessage = (FrostMessageObject)getMessageTableModel().getRow(rows[0]);
                }
            }

            // remove previous msgs
            getMessagePanel().getMessageTable().setNewRootNode(new FrostMessageObject(true));
            getMessagePanel().updateMessageCountLabels(node);

            // read all messages for this board into message table (starts a thread)
            TOF.getInstance().updateTofTable((Board)node, previousMessage);

            getMessagePanel().getMessageTable().clearSelection();
        } else if (node.isFolder()) {
            // node is a folder
            getMessagePanel().getMessageTable().setNewRootNode(new FrostMessageObject(true));
            getMessagePanel().updateMessageCountLabels(node);

            renameFolderButton.setEnabled(true);
            if (node.isRoot()) {
                removeBoardButton.setEnabled(false);
            } else {
                removeBoardButton.setEnabled(true);
            }
            configBoardButton.setEnabled(false);
        } else if (node.isUnsentMessagesFolder()) {
            // remove previous msgs to save memory
            getMessagePanel().getMessageTable().setNewRootNode(new FrostMessageObject(true));

            removeBoardButton.setEnabled(false);
            configBoardButton.setEnabled(false);

            showUnsentMessagesPanel = true;
        } else if (node.isSentMessagesFolder()) {
            // remove previous msgs to save memory
            getMessagePanel().getMessageTable().setNewRootNode(new FrostMessageObject(true));

            removeBoardButton.setEnabled(false);
            configBoardButton.setEnabled(false);

            showSentMessagesPanel = true;
        }

        if( showSentMessagesPanel ) {
            showSentMessagePanelInSplitpane();
        } else if( showUnsentMessagesPanel ) {
            showUnsentMessagePanelInSplitpane();
        } else {
            showMessagePanelInSplitpane();
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
        configBoardButton.setToolTipText(language.getString("MainFrame.toolbar.tooltip.configureBoard"));
    }

    private void translateMainMenu() {
        fileMenu.setText(language.getString("MainFrame.menu.file"));
        fileExitMenuItem.setText(language.getString("Common.exit"));
        fileStatisticsMenuItem.setText(language.getString("MainFrame.menu.file.statistics"));
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

    public void updateSettings() {
        frostSettings.setValue(SettingsClass.BOARD_AUTOUPDATE_ENABLED, tofAutomaticUpdateMenuItem.isSelected());
    }
    
    /**
     * Selects message icon in lower right corner
     */
    public void displayNewMessageIcon(boolean showNewMessageIcon) {

        getStatusBar().showNewMessageIcon(showNewMessageIcon);

        if( JSysTrayIcon.getInstance() != null ) {
            try {
                if( showNewMessageIcon ) {
                    JSysTrayIcon.getInstance().setIcon(5); // new message icon
                } else {
                    JSysTrayIcon.getInstance().setIcon(0); // default icon
                }
            } catch(IOException ex) {
                logger.log(Level.SEVERE, "Exception during JSysTrayIcon.setIcon()", ex);
            }
        }

        final ImageIcon iconToSet;
        if (showNewMessageIcon) {
            iconToSet = frameIconNewMessage;
            // The title should never be changed on Windows systems (JSysTray.dll expects "Frost" as title)
            if( System.getProperty("os.name").startsWith("Windows") == false ) {
                String t = getTitle();
                // if not already done, append * on begin and end of title string
                if( !t.equals("*") && !(t.startsWith("*") && t.endsWith("*")) ) {
                    t = "*" + t + "*";
                }
                setTitle(t);
            }
        } else {
            iconToSet = frameIconDefault;
            // The title should never be changed on Windows systems (JSysTray.dll expects "Frost" as title)
            if( System.getProperty("os.name").startsWith("Windows") == false ) {
                String t = getTitle();
                // if not already done, remove * on begin and end of title string
                if( !t.equals("*") && t.startsWith("*") && t.endsWith("*") ) {
                    // remove * on begin and end
                    t = t.substring(1, t.length()-1);
                }
                setTitle(t);
            }
        }
        setIconImage(iconToSet.getImage());
    }

    /**
     * Fires a nodeChanged (redraw) for this board and updates buttons.
     */
    public void updateTofTree(AbstractNode board) {
        // fire update for node
        tofTreeModel.nodeChanged(board);
        // also update all parents
        TreeNode parentFolder = board.getParent();
        while (parentFolder != null) {
            tofTreeModel.nodeChanged(parentFolder);
            parentFolder = parentFolder.getParent();
        }
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
    }
    
    public void showHtmlHelp(String item) {
        if( Core.isHelpHtmlSecure() == false ) {
            return;
        }
        if( helpBrowser == null ) {
            helpBrowser = new HelpBrowserFrame(frostSettings.getValue(SettingsClass.LANGUAGE_LOCALE), "help/help.zip");
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
    
    private class WindowClosingListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            fileExitMenuItem_actionPerformed(null);
        }
    }
    
    private class WindowStateListener extends WindowAdapter {
        public void windowStateChanged(WindowEvent e) {
            // maybe minimize to tray
            if( (e.getNewState() & Frame.ICONIFIED) != 0 ) {
                // Hide the Frost window
                // because this WindowEvent arrives when we are already minimized, JSYstray can't know
                // if we were maimized before or not. Therefore tell JSystrayIcon if we were maximized.
                if ( Core.frostSettings.getBoolValue(SettingsClass.MINIMIZE_TO_SYSTRAY)
                        && JSysTrayIcon.getInstance() != null) 
                {
                    boolean wasMaximized = ((e.getOldState() & Frame.MAXIMIZED_BOTH) != 0);
                    try { 
                        if( wasMaximized ) {
                            JSysTrayIcon.getInstance().showWindow(JSysTrayIcon.SHOW_CMD_HIDE_WAS_MAXIMIZED);
                        } else {
                            JSysTrayIcon.getInstance().showWindow(JSysTrayIcon.SHOW_CMD_HIDE);
                        }
                    } catch (IOException _IoExc) {
                    }
                }
            }
        }
    }
    
    public void activateGlassPane() {
        showProgress();
        
        // Mount the glasspane on the component window
        GlassPane aPane = GlassPane.mount(this, true);

        // keep track of the glasspane as an instance variable
        glassPane = aPane;

        if (glassPane != null) {
            // Start interception UI interactions
            glassPane.setVisible(true);
        }
    }
    
    public void deactivateGlassPane() {
        if (glassPane != null) {
            // Stop UI interception
            glassPane.setVisible(false);
            glassPane = null;
        }
        hideProgress();
    }
    
    /**
     *  Updates the component tree UI of all the frames and dialogs of the application
     */
    public void updateComponentTreesUI() {
        Frame[] appFrames = Frame.getFrames();
        JSkinnablePopupMenu[] appPopups = JSkinnablePopupMenu.getSkinnablePopupMenus();
        for (int i = 0; i < appFrames.length; i++) { //Loop to update all the frames
            SwingUtilities.updateComponentTreeUI(appFrames[i]);
            Window[] ownedWindows = appFrames[i].getOwnedWindows();
            for (int j = 0; j < ownedWindows.length; j++) { //Loop to update the dialogs
                if (ownedWindows[j] instanceof Dialog) {
                    SwingUtilities.updateComponentTreeUI(ownedWindows[j]);
                }
            }
        }
        for (int i = 0; i < appPopups.length; i++) { //Loop to update all the popups
            SwingUtilities.updateComponentTreeUI(appPopups[i]);
        }
        // the panels are not all in the component tree, update them manually
        SwingUtilities.updateComponentTreeUI(getMessagePanel());
        SwingUtilities.updateComponentTreeUI(getSentMessagesPanel());
        SwingUtilities.updateComponentTreeUI(getUnsentMessagesPanel());
        repaint();
    }
    
    /**
     * Enqueue a message that is shown after the mainframe became visible.
     * Used to show messages about problems occured during loading (e.g. missing shared files).
     */
    public static void enqueueStartupMessage(StartupMessage sm) {
        queuedStartupMessages.add( sm );
    }

    /**
     * Show the enqueued messages and finally clear the messages queue.
     */
    public void showStartupMessages() {
        for(Iterator<StartupMessage> i=queuedStartupMessages.iterator(); i.hasNext(); ) {
            StartupMessage sm = i.next();
            sm.display(this);
        }
        // cleanup
        StartupMessage.cleanup();
        queuedStartupMessages.clear();
        queuedStartupMessages = null;
    }
}
