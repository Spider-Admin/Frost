/*
 DownloadFilesDatabaseTable.java / Frost
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
 * Stores manually added download files
 */
public class DownloadFilesDatabaseTable {

    public static List<PerstFrostDownloadItem> migrateDownloadFiles() throws SQLException {

        List<PerstFrostDownloadItem> downloadItems = new LinkedList<PerstFrostDownloadItem>();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepareStatement(
                "SELECT " +
                "name,targetpath,size,fnkey,enabled,state,downloadaddedtime,downloadstartedtime,downloadfinishedtime,"+
                "retries,lastdownloadstoptime,gqid,filelistfilesha FROM DOWNLOADFILES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix=1;
            String filename = rs.getString(ix++);
            String targetPath = rs.getString(ix++);
            long size = rs.getLong(ix++);
            String key = rs.getString(ix++);
            boolean enabledownload = rs.getBoolean(ix++);
            int state = rs.getInt(ix++);
            long downloadAddedTime = rs.getLong(ix++);
            long downloadStartedTime = rs.getLong(ix++);
            long downloadFinishedTime = rs.getLong(ix++);
            int retries = rs.getInt(ix++);
            long lastDownloadStopTime = rs.getLong(ix++);
            String gqId = rs.getString(ix++);
            String sharedFileSha = rs.getString(ix++);
            
            PerstFrostDownloadItem pi = new PerstFrostDownloadItem();
            pi.fileName = filename;
            pi.targetPath = targetPath;
            pi.fileSize = size;
            pi.key = key;
            pi.enabled = enabledownload;
            pi.state = state;
            pi.downloadAddedTime = downloadAddedTime;
            pi.downloadStartedTime = downloadStartedTime;
            pi.downloadFinishedTime = downloadFinishedTime;
            pi.retries = retries;
            pi.lastDownloadStopTime = lastDownloadStopTime;
            pi.gqIdentifier = gqId;
            pi.fileListFileSha = sharedFileSha;
            
            downloadItems.add(pi);
        }
        rs.close();
        ps.close();

        return downloadItems;
    }
}
