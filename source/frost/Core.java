/*
  Core.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import frost.crypt.*;
import frost.ext.JSysTrayIcon;
import frost.fcp.*;
import frost.fileTransfer.download.DownloadManager;
import frost.fileTransfer.search.SearchManager;
import frost.fileTransfer.upload.UploadManager;
import frost.gui.Splashscreen;
import frost.gui.objects.*;
import frost.identities.FrostIdentities;
import frost.messages.*;
import frost.storage.*;
import frost.storage.Savable;
import frost.threads.*;
import frost.threads.maintenance.*;
import frost.util.FlexibleObserver;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * Class hold the more non-gui parts of frame1.java.
 */

public class Core implements Savable {
	
	private static Logger logger = Logger.getLogger(Core.class.getName());

	private static Core instance = new Core();
	private static Locale locale = null;
	
	private static Set nodes = new HashSet(); //list of available nodes
	private static Set messageSet = new HashSet(); // set of message digests
	private static SortedSet knownBoards = new TreeSet(); //list of known boards
	private static NotifyByEmailThread emailNotifier = null;
	private UpdatingLanguageResource languageResource = null;
	
	private boolean freenetIsOnline = false;
	private boolean freenetIsTransient = false;
	
	private Timer timer = new Timer(true);

	public static SettingsClass frostSettings = new SettingsClass();
	static Hashtable myBatches = new Hashtable();

	private static Crypt crypto = new FrostCrypt();
	
	private MainFrame mainFrame;
	private SearchManager searchManager;
	private DownloadManager downloadManager;
	private UploadManager uploadManager;
	
	private static CleanUp fileCleaner = new CleanUp("keypool", false);
	
	private FrostIdentities identities;
	private String keypool;

	private Core() {
		
	}
	
	/**
	 * This methods parses the list of available nodes (and converts it if it is in
	 * the old format). If there are no available nodes, it shows a Dialog warning the
	 * user of the situation and returns false.
	 * @return boolean false if no nodes are available. True otherwise.
	 */
	private boolean initializeConnectivity() {
		// First of all we parse the list of available nodes
		String nodesUnparsed = frostSettings.getValue("availableNodes");

		if (nodesUnparsed == null) { //old format
			String converted =
				new String(
					frostSettings.getValue("nodeAddress")
						+ ":"
						+ frostSettings.getValue("nodePort"));
			nodes.add(converted.trim());
			frostSettings.setValue("availableNodes", converted.trim());
		} else { // new format
			String[] _nodes = nodesUnparsed.split(",");
			for (int i = 0; i < _nodes.length; i++)
				nodes.add(_nodes[i]);
		}
		if (nodes.size() == 0) {
			MiscToolkit.getInstance().showMessage(
				"Not a single Freenet node configured. You need at least one.",
				JOptionPane.ERROR_MESSAGE,
				"ERROR: No Freenet nodes are available.");
			return false;
		}
		logger.info("Frost will use " + nodes.size() + " Freenet nodes");

		// Then we check if the user is running a transient node or not
		try {
			FcpConnection con1 = FcpFactory.getFcpConnectionInstance();
			if (con1 != null) {
				String[] nodeInfo = con1.getInfo();
				// freenet is online
				freenetIsOnline = true;
				for (int ij = 0; ij < nodeInfo.length; ij++) {
					if (nodeInfo[ij].startsWith("IsTransient")
						&& nodeInfo[ij].indexOf("true") != -1) {
						freenetIsTransient = true;
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in initializeConnectivity", e);
		}

		// We warn the user if there aren't any running nodes
		if (!freenetIsOnline) {
			MiscToolkit.getInstance().showMessage(
				getLanguageResource().getString("Core.init.NodeNotRunningBody"),
				JOptionPane.WARNING_MESSAGE,
				getLanguageResource().getString("Core.init.NodeNotRunningTitle"));
		}

		// We warn the user if the only node that is running is transient
		if (isFreenetTransient() && nodes.size() == 1) {
			MiscToolkit.getInstance().showMessage(
				getLanguageResource().getString("Core.init.TransientNodeBody"),
				JOptionPane.WARNING_MESSAGE,
				getLanguageResource().getString("Core.init.TransientNodeTitle"));
		}

		return true;
	}

	public boolean isFreenetOnline() {
		return freenetIsOnline;
	}
	public boolean isFreenetTransient() {
		return freenetIsTransient;
	}
	
	
    private void loadHashes()
    {
        File hashes = new File("hashes");
        if (hashes.exists())
        	try{
        		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(hashes));
        		messageSet = (HashSet)ois.readObject();
        		logger.info("loaded "+messageSet.size() +" message hashes");	
        		ois.close();
        	} catch(Throwable t){
				logger.log(Level.SEVERE, "Exception thrown in loadHashes()", t);
        	}
    }
    
	private boolean saveHashes() {
		try {
			synchronized (getMessageSet()) {
				File hashes = new File("hashes");
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(hashes));
				oos.writeObject(Core.getMessageSet());
				return true;
			}
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Exception thrown in saveHashes()", t);
		}
		return false;
	}
    
    private void loadKnownBoards()
    {
		// load the known boards
		// just a flat list in xml
		File boards = new File("boards");
		if(boards.exists()) // old style file, 1 time conversion
        {
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
				FrostBoardObject bo = new FrostBoardObject(bname, bpubkey, bprivkey, null);
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
    
    private void loadBatches()
    {
        //load the batches
        File batches = new File("batches");
        if (batches.exists() && batches.length() > 0) //fix previous version bug
        	try {
        		String allBatches = FileAccess.readFileRaw(batches);
        		String[] _batches = allBatches.split("_");
        		//dumb.  will fix later
        
        		for (int i = 0; i < _batches.length; i++)
                {
                    myBatches.put(_batches[i], _batches[i]);
                }
        
        		logger.info("loaded " + _batches.length + " batches of shared files");
        	} catch (Throwable e) {
				logger.log(Level.SEVERE, "couldn't load batches:", e);
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
    
	public static Crypt getCrypto() {
		return crypto;
	}
	/**
	 * @return
	 */
	public static Hashtable getMyBatches() {
		return myBatches;
	}

	/**
	   * Tries to send old messages that have not been sent yet
	   */
	protected void resendFailedMessages() {
		// start a thread that waits some seconds for gui to appear, then searches for
		// unsent messages
		ResendFailedMessagesThread t =
			new ResendFailedMessagesThread(this, MainFrame.getInstance());
		t.start();
	}

	public void deleteDir(String which) {
		(new DeleteWholeDirThread(this, which)).start();
	}

	public void startTruster(boolean what, FrostMessageObject which) {
		new Truster(getIdentities(), Boolean.valueOf(what), which.getFrom()).start();
	}

	public void startTruster(FrostMessageObject which) {
		new Truster(getIdentities(), null, which.getFrom()).start();
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
	 * @return list of nodes Frost is using
	 */
	public static Set getNodes() {
		return nodes;
	}

	/**
	 * @return list of known boards
	 */
	public static SortedSet getKnownBoards() {
		return knownBoards;
	}
    
    /**
     * Called with a list of BoardAttachments, should add all boards
     * that are not contained already
     */
    public static void addNewKnownBoards( List lst )
    {
        if( lst == null || lst.size() == 0 )
            return;
            
        Iterator i = lst.iterator();
        while(i.hasNext())
        {
            BoardAttachment newba = (BoardAttachment)i.next();
            
            String bname = newba.getBoardObj().getBoardName();
            String bprivkey = newba.getBoardObj().getPrivateKey();
            String bpubkey = newba.getBoardObj().getPublicKey();
            
            boolean addMe = true;
            synchronized(getKnownBoards())
            {
                Iterator j = getKnownBoards().iterator();
                while(j.hasNext())
                {
                    BoardAttachment board = (BoardAttachment)j.next();
                    if( board.getBoardObj().getBoardName().equalsIgnoreCase(bname) &&
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
            if( addMe )
            {
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
	 * 
	 */
	public void initialize() throws Exception {
		Splashscreen splashscreen = new Splashscreen();
		splashscreen.setVisible(true);

		keypool = frostSettings.getValue("keypool.dir");

		splashscreen.setText(getLanguageResource().getString("Initializing Mainframe"));
		splashscreen.setProgress(20);

		//Initializes the logging and skins
		new Logging(frostSettings);
		initializeSkins();

		splashscreen.setText(getLanguageResource().getString("Hypercube fluctuating!"));
		splashscreen.setProgress(50);

		if (!initializeConnectivity()) {
			System.exit(1);
		}

		// CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
		Startup.startupCheck(frostSettings, keypool);
		FileAccess.cleanKeypool(keypool);

		getIdentities().initialize(freenetIsOnline);
		
		splashscreen.setText(getLanguageResource().getString("Sending IP address to NSA"));
		splashscreen.setProgress(60);

		//Main frame		
		mainFrame = new MainFrame(frostSettings, getLanguageResource());
		getDownloadManager().initialize();
		getUploadManager().initialize();
		getSearchManager().initialize();
		
		//Until the downloads and uploads are fully separated from frame1:
		mainFrame.setDownloadTicker(getDownloadManager().getTicker());
		mainFrame.setDownloadModel(getDownloadManager().getModel());
		mainFrame.setUploadTicker(getUploadManager().getTicker());
		mainFrame.setUploadPanel(getUploadManager().getPanel());
		mainFrame.initialize();

		splashscreen.setText(getLanguageResource().getString("Wasting more time"));
		splashscreen.setProgress(70);

		//load vital data
		loadBatches();
		loadKnownBoards();
		loadHashes();

		// Start tofTree
		if (isFreenetOnline()) {
			resendFailedMessages();
		}

		//TODO: check if email notification is on and instantiate the emailNotifier
		//of course it needs to be added as a setting first ;-p

		Thread requestsThread =
			new GetRequestsThread(
				frostSettings.getIntValue("tofDownloadHtl"),
				frostSettings.getValue("keypool.dir"),
				getUploadManager().getModel(),
				getIdentities());
		requestsThread.start();
		
		initializeTasks(mainFrame);

		splashscreen.setText(getLanguageResource().getString("Reaching ridiculous speed..."));
		splashscreen.setProgress(80);

		mainFrame.setVisible(true);

		splashscreen.closeMe();

		// Display the tray icon
		if (frostSettings.getBoolValue("showSystrayIcon") == true) {
			if (JSysTrayIcon.createInstance(0, "Frost", "Frost") == false) {
				logger.severe("Could not create systray icon.");
			}
		}
	}

	/**
	 * 
	 */
	private UploadManager getUploadManager() {
		if (uploadManager == null) {
			uploadManager = new UploadManager(getLanguageResource(), frostSettings);
			uploadManager.setMainFrame(mainFrame);
			uploadManager.setTofTree(mainFrame.getTofTree());
			uploadManager.setFreenetIsOnline(isFreenetOnline());
			uploadManager.setMyID(getIdentities().getMyId());
		}
		return uploadManager;
	}

	/**
	 * 
	 */
	private SearchManager getSearchManager() {
		if (searchManager == null) {
			searchManager = new SearchManager(getLanguageResource(), frostSettings);
			searchManager.setMainFrame(mainFrame);
			searchManager.setDownloadModel(getDownloadManager().getModel());
			searchManager.setUploadModel(getUploadManager().getModel());
			searchManager.setTofTree(mainFrame.getTofTree());
			searchManager.setKeypool(keypool);
			searchManager.setIdentities(getIdentities());
		}
		return searchManager;
	}
	
	/**
	 * 
	 */
	private DownloadManager getDownloadManager() {
		if (downloadManager == null) {
			downloadManager = new DownloadManager(getLanguageResource(), frostSettings);
			downloadManager.setMainFrame(mainFrame);
			downloadManager.setFreenetIsOnline(isFreenetOnline());
		}
		return downloadManager;
	}

	/**
	 * @param parentFrame the frame that will be the parent of any
	 * 			dialog that has to be shown in case an error happens
	 * 			in one of those tasks
	 */
	private void initializeTasks(JFrame parentFrame) {
		//We initialize the task that checks for spam
		timer.schedule(
			new CheckForSpam(this),
			0,
			frostSettings.getIntValue("sampleInterval") * 60 * 60 * 1000);

		//We initialize the tash that discards old files and frees memory
		TimerTask cleaner = new TimerTask() {
			int i = 0;
			public void run() {
				// maybe each 6 hours cleanup files (12 * 30 minutes)
				if (i == 12 && frostSettings.getBoolValue("doCleanUp")) {
					i = 0;
					logger.info("discarding old files");
					fileCleaner.doCleanup();
				}
				logger.info("freeing memory");
				System.gc();
				i++;
			}
		};
		timer.schedule(cleaner, 30 * 60 * 1000, 30 * 60 * 1000);	//30 minutes

		//We initialize the task that saves data
		
		Saver saver = new Saver(frostSettings, languageResource, parentFrame);
		saver.addAutoSavable(this);
		saver.addAutoSavable(getIdentities());
		saver.addAutoSavable(MainFrame.getInstance().getTofTree());
		saver.addAutoSavable(getDownloadManager().getModel());
		saver.addAutoSavable(getUploadManager().getModel());
		saver.addExitSavable(this);
		saver.addExitSavable(getIdentities());
		saver.addExitSavable(MainFrame.getInstance().getTofTree());
		saver.addExitSavable(getDownloadManager().getModel());
		saver.addExitSavable(getUploadManager().getModel());
		saver.addExitSavable(frostSettings);
					
		// We initialize the task that helps requests of friends
		if (frostSettings.getBoolValue("helpFriends"))
			timer.schedule(new GetFriendsRequestsThread(identities), 5 * 60 * 1000, 3 * 60 * 60 * 1000);
	}

	/**
	 * @return the set of message hashes
	 */
	public static Set getMessageSet() {
		return messageSet;
	}

	/**
	 * @return the thread that will notify the user for email
	 */
	public static FlexibleObserver getEmailNotifier(){
		return emailNotifier;
	}

	/**
	 * @param locale
	 */
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

	/**
 	 * description
 	 * 
 	 * @return description
 	 */
	public FrostIdentities getIdentities() {
		if (identities == null) {
			identities = new FrostIdentities(getLanguageResource());
		}
		return identities;
	}

	/* (non-Javadoc)
  	 * @see frost.threads.maintenance.Savable#save()
 	 */
	public void save() throws StorageException {
		boolean saveOK;
		saveOK = saveBatches();
		saveOK &= saveHashes();
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
	 * @return the language resource to get internationalized messages from.
	 */
	public UpdatingLanguageResource getLanguageResource() {
		if (languageResource == null) {
			if (locale != null) {
				languageResource = new UpdatingLanguageResource("res.LangRes", locale);
			} else {
				String language = frostSettings.getValue("locale");
				if (!language.equals("default")) {
					languageResource = new UpdatingLanguageResource("res.LangRes", new Locale(language));
				} else {
					languageResource = new UpdatingLanguageResource("res.LangRes");
				}
			}
		}
		return languageResource;
	}

}
