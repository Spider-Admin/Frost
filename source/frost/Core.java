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

import frost.boards.*;
import frost.crypt.*;
import frost.events.*;
import frost.ext.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.gui.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.messaging.*;
import frost.storage.*;
import frost.threads.*;
import frost.threads.maintenance.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * Class hold the more non-gui parts of frame1.java.
 * 
 * @pattern Singleton
 * 
 * @author $Author$
 * @version $Revision$
 */
public class Core implements Savable, FrostEventDispatcher  {
	
	private static Logger logger = Logger.getLogger(Core.class.getName());
	
	static Hashtable myBatches = new Hashtable();
	
	private static Core instance = new Core();
	private static Locale locale = null;
	
	private static Set nodes = new HashSet(); //list of available nodes
	private static List knownBoards = new ArrayList(); //list of known boards
	private static NotifyByEmailThread emailNotifier = null;
	
	public static SettingsClass frostSettings;
	
	private static Crypt crypto = new FrostCrypt();
	
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
	
	
	private EventDispatcher dispatcher = new EventDispatcher();
	private Language language = null;
	
	private boolean freenetIsOnline = false;
	private boolean freenetIsTransient = false;
	
	private Timer timer = new Timer(true);
	
	private MainFrame mainFrame;
	private BoardsManager boardsManager;
	private FileTransferManager fileTransferManager;
	private MessagingManager messagingManager;
	
	private FrostIdentities identities;
	private String keypool;

	/**
	 * 
	 */
	private Core() {
		frostSettings = new SettingsClass();
		initializeLanguage();
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
				language.getString("Core.init.NodeNotRunningBody"),
				JOptionPane.WARNING_MESSAGE,
				language.getString("Core.init.NodeNotRunningTitle"));
		}

		// We warn the user if the only node that is running is transient
		if (isFreenetTransient() && nodes.size() == 1) {
			MiscToolkit.getInstance().showMessage(
				language.getString("Core.init.TransientNodeBody"),
				JOptionPane.WARNING_MESSAGE,
				language.getString("Core.init.TransientNodeTitle"));
		}

		return true;
	}

	public boolean isFreenetOnline() {
		return freenetIsOnline;
	}
	
	public boolean isFreenetTransient() {
		return freenetIsTransient;
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
	 * @return list of nodes Frost is using
	 */
	public static Set getNodes() {
		return nodes;
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
    public static void addNewKnownBoards( List lst )
    {
        if( lst == null || lst.size() == 0 )
            return;
            
        Iterator i = lst.iterator();
        while(i.hasNext())
        {
            BoardAttachment newba = (BoardAttachment)i.next();
            
            String bname = newba.getBoardObj().getName();
            String bprivkey = newba.getBoardObj().getPrivateKey();
            String bpubkey = newba.getBoardObj().getPublicKey();
            
            boolean addMe = true;
            synchronized(getKnownBoards())
            {
                Iterator j = getKnownBoards().iterator();
                while(j.hasNext())
                {
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
              tempMsg = FrostMessageFactory.createFrostMessageObject(msgFile);
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

        // CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
        Startup.startupCheck(frostSettings, keypool);
//        FileAccess.cleanKeypool(keypool); // not longer needed
        if( frostSettings.getBoolValue("deleteExpiredMessages") ) {
            File keypoolFolder = new File(keypool);
            CleanUp.deleteExpiredFiles(keypoolFolder);
            CleanUp.deleteEmptyBoardDateDirs(keypoolFolder);
        }
        
		splashscreen.setText(language.getString("Hypercube fluctuating!"));
		splashscreen.setProgress(40);

		if (!initializeConnectivity()) {
			System.exit(1);
		}

        // TODO: one time convert, remove later (added: 2005-09-02)

        // check if this is a first time startup and maybe skip conversion
        File identitiesFile = new File("identities.xml");
        if( identitiesFile.exists() == false || identitiesFile.length() == 0 ) {
            frostSettings.setValue("oneTimeUpdate.convertSigs.didRun", true);
            frostSettings.setValue("oneTimeUpdate.repairIdentities.didRun", true);
        }

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

		//Main frame		
		mainFrame = new MainFrame(frostSettings);
		getMessagingManager().initialize();
		getBoardsManager().initialize();
		getFileTransferManager().initialize();

		splashscreen.setText(language.getString("Wasting more time"));
		splashscreen.setProgress(70);

        mainFrame.initialize();

		//load vital data
		loadBatches();
		loadKnownBoards();

		// Start tofTree
		if (isFreenetOnline()) {
			resendFailedMessages();
		}

		//TODO: check if email notification is on and instantiate the emailNotifier
		//of course it needs to be added as a setting first ;-p

        splashscreen.setText(language.getString("Reaching ridiculous speed..."));
        splashscreen.setProgress(80);

		initializeTasks(mainFrame);

        // Display the tray icon
        if (frostSettings.getBoolValue("showSystrayIcon") == true) {
            if (JSysTrayIcon.createInstance(0, "Frost", "Frost") == false) {
                logger.severe("Could not create systray icon.");
            }
        }

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainFrame.setVisible(true);	
			}
		});

		splashscreen.closeMe();
	}

	/**
	 * @return
	 */
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
	
	/**
	 * 
	 */
	private MessagingManager getMessagingManager() {
		if (messagingManager == null) {
			messagingManager = new MessagingManager(frostSettings);
		}
		return messagingManager;
	}
	
	/**
	 * 
	 */
	private BoardsManager getBoardsManager() {
		if (boardsManager == null) {
			boardsManager = new BoardsManager(frostSettings);
			boardsManager.setMainFrame(mainFrame);
			boardsManager.setCore(this);
			boardsManager.setMessageHashes(getMessagingManager().getMessageHashes());
		}
		return boardsManager;
	}
	
	/**
	 * @param parentFrame the frame that will be the parent of any
	 * 			dialog that has to be shown in case an error happens
	 * 			in one of those tasks
	 */
	private void initializeTasks(JFrame parentFrame) {
		//We initialize the task that checks for spam
		timer.schedule(
			new CheckForSpam(frostSettings, getBoardsManager().getTofTree(), getBoardsManager().getTofTreeModel()),
			0,
			frostSettings.getIntValue("sampleInterval") * 60 * 60 * 1000);

		//We initialize the tash that discards old files and frees memory
		TimerTask cleaner = new TimerTask() {
			int i = 0;
			public void run() {
				// maybe each 6 hours cleanup files (12 * 30 minutes)
				if (i >= 12 && frostSettings.getBoolValue("deleteExpiredMessages")) {
					logger.info("discarding expired files");
                    CleanUp.deleteExpiredFiles(new File(keypool));
				}
                // free memory each hour
                if( i % 2 == 0 ) {
                    logger.info("freeing memory");
                    System.gc();
                }
                if( i >= 12 ) { // reset
                    i = 0;
                }
				i++;
			}
		};
		timer.schedule(cleaner, 30 * 60 * 1000, 30 * 60 * 1000);	//30 minutes

		//We initialize the task that saves data
		
		StorageManager saver = new StorageManager(frostSettings, this);
		saver.addAutoSavable(this);
		saver.addAutoSavable(getIdentities());
		saver.addAutoSavable(getMessagingManager().getMessageHashes());
		saver.addAutoSavable(getBoardsManager().getTofTree());
		saver.addAutoSavable(getFileTransferManager());
		saver.addExitSavable(this);
		saver.addExitSavable(getIdentities());
		saver.addExitSavable(getMessagingManager().getMessageHashes());
		saver.addExitSavable(getBoardsManager().getTofTree());
		saver.addExitSavable(getFileTransferManager());
		saver.addExitSavable(frostSettings);
					
		// We initialize the task that helps requests of friends
		if (frostSettings.getBoolValue("helpFriends"))
			timer.schedule(new GetFriendsRequestsThread(identities), 5 * 60 * 1000, 3 * 60 * 60 * 1000);
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
				Language.initialize("res.LangRes", locale);
			} else {
				String language = frostSettings.getValue("locale");
				if (!language.equals("default")) {
					Language.initialize("res.LangRes", new Locale(language));
				} else {
					Language.initialize("res.LangRes");
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
}

