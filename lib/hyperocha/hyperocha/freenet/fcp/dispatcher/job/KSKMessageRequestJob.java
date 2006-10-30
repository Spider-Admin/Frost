/**
 *   This file is part of JHyperochaFCPLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaFCPLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaFCPLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FCPConnectionRunner;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.FreenetKeyType;
import hyperocha.freenet.fcp.Persistance;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;


/**
 * @author saces
 *
 */
public class KSKMessageRequestJob extends Job {

	private FreenetKey keyToDownload;
	private File targetFile;
	private FileOutputStream os;
	
	protected KSKMessageRequestJob(int requirednetworktype, String id, FreenetKey key, File dest) {
		super(requirednetworktype, id);
		keyToDownload = key;
		targetFile = dest;
	}

	public boolean doPrepare() {
		if (!(keyToDownload.isFreenetKeyType(FreenetKeyType.KSK))) {
			throw new Error("Not a KSK: " + keyToDownload);
			//return false;
		}
		// TODO 
		//is targetFile a valid file?
		return true;
	}

	public void runFCP2(Dispatcher dispatcher, boolean resume) {
		
		FCPConnectionRunner conn = dispatcher.getDefaultFCPConnectionRunner(getRequiredNetworkType());
		
		List cmd = new LinkedList();
		cmd.add("ClientGet");
		cmd.add("IgnoreDS=false");
		cmd.add("DSOnly=false");
		cmd.add("URI=" + keyToDownload.getReadFreenetKey());
		cmd.add("Identifier=" + getJobID()); 
		cmd.add("Verbosity=0");  // no simple progress for ksk
		cmd.add("MaxRetries=0");      // only one try 
		cmd.add("PriorityClass=2");   // today, please ;) 
		cmd.add("Global=false");
		cmd.add("Persistance=" + Persistance.CONNECTION);
		cmd.add("ClientToken=" + getClientToken()); 
		cmd.add("ReturnType=direct");
		cmd.add("EndMessage");
		
		conn.send(cmd);
		
		//System.out.println("KSK gestarted: " + getJobID() + " -> " + cmd); 

		waitFine();
		//System.out.print("KSK fertig: " + getJobID() + " result: ");
//		if (isSuccess()) {
//			System.out.println("success");
//		} else {
//			System.out.println(getLastError());
//		}
	}
	
	public void incomingData(String id, Hashtable message, FCPConnection conn) {
		if ("AllData".equals(message.get(FCPConnection.MESSAGENAME))) {
			long size = Long.parseLong((String)(message.get("DataLength"))); 
			//System.out.println("DataHandler: " + message);
			conn.copyFrom(size, os);
			// FIXME: daten sind ins file copiert, feierabend
			setSuccess();
			return;
		}
		if ("DataChunk".equals(message.get(FCPConnection.MESSAGENAME))) {
			long size = Long.parseLong((String)(message.get("Length")), 16); 
			//System.out.println("KSK save DataHandler: " + message);
			conn.copyFrom(size, os);
			// FIXME: daten sind ins file copiert, feierabend
			//setSuccess();
			return;
		}
		System.out.println("KSK DataHandler (unhandled): " + message);
		//if (true) { throw new Error(); }
	}
	


	public void incomingMessage(String id, Hashtable message) {
		if ("DataFound".equals(message.get(FCPConnection.MESSAGENAME))) {
			try {
				os = new FileOutputStream(targetFile);
			} catch (FileNotFoundException e) {
				System.err.println("This scouldn't happen!!!!");
				System.err.println("Hu! The prepare have to check for valid filenames!");
				e.printStackTrace();
				return;
			}
			return;
		}
		
		if ("GetFailed".equals(message.get(FCPConnection.MESSAGENAME))) {
			setError((String)message.get("CodeDescription"));
			return;
		}

		System.out.println("KSK down MessageHandler (unhandled): " + message);
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP1(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP1(Dispatcher dispatcher, boolean resume) {
		// TODO Auto-generated method stub
		FCPConnectionRunner conn = dispatcher.getNewFCPConnectionRunner(getRequiredNetworkType(), getJobID());

		List cmd = new LinkedList();
		cmd.add("ClientGet");
        cmd.add("URI=" + keyToDownload.getReadFreenetKey());
        cmd.add("HopsToLive=10");
        cmd.add("EndMessage");
        
        conn.send(cmd);
        
        System.out.println("KSK gestarted: " + getJobID() + " -> " + cmd); 
        waitFine();
		//throw new Error("Hier!");
	}
	
	public FileOutputStream getOutStream() {
		return os;
	}

}
