/*
 * Created on Jan 28, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost;

import java.beans.*;
import java.io.IOException;
import java.util.logging.*;

/**
 * 
 */
public class Logging {
	
	/**
	 * 
	 */
	private class Listener implements PropertyChangeListener {
		/**
		 * 
		 */
		public Listener() {
			super();
		}
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(LOG_TO_FILE)) {
				logToFileSettingChanged();
			}
			if (evt.getPropertyName().equals(LOG_FILE_SIZE_LIMIT)) {
				logFileSizeSettingChanged();
			}
			if (evt.getPropertyName().equals(LOG_LEVEL)) {
				logLevelSettingChanged();
			}
		}
	}	
	
	public static final String VERY_LOW = "Very low";	//Severe
	public static final String LOW = "Low";				//Warning
	public static final String MEDIUM = "Medium";		//Info
	public static final String HIGH = "High";			//Finer
	public static final String VERY_HIGH = "Very high";	//Finest
	public static final String DEFAULT = "Low";
	
	public static final String LOG_TO_FILE = "logToFile";
	public static final String LOG_FILE_SIZE_LIMIT = "logFileSizeLimit";
	public static final String LOG_LEVEL = "logLevel";
	private static final String LOG_FILE_NAME = "frost.log";

	private SettingsClass frostSettings = null;
	private Listener listener = new Listener();
	private Logger rootLogger = null;
	private FileHandler fileHandler = null;

	/**
	 * 
	 */
	public Logging(SettingsClass frostSettings) {
		super();
		this.frostSettings = frostSettings;
		initialize();
	}
	
	/**
	 * 
	 */
	private void initialize() {
		LogManager logManager = LogManager.getLogManager();
		rootLogger = logManager.getLogger("");

		if (frostSettings.getBoolValue("logToFile")) {
			logFileSizeSettingChanged();
			logLevelSettingChanged();
		}
		
		frostSettings.addPropertyChangeListener(LOG_TO_FILE, listener);
		frostSettings.addPropertyChangeListener(LOG_FILE_SIZE_LIMIT, listener);
		frostSettings.addPropertyChangeListener(LOG_LEVEL, listener);
	}
	
	/**
	 * 
	 */
	private void logLevelSettingChanged() {
		boolean valueFound = setLevel(frostSettings.getValue(LOG_LEVEL));
		if (!valueFound) {
			setLevel(frostSettings.getDefaultValue(LOG_LEVEL));
		}
	}
		
	/**
	 * @param string
	 * @return
	 */
	private boolean setLevel(String level) {
		if (!Level.OFF.equals(rootLogger.getLevel())) {
			if (level.equals(VERY_LOW)) {
				rootLogger.setLevel(Level.SEVERE);
				return true;
			}
			if (level.equals(LOW)) {
				rootLogger.setLevel(Level.WARNING);
				return true;
			}
			if (level.equals(MEDIUM)) {
				rootLogger.setLevel(Level.INFO);
				return true;
			}
			if (level.equals(HIGH)) {
				rootLogger.setLevel(Level.FINER);
				return true;
			}
			if (level.equals(VERY_HIGH)) {
				rootLogger.setLevel(Level.FINEST);
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 */
	private void logFileSizeSettingChanged() {
		try {
			int fileSize = frostSettings.getIntValue(LOG_FILE_SIZE_LIMIT);
			if (fileHandler != null) {
				rootLogger.removeHandler(fileHandler);
				fileHandler = null;	
			}
			fileHandler = new FileHandler(LOG_FILE_NAME, fileSize * 1024, 1, true);
			rootLogger.addHandler(fileHandler);
		} catch (IOException exception) {
			System.out.println("There was an error while initializing the logging system:\n");
			System.out.println(exception.getMessage());
			exception.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private void logToFileSettingChanged() {
		if (frostSettings.getBoolValue(LOG_TO_FILE)) {
			rootLogger.setLevel(null);
			logLevelSettingChanged();	
		} else {
			rootLogger.setLevel(Level.OFF);
		}			
	}
}
