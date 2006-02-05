/*
 Startup.java / Frost
 Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.util.logging.Logger;

/**
 * Does some things that have to be done when starting Frost
 */
public class Startup {
    private static Logger logger = Logger.getLogger(Startup.class.getName());

    /**
     * The Main method, check if allowed to run
     * and starts the other startup work.
     */
    public static void startupCheck(SettingsClass settings, String keypool) {
        checkDirectories(settings, keypool);
        copyFiles();
        cleanTempDir(settings);
        deleteObsoleteFiles();
    }

    // Copy some files from the jar file, if they don't exist
    private static void copyFiles() {
        String fileSeparator = System.getProperty("file.separator");

        try {
            File execfile = new File("exec.bat");
            if( !execfile.isFile() )
                Mixed.copyFromResource("/data/exec.bat", execfile);
        } catch (IOException e) {
            ;
        }

        try {
            File tray1file = new File("exec" + fileSeparator + "JSysTray.dll");
            if( !tray1file.isFile() )
                Mixed.copyFromResource("/data/JSysTray.dll", tray1file);
        } catch (IOException e) {
            ;
        }
    }

    private static void checkDirectories(SettingsClass settings, String keypool) {
        File downloadDirectory = new File(settings.getValue("downloadDirectory"));
        if( !downloadDirectory.isDirectory() ) {
            logger.warning("Creating download directory");
            downloadDirectory.mkdirs();
        }

        File keypoolDirectory = new File(keypool);
        if( !keypoolDirectory.isDirectory() ) {
            logger.warning("Creating keypool directory");
            keypoolDirectory.mkdirs();
        }

        File execDirectory = new File("exec");
        if( !execDirectory.isDirectory() ) {
            logger.warning("Creating exec directory");
            execDirectory.mkdirs();
        }

        File unsentDirectory = new File(settings.getValue("unsent.dir"));
        if( !unsentDirectory.isDirectory() ) {
            logger.warning("Creating unsent directory");
            unsentDirectory.mkdirs();
        }

        File sentDirectory = new File(settings.getValue("sent.dir"));
        if( !sentDirectory.isDirectory() ) {
            logger.warning("Creating sent directory");
            sentDirectory.mkdirs();
        }

        File tempDirectory = new File(settings.getValue("temp.dir"));
        if( !tempDirectory.isDirectory() ) {
            logger.warning("Creating temp directory");
            tempDirectory.mkdirs();
        }
    }

    private static void cleanTempDir(SettingsClass settings) {
        File[] entries = new File(settings.getValue("temp.dir")).listFiles();
        for( int i = 0; i < entries.length; i++ ) {
            File entry = entries[i];
            if( entry.isDirectory() == false ) {
                entry.delete();
            }
        }
    }

    /**
     * - delete all .key files in frost dir (not recursive)
     */
    private static void deleteObsoleteFiles() {
        /*       File[] entries = new File(".").listFiles();
         for( int i = 0; i < entries.length; i++ )
         {
         File entry = entries[i];
         if( entry.isDirectory() == false && entry.getName().endsWith(".key") )
         {
         entry.delete();
         }
         }
         */
    }
}
