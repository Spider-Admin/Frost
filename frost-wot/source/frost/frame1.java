/*
  frame1.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
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
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.net.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.filechooser.*;
import java.io.*;
import javax.swing.tree.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.datatransfer.*;
import frost.FcpTools.*;
import frost.gui.*;
import frost.gui.model.*;
import frost.ext.*;
import frost.components.*;
import frost.crypt.*;

public class frame1 extends JFrame implements ClipboardOwner {
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");
    static ImageIcon[] newMessage = new ImageIcon[2];
//    Hashtable messages = new Hashtable();
    String clipboard = new String();
    int counter = 55;
    int idleTime = 0;

    private static frame1 instance = null; // set in constructor
    boolean started = false;
    boolean stopTofTreeUpdate = false;
    public static boolean updateDownloads = true;
    public static Vector activeTofThreads = new Vector();
    public static Vector TOFThreads = new Vector();
    public static Vector GRTThreads = new Vector();
    public static int tofUploadThreads = 0;
    public static int tofDownloadThreads = 0;
    public static boolean updateTof = false;
    public static boolean updateTree = false;
    public static String fileSeparator = System.getProperty("file.separator");
    public static String keypool = "keypool" + fileSeparator;
    public static String newMessageHeader = new String("");
    public static String oldMessageHeader = new String("");
    public static int activeUploadThreads = 0;
    public static int activeDownloadThreads = 0;
    private String lastSelectedMessage;
    private String lastUsedBoard = "Frost";
    public static int tofUpdateSpeed = 6; // Default at least 6!
    public static int tofUpdateInterleave = 60; // Default 60
    public static Map boardStats = Collections.synchronizedMap(new TreeMap());
    public static VerifyableMessageObject selectedMessage = new VerifyableMessageObject();
    public static boolean generateCHK = false;

    private Hashtable boardsThatContainNewMessages = new Hashtable();

    public static volatile Object threadCountLock = new Object();

    //the identity stuff.  This really shouldn't be here but where else?

    public static ObjectInputStream id_reader;
    public ObjectOutputStream id_writer;
    public static LocalIdentity mySelf;
    public static BuddyList friends,enemies;
    public static crypt crypto;

    // saved to frost.ini
    public static SettingsClass frostSettings = new SettingsClass();
    public static AltEdit altEdit;

    javax.swing.Timer timer; // Uploads / Downloads
    java.util.Timer timer2;

    //a shutdown hook
    public Thread saver;
    private TimerTask cleaner;
    private static CleanUp fileCleaner = new CleanUp("keypool",frostSettings.getIntValue("maxMessageDisplay")+1,false);

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------

    JPanel contentPanel;
    JPanel statusPanel = new JPanel(new BorderLayout());
    JPanel searchMainPanel = new JPanel(new BorderLayout());
    //JPanel searchTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JPanel searchTopPanel = null;
    JPanel downloadMainPanel = new JPanel(new BorderLayout());
    JPanel downloadTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JPanel uploadMainPanel = new JPanel(new BorderLayout());
    JPanel uploadTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JPanel tofMainPanel = new JPanel(new BorderLayout());
    //JPanel tofTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JPanel tofTopPanel = null;
    JPanel messageTablePanel = new JPanel(new BorderLayout());
    JPanel tofAttachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JPanel boardAttachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JPanel buttonPanel = new JPanel(new GridBagLayout());

    GridBagConstraints buttonPanelConstr = new GridBagConstraints();

    String[] searchComboBoxItems = {"All files", "Audio", "Video", "Images", "Documents", "Executables", "Archives"};
    JComboBox searchComboBox = new JComboBox(searchComboBoxItems);

    JTextField searchTextField = new JTextField(25);
    JTextField downloadTextField = new JTextField(25);

    JTextArea tofTextArea = new JTextArea();

    JLabel statusLabel = new JLabel(LangRes.getString("Frost by Jantho"));
    static JLabel statusMessageLabel = new JLabel();
    JLabel timeLabel = new JLabel("");

    final String allMessagesCountPrefix = "Msg: ";
    final String newMessagesCountPrefix = "New: ";
    JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix+"0");
    JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix+"0");
    private String searchResultsCountPrefix = null;
    JLabel searchResultsCountLabel = new JLabel();

    JCheckBox downloadActivateCheckBox = new JCheckBox(new ImageIcon(frame1.class.getResource("/data/down.gif")), true);
//     JCheckBox uploadActivateCheckBox = new JCheckBox(new ImageIcon(frame1.class.getResource("/data/up.gif")), true);
    JCheckBox searchAllBoardsCheckBox= new JCheckBox("all boards",true);
//    JCheckBox reducedBlockCheckCheckBox= new JCheckBox();

    public static DownloadTableModel downloadTableModel = new DownloadTableModel();
    public static SearchTableModel searchTableModel = new SearchTableModel();
    public static MessageTableModel messageTableModel = new MessageTableModel();
    public static UploadTableModel uploadTableModel = new UploadTableModel();
    AttachmentTableModel attachmentTableModel = new AttachmentTableModel();
    AttachmentTableModel boardTableModel = new AttachmentTableModel();

    Vector downloadTableColumnNames = new Vector();
    Vector uploadTableColumnNames = new Vector();
    Vector searchTableColumnNames = new Vector();
    Vector messageTableColumnNames = new Vector();
    Vector attachmentTableColumnNames = new Vector();
    Vector boardTableColumnNames = new Vector();

    public static JTable downloadTable = new JTable(downloadTableModel);
    public static JTable uploadTable = new JTable(uploadTableModel);
    SearchTable searchTable = new SearchTable(searchTableModel);
    MessageTable messageTable = new MessageTable(messageTableModel);
    JTable attachmentTable = new JTable(attachmentTableModel);
    JTable boardTable = new JTable(boardTableModel);

    DefaultListSelectionModel searchTableListModel = new DefaultListSelectionModel();
    DefaultListSelectionModel messageTableListModel = new DefaultListSelectionModel();

    JButton searchDownloadButton = new JButton(new ImageIcon(frame1.class.getResource("/data/save.gif")));
    JButton tofUpdateButton = new JButton(new ImageIcon(frame1.class.getResource("/data/update.gif")));
    public static JButton searchButton = new JButton(new ImageIcon(frame1.class.getResource("/data/search.gif")));
    JButton uploadAddFilesButton = new JButton(new ImageIcon(frame1.class.getResource("/data/browse.gif")));
    JButton tofNewMessageButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newmessage.gif")));
    JButton tofReplyButton = new JButton(new ImageIcon(frame1.class.getResource("/data/reply.gif")));
    JButton newBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newboard.gif")));
    JButton removeBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/remove.gif")));
    JButton renameBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/rename.gif")));
    JButton cutBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/cut.gif")));
    JButton copyBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/copy.gif")));
    JButton pasteBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/paste.gif")));
    JButton configBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/configure.gif")));
    JButton downloadAttachmentsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachment.gif")));
    JButton downloadBoardsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachmentBoard.gif")));
    JButton saveMessageButton= new JButton(new ImageIcon(frame1.class.getResource("/data/save.gif")));
    JButton boardInfoButton= new JButton(new ImageIcon(frame1.class.getResource("/data/info.gif")));
    JButton systemTrayButton= new JButton(new ImageIcon(frame1.class.getResource("/data/tray.gif")));
    JButton trustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/trust.gif")));
    JButton notTrustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/nottrust.gif")));

    JMenuBar menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu(LangRes.getString("File"));
    JMenuItem fileExitMenuItem = new JMenuItem(LangRes.getString("Exit"));

    JMenu tofMenu = new JMenu(LangRes.getString("News"));
    JMenuItem tofConfigureBoardMenuItem = new JMenuItem(LangRes.getString("Configure selected board"));
    JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem(LangRes.getString("Display board information window"));
    JCheckBoxMenuItem tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem(LangRes.getString("Automatic message update"), true);
    JMenuItem tofIncreaseFontSizeMenuItem = new JMenuItem (LangRes.getString ("Increase Font Size"));
    JMenuItem tofDecreaseFontSizeMenuItem = new JMenuItem (LangRes.getString ("Decrease Font Size"));

    JMenu optionsMenu = new JMenu(LangRes.getString("Options"));
    JMenuItem optionsPreferencesMenuItem = new JMenuItem(LangRes.getString("Preferences"));

    JMenu pluginMenu = new JMenu("Plugin");
    JMenuItem pluginBrowserMenuItem = new JMenuItem("EFB (Experimental Freenet Browser)");

    JMenu helpMenu = new JMenu(LangRes.getString("Help"));
    JMenuItem helpHelpMenuItem = new JMenuItem("Help");
    JMenuItem helpAboutMenuItem = new JMenuItem(LangRes.getString("About"));

    JPopupMenu searchPopupMenu = new JPopupMenu();
    JMenuItem searchPopupDownloadSelectedKeys = new JMenuItem(LangRes.getString("Download selected keys"));
    JMenuItem searchPopupDownloadAllKeys = new JMenuItem(LangRes.getString("Download all keys"));
    JMenuItem searchPopupCopyAttachment = new JMenuItem(LangRes.getString("Copy as attachment to clipboard"));
    JMenuItem searchPopupCancel = new JMenuItem(LangRes.getString("Cancel"));

    JPopupMenu uploadPopupMenu = new JPopupMenu();
    JMenuItem uploadPopupMoveSelectedFilesUp = new JMenuItem(LangRes.getString("Move selected files up"));
    JMenuItem uploadPopupMoveSelectedFilesDown = new JMenuItem(LangRes.getString("Move selected files down"));
    JMenuItem uploadPopupRemoveSelectedFiles = new JMenuItem(LangRes.getString("Remove selected files"));
    JMenuItem uploadPopupRemoveAllFiles = new JMenuItem(LangRes.getString("Remove all files"));
    JMenuItem uploadPopupReloadSelectedFiles = new JMenuItem(LangRes.getString("Reload selected files"));
    JMenuItem uploadPopupReloadAllFiles = new JMenuItem(LangRes.getString("Reload all files"));
    JMenuItem uploadPopupSetPrefixForSelectedFiles = new JMenuItem(LangRes.getString("Set prefix for selected files"));
    JMenuItem uploadPopupSetPrefixForAllFiles = new JMenuItem(LangRes.getString("Set prefix for all files"));
    JMenuItem uploadPopupRestoreDefaultFilenamesForSelectedFiles = new JMenuItem(LangRes.getString("Restore default filenames for selected files"));
    JMenuItem uploadPopupRestoreDefaultFilenamesForAllFiles = new JMenuItem(LangRes.getString("Restore default filenames for all files"));
    JMenu uploadPopupChangeDestinationBoard = new JMenu(LangRes.getString("Change destination board"));
    JMenuItem uploadPopupAddFilesToBoard = new JMenuItem(LangRes.getString("Add files to board"));
    JMenuItem uploadPopupCancel = new JMenuItem(LangRes.getString("Cancel"));

    JPopupMenu downloadPopupMenu = new JPopupMenu(); // Downloads popup
    JMenuItem downloadPopupRemoveSelectedDownloads = new JMenuItem(LangRes.getString("Remove selected downloads"));
    JMenuItem downloadPopupRemoveAllDownloads = new JMenuItem(LangRes.getString("Remove all downloads"));
    JMenuItem downloadPopupResetHtlValues = new JMenuItem(LangRes.getString("Retry selected downloads"));
    JMenuItem downloadPopupMoveUp = new JMenuItem(LangRes.getString("Move selected downloads up"));
    JMenuItem downloadPopupMoveDown = new JMenuItem(LangRes.getString("Move selected downloads down"));
    JMenuItem downloadPopupCancel = new JMenuItem(LangRes.getString("Cancel"));

    JPopupMenu tofTextPopupMenu = new JPopupMenu(); // TOF text popup
    JMenuItem tofTextPopupSaveMessage = new JMenuItem(LangRes.getString("Save message to disk"));
    JMenuItem tofTextPopupSaveAttachments = new JMenuItem(LangRes.getString("Download attachment(s)"));
    JMenuItem tofTextPopupSaveAttachment = new JMenuItem("Download selected attachment");
    JMenuItem tofTextPopupSaveBoards = new JMenuItem("Add board(s)");
    JMenuItem tofTextPopupSaveBoard = new JMenuItem("Add selected board");
    JMenuItem tofTextPopupCancel = new JMenuItem(LangRes.getString("Cancel"));

    JPopupMenu tofTreePopupMenu = new JPopupMenu(); // TOF tree popup
    JMenuItem tofTreePopupRefresh = new JMenuItem("Refresh board/folder");
    JMenuItem tofTreePopupAddNode = new JMenuItem(LangRes.getString("Add new board / folder"));
    JMenuItem tofTreePopupRemoveNode = new JMenuItem(LangRes.getString("Remove selected board / folder"));
    JMenuItem tofTreePopupCopyNode = new JMenuItem(LangRes.getString("Copy selected board / folder"));
    JMenuItem tofTreePopupCutNode = new JMenuItem(LangRes.getString("Cut selected board / folder"));
    JMenuItem tofTreePopupPasteNode = new JMenuItem(LangRes.getString("Paste board / folder"));
    JMenuItem tofTreePopupConfigureBoard = new JMenuItem(LangRes.getString("Configure selected board"));
    JMenuItem tofTreePopupCancel = new JMenuItem(LangRes.getString("Cancel"));

    JTabbedPane tabbedPane = new JTabbedPane();

    private DefaultMutableTreeNode tofTreeNode = new DefaultMutableTreeNode("Frost Message System");
    private TofTree tofTree = new TofTree(tofTreeNode);

    JScrollPane searchTableScrollPane = new JScrollPane(searchTable);
    JScrollPane downloadTableScrollPane = new JScrollPane(downloadTable);
    JScrollPane messageTableScrollPane = new JScrollPane(messageTable);
    JScrollPane uploadTableScrollPane = new JScrollPane(uploadTable);
    JScrollPane tofTextAreaScrollPane = new JScrollPane(tofTextArea);
    JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
    JScrollPane attachmentTableScrollPane = new JScrollPane(attachmentTable);
    JScrollPane boardTableScrollPane = new JScrollPane(boardTable);

    JSplitPane tofSplitPane, tofSplitPane1, tofSplitPane2, tofSplitPane3;
    JSplitPane attachmentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tofTextAreaScrollPane, attachmentTableScrollPane);
    JSplitPane boardSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, attachmentSplitPane, boardTableScrollPane);

    boolean loaded_tables;

    // returns the current id,crypt, etc.
    public static LocalIdentity getMyId() {return mySelf;}
    public static BuddyList getFriends() {return friends;}
    public static crypt getCrypto() {return crypto;}
    public static BuddyList getEnemies() {return enemies;}
    //------------------------------------------------------------------------

    public static frame1 getInstance()
    {
        return instance;
    }

    public TofTree getTofTree()
    {
        return tofTree;
    }
    public MessageTable getMessageTable()
    {
        return messageTable;
    }
    public String getLastUsedBoard()
    {
        return lastUsedBoard;
    }

    /**Construct the frame*/
    public frame1() {
        instance = this;
        loaded_tables=false;

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
        jbInit();

        saver = new Thread() {
        public void run() {
            System.out.println("saving identities");
            File identities = new File("identities");

            try{ //TODO: complete this
            /*    id_writer = new ObjectOutputStream(new FileOutputStream(identities));
            //System.out.println("myself: " + frame1.getMyId().toString());
            //id_writer.writeObject(frame1.getMyId());
            System.out.println("friends: " + frame1.getFriends().toString());
            id_writer.writeObject(frame1.getFriends());
            System.out.println("enemies: " + frame1.getEnemies().toString());
            id_writer.writeObject(frame1.getEnemies());
            id_writer.close();*/
	    FileWriter fout = new FileWriter(identities);
		fout.write(mySelf.getName() + "\n");
		fout.write(mySelf.getKeyAddress() + "\n");
		fout.write(mySelf.getKey() + "\n");
		fout.write(mySelf.getPrivKey() + "\n");
		
		//now do the friends
		fout.write("*****************\n");
		Iterator i = friends.values().iterator();
		while (i.hasNext()) {
			Identity cur = (Identity)i.next();
			fout.write(cur.getName() + "\n");
			fout.write(cur.getKeyAddress() + "\n");
			fout.write(cur.getKey() + "\n");
		}
		fout.write("*****************\n");
		i = enemies.values().iterator();
		while (i.hasNext()) {
			Identity cur = (Identity)i.next();
			fout.write(cur.getName() + "\n");
			fout.write(cur.getKeyAddress() + "\n");
			fout.write(cur.getKey() + "\n");
		}
		fout.write("*****************\n");
		fout.close();
            }catch(IOException e){System.out.println("couldn't save buddy list"); System.out.println(e.toString());}
            saveOnExit();
            FileAccess.cleanKeypool(keypool);
        }
        };
        Runtime.getRuntime().addShutdownHook(saver);

        cleaner = new TimerTask() {
        public void run() {
            int i =0;

            if (i==10 && frostSettings.getBoolValue("doCleanUp")) {
                 i=0;
             System.out.println("discarding old files");
            fileCleaner.doCleanup();
             }
            System.out.println("freeing memory");
            System.gc();
            i++;

        }
        };

     timer2.schedule(cleaner,10*60*1000,10*60*1000);
    }
    catch(Exception e) {
        e.printStackTrace();
    }
    }

    /**
     * Configures a button to be a default icon button
     * @param button The new icon button
     * @param toolTipText Is displayed when the mousepointer is some seconds over a button
     * @param rolloverIcon Displayed when mouse is over button
     */
    protected void configureButton(JButton button, String toolTipText, String rolloverIcon)
    {
        button.setToolTipText(LangRes.getString(toolTipText));
        button.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
    /**
     * Configures a CheckBox to be a default icon CheckBox
     * @param checkBox The new icon CheckBox
     * @param toolTipText Is displayed when the mousepointer is some seconds over the CheckBox
     * @param rolloverIcon Displayed when mouse is over the CheckBox
     * @param selectedIcon Displayed when CheckBox is checked
     * @param rolloverSelectedIcon Displayed when mouse is over the selected CheckBox
     */
    public void configureCheckBox(JCheckBox checkBox, String toolTipText, String rolloverIcon,
                                    String selectedIcon, String rolloverSelectedIcon)
    {
        
	checkBox.setToolTipText(LangRes.getString(toolTipText));
        checkBox.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
        checkBox.setSelectedIcon(new ImageIcon(frame1.class.getResource(selectedIcon)));
        checkBox.setRolloverSelectedIcon(new ImageIcon(frame1.class.getResource(rolloverSelectedIcon)));
        checkBox.setMargin(new Insets(0, 0, 0, 0));
        checkBox.setFocusPainted(false);
    }

    /**Component initialization*/
    private void jbInit() throws Exception  {
    
setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
    this.setSize(new Dimension(790, 580));
    this.setResizable(true);

    this.setTitle("Frost");

    contentPanel = (JPanel) this.getContentPane();
    contentPanel.setLayout(new BorderLayout());

    configureButton(tofNewMessageButton, "New message", "/data/newmessage_rollover.gif");
    configureButton(tofUpdateButton, "Update", "/data/update_rollover.gif");
    configureButton(tofReplyButton, "Reply", "/data/reply_rollover.gif");
    configureButton(downloadAttachmentsButton, "Download attachment(s)", "/data/attachment_rollover.gif");
    configureButton(downloadBoardsButton, "Add Board(s)", "/data/attachmentBoard_rollover.gif");
    configureButton(cutBoardButton, "Cut board", "/data/cut_rollover.gif");
    configureButton(copyBoardButton, "Copy board", "/data/copy_rollover.gif");
    configureButton(pasteBoardButton, "Paste board", "/data/paste_rollover.gif");
    configureButton(removeBoardButton, "Remove board", "/data/remove_rollover.gif");
    configureButton(newBoardButton, "New board", "/data/newboard_rollover.gif");
    configureButton(renameBoardButton, "Rename board", "/data/rename_rollover.gif");
    configureButton(configBoardButton, "Configure board", "/data/configure_rollover.gif");
    configureButton(saveMessageButton, "Save message", "/data/save_rollover.gif");
    configureButton(searchButton, "Search", "/data/search_rollover.gif");
    configureButton(searchDownloadButton, "Download selected keys", "/data/save_rollover.gif");
    configureButton(uploadAddFilesButton, "Browse...", "/data/browse_rollover.gif");
    configureButton(boardInfoButton, "Board Information Window", "/data/info_rollover.gif");
    configureButton(systemTrayButton, "Minimize to System Tray", "/data/tray_rollover.gif");
    configureButton(trustButton, "Trust", "/data/trust_rollover.gif");
    configureButton(notTrustButton, "Do not trust", "/data/nottrust_rollover.gif");
    configureCheckBox(downloadActivateCheckBox,
                 "Activate downloading",
                 "/data/down_rollover.gif",
                 "/data/down_selected.gif",
                 "/data/down_selected_rollover.gif");

    /*configureCheckBox(searchAllBoardsCheckBox,
                 "Search all boards",
                 "data/allboards_rollover.gif",
                 "data/allboards_selected.gif",
                 "data/allboards_selected_rollover.gif");*/

    getTofTree().setRootVisible(true);
    getTofTree().setEditable(true);
    TofTreeCellRenderer toftreecr = new TofTreeCellRenderer();
    // TODO: update this if maxMessageDisplay changes!
    //toftreecr.setDaysToRead(frostSettings.getIntValue("maxMessageDisplay"));
    getTofTree().setCellRenderer(toftreecr);
    getTofTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    tofSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageTableScrollPane, boardSplitPane);
    tofSplitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, tabbedPane);

    tofSplitPane.setDividerSize(10);
    tofSplitPane.setDividerLocation(160);
    tofSplitPane.setResizeWeight(0.5d);
    tofSplitPane.setMinimumSize(new Dimension(50, 20));

    tofSplitPane1 = tofSplitPane;
    tofSplitPane1.setBorder(null);
    tofSplitPane1.setMinimumSize(new Dimension(50, 20));
    tofSplitPane1.setDividerLocation(160); // Horizontal MessageTable/Message divider
    tofSplitPane1.setResizeWeight(0.5);
    tofAttachmentPanel.setMinimumSize(new Dimension(50, 20));
    boardAttachmentPanel.setMinimumSize(new Dimension(50, 20));

    tofSplitPane2.setDividerLocation(160); // Vertical Board Tree / MessagePane Divider

    tofSplitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tofSplitPane1, tofAttachmentPanel);
    //tofSplitPane4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tofSplitPane3, boardAttachmentPanel);
    tofSplitPane3.setResizeWeight(1);
    //tofSplitPane4.setResizeWeight(1);

    tofSplitPane3.setDividerLocation(390);
    tofSplitPane3.setDividerSize(1);
    tofSplitPane3.setMinimumSize(new Dimension(50,20));

    //tofSplitPane4.setDividerLocation(390);
    //tofSplitPane4.setDividerSize(1);
    //tofSplitPane4.setMinimumSize(new Dimension(50,20));

    tofTextArea.setText(LangRes.getString("Select a message to view its content."));
    tofTextArea.setEditable(false);
    tofTextArea.setLineWrap(true);
    tofTextArea.setWrapStyleWord(true);

    // Tables everywhere
    downloadTableColumnNames.add(LangRes.getString("Filename"));
    downloadTableColumnNames.add(LangRes.getString("Size"));
    downloadTableColumnNames.add(LangRes.getString("Age"));
    downloadTableColumnNames.add(LangRes.getString("State"));
    downloadTableColumnNames.add(LangRes.getString("HTL"));
    downloadTableColumnNames.add(LangRes.getString("Source"));
    downloadTableColumnNames.add(LangRes.getString("Key"));
    downloadTableModel.setDataVector(new Vector(), downloadTableColumnNames);

    uploadTableColumnNames.add(LangRes.getString("Filename"));
    uploadTableColumnNames.add(LangRes.getString("Size"));
    uploadTableColumnNames.add(LangRes.getString("Last upload"));
    uploadTableColumnNames.add(LangRes.getString("Path"));
    uploadTableColumnNames.add(LangRes.getString("Destination"));
    uploadTableColumnNames.add(LangRes.getString("Key"));
    uploadTableModel.setDataVector(new Vector(), uploadTableColumnNames);
    attachmentTableColumnNames.add(LangRes.getString("Filename"));
    attachmentTableColumnNames.add(LangRes.getString("Key"));
    attachmentTableModel.setDataVector(new Vector(), attachmentTableColumnNames);
    boardTableColumnNames.add("Board Name");
    boardTableColumnNames.add("Public Key");
    boardTableColumnNames.add("Private Key");
    boardTableModel.setDataVector(new Vector(), boardTableColumnNames);

    searchTableColumnNames.add(LangRes.getString("Filename"));
    searchTableColumnNames.add(LangRes.getString("Size"));
    searchTableColumnNames.add(LangRes.getString("Age"));
    searchTableColumnNames.add(LangRes.getString("Key"));
    searchTableColumnNames.add(LangRes.getString("Board"));
    searchTableModel.setDataVector(new Vector(), searchTableColumnNames);
    searchTable.setSelectionModel(searchTableListModel);

    messageTableColumnNames.add(LangRes.getString("Index"));
    messageTableColumnNames.add(LangRes.getString("From"));
    messageTableColumnNames.add(LangRes.getString("Subject"));
    messageTableColumnNames.add("Sig");
    messageTableColumnNames.add(LangRes.getString("Date"));
    messageTableModel.setDataVector(new Vector(), messageTableColumnNames);
    messageTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
    messageTableListModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
    messageTable.setSelectionModel(messageTableListModel);

    // Set the column widths of the tables
    {int[] widths = {30, 150, 250, 50, 150};
    //{int[] widths = {30, 150, 250, 150};
    TableFun.setColumnWidth(messageTable, widths);}

    {int[] widths = {250, 80, 80, 80, 80};
    TableFun.setColumnWidth(searchTable, widths);}

    {int[] widths = {250, 90, 90, 80, 40, 50, 60};
    TableFun.setColumnWidth(downloadTable, widths);}

    {int[] widths = {250, 80, 80, 80, 80, 80};
    TableFun.setColumnWidth(uploadTable, widths);}

    // Add Popup listeners
    MouseListener popupListener = new PopupListener();
    downloadTable.addMouseListener(popupListener);
    searchTable.addMouseListener(popupListener);
    uploadTable.addMouseListener(popupListener);
    tofTextArea.addMouseListener(popupListener);
    getTofTree().addMouseListener(popupListener);
    attachmentTable.addMouseListener(popupListener);
    boardTable.addMouseListener(popupListener);

    // Add IdleStoppers to some components
    MouseListener idleStopper = new IdleStopper();
    tofSplitPane1.addMouseListener(idleStopper);
    tofSplitPane2.addMouseListener(idleStopper);
    tofSplitPane3.addMouseListener(idleStopper);
    //tofSplitPane4.addMouseListener(idleStopper);
    downloadTable.addMouseListener(idleStopper);
    searchTable.addMouseListener(idleStopper);
    messageTable.addMouseListener(idleStopper);
    uploadTable.addMouseListener(idleStopper);
   getTofTree().addMouseListener(idleStopper);
    tofTextArea.addMouseListener(idleStopper);
    contentPanel.addMouseListener(idleStopper);
    buttonPanel.addMouseListener(idleStopper);
    statusPanel.addMouseListener(idleStopper);

    // tofTree selection listener
    tofTree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            tofTree_actionPerformed(e);
            int i[] = getTofTree().getSelectionRows();

            if (i != null) {
            if (i.length > 0)
                frostSettings.setValue("tofTreeSelectedRow", i[0]);
            }

            TreePath selectedTreePath = e.getNewLeadSelectionPath();
            if (selectedTreePath == null)
                getTofTree().setSelectedTof(null);
            else
                getTofTree().setSelectedTof((DefaultMutableTreeNode)selectedTreePath.getLastPathComponent());

        }
        });

    //tofTree / KeyEvent
    getTofTree().addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            tofTree_keyPressed(e);
        }
        public void keyReleased(KeyEvent e) {}
        });

    //Downloads / KeyEvent
    downloadTable.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            downloadTable_keyPressed(e);
        }
        public void keyReleased(KeyEvent e) {}
        });

    // uploadTable / KeyEvent
    uploadTable.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_DELETE && !uploadTable.isEditing())
            TableFun.removeSelectedRows(uploadTable);
        }
        public void keyReleased(KeyEvent e) {}
        });

    // TOF Table / valueChanged
    messageTableListModel.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e){
            messageTableListModel_valueChanged(e);
        }
        });

    timer = new javax.swing.Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            timer_actionPerformed();
        }
        });

    timer2 = new java.util.Timer(true);
    timer2.schedule(new checkForSpam(), 0, frostSettings.getIntValue("sampleInterval")*60*60*1000);

    //TODO:*** remove, its debug only
    TimerTask printer = new TimerTask() {
        public void run() {
            Iterator iter = frame1.activeTofThreads.iterator();
            while(iter.hasNext())
                System.out.println((String)iter.next());
        }
    };
    timer2.schedule(printer,0, 60*1000);
    //END of remove

    searchTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            searchTextField_actionPerformed(e);
        }
        });

    downloadTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            downloadTextField_actionPerformed(e);
        }
        });

    searchDownloadButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            SearchTableFun.downloadSelectedKeys(frostSettings.getIntValue("htl"), searchTable, downloadTable);
        }
        });

    // Update selected board
    tofUpdateButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (doUpdate(lastUsedBoard)) {
            updateBoard(lastUsedBoard);
            }
        }
        });

    searchButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            searchButton_actionPerformed(e);
        }
        });

    boardInfoButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            tofDisplayBoardInfoMenuItem_actionPerformed(e);
        }
        });

    uploadAddFilesButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            uploadAddFilesButton_actionPerformed(e);
        }
        });

    tofNewMessageButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            tofNewMessageButton_actionPerformed(e);
        }
        });

    downloadAttachmentsButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            downloadAttachments();
        }
        });

     downloadBoardsButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            downloadBoards();
        }
        });

    tofReplyButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            tofReplyButton_actionPerformed(e);
        }
        });

     trustButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            trustButton.setEnabled(false);
            notTrustButton.setEnabled(false);
            if (selectedMessage!=null) {
                if (enemies.containsKey(selectedMessage.getFrom())){
                    if (JOptionPane.showConfirmDialog(getInstance(),
                    "are you sure you want to grant trust to user " +
                    selectedMessage.getFrom().substring(0,selectedMessage.getFrom().indexOf("@")) +
                    " ? \n If you choose yes, future messages from this user will be marked GOOD",
                    "re-grant trust",
                    JOptionPane.YES_NO_OPTION) ==0) {
                        Identity x = enemies.Get(selectedMessage.getFrom());
                        enemies.remove(selectedMessage.getFrom());
                        friends.Add(x);
                    }
            }else {
            Truster truster = new Truster(true);
            truster.start();
            }}
            }
        });

    notTrustButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            trustButton.setEnabled(false);
            notTrustButton.setEnabled(false);
            if (selectedMessage!=null) {
                if (friends.containsKey(selectedMessage.getFrom())){
                    if (JOptionPane.showConfirmDialog(getInstance(),
                    "are you sure you want to revoke trust to user " +
                    selectedMessage.getFrom().substring(0,selectedMessage.getFrom().indexOf("@")) +
                    " ? \n If you choose yes, future messages from this user will be marked BAD",
                    "revoke trust",
                    JOptionPane.YES_NO_OPTION) ==0) {
                        Identity x = friends.Get(selectedMessage.getFrom());
                        friends.remove(selectedMessage.getFrom());
                        enemies.Add(x);
                    }
            }else {
            Truster truster = new Truster(false);
            truster.start();
            }}
            }
        });

    newBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            addNodeToTree();
        }
        });
    renameBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            renameSelectedNode();
        }
        });
    removeBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            removeSelectedNode();
        }
        });
    cutBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            cutSelectedNode();
        }
        });
    copyBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            copyToClipboard();
        }
        });
    pasteBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            pasteFromClipboard();
        }
        });
    configBoardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            tofConfigureBoardMenuItem_actionPerformed(e);
        }
        });

    systemTrayButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {

        // Hide the Frost window
        try {
            Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayHide.exe");
        }catch(IOException _IoExc) { }

        }
        });




    saveMessageButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            FileAccess.saveDialog(getInstance(), tofTextArea.getText(), frostSettings.getValue("lastUsedDirectory"), LangRes.getString("Save message to disk"));
        }
        });

    fileExitMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            fileExitMenuItem_actionPerformed(e);
        }
        });

    optionsPreferencesMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            optionsPreferencesMenuItem_actionPerformed(e);
        }
        });

    tofIncreaseFontSizeMenuItem.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e)
        {
            // make the font size in the TOF text area one point bigger
            Font f = tofTextArea.getFont ();
            frostSettings.setValue("tofFontSize",f.getSize () + 1.0f);
            f = f.deriveFont (frostSettings.getFloatValue("tofFontSize"));
            tofTextArea.setFont (f);
        }});

    tofDecreaseFontSizeMenuItem.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e)
        {
            // make the font size in the TOF text area one point smaller
            Font f = tofTextArea.getFont ();
            frostSettings.setValue("tofFontSize",f.getSize () - 1.0f);
            f = f.deriveFont (frostSettings.getFloatValue("tofFontSize"));
            tofTextArea.setFont (f);
        }});

    tofConfigureBoardMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            tofConfigureBoardMenuItem_actionPerformed(e);
        }
        });

    tofDisplayBoardInfoMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            tofDisplayBoardInfoMenuItem_actionPerformed(e);
        }
        });

    pluginBrowserMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            BrowserFrame browser = new BrowserFrame(true);
            browser.show();
        }
        });

    helpHelpMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            HelpFrame dlg = new HelpFrame(getInstance());
            dlg.show();
        }
        });

    helpAboutMenuItem.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            helpAboutMenuItem_actionPerformed(e);
        }
        });

    //------------------------------------------------------------------------
    // PopupMenu Listener
    //------------------------------------------------------------------------

    // Upload / Move selected files up
    uploadPopupMoveSelectedFilesUp.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.moveSelectedEntriesUp(uploadTable);
        }
        });

    // Upload / Move selected files down
    uploadPopupMoveSelectedFilesDown.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.moveSelectedEntriesDown(uploadTable);
        }
        });

    // Upload / Remove selected files
    uploadPopupRemoveSelectedFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.removeSelectedRows(uploadTable);
        }
        });

    // Upload / Remove all files
    uploadPopupRemoveAllFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.removeAllRows(uploadTable);
        }
        });

    // Upload / Reload selected files
    uploadPopupReloadSelectedFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            DefaultTableModel tableModel = (DefaultTableModel)uploadTable.getModel();
            synchronized (uploadTable){
            try{
            int[] selectedRows = uploadTable.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++){
                String state = (String)tableModel.getValueAt(selectedRows[i], 2);
                // Since it is difficult to identify the states where we are allowed to
                // start an upload we decide based on the states in which we are not allowed
                if (!state.equals(LangRes.getString("Uploading")) && (state.indexOf("Kb") == -1)){
                tableModel.setValueAt(LangRes.getString("Requested"), selectedRows[i], 2);
                }
            }
            }catch (Exception ex) {System.out.println("reload files NOT GOOD " +ex.toString());}
            }
        }
        });


    // Upload / Reload all files
    uploadPopupReloadAllFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            // Needs to be fixed for proper synchronization and locked against restart
            synchronized(uploadTable) {
            try{
            uploadTable.selectAll();
            TableFun.setSelectedRowsColumnValue(uploadTable, 2, LangRes.getString("Requested"));
            }catch (Exception ex) {System.out.println("reload files NOT GOOD " +ex.toString());}
            }
        }
        });

    // Upload / Set Prefix for selected files
    uploadPopupSetPrefixForSelectedFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            UploadTableFun.setPrefixForSelectedFiles(uploadTable);
        }
        });

    // Upload / Set Prefix for all files
    uploadPopupSetPrefixForAllFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            uploadTable.selectAll();
            UploadTableFun.setPrefixForSelectedFiles(uploadTable);
        }
        });

    // Upload / Restore default filenames for selected files
    uploadPopupRestoreDefaultFilenamesForSelectedFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            UploadTableFun.restoreDefaultFilenames(uploadTable);
        }
        });

    // Upload / Restore default filenames for all files
    uploadPopupRestoreDefaultFilenamesForAllFiles.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            uploadTable.selectAll();
            UploadTableFun.restoreDefaultFilenames(uploadTable);
        }
        });

    // Upload / Restore default filenames for all files
    uploadPopupAddFilesToBoard.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            UploadTableFun.addFilesToBoard(uploadTable);
        }
        });

    // Search / Download selected keys
    searchPopupDownloadSelectedKeys.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            SearchTableFun.downloadSelectedKeys(frostSettings.getIntValue("htl"), searchTable, downloadTable);
        }
        });

    // Search / Download all keys
    searchPopupDownloadAllKeys.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            searchTable.selectAll();
            SearchTableFun.downloadSelectedKeys(frostSettings.getIntValue("htl"), searchTable, downloadTable);
        }
        });

    // Search / Copy attachment to clipboard
    searchPopupCopyAttachment.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String srcData=SearchTableFun.getSelectedAttachmentsString(searchTable);
            Clipboard clipboard = getToolkit().getSystemClipboard();
            StringSelection contents = new StringSelection(srcData);
            clipboard.setContents(contents, frame1.this);
        }
        });

    // Downloads / Remove selected downloads
    downloadPopupRemoveSelectedDownloads.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            DownloadTableFun.removeSelectedChunks(downloadTable);
            TableFun.removeSelectedRows(downloadTable);
        }
        });

    // Downloads / Remove all downloads
    downloadPopupRemoveAllDownloads.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            downloadTable.selectAll();
            DownloadTableFun.removeSelectedChunks(downloadTable);
            TableFun.removeAllRows(downloadTable);
        }
        });
    // Downloads / Move selected downloads up
    downloadPopupMoveUp.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.moveSelectedEntriesUp(downloadTable);
        }
        });

    // Downloads / Move selected downloads down
    downloadPopupMoveDown.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.moveSelectedEntriesDown(downloadTable);
        }
        });

    // Downloads / Reset HTL values
    downloadPopupResetHtlValues.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            TableFun.setSelectedRowsColumnValue(downloadTable, 4, frostSettings.getValue("htl"));
            TableFun.setSelectedRowsColumnValue(downloadTable, 3, LangRes.getString("Waiting"));
        }
        });

    // TOF text / Save message to disk
    tofTextPopupSaveMessage.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            FileAccess.saveDialog(getInstance(), tofTextArea.getText(), frostSettings.getValue("lastUsedDirectory"), LangRes.getString("Save message to disk"));
        }
        });

    // TOF text / Save attachments to disk
    tofTextPopupSaveAttachments.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            downloadAttachments();
        }
        });

     tofTextPopupSaveAttachment.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            downloadAttachments();
        }
        });

     //TOF text / Save Boards

     tofTextPopupSaveBoards.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            downloadBoards();
        }
        });
     //he he heee
     tofTextPopupSaveBoard.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            downloadBoards();
        }
        });

    // TOF tree / Add node
    tofTreePopupAddNode.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            addNodeToTree();
        }
        });

     // TOF tree / Refresh node
    tofTreePopupRefresh.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)getTofTree().getLastSelectedPathComponent();
            refreshNode(node);
        }
        });

    // TOF tree / Remove node
    tofTreePopupRemoveNode.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            removeSelectedNode();
        }
        });

    // TOF tree / Cut node
    tofTreePopupCutNode.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            cutSelectedNode();
        }
        });

    // TOF tree / Copy node
    tofTreePopupCopyNode.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            copyToClipboard();
        }
        });

    // TOF tree / Paste node
    tofTreePopupPasteNode.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            pasteFromClipboard();
        }
        });

    // TOF tree / Configure selected board
    tofTreePopupConfigureBoard.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            tofConfigureBoardMenuItem_actionPerformed(e);
        }
        });

    //------------------------------------------------------------------------
    // Menu
    //------------------------------------------------------------------------

    // File Menu
    fileMenu.add(fileExitMenuItem);

    // News Menu
    tofMenu.add(tofAutomaticUpdateMenuItem);
    tofMenu.addSeparator();
    tofMenu.add(tofIncreaseFontSizeMenuItem);
    tofMenu.add(tofDecreaseFontSizeMenuItem);
    tofMenu.addSeparator();
    tofMenu.add(tofConfigureBoardMenuItem);
    tofMenu.addSeparator();
    tofMenu.add(tofDisplayBoardInfoMenuItem);

    // Options Menu
    optionsMenu.add(optionsPreferencesMenuItem);

    // Plugin Menu
    pluginMenu.add(pluginBrowserMenuItem);

    // Help Menu
    helpMenu.add(helpHelpMenuItem);
    helpMenu.add(helpAboutMenuItem);

    menuBar.add(fileMenu);
    menuBar.add(tofMenu);
    menuBar.add(optionsMenu);
    menuBar.add(pluginMenu);
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    // Upload popup
    uploadPopupMenu.add(uploadPopupRemoveSelectedFiles);
    uploadPopupMenu.add(uploadPopupRemoveAllFiles);
    uploadPopupMenu.addSeparator();
    uploadPopupMenu.add(uploadPopupMoveSelectedFilesUp);
    uploadPopupMenu.add(uploadPopupMoveSelectedFilesDown);
    uploadPopupMenu.addSeparator();
    uploadPopupMenu.add(uploadPopupReloadSelectedFiles);
    uploadPopupMenu.add(uploadPopupReloadAllFiles);
    uploadPopupMenu.addSeparator();
    uploadPopupMenu.add(uploadPopupSetPrefixForSelectedFiles);
    uploadPopupMenu.add(uploadPopupSetPrefixForAllFiles);
    uploadPopupMenu.addSeparator();
    uploadPopupMenu.add(uploadPopupRestoreDefaultFilenamesForSelectedFiles);
    uploadPopupMenu.add(uploadPopupRestoreDefaultFilenamesForAllFiles);
    uploadPopupMenu.addSeparator();
    uploadPopupMenu.add(uploadPopupChangeDestinationBoard);
    uploadPopupMenu.add(uploadPopupAddFilesToBoard);
    uploadPopupMenu.addSeparator();
    uploadPopupMenu.add(uploadPopupCancel);

    // Search popup
    searchPopupMenu.add(searchPopupDownloadSelectedKeys);
    searchPopupMenu.addSeparator();
    searchPopupMenu.add(searchPopupDownloadAllKeys);
    searchPopupMenu.addSeparator();
    searchPopupMenu.add(searchPopupCopyAttachment);
    searchPopupMenu.addSeparator();
    searchPopupMenu.add(searchPopupCancel);

    // Download popup
    downloadPopupMenu.add(downloadPopupRemoveSelectedDownloads);
    downloadPopupMenu.add(downloadPopupRemoveAllDownloads);
    downloadPopupMenu.addSeparator();
    downloadPopupMenu.add(downloadPopupResetHtlValues);
    downloadPopupMenu.addSeparator();
    downloadPopupMenu.add(downloadPopupMoveUp);
    downloadPopupMenu.add(downloadPopupMoveDown);
    downloadPopupMenu.addSeparator();
    downloadPopupMenu.add(downloadPopupCancel);

    // TOF text popup
    tofTextPopupMenu.add(tofTextPopupSaveMessage);
    tofTextPopupMenu.addSeparator();
    tofTextPopupMenu.add(tofTextPopupSaveAttachments);
    tofTextPopupMenu.add(tofTextPopupSaveAttachment);
    tofTextPopupMenu.addSeparator();
    tofTextPopupMenu.add(tofTextPopupSaveBoards);
    tofTextPopupMenu.add(tofTextPopupSaveBoard);
    tofTextPopupMenu.addSeparator();
    tofTextPopupMenu.add(tofTextPopupCancel);

    // TOF tree popup
    tofTreePopupMenu.add(tofTreePopupRefresh);
    tofTreePopupMenu.addSeparator();
    tofTreePopupMenu.add(tofTreePopupAddNode);
    tofTreePopupMenu.add(tofTreePopupRemoveNode);
    tofTreePopupMenu.addSeparator();
    tofTreePopupMenu.add(tofTreePopupCopyNode);
    tofTreePopupMenu.add(tofTreePopupCutNode);
    tofTreePopupMenu.add(tofTreePopupPasteNode);
    tofTreePopupMenu.addSeparator();
    tofTreePopupMenu.add(tofTreePopupConfigureBoard);
    tofTreePopupMenu.addSeparator();
    tofTreePopupMenu.add(tofTreePopupCancel);

    //------------------------------------------------------------------------

    statusPanel.add(statusLabel, BorderLayout.CENTER); // Statusbar
    statusPanel.add(statusMessageLabel, BorderLayout.EAST); // Statusbar / new Message

    buttonPanelConstr.anchor = buttonPanelConstr.WEST;
    buttonPanelConstr.gridx = buttonPanelConstr.RELATIVE;
    buttonPanelConstr.insets = new Insets(0,2,0,2);
    buttonPanel.add(newBoardButton,buttonPanelConstr);
    buttonPanel.add(configBoardButton,buttonPanelConstr);
    buttonPanel.add(renameBoardButton,buttonPanelConstr);
    buttonPanel.add(removeBoardButton,buttonPanelConstr);
    buttonPanel.add(cutBoardButton,buttonPanelConstr);
    buttonPanel.add(copyBoardButton,buttonPanelConstr);
    buttonPanel.add(pasteBoardButton,buttonPanelConstr);
    buttonPanel.add(boardInfoButton,buttonPanelConstr);

    // The System Tray Icon does only work in Windows machines.
    // It uses the Visual Basic files (compiled ones) in the
    // data directory.
    if ((System.getProperty("os.name").startsWith("Windows")))
        buttonPanel.add(systemTrayButton,buttonPanelConstr);

    buttonPanelConstr.gridwidth = buttonPanelConstr.REMAINDER;
    buttonPanelConstr.anchor = buttonPanelConstr.EAST;
    buttonPanelConstr.weightx = 1;
    buttonPanel.add(timeLabel,buttonPanelConstr);


    downloadMainPanel.add(downloadTopPanel, BorderLayout.NORTH); // Download/Buttons
    downloadMainPanel.add(downloadTableScrollPane, BorderLayout.CENTER); //Downloadlist

    uploadMainPanel.add(uploadTopPanel, BorderLayout.NORTH);
    uploadMainPanel.add(uploadTableScrollPane, BorderLayout.CENTER);
    uploadTopPanel.add(uploadAddFilesButton);

    downloadTopPanel.add(downloadTextField);//Download/Quickload
    downloadTopPanel.add(downloadActivateCheckBox);//Download/Start transfer

    tofTopPanel = new JPanel();
    BoxLayout dummyLayout = new BoxLayout(tofTopPanel, BoxLayout.X_AXIS);
    tofTopPanel.setLayout(dummyLayout);

    tofMainPanel.add(tofTopPanel, BorderLayout.NORTH); // TOF/Buttons
    tofMainPanel.add(tofSplitPane2, BorderLayout.CENTER); // TOF/Text

    tofTopPanel.add(saveMessageButton); // TOF/ Save Message
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(tofNewMessageButton); // TOF/ New Message
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(tofReplyButton); // TOF/ Reply
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(tofUpdateButton); // TOF/ Update
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(downloadAttachmentsButton); // TOF/ Download Attachments
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(downloadBoardsButton); // TOF/ Download Boards
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(trustButton); //TOF /trust
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(notTrustButton); //TOF /do not trust
    tofTopPanel.add( Box.createHorizontalGlue() );
    JLabel dummyLabel = new JLabel(allMessagesCountPrefix + "00000");
    dummyLabel.doLayout();
    Dimension msgLabelSize = dummyLabel.getPreferredSize();
    allMessagesCountLabel.setPreferredSize(msgLabelSize);
    allMessagesCountLabel.setMinimumSize(msgLabelSize);
    newMessagesCountLabel.setPreferredSize(msgLabelSize);
    newMessagesCountLabel.setMinimumSize(msgLabelSize);
    tofTopPanel.add(allMessagesCountLabel);
    tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    tofTopPanel.add(newMessagesCountLabel);

    messageTablePanel.add(tofTopPanel, BorderLayout.NORTH);
    messageTablePanel.add(tofSplitPane, BorderLayout.CENTER);

    searchTopPanel = new JPanel();
    dummyLayout = new BoxLayout( searchTopPanel, BoxLayout.X_AXIS );
    searchTopPanel.setLayout( dummyLayout );

    searchTopPanel.add(searchTextField); // Search / text
    searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    searchTopPanel.add(searchComboBox);
    searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    searchTopPanel.add(searchButton); // Search / Search button
    searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    searchTopPanel.add(searchDownloadButton); // Search / Download selected files
    searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
    searchTopPanel.add(searchAllBoardsCheckBox);
    searchTopPanel.add( Box.createRigidArea(new Dimension(80,0)));
    searchTopPanel.add( Box.createHorizontalGlue() );

    searchResultsCountPrefix = LangRes.getString("   Results: ");
    dummyLabel = new JLabel(searchResultsCountPrefix + "00000");
    dummyLabel.doLayout();
    msgLabelSize = dummyLabel.getPreferredSize();
    searchResultsCountLabel.setPreferredSize(msgLabelSize);
    searchResultsCountLabel.setMinimumSize(msgLabelSize);
    searchResultsCountLabel.setText(searchResultsCountPrefix+"0");
    searchTopPanel.add(searchResultsCountLabel);

    searchMainPanel.add(searchTopPanel, BorderLayout.NORTH); // Search / Buttons
    searchMainPanel.add(searchTableScrollPane, BorderLayout.CENTER); // Search / Results

    //add a tab for buddies perhaps?
    tabbedPane.add(LangRes.getString("News"), messageTablePanel);
    tabbedPane.add(LangRes.getString("Search"), searchMainPanel);
    tabbedPane.add(LangRes.getString("Downloads"), downloadMainPanel);
    tabbedPane.add(LangRes.getString("Uploads"), uploadMainPanel);

    contentPanel.add(buttonPanel, BorderLayout.NORTH);
    contentPanel.add(tofMainPanel, BorderLayout.CENTER);
    contentPanel.add(statusPanel, BorderLayout.SOUTH); // Statusbar

    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

    newMessage[0] = new ImageIcon(frame1.class.getResource("/data/messagebright.gif"));
    newMessage[1] = new ImageIcon(frame1.class.getResource("/data/messagedark.gif"));
    statusMessageLabel.setIcon(newMessage[1]);
    tofReplyButton.setEnabled(false);
    downloadAttachmentsButton.setEnabled(false);
    downloadBoardsButton.setEnabled(false);
    saveMessageButton.setEnabled(false);
    pasteBoardButton.setEnabled(false);
    searchAllBoardsCheckBox.setSelected(true);
    trustButton.setEnabled(false);
    notTrustButton.setEnabled(false);

    //finally start something maybe time for thread?

    File boardsfile = new File("boards.txt");

    //create a crypt object
    crypto = new FrostCrypt();
    //load the identities

    File identities = new File("identities");
    //File contacts = new File("contacts");
    System.out.println("trying to create/load ids");
    try {
        if (identities.createNewFile()) {//create new identities

            try {
                String nick = null;
                do{
                    nick = JOptionPane.showInputDialog("Choose an identity name, it doesn't have to be unique\n");
                }while(nick.compareTo("") == 0);
                mySelf = new LocalIdentity(nick);
        //JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));

            }
            catch(Exception e) {System.out.println("couldn't create new identitiy");
            System.out.println(e.toString());}
            friends = new BuddyList();
            if (friends.Add(frame1.getMyId())) System.out.println("added myself to list");
            enemies = new BuddyList();
        } else try {
		BufferedReader fin = new BufferedReader(new FileReader(identities));
		String name = fin.readLine();
		String address = fin.readLine();
		String keys[] = new String[2];
		keys[1] = fin.readLine();
		keys[0] = fin.readLine();
		mySelf = new LocalIdentity(name, keys, address);
		System.out.println("loaded myself with name " + mySelf.getName());
            	System.out.println("and public key" + mySelf.getKey());
	    
		//take out the ****
		fin.readLine();
		
		//process the friends
		System.out.println("loading friends");
		friends = new BuddyList();
	    	boolean stop = false;
	    	String key;
	    	while (!stop) {
			name = fin.readLine();
			if (name.startsWith("***")) break;
			address = fin.readLine();
			key = fin.readLine();
			friends.Add(new Identity(name, address,key));
		}
		System.out.println("loaded " + friends.size() + " friends");
		
		//and the enemies
		enemies = new BuddyList();
		System.out.println("loading enemies");
		while (!stop) {
			name = fin.readLine();
			if (name.startsWith("***")) break;
			address = fin.readLine();
			key = fin.readLine();
			enemies.Add(new Identity(name, address,key));
		}
		System.out.println("loaded " + enemies.size() + " enemies");
	    
        }
        catch(IOException e) {
		System.out.println("IOException :" + e.toString());
		friends = new BuddyList();
		enemies = new BuddyList();
		friends.Add(mySelf);
	}
        catch(Exception e) {System.out.println(e.toString());}


        }
    catch(IOException e) {System.out.println("couldn't create identities file");}

    TimerTask KeyReinserter = new TimerTask() {
        public void run() {
            System.out.println("re-uploading public key");
            FcpInsert.putFile("CHK@",new File("pubkey.txt"),25,false,true);
            System.out.println("finished re-uploading public key");
        }
    };

    timer2.schedule(KeyReinserter,0,60*60*1000);

    //on with other stuff
    getTofTree().loadTree(boardsfile);
    getTofTree().readTreeState(new File("toftree.txt"));
    TOF.initialSearchNewMessages(getTofTree(), frostSettings.getIntValue("maxMessageDisplay"));
    //updateTofTree();

    loadSettings(); //check this!
    Startup.startupCheck();

    FileAccess.cleanKeypool(keypool);

    // Display the tray icon
    try {
        Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTray.exe");
    }catch(IOException _IoExc) { }

    if(getTofTree().getRowCount() > frostSettings.getIntValue("tofTreeSelectedRow"))
       getTofTree().setSelectionRow(frostSettings.getIntValue("tofTreeSelectedRow"));

    // make sure the font size isn't too small to see
    if (frostSettings.getFloatValue("tofFontSize") < 6.0f)
        frostSettings.setValue("tofFontSize",  6.0f);

    tofTextArea.setFont(tofTextArea.getFont ().deriveFont (frostSettings.getFloatValue("tofFontSize")));
    // Load table settings
    /*
    synchronized(initLock) {
        while(!loaded_tables) try{initLock.wait(100);}catch(InterruptedException e){};
    }*/

    DownloadTableFun.load(downloadTable);
    UploadTableFun.load(uploadTable);


    // Start tofTree
    Startup.resendFailedMessages(this);
    timer.start();
    started = true;
    }

    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

    public static void displayWarning(String message) {
    newMessageHeader = " " + message;
    }

    // Add attachments to download table
    public void downloadAttachments() {
    int[] selectedRows = attachmentTable.getSelectedRows();

    // If no rows are selected, add all attachments to download table
    if (selectedRows.length == 0) {
        for (int i = 0; i < attachmentTableModel.getRowCount(); i++) {
        String filename = (String)attachmentTableModel.getValueAt(i, 0);
        String key = (String)attachmentTableModel.getValueAt(i, 1);
        DownloadTableFun.insertDownload(filename,
                        "Unknown",
                        "Unknown",
                        key,
                        frostSettings.getIntValue("htl"),
                        downloadTable,
                        lastUsedBoard);
        }
    }
    else {
        for (int i = 0; i < selectedRows.length; i++) {
        String filename = (String)attachmentTableModel.getValueAt(selectedRows[i],0);
        String key = (String)attachmentTableModel.getValueAt(selectedRows[i],1);
        DownloadTableFun.insertDownload(filename,
                        "Unknown",
                        "Unknown",
                        key,
                        frostSettings.getIntValue("htl"),
                        downloadTable,
                        lastUsedBoard);
        }
    }
    }


    private void downloadBoards() {
        System.out.println("adding boards");
    int[] selectedRows = boardTable.getSelectedRows();

    if (selectedRows.length == 0)
        for (int i = 0; i < boardTableModel.getRowCount(); i++) {
            String name = (String)boardTableModel.getValueAt(i, 0);
        String pubKey = (String)boardTableModel.getValueAt(i, 1);
        String privKey = (String)boardTableModel.getValueAt(i, 2);

        File newBoard = new File("keypool" + File.separatorChar+ name.toLowerCase() + ".key");

        //ask if we already have the board
        if (newBoard.exists())
            if (JOptionPane.showConfirmDialog(this, "you already have a board named " + name + "\n" +
                        "are you sure you want to download this one over it?","board exists",
                        JOptionPane.YES_NO_OPTION) !=0) continue;

        //create the key file
        try{
        newBoard.createNewFile();
        }catch(IOException e){System.out.println(e.toString());}

        //create the content of the file
        String content = new String();
        if (privKey.compareTo("N/A") == 0)
            content = content + "privateKey=\n";
        else content = content + "privateKey="+privKey+"\n";
        content = content + "publicKey="+pubKey+"\n";
        if (privKey.compareTo("N/A") == 0)
            content = content + "state=readAccess";
        else content = content + "state=writeAccess";

        FileAccess.writeFile(content,newBoard);
        addNodeTree(name);
        }
    else
    for (int i = 0; i < selectedRows.length; i++) {
            String name = (String)boardTableModel.getValueAt(selectedRows[i], 0);
        String pubKey = (String)boardTableModel.getValueAt(selectedRows[i], 1);
        String privKey = (String)boardTableModel.getValueAt(selectedRows[i], 2);

        File newBoard = new File("keypool" + File.separatorChar+ name + ".key");

        //ask if we already have the board
        if (newBoard.exists())
            if (JOptionPane.showConfirmDialog(this, "you already have a board named " + name + "\n" +
                        "are you sure you want to download this one over it?","board exists",
                        JOptionPane.YES_NO_OPTION) !=0) continue;

        //create the key file
        try{
        newBoard.createNewFile();
        }catch(IOException e){System.out.println(e.toString());}

        //create the content of the file
        String content = new String();
        if (privKey.compareTo("N/A") == 0)
            content = content + "privateKey=\n";
        else content = content + "privateKey="+privKey+"\n";
        content = content + "publicKey="+pubKey+"\n";
        if (privKey.compareTo("N/A") == 0)
            content = content + "state=readAccess";
        else content = content + "state=writeAccess";

        FileAccess.writeFile(content,newBoard);
        addNodeTree(name);
        }
    }
    // Test if board should be updated
    public boolean doUpdate(String board) {

    // Do not allow root node as board
    if (board.equals("frost_message_system"))
        return false;

    if (board.length()==0)
        return false;

    if (boardStats.containsKey(board))
        return !((BoardStat)boardStats.get(board)).spammed();

    if( isUpdating(board) )
    {
        return false;
    }
    return true;
    }

    public boolean isUpdating(String board)
    {
        int threadCount = activeTofThreads.size();
        for (int i = 0; i < threadCount; i++)
        {
            if (board.equals((String)activeTofThreads.elementAt(i)))
            {
                return true;
            }
        }
        return false;
    }

    /**tof / Update*/
    /**
     * Should only be called if this board is not already updating.
     */
    public void updateBoard(String board)
    {
        resetAccess(board);

//        String[] args = {board, frostSettings.getValue("tofDownloadHtl"), keypool};
        // first download the messages of today
        MessageDownloadThread tofd = new MessageDownloadThread( true,
                                                                board,
                                                                frostSettings.getValue("tofDownloadHtl"),
                                                                keypool,
                                                                frostSettings.getValue("maxMessageDownload"),
                                                                this
                                                              );
        tofd.start();
        synchronized(TOFThreads)
        {
            TOFThreads.add(tofd);
        }
        System.out.println("Default update of " + board);
        // maybe get the files list
        if( !frostSettings.getBoolValue("disableRequests") )
        {
            GetRequestsThread grt = new GetRequestsThread( board,
                                                           frostSettings.getValue("tofDownloadHtl"),
                                                           keypool,
                                                           uploadTable
                                                         );
            grt.start();

            synchronized(GRTThreads)
            {
                GRTThreads.add(grt);
            }
        }

        // finally get the older messages
        MessageDownloadThread backload = new MessageDownloadThread( false,
                                                                board,
                                                                frostSettings.getValue("tofDownloadHtl"),
                                                                keypool,
                                                                frostSettings.getValue("maxMessageDownload"),
                                                                this
                                                              );
        backload.start();
        System.out.println("Backload update of " + board);
    }

    public void updateTofTree() {
    stopTofTreeUpdate = true;
//    TreePath remember = getTofTree().getSelectionPath();
    // fire update for each node
    DefaultTreeModel model = (DefaultTreeModel)getTofTree().getModel();
    Enumeration nodes = ((DefaultMutableTreeNode)getTofTree().getModel().getRoot()).depthFirstEnumeration();
    while( nodes.hasMoreElements() )
    {
        DefaultMutableTreeNode n = (DefaultMutableTreeNode)nodes.nextElement();
        model.nodeChanged( n );
    }
//    getTofTree().setSelectionPath(remember);
    stopTofTreeUpdate = false;
    updateButtons();
    }

    private void updateButtons() {
    String state = SettingsFun.getValue(keypool + lastUsedBoard + ".key", "state");
    if (state.equals("readAccess")) {
        tofNewMessageButton.setEnabled(false);
        uploadAddFilesButton.setEnabled(false);
    }
    else {
        tofNewMessageButton.setEnabled(true);
        uploadAddFilesButton.setEnabled(true);
    }
    }

    private class Truster extends Thread {
        private boolean trust;
    private Identity newFriend;
    private VerifyableMessageObject currentMsg;

    private void recursDir(String dirItem) {
            String list[];
            File file = new File(dirItem);
            if (file.isDirectory() && file.listFiles().length > 0) {
            //System.out.println("\n"+file+":");
            Vector vd = new Vector();
            Vector vf = new Vector();
            list = file.list();
            Arrays.sort(list,String.CASE_INSENSITIVE_ORDER);

            for (int i = 0; i < list.length; i++) {
                    File f = new File(dirItem + File.separatorChar + list[i]);
                    if (f.isDirectory()) vd.add(list[i]); else vf.add(list[i]);
            }
            for (int a=0; a < vf.size(); a++)
                    recursDir (dirItem + File.separatorChar + vf.get(a));
            for (int d=0; d < vd.size(); d++)
                    recursDir (dirItem + File.separatorChar + vd.get(d));
        } else
            processItem(dirItem);
            list=null;

        }
        private void processItem(String dirItem) {
            File f = new File(dirItem);
        VerifyableMessageObject temp;
        if (f.getPath().endsWith(".txt")) {//open file and check it
            temp = new VerifyableMessageObject(f);
            if (temp.getFrom().equals(currentMsg.getFrom()) &&
                temp.getStatus().trim().equals(VerifyableMessageObject.PENDING))
                if (trust) temp.setStatus(VerifyableMessageObject.VERIFIED);
                else temp.setStatus(VerifyableMessageObject.FAILED);




        }
    f=null;
    temp =null;
        }


        public Truster(boolean what) {
        trust=what;
        try{
        currentMsg= selectedMessage.copy();
        }catch(Exception e){System.out.println(e.toString());}
    }

    public void run() {
        System.out.println("starting to update .sigs");
        newFriend = new Identity(currentMsg.getFrom(),currentMsg.getKeyAddress());
        if (trust) friends.Add(newFriend);
        else enemies.Add(newFriend);

        /*timer2.schedule(new TimerTask() {
            public void run() {tofTree_actionPerformed(null);}
            },800);
        timer2.schedule(new TimerTask() {
            public void run() {tofTree_actionPerformed(null);}
            },3500);*/

        recursDir("keypool");
        tofTree_actionPerformed(null);
    }
    }

    private class checkForSpam extends TimerTask
    {
        public void run()
        {
            //get an iterator
            Iterator iter = boardStats.values().iterator();
            BoardStat current;
            //walk through them
            if(frostSettings.getBoolValue("doBoardBackoff"))
            {
                while (iter.hasNext())
                {
                    current = (BoardStat)iter.next();
                    if (current.numberBlocked > frostSettings.getIntValue("spamTreshold"))
                    {
                        //board is spammed
                        System.out.println("#########setting spam status############");
                        current.spam();
                        timer2.schedule(new ClearSpam(current),24*60*60*1000);

                        //now, kill the thread
                        ListIterator threads = TOFThreads.listIterator();
                        Thread doomed;
                        while(threads.hasNext())
                        {
                            doomed = (MessageDownloadThread)threads.next();
                            if(current.getBoard().equals(((MessageDownloadThread)doomed).board) ||
                                ((MessageDownloadThread)doomed).board.equals("_boardlist"))
                            {
                                doomed.stop();//also kills boardlist.  wander why.
                                frame1.activeTofThreads.remove(((MessageDownloadThread)doomed).board);
                                threads.remove();
                            }
                        }
                        //repeat with request threads
                        threads = GRTThreads.listIterator();
                        while(threads.hasNext())
                        {
                            doomed = (GetRequestsThread)threads.next();
                            if (current.getBoard().equals(((GetRequestsThread)doomed).board))
                            {
                                doomed.stop();//TODO kill doomed
                                threads.remove();
                            }
                        }
                        tofDownloadThreads--;
                    }
                    current.resetBlocked();
                }
            }
        }
    }

    private class ClearSpam extends TimerTask
    {
        private BoardStat cleared;
        public ClearSpam(BoardStat which) {cleared = which;}
        public void run() {
        System.out.println("############clearing spam status###########");
        cleared.unspam();
        }
    }


    /**TOF Board selected*/
    public void tofTree_actionPerformed(TreeSelectionEvent e) {
    if (!stopTofTreeUpdate) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getTofTree().getLastSelectedPathComponent();

        if (node != null) {
        if (node.isLeaf()) {
            lastUsedBoard = mixed.makeFilename((String)node.getUserObject());
            saveMessageButton.setEnabled(false);
            configBoardButton.setEnabled(true);

            updateButtons();
            if ((boardStats != null) && boardStats.containsKey(lastUsedBoard))
            System.out.println(lastUsedBoard + " blocked count: " + ((BoardStat)boardStats.get(lastUsedBoard)).numberBlocked);
            tofReplyButton.setEnabled(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);
            tofTextArea.setText("");

            tofAttachmentPanel.removeAll();
            tofAttachmentPanel.repaint(new Rectangle(tofAttachmentPanel.getX(), tofAttachmentPanel.getY(), tofAttachmentPanel.getWidth(), tofAttachmentPanel.getHeight()));
            tofSplitPane3.revalidate();
            //boardAttachmentPanel.removeAll();
            //boardAttachmentPanel.repaint(new Rectangle(boardAttachmentPanel.getX(), boardAttachmentPanel.getY(), boardAttachmentPanel.getWidth(), boardAttachmentPanel.getHeight()));
            //tofSplitPane4.revalidate();
            messageTablePanel.remove(tofSplitPane3);
            //messageTablePanel.remove(tofSplitPane4);
            messageTablePanel.add(tofSplitPane, BorderLayout.CENTER);

            TOF.updateTofTable(lastUsedBoard,
                          keypool,
                          frostSettings.getIntValue("maxMessageDisplay"));

            messageTable.clearSelection();
        }
        else {
            configBoardButton.setEnabled(false);
        }
        }
    }
    }

    public void addNodeToTree() {
    Object nodeNameOb = JOptionPane.showInputDialog ((Component)this,
                             LangRes.getString ("New Node Name"),
                             LangRes.getString ("New Node Name"),
                             JOptionPane.QUESTION_MESSAGE, null, null,
                             LangRes.getString ("New Folder"));

    String nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString ());

    if (nodeName == null || nodeName.length () == 0)
        return;

    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)getTofTree().getLastSelectedPathComponent();
    if (selectedNode != null)
    {
        selectedNode.add(new DefaultMutableTreeNode(nodeName));
    }
    else
    {
        // add to root node
        selectedNode = (DefaultMutableTreeNode)getTofTree().getModel().getRoot();
        selectedNode.add(new DefaultMutableTreeNode(nodeName));
    }
    int insertedIndex[] = { selectedNode.getChildCount()-1 }; // last in list is the newly added
    ((DefaultTreeModel)getTofTree().getModel()).nodesWereInserted( selectedNode, insertedIndex );
    }

    public void addNodeTree(String name) {
    DefaultMutableTreeNode current = (DefaultMutableTreeNode)getTofTree().getLastSelectedPathComponent();
    current = (DefaultMutableTreeNode)current.getRoot();
    current.add(new DefaultMutableTreeNode(name));

    int insertedIndex[] = { current.getChildCount()-1 }; // last in list is the newly added
    ((DefaultTreeModel)getTofTree().getModel()).nodesWereInserted( current, insertedIndex );
    }

    private void refreshNode(DefaultMutableTreeNode node) {

    if (node!=null)
    if (node.isLeaf()) { //TODO: refresh current board
        lastUsedBoard = mixed.makeFilename((String)node.getUserObject());
        if( doUpdate( lastUsedBoard ) )
        {
            updateBoard(lastUsedBoard);
        }
    }
    else {
        Enumeration leafs = node.children();
        while(leafs.hasMoreElements()) refreshNode((DefaultMutableTreeNode)leafs.nextElement());
    }
    }

    public void removeSelectedNode() {
        getTofTree().removeSelectedNode();
    }

    public void renameSelectedNode() {
    getTofTree().startEditingAtPath(getTofTree().getSelectionPath());
    }

    public void pasteFromClipboard()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getTofTree().getLastSelectedPathComponent();
        if( node != null && !clipboard.equals("") )
        {
            DefaultMutableTreeNode actualNode = node;

            Vector lines = new Vector();
            clipboard = clipboard.trim();
            while( clipboard.indexOf("\r\n") != -1 )
            {
                lines.add(clipboard.substring(0, clipboard.indexOf("\r\n")));
                clipboard = clipboard.substring(clipboard.indexOf("\r\n") + 2, clipboard.length());
            }
            for( int i = 0; i < lines.size(); i++ )
            {
                String line = ((String)lines.elementAt(i)).trim();
                String name = line.substring(1, line.length());

                if( line.startsWith("=") )
                {
                    actualNode.add(new DefaultMutableTreeNode(name));
                    int insertedIndex[] = { actualNode.getChildCount()-1 }; // last in list is the newly added
                    ((DefaultTreeModel)getTofTree().getModel()).nodesWereInserted( actualNode, insertedIndex );
                }
                else if( line.startsWith(">") )
                {
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
                    actualNode.add(newNode);
                    int insertedIndex[] = { actualNode.getChildCount()-1 }; // last in list is the newly added
                    ((DefaultTreeModel)getTofTree().getModel()).nodesWereInserted( actualNode, insertedIndex );

                    actualNode = newNode;
                }
                else if( line.startsWith("<") )
                {
                    actualNode = (DefaultMutableTreeNode)actualNode.getParent();
                }
            }
            clipboard = "";
            pasteBoardButton.setEnabled(false);
        }
    }

    public void cutSelectedNode() {
        String cuttedNode = getTofTree().cutSelectedNode();
        if( cuttedNode != null )
        {
            clipboard = cuttedNode;
            pasteBoardButton.setEnabled(true);
        }
    }

    public void copyToClipboard()
    {
        String copiedNode = getTofTree().copySelectedNode();
        if( copiedNode != null )
        {
            clipboard = copiedNode;
            pasteBoardButton.setEnabled(true);
        }
    }

    /**Get keyTyped for tofTree*/
    public void tofTree_keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (!getTofTree().isEditing()) {
        if (key == KeyEvent.VK_DELETE)
        removeSelectedNode();
        if (key == KeyEvent.VK_N)
        addNodeToTree();
        if (key == KeyEvent.VK_X)
        cutSelectedNode();
        if (key == KeyEvent.VK_V)
        pasteFromClipboard();
        if (key == KeyEvent.VK_C)
        copyToClipboard();
    }
    }

    /**Get keyTyped for downloadTable*/
    public void downloadTable_keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (key == KeyEvent.VK_DELETE && !downloadTable.isEditing()) {
        DownloadTableFun.removeSelectedChunks(downloadTable);
        TableFun.removeSelectedRows(downloadTable);
    }
    }

    /**valueChanged messageTable (messageTableListModel / TOF)*/
    public void messageTableListModel_valueChanged(ListSelectionEvent e)
    {
        selectedMessage = TOF.evalSelection(e, messageTable);
        if( selectedMessage != null )
        {
            displayNewMessageIcon(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);

            lastSelectedMessage = selectedMessage.getSubject();
            String state = SettingsFun.getValue(keypool + lastUsedBoard + ".key", "state");
            if( !state.equals("readAccess") )
                tofReplyButton.setEnabled(true);

            if( selectedMessage.getStatus().trim().compareTo(VerifyableMessageObject.PENDING) == 0 )
            {
                trustButton.setEnabled(true);
                notTrustButton.setEnabled(true);
            }
            else if( selectedMessage.getStatus().trim().compareTo(VerifyableMessageObject.VERIFIED) ==0 )
            {
                trustButton.setEnabled(false);
                notTrustButton.setEnabled(true);
            }
            else if( selectedMessage.getStatus().trim().compareTo(VerifyableMessageObject.VERIFIED) ==0
                     && enemies.containsKey(selectedMessage.getFrom()) )
            {
                trustButton.setEnabled(true);
                notTrustButton.setEnabled(false);
            }
            else
            {
                trustButton.setEnabled(false);
                notTrustButton.setEnabled(false);
            }

            String content = selectedMessage.getContent();

            int start = content.indexOf("<attached>");
            int end = content.indexOf("</attached>");
            int bstart = content.indexOf("<board>");
            int bend = content.indexOf("</board>");
            int boardPartLength = bend - bstart; // must be at least 14, 1 char boardnamwe, 2 times " * " and keys="N/A"

// TODO: check for validness of found , e.g. format for boards is:
//  "<board>mp3ogg * SSK@PJONkPZC6a4EuhHE~dP0nYyWK-oPAgM * N/A</board>"

            if( ( start ==-1 || end == -1) && (bstart == -1 || bend ==-1 || boardPartLength < 14) )
            {
                // Move divider to 100% and make it invisible
                attachmentSplitPane.setDividerSize(0);
                attachmentSplitPane.setDividerLocation(1.0);
                boardSplitPane.setDividerSize(0);
                boardSplitPane.setDividerLocation(1.0);

                tofTextArea.setText(selectedMessage.getPlaintext());
            }
            else
            {
                // Attachment available
                if( start != -1 && end != -1 )
                {
                    if( bstart== -1  || bend ==-1 )
                    {
                        boardSplitPane.setDividerSize(0);
                        boardSplitPane.setDividerLocation(1.0);
                    }
                    attachmentSplitPane.setDividerLocation(0.75);
                    attachmentSplitPane.setDividerSize(3);

                    // Add attachments to table
                    attachmentTableModel.setDataVector(selectedMessage.getAttachments(), attachmentTableColumnNames);
                    tofTextArea.setText(selectedMessage.getPlaintext());  //was getNewContent()
                    downloadAttachmentsButton.setEnabled(true);
                }

                // Board Available
                if( bstart != -1 && bend != -1 )
                {
                    //only a board, no attachments.
                    if( start== -1  || end ==-1 )
                    {
                        attachmentSplitPane.setDividerSize(0);
                        attachmentSplitPane.setDividerLocation(1.0);
                    }
                    boardSplitPane.setDividerLocation(0.75);
                    boardSplitPane.setDividerSize(3);

                    // Add attachments to table
                    boardTableModel.setDataVector(selectedMessage.getBoards(), boardTableColumnNames);
                    tofTextArea.setText(selectedMessage.getPlaintext());  //was getNewContent()
                    downloadBoardsButton.setEnabled(true); //TODO: downloadBoardsButton
                }
            }
            if( content.length() > 0 )
                saveMessageButton.setEnabled(true);
            else
                saveMessageButton.setEnabled(false);
        }
        else
        {
            tofReplyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);
        }
    }

    /**Selects message icon in lower right corner*/
    public static void displayNewMessageIcon(boolean showNewMessageIcon) {
    if (showNewMessageIcon) {
        
frame1.getInstance().setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/newmessage.gif")));
        statusMessageLabel.setIcon(newMessage[0]);
    }
    else {        
frame1.getInstance().setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
        statusMessageLabel.setIcon(newMessage[1]);
    }
    }

    public static void incTries(String board) {
    BoardStat boardStat = (BoardStat)boardStats.get(board);
    if (boardStat != null)
        boardStat.incTries();
    }
    public static void incSuccess(String board) {
    BoardStat boardStat = (BoardStat)boardStats.get(board);
    if (boardStat != null)
        boardStat.incSuccess();
    }
    public static void incAccess(String board) {
    BoardStat boardStat = (BoardStat)boardStats.get(board);
    if (boardStat != null)
        boardStat.incAccess();
    }
    public static void resetAccess(String board) {
    BoardStat boardStat = (BoardStat)boardStats.get(board);
    if (boardStat != null)
        boardStat.resetAccess();
    }

    static final Comparator statCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
        int value1 = ((BoardStat)o1).getCp();
        int value2 = ((BoardStat)o2).getCp();
        if (value1 < value2)
            return 1;
        else
            return -1;
        }
    };

    static final Comparator successCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
        int value1 = ((BoardStat)o1).getSuccess();
        int value2 = ((BoardStat)o2).getSuccess();
        if (value1 < value2)
            return 1;
        else
            return -1;
        }
    };

    public String selectNextBoard(Vector boards, int boardCount) {
    Random rand = new Random(System.currentTimeMillis());
    String board = new String();
    BoardStat[] statArray = new BoardStat[boardCount];


    // Copy all board statistics into one array, statArray
    // and increase the access counter
    int minValue = Integer.MAX_VALUE;
    int maxValue = Integer.MIN_VALUE;
    for (int i = 0; i < boards.size(); i++) {
        String tmp = (String)boards.elementAt(i);
        statArray[i] = (BoardStat)boardStats.get(tmp);
        statArray[i].incAccess();
    }

    // Sort by succes value
    Arrays.sort(statArray, successCmp);

    // Minimize success values
    for (int i = 0; i < statArray.length; i++)
        statArray[i].setSuccess(statArray.length - i);

    // Sort by CP
    Arrays.sort(statArray, statCmp);

    System.out.println("*****************************************");
    System.out.println("CP**AC**RV**Board************************");
    for (int i = 0; i < statArray.length; i++) {

        // 50% chance to take the first board
        int randomValue = Math.abs(rand.nextInt())%2;

        String outLine = new StringBuffer()
                            .append(statArray[i].getCp()).append(" | ")
                            .append(statArray[i].getLastAccess()).append(" | ")
                            .append(randomValue).append(" | ")
                            .append(statArray[i].getBoard()).toString();

        System.out.println(outLine);
        String tmp = statArray[i].getBoard();
        if (doUpdate(tmp) && board.equals("") && randomValue == 0) {
        System.out.println("-----------------------------------------");
        board = tmp;
        }
    }
    System.out.println("*****************************************");

    if (board.equals(""))
        board = statArray[boardCount - 1].getBoard();

    return board;
    }

    /**timer Action Listener (automatic download)*/
    private void timer_actionPerformed() {
    counter++;
    idleTime++;

    // Display welcome message if no boards are available
    if (tofTreeNode.getChildCount() == 0) {
        attachmentSplitPane.setDividerSize(0);
        attachmentSplitPane.setDividerLocation(1.0);
        tofTextArea.setText(LangRes.getString("Welcome message"));
    }

    if (idleTime > 900) { // 15 minutes
        tofUpdateInterleave = 300; // 5 minutes
    }
    if (idleTime > 3600) { // 1 hour
        tofUpdateInterleave = 900; // 15 minutes
    }
    if (idleTime > 10800) { // 3 hours
        tofUpdateInterleave = 1800; // 30 minutes
    }

//  System.out.println("UMS: " + idleTime + " - " + tofUpdateInterleave);

    if (counter%180 == 0) // Check uploadTable every 3 minutes
        UploadTableFun.update(uploadTable);

    if (counter%300 == 0 && frostSettings.getBoolValue("removeFinishedDownloads"))
        DownloadTableFun.removeFinishedDownloads(downloadTable);

    String newText = new StringBuffer()
                .append(LangRes.getString("Up: ")).append(activeUploadThreads)
                .append(LangRes.getString("   Down: ")).append(activeDownloadThreads)
                .append(LangRes.getString("   TOFUP: ")).append(tofUploadThreads)
                .append(LangRes.getString("   TOFDO: ")).append(tofDownloadThreads)
//                .append(LangRes.getString("   Results: ")).append(searchTableModel.getRowCount())
                .append(LangRes.getString("   Selected board: ")).append(lastUsedBoard).toString();

    statusLabel.setText(newText);

/*    if (updateTof) {
        TOF.updateTofTable(lastUsedBoard,
                      keypool,
                      frostSettings.getIntValue("maxMessageDisplay"));
        updateTof = false;
    }*/

/*    if (updateTree) {
        updateTofTree();
        updateTree = false;
    }*/

    if (updateDownloads || counter%10 == 0) {
        DownloadTableFun.update(downloadTable, frostSettings.getIntValue("htlMax"), new File(keypool), new File(frostSettings.getValue("downloadDirectory")));
        updateDownloads = false;

        // Sometimes it seems that download table entries do not get reset to "Failed"
        // I did not find the bug yet, but if there is no download thread active
        // all unfinished downloads are set to "Waiting"
        if (activeDownloadThreads == 0 && downloadActivateCheckBox.isSelected())
        for (int i = 0; i < downloadTableModel.getRowCount(); i++)
            if (!downloadTableModel.getValueAt(i, 3).equals(LangRes.getString("Done")))
            downloadTableModel.setValueAt(LangRes.getString("Waiting"), i, 3);
    }

    // automatic TOF update
    if (counter%tofUpdateInterleave == 0 &&
        tofDownloadThreads < tofUpdateSpeed &&
        tofAutomaticUpdateMenuItem.isSelected()) {

        Vector boards = getTofTree().getAllBoards();
        //boards.insertElementAt("_boardlist", 0);
        int itemCount = boards.size();

        // Update boardStats
        for (int i = 0; i < boards.size(); i++) {
        String boardname = (String)boards.elementAt(i);
        if (!boardStats.containsKey(boardname)) {
            boardStats.put(boardname, new BoardStat(boardname));
        }
        }

        if (itemCount > 0) {
        String actualBoard = new String();
        actualBoard = selectNextBoard(boards, itemCount);
        incTries(actualBoard);
        resetAccess(actualBoard);
        if (doUpdate(actualBoard)) {
            System.out.println("Updating: " + actualBoard);
            updateBoard(actualBoard);
        }
        }
    }

    // Display time in button bar

    timeLabel.setText( new StringBuffer().append(DateFun.getExtendedDate())
                      .append(" - ").append(DateFun.getFullExtendedTime()).append(" GMT").toString());

    // Generate CHK's for upload table entries
    if (!generateCHK) {
        if (uploadTableModel.getRowCount() > 0) {
        for (int i = 0; i < uploadTableModel.getRowCount(); i++) {
            String file = null;
            String target = null;
            String destination = null;
            boolean isUnknown = false;
            synchronized (uploadTable){
                try{
                String state = (String)uploadTableModel.getValueAt(i, 5);
                if (state.equals(LangRes.getString("Unknown"))) {
                file = (String)uploadTableModel.getValueAt(i, 3);
                target = (String)uploadTableModel.getValueAt(i, 4);
                destination = (String)uploadTableModel.getValueAt(i, 0);
                uploadTableModel.setValueAt("Working...", i, 5);
                isUnknown = true;
                }
            }
            catch (Exception e) {System.out.println("generating chk NOT GOOD " +e.toString());}
            }
            if (isUnknown){
            insertThread newInsert = new insertThread(destination,
                                  new File(file),
                                  frostSettings.getValue("htlUpload"),
                                  target,
                                  false);
            newInsert.start();
            break;
            }
        }
        }
    }

    // Start upload thread
    int activeUthreads = 0;
    synchronized(threadCountLock) {
        activeUthreads=activeUploadThreads;
    }
    if (activeUthreads < frostSettings.getIntValue("uploadThreads")) {
        if (uploadTableModel.getRowCount() > 0) {
        for (int i = 0; i < uploadTableModel.getRowCount(); i++) {
            String file = null;
            String target = null;
            String destination = null;
            boolean isRequested = false;
            synchronized (uploadTable){
                try{
                String state = (String)uploadTableModel.getValueAt(i, 2);
                String key = (String)uploadTableModel.getValueAt(i, 5);
                if (state.equals(LangRes.getString("Requested")) && key.startsWith("CHK@")) {
                file = (String)uploadTableModel.getValueAt(i, 3);
                target = (String)uploadTableModel.getValueAt(i, 4);
                destination = (String)uploadTableModel.getValueAt(i, 0);
                uploadTableModel.setValueAt(LangRes.getString("Uploading"), i, 2);
                isRequested = true;
                }
            }
            catch (Exception e) {System.out.println("uploading NOT GOOD " +e.toString());}
            }
            if (isRequested){
            insertThread newInsert = new insertThread(destination,
                                  new File(file),
                                  frostSettings.getValue("htlUpload"),
                                  target,
                                  true);
            newInsert.start();
            break;
            }
        }
        }
    }

    // Start download thread
    int activeDthreads = 0;
    synchronized(threadCountLock) {
        activeDthreads=activeDownloadThreads;
    }
    if (activeDthreads < frostSettings.getIntValue("downloadThreads") && downloadActivateCheckBox.isSelected()) {
        for (int i = 0; i < downloadTableModel.getRowCount(); i++) {
        if (downloadTableModel.getValueAt(i, 3).equals(LangRes.getString("Waiting"))) {
            String filename = null;
            String size = null;
            String htl = null;
            String source = null;
            String key = null;
            boolean isWaiting = false;
            synchronized (downloadTable){
                try{
                filename = (String)downloadTableModel.getValueAt(i,0);
                size = (String)downloadTableModel.getValueAt(i,1);
                htl = (String)downloadTableModel.getValueAt(i,4);
                key = (String)downloadTableModel.getValueAt(i,6);
                source = (String)downloadTableModel.getValueAt(i,5);
                downloadTableModel.setValueAt(LangRes.getString("Trying"), i, 3);
                isWaiting = true;
            }
            catch (Exception e) {System.out.println("download NOT GOOD " +e.toString());}
            }
            if (isWaiting){
            requestThread newRequest = new requestThread(filename, size, downloadTable, uploadTable, htl, key, source);
            newRequest.start();
            break;
            }
        }
        }
    }
    }

    /**searchTextField Action Listener (search)*/
    private void searchTextField_actionPerformed(ActionEvent e) {
    if (searchButton.isEnabled())
        searchButton_actionPerformed(e);
    }

    /**downloadTextField Action Listener (Download/Quickload)*/
    private void downloadTextField_actionPerformed(ActionEvent e) {
    String key = (downloadTextField.getText()).trim();
    if (key.length() > 0) {
        // strip the 'freenet:' prefix
        if(key.indexOf("freenet:") == 0){
        key = key.substring(8);
        }

        String validkeys[]={"SSK@", "CHK@", "KSK@"};
        boolean valid=false;

        for (int i = 0; i < validkeys.length; i++) {
        if (key.substring(0, validkeys[i].length()).equals(validkeys[i]))
            valid=true;
        }

        if (valid) {
        // added a way to specify a file name. The filename is preceeded by a colon.
        String fileName;

        int sepIndex = key.lastIndexOf(":");

        if (sepIndex != -1){
            fileName = key.substring(sepIndex + 1);
            key = key.substring(0, sepIndex);
        }
        // take the filename from the last part the SSK or KSK
        else if (-1 != (sepIndex = key.lastIndexOf("/"))){
            fileName = key.substring(sepIndex + 1);
        }
        else {
            fileName = key.substring(4);
        }
        // add valid key to download table
        DownloadTableFun.insertDownload(mixed.makeFilename(fileName), "Unknown", "Unknown", key, frostSettings.getIntValue("htl"), downloadTable, lastUsedBoard);
        }
        else {
        // show messagebox that key is invalid
        String keylist = "";
        for (int i=0; i < validkeys.length; i++) {
            if (i > 0)
            keylist += ", ";
            keylist += validkeys[i];
        }
        JOptionPane.showMessageDialog(this,
                          LangRes.getString("Invalid key.  Key must begin with one of") + ": "  + keylist,
                          LangRes.getString("Invalid key"),
                          JOptionPane.ERROR_MESSAGE);
        }
    }
    }

    /**searchButton Action Listener (Search)*/
    private void searchButton_actionPerformed(ActionEvent e) {
    searchButton.setEnabled(false);
    searchTable.clearSelection();
    TableFun.removeAllRows(searchTable);
    Vector boardsToSearch;
    if( searchAllBoardsCheckBox.isSelected() )
    {
        // search in all boards
        boardsToSearch = getTofTree().getAllBoards();
    }
    else
    {
        boardsToSearch = new Vector();
        boardsToSearch.add( lastUsedBoard );
    }

    SearchThread searchThread = new SearchThread(searchTextField.getText(),
                             boardsToSearch,
                             keypool,
                             (String)searchComboBox.getSelectedItem(),
                             frostSettings
                             );
    searchThread.start();
    }

    /**tofNewMessageButton Action Listener (tof/ New Message)*/
    private void tofNewMessageButton_actionPerformed(ActionEvent e) {
    String[] args = {lastUsedBoard,
             frostSettings.getValue("userName"),
             "No subject", "",
             frostSettings.getValue("lastUsedDirectory")
    };

    if(frostSettings.getBoolValue("useAltEdit")){
        altEdit = new AltEdit(lastUsedBoard,"No subject", "", keypool, frostSettings, this);
        altEdit.start();
    }
    else{
        MessageFrame newMessage = new MessageFrame(args, keypool, frostSettings, this);
        newMessage.show();
    }
    }

    /**tofReplyButton Action Listener (tof/Reply)*/
    private void tofReplyButton_actionPerformed(ActionEvent e) {
    String subject = "No subject";
    int selectedRow = messageTable.getSelectedRow();
    String[] args = {lastUsedBoard,
             frostSettings.getValue("userName"),
             lastSelectedMessage,
             tofTextArea.getText(),
             frostSettings.getValue("lastUsedDirectory")};
    if (!args[2].startsWith("Re:"))
        args[2] = "Re: " + args[2];

    if(frostSettings.getBoolValue("useAltEdit")){
        altEdit = new AltEdit(lastUsedBoard, args[2], args[3], keypool, frostSettings, this);
        altEdit.start();
    }
    else{
        MessageFrame newMessage = new MessageFrame(args, keypool, frostSettings , this);
        newMessage.show();
    }
    }

    private void tofDisplayBoardInfoMenuItem_actionPerformed(ActionEvent e) {
    BoardInfoFrame boardInfo = new BoardInfoFrame(this);
    boardInfo.show();
    }

    //------------------------------------------------------------------------

    private void uploadAddFilesButton_actionPerformed(ActionEvent e) {
    final JFileChooser fc = new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
    fc.setDialogTitle("Select files you want to upload to the " + lastUsedBoard + " board.");
    fc.setFileHidingEnabled(true);
    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fc.setMultiSelectionEnabled(true);
    fc.setPreferredSize(new Dimension(600, 400));

    int returnVal = fc.showOpenDialog(frame1.this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        if (file != null) {
        frostSettings.setValue("lastUsedDirectory", file.getParent());
        File[] selectedFiles = fc.getSelectedFiles();

        String masterDirectory = new String();
        for (int i = 0; i < selectedFiles.length; i++) {
            Vector allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
            for (int j = 0; j < allFiles.size(); j++) {
            File newFile = (File)allFiles.elementAt(j);
            if (newFile.isFile()) {
                UploadTableFun.add(uploadTable, newFile, selectedFiles[i], lastUsedBoard);
            }
            }
        }
        }
    }
    }

    /**File | Exit action performed*/
    private void fileExitMenuItem_actionPerformed(ActionEvent e) {

    // Remove the tray icon
    try {
        Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayKill.exe");
    }catch(IOException _IoExc) { }

    System.exit(0);
    }

    /**News | Configure Board action performed*/
    private void tofConfigureBoardMenuItem_actionPerformed(ActionEvent e) {
    BoardSettingsFrame newFrame = new BoardSettingsFrame(this, lastUsedBoard);
    newFrame.setModal(true); // lock main window
    newFrame.show();
    if (newFrame.getExitState()) {
        updateTofTree();
    }
    }

    /**Options | Preferences action performed*/
    private void optionsPreferencesMenuItem_actionPerformed(ActionEvent e) {
    saveSettings();
    OptionsFrame newFrame = new OptionsFrame(this);
    newFrame.setModal(true); // lock main window
    newFrame.show();
    if (newFrame.getExitState()) {
        frostSettings.readSettingsFile();
    }
    oldMessageHeader = "";
    timeLabel.setText("");
    }

    /**Help | About action performed*/
    private void helpAboutMenuItem_actionPerformed(ActionEvent e) {
    AboutBox dlg = new AboutBox(this);
    dlg.setModal(true);
    dlg.show();
    }

    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        fileExitMenuItem_actionPerformed(null);
    }
    }

    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

    /**Save on exit*/
    private void saveOnExit() {
    saveSettings();
    TableFun.saveTable(downloadTable, new File("download.txt"));
    TableFun.saveTable(uploadTable, new File("upload.txt"));
    System.out.println("Bye...");
    }

    /**Save settings*/
    private void saveSettings() {
    frostSettings.setValue("downloadingActivated", downloadActivateCheckBox.isSelected());
//      frostSettings.setValue("uploadingActivated", uploadActivateCheckBox.isSelected());
    frostSettings.setValue("searchAllBoards", searchAllBoardsCheckBox.isSelected());
//      frostSettings.setValue("reducedBlockCheck", reducedBlockCheckCheckBox.isSelected());
    frostSettings.setValue("automaticUpdate", tofAutomaticUpdateMenuItem.isSelected());
    frostSettings.writeSettingsFile();
    getTofTree().saveTree(new File("boards.txt"));
    getTofTree().writeTreeState(new File("toftree.txt"));
    }

    /**Load Settings*/
    private void loadSettings() {
    downloadActivateCheckBox.setSelected(frostSettings.getBoolValue("downloadingActivated"));
//      uploadActivateCheckBox.setSelected(frostSettings.getBoolValue("uploadingActivated"));
    searchAllBoardsCheckBox.setSelected(frostSettings.getBoolValue("searchAllBoards"));
//      reducedBlockCheckCheckBox.setSelected(frostSettings.getBoolValue("reducedBlockCheck"));
    tofAutomaticUpdateMenuItem.setSelected(frostSettings.getBoolValue("automaticUpdate"));
    }

    class IdleStopper extends MouseAdapter {

    public void mousePressed(MouseEvent e) {
        doThisPlease(e);
    }

    public void mouseReleased(MouseEvent e) {
        doThisPlease(e);
    }

    public void mouseEntered(MouseEvent e) {
        doThisPlease(e);
    }

    public void mouseExited(MouseEvent e) {
        doThisPlease(e);
    }

    public void mouseClicked(MouseEvent e) {
        doThisPlease(e);
    }

    void doThisPlease(MouseEvent e) {
        idleTime = 0;
        tofUpdateInterleave = 60;
    }

    }

    class PopupListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2) {

        // Start file from download table
        if (e.getComponent().equals(downloadTable)) {
            File file = new File(System.getProperty("user.dir") +
                     fileSeparator +
                     frostSettings.getValue("downloadDirectory") +
                     (String)downloadTableModel.getValueAt(downloadTable.getSelectedRow(), 0));
            System.out.println(file.getPath());
            if (file.exists())
            Execute.run("exec.bat" + " " + file.getPath());
        }

        // Start file from upload table
        if (e.getComponent().equals(uploadTable)) {
            File file = new File((String)uploadTableModel.getValueAt(uploadTable.getSelectedRow(), 3));
            System.out.println(file.getPath());
            if (file.exists())
            Execute.run("exec.bat" + " " + file.getPath());
        }

        // Add search result to download table
        if (e.getComponent().equals(searchTable)) {
            SearchTableFun.downloadSelectedKeys(frostSettings.getIntValue("htl"), searchTable, downloadTable);
        }

        }
        else {
        maybeShowPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {

        if (e.isPopupTrigger()) {

        if (e.getComponent().equals(uploadTable)) { // Upload Popup

            // Add boards to changeDestinationBoard submenu
            Vector boards = getTofTree().getAllBoards();
            uploadPopupChangeDestinationBoard.removeAll();
            for (int i = 0; i < boards.size(); i++) {
            JMenuItem boardMenuItem = new JMenuItem((String)boards.elementAt(i));
            uploadPopupChangeDestinationBoard.add(boardMenuItem);
            boardMenuItem.addActionListener(new ActionListener()  {
                public void actionPerformed(ActionEvent e) {
                    TableFun.setSelectedRowsColumnValue(uploadTable, 4, mixed.makeFilename(e.getActionCommand()));
                }
                });
            }

            if (uploadTable.getSelectedRow() == -1) {
            uploadPopupRemoveSelectedFiles.setEnabled(false);
            uploadPopupReloadSelectedFiles.setEnabled(false);
            uploadPopupMoveSelectedFilesUp.setEnabled(false);
            uploadPopupMoveSelectedFilesDown.setEnabled(false);
            uploadPopupSetPrefixForSelectedFiles.setEnabled(false);
            uploadPopupRestoreDefaultFilenamesForSelectedFiles.setEnabled(false);
            uploadPopupChangeDestinationBoard.setEnabled(false);
            }
            else {
            uploadPopupRemoveSelectedFiles.setEnabled(true);
            uploadPopupMoveSelectedFilesUp.setEnabled(true);
            uploadPopupReloadSelectedFiles.setEnabled(true);
            uploadPopupMoveSelectedFilesDown.setEnabled(true);
            uploadPopupSetPrefixForSelectedFiles.setEnabled(true);
            uploadPopupRestoreDefaultFilenamesForSelectedFiles.setEnabled(true);
            uploadPopupChangeDestinationBoard.setEnabled(true);
            }
            uploadPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        if (e.getComponent().equals(searchTable)) { // Search Popup
            if (searchTable.getSelectedRow() == -1) {
            searchPopupDownloadSelectedKeys.setEnabled(false);
            searchPopupCopyAttachment.setEnabled(false);
            }
            else {
            searchPopupDownloadSelectedKeys.setEnabled(true);
            searchPopupCopyAttachment.setEnabled(true);
            }
            searchPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        if (e.getComponent().equals(downloadTable)) { // Downloads Popup
            if (downloadTable.getSelectedRow() == -1) {
            downloadPopupRemoveSelectedDownloads.setEnabled(false);
            downloadPopupMoveUp.setEnabled(false);
            downloadPopupMoveDown.setEnabled(false);
            downloadPopupResetHtlValues.setEnabled(false);
            }
            else {
            downloadPopupRemoveSelectedDownloads.setEnabled(true);
            downloadPopupMoveUp.setEnabled(true);
            downloadPopupMoveDown.setEnabled(true);
            downloadPopupResetHtlValues.setEnabled(true);
            }
            downloadPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        if (e.getComponent().equals(tofTextArea)) { // TOF text popup
            String text = selectedMessage.getContent();
            if (text != null) {
                tofTextPopupSaveBoard.setEnabled(false);
            tofTextPopupSaveBoards.setEnabled(false);
            tofTextPopupSaveAttachment.setEnabled(false);
            tofTextPopupSaveAttachments.setEnabled(false);
        /*
            if (text.indexOf("<attached>") != -1 && text.indexOf("</attached>") != -1) {
                tofTextPopupSaveAttachments.setEnabled(true);
            }
            else
                tofTextPopupSaveAttachments.setEnabled(false);

            if (text.indexOf("<board>") != -1 && text.indexOf("</board>") != -1) {
                tofTextPopupSaveBoards.setEnabled(true);
            }
            else
                tofTextPopupSaveBoards.setEnabled(false);*/



            tofTextPopupSaveMessage.setEnabled(true);
            tofTextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            else {
            tofTextPopupSaveAttachments.setEnabled(false);
            tofTextPopupSaveAttachment.setEnabled(false);
            tofTextPopupSaveBoards.setEnabled(false);
            tofTextPopupSaveBoard.setEnabled(false);
            tofTextPopupSaveMessage.setEnabled(false);

            }
        }

        if (e.getComponent().equals(boardTable)) {// Board attached popup
            //if (e.getComponent().equals(tofTextArea)) System.out.println("uh oh");
            if (boardTable.getSelectedRow() == -1) {
                tofTextPopupSaveBoards.setEnabled(true);
                tofTextPopupSaveBoard.setEnabled(false);
            } else {
                tofTextPopupSaveBoards.setEnabled(false);
                tofTextPopupSaveBoard.setEnabled(true);
            }
            tofTextPopupSaveAttachments.setEnabled(false);
            tofTextPopupSaveAttachment.setEnabled(false);
            tofTextPopupSaveMessage.setEnabled(false);
                tofTextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        if (e.getComponent().equals(attachmentTable)) {// Board attached popup
            //if (e.getComponent().equals(tofTextArea)) System.out.println("uh oh");
            if (attachmentTable.getSelectedRow() == -1) {
                tofTextPopupSaveAttachments.setEnabled(true);
                tofTextPopupSaveAttachment.setEnabled(false);
            } else {
                tofTextPopupSaveAttachments.setEnabled(false);
                tofTextPopupSaveAttachment.setEnabled(true);
            }
            tofTextPopupSaveBoards.setEnabled(false);
            tofTextPopupSaveBoard.setEnabled(false);
            tofTextPopupSaveMessage.setEnabled(false);
                tofTextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        if (e.getComponent().equals(getTofTree())) { // TOF tree popup
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)getTofTree().getLastSelectedPathComponent();
            tofTreePopupConfigureBoard.setEnabled(false);
            tofTreePopupRemoveNode.setEnabled(false);
            tofTreePopupCutNode.setEnabled(false);
            tofTreePopupCopyNode.setEnabled(false);
            tofTreePopupPasteNode.setEnabled(false);
            if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if (parent != null) {
                tofTreePopupRemoveNode.setEnabled(true);
                tofTreePopupCutNode.setEnabled(true);
                tofTreePopupCopyNode.setEnabled(true);
                if (node.isLeaf())
                tofTreePopupConfigureBoard.setEnabled(true);
            }
            }
            if (!clipboard.equals(""))
            tofTreePopupPasteNode.setEnabled(true);

            tofTreePopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
        }
    }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    //System.out.println("Clipboard contents replaced");
    }

    public void updateMessageCountLabels()
    {
        updateMessageCountLabels(false, null);
    }
    // methods that update the Msg and New counts for tof table
    public void updateMessageCountLabels(boolean updateNewBoardsList, String board)
    {
        DefaultTableModel model = (DefaultTableModel)messageTable.getModel();

        int allMessages = model.getRowCount();
        allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

        int newMessages=0;
        for(int x=0; x<allMessages; x++)
        {
            String sender = (String)model.getValueAt(x, 1);
            if( sender.startsWith("<html><b>") )
            {
                newMessages++;
            }
        }
        newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
        if( updateNewBoardsList == true )
        {
            if( newMessages == 0 )
                getBoardsThatContainNewMsg().remove(board);
            else if( newMessages > 0 )
                getBoardsThatContainNewMsg().put(board,board);
        }
    }

    public void updateSearchResultCountLabel()
    {
        DefaultTableModel model = (DefaultTableModel)searchTable.getModel();
        int searchResults = model.getRowCount();
        if( searchResults == 0 )
        {
            searchResultsCountLabel.setText(searchResultsCountPrefix + "0");
        }
        searchResultsCountLabel.setText(searchResultsCountPrefix + searchResults);
    }

    public Hashtable getBoardsThatContainNewMsg()
    {
        return boardsThatContainNewMessages;
    }
}

