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
import java.sql.*;
import java.util.*;
import java.util.Timer;
import java.util.logging.*;

import javax.swing.*;

import com.l2fprod.gui.plaf.skin.*;

import frost.boards.*;
import frost.crypt.*;
import frost.events.*;
import frost.ext.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.gui.*;
import frost.gui.help.*;
import frost.identities.*;
import frost.messaging.*;
import frost.storage.*;
import frost.storage.database.*;
import frost.storage.database.applayer.*;
import frost.threads.maintenance.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * Class hold the more non-gui parts of Frost.
 *
 * @pattern Singleton
 *
 */
public class Core implements FrostEventDispatcher  {

    private static Logger logger = Logger.getLogger(Core.class.getName());

    // Core instanciates itself, frostSettings must be created before instance=Core() !
    public static SettingsClass frostSettings = new SettingsClass();

    private static Core instance = null;

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
        
        if( Frost.isOfflineMode() ) {
            // keep offline
            return true;
        }
        
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

    public static FrostCrypt getCrypto() {
        return crypto;
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
     * @return pointer to the live core
     */
    public static Core getInstance() {
        if( instance == null ) {
            instance = new Core();
        }
        return instance;
    }

    /**
     * @throws Exception
     */
    public void initialize() throws Exception {
        Splashscreen splashscreen = new Splashscreen();
        splashscreen.setVisible(true);

        keypool = frostSettings.getValue("keypool.dir");

        splashscreen.setText(language.getString("Splashscreen.message.1"));
        splashscreen.setProgress(20);

        //Initializes the logging and skins
        new Logging(frostSettings);
        initializeSkins();

        //Initializes storage
        DAOFactory.initialize(frostSettings);
        
        // open databases
        try {
            AppLayerDatabase.initialize();
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error opening the databases", ex);
            ex.printStackTrace();
            throw ex;
        }
        
        // initialize messageHashes
        messageHashes = new MessageHashes();
        messageHashes.initialize();

        // CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
        Startup.startupCheck(frostSettings, keypool);
        // nothing was started until now, its the perfect time to delete all empty date dirs in keypool...
        CleanUp.deleteEmptyBoardDateDirs( new File(keypool) );

        splashscreen.setText(language.getString("Splashscreen.message.2"));
        splashscreen.setProgress(40);

        // check if help.zip contains only secure files (no http or ftp links at all)
        CheckHtmlIntegrity chi = new CheckHtmlIntegrity();
        isHelpHtmlSecure = chi.scanZipFile("help/help.zip");
        chi = null;
        
        boolean doImport = false;
        
        // check if this is a first startup
        if( frostSettings.getIntValue("freenetVersion") == 0 ) {
            
            frostSettings.setValue("oneTimeUpdate.importMessages.didRun", true);
            frostSettings.setValue("oneTimeUpdate.convertSigs.didRun", true);
            frostSettings.setValue("oneTimeUpdate.repairIdentities.didRun", true);

            // ask user which freenet version to use, set correct default availableNodes,
            // allow to import an existing identities file
            FirstStartupDialog startdlg = new FirstStartupDialog();
            boolean exitChoosed = startdlg.startDialog();
            if( exitChoosed ) {
                System.exit(1);
            }
            // set used version
            frostSettings.setValue("freenetVersion", startdlg.getFreenetVersion()); // 5 or 7
            // init availableNodes with correct port
            if( startdlg.getOwnHostAndPort() != null ) {
                // user set own host:port
                frostSettings.setValue("availableNodes", startdlg.getOwnHostAndPort());
            } else if( startdlg.getFreenetVersion() == FcpHandler.FREENET_05 ) {
                frostSettings.setValue("availableNodes", "127.0.0.1:8481");
            } else {
                // 0.7
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
                            "Copy of old identities.xml file failed.",
                            JOptionPane.ERROR_MESSAGE,
                            "Copy failed");
                }
                // import old identities file into database
                new ImportIdentities().importIdentities();
            }
        } else {
            
            // import xml messages into database
            if( frostSettings.getBoolValue("oneTimeUpdate.importMessages.didRun") == false ) {

                String txt = "<html>Frost will now import your existing data, and this could take some time.<br>"+
                             "Afterwards the files in keypool are not longer needed and will be deleted.<br><br>"+
                             "<b>BACKUP YOUR FROST DIRECTORY BEFORE STARTING!</b><br>"+
                             "<br><br>Do you want to start the import NOW press yes.</html>";
                int answer = JOptionPane.showConfirmDialog(splashscreen, txt, "About to start import process",
                                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if( answer != JOptionPane.YES_OPTION ) {
                    System.exit(1);
                }

                // we need the identities to initialize connectivity
                System.out.println("Importing identities");
                new ImportIdentities().importIdentities();

                doImport = true;
            }
        }

        splashscreen.setText(language.getString("Splashscreen.message.3"));
        splashscreen.setProgress(60);

        // needs to be done before knownboard import, the keychecker needs to know the freenetversion!
        if (!initializeConnectivity()) {
            System.exit(1);
        }

        getIdentities().initialize(freenetIsOnline);

        // TODO: maybe make this configureable in options dialog for the paranoic people?
        String title;
//        if( frostSettings.getBoolValue("mainframe.showSimpleTitle") == false ) {
//            title = "Frost - " + getIdentities().getMyId().getUniqueName();
//        } else {
            title = "Frost";
//        }

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
        
        if( doImport ) {
            splashscreen.setText("Importing known boards");
            new ImportKnownBoards().importKnownBoards();
            splashscreen.setText("Importing messages");
            new ImportXmlMessages().importXmlMessages(
                    getBoardsManager().getTofTreeModel().getAllBoards(),
                    splashscreen,
                    "Importing messages");
            splashscreen.setText("Importing files");
            new ImportFiles().importFiles();
            new ImportDownloadFiles().importDownloadFiles(getBoardsManager().getTofTreeModel(), getFileTransferManager());
            new ImportUploadFiles().importUploadFiles(getBoardsManager().getTofTreeModel(), getFileTransferManager());

            frostSettings.setValue("oneTimeUpdate.importMessages.didRun", true);
        }

        splashscreen.setText(language.getString("Splashscreen.message.4"));
        splashscreen.setProgress(70);

        mainFrame.initialize();

        if (isFreenetOnline()) {
            resendFailedMessages();
        }

        splashscreen.setText(language.getString("Splashscreen.message.5"));
        splashscreen.setProgress(80);

        // toftree must be loaded before expiration can run!
        // (cleanup gets the expiration mode from settings itself)
//        CleanUp.processExpiredFiles(MainFrame.getInstance().getTofTreeModel().getAllBoards());

        initializeTasks(mainFrame);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mainFrame.setVisible(true);
            }
        });

        splashscreen.closeMe();
    }

    public FileTransferManager getFileTransferManager() {
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
    
    public MainFrame getMainFrame(){
    	return mainFrame;
    }

    private BoardsManager getBoardsManager() {
        if (boardsManager == null) {
            boardsManager = new BoardsManager(frostSettings);
            boardsManager.setMainFrame(mainFrame);
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
        saver.addAutoSavable(getMessageHashes());
        saver.addAutoSavable(getBoardsManager().getTofTree());
        saver.addAutoSavable(getFileTransferManager());
        
        saver.addExitSavable(getMessageHashes());
        saver.addExitSavable(getIdentities());
        saver.addExitSavable(getBoardsManager().getTofTree());
        saver.addExitSavable(getFileTransferManager());
        saver.addExitSavable(frostSettings);
        // close databases
        saver.addExitSavable(AppLayerDatabase.getInstance());
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
            identities = new FrostIdentities();
        }
        return identities;
    }

    /**
     * This method returns the language resource to get internationalized messages
     * from. That language resource is initialized the first time this method is called.
     * In that case, if the locale field has a value, it is used to select the
     * LanguageResource. If not, the locale value in frostSettings is used for that.
     */
    private void initializeLanguage() {
        if( Frost.getCmdLineLocaleFileName() != null ) {
            // external bundle specified on command line (overrides config setting)
            File f = new File(Frost.getCmdLineLocaleFileName());
            Language.initializeWithFile(f);
        } else if (Frost.getCmdLineLocaleName() != null) {
            // use locale specified on command line (overrides config setting)
            Language.initializeWithName(Frost.getCmdLineLocaleName());
        } else {
            // use config file parameter (format: de or de;ext
            String lang = frostSettings.getValue("locale");
            String langIsExternal = frostSettings.getValue("localeExternal");
            if( lang == null || lang.length() == 0 || lang.equals("default") ) {
                // for default or if not set at all
                frostSettings.setValue("locale", "default");
                Language.initializeWithName(null);
            } else {
                boolean isExternal;
                if( langIsExternal == null || langIsExternal.length() == 0 || !langIsExternal.equals("true")) {
                    isExternal = false;
                } else {
                    isExternal = true;
                }
                Language.initializeWithName(lang, isExternal);
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
