/*
  AppLayerDatabase.java / Frost
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
package frost.util.migration.migrate1to2;

import java.io.*;
import java.sql.*;

public class AppLayerDatabase {
    
    private static AppLayerDatabase instance = null;
    
    private Connection connection;
    
    protected AppLayerDatabase() throws SQLException {
        
        File storeDir = new File("store/");
        storeDir.mkdirs();
        
        // ensure database is created
        String createUrl = "jdbc:mckoi:local://store/applayerdb.conf?create=true";
        String url = "jdbc:mckoi:local://store/applayerdb.conf";
        String username = "frost";
        String password = "tsorf";
        try {
          connection = DriverManager.getConnection(createUrl, username, password);
        } catch (SQLException e) {
            // TODO: check if there is another error than 'already existing'

            // create a non-create connection to work with. if this fails too, exception is thrown to caller
            connection = DriverManager.getConnection(url, username, password);
        }
    }
    
    static {
        try {
            Class.forName("com.mckoi.JDBCDriver");
        } catch(ClassNotFoundException e) {
            System.out.println("ERROR: McKOI DB not found!");
        }
    }
    
    public void close() throws SQLException {
        Statement st = getConnection().createStatement();
        st.execute("SHUTDOWN");
        getConnection().close();
        connection = null;
    }
    
    public static void destroy() {
        instance = null;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public PreparedStatement prepareStatement(String p) throws SQLException {
        return getConnection().prepareStatement(p);
    }
    
    public Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
    
//    public int update(String q) throws SQLException {
//        Statement s = getConnection().createStatement();
//        int i = s.executeUpdate(q);
//        s.close();
//        return i;
//    }
    
//    public static void dump(ResultSet rs) throws SQLException {
//
//        // the order of the rows in a cursor
//        // are implementation dependent unless you use the SQL ORDER statement
//        ResultSetMetaData meta   = rs.getMetaData();
//        int               colmax = meta.getColumnCount();
//        int               i;
//        Object            o = null;
//
//        // the result set is a cursor into the data.  You can only
//        // point to one row at a time
//        // assume we are pointing to BEFORE the first row
//        // rs.next() points to next row and returns true
//        // or false if there is no next row, which breaks the loop
//        for (; rs.next(); ) {
//            for (i = 0; i < colmax; ++i) {
//                o = rs.getObject(i + 1);    // Is SQL the first column is indexed
//
//                // with 1 not 0
//                System.out.print(o.toString() + " ");
//            }
//            System.out.println(" ");
//        }
//    }

//    public void save() throws StorageException {
//        if( connection != null ) {
//            try {
//                close();
//            } catch(SQLException ex) {
//                StorageException ex2 = new StorageException("Error closing database.");
//                ex.initCause(ex);
//                throw ex2;
//            }
//            System.out.println("INFO: database closed.");
//        }
//    }
    
    public static void initialize() throws SQLException {
        if( instance == null ) {
            instance = new AppLayerDatabase();
        } else {
            System.out.println("ERROR: AppLayerDatabase already initialized!");
        }
    }
    
//    private void compactDatabaseTables(List<AbstractDatabaseTable> lst) throws SQLException {
//        System.out.println("Compacting database tables...");
//        long beforeTime = System.currentTimeMillis();
//        
//        setAutoCommitOff();
//        
//        Statement stmt = instance.createStatement();
//        for( Iterator<AbstractDatabaseTable> i = lst.iterator(); i.hasNext(); ) {
//            AbstractDatabaseTable table = i.next();
//            try {
//                table.compact(stmt);
//            } catch (SQLException e) {
//                System.out.println("*** Exception during compact:");
//                e.printStackTrace();
//            }
//        }
//        stmt.close();
//        
//        commit();
//        
//        setAutoCommitOn();
//        
//        long afterTime = System.currentTimeMillis();
//        System.out.println("Finished compact of database tables, duration "+(afterTime - beforeTime)+" ms");
//    }

    public static AppLayerDatabase getInstance() {
        return instance;
    }
    
//    private void ensureTables(List<AbstractDatabaseTable> lst) {
//
//        for(Iterator<AbstractDatabaseTable> i=lst.iterator(); i.hasNext(); ) {
//            AbstractDatabaseTable t = i.next();
//            for(Iterator j=t.getTableDDL().iterator(); j.hasNext(); ) {
//                String tableDDL = (String)j.next();
//                try {
//                    update(tableDDL);
//                } catch(SQLException ex) {
//                    // table/index already exists?
//                    if( ex.getMessage().endsWith("already exists.") == false ) {
////                    if( ! (ex.getSQLState().equals("S0001") || ex.getSQLState().equals("S0011")) ) {
//                        System.out.println("Statement: "+tableDDL);
//                        ex.printStackTrace(); // another exception, show
//                    }
//                }
//            }
//        }
        // indexslots table (static)
//        List ddls = IndexSlotsDatabaseTable.getTableDDL();
//        for(Iterator i=ddls.iterator(); i.hasNext(); ) {
//            String tableDDL = (String)i.next();
//            try {
//                update(tableDDL);
//            } catch(SQLException ex) {
//                // table already exists
//            }
//        }
//        List gddls = GlobalIndexSlotsDatabaseTable.getTableDDL();
//        for(Iterator i=gddls.iterator(); i.hasNext(); ) {
//            String tableDDL = (String)i.next();
//            try {
//                update(tableDDL);
//            } catch(SQLException ex) {
//                // table already exists
//            }
//        }
//    }
    
    public void commit() throws SQLException {
        getConnection().commit();
    }
}
