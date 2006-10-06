/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import java.io.File;

import hyperocha.freenet.fcp.FreenetKey;


/**
 * @author saces
 *
 */
public class GenerateCHKJob extends Job {

	/**
	 * 
	 */
	private GenerateCHKJob(int requirednetworktype, String id) {
		super(requirednetworktype, id);
	}
	
	public GenerateCHKJob(int requirednetworktype, String id, File file) {
		this(requirednetworktype, id);
	}
	
	public GenerateCHKJob(int requirednetworktype, CHKFileInsertJob fij, String id) {
		this(requirednetworktype, id);
	}

	
	public FreenetKey getKey() {
		return null;
	}

	public boolean doPrepare() {
		return false;
	}

}
