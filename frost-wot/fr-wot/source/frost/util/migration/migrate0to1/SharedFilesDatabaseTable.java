/*
 SharedFilesDatabaseTable.java / Frost
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
 * This table contains all our own shared files.
 * One file can be shared only by one local identity.
 */
public class SharedFilesDatabaseTable {
    
    public static List<PerstFrostSharedFileItem> migrateSharedFiles() throws SQLException {

        LinkedList<PerstFrostSharedFileItem> sfItems = new LinkedList<PerstFrostSharedFileItem>();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepareStatement(
                "SELECT "+
                  "path,size,fnkey,sha,owner,comment,rating,keywords,"+
                  "lastuploaded,uploadcount,reflastsent,requestlastreceived,requestsreceivedcount,lastmodified "+
                "FROM SHAREDFILES");

        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix = 1;
            String filepath = rs.getString(ix++);
            long filesize = rs.getLong(ix++);
            String key = rs.getString(ix++);
            
            String sha = rs.getString(ix++);
            String owner = rs.getString(ix++);
            String comment = rs.getString(ix++);
            int rating = rs.getInt(ix++);
            String keywords = rs.getString(ix++);
            long lastUploaded = rs.getLong(ix++);
            int uploadCount = rs.getInt(ix++);
            long refLastSent = rs.getLong(ix++);
            long requestLastReceived = rs.getLong(ix++);
            int requestsReceivedCount = rs.getInt(ix++);
            long lastModified = rs.getLong(ix++);

            PerstFrostSharedFileItem pi = new PerstFrostSharedFileItem();
            pi.filePath = filepath;
            pi.fileSize = filesize;
            pi.key = key;
            pi.sha = sha;
            pi.owner = owner;
            pi.comment = comment;
            pi.rating = rating;
            pi.keywords = keywords;
            pi.lastUploaded = lastUploaded;
            pi.uploadCount = uploadCount;
            pi.refLastSent = refLastSent;
            pi.requestLastReceived = requestLastReceived;
            pi.requestsReceived = requestsReceivedCount;
            pi.lastModified = lastModified;
            
            sfItems.add(pi);
        }
        rs.close();
        ps.close();

        return sfItems;
    }
}
