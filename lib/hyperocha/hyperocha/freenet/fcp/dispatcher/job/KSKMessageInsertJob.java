/**
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
	public void runFCP2(Dispatcher dispatcher) {
		
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
		// TODO Auto-generated method stub
		System.err.println("KSK ins: " + message);
	}
}
