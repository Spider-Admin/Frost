/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.util.IStorageObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author saces
 *
 */
public interface Resumable extends IStorageObject {
	
	public boolean resume(DataInputStream dis);
	public boolean suspend(DataOutputStream dos); 

}
