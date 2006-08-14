/**
 * 
 */
package hyperocha.freenet.fcp.io;

import java.io.IOException;

/**
 * @author saces
 *
 */
public interface IOConnectionErrorHandler {
	
	//void onTimeOut();
	//void onConnectionLost();
	void onCantConnect(IOException e);

}
