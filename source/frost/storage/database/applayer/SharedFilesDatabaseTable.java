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
package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.database.*;

public class SharedFilesDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(SharedFilesDatabaseTable.class.getName());
    
    // FIXME: add comment, rating (fix), category (fix), keywords, language (fix?) (?)
    
    private final static String SQL_SHAREDFILES_DDL =
        "CREATE TABLE SHAREDFILES ("+
        
        "primkey BIGINT NOT NULL,"+
        "sha1 VARCHAR NOT NULL,"+
        "name VARCHAR NOT NULL,"+
        "path VARCHAR NOT NULL,"+   // complete path, with name
        "size BIGINT NOT NULL,"+
        "fnkey VARCHAR,"+           // if NULL file was not uploaded by us yet
        "lastuploaded DATE,"+       // date of last successful upload
        "uploadcount INT,"+         // number of uploads for this file so far
        "lastrequested DATE,"+      // date of last request (from any board)
        "requestcount INT,"+        // number of requests received for this file so far
        "state INT,"+
        "enabled BOOLEAN,"+         // is upload enabled?
        "laststopped TIMESTAMP NOT NULL,"+   // time of last start of upload
        "retries INT,"+             // number of upload tries, set to 0 on any successful upload
        "CONSTRAINT ulfiles_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT UPLOADFILES_1 UNIQUE(sha1) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_SHAREDFILES_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE SHAREDFILES");
        return true;
    }

}
