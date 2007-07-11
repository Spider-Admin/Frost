/*
 UploadFilesDatabaseTable.java / Frost
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

import java.sql.*;
import java.util.*;

import frost.storage.database.applayer.*;
import frost.storage.perst.*;

/**
 * This table contains the currently uploaded files and their state.
 * Onyl manually added files are saved, the uploading shared files have their own state,
 * but both types of files appear in the GUI upload table.
 */
public class UploadFilesDatabaseTable {

    public static List<PerstFrostUploadItem> migrateUploadFiles() throws SQLException {

        List<PerstFrostUploadItem> uploadItems = new LinkedList<PerstFrostUploadItem>();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement(
                "SELECT path,size,fnkey,enabled,state," +
                "uploadaddedtime,uploadstartedtime,uploadfinishedtime,retries,lastuploadstoptime,gqid,sharedfilessha "+
                "FROM UPLOADFILES");

        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix = 1;
            String filepath = rs.getString(ix++);
            long filesize = rs.getLong(ix++);
            String key = rs.getString(ix++);
            boolean isEnabled = rs.getBoolean(ix++);
            int state = rs.getInt(ix++);
            long uploadAddedTime = rs.getLong(ix++);
            long uploadStartedTime = rs.getLong(ix++);
            long uploadFinishedTime = rs.getLong(ix++);
            int retries = rs.getInt(ix++);
            long lastUploadStopMillis = rs.getLong(ix++);
            String gqId = rs.getString(ix++);
            String sharedFilesSha = rs.getString(ix++);
            
            PerstFrostUploadItem pi = new PerstFrostUploadItem();
            pi.filePath = filepath;
            pi.fileSize = filesize;
            pi.chkKey = key;
            pi.enabled = isEnabled;
            pi.state = state;
            pi.uploadAddedMillis = uploadAddedTime;
            pi.uploadStartedMillis = uploadStartedTime;
            pi.uploadFinishedMillis = uploadFinishedTime;
            pi.retries = retries;
            pi.lastUploadStopTimeMillis = lastUploadStopMillis;
            pi.gqIdentifier = gqId;
            pi.sharedFilesSha = sharedFilesSha;

            uploadItems.add(pi);
        }
        rs.close();
        ps.close();

        return uploadItems;
    }
}
