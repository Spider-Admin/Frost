/*
  SettingsClass.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>
  This file contributed by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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

package frost;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.*;
import frost.storage.Savable;

/**
 * Read settings from frost.ini and store them.
 */
public class SettingsClass implements Savable {
    private File settingsFile;
    private Hashtable settingsHash;
    private Hashtable defaults = null;
    private final String fs = System.getProperty("file.separator");
    private PropertyChangeSupport changeSupport = null;
    private Vector updaters = null;

    private static Logger logger = Logger.getLogger(SettingsClass.class.getName());

    public static final String AUTO_SAVE_INTERVAL = "autoSaveInterval";
    public static final String DISABLE_DOWNLOADS = "disableDownloads";
    public static final String DISABLE_REQUESTS = "disableRequests";
    public static final String DOWNLOADING_ACTIVATED = "downloadingActivated";
    public static final String LOG_FILE_SIZE_LIMIT = "logFileSizeLimit";
    public static final String LOG_LEVEL = "logLevel";
    public static final String LOG_TO_FILE = "logToFile";
    public static final String FILE_LIST_FONT_NAME = "fileListFontName";
    public static final String FILE_LIST_FONT_SIZE = "fileListFontSize";
    public static final String FILE_LIST_FONT_STYLE = "fileListFontStyle";
    public static final String MESSAGE_BODY_FONT_NAME = "messageBodyFontName";
    public static final String MESSAGE_BODY_FONT_SIZE = "messageBodyFontSize";
    public static final String MESSAGE_BODY_FONT_STYLE = "messageBodyFontStyle";
    public static final String MESSAGE_LIST_FONT_NAME = "messageListFontName";
    public static final String MESSAGE_LIST_FONT_SIZE = "messageListFontSize";
    public static final String MESSAGE_LIST_FONT_STYLE = "messageListFontStyle";
    public static final String RESTART_FAILED_UPLOADS = "restartFailedUploads";
    public static final String SEARCH_ALL_BOARDS = "searchAllBoards";
    public static final String SHOW_DELETED_MESSAGES = "showDeletedMessages";
    public static final String SILENTLY_RETRY_MESSAGES = "silentlyRetryMessages";
    public static final String RECEIVE_DUPLICATE_MESSAGES = "receiveDuplicateMessages";
    public static final String UPLOAD_MAX_RETRIES = "uploadMaxRetries";
    public static final String UPLOAD_RETRIES_WAIT_TIME = "uploadRetriesWaitTime";

    public static final String HIDE_MESSAGES_OBSERVE = "hideObserveMessages";
    public static final String HIDE_MESSAGES_CHECK = "hideCheckMessages";
    public static final String HIDE_MESSAGES_BAD = "hideBadMessages";
    public static final String HIDE_MESSAGES_UNSIGNED = "signedOnly";

    public static final String BLOCK_BOARDS_FROM_OBSERVE = "blockBoardsFromObserve";
    public static final String BLOCK_BOARDS_FROM_CHECK = "blockBoardsFromCheck";
    public static final String BLOCK_BOARDS_FROM_BAD = "blockBoardsFromBad";
    public static final String BLOCK_BOARDS_FROM_UNSIGNED = "blockBoardsFromUnsigned";

    //Constructors
    public SettingsClass() {
        settingsHash = new Hashtable();
        // the FIX config.dir
        settingsHash.put("config.dir", "config" + fs);
        String configFilename = "config" + fs + "frost.ini";
        settingsFile = new File(configFilename);
        loadDefaults();
        if (!readSettingsFile()) {
            writeSettingsFile();
        }
    }

    private String setSystemsFileSeparator(String path) {
        if (fs.equals("\\")) {
            path = path.replace('/', File.separatorChar);
        } else if (fs.equals("/")) {
            path = path.replace('\\', File.separatorChar);
        }

        // append fileseparator to end if needed
        if (path.endsWith(fs) == false) {
            path = path + fs;
        }
        return path;
    }

    public String getDefaultValue(String key) {
        String val = (String) defaults.get(key);
        if (val == null) {
            val = "";
        }
        return val;
    }

    public boolean readSettingsFile() {
        LineNumberReader settingsReader = null;
        String line;

        if (settingsFile.exists() == false) {
            // try to get old frost.ini
            File oldIni = new File("frost.ini");
            if (oldIni.exists() && oldIni.length() > 0) {
                oldIni.renameTo(settingsFile);
            }
        }

        try {
            settingsReader = new LineNumberReader(new FileReader(settingsFile));
        } catch (Exception e) {
            logger.warning(settingsFile.getName() + " does not exist, will create it");
            return false;
        }
        try {
            while ((line = settingsReader.readLine()) != null) {
                line = line.trim();
                if (line.length() != 0 && line.startsWith("#") == false) {
                    StringTokenizer strtok = new StringTokenizer(line, "=");
                    String key = "";
                    String value = "";
                    Object objValue = value;
                    if (strtok.countTokens() >= 2) {
                        key = strtok.nextToken().trim();
                        value = strtok.nextToken().trim();
                        // to allow '=' in values
                        while (strtok.hasMoreElements()) {
                            value += "=" + strtok.nextToken();
                        }
                        if (value.startsWith("type.color(") && value.endsWith(")")) {
                            // this is a color
                            String rgbPart = value.substring(11, value.length() - 1);
                            StringTokenizer strtok2 = new StringTokenizer(rgbPart, ",");

                            if (strtok2.countTokens() == 3) {
                                try {
                                    int red, green, blue;
                                    red = Integer.parseInt(strtok2.nextToken().trim());
                                    green = Integer.parseInt(strtok2.nextToken().trim());
                                    blue = Integer.parseInt(strtok2.nextToken().trim());
                                    Color c = new Color(red, green, blue);
                                    objValue = c;
                                } catch (Exception ex) {
                                    objValue = null;
                                }
                            } else {
                                objValue = null; // dont insert in settings, use default instead
                            }
                        }
                        // scan all path config values and set correct system file separator
                        else if (
                            key.equals("unsent.dir")
                                || key.equals("sent.dir")
                                || key.equals("temp.dir")
                                || key.equals("keypool.dir")
                                || key.equals("archive.dir")
                                || key.equals("downloadDirectory")
                                || key.equals("lastUsedDirectory")) {
                            value = setSystemsFileSeparator(value);
                            objValue = value;
                        } else {
                            // 'old' behaviour
                            objValue = value;
                        }
                        if (objValue != null) {
                            settingsHash.put(key, objValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in readSettingsFile()", e);
        }
        try {
            settingsReader.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in readSettingsFile()", e);
        }

        if (this.getValue("messageBase").equals("")) {
            this.setValue("messageBase", "news");
        }

        logger.info("Read user configuration");
        return true;
    }

    /**
     * @return
     */
    private boolean writeSettingsFile() {
        PrintWriter settingsWriter = null;
        try {
            settingsWriter = new PrintWriter(new FileWriter(settingsFile));
        } catch (IOException exception) {
            try {
                //Perhaps the problem is that the config dir doesn't exist? In that case, we create it and try again
                File configDir = new File("config");
                if (!configDir.isDirectory()) {
                    configDir.mkdirs(); // if the config dir doesn't exist, we create it
                }
                settingsWriter = new PrintWriter(new FileWriter(settingsFile));
            } catch (IOException exception2) {
                logger.log(Level.SEVERE, "Exception thrown in writeSettingsFile()", exception2);
                return false;
            }
        }

        TreeMap sortedSettings = new TreeMap(settingsHash); // sort the lines
        Iterator i = sortedSettings.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if (key.equals("config.dir")) {
                continue; // do not save the config dir, its unchangeable
            }

            String val = null;
            if (sortedSettings.get(key) instanceof Color) {
                Color c = (Color) sortedSettings.get(key);
                val = new StringBuffer()
                        .append("type.color(")
                        .append(c.getRed()).append(",")
                        .append(c.getGreen()).append(",")
                        .append(c.getBlue())
                        .append(")")
                        .toString();
            } else {
                val = sortedSettings.get(key).toString();
            }

            settingsWriter.println(key + "=" + val);
        }

        try {
            settingsWriter.close();
            logger.info("Wrote configuration");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in writeSettingsFile", e);
        }
        return false;
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     * <p>
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param    listener  the PropertyChangeListener to be added
     *
     * @see #removePropertyChangeListener
     * @see #getPropertyChangeListeners
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Adds a PropertyChangeListener to the listener list for a specific
     * property.
     * <p>
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param propertyName one of the property names listed above
     * @param listener the PropertyChangeListener to be added
     *
     * @see #removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners(java.lang.String)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public synchronized void addPropertyChangeListener(
        String propertyName,
        PropertyChangeListener listener) {

        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Adds a SettingsUpdater to the updaters list.
     * <p>
     * If updater is null, no exception is thrown and no action is performed.
     *
     * @param    updater  the SettingsUpdater to be added
     *
     * @see #removeUpdater
     */
    public synchronized void addUpdater(SettingsUpdater updater) {
        if (updater == null) {
            return;
        }
        if (updaters == null) {
            updaters = new Vector();
        }
        updaters.addElement(updater);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * <p>
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param listener the PropertyChangeListener to be removed
     *
     * @see #addPropertyChangeListener
     * @see #getPropertyChangeListeners
     * @see #removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null || changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list for a specific
     * property.
     * <p>
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param propertyName a valid property name
     * @param listener the PropertyChangeListener to be removed
     *
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners(java.lang.String)
     * @see #removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public synchronized void removePropertyChangeListener(
        String propertyName,
        PropertyChangeListener listener) {

        if (listener == null || changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a SettingsUpdater from the updaters list.
     * <p>
     * If updaters is null, no exception is thrown and no action is performed.
     *
     * @param updater the SettingsUpdater to be removed
     *
     * @see #addUpdater
     */
    public synchronized void removeUpdater(SettingsUpdater updater) {
        if (updater == null || updaters == null) {
            return;
        }
        updaters.removeElement(updater);
    }

    /**
     * Returns an array of all the property change listeners
     * registered on this component.
     *
     * @return all of this component's <code>PropertyChangeListener</code>s
     *         or an empty array if no property change
     *         listeners are currently registered
     *
     * @see      #addPropertyChangeListener
     * @see      #removePropertyChangeListener
     * @see      #getPropertyChangeListeners(java.lang.String)
     * @see      java.beans.PropertyChangeSupport#getPropertyChangeListeners
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }

    /**
     * Returns an array of all the listeners which have been associated
     * with the named property.
     *
     * @return all of the <code>PropertyChangeListeners</code> associated with
     *         the named property or an empty array if no listeners have
     *         been added
     *
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * Support for reporting bound property changes for Object properties.
     * This method can be called when a bound property has changed and it will
     * send the appropriate PropertyChangeEvent to any registered
     * PropertyChangeListeners.
     *
     * @param propertyName the property whose value has changed
     * @param oldValue the property's previous value
     * @param newValue the property's new value
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (changeSupport == null) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /* Get the values from the Hash
     * Functions will return null if nothing appropriate
     * is found or the settings are wrongly formatted or
     * any other conceivable exception.
     */
    public String getValue(String key) {
        return (String) settingsHash.get(key);
    }

    public Object getObjectValue(String key) {
        return settingsHash.get(key);
    }

    public String[] getArrayValue(String key) {
        String str = (String) settingsHash.get(key);
        if (str == null)
            return new String[0];
        StringTokenizer strtok = new StringTokenizer(str, ";");
        String[] returnStrArr = new String[strtok.countTokens()];

        for (int i = 0; strtok.hasMoreElements(); i++) {
            returnStrArr[i] = (String) strtok.nextToken();
        }
        return returnStrArr;
    }

    public boolean getBoolValue(String key) {
        String str = (String) settingsHash.get(key);
        if (str == null)
            return false;
        try {
            if (str.toLowerCase().equals("false")) {
                return false;
            }
            if (str.toLowerCase().equals("true")) {
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return getBoolValue(getDefaultValue(key));
    }

    public int getIntValue(String key) {
        String str = (String) settingsHash.get(key);
        if (str == null)
            return 0;
        int val = 0;
        try {
            val = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return getIntValue(getDefaultValue(key));
        } catch (Exception e) {
            return 0;
        }
        return val;
    }

    public float getFloatValue(String key) {
        float val = 0.0f;
        String str = (String) settingsHash.get(key);
        if (str == null)
            return val;
        try {
            val = Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return getFloatValue(getDefaultValue(key));
        } catch (Exception e) {
            return 0.0f;
        }
        return val;
    }

    public void setValue(String key, String value) {
        // for all dirs ensure correct separator chars and a separator checr at end of name
        if( key.endsWith(".dir") ) {
            value = setSystemsFileSeparator(value);
        }
        Object oldValue = settingsHash.get(key);
        settingsHash.put(key, value);
        // Report the change to any registered listeners.
        firePropertyChange(key, oldValue, value);
    }
    public void setValue(String key, Integer value) {
        setValue(key, String.valueOf(value));
    }
    public void setValue(String key, int value) {
        setValue(key, String.valueOf(value));
    }
    public void setValue(String key, Float value) {
        setValue(key, String.valueOf(value));
    }
    public void setValue(String key, float value) {
        setValue(key, String.valueOf(value));
    }
    public void setValue(String key, Boolean value) {
        setValue(key, String.valueOf(value));
    }
    public void setValue(String key, boolean value) {
        setValue(key, String.valueOf(value));
    }

    public void setObjectValue(String key, Object value) {
        Object oldValue = settingsHash.get(key);
        settingsHash.put(key, value);
        // Report the change to any registered listeners.
        firePropertyChange(key, oldValue, value);
    }

    /**
     * Contains all default values that are used if no value is found in .ini file.
     */
    public void loadDefaults() {
        defaults = new Hashtable();
        File fn = File.listRoots()[0];

        // DIRECTORIES
        defaults.put("keypool.dir", "keypool" + fs);
        defaults.put("unsent.dir", "localdata" + fs + "unsent" + fs);
        defaults.put("sent.dir", "localdata" + fs + "sent" + fs);
        defaults.put("temp.dir", "localdata" + fs + "temp" + fs);
        defaults.put("archive.dir", "archive" + fs);
        defaults.put("downloadDirectory", "downloads" + fs);
        defaults.put("lastUsedDirectory", "." + fs);

        defaults.put("mainframe.showSimpleTitle", "false");

        defaults.put("allowEvilBert", "false");
        defaults.put("altEdit", fn + "path" + fs + "to" + fs + "editor" + " %f");
        defaults.put("automaticUpdate", "true");
        defaults.put("automaticUpdate.concurrentBoardUpdates", "6");
        // no. of concurrent updating boards in auto update
        defaults.put("automaticUpdate.boardsMinimumUpdateInterval", "45");
        // time in min to wait between start of updates for 1 board
        defaults.put("boardUpdateVisualization", "true");
        defaults.put("doBoardBackoff", "false");
        defaults.put("spamTreshold", "5");
        defaults.put("sampleInterval", "5");
        defaults.put("blockMessage", "");
        defaults.put("blockMessageChecked", "false");
        defaults.put("blockMessageBody", "");
        defaults.put("blockMessageBodyChecked", "false");
        defaults.put("blockMessageBoard", "");
        defaults.put("blockMessageBoardChecked", "false");

        defaults.put(HIDE_MESSAGES_UNSIGNED, "false");
        defaults.put(HIDE_MESSAGES_BAD, "false");
        defaults.put(HIDE_MESSAGES_CHECK, "false");
        defaults.put(HIDE_MESSAGES_OBSERVE, "false");

        defaults.put(BLOCK_BOARDS_FROM_UNSIGNED, "false");
        defaults.put(BLOCK_BOARDS_FROM_BAD, "false");
        defaults.put(BLOCK_BOARDS_FROM_CHECK, "false");
        defaults.put(BLOCK_BOARDS_FROM_OBSERVE, "false");

        defaults.put("downloadThreads", "3");
        defaults.put(DOWNLOADING_ACTIVATED, "true");
        //        defaults.put("downloadMethodLeastHtl", "false");
        //        defaults.put("downloadMethodOneByOne", "true");
        defaults.put("downloadRestartFailedDownloads", "true");
        defaults.put("downloadEnableRequesting", "true");

        defaults.put("downloadRequestAfterTries", "5");
        defaults.put("downloadMaxRetries", "5");
        defaults.put("downloadWaittime", "5");

        defaults.put("downloadDecodeAfterEachSegment", "true");
        defaults.put("downloadTryAllSegments", "true");

        defaults.put(DISABLE_REQUESTS, "false");
        defaults.put(DISABLE_DOWNLOADS, "false");
        defaults.put("htlUpload", "21");
        defaults.put("keyDownloadHtl", "24");
        defaults.put("keyUploadHtl", "21");
        defaults.put("maxAge", "5");
        defaults.put("maxKeys", "500000");
        defaults.put("maxMessageDisplay", "15");
        defaults.put("maxMessageDownload", "5");
        defaults.put("messageBase", "news");

        defaults.put("showSystrayIcon", "true");
        defaults.put("removeFinishedDownloads", "false");
        defaults.put("reducedBlockCheck", "false");
        defaults.put(SEARCH_ALL_BOARDS, "true");
        defaults.put("maxSearchResults", "10000");
        defaults.put("splitfileDownloadThreads", "30");
        defaults.put("splitfileUploadThreads", "15");
        defaults.put("tofDownloadHtl", "23");
        defaults.put("tofTreeSelectedRow", "0");
        defaults.put("tofUploadHtl", "21");
        defaults.put("uploadThreads", "3");
        defaults.put("uploadBatchSize", "100");
        defaults.put("indexFileRedundancy", "1");
        defaults.put("uploadingActivated", "true");
        defaults.put("automaticIndexing", "true");
        defaults.put("shareDownloads", "true");
        defaults.put("signUploads", "true");
        defaults.put("helpFriends", "true");
        defaults.put("hideBadFiles", "true");
        defaults.put("hideAnonFiles", "false");
        defaults.put("useAltEdit", "false");
        defaults.put("userName", "Anonymous");
        defaults.put("audioExtension", ".mp3;.ogg;.wav;.mid;.mod;.flac;.sid");
        defaults.put("videoExtension", ".mpeg;.mpg;.avi;.divx;.asf;.wmv;.rm;.ogm;.mov");
        defaults.put("documentExtension", ".doc;.txt;.tex;.pdf;.dvi;.ps;.odt;.sxw;.sdw;.rtf;.pdb;.psw");
        defaults.put("executableExtension", ".exe;.vbs;.jar;.sh;.bat;.bin");
        defaults.put("archiveExtension", ".zip;.rar;.jar;.gz;.arj;.ace;.bz;.tar;.tgz;.tbz");
        defaults.put("imageExtension", ".jpeg;.jpg;.jfif;.gif;.png;.tif;.tiff;.bmp;.xpm");
        defaults.put(AUTO_SAVE_INTERVAL, "15");

        defaults.put("messageExpireDays", "30");
        defaults.put("messageExpirationMode", "KEEP"); // KEEP or ARCHIVE or DELETE, default KEEP

        defaults.put("boardUpdatingNonSelectedBackgroundColor", new Color(233, 233, 233)); // "type.color(233,233,233)"
        defaults.put("boardUpdatingSelectedBackgroundColor", new Color(137, 137, 191)); // "type.color(137,137,191)"

        defaults.put("skinsEnabled", "false");
        defaults.put("selectedSkin", "none");

        defaults.put("locale", "default");

        defaults.put("lastFrameWidth", "700");
        defaults.put("lastFrameHeight", "500");
        defaults.put("lastFramePosX", "50");
        defaults.put("lastFramePosY", "50");
        defaults.put("lastFrameMaximized", "false");

        defaults.put(MESSAGE_BODY_FONT_NAME, "Monospaced");
        defaults.put(MESSAGE_BODY_FONT_STYLE, new Integer(Font.PLAIN).toString());
        defaults.put(MESSAGE_BODY_FONT_SIZE, "12");
        defaults.put(MESSAGE_LIST_FONT_NAME, "SansSerif");
        defaults.put(MESSAGE_LIST_FONT_STYLE, new Integer(Font.PLAIN).toString());
        defaults.put(MESSAGE_LIST_FONT_SIZE, "11");
        defaults.put(FILE_LIST_FONT_NAME, "SansSerif");
        defaults.put(FILE_LIST_FONT_STYLE, new Integer(Font.PLAIN).toString());
        defaults.put(FILE_LIST_FONT_SIZE, "11");

        defaults.put("messageBodyAA", "false");

        defaults.put(LOG_TO_FILE, "true");
        defaults.put(LOG_LEVEL, Logging.DEFAULT);
        defaults.put(LOG_FILE_SIZE_LIMIT, "1000");

        defaults.put(SILENTLY_RETRY_MESSAGES, "false");
        defaults.put(SHOW_DELETED_MESSAGES, "false");
        defaults.put(RECEIVE_DUPLICATE_MESSAGES, "false");

        defaults.put(RESTART_FAILED_UPLOADS, "true");
        defaults.put(UPLOAD_MAX_RETRIES, "5");
        defaults.put(UPLOAD_RETRIES_WAIT_TIME, "5");

        defaults.put("oneTimeUpdate.convertSigs.didRun", "false");
        defaults.put("oneTimeUpdate.repairIdentities.didRun", "false");

        settingsHash.putAll(defaults);
    }

    /**
     * This method asks all of the updaters to update the settings values
     * they have knowledge about and saves all of the settings values to disk.
     *
     * (Not thread-safe with addUpdater/removeUpdater)
     */
    public void save() throws StorageException {
        if (updaters != null) {
            Enumeration enumeration = updaters.elements();
            while (enumeration.hasMoreElements()) {
                ((SettingsUpdater) enumeration.nextElement()).updateSettings();
            }
        }
        if (!writeSettingsFile()) {
            throw new StorageException("Error while saving the settings.");
        }
    }
}
