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
public class KSKMessageDownloadJob extends Job {

	private FreenetKey keyToDownload;
	private File targetFile;
	private FileOutputStream os;
	
	protected KSKMessageDownloadJob(int requirednetworktype, String id, FreenetKey key, File dest) {
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

	public void runFCP2(Dispatcher dispatcher) {
		
		//if (true) return;
		
		FCPConnection conn = dispatcher.getDefaultFCPConnection(getRequiredNetworkType());
		
		List cmd = new LinkedList();
		cmd.add("ClientGet");
		cmd.add("IgnoreDS=false");
		cmd.add("DSOnly=false");
		cmd.add("URI=" + keyToDownload.getReadFreenetKey());
		cmd.add("Identifier=" + getJobID()); 
		cmd.add("Verbosity=1");
		cmd.add("MaxRetries=0");      // only one try 
		cmd.add("PriorityClass=2");   // today, please ;) 
		cmd.add("Global=false");
		cmd.add("Persistance=" + Persistance.CONNECTION);
		cmd.add("ClientToken=" + getClientToken()); 
		cmd.add("ReturnType=direct");
		cmd.add("EndMessage");
		
		conn.start(cmd);
		
		try {
			os = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		System.out.println("KSK@down gestartet: " + cmd);
		//conn.startMonitor(this);
		waitFine();
		
		//throw (new Error("hsabhbs"));
	}
	
	public void incommingData(FCPConnection conn, Hashtable result) {
		long size = Long.parseLong((String)(result.get("DataLength"))); 
		System.out.println("DataHandler: " + result);
		conn.copyFrom(size, os);
		// FIXME: daten sind ins file copiert, feierabend
		setSuccess();
	}
	


	public void incommingMessage(FCPConnection conn, Hashtable result) {
		System.out.println("KSK down MessageHandler: " + result);
	}

}
