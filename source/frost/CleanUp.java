/*
 CleanUp.java / Frost
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

package frost;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Clean up the keypool.
 * 
 * Deletes all files with a LastModifiedDate that is before maxMessageDisplay.
 * Deletes all empty directories.
 */
public class CleanUp {

    private static Logger logger = Logger.getLogger(CleanUp.class.getName());

    /**
     * Cleans the keypool during runtime of Frost.
     * Deletes all expired files in keypool.
     */
    public static void deleteExpiredFiles(File keypoolFolder) {
        
        int daysOld = Core.frostSettings.getIntValue("messageExpireDays") + 1;
        long expiration = new Date().getTime() - (daysOld * 24 * 60 * 60 * 1000);
        
        recursDir(keypoolFolder, expiration);
    }
    
    /**
     * MUST run only during startup of Frost.
     * Removes all empty date directories in a board directory. 
     */
    public static void deleteEmptyBoardDateDirs(File keypool) {
        File[] boardDirs = keypool.listFiles();
        if( boardDirs == null || boardDirs.length == 0 ) {
            return;
        }
        for(int x=0; x < boardDirs.length; x++) {
            if( boardDirs[x].isFile() ) {
                continue;
            }
            File[] dateDirs = boardDirs[x].listFiles();
            if( dateDirs == null || dateDirs.length == 0 ) {
                continue;
            }
            for(int y=0; y < dateDirs.length; y++) {
                if( dateDirs[y].isFile() ) {
                    continue;
                }
                String[] filesList = dateDirs[y].list();
                if( filesList == null ) {
                    continue;
                }
                if( filesList.length == 0 ) {
                    // empty directory
                    dateDirs[y].delete();
                }
            }
        }
    }

    /**
     * Deletes all expired FILES.
     * Must not delete directories (could be a target for a running download,...). 
     * @param dirItem  Folder for recursion
     */
    private static void recursDir(File dirItem, long expiration) {
        
        if( dirItem.isDirectory() ) {
            
            File[] list = dirItem.listFiles();
            if( list == null ) {
                return;
            }
            for( int i = 0; i < list.length; i++ ) {
                File f = list[i];
                recursDir( f, expiration );
            }
        } else {
            // its a file
            if( dirItem.lastModified() < expiration ) {
                dirItem.delete();
            }
        }
    }
}
