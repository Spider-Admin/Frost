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


/**
 * @author saces
 *
 */
public class KSKMessageDownloadJob extends Job {

	private class DataHandler implements IIncommingData {

		public void incommingData(FCPConnection conn, Hashtable result) {
			long size = Long.parseLong((String)(result.get("DataLength"))); {
			System.out.println("DataHandler: " + result);
			conn.copyFrom(size, os);
		}
		
	}}

	
	private FreenetKey keyToDownload;
	private File targetFile;
	private FileOutputStream os;
	
	protected KSKMessageDownloadJob(int requirednetworktype, String id, FreenetKey key, File dest) {
		super(requirednetworktype, id);
		keyToDownload = key;
		targetFile = dest;
	}


	
	
	private DataHandler dataHandler = new DataHandler();



	public boolean doPrepare() {
		if (!(keyToDownload.isFreenetKeyType(FreenetKeyType.KSK))) {
			return false;
		}
		// TODO 
		//is targetFile a valid file?
		return true;
	}

	public void runFCP2(Dispatcher dispatcher) {
		
		
		FCPConnection conn = dispatcher.getFCPConnection(getRequiredNetworkType());
		
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
		
		//boolean repeat = true;
		Hashtable result = null;
		try { 
			os = new FileOutputStream(targetFile);
			//System.out.println("starte: " + cmd);
			conn.start(cmd);
			//System.out.println("started io" + conn.isIOValid());
			//System.out.println("started" + conn.isValid());
			
			while (true) {
				result = conn.readMessage(dataHandler);
				//System.out.println("Bimmi: " + result);
				
				if (("GetFailed").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
					setError((String)result.get("CodeDescription"));
					break;
				}
				
				if (("AllData").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
					break;
				}
			}
			
//			if (("PutSuccessful").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
//				conn.close();
//				return; // the only one case for return ok.
//			}
			//System.out.println("Result:" + result.get("judl-reason"));
		} catch (Exception ex) {
			ex.printStackTrace();
			setError(ex);
		} finally {
			if( targetFile.length() == 0 ) {
				targetFile.delete();
		    }
		}
		
		//System.out.println("Bimmi ende: " + result);
//		conn.close();
//		return false;

		
		
		
		//throw (new Error("hsabhbs"));
		//if dda 
		//    download file
		//else 
		//    download direct 
	}
}
