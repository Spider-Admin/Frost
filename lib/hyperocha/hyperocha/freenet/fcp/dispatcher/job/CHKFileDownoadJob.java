/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.FreenetKeyType;
import hyperocha.freenet.fcp.IIncommingData;
import hyperocha.freenet.fcp.Persistance;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * download a file the best way 
 * includes black magic ritual and eating childs for finding the best way
 */
public class CHKFileDownoadJob extends Job {
	
	private FreenetKey keyToDownload;
	private File targetFile;
	private FileOutputStream os;
	
	protected CHKFileDownoadJob(int requirednetworktype, String id, FreenetKey key, File target) {
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
	public void runFCP2(Dispatcher dispatcher) {
		
		//System.out.println("CHK@Down 01");
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Error("Hicks");
		}
		//FCPConnection conn = dispatcher.getDefaultFCPConnection(getRequiredNetworkType());
		FCPConnection conn = dispatcher.getDefaultFCPConnection(getRequiredNetworkType());
		//System.out.println("CHK@Down 02");
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
		
		
		System.out.println("CHK@Down 03");
		conn.start(cmd);
		
		//conn.startMonitor(this);
		System.out.println("CHK@Down gestartet: " + cmd);
		
		waitFine();

		//System.out.println("Bimmi ende: " + result);

		throw (new Error("jkn"));
	}
}
