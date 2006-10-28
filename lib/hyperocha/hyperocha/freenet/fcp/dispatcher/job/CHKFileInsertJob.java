/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FCPConnectionRunner;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;
import hyperocha.util.DefaultMIMETypes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * insert a file the best way 
 * includes black magic ritual and making coffe for finding the best way
 *
 */
public class CHKFileInsertJob extends Job {
	
	private File insertFile;
	private BufferedInputStream fis;
	private FreenetKey targetKey;
	
	//private boolean halfDone = false;
	
	public CHKFileInsertJob(int requirednetworktype, String id, File source) {
		super(requirednetworktype, id);
		insertFile = source;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#doPrepare()
	 */
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

	public String getChkKey() {
		return targetKey.getReadFreenetKey();
	}

	public boolean isKeyCollision() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP2(Dispatcher dispatcher) {
		
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
		
		waitFine();
		

		// TODO Auto-generated method stub
		//throw new Error();
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incommingMessage(String id, Hashtable message) {
		if ("URIGenerated".equals(message.get(FCPConnection.MESSAGENAME))) {
			//trash the uri-generated
			return;
		}
		
		if ("PutFetchable".equals(message.get(FCPConnection.MESSAGENAME))) {
			targetKey = FreenetKey.CHKfromString((String)message.get("URI"));
			//System.out.println("CHK ins PF: " + message);
			// if fast mode setSuccess();
			return;
		}
		
		if ("PutSuccessful".equals(message.get(FCPConnection.MESSAGENAME))) {
			//System.out.println("CHK ins PS: " + message);
			targetKey = FreenetKey.CHKfromString((String)message.get("URI"));
			setSuccess();
			return;
		}
		
		if ("PutFailed".equals(message.get(FCPConnection.MESSAGENAME))) {
			targetKey = FreenetKey.CHKfromString((String)message.get("URI"));
			setError((String)message.get("ShortCodeDescription"));
			return;
		}
		
		// TODO Auto-generated method stub
		System.out.println("CHK ins: " + message);
		//super.incommingMessage(conn, message);
	}


}
