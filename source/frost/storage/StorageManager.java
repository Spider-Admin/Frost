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

    private static final Logger logger = Logger.getLogger(StorageManager.class.getName());

    private final Language language;
    private final FrostEventDispatcher listener;

    private final ShutdownThread shutdownThread = new ShutdownThread();
    private final AutoTask autoTask = new AutoTask();

    private Vector<AutoSavable> autoSavables;
    private Vector<ExitSavable> exitSavables;

	private class AutoTask extends TimerTask {
		public AutoTask() {
			super();
		}
		@Override
        public void run() {
			if (autoSavables != null) {
			    for( final AutoSavable savable : autoSavables ) {
					try {
						savable.autoSave();
					} catch (final StorageException se) {
						logger.log(Level.SEVERE, "Error while saving a resource inside the timer.", se);
						final StorageErrorEvent errorEvent = new StorageErrorEvent(language.getString("Saver.AutoTask.message"));
						errorEvent.setException(se);
						listener.dispatchEvent(errorEvent);
					}
				}
                // autosave consumes some memory, clean up now
                System.gc();
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
		@Override
        public void run() {
			logger.info("Saving settings ...");

			if (exitSavables != null) {
                for( final ExitSavable savable : exitSavables ) {
					try {
						savable.exitSave();
					} catch (final StorageException se) {
						logger.log(Level.SEVERE, "Error while saving a resource inside the shutdown hook.", se);
					}
				}
			}
            Frost.releaseLockFile();
			logger.info("Bye!");
		}
	}

	/**
	 * @param frostSettings
	 * @param parentFrame
	 */
	public StorageManager(final SettingsClass frostSettings, final FrostEventDispatcher listener) {
		this.language = Language.getInstance();
		this.listener = listener;
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		final int autoSaveIntervalMinutes = frostSettings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL);
		schedule(
			autoTask,
			autoSaveIntervalMinutes * 60L * 1000L,
			autoSaveIntervalMinutes * 60L * 1000L);
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
	public synchronized void addAutoSavable(final AutoSavable autoSavable) {
		if (autoSavable == null) {
			return;
		}
		if (autoSavables == null) {
			autoSavables = new Vector<AutoSavable>();
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
	public synchronized void addExitSavable(final ExitSavable exitSavable) {
		if (exitSavable == null) {
			return;
		}
		if (exitSavables == null) {
			exitSavables = new Vector<ExitSavable>();
		}
		exitSavables.addElement(exitSavable);
	}
}
