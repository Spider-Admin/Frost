/*
 * Created on 04-ene-2005
 * 
 */
package frost.fileTransfer.upload;

import java.util.EventListener;


/**
 * @author $Author$
 * @version $Revision$
 */
public interface UploadTickerListener extends EventListener {
	
	/**
	 * This event is fired when the number of uploading threads changes.
	 */
	void uploadingCountChanged();
	
	/**
	 * This event is fired when the number of generating threads changes.
	 */
	void generatingCountChanged();
}
