/*
 * Created on Sep 10, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost;
import frost.FcpTools.*;
import java.util.*;
import frost.crypt.*;
import frost.identities.*;
import frost.gui.objects.*;
import frost.threads.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class Core {
	
	public Core(){
//		check whether the user is running a transient node
			  setFreenetIsTransient(false);
			  setFreenetIsOnline(false);
		try{
		frostSettings = frame1.frostSettings;
		FcpConnection con1 = FcpFactory.getFcpConnectionInstance();
				if( con1 != null )
				{
					String[] nodeInfo = con1.getInfo();
					// freenet is online
					setFreenetIsOnline(true);
					for (int ij=0;ij<nodeInfo.length;ij++)
					{
						if (nodeInfo[ij].startsWith("IsTransient") && nodeInfo[ij].indexOf("true") != -1)
						{
							setFreenetIsTransient(true);
						}
					}
				}
		}catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}
	class ResendFailedMessagesThread extends Thread
	{
	    Frame frameToLock;
	    public ResendFailedMessagesThread(Frame frameToLock)
	    {
	        this.frameToLock = frameToLock;
	    }
	    public void run()
	    {
	        // give gui a chance to appear ... then start searching for unsent messages
	        try { Thread.sleep(10000); } // wait 10 seconds
	        catch(InterruptedException ex) { ; }
	        if( isInterrupted() )
	            return;
	
	        System.out.println("Starting search for unsent messages ...");
	
	        ArrayList entries = FileAccess.getAllEntries(new File(frostSettings.getValue("unsent.dir")), ".txt");
	
	        for( int i = 0; i < entries.size(); i++ )
	        {
	            File unsentMsgFile = (File)entries.get(i);
	            if( unsentMsgFile.getName().startsWith("unsent") )
	            {
	                // Resend message
	                VerifyableMessageObject mo = new VerifyableMessageObject(unsentMsgFile);
	                if( mo.isValid() )
	                {
	                    FrostBoardObject board = frame1.getInstance().getTofTree().getBoardByName( mo.getBoard() );
	                    if( board == null )
	                    {
	                        System.out.println("Can't resend Message '"+mo.getSubject()+"', the target board '"+mo.getBoard()+
	                                           "' was not found in your boardlist.");
	                        // TODO: maybe delete msg? or it will always be retried to send
	                        continue;
	                    }
	                    // message will be resigned before send, actual date/time will be used
	                    // no more faking here :)
	                    frame1.getInstance().getRunningBoardUpdateThreads().startMessageUpload(
	                        board,
	                        mo.getFrom(),
	                        mo.getSubject(),
	                        mo.getContent(),
	                        "",
	                        frostSettings,
	                        frameToLock,
	                        null);
	                    System.out.println("Message '" + mo.getSubject() + "' will be resent to board '"+board.toString()+"'.");
	                }
	                // check if upload was successful before deleting the file -
	                // is not needed, the upload thread creates new unsent file
	                unsentMsgFile.delete();
	            }
	        }
	        System.out.println("Finished search for unsent messages ...");
	    }
	}
	private static CleanUp fileCleaner = new CleanUp("keypool",false);
	private boolean freenetIsOnline = false;
	private boolean freenetIsTransient = false;
	public ObjectOutputStream id_writer;
	//a shutdown hook
	public Thread saver;
	boolean started = false;
	public boolean isFreenetOnline() { return freenetIsOnline; }
	public boolean isFreenetTransient() { return freenetIsTransient; }
	protected void loadIdentities()
	{
		goodIds = new Hashtable();
	badIds = new Hashtable();
	myBatches = new Hashtable();
	    File identities = new File("identities");
	
	    //File contacts = new File("contacts");
	    System.out.println("trying to create/load ids");
	    try {
	        if( identities.length() == 0 )
	            identities.delete();
	        if( identities.createNewFile() )
	        {
	            if( isFreenetOnline() == false )
	            {
	                JOptionPane.showMessageDialog(frame1.getInstance(),
	                                              "Frost could not establish a connection to your freenet node. "+
	                                              "For first setup of Frost and creating your identity a connection is needed,"+
	                                              "later you can run Frost without a connection.\n"+
	                                              "Please ensure that you are online and freenet is running, then restart Frost.",
	                                              "Connect to Freenet node failed",
	                                              JOptionPane.ERROR_MESSAGE);
	                System.exit(2);
	            }
	            //create new identities
	            try {
	                String nick = null;
	                do
	                {
	                    nick = JOptionPane.showInputDialog("Choose an identity name, it doesn't have to be unique\n");
	                    if( !(nick == null || nick.length() == 0) )
	                    {
	                        // check for a '@' in nick, this is strongly forbidden
	                        if( nick.indexOf("@") > -1 )
	                        {
	                            JOptionPane.showMessageDialog(frame1.getInstance(),
	                                                          "Your name must not contain a '@'!",
	                                                          "Invalid identity name",
	                                                          JOptionPane.ERROR_MESSAGE );
	                            nick="";
	                        }
	                    }
	
	                } while( nick != null && nick.length() == 0 );
	                if( nick == null )
	                {
	                    System.out.println("Frost can't run without an identity.");
	                    System.exit(1);
	                }
	                mySelf = new LocalIdentity(nick);
	                //JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
	            }
	            catch( Exception e ) {
	                System.out.println("couldn't create new identitiy");
	                System.out.println(e.toString());
	            }
	            friends = new BuddyList();
		
	            if( friends.Add(frame1.getMyId()) )
	            {
	                System.out.println("added myself to list");
	            }
	            enemies = new BuddyList();
		
	        }
	        else
	        {
	            try {
	                BufferedReader fin = new BufferedReader(new FileReader(identities));
	                String name = fin.readLine();
	                String address = fin.readLine();
	                String keys[] = new String[2];
	                keys[1] = fin.readLine();
	                keys[0] = fin.readLine();
	                if( address.startsWith("CHK@") == false )
	                {
	                    // pubkey chk was not successfully computed
	                    byte[] pubkeydata;
	                    try { pubkeydata = keys[1].getBytes("UTF-8"); }
	                    catch(UnsupportedEncodingException ex) { pubkeydata = keys[1].getBytes(); }
	
	                    try {
	                        FcpConnection con = FcpFactory.getFcpConnectionInstance();
	                        if( con != null )
	                        {
	                            String tmp = con.putKeyFromFile("CHK@", pubkeydata, null, 0, false);
	                            address = tmp.substring(tmp.indexOf("CHK@"),tmp.indexOf("CHK@") + 58);
	                            System.out.println("Re-calculated my public key CHK: " + address + "\n");
	                        }
	                    }
	                    catch( IOException e ) {
	                        System.out.println("Couldn't re-calculate my public key CHK: "+e.toString());
	                    }
	                }
	                mySelf = new LocalIdentity(name, keys, address);
	                System.out.println("loaded myself with name " + mySelf.getName());
	                //System.out.println("and public key" + mySelf.getKey());
	
	                //take out the ****
	                fin.readLine();
	
	                //process the friends
	                System.out.println("loading friends");
	                friends = new BuddyList();
	                boolean stop = false;
	                String key;
	                while( !stop )
	                {
	                    name = fin.readLine();
	                    if( name==null || name.startsWith("***") ) break;
	                    address = fin.readLine();
	                    key = fin.readLine();
	                    friends.Add(new Identity(name, address,key));
	                }
	                System.out.println("loaded " + friends.size() + " friends");
		    
		    //just the good ids
		    while (!stop) {
		        String id = fin.readLine();
			if (id == null || id.startsWith("***")) break;
			goodIds.put(id,id);
		    }
		    System.out.println("loaded " +goodIds.size() + " good ids");
	
	                //and the enemies
	                enemies = new BuddyList();
	                System.out.println("loading enemies");
	                while( !stop )
	                {
	                    name = fin.readLine();
	                    if( name == null || name.startsWith("***") ) break;
	                    address = fin.readLine();
	                    key = fin.readLine();
	                    enemies.Add(new Identity(name, address,key));
	                }
	                System.out.println("loaded " + enemies.size() + " enemies");
		    
		    //and the bad ids
		    while (!stop) {
		    	String id = fin.readLine();
			if (id == null || id.startsWith("***")) break;
			badIds.put(id,id);
		    }
		    System.out.println("loaded " +badIds.size() + " bad ids");
		    
	
	            }
	            catch( IOException e ) {
	                System.out.println("IOException :" + e.toString());
	                friends = new BuddyList();
	                enemies = new BuddyList();
	                friends.Add(mySelf);
	            }
	            catch( Exception e ) {
	                e.printStackTrace(System.out);
	            }
	        }
	    }
	    catch( IOException e ) {
	        System.out.println("couldn't create identities file");
	    }
	    System.out.println("ME = '"+getMyId().getUniqueName()+"'");
	
	File batches = new File("batches");
	if (batches.exists())
	try{
		String allBatches = FileAccess.readFileRaw(batches);
		String[] _batches = allBatches.split("_"); //dumb.  will fix later
		
		for (int i = 0;i<_batches.length;i++)
			myBatches.put(_batches[i],_batches[i]);
			
		System.out.println("loaded "+_batches.length+" batches of shared files");
	}catch(Throwable e) {
		System.out.println("couldn't load batches");
		e.printStackTrace(System.out);
	}
	
	}
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------
	
	/**Save on exit*/
	private void saveOnExit()
	{
	    System.out.println("Saving settings ...");
	    frame1.getInstance().saveSettings();
	    System.out.println("Bye!");
	}
	 java.util.Timer timer; // Uploads / Downloads
	 java.util.Timer timer2;
	private class checkForSpam extends TimerTask
	{
	    public void run()
	    {
	        if(frostSettings.getBoolValue("doBoardBackoff"))
	        {
	            Iterator iter = frame1.getInstance().getTofTree().getAllBoards().iterator();
	            while (iter.hasNext())
	            {
	                FrostBoardObject current = (FrostBoardObject)iter.next();
	                if (current.getBlockedCount() > frostSettings.getIntValue("spamTreshold"))
	                {
	                    //board is spammed
	                    System.out.println("######### board '"+current.toString()+"' is spammed, update stops for 24h ############");
	                    current.setSpammed(true);
	                    // clear spam status in 24 hours
	                    timer2.schedule(new ClearSpam(current),24*60*60*1000);
	
	                    //now, kill all threads for board
	                    Vector threads = frame1.getInstance().getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
	                    Iterator i = threads.iterator();
	                    while( i.hasNext() )
	                    {
	                        BoardUpdateThread thread = (BoardUpdateThread)i.next();
	                        while( thread.isInterrupted() == false )
	                            thread.interrupt();
	                    }
	                }
	                current.resetBlocked();
	            }
	        }
	    }
	}
	private class ClearSpam extends TimerTask
	{
	    private FrostBoardObject clearMe;
	
	    public ClearSpam(FrostBoardObject which) { clearMe = which; }
	    public void run()
	    {
	        System.out.println("############ clearing spam status for board '"+clearMe.toString()+"' ###########");
	        clearMe.setSpammed(false);
	    }
	}
	private class DeleteWholeDirThread extends Thread
	{
	    String delDir;
	    public DeleteWholeDirThread(String dirToDelete)
	    {
	        delDir = dirToDelete;
	    }
	    public void run()
	    {
	        FileAccess.deleteDir( new File(delDir) );
	    }
	}
	/**
	 * Thread is invoked if the Trust or NotTrust button is clicked.
	 */
	private class Truster extends Thread
	{
	    private Boolean trust;
	    private Identity newIdentity;
	    private VerifyableMessageObject currentMsg;
	
	    public Truster(Boolean what, VerifyableMessageObject msg)
	    {
	        trust=what;
	        currentMsg = msg;
	    }
	
	    public void run()
	    {
	        String from = currentMsg.getFrom();
	        String newState;
	
	        if( trust == null )  newState = "CHECK";
	        else if( trust.booleanValue() == true ) newState = "GOOD";
	        else newState = "BAD";
	
	        System.out.println("Truster: Setting '"+
	                           from+
	                           "' to '"+
	                           newState+
	                           "'.");
	
	        if( trust == null )
	        {
	            // set enemy/friend to CHECK
	            newIdentity = friends.Get(from);
	            if( newIdentity==null )
	                newIdentity=enemies.Get(from);
	
	            if( newIdentity == null ) // not found -> paranoia
	            {
	                newIdentity = new Identity(currentMsg.getFrom(), currentMsg.getKeyAddress());
	            }
	            else
	            {
	                friends.remove( from );
	                enemies.remove( from );
	            }
	        }
	        else if( friends.containsKey(from) && trust.booleanValue() == false )
	        {
	            // set friend to bad
	            newIdentity = friends.Get(from);
	            friends.remove( from );
	            enemies.Add( newIdentity );
	        }
	        else if( enemies.containsKey(from) && trust.booleanValue() == true )
	        {
	            // set enemy to good
	            newIdentity = enemies.Get(from);
	            enemies.remove( newIdentity );
	            friends.Add( newIdentity );
	        }
	        else
	        {
	            // new new enemy/friend
	            newIdentity = new Identity(currentMsg.getFrom(), currentMsg.getKeyAddress());
	            if( trust.booleanValue() )
	                friends.Add(newIdentity);
	            else
	                enemies.Add(newIdentity);
	        }
	
	        if( newIdentity == null || Identity.NA.equals( newIdentity.getKey() ) )
	        {
	            System.out.println("Truster - ERROR: could not get public key for '"+currentMsg.getFrom()+"'");
	            System.out.println("Truster: Will stop to set message states!!!");
	            return;
	        }
	
	        // get all .txt files in keypool
	        ArrayList entries = FileAccess.getAllEntries( new File(frame1.frostSettings.getValue("keypool.dir")),
	                                                   ".txt");
	        System.out.println("Truster: Starting to update messages:");
	
	        for( int ii=0; ii<entries.size(); ii++ )
	        {
	            File msgFile = (File)entries.get(ii);
	            FrostMessageObject tempMsg = new FrostMessageObject( msgFile );
	            if( tempMsg.getFrom().equals(currentMsg.getFrom()) &&
	                (
	                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.PENDING) ||
	                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.VERIFIED) ||
	                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.FAILED)
	                )
	              )
	            {
	                // check if message is correctly signed
	                if( newIdentity.getKeyAddress().equals( tempMsg.getKeyAddress() ) &&
	                    getCrypto().verify(tempMsg.getContent(), newIdentity.getKey()) )
	                {
	                    // set new state of message
	                    if( trust == null )
	                        tempMsg.setStatus(VerifyableMessageObject.PENDING);
	                    else if( trust.booleanValue() )
	                        tempMsg.setStatus(VerifyableMessageObject.VERIFIED);
	                    else
	                        tempMsg.setStatus(VerifyableMessageObject.FAILED);
	
	                    System.out.print("."); // progress
	                }
	                else
	                {
	                    System.out.println("\n!Truster: Could not verify message, maybe the message is faked!" +
	                                       " Message state set to N/A for '"+msgFile.getPath()+"'.");
	                    tempMsg.setStatus(VerifyableMessageObject.NA);
	                }
	            }
	        }
	        // finally step through all board files, count new messages and delete new messages from enemies
	        TOF.initialSearchNewMessages();
	
	        SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    frame1.getInstance().tofTree_actionPerformed(null);
	                } });
	        System.out.println("\nTruster: Finished to update messages, set '"+currentMsg.getFrom()+"' to '"+
	                           newState+"'");
	    }
	}
	public static LocalIdentity mySelf;
	//------------------------------------------------------------------------
	// end-of: Generate objects
	//------------------------------------------------------------------------
	
	// returns the current id,crypt, etc.
	
	public static BuddyList friends,enemies;
	// saved to frost.ini
	public static SettingsClass frostSettings = null;
	static Hashtable goodIds;
	static Hashtable badIds;
	static Hashtable myBatches;
	
	public static crypt crypto;
	
	
	public static Hashtable getBadIds() {return badIds;}
	public static crypt getCrypto() {return crypto;}
	public static BuddyList getEnemies() {return enemies;}
	public static BuddyList getFriends() {return friends;}
	public static Hashtable getGoodIds() {return goodIds;}
	public static LocalIdentity getMyId() {return mySelf;}




	/**
	 * @return
	 */
	public static Hashtable getMyBatches() {
		return myBatches;
	}
	
	
	public void init() {
		
		timer2 = new java.util.Timer(true);
				timer2.schedule(new checkForSpam(), 0, frostSettings.getIntValue("sampleInterval")*60*60*1000);
		saver = new Thread() {
						public void run() {
							System.out.println("saving identities");
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
								fout.write(mySelf.getName() + "\n");
								fout.write(mySelf.getKeyAddress() + "\n");
								fout.write(mySelf.getKey() + "\n");
								fout.write(mySelf.getPrivKey() + "\n");

								//now do the friends
								fout.write("*****************\n");
								Iterator i = friends.values().iterator();
								while( i.hasNext() )
								{
									Identity cur = (Identity)i.next();
									fout.write(cur.getName() + "\n");
									fout.write(cur.getKeyAddress() + "\n");
									fout.write(cur.getKey() + "\n");
								}
								fout.write("*****************\n");
					i = getGoodIds().values().iterator();
					while (i.hasNext()) {
						fout.write((String)i.next() + "\n");
					}
					fout.write("*****************\n");
								i = getEnemies().values().iterator();
								while( i.hasNext() )
								{
									Identity cur = (Identity)i.next();
									fout.write(cur.getName() + "\n");
									fout.write(cur.getKeyAddress() + "\n");
									fout.write(cur.getKey() + "\n");
								}
								fout.write("*****************\n");
					i = getBadIds().values().iterator();
					while (i.hasNext()) {
						fout.write((String)i.next() + "\n");
					}
					fout.write("*****************\n");
								fout.close();
								System.out.println("identities saved successfully.");

							}
							catch( IOException e )
							{
								System.out.println("ERROR: couldn't save identities:");
								e.printStackTrace(System.out);
							}
					try {
						StringBuffer buf = new StringBuffer();
					Iterator i = myBatches.keySet().iterator();
					while (i.hasNext()) 
						buf.append((String)i.next()).append("_");
					if (buf.length() > 0)
						buf.deleteCharAt(buf.length()-1); //remove the _ at the end
					File batches = new File("batches");
					FileAccess.writeFile(buf.toString(),batches);
			
					} catch (Throwable t) {
						t.printStackTrace(System.out);
					}
							saveOnExit();
							FileAccess.cleanKeypool(frame1.keypool);
						}
						};
					Runtime.getRuntime().addShutdownHook(saver);
					
		TimerTask cleaner = new TimerTask() {
						int i = 0;
						public void run() {
							// maybe each 6 hours cleanup files (12 * 30 minutes)
							if( i==12 && frostSettings.getBoolValue("doCleanUp") )
							{
								i=0;
								System.out.println("discarding old files");
								fileCleaner.doCleanup();
							}
							System.out.println("freeing memory");
							System.gc();
							i++;
						}
					};
					timer2.schedule(cleaner,30*60*1000,30*60*1000); // all 30 minutes

					TimerTask autoSaver = new TimerTask() {
						public void run()
						{
							frame1.getInstance().getTofTree().saveTree();
							frame1.getInstance().getDownloadTable().save();
							frame1.getInstance().getUploadTable().save();
						}
					};
					int autoSaveIntervalMinutes = frostSettings.getIntValue("autoSaveInterval");
					timer2.schedule(autoSaver,
									autoSaveIntervalMinutes*60*1000,
									autoSaveIntervalMinutes*60*1000);
				
				
	timer2.schedule(new TimerTask() {
			public void run() {
				//TODO: refactor this method here. lots of work :)
				frame1.getInstance().timer_actionPerformed();
		} },
		1000,
		1000);
		
		// CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
		Startup.startupCheck();

		FileAccess.cleanKeypool(frame1.keypool);

		// Display the tray icon
		try {
			Process process = Runtime.getRuntime().exec("exec" + frame1.fileSeparator + "SystemTray.exe");
		}catch(IOException _IoExc) { }

		

		
		
		
		
		
		if (!isFreenetIsOnline()){
			JOptionPane.showMessageDialog(frame1.getInstance(),
			"Make sure your node is running and that you have configured frost correctly.\n"+
			"Nevertheless, to allow you to read messages, Frost will startup now.\n"+
			"Don't get confused by some error messages ;)\n",
		"Error - could not establish a connection to freenet node.",
		JOptionPane.WARNING_MESSAGE);
			setFreenetIsOnline(false);
		}
		
		if( isFreenetTransient() )
		{
			JOptionPane.showMessageDialog(frame1.getInstance(),
							"      You are running a TRANSIENT node.  "+
							"Better run a PERMANENT freenet node.",
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
				 if( isFreenetOnline() == false )
					 return;
				 File tempUploadfile = null;
				 try {
					 tempUploadfile = File.createTempFile("pubkey_", null, new File(frame1.frostSettings.getValue("temp.dir")) );
				 }
				 catch(Exception ex)
				 {
					 tempUploadfile = new File(frame1.frostSettings.getValue("temp.dir") + "pubkey_"+System.currentTimeMillis()+".tmp");
				 }
				 FileAccess.writeFile(mySelf.getKey(), tempUploadfile);

				 System.out.println("KeyReinserter: Re-uploading public key...");
				 FcpInsert.putFile("CHK@",tempUploadfile,25,false,true,null);
				 System.out.println("KeyReinserter: Finished re-uploading public key.");

				 tempUploadfile.deleteOnExit();
				 tempUploadfile.delete();
			 }
		 };
		 timer2.schedule(KeyReinserter,0,60*60*1000); // run each hour
		// Start tofTree
		 if( isFreenetOnline() )
		 {
			 resendFailedMessages();
		 }
		 
		 Thread requestsThread = new GetRequestsThread(frostSettings.getIntValue("tofDownloadHtl"),
									 frostSettings.getValue("keypool.dir"),
								 frame1.getInstance().getUploadTable());
		 requestsThread.start();
		 started = true;
	} //end of init()
	
	/**
	   * Tries to send old messages that have not been sent yet
	   */
	  protected void resendFailedMessages()
	  {
		  // start a thread that waits some seconds for gui to appear, then searches for
		  // unsent messages
		  ResendFailedMessagesThread t = new ResendFailedMessagesThread(frame1.getInstance());
		  t.start();
	  }
	
	public void deleteDir(String which) {
		(new DeleteWholeDirThread( which )).start();
	}
	
	public void startTruster(boolean what, FrostMessageObject which) {
		new Truster(Boolean.valueOf(what), which).start();
	}
	
	public void startTruster(FrostMessageObject which){
		new Truster(null, which).start();
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

}
	




