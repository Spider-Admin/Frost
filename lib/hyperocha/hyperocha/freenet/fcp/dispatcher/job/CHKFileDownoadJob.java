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
 * download a file the best way 
 * includes black magic ritual and eating childs for finding the best way
 */
public class CHKFileDownoadJob extends Job {
	
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
	
	private DataHandler dataHandler = new DataHandler();
	

	
	protected CHKFileDownoadJob(int requirednetworktype, String id, FreenetKey key, File target) {
		super(requirednetworktype, id);
		keyToDownload = key;
		targetFile = target;
	}

	public boolean doPrepare() {
		if (!(keyToDownload.isFreenetKeyType(FreenetKeyType.CHK))) {
			return false;
		}
		// TODO: is targetFile a valid file?
		return true;
	}
	
	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher)
	 */
	public void runFCP2(Dispatcher dispatcher) {
		
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

		// TODO Auto-generated method stub
		throw (new Error("jkn"));
//		super.runFCP2(dispatcher);
	}

	//private class 


}
