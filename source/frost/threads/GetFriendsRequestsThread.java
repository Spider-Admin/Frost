/*
  GetFriendsRequestsThread.java / Frost
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
package frost.threads;

import java.util.*;
import java.io.*;
import frost.*;
import frost.FcpTools.*;
import frost.messages.*;

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
			//Core.getOut().println("helper analyzing index at " + current.getPath());
			FileAccess.readKeyFile(current, allFiles);
		}
		Core.getOut().println("helper will traverse through " + allFiles.size()+" files against "+
					Core.getFriends().size() + " friends ");
		//get the prefixes of the good people
		it = allFiles.values().iterator();
		while (it.hasNext()) {
			SharedFileObject current = (SharedFileObject) it.next();
			if (current.getOwner() == null)
				continue; //do not request anonymous files
			//Core.getOut().println("current file's owner is "+current.getOwner() 
			//		+ "and his safe name is "+ mixed.makeFilename(current.getOwner()));	
			if (
				current.getOwner().compareTo(Core.getMyId().getUniqueName()) != 0 //not me
				&&
			 		(
						//Core.getFriends().Get(current.getOwner().substring(0,current.getOwner().indexOf("@"))) != null
						Core.getFriends().containsKey(mixed.makeFilename(current.getOwner()))
							|| //marked GOOD
						Core.getGoodIds().contains(current.getOwner())
					) //marked to be helped
			) {
				String newPrefix = new String("KSK@frost/request/"
					+ Core.frostSettings.getValue("messageBase")
					+ "/"
					+ mixed.makeFilename(current.getOwner())
					+ "-"
					+ current.getBatch()); 
				prefixes.add(newPrefix);
			//	Core.getOut().println("helper adding "+ newPrefix);
			}
		}
		
		allFiles=null; //this is too big, clean it fast
		System.gc();
	}

	public void run() {
		Core.getOut().println("starting to request requests for friends");
		prefixes = new HashSet();
		generatePrefixes();
		
		Core.getOut().println("will help total of "+ prefixes.size() +" batches");
		Iterator it = prefixes.iterator();
		File tempFile = new File("requests"+File.separator+"helper");
		//try {
	//		tempFile = File.createTempFile("tmp"+System.currentTimeMillis(),null);
	//	}catch (IOException e) {
	//		e.printStackTrace(Core.getOut());
	//	}
		while (it.hasNext()){
			String currentPrefix = (String)it.next();
			
			//request until a DNF - we're doing this for other people so we don't really care
			//and its ok to request the same keys again the next time around
			
				String date = DateFun.getDate();
				int index =-1;
				String slot;
				do{
					index++;
					slot = currentPrefix +"-"+date+"-"+index+".req.sha";
					Core.getOut().println("friend's request address is "+slot);
					tempFile.delete();
					
				} while(FcpRequest.getFile(slot,
					null,
					tempFile,
					25,
					true, //do redirect
					false) //deep request
					!= null ); //break when dnfs
			
			Core.getOut().println("batch of "+currentPrefix+ " had "+ index + " requests");
		}
		tempFile.delete();
		prefixes = null;
		//Core.schedule(this,3*60*60*1000); //3 hrs	
		Core.getOut().println("finishing requesting friend's requests");
	}

}
