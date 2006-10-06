/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.Network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * insert a file the best way 
 * includes black magic ritual and making coffe for finding the best way
 *
 */
public class CHKFileInsertJob extends Job {

	/**
	 * 
	 */
	public CHKFileInsertJob() {
		super(Network.FCP2, null);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#doPrepare()
	 */
	public boolean doPrepare() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#panic()
	 */
	public void panic() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#resume()
	 */
	public void resume() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#start()
	 */
	public void start() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.job.Job#suspend()
	 */
	public void suspend() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hyperocha.util.IStorageObject#loadData(java.io.DataInputStream)
	 */
	public boolean loadData(DataInputStream dis) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see hyperocha.util.IStorageObject#storeData(java.io.DataOutputStream)
	 */
	public boolean storeData(DataOutputStream dos) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void doRun() {
		// TODO Auto-generated method stub
		
	}

}
