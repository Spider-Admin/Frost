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
import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.Persistance;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;
import hyperocha.freenet.fcp.utils.FCPTests;
import hyperocha.freenet.fcp.utils.FCPUtil;

/**
 * @author saces
 *
 */
public class UpdateNodePropertiesJob extends Job {
	
	FCPConnectionRunner conn = null;
	
	private String idStringDDA;
	private String idStringGQ;
	private boolean ddaDone = true; // default is on
	private boolean gqDone = true; // default is on
	
	private FCPNode node;

	/**
	 * @param requirednetworktype
	 * @param id
	 */
	public UpdateNodePropertiesJob(FCPNode aNode) {
		super(Network.FCP2, aNode.getID());
		node = aNode;
		idStringDDA = FCPUtil.getNewConnectionId(getJobID() + "-", "-DDA");
		idStringGQ = FCPUtil.getNewConnectionId(getJobID() + "-", "-GQ");
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#runFCP2(hyperocha.freenet.fcp.dispatcher.Dispatcher, boolean)
	 */
	public void runFCP2(Dispatcher dispatcher, boolean resume) {
		
		conn = new FCPConnectionRunner(node, "test-"+ getJobID(), this);
		conn.start();
		dispatcher.registerJob(idStringDDA, this);
		//dispatcher.registerJob(idStringGQ, this);
		
		File testFile = FCPTests.createTestFile();
		
		List cmd = new LinkedList();
		
		cmd.add("ClientPut");
		cmd.add("URI=CHK@");
		cmd.add("Identifier=" + idStringDDA); 
		cmd.add("Verbosity=0");
		cmd.add("MaxRetries=0");      // only one try, the node accepts the filename or net
		cmd.add("PriorityClass=0");   // today, please ;) 
		cmd.add("GetCHKOnly=true");   // calculate the chk from 1k (the default testfile)
		cmd.add("Global=false");
		cmd.add("Persistance=" + Persistance.CONNECTION);
		cmd.add("DontCompress=true");
		cmd.add("ClientToken=" + getClientToken()); 
		cmd.add("UploadFrom=disk");
		cmd.add("Filename=" + testFile.getAbsolutePath());
		cmd.add("EndMessage");

		
		conn.send(cmd);
		
		// TODO:
		// read the persistance state from config
		
		waitFine();
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incomingMessage(java.lang.String, java.util.Hashtable)
	 */
	public void incomingMessage(String id, Hashtable message) {
		if (id.equals(idStringDDA)) {
			if ("ProtocolError".equals(message.get(FCPConnection.MESSAGENAME))) {
				if (Integer.parseInt((String)(message.get("Code")))==9) {
					// File not found
					node.getNodeConfig().setDDA(false);
				}
				setSuccess();
				return;
			}
			
			if ("PutSuccessful".equals(message.get(FCPConnection.MESSAGENAME))) {
				node.getNodeConfig().setDDA(true);  // juhu
				setSuccess();
				return;
			}
			
			if ("PutFailed".equals(message.get(FCPConnection.MESSAGENAME))) {
				// hu, das sollte eigentlich nicht passieren.
				// don't change config
				setSuccess();
				return;
			}
			
			// all other ignored

		}
	}

}
