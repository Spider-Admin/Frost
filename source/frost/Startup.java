/*
  TOF.java / Frost
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

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;

import frost.threads.*;

/**
 * Does some things that have to be done when starting Frost
 */
public class Startup
{
    /**
     * The Main method
     * @param parentFrame needed for warning messages
     */
    public static void startupCheck()
    {
        checkDirectories();
        copyFiles();
        deleteLockFiles();
    }

    // Copy some files from the jar file, if they don't exists
    private static void copyFiles()
    {
        String fileSeparator = System.getProperty("file.separator");

        try {
            File execfile = new File("exec.bat");
            if( !execfile.isFile() )
                mixed.copyFromResource("/data/exec.bat", execfile);
        }
        catch( IOException e ) { ; }

        try {
            File tray1file = new File("exec" + fileSeparator + "SystemTray.exe");
            if( !tray1file.isFile() )
                mixed.copyFromResource("/data/SystemTray.exe", tray1file);
        }
        catch( IOException e ) { ; }

        try {
            File tray2file = new File("exec" + fileSeparator + "SystemTrayHide.exe");
            if( !tray2file.isFile() )
                mixed.copyFromResource("/data/SystemTrayHide.exe", tray2file);
        }
        catch( IOException e ) { ; }

        try {
            File tray3file = new File("exec" + fileSeparator + "SystemTrayKill.exe");
            if( !tray3file.isFile() )
                mixed.copyFromResource("/data/SystemTrayKill.exe", tray3file);
        }
        catch( IOException e ) { ; }
    }

    private static void checkDirectories()
    {
        File downloadDirectory = new File(frame1.frostSettings.getValue("downloadDirectory"));
        if( !downloadDirectory.isDirectory() )
        {
            System.out.println("Creating download directory");
            downloadDirectory.mkdir();
        }

        File keypoolDirectory = new File(frame1.keypool);
        if( !keypoolDirectory.isDirectory() )
        {
            System.out.println("Creating keypool directory");
            keypoolDirectory.mkdir();
        }

        File execDirectory = new File("exec");
        if( !execDirectory.isDirectory() )
        {
            System.out.println("Creating exec directory");
            execDirectory.mkdir();
        }
    }

    private static void deleteLockFiles()
    {
        Vector entries = FileAccess.getAllEntries(new File(frame1.frostSettings.getValue("keypool.dir")),
                                                  ".lock");

        for( int i = 0; i < entries.size(); i++ )
        {
            // delete lock files
            ((File)entries.get( i )).delete();
        }
    }
}
