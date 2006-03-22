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
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import frost.boards.*;
import frost.ext.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.UploadPanel;
import frost.gui.*;
import frost.gui.help.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.gui.preferences.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

 /**
  * TODO: - after removing a board, let current board selected (currently if you
  *          delete another than selected board the tofTree is updated)
  */
public class MainFrame extends JFrame implements ClipboardOwner, SettingsUpdater {
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
    public static SettingsClass frostSettings = null;

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
    private JMenuItem helpMemMonMenuItem = new JMenuItem("Show memory monitor");

    //Help Menu
    private JMenu helpMenu = new JMenu();
    private JRadioButtonMenuItem languageBulgarianMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageDefaultMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageDutchMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageEnglishMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageFrenchMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageGermanMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageItalianMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageJapaneseMenuItem = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem languageRussianMenuItem = new JRadioButtonMenuItem();

    //Language Menu
    private JMenu languageMenu = new JMenu();

    private Language language = null;
    private JRadioButtonMenuItem languageSpanishMenuItem = new JRadioButtonMenuItem();

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
//    private JMenuItem pluginBrowserMenuItem = new JMenuItem();

    //Plugin Menu
//    private JMenu pluginMenu = new JMenu();
//    private JMenuItem pluginTranslateMenuItem = new JMenuItem();

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
    private JMenuItem tofDecreaseFontSizeMenuItem = new JMenuItem();

    private JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem();
    private JMenuItem tofDisplayKnownBoards = new JMenuItem();
    private JMenuItem tofSearchMessages = new JMenuItem();

    private JMenuItem tofIncreaseFontSizeMenuItem = new JMenuItem();

    //Messages (tof) Menu
    private JMenu tofMenu = new JMenu();

    private TofTree tofTree = null;
    private TofTreeModel tofTreeModel = null;
    private UploadPanel uploadPanel = null;

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
            toolkit.configureButton(newBoardButton, "New board", "/data/newboard_rollover.gif", language);
            toolkit.configureButton(newFolderButton, "New folder", "/data/newfolder_rollover.gif", language);
            toolkit.configureButton(removeBoardButton, "Remove board", "/data/remove_rollover.gif", language);
            toolkit.configureButton(renameFolderButton, "Rename folder", "/data/rename_rollover.gif", language);
            toolkit.configureButton(boardInfoButton, "Board Information Window", "/data/info_rollover.gif", language);
            toolkit.configureButton(systemTrayButton, "Minimize to System Tray", "/data/tray_rollover.gif", language);
            toolkit.configureButton(knownBoardsButton, "Display list of known boards", "/data/knownboards_rollover.gif", language);
            toolkit.configureButton(searchMessagesButton, "Search messages", "/data/search_rollover.gif", language);

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
                    renameNode(tofTreeModel.getSelectedNode());
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
            tofIncreaseFontSizeMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // make size of the message body font one point bigger
                    int size = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
                    frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, size + 1);
                }
            });
            tofDecreaseFontSizeMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // make size of the message body font one point smaller
                    int size = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
                    frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, size - 1);
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
//            pluginTranslateMenuItem.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    TranslateFrame translate = new TranslateFrame(true);
//                    translate.setVisible(true);
//                }
//            });
            languageDefaultMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes");
                    frostSettings.setValue("locale", "default");
                    setLanguageResource(bundle);
                }
            });

            languageBulgarianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_bg.png", 16, 16));
            languageGermanMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_de.png", 16, 16));
            languageEnglishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_en.png", 16, 16));
            languageSpanishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_es.png", 16, 16));
            languageFrenchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_fr.png", 16, 16));
            languageItalianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_it.png", 16, 16));
            languageJapaneseMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_jp.png", 16, 16));
            languageDutchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_nl.png", 16, 16));
            languageRussianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_ru.png", 16, 16));

            languageGermanMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("de"));
                    frostSettings.setValue("locale", "de");
                    setLanguageResource(bundle);
                }
            });
            languageEnglishMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("en"));
                    frostSettings.setValue("locale", "en");
                    setLanguageResource(bundle);
                }
            });
            languageDutchMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("nl"));
                    frostSettings.setValue("locale", "nl");
                    setLanguageResource(bundle);
                }
            });
            languageFrenchMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("fr"));
                    frostSettings.setValue("locale", "fr");
                    setLanguageResource(bundle);
                }
            });
            languageJapaneseMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("ja"));
                    frostSettings.setValue("locale", "ja");
                    setLanguageResource(bundle);
                }
            });
            languageRussianMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("ru"));
                    frostSettings.setValue("locale", "ru");
                    setLanguageResource(bundle);
                }
            });
            languageItalianMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("it"));
                    frostSettings.setValue("locale", "it");
                    setLanguageResource(bundle);
                }
            });
            languageSpanishMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("es"));
                    frostSettings.setValue("locale", "es");
                    setLanguageResource(bundle);
                }
            });
            languageBulgarianMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ResourceBundle bundle = ResourceBundle.getBundle("res.LangRes", new Locale("bg"));
                    frostSettings.setValue("locale", "bg");
                    setLanguageResource(bundle);
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
            helpAboutMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    helpAboutMenuItem_actionPerformed(e);
                }
            });
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
            // Plugin Menu
//            pluginMenu.add(pluginBrowserMenuItem);
//            pluginMenu.add(pluginTranslateMenuItem);
            // Language Menu
            ButtonGroup languageMenuButtonGroup = new ButtonGroup();
            languageDefaultMenuItem.setSelected(true);
            languageMenuButtonGroup.add(languageDefaultMenuItem);
            languageMenuButtonGroup.add(languageBulgarianMenuItem);
            languageMenuButtonGroup.add(languageDutchMenuItem);
            languageMenuButtonGroup.add(languageEnglishMenuItem);
            languageMenuButtonGroup.add(languageFrenchMenuItem);
            languageMenuButtonGroup.add(languageGermanMenuItem);
            languageMenuButtonGroup.add(languageItalianMenuItem);
            languageMenuButtonGroup.add(languageJapaneseMenuItem);
            languageMenuButtonGroup.add(languageRussianMenuItem);
            languageMenuButtonGroup.add(languageSpanishMenuItem);

            // Selects the language menu option according to the settings
            HashMap languageMenuItems = new HashMap();
            languageMenuItems.put("default", languageDefaultMenuItem);
            languageMenuItems.put("de", languageGermanMenuItem);
            languageMenuItems.put("en", languageEnglishMenuItem);
            languageMenuItems.put("nl", languageDutchMenuItem);
            languageMenuItems.put("fr", languageFrenchMenuItem);
            languageMenuItems.put("ja", languageJapaneseMenuItem);
            languageMenuItems.put("it", languageItalianMenuItem);
            languageMenuItems.put("es", languageSpanishMenuItem);
            languageMenuItems.put("bg", languageBulgarianMenuItem);
            languageMenuItems.put("ru", languageRussianMenuItem);

            String setLanguage = frostSettings.getValue("locale");
            Object languageItem = languageMenuItems.get(setLanguage);
            if (languageItem != null) {
                languageMenuButtonGroup.setSelected(((JMenuItem) languageItem).getModel(), true);
            }

            languageMenu.add(languageDefaultMenuItem);
            languageMenu.addSeparator();
            languageMenu.add(languageBulgarianMenuItem);
            languageMenu.add(languageDutchMenuItem);
            languageMenu.add(languageEnglishMenuItem);
            languageMenu.add(languageFrenchMenuItem);
            languageMenu.add(languageGermanMenuItem);
            languageMenu.add(languageItalianMenuItem);
            languageMenu.add(languageJapaneseMenuItem);
            languageMenu.add(languageRussianMenuItem);
            languageMenu.add(languageSpanishMenuItem);
            // Help Menu
            helpMenu.add(helpMemMonMenuItem);
            helpMenu.add(helpHelpMenuItem);
            helpMenu.add(helpAboutMenuItem);
            // add all to bar
            menuBar.add(fileMenu);
            menuBar.add(tofMenu);
            menuBar.add(optionsMenu);
//            menuBar.add(pluginMenu);
            menuBar.add(languageMenu);
            menuBar.add(helpMenu);

            translateMainMenu();
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

        statusLabel = new JLabel(language.getString("Frost by Jantho"));
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
        //add a tab for buddies perhaps?
        getTabbedPane().insertTab("News", null, getMessagePanel(), null, 0);
        getTabbedPane().setSelectedIndex(0);

        JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
        tofTreeScrollPane.setWheelScrollingEnabled(true);
        // tofTree selection listener
        tofTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                tofTree_actionPerformed(e);
            }
        });

        JSplitPane treeAndTabbedPane =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, getTabbedPane());
        treeAndTabbedPane.setDividerLocation(160);
        // Vertical Board Tree / MessagePane Divider

        JPanel tofMainPanel = new JPanel(new BorderLayout());
        tofMainPanel.add(treeAndTabbedPane, BorderLayout.CENTER); // TOF/Text
        return tofMainPanel;
    }

    /**
     * Returns true if board is allowed to be updated.
     * Also checks if board update is already running.
     * @param board
     * @return
     */
    public boolean doUpdate(Board board) {
        if (tofTree.isUpdateAllowed(board) == false)
            return false;

        if (board.isUpdating())
            return false;

        return true;
    }

    /**
     * File | Exit action performed
     * @param e
     */
    private void fileExitMenuItem_actionPerformed(ActionEvent e) {

        // TODO: move to saveable???
        // TODO: save msg table column sizes!!!

        // save size,location and state of window
        Rectangle bounds = getBounds();
        boolean isMaximized = ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);

        frostSettings.setValue("lastFrameMaximized", isMaximized);

        if (!isMaximized) { // Only save the current dimension if frame is not maximized
            frostSettings.setValue("lastFrameHeight", bounds.height);
            frostSettings.setValue("lastFrameWidth", bounds.width);
            frostSettings.setValue("lastFramePosX", bounds.x);
            frostSettings.setValue("lastFramePosY", bounds.y);
        }

        getMessagePanel().getMessageTable().saveLayout(frostSettings);

        if (tofTree.getRunningBoardUpdateThreads().getRunningUploadThreadCount() > 0) {
            int result =
                JOptionPane.showConfirmDialog(
                    this,
                    language.getString("UploadsUnderway.body"),
                    language.getString("UploadsUnderway.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    public MessagePanel getMessagePanel() {
        if (messagePanel == null) {
            messagePanel = new MessagePanel(frostSettings, this);
            messagePanel.setParentFrame(this);
            messagePanel.setIdentities(core.getIdentities());
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
        TOF.getInstance().initialSearchNewMessages();

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
                TOF.getInstance().initialSearchNewMessages();
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

    /**
     * Opens dialog to rename the board / folder.
     * For boards it checks for double names.
     * @param selected
     */
    public void renameNode(Board selected) {
        if (selected == null)
            return;
        String newname = null;
        do {
            newname =
                JOptionPane.showInputDialog(
                    this,
                    "Please enter the new name:\n",  // TODO: translate
                    selected.getName());
            if (newname == null)
                return; // cancel
            if (selected.isFolder() == false
                && // double folder names are ok
            tofTreeModel.getBoardByName(newname) != null) {
                JOptionPane.showMessageDialog(
                    this,
                    "You already have a board with name '" // TODO: translate
                        + newname
                        + "'!\nPlease choose a new name.");
                newname = ""; // loop again
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
    public Board selectNextBoard(Vector boards) {
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

        for (int i = 0; i < boards.size(); i++) {
            board = (Board) boards.get(i);
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
            logger.info(
                "*** Automatic board update - min update interval not reached.  waiting...");
        }
        return nextBoard;
    }

    /**
     * Setter for thelanguage resource bundle
     * @param newLanguageResource
     */
    private void setLanguageResource(ResourceBundle newLanguageResource) {
        language.setLanguageResource(newLanguageResource);
        translateMainMenu();
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
     * timer Action Listener (automatic download)
     */
    public void timer_actionPerformed() {
        // this method is called by a timer each second, so this counter counts seconds
        counter++;

        //////////////////////////////////////////////////
        //   Automatic TOF update
        //////////////////////////////////////////////////
        if (counter % 15 == 0 && // check all 5 seconds if a board update could be started
           isAutomaticBoardUpdateEnabled() &&
           tofTree.getRunningBoardUpdateThreads().getUpdatingBoardCount()
                < frostSettings.getIntValue("automaticUpdate.concurrentBoardUpdates"))
        {
            Vector boards = tofTreeModel.getAllBoards();
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
                .append("   " + language.getString("TOFUP") + ": ")
                .append(tofTree.getRunningBoardUpdateThreads().getUploadingBoardCount())
                .append("B / ")
                .append(tofTree.getRunningBoardUpdateThreads().getRunningUploadThreadCount())
                .append("T")
                .append("   " + language.getString("TOFDO") + ": ")
                .append(tofTree.getRunningBoardUpdateThreads().getUpdatingBoardCount())
                .append("B / ")
                .append(tofTree.getRunningBoardUpdateThreads().getRunningDownloadThreadCount())
                .append("T")
                .append("   " + language.getString("Selected board") + ": ")
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

                // read all messages for this board into message table
                TOF.getInstance().updateTofTable(node, keypool);
                getMessagePanel().getMessageTable().clearSelection();
            } else {
                // node is a folder
                getMessagePanel().getMessageTableModel().clearDataModel();
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
        newBoardButton.setToolTipText(language.getString("New board"));
        systemTrayButton.setToolTipText(language.getString("Minimize to System Tray"));
        knownBoardsButton.setToolTipText(language.getString("Display list of known boards"));
        searchMessagesButton.setToolTipText(language.getString("Search messages"));
        boardInfoButton.setToolTipText(language.getString("Board Information Window"));
        newFolderButton.setToolTipText(language.getString("New folder"));
        removeBoardButton.setToolTipText(language.getString("Remove board"));
        renameFolderButton.setToolTipText(language.getString("Rename folder"));
    }

    private void translateMainMenu() {
        fileMenu.setText(language.getString("File"));
        fileExitMenuItem.setText(language.getString("Exit"));
        tofMenu.setText(language.getString("News"));
        tofDisplayBoardInfoMenuItem.setText(
                language.getString("Display board information window"));
        tofAutomaticUpdateMenuItem.setText(language.getString("Automatic message update"));
        tofIncreaseFontSizeMenuItem.setText(language.getString("Increase Font Size"));
        tofDecreaseFontSizeMenuItem.setText(language.getString("Decrease Font Size"));
        tofDisplayKnownBoards.setText(language.getString("Display known boards"));
        tofSearchMessages.setText(language.getString("Search messages"));
        optionsMenu.setText(language.getString("Options"));
        optionsPreferencesMenuItem.setText(language.getString("Preferences"));
//        pluginMenu.setText(language.getString("Plugins"));
//        pluginBrowserMenuItem.setText(language.getString("Experimental Freenet Browser"));
//        pluginTranslateMenuItem.setText(language.getString("Translate Frost into another language"));
        languageMenu.setText(language.getString("Language"));
        languageDefaultMenuItem.setText(language.getString("Default"));
        languageDutchMenuItem.setText(language.getString("Dutch"));
        languageEnglishMenuItem.setText(language.getString("English"));
        languageFrenchMenuItem.setText(language.getString("French"));
        languageGermanMenuItem.setText(language.getString("German"));
        languageItalianMenuItem.setText(language.getString("Italian"));
        languageJapaneseMenuItem.setText(language.getString("Japanese"));
        languageSpanishMenuItem.setText(language.getString("Spanish"));
        languageBulgarianMenuItem.setText(language.getString("Bulgarian"));
        languageRussianMenuItem.setText(language.getString("Russian"));
        helpMenu.setText(language.getString("Help"));
        helpHelpMenuItem.setText(language.getString("Help"));
        helpAboutMenuItem.setText(language.getString("About"));
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

        if (board == tofTreeModel.getSelectedNode()) // is the board actually shown?
        {
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

    public void showHtmlHelp(String item) {
      if( helpBrowser == null ) {
          helpBrowser = new HelpBrowserFrame(frostSettings.getValue("locale"), "help/help.zip");
      }
      // show first time or bring to front
      helpBrowser.setVisible(true);
      helpBrowser.showHelpPage(item);

      return;
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
    public MessageTableModel getMessageTableModel() {
        // forward to MessagePanel
        return getMessagePanel().getMessageTableModel();
    }
}
