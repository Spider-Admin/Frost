/*
 * Created on 07-ene-2005
 * 
 */
package frost.fileTransfer.download;

import java.util.EventListener;

/**
 * @author $Author$
 * @version $Revision$
 */
public interface DownloadTickerListener extends EventListener {

	/**
	 * This event is fired when the number of running threads changes.
	 */
	void threadCountChanged();
}
