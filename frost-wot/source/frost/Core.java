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

import javax.swing.JOptionPane;

import org.w3c.dom.*;

import frost.FcpTools.*;
import frost.crypt.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.threads.*;
import frost.threads.maintenance.*;

/**
 * Class hold the more non-gui parts of frame1.java.
 */

public class Core {
	
	private static PrintStream out = System.out; //default is System.out
	private static final Set nodes = new HashSet(); //list of available nodes
	private static Set messageSet = new HashSet(); // set of message digests
	private static final SortedSet knownBoards = new TreeSet(); //list of known boards
	private static Core self = null;
    
	public Core() {
		out = System.out; //when we want to redirect to file just change this.
		
		frostSettings = frame1.frostSettings;
		//parse the list of available nodes
		String nodesUnparsed = frostSettings.getValue("availableNodes");
		
		if (nodesUnparsed == null) { //old format
			String converted = new String(frostSettings.getValue("nodeAddress")+":"+
							frostSettings.getValue("nodePort"));
			nodes.add(converted.trim());
			frostSettings.setValue("availableNodes",converted.trim());
		} else { // new format
			String []_nodes = nodesUnparsed.split(",");
			for (int i=0;i<_nodes.length;i++)
				nodes.add(_nodes[i]);	
		}
		
		if (nodes.size() == 0) {
			getOut().println("not a single Freenet node configured!  You need at least one");
			System.exit(1);
		}
		
		getOut().println("Frost will use "+nodes.size() +" Freenet nodes");
		
		
		//		check whether the user is running a transient node
		setFreenetIsTransient(false);
		setFreenetIsOnline(false);
		try {
			
			FcpConnection con1 = FcpFactory.getFcpConnectionInstance();
			if (con1 != null) {
				String[] nodeInfo = con1.getInfo();
				// freenet is online
				setFreenetIsOnline(true);
				for (int ij = 0; ij < nodeInfo.length; ij++) {
					if (nodeInfo[ij].startsWith("IsTransient")
						&& nodeInfo[ij].indexOf("true") != -1) {
						setFreenetIsTransient(true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}
    
	private static CleanUp fileCleaner = new CleanUp("keypool", false);
	private boolean freenetIsOnline = false;
	private boolean freenetIsTransient = false;
	public ObjectOutputStream id_writer;
	boolean started = false;
	public boolean isFreenetOnline() {
		return freenetIsOnline;
	}
	public boolean isFreenetTransient() {
		return freenetIsTransient;
	}
	
	
	private void loadIdentities() 
    {
		goodIds = new Hashtable();
		badIds = new Hashtable();
		myBatches = new Hashtable();
		friends = new BuddyList();
		enemies = new BuddyList();
		neutral = new BuddyList();
		File identities = new File("identities");
		File identitiesxml = new File ("identities.xml");
		try{
		
		if (identities.length() == 0)
				identities.delete();
		if (identitiesxml.length() ==0)
				identitiesxml.delete();
		if (identities.createNewFile() && identitiesxml.createNewFile()) {
				if (isFreenetOnline() == false) {
						JOptionPane.showMessageDialog(
							frame1.getInstance(),
							"Frost could not establish a connection to your freenet node(s). "
								+ "For first setup of Frost and creating your identity a connection is needed,"
								+ "later you can run Frost without a connection.\n"
								+ "Please ensure that you are online and freenet is running, then restart Frost.",
							"Connect to Freenet node failed",
							JOptionPane.ERROR_MESSAGE);
						System.exit(2);
				}
					//create new identities
					try {
						String nick = null;
						do {
							nick =
								JOptionPane.showInputDialog(
									"Choose an identity name, it doesn't have to be unique\n");
							if (!(nick == null || nick.length() == 0)) {
								// check for a '@' in nick, this is strongly forbidden
								if (nick.indexOf("@") > -1) {
									JOptionPane.showMessageDialog(
										frame1.getInstance(),
										"Your name must not contain a '@'!",
										"Invalid identity name",
										JOptionPane.ERROR_MESSAGE);
									nick = "";
								}
							}

						} while (nick != null && nick.length() == 0);
						if (nick == null) {
							out.println(
								"Frost can't run without an identity.");
							System.exit(1);
						}
				
						do { //make sure there's no // in the name.
						mySelf = new LocalIdentity(nick);
						}while (mySelf.getUniqueName().indexOf("//")!=-1);
				
						//JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
					} catch (Exception e) {
						out.println("couldn't create new identitiy");
						out.println(e.toString());
					}
					//friends = new BuddyList();

					if (friends.Add(frame1.getMyId())) {
						out.println("added myself to list");
					}
					//enemies = new BuddyList();
		} else
		//first try with the new format
		if (identitiesxml.exists()){
			//friends = new BuddyList();
			//enemies = new BuddyList();
			try{
				out.println("trying to create/load ids");	
			Document d = XMLTools.parseXmlFile("identities.xml",false);
			Element rootEl = d.getDocumentElement();
			//first myself
			Element myself = (Element) XMLTools.getChildElementsByTagName(rootEl,"MyIdentity").get(0);
			mySelf = new LocalIdentity(myself);
			
			//then friends
			List lists = XMLTools.getChildElementsByTagName(rootEl,"BuddyList");
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
			}catch (Exception e){
				e.printStackTrace(getOut());				
			}
			Core.getOut().println("loaded "+friends.size() +" friends and "+ enemies.size() +" enemies and "+ neutral.size()+" neutrals.");
		}else {
		try {
				
					BufferedReader fin =
						new BufferedReader(new FileReader(identities));
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
						address =
							tmp.substring(
								tmp.indexOf("CHK@"),
								tmp.indexOf("CHK@") + 58);
						out.println(
							"Re-calculated my public key CHK: "
								+ address
								+ "\n");

					}
					mySelf = new LocalIdentity(name, keys, address);
					out.println(
						"loaded myself with name " + mySelf.getName());
					//out.println("and public key" + mySelf.getKey());

					//take out the ****
					fin.readLine();

					//process the friends
					out.println("loading friends");
					friends = new BuddyList();
					boolean stop = false;
					String key;
					while (!stop) {
						name = fin.readLine();
						if (name == null || name.startsWith("***"))
							break;
						address = fin.readLine();
						key = fin.readLine();
						friends.Add(new Identity(name, address, key));
					}
					out.println("loaded " + friends.size() + " friends");

					//just the good ids
					while (!stop) {
						String id = fin.readLine();
						if (id == null || id.startsWith("***"))
							break;
						goodIds.put(id, id);
					}
					out.println(
						"loaded " + goodIds.size() + " good ids");

					//and the enemies
					enemies = new BuddyList();
					out.println("loading enemies");
					while (!stop) {
						name = fin.readLine();
						if (name == null || name.startsWith("***"))
							break;
						address = fin.readLine();
						key = fin.readLine();
						enemies.Add(new Identity(name, address, key));
					}
					out.println("loaded " + enemies.size() + " enemies");

					//and the bad ids
					while (!stop) {
						String id = fin.readLine();
						if (id == null || id.startsWith("***"))
							break;
						badIds.put(id, id);
					}
					out.println("loaded " + badIds.size() + " bad ids");

				} catch (IOException e) {
					out.println("IOException :" + e.toString());
					friends = new BuddyList();
					enemies = new BuddyList();
					friends.Add(mySelf);
				} catch (Exception e) {
					e.printStackTrace(out);
				}
		}
		
		}catch (IOException e) {
			e.printStackTrace(Core.getOut());
		}
		out.println("ME = '" + getMyId().getUniqueName() + "'");
	}
    
    public void saveIdentities()
    {
        Core.getOut().println("saving identities.xml");
        File identities = new File("identities.xml");
        if (identities.exists())
        {
            String bakFilename = "identities.xml.bak";
            File bakFile = new File(bakFilename);
            bakFile.delete();
            identities.renameTo(bakFile);
            identities = new File("identities.xml");
        }
        try
        {
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
        
            //save to file
            XMLTools.writeXmlFile(d, "identities.xml");
        }
        catch (Throwable e)
        {
            e.printStackTrace(Core.getOut());
        }
        
        /*
                Core.getOut().println("saving identities");
                File identities = new File("identities");
                if( identities.exists() )
                {
                    String bakFilename = "identities.bak";
                    File bakFile = new File(bakFilename);
                    bakFile.delete();
                    identities.renameTo(bakFile);
                    identities = new File("identities");
                }

                try
                { //TODO: replace this with a call to XML serializer
                    FileWriter fout = new FileWriter(identities);
                    fout.write(Core.mySelf.getName() + "\n");
                    fout.write(Core.mySelf.getKeyAddress() + "\n");
                    fout.write(Core.mySelf.getKey() + "\n");
                    fout.write(Core.mySelf.getPrivKey() + "\n");

                    //now do the friends
                    fout.write("*****************\n");
                    Iterator i = Core.friends.values().iterator();
                    while( i.hasNext() )
                    {
                        Identity cur = (Identity)i.next();
                        fout.write(cur.getName() + "\n");
                        fout.write(cur.getKeyAddress() + "\n");
                        fout.write(cur.getKey() + "\n");
                    }
                    fout.write("*****************\n");
        i = Core.getGoodIds().values().iterator();
        while (i.hasNext()) {
            fout.write((String)i.next() + "\n");
        }
        fout.write("*****************\n");
                    i = Core.getEnemies().values().iterator();
                    while( i.hasNext() )
                    {
                        Identity cur = (Identity)i.next();
                        fout.write(cur.getName() + "\n");
                        fout.write(cur.getKeyAddress() + "\n");
                        fout.write(cur.getKey() + "\n");
                    }
                    fout.write("*****************\n");
        i = Core.getBadIds().values().iterator();
        while (i.hasNext()) {
            fout.write((String)i.next() + "\n");
        }
        fout.write("*****************\n");
                    fout.close();
                    Core.getOut().println("identities saved successfully.");

                }
                catch( IOException e )
                {
                    Core.getOut().println("ERROR: couldn't save identities:");
                    e.printStackTrace(Core.getOut());
                }*/
    }
    
    
    private void loadHashes()
    {
        File hashes = new File("hashes");
        if (hashes.exists())
        	try{
        		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(hashes));
        		messageSet = (HashSet)ois.readObject();
        		getOut().println("loaded "+messageSet.size() +" message hashes");	
        		ois.close();
        	} catch(Throwable t){
        		t.printStackTrace(getOut());
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
            t.printStackTrace(Core.getOut());
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
                getOut().println("Error reading knownboards.xml:");
                getOut().println( ex.getMessage() );
                return;
            }
            Element rootNode = doc.getDocumentElement();
            if( rootNode.getTagName().equals("FrostKnownBoards") == false )
            {
                getOut().println("Error - invalid knownboards.xml: does not contain the root tag 'FrostKnownBoards'");
                return;
            }
            // pass this as an 'AttachmentList' to xml read method and get
            // all board attachments
            AttachmentList al = new AttachmentList();
            try { al.loadXMLElement(rootNode); }
            catch(Exception ex)
            {
                getOut().println("Error - knownboards.xml: contains unexpected content.");
                return;
            }
            List lst = al.getAllOfType(Attachment.BOARD);
            knownBoards.addAll(lst);
            getOut().println("Loaded "+knownBoards.size()+" known boards.");
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
            out.println("Loaded "+ _boards.length +" OLD known boards (converting).");
            addNewKnownBoards(tmpList);
        }catch (Throwable t){
            out.println("couldn't load/convert OLD known boards");
            t.printStackTrace(out);
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
            Core.getOut().println("Error - saveBoardTree: factory could'nt create XML Document.");
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
            Core.getOut().println("Exception while writing knownboards.xml: "+ex.getMessage());
        }
        if( !writeOK )
        {
            Core.getOut().println("Error while writing knownboards.xml, file was not saved");
        }
        else
        {
            Core.getOut().println("Saved "+getKnownBoards().size()+" known boards.");
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
        
        		getOut().println(
        			"loaded " + _batches.length + " batches of shared files");
        	} catch (Throwable e) {
        		getOut().println("couldn't load batches");
        		e.printStackTrace(getOut());
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
            t.printStackTrace(Core.getOut());
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

	public void init() {
		self = this;
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
					out.println("discarding old files");
					fileCleaner.doCleanup();
				}
				out.println("freeing memory");
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
		int autoSaveIntervalMinutes =
			frostSettings.getIntValue("autoSaveInterval");
		timer2.schedule(
			autoSaver,
			autoSaveIntervalMinutes * 60 * 1000,
			autoSaveIntervalMinutes * 60 * 1000);

		// CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
		Startup.startupCheck();

		FileAccess.cleanKeypool(frame1.keypool);

		if (!isFreenetIsOnline()) {
			JOptionPane.showMessageDialog(
				frame1.getInstance(),
				"Make sure your node is running and that you have configured frost correctly.\n"
					+ "Nevertheless, to allow you to read messages, Frost will startup now.\n"
					+ "Don't get confused by some error messages ;)\n",
				"Error - could not establish a connection to freenet node.",
				JOptionPane.WARNING_MESSAGE);
			setFreenetIsOnline(false);
		}

        // show a warning if freenet=transient AND only 1 node is used
		if( isFreenetTransient() && nodes.size() == 1 ) 
        {
			JOptionPane.showMessageDialog(
				frame1.getInstance(),
				"      You are running a TRANSIENT node.  "
					+ "Better run a PERMANENT freenet node.",
				"Transient node detected",
				JOptionPane.WARNING_MESSAGE);
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

		Thread requestsThread =
			new GetRequestsThread(
				frostSettings.getIntValue("tofDownloadHtl"),
				frostSettings.getValue("keypool.dir"),
				frame1.getInstance().getUploadTable());
		requestsThread.start();
		if(frostSettings.getBoolValue("helpFriends"))
			timer2.schedule(new GetFriendsRequestsThread(), 5*60*1000, 3*60*60*1000);
		
		started = true;
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
		new Truster(this, Boolean.valueOf(what), which).start();
	}

	public void startTruster(FrostMessageObject which) {
		new Truster(this, null, which).start();
	}
	/**
	 * @param b
	 */
	public void setFreenetIsOnline(boolean b) {
		freenetIsOnline = b;
	}

	/**
	 * @param b
	 */
	public void setFreenetIsTransient(boolean b) {
		freenetIsTransient = b;
	}

	/**
	 * @return
	 */
	public boolean isFreenetIsOnline() {
		return freenetIsOnline;
	}

	/**
	 * @return
	 */
	public boolean isFreenetIsTransient() {
		return freenetIsTransient;
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
	 * @return the PrintStream where messages are logged
	 */
	public static PrintStream getOut() {
		return out;
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
	public static Core getInstance(){
		return self;
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

}
