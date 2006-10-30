/**
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
 * download a file the best way 
 * includes black magic ritual and eating childs for finding the best way
 */
public class CHKFileRequestJob extends Job {
	
	private FreenetKey keyToDownload;
	private File targetFile;
	private FileOutputStream os;
	
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
		// TODO: is targetFile a valid file?
		return true;
	}
	
	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP2(Dispatcher dispatcher, boolean resume) {
		//System.out.println("runFCP2: 01");
		FCPConnectionRunner conn = dispatcher.getDefaultFCPConnectionRunner(getRequiredNetworkType());

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
		
		//System.out.println("runFCP2: 02");
		
		try {
			os = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		//System.out.println("runFCP2: 03");
		conn.send(cmd);
		
		//System.out.println("runFCP2: 04");
		
		waitFine();
		
		//System.out.println("runFCP2: 05");
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingData(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingData(String id, Hashtable message, FCPConnection conn) {
		long size = Long.parseLong((String)(message.get("DataLength"))); 
		//System.out.println("CHK DataHandler: " + message);
		conn.copyFrom(size, os);
		// FIXME: daten sind ins file copiert, feierabend
		setSuccess();
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incommingMessage(hyperocha.freenet.fcp.FCPConnection, java.util.Hashtable)
	 */
	public void incomingMessage(String id, Hashtable message) {
		System.out.println("CHK down MessageHandler: " + message);
	}
}
