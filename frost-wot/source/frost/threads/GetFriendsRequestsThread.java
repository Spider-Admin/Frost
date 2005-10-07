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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.identities.*;
import frost.messages.*;

public class GetFriendsRequestsThread extends TimerTask {

	private FrostIdentities identities;
    private RequestThread runner = null;

	private static Logger logger = Logger.getLogger(GetFriendsRequestsThread.class.getName());

	public GetFriendsRequestsThread(FrostIdentities newIdentities) {
		super();
		identities = newIdentities;
	}

    private class RequestThread extends Thread {
        public void run() {
            doRequests();
            runner = null;
        }
    }

    public void run() {
        // we start and maintain a single thread here to be able to return quickly
        // in the timertask. otherwise we block other tasks.
        if( runner == null ) {
            runner = new RequestThread();
            runner.start();
        }
    }

	private Set generatePrefixes() {

        Set prefixes = new HashSet();

		// process all index files
        File keypool = new File(MainFrame.keypool);
        File[] boardDirs = keypool.listFiles();
		for (int i = 0; i < boardDirs.length; i++) {
			if (boardDirs[i].isDirectory()) {
                File current = new File(boardDirs[i].getPath() + File.separator + "files.xml");
                if (!current.isFile()) {
                    continue;
                }
                FrostIndex frostIndex = null;
                Index idx = Index.getInstance();
                synchronized(idx) {
                    frostIndex = idx.readKeyFile(current);
                }
                if( frostIndex != null ) {
                    Collection c = frostIndex.getFilesMap().values();
                    addToPrefixes(c, prefixes);
                }
            }
        }
        boardDirs = null;
        System.gc();
        return prefixes;
	}
    
    private void addToPrefixes(Collection c, Set prefixes) {
        for(Iterator it = c.iterator(); it.hasNext(); ) {
            SharedFileObject current = (SharedFileObject) it.next();
            if (current.getOwner() == null) {
                continue; //do not request anonymous files
            }
            Identity id = identities.getIdentity(current.getOwner());
            if( id == null ) {
                continue;
            }
            if (current.getOwner().compareTo(identities.getMyId().getUniqueName()) != 0 && //not me
                id.getState() == FrostIdentities.FRIEND ) //marked to be helped
            {
                String newPrefix = new StringBuffer()
                    .append("KSK@frost/request/")
                    .append(Core.frostSettings.getValue("messageBase"))
                    .append("/")
                    .append(Mixed.makeFilename(current.getOwner()))
                    .append("-")
                    .append(current.getBatch()).toString();

                prefixes.add(newPrefix);
            }
        }
    }

	private void doRequests() {
		logger.info("starting to request requests for friends");

		Set prefixes = generatePrefixes();

		logger.info("Will help a total of " + prefixes.size() + " batches");
		File tempFile = new File("requests" + File.separator + "helper.tmp");
		for(Iterator it = prefixes.iterator(); it.hasNext(); ) {
			String currentPrefix = (String)it.next();
            it.remove(); // remove currentPrefix to free mem
			
			//request until a DNF - we're doing this for other people so we don't really care
			//and its ok to request the same keys again the next time around

			String date = DateFun.getDate();
			int index = -1;
			String slot;
			do {
				index++;
				slot = new StringBuffer()
                        .append(currentPrefix)
                        .append("-")
                        .append(date)
                        .append("-")
                        .append(index)
                        .append(".req.sha").toString(); 
				logger.fine("friend's request address is " + slot);
				tempFile.delete();

                // sleep 1 second between requests to not hurt the node to much
                try { Thread.sleep(1000); }
                catch(Exception ex) { }

			} while(FcpRequest.getFile(slot,
				null,
				tempFile,
				21,
				true, //do redirect
				false) //deep request
				!= null ); //break when dnfs
			
			logger.fine("batch of " + currentPrefix + " had " + index + " requests");
		}
		tempFile.delete();
		logger.info("finishing requesting friend's requests");
	}
}
