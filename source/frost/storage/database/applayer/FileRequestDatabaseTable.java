/*
 FileRequestDatabaseTable.java / Frost
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
package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.database.*;

/**
 * Tracks received and sent file requests.
 */
public class FileRequestDatabaseTable extends AbstractDatabaseTable {

    // FIXME: somehow track file requests (or only of file we currently request?), check if requests for our requested file
    //        are already sent, don't send new request then. don't send our requests too often (daily? track this!)

    private static Logger logger = Logger.getLogger(FileRequestDatabaseTable.class.getName());
    
    private final static String SQL_FILEREQUESTS_DDL =
        "CREATE TABLE FILEREQUESTS ("+
          "sha VARCHAR NOT NULL,"+ // checksum of requested file
          "lastreceived BIGINT,"+  // time when we received the last request for this sha
          "receivedcount INT,"+    // received requests count
          "lastsent BIGINT,"+      // time when we sent the last request for this file
          "sentcount INT,"+        // sent requests count
        "CONSTRAINT FILEREQUESTS_1 UNIQUE(sha) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_FILEREQUESTS_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE FILEREQUESTS");
        return true;
    }

}
