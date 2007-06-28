/*
  NewUploadFilesTable.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.migration.migrate0to1;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.database.applayer.*;
import frost.storage.perst.*;

/**
 * Access to a database table that holds the new upload files which
 * do not have a SHA yet. A thread gets the entries from this file
 * one by one and computes the SHA. Then the entries are stored into 
 * the SHAREDFILES database table and are deleted from this table.
 * 
 * This table is needed in case the user shuts down Frost until all
 * SHA checksums were deleted.
 */
public class NewUploadFilesDatabaseTable {

    private static final Logger logger = Logger.getLogger(NewUploadFilesDatabaseTable.class.getName());

    public static List<NewUploadFile> migrateNewUploadFiles() throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        LinkedList<NewUploadFile> newUploadFiles = new LinkedList<NewUploadFile>();

        PreparedStatement ps = db.prepareStatement("SELECT filepath,fromname FROM NEWUPLOADFILES");

        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            String filepath = rs.getString(1);
            String from = rs.getString(2);
            
            File f = new File(filepath);
            if (!f.isFile()) {
                logger.warning("File ("+filepath+") is missing. File removed.");
                continue;
            }

            NewUploadFile nuf = new NewUploadFile(f, from);
            newUploadFiles.add(nuf);
        }
        rs.close();
        ps.close();
        
        return newUploadFiles;
    }
}
