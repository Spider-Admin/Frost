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
import frost.messages.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.storage.perst.*;
import frost.threads.*;
import frost.util.*;
import frost.util.Logging;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.migration.*;

/**
 * Class hold the more non-gui parts of Frost.
 * @pattern Singleton
 * @version $Id$
 */
public class Core implements FrostEventDispatcher  {

    private static final Logger logger = Logger.getLogger(Core.class.getName());

    // Core instanciates itself, frostSettings must be created before instance=Core() !
    public static final SettingsClass frostSettings = new SettingsClass();

    private static Core instance = null;

    private static final FrostCrypt crypto = new FrostCrypt();

    private static boolean isHelpHtmlSecure = false;

    private final EventDispatcher dispatcher = new EventDispatcher();
    private Language language = null;

    private static boolean freenetIsOnline = false;

    private final Timer timer = new Timer(true);

    private MainFrame mainFrame;
    private BoardsManager boardsManager;
    private FileTransferManager fileTransferManager;

    private static FrostIdentities identities;
    
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
        int freenetVersion = frostSettings.getIntValue(SettingsClass.FREENET_VERSION); // 5 or 7
        if( freenetVersion <= 0 ) {
            FreenetVersionDialog dlg = new FreenetVersionDialog();
            dlg.setVisible(true);
            if( dlg.isChoosedExit() ) {
                return false;
            }
            if( dlg.isChoosedFreenet05() ) {
                frostSettings.setValue(SettingsClass.FREENET_VERSION, "5");
            } else if( dlg.isChoosedFreenet07() ) {
                frostSettings.setValue(SettingsClass.FREENET_VERSION, "7");
            } else {
                return false;
            }
            freenetVersion = frostSettings.getIntValue(SettingsClass.FREENET_VERSION); // 5 or 7
        }

        if( freenetVersion != FcpHandler.FREENET_05 && freenetVersion != FcpHandler.FREENET_07 ) {
            MiscToolkit.getInstance().showMessage(
                    language.getString("Core.init.UnsupportedFreenetVersionBody")+": "+freenetVersion,
                    JOptionPane.ERROR_MESSAGE,
                    language.getString("Core.init.UnsupportedFreenetVersionTitle"));
            return false;
        }
        
        // get the list of available nodes
        String nodesUnparsed = frostSettings.getValue(SettingsClass.AVAILABLE_NODES);
        
        List<String> nodes = new ArrayList<String>();

        if (nodesUnparsed == null) { //old format
            String converted = new String(frostSettings.getValue("nodeAddress")+":"+frostSettings.getValue("nodePort"));
            nodes.add(converted.trim());
            frostSettings.setValue(SettingsClass.AVAILABLE_NODES, converted.trim());
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

        if( freenetVersion == FcpHandler.FREENET_07 ) {
            if (nodes.size() > 1) {
                if( frostSettings.getBoolValue(SettingsClass.FCP2_USE_PERSISTENCE) ) {
                    // persistence is not possible with more than 1 node
                    MiscToolkit.getInstance().showMessage(
                            "Persistence is not possible with more than 1 node. Persistence disabled.",
                            JOptionPane.ERROR_MESSAGE,
                            "Warning: Persistence is not possible");
                    frostSettings.setValue(SettingsClass.FCP2_USE_PERSISTENCE, false);
                }
            }
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
        setFreenetOnline(false);
        
        if( Frost.isOfflineMode() ) {
            // keep offline
            return true;
        }
        
        boolean runningOnTestnet = false;
        try {
            List<String> nodeInfo = FcpHandler.inst().getNodeInfo();
            if( nodeInfo != null ) {
                // freenet is online
                setFreenetOnline(true);
                
                // on 0.7 check for "Testnet=true" and warn user
                if( FcpHandler.isFreenet07() ) {
                    for(Iterator<String> i=nodeInfo.iterator(); i.hasNext(); ) {
                        String val = i.next();
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
        if (!isFreenetOnline()) {
            MiscToolkit.getInstance().showMessage(
                language.getString("Core.init.NodeNotRunningBody"),
                JOptionPane.WARNING_MESSAGE,
                language.getString("Core.init.NodeNotRunningTitle"));
        }

        return true;
    }
    
    public static void setFreenetOnline(boolean v) {
        freenetIsOnline = v;
    }
    public static boolean isFreenetOnline() {
        return freenetIsOnline;
    }

    public static FrostCrypt getCrypto() {
        return crypto;
    }

    public static void schedule(TimerTask task, long delay) {
        getInstance().timer.schedule(task, delay);
    }

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
    
    private void showFirstStartupDialog() {
        // clean startup, ask user which freenet version to use, set correct default availableNodes
        FirstStartupDialog startdlg = new FirstStartupDialog();
        boolean exitChoosed = startdlg.startDialog();
        if( exitChoosed ) {
            System.exit(1);
        }
        
        // first startup, no migrate needed
        frostSettings.setValue(SettingsClass.MIGRATE_VERSION, 1);
        
        // set used version
        frostSettings.setValue(SettingsClass.FREENET_VERSION, startdlg.getFreenetVersion()); // 5 or 7
        // init availableNodes with correct port
        if( startdlg.getOwnHostAndPort() != null ) {
            // user set own host:port
            frostSettings.setValue(SettingsClass.AVAILABLE_NODES, startdlg.getOwnHostAndPort());
        } else if( startdlg.getFreenetVersion() == FcpHandler.FREENET_05 ) {
            frostSettings.setValue(SettingsClass.AVAILABLE_NODES, "127.0.0.1:8481");
        } else {
            // 0.7
            if( startdlg.isTestnet() == false ) {
                frostSettings.setValue(SettingsClass.AVAILABLE_NODES, "127.0.0.1:9481");
            } else {
                frostSettings.setValue(SettingsClass.AVAILABLE_NODES, "127.0.0.1:9482");
            }
        }
    }

    /**
     * Initialize, show splashscreen.
     */
    public void initialize() throws Exception {
        Splashscreen splashscreen = new Splashscreen(frostSettings.getBoolValue(SettingsClass.DISABLE_SPLASHSCREEN));
        splashscreen.setVisible(true);

        splashscreen.setText(language.getString("Splashscreen.message.1"));
        splashscreen.setProgress(20);

        //Initializes the logging and skins
        new Logging(frostSettings);
        initializeSkins();
        
        // if first startup ask user for freenet version to use
        if( frostSettings.getIntValue(SettingsClass.FREENET_VERSION) == 0 ) {
            showFirstStartupDialog();
        }

        // open databases
        boolean compactTables = frostSettings.getBoolValue(SettingsClass.COMPACT_DBTABLES);
        try {
            if( compactTables ) {
                splashscreen.setText("Compacting database tables...");
            }
            AppLayerDatabase.initialize(compactTables);
            if( compactTables ) {
                splashscreen.setText(language.getString("Splashscreen.message.1"));
            }
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error opening the databases", ex);
            ex.printStackTrace();
            throw ex;
        }
        frostSettings.setValue(SettingsClass.COMPACT_DBTABLES, false);
        
        // initialize perst storages
        IndexSlotsStorage.inst().initStorage();
        SharedFilesCHKKeyStorage.inst().initStorage();
        FrostFilesStorage.inst().initStorage();
        
        // migrate various tables from McKoi to perst (migrate version 0 -> 1 )
        if( frostSettings.getIntValue(SettingsClass.MIGRATE_VERSION) < 1 ) {
            if( new Migrate0to1().run() == false ) {
                System.out.println("Error during migration!");
                System.exit(8);
            }
            frostSettings.setValue(SettingsClass.MIGRATE_VERSION, 1);
            frostSettings.save();
        }
        
        // CLEANS TEMP DIR! START NO INSERTS BEFORE THIS DID RUN
        Startup.startupCheck(frostSettings);

        splashscreen.setText(language.getString("Splashscreen.message.2"));
        splashscreen.setProgress(40);

        // check if help.zip contains only secure files (no http or ftp links at all)
        {
            CheckHtmlIntegrity chi = new CheckHtmlIntegrity();
            isHelpHtmlSecure = chi.scanZipFile("help/help.zip");
            chi = null;
        }

        splashscreen.setText(language.getString("Splashscreen.message.3"));
        splashscreen.setProgress(60);

        // needs to be done before knownboard import, the keychecker needs to know the freenetversion!
        if (!initializeConnectivity()) {
            System.exit(1);
        }

        getIdentities().initialize(isFreenetOnline());
        
        String title;
    	if( FcpHandler.isFreenet05() ) {
    		title = "Frost@Freenet 0.5";
    	} else if( FcpHandler.isFreenet07() ) {
    		title = "Frost@Freenet 0.7";
    	} else {
    		title = "Frost";
    	}
        
        if( !isFreenetOnline() ) {
            title += " (offline mode)";
        }

        // Main frame
        mainFrame = new MainFrame(frostSettings, title);
        KnownBoardsManager.initialize();
        getBoardsManager().initialize();
        getFileTransferManager().initialize();
        UnsentMessagesManager.initialize();
        
        splashscreen.setText(language.getString("Splashscreen.message.4"));
        splashscreen.setProgress(70);

        // Display the tray icon (do this before mainframe initializes)
        if (frostSettings.getBoolValue(SettingsClass.SHOW_SYSTRAY_ICON) == true) {
            if (JSysTrayIcon.createInstance(0, title, title) == false) {
                logger.severe("Could not create systray icon.");
            }
        }

        mainFrame.initialize();

        // (cleanup gets the expiration mode from settings)
        CleanUp.runExpirationTasks(splashscreen, MainFrame.getInstance().getTofTreeModel().getAllBoards());

        splashscreen.setText(language.getString("Splashscreen.message.5"));
        splashscreen.setProgress(80);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                mainFrame.setVisible(true);
            }
        });

        splashscreen.closeMe();
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                mainFrame.showStartupMessages();
            }
        });
        
        // boot up the machinery ;)
        initializeTasks(mainFrame);
    }

    public FileTransferManager getFileTransferManager() {
        if (fileTransferManager == null) {
            fileTransferManager = FileTransferManager.inst();
        }
        return fileTransferManager;
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
    private void initializeTasks(MainFrame mainframe) {
        // initialize the task that frees memory
        TimerTask cleaner = new TimerTask() {
            public void run() {
                logger.info("freeing memory");
                System.gc();
            }
        };
        long gcMinutes = 10;
        timer.schedule(cleaner, gcMinutes * 60L * 1000L, gcMinutes * 60L * 1000L);
        cleaner = null;
        
        // initialize the task that saves data
        StorageManager saver = new StorageManager(frostSettings, this);
        saver.addAutoSavable(getBoardsManager().getTofTree());
        saver.addAutoSavable(getFileTransferManager());
        
        saver.addExitSavable(getIdentities());
        saver.addExitSavable(getBoardsManager().getTofTree());
        saver.addExitSavable(getFileTransferManager());
        saver.addExitSavable(KnownBoardsManager.getInstance());
        
        saver.addExitSavable(frostSettings);

        // close databases
        saver.addExitSavable(AppLayerDatabase.getInstance());
        // close perst Storages
        saver.addExitSavable(IndexSlotsStorage.inst());
        saver.addExitSavable(SharedFilesCHKKeyStorage.inst());
        saver.addExitSavable(FrostFilesStorage.inst());

        // invoke the mainframe ticker (board updates, clock, ...)
        mainframe.startTickerThread();
        
        // start file attachment uploads
        FileAttachmentUploadThread.getInstance().start();
        
        // start all filetransfer tickers
        getFileTransferManager().startTickers();
        
        // after 15 seconds, start filesharing threads if enabled
        if( isFreenetOnline() && !frostSettings.getBoolValue(SettingsClass.DISABLE_FILESHARING)) {
            Thread t = new Thread() {
                public void run() {
                    Mixed.wait(15000);
                    FileSharingManager.startFileSharing();
                }
            };
            t.start();
        }
    }

    /**
     * Initializes the skins system
     * @param frostSettings the SettingsClass that has the preferences to initialize the skins
     */
    private void initializeSkins() {
        String skinsEnabled = frostSettings.getValue(SettingsClass.SKINS_ENABLED);
        if ((skinsEnabled != null) && (skinsEnabled.equals("true"))) {
            String selectedSkinPath = frostSettings.getValue(SettingsClass.SKIN_NAME);
            if ((selectedSkinPath != null) && (!selectedSkinPath.equals("none"))) {
                try {
                    Skin selectedSkin = SkinLookAndFeel.loadThemePack(selectedSkinPath);
                    SkinLookAndFeel.setSkin(selectedSkin);
                    UIManager.setLookAndFeel(new SkinLookAndFeel());
                } catch (UnsupportedLookAndFeelException exception) {
                    logger.severe("The selected skin is not supported by your system\n" +
                                "Skins will be disabled until you choose another one");
                    frostSettings.setValue(SettingsClass.SKINS_ENABLED, false);
                } catch (Exception exception) {
                    logger.severe("There was an error while loading the selected skin\n" +
                                "Skins will be disabled until you choose another one");
                    frostSettings.setValue(SettingsClass.SKINS_ENABLED, false);
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
            String lang = frostSettings.getValue(SettingsClass.LANGUAGE_LOCALE);
            String langIsExternal = frostSettings.getValue("localeExternal");
            if( lang == null || lang.length() == 0 || lang.equals("default") ) {
                // for default or if not set at all
                frostSettings.setValue(SettingsClass.LANGUAGE_LOCALE, "default");
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
        public void dispatchEvent(FrostEvent frostEvent) {
            switch(frostEvent.getId()) {
                case FrostEvent.STORAGE_ERROR_EVENT_ID:
                    dispatchStorageErrorEvent((StorageErrorEvent) frostEvent);
                    break;
                default:
                    logger.severe("Unknown FrostEvent received. Id: '" + frostEvent.getId() + "'");
            }
        }
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
