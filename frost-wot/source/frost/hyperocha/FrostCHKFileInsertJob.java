/**
 * 
 */
package frost.hyperocha;

import java.io.File;

import frost.Core;
import hyperocha.freenet.fcp.dispatcher.job.Job;

/**
 * @author saces
 *
 */
public class FrostCHKFileInsertJob extends Job {

	/**
	 * @param requirednetworktype
	 */
	public FrostCHKFileInsertJob(File insertfile) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID());
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.Job#doPrepare()
	 */
	public boolean doPrepare() {
		// TODO test if file is ok
		return false;
	}

	public String getChkKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
