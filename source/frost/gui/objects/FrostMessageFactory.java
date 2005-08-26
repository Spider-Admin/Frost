/*
  FrostMessageFactory.java / Frost
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
package frost.gui.objects;

import java.io.File;
import java.util.logging.*;

import org.shiftone.cache.*;

import frost.messages.MessageCreationException;

/**
 * There is a single instance of the class <code>FrostmessageFactory</code>,
 * accessed through the static method {@link #getInstance()}.
 *
 * @pattern Singleton
 */
public class FrostMessageFactory {

	private static final String CACHE_NAME = "fifo";
	private static final int CACHE_SIZE = 500;	//TODO: Add this value to the preferences
	private static final int CACHE_TIMEOUT = Integer.MAX_VALUE;

	private static Logger logger = Logger.getLogger(FrostMessageFactory.class.getName());

	private Cache cache = null;

	/**
	 * The unique instance of this class.
	 */
	private static FrostMessageFactory instance = null;

	/**
	 * Prevent instances of this class from being created.
	 */
	private FrostMessageFactory() {
		try {
			cache =
				new CacheConfiguration().createConfiguredCache(
					CACHE_NAME,
					CACHE_TIMEOUT,
					CACHE_SIZE);
		} catch (Exception exception) {
			logger.log(
				Level.SEVERE,
				"Error while creating the object cache. Object caching will not be used.",
				exception);
		}
	}

	/**
	 * Return the unique instance of this class.
	 *
	 * @return the unique instance of this class
	 */
	public static FrostMessageFactory getInstance() {
		if (instance == null) {
			instance = new FrostMessageFactory();
		}
		return instance;
	}

	/**
	 * @param messageFile
	 * @return
	 * @throws MessageCreationException
	 */
	public static FrostMessageObject createFrostMessageObject(File messageFile) throws MessageCreationException {
		return getInstance().innerCreateFrostMessageObject(messageFile);
	}

	/**
	 * @param messageFile
	 * @return
	 * @throws MessageCreationException
	 */
	private FrostMessageObject innerCreateFrostMessageObject(File messageFile) throws MessageCreationException {
		if (cache != null) {
			Object messageObject = cache.getObject(messageFile);
			if (messageObject != null) {
				return (FrostMessageObject) messageObject;
			} else {
				messageObject = new FrostMessageObject(messageFile);
				cache.addObject(messageFile, messageObject);
				return (FrostMessageObject) messageObject;
			}
		} else {
			return new FrostMessageObject(messageFile);
		}
	}

}
