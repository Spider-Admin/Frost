/*
 * Created on Oct 5, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads;

import java.util.*;
import java.io.*;
import frost.*;
import frost.FcpTools.*;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GetFriendsRequestsThread extends TimerTask {

	Set prefixes;

	private final void generatePrefixes() {
		File keypool = new File(frame1.keypool);
		File[] boardDirs = keypool.listFiles();
		LinkedList indices = new LinkedList();
		Map allFiles = new HashMap();

		//put all index files in a list
		for (int i = 0; i < boardDirs.length; i++)
			if (boardDirs[i].isDirectory())
				indices.add(
					new File(
						boardDirs[i].getPath() + File.separator + "files.xml"));

		//put all files in a map
		Iterator it = indices.iterator();
		while (it.hasNext()) {
			File current = (File) it.next();
			if (!current.exists())
				continue;
			FileAccess.readKeyFile(current, allFiles);
		}

		//get the prefixes of the good people
		it = allFiles.values().iterator();
		while (it.hasNext()) {
			SharedFileObject current = (SharedFileObject) it.next();
			if (current.getOwner() == null || current.getBatch() ==null)
				continue; //do not request anonymous files or broken batches
			if (
				current.getOwner().compareTo(Core.getMyId().getUniqueName())
				!= 0
				&& // not me 
			 (
					Core.getFriends().Get(current.getOwner()) != null
				|| //marked GOOD
			Core.getGoodIds().contains(
					current.getOwner())) //marked to be helped
			)
				prefixes.add(
					new String(
						"KSK@frost/request/"
							+ Core.frostSettings.getValue("messageBase")
							+ "/"
							+ current.getOwner()
							+ "-"
							+ current.getBatch()));
		}
		
		allFiles=null; //this is too big, clean it fast
		System.gc();
	}

	public synchronized void run() {
		Core.getOut().println("starting to request requests for friends");
		prefixes = new HashSet();
		generatePrefixes();
		
		Core.getOut().println("will help total of "+ prefixes.size() +" batches");
		Iterator it = prefixes.iterator();
		File tempFile = null;
		try {
			tempFile = File.createTempFile("tmp"+System.currentTimeMillis(),null);
		}catch (IOException e) {
			e.printStackTrace(Core.getOut());
		}
		while (it.hasNext()){
			String currentPrefix = (String)it.next();
			
			//request until a DNF - we're doing this for other people so we don't really care
			//and its ok to request the same keys again the next time around
			
				String date = DateFun.getDate();
				int index =0;
				
				do{
					tempFile.delete();
					String slot = currentPrefix +"-"+date+"-"+index+".req.sha";
					Core.getOut().println("friend's request address is "+slot);
					FcpRequest.getFile(slot,
													null,
													tempFile,
													25,
													true, //do redirect
													false); //deep request
					index++;
				} while(tempFile.exists() && tempFile.length() > 0); //break when dnfs
			
			Core.getOut().println("batch of "+currentPrefix+ " had "+ (index--) + " requests");
		}
		tempFile.delete();
		prefixes = null;
		
		Core.getOut().println("finishing requesting friend's requests");
	}

}
