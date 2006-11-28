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

import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * insert a file the best way 
 * includes black magic ritual and making coffe for finding the best way
 *
 * @version $Id$
 * 
 */
public class CHKFileInsertJob extends Job {
	
	private File insertFile;
	private BufferedInputStream fis;
	private FreenetKey targetKey;
	
	private FCPNode node; // run job on this node
	
	public CHKFileInsertJob(int requirednetworktype, String id, File source) {
		super(requirednetworktype, id);
		insertFile = source;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#doPrepare()
	 */
	public boolean doPrepare() {
		// TODO Check file exists, is file readable? etc pp 
		return insertFile.exists();
	}

	public String getChkKey() {
		return targetKey.getReadFreenetKey();
	}
	
	private List composeResume(List cmd) {
		cmd.add("GetRequestStatus");
		cmd.add("Identifier=" + this.getJobID());
		cmd.add("Global=true");
		cmd.add("OnlyData=false");
		cmd.add("EndMessage");
		return cmd;
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
			cmd.add("ClientPut");
			cmd.add("URI=CHK@");
			cmd.add("Identifier=" + this.getJobID());
			cmd.add("Verbosity=257"); // recive SimpleProgress for unterdruecken timeout       
			cmd.add("MaxRetries=-1");
			cmd.add("DontCompress=false"); // force compression
			cmd.add("TargetFilename=");  // disable gurken-keys
			cmd.add("EarlyEncode=false");
			cmd.add("GetCHKOnly=false");
			cmd.add("Metadata.ContentType=application/octet-stream");
			cmd.add("PriorityClass=4");
			
			if (node.haveGQ()) {
				cmd.add("Global=true");
				cmd.add("Persistence=forever");
			} else {
				cmd.add("Persistence=connection");
			}

		
			if (node.haveDDA()) {  // direct file acess
				cmd.add("UploadFrom=disk");
				cmd.add("Filename=" + insertFile.getAbsolutePath());
				cmd.add("EndMessage");
				node.getDefaultFCPConnectionRunner().send(cmd);
				//System.err.println("CHK ins: " + cmd);
				
			} else {
				cmd.add("UploadFrom=direct");
				cmd.add("DataLength=" + Long.toString(insertFile.length()));
				cmd.add("Data");
				//System.err.println("CHK ins: " + cmd);
				node.getDefaultFCPConnectionRunner().send(cmd, insertFile.length(), fis);
			}
			
			System.err.println("CHK ins: " + cmd);

		}

	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingMessage(String id, NodeMessage msg) {
		super.incomingMessage(id, msg);
		if (msg.isMessageName("URIGenerated")) {
			//trash the uri-generated
			return;
		}
		
		if (msg.isMessageName("PutFetchable")) {
			targetKey = msg.getKeyValue("URI");
			//System.out.println("CHK ins PF: " + message);
			// if fast mode setSuccess();
			return;
		}
		
		if (msg.isMessageName("PutSuccessful")) {
			//System.out.println("CHK ins PS: " + message);
			targetKey = msg.getKeyValue("URI");
			setSuccess();
			return;
		}
		
		if (msg.isMessageName("PutFailed")) {
			//targetKey = FreenetKey.CHKfromString((String)message.get("URI"));
			setError(msg.getStringValue("ShortCodeDescription"));
			return;
		}
		
		// TODO Auto-generated method stub
		System.out.println("CHK ins not handled: " + msg);
		//super.incommingMessage(conn, message);
	}
	

}
