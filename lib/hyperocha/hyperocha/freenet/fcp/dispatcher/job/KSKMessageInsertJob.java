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
import hyperocha.freenet.fcp.dispatcher.Dispatcher;
import hyperocha.util.DefaultMIMETypes;

/**
 * @author saces
 *
 */
public class KSKMessageInsertJob extends Job {
	
	private File insertFile;
	private BufferedInputStream fis;
	private FreenetKey targetKey;

	public KSKMessageInsertJob(int requirednetworktype, String id, File source, FreenetKey targetkey) {
		super(requirednetworktype, id);
		insertFile = source;
		targetKey = targetkey;
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
	
	public boolean isKeyCollision() {
		// TODO Auto-generated method stub
		if (true) { throw new Error(); }
		return false;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP2(Dispatcher dispatcher, boolean resume) {
		
		FCPConnectionRunner conn = dispatcher.getDefaultFCPConnectionRunner(getRequiredNetworkType());
		
		List cmd = new LinkedList();
		
		cmd.add("ClientPut");
		cmd.add("URI=" + targetKey.getReadFreenetKey());
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
		
		waitFine();
		

		// TODO Auto-generated method stub
		//throw new Error();
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingMessage(String id, Hashtable message) {
		if ("URIGenerated".equals(message.get(FCPConnection.MESSAGENAME))) {
			//trash the uri-generated
			return;
		}
		
		if ("PutFetchable".equals(message.get(FCPConnection.MESSAGENAME))) {
			targetKey = FreenetKey.KSKfromString((String)message.get("URI"));
			//System.out.println("CHK ins PF: " + message);
			// if fast mode setSuccess();
			return;
		}
		
		if ("PutSuccessful".equals(message.get(FCPConnection.MESSAGENAME))) {
			//System.out.println("CHK ins PS: " + message);
			targetKey = FreenetKey.KSKfromString((String)message.get("URI"));
			setSuccess();
			return;
		}
		
		if ("PutFailed".equals(message.get(FCPConnection.MESSAGENAME))) {
			targetKey = FreenetKey.CHKfromString((String)message.get("URI"));
			setError((String)message.get("ShortCodeDescription"));
			return;
		}

		System.err.println("KSK ins not handled: " + message);
	}
}
