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
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.logging.*;

import javax.swing.*;

import frost.ext.*;
import frost.fcp.*;
import frost.fcp.fcp07.*;
import frost.fileTransfer.*;
import frost.gui.*;
import frost.gui.help.*;
import frost.identities.*;
import frost.messaging.freetalk.*;
import frost.messaging.frost.*;
import frost.messaging.frost.boards.*;
import frost.messaging.frost.threads.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.storage.perst.filelist.*;
import frost.storage.perst.identities.*;
import frost.storage.perst.messagearchive.*;
import frost.storage.perst.messages.*;
import frost.util.*;
import frost.util.Logging;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * Class hold the more non-gui parts of Frost.
 * @pattern Singleton
 * @version $Id$
 */
public class Core {

    private static final Logger logger = Logger.getLogger(Core.class.getName());

    // Core instanciates itself, frostSettings must be created before instance=Core() !
    public static final SettingsClass frostSettings = new SettingsClass();

    private static Core instance = null;

    private static final FrostCrypt crypto = new FrostCrypt();

    private static boolean isHelpHtmlSecure = false;

    private Language language = null;

    private static boolean freenetIsOnline = false;
    private static boolean freetalkIsTalkable = false;

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
        final int freenetVersion = frostSettings.getIntValue(SettingsClass.FREENET_VERSION); // only 7 is supported
        if( freenetVersion != 7 ) {
            MiscToolkit.showMessage(
                    language.getString("Core.init.UnsupportedFreenetVersionBody")+": "+freenetVersion,
                    JOptionPane.ERROR_MESSAGE,
                    language.getString("Core.init.UnsupportedFreenetVersionTitle"));
            return false;
        }

        // get the list of available nodes
        String nodesUnparsed = frostSettings.getValue(SettingsClass.FREENET_FCP_ADDRESS);
        if (nodesUnparsed == null || nodesUnparsed.length() == 0) {
            frostSettings.setValue(SettingsClass.FREENET_FCP_ADDRESS, "127.0.0.1:9481");
            nodesUnparsed = frostSettings.getValue(SettingsClass.FREENET_FCP_ADDRESS);
        }

        final List<String> nodes = new ArrayList<String>();

        // earlier we supported multiple nodes, so check if there is more than one node
        if( nodesUnparsed != null ) {
            final String[] _nodes = nodesUnparsed.split(",");
            for( final String element : _nodes ) {
                nodes.add(element);
            }
        }

        // paranoia, should never happen
        if (nodes.size() == 0) {
            MiscToolkit.showMessage(
                "Not a single Freenet node configured. Frost cannot start.",
                JOptionPane.ERROR_MESSAGE,
                "ERROR: No Freenet nodes are configured.");
            return false;
        }

        if (nodes.size() > 1) {
            MiscToolkit.showMessage(
                    "Frost doesn' support multiple Freenet nodes and will use the first configured node.",
                    JOptionPane.ERROR_MESSAGE,
                    "Warning: Using first configured node");
            frostSettings.setValue(SettingsClass.FREENET_FCP_ADDRESS, nodes.get(0));
        }

        // init the factory with configured node
        try {
            FcpHandler.initializeFcp(nodes.get(0));
        } catch(final Exception ex) {
            MiscToolkit.showMessage(
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

        // We warn the user when he connects to a 0.7 testnet node
        // this also tries to connect to a configured node and sets 'freenetOnline'
        boolean runningOnTestnet = false;
        try {
            final FcpConnection fcpConn = new FcpConnection(FcpHandler.inst().getFreenetNode());
            final NodeMessage nodeMessage = fcpConn.getNodeInfo();

            // node answered, freenet is online
            setFreenetOnline(true);

            if (nodeMessage.getBoolValue("Testnet")) {
                runningOnTestnet = true;
            }

            final boolean freetalkTalkable = fcpConn.checkFreetalkPlugin();
            setFreetalkTalkable (freetalkTalkable);

            if (freetalkTalkable) {
                System.out.println("**** Freetalk is Talkable. ****");
            } else {
                System.out.println("**** Freetalk is NOT Talkable. ****");
            }

            fcpConn.close();

        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in initializeConnectivity", e);
        }

        if (runningOnTestnet) {
            MiscToolkit.showMessage(
                    language.getString("Core.init.TestnetWarningBody"),
                    JOptionPane.WARNING_MESSAGE,
                    language.getString("Core.init.TestnetWarningTitle"));
        }

        // We warn the user if there aren't any running nodes
        if (!isFreenetOnline()) {
            MiscToolkit.showMessage(
                language.getString("Core.init.NodeNotRunningBody"),
                JOptionPane.WARNING_MESSAGE,
                language.getString("Core.init.NodeNotRunningTitle"));
        } else {
            // maybe start a single message connection
            FcpHandler.inst().goneOnline();
        }

        return true;
    }

    public static void setFreenetOnline(final boolean v) {
        freenetIsOnline = v;
    }
    public static boolean isFreenetOnline() {
        return freenetIsOnline;
    }

    public static void setFreetalkTalkable(final boolean v) {
        freetalkIsTalkable = v;
    }
    public static boolean isFreetalkTalkable() {
        return freetalkIsTalkable;
    }

    public static FrostCrypt getCrypto() {
        return crypto;
    }

    public static void schedule(final TimerTask task, final long delay) {
        getInstance().timer.schedule(task, delay);
    }

    public static void schedule(final TimerTask task, final long delay, final long period) {
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
        final FirstStartupDialog startdlg = new FirstStartupDialog();
        final boolean exitChoosed = startdlg.startDialog();
        if( exitChoosed ) {
            System.exit(1);
        }

        // first startup, no migrate needed
        frostSettings.setValue(SettingsClass.MIGRATE_VERSION, 3);

        // set used version
        final int freenetVersion = 7;
        frostSettings.setValue(SettingsClass.FREENET_VERSION, freenetVersion);
        // init availableNodes with correct port
        if( startdlg.getOwnHostAndPort() != null ) {
            // user set own host:port
            frostSettings.setValue(SettingsClass.FREENET_FCP_ADDRESS, startdlg.getOwnHostAndPort());
        } else {
            // 0.7 darknet
            frostSettings.setValue(SettingsClass.FREENET_FCP_ADDRESS, "127.0.0.1:9481");
        }
    }

    private void compactPerstStorages(final Splashscreen splashscreen) throws Exception {
        try {
            long savedBytes = 0;
            savedBytes += compactStorage(splashscreen, IndexSlotsStorage.inst());
            savedBytes += compactStorage(splashscreen, FrostFilesStorage.inst());
            savedBytes += compactStorage(splashscreen, IdentitiesStorage.inst());
            savedBytes += compactStorage(splashscreen, SharedFilesCHKKeyStorage.inst());
            savedBytes += compactStorage(splashscreen, MessageStorage.inst());
            savedBytes += compactStorage(splashscreen, MessageContentStorage.inst());
            savedBytes += compactStorage(splashscreen, FileListStorage.inst());
            savedBytes += compactStorage(splashscreen, ArchiveMessageStorage.inst());

            final NumberFormat nf = NumberFormat.getInstance();
            logger.warning("Finished compact of storages, released "+nf.format(savedBytes)+" bytes.");
        } catch(final Exception ex) {
            logger.log(Level.SEVERE, "Error compacting perst storages", ex);
            ex.printStackTrace();
            MiscToolkit.showMessage(
                    "Error compacting perst storages, compact did not complete: "+ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE,
                    "Error compacting perst storages");
            throw ex;
        }
    }

    private long compactStorage(final Splashscreen splashscreen, final AbstractFrostStorage storage) throws Exception {
        splashscreen.setText("Compacting storage file '"+storage.getStorageFilename()+"'...");
        return storage.compactStorage();
    }

    private void exportStoragesToXml(final Splashscreen splashscreen) throws Exception {
        try {
            exportStorage(splashscreen, IndexSlotsStorage.inst());
            exportStorage(splashscreen, FrostFilesStorage.inst());
            exportStorage(splashscreen, IdentitiesStorage.inst());
            exportStorage(splashscreen, SharedFilesCHKKeyStorage.inst());
            exportStorage(splashscreen, MessageStorage.inst());
            exportStorage(splashscreen, MessageContentStorage.inst());
            exportStorage(splashscreen, FileListStorage.inst());
            exportStorage(splashscreen, ArchiveMessageStorage.inst());
            logger.warning("Finished export to XML");
        } catch(final Exception ex) {
            logger.log(Level.SEVERE, "Error exporting perst storages", ex);
            ex.printStackTrace();
            MiscToolkit.showMessage(
                    "Error exporting perst storages, export did not complete: "+ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE,
            "Error exporting perst storages");
            throw ex;
        }
    }

    private void exportStorage(final Splashscreen splashscreen, final AbstractFrostStorage storage) throws Exception {
        splashscreen.setText("Exporting storage file '"+storage.getStorageFilename()+"'...");
        storage.exportToXml();
    }

    /**
     * Initialize, show splashscreen.
     */
    public void initialize() throws Exception {

        final Splashscreen splashscreen = new Splashscreen(frostSettings.getBoolValue(SettingsClass.DISABLE_SPLASHSCREEN));
        splashscreen.setVisible(true);

        splashscreen.setText(language.getString("Splashscreen.message.1"));
        splashscreen.setProgress(20);

        //Initializes the logging and skins
        new Logging(frostSettings);

        {
            StringBuilder sb = new StringBuilder();
            sb.append("***** Starting Frost "+getClass().getPackage().getSpecificationVersion()+" *****\n");
            for( final String s : Frost.getEnvironmentInformation() ) {
                sb.append(s).append("\n");
            }
            logger.severe(sb.toString());
            sb = null;
        }

        // CLEANS TEMP DIR! START NO INSERTS BEFORE THIS DID RUN
        Startup.startupCheck(frostSettings);

        // if first startup ask user for freenet version to use
        if( frostSettings.getIntValue(SettingsClass.FREENET_VERSION) == 0 ) {
            showFirstStartupDialog();
        }

        // we must be at migration level 2 (no mckoi)!!!
        if( frostSettings.getIntValue(SettingsClass.MIGRATE_VERSION) < 2 ) {
            final String errText = "Error: You must update this Frost version from version 11-Dec-2007 !!!";
            logger.log(Level.SEVERE, errText);
            System.out.println(errText);
            System.exit(8);
        }

        // before opening the storages, maybe compact them
        if( frostSettings.getBoolValue(SettingsClass.PERST_COMPACT_STORAGES) ) {
            compactPerstStorages(splashscreen);
            frostSettings.setValue(SettingsClass.PERST_COMPACT_STORAGES, false);
        }

        // one time: change cleanup settings to new default, they were way to high
        if( frostSettings.getIntValue(SettingsClass.MIGRATE_VERSION) < 3 ) {
            frostSettings.setValue(SettingsClass.DB_CLEANUP_REMOVEOFFLINEFILEWITHKEY, true);
            if (frostSettings.getIntValue(SettingsClass.DB_CLEANUP_OFFLINEFILESMAXDAYSOLD) > 30) {
                frostSettings.setValue(SettingsClass.DB_CLEANUP_OFFLINEFILESMAXDAYSOLD, 30);
            }

            // run cleanup now
            frostSettings.setValue(SettingsClass.DB_CLEANUP_LASTRUN, 0L);
            // run compact during next startup (after the cleanup)
            frostSettings.setValue(SettingsClass.PERST_COMPACT_STORAGES, true);
            // migration is done
            frostSettings.setValue(SettingsClass.MIGRATE_VERSION, 3);
        }

        // maybe export perst storages to XML
        if( frostSettings.getBoolValue(SettingsClass.PERST_EXPORT_STORAGES) ) {
            exportStoragesToXml(splashscreen);
            frostSettings.setValue(SettingsClass.PERST_EXPORT_STORAGES, false);
        }

        // initialize perst storages
        IndexSlotsStorage.inst().initStorage();
        SharedFilesCHKKeyStorage.inst().initStorage();
        FrostFilesStorage.inst().initStorage();
        MessageStorage.inst().initStorage();
        MessageContentStorage.inst().initStorage();
        ArchiveMessageStorage.inst().initStorage();
        IdentitiesStorage.inst().initStorage();
        FileListStorage.inst().initStorage();

        splashscreen.setText(language.getString("Splashscreen.message.2"));
        splashscreen.setProgress(40);

        // check if help.zip contains only secure files (no http or ftp links at all)
        {
            final CheckHtmlIntegrity chi = new CheckHtmlIntegrity();
            isHelpHtmlSecure = chi.scanZipFile("help/help.zip");
        }

        splashscreen.setText(language.getString("Splashscreen.message.3"));
        splashscreen.setProgress(60);

        // sets the freenet version, initializes identities
        if (!initializeConnectivity()) {
            System.exit(1);
        }

        getIdentities().initialize();

        String title = "Frost@Freenet 0.7";

        if( !isFreenetOnline() ) {
            title += " (offline mode)";
        }

        // Main frame
        mainFrame = new MainFrame(frostSettings, title);
        getBoardsManager().initialize();

        getFileTransferManager().initialize();
        UnsentMessagesManager.initialize();

        FreetalkManager.initialize();

        splashscreen.setText(language.getString("Splashscreen.message.4"));
        splashscreen.setProgress(70);

        // Display the tray icon (do this before mainframe initializes)
        if (frostSettings.getBoolValue(SettingsClass.SHOW_SYSTRAY_ICON) == true) {
            try {
                JSysTrayIcon.createInstance(0, title, title);
            } catch(final Throwable t) {
                logger.log(Level.SEVERE, "Could not create systray icon.", t);
            }
        }

        mainFrame.initialize();

        // cleanup gets the expiration mode from settings
        CleanUp.runExpirationTasks(splashscreen, MainFrame.getInstance().getFrostMessageTab().getTofTreeModel().getAllBoards());

        // Show enqueued startup messages before showing the mainframe,
        // otherwise the glasspane used during load of board messages could corrupt the modal message dialog!
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                mainFrame.showStartupMessages();
            }
        });

        // After expiration, select previously selected board tree row.
        // NOTE: This loads the message table!!!
        mainFrame.postInitialize();

        splashscreen.setText(language.getString("Splashscreen.message.5"));
        splashscreen.setProgress(80);

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                mainFrame.setVisible(true);
            }
        });

        splashscreen.closeMe();

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
    private void initializeTasks(final MainFrame mainframe) {
        // initialize the task that frees memory
        TimerTask cleaner = new TimerTask() {
            @Override
            public void run() {
                logger.info("freeing memory");
                System.gc();
            }
        };
        final long gcMinutes = 10;
        timer.schedule(cleaner, gcMinutes * 60L * 1000L, gcMinutes * 60L * 1000L);
        cleaner = null;

        // initialize the task that saves data
        final StorageManager saver = new StorageManager(frostSettings);

        // auto savables
        saver.addAutoSavable(getBoardsManager().getTofTree());
        saver.addAutoSavable(getFileTransferManager());
        saver.addAutoSavable(new IdentityAutoBackupTask());

        // exit savables, must run before the perst storages are closed
        saver.addExitSavable(new IdentityAutoBackupTask());
        saver.addExitSavable(getBoardsManager().getTofTree());
        saver.addExitSavable(getFileTransferManager());

        saver.addExitSavable(frostSettings);

        // close perst Storages
        saver.addExitSavable(IndexSlotsStorage.inst());
        saver.addExitSavable(SharedFilesCHKKeyStorage.inst());
        saver.addExitSavable(FrostFilesStorage.inst());
        saver.addExitSavable(MessageStorage.inst());
        saver.addExitSavable(MessageContentStorage.inst());
        saver.addExitSavable(ArchiveMessageStorage.inst());
        saver.addExitSavable(IdentitiesStorage.inst());
        saver.addExitSavable(FileListStorage.inst());

        // invoke the mainframe ticker (board updates, clock, ...)
        mainframe.startTickerThread();

        // start file attachment uploads
        FileAttachmentUploadThread.getInstance().start();

        // start all filetransfer tickers
        getFileTransferManager().startTickers();

        // after X seconds, start filesharing threads if enabled
        if( isFreenetOnline() && !frostSettings.getBoolValue(SettingsClass.DISABLE_FILESHARING)) {
            final Thread t = new Thread() {
                @Override
                public void run() {
                    Mixed.wait(10000);
                    FileSharingManager.startFileSharing();
                }
            };
            t.start();
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
            final File f = new File(Frost.getCmdLineLocaleFileName());
            Language.initializeWithFile(f);
        } else if (Frost.getCmdLineLocaleName() != null) {
            // use locale specified on command line (overrides config setting)
            Language.initializeWithName(Frost.getCmdLineLocaleName());
        } else {
            // use config file parameter (format: de or de;ext
            final String lang = frostSettings.getValue(SettingsClass.LANGUAGE_LOCALE);
            final String langIsExternal = frostSettings.getValue("localeExternal");
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

    public void showAutoSaveError(final Exception exception) {
        final StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (mainFrame != null) {
                    JDialogWithDetails.showErrorDialog(
                            mainFrame,
                            language.getString("Saver.AutoTask.title"),
                            language.getString("Saver.AutoTask.message"),
                            stringWriter.toString());
                    System.exit(3);
                }
            }
        });
    }

    public static boolean isHelpHtmlSecure() {
        return isHelpHtmlSecure;
    }
}
