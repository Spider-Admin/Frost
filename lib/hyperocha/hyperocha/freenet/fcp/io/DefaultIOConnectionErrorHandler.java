/**
 * 
 */
package hyperocha.freenet.fcp.io;

import java.io.IOException;



/**
 * @author saces
 *
 */
public class DefaultIOConnectionErrorHandler implements
		IOConnectionErrorHandler {

	/* (non-Javadoc)
	 * @see judl.fcp.lib.FCPRawConnectionErrorHandler#onCantConnect(java.io.IOException)
	 */
	public void onCantConnect(IOException e) {
		// TODO Auto-generated method stub
		System.out.println("Cant connect - handler - juhu");
		e.printStackTrace();
	}

}
