/*
 * Created on 26-ene-2005
 * 
 */
package frost.messaging;

import java.util.logging.*;

import frost.SettingsClass;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessagingManager {
	
    private static Logger logger = Logger.getLogger(MessagingManager.class.getName());

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
	public void initialize() {
        try {
            getMessageHashes().initialize();
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception when loading hashes, continuing", t);
            messageHashes = new MessageHashes();
        }
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
