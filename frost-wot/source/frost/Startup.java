/*
 Startup.java / Frost
 Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

import java.io.*;
import java.net.*;
import java.util.logging.*;

import frost.util.*;

/**
 * Does some things that have to be done when starting Frost.
 */
public class Startup {
    private static final Logger logger = Logger.getLogger(Startup.class.getName());

    /**
     * The Main method, check if allowed to run
     * and starts the other startup work.
     */
    public static void startupCheck(SettingsClass settings) {
        checkDirectories(settings);
        copyFiles();
        cleanTempDir(settings);
    }
    
    // Copy some files from the jar file, if they don't exist
    private static void copyFiles() {
        String fileSeparator = System.getProperty("file.separator");

        try {
            boolean copyResource = false;
            File tray1file = new File("exec" + fileSeparator + "JSysTray.dll");
            if( !tray1file.isFile() ) {
                copyResource = true;
            } else {
                // check if size of existing dll file is different. If yes extract new version from jar.
                URL url = MainFrame.class.getResource("/data/JSysTray.dll");
                URLConnection urlConn = url.openConnection();
                long len = urlConn.getContentLength();
                if( len != tray1file.length() ) {
                    tray1file.delete();
                    copyResource = true;
                }
            }
            if( copyResource ) {
                FileAccess.copyFromResource("/data/JSysTray.dll", tray1file);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void checkDirectories(SettingsClass settings) {
        File downloadDirectory = new File(settings.getValue(SettingsClass.DIR_DOWNLOAD));
        if( !downloadDirectory.isDirectory() ) {
            logger.warning("Creating download directory");
            downloadDirectory.mkdirs();
        }

        File execDirectory = new File("exec");
        if( !execDirectory.isDirectory() ) {
            logger.warning("Creating exec directory");
            execDirectory.mkdirs();
        }

        File tempDirectory = new File(settings.getValue(SettingsClass.DIR_TEMP));
        if( !tempDirectory.isDirectory() ) {
            logger.warning("Creating temp directory");
            tempDirectory.mkdirs();
        }
    }

    private static void cleanTempDir(SettingsClass settings) {
        File[] entries = new File(settings.getValue(SettingsClass.DIR_TEMP)).listFiles();
        for( int i = 0; i < entries.length; i++ ) {
            File entry = entries[i];
            if( entry.isDirectory() == false ) {
                entry.delete();
            }
        }
    }
}
