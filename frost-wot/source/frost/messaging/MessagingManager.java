/*
 * Created on 26-ene-2005
 * 
 */
package frost.messaging;

import frost.SettingsClass;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessagingManager {
	
	private SettingsClass settings;
	
	private MessageHashes messageHashes;
	
	/**
	 * 
	 */
	public MessagingManager(SettingsClass settings) {
		super();
		this.settings = settings;
	}
	
	/**
	 * 
	 */
	public void initialize() throws StorageException {
		getMessageHashes().initialize();
	}

	/**
	 * @return
	 */
	public MessageHashes getMessageHashes() {
		if (messageHashes == null) {
			messageHashes = new MessageHashes();	
		}
		return messageHashes;
	}
}
