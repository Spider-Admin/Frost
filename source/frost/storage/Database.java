/*
  Database.java / Frost
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
package frost.storage;

import java.io.*;
import java.sql.*;

public class Database implements Savable {
    
    private Connection connection;
    private String databaseName;
    
    private static final String storeDirName = "store/"; 
    
    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch(ClassNotFoundException e) {
            System.out.println("ERROR: HSQLDB not found!");
        }
    }
    
    public Database(String name) throws SQLException {
        databaseName = name;
        
        File storeDir = new File(storeDirName);
        storeDir.mkdirs();
        
        connection = DriverManager.getConnection(
                "jdbc:hsqldb:file:"+storeDirName+databaseName,  // filenames
                "sa",                              // username
                "");                               // password
    }
    
    public void close() throws SQLException {
        Statement st = getConnection().createStatement();
        st.execute("SHUTDOWN");
        getConnection().close();
        connection = null;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public PreparedStatement prepare(String p) throws SQLException {
        return getConnection().prepareStatement(p);
    }
    
    public Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
    
    public int update(String q) throws SQLException {
        Statement s = getConnection().createStatement();
        int i = s.executeUpdate(q);
        s.close();
        return i;
    }
    
    public static void dump(ResultSet rs) throws SQLException {

        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column is indexed

                // with 1 not 0
                System.out.print(o.toString() + " ");
            }
            System.out.println(" ");
        }
    }

    public void save() throws StorageException {
        if( connection != null ) {
            try {
                close();
            } catch(SQLException ex) {
                StorageException ex2 = new StorageException("Error closing database "+databaseName);
                ex.initCause(ex);
                throw ex2;
            }
            System.out.println("INFO: database "+databaseName+" closed.");
        }
    }
}
