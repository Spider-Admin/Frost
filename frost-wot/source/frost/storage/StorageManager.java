/*
 StorageManager.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage;

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.events.*;
import frost.util.gui.translation.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class StorageManager extends Timer {

	private class AutoTask extends TimerTask {
		public AutoTask() {
			super();
		}
		public void run() {
			if (autoSavables != null) {
				Enumeration enumeration = autoSavables.elements();
				while (enumeration.hasMoreElements()) {
					Savable savable = (Savable) enumeration.nextElement();
					try {
						savable.save();
					} catch (StorageException se) {
						logger.log(Level.SEVERE, "Error while saving a resource inside the timer.", se);
						StorageErrorEvent errorEvent = new StorageErrorEvent(language.getString("Saver.AutoTask.message"));
						errorEvent.setException(se);
						listener.dispatchEvent(errorEvent);
					}
				}
			}
		}
	}

    private class ShutdownThread extends Thread {
		public ShutdownThread() {
            super();
		}

		/**
		 * Called by shutdown hook.
		 */
		public void run() {
			logger.info("Saving settings ...");
            
			if (exitSavables != null) {
				Iterator it = exitSavables.iterator();
				while (it.hasNext()) {
					Savable savable = (Savable) it.next();
					try {
						savable.save();
					} catch (StorageException se) {
						logger.log(Level.SEVERE, "Error while saving a resource inside the shutdown hook.", se);
					}
				}
			}
//			FileAccess.cleanKeypool(MainFrame.keypool);
			logger.info("Bye!");
		}
	}
	
	private static Logger logger = Logger.getLogger(StorageManager.class.getName());

	private Language language;
	private FrostEventDispatcher listener;
	
	private ShutdownThread shutdownThread = new ShutdownThread();
	private AutoTask autoTask = new AutoTask();
	
	private Vector autoSavables;
	private Vector exitSavables;
    
	/**
	 * @param frostSettings
	 * @param parentFrame
	 */
	public StorageManager(SettingsClass frostSettings, FrostEventDispatcher listener) {
		this.language = Language.getInstance();
		this.listener = listener;
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		int autoSaveIntervalMinutes = frostSettings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL);
		schedule(
			autoTask,
			autoSaveIntervalMinutes * 60 * 1000,
			autoSaveIntervalMinutes * 60 * 1000);
	}

	/**
	 * Adds a Savable to the autoSavables list. 
	 * <p>
	 * If autoSavable is null, no exception is thrown and no action is performed.
	 *
	 * @param    autoSavable  the Savable to be added
	 *
	 * @see #removeAutoSavable 
	 */
	public synchronized void addAutoSavable(Savable autoSavable) {
		if (autoSavable == null) {
			return;
		}
		if (autoSavables == null) {
			autoSavables = new Vector();
		}
		autoSavables.addElement(autoSavable);
	}

	/**
	 * Adds a Savable to the exitSavables list. 
	 * <p>
	 * If exitSavable is null, no exception is thrown and no action is performed.
	 *
	 * @param    exitSavable  the Savable to be added
	 *
	 * @see #removeExitSavable
	 */
	public synchronized void addExitSavable(Savable exitSavable) {
		if (exitSavable == null) {
			return;
		}
		if (exitSavables == null) {
			exitSavables = new Vector();
		}
		exitSavables.addElement(exitSavable);
	}
}
