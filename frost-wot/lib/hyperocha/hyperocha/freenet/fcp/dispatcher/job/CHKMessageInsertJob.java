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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FCPConnectionRunner;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;
import hyperocha.util.DefaultMIMETypes;

/**
 * @author saces
 *
 */
public class CHKMessageInsertJob extends Job {
	
	private File insertFile;
	private BufferedInputStream fis;
	private FreenetKey targetKey;

	public CHKMessageInsertJob(int requirednetworktype, String id, File source) {
		super(requirednetworktype, id);
		insertFile = source;
	}

	public boolean doPrepare() {
		// TODO Check file exists, is file, read 
		try {
			fis = new BufferedInputStream(new FileInputStream(insertFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return insertFile.exists();
	}
	
	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP2(Dispatcher dispatcher, boolean resume) {
		
		FCPConnectionRunner conn = dispatcher.getDefaultFCPConnectionRunner(getRequiredNetworkType());
		
		List cmd = new LinkedList();
		
		cmd.add("ClientPut");
		cmd.add("URI=CHK@");
		cmd.add("Identifier=" + this.getJobID());
        cmd.add("Verbosity=257"); // recive SimpleProgress for unterdruecken timeout       
		cmd.add("MaxRetries=0");
		cmd.add("DontCompress=false"); // force compression
        cmd.add("TargetFilename=");  // disable gurken-keys
        cmd.add("EarlyEncode=false");
		cmd.add("GetCHKOnly=false");
        cmd.add("Metadata.ContentType=" + DefaultMIMETypes.guessMIMEType(insertFile.getAbsolutePath()));
        cmd.add("PriorityClass=2");  
		
//		if (dda) {  // direct file acess
//			//fcpOut.println("Global=true");
//			fcpOut.println("Persistence=connection");
//			fcpOut.println("ClientToken=blasuelz");
//			
//	        fcpOut.println("UploadFrom=disk");
//	        fcpOut.println("Filename=" + sourceFile.getAbsolutePath());
//	        fcpOut.println("EndMessage");
//	        //System.out.println("FileName -> " + sourceFile.getAbsolutePath());
			

        cmd.add("UploadFrom=direct");

		cmd.add("DataLength=" + Long.toString(insertFile.length()));
		cmd.add("Data");
		
		conn.send(cmd, insertFile.length(), fis);
		
		System.err.println("KSK sent: " + cmd);
		
		//waitFine();
		

		// TODO Auto-generated method stub
		//throw new Error();
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingMessage(String id, NodeMessage msg) {
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

		System.err.println("KSK ins not handled: " + msg);
	}
}
