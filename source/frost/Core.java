/*
 * Created on Sep 10, 2003
 */
package frost;

import frost.FcpTools.*;
import java.util.*;

import frost.crypt.*;
import frost.identities.*;
import frost.gui.objects.*;
import frost.threads.*;
import frost.threads.maintenance.*;

import java.io.*;
import javax.swing.*;

/**
 * Class hold the more non-gui parts of frame1.java.
 */

public class Core {

	public Core() {
		//		check whether the user is running a transient node
		setFreenetIsTransient(false);
		setFreenetIsOnline(false);
		try {
			frostSettings = frame1.frostSettings;
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
			e.printStackTrace(System.out);
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

		//File contacts = new File("contacts");
		System.out.println("trying to create/load ids");
		try {
			if (identities.length() == 0)
				identities.delete();
			if (identities.createNewFile()) {
				if (isFreenetOnline() == false) {
					JOptionPane.showMessageDialog(
						frame1.getInstance(),
						"Frost could not establish a connection to your freenet node. "
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
						System.out.println(
							"Frost can't run without an identity.");
						System.exit(1);
					}
					mySelf = new LocalIdentity(nick);
					//JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
				} catch (Exception e) {
					System.out.println("couldn't create new identitiy");
					System.out.println(e.toString());
				}
				friends = new BuddyList();

				if (friends.Add(frame1.getMyId())) {
					System.out.println("added myself to list");
				}
				enemies = new BuddyList();

			} else {
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

						try {
							FcpConnection con =
								FcpFactory.getFcpConnectionInstance();
							if (con != null) {
								String tmp =
									con.putKeyFromArray(
										"CHK@",
										pubkeydata,
										null,
										0,
										false);
								address =
									tmp.substring(
										tmp.indexOf("CHK@"),
										tmp.indexOf("CHK@") + 58);
								System.out.println(
									"Re-calculated my public key CHK: "
										+ address
										+ "\n");
							}
						} catch (IOException e) {
							System.out.println(
								"Couldn't re-calculate my public key CHK: "
									+ e.toString());
						}
					}
					mySelf = new LocalIdentity(name, keys, address);
					System.out.println(
						"loaded myself with name " + mySelf.getName());
					//System.out.println("and public key" + mySelf.getKey());

					//take out the ****
					fin.readLine();

					//process the friends
					System.out.println("loading friends");
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
					System.out.println("loaded " + friends.size() + " friends");

					//just the good ids
					while (!stop) {
						String id = fin.readLine();
						if (id == null || id.startsWith("***"))
							break;
						goodIds.put(id, id);
					}
					System.out.println(
						"loaded " + goodIds.size() + " good ids");

					//and the enemies
					enemies = new BuddyList();
					System.out.println("loading enemies");
					while (!stop) {
						name = fin.readLine();
						if (name == null || name.startsWith("***"))
							break;
						address = fin.readLine();
						key = fin.readLine();
						enemies.Add(new Identity(name, address, key));
					}
					System.out.println("loaded " + enemies.size() + " enemies");

					//and the bad ids
					while (!stop) {
						String id = fin.readLine();
						if (id == null || id.startsWith("***"))
							break;
						badIds.put(id, id);
					}
					System.out.println("loaded " + badIds.size() + " bad ids");

				} catch (IOException e) {
					System.out.println("IOException :" + e.toString());
					friends = new BuddyList();
					enemies = new BuddyList();
					friends.Add(mySelf);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
		} catch (IOException e) {
			System.out.println("couldn't create identities file");
		}
		System.out.println("ME = '" + getMyId().getUniqueName() + "'");

		File batches = new File("batches");
		if (batches.exists())
			try {
				String allBatches = FileAccess.readFileRaw(batches);
				String[] _batches = allBatches.split("_");
				//dumb.  will fix later

				for (int i = 0; i < _batches.length; i++)
					myBatches.put(_batches[i], _batches[i]);

				System.out.println(
					"loaded " + _batches.length + " batches of shared files");
			} catch (Throwable e) {
				System.out.println("couldn't load batches");
				e.printStackTrace(System.out);
			}

	}
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	/**Save on exit*/
	public void saveOnExit() {
		System.out.println("Saving settings ...");
		frame1.getInstance().saveSettings();
		System.out.println("Bye!");
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
					System.out.println("discarding old files");
					fileCleaner.doCleanup();
				}
				System.out.println("freeing memory");
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

		if (isFreenetTransient()) {
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

				System.out.println("KeyReinserter: Re-uploading public key...");
				FcpInsert.putFile(
					"CHK@",
					tempUploadfile,
					25,
					false,
					null);
				System.out.println(
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
	public void schedule(TimerTask task, long delay) {
		timer2.schedule(task, delay);
	}

	/**
	 * @param task
	 * @param delay
	 * @param period
	 */
	public void schedule(TimerTask task, long delay, long period) {
		timer2.schedule(task, delay, period);
	}
}
