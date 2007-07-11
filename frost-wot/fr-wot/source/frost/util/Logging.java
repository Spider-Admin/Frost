/*
  Logging.java / Frost
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
package frost.util;

import java.beans.*;
import java.io.*;
import java.util.logging.*;

import frost.*;

public class Logging {

    private static final Logger logger = Logger.getLogger(Logging.class.getName());

    private class ShutdownHook extends Thread {

        public ShutdownHook() {
            super();
        }
        public void run() {
            frostSettings.removePropertyChangeListener(SettingsClass.LOG_TO_FILE, listener);
            frostSettings.removePropertyChangeListener(SettingsClass.LOG_FILE_SIZE_LIMIT, listener);
            frostSettings.removePropertyChangeListener(SettingsClass.LOG_LEVEL, listener);

            if (fileHandler != null) {
                rootLogger.removeHandler(fileHandler);
                fileHandler.close();
            }
        }
    }

    private class Listener implements PropertyChangeListener {
        public Listener() {
            super();
        }
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(SettingsClass.LOG_TO_FILE)) {
                logToFileSettingChanged();
            }
            if (evt.getPropertyName().equals(SettingsClass.LOG_FILE_SIZE_LIMIT)) {
                logFileSizeSettingChanged();
            }
            if (evt.getPropertyName().equals(SettingsClass.LOG_LEVEL)) {
                logLevelSettingChanged();
            }
        }
    }

    public static final String VERY_LOW = "Options.miscellaneous.logLevel.veryLow";   //Severe
    public static final String LOW = "Options.miscellaneous.logLevel.low";             //Warning
    public static final String MEDIUM = "Options.miscellaneous.logLevel.medium";       //Info
    public static final String HIGH = "Options.miscellaneous.logLevel.high";           //Finer
    public static final String VERY_HIGH = "Options.miscellaneous.logLevel.veryHigh"; //Finest
    public static final String DEFAULT = "Options.miscellaneous.logLevel.low";

    private static final String LOG_FILE_NAME = "frost%g.log";
    
    private static Logging instance;

    private final SettingsClass frostSettings;
    private final Listener listener = new Listener();
    private Logger rootLogger = null;
    private FileHandler fileHandler = null;
    private final SimpleFormatter simpleFormatter = new SimpleFormatter();
    
    private boolean logFcp2Messages = false;
    private boolean logFilebaseMessages = false;
    
    public Logging(SettingsClass frostSettings) {
        super();
        this.frostSettings = frostSettings;
        initialize();
        instance = this;
    }
    
    public static Logging inst() {
        return instance;
    }
    
    public boolean doLogFcp2Messages() {
        return logFcp2Messages;
    }
    public boolean doLogFilebaseMessages() {
        return logFilebaseMessages;
    }

    private void initialize() {
        LogManager logManager = LogManager.getLogManager();
        rootLogger = logManager.getLogger("");
        //We remove the console handler that is used by default
        if (!frostSettings.getBoolValue(SettingsClass.LOG_TO_CONSOLE)) {
        	Handler[] handlers = rootLogger.getHandlers();
        	for (int i = 0; i < handlers.length; i++) {
        		rootLogger.removeHandler(handlers[i]);
        	}
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        logToFileSettingChanged();

        frostSettings.addPropertyChangeListener(SettingsClass.LOG_TO_FILE, listener);
        frostSettings.addPropertyChangeListener(SettingsClass.LOG_FILE_SIZE_LIMIT, listener);
        frostSettings.addPropertyChangeListener(SettingsClass.LOG_LEVEL, listener);

        logFcp2Messages = frostSettings.getBoolValue(SettingsClass.LOG_FCP2_MESSAGES);
        logFilebaseMessages = frostSettings.getBoolValue(SettingsClass.LOG_FILEBASE_MESSAGES);
    }

    private void logLevelSettingChanged() {
        boolean valueFound = setLevel(frostSettings.getValue(SettingsClass.LOG_LEVEL));
        if (!valueFound) {
            //If the value in the settings was not a valid one, we set the default one
            setLevel(frostSettings.getDefaultValue(SettingsClass.LOG_LEVEL));
        }
    }

    private boolean setLevel(String level) {
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
    }

    private void logFileSizeSettingChanged() {
        // We only change the file size if logging is not disabled
        if (!Level.OFF.equals(rootLogger.getLevel())) {
            try {
                int fileSize = frostSettings.getIntValue(SettingsClass.LOG_FILE_SIZE_LIMIT);
                if (fileHandler != null) {
                    rootLogger.removeHandler(fileHandler);
                    fileHandler.close();
                    fileHandler = null;
                }
                fileHandler = new FileHandler(LOG_FILE_NAME, fileSize * 1024, 2, true);
                fileHandler.setEncoding("UTF-8");
                fileHandler.setFormatter(simpleFormatter);
                rootLogger.addHandler(fileHandler);
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "There was an error while initializing the logging system.", exception);
            }
        }
    }

    private void logToFileSettingChanged() {
        // We only change the level if logging is not disabled
        if (!Level.OFF.equals(rootLogger.getLevel())) {
            if (frostSettings.getBoolValue(SettingsClass.LOG_TO_FILE)) {
                rootLogger.setLevel(null);
                logLevelSettingChanged();
                logFileSizeSettingChanged();
            } else {
                rootLogger.setLevel(Level.OFF);
            }
        }
    }
}
