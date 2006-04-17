/*
  Core.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.logging.*;

import javax.swing.*;

import org.w3c.dom.*;

import com.l2fprod.gui.plaf.skin.*;

import frost.boards.*;
import frost.crypt.*;
import frost.events.*;
import frost.ext.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.gui.*;
import frost.gui.help.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.messaging.*;
import frost.storage.*;
import frost.threads.*;
import frost.threads.maintenance.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * Class hold the more non-gui parts of Frost.
 *
 * @pattern Singleton
 *
 */
public class Core implements Savable, FrostEventDispatcher  {

    private static Logger logger = Logger.getLogger(Core.class.getName());

    static Hashtable myBatches = new Hashtable();

    // Core instanciates itself, frostSettings must be created before instance=Core() !
    public static SettingsClass frostSettings = new SettingsClass();

    private static Core instance = new Core();
    private static Locale locale = null;

    private static List knownBoards = new ArrayList(); //list of known boards

    private static FrostCrypt crypto = new FrostCrypt();

    private static boolean isHelpHtmlSecure = false;

    private EventDispatcher dispatcher = new EventDispatcher();
    private Language language = null;

    private boolean freenetIsOnline = false;

    private Timer timer = new Timer(true);

    private MainFrame mainFrame;
    private BoardsManager boardsManager;
    private FileTransferManager fileTransferManager;
    private static MessageHashes messageHashes;

    private static FrostIdentities identities;
    private String keypool;

    private Core() {
        initializeLanguage();
    }

    /**
     * This methods parses the list of available nodes (and converts it if it is in
     * the old format). If there are no available nodes, it shows a Dialog warning the
     * user of the situation and returns false.
     * @return boolean false if no nodes are available. True otherwise.
     */
    private boolean initializeConnectivity() {

        // determine configured freenet version
        int freenetVersion = frostSettings.getIntValue("freenetVersion"); // 5 or 7
        if( freenetVersion <= 0 ) {
            FreenetVersionDialog dlg = new FreenetVersionDialog();
            dlg.setVisible(true);
            if( dlg.isChoosedExit() ) {
                return false;
            }
            if( dlg.isChoosedFreenet05() ) {
                frostSettings.setValue("freenetVersion", "5");
            } else if( dlg.isChoosedFreenet07() ) {
                frostSettings.setValue("freenetVersion", "7");
            } else {
                return false;
            }
            freenetVersion = frostSettings.getIntValue("freenetVersion"); // 5 or 7
        }

        if( freenetVersion != FcpHandler.FREENET_05 && freenetVersion != FcpHandler.FREENET_07 ) {
            MiscToolkit.getInstance().showMessage(
                    language.getString("Core.init.UnsupportedFreenetVersionBody")+": "+freenetVersion,
                    JOptionPane.ERROR_MESSAGE,
                    language.getString("Core.init.UnsupportedFreenetVersionTitle"));
            return false;
        }
        
        // get the list of available nodes
        String nodesUnparsed = frostSettings.getValue("availableNodes");
        
        List nodes = new ArrayList();

        if (nodesUnparsed == null) { //old format
            String converted = new String(frostSettings.getValue("nodeAddress")+":"+frostSettings.getValue("nodePort"));
            nodes.add(converted.trim());
            frostSettings.setValue("availableNodes", converted.trim());
        } else { // new format
            String[] _nodes = nodesUnparsed.split(",");
            for (int i = 0; i < _nodes.length; i++) {
                nodes.add(_nodes[i]);
            }
        }
        if (nodes.size() == 0) {
            MiscToolkit.getInstance().showMessage(
                "Not a single Freenet node configured. You need at least one.",
                JOptionPane.ERROR_MESSAGE,
                "ERROR: No Freenet nodes are configured.");
            return false;
        }
        
        // init the factory with configured nodes
        try {
            FcpHandler.initializeFcp(nodes, freenetVersion); 
        } catch(UnsupportedOperationException ex) {
            MiscToolkit.getInstance().showMessage(
                    ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE,
                    language.getString("Core.init.UnsupportedFreenetVersionTitle"));
            return false;
        }
        
        // install our security manager that only allows connections to the configured FCP hosts
        System.setSecurityManager(new FrostSecurityManager());

        // check if node is online and if we run on 0.7 testnet
        freenetIsOnline = false;
        boolean runningOnTestnet = false;
        try {
            List nodeInfo = FcpHandler.inst().getNodeInfo();
            if( nodeInfo != null ) {
                // freenet is online
                freenetIsOnline = true;
                
                // on 0.7 check for "Testnet=true" and warn user
                if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
                    for(Iterator i=nodeInfo.iterator(); i.hasNext(); ) {
                        String val = (String)i.next();
                        if( val.startsWith("Testnet") && val.indexOf("true") > 0 ) {
                            runningOnTestnet = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in initializeConnectivity", e);
        }
        
        if( runningOnTestnet ) {
            MiscToolkit.getInstance().showMessage(
                    language.getString("Core.init.TestnetWarningBody"),
                    JOptionPane.WARNING_MESSAGE,
                    language.getString("Core.init.TestnetWarningTitle"));
        }

        // We warn the user if there aren't any running nodes
        if (!freenetIsOnline) {
            MiscToolkit.getInstance().showMessage(
                language.getString("Core.init.NodeNotRunningBody"),
                JOptionPane.WARNING_MESSAGE,
                language.getString("Core.init.NodeNotRunningTitle"));
        }

        return true;
    }

    public boolean isFreenetOnline() {
        return freenetIsOnline;
    }

    private void loadKnownBoards() {
        // load the known boards
        // just a flat list in xml
        File boards = new File("boards");
        if(boards.exists()) // old style file, 1 time conversion
        {
            // TODO: remove if not longer needed
            loadOLDKnownBoards(boards);
            // save converted list
            saveKnownBoards();
            return;
        }
        boards = new File("knownboards.xml");
        if( boards.exists() )
        {
            Document doc = null;
            try {
                doc = XMLTools.parseXmlFile(boards, false);
            }
            catch(Exception ex)
            {
                logger.log(Level.SEVERE, "Error reading knownboards.xml", ex);
                return;
            }
            Element rootNode = doc.getDocumentElement();
            if( rootNode.getTagName().equals("FrostKnownBoards") == false )
            {
                logger.severe("Error - invalid knownboards.xml: does not contain the root tag 'FrostKnownBoards'");
                return;
            }
            // pass this as an 'AttachmentList' to xml read method and get
            // all board attachments
            AttachmentList al = new AttachmentList();
            try { al.loadXMLElement(rootNode); }
            catch(Exception ex)
            {
                logger.log(Level.SEVERE, "Error - knownboards.xml: contains unexpected content.", ex);
                return;
            }
            List lst = al.getAllOfType(Attachment.BOARD);
            knownBoards.addAll(lst);
            logger.info("Loaded "+knownBoards.size()+" known boards.");
        }
    }

    /**
     * @param boards
     */
    private void loadOLDKnownBoards(File boards) {
        try {
            ArrayList tmpList = new ArrayList();
            String allBoards = FileAccess.readFile(boards);
            String[] _boards = allBoards.split(":");
            for (int i = 0; i < _boards.length; i++) {
                String aboardstr = _boards[i].trim();
                if (aboardstr.length() < 13
                    || aboardstr.indexOf("*") < 3
                    || !(aboardstr.indexOf("*") < aboardstr.lastIndexOf("*"))) {
                    continue;
                }
                String bname, bpubkey, bprivkey;
                int pos = aboardstr.indexOf("*");
                bname = aboardstr.substring(0, pos).trim();
                int pos2 = aboardstr.indexOf("*", pos + 1);
                bpubkey = aboardstr.substring(pos + 1, pos2).trim();
                bprivkey = aboardstr.substring(pos2 + 1).trim();
                if (bpubkey.length() < 10)
                    bpubkey = null;
                if (bprivkey.length() < 10)
                    bprivkey = null;

                // create BoardAttachment objects and pass them to add method
                // which checks for doubles
                Board bo = new Board(bname, bpubkey, bprivkey, null);
                BoardAttachment ba = new BoardAttachment(bo);
                tmpList.add(ba);
            }
            logger.info("Loaded " + _boards.length + " OLD known boards (converting).");
            addNewKnownBoards(tmpList);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "couldn't load/convert OLD known boards", t);
        }

        if (boards.renameTo(new File("boards.old")) == false) {
            boards.delete(); // paranoia
        }
    }

    /**
     * @return
     */
    public boolean saveKnownBoards() {
        Document doc = XMLTools.createDomDocument();
        if (doc == null) {
            logger.severe("Error - saveBoardTree: factory couldn't create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostKnownBoards");
        doc.appendChild(rootElement);

        synchronized (getKnownBoards()) {
            Iterator i = getKnownBoards().iterator();
            while (i.hasNext()) {
                BoardAttachment current = (BoardAttachment) i.next();
                Element anAttachment = current.getXMLElement(doc);
                rootElement.appendChild(anAttachment);
            }
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, "knownboards.xml");
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Exception while writing knownboards.xml:", ex);
        }
        if (!writeOK) {
            logger.severe("Error while writing knownboards.xml, file was not saved");
        } else {
            logger.info("Saved " + getKnownBoards().size() + " known boards.");
        }
        return writeOK;
    }

    private void loadBatches() {
        //load the batches
        File batches = new File("batches");
        if (batches.exists() && batches.length() > 0) { //fix previous version bug
            try {
                String allBatches = FileAccess.readFile(batches);
                String[] _batches = allBatches.split("_");
                //dumb.  will fix later

                for (int i = 0; i < _batches.length; i++) {
                    myBatches.put(_batches[i], _batches[i]);
                }

                logger.info("loaded " + _batches.length + " batches of shared files");
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "couldn't load batches:", e);
            }
        }
    }

    private boolean saveBatches() {
        try {
            StringBuffer buf = new StringBuffer();
            synchronized (getMyBatches()) {
                Iterator i = getMyBatches().keySet().iterator();
                while (i.hasNext()) {
                    String current = (String) i.next();
                    if (current.length() > 0) {
                        buf.append(current);
                        if (i.hasNext()) {
                            buf.append("_");
                        }
                    } else {
                        i.remove(); //make sure no empty batches are saved
                    }
                }
            }
            File batches = new File("batches");
            FileAccess.writeFile(buf.toString(), batches);
            return true;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception thrown in saveBatches():", t);
        }
        return false;
    }

    public static FrostCrypt getCrypto() {
        return crypto;
    }

    public static Hashtable getMyBatches() {
        return myBatches;
    }

    /**
     * Tries to send old messages that have not been sent yet
     */
    protected void resendFailedMessages() {
        // start a thread that waits some seconds for gui to appear, then searches for unsent messages
        ResendFailedMessagesThread t =
            new ResendFailedMessagesThread(getBoardsManager().getTofTree(), getBoardsManager().getTofTreeModel());
        t.start();
    }

    /**
     * @param which
     */
    public void deleteDir(String which) {
        (new DeleteWholeDirThread(this, which)).start();
    }

    /**
     * @param task
     * @param delay
     */
    public static void schedule(TimerTask task, long delay) {
        getInstance().timer.schedule(task, delay);
    }

    /**
     * @param task
     * @param delay
     * @param period
     */
    public static void schedule(TimerTask task, long delay, long period) {
        getInstance().timer.schedule(task, delay, period);
    }

    /**
     * @return list of known boards
     */
    public static List getKnownBoards() {
        return knownBoards;
    }

    /**
     * Called with a list of BoardAttachments, should add all boards
     * that are not contained already
     * @param lst
     */
    public static void addNewKnownBoards( List lst ) {
        if( lst == null || lst.size() == 0 ) {
            return;
        }

        Iterator i = lst.iterator();
        while(i.hasNext()) {
            BoardAttachment newba = (BoardAttachment)i.next();

            String bname = newba.getBoardObj().getName();
            String bprivkey = newba.getBoardObj().getPrivateKey();
            String bpubkey = newba.getBoardObj().getPublicKey();

            boolean addMe = true;
            synchronized(getKnownBoards()) {
                Iterator j = getKnownBoards().iterator();
                while(j.hasNext()) {
                    BoardAttachment board = (BoardAttachment)j.next();
                    if( board.getBoardObj().getName().equalsIgnoreCase(bname) &&
                        (
                          ( board.getBoardObj().getPrivateKey() == null &&
                            bprivkey == null
                          ) ||
                          ( board.getBoardObj().getPrivateKey() != null &&
                            board.getBoardObj().getPrivateKey().equals(bprivkey)
                          )
                        ) &&
                        (
                          ( board.getBoardObj().getPublicKey() == null &&
                            bpubkey == null
                          ) ||
                          ( board.getBoardObj().getPublicKey() != null &&
                            board.getBoardObj().getPublicKey().equals(bpubkey)
                          )
                        )
                      )
                      {
                          // same boards, dont add
                          addMe = false;
                          break;
                      }
                }
            }
            if( addMe ) {
                getKnownBoards().add(newba);
            }
        }
    }

    /**
     *
     * @return pointer to the live core
     */
    public static Core getInstance() {
        return instance;
    }

    /**
     * One time repair: finds all .sig files, reads the sig state from sig, loads
     * message and sets the signature state into the xml.
     * If message load failed .sig is removed (won't load either).
     */
    private void convertSigIntoXml() {
        // get all .sig files in keypool
      ArrayList entries = FileAccess.getAllEntries( new File(frostSettings.getValue("keypool.dir")), ".sig");
      logger.info("convertSigIntoXml: Starting to convert "+entries.size()+" .sig files.");

      for( int ii=0; ii<entries.size(); ii++ ) {

          File sigFile = (File)entries.get(ii);
          File msgFile = new File(sigFile.getPath().substring(0, sigFile.getPath().length() - 4)); // .xml.sig -> .xml
          if (msgFile.getName().equals("files.xml")) continue;
          if (msgFile.getName().equals("new_files.xml")) continue;
          FrostMessageObject tempMsg = null;
          try {
              tempMsg = new FrostMessageObject(msgFile);
          } catch (MessageCreationException mce){
              if (mce.isEmpty()) {
                  logger.log(Level.INFO, "A message could not be created. It is empty.", mce);
              } else {
                  logger.log(Level.WARNING, "A message could not be created.", mce);
              }
              sigFile.delete();
              continue;
          }
          String oldStatus = FileAccess.readFile(sigFile);
          if( oldStatus.indexOf("GOOD") >= 0 ||
              oldStatus.indexOf("CHECK") >= 0 ||
              oldStatus.indexOf("BAD") >= 0 )
          {
              // msg was signed
              tempMsg.setSignatureStatus(MessageObject.SIGNATURESTATUS_VERIFIED);
          } else if( oldStatus.indexOf("NONE") >= 0 ||
                     oldStatus.indexOf("N/A") >= 0 )
          {
              // set to OLD
              tempMsg.setSignatureStatus(MessageObject.SIGNATURESTATUS_OLD);
          } else {
              // set to tampered
              tempMsg.setSignatureStatus(MessageObject.SIGNATURESTATUS_TAMPERED);
          }
          tempMsg.save();
          sigFile.delete();
      }
    }

    /**
     * @throws Exception
     */
    public void initialize() throws Exception {
        Splashscreen splashscreen = new Splashscreen();
        splashscreen.setVisible(true);

        keypool = frostSettings.getValue("keypool.dir");

        splashscreen.setText(language.getString("Initializing Mainframe"));
        splashscreen.setProgress(20);

        //Initializes the logging and skins
        new Logging(frostSettings);
        initializeSkins();

        //Initializes storage
        DAOFactory.initialize(frostSettings);
        
        // initialize messageHashes
        messageHashes = new MessageHashes();
        messageHashes.initialize();

        // CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
        Startup.startupCheck(frostSettings, keypool);
        // nothing was started until now, its the perfect time to delete all empty date dirs in keypool...
        CleanUp.deleteEmptyBoardDateDirs( new File(keypool) );

        splashscreen.setText(language.getString("Hypercube fluctuating!"));
        splashscreen.setProgress(40);

        // check if help.zip contains only secure files (no http or ftp links at all)
        CheckHtmlIntegrity chi = new CheckHtmlIntegrity();
        isHelpHtmlSecure = chi.scanZipFile("help/help.zip");
        chi = null;

        // check if this is a first time startup and maybe skip conversion
        File identitiesFile = new File("identities.xml");
        if( identitiesFile.exists() == false || identitiesFile.length() == 0 ) {
            frostSettings.setValue("oneTimeUpdate.convertSigs.didRun", true);
            frostSettings.setValue("oneTimeUpdate.repairIdentities.didRun", true);
            
            // TODO: ask user which freenet version to use, set correct default availableNodes,
            // allow to import an existing identities file
            FirstStartupDialog startdlg = new FirstStartupDialog();
            boolean exitChoosed = startdlg.startDialog();
            if( exitChoosed ) {
                System.exit(1);
            }
            // set used version
            frostSettings.setValue("freenetVersion", startdlg.getFreenetVersion()); // 5 or 7
            // init availableNodes with correct port
            if( startdlg.getFreenetVersion() == FcpHandler.FREENET_05 ) {
                frostSettings.setValue("availableNodes", "127.0.0.1:8481");
            } else {
                if( startdlg.isTestnet() == false ) {
                    frostSettings.setValue("availableNodes", "127.0.0.1:9481");
                } else {
                    frostSettings.setValue("availableNodes", "127.0.0.1:9482");
                }
            }
            if( startdlg.getOldIdentitiesFile() != null && startdlg.getOldIdentitiesFile().length() > 0 ) {
                boolean wasOk = FileAccess.copyFile(startdlg.getOldIdentitiesFile(), "identities.xml");
                if( wasOk == false ) {
                    MiscToolkit.getInstance().showMessage(
                            "Import of old identities.xml file failed.",
                            JOptionPane.ERROR_MESSAGE,
                            "Import failed");
                }
            }
        }

        if (!initializeConnectivity()) {
            System.exit(1);
        }

        // TODO: one time convert, remove later (added: 2005-09-02)
        if( frostSettings.getBoolValue("oneTimeUpdate.convertSigs.didRun") == false ) {
            splashscreen.setText("Convert from old format");

            // convert .sig files into xml files
            //  - find all existing .sig files
            //  - find xml file for .sig
            //  - open XML file
            //  - read .sig file and set:
            //     - xml to VERIFIED if .sig contains ...GOOD... or BAD or CHECK
            //     - xml to TAMPERED if FAKE
            //     - xml to OLD if NONE or N/A
//          public static final String PENDING  = "<html><b><font color=#FFCC00>CHECK</font></b></html>";
//          public static final String VERIFIED = "<html><b><font color=\"green\">GOOD</font></b></html>";
//          public static final String FAILED   = "<html><b><font color=\"red\">BAD</font></b></html>";
//          public static final String NA       = "N/A";
//          public static final String OLD      = "NONE";
//          public static final String TAMPERED = "FAKE :(";

            String txt = "<html>Frost must now convert the messages, and this could take some time.<br>"+
                         "Afterwards the .sig files are not longer needed and will be deleted.<br><br>"+
                         "<b>BACKUP YOUR FROST DIRECTORY BEFORE STARTING!</b><br>"+
                         "<br><br>Do you want to start the conversion NOW press yes.</html>";
            int answer = JOptionPane.showConfirmDialog(splashscreen, txt, "About to start convert process",
                          JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION);

            if( answer != JOptionPane.YES_OPTION ) {
                System.exit(1);
            }

            convertSigIntoXml();

            frostSettings.setValue("oneTimeUpdate.convertSigs.didRun", true);
        }

        splashscreen.setText(language.getString("Sending IP address to NSA"));
        splashscreen.setProgress(60);

        getIdentities().initialize(freenetIsOnline);

        // TODO: maybe make this configureable in options dialog for the paranoic people?
        String title;
        if( frostSettings.getBoolValue("mainframe.showSimpleTitle") == false ) {
            title = "Frost - " + getIdentities().getMyId().getUniqueName();
        } else {
            title = "Frost";
        }

        // Display the tray icon (do this before mainframe initializes)
        if (frostSettings.getBoolValue("showSystrayIcon") == true) {
            if (JSysTrayIcon.createInstance(0, title, title) == false) {
                logger.severe("Could not create systray icon.");
            }
        }

        // Main frame
        mainFrame = new MainFrame(frostSettings, title);
        getBoardsManager().initialize();
        getFileTransferManager().initialize();

        splashscreen.setText(language.getString("Wasting more time"));
        splashscreen.setProgress(70);

        mainFrame.initialize();

        //load vital data
        loadBatches();
        loadKnownBoards();

        if (isFreenetOnline()) {
            resendFailedMessages();
        }

        splashscreen.setText(language.getString("Reaching ridiculous speed..."));
        splashscreen.setProgress(80);

        // toftree must be loaded before expiration can run!
        // (cleanup gets the expiration mode from settings itself)
        CleanUp.processExpiredFiles(MainFrame.getInstance().getTofTreeModel().getAllBoards());

        initializeTasks(mainFrame);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mainFrame.setVisible(true);
            }
        });

        splashscreen.closeMe();
    }

    private FileTransferManager getFileTransferManager() {
        if (fileTransferManager == null) {
            fileTransferManager = new FileTransferManager(frostSettings);
            fileTransferManager.setMainFrame(mainFrame);
            fileTransferManager.setTofTreeModel(getBoardsManager().getTofTreeModel());
            fileTransferManager.setFreenetIsOnline(isFreenetOnline());
            fileTransferManager.setIdentities(getIdentities());
            fileTransferManager.setKeypool(keypool);
        }
        return fileTransferManager;
    }

    public static MessageHashes getMessageHashes() {
        return messageHashes;
    }

    private BoardsManager getBoardsManager() {
        if (boardsManager == null) {
            boardsManager = new BoardsManager(frostSettings);
            boardsManager.setMainFrame(mainFrame);
            boardsManager.setCore(this);
        }
        return boardsManager;
    }

    /**
     * @param parentFrame the frame that will be the parent of any
     *          dialog that has to be shown in case an error happens
     *          in one of those tasks
     */
    private void initializeTasks(JFrame parentFrame) {
        //We initialize the task that checks for spam
        timer.schedule(
            new CheckForSpam(frostSettings, getBoardsManager().getTofTree(), getBoardsManager().getTofTreeModel()),
            1*60*60*1000, // wait 1 min
            frostSettings.getIntValue("sampleInterval") * 60 * 60 * 1000);

        // initialize the task that discards old files
        TimerTask cleaner = new TimerTask() {
            public void run() {
                // each 6 hours cleanup files
                logger.info("Timer cleaner: Starting to process expired files.");
                CleanUp.processExpiredFiles(MainFrame.getInstance().getTofTreeModel().getAllBoards());
            }
        };
        timer.schedule(cleaner, 6*60*60*1000, 6*60*60*1000); // 6 hrs interval, always run during startup
        cleaner = null;

        // initialize the task that frees memory
        cleaner = new TimerTask() {
            public void run() {
                // free memory each 30 min
                logger.info("freeing memory");
                System.gc();
            }
        };
        timer.schedule(cleaner, 30 * 60 * 1000, 30 * 60 * 1000);    //30 minutes
        cleaner = null;

        // initialize the task that saves data
        StorageManager saver = new StorageManager(frostSettings, this);
        saver.addAutoSavable(this);
        saver.addAutoSavable(getIdentities());
        saver.addAutoSavable(getMessageHashes());
        saver.addAutoSavable(getBoardsManager().getTofTree());
        saver.addAutoSavable(getFileTransferManager());
        saver.addExitSavable(this);
        saver.addExitSavable(getIdentities());
        saver.addExitSavable(getMessageHashes());
        saver.addExitSavable(getBoardsManager().getTofTree());
        saver.addExitSavable(getFileTransferManager());
        saver.addExitSavable(frostSettings);

        // We initialize the task that helps requests of friends (delay 5min, repeat all 6hrs)
        if (frostSettings.getBoolValue("helpFriends")) {
            timer.schedule(new GetFriendsRequestsThread(identities), 5 * 60 * 1000, 6 * 60 * 60 * 1000);
        }
    }

    public static void setLocale(Locale locale) {
        Core.locale = locale;
    }

    /**
     * Initializes the skins system
     * @param frostSettings the SettingsClass that has the preferences to initialize the skins
     */
    private void initializeSkins() {
        String skinsEnabled = frostSettings.getValue("skinsEnabled");
        if ((skinsEnabled != null) && (skinsEnabled.equals("true"))) {
            String selectedSkinPath = frostSettings.getValue("selectedSkin");
            if ((selectedSkinPath != null) && (!selectedSkinPath.equals("none"))) {
                try {
                    Skin selectedSkin = SkinLookAndFeel.loadThemePack(selectedSkinPath);
                    SkinLookAndFeel.setSkin(selectedSkin);
                    UIManager.setLookAndFeel(new SkinLookAndFeel());
                } catch (UnsupportedLookAndFeelException exception) {
                    logger.severe("The selected skin is not supported by your system\n" +
                                "Skins will be disabled until you choose another one");
                    frostSettings.setValue("skinsEnabled", false);
                } catch (Exception exception) {
                    logger.severe("There was an error while loading the selected skin\n" +
                                "Skins will be disabled until you choose another one");
                    frostSettings.setValue("skinsEnabled", false);
                }
            }
        }
    }

    public static FrostIdentities getIdentities() {
        if (identities == null) {
            identities = new FrostIdentities(frostSettings);
        }
        return identities;
    }

    /* (non-Javadoc)
     * @see frost.storage.Savable#save()
     */
    public void save() throws StorageException {
        boolean saveOK;
        saveOK = saveBatches();
        saveOK &= saveKnownBoards();
        if (!saveOK) {
            throw new StorageException("Error while saving the core items.");
        }
    }

    /**
     * This method returns the language resource to get internationalized messages
     * from. That language resource is initialized the first time this method is called.
     * In that case, if the locale field has a value, it is used to select the
     * LanguageResource. If not, the locale value in frostSettings is used for that.
     */
    private void initializeLanguage() {
        if (locale != null) {
            // use locale specified on command line (overrides config setting)
            Language.initialize("res.LangRes", locale);
        } else {
            String lang = frostSettings.getValue("locale");
            if( lang == null || lang.length() == 0 || lang.equals("default") ) {
                // for default or if not set at all
                Language.initialize("res.LangRes");
            } else {
                Language.initialize("res.LangRes", new Locale(lang));
            }
        }
        language = Language.getInstance();
    }

    /* (non-Javadoc)
     * @see frost.events.FrostEventDispatcher#dispatchEvent(frost.events.FrostEvent)
     */
    public void dispatchEvent(FrostEvent frostEvent) {
        dispatcher.dispatchEvent(frostEvent);
    }

    public static boolean isHelpHtmlSecure() {
        return isHelpHtmlSecure;
    }
    
    private class EventDispatcher {
        /**
         * @param frostEvent
         */
        public void dispatchEvent(FrostEvent frostEvent) {
            switch(frostEvent.getId()) {
                case FrostEvent.STORAGE_ERROR_EVENT_ID:
                    dispatchStorageErrorEvent((StorageErrorEvent) frostEvent);
                    break;
                default:
                    logger.severe("Unknown FrostEvent received. Id: '" + frostEvent.getId() + "'");
            }
        }

        /**
         * @param errorEvent
         */
        public void dispatchStorageErrorEvent(StorageErrorEvent errorEvent) {
            StringWriter stringWriter = new StringWriter();
            errorEvent.getException().printStackTrace(new PrintWriter(stringWriter));

            if (mainFrame != null) {
                JDialogWithDetails.showErrorDialog(mainFrame,
                                    language.getString("Saver.AutoTask.title"),
                                    errorEvent.getMessage(),
                                    stringWriter.toString());
            }
            System.exit(3);
        }
    }
}
