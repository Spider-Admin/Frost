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

import frost.FcpTools.*;
import java.util.*;

import frost.crypt.*;
import frost.identities.*;
import frost.gui.objects.*;
import frost.threads.*;
import frost.threads.maintenance.*;
import org.w3c.dom.*;

import java.io.*;
import javax.swing.*;

/**
 * Class hold the more non-gui parts of frame1.java.
 */

public class Core {
	
	private static PrintStream out = System.out; //default is System.out
	private static final Set nodes = new HashSet(); //list of available nodes
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
	//a shutdown hook
	public Thread saver;
	boolean started = false;
	public boolean isFreenetOnline() {
		return freenetIsOnline;
	}
	public boolean isFreenetTransient() {
		return freenetIsTransient;
	}
	
	
	protected void loadIdentities() {
		goodIds = new Hashtable();
		badIds = new Hashtable();
		myBatches = new Hashtable();
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
					friends = new BuddyList();

					if (friends.Add(frame1.getMyId())) {
						out.println("added myself to list");
					}
					enemies = new BuddyList();
		} else
		
		//first try with the new format
		
		if (identitiesxml.exists()){
			friends = new BuddyList();
			enemies = new BuddyList();
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
				else
					enemies.loadXMLElement(current);
			}
			}catch (Exception e){
				e.printStackTrace(getOut());				
			}
			Core.getOut().println("loaded "+friends.size() +" friends and "+ enemies.size() +" enemies.");
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

		//load the batches
		File batches = new File("batches");
		if (batches.exists() && batches.length() > 0) //fix previous version bug
			try {
				String allBatches = FileAccess.readFileRaw(batches);
				String[] _batches = allBatches.split("_");
				//dumb.  will fix later

				for (int i = 0; i < _batches.length; i++)
					myBatches.put(_batches[i], _batches[i]);

				out.println(
					"loaded " + _batches.length + " batches of shared files");
			} catch (Throwable e) {
				out.println("couldn't load batches");
				e.printStackTrace(out);
			}
			
		//and load the known boards
		//don't really need xml here, its just a flat list
		File boards = new File("boards");
		if (boards.exists())
			try {
// FIXME: !!! we should explicitely load/save this file in UTF-16, because it contains
//            user visible strings                
				String allBoards = FileAccess.readFile(boards);
				String []_boards = allBoards.split(":");
				for (int i=0;i<_boards.length;i++)
                {
//System.out.println("DBG-loadedBoard: '"+_boards[i].trim()+"'");
                    knownBoards.add(_boards[i].trim());
                }
				out.println("loaded "+ _boards.length +" known boards");
			}catch (Throwable t){
				out.println("couldn't load known boards");
				t.printStackTrace(out);
			}

	}
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	/**Save on exit*/
	public void saveOnExit() {
		out.println("Saving settings ...");
		frame1.getInstance().saveSettings();
		out.println("Bye!");
	}

	java.util.Timer timer; // Uploads / Downloads
	java.util.Timer timer2;

	public static LocalIdentity mySelf;
	//------------------------------------------------------------------------
	// end-of: Generate objects
	//------------------------------------------------------------------------

	// returns the current id,crypt, etc.

	public static BuddyList friends, enemies;
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
		saver = new Saver(this);
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
				frame1.getInstance().getTofTree().saveTree();
				frame1.getInstance().getDownloadTable().save();
				frame1.getInstance().getUploadTable().save();
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

		//load the identities
		loadIdentities();

		//		a class that reinserts the pubkey each hour
		TimerTask KeyReinserter = new TimerTask() {
			public void run() {
				if (isFreenetOnline() == false)
					return;
				File tempUploadfile = null;
				try {
					tempUploadfile =
						File.createTempFile(
							"pubkey_",
							null,
							new File(
								frame1.frostSettings.getValue("temp.dir")));
				} catch (Exception ex) {
					tempUploadfile =
						new File(
							frame1.frostSettings.getValue("temp.dir")
								+ "pubkey_"
								+ System.currentTimeMillis()
								+ ".tmp");
				}
				FileAccess.writeFile(mySelf.getKey(), tempUploadfile);

				out.println("KeyReinserter: Re-uploading public key...");
				FcpInsert.putFile(
					"CHK@",
					tempUploadfile,
					25,
					false, // doRedirect
					null);
				out.println(
					"KeyReinserter: Finished re-uploading public key.");

				tempUploadfile.deleteOnExit();
				tempUploadfile.delete();
			}
		};
		timer2.schedule(KeyReinserter, 0, 60 * 60 * 1000); // run each hour
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
	 * 
	 * @return pointer to the live core
	 */
	public static Core getInstance(){
		return self;
	}

}
