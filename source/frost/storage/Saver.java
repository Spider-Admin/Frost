/*
 * Created on Apr 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.storage;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.JFrame;

import frost.*;
import frost.util.gui.JDialogWithDetails;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class Saver extends Timer {

	/**
	 * 
	 */
	private class AutoTask extends TimerTask {

		/**
		 * 
		 */
		public AutoTask() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		public void run() {
			if (autoSavables != null) {
				Enumeration enumeration = autoSavables.elements();
				while (enumeration.hasMoreElements()) {
					Savable savable = (Savable) enumeration.nextElement();
					try {
						savable.save();
					} catch (StorageException se) {
						logger.log(Level.SEVERE,
								"Error while saving a resource inside the timer.", se);
						StringWriter stringWriter = new StringWriter();
						se.printStackTrace(new PrintWriter(stringWriter));
						JDialogWithDetails.showErrorDialog(parentFrame, 
														language.getString("Saver.AutoTask.title"),
														language.getString("Saver.AutoTask.message"),
														stringWriter.toString());
						System.exit(3);
					}
				}
			}
		}

	}
	/**
	 * 
	 */
	private class ShutdownThread extends Thread {

		/**
		 * 
		 */
		public ShutdownThread() {
		}

		/**
		 * Called by shutdown hook.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			logger.info("Saving settings ...");

			if (exitSavables != null) {
				Enumeration enumeration = exitSavables.elements();
				while (enumeration.hasMoreElements()) {
					Savable savable = (Savable) enumeration.nextElement();
					try {
						savable.save();
					} catch (StorageException se) {
						logger.log(Level.SEVERE,
								"Error while saving a resource inside the shutdown hook.", se);
					}
				}
			}
			FileAccess.cleanKeypool(MainFrame.keypool);

			logger.info("Bye!");
		}
	}
	
	private static Logger logger = Logger.getLogger(Saver.class.getName());

	private Language language;
	private JFrame parentFrame;
	
	private ShutdownThread shutdownThread = new ShutdownThread();
	private AutoTask autoTask = new AutoTask();
	
	private Vector autoSavables;
	private Vector exitSavables;

	/**
	 * @param frostSettings
	 * @param parentFrame
	 */
	public Saver(SettingsClass frostSettings, JFrame parentFrame) {
		super();
		this.language = Language.getInstance();
		this.parentFrame = parentFrame;
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
