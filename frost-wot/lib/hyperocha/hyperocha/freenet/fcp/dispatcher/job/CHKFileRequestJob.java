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
import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.FreenetKeyType;
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;
import hyperocha.freenet.fcp.utils.FCPUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * download a file the best way 
 * includes black magic ritual and eating childs for finding the best way
 * 
 * @version $Id$
 */
public class CHKFileRequestJob extends Job {
	
	private FreenetKey keyToDownload;
	private File targetFile;
	private FileOutputStream os;
	
	// first try is CHK without any metakey (lib default),
	// the secound CHK + filename (the complete one)
	private boolean useFilename = false;  
	
	private FCPNode node; // run job on this node
	private FCPConnectionRunner conn = null;
	//private FCPConnectionRunner dataConn = null;
	
	public CHKFileRequestJob(int requirednetworktype, String id, FreenetKey key, File target) {
		super(requirednetworktype, id);
		keyToDownload = key;
		targetFile = target;
	}

	public boolean doPrepare() {
		if (!(keyToDownload.isFreenetKeyType(FreenetKeyType.CHK))) {
			throw new Error("not a chk" + keyToDownload);
			//return false;
		}
		// TODO: is targetFile a valid file name/target?
		//        check for writable? but do not create bzw. leave the file after test!
		return true;
	}
	
	private List composeResume(List cmd) {
		cmd.add("GetRequestStatus");
		cmd.add("Identifier=" + this.getJobID());
		cmd.add("Global=true");
		cmd.add("OnlyData=false");
		cmd.add("EndMessage");
		return cmd;
	}
	
	private List composeGet(List cmd) {
		cmd.add("ClientGet");
		cmd.add("IgnoreDS=false");
		cmd.add("DSOnly=false");
		if (useFilename) { 
			cmd.add("URI=" + keyToDownload.getReadFreenetKey());
		} else {
			cmd.add("URI=" + keyToDownload.getBaseReadKey());
		}
		cmd.add("Identifier=" + getJobID()); 
		cmd.add("Verbosity=1");
		cmd.add("MaxRetries=2");       
		cmd.add("PriorityClass=4");   
		
		if (node.haveGQ()) {
			cmd.add("Global=true");
			cmd.add("Persistence=forever");
		} else {
			cmd.add("Global=false");
			cmd.add("Persistence=connection");
		}

		if (node.haveDDA()) {  // direct file acess
			cmd.add("ReturnType=disk");
			cmd.add("Filename=" + targetFile.getAbsolutePath());
		} else {
			cmd.add("ReturnType=direct");
		}
		cmd.add("EndMessage");
		return cmd;
	}
	
	private void removeMeFromGQ() {
		List cmd = new LinkedList();
		cmd.add("RemovePersistentRequest");
		cmd.add("Global=true");
		cmd.add("Identifier=" + getJobID());
		cmd.add("EndMessage");
		node.getDefaultFCPConnectionRunner().send(cmd);
	}
	
	private void fetchAllData() {
		String s = FCPUtil.getNewConnectionId();
		FCPConnectionRunner r = node.getNewFCPConnectionRunner(s);
		
		List cmd = new LinkedList();
		cmd.add("GetRequestStatus");
		cmd.add("Identifier=" + getJobID());
		cmd.add("Global=true");
		cmd.add("OnlyData=true");
		cmd.add("End");
		
		r.send(cmd);
	}
	
	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP2(Dispatcher dispatcher, boolean resume) {

		node = dispatcher.getNextNode(getRequiredNetworkType());

		List cmd = new LinkedList();

		if (resume) {
			composeResume(cmd);
		} else {
			composeGet(cmd);
		}
		node.getDefaultFCPConnectionRunner().send(cmd);
		System.err.println("CHK req: " + cmd);
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingData(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingData(String id, NodeMessage msg, FCPConnection conn) {
		if (msg.isMessageName("AllData")) { // FCP 2
			long size = msg.getLongValue("DataLength"); 
			// openStream();
			try {
				os = new FileOutputStream(targetFile);
				conn.copyFrom(size, os);
			} catch (FileNotFoundException e) {
				conn.skip(size);
				// data are gone
				setError(e);
			}
			setSuccess();
			removeMeFromGQ();
			return;
		}
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingMessage(String id, NodeMessage msg) {
		if (!useFilename) {
			if (msg.isMessageName("GetFailed")) {
				if (msg.getIntValue("Code") == 24) {
					// Give more metastrings (path components) in URI, Not enough meta-strings
					removeMeFromGQ();
					useFilename = true;
					List cmd = new LinkedList();
					composeGet(cmd);
					node.getDefaultFCPConnectionRunner().send(cmd);
					System.err.println("CHK req2: " + cmd);
					return;
				}
			}
		}
		if (msg.isMessageName("DataFound")) {
			// DDA
			if (node.haveDDA()) {
				if (node.haveGQ()) {
					removeMeFromGQ();
				}
				setSuccess();
				return;
			}
			// non DDA
			if (node.haveGQ()) {
				// fetch data on seperate conn
				fetchAllData();
				return;
			}
			//System.err.println("CHK DF MessageHandler: " + msg);
		}
		super.incomingMessage(id, msg);

		System.out.println("CHK down MessageHandler: " + msg);
	}
}
