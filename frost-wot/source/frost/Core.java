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
import java.util.logging.*;

import javax.swing.*;

import org.w3c.dom.*;

import com.l2fprod.gui.plaf.skin.*;

import frost.FcpTools.*;
import frost.crypt.*;
import frost.ext.JSysTrayIcon;
import frost.gui.Splashscreen;
import frost.gui.components.MiscToolkit;
import frost.gui.objects.*;
import frost.gui.translation.UpdatingLanguageResource;
import frost.identities.*;
import frost.messages.*;
import frost.threads.*;
import frost.threads.maintenance.*;

/**
 * Class hold the more non-gui parts of frame1.java.
 */

public class Core {
	
	private static Logger logger = Logger.getLogger(Core.class.getName());

	private static Core instance = new Core();
	private static boolean initialized = false;
	private static Locale locale = null;
	
	private static Set nodes = new HashSet(); //list of available nodes
	private static Set messageSet = new HashSet(); // set of message digests
	private static SortedSet knownBoards = new TreeSet(); //list of known boards
	private static NotifyByEmailThread emailNotifier = null;
	private UpdatingLanguageResource languageResource = null;
	
	private boolean freenetIsOnline = false;
	private boolean freenetIsTransient = false;
	
	private Core() {
		
	}
	
	/**
	 *	This method checks whether the user is running a transient node or not.
	 */
	private void checkTransient() {
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
			logger.log(Level.SEVERE, "Exception thrown in checkTransient", e);
		}		
	}

	/**
	 * This methods parses the list of available nodes (and converts it if it is in
	 * the old format). If there are no available nodes, it shows a Dialog warning the
	 * user of the situation and returns false.
	 * @return boolean false if no nodes are available. True otherwise.
	 */
	private boolean initializeNodes() {
		//parse the list of available nodes
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
							"ERROR: No Freenet nodes are available");
			return false;
		}
		logger.info("Frost will use " + nodes.size() + " Freenet nodes");
		return true;
	}

	/**
	 * This method initializes the language. If the locale field has a value, it
	 * is used to select the LanguageResource. If not, the locale value in frostSettings 
	 * is used for that.
	 */
	private void initializeLanguage() {
		if (locale != null) {
			languageResource = new UpdatingLanguageResource("res.LangRes", locale);
		} else {
			String language = frostSettings.getValue("locale");
			if (!language.equals("default")) {
				languageResource =
					new UpdatingLanguageResource("res.LangRes", new Locale(language));
			} else {
				languageResource = new UpdatingLanguageResource("res.LangRes");
			}
		}
	}

	private static CleanUp fileCleaner = new CleanUp("keypool", false);
	public ObjectOutputStream id_writer;
	
	public boolean isFreenetOnline() {
		return freenetIsOnline;
	}
	public boolean isFreenetTransient() {
		return freenetIsTransient;
	}
	
	
	private void loadIdentities() {
		goodIds = new Hashtable();
		badIds = new Hashtable();
		myBatches = new Hashtable();
		friends = new BuddyList();
		enemies = new BuddyList();
		neutral = new BuddyList();
		File identities = new File("identities");
		File identitiesxml = new File("identities.xml");
		try {

			if (identities.length() == 0)
				identities.delete();
			if (identitiesxml.length() == 0)
				identitiesxml.delete();
			if (identities.createNewFile() && identitiesxml.createNewFile()) {
				if (isFreenetOnline() == false) {
					JOptionPane.showMessageDialog(
						frame1.getInstance(),
						languageResource.getString(
							"Core.loadIdentities.ConnectionNotEstablishedBody"),
						languageResource.getString(
							"Core.loadIdentities.ConnectionNotEstablishedTitle"),
						JOptionPane.ERROR_MESSAGE);
					System.exit(2);
				}
				//create new identities
				try {
					String nick = null;
					do {
						nick =
							JOptionPane.showInputDialog(
								languageResource.getString("Core.loadIdentities.ChooseName"));
						if (!(nick == null || nick.length() == 0)) {
							// check for a '@' in nick, this is strongly forbidden
							if (nick.indexOf("@") > -1) {
								JOptionPane.showMessageDialog(
									frame1.getInstance(),
									languageResource.getString(
										"Core.loadIdentities.InvalidNameBody"),
									languageResource.getString(
										"Core.loadIdentities.InvalidNameTitle"),
									JOptionPane.ERROR_MESSAGE);
								nick = "";
							}
						}

					} while (nick != null && nick.length() == 0);
					if (nick == null) {
						logger.severe("Frost can't run without an identity.");
						System.exit(1);
					}

					do { //make sure there's no // in the name.
						mySelf = new LocalIdentity(nick);
					} while (mySelf.getUniqueName().indexOf("//") != -1);

					//JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
				} catch (Exception e) {
					logger.severe("couldn't create new identitiy" + e.toString());
				}
				//friends = new BuddyList();

				if (friends.Add(Core.getMyId())) {
					logger.info("added myself to list");
				}
				//enemies = new BuddyList();
			} else
				//first try with the new format
				if (identitiesxml.exists()) {
					//friends = new BuddyList();
					//enemies = new BuddyList();
					try {
						logger.info("trying to create/load ids");
						Document d = XMLTools.parseXmlFile("identities.xml", false);
						Element rootEl = d.getDocumentElement();
						//first myself
						Element myself =
							(Element) XMLTools.getChildElementsByTagName(rootEl, "MyIdentity").get(
								0);
						mySelf = new LocalIdentity(myself);

						//then friends
						List lists = XMLTools.getChildElementsByTagName(rootEl, "BuddyList");
						Iterator it = lists.iterator();
						while (it.hasNext()) {
							Element current = (Element) it.next();
							if (current.getAttribute("type").equals("friends"))
								friends.loadXMLElement(current);
							else if (current.getAttribute("type").equals("enemies"))
								enemies.loadXMLElement(current);
							else
								neutral.loadXMLElement(current);
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Exception thrown in loadIdentities()", e);
					}
					logger.info("loaded " + friends.size() + " friends and " 
										  + enemies.size() + " enemies and "
										  + neutral.size() + " neutrals.");
					if (friends.Add(Core.getMyId()))
						logger.info("added myself to list");

				} else {
					try {

						BufferedReader fin = new BufferedReader(new FileReader(identities));
						String name = fin.readLine();
						String address = fin.readLine();
						String keys[] = new String[2];
						keys[1] = fin.readLine();
						keys[0] = fin.readLine();
						if (address.startsWith("CHK@") == false) {
							// pubkey chk was not successfully computed
							byte[] pubkeydata;
							try {
								pubkeydata = keys[1].getBytes("UTF-8");
							} catch (UnsupportedEncodingException ex) {
								pubkeydata = keys[1].getBytes();
							}

							String tmp = FecTools.generateCHK(pubkeydata);
							address = tmp.substring(tmp.indexOf("CHK@"), tmp.indexOf("CHK@") + 58);
							logger.info("Re-calculated my public key CHK: " + address + "\n");

						}
						mySelf = new LocalIdentity(name, keys);
						logger.info("loaded myself with name " + mySelf.getName());
						//out.println("and public key" + mySelf.getKey());

						//take out the ****
						fin.readLine();

						//process the friends
						logger.info("loading friends");
						friends = new BuddyList();
						boolean stop = false;
						String key;
						while (!stop) {
							name = fin.readLine();
							if (name == null || name.startsWith("***"))
								break;
							address = fin.readLine();
							key = fin.readLine();
							friends.Add(new Identity(name, key));
						}
						logger.info("loaded " + friends.size() + " friends");

						//just the good ids
						while (!stop) {
							String id = fin.readLine();
							if (id == null || id.startsWith("***"))
								break;
							goodIds.put(id, id);
						}
						logger.info("loaded " + goodIds.size() + " good ids");

						//and the enemies
						enemies = new BuddyList();
						logger.info("loading enemies");
						while (!stop) {
							name = fin.readLine();
							if (name == null || name.startsWith("***"))
								break;
							address = fin.readLine();
							key = fin.readLine();
							enemies.Add(new Identity(name, key));
						}
						logger.info("loaded " + enemies.size() + " enemies");

						//and the bad ids
						while (!stop) {
							String id = fin.readLine();
							if (id == null || id.startsWith("***"))
								break;
							badIds.put(id, id);
						}
						logger.info("loaded " + badIds.size() + " bad ids");

					} catch (IOException e) {
						logger.severe("IOException :" + e.toString());
						friends = new BuddyList();
						enemies = new BuddyList();
						friends.Add(mySelf);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Exception thrown in loadIdentities()", e);
					}
				}

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception thrown in loadIdentities()", e);
		}
		logger.info("ME = '" + getMyId().getUniqueName() + "'");
	}
    
	public void saveIdentities() {
		logger.info("saving identities.xml");

		String identitiesName = "identities.xml";
		String identitiesTmpName = "identities.xml.tmp";
		String identitiesBakName = "identities.xml.bak";

		//First we copy "identities.xml" to "identities.xml.bak"
		File identitiesFile = new File(identitiesName);
		if (identitiesFile.exists()) {
			File bakFile = new File(identitiesBakName);
			bakFile.delete();
			try {
				FileAccess.copyFile(identitiesName, identitiesBakName);
			} catch (IOException exception) {
				logger.log(Level.SEVERE, "Error while saving identities.xml", exception);
			}
		}

		//We delete "identities.xml.tmp"
		File identitiesTmpFile = new File(identitiesTmpName);
		if (identitiesTmpFile.exists()) {
			identitiesTmpFile.delete();
		}

		Document d = XMLTools.createDomDocument();
		Element rootElement = d.createElement("FrostIdentities");
		//first save myself
		rootElement.appendChild(Core.getMyId().getXMLElement(d));
		//then friends
		Element friends = Core.getFriends().getXMLElement(d);
		friends.setAttribute("type", "friends");
		rootElement.appendChild(friends);
		//then enemies 
		Element enemies = Core.getEnemies().getXMLElement(d);
		enemies.setAttribute("type", "enemies");
		rootElement.appendChild(enemies);
		//then everybody else
		Element neutral = Core.getNeutral().getXMLElement(d);
		neutral.setAttribute("type", "neutral");
		rootElement.appendChild(neutral);
		d.appendChild(rootElement);

		//We save identities to "identities.xml.tmp"
		if (XMLTools.writeXmlFile(d, identitiesTmpName)) {
			//Success	
			if (identitiesTmpFile.exists()) {
				//We replace "identities.xml" by "identities.xml.tmp"
				identitiesFile.delete();
				if (!identitiesTmpFile.renameTo(identitiesFile)) {
					//Replacement failed. We try to restore "identities.xml" from "identities.xml.bak"
					try {
						FileAccess.copyFile(identitiesBakName, identitiesName);
					} catch (IOException exception) {
						//Uh, oh, we are having a bad, bad day.
						logger.log(Level.SEVERE, "Error while restoring identities.xml", exception);
					}
				}
			} else {
				//This shouldn't happen, but...
				logger.severe("Could not save identities.xml");
			}
		} else {
			//Failure
			logger.severe("Could not save identities.xml");
		}
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
    
    public void saveHashes()
    {
        try {
            synchronized( getMessageSet() )
            {
                File hashes = new File("hashes");
                ObjectOutputStream oos =
                    new ObjectOutputStream(new FileOutputStream(hashes));
                oos.writeObject(Core.getMessageSet());
            }
        }
        catch (Throwable t)
        {
			logger.log(Level.SEVERE, "Exception thrown in saveHashes()", t);
        }
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
    
    private void loadOLDKnownBoards(File boards)
    {
        try {
            ArrayList tmpList = new ArrayList();
            String allBoards = FileAccess.readFile(boards);
            String []_boards = allBoards.split(":");
            for (int i=0;i<_boards.length;i++)
            {
                String aboardstr = _boards[i].trim();
                if( aboardstr.length() < 13 || aboardstr.indexOf("*") < 3 ||
                    ! ( aboardstr.indexOf("*") < aboardstr.lastIndexOf("*") ) )
                {
                    continue;
                }
                String bname, bpubkey, bprivkey;
                int pos = aboardstr.indexOf("*");
                bname = aboardstr.substring(0, pos).trim();
                int pos2 = aboardstr.indexOf("*", pos+1);
                bpubkey = aboardstr.substring(pos+1, pos2).trim();
                bprivkey = aboardstr.substring(pos2+1).trim();
                if( bpubkey.length() < 10 )  bpubkey = null;
                if( bprivkey.length() < 10 )  bprivkey = null;
                
                // create BoardAttachment objects and pass them to add method
                // which checks for doubles
                FrostBoardObject bo = new FrostBoardObject(bname, bpubkey, bprivkey);
                BoardAttachment ba = new BoardAttachment(bo);
                tmpList.add( ba );
            }
            logger.info("Loaded "+ _boards.length +" OLD known boards (converting).");
            addNewKnownBoards(tmpList);
        }catch (Throwable t){
			logger.log(Level.SEVERE, "couldn't load/convert OLD known boards", t);
        }
        
        if( boards.renameTo(new File( "boards.old")) == false )
        {
            boards.delete(); // paranoia
        }
    }
    
    public void saveKnownBoards()
    {
        Document doc = XMLTools.createDomDocument();
        if( doc == null )
        {
            logger.severe("Error - saveBoardTree: factory could'nt create XML Document.");
            return;
        }
        
        Element rootElement = doc.createElement("FrostKnownBoards");
        doc.appendChild(rootElement);
        
        synchronized( getKnownBoards() )
        {
            Iterator i = getKnownBoards().iterator();
            while(i.hasNext())
            {
                BoardAttachment current = (BoardAttachment)i.next();
                Element anAttachment = current.getXMLElement(doc);
                rootElement.appendChild(anAttachment);
            }
        }
        
        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, "knownboards.xml");
        }
        catch(Throwable ex) {
			logger.log(Level.SEVERE, "Exception while writing knownboards.xml:", ex);
        }
        if( !writeOK )
        {
            logger.severe("Error while writing knownboards.xml, file was not saved");
        }
        else
        {
            logger.info("Saved "+getKnownBoards().size()+" known boards.");
        }            
        
/*        try {
            StringBuffer buf = new StringBuffer();
            synchronized( getKnownBoards() )
            {
                Iterator i = getKnownBoards().iterator();
                while (i.hasNext())
                {
                    String current = (String)i.next();
                    buf.append(current);
                    if( i.hasNext() )
                    {
                        buf.append(":");
                    }
                }
            }
            File boards = new File("boards");
            FileAccess.writeFile(buf.toString(), boards);
        }
        catch (Throwable t) {
            t.printStackTrace(Core.getOut());
        }
*/        
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
    
    public void saveBatches()
    {
        try {
            StringBuffer buf = new StringBuffer();
            synchronized( getMyBatches() )
            {
                Iterator i = getMyBatches().keySet().iterator();
                while (i.hasNext())
                {
                    String current = (String)i.next();
                    if (current.length() > 0)
                    {
                        buf.append(current);
                        if( i.hasNext() )
                        {
                            buf.append("_");
                        }
                    }
                    else
                    {
                        i.remove(); //make sure no empty batches are saved
                    }
                }
            }
            File batches = new File("batches");
            FileAccess.writeFile(buf.toString(), batches);
        }
        catch (Throwable t) {
			logger.log(Level.SEVERE, "Exception thrown in saveBatches():", t);
        }
    }
    
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	java.util.Timer timer; // Uploads / Downloads
	java.util.Timer timer2;

	public static LocalIdentity mySelf;
	//------------------------------------------------------------------------
	// end-of: Generate objects
	//------------------------------------------------------------------------

	// returns the current id,crypt, etc.

	public static BuddyList friends, enemies, neutral;
	// saved to frost.ini
	public static SettingsClass frostSettings = null;
	static Hashtable goodIds;
	static Hashtable badIds;
	static Hashtable myBatches;

	public static crypt crypto;

	public static Hashtable getBadIds() {
		return badIds;
	}
	public static crypt getCrypto() {
		return crypto;
	}
	public static BuddyList getEnemies() {
		return enemies;
	}
	public static BuddyList getFriends() {
		return friends;
	}
	public static Hashtable getGoodIds() {
		return goodIds;
	}
	public static LocalIdentity getMyId() {
		return mySelf;
	}

	/**
	 * @return
	 */
	public static Hashtable getMyBatches() {
		return myBatches;
	}

	private void initialize2() {
		timer2 = new java.util.Timer(true);
		timer2.schedule(
			new checkForSpam(this),
			0,
			frostSettings.getIntValue("sampleInterval") * 60 * 60 * 1000);

		// the saver
		final Saver saver = new Saver(this);
		Runtime.getRuntime().addShutdownHook(saver);

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
		timer2.schedule(cleaner, 30 * 60 * 1000, 30 * 60 * 1000);
		// all 30 minutes

		TimerTask autoSaver = new TimerTask() {
			public void run() {
				saver.autoSave();
			}
		};
		int autoSaveIntervalMinutes = frostSettings.getIntValue("autoSaveInterval");
		timer2.schedule(
			autoSaver,
			autoSaveIntervalMinutes * 60 * 1000,
			autoSaveIntervalMinutes * 60 * 1000);

		// CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
		Startup.startupCheck();

		FileAccess.cleanKeypool(frame1.keypool);

		if (!freenetIsOnline) {
			MiscToolkit.getInstance().showMessage(
				languageResource.getString("Core.init.NodeNotRunningBody"),
				JOptionPane.WARNING_MESSAGE,
				languageResource.getString("Core.init.NodeNotRunningTitle"));
			freenetIsOnline = false;
		}

		// show a warning if freenet=transient AND only 1 node is used
		if (isFreenetTransient() && nodes.size() == 1) {
			MiscToolkit.getInstance().showMessage(
				languageResource.getString("Core.init.TransientNodeBody"),
				JOptionPane.WARNING_MESSAGE,
				languageResource.getString("Core.init.TransientNodeTitle"));
		}

		//create a crypt object
		crypto = new FrostCrypt();

		//load vital data
		loadIdentities();
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
				frame1.getInstance().getUploadTable());
		requestsThread.start();
		if (frostSettings.getBoolValue("helpFriends"))
			timer2.schedule(new GetFriendsRequestsThread(), 5 * 60 * 1000, 3 * 60 * 60 * 1000);

	} //end of init()

	/**
	   * Tries to send old messages that have not been sent yet
	   */
	protected void resendFailedMessages() {
		// start a thread that waits some seconds for gui to appear, then searches for
		// unsent messages
		ResendFailedMessagesThread t =
			new ResendFailedMessagesThread(this, frame1.getInstance());
		t.start();
	}

	public void deleteDir(String which) {
		(new DeleteWholeDirThread(this, which)).start();
	}

	public void startTruster(boolean what, FrostMessageObject which) {
		new Truster(this, Boolean.valueOf(what), which.getFrom()).start();
	}

	public void startTruster(FrostMessageObject which) {
		new Truster(this, null, which.getFrom()).start();
	}
	/**
	 * @param task
	 * @param delay
	 */
	public static void schedule(TimerTask task, long delay) {
		getInstance().timer2.schedule(task, delay);
	}

	/**
	 * @param task
	 * @param delay
	 * @param period
	 */
	public static void schedule(TimerTask task, long delay, long period) {
		getInstance().timer2.schedule(task, delay, period);
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
		if (!initialized) {
			initialized = true;
			instance.initialize();	
		}
		return instance;
	}

	/**
	 * 
	 */
	private void initialize() {
		Splashscreen splashscreen = new Splashscreen();
		splashscreen.setVisible(true);

		frostSettings = new SettingsClass();
		
		// Initializes the language
		initializeLanguage();

		splashscreen.setText(languageResource.getString("Initializing Mainframe"));
		splashscreen.setProgress(20);

		//Initializes the logging and skins
		new Logging(frostSettings);
		initializeSkins();
		
		splashscreen.setText(languageResource.getString("Hypercube fluctuating!"));
		splashscreen.setProgress(50);

		if (!initializeNodes()) {
			System.exit(1);
		}
		checkTransient();
		
		splashscreen.setText(languageResource.getString("Sending IP address to NSA"));
		splashscreen.setProgress(60);

		//Main frame		
		frame1 frame = new frame1(frostSettings, languageResource);
		frame.validate();
		
		splashscreen.setText(languageResource.getString("Wasting more time"));
		splashscreen.setProgress(70);
		
		initialize2();	//TODO: interim name (old constructor)
		
		splashscreen.setText(languageResource.getString("Reaching ridiculous speed..."));
		splashscreen.setProgress(80);
		
		frame.setVisible(true);
		
		splashscreen.closeMe();

		// Display the tray icon
		if (frostSettings.getBoolValue("showSystrayIcon") == true) {
			if (JSysTrayIcon.createInstance(0, "Frost", "Frost") == false) {
				logger.severe("Could not create systray icon.");
			}
		}		
	}

	/**
	 * @return the list of neutral people
	 */
	public static BuddyList getNeutral() {
		return neutral;
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
	public static Observer getEmailNotifier(){
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
				SkinLookAndFeel.enable();
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

}
