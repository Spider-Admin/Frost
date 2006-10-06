/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

/**
 * @author saces
 *
 */
public class KSKMessageUploadJob extends Job {

	protected KSKMessageUploadJob(int requirednetworktype, String id) {
		super(requirednetworktype, id);
	}

	public boolean doPrepare() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isKeyCollision() {
		// TODO Auto-generated method stub
		if (true) { throw new Error(); }
		return false;
	}

}
