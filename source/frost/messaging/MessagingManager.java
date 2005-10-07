/*
 MessagingManager.java / Frost
 Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package frost.messaging;

import java.util.logging.*;

import frost.*;

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
