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
import javax.swing.border.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;

import frost.FcpTools.*;
import frost.gui.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.ext.*;
import frost.components.*;
import frost.crypt.*;
import frost.threads.*;
import frost.identities.*;

public class frame1 extends JFrame implements ClipboardOwner
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");
    static ImageIcon[] newMessage = new ImageIcon[2];

    private RunningBoardUpdateThreads runningBoardUpdateThreads = null;

    FrostBoardObject clipboard = null;
    long counter = 55;

    private static frame1 instance = null; // set in constructor
    boolean started = false;
    public static boolean updateDownloads = true;

    public static boolean updateTof = false;
    public static boolean updateTree = false;
    public static String fileSeparator = System.getProperty("file.separator");
    // "keypool.dir" is the corresponding key in frostSettings,
    // set in constructor of SettingsClass to this val.
    // this is the new way to access this value.
    public static String keypool = "keypool" + fileSeparator;
    public static String newMessageHeader = new String("");
    public static String oldMessageHeader = new String("");
    public static int activeUploadThreads = 0;
    public static int activeDownloadThreads = 0;
    private String lastSelectedMessage;

    public static FrostMessageObject selectedMessage = new FrostMessageObject();
    public static boolean generateCHK = false;

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

    // buttons that are enabled/disabled later
    JButton pasteBoardButton = null;
    JButton configBoardButton = null;

    JButton tofNewMessageButton = null;
    JButton tofReplyButton = null;
    JButton downloadAttachmentsButton= null;
    JButton downloadBoardsButton= null;
    JButton saveMessageButton= null;
    JButton trustButton= null;
    JButton notTrustButton= null;
    JButton tofUpdateButton = null;

    JButton uploadAddFilesButton = null;
    JButton searchButton = null;

    // labels that are updated later
    JLabel statusLabel = null;
    JLabel statusMessageLabel = null;

    JLabel timeLabel = null;

    JCheckBox searchAllBoardsCheckBox = null;
    JCheckBox downloadActivateCheckBox = null;
    JCheckBoxMenuItem tofAutomaticUpdateMenuItem = null;

    JComboBox searchComboBox = null;

    JTabbedPane tabbedPane = null;

    JSplitPane attachmentSplitPane = null;
    JSplitPane boardSplitPane = null;

    final String allMessagesCountPrefix = "Msg: ";
    final String newMessagesCountPrefix = "New: ";
    JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix+"0");
    JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix+"0");

    private String searchResultsCountPrefix = null;
    JLabel searchResultsCountLabel = new JLabel();

    TofTree tofTree = null;

    private UploadTable uploadTable = null;
    private MessageTable messageTable = null;
    private SearchTable searchTable = null;
    private DownloadTable downloadTable = null;
    private JTable attachmentTable = null;
    private JTable boardTable = null;

    private JTextArea tofTextArea = null;
    private JTextField downloadTextField = null;
    private JTextField searchTextField = null;

//    JCheckBox uploadActivateCheckBox = new JCheckBox(new ImageIcon(frame1.class.getResource("/data/up.gif")), true);
//    JCheckBox reducedBlockCheckCheckBox= new JCheckBox();

    // all popupmenu items
    JPopupMenu searchPopupMenu = null;
    JMenuItem searchPopupDownloadSelectedKeys = null;
    JMenuItem searchPopupDownloadAllKeys = null;
    JMenuItem searchPopupCopyAttachment = null;
    JMenuItem searchPopupCancel = null;

    JPopupMenu uploadPopupMenu = null;
//    JMenuItem uploadPopupMoveSelectedFilesUp = null;
//    JMenuItem uploadPopupMoveSelectedFilesDown = null;
    JMenuItem uploadPopupRemoveSelectedFiles = null;
    JMenuItem uploadPopupRemoveAllFiles = null;
    JMenuItem uploadPopupReloadSelectedFiles = null;
    JMenuItem uploadPopupReloadAllFiles = null;
    JMenuItem uploadPopupSetPrefixForSelectedFiles = null;
    JMenuItem uploadPopupSetPrefixForAllFiles = null;
    JMenuItem uploadPopupRestoreDefaultFilenamesForSelectedFiles = null;
    JMenuItem uploadPopupRestoreDefaultFilenamesForAllFiles = null;
    JMenu uploadPopupChangeDestinationBoard = null;
    JMenuItem uploadPopupAddFilesToBoard = null;
    JMenuItem uploadPopupCancel = null;

    JPopupMenu downloadPopupMenu = null;
    JMenuItem downloadPopupRemoveSelectedDownloads = null;
    JMenuItem downloadPopupRemoveAllDownloads = null;
    JMenuItem downloadPopupResetHtlValues = null;
//    JMenuItem downloadPopupMoveUp = null;
//    JMenuItem downloadPopupMoveDown = null;
    JMenuItem downloadPopupCancel = null;

    JPopupMenu tofTextPopupMenu = null;
    JMenuItem tofTextPopupSaveMessage = null;
    JMenuItem tofTextPopupSaveAttachments = null;
    JMenuItem tofTextPopupSaveAttachment = null;
    JMenuItem tofTextPopupSaveBoards = null;
    JMenuItem tofTextPopupSaveBoard = null;
    JMenuItem tofTextPopupCancel = null;

    JPopupMenu tofTreePopupMenu = null;
    JMenuItem tofTreePopupRefresh = null;
    JMenuItem tofTreePopupAddNode = null;
    JMenuItem tofTreePopupAddFolder = null;
    JMenuItem tofTreePopupRemoveNode = null;
    JMenuItem tofTreePopupCutNode = null;
    JMenuItem tofTreePopupPasteNode = null;
    JMenuItem tofTreePopupConfigureBoard = null;
    JMenuItem tofTreePopupCancel = null;

    //------------------------------------------------------------------------
    // end-of: Generate objects
    //------------------------------------------------------------------------

    boolean loaded_tables;

    // returns the current id,crypt, etc.
    public static LocalIdentity getMyId() {return mySelf;}
    public static BuddyList getFriends() {return friends;}
    public static crypt getCrypto() {return crypto;}
    public static BuddyList getEnemies() {return enemies;}
    //------------------------------------------------------------------------

    /*************************
     * GETTER + SETTER       *
     *************************/
    public static frame1    getInstance() { return instance; }
    public UploadTable      getUploadTable() { return uploadTable; }
    public MessageTable     getMessageTable() { return messageTable; }
    public SearchTable      getSearchTable() { return searchTable; }
    public DownloadTable    getDownloadTable() { return downloadTable; }
    public JTable           getAttachmentTable() { return attachmentTable; }
    public JTable           getAttachedBoardsTable() { return boardTable; }
    public String           getTofTextAreaText() { return tofTextArea.getText(); }
    public void             setTofTextAreaText(String txt) { tofTextArea.setText(txt); }
    public TofTree          getTofTree() { return tofTree; }
    public JButton          getSearchButton() { return searchButton; }

    public FrostBoardObject getActualNode()
    {
        FrostBoardObject node = (FrostBoardObject)getTofTree().getLastSelectedPathComponent();
        if( node == null )
        {
            // nothing selected? unbelievable ! so select the root ...
            getTofTree().setSelectionRow(0);
            node = (FrostBoardObject)getTofTree().getModel().getRoot();
        }
        return node;
    }

    public RunningBoardUpdateThreads getRunningBoardUpdateThreads()
    {
        return runningBoardUpdateThreads;
    }

    /**Construct the frame*/
    public frame1()
    {
        instance = this;
        loaded_tables=false;

        runningBoardUpdateThreads = new RunningBoardUpdateThreads();

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();

            saver = new Thread() {
                public void run() {
                    System.out.println("saving identities");
                    File identities = new File("identities");

                    try
                    { //TODO: replace this with a call to XML serializer
                        FileWriter fout = new FileWriter(identities);
                        fout.write(mySelf.getName() + "\n");
                        fout.write(mySelf.getKeyAddress() + "\n");
                        fout.write(mySelf.getKey() + "\n");
                        fout.write(mySelf.getPrivKey() + "\n");

                        //now do the friends
                        fout.write("*****************\n");
                        Iterator i = friends.values().iterator();
                        while( i.hasNext() )
                        {
                            Identity cur = (Identity)i.next();
                            fout.write(cur.getName() + "\n");
                            fout.write(cur.getKeyAddress() + "\n");
                            fout.write(cur.getKey() + "\n");
                        }
                        fout.write("*****************\n");
                        i = enemies.values().iterator();
                        while( i.hasNext() )
                        {
                            Identity cur = (Identity)i.next();
                            fout.write(cur.getName() + "\n");
                            fout.write(cur.getKeyAddress() + "\n");
                            fout.write(cur.getKey() + "\n");
                        }
                        fout.write("*****************\n");
                        fout.close();
                    }
                    catch( IOException e )
                    {
                        System.out.println("couldn't save buddy list"); System.out.println(e.toString());
                    }
                    saveOnExit();
                    FileAccess.cleanKeypool(keypool);
                }
                };
            Runtime.getRuntime().addShutdownHook(saver);

            cleaner = new TimerTask() {
                public void run() {
                    int i =0;
                    if( i==10 && frostSettings.getBoolValue("doCleanUp") )
                    {
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
        catch( Exception e ) { e.printStackTrace(); }
    }

    /**
     * Configures a button to be a default icon button
     * @param button The new icon button
     * @param toolTipText Is displayed when the mousepointer is some seconds over a button
     * @param rolloverIcon Displayed when mouse is over button
     */
    protected void configureButton(JButton button, String toolTipText, String rolloverIcon)
    {
        String text = null;
        try {
            text = LangRes.getString(toolTipText);
        } catch(MissingResourceException ex)
        {
            text = toolTipText; // better than nothing ;)
        }
        button.setToolTipText(text);

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

    private JToolBar buildButtonPanel()
    {
        timeLabel = new JLabel("");
// configure buttons
        this.pasteBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/paste.gif")));
        this.configBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/configure.gif")));

        JButton newBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newboard.gif")));
        JButton newFolderButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newfolder.gif")));
        JButton removeBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/remove.gif")));
        JButton renameBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/rename.gif")));
        JButton cutBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/cut.gif")));
        JButton boardInfoButton= new JButton(new ImageIcon(frame1.class.getResource("/data/info.gif")));
        JButton systemTrayButton= new JButton(new ImageIcon(frame1.class.getResource("/data/tray.gif")));

        configureButton(newBoardButton, "New board", "/data/newboard_rollover.gif");
        configureButton(newFolderButton, "New folder", "/data/newfolder_rollover.gif");
        configureButton(removeBoardButton, "Remove board", "/data/remove_rollover.gif");
        configureButton(renameBoardButton, "Rename board", "/data/rename_rollover.gif");
        configureButton(configBoardButton, "Configure board", "/data/configure_rollover.gif");
        configureButton(cutBoardButton, "Cut board", "/data/cut_rollover.gif");
        configureButton(pasteBoardButton, "Paste board", "/data/paste_rollover.gif");
        configureButton(boardInfoButton, "Board Information Window", "/data/info_rollover.gif");
        configureButton(systemTrayButton, "Minimize to System Tray", "/data/tray_rollover.gif");

// add action listener
        newBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewBoard(frame1.getInstance());
            } });
        newFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewFolder(frame1.getInstance());
            } });
        renameBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameSelectedNode();
            } });
        removeBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedNode();
            } });
        cutBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cutSelectedNode();
            } });
        pasteBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pasteFromClipboard();
            } });
        configBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tofConfigureBoardMenuItem_actionPerformed(e);
            } });
        systemTrayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            try { // Hide the Frost window
                Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayHide.exe");
            }catch(IOException _IoExc) { }
            } });
        boardInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tofDisplayBoardInfoMenuItem_actionPerformed(e);
            } });
// build panel
        JToolBar buttonPanel = new JToolBar();
        buttonPanel.setRollover( true );
        buttonPanel.setFloatable( false );
        Dimension blankSpace = new Dimension(3,3);

        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(newBoardButton);
        buttonPanel.add(newFolderButton);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.addSeparator();
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(configBoardButton);
        buttonPanel.add(renameBoardButton);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.addSeparator();
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(cutBoardButton);
        buttonPanel.add(pasteBoardButton);
        buttonPanel.add(removeBoardButton);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.addSeparator();
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(boardInfoButton);
        // The System Tray Icon does only work on Windows machines.
        // It uses the Visual Basic files (compiled ones) in the data directory.
        if ((System.getProperty("os.name").startsWith("Windows")))
        {
            buttonPanel.add( Box.createRigidArea( blankSpace ) );
            buttonPanel.addSeparator();
            buttonPanel.add( Box.createRigidArea( blankSpace ) );

            buttonPanel.add(systemTrayButton);
        }
        buttonPanel.add( Box.createHorizontalGlue() );
        buttonPanel.add(timeLabel);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );

        return buttonPanel;
    }

    private JPanel buildStatusPanel()
    {
        statusLabel = new JLabel(LangRes.getString("Frost by Jantho"));
        statusMessageLabel = new JLabel();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER); // Statusbar
        statusPanel.add(statusMessageLabel, BorderLayout.EAST); // Statusbar / new Message
        return statusPanel;
    }

    private JPanel buildTofMainPanel()
    {
        this.tabbedPane = new JTabbedPane();
        //add a tab for buddies perhaps?
        tabbedPane.add(LangRes.getString("News"), buildMessagePane());
        tabbedPane.add(LangRes.getString("Search"), buildSearchPane());
        tabbedPane.add(LangRes.getString("Downloads"), buildDownloadPane());
        tabbedPane.add(LangRes.getString("Uploads"), buildUploadPane());

        updateOptionsAffectedComponents();

        // this rootnode is discarded later, but if we create the tree without parameters,
        // a new Model is created wich contains some sample data by default (swing)
        // this confuses our renderer wich only expects FrostBoardObjects in the tree
        FrostBoardObject dummyRootNode = new FrostBoardObject("Frost Message System", true);
        tofTree = new TofTree(dummyRootNode);
        JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
        tofTree.setRootVisible(true);
        //tofTree.setEditable(true);
        tofTree.setCellRenderer(new TofTreeCellRenderer());
        tofTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // tofTree selection listener
        tofTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                tofTree_actionPerformed(e);
        } });
        //tofTree / KeyEvent
        tofTree.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) { tofTree_keyPressed(e); }
            public void keyReleased(KeyEvent e) {}
        });

        JSplitPane treeAndTabbedPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, tabbedPane);
        treeAndTabbedPane.setDividerLocation(160); // Vertical Board Tree / MessagePane Divider

        JPanel tofMainPanel = new JPanel(new BorderLayout());
        tofMainPanel.add(treeAndTabbedPane, BorderLayout.CENTER); // TOF/Text
        return tofMainPanel;
    }

    private JPanel buildMessagePane()
    {
// configure buttons
        this.tofNewMessageButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newmessage.gif")));
        this.tofUpdateButton = new JButton(new ImageIcon(frame1.class.getResource("/data/update.gif")));
        this.tofReplyButton = new JButton(new ImageIcon(frame1.class.getResource("/data/reply.gif")));
        this.downloadAttachmentsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachment.gif")));
        this.downloadBoardsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachmentBoard.gif")));
        this.saveMessageButton= new JButton(new ImageIcon(frame1.class.getResource("/data/save.gif")));
        this.trustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/trust.gif")));
        this.notTrustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/nottrust.gif")));

        configureButton(tofNewMessageButton, "New message", "/data/newmessage_rollover.gif");
        configureButton(tofUpdateButton, "Update", "/data/update_rollover.gif");
        configureButton(tofReplyButton, "Reply", "/data/reply_rollover.gif");
        configureButton(downloadAttachmentsButton, "Download attachment(s)", "/data/attachment_rollover.gif");
        configureButton(downloadBoardsButton, "Add Board(s)", "/data/attachmentBoard_rollover.gif");
        configureButton(saveMessageButton, "Save message", "/data/save_rollover.gif");
        configureButton(trustButton, "Trust", "/data/trust_rollover.gif");
        configureButton(notTrustButton, "Do not trust", "/data/nottrust_rollover.gif");

// add action listener to buttons
        tofUpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) { // Update selected board
                // restarts all finished threads if there are some long running threads
                if (isUpdateAllowed(getActualNode()))  { updateBoard(getActualNode());  }
            } });
        tofNewMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tofNewMessageButton_actionPerformed(e);
            } });
        downloadAttachmentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadAttachments();
            } });
        downloadBoardsButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               downloadBoards();
           } });
       tofReplyButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               tofReplyButton_actionPerformed(e);
           } });
       saveMessageButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               FileAccess.saveDialog(getInstance(), getTofTextAreaText(), frostSettings.getValue("lastUsedDirectory"), LangRes.getString("Save message to disk"));
           } });
        trustButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               trustButton_actionPerformed(e);
           } });
       notTrustButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               notTrustButton_actionPerformed(e);
           } });
// build buttons panel
        JPanel tofTopPanel = new JPanel();
        BoxLayout dummyLayout = new BoxLayout(tofTopPanel, BoxLayout.X_AXIS);
        tofTopPanel.setLayout(dummyLayout);

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
        Dimension labelSize = dummyLabel.getPreferredSize();
        allMessagesCountLabel.setPreferredSize(labelSize);
        allMessagesCountLabel.setMinimumSize(labelSize);
        newMessagesCountLabel.setPreferredSize(labelSize);
        newMessagesCountLabel.setMinimumSize(labelSize);
        tofTopPanel.add(allMessagesCountLabel);
        tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        tofTopPanel.add(newMessagesCountLabel);
// build panel wich shows the message list + message
        MessageTableModel messageTableModel = new MessageTableModel();
        this.messageTable = new MessageTable(messageTableModel);
        messageTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        messageTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                messageTableListModel_valueChanged(e);
            } });
        JScrollPane messageTableScrollPane = new JScrollPane(messageTable);

        this.tofTextArea = new JTextArea();
        JScrollPane tofTextAreaScrollPane = new JScrollPane(tofTextArea);
        tofTextArea.setEditable(false);
        tofTextArea.setLineWrap(true);
        tofTextArea.setWrapStyleWord(true);

        AttachmentTableModel attachmentTableModel = new AttachmentTableModel();
        this.attachmentTable = new JTable(attachmentTableModel);
        JScrollPane attachmentTableScrollPane = new JScrollPane(attachmentTable);

        AttachedBoardTableModel boardTableModel = new AttachedBoardTableModel();
        this.boardTable = new JTable(boardTableModel);
        JScrollPane boardTableScrollPane = new JScrollPane(boardTable);

        this.attachmentSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                                                   tofTextAreaScrollPane,
                                                   attachmentTableScrollPane );
        this.boardSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                                              attachmentSplitPane,
                                              boardTableScrollPane );

        JSplitPane tofSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageTableScrollPane, boardSplitPane);
        tofSplitPane.setDividerSize(10);
        tofSplitPane.setDividerLocation(160);
        tofSplitPane.setResizeWeight(0.5d);
        tofSplitPane.setMinimumSize(new Dimension(50, 20));

// build panel
        JPanel messageTablePanel = new JPanel(new BorderLayout());
        messageTablePanel.add(tofTopPanel, BorderLayout.NORTH);
        messageTablePanel.add(tofSplitPane, BorderLayout.CENTER);
        return messageTablePanel;
    }

    /**
     * Called by frost after call of show().
     * Sets the initial states of the message splitpanes.
     * Must be called AFTER frame is shown.
     **/
    public void resetMessageViewSplitPanes()
    {
        // initially hide the attachment tables
        attachmentSplitPane.setDividerSize( 0 );
        attachmentSplitPane.setDividerLocation(1.0);
        boardSplitPane.setDividerSize( 0 );
        boardSplitPane.setDividerLocation(1.0);
        setTofTextAreaText( LangRes.getString("Select a message to view its content.") );
    }

    private JPanel buildSearchPane()
    {
// create objects for button toolbar
        this.searchAllBoardsCheckBox= new JCheckBox("all boards",true);
        JButton searchDownloadButton = new JButton(new ImageIcon(frame1.class.getResource("/data/save.gif")));
        this.searchButton = new JButton(new ImageIcon(frame1.class.getResource("/data/search.gif")));

        this.searchTextField = new JTextField(25);
        String[] searchComboBoxItems = {"All files", "Audio", "Video", "Images", "Documents", "Executables", "Archives"};
        this.searchComboBox = new JComboBox(searchComboBoxItems);
// configure buttons
        configureButton(searchButton, "Search", "/data/search_rollover.gif");
        configureButton(searchDownloadButton, "Download selected keys", "/data/save_rollover.gif");
// build button toolbar panel
        JPanel searchTopPanel = new JPanel();
        BoxLayout dummyLayout = new BoxLayout( searchTopPanel, BoxLayout.X_AXIS );
        searchTopPanel.setLayout( dummyLayout );

        searchTextField.setMaximumSize( searchTextField.getPreferredSize() );
        searchTopPanel.add(searchTextField); // Search / text
        searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        searchComboBox.setMaximumSize( searchComboBox.getPreferredSize() );
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
        JLabel dummyLabel = new JLabel(searchResultsCountPrefix + "00000");
        dummyLabel.doLayout();
        Dimension labelSize = dummyLabel.getPreferredSize();
        searchResultsCountLabel.setPreferredSize(labelSize);
        searchResultsCountLabel.setMinimumSize(labelSize);
        searchResultsCountLabel.setText(searchResultsCountPrefix+"0");
        searchTopPanel.add(searchResultsCountLabel);
// build table
        SearchTableModel searchTableModel = new SearchTableModel();
        this.searchTable = new SearchTable(searchTableModel);
        JScrollPane searchTableScrollPane = new JScrollPane(searchTable);
// add action listener
        searchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchTextField_actionPerformed(e);
            } });
        searchDownloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSelectedSearchItemsToDownloadTable();
            } });
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchButton_actionPerformed(e);
            } });
// build panel
        JPanel searchMainPanel = new JPanel(new BorderLayout());
        searchMainPanel.add(searchTopPanel, BorderLayout.NORTH); // Search / Buttons
        searchMainPanel.add(searchTableScrollPane, BorderLayout.CENTER); // Search / Results
        return searchMainPanel;
    }

    private JPanel buildDownloadPane()
    {
// create objects for buttons toolbar panel
        this.downloadActivateCheckBox = new JCheckBox(new ImageIcon(frame1.class.getResource("/data/down.gif")), true);
        configureCheckBox(downloadActivateCheckBox,
                     "Activate downloading",
                     "/data/down_rollover.gif",
                     "/data/down_selected.gif",
                     "/data/down_selected_rollover.gif");

        this.downloadTextField = new JTextField(25);
        downloadTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadTextField_actionPerformed(e);
        } });
// create buttons toolbar panel
        JPanel downloadTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        downloadTopPanel.add(downloadTextField);//Download/Quickload
        downloadTopPanel.add(downloadActivateCheckBox);//Download/Start transfer
// create downloadTable
        DownloadTableModel downloadTableModel = new DownloadTableModel();
        this.downloadTable = new DownloadTable(downloadTableModel);
        JScrollPane downloadTableScrollPane = new JScrollPane(downloadTable);
        //Downloads / KeyEvent
        downloadTable.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) { downloadTable_keyPressed(e); }
            public void keyReleased(KeyEvent e) {}
        });
// create the main download panel
        JPanel downloadMainPanel = new JPanel(new BorderLayout());
        downloadMainPanel.add(downloadTopPanel, BorderLayout.NORTH); // Download/Buttons
        downloadMainPanel.add(downloadTableScrollPane, BorderLayout.CENTER); //Downloadlist
        return downloadMainPanel;
    }

    private JPanel buildUploadPane()
    {
// create upload table
        UploadTableModel uploadTableModel = new UploadTableModel();
        this.uploadTable = new UploadTable(uploadTableModel);
        JScrollPane uploadTableScrollPane = new JScrollPane(uploadTable);
        // uploadTable / KeyEvent
        uploadTable.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE && !uploadTable.isEditing())
                    getUploadTable().removeSelectedRows();
            }
            public void keyReleased(KeyEvent e) {}
        });
// create toolbar button
        this.uploadAddFilesButton = new JButton(new ImageIcon(frame1.class.getResource("/data/browse.gif")));
        configureButton(uploadAddFilesButton, "Browse...", "/data/browse_rollover.gif");
        uploadAddFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadAddFilesButton_actionPerformed(e);
            } });
// add button to top panel
        JPanel uploadTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        uploadTopPanel.add(uploadAddFilesButton);
// add all to main upload panel
        JPanel uploadMainPanel = new JPanel(new BorderLayout());
        uploadMainPanel.add(uploadTopPanel, BorderLayout.NORTH);
        uploadMainPanel.add(uploadTableScrollPane, BorderLayout.CENTER);
        return uploadMainPanel;
    }
//**********************************************************************************************
//**********************************************************************************************
//**********************************************************************************************
    /**Component initialization*/
    private void jbInit() throws Exception  {

    setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
    this.setSize(new Dimension(790, 580));
    this.setResizable(true);

    this.setTitle("Frost");

    JPanel contentPanel = (JPanel)this.getContentPane();
    contentPanel.setLayout(new BorderLayout());

    contentPanel.add( buildButtonPanel(), BorderLayout.NORTH); // buttons toolbar
    contentPanel.add( buildTofMainPanel(), BorderLayout.CENTER); // tree / tabbed pane
    contentPanel.add( buildStatusPanel(), BorderLayout.SOUTH); // Statusbar

    buildMenuBar();
    buildPopupMenus();

//**********************************************************************************************
//**********************************************************************************************
//**********************************************************************************************

    /*configureCheckBox(searchAllBoardsCheckBox,
                 "Search all boards",
                 "data/allboards_rollover.gif",
                 "data/allboards_selected.gif",
                 "data/allboards_selected_rollover.gif");*/

    // Add Popup listeners
    MouseListener popupListener = new PopupListener();
    getDownloadTable().addMouseListener(popupListener);
    getSearchTable().addMouseListener(popupListener);
    getUploadTable().addMouseListener(popupListener);
    tofTextArea.addMouseListener(popupListener);
    getTofTree().addMouseListener(popupListener);
    getAttachmentTable().addMouseListener(popupListener);
    getAttachedBoardsTable().addMouseListener(popupListener);

//**********************************************************************************************
//**********************************************************************************************
//**********************************************************************************************

    timer = new javax.swing.Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            timer_actionPerformed();
    } });

    timer2 = new java.util.Timer(true);
    timer2.schedule(new checkForSpam(), 0, frostSettings.getIntValue("sampleInterval")*60*60*1000);

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

    //check whether the user is running a transient node
    FcpConnection con1 = null;
    String []nodeInfo=null;
    try {
        con1 = new FcpConnection(frostSettings.getValue("nodeAddress"), frostSettings.getValue("nodePort"));
        nodeInfo = con1.getInfo();
    }catch(Exception e) {
        System.out.println("Error - could not establish a connection to freenet node.");
        System.out.println("Make sure your node is running and that you have configured frost correctly.");
        System.out.println("Nevertheless, to allow you to read messages, Frost will startup now.");
        System.out.println("And don't get confusing by the error messages ... maybe disable the automatic board update.");
    }

    boolean transientNode = false;
    if( nodeInfo != null )
    {
        for (int ij=0;ij<nodeInfo.length;ij++)
        {
            if (nodeInfo[ij].startsWith("IsTransient") && nodeInfo[ij].indexOf("true") != -1)
            {
                transientNode=true;
            }
        }
    }

    if (transientNode)
    {
        JOptionPane.showMessageDialog(this,
                        "      You are running a TRANSIENT node.  "+
//                        "Filesharing will be disabled!\n"+
//                        "If you want to be able to download/upload files,"+
//                        "run a PERMANENT node.",
                        "Better run a PERMANENT freenet node.",
                        "Transient node detected",
                        JOptionPane.WARNING_MESSAGE);
    }
    con1=null;
    nodeInfo=null;

    //create a crypt object
    crypto = new FrostCrypt();

    //load the identities
    loadIdentities();

    TimerTask KeyReinserter = new TimerTask() {
        public void run() {
            System.out.println("re-uploading public key");
            FcpInsert.putFile("CHK@",new File("pubkey.txt"),25,false,true);
            System.out.println("finished re-uploading public key");
        }
    };

    timer2.schedule(KeyReinserter,0,60*60*1000);

    //on with other stuff

    getTofTree().initialize();

    // step through all messages on disk up to maxMessageDisplay and check if there are new messages
    // if a new message is in a folder, this folder is show yellow in tree
    TOF.initialSearchNewMessages(getTofTree(), frostSettings.getIntValue("maxMessageDisplay"));

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

    tofTextArea.setFont( tofTextArea.getFont().deriveFont(frostSettings.getFloatValue("tofFontSize")) );

    // Load table settings
    getDownloadTable().load();
    getUploadTable().load();

    // Start tofTree
    resendFailedMessages();
    timer.start();
    started = true;
    } // ************** end-of: jbInit()

    protected void loadIdentities()
    {
        File identities = new File("identities");
        //File contacts = new File("contacts");
        System.out.println("trying to create/load ids");
        try {
            if( identities.createNewFile() )
            {
                //create new identities
                try {
                    String nick = null;
                    do
                    {
                        nick = JOptionPane.showInputDialog("Choose an identity name, it doesn't have to be unique\n");
                    } while( nick == null || nick.length() == 0 );
                    mySelf = new LocalIdentity(nick);
                    //JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
                }
                catch( Exception e ) {
                    System.out.println("couldn't create new identitiy");
                    System.out.println(e.toString());
                }
                friends = new BuddyList();
                if( friends.Add(frame1.getMyId()) )
                {
                    System.out.println("added myself to list");
                }
                enemies = new BuddyList();
            }
            else
            {
                try {
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
                    while( !stop )
                    {
                        name = fin.readLine();
                        if( name.startsWith("***") ) break;
                        address = fin.readLine();
                        key = fin.readLine();
                        friends.Add(new Identity(name, address,key));
                    }
                    System.out.println("loaded " + friends.size() + " friends");

                    //and the enemies
                    enemies = new BuddyList();
                    System.out.println("loading enemies");
                    while( !stop )
                    {
                        name = fin.readLine();
                        if( name.startsWith("***") ) break;
                        address = fin.readLine();
                        key = fin.readLine();
                        enemies.Add(new Identity(name, address,key));
                    }
                    System.out.println("loaded " + enemies.size() + " enemies");

                }
                catch( IOException e ) {
                    System.out.println("IOException :" + e.toString());
                    friends = new BuddyList();
                    enemies = new BuddyList();
                    friends.Add(mySelf);
                }
                catch( Exception e ) {
                    System.out.println(e.toString());
                }
            }
        }
        catch( IOException e ) {
            System.out.println("couldn't create identities file");
        }
    }

    /**
     * Build ALL popup menus.
     * Should be called only once.
     */
    private void buildPopupMenus()
    {
        buildPopupMenuSearch();
        buildPopupMenuUpload();
        buildPopupMenuDownload();
        buildPopupMenuTofText();
        buildPopupMenuTofTree();
    }

    /**
     * Build the search table popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuSearch()
    {
// create objects
        searchPopupMenu = new JPopupMenu();
        searchPopupDownloadSelectedKeys = new JMenuItem(LangRes.getString("Download selected keys"));
        searchPopupDownloadAllKeys = new JMenuItem(LangRes.getString("Download all keys"));
        searchPopupCopyAttachment = new JMenuItem(LangRes.getString("Copy as attachment to clipboard"));
        searchPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        searchPopupDownloadSelectedKeys.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                addSelectedSearchItemsToDownloadTable();
            } });
        searchPopupDownloadAllKeys.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                searchTable.selectAll();
                addSelectedSearchItemsToDownloadTable();
            } });
        searchPopupCopyAttachment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String srcData = getSelectedSearchItemsAsAttachmentsString();
                Clipboard clipboard = getToolkit().getSystemClipboard();
                StringSelection contents = new StringSelection(srcData);
                clipboard.setContents(contents, frame1.this);
            } });
// construct menu
        searchPopupMenu.add(searchPopupDownloadSelectedKeys);
        searchPopupMenu.addSeparator();
        searchPopupMenu.add(searchPopupDownloadAllKeys);
        searchPopupMenu.addSeparator();
        searchPopupMenu.add(searchPopupCopyAttachment);
        searchPopupMenu.addSeparator();
        searchPopupMenu.add(searchPopupCancel);
    }

    /**
     * Build the upload table popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuUpload()
    {
// create objects
        uploadPopupMenu = new JPopupMenu();
//        uploadPopupMoveSelectedFilesUp = new JMenuItem(LangRes.getString("Move selected files up"));
//        uploadPopupMoveSelectedFilesDown = new JMenuItem(LangRes.getString("Move selected files down"));
        uploadPopupRemoveSelectedFiles = new JMenuItem(LangRes.getString("Remove selected files"));
        uploadPopupRemoveAllFiles = new JMenuItem(LangRes.getString("Remove all files"));
        uploadPopupReloadSelectedFiles = new JMenuItem(LangRes.getString("Reload selected files"));
        uploadPopupReloadAllFiles = new JMenuItem(LangRes.getString("Reload all files"));
        uploadPopupSetPrefixForSelectedFiles = new JMenuItem(LangRes.getString("Set prefix for selected files"));
        uploadPopupSetPrefixForAllFiles = new JMenuItem(LangRes.getString("Set prefix for all files"));
        uploadPopupRestoreDefaultFilenamesForSelectedFiles = new JMenuItem(LangRes.getString("Restore default filenames for selected files"));
        uploadPopupRestoreDefaultFilenamesForAllFiles = new JMenuItem(LangRes.getString("Restore default filenames for all files"));
        uploadPopupChangeDestinationBoard = new JMenu(LangRes.getString("Change destination board"));
        uploadPopupAddFilesToBoard = new JMenuItem(LangRes.getString("Add files to board"));
        uploadPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        // Upload / Move selected files up
/*        uploadPopupMoveSelectedFilesUp.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                TableFun.moveSelectedEntriesUp(getUploadTable());
            } });
        // Upload / Move selected files down
        uploadPopupMoveSelectedFilesDown.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                TableFun.moveSelectedEntriesDown(getUploadTable());
            } });
*/
        // Upload / Remove selected files
        uploadPopupRemoveSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().removeSelectedRows();
            } });
        // Upload / Remove all files
        uploadPopupRemoveAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                UploadTableModel model = (UploadTableModel)getUploadTable().getModel();
                model.clearDataModel();
            } });
        // Upload / Reload selected files
        uploadPopupReloadSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                UploadTableModel tableModel = (UploadTableModel)getUploadTable().getModel();
                synchronized (getUploadTable()) { try {
                int[] selectedRows = getUploadTable().getSelectedRows();
                for (int i = 0; i < selectedRows.length; i++){
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( selectedRows[i] );
                    // Since it is difficult to identify the states where we are allowed to
                    // start an upload we decide based on the states in which we are not allowed
                    if( !ulItem.getState().equals(LangRes.getString("Uploading")) &&
                        ulItem.getState().indexOf("Kb") == -1 )
                    {
                        ulItem.setState(LangRes.getString("Requested"));
                        tableModel.updateRow(ulItem);
                    }
                }
                }catch (Exception ex) {System.out.println("reload files NOT GOOD " +ex.toString());}
                }
            } });
        // Upload / Reload all files
        uploadPopupReloadAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                UploadTableModel tableModel = (UploadTableModel)getUploadTable().getModel();
                synchronized (getUploadTable()) { try {
                for (int i = 0; i < tableModel.getRowCount(); i++){
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( i );
                    ulItem.setState(LangRes.getString("Requested"));
                    tableModel.updateRow(ulItem);
                }
                }catch (Exception ex) {System.out.println("reload files NOT GOOD " +ex.toString());}
                }
            } });
        // Upload / Set Prefix for selected files
        uploadPopupSetPrefixForSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().setPrefixForSelectedFiles();
            } });
        // Upload / Set Prefix for all files
        uploadPopupSetPrefixForAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().selectAll();
                getUploadTable().setPrefixForSelectedFiles();
            } });
        // Upload / Restore default filenames for selected files
        uploadPopupRestoreDefaultFilenamesForSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().restoreOriginalFilenamesForSelectedRows();
            } });
        // Upload / Restore default filenames for all files
        uploadPopupRestoreDefaultFilenamesForAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().selectAll();
                getUploadTable().restoreOriginalFilenamesForSelectedRows();
            } });
        // Upload / Restore default filenames for all files
        uploadPopupAddFilesToBoard.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().addFilesToBoardIndex();
            } });
// construct menu
        uploadPopupMenu.add(uploadPopupRemoveSelectedFiles);
        uploadPopupMenu.add(uploadPopupRemoveAllFiles);
        uploadPopupMenu.addSeparator();
//        uploadPopupMenu.add(uploadPopupMoveSelectedFilesUp);
//        uploadPopupMenu.add(uploadPopupMoveSelectedFilesDown);
//        uploadPopupMenu.addSeparator();
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

    }

    /**
     * Build the download table popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuDownload()
    {
// construct objects
        downloadPopupMenu = new JPopupMenu(); // Downloads popup
        downloadPopupRemoveSelectedDownloads = new JMenuItem(LangRes.getString("Remove selected downloads"));
        downloadPopupRemoveAllDownloads = new JMenuItem(LangRes.getString("Remove all downloads"));
        downloadPopupResetHtlValues = new JMenuItem(LangRes.getString("Retry selected downloads"));
//        downloadPopupMoveUp = new JMenuItem(LangRes.getString("Move selected downloads up"));
//        downloadPopupMoveDown = new JMenuItem(LangRes.getString("Move selected downloads down"));

        // TODO: implement cancel of downloading
        downloadPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        downloadPopupRemoveSelectedDownloads.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().removeSelectedChunks();
                getDownloadTable().removeSelectedRows();
            } });
        downloadPopupRemoveAllDownloads.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().selectAll();
                getDownloadTable().removeSelectedChunks();
                DownloadTableModel model = (DownloadTableModel)getDownloadTable().getModel();
                model.clearDataModel();
            } });
/*        downloadPopupMoveUp.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                TableFun.moveSelectedEntriesUp(getDownloadTable());
            } });
        downloadPopupMoveDown.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                TableFun.moveSelectedEntriesDown(getDownloadTable());
            } });
*/
        downloadPopupResetHtlValues.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                // reset HTL for all selected items in download table
                DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();
                int[] selectedRows = getDownloadTable().getSelectedRows();
                for( int x=0; x<selectedRows.length; x++ )
                {
                    FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( selectedRows[x] );
                    dlItem.setHtl( frostSettings.getIntValue("htl"));
                    dlItem.setState(LangRes.getString("Waiting"));
                    dlModel.updateRow( dlItem );
                }
            } });
// construct menu
        downloadPopupMenu.add(downloadPopupRemoveSelectedDownloads);
        downloadPopupMenu.add(downloadPopupRemoveAllDownloads);
        downloadPopupMenu.addSeparator();
        downloadPopupMenu.add(downloadPopupResetHtlValues);
        downloadPopupMenu.addSeparator();
//        downloadPopupMenu.add(downloadPopupMoveUp);
//        downloadPopupMenu.add(downloadPopupMoveDown);
//        downloadPopupMenu.addSeparator();
        downloadPopupMenu.add(downloadPopupCancel);

    }

    /**
     * Build the tof text popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuTofText()
    {
// create objects
        tofTextPopupMenu = new JPopupMenu(); // TOF text popup
        tofTextPopupSaveMessage = new JMenuItem(LangRes.getString("Save message to disk"));
        tofTextPopupSaveAttachments = new JMenuItem(LangRes.getString("Download attachment(s)"));
        tofTextPopupSaveAttachment = new JMenuItem("Download selected attachment");
        tofTextPopupSaveBoards = new JMenuItem("Add board(s)");
        tofTextPopupSaveBoard = new JMenuItem("Add selected board");
        tofTextPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        tofTextPopupSaveMessage.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                FileAccess.saveDialog(getInstance(), getTofTextAreaText(), frostSettings.getValue("lastUsedDirectory"), LangRes.getString("Save message to disk"));
            } });
        tofTextPopupSaveAttachments.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadAttachments();
            } });
         tofTextPopupSaveAttachment.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadAttachments();
            } });
         tofTextPopupSaveBoards.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadBoards();
            } });
         tofTextPopupSaveBoard.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadBoards();
            } });
// construct menu
        tofTextPopupMenu.add(tofTextPopupSaveMessage);
        tofTextPopupMenu.addSeparator();
        tofTextPopupMenu.add(tofTextPopupSaveAttachments);
        tofTextPopupMenu.add(tofTextPopupSaveAttachment);
        tofTextPopupMenu.addSeparator();
        tofTextPopupMenu.add(tofTextPopupSaveBoards);
        tofTextPopupMenu.add(tofTextPopupSaveBoard);
        tofTextPopupMenu.addSeparator();
        tofTextPopupMenu.add(tofTextPopupCancel);

    }

    /**
     * Build the tof tree popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuTofTree()
    {
// create objects
        tofTreePopupMenu = new JPopupMenu(); // TOF tree popup
        tofTreePopupRefresh = new JMenuItem("Refresh board/folder");
        tofTreePopupAddNode = new JMenuItem("Add new board"); // TODO: translate
        tofTreePopupAddFolder = new JMenuItem("Add new folder");
        tofTreePopupRemoveNode = new JMenuItem(LangRes.getString("Remove selected board / folder"));
        tofTreePopupCutNode = new JMenuItem(LangRes.getString("Cut selected board / folder"));
        tofTreePopupPasteNode = new JMenuItem(LangRes.getString("Paste board / folder"));
        tofTreePopupConfigureBoard = new JMenuItem(LangRes.getString("Configure selected board"));
        tofTreePopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        tofTreePopupAddNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewBoard(frame1.getInstance());
            } });
        tofTreePopupAddFolder.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewFolder(frame1.getInstance());
            } });
        tofTreePopupRefresh.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                refreshNode( getActualNode() );
            } });
        tofTreePopupRemoveNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                removeSelectedNode();
            } });
        tofTreePopupCutNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                cutSelectedNode();
            } });
        tofTreePopupPasteNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                pasteFromClipboard();
            } });
        tofTreePopupConfigureBoard.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                tofConfigureBoardMenuItem_actionPerformed(e);
            } });

// construct menu
        tofTreePopupMenu.add(tofTreePopupRefresh);
        tofTreePopupMenu.addSeparator();
        tofTreePopupMenu.add(tofTreePopupAddNode);
        tofTreePopupMenu.add(tofTreePopupAddFolder);
        tofTreePopupMenu.add(tofTreePopupRemoveNode);
        tofTreePopupMenu.addSeparator();
        tofTreePopupMenu.add(tofTreePopupCutNode);
        tofTreePopupMenu.add(tofTreePopupPasteNode);
        tofTreePopupMenu.addSeparator();
        tofTreePopupMenu.add(tofTreePopupConfigureBoard);
        tofTreePopupMenu.addSeparator();
        tofTreePopupMenu.add(tofTreePopupCancel);
    }

    /**
     * Build the menu bar.
     * Should be called only once.
     */
    private void buildMenuBar()
    {
// create objects
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(LangRes.getString("File"));
        JMenuItem fileExitMenuItem = new JMenuItem(LangRes.getString("Exit"));

        JMenu tofMenu = new JMenu(LangRes.getString("News"));
        JMenuItem tofConfigureBoardMenuItem = new JMenuItem(LangRes.getString("Configure selected board"));
        JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem(LangRes.getString("Display board information window"));
        this.tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem(LangRes.getString("Automatic message update"), true);
        JMenuItem tofIncreaseFontSizeMenuItem = new JMenuItem (LangRes.getString ("Increase Font Size"));
        JMenuItem tofDecreaseFontSizeMenuItem = new JMenuItem (LangRes.getString ("Decrease Font Size"));

        JMenu optionsMenu = new JMenu(LangRes.getString("Options"));
        JMenuItem optionsPreferencesMenuItem = new JMenuItem(LangRes.getString("Preferences"));

        JMenu pluginMenu = new JMenu("Plugin");
        JMenuItem pluginBrowserMenuItem = new JMenuItem("EFB (Experimental Freenet Browser)");

        JMenu helpMenu = new JMenu(LangRes.getString("Help"));
        JMenuItem helpHelpMenuItem = new JMenuItem(LangRes.getString("Help"));
        JMenuItem helpAboutMenuItem = new JMenuItem(LangRes.getString("About"));
// add action listener
        fileExitMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                fileExitMenuItem_actionPerformed(e);
            } });
        optionsPreferencesMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                optionsPreferencesMenuItem_actionPerformed(e);
            } });
        tofIncreaseFontSizeMenuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                // make the font size in the TOF text area one point bigger
                Font f = tofTextArea.getFont();
                frostSettings.setValue("tofFontSize",f.getSize() + 1.0f);
                f = f.deriveFont(frostSettings.getFloatValue("tofFontSize"));
                tofTextArea.setFont(f);
            }});
        tofDecreaseFontSizeMenuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                // make the font size in the TOF text area one point smaller
                Font f = tofTextArea.getFont();
                frostSettings.setValue("tofFontSize",f.getSize () - 1.0f);
                f = f.deriveFont(frostSettings.getFloatValue("tofFontSize"));
                tofTextArea.setFont(f);
            }});
        tofConfigureBoardMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                tofConfigureBoardMenuItem_actionPerformed(e);
            } });
        tofDisplayBoardInfoMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                tofDisplayBoardInfoMenuItem_actionPerformed(e);
            } });
        pluginBrowserMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                BrowserFrame browser = new BrowserFrame(true);
                browser.show();
            } });
        helpHelpMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                HelpFrame dlg = new HelpFrame(getInstance());
                dlg.show();
            } });
        helpAboutMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                helpAboutMenuItem_actionPerformed(e);
            } });

// construct menu
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
        // add all to bar
        menuBar.add(fileMenu);
        menuBar.add(tofMenu);
        menuBar.add(optionsMenu);
        menuBar.add(pluginMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);
    }

    //------------------------------------------------------------------------

    public static void displayWarning(String message)
    {
        newMessageHeader = " " + message;
    }

    /**
     * Adds either the selected or all files from the attachmentTable to downloads table.
     */
    public void downloadAttachments()
    {
        int[] selectedRows = attachmentTable.getSelectedRows();

        // If no rows are selected, add all attachments to download table
        if( selectedRows.length == 0 )
        {
            for( int i = 0; i < getAttachmentTable().getModel().getRowCount(); i++ )
            {
                String filename = (String)getAttachmentTable().getModel().getValueAt(i, 0);
                String key = (String)getAttachmentTable().getModel().getValueAt(i, 1);
                FrostDownloadItemObject dlItem = new FrostDownloadItemObject( filename,
                                                                              key,
                                                                              getActualNode(),
                                                                              frostSettings.getIntValue("htl")
                                                                            );
                 boolean added = getDownloadTable().addDownloadItem( dlItem );
            }
        }
        else
        {
            for( int i = 0; i < selectedRows.length; i++ )
            {
                String filename = (String)getAttachmentTable().getModel().getValueAt(selectedRows[i],0);
                String key = (String)getAttachmentTable().getModel().getValueAt(selectedRows[i],1);
                FrostDownloadItemObject dlItem = new FrostDownloadItemObject( filename,
                                                                              key,
                                                                              getActualNode(),
                                                                              frostSettings.getIntValue("htl")
                                                                            );
                 boolean added = getDownloadTable().addDownloadItem( dlItem );
            }
        }
    }

    /**
     * Adds all boards from the attachedBoardsTable to board list.
     */
    private void downloadBoards()
    {
        System.out.println("adding boards");
        int[] selectedRows = getAttachedBoardsTable().getSelectedRows();

        if( selectedRows.length == 0 )
        {
            // add all rows
            getAttachedBoardsTable().selectAll();
            selectedRows = getAttachedBoardsTable().getSelectedRows();
            if( selectedRows.length == 0 )
                return;
        }
            for( int i = 0; i < selectedRows.length; i++ )
            {
                String name = (String)getAttachedBoardsTable().getModel().getValueAt(selectedRows[i], 0);
                String pubKey = (String)getAttachedBoardsTable().getModel().getValueAt(selectedRows[i], 1);
                String privKey = (String)getAttachedBoardsTable().getModel().getValueAt(selectedRows[i], 2);

            FrostBoardObject board = getTofTree().getBoardByName( name );

            //ask if we already have the board
            if( board != null )
            {
                if( JOptionPane.showConfirmDialog(this, "You already have a board named " + name + ".\n" +
                                                  "Are you sure you want to download this one over it?",
                                                  "Board already exists",
                                                  JOptionPane.YES_NO_OPTION) !=0 )
                    continue; // next row of table / next attached board
            }

            // prepare key vars for creation of FrostBoardObject (val=null if key is empty)
            if( privKey.compareTo("N/A") == 0 || privKey.length() == 0 )
            {
                privKey = null;
            }
            if( pubKey.compareTo("N/A") == 0 || pubKey.length() == 0 )
            {
                pubKey = null;
            }

            getTofTree().addNodeToTree( new FrostBoardObject(name, pubKey, privKey) );
        }
    }

    /**
     * Returns true if board is allowed to be updated.
     * Also checks if board update is already running.
     */
    public boolean doUpdate(FrostBoardObject board)
    {
        if( isUpdateAllowed(board) == false )
            return false;

        if( isUpdating(board) )
            return false;

        return true;
    }

    /**
     * Returns true if board is allowed to be updated.
     * Does NOT check if board update is already running.
     */
    public boolean isUpdateAllowed(FrostBoardObject board)
    {
        if( board == null )
            return false;
        // Do not allow folders to update
        if(board.isFolder())
            return false;

        if (board.isSpammed())
            return false;

        return true;
    }

    private boolean isThreadOfTypeRunning(FrostBoardObject board, int type)
    {
        Vector threads = getRunningBoardUpdateThreads().getDownloadThreadsForBoard(board);
        for( int x=0; x<threads.size(); x++ )
        {
            BoardUpdateThread thread = (BoardUpdateThread)threads.get(x);
            if( thread.getThreadType() == type )
                return true;
        }
        return false;
    }

    /**
     * Returns true if this board is currently running update threads.
     */
    public boolean isUpdating(FrostBoardObject board)
    {
        return getRunningBoardUpdateThreads().isUpdating(board);
    }

    /**tof / Update*/
    /**
     * Starts the board update threads, getRequest thread and update id thread.
     * Checks for each type of thread if its already running, and starts allowed
     * not-running threads for this board.
     */
    public void updateBoard(FrostBoardObject board)
    {
        if( board == null || board.isFolder() )
            return;

        boolean threadStarted = false;

        // first download the messages of today
        if( isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_TODAY) == false )
        {
            getRunningBoardUpdateThreads().startMessageDownloadToday(board, frostSettings, null);
            System.out.println("Starting update (MSG_TODAY) of " + board.toString());
            threadStarted = true;
        }

        // maybe get the files list
        if( !frostSettings.getBoolValue("disableRequests") &&
            !isThreadOfTypeRunning(board, BoardUpdateThread.BOARD_FILE_UPLOAD)
          )
        {
            getRunningBoardUpdateThreads().startBoardFilesUpload(board, frostSettings, null);
            System.out.println("Starting update (BOARD_UPLOAD) of " + board.toString());
            threadStarted = true;
        }

        if( !frostSettings.getBoolValue("disableDownloads") &&
            !isThreadOfTypeRunning(board, BoardUpdateThread.BOARD_FILE_DNLOAD)
          )
        {
            getRunningBoardUpdateThreads().startBoardFilesDownload(board, frostSettings, null);
            System.out.println("Starting update (BOARD_DOWNLOAD) of " + board.toString());
            threadStarted = true;
        }

        // finally get the older messages
        if( isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_BACK) == false )
        {
            getRunningBoardUpdateThreads().startMessageDownloadBack(board, frostSettings, null);
            System.out.println("Starting update (MSG_BACKLOAD) of " + board.toString());
            threadStarted = true;
        }

        // if there was a new thread started, update the lastUpdateStartTimeMillis
        if( threadStarted == true )
        {
            board.setLastUpdateStartMillis( System.currentTimeMillis() );
        }
    }

    public void updateTofTree(FrostBoardObject board)
    {
        // fire update for node
        DefaultTreeModel model = (DefaultTreeModel)getTofTree().getModel();
        model.nodeChanged( board );
        if( board == getActualNode() ) // is the board actually shown?
        {
            updateButtons(board);
        }
    }

    private void updateButtons(FrostBoardObject board)
    {
        if( board.isReadAccessBoard() )
        {
            tofNewMessageButton.setEnabled(false);
            uploadAddFilesButton.setEnabled(false);
        }
        else
        {
            tofNewMessageButton.setEnabled(true);
            uploadAddFilesButton.setEnabled(true);
        }
    }

    private class Truster extends Thread
    {
        private boolean trust;
        private Identity newFriend;
        private VerifyableMessageObject currentMsg;

        private void recursDir(String dirItem)
        {
            String list[];
            File file = new File(dirItem);
            if( file.isDirectory() && file.listFiles().length > 0 )
            {
                //System.out.println("\n"+file+":");
                Vector vd = new Vector();
                Vector vf = new Vector();
                list = file.list();
                Arrays.sort(list,String.CASE_INSENSITIVE_ORDER);

                for( int i = 0; i < list.length; i++ )
                {
                    File f = new File(dirItem + File.separatorChar + list[i]);
                    if( f.isDirectory() ) vd.add(list[i]);
                    else vf.add(list[i]);
                }
                for( int a=0; a < vf.size(); a++ )
                    recursDir (dirItem + File.separatorChar + vf.get(a));
                for( int d=0; d < vd.size(); d++ )
                    recursDir (dirItem + File.separatorChar + vd.get(d));
            }
            else
                processItem(dirItem);
            list=null;

        }

        private void processItem(String dirItem)
        {
            File f = new File(dirItem);
            FrostMessageObject temp;
            if( f.getPath().endsWith(".txt") )
            {//open file and check it
                temp = new FrostMessageObject(f);
                if( temp.getFrom().equals(currentMsg.getFrom()) &&
                    temp.getStatus().trim().equals(VerifyableMessageObject.PENDING) )
                    if( trust ) temp.setStatus(VerifyableMessageObject.VERIFIED);
                    else temp.setStatus(VerifyableMessageObject.FAILED);
            }
            f=null;
            temp =null;
        }

        public Truster(boolean what)
        {
            trust=what;
            try {
                currentMsg= selectedMessage.copy();
            }
            catch( Exception e ) {
                System.out.println(e.toString());
            }
        }

        public void run()
        {
            System.out.println("starting to update .sigs");
            newFriend = new Identity(currentMsg.getFrom(),currentMsg.getKeyAddress());
            if( trust ) friends.Add(newFriend);
            else enemies.Add(newFriend);

            recursDir("keypool");
            tofTree_actionPerformed(null);
        }
    }

    private class checkForSpam extends TimerTask
    {
        public void run()
        {
            if(frostSettings.getBoolValue("doBoardBackoff"))
            {
                Iterator iter = getTofTree().getAllBoards().iterator();
                while (iter.hasNext())
                {
                    FrostBoardObject current = (FrostBoardObject)iter.next();
                    if (current.getBlockedCount() > frostSettings.getIntValue("spamTreshold"))
                    {
                        //board is spammed
                        System.out.println("#########setting spam status############");
                        current.setSpammed(true);
                        // clear spam status in 24 hours
                        timer2.schedule(new ClearSpam(current),24*60*60*1000);

                        //now, kill all threads for board
                        Vector threads = getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
                        Iterator i = threads.iterator();
                        while( i.hasNext() )
                        {
                            BoardUpdateThread thread = (BoardUpdateThread)i.next();
                            while( thread.isInterrupted() == false )
                                thread.interrupt();
                        }
                    }
                    current.resetBlocked();
                }
            }
        }
    }

    private class ClearSpam extends TimerTask
    {
        private FrostBoardObject clearMe;

        public ClearSpam(FrostBoardObject which) { clearMe = which; }
        public void run()
        {
            System.out.println("############clearing spam status for board "+clearMe.toString()+" ###########");
            clearMe.setSpammed(false);
        }
    }

    /**TOF Board selected*/
    public void tofTree_actionPerformed(TreeSelectionEvent e)
    {
        int i[] = getTofTree().getSelectionRows();
        if( i != null && i.length > 0 )
        {
            frostSettings.setValue("tofTreeSelectedRow", i[0]);
        }

        FrostBoardObject node = (FrostBoardObject)getTofTree().getLastSelectedPathComponent();

        resetMessageViewSplitPanes(); // clear message view

        if(node != null)
        {
            if(node.isFolder() == false)
            {
                // node is a board
                configBoardButton.setEnabled(true);
                tofNewMessageButton.setEnabled(true);
                tofUpdateButton.setEnabled(true);

                saveMessageButton.setEnabled(false);

                updateButtons(node);

                System.out.println( "Board "+node.toString()+" blocked count: "+node.getBlockedCount() );

                tofReplyButton.setEnabled(false);
                downloadAttachmentsButton.setEnabled(false);
                downloadBoardsButton.setEnabled(false);

                // read all messages for this board into message table
                TOF.updateTofTable(node, keypool, frostSettings.getIntValue("maxMessageDisplay"));
                messageTable.clearSelection();
            }
            else
            {
                // node is a folder
                DefaultTableModel model = (DefaultTableModel)getMessageTable().getModel();
                model.setRowCount( 0 );
                updateMessageCountLabels( node );

                configBoardButton.setEnabled(false);
                tofNewMessageButton.setEnabled(false);
                tofUpdateButton.setEnabled(false);
            }
        }
    }

    /**
     * starts update for the selected board, or for all childs (and their childs) of a folder
     */
    private void refreshNode(FrostBoardObject node)
    {
        if( node==null )
            return;

        if( node.isFolder() == false )
        {
            if( isUpdateAllowed( node ) )
            {
                updateBoard(node);
            }
        }
        else
        {
            // update all childs recursiv
            Enumeration leafs = node.children();
            while( leafs.hasMoreElements() ) refreshNode((FrostBoardObject)leafs.nextElement());
        }
    }

    public void removeSelectedNode()
    {
        getTofTree().removeSelectedNode();
    }

    /**
     * Opens dialog to rename the board / folder.
     * For boards it checks for double names.
     */
    public void renameSelectedNode()
    {
        // getTofTree().startEditingAtPath(getTofTree().getSelectionPath());
        FrostBoardObject selected = getActualNode();
        if( selected == null )
            return;
        String newname = null;
        do
        {
            newname = JOptionPane.showInputDialog(this,
                                                  "Please enter the new name:\n",
                                                  selected.toString() );
            if( newname == null )
                return; // cancel
            if( selected.isFolder() == false && // double folder names are ok
                getTofTree().getBoardByName( newname ) != null )
            {
                JOptionPane.showMessageDialog(this, "You already have a board with name '"+newname+"'!\nPlease choose a new name.");
                newname = ""; // loop again
            }
        } while( newname.length()==0 );

        selected.setBoardName(newname);
        ((DefaultTreeModel)getTofTree().getModel()).nodeChanged( selected );
    }

    public void pasteFromClipboard()
    {
        if( clipboard == null )
        {
            pasteBoardButton.setEnabled(false);
            return;
        }

        if( getTofTree().pasteFromClipboard(clipboard) == true )
        {
            clipboard = null;
            pasteBoardButton.setEnabled(false);
        }
    }

    public void cutSelectedNode() {
        FrostBoardObject cuttedNode = getTofTree().cutSelectedNode();
        if( cuttedNode != null )
        {
            clipboard = cuttedNode;
            pasteBoardButton.setEnabled(true);
        }
    }

    /**Get keyTyped for tofTree*/
    public void tofTree_keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (!getTofTree().isEditing())
    {
        if (key == KeyEvent.VK_DELETE)
            removeSelectedNode();
        if (key == KeyEvent.VK_N)
            getTofTree().createNewBoard(frame1.getInstance());
        if (key == KeyEvent.VK_X)
            cutSelectedNode();
        if (key == KeyEvent.VK_V)
            pasteFromClipboard();
    }
    }

    /**Get keyTyped for downloadTable*/
    public void downloadTable_keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (key == KeyEvent.VK_DELETE && !getDownloadTable().isEditing()) {
        getDownloadTable().removeSelectedChunks();
        getDownloadTable().removeSelectedRows();
    }
    }

    /**valueChanged messageTable (messageTableListModel / TOF)*/
    public void messageTableListModel_valueChanged(ListSelectionEvent e)
    {
        FrostBoardObject selectedBoard = getActualNode();
        if( selectedBoard.isFolder() )
            return;
        selectedMessage = TOF.evalSelection(e, messageTable, selectedBoard);
        if( selectedMessage != null )
        {
            displayNewMessageIcon(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);

            lastSelectedMessage = selectedMessage.getSubject();
            if( selectedBoard.isReadAccessBoard() == false )
            {
                tofReplyButton.setEnabled(true);
            }

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
            int boardPartLength = bend - bstart; // must be at least 14, 1 char boardname, 2 times " * " and keys="N/A"

// TODO: check for validness of found , e.g. format for boards is:
//  "<board>mp3ogg * SSK@PJONkPZC6a4EuhHE~dP0nYyWK-oPAgM * N/A</board>"

            if( ( start ==-1 || end == -1) && (bstart == -1 || bend ==-1 || boardPartLength < 14) )
            {
                // Move divider to 100% and make it invisible
                attachmentSplitPane.setDividerSize(0);
                attachmentSplitPane.setDividerLocation(1.0);
                boardSplitPane.setDividerSize(0);
                boardSplitPane.setDividerLocation(1.0);

                setTofTextAreaText(selectedMessage.getPlaintext());
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
                    ((DefaultTableModel)getAttachmentTable().getModel()).setDataVector(selectedMessage.getAttachments(),
                                                                                       null); // our model does not need this
                    setTofTextAreaText(selectedMessage.getPlaintext());  //was getNewContent()
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
                    ((DefaultTableModel)getAttachedBoardsTable().getModel()).setDataVector(selectedMessage.getBoards(),
                                                                                           null);
                    setTofTextAreaText(selectedMessage.getPlaintext());  //was getNewContent()
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
            // no msg selected
            resetMessageViewSplitPanes(); // clear message view
            tofReplyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);
        }
    }

    /**Selects message icon in lower right corner*/
    public static void displayNewMessageIcon(boolean showNewMessageIcon)
    {
        frame1 frame1inst = frame1.getInstance();
        if( showNewMessageIcon )
        {
            frame1inst.setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/newmessage.gif")));
            frame1inst.statusMessageLabel.setIcon(newMessage[0]);
        }
        else
        {
            frame1inst.setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
            frame1inst.statusMessageLabel.setIcon(newMessage[1]);
        }
    }

    /**
     * Used to sort FrostBoardObjects by lastUpdateStartMillis ascending.
     */
    static final Comparator lastUpdateStartMillisCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
        FrostBoardObject value1 = (FrostBoardObject)o1;
        FrostBoardObject value2 = (FrostBoardObject)o2;
        if( value1.getLastUpdateStartMillis() > value2.getLastUpdateStartMillis() )
            return 1;
        else
            return -1;
        }
    };

    /**
     * Chooses the next FrostBoard to update (automatic update).
     * First sorts by lastUpdateStarted time, then chooses first board
     * that is allowed to update.
     * Used only for automatic updating.
     * Returns NULL if no board to update is found.
     */
    public FrostBoardObject selectNextBoard(Vector boards)
    {
        Collections.sort(boards, lastUpdateStartMillisCmp);
        // now first board in list should be the one with latest update of all
        FrostBoardObject board;
        FrostBoardObject nextBoard = null;

        long curTime = System.currentTimeMillis();
        // get in minutes
        int minUpdateInterval = frostSettings.getIntValue("automaticUpdate.boardsMinimumUpdateInterval");
        // min -> ms
        long minUpdateIntervalMillis = minUpdateInterval * 60 * 1000;

        for (int i = 0; i < boards.size(); i++)
        {
            board = (FrostBoardObject)boards.get(i);
            if( nextBoard == null &&
                doUpdate(board) &&
                (curTime - minUpdateIntervalMillis) > board.getLastUpdateStartMillis() // minInterval
              )
            {
                nextBoard = board;
                break;
            }
        }
        System.out.println("*****************************************");
        if( nextBoard != null )
            System.out.println("Automatic board update - starting update for: "+nextBoard.toString());
        else
            System.out.println("Automatic board update - no board to update");
        System.out.println("*****************************************");
        return nextBoard;
    }

    /**timer Action Listener (automatic download)*/
    private void timer_actionPerformed()
    {
        counter++;

        // Display welcome message if no boards are available
        if( ((TreeNode)getTofTree().getModel().getRoot()).getChildCount() == 0 )
        {
            attachmentSplitPane.setDividerSize(0);
            attachmentSplitPane.setDividerLocation(1.0);
            setTofTextAreaText(LangRes.getString("Welcome message"));
        }

        if( counter%180 == 0 ) // Check uploadTable every 3 minutes
        {
            getUploadTable().removeNotExistingFiles();
        }

        if( counter%300 == 0 && frostSettings.getBoolValue("removeFinishedDownloads") )
        {
            getDownloadTable().removeFinishedDownloads();
        }

        if( updateDownloads || counter%10 == 0 )
        {
            getDownloadTable().update( frostSettings );
            updateDownloads = false;

            // Sometimes it seems that download table entries do not get reset to "Failed"
            // I did not find the bug yet, but if there is no download thread active
            // all unfinished downloads are set to "Waiting"
            if( activeDownloadThreads == 0 && downloadActivateCheckBox.isSelected() )
            {
                DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();
                for( int i = 0; i < dlModel.getRowCount(); i++ )
                {
                    FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( i );
                    if( dlItem.getState().equals(LangRes.getString("Done")) == false )
                    {
                        dlItem.setState(LangRes.getString("Waiting"));
                        dlModel.updateRow( dlItem );
                    }
                }
            }
        }

        //////////////////////////////////////////////////
        //   Automatic TOF update
        //////////////////////////////////////////////////
        if( counter % 3 == 0 && // check all 3 seconds if a board update could be started
            tofAutomaticUpdateMenuItem.isSelected() &&
            getRunningBoardUpdateThreads().getUpdatingBoardCount() <
                    frostSettings.getIntValue("automaticUpdate.concurrentBoardUpdates")
          )
        {
            Vector boards = getTofTree().getAllBoards();
            if( boards.size() > 0 )
            {
                FrostBoardObject actualBoard = selectNextBoard(boards);
                if( actualBoard != null )
                {
                    updateBoard(actualBoard);
                }
            }
        }

        //////////////////////////////////////////////////
        //   Display time in button bar
        //////////////////////////////////////////////////
        timeLabel.setText( new StringBuffer().append(DateFun.getExtendedDate())
                           .append(" - ").append(DateFun.getFullExtendedTime()).append(" GMT").toString());

        /////////////////////////////////////////////////
        //   Update status bar
        /////////////////////////////////////////////////
        String newText = new StringBuffer()
                 .append(LangRes.getString("Up: ")).append(activeUploadThreads)
                 .append(LangRes.getString("   Down: ")).append(activeDownloadThreads)

                 .append(LangRes.getString("   TOFUP: "))
                 .append(getRunningBoardUpdateThreads().getUploadingBoardCount() )
                 .append("B / ")
                 .append(getRunningBoardUpdateThreads().getRunningUploadThreadCount() )
                 .append("T")

                 .append(LangRes.getString("   TOFDO: "))
                 .append(getRunningBoardUpdateThreads().getUpdatingBoardCount() )
                 .append("B / ")
                 .append( getRunningBoardUpdateThreads().getRunningDownloadThreadCount() )
                 .append("T")

                 .append(LangRes.getString("   Selected board: ")).append(getActualNode().toString())
                 .toString();
        statusLabel.setText(newText);

        //////////////////////////////////////////////////
        // Generate CHK's for upload table entries
        //////////////////////////////////////////////////
        if( !generateCHK )
        {
            UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
            if( ulModel.getRowCount() > 0 )
            {
                for( int i = 0; i < ulModel.getRowCount(); i++ )
                {
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( i );
                    if( ulItem.getKey() == null || ulItem.getKey().equals(LangRes.getString("Unknown")) )
                    {
                        ulItem.setKey( "Working..." );
                        ulModel.updateRow( ulItem );
                        insertThread newInsert = new insertThread(ulItem, frostSettings, false);
                        newInsert.start();
                        break; // start only 1 thread per loop (=second)
                    }
                }
            }
        }

        //////////////////////////////////////////////////
        // Start upload thread
        //////////////////////////////////////////////////
        int activeUthreads = 0;
        synchronized(threadCountLock)
        {
            activeUthreads=activeUploadThreads;
        }
        if( activeUthreads < frostSettings.getIntValue("uploadThreads") )
        {
            UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
            if( ulModel.getRowCount() > 0 )
            {
                for( int i = 0; i < ulModel.getRowCount(); i++ )
                {
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( i );
                    if( ulItem.getState().equals(LangRes.getString("Requested")) &&
                        ulItem.getKey().startsWith("CHK@")
                      )
                    {
                        ulItem.setState( LangRes.getString("Uploading") );
                        ulModel.updateRow( ulItem );
                        insertThread newInsert = new insertThread(ulItem, frostSettings, true);
                        newInsert.start();
                        break; // start only 1 thread per loop (=second)
                    }
                }
            }
        }

        //////////////////////////////////////////////////
        // Start download thread
        //////////////////////////////////////////////////
        int activeDthreads = 0;
        synchronized(threadCountLock) {
            activeDthreads=activeDownloadThreads;
        }
        if( activeDthreads < frostSettings.getIntValue("downloadThreads") &&
            downloadActivateCheckBox.isSelected() )
        {
            DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();

            // get the item with state "Waiting", minimum htl and not over maximum htl
            ArrayList waitingItems = new ArrayList();
            for( int i = 0; i < dlModel.getRowCount(); i++ )
            {
                FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( i );
                if( dlItem.getState().equals(LangRes.getString("Waiting")) )
                {
                    if( dlItem.getHtl().intValue() <= frostSettings.getIntValue("htlMax") )
                    {
                        waitingItems.add( dlItem );
                    }
                    else
                    {
                        // set item to failed state
                        dlItem.setState( LangRes.getString("Failed") );
                        dlModel.updateRow( dlItem );
                    }
                }
            }
            if( waitingItems.size() > 0 )
            {
                // sort waiting items by htl ascending
                Collections.sort( waitingItems, downloadHtlCmp );
                // choose first item
                FrostDownloadItemObject dlItem = (FrostDownloadItemObject)waitingItems.get(0);
                dlItem.setState( LangRes.getString("Trying") );
                dlModel.updateRow( dlItem );

                requestThread newRequest = new requestThread( dlItem, getDownloadTable() );
                newRequest.start();
            }
        }
    }

    /**
     * Used to sort FrostDownloadItemObjects by htl ascending.
     */
    static final Comparator downloadHtlCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
        FrostDownloadItemObject value1 = (FrostDownloadItemObject)o1;
        FrostDownloadItemObject value2 = (FrostDownloadItemObject)o2;
        if( value1.getHtl().intValue() > value2.getHtl().intValue() )
            return 1;
        else
            return -1;
        }
    };

    /**searchTextField Action Listener (search)*/
    private void searchTextField_actionPerformed(ActionEvent e)
    {
        if( searchButton.isEnabled() )
            searchButton_actionPerformed(e);
    }

    /**downloadTextField Action Listener (Download/Quickload)*/
    private void downloadTextField_actionPerformed(ActionEvent e)
    {
        String key = (downloadTextField.getText()).trim();
        if( key.length() > 0 )
        {
            // strip the 'freenet:' prefix
            if( key.indexOf("freenet:") == 0 )
            {
                key = key.substring(8);
            }

            String validkeys[]={"SSK@", "CHK@", "KSK@"};
            boolean valid=false;

            for( int i = 0; i < validkeys.length; i++ )
            {
                if( key.substring(0, validkeys[i].length()).equals(validkeys[i]) )
                    valid=true;
            }

            if( valid )
            {
                // added a way to specify a file name. The filename is preceeded by a colon.
                String fileName;

                int sepIndex = key.lastIndexOf(":");

                if( sepIndex != -1 )
                {
                    fileName = key.substring(sepIndex + 1);
                    key = key.substring(0, sepIndex);
                }
                // take the filename from the last part the SSK or KSK
                else if( -1 != (sepIndex = key.lastIndexOf("/")) )
                {
                    fileName = key.substring(sepIndex + 1);
                }
                else
                {
                    fileName = key.substring(4);
                }
                // add valid key to download table
                FrostDownloadItemObject dlItem = new FrostDownloadItemObject( mixed.makeFilename(fileName),
                                                                              key,
                                                                              getActualNode(),
                                                                              frostSettings.getIntValue("htl")
                                                                            );
                boolean isAdded = getDownloadTable().addDownloadItem( dlItem );
            }
            else
            {
                // show messagebox that key is invalid
                String keylist = "";
                for( int i=0; i < validkeys.length; i++ )
                {
                    if( i > 0 )
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
    private void searchButton_actionPerformed(ActionEvent e)
    {
        searchButton.setEnabled(false);
        getSearchTable().clearSelection();
        ((SearchTableModel)getSearchTable().getModel()).clearDataModel();
        Vector boardsToSearch;
        if( searchAllBoardsCheckBox.isSelected() )
        {
            // search in all boards
            boardsToSearch = getTofTree().getAllBoards();
        }
        else
        {
            boardsToSearch = new Vector();
            boardsToSearch.add( getActualNode() );
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
    private void tofNewMessageButton_actionPerformed(ActionEvent e)
    {
        String subject = "No subject";

        if( frostSettings.getBoolValue("useAltEdit") )
        {
            // TODO: pass FrostBoardObject
            altEdit = new AltEdit(getActualNode(),
                                  frostSettings.getValue("userName"),
                                  subject, // subject
                                  "", // new msg
                                  frostSettings,
                                  this);
            altEdit.start();
        }
        else
        {
            MessageFrame newMessage = new MessageFrame(getActualNode(),
                                                       frostSettings.getValue("userName"),
                                                       subject, // subject
                                                       "",
                                                       frostSettings,
                                                       this);
            newMessage.show();
        }
    }

    /**tofReplyButton Action Listener (tof/Reply)*/
    private void tofReplyButton_actionPerformed(ActionEvent e)
    {
        String subject = lastSelectedMessage;

        if( subject.startsWith("Re:") == false )
            subject = "Re: " + subject;

        if( frostSettings.getBoolValue("useAltEdit") )
        {
            altEdit = new AltEdit(getActualNode(),
                                  frostSettings.getValue("userName"),
                                  subject, // subject
                                  getTofTextAreaText(),
                                  frostSettings,
                                  this);
            altEdit.start();
        }
        else
        {
            MessageFrame newMessage = new MessageFrame(getActualNode(),
                                                       frostSettings.getValue("userName"),
                                                       subject, // subject
                                                       getTofTextAreaText(),
                                                       frostSettings,
                                                       this);
            newMessage.show();
        }
    }

    private void tofDisplayBoardInfoMenuItem_actionPerformed(ActionEvent e)
    {
        if( BoardInfoFrame.isDialogShowing() == false )
        {
            BoardInfoFrame boardInfo = new BoardInfoFrame(this);
            boardInfo.startDialog();
        }
    }

    //------------------------------------------------------------------------

    private void uploadAddFilesButton_actionPerformed(ActionEvent e)
    {
        FrostBoardObject board = getActualNode();
        if( board.isFolder() )
            return;

        final JFileChooser fc = new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
        fc.setDialogTitle("Select files you want to upload to the " + board.toString() + " board.");
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setPreferredSize(new Dimension(600, 400));

        int returnVal = fc.showOpenDialog(frame1.this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File file = fc.getSelectedFile();
            if( file != null )
            {
                frostSettings.setValue("lastUsedDirectory", file.getParent());
                File[] selectedFiles = fc.getSelectedFiles();

                for( int i = 0; i < selectedFiles.length; i++ )
                {
                    // collect all choosed files + files in all choosed directories
                    Vector allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
                    for( int j = 0; j < allFiles.size(); j++ )
                    {
                        File newFile = (File)allFiles.elementAt(j);
                        if( newFile.isFile() )
                        {
                            FrostUploadItemObject ulItem = new FrostUploadItemObject( newFile, board );
                            boolean isAdded = getUploadTable().addUploadItem( ulItem );
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
    private void tofConfigureBoardMenuItem_actionPerformed(ActionEvent e)
    {
        FrostBoardObject board = getActualNode();
        if( board.isFolder() )
            return;

        BoardSettingsFrame newFrame = new BoardSettingsFrame(this, board);
        if( newFrame.runDialog() == true ) // OK pressed?
        {
            updateTofTree(board);
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
        updateOptionsAffectedComponents();
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
    private void saveOnExit()
    {
        System.out.println("Saving settings ...");
        saveSettings();
        getDownloadTable().save();
        getUploadTable().save();
        System.out.println("Bye!");
    }

    /**Save settings*/
    private void saveSettings()
    {
        frostSettings.setValue("downloadingActivated", downloadActivateCheckBox.isSelected());
        //      frostSettings.setValue("uploadingActivated", uploadActivateCheckBox.isSelected());
        frostSettings.setValue("searchAllBoards", searchAllBoardsCheckBox.isSelected());
        //      frostSettings.setValue("reducedBlockCheck", reducedBlockCheckCheckBox.isSelected());
        frostSettings.setValue("automaticUpdate", tofAutomaticUpdateMenuItem.isSelected());
        frostSettings.writeSettingsFile();
        getTofTree().saveTree();
    }

    /**Load Settings*/
    private void loadSettings()
    {
        downloadActivateCheckBox.setSelected(frostSettings.getBoolValue("downloadingActivated"));
        //      uploadActivateCheckBox.setSelected(frostSettings.getBoolValue("uploadingActivated"));
        searchAllBoardsCheckBox.setSelected(frostSettings.getBoolValue("searchAllBoards"));
        //      reducedBlockCheckCheckBox.setSelected(frostSettings.getBoolValue("reducedBlockCheck"));
        tofAutomaticUpdateMenuItem.setSelected(frostSettings.getBoolValue("automaticUpdate"));
    }

    class PopupListener extends MouseAdapter {
    public void mousePressed(MouseEvent e)
    {
        if( e.getClickCount() == 2 )
        {

            // Start file from download table
            if( e.getComponent().equals(getDownloadTable()) )
            {
                DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();
                FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( getDownloadTable().getSelectedRow() );
                String execFilename = new StringBuffer()
                                            .append(System.getProperty("user.dir") )
                                            .append(fileSeparator)
                                            .append(frostSettings.getValue("downloadDirectory"))
                                            .append(dlItem.getFileName()).toString();
                File file = new File( execFilename );
                System.out.println("Executing: " + file.getPath());
                if( file.exists() )
                {
                    Execute.run("exec.bat" + " " + file.getPath());
                }
            }

            // Start file from upload table
            if( e.getComponent().equals(getUploadTable()) )
            {
                UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
                FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( getUploadTable().getSelectedRow() );
                File file = new File( ulItem.getFilePath() );
                System.out.println("Executing: "+file.getPath());
                if( file.exists() )
                {
                    Execute.run("exec.bat" + " " + file.getPath());
                }
            }

            // Add search result to download table
            if( e.getComponent().equals(searchTable) )
            {
                addSelectedSearchItemsToDownloadTable();
            }

        }
        else
        {
            maybeShowPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if( e.isPopupTrigger() == false )
        {
            return;
        }

        if (e.getComponent().equals(getUploadTable())) { // Upload Popup

            // Add boards to changeDestinationBoard submenu
            Vector boards = getTofTree().getAllBoards();
            Collections.sort(boards);
            uploadPopupChangeDestinationBoard.removeAll();
            for (int i = 0; i < boards.size(); i++)
            {
                final FrostBoardObject aBoard = (FrostBoardObject)boards.elementAt(i);
                JMenuItem boardMenuItem = new JMenuItem(aBoard.toString());
                uploadPopupChangeDestinationBoard.add(boardMenuItem);
                // add all boards to menu + set action listener for each board menu item
                boardMenuItem.addActionListener(new ActionListener()  {
                    public void actionPerformed(ActionEvent e) {
                        // set new board for all selected rows
                        UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
                        int[] selectedRows = getUploadTable().getSelectedRows();
                        for( int x=0; x<selectedRows.length; x++ )
                        {
                            FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( selectedRows[x] );
                            ulItem.setTargetBoard( aBoard );
                            ulModel.updateRow( ulItem );
                        }
                    } });
            }

            if (getUploadTable().getSelectedRow() == -1) {
                uploadPopupRemoveSelectedFiles.setEnabled(false);
                uploadPopupReloadSelectedFiles.setEnabled(false);
//                uploadPopupMoveSelectedFilesUp.setEnabled(false);
//                uploadPopupMoveSelectedFilesDown.setEnabled(false);
                uploadPopupSetPrefixForSelectedFiles.setEnabled(false);
                uploadPopupRestoreDefaultFilenamesForSelectedFiles.setEnabled(false);
                uploadPopupChangeDestinationBoard.setEnabled(false);
            }
            else {
                uploadPopupRemoveSelectedFiles.setEnabled(true);
//                uploadPopupMoveSelectedFilesUp.setEnabled(true);
                uploadPopupReloadSelectedFiles.setEnabled(true);
//                uploadPopupMoveSelectedFilesDown.setEnabled(true);
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

        if (e.getComponent().equals(getDownloadTable())) { // Downloads Popup
            if (getDownloadTable().getSelectedRow() == -1) {
            downloadPopupRemoveSelectedDownloads.setEnabled(false);
//            downloadPopupMoveUp.setEnabled(false);
//            downloadPopupMoveDown.setEnabled(false);
            downloadPopupResetHtlValues.setEnabled(false);
            }
            else {
            downloadPopupRemoveSelectedDownloads.setEnabled(true);
//            downloadPopupMoveUp.setEnabled(true);
//            downloadPopupMoveDown.setEnabled(true);
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
                tofTextPopupSaveBoards.setEnabled(false);
*/
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
            tofTreePopupPasteNode.setEnabled(false);
            if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if (parent != null) {
                tofTreePopupRemoveNode.setEnabled(true);
                tofTreePopupCutNode.setEnabled(true);
                if (node.isLeaf())
                    tofTreePopupConfigureBoard.setEnabled(true);
            }
            }
            if( clipboard != null )
                tofTreePopupPasteNode.setEnabled(true);

            tofTreePopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    //System.out.println("Clipboard contents replaced");
    }

    // methods that update the Msg and New counts for tof table
    // expects that the boards messages are shown in table
    public void updateMessageCountLabels(FrostBoardObject board)
    {
        if( board.isFolder() == true )
        {
            allMessagesCountLabel.setText("");
            newMessagesCountLabel.setText("");
        }
        else
        {
            DefaultTableModel model = (DefaultTableModel)messageTable.getModel();

            int allMessages = model.getRowCount();
            allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

            int newMessages=board.getNewMessageCount();
            newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
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

    private void trustButton_actionPerformed(ActionEvent e)
    {
        trustButton.setEnabled(false);
        notTrustButton.setEnabled(false);
        if( selectedMessage!=null )
        {
            if( enemies.containsKey(selectedMessage.getFrom()) )
            {
                if( JOptionPane.showConfirmDialog(getInstance(),
                              "are you sure you want to grant trust to user " +
                              selectedMessage.getFrom().substring(0,selectedMessage.getFrom().indexOf("@")) +
                              " ? \n If you choose yes, future messages from this user will be marked GOOD",
                              "re-grant trust",
                              JOptionPane.YES_NO_OPTION) ==0 )
                {
                    Identity x = enemies.Get(selectedMessage.getFrom());
                    enemies.remove(selectedMessage.getFrom());
                    friends.Add(x);
                }
            }
            else
            {
                Truster truster = new Truster(true);
                truster.start();
            }
        }
    }

    private void notTrustButton_actionPerformed(ActionEvent e)
    {
        trustButton.setEnabled(false);
        notTrustButton.setEnabled(false);
        if( selectedMessage!=null )
        {
            if( friends.containsKey(selectedMessage.getFrom()) )
            {
                if( JOptionPane.showConfirmDialog(getInstance(),
                          "are you sure you want to revoke trust to user " +
                          selectedMessage.getFrom().substring(0,selectedMessage.getFrom().indexOf("@")) +
                          " ? \n If you choose yes, future messages from this user will be marked BAD",
                          "revoke trust",
                          JOptionPane.YES_NO_OPTION) ==0 )
                {
                    Identity x = friends.Get(selectedMessage.getFrom());
                    friends.remove(selectedMessage.getFrom());
                    enemies.Add(x);
                }
            }
            else
            {
                Truster truster = new Truster(false);
                truster.start();
            }
        }
    }

    /**
     * Tries to send old messages that have not been sent yet
     */
    protected void resendFailedMessages()
    {
        // start a thread that waits some seconds for gui to appear, then searches for
        // unsent messages
        ResendFailedMessagesThread t = new ResendFailedMessagesThread(this);
        t.start();
    }

    class ResendFailedMessagesThread extends Thread
    {
        Frame frameToLock;
        public ResendFailedMessagesThread(Frame frameToLock)
        {
            this.frameToLock = frameToLock;
        }
        public void run()
        {
            // give gui a chance to appear ... then start searching for unsent messages
            try { Thread.sleep(10000); } // wait 10 seconds
            catch(InterruptedException ex) { ; }
            if( isInterrupted() )
                return;

            Vector entries = FileAccess.getAllEntries(new File(frostSettings.getValue("keypool.dir")), ".txt");

            if( isInterrupted() )
                return;

            for( int i = 0; i < entries.size(); i++ )
            {
                if( ((File)entries.elementAt(i)).getName().startsWith("unsent") )
                {
                    File unsentMsgFile = (File)entries.elementAt(i);
                    // Resend message
                    VerifyableMessageObject mo = new VerifyableMessageObject(unsentMsgFile);
                    if( mo.isValid() )
                    {
                        FrostBoardObject board = getTofTree().getBoardByName( mo.getBoard() );
                        if( board == null )
                        {
                            System.out.println("Can't resend Message '"+mo.getSubject()+"', the target board '"+mo.getBoard()+
                                               "' was not found in your boardlist.");
                            // TODO: maybe delete msg? or it will always be retried to send
                            continue;
                        }
                        getRunningBoardUpdateThreads().startMessageUpload(
                            board,
                            mo.getFrom(),
                            mo.getSubject(),
                            mo.getContent(),
                            mo.getDate(),
                            mo.getTime(),
                            "",
                            frostSettings,
                            frameToLock,
                            null);
                        System.out.println("Message '" + mo.getSubject() + "' will be resent to board '"+board.toString()+"'.");
                    }
                    // check if upload was successful before deleting the file -
                    // is not needed, the upload thread creates new unsent file
                    unsentMsgFile.delete();
                }
            }
        }
    }

    /**
     * Called after the OptionsFrame changed some settings to reflect
     * the new settings in the GUI.
     *
     * E.g. if downloads are disabled, it removes the tabbed panes
     * 'Search' and 'Downloads'
     */
    protected void updateOptionsAffectedComponents()
    {
        if( frostSettings.getBoolValue("disableDownloads") == false )
        {
            // search + downloads enabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Search")), true );
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Downloads")), true );
        }
        else
        {
            // search + downloads disabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Search")), false );
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Downloads")), false );
        }

        if( frostSettings.getBoolValue("disableRequests") == false )
        {
            // uploads enabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Uploads")), true );
        }
        else
        {
            // uploads disabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Uploads")), false );
        }
    }

    /**
     * Adds all selected items in searchtable to download table.
     */
    protected void addSelectedSearchItemsToDownloadTable()
    {
        SearchTableModel searchTableModel = (SearchTableModel)getSearchTable().getModel();
        int[] selectedRows = searchTable.getSelectedRows();

        for (int i = 0; i < selectedRows.length; i++)
        {
            FrostSearchItem searchItem = (FrostSearchItem)searchTableModel.getRow( selectedRows[i] );
            FrostDownloadItemObject dlItem = new FrostDownloadItemObject(searchItem, frostSettings.getIntValue("htl"));

            boolean isAdded = getDownloadTable().addDownloadItem( dlItem ); // will not add if item is already in table
        }
    }

    /**
     * Builds a String with contains all selected files from searchtable as attachements.
     */
    public String getSelectedSearchItemsAsAttachmentsString()
    {
        SearchTableModel searchTableModel = (SearchTableModel)searchTable.getModel();
        int[] selectedRows = searchTable.getSelectedRows();
        String attachments = "";
        for( int i = 0; i < selectedRows.length; i++ )
        {
            FrostSearchItemObject srItem = (FrostSearchItemObject)searchTableModel.getRow( selectedRows[i] );

            String key = srItem.getKey();
            String filename = srItem.getFilename();
            attachments += "<attached>" + filename + " * " + key + "</attached>\n";
        }
        return(attachments);
    }
}

